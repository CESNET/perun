package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception throw when application can't be approved by custom VO rules.
 * It's not meant as a "hard" error but only as a notice to GUI.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CantBeApprovedException extends PerunException {

	private static final long serialVersionUID = 1L;

	private String reason = null;
	private String category = null;
	private String affiliation = null;
	private boolean isSoft = false;

	public CantBeApprovedException(String message) {
		super(message);
	}

	public CantBeApprovedException(String message, String reason, String category, String affiliation) {
		super(message);
		this.reason = reason;
		this.category = category;
		this.affiliation = affiliation;
	}

	public CantBeApprovedException(String message, String reason, String category, String affiliation, boolean isSoft) {
		super(message);
		this.reason = reason;
		this.category = category;
		this.affiliation = affiliation;
		this.isSoft = isSoft;
	}

	public CantBeApprovedException(String message, Throwable ex) {
		super(message, ex);
	}

	public CantBeApprovedException(String message, String reason, Throwable ex) {
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

	public boolean isSoft() {
		return isSoft;
	}

	public void setSoft(boolean soft) {
		isSoft = soft;
	}

}
