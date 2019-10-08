package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;

/**
 * This exception is thrown when expecting a user who is not service user or expecting a user who is not sponsored
 *
 * @author Michal Šťava
 */
public class NotSpecificUserExpectedException extends PerunException {
	static final long serialVersionUID = 0;

	private User user;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public NotSpecificUserExpectedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public NotSpecificUserExpectedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public NotSpecificUserExpectedException(Throwable cause) {
		super(cause);
	}

	public NotSpecificUserExpectedException(User user) {
		super(user.toString());
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}
}
