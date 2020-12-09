package cz.metacentrum.perun.core.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class represents a node in the hierarchy of hashed data for provisioning.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GenDataNode {

	private final Map<Integer, GenDataNode> children;
	private final Map<Integer, Integer> members;

	protected GenDataNode(Map<Integer, GenDataNode> children, Map<Integer, Integer> members) {
		this.children = children;
		this.members = members;
	}

	@JsonProperty("c")
	public Map<Integer, GenDataNode> getChildren() {
		return Collections.unmodifiableMap(children);
	}

	@JsonProperty("m")
	public Map<Integer, Integer> getMembers() {
		return Collections.unmodifiableMap(members);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenDataNode that = (GenDataNode) o;
		return Objects.equals(children, that.children) &&
				Objects.equals(members, that.members);
	}

	@Override
	public int hashCode() {
		return Objects.hash(children, members);
	}

	public static class Builder {

		private Map<Integer, GenDataNode> children = new HashMap<>();
		private Map<Integer, Integer> members = new HashMap<>();

		public Builder hashes() {
			return this;
		}

		public Builder children(Map<Integer, GenDataNode> children) {
			this.children = children;
			return this;
		}

		public Builder members(Map<Integer, Integer> members) {
			this.members = members;
			return this;
		}

		public GenDataNode build() {
			return new GenDataNode(children, members);
		}
	}
}
