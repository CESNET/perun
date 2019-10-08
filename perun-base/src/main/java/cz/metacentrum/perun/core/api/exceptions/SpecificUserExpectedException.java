package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;

/**
 * Thrown when expecting specificUser but the user was not specificUser
 *
 * @author Michal Šťava
 */
public class SpecificUserExpectedException extends PerunException {
	static final long serialVersionUID = 0;

	private User user;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public SpecificUserExpectedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SpecificUserExpectedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public SpecificUserExpectedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the user
	 * @param user user who is not specificUser
	 */
	public SpecificUserExpectedException(User user) {
		super(user.toString());
		this.user = user;
	}

	/**
	 * Getter for the user
	 * @return user who is not specificUser
	 */
	public User getUser() {
		return this.user;
	}
}
