package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when the name of entity contains at least one space.
 *
 * @author Michal Šťava
 */
public class SpaceNotAllowedException extends InternalErrorException {
	static final long serialVersionUID = 0;


	public SpaceNotAllowedException(String message) {
		super(message);
	}

	public SpaceNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpaceNotAllowedException(Throwable cause) {
		super(cause);
	}
}
