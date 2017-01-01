package org.neo4j.ogm.metadata.impl.legacy;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.metadata.*;

/**
 * Created by markangrish on 30/12/2016.
 */
public class LegacyMethodsInfo  {


	private final Map<String, LegacyMethodInfo> methods = new HashMap<>();
	private final Map<String, LegacyMethodInfo> getters = new HashMap<>();
	private final Map<String, LegacyMethodInfo> setters = new HashMap<>();

	public LegacyMethodsInfo() {}

	public LegacyMethodsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
		// get the method information for this class
		int methodCount = dataInputStream.readUnsignedShort();
		for (int i = 0; i < methodCount; i++) {
			dataInputStream.skipBytes(2); // access_flags
			String methodName = constantPool.readString(dataInputStream.readUnsignedShort()); // name_index
			String descriptor = constantPool.readString(dataInputStream.readUnsignedShort()); // descriptor
			LegacyObjectAnnotations objectAnnotations = new LegacyObjectAnnotations();
			int attributesCount = dataInputStream.readUnsignedShort();
			String typeParameterDescriptor = null; // available as an attribute for parameterised collections
			for (int j = 0; j < attributesCount; j++) {
				String attributeName = constantPool.readString(dataInputStream.readUnsignedShort());
				int attributeLength = dataInputStream.readInt();
				if ("RuntimeVisibleAnnotations".equals(attributeName)) {
					int annotationCount = dataInputStream.readUnsignedShort();
					for (int m = 0; m < annotationCount; m++) {
						LegacyAnnotationInfo info = new LegacyAnnotationInfo(dataInputStream, constantPool);
						// todo: maybe register just the annotations we're interested in.
						objectAnnotations.put(info.getName(), info);
					}
				} else if ("Signature".equals(attributeName)) {
					String signature = constantPool.readString(dataInputStream.readUnsignedShort());
					if (signature.contains("<")) {
						typeParameterDescriptor = signature.substring(signature.indexOf('<') + 1, signature.indexOf('>'));
					}
				} else {
					dataInputStream.skipBytes(attributeLength);
				}
			}
			if (!methodName.equals("<init>") && !methodName.equals("<clinit>") && objectAnnotations.get(Transient.class.getCanonicalName()) == null) {
				addMethod(new LegacyMethodInfo(methodName, descriptor, typeParameterDescriptor, objectAnnotations));
			}
		}
	}

	public Collection<LegacyMethodInfo> methods() {
		return methods.values();
	}

	public Collection<LegacyMethodInfo> getters() {
		return getters.values();
	}

	public Collection<LegacyMethodInfo> setters() {
		return setters.values();
	}

	public LegacyMethodInfo get(String methodName) {
		return methods.get(methodName);
	}

	public void append(LegacyMethodsInfo methodsInfo) {
		for (LegacyMethodInfo methodInfo : methodsInfo.methods()) {
			if (!methods.containsKey(methodInfo.getName())) {
				addMethod(methodInfo);
			}
		}
	}

	void removeGettersAndSetters(LegacyMethodInfo methodInfo) {
		getters.remove(methodInfo.getName());
		setters.remove(methodInfo.getName());
	}

	private void addMethod(LegacyMethodInfo methodInfo) {
		String methodName = methodInfo.getName();
		methods.put(methodName, methodInfo);
		if (methodInfo.isGetter()) {
			getters.put(methodName, methodInfo);
		}
		else if (methodInfo.isSetter()) {
			setters.put(methodName, methodInfo);
		}
	}
}
