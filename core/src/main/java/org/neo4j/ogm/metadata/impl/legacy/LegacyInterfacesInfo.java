package org.neo4j.ogm.metadata.impl.legacy;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.InterfaceInfo;
import org.neo4j.ogm.metadata.InterfacesInfo;

/**
 * Created by markangrish on 30/12/2016.
 */
public class LegacyInterfacesInfo implements InterfacesInfo{

	private final Map<String, InterfaceInfo> interfaceMap = new HashMap<>();

	public LegacyInterfacesInfo() {}

	public LegacyInterfacesInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
		int interfaceCount = dataInputStream.readUnsignedShort();
		for (int i = 0; i < interfaceCount; i++) {
			String interfaceName = constantPool.lookup(dataInputStream.readUnsignedShort()).replace('/', '.');
			interfaceMap.put(interfaceName, new LegacyInterfaceInfo(interfaceName));
		}
	}

	@Override
	public Collection<InterfaceInfo> list() {
		return interfaceMap.values();
	}

	public InterfaceInfo get(String interfaceName) {
		return interfaceMap.get(interfaceName);
	}

	void add(InterfaceInfo interfaceInfo) {
		interfaceMap.put(interfaceInfo.name(), interfaceInfo);
	}

	public void append(InterfacesInfo interfacesInfo) {
		for (InterfaceInfo interfaceInfo : interfacesInfo.list()) {
			add(interfaceInfo);
		}
	}
}
