package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public class Service extends Auditable {
	private String name;

	public Service(){
		super();
	}

	public Service(int id, String name){
		super(id);
		this.name = name;
	}

	public Service(int id, String name, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid){
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
		Service other = (Service) obj;
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
	public String serializeToString() {
		return this.getClass().getSimpleName() +":[" +
			"id=<" + getId() + ">" +
			", name=<" + (getName() == null ? "\\0" : BeansUtils.createEscaping(getName())) + ">" +
			']';
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":[id='" + getId() + "', name='" + name
			+ "']";
	}


}
