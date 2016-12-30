package org.neo4j.ogm.metadata.impl.legacy;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.metadata.*;

/**
 * Created by markangrish on 30/12/2016.
 */
public class LegacyFieldsInfo implements FieldsInfo {


	private static final int STATIC_FIELD = 0x0008;
	private static final int FINAL_FIELD = 0x0010;
	private static final int TRANSIENT_FIELD = 0x0080;

	private final Map<String, FieldInfo> fields = new HashMap<>();

	public LegacyFieldsInfo() {
	}

	public LegacyFieldsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
		// get the field information for this class
		int fieldCount = dataInputStream.readUnsignedShort();
		for (int i = 0; i < fieldCount; i++) {
			int accessFlags = dataInputStream.readUnsignedShort();
			String fieldName = constantPool.readString(dataInputStream.readUnsignedShort()); // name_index
			String descriptor = constantPool.readString(dataInputStream.readUnsignedShort()); // descriptor_index
			int attributesCount = dataInputStream.readUnsignedShort();
			ObjectAnnotations objectAnnotations = new ObjectAnnotations();
			String typeParameterDescriptor = null; // available as an attribute for parameterised collections
			for (int j = 0; j < attributesCount; j++) {
				String attributeName = constantPool.readString(dataInputStream.readUnsignedShort());
				int attributeLength = dataInputStream.readInt();
				if ("RuntimeVisibleAnnotations".equals(attributeName)) {
					int annotationCount = dataInputStream.readUnsignedShort();
					for (int m = 0; m < annotationCount; m++) {
						AnnotationInfo info = new LegacyAnnotationInfo(dataInputStream, constantPool);
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
			if ((accessFlags & (STATIC_FIELD | FINAL_FIELD | TRANSIENT_FIELD)) == 0 && objectAnnotations.get(Transient.class.getCanonicalName()) == null) {
				fields.put(fieldName, new LegacyFieldInfo(fieldName, descriptor, typeParameterDescriptor, objectAnnotations));
			}
		}
	}

	public Collection<FieldInfo> fields() {
		return fields.values();
	}

	public Collection<FieldInfo> compositeFields() {
		List<FieldInfo> fields = new ArrayList<>();
		for (FieldInfo field : fields()) {
			if (field.hasCompositeConverter()) {
				fields.add(field);
			}
		}
		return Collections.unmodifiableList(fields);
	}


	public FieldInfo get(String name) {
		return fields.get(name);
	}

	public void append(FieldsInfo fieldsInfo) {
		for (FieldInfo fieldInfo : fieldsInfo.fields()) {
			if (!fields.containsKey(fieldInfo.getName())) {
				fields.put(fieldInfo.getName(), fieldInfo);
			}
		}
	}
}
