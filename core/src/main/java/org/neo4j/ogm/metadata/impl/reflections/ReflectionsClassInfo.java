package org.neo4j.ogm.metadata.impl.reflections;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javassist.Modifier;
import org.neo4j.ogm.metadata.*;

/**
 * Created by markangrish on 30/12/2016.
 */
public class ReflectionsClassInfo implements ClassInfo {

	private final Class<?> underlyingClass;

	public ReflectionsClassInfo(Class<?> type) {

		underlyingClass = type;
	}


	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract( underlyingClass.getModifiers() );
	}

	@Override
	public boolean isInterface() {
		return underlyingClass.isInterface();
	}

	@Override
	public boolean isEnum() {
		return underlyingClass.isEnum();
	}

	@Override
	public String superclassName() {
		return underlyingClass.getSuperclass().getName();
	}

	@Override
	public InterfacesInfo interfacesInfo() {
		return null;
	}

	@Override
	public AnnotationsInfo annotationsInfo() {
		return null;
	}

	@Override
	public FieldsInfo fieldsInfo() {
		return null;
	}

	@Override
	public MethodsInfo methodsInfo() {
		return null;
	}

	@Override
	public ClassInfo directSuperclass() {
		return null;
	}

	@Override
	public Collection<String> collectLabels(Collection<String> labelNames) {
		return null;
	}

	@Override
	public Field getField(FieldInfo fieldInfo) {
		return null;
	}

	@Override
	public List<ClassInfo> directSubclasses() {
		return null;
	}

	@Override
	public String neo4jName() {
		return null;
	}

	@Override
	public boolean isSubclassOf(ClassInfo classInfo) {
		return false;
	}

	@Override
	public String name() {
		return null;
	}

	@Override
	public List<ClassInfo> directImplementingClasses() {
		return null;
	}

	@Override
	public Collection<AnnotationInfo> annotations() {
		return null;
	}

	@Override
	public boolean isRelationshipEntity() {
		return false;
	}

	@Override
	public String simpleName() {
		return null;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public Class getUnderlyingClass() {
		return null;
	}

	@Override
	public void extend(ClassInfo superclass) {

	}

	@Override
	public boolean hydrated() {
		return false;
	}

	@Override
	public void hydrate(ClassInfo classInfo) {

	}

	@Override
	public void addSubclass(ClassInfo classInfo) {

	}

	@Override
	public List<ClassInfo> directInterfaces() {
		return null;
	}

	@Override
	public FieldInfo propertyFieldByName(String propertyName) {
		return null;
	}

	@Override
	public FieldInfo relationshipFieldByName(String propertyName) {
		return null;
	}

	@Override
	public FieldInfo primaryIndexField() {
		return null;
	}

	@Override
	public FieldInfo identityField() {
		return null;
	}

	@Override
	public FieldInfo labelFieldOrNull() {
		return null;
	}

	@Override
	public Collection<String> staticLabels() {
		return null;
	}

	@Override
	public Collection<FieldInfo> propertyFields() {
		return null;
	}

	@Override
	public boolean containsIndexes() {
		return false;
	}

	@Override
	public Collection<FieldInfo> getIndexFields() {
		return null;
	}

	@Override
	public FieldInfo propertyField(String propertyName) {
		return null;
	}

	@Override
	public List<FieldInfo> findFields(String type) {
		return null;
	}

	@Override
	public Set<FieldInfo> candidateRelationshipFields(String relationshipName, String relationshipDirection, boolean strict) {
		return null;
	}

	@Override
	public Collection<FieldInfo> relationshipFields() {
		return null;
	}

	@Override
	public List<FieldInfo> findIterableFields(Class<?> parameterType) {
		return null;
	}

	@Override
	public List<FieldInfo> findIterableFields(Class<?> parameterType, String relationshipType, String relationshipDirection, boolean strict) {
		return null;
	}

	@Override
	public FieldInfo relationshipField(String relationshipType, String relationshipDirection, boolean strictMode) {
		return null;
	}

	@Override
	public List<FieldInfo> findFields(Class<?> aClass) {
		return null;
	}

	@Override
	public FieldInfo relationshipField(String relationshipType) {
		return null;
	}

	@Override
	public Object getTypeParameterDescriptorForRelationship(String type, String direction) {
		return null;
	}

	@Override
	public List<FieldInfo> findIterableFields() {
		return null;
	}
}
