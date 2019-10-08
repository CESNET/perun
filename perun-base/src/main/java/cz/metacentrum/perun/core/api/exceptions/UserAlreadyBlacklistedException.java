package cz.metacentrum.perun.core.api.exceptions;

/**
 * User is already blacklisted by security team
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class UserAlreadyBlacklistedException extends PerunException {

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public UserAlreadyBlacklistedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserAlreadyBlacklistedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserAlreadyBlacklistedException(Throwable cause) {
		super(cause);
	}

}
