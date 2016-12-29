package org.neo4j.ogm.metadata.impl.legacy;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.AnnotationsInfo;
import org.neo4j.ogm.metadata.ConstantPool;

/**
 * Created by markangrish on 29/12/2016.
 */
public class LegacyAnnotationsInfo implements AnnotationsInfo {


	private final Map<String, AnnotationInfo> classAnnotations = new HashMap<>();

	public LegacyAnnotationsInfo() {}

	public LegacyAnnotationsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
		int attributesCount = dataInputStream.readUnsignedShort();
		for (int i = 0; i < attributesCount; i++) {
			String attributeName = constantPool.readString(dataInputStream.readUnsignedShort());
			int attributeLength = dataInputStream.readInt();
			if ("RuntimeVisibleAnnotations".equals(attributeName)) {
				int annotationCount = dataInputStream.readUnsignedShort();
				for (int m = 0; m < annotationCount; m++) {
					AnnotationInfo info = new LegacyAnnotationInfo(dataInputStream, constantPool);
					// todo: maybe register just the annotations we're interested in.
					classAnnotations.put(info.getName(), info);
				}
			}
			else {
				dataInputStream.skipBytes(attributeLength);
			}
		}
	}

	public Collection<AnnotationInfo> list() {
		return classAnnotations.values();
	}

	/**
	 * @param annotationName The fully-qualified class name of the annotation type
	 * @return The {@link AnnotationInfo} that matches the given name or <code>null</code> if it's not present
	 */
	public AnnotationInfo get(String annotationName) {
		return classAnnotations.get(annotationName);
	}

	void add(AnnotationInfo annotationInfo) {
		classAnnotations.put(annotationInfo.getName(), annotationInfo);
	}

	public void append(AnnotationsInfo annotationsInfo) {
		for (AnnotationInfo annotationInfo : annotationsInfo.list()) {
			add(annotationInfo);
		}
	}
}
