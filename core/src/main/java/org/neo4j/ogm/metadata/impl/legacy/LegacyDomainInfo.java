package org.neo4j.ogm.metadata.impl.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.metadata.*;
import org.neo4j.ogm.metadata.impl.legacy.scanner.ClassPathScanner;
import org.neo4j.ogm.typeconversion.ConversionCallbackRegistry;
import org.neo4j.ogm.typeconversion.ConvertibleTypes;
import org.neo4j.ogm.typeconversion.ProxyAttributeConverter;
import org.neo4j.ogm.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by markangrish on 30/12/2016.
 */
public class LegacyDomainInfo implements DomainInfo {

	private static final Logger LOGGER = LoggerFactory.getLogger(LegacyDomainInfo.class);

	private static final String dateSignature = "java/util/Date";
	private static final String bigDecimalSignature = "java/math/BigDecimal";
	private static final String bigIntegerSignature = "java/math/BigInteger";
	private static final String byteArraySignature = "[B";
	private static final String byteArrayWrapperSignature = "[Ljava/lang/Byte";
	private static final String arraySignature = "[L";
	private static final String collectionSignature = "L";

	private final List<String> classPaths = new ArrayList<>();

	private final Map<String, ClassInfo> classNameToClassInfo = new HashMap<>();
	private final Map<String, ArrayList<ClassInfo>> annotationNameToClassInfo = new HashMap<>();
	private final Map<String, ArrayList<ClassInfo>> interfaceNameToClassInfo = new HashMap<>();

	private final Set<Class> enumTypes = new HashSet<>();

	private final ConversionCallbackRegistry conversionCallbackRegistry = new ConversionCallbackRegistry();

	public LegacyDomainInfo(String... packages) {
		long startTime = System.nanoTime();
		load(packages);

		LOGGER.info("{} classes loaded in {} nanoseconds", classNameToClassInfo.entrySet().size(), (System.nanoTime() - startTime));
	}


