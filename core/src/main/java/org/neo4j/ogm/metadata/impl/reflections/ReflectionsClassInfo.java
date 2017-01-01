package org.neo4j.ogm.metadata.impl.reflections;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javassist.Modifier;
import org.neo4j.ogm.metadata.*;
import org.neo4j.ogm.metadata.impl.legacy.LegacyInterfacesInfo;
import org.neo4j.ogm.metadata.impl.legacy.LegacyMethodsInfo;

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

	public boolean isEnum() {
		return underlyingClass.isEnum();
	}

	public String superclassName() {
		return underlyingClass.getSuperclass().getName();
	}

	public LegacyInterfacesInfo interfacesInfo() {
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

	public LegacyMethodsInfo methodsInfo() {
		return null;
	}

	public ClassInfo directSuperclass() {
		return null;
	}

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

	public Collection<AnnotationInfo> annotations() {
		return null;
	}

	@Override
	public boolean isRelationshipEntity() {
		return false;
	}

	public String simpleName() {
		return null;
	}

	public boolean isTransient() {
		return false;
	}

	@Override
	public Class getUnderlyingClass() {
		return null;
	}

	public void extend(ClassInfo superclass) {

	}

	public boolean hydrated() {
		return false;
	}

	public void hydrate(ClassInfo classInfo) {

	}

	public void addSubclass(ClassInfo classInfo) {

	}

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
