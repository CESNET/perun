package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when name of entity is not long enough.
 *
 * @author Michal Šťava
 */
public class MinSizeExceededException extends InternalErrorException {
	static final long serialVersionUID = 0;


	public MinSizeExceededException(String message) {
		super(message);
	}

	public MinSizeExceededException(String message, Throwable cause) {
		super(message, cause);
	}

	public MinSizeExceededException(Throwable cause) {
		super(cause);
	}
}
