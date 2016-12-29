package org.neo4j.ogm.metadata.impl.legacy;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.metadata.*;
import org.neo4j.ogm.session.Neo4jException;
import org.neo4j.ogm.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by markangrish on 29/12/2016.
 */
public class LegacyClassInfo implements ClassInfo {


	private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfo.class);
	private final List<ClassInfo> directSubclasses = new ArrayList<>();
	private final List<ClassInfo> directInterfaces = new ArrayList<>();
	private final List<ClassInfo> directImplementingClasses = new ArrayList<>();
	/**
	 * ISSUE-180: synchronized can be used instead of this lock but right now this mechanism is here to see if
	 * ConcurrentModificationException stops occurring.
	 */
	private final Lock lock = new ReentrantLock();
	private String className;
	private String directSuperclassName;
	private String neo4jName;
	private boolean isInterface;
	private boolean isAbstract;
	private boolean isEnum;
	private boolean hydrated;
	private FieldsInfo fieldsInfo = new FieldsInfo();
	private MethodsInfo methodsInfo = new MethodsInfo();
	private AnnotationsInfo annotationsInfo = new LegacyAnnotationsInfo();
	private InterfacesInfo interfacesInfo = new InterfacesInfo();
	private ClassInfo directSuperclass;
	private Map<Class, List<FieldInfo>> iterableFieldsForType = new HashMap<>();
	private Map<FieldInfo, Field> fieldInfoFields = new ConcurrentHashMap<>();
	private volatile Set<FieldInfo> fieldInfos;
	private volatile Map<String, FieldInfo> propertyFields;
	private volatile Map<String, FieldInfo> indexFields;
	private volatile FieldInfo identityField = null;
	private volatile FieldInfo primaryIndexField = null;
	private volatile FieldInfo labelField = null;
	private volatile boolean labelFieldMapped = false;
	private boolean primaryIndexFieldChecked = false;

	// todo move this to a factory class
	public LegacyClassInfo(InputStream inputStream) throws IOException {

		DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream, 1024));

		// Magic
		if (dataInputStream.readInt() != 0xCAFEBABE) {
			return;
		}

		dataInputStream.readUnsignedShort();    //minor version
		dataInputStream.readUnsignedShort();    // major version

		ConstantPool constantPool = new ConstantPool(dataInputStream);

		// Access flags
		int flags = dataInputStream.readUnsignedShort();

		isInterface = (flags & 0x0200) != 0;
		isAbstract = (flags & 0x0400) != 0;
		isEnum = (flags & 0x4000) != 0;

		className = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');
		String sce = constantPool.lookup(dataInputStream.readUnsignedShort());
		if (sce != null) {
			directSuperclassName = sce.replace('/', '.');
		}
		interfacesInfo = new InterfacesInfo(dataInputStream, constantPool);
		fieldsInfo = new FieldsInfo(dataInputStream, constantPool);
		methodsInfo = new MethodsInfo(dataInputStream, constantPool);
		annotationsInfo = new LegacyAnnotationsInfo(dataInputStream, constantPool);
		new ClassValidator(this).validate();
		primaryIndexField = primaryIndexField();
	}

	/**
	 * This class was referenced as a superclass of the given subclass.
	 *
	 * @param name the name of the class
	 * @param subclass {@link ClassInfo} of the subclass
	 */
	public LegacyClassInfo(String name, ClassInfo subclass) {
		this.className = name;
		this.hydrated = false;
		addSubclass((LegacyClassInfo) subclass);
	}

	/**
	 * A class that was previously only seen as a temp superclass of another class can now be fully hydrated.
	 *
	 * @param classInfoDetails ClassInfo details
	 */
	public void hydrate(ClassInfo classInfoDetails) {

		if (!this.hydrated) {
			this.hydrated = true;

			this.isAbstract = classInfoDetails.isAbstract();
			this.isInterface = classInfoDetails.isInterface();
			this.isEnum = classInfoDetails.isEnum();
			this.directSuperclassName = classInfoDetails.superclassName();

			//this.interfaces.addAll(classInfoDetails.interfaces());

			this.interfacesInfo.append(classInfoDetails.interfacesInfo());

			this.annotationsInfo.append(classInfoDetails.annotationsInfo());
			this.fieldsInfo.append(classInfoDetails.fieldsInfo());
			this.methodsInfo.append(classInfoDetails.methodsInfo());
		}
	}

	@Override
	public void extend(ClassInfo classInfo) {
		this.interfacesInfo.append(classInfo.interfacesInfo());
		this.fieldsInfo.append(classInfo.fieldsInfo());
		this.methodsInfo.append(classInfo.methodsInfo());
	}

	/**
	 * Connect this class to a subclass.
	 *
	 * @param subclass the subclass
	 */
	@Override
	public void addSubclass(ClassInfo subclass) {
		if (subclass.directSuperclass() != null && subclass.directSuperclass() != this) {
			throw new RuntimeException(subclass.name() + " has two superclasses: " + subclass.directSuperclass().name() + ", " + this.className);
		}
		((LegacyClassInfo)subclass).directSuperclass = this;
		this.directSubclasses.add(subclass);
	}

	public boolean hydrated() {
		return hydrated;
	}

	@Override
	public String name() {
		return className;
	}

	@Override
	public String simpleName() {
		return className.substring(className.lastIndexOf('.') + 1);
	}

	@Override
	public ClassInfo directSuperclass() {
		return directSuperclass;
	}

	/**
	 * <p>
	 * Retrieves the static labels that are applied to nodes in the database. If the class' instances are persisted by
	 * a relationship instead of a node then this method returns an empty collection.
	 * </p>
	 * <p>
	 * Note that this method returns only the static labels. A node entity instance may declare additional labels
	 * manged at runtime by using the @Labels annotation on a collection field, therefore the full set of labels to be
	 * mapped to a node will be the static labels, in addition to any labels declared by the backing field of an
	 * {@link Labels} annotation.
	 * </p>
	 *
	 * @return A {@link Collection} of all the static labels that apply to the node or an empty list if there aren't
	 * any, never <code>null</code>
	 */
	public Collection<String> staticLabels() {
		return collectLabels(new ArrayList<String>());
	}

	public String neo4jName() {
		if (neo4jName == null) {
			try {
				lock.lock();
				if (neo4jName == null) {
					AnnotationInfo annotationInfo = annotationsInfo.get(NodeEntity.class.getCanonicalName());
					if (annotationInfo != null) {
						neo4jName = annotationInfo.get("label", simpleName());
						return neo4jName;
					}
					annotationInfo = annotationsInfo.get(RelationshipEntity.class.getCanonicalName());
					if (annotationInfo != null) {
						neo4jName = annotationInfo.get("type", simpleName().toUpperCase());
						return neo4jName;
					}
					neo4jName = simpleName();
				}
			} finally {
				lock.unlock();
			}
		}
		return neo4jName;
	}

	@Override
	public Collection<String> collectLabels(Collection<String> labelNames) {
		if (!isAbstract || annotationsInfo.get(NodeEntity.class.getCanonicalName()) != null) {
			labelNames.add(neo4jName());
		}
		if (directSuperclass != null && !"java.lang.Object".equals(directSuperclass.name())) {
			directSuperclass.collectLabels(labelNames);
		}
		for (ClassInfo interfaceInfo : directInterfaces()) {
			interfaceInfo.collectLabels(labelNames);
		}
		return labelNames;
	}

	@Override
	public List<ClassInfo> directSubclasses() {
		return directSubclasses;
	}

	@Override
	public List<ClassInfo> directImplementingClasses() {
		return directImplementingClasses;
	}

	public List<ClassInfo> directInterfaces() {
		return directInterfaces;
	}

	@Override
	public org.neo4j.ogm.metadata.InterfacesInfo interfacesInfo() {
		return interfacesInfo;
	}

	@Override
	public Collection<AnnotationInfo> annotations() {
		return annotationsInfo.list();
	}

	@Override
	public boolean isInterface() {
		return isInterface;
	}

	@Override
	public boolean isEnum() {
		return isEnum;
	}

	@Override
	public org.neo4j.ogm.metadata.AnnotationsInfo annotationsInfo() {
		return annotationsInfo;
	}

	@Override
	public String superclassName() {
		return directSuperclassName;
	}

	@Override
	public org.neo4j.ogm.metadata.FieldsInfo fieldsInfo() {
		return fieldsInfo;
	}

	@Override
	public org.neo4j.ogm.metadata.MethodsInfo methodsInfo() {
		return methodsInfo;
	}

	@Override
	public String toString() {
		return name();
	}

	private FieldInfo identityFieldOrNull() {
		try {
			return identityField();
		} catch (MappingException me) {
			return null;
		}
	}

	/**
	 * The identity field is a field annotated with @NodeId, or if none exists, a field
	 * of type Long called 'id'
	 *
	 * @return A {@link FieldInfo} object representing the identity field never <code>null</code>
	 * @throws MappingException if no identity field can be found
	 */
	public FieldInfo identityField() {
		if (identityField != null) {
			return identityField;
		}
		try {
			lock.lock();
			if (identityField == null) {
				for (FieldInfo fieldInfo : fieldsInfo().fields()) {
					AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(GraphId.class.getCanonicalName());
					if (annotationInfo != null) {
						if (fieldInfo.getTypeDescriptor().equals("Ljava/lang/Long;")) {
							identityField = fieldInfo;
							return fieldInfo;
						}
					}
				}
				FieldInfo fieldInfo = fieldsInfo().get("id");
				if (fieldInfo != null) {
					if (fieldInfo.getTypeDescriptor().equals("Ljava/lang/Long;")) {
						identityField = fieldInfo;
						return fieldInfo;
					}
				}
				throw new MappingException("No identity field found for class: " + this.className);
			} else {
				return identityField;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * The label field is an optional field annotated with @Labels.
	 *
	 * @return A {@link FieldInfo} object representing the label field. Optionally <code>null</code>
	 */
	public FieldInfo labelFieldOrNull() {
		if (labelFieldMapped) {
			return labelField;
		}
		try {
			lock.lock();
			if (!labelFieldMapped) {
				for (FieldInfo fieldInfo : fieldsInfo().fields()) {
					if (fieldInfo.isLabelField()) {
						if (!fieldInfo.isIterable()) {
							throw new MappingException(String.format(
									"Field '%s' in class '%s' includes the @Labels annotation, however this field is not a " +
											"type of collection.", fieldInfo.getName(), this.name()));
						}
						labelFieldMapped = true;
						labelField = fieldInfo;
						return labelField;
					}
				}
			} else {
				return labelField;
			}
		} finally {
			lock.unlock();
		}
		return null;
	}

	public boolean isRelationshipEntity() {
		for (AnnotationInfo info : annotations()) {
			if (info.getName().equals(RelationshipEntity.class.getCanonicalName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A property field is any field annotated with @Property, or any field that can be mapped to a
	 * node property. The identity field is not a property field.
	 *
	 * @return A Collection of FieldInfo objects describing the classInfo's property fields
	 */
	public Collection<FieldInfo> propertyFields() {
		if (fieldInfos == null) {
			try {
				lock.lock();
				if (fieldInfos == null) {
					FieldInfo identityField = identityFieldOrNull();
					fieldInfos = new HashSet<>();
					for (FieldInfo fieldInfo : fieldsInfo().fields()) {
						if (fieldInfo != identityField && !fieldInfo.isLabelField()) {
							AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Property.class.getCanonicalName());
							if (annotationInfo == null) {
								if (fieldInfo.persistableAsProperty()) {
									fieldInfos.add(fieldInfo);
								}
							} else {
								fieldInfos.add(fieldInfo);
							}
						}
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return fieldInfos;
	}

	/**
	 * Finds the property field with a specific property name from the ClassInfo's property fields
	 * Note that this method does not allow for property names with differing case. //TODO
	 *
	 * @param propertyName the propertyName of the field to find
	 * @return A FieldInfo object describing the required property field, or null if it doesn't exist.
	 */
	public FieldInfo propertyField(String propertyName) {
		if (propertyFields == null) {

			try {
				lock.lock();
				if (propertyFields == null) {
					Collection<FieldInfo> fieldInfos = propertyFields();
					propertyFields = new HashMap<>(fieldInfos.size());
					for (FieldInfo fieldInfo : fieldInfos) {

						propertyFields.put(fieldInfo.property().toLowerCase(), fieldInfo);
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return propertyFields.get(propertyName.toLowerCase());
	}


	/**
	 * Finds the property field with a specific field name from the ClassInfo's property fields
	 *
	 * @param propertyName the propertyName of the field to find
	 * @return A FieldInfo object describing the required property field, or null if it doesn't exist.
	 */
	public FieldInfo propertyFieldByName(String propertyName) {
		for (FieldInfo fieldInfo : propertyFields()) {
			if (fieldInfo.getName().equalsIgnoreCase(propertyName)) {
				return fieldInfo;
			}
		}
		return null;
	}

	/**
	 * A relationship field is any field annotated with @Relationship, or any field that cannot be mapped to a
	 * node property. The identity field is not a relationship field.
	 *
	 * @return A Collection of FieldInfo objects describing the classInfo's relationship fields
	 */
	public Collection<FieldInfo> relationshipFields() {
		FieldInfo identityField = identityFieldOrNull();
		Set<FieldInfo> fieldInfos = new HashSet<>();
		for (FieldInfo fieldInfo : fieldsInfo().fields()) {
			if (fieldInfo != identityField) {
				AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(Relationship.class.getCanonicalName());
				if (annotationInfo == null) {
					if (!fieldInfo.persistableAsProperty()) {
						fieldInfos.add(fieldInfo);
					}
				} else {
					fieldInfos.add(fieldInfo);
				}
			}
		}
		return fieldInfos;
	}

	/**
	 * Finds the relationship field with a specific name from the ClassInfo's relationship fields
	 *
	 * @param relationshipName the relationshipName of the field to find
	 * @return A FieldInfo object describing the required relationship field, or null if it doesn't exist.
	 */
	public FieldInfo relationshipField(String relationshipName) {
		for (FieldInfo fieldInfo : relationshipFields()) {
			if (fieldInfo.relationship().equalsIgnoreCase(relationshipName)) {
				return fieldInfo;
			}
		}
		return null;
	}

	/**
	 * Finds the relationship field with a specific name and direction from the ClassInfo's relationship fields
	 *
	 * @param relationshipName the relationshipName of the field to find
	 * @param relationshipDirection the direction of the relationship
	 * @param strict if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
	 * @return A FieldInfo object describing the required relationship field, or null if it doesn't exist.
	 */
	public FieldInfo relationshipField(String relationshipName, String relationshipDirection, boolean strict) {
		for (FieldInfo fieldInfo : relationshipFields()) {
			String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
			if (relationshipName.equalsIgnoreCase(relationship)) {
				if (((fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && (relationshipDirection.equals(Relationship.INCOMING)))
						|| (relationshipDirection.equals(Relationship.OUTGOING) && !(fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
					return fieldInfo;
				}
			}
		}
		return null;
	}

	/**
	 * Finds all relationship fields with a specific name and direction from the ClassInfo's relationship fields
	 *
	 * @param relationshipName the relationshipName of the field to find
	 * @param relationshipDirection the direction of the relationship
	 * @param strict if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
	 * @return Set of  FieldInfo objects describing the required relationship field, or empty set if it doesn't exist.
	 */
	@Override
	public Set<FieldInfo> candidateRelationshipFields(String relationshipName, String relationshipDirection, boolean strict) {
		Set<FieldInfo> candidateFields = new HashSet<>();
		for (FieldInfo fieldInfo : relationshipFields()) {
			String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
			if (relationshipName.equalsIgnoreCase(relationship)) {
				if (((fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && (relationshipDirection.equals(Relationship.INCOMING)))
						|| (relationshipDirection.equals(Relationship.OUTGOING) && !(fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
					candidateFields.add(fieldInfo);
				}
			}
		}
		return candidateFields;
	}

	/**
	 * Finds the relationship field with a specific property name from the ClassInfo's relationship fields
	 *
	 * @param fieldName the name of the field
	 * @return A FieldInfo object describing the required relationship field, or null if it doesn't exist.
	 */
	public FieldInfo relationshipFieldByName(String fieldName) {
		for (FieldInfo fieldInfo : relationshipFields()) {
			if (fieldInfo.getName().equalsIgnoreCase(fieldName)) {
				return fieldInfo;
			}
		}
		return null;
	}


	public Field getField(FieldInfo fieldInfo) {
		Field field = fieldInfoFields.get(fieldInfo);
		if (field != null) {
			return field;
		}
		try {
			field = MetaDataClassLoader.loadClass(name()).getDeclaredField(fieldInfo.getName());
			fieldInfoFields.put(fieldInfo, field);
			return field;
		} catch (NoSuchFieldException e) {
			if (directSuperclass() != null) {
				field = directSuperclass().getField(fieldInfo);
				fieldInfoFields.put(fieldInfo, field);
				return field;
			} else {
				throw new RuntimeException("Field " + fieldInfo.getName() + " not found in class " + name() + " or any of its superclasses");
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Find all FieldInfos for the specified ClassInfo whose type matches the supplied fieldType
	 *
	 * @param fieldType The field type to look for
	 * @return A {@link List} of {@link FieldInfo} objects that are of the given type, never <code>null</code>
	 */
	public List<FieldInfo> findFields(Class<?> fieldType) {
		String fieldSignature = "L" + fieldType.getName().replace(".", "/") + ";";
		List<FieldInfo> fieldInfos = new ArrayList<>();
		for (FieldInfo fieldInfo : fieldsInfo().fields()) {
			if (fieldInfo.getTypeDescriptor().equals(fieldSignature)) {
				fieldInfos.add(fieldInfo);
			}
		}
		return fieldInfos;
	}

	/**
	 * Find all FieldInfos for the specified ClassInfo which have the specified annotation
	 *
	 * @param annotation The annotation
	 * @return A {@link List} of {@link FieldInfo} objects that are of the given type, never <code>null</code>
	 */
	public List<FieldInfo> findFields(String annotation) {
		List<FieldInfo> fieldInfos = new ArrayList<>();
		for (FieldInfo fieldInfo : fieldsInfo().fields()) {
			if (fieldInfo.hasAnnotation(annotation)) {
				fieldInfos.add(fieldInfo);
			}
		}
		return fieldInfos;
	}

	/**
	 * Retrieves a {@link List} of {@link FieldInfo} representing all of the fields that can be iterated over
	 * using a "foreach" loop.
	 *
	 * @return {@link List} of {@link FieldInfo}
	 */
	public List<FieldInfo> findIterableFields() {
		List<FieldInfo> fieldInfos = new ArrayList<>();
		try {
			for (FieldInfo fieldInfo : fieldsInfo().fields()) {
				Class type = getField(fieldInfo).getType();
				if (type.isArray() || Iterable.class.isAssignableFrom(type)) {
					fieldInfos.add(fieldInfo);
				}
			}
			return fieldInfos;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Finds all fields whose type is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
	 * where X is the generic parameter type of the Array or Iterable
	 *
	 * @param iteratedType the type of iterable
	 * @return {@link List} of {@link MethodInfo}, never <code>null</code>
	 */
	public List<FieldInfo> findIterableFields(Class iteratedType) {
		if (iterableFieldsForType.containsKey(iteratedType)) {
			return iterableFieldsForType.get(iteratedType);
		}
		List<FieldInfo> fieldInfos = new ArrayList<>();
		String typeSignature = "L" + iteratedType.getName().replace('.', '/') + ";";
		String arrayOfTypeSignature = "[" + typeSignature;
		try {
			for (FieldInfo fieldInfo : fieldsInfo().fields()) {
				String fieldType = fieldInfo.getTypeDescriptor();
				if (fieldInfo.isArray() && (fieldType.equals(arrayOfTypeSignature) || fieldInfo.isParameterisedTypeOf(iteratedType))) {
					fieldInfos.add(fieldInfo);
				} else if (fieldInfo.isIterable() && (fieldType.equals(typeSignature) || fieldInfo.isParameterisedTypeOf(iteratedType))) {
					fieldInfos.add(fieldInfo);
				}
			}
			iterableFieldsForType.put(iteratedType, fieldInfos);
			return fieldInfos;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Finds all fields whose type is equivalent to Array&lt;X&gt; or assignable from Iterable&lt;X&gt;
	 * where X is the generic parameter type of the Array or Iterable and the relationship type backing this iterable is "relationshipType"
	 *
	 * @param iteratedType the type of iterable
	 * @param relationshipType the relationship type
	 * @param relationshipDirection the relationship direction
	 * @param strict if true, does not infer relationship type but looks for it in the @Relationship annotation. Null if missing. If false, infers relationship type from FieldInfo
	 * @return {@link List} of {@link MethodInfo}, never <code>null</code>
	 */
	public List<FieldInfo> findIterableFields(Class iteratedType, String relationshipType, String relationshipDirection, boolean strict) {
		List<FieldInfo> fieldInfos = new ArrayList<>();
		for (FieldInfo fieldInfo : findIterableFields(iteratedType)) {
			String relationship = strict ? fieldInfo.relationshipTypeAnnotation() : fieldInfo.relationship();
			if (relationshipType.equals(relationship)) {
				if (((fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING) || fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.UNDIRECTED)) && relationshipDirection.equals(Relationship.INCOMING))
						|| (relationshipDirection.equals(Relationship.OUTGOING) && !(fieldInfo.relationshipDirection(Relationship.OUTGOING).equals(Relationship.INCOMING)))) {
					fieldInfos.add(fieldInfo);
				}
			}
		}
		return fieldInfos;
	}

	public boolean isTransient() {
		return annotationsInfo.get(Transient.class.getCanonicalName()) != null;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	/**
	 * Returns true if this classInfo is in the subclass hierarchy of b, or if this classInfo is the same as b, false otherwise
	 *
	 * @param classInfo the classInfo at the toplevel of a type hierarchy to search through
	 * @return true if this classInfo is in the subclass hierarchy of classInfo, false otherwise
	 */
	public boolean isSubclassOf(ClassInfo classInfo) {

		if (this == classInfo) {
			return true;
		}

		boolean found = false;

		for (ClassInfo subclass : classInfo.directSubclasses()) {
			found = isSubclassOf(subclass);
			if (found) {
				break;
			}
		}

		return found;
	}

	public Class<?> getType(String typeParameterDescriptor) {
		return ClassUtils.getType(typeParameterDescriptor);
	}

	/**
	 * Get the underlying class represented by this ClassInfo
	 *
	 * @return the underlying class or null if it cannot be determined
	 */
	@Override
	public Class getUnderlyingClass() {
		try {
			return MetaDataClassLoader.loadClass(className);//Class.forName(className);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Could not get underlying class for {}", className);
		}
		return null;
	}

	/**
	 * Gets the class of the type parameter description of the entity related to this.
	 * The match is done based on the following-
	 * 1. Look for a setter explicitly annotated with @Relationship for a type and implied direction
	 * 2. Look for a field explicitly annotated with @Relationship for a type and implied direction
	 * 3. Look for a setter with name derived from the relationship type for the given direction
	 * 4. Look for a field with name derived from the relationship type for the given direction
	 *
	 * @param relationshipType the relationship type
	 * @param relationshipDirection the relationship direction
	 * @return class of the type parameter descriptor or null if it could not be determined
	 */
	public Class getTypeParameterDescriptorForRelationship(String relationshipType, String relationshipDirection) {
		final boolean STRICT_MODE = true; //strict mode for matching methods and fields, will only look for explicit annotations
		final boolean INFERRED_MODE = false; //inferred mode for matching methods and fields, will infer the relationship type from the getter/setter/property

		try {
			FieldInfo fieldInfo = relationshipField(relationshipType, relationshipDirection, STRICT_MODE);
			if (fieldInfo != null && fieldInfo.getTypeDescriptor() != null) {
				return ClassUtils.getType(fieldInfo.getTypeDescriptor());
			}

			if (!relationshipDirection.equals(Relationship.INCOMING)) { //we always expect an annotation for INCOMING
				fieldInfo = relationshipField(relationshipType, relationshipDirection, INFERRED_MODE);
				if (fieldInfo != null && fieldInfo.getTypeDescriptor() != null) {
					return ClassUtils.getType(fieldInfo.getTypeDescriptor());
				}
			}
		} catch (RuntimeException e) {
			LOGGER.debug("Could not get {} class type for relationshipType {} and relationshipDirection {} ", className, relationshipType, relationshipDirection);
		}
		return null;
	}

	/**
	 * @return If this class contains any fields/properties annotated with @Index.
	 */
	@Override
	public boolean containsIndexes() {
		return !getIndexFields().isEmpty();
	}

	/**
	 * @return The <code>FieldInfo</code>s representing the Indexed fields in this class.
	 */
	@Override
	public Collection<FieldInfo> getIndexFields() {
		if (indexFields == null) {
			indexFields = addIndexes();
		}
		return indexFields.values();
	}

	private Map<String, FieldInfo> addIndexes() {
		Map<String, FieldInfo> indexes = new HashMap<>();

		// No way to get declared fields from current byte code impl. Using reflection instead.
		Field[] declaredFields;
		try {
			declaredFields = Class.forName(className).getDeclaredFields();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could not reflectively read declared fields", e);
		}

		final String indexAnnotation = Index.class.getCanonicalName();

		for (FieldInfo fieldInfo : fieldsInfo().fields()) {
			if (isDeclaredField(declaredFields, fieldInfo.getName()) && fieldInfo.hasAnnotation(indexAnnotation)) {
				indexes.put(fieldInfo.property(), fieldInfo);
			}
		}
		return indexes;
	}

	private static boolean isDeclaredField(Field[] declaredFields, String name) {

		for (Field field : declaredFields) {
			if (field.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}


	public FieldInfo primaryIndexField() {
		if (!primaryIndexFieldChecked && primaryIndexField == null) {
			final String indexAnnotation = Index.class.getCanonicalName();

			for (FieldInfo fieldInfo : fieldsInfo().fields()) {
				AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get(indexAnnotation);
				if (annotationInfo != null && annotationInfo.get("primary") != null && annotationInfo.get("primary").equals("true")) {

					if (primaryIndexField == null) {
						primaryIndexField = fieldInfo;
					} else {
						throw new Neo4jException("Each class may only define one primary index.");
					}
				}
			}
			primaryIndexFieldChecked = true;
		}

		return primaryIndexField;
	}
}
