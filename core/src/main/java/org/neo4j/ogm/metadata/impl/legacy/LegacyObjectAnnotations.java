package org.neo4j.ogm.metadata.impl.legacy;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.typeconversion.*;
import org.neo4j.ogm.classloader.MetaDataClassLoader;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ObjectAnnotations;
import org.neo4j.ogm.typeconversion.DateLongConverter;
import org.neo4j.ogm.typeconversion.DateStringConverter;
import org.neo4j.ogm.typeconversion.EnumStringConverter;
import org.neo4j.ogm.typeconversion.NumberStringConverter;

/**
 * Created by markangrish on 30/12/2016.
 */
public class LegacyObjectAnnotations implements ObjectAnnotations {


	private String objectName; // fully qualified class, method or field name.
	private final Map<String, AnnotationInfo> annotations = new HashMap<>();

	public String getName() {
		return objectName;
	}

	public void setName(String objectName) {
		this.objectName = objectName;
	}

	public void put(String key, AnnotationInfo value) {
		annotations.put(key, value);
	}

	public AnnotationInfo get(String key) {
		return annotations.get(key);
	}

	public boolean isEmpty() {
		return annotations.isEmpty();
	}

	public Object getConverter() {

		// try to get a custom type converter
		AnnotationInfo customType = get(Convert.CLASS);
		if (customType != null) {
			String classDescriptor = customType.get(Convert.CONVERTER, null);
			if (classDescriptor == null) {
				return null; // will have a default proxy converter applied later on
			}

			try {
				String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
				Class<?> clazz = MetaDataClassLoader.loadClass(className);//Class.forName(className);
				return clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// try to find a pre-registered type annotation. this is very clumsy, but at least it is done only once
		AnnotationInfo dateLongConverterInfo = get(DateLong.CLASS);
		if (dateLongConverterInfo != null) {
			return new DateLongConverter();
		}

		AnnotationInfo dateStringConverterInfo = get(DateString.CLASS);
		if (dateStringConverterInfo != null) {
			String format = dateStringConverterInfo.get(DateString.FORMAT, DateString.ISO_8601);
			return new DateStringConverter(format);
		}

		AnnotationInfo enumStringConverterInfo = get(EnumString.CLASS);
		if (enumStringConverterInfo != null) {
			String classDescriptor = enumStringConverterInfo.get(EnumString.TYPE, null);
			String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
			try {
				Class clazz = MetaDataClassLoader.loadClass(className);//Class.forName(className);
				return new EnumStringConverter(clazz);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		AnnotationInfo numberStringConverterInfo = get(NumberString.CLASS);
		if (numberStringConverterInfo != null) {
			String classDescriptor = numberStringConverterInfo.get(NumberString.TYPE, null);
			String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
			try {
				Class clazz = MetaDataClassLoader.loadClass(className);//Class.forName(className);
				return new NumberStringConverter(clazz);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}
}
