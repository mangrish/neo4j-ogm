/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices and license terms. Your use of the source code for these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 *
 */

package org.neo4j.ogm.entity.io;

import java.util.*;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.context.DirectedRelationship;
import org.neo4j.ogm.context.DirectedRelationshipForType;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determines how entities should be accessed in both reading and writing scenarios by looking up information from
 * {@link ClassInfo} in the following order.
 * <ol>
 * <li>Annotated Method (getter/setter)</li>
 * <li>Annotated Field</li>
 * <li>Plain Method (getter/setter)</li>
 * <li>Plain Field</li>
 * </ol>
 * The rationale is simply that we want annotations, whether on fields or on methods, to always take precedence, and we want to
 * use methods in preference to field access, because in many cases hydrating an object means more than just assigning values to
 * fields.
 *
 * @author Adam George
 * @author Luanne Misquitta
 */
public class EntityAccessManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntityAccessManager.class);

	//TODO make these LRU caches with configurable size
	private static Map<ClassInfo, Map<DirectedRelationship, RelationalReader>> relationalReaderCache = new HashMap<>();
	private static Map<ClassInfo, Map<DirectedRelationshipForType, RelationalWriter>> relationalWriterCache = new HashMap<>();
	private static Map<ClassInfo, Map<DirectedRelationshipForType, RelationalWriter>> iterableWriterCache = new HashMap<>();
	private static Map<ClassInfo, Map<DirectedRelationshipForType, RelationalReader>> iterableReaderCache = new HashMap<>();
	private static Map<ClassInfo, Map<Class, RelationalWriter>> relationshipEntityWriterCache = new HashMap<>();
	private static Map<ClassInfo, Map<String, EntityAccess>> propertyWriterCache = new HashMap<>();
	private static Map<ClassInfo, Map<String, PropertyReader>> propertyReaderCache = new HashMap<>();
	private static Map<ClassInfo, Collection<PropertyReader>> propertyReaders = new HashMap<>();
	private static Map<ClassInfo, PropertyReader> identityPropertyReaderCache = new HashMap<>();
	private static Map<ClassInfo, Collection<RelationalReader>> relationalReaders = new HashMap<>();

	private static final boolean STRICT_MODE = true; //strict mode for matching readers and writers, will only look for explicit annotations
	private static final boolean INFERRED_MODE = false; //inferred mode for matching readers and writers, will infer the relationship type from the getter/setter


	/**
	 * Used internally to hide differences in object construction from strategy algorithm.
	 */
	private interface AccessorFactory<T> {

		T makeFieldAccessor(FieldInfo fieldInfo);
	}

	public static EntityAccess getPropertyWriter(final ClassInfo classInfo, String propertyName) {
		if (!propertyWriterCache.containsKey(classInfo)) {
			propertyWriterCache.put(classInfo, new HashMap<String, EntityAccess>());
		}
		Map<String, EntityAccess> entityAccessMap = propertyWriterCache.get(classInfo);
		if (entityAccessMap.containsKey(propertyName)) {
			return propertyWriterCache.get(classInfo).get(propertyName);
		}

		EntityAccess propertyWriter = determinePropertyAccessor(classInfo, propertyName, new AccessorFactory<EntityAccess>() {

			@Override
			public EntityAccess makeFieldAccessor(FieldInfo fieldInfo) {
				return new FieldWriter(classInfo, fieldInfo);
			}
		});
		propertyWriterCache.get(classInfo).put(propertyName, propertyWriter);
		return propertyWriter;
	}

	public static PropertyReader getPropertyReader(final ClassInfo classInfo, String propertyName) {
		if (!propertyReaderCache.containsKey(classInfo)) {
			propertyReaderCache.put(classInfo, new HashMap<String, PropertyReader>());
		}
		if (propertyReaderCache.get(classInfo).containsKey(propertyName)) {
			return propertyReaderCache.get(classInfo).get(propertyName);
		}

		PropertyReader propertyReader = determinePropertyAccessor(classInfo, propertyName, new AccessorFactory<PropertyReader>() {

			@Override
			public PropertyReader makeFieldAccessor(FieldInfo fieldInfo) {
				return new FieldReader(classInfo, fieldInfo);
			}
		});
		propertyReaderCache.get(classInfo).put(propertyName, propertyReader);
		return propertyReader;
	}

	private static <T> T determinePropertyAccessor(ClassInfo classInfo, String propertyName, AccessorFactory<T> factory) {

		// fall back to the field if method cannot be found
		FieldInfo labelField = classInfo.labelFieldOrNull();
		if (labelField != null && labelField.getName().equals(propertyName)) {
			return factory.makeFieldAccessor(labelField);
		}
		FieldInfo fieldInfo = classInfo.propertyField(propertyName);
		if (fieldInfo != null) {
			return factory.makeFieldAccessor(fieldInfo);
		}
		return null;
	}

	public static RelationalWriter getRelationalWriter(ClassInfo classInfo, String relationshipType, String relationshipDirection, Object scalarValue) {
		if (!relationalWriterCache.containsKey(classInfo)) {
			relationalWriterCache.put(classInfo, new HashMap<DirectedRelationshipForType, RelationalWriter>());
		}
		DirectedRelationshipForType directedRelationship = new DirectedRelationshipForType(relationshipType, relationshipDirection, scalarValue.getClass());
		if (relationalWriterCache.get(classInfo).containsKey(directedRelationship)) {
			return relationalWriterCache.get(classInfo).get(directedRelationship);
		}

		// 2nd, try to find a scalar or vector field explicitly annotated as the neo4j relationship type and direction
		for (FieldInfo fieldInfo : classInfo.candidateRelationshipFields(relationshipType, relationshipDirection, STRICT_MODE)) {
			if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
				if (fieldInfo.isTypeOf(scalarValue.getClass()) ||
						fieldInfo.isParameterisedTypeOf(scalarValue.getClass()) ||
						fieldInfo.isArrayOf(scalarValue.getClass())) {
					FieldWriter fieldWriter = new FieldWriter(classInfo, fieldInfo);
					relationalWriterCache.get(classInfo).put(directedRelationship, fieldWriter);
					return fieldWriter;
				}
			}
		}

		//If the direction is INCOMING, then the annotation should have been present and we should have found a match already.
		//If it's outgoing, then proceed to find other matches
		if (!relationshipDirection.equals(Relationship.INCOMING)) {

			// 4th, try to find a scalar or vector field annotated as the neo4j relationship type and direction, allowing for implied relationships
			for (FieldInfo fieldInfo : classInfo.candidateRelationshipFields(relationshipType, relationshipDirection, INFERRED_MODE)) {
				if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
					if (fieldInfo.isTypeOf(scalarValue.getClass()) ||
							fieldInfo.isParameterisedTypeOf(scalarValue.getClass()) ||
							fieldInfo.isArrayOf(scalarValue.getClass())) {
						FieldWriter fieldWriter = new FieldWriter(classInfo, fieldInfo);
						relationalWriterCache.get(classInfo).put(directedRelationship, fieldWriter);
						return fieldWriter;
					}
				}
			}

			// 6th, try to find a "XYZ" field name where XYZ is derived from the relationship type
			for (FieldInfo fieldInfo : classInfo.candidateRelationshipFields(relationshipType, relationshipDirection, INFERRED_MODE)) {
				if (fieldInfo != null) {
					if (fieldInfo.isTypeOf(scalarValue.getClass()) ||
							fieldInfo.isParameterisedTypeOf(scalarValue.getClass()) ||
							fieldInfo.isArrayOf(scalarValue.getClass())) {
						FieldWriter fieldWriter = new FieldWriter(classInfo, fieldInfo);
						relationalWriterCache.get(classInfo).put(directedRelationship, fieldWriter);
						return fieldWriter;
					}
				}
			}

			// 8th, try to find a unique field that has the same type as the parameter
			List<FieldInfo> fieldInfos = classInfo.findFields(scalarValue.getClass());
			if (fieldInfos.size() == 1) {
				FieldInfo candidateFieldInfo = fieldInfos.iterator().next();
				if (!candidateFieldInfo.relationshipDirection(Relationship.UNDIRECTED).equals(Relationship.INCOMING)) {
					FieldWriter fieldWriter = new FieldWriter(classInfo, candidateFieldInfo);
					relationalWriterCache.get(classInfo).put(directedRelationship, fieldWriter);
					return fieldWriter;
				}
			}
		}
		relationalWriterCache.get(classInfo).put(directedRelationship, null);
		return null;
	}

	public static RelationalReader getRelationalReader(ClassInfo classInfo, String relationshipType, String relationshipDirection) {
		if (!relationalReaderCache.containsKey(classInfo)) {
			relationalReaderCache.put(classInfo, new HashMap<DirectedRelationship, RelationalReader>());
		}
		DirectedRelationship directedRelationship = new DirectedRelationship(relationshipType, relationshipDirection);
		if (relationalReaderCache.get(classInfo).containsKey(directedRelationship)) {
			return relationalReaderCache.get(classInfo).get(directedRelationship);
		}

		// 2nd, try to find a field explicitly annotated with the neo4j relationship type and direction
		FieldInfo fieldInfo = classInfo.relationshipField(relationshipType, relationshipDirection, STRICT_MODE);
		if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
			FieldReader fieldReader = new FieldReader(classInfo, fieldInfo);
			relationalReaderCache.get(classInfo).put(directedRelationship, fieldReader);
			return fieldReader;
		}

		//If the direction is INCOMING, then the annotation should have been present and we should have found a match already.
		//If it's outgoing, then proceed to find other matches
		if (!relationshipDirection.equals(Relationship.INCOMING)) {

			// 4th, try to find a field  annotated with the neo4j relationship type and direction, allowing for implied relationships
			fieldInfo = classInfo.relationshipField(relationshipType, relationshipDirection, INFERRED_MODE);
			if (fieldInfo != null && !fieldInfo.getAnnotations().isEmpty()) {
				FieldReader fieldReader = new FieldReader(classInfo, fieldInfo);
				relationalReaderCache.get(classInfo).put(directedRelationship, fieldReader);
				return fieldReader;
			}

			// 6th, try to find a "XYZ" field name where XYZ is derived from the relationship type
			if (fieldInfo != null) {
				FieldReader fieldReader = new FieldReader(classInfo, fieldInfo);
				relationalReaderCache.get(classInfo).put(directedRelationship, fieldReader);
				return fieldReader;
			}
		}
		relationalReaderCache.get(classInfo).put(directedRelationship, null);
		return null;
	}

	public static Collection<PropertyReader> getPropertyReaders(ClassInfo classInfo) {
		// do we care about "implicit" fields?  i.e., setX/getX with no matching X field
		if (propertyReaders.containsKey(classInfo)) {
			return propertyReaders.get(classInfo);
		}
		Collection<PropertyReader> readers = new ArrayList<>();
		for (FieldInfo fieldInfo : classInfo.propertyFields()) {
			readers.add(new FieldReader(classInfo, fieldInfo)); //otherwise use the field
		}
		propertyReaders.put(classInfo, readers);
		return readers;
	}

	public static Collection<RelationalReader> getRelationalReaders(ClassInfo classInfo) {
		if (relationalReaders.containsKey(classInfo)) {
			return relationalReaders.get(classInfo);
		}
		Collection<RelationalReader> readers = new ArrayList<>();

		for (FieldInfo fieldInfo : classInfo.relationshipFields()) {
			readers.add(new FieldReader(classInfo, fieldInfo));
		}
		relationalReaders.put(classInfo, readers);
		return readers;
	}


	public static RelationalWriter getIterableWriter(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection) {
		if (!iterableWriterCache.containsKey(classInfo)) {
			iterableWriterCache.put(classInfo, new HashMap<DirectedRelationshipForType, RelationalWriter>());
		}
		DirectedRelationshipForType directedRelationshipForType = new DirectedRelationshipForType(relationshipType, relationshipDirection, parameterType);
		if (iterableWriterCache.get(classInfo).containsKey(directedRelationshipForType)) {
			return iterableWriterCache.get(classInfo).get(directedRelationshipForType);
		}

		//2nd find a field annotated with type and direction
		FieldInfo fieldInfo = getIterableFieldInfo(classInfo, parameterType, relationshipType, relationshipDirection, STRICT_MODE);
		if (fieldInfo != null) {
			FieldWriter fieldWriter = new FieldWriter(classInfo, fieldInfo);
			cacheIterableFieldWriter(classInfo, parameterType, relationshipType, relationshipDirection, directedRelationshipForType, fieldInfo, fieldWriter);
			return fieldWriter;
		}

		//If relationshipDirection=INCOMING, we should have found an annotated field already

		if (!relationshipDirection.equals(Relationship.INCOMING)) {

			//4th find a field with implied type and direction
			fieldInfo = getIterableFieldInfo(classInfo, parameterType, relationshipType, relationshipDirection, INFERRED_MODE);
			if (fieldInfo != null) {
				FieldWriter fieldWriter = new FieldWriter(classInfo, fieldInfo);
				cacheIterableFieldWriter(classInfo, parameterType, relationshipType, relationshipDirection, directedRelationshipForType, fieldInfo, fieldWriter);
				return fieldWriter;
			}
		}
		iterableWriterCache.get(classInfo).put(directedRelationshipForType, null);
		return null;
	}


	public static RelationalReader getIterableReader(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection) {
		if (!iterableReaderCache.containsKey(classInfo)) {
			iterableReaderCache.put(classInfo, new HashMap<DirectedRelationshipForType, RelationalReader>());
		}
		DirectedRelationshipForType directedRelationshipForType = new DirectedRelationshipForType(relationshipType, relationshipDirection, parameterType);
		if (iterableReaderCache.get(classInfo).containsKey(directedRelationshipForType)) {
			return iterableReaderCache.get(classInfo).get(directedRelationshipForType);
		}

		//2nd find a field annotated with type and direction
		FieldInfo fieldInfo = getIterableFieldInfo(classInfo, parameterType, relationshipType, relationshipDirection, STRICT_MODE);
		if (fieldInfo != null) {
			FieldReader fieldReader = new FieldReader(classInfo, fieldInfo);
			iterableReaderCache.get(classInfo).put(directedRelationshipForType, fieldReader);
			return fieldReader;
		}

		//If relationshipDirection=INCOMING, we should have found an annotated field already

		if (!relationshipDirection.equals(Relationship.INCOMING)) {
			//4th find a field with implied type and direction
			fieldInfo = getIterableFieldInfo(classInfo, parameterType, relationshipType, relationshipDirection, INFERRED_MODE);
			if (fieldInfo != null) {
				FieldReader fieldReader = new FieldReader(classInfo, fieldInfo);
				iterableReaderCache.get(classInfo).put(directedRelationshipForType, fieldReader);
				return fieldReader;
			}
		}
		iterableReaderCache.get(classInfo).put(directedRelationshipForType, null);
		return null;
	}

	public static PropertyReader getIdentityPropertyReader(ClassInfo classInfo) {
		PropertyReader propertyReader = identityPropertyReaderCache.get(classInfo);
		if (propertyReader != null) {
			return propertyReader;
		}
		propertyReader = new FieldReader(classInfo, classInfo.identityField());
		identityPropertyReaderCache.put(classInfo, propertyReader);
		return propertyReader;
	}

	public static RelationalReader getEndNodeReader(ClassInfo relationshipEntityClassInfo) {
		for (FieldInfo fieldInfo : relationshipEntityClassInfo.relationshipFields()) {
			if (fieldInfo.getAnnotations().get(EndNode.class.getCanonicalName()) != null) {
				return new FieldReader(relationshipEntityClassInfo, fieldInfo);
			}
		}
		LOGGER.warn("Failed to find an @EndNode on {}", relationshipEntityClassInfo);
		return null;
	}

	public static RelationalReader getStartNodeReader(ClassInfo relationshipEntityClassInfo) {
		for (FieldInfo fieldInfo : relationshipEntityClassInfo.relationshipFields()) {
			if (fieldInfo.getAnnotations().get(StartNode.class.getCanonicalName()) != null) {
				return new FieldReader(relationshipEntityClassInfo, fieldInfo);
			}
		}
		LOGGER.warn("Failed to find an @StartNode on {}", relationshipEntityClassInfo);
		return null;
	}

	public static RelationalWriter getRelationalEntityWriter(ClassInfo classInfo, Class entityAnnotation) {
		if (entityAnnotation.getName() == null) {
			throw new RuntimeException(entityAnnotation.getSimpleName() + " is not defined on " + classInfo.name());
		}

		if (relationshipEntityWriterCache.get(classInfo) == null) {
			relationshipEntityWriterCache.put(classInfo, new HashMap<Class, RelationalWriter>());
		}
		if (relationshipEntityWriterCache.get(classInfo).containsKey(entityAnnotation)) {
			return relationshipEntityWriterCache.get(classInfo).get(entityAnnotation);
		}

		//Find annotated field
		FieldInfo field = null;
		for (FieldInfo fieldInfo : classInfo.relationshipFields()) {
			if (fieldInfo.getAnnotations().get(entityAnnotation.getName()) != null) {
				field = fieldInfo;
				break;
			}
		}
		if (field != null) {
			FieldWriter fieldWriter = new FieldWriter(classInfo, field);
			relationshipEntityWriterCache.get(classInfo).put(entityAnnotation, fieldWriter);
			return fieldWriter;
		}
		relationshipEntityWriterCache.get(classInfo).put(entityAnnotation, null);
		return null;
	}

	private static FieldInfo getIterableFieldInfo(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection, boolean strict) {
		List<FieldInfo> fieldInfos = classInfo.findIterableFields(parameterType, relationshipType, relationshipDirection, strict);
		if (fieldInfos.size() == 0) {
			if (!strict) {
				fieldInfos = classInfo.findIterableFields(parameterType);
			}
		}
		if (fieldInfos.size() == 1) {
			FieldInfo candidateFieldInfo = fieldInfos.iterator().next();
			if (candidateFieldInfo.hasAnnotation(Relationship.class.getCanonicalName())) {
				AnnotationInfo relationshipAnnotation = candidateFieldInfo.getAnnotations().get(Relationship.class.getCanonicalName());
				if (!relationshipType.equals(relationshipAnnotation.get("type", null))) {
					return null;
				}
			}
			//If the relationshipDirection is incoming and the candidateFieldInfo is also incoming or undirected
			if (relationshipDirection.equals(Relationship.INCOMING) &&
					(candidateFieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)) ||
					(candidateFieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED))) {
				return candidateFieldInfo;
			}
			//If the relationshipDirection is not incoming and the candidateFieldInfo is not incoming
			if (!relationshipDirection.equals(Relationship.INCOMING) && !candidateFieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)) {
				return candidateFieldInfo;
			}
		}

		if (fieldInfos.size() > 0) {
			LOGGER.warn("Cannot map iterable of {} to instance of {}. More than one potential matching field found.",
					parameterType, classInfo.name());
		}

		return null;
	}

	private static void cacheIterableFieldWriter(ClassInfo classInfo, Class<?> parameterType, String relationshipType, String relationshipDirection, DirectedRelationshipForType directedRelationshipForType, FieldInfo fieldInfo, FieldWriter fieldWriter) {
		if (fieldInfo.isParameterisedTypeOf(parameterType)) {
			//Cache the writer for the superclass used in the type param
			directedRelationshipForType = new DirectedRelationshipForType(relationshipType, relationshipDirection, ClassUtils.getType(fieldInfo.getTypeDescriptor()));
		}
		iterableWriterCache.get(classInfo).put(directedRelationshipForType, fieldWriter);
	}
}
