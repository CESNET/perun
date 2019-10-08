package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.UserExtSource;

/**
 * Thrown when the userExtSource has not been found either because it is not in the database or
 * additional identifiers were not provided
 *
 * @author Slavek Licehammer
 */
public class UserExtSourceNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private UserExtSource userExtSource;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public UserExtSourceNotExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserExtSourceNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserExtSourceNotExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the userExtSource
	 * @param userExtSource userExtSource that doesn't exist
	 */
	public UserExtSourceNotExistsException(UserExtSource userExtSource) {
		super(userExtSource.toString());
		this.userExtSource = userExtSource;
	}

	/**
	 * Getter for the userExtSource
	 * @return userExtSource that doesn't exist
	 */
	public UserExtSource getExtSource() {
		return this.userExtSource;
	}
}
