package cz.metacentrum.perun.scim.api.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Member of group resource type.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MemberSCIM {

	@JsonProperty
	private String value;

	@JsonProperty("$ref")
	private String ref;

	@JsonProperty
	private String display;

	public MemberSCIM(String value, String ref, String display) {
		this.value = value;
		this.ref = ref;
		this.display = display;
	}

	public MemberSCIM() {
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MemberSCIM)) return false;

		MemberSCIM that = (MemberSCIM) o;

		if (getValue() != null ? !getValue().equals(that.getValue()) : that.getValue() != null) return false;
		if (getRef() != null ? !getRef().equals(that.getRef()) : that.getRef() != null) return false;
		return getDisplay() != null ? getDisplay().equals(that.getDisplay()) : that.getDisplay() == null;

	}

	@Override
	public int hashCode() {
		int result = getValue() != null ? getValue().hashCode() : 0;
		result = 31 * result + (getRef() != null ? getRef().hashCode() : 0);
		result = 31 * result + (getDisplay() != null ? getDisplay().hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "MemberSCIM{" +
				"value='" + value + '\'' +
				", ref='" + ref + '\'' +
				", display='" + display + '\'' +
				'}';
	}
}
