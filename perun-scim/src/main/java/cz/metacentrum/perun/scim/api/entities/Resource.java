package cz.metacentrum.perun.scim.api.entities;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * All resources (user, group, ..) extend from this class, that contains resource
 * id, resource externalId and resource metadata.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Resource {

	@JsonProperty
	private Long id;

	@JsonProperty
	private Long externalId;

	@JsonProperty
	private Meta meta;

	public Resource(Long id, Long externalId, Meta meta) {
		this.id = id;
		this.externalId = externalId;
		this.meta = meta;
	}

	public Resource() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getExternalId() {
		return externalId;
	}

	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Resource)) return false;

		Resource resource = (Resource) o;

		if (getId() != null ? !getId().equals(resource.getId()) : resource.getId() != null) return false;
		if (getExternalId() != null ? !getExternalId().equals(resource.getExternalId()) : resource.getExternalId() != null)
			return false;
		return getMeta() != null ? getMeta().equals(resource.getMeta()) : resource.getMeta() == null;

	}

	@Override
	public int hashCode() {
		int result = getId() != null ? getId().hashCode() : 0;
		result = 31 * result + (getExternalId() != null ? getExternalId().hashCode() : 0);
		result = 31 * result + (getMeta() != null ? getMeta().hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Resource{" +
				"id=" + id +
				", externalId=" + externalId +
				", meta=" + meta +
				'}';
	}
}
