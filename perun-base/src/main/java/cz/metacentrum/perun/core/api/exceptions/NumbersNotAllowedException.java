package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when in the name of entity is any number.
 *
 * @author Michal Šťava
 */
public class NumbersNotAllowedException extends InternalErrorException {
	static final long serialVersionUID = 0;


	public NumbersNotAllowedException(String message) {
		super(message);
	}

	public NumbersNotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NumbersNotAllowedException(Throwable cause) {
		super(cause);
	}
}
