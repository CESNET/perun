package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Michal Karm Babacek
 */
public class ServicesPackage extends Auditable {
	private int id;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
		result = prime * result + id;
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
		if (id != other.id)
			return false;
		return true;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[").append(
			"id='").append(id).append("'").append(
			"description='").append(description).append("'").append(
			"name='").append(name).append("'").append(
			"]").toString();
	}
}
