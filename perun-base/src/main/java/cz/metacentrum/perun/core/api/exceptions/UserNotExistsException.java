package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;

/**
 * Thrown when the user has not been found in the database
 *
 * @author Martin Kuba
 */
public class UserNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private User user;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public UserNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the user
	 * @param user user who does not exist
	 */
	public UserNotExistsException(User user) {
		super(user.toString());
		this.user = user;
	}

	/**
	 * Getter for the user
	 * @return user who does not exist
	 */
	public User getUser() {
		return this.user;
	}
}
