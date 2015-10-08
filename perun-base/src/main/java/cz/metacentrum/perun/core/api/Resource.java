package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Auditable;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Represents resource.
 *
 * @author  Slavek Licehammer
 */
public class Resource extends Auditable implements Comparable<PerunBean> {
	private int facilityId;
	private int voId;
	private String name;
	private String description;

	/**
	 * Constructs a new instance.
	 */
	public Resource() {
		super();
	}

	public Resource(int id, String name, String description, int facilityId) {
		super(id);
		this.name = name;
		this.description = description;
		this.facilityId = facilityId;
	}

	public Resource(int id, String name, String description, int facilityId,  int voId) {
		this(id, name, description, facilityId);
		this.voId = voId;
	}

	public Resource(int id, String name, String description, int facilityId, String createdAt,
			String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.description = description;
		this.facilityId = facilityId;
	}

	public Resource(int id, String name, String description, int facilityId, int voId, String createdAt,
			String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
		this.name = name;
		this.description = description;
		this.facilityId = facilityId;
		this.voId = voId;
	}

	/**
	 * Gets the name for this instance.
	 *
	 * @return The name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name for this instance.
	 *
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the description for this instance.
	 *
	 * @return The description.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description for this instance.
	 *
	 * @param description The description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public int getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(int facilityId) {
		this.facilityId = facilityId;
	}

	public int getVoId() {
		return voId;
	}

	public void setVoId(int voId) {
		this.voId = voId;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", voId=<").append(getVoId()).append(">").append(
			", facilityId=<").append(getFacilityId()).append(">").append(
			", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(
			", description=<").append(getDescription() == null ? "\\0" : BeansUtils.createEscaping(getDescription())).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[id='").append(getId()
			).append("', voId='").append(voId
			).append("', facilityId='").append(facilityId
			).append("', name='").append(name
			).append("', description='").append(description).append("']").toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Resource other = (Resource) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		if (this.name == null ? other.getName() != null : !this.name.equals(other.getName())) {
			return false;
		}
		if (this.description == null ? other.getDescription() != null : !this.description.equals(other.getDescription())) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof Resource) {
			Resource resource = (Resource) perunBean;
			if (this.getName() == null && resource.getName() != null) return -1;
			if (resource.getName() == null && this.getName() != null) return 1;
			if (this.getName() == null && resource.getName() == null) return 0;
			return this.getName().compareToIgnoreCase(resource.getName());
		} else {
			return (this.getId() - perunBean.getId());
		}
	}
}
