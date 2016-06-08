package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException;

/**
 * If expecting user who is not service User
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException
 * @author Michal Šťava
 */
public class NotSpecificUserExpectedException extends PerunException {
	static final long serialVersionUID = 0;

	private User user;

	public NotSpecificUserExpectedException(String message) {
		super(message);
	}

	public NotSpecificUserExpectedException(String message, Throwable cause) {
		super(message, cause);
	}

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
