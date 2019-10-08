package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the login is already in use in the specific namespace
 *
 * @author Pavel Zlámal <256627@mail.muni.cz>
 */
public class LoginExistsException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public LoginExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public LoginExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public LoginExistsException(Throwable cause) {
		super(cause);
	}
}
