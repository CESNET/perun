package cz.metacentrum.perun.scim.api.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Email for user resources.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class EmailSCIM {

	@JsonProperty
	private String type;

	@JsonProperty
	private String value;

	@JsonProperty
	private Boolean primary;

	public EmailSCIM(String type, String value, Boolean primary) {
		this.type = type;
		this.value = value;
		this.primary = primary;
	}

	public EmailSCIM() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getPrimary() {
		return primary;
	}

	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EmailSCIM)) return false;

		EmailSCIM emailSCIM = (EmailSCIM) o;

		if (getType() != null ? !getType().equals(emailSCIM.getType()) : emailSCIM.getType() != null) return false;
		if (getValue() != null ? !getValue().equals(emailSCIM.getValue()) : emailSCIM.getValue() != null) return false;
		return getPrimary() != null ? getPrimary().equals(emailSCIM.getPrimary()) : emailSCIM.getPrimary() == null;

	}

	@Override
	public int hashCode() {
		int result = getType() != null ? getType().hashCode() : 0;
		result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
		result = 31 * result + (getPrimary() != null ? getPrimary().hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "EmailSCIM{" +
				"type='" + type + '\'' +
				", value='" + value + '\'' +
				", primary=" + primary +
				'}';
	}
}
