package cz.metacentrum.perun.cabinet.model;

import cz.metacentrum.perun.core.api.PerunBean;

import java.util.Date;
import java.util.Objects;

/**
 * Class represents Thanks = expression of acknowledgment
 * from authors to facility owners.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Thanks extends PerunBean {

	private int publicationId;
	private int ownerId;
	private String createdBy;
	private int createdByUid;
	private Date createdDate;

	public Thanks() { }

	public Thanks(int id, int publicationId, int ownerId, String createdBy, Date createdDate) {
		super(id);
		this.publicationId = publicationId;
		this.ownerId = ownerId;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
	}

	public Thanks(int id, int publicationId, int ownerId, String createdBy, Date createdDate, int createdByUid) {
		this(id, publicationId, ownerId, createdBy, createdDate);
		this.createdByUid = createdByUid;
	}

	/**
	 * This method returns the value of the database column THANKS.reportId
	 *
	 * @return the value of THANKS.reportId
	 */
	public int getPublicationId() {
		return publicationId;
	}

	/**
	 * This method sets the value of the database column THANKS.publicationId
	 *
	 * @param publicationId the value for THANKS.publicationId
	 */
	public void setPublicationId(int publicationId) {
		this.publicationId = publicationId;
	}

	/**
	 * This method returns the value of the database column THANKS.ownerId
	 *
	 * @return the value of THANKS.ownerId
	 */
	public int getOwnerId() {
		return ownerId;
	}

	/**
	 * This method sets the value of the database column THANKS.ownerId
	 *
	 * @param ownerId the value for THANKS.ownerId
	 */
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * This method returns the value of the database column THANKS.createdBy
	 *
	 * @return the value of THANKS.createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * This method sets the value of the database column THANKS.createdBy
	 *
	 * @param createdBy the value for THANKS.createdBy
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * This method returns the value of the database column THANKS.createdDate
	 *
	 * @return the value of THANKS.createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * This method sets the value of the database column THANKS.createdDate
	 *
	 * @param createdDate the value for THANKS.createdDate
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public int getCreatedByUid() {
		return createdByUid;
	}

	public void setCreatedByUid(int createdByUid) {
		this.createdByUid = createdByUid;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Thanks)) return false;
		if (!super.equals(o)) return false;
		Thanks thanks = (Thanks) o;
		return publicationId == thanks.publicationId &&
				ownerId == thanks.ownerId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), publicationId, ownerId);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		return str.append(getClass().getSimpleName()).append(":[id=").append(getId()).append(", pubId=").append(publicationId).append(", ownerId=").append(ownerId).append(", createdBy=").append(createdBy).append(", createdDate=").append(createdDate).append(", createdByUid=").append(createdByUid).append("]").toString();
	}

}
