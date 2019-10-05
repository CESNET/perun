package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to remove a host from the facility but the host has already been removed
 *
 * @author Michal Stava
 */
public class HostAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public HostAlreadyRemovedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public HostAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public HostAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
