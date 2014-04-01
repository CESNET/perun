package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException;

/**
 * If expecting user who is not service User
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException
 * @author Michal Šťava
 */
public class NotServiceUserExpectedException extends PerunException {
	static final long serialVersionUID = 0;

	private User user;

	public NotServiceUserExpectedException(String message) {
		super(message);
	}

	public NotServiceUserExpectedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotServiceUserExpectedException(Throwable cause) {
		super(cause);
	}

	public NotServiceUserExpectedException(User user) {
		super(user.toString());
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}
}
