/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.metadata;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.context.register.EntityRegister;

/**
 * Maintains object to graph mapping details at the class (type) level
 * The ClassInfo object is used to maintain mappings from Java Types-&gt;Neo4j Labels
 * thereby allowing the correct labels to be applied to new nodes when they
 * are persisted.
 * The ClassInfo object also maintains a map of FieldInfo and MethodInfo objects
 * that maintain the appropriate information for mapping Java class attributes to Neo4j
 * node properties / paths (node)-[:relationship]-&gt;(node), via field or method
 * accessors respectively.
 * Given a type hierarchy, the ClassInfo object guarantees that for any type in that
 * hierarchy, the labels associated with that type will include the labels for
 * all its superclass and interface types as well. This is to avoid the need to iterate
 * through the ClassInfo hierarchy to recover label information.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public interface ClassInfo {

	boolean isAbstract();

	boolean isInterface();

	AnnotationsInfo annotationsInfo();

	FieldsInfo fieldsInfo();

	Field getField(FieldInfo fieldInfo);

	List<ClassInfo> directSubclasses();

	String neo4jName();

	boolean isSubclassOf(ClassInfo classInfo);

	String name();

	List<ClassInfo> directImplementingClasses();

	boolean isRelationshipEntity();

	Class getUnderlyingClass();

	FieldInfo propertyFieldByName(String propertyName);

	FieldInfo relationshipFieldByName(String propertyName);

	FieldInfo primaryIndexField();

	FieldInfo identityField();

	FieldInfo labelFieldOrNull();

	Collection<String> staticLabels();

	Collection<FieldInfo> propertyFields();

	boolean containsIndexes();

	Collection<FieldInfo> getIndexFields();

	FieldInfo propertyField(String propertyName);

	List<FieldInfo> findFields(String type);

	Set<FieldInfo> candidateRelationshipFields(String relationshipName, String relationshipDirection, boolean strict);

	Collection<FieldInfo> relationshipFields();

	List<FieldInfo> findIterableFields(Class<?> parameterType);

	List<FieldInfo> findIterableFields(Class<?> parameterType, String relationshipType, String relationshipDirection, boolean strict);

	FieldInfo relationshipField(String relationshipType, String relationshipDirection, boolean strictMode);

	List<FieldInfo> findFields(Class<?> aClass);

	// for testing
	FieldInfo relationshipField(String relationshipType);

	// for testing
	Object getTypeParameterDescriptorForRelationship(String type, String direction);

	// for testing
	List<FieldInfo> findIterableFields();


}


