package cz.metacentrum.perun.scim.api.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * SCIM resource type.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResourceTypeSCIM {

	@JsonProperty
	private List<String> schemas;

	@JsonProperty
	private String id;

	@JsonProperty
	private String name;

	@JsonProperty
	private String description;

	@JsonProperty
	private String endpoint;

	@JsonProperty
	private String schema;

	@JsonProperty
	private Meta meta;

	public ResourceTypeSCIM(List<String> schemas, String id, String name, String description, String endpoint, String schema, Meta meta) {
		this.schemas = schemas;
		this.id = id;
		this.name = name;
		this.description = description;
		this.endpoint = endpoint;
		this.schema = schema;
		this.meta = meta;
	}

	public ResourceTypeSCIM() {
	}

	public List<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
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
		if (!(o instanceof ResourceTypeSCIM)) return false;

		ResourceTypeSCIM that = (ResourceTypeSCIM) o;

		if (getSchemas() != null ? !getSchemas().equals(that.getSchemas()) : that.getSchemas() != null) return false;
		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
		if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
		if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
			return false;
		if (getEndpoint() != null ? !getEndpoint().equals(that.getEndpoint()) : that.getEndpoint() != null)
			return false;
		if (getSchema() != null ? !getSchema().equals(that.getSchema()) : that.getSchema() != null) return false;
		return getMeta() != null ? getMeta().equals(that.getMeta()) : that.getMeta() == null;

	}

	@Override
	public int hashCode() {
		int result = getSchemas() != null ? getSchemas().hashCode() : 0;
		result = 31 * result + (getId() != null ? getId().hashCode() : 0);
		result = 31 * result + (getName() != null ? getName().hashCode() : 0);
		result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
		result = 31 * result + (getEndpoint() != null ? getEndpoint().hashCode() : 0);
		result = 31 * result + (getSchema() != null ? getSchema().hashCode() : 0);
		result = 31 * result + (getMeta() != null ? getMeta().hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ResourceTypeSCIM{" +
				"schemas=" + schemas +
				", id='" + id + '\'' +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", endpoint='" + endpoint + '\'' +
				", schema='" + schema + '\'' +
				", meta=" + meta +
				'}';
	}
}
