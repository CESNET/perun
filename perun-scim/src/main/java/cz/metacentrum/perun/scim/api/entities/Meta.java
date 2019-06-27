package cz.metacentrum.perun.scim.api.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

/**
 * Metadata of the resource.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Meta {

	@JsonProperty
	private String resourceType;

	@JsonProperty
	private Date created;

	@JsonProperty
	private Date lastModified;

	@JsonProperty
	private String location;

	@JsonProperty
	private String version;

	public Meta(String resourceType, Date created, Date lastModified, String location, String version) {
		this.resourceType = resourceType;
		this.created = created;
		this.lastModified = lastModified;
		this.location = location;
		this.version = version;
	}

	public Meta() {
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Meta)) return false;

		Meta meta = (Meta) o;

		if (getResourceType() != null ? !getResourceType().equals(meta.getResourceType()) : meta.getResourceType() != null)
			return false;
		if (getCreated() != null ? !getCreated().equals(meta.getCreated()) : meta.getCreated() != null) return false;
		if (getLastModified() != null ? !getLastModified().equals(meta.getLastModified()) : meta.getLastModified() != null)
			return false;
		if (getLocation() != null ? !getLocation().equals(meta.getLocation()) : meta.getLocation() != null)
			return false;
		return getVersion() != null ? getVersion().equals(meta.getVersion()) : meta.getVersion() == null;

	}

	@Override
	public int hashCode() {
		int result = getResourceType() != null ? getResourceType().hashCode() : 0;
		result = 31 * result + (getCreated() != null ? getCreated().hashCode() : 0);
		result = 31 * result + (getLastModified() != null ? getLastModified().hashCode() : 0);
		result = 31 * result + (getLocation() != null ? getLocation().hashCode() : 0);
		result = 31 * result + (getVersion() != null ? getVersion().hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Meta{" +
				"resourceType='" + resourceType + '\'' +
				", created=" + created +
				", lastModified=" + lastModified +
				", location='" + location + '\'' +
				", version='" + version + '\'' +
				'}';
	}
}
