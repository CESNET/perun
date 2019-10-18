package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;

/**
 * Thrown when the user is not the admin or is not in a role allowing him to do a specific action
 *
 * @author Michal Stava
 */
public class UserNotAdminException extends PerunException {
	static final long serialVersionUID = 0;

	private User user;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public UserNotAdminException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserNotAdminException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserNotAdminException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the user
	 * @param user user who is not an admin
	 */
	public UserNotAdminException(User user) {
		super(user.toString());
		this.user = user;
	}

	/**
	 * Getter for the user
	 * @return the user who is not the admin
	 */
	public User getUser() {
		return user;
	}
}
