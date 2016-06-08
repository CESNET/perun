package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Michal Karm Babacek
 */
public class ServicesPackage extends Auditable implements Comparable<PerunBean> {
	private String description;
	private String name;

	public ServicesPackage(){
	}

	public ServicesPackage(int id, String description, String name) {
		super(id);
		this.description = description;
		this.name = name;
	}

	public ServicesPackage(int id, String description, String name, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.description = description;
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
		result = prime * result + this.getId();
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
		ServicesPackage other = (ServicesPackage) obj;
		if (this.getId() != other.getId())
			return false;
		return true;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[").append(
			"id='").append(this.getId()).append("'").append(
			"description='").append(description).append("'").append(
			"name='").append(name).append("'").append(
			"]").toString();
	}

	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof ServicesPackage) {
			ServicesPackage servicesPackage = (ServicesPackage) perunBean;
			int compare;
			//Compare on name
			if (this.getName()== null && servicesPackage.getName() != null) compare = -1;
			else if (servicesPackage.getName() == null && this.getName() != null) compare = 1;
			else if (this.getName() == null && servicesPackage.getName() == null) compare = 0;
			else compare = this.getName().compareToIgnoreCase(servicesPackage.getName());
			if(compare != 0) return compare;
			//Compare on description
			if (this.getDescription()== null && servicesPackage.getDescription() != null) compare = -1;
			else if (servicesPackage.getDescription() == null && this.getDescription() != null) compare = 1;
			else if (this.getDescription()== null && servicesPackage.getDescription() == null) compare = 0;
			else compare = this.getDescription().compareToIgnoreCase(servicesPackage.getDescription());
			if(compare != 0) return compare;
			//Compare to id if not
			return (this.getId() - perunBean.getId());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}
}
