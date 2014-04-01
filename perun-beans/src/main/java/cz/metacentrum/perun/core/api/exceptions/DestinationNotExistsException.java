package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.DestinationNotExistsRuntimeException;

/**
 * Checked version of DestinationNotExistsException
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.DestinationNotExistsRuntimeException
 * @author Michal Prochazka
 */
public class DestinationNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	public DestinationNotExistsException(DestinationNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public DestinationNotExistsException(String message) {
		super(message);
	}

	public DestinationNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public DestinationNotExistsException(Throwable cause) {
		super(cause);
	}
}