	private void buildAnnotationNameToClassInfoMap() {

		LOGGER.info("Building annotation class map");
		for (ClassInfo classInfo : classNameToClassInfo.values()) {
			for (LegacyAnnotationInfo annotation : ((LegacyClassInfo)classInfo).annotations()) {
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
			LOGGER.debug(" - {} implements {} interfaces", ((LegacyClassInfo)classInfo).simpleName(), ((LegacyClassInfo)classInfo).interfacesInfo().list().size());
			for (LegacyInterfaceInfo iface : ((LegacyClassInfo)classInfo).interfacesInfo().list()) {
				ArrayList<ClassInfo> classInfoList = interfaceNameToClassInfo.get(iface.name());
				if (classInfoList == null) {
					interfaceNameToClassInfo.put(iface.name(), classInfoList = new ArrayList<>());
				}
				LOGGER.debug("   - {}", iface.name());
				classInfoList.add(classInfo);
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

			if (((LegacyClassInfo)classInfo).isTransient()) {
				LOGGER.debug(" - Registering @Transient baseclass: {}", classInfo.name());
				transientClasses.add(classInfo);
				continue;
			}

			if (((LegacyClassInfo)classInfo).superclassName() == null || ((LegacyClassInfo)classInfo).superclassName().equals("java.lang.Object")) {
				extend(classInfo, classInfo.directSubclasses());
			}

			for (LegacyInterfaceInfo interfaceInfo : ((LegacyClassInfo)classInfo).interfacesInfo().list()) {
				implement((LegacyClassInfo) classInfo, interfaceInfo);
			}
		}

		LOGGER.debug("Checking for @Transient classes....");

		// find transient interfaces
		Collection<ArrayList<ClassInfo>> interfaceInfos = interfaceNameToClassInfo.values();
		for (ArrayList<ClassInfo> classInfos : interfaceInfos) {
			for (ClassInfo classInfo : classInfos) {
				if (((LegacyClassInfo)classInfo).isTransient()) {
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

	private void postProcessFields(Set<Class> transientClassesRemoved) {
		for (ClassInfo classInfo : classNameToClassInfo.values()) {
			boolean registerConverters = false;
			if (!((LegacyClassInfo)classInfo).isEnum() && !classInfo.isInterface()) {
				registerConverters = true;
			}
			Iterator<FieldInfo> fieldInfoIterator = classInfo.fieldsInfo().fields().iterator();
			while (fieldInfoIterator.hasNext()) {
				LegacyFieldInfo fieldInfo = (LegacyFieldInfo) fieldInfoIterator.next();
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


	private void extend(ClassInfo superclass, List<ClassInfo> subclasses) {
		for (ClassInfo subclass : subclasses) {
			((LegacyClassInfo)subclass).extend(superclass);
			extend(subclass, subclass.directSubclasses());
		}
	}

	private void implement(LegacyClassInfo implementingClass, LegacyInterfaceInfo interfaceInfo) {

		ClassInfo interfaceClass = classNameToClassInfo.get(interfaceInfo.name());

		if (interfaceClass != null) {
			if (!implementingClass.directInterfaces().contains(interfaceClass)) {
				LOGGER.debug(" - Setting {} implements {}", implementingClass.simpleName(), ((LegacyClassInfo)interfaceClass).simpleName());
				implementingClass.directInterfaces().add((LegacyClassInfo)interfaceClass);
			}

			if (!interfaceClass.directImplementingClasses().contains(implementingClass)) {
				interfaceClass.directImplementingClasses().add(implementingClass);
			}

			for (ClassInfo subClassInfo : implementingClass.directSubclasses()) {
				implement((LegacyClassInfo) subClassInfo, interfaceInfo);
			}
		} else {
			LOGGER.debug(" - No LegacyClassInfo found for interface class: {}", interfaceInfo.name());
		}
	}

	public void process(final InputStream inputStream) throws IOException {

		LegacyClassInfo classInfo = new LegacyClassInfo(inputStream);

		String className = classInfo.name();
		String superclassName = classInfo.superclassName();

		LOGGER.debug("Processing: {} -> {}", className, superclassName);

		if (className != null) {

			ClassInfo thisClassInfo = classNameToClassInfo.get(className);

			if (thisClassInfo == null) {
				thisClassInfo = classInfo;
				classNameToClassInfo.put(className, thisClassInfo);
			}

			if (!((LegacyClassInfo)thisClassInfo).hydrated()) {

				((LegacyClassInfo)thisClassInfo).hydrate(classInfo);

				ClassInfo superclassInfo = classNameToClassInfo.get(superclassName);
				if (superclassInfo == null) {
					classNameToClassInfo.put(superclassName, new LegacyClassInfo(superclassName, thisClassInfo));
				} else {
					((LegacyClassInfo)superclassInfo).addSubclass(thisClassInfo);
				}
			}

			if (((LegacyClassInfo)thisClassInfo).isEnum()) {
				LOGGER.debug("Registering enum class: {}", thisClassInfo.name());
				enumTypes.add(thisClassInfo.getUnderlyingClass());
			}
		}
	}

	private void load(String... packages) {
		classPaths.clear();
		classNameToClassInfo.clear();
		annotationNameToClassInfo.clear();
		interfaceNameToClassInfo.clear();

		for (String packageName : packages) {
			String path = packageName.replace(".", "/");
			// ensure classpath entries are complete, to ensure we don't accidentally admit partial matches.
			if (!path.endsWith("/")) {
				path = path.concat("/");
			}
			classPaths.add(path);
		}

		new ClassPathScanner().scan(classPaths, this);
	}

	public ClassInfo getClass(String fqn) {
		return classNameToClassInfo.get(fqn);
	}

	// all classes, including interfaces will be registered in classNameToClassInfo map
	public ClassInfo getClassSimpleName(String fullOrPartialClassName) {
		return getClassInfo(fullOrPartialClassName, classNameToClassInfo);
	}


	public ClassInfo getClassInfoForInterface(String fullOrPartialClassName) {
		ClassInfo classInfo = getClassSimpleName(fullOrPartialClassName);
		if (classInfo != null && classInfo.isInterface()) {
			return classInfo;
		}
		return null;
	}

	private LegacyClassInfo getClassInfo(String fullOrPartialClassName, Map<String, ClassInfo> infos) {
		ClassInfo match = null;
		for (String fqn : infos.keySet()) {
			if (fqn.endsWith("." + fullOrPartialClassName) || fqn.equals(fullOrPartialClassName)) {
				if (match == null) {
					match = infos.get(fqn);
				} else {
					throw new MappingException("More than one class has simple name: " + fullOrPartialClassName);
				}
			}
		}
		return (LegacyClassInfo) match;
	}

	public List<ClassInfo> getClassInfosWithAnnotation(String annotation) {
		return annotationNameToClassInfo.get(annotation);
	}

	private void registerDefaultFieldConverters(ClassInfo classInfo, LegacyFieldInfo fieldInfo) {

		if (!fieldInfo.hasPropertyConverter() && !fieldInfo.hasCompositeConverter()) {

			if (fieldInfo.getTypeDescriptor().contains(dateSignature)) {
				setDateFieldConverter(fieldInfo);
			} else if (fieldInfo.getTypeDescriptor().contains(bigIntegerSignature)) {
				setBigIntegerFieldConverter(fieldInfo);
			} else if (fieldInfo.getTypeDescriptor().contains(bigDecimalSignature)) {
				setBigDecimalConverter(fieldInfo);
			} else if (fieldInfo.getTypeDescriptor().contains(byteArraySignature)) {
				fieldInfo.setPropertyConverter(ConvertibleTypes.getByteArrayBase64Converter());
			} else if (fieldInfo.getTypeDescriptor().contains(byteArrayWrapperSignature)) {
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
					fieldInfo.setPropertyConverter(new ProxyAttributeConverter(entityAttributeType, ClassUtils.getType(graphTypeDescriptor), this.conversionCallbackRegistry));
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


	private void setEnumFieldConverter(LegacyFieldInfo fieldInfo, Class enumClass) {
		if (fieldInfo.isArray()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumArrayConverter(enumClass));
		} else if (fieldInfo.isIterable()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumCollectionConverter(enumClass, fieldInfo.getCollectionClassname()));
		} else {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getEnumConverter(enumClass));
		}
	}

	private void setBigDecimalConverter(LegacyFieldInfo fieldInfo) {
		if (fieldInfo.isArray()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalArrayConverter());
		} else if (fieldInfo.isIterable()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalCollectionConverter(fieldInfo.getCollectionClassname()));
		} else {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigDecimalConverter());
		}
	}

	private void setBigIntegerFieldConverter(LegacyFieldInfo fieldInfo) {
		if (fieldInfo.isArray()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerArrayConverter());
		} else if (fieldInfo.isIterable()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerCollectionConverter(fieldInfo.getCollectionClassname()));
		} else {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getBigIntegerConverter());
		}
	}

	private void setDateFieldConverter(LegacyFieldInfo fieldInfo) {
		if (fieldInfo.isArray()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getDateArrayConverter());
		} else if (fieldInfo.isIterable()) {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getDateCollectionConverter(fieldInfo.getCollectionClassname()));
		} else {
			fieldInfo.setPropertyConverter(ConvertibleTypes.getDateConverter());
		}
	}

	// leaky for spring
	public Map<String, ClassInfo> getClassInfoMap() {
		return classNameToClassInfo;
	}

	public List<ClassInfo> getClassInfos(String interfaceName) {
		return interfaceNameToClassInfo.get(interfaceName);
	}
}
