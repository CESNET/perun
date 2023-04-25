package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to unblock service which is not blocked.
 *
 * @author Sarka Palkovicova
 */
public class ServiceIsNotBannedException extends PerunException  {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public ServiceIsNotBannedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceIsNotBannedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public ServiceIsNotBannedException(Throwable cause) {
		super(cause);
	}

}
