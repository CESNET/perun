package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.UserExtSource;

/**
 * Thrown when userExtSource with the specific login already exists
 *
 * @author Slavek Licehammer
 */
public class UserExtSourceExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private UserExtSource userExtSource;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public UserExtSourceExistsException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserExtSourceExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public UserExtSourceExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the userExtSource
	 * @param userExtSource userExtSource that already exists
	 */
	public UserExtSourceExistsException(UserExtSource userExtSource) {
		super(userExtSource.toString());
		this.userExtSource = userExtSource;
	}

	/**
	 * Getter for the userExtSource
	 * @return userExtSource that already exists
	 */
	public UserExtSource getExtSource() {
		return this.userExtSource;
	}
}
