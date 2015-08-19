package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;

/**
 * Checked version of UserNotAdminException.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.AlreadyAdminRuntimeException
 * @author Michal Stava
 */
public class UserNotAdminException extends PerunException {
	static final long serialVersionUID = 0;

	private User user;

	public UserNotAdminException(String message) {
		super(message);
	}

	public UserNotAdminException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserNotAdminException(Throwable cause) {
		super(cause);
	}

	public UserNotAdminException(User user) {
		super(user.toString());
		this.user = user;
	}

	public User getUser() {
		return user;
	}
}
