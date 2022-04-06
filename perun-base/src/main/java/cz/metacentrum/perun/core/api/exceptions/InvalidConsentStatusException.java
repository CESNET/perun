package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to set consent status on an invalid value.
 *
 * @author Sarka Palkovicova
 */
public class InvalidConsentStatusException extends PerunException {

	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public InvalidConsentStatusException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public InvalidConsentStatusException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public InvalidConsentStatusException(Throwable cause) {
		super(cause);
	}
}
