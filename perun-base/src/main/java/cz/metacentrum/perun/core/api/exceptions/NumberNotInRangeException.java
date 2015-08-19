package cz.metacentrum.perun.core.api.exceptions;


/**
 * This exception raises when a number is not in the range of numbers.
 *
 * @author Michal Šťava
 */
public class NumberNotInRangeException extends InternalErrorException {
	static final long serialVersionUID = 0;


	public NumberNotInRangeException(String message) {
		super(message);
	}

	public NumberNotInRangeException(String message, Throwable cause) {
		super(message, cause);
	}

	public NumberNotInRangeException(Throwable cause) {
		super(cause);
	}
}
