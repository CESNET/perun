package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown when wrong facility provided.
 *
 */
public class FacilityMismatchException extends PerunException {

	/**
	 * Constructor with no arguments
	 */
	public FacilityMismatchException() {}

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public FacilityMismatchException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FacilityMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public FacilityMismatchException(Throwable cause) {
		super(cause);
	}
}
