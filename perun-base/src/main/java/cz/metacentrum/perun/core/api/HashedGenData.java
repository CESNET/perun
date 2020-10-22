package cz.metacentrum.perun.core.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class representing provisioning data structure with hashes.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class HashedGenData {
	private final Map<String, Map<String, Object>> attributes;
	private final Map<Integer, GenDataNode> hierarchy = new HashMap<>();

	public HashedGenData(Map<String, Map<String, Object>> attributes, GenDataNode hierarchy, Integer facilityId) {
		this.attributes = attributes;
		this.hierarchy.put(facilityId, hierarchy);
	}

	public Map<String, Map<String, Object>> getAttributes() {
		return attributes;
	}

	public Map<Integer, GenDataNode> getHierarchy() {
		return hierarchy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HashedGenData that = (HashedGenData) o;
		return Objects.equals(getAttributes(), that.getAttributes()) &&
				Objects.equals(getHierarchy(), that.getHierarchy());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getAttributes(), getHierarchy());
	}

	@Override
	public String toString() {
		return "HashedGenData[" +
				"attributes=" + attributes +
				", hierarchy=" + hierarchy +
				']';
	}
}
