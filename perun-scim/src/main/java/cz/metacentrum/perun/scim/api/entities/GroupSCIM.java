package cz.metacentrum.perun.scim.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class GroupSCIM {

	@JsonIgnore
	private Resource resource;

	@JsonProperty
	private List<String> schemas;

	@JsonProperty
	private String displayName;

	@JsonProperty
	private List<MemberSCIM> members;

	public GroupSCIM(Long id, Long externalId, Meta meta, List<String> schemas, String displayName, List<MemberSCIM> members) {
		resource = new Resource();
		resource.setId(id);
		resource.setExternalId(externalId);
		resource.setMeta(meta);
		this.schemas = schemas;
		this.displayName = displayName;
		this.members = members;
	}

	public GroupSCIM(Long id, Long externalId, Meta meta) {
		resource = new Resource();
		resource.setId(id);
		resource.setExternalId(externalId);
		resource.setMeta(meta);
	}

	public GroupSCIM() {
		resource = new Resource();
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

	public void setId(Long id) {
		resource.setId(id);
	}

	public void setExternalId(Long externalId) {
		resource.setExternalId(externalId);
	}

	public void setMeta(Meta meta) {
		resource.setMeta(meta);
	}

	public Long getId() {
		return resource.getId();
	}

	public Long getExternalId() {
		return resource.getExternalId();
	}

	public Meta getMeta() {
		return resource.getMeta();
	}

	@JsonIgnore
	public Resource getResource() {
		return resource;
	}

	@JsonIgnore
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GroupSCIM)) return false;

		GroupSCIM groupSCIM = (GroupSCIM) o;

		if (getSchemas() != null ? !getSchemas().equals(groupSCIM.getSchemas()) : groupSCIM.getSchemas() != null)
			return false;
		if (getDisplayName() != null ? !getDisplayName().equals(groupSCIM.getDisplayName()) : groupSCIM.getDisplayName() != null)
			return false;
		if (getResource() != null ? !getResource().equals(groupSCIM.getResource()) : groupSCIM.getResource() != null)
			return false;
		return getMembers() != null ? getMembers().equals(groupSCIM.getMembers()) : groupSCIM.getMembers() == null;

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (getSchemas() != null ? getSchemas().hashCode() : 0);
		result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
		result = 31 * result + (getMembers() != null ? getMembers().hashCode() : 0);
		result = 31 * result + (getResource() != null ? getResource().hashCode() : 0);
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
