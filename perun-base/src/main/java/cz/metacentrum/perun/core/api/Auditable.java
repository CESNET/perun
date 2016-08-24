package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.api.BeansUtils;
import java.text.ParseException;
import java.util.Date;

/**
 * This class represent audit information.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public abstract class Auditable extends PerunBean {

	/**
	 * Attribute with information about time when it was created.
	 */
	private String createdAt;

	/**
	 * Attribute with information who created it.
	 */
	private String createdBy;

	/**
	 * Attribute with information when modified it.
	 */
	private String modifiedAt;

	/**
	 * Attribute with information who modified it.
	 */
	private String modifiedBy;

	/**
	 * Attribute with ID information who user created it.
	 */
	private Integer createdByUid;

	/**
	 * Attribute with ID information who user modified it.
	 */
	private Integer modifiedByUid;

	public Auditable() {
	}

	public Auditable(int id) {
		super(id);
	}

	public Auditable(int id, String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		this(id);
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.modifiedAt = modifiedAt;
		this.modifiedBy = modifiedBy;
		this.modifiedByUid = modifiedByUid;
		this.createdByUid = createdByUid;
	}

	public Auditable(String createdAt, String createdBy, String modifiedAt, String modifiedBy, Integer createdByUid, Integer modifiedByUid) {
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		this.modifiedAt = modifiedAt;
		this.modifiedBy = modifiedBy;
		this.modifiedByUid = modifiedByUid;
		this.createdByUid = createdByUid;
	}

	/**
	 * Gets the information when it was created.
	 *
	 * @return createdAt, the date
	 */
	public String getCreatedAt() {
		return createdAt;
	}

	/**
	 * Sets created time for this instance.
	 *
	 * @param createdAt, the date.
	 */
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Gets the information who created it.
	 *
	 * @return createdBy, the user
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * Sets creator for this instance.
	 *
	 * @param createdBy, the user.
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * Gets the information when it was modofied.
	 *
	 * @return modifiedAt, the time
	 */
	public String getModifiedAt() {
		return modifiedAt;
	}

	/**
	 * Sets modified time for this instance.
	 *
	 * @param modifiedAt, the time.
	 */
	public void setModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	/**
	 * Gets the information who modified it.
	 *
	 * @return modifiedBy, the user
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * Sets information about who modified it.
	 *
	 * @param modifiedBy, the user.
	 */
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	/**
	 * Gets the information who user modified it.
	 *
	 * @return modifiedByUid, the user ID
	 */
	public Integer getModifiedByUid() {
		return modifiedByUid;
	}

	/**
	 * Sets information about who user modified it.
	 *
	 * @param modifiedByUid, the user.
	 */
	public void setModifiedByUid(Integer modifiedByUid) {
		this.modifiedByUid = modifiedByUid;
	}

	/**
	 * Gets the information who user created it.
	 *
	 * @return createdByUid, the user
	 */
	public Integer getCreatedByUid() {
		return createdByUid;
	}

	/**
	 * Sets information about who user created it.
	 *
	 * @param createdByUid, the user.
	 */
	public void setCreatedByUid(Integer createdByUid) {
		this.createdByUid = createdByUid;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;

		if (getClass() != obj.getClass()) return false;

		final Auditable other = (Auditable) obj;

		if (this.getId() != other.getId()) return false;

		return true;
	}

	public int compareByCreatedAt(Auditable auditable) {
		if (auditable == null || auditable.createdAt == null || this.createdAt == null) {
			throw new InternalErrorRuntimeException(new NullPointerException("There is null pointer in auditable object or in createdAt"));
		}
		Date date1;
		try {
			date1 = BeansUtils.getDateFormatter().parse(createdAt);
		} catch (Exception ex) {
			throw new InternalErrorRuntimeException("There is problem with parsing createdAt in object " + this,ex);
		}

		Date date2;
		try {
			date2 = BeansUtils.getDateFormatter().parse(auditable.getCreatedAt());
		} catch (Exception ex) {
			throw new InternalErrorRuntimeException("There is problem with parsing createdAt in object " + auditable,ex);
		}

		return date1.compareTo(date2);
	}

	public int compareByModifiedAt(Auditable auditable) {
		if (auditable == null || auditable.modifiedAt == null || this.modifiedAt == null) {
			throw new InternalErrorRuntimeException(new NullPointerException("There is null pointer in auditable object or in createdAt"));
		}
		Date date1;
		try {
			date1 = BeansUtils.getDateFormatter().parse(modifiedAt);
		} catch (Exception ex) {
			throw new InternalErrorRuntimeException("There is problem with parsing createdAt in object " + this,ex);
		}

		Date date2;
		try {
			date2 = BeansUtils.getDateFormatter().parse(auditable.getModifiedAt());
		} catch (Exception ex) {
			throw new InternalErrorRuntimeException("There is problem with parsing createdAt in object " + auditable,ex);
		}

		return date1.compareTo(date2);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
				"id='").append(this.getId()).append('\'').append(
				']').toString();
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		return str.append(this.getClass().getSimpleName()).append(":[").append(
				"id=<").append(this.getId()).append(">").append(
				']').toString();
	}
}
