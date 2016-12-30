package org.neo4j.ogm.metadata.impl.reflections;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.typeconversion.ConversionCallback;

/**
 * Created by markangrish on 29/12/2016.
 */
public class ReflectionsDomainInfo implements DomainInfo {

	@Override
	public void process(InputStream inputStream) throws IOException {

	}

	@Override
	public void finish() {

	}

	@Override
	public ClassInfo getClassSimpleName(String name) {
		return null;
	}

	@Override
	public List<ClassInfo> getClassInfosWithAnnotation(String annotationName) {
		return null;
	}

	@Override
	public ClassInfo getClassInfoForInterface(String interfaceName) {
		return null;
	}

	@Override
	public Map<String, ClassInfo> getClassInfoMap() {
		return null;
	}

	@Override
	public List<ClassInfo> getClassInfos(String interfaceName) {
		return null;
	}

	@Override
	public void registerConversionCallback(ConversionCallback conversionCallback) {

	}

	@Override
	public ClassInfo getClass(String name) {
		return null;
	}
}
