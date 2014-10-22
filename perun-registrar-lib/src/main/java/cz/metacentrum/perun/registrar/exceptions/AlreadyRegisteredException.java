package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Custom exception thrown by Registrar when user is already registered in VO or Group (is member).
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AlreadyRegisteredException extends PerunException {

	private static final long serialVersionUID = 1L;

	public AlreadyRegisteredException(String message) {
		super(message);
	}

	public AlreadyRegisteredException(String message, Throwable ex) {
		super(message, ex);
	}

}
