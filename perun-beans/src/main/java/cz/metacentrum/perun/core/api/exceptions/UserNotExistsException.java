package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.rt.UserNotExistsRuntimeException;

/**
 * Checked version of UserNotExistsException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.UserNotExistsRuntimeException
 * @author Martin Kuba
 */
public class UserNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private User user;

	public UserNotExistsException(UserNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public UserNotExistsException(String message) {
		super(message);
	}

	public UserNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserNotExistsException(Throwable cause) {
		super(cause);
	}

	public UserNotExistsException(User user) {
		super(user.toString());
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}
}
