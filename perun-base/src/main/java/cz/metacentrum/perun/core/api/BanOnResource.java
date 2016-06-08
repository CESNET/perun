package cz.metacentrum.perun.core.api;

import java.util.Date;

/**
 * Represents specific ban of member on resource.
 *
 * @author  Michal Stava
 */
public class BanOnResource extends Ban implements Comparable<PerunBean> {
	private int memberId;
	private int resourceId;

	/**
	 * Constructs a new instance.
	 */
	public BanOnResource() {
		super();
	}

	public BanOnResource(int id, Date validityTo, String description, int memberId, int resourceId) {
		super(id, validityTo, description);
		this.memberId = memberId;
		this.resourceId = resourceId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public String getType() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getSubjectId() {
		return this.getMemberId();
	}

	@Override
	public int getTargetId() {
		return this.getResourceId();
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", memberId=<").append(getMemberId()).append(">").append(
			", resourceId=<").append(getResourceId()).append(">").append(
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
			).append("', memberId='").append(getMemberId()
			).append("', resourceId='").append(getResourceId()
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
		final BanOnResource other = (BanOnResource) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		if (this.getMemberId() != other.getMemberId()) {
			return false;
		}
		if (this.getResourceId() != other.getResourceId()) {
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
