package cz.metacentrum.perun.core.api;

/**
 * Class represents facility.
 *
 * @author Michal Prochazka
 */
public class Facility extends Auditable implements Comparable<PerunBean> {

	private String name;
	private String description;

	public Facility() {
	}

	public Facility(int id, String name) {
		super(id);
		this.name = name;
	}

	public Facility(int id, String name, String description) {
		super(id);
		this.name = name;
		this.description = description;
	}

	public Facility(int id, String name, String description, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.description = description;
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
	
	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(
			", description=<").append(getDescription() == null ? "\\0" : BeansUtils.createEscaping(getDescription())).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append( ":[id='").append(getId()).append("', name='").append(name).append(
				"', description='").append(description).append("']").toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getId();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Facility other = (Facility) obj;
		if (getId() != other.getId()) {
			return false;
		}
		if (this.name == null ? other.getName() != null : !this.name.equals(other.getName())) {
			return false;
		}
		if (description == null ? other.getDescription() != null : !this.description.equals(other.getDescription())) {
			return false;
		}
		
		return true;
	}

	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof Facility) {
			Facility facility = (Facility) perunBean;
			if (this.getName() == null && facility.getName() != null) return -1;
			if (facility.getName() == null && this.getName() != null) return 1;
			if (this.getName() == null && facility.getName() == null) return 0;
			return this.getName().compareToIgnoreCase(facility.getName());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}
}
