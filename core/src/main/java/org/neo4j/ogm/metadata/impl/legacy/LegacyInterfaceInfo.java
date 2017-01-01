package org.neo4j.ogm.metadata.impl.legacy;


/**
 * Created by markangrish on 30/12/2016.
 */
public class LegacyInterfaceInfo {

	private final String interfaceName;

	public LegacyInterfaceInfo(String name) {
		this.interfaceName = name;
	}

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

		LegacyInterfaceInfo that = (LegacyInterfaceInfo) o;

		return interfaceName.equals(that.name());
	}

	@Override
	public int hashCode() {
		return interfaceName.hashCode();
	}
}
