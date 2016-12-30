package org.neo4j.ogm.metadata.impl.legacy;

import org.neo4j.ogm.metadata.InterfaceInfo;

/**
 * Created by markangrish on 30/12/2016.
 */
public class LegacyInterfaceInfo implements InterfaceInfo {

	private final String interfaceName;

	public LegacyInterfaceInfo(String name) {
		this.interfaceName = name;
	}

	@Override
	public String name() {
		return interfaceName;
	}

	public String toString() {
		return name();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		InterfaceInfo that = (InterfaceInfo) o;

		return interfaceName.equals(that.name());
	}

	@Override
	public int hashCode() {
		return interfaceName.hashCode();
	}
}
