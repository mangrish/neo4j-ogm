package org.neo4j.ogm.metadata.impl.legacy;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by markangrish on 30/12/2016.
 */
public class LegacyInterfacesInfo {

	private final Map<String, LegacyInterfaceInfo> interfaceMap = new HashMap<>();

	public LegacyInterfacesInfo() {
	}

	public LegacyInterfacesInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
		int interfaceCount = dataInputStream.readUnsignedShort();
		for (int i = 0; i < interfaceCount; i++) {
			String interfaceName = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');
			interfaceMap.put(interfaceName, new LegacyInterfaceInfo(interfaceName));
		}
	}

	public Collection<LegacyInterfaceInfo> list() {
		return interfaceMap.values();
	}

	public LegacyInterfaceInfo get(String interfaceName) {
		return interfaceMap.get(interfaceName);
	}

	void add(LegacyInterfaceInfo interfaceInfo) {
		interfaceMap.put(interfaceInfo.name(), interfaceInfo);
	}

	public void append(LegacyInterfacesInfo interfacesInfo) {
		for (LegacyInterfaceInfo interfaceInfo : interfacesInfo.list()) {
			add(interfaceInfo);
		}
	}
}
