package cz.metacentrum.perun.scim.api.entities;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * User resource type for SCIM protocol.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class UserSCIM extends Resource {

	@JsonProperty
	private List<String> schemas;

	@JsonProperty
	private String userName;

	@JsonProperty
	private String name;

	@JsonProperty
	private String displayName;

	@JsonProperty
	private List<EmailSCIM> emails;

	public UserSCIM(Long id, Long externalId, Meta meta, List<String> schemas, String userName, String name, String displayName, List<EmailSCIM> emails) {
		super(id, externalId, meta);
		this.schemas = schemas;
		this.userName = userName;
		this.name = name;
		this.displayName = displayName;
		this.emails = emails;
	}

	public UserSCIM(Long id, Long externalId, Meta meta) {
		super(id, externalId, meta);
	}

	public UserSCIM() {
	}

	public List<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public List<EmailSCIM> getEmails() {
		return emails;
	}

	public void setEmails(List<EmailSCIM> emails) {
		this.emails = emails;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UserSCIM)) return false;
		if (!super.equals(o)) return false;

		UserSCIM userSCIM = (UserSCIM) o;

		if (getSchemas() != null ? !getSchemas().equals(userSCIM.getSchemas()) : userSCIM.getSchemas() != null)
			return false;
		if (getUserName() != null ? !getUserName().equals(userSCIM.getUserName()) : userSCIM.getUserName() != null)
			return false;
		if (getName() != null ? !getName().equals(userSCIM.getName()) : userSCIM.getName() != null) return false;
		if (getDisplayName() != null ? !getDisplayName().equals(userSCIM.getDisplayName()) : userSCIM.getDisplayName() != null)
			return false;
		return getEmails() != null ? getEmails().equals(userSCIM.getEmails()) : userSCIM.getEmails() == null;

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (getSchemas() != null ? getSchemas().hashCode() : 0);
		result = 31 * result + (getUserName() != null ? getUserName().hashCode() : 0);
		result = 31 * result + (getName() != null ? getName().hashCode() : 0);
		result = 31 * result + (getDisplayName() != null ? getDisplayName().hashCode() : 0);
		result = 31 * result + (getEmails() != null ? getEmails().hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "UserSCIM{" +
				"schemas=" + schemas +
				", userName='" + userName + '\'' +
				", name='" + name + '\'' +
				", displayName='" + displayName + '\'' +
				", emails=" + emails +
				'}';
	}
}
