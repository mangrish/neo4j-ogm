package org.neo4j.ogm.metadata.impl.reflections;


import java.util.*;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.metadata.*;
import org.neo4j.ogm.metadata.impl.legacy.LegacyClassInfo;
import org.neo4j.ogm.typeconversion.ConvertibleTypes;
import org.neo4j.ogm.typeconversion.ProxyAttributeConverter;
import org.neo4j.ogm.utils.ClassUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by markangrish on 29/12/2016.
 */
public class ReflectionsDomainInfo implements DomainInfo {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionsDomainInfo.class);

	private Reflections reflections;

	private final Map<String, ClassInfo> classNameToClassInfo = new HashMap<>();
	private final Map<String, ArrayList<ClassInfo>> annotationNameToClassInfo = new HashMap<>();
	private final Map<String, ArrayList<ClassInfo>> interfaceNameToClassInfo = new HashMap<>();
	private final Set<Class> enumTypes = new HashSet<>();


	public ReflectionsDomainInfo(String... packages) {
		long startTime = System.nanoTime();
		this.reflections = new Reflections(packages);

		for (Class<?> type : reflections.getSubTypesOf(Object.class)) {
			process(type);
		}
		finish();

		LOGGER.info("{} classes loaded in {} nanoseconds", classNameToClassInfo.entrySet().size(), (System.nanoTime() - startTime));
	}

	public void process(final Class<?> type) {

		ClassInfo classInfo = new ReflectionsClassInfo(type);

		String className = classInfo.name();
		String superclassName = classInfo.superclassName();

		LOGGER.debug("Processing: {} -> {}", className, superclassName);

		if (className != null) {

			ClassInfo thisClassInfo = classNameToClassInfo.get(className);

			if (thisClassInfo == null) {
				thisClassInfo = classInfo;
				classNameToClassInfo.put(className, thisClassInfo);
			}

			if (!thisClassInfo.hydrated()) {

				thisClassInfo.hydrate(classInfo);

				ClassInfo superclassInfo = classNameToClassInfo.get(superclassName);
				if (superclassInfo == null) {
					classNameToClassInfo.put(superclassName, new LegacyClassInfo(superclassName, thisClassInfo));
				} else {
					superclassInfo.addSubclass(thisClassInfo);
				}
			}

			if (thisClassInfo.isEnum()) {
				LOGGER.debug("Registering enum class: {}", thisClassInfo.name());
				enumTypes.add(thisClassInfo.getUnderlyingClass());
			}
		}
	}

	public void finish() {

		LOGGER.info("Starting Post-processing phase");

		buildAnnotationNameToClassInfoMap();
		buildInterfaceNameToClassInfoMap();

		List<ClassInfo> transientClasses = new ArrayList<>();

		for (ClassInfo classInfo : classNameToClassInfo.values()) {

			if (classInfo.name() == null || classInfo.name().equals("java.lang.Object")) continue;

			LOGGER.debug("Post-processing: {}", classInfo.name());

			if (classInfo.isTransient()) {
				LOGGER.debug(" - Registering @Transient baseclass: {}", classInfo.name());
				transientClasses.add(classInfo);
				continue;
			}

			if (classInfo.superclassName() == null || classInfo.superclassName().equals("java.lang.Object")) {
				extend(classInfo, classInfo.directSubclasses());
			}

			for (InterfaceInfo interfaceInfo : classInfo.interfacesInfo().list()) {
				implement(classInfo, interfaceInfo);
			}
		}

		LOGGER.debug("Checking for @Transient classes....");

		// find transient interfaces
		Collection<ArrayList<ClassInfo>> interfaceInfos = interfaceNameToClassInfo.values();
		for (ArrayList<ClassInfo> classInfos : interfaceInfos) {
			for (ClassInfo classInfo : classInfos) {
				if (classInfo.isTransient()) {
					LOGGER.debug("Registering @Transient baseclass: {}", classInfo.name());
					transientClasses.add(classInfo);
				}
			}
		}

		// remove all transient class hierarchies
		Set<Class> transientClassesRemoved = new HashSet<>();
		for (ClassInfo transientClass : transientClasses) {
			transientClassesRemoved.addAll(removeTransientClass(transientClass));
		}

		LOGGER.debug("Registering converters and deregistering transient fields....");
		postProcessFields(transientClassesRemoved);

		LOGGER.info("Post-processing complete");
	}


	@Override
	public ClassInfo getClassSimpleName(String name) {
		return null;
	}

	@Override
	public List<ClassInfo> getClassInfosWithAnnotation(String annotation) {
		return annotationNameToClassInfo.get(annotation);
	}

	@Override
	public ClassInfo getClassInfoForInterface(String interfaceName) {
		return null;
	}

	@Override
	public Map<String, ClassInfo> getClassInfoMap() {
		return classNameToClassInfo;
	}

	@Override
	public List<ClassInfo> getClassInfos(String interfaceName) {
		return interfaceNameToClassInfo.get(interfaceName);
	}

	@Override
	public ClassInfo getClass(String fqn) {
		return classNameToClassInfo.get(fqn);
	}

	private void buildAnnotationNameToClassInfoMap() {

		LOGGER.info("Building annotation class map");
		for (ClassInfo classInfo : classNameToClassInfo.values()) {
			for (AnnotationInfo annotation : classInfo.annotations()) {
				ArrayList<ClassInfo> classInfoList = annotationNameToClassInfo.get(annotation.getName());
				if (classInfoList == null) {
					annotationNameToClassInfo.put(annotation.getName(), classInfoList = new ArrayList<>());
				}
				classInfoList.add(classInfo);
			}
		}
	}

	private void buildInterfaceNameToClassInfoMap() {
		LOGGER.info("Building interface class map for {} classes", classNameToClassInfo.values().size());
		for (ClassInfo classInfo : classNameToClassInfo.values()) {
			LOGGER.debug(" - {} implements {} interfaces", classInfo.simpleName(), classInfo.interfacesInfo().list().size());
			for (InterfaceInfo iface : classInfo.interfacesInfo().list()) {
				ArrayList<ClassInfo> classInfoList = interfaceNameToClassInfo.get(iface.name());
				if (classInfoList == null) {
					interfaceNameToClassInfo.put(iface.name(), classInfoList = new ArrayList<>());
				}
				LOGGER.debug("   - {}", iface.name());
				classInfoList.add(classInfo);
			}
		}
	}

	private void extend(ClassInfo superclass, List<ClassInfo> subclasses) {
		for (ClassInfo subclass : subclasses) {
			subclass.extend(superclass);
			extend(subclass, subclass.directSubclasses());
		}
	}

	private void implement(ClassInfo implementingClass, InterfaceInfo interfaceInfo) {

		ClassInfo interfaceClass = classNameToClassInfo.get(interfaceInfo.name());

		if (interfaceClass != null) {
			if (!implementingClass.directInterfaces().contains(interfaceClass)) {
				LOGGER.debug(" - Setting {} implements {}", implementingClass.simpleName(), interfaceClass.simpleName());
				implementingClass.directInterfaces().add(interfaceClass);
			}

			if (!interfaceClass.directImplementingClasses().contains(implementingClass)) {
				interfaceClass.directImplementingClasses().add(implementingClass);
			}

			for (ClassInfo subClassInfo : implementingClass.directSubclasses()) {
				implement(subClassInfo, interfaceInfo);
			}
		} else {
			LOGGER.debug(" - No ClassInfo found for interface class: {}", interfaceInfo.name());
		}
	}

	private void postProcessFields(Set<Class> transientClassesRemoved) {
		for (ClassInfo classInfo : classNameToClassInfo.values()) {
			boolean registerConverters = false;
			if (!classInfo.isEnum() && !classInfo.isInterface()) {
				registerConverters = true;
			}
			Iterator<FieldInfo> fieldInfoIterator = classInfo.fieldsInfo().fields().iterator();
			while (fieldInfoIterator.hasNext()) {
				FieldInfo fieldInfo = fieldInfoIterator.next();
				if (!fieldInfo.persistableAsProperty()) {
					Class fieldClass = null;
					try {
						fieldClass = ClassUtils.getType(fieldInfo.getTypeDescriptor());
					} catch (Exception e) {
						LOGGER.debug("Unable to compute class type for " + classInfo.name() + ", field: " + fieldInfo.getName());
					}
					if (fieldClass != null && transientClassesRemoved.contains(fieldClass)) {
						fieldInfoIterator.remove();
						continue;
					}
				}
				if (registerConverters) {
					registerDefaultFieldConverters(classInfo, fieldInfo);
				}
			}
		}
	}

	private Set<Class> removeTransientClass(ClassInfo transientClass) {
		Set<Class> removed = new HashSet<>();
		if (transientClass != null && !transientClass.name().equals("java.lang.Object")) {
			LOGGER.debug("Removing @Transient class: {}", transientClass.name());
			classNameToClassInfo.remove(transientClass.name());
			removed.add(transientClass.getUnderlyingClass());
			for (ClassInfo transientChild : transientClass.directSubclasses()) {
				removeTransientClass(transientChild);
			}
			for (ClassInfo transientChild : transientClass.directImplementingClasses()) {
				removeTransientClass(transientChild);
			}
		}
		return removed;
	}

	private void registerDefaultFieldConverters(ClassInfo classInfo, FieldInfo fieldInfo) {

		if (!fieldInfo.hasPropertyConverter() && !fieldInfo.hasCompositeConverter()) {

			if (fieldInfo.isDate()) {
				setDateFieldConverter(fieldInfo);
			} else if (fieldInfo.isBigInteger()) {
				setBigIntegerFieldConverter(fieldInfo);
			} else if (fieldInfo.isBigDecimal()) {
				setBigDecimalConverter(fieldInfo);
			} else if (fieldInfo.isByteArray()) {
				fieldInfo.setPropertyConverter(ConvertibleTypes.getByteArrayBase64Converter());
			} else if (fieldInfo.isByteArrayWrapper()) {
				fieldInfo.setPropertyConverter(ConvertibleTypes.getByteArrayWrapperBase64Converter());
			} else {
				if (fieldInfo.getAnnotations().get(Convert.CLASS) != null) {
					// no converter's been set but this method is annotated with @Convert so we need to proxy it
					Class<?> entityAttributeType = ClassUtils.getType(fieldInfo.getTypeDescriptor());
					String graphTypeDescriptor = fieldInfo.getAnnotations().get(Convert.CLASS).get(Convert.GRAPH_TYPE, null);
					if (graphTypeDescriptor == null) {
						throw new MappingException("Found annotation to convert a " + entityAttributeType.getName()
								+ " on " + classInfo.name() + '.' + fieldInfo.getName()
								+ " but no target graph property type or specific AttributeConverter have been specified.");
					}
					fieldInfo.setPropertyConverter(new ProxyAttributeConverter(entityAttributeType, ClassUtils.getType(graphTypeDescriptor), null));
				}

				Class fieldType = ClassUtils.getType(fieldInfo.getTypeDescriptor());

				boolean enumConverterSet = false;
				for (Class enumClass : enumTypes) {
					if (fieldType.equals(enumClass)) {
						setEnumFieldConverter(fieldInfo, enumClass);
						enumConverterSet = true;
						break;
					}
				}

				if (!enumConverterSet) {
					if (fieldType.isEnum()) {
						LOGGER.debug("Setting default enum converter for unscanned class " + classInfo.name() + ", field: " + fieldInfo.getName());
						setEnumFieldConverter(fieldInfo, fieldType);
					}
				}
			}
		}
	}


	private void setEnumFieldConverter(FieldInfo fieldInfo, Class enumClass) {
		if (fieldInfo.isArray()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumArrayConverter(enumClass));
		} else if (fieldInfo.isIterable()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumCollectionConverter(enumClass, fieldInfo.getCollectionClassname()));
		} else {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumConverter(enumClass));
		}
	}

	private void setBigDecimalConverter(FieldInfo fieldInfo) {
		if (fieldInfo.isArray()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalArrayConverter());
		} else if (fieldInfo.isIterable()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalCollectionConverter(fieldInfo.getCollectionClassname()));
		} else {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalConverter());
		}
	}

	private void setBigIntegerFieldConverter(FieldInfo fieldInfo) {
		if (fieldInfo.isArray()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerArrayConverter());
		} else if (fieldInfo.isIterable()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerCollectionConverter(fieldInfo.getCollectionClassname()));
		} else {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerConverter());
		}
	}

	private void setDateFieldConverter(FieldInfo fieldInfo) {
		if (fieldInfo.isArray()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getDateArrayConverter());
		} else if (fieldInfo.isIterable()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getDateCollectionConverter(fieldInfo.getCollectionClassname()));
		} else {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getDateConverter());
		}
	}
}
