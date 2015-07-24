package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException;

/**
 * If expecting user who is service User
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException
 * @author Michal Šťava
 */
public class ServiceUserExpectedException extends PerunException {
	static final long serialVersionUID = 0;

	private User user;

	public ServiceUserExpectedException(String message) {
		super(message);
	}

	public ServiceUserExpectedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceUserExpectedException(Throwable cause) {
		super(cause);
	}

	public ServiceUserExpectedException(User user) {
		super(user.toString());
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}
}
