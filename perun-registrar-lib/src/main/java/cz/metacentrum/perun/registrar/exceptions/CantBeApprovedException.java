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

	public CantBeApprovedException(String message) {
		super(message);
	}

	public CantBeApprovedException(String message, Throwable ex) {
		super(message, ex);
	}

}
