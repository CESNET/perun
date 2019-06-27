package cz.metacentrum.perun.scim.api.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Group resource type for SCIM protocol.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class GroupSCIM extends Resource {

	@JsonProperty
	private List<String> schemas;

	@JsonProperty
	private String displayName;

	@JsonProperty
	private List<MemberSCIM> members;

	public GroupSCIM(Long id, Long externalId, Meta meta, List<String> schemas, String displayName, List<MemberSCIM> members) {
		super(id, externalId, meta);
		this.schemas = schemas;
		this.displayName = displayName;
		this.members = members;
	}

	public GroupSCIM(Long id, Long externalId, Meta meta) {
		super(id, externalId, meta);
	}

	public GroupSCIM() {
	}

	public List<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public List<MemberSCIM> getMembers() {
		return members;
	}

	public void setMembers(List<MemberSCIM> members) {
		this.members = members;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GroupSCIM)) return false;
		if (!super.equals(o)) return false;

		GroupSCIM groupSCIM = (GroupSCIM) o;

		if (getSchemas() != null ? !getSchemas().equals(groupSCIM.getSchemas()) : groupSCIM.getSchemas() != null)
			return false;
		if (getDisplayName() != null ? !getDisplayName().equals(groupSCIM.getDisplayName()) : groupSCIM.getDisplayName() != null)
			return false;
		return getMembers() != null ? getMembers().equals(groupSCIM.getMembers()) : groupSCIM.getMembers() == null;

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (getSchemas() != null ? getSchemas().hashCode() : 0);
		result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
		result = 31 * result + (getMembers() != null ? getMembers().hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "GroupSCIM{" +
				"schemas=" + schemas +
				", displayName='" + displayName + '\'' +
				", members=" + members +
				'}';
	}
}
