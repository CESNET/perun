package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when name of entity is too long.
 *
 * @author Michal Šťava
 */
public class MaxSizeExceededException extends InternalErrorException {
	static final long serialVersionUID = 0;


	public MaxSizeExceededException(String message) {
		super(message);
	}

	public MaxSizeExceededException(String message, Throwable cause) {
		super(message, cause);
	}

	public MaxSizeExceededException(Throwable cause) {
		super(cause);
	}
}
