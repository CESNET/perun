package cz.metacentrum.perun.core.api;

import java.util.Date;

/**
 * Represents specific ban of user on facility.
 *
 * @author  Michal Stava
 */
public class BanOnFacility extends Ban implements Comparable<PerunBean> {
	private int userId;
	private int facilityId;

	/**
	 * Constructs a new instance.
	 */
	public BanOnFacility() {
		super();
	}

	public BanOnFacility(int id, Date validityTo, String description, int userId, int facilityId) {
		super(id, validityTo, description);
		this.userId = userId;
		this.facilityId = facilityId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(int facilityId) {
		this.facilityId = facilityId;
	}

	@Override
	public String getType() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getSubjectId() {
		return this.getUserId();
	}

	@Override
	public int getTargetId() {
		return this.getFacilityId();
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", userId=<").append(getUserId()).append(">").append(
			", facilityId=<").append(getFacilityId()).append(">").append(
			", validityTo=<").append(getValidityTo() == null ? "\\0" : getValidityTo().getTime()).append(">").append(
			", description=<").append(getDescription() == null ? "\\0" : BeansUtils.createEscaping(getDescription())).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		Long validityInMiliseconds = null;
		if(getValidityTo() != null) validityInMiliseconds = getValidityTo().getTime();

		return str.append(getClass().getSimpleName()).append(":[id='").append(getId()
			).append("', userId='").append(getUserId()
			).append("', facilityId='").append(getFacilityId()
			).append("', validityTo='").append(validityInMiliseconds
			).append("', description='").append(this.getDescription()).append("']").toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BanOnFacility other = (BanOnFacility) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		if (this.getUserId()!= other.getUserId()) {
			return false;
		}
		if (this.getFacilityId()!= other.getFacilityId()) {
			return false;
		}
		if (this.getValidityTo() == null ? other.getValidityTo()!= null : this.getValidityTo().getTime() != other.getValidityTo().getTime()) {
			return false;
		}
		if (this.getDescription() == null ? other.getDescription() != null : !this.getDescription().equals(other.getDescription())) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof Ban) {
			Ban ban = (Ban) perunBean;
			if (this.getValidityTo() == null && ban.getValidityTo() != null) return -1;
			if (ban.getValidityTo() == null && this.getValidityTo() != null) return 1;
			if (ban.getValidityTo() == null && this.getValidityTo() == null) return 0;
			Long thisValidity = this.getValidityTo().getTime();
			Long otherValidity = ban.getValidityTo().getTime();
			return thisValidity.compareTo(otherValidity);
		} else {
			return (this.getId() - perunBean.getId());
		}
	}
}
