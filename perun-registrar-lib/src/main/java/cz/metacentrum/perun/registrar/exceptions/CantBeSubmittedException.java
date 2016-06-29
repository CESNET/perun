package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception throw when application can't be submitted by custom VO rules.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CantBeSubmittedException extends PerunException {

	private static final long serialVersionUID = 1L;

	private String reason = null;
	private String category = null;
	private String affiliation = null;

	public CantBeSubmittedException(String message) {
		super(message);
	}

	public CantBeSubmittedException(String message, String reason, String category, String affiliation) {
		super(message);
		this.reason = reason;
		this.category = category;
		this.affiliation = affiliation;
	}

	public CantBeSubmittedException(String message, Throwable ex) {
		super(message, ex);
	}

	public CantBeSubmittedException(String message, String reason, Throwable ex) {
		super(message, ex);
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

}
