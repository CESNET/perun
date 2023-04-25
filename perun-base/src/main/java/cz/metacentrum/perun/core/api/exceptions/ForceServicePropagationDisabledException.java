package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when forcing service propagation is not possible.
 *
 * @author Sarka Palkovicova
 */
public class ForceServicePropagationDisabledException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ForceServicePropagationDisabledException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ForceServicePropagationDisabledException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ForceServicePropagationDisabledException(Throwable cause) {
		super(cause);
	}
}
