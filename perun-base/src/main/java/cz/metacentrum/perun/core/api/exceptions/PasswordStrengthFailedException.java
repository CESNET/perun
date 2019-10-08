package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when password doesn't match expected strength requirements.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PasswordStrengthFailedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public PasswordStrengthFailedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordStrengthFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordStrengthFailedException(Throwable cause) {
		super(cause);
	}
}
