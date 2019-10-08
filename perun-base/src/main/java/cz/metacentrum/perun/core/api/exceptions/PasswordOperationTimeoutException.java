package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when operation with password in backend exceeds expected limit.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PasswordOperationTimeoutException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public PasswordOperationTimeoutException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordOperationTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public PasswordOperationTimeoutException(Throwable cause) {
		super(cause);
	}
}
