package cz.metacentrum.perun.core.api;

import java.util.Date;

/**
 * Represents ban for someone on something in perun.
 * Description and timestamp.
 *
 * @author  Michal Stava
 */
public abstract class Ban extends Auditable implements Comparable<PerunBean> {
	private Date validityTo;
	private String description;
	
	/**
	 * Constructs a new instance.
	 */
	public Ban() {
		super();
	}

	public Ban(int id, Date validityTo) {
		super(id);
		//set precision for validity on seconds
		if(validityTo == null) this.validityTo = validityTo;
		else validityTo = new Date(validityTo.getTime() / 1000 * 1000);
		this.validityTo = validityTo;
	}

	public Ban(int id, Date validityTo, String description) {
		this(id, validityTo);
		this.description = description;
	}

	public Date getValidityTo() {
		return validityTo;
	}

	public void setValidityTo(Date validityTo) {
		//set precision for validity on seconds
		if(validityTo == null) this.validityTo = validityTo;
		else validityTo = new Date(validityTo.getTime() / 1000 * 1000);
		this.validityTo = validityTo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Class name of specific ban.
	 *
	 * @return class name of specific ban
	 */
	public abstract String getType();
	
	/**
	 * Id of subject who is banned on target.
	 * 
	 * @return id of affected subject
	 */
	public abstract int getSubjectId();

	/**
	 * Id of target where subject is banned on.
	 *
	 * @return id of affected target
	 */
	public abstract int getTargetId();


	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
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
			).append("', validityTo='").append(validityInMiliseconds
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
		final Ban other = (Ban) obj;
		if (this.getId() != other.getId()) {
			return false;
		}
		if (this.validityTo == null ? other.getValidityTo()!= null : this.validityTo.getTime() != other.getValidityTo().getTime()) {
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
		if(perunBean instanceof Ban) {
			Ban ban = (Ban) perunBean;
			if (this.validityTo == null && ban.getValidityTo() != null) return -1;
			if (ban.getValidityTo() == null && this.validityTo != null) return 1;
			if (ban.getValidityTo() == null && this.validityTo == null) return 0;
			Long thisValidity = this.validityTo.getTime();
			Long otherValidity = ban.getValidityTo().getTime();
			return thisValidity.compareTo(otherValidity);
		} else {
			return (this.getId() - perunBean.getId());
		}
	}
}
