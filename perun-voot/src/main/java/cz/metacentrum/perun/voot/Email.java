package cz.metacentrum.perun.voot;

/**
 * Class defines email, which is multi-valued attribute encoded according to the OpenSocial specification.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class Email {

	//value of email, e.g. 374128@mail.muni.cz
	private String value;

	//type of email, e.g. 'mail'
	private String type;

	/**
	 * Return value of email, e.g. 374128@mail.muni.cz.
	 *
	 * @return value of email
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set value of email, e.g. 374128@mail.muni.cz.
	 *
	 * @param value    value of email
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Return type of email, e.g. 'mail'.
	 *
	 * @return type of email
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set type of email, e.g. 'mail'.
	 *
	 * @param type	  type of email
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 23;
		result = prime * result + (type != null ? type.hashCode() : 0);
		result = prime * result + (value != null ? value.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final Email other = (Email) obj;

		if ((type == null) ? (other.type != null) : !type.equals(other.type)) {
			return false;
		}
		if ((value == null) ? (other.value != null) : !value.equals(other.value)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString(){
		return new StringBuilder().append(getClass().getSimpleName()).append(":[")
				.append("type='").append(getType()).append("', ")
				.append("value='").append(getValue()).append("']").toString();
	}
}
