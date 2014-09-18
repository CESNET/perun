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
	// TODO kontakty na spravce facility

	public Facility() {
	}

	public Facility(int id, String name) {
		super(id);
		this.name = name;
	}

	public Facility(int id, String name, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append( ":[id='").append(getId()).append("', name='").append(name).append("']").toString();
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
