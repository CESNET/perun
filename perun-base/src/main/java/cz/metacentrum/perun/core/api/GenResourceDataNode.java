package cz.metacentrum.perun.core.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class represents a node in the hierarchy of hashed data for provisioning.
 * This node is meant for a resource and additionally, contains vo id.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GenResourceDataNode extends GenDataNode {

	private final Integer voId;

	private GenResourceDataNode(Map<Integer, GenDataNode> children, Map<Integer, Integer> members, Integer vo) {
		super(children, members);
		this.voId = vo;
	}

	@JsonProperty("v")
	public Integer getVoId() {
		return voId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GenResourceDataNode)) return false;
		if (!super.equals(o)) return false;

		GenResourceDataNode that = (GenResourceDataNode) o;

		return getVoId() != null ? getVoId().equals(that.getVoId()) : that.getVoId() == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (getVoId() != null ? getVoId().hashCode() : 0);
		return result;
	}

	public static class Builder {

		private Map<Integer, GenDataNode> children = new HashMap<>();
		private Map<Integer, Integer> members = new HashMap<>();
		private Integer voId;

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

		public Builder voId(Integer voId) {
			this.voId = voId;
			return this;
		}

		public GenResourceDataNode build() {
			return new GenResourceDataNode(children, members, voId);
		}
	}
}
