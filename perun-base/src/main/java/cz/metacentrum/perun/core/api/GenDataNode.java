package cz.metacentrum.perun.core.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a node in the hierarchy of hashed data for provisioning.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GenDataNode {

	private final List<String> hashes;
	private final List<GenDataNode> children;
	private final List<GenMemberDataNode> members;

	private GenDataNode(List<String> hashes, List<GenDataNode> children, List<GenMemberDataNode> members) {
		this.hashes = hashes;
		this.children = children;
		this.members = members;
	}

	@JsonProperty("h")
	public List<String> getHashes() {
		return Collections.unmodifiableList(hashes);
	}

	@JsonProperty("c")
	public List<GenDataNode> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@JsonProperty("m")
	public List<GenMemberDataNode> getMembers() {
		return Collections.unmodifiableList(members);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenDataNode that = (GenDataNode) o;
		return Objects.equals(hashes, that.hashes) &&
				Objects.equals(children, that.children) &&
				Objects.equals(members, that.members);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hashes, children, members);
	}

	public static class Builder {

		private List<String> hashes = new ArrayList<>();
		private List<GenDataNode> children = new ArrayList<>();
		private List<GenMemberDataNode> members = new ArrayList<>();

		public Builder hashes(List<String> hashes) {
			this.hashes = hashes;
			return this;
		}

		public Builder children(List<GenDataNode> children) {
			this.children = children;
			return this;
		}

		public Builder members(List<GenMemberDataNode> members) {
			this.members = members;
			return this;
		}

		public GenDataNode build() {
			return new GenDataNode(hashes, children, members);
		}
	}
}
