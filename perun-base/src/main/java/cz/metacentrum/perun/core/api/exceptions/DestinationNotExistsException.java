package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of DestinationNotExistsException
 *
 * @author Michal Prochazka
 */
public class DestinationNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

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
