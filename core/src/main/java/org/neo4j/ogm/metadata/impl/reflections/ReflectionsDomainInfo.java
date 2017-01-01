package org.neo4j.ogm.metadata.impl.reflections;


import java.util.*;

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
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
	}

	public void finish() {
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
}
