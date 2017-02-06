package cz.metacentrum.perun.cabinet.model;

import java.util.Date;
import java.util.Objects;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Class represents Authorship = connection between
 * publication and its author.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Authorship extends PerunBean {

	private int publicationId;
	private int userId;
	private String createdBy;
	private Date createdDate;
	private int createdByUid;

	/**
	 * Return ID of Publication this Authorship belongs to.
	 *
	 * @return the value of publicationId
	 */
	public int getPublicationId() {
		return publicationId;
	}

	/**
	 * Set ID of Publication this Authorship belongs to.
	 *
	 * @param publicationId ID of Publication
	 */
	public void setPublicationId(int publicationId) {
		this.publicationId = publicationId;
	}

	/**
	 * Return ID of User (Author) this Authorship belongs to.
	 *
	 * @return the value of userId
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * Set ID of User (Author) this Authorship belongs to.
	 *
	 * @param userId ID of Uublication (Author)
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

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
		if (!(o instanceof Authorship)) return false;
		if (!super.equals(o)) return false;
		Authorship that = (Authorship) o;
		return publicationId == that.publicationId &&
				userId == that.userId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), publicationId, userId);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(getClass().getSimpleName()).append(":[id='").append(getId()).append("', userId='").append(userId).append("', publicationId='").append(publicationId).append("', createdBy='").append(createdBy).append("', createdDate='").append(createdDate).append("', createdByUid='").append(createdByUid).append("']").toString();
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		String dateString;
		if(getCreatedDate() != null) dateString = BeansUtils.getDateFormatter().format(getCreatedDate());
		else dateString = "\\0";

		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", userId=<").append(getUserId()).append(">").append(
			", publicationId=<").append(getPublicationId()).append(">").append(
			", createdBy=<").append(getCreatedBy() == null ? "\\0" : BeansUtils.createEscaping(getCreatedBy())).append(">").append(
			", createdDate=<").append(dateString).append( ">").append(
			", createdByUid=<").append(getCreatedByUid()).append(">").append(
			"]").toString();
	}

}
