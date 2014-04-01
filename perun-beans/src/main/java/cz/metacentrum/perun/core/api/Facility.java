package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Class represents facility.
 *
 * @author  Michal Prochazka
 */
public class Facility extends Auditable implements Comparable<Facility> {

	private String name;
	private String type = "general";
	// TODO kontakty na spravce facility

	public Facility() {
	}

	public Facility(int id, String name) {
		super(id);
		this.name = name;
	}

	public Facility(int id, String name, String type) {
		this(id, name);
		this.type = type;
	}

	public Facility(int id, String name, String type, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the type for this instance.
	 *
	 * @return The type.
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Sets the type for this instance.
	 *
	 * @param type The type.
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String serializeToString() {
		return this.getClass().getSimpleName() +":[" +
			"id=<" + getId() + ">" +
			", name=<" + (getName() == null ? "\\0" : BeansUtils.createEscaping(getName())) + ">" +
			", type=<" + (getType() == null ? "\\0" : BeansUtils.createEscaping(getType())) + ">" +
			']';
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +  ":[id='" + getId() + "', name='" + name + "', type='" + type + "']";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getId();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Facility other = (Facility) obj;
		if (getId() != other.getId())
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public int compareTo(Facility facility) {
		if (facility == null || this.name == null) throw new InternalErrorRuntimeException(new NullPointerException("Facility facility or name"));
		return this.name.compareTo(facility.getName());
	}

}
