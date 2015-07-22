package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception thrown when user uses invalid token (expired or totally wrong)
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class InvalidTokenException extends PerunException {

	private static final long serialVersionUID = 1L;

	public InvalidTokenException(String message) {
		super(message);
	}

	public InvalidTokenException(String message, Throwable ex) {
		super(message, ex);
	}

}
