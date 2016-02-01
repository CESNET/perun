package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException;

/**
 * If expecting user who is service User
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.RelationExistsRuntimeException
 * @author Michal Šťava
 */
public class SpecificUserExpectedException extends PerunException {
	static final long serialVersionUID = 0;

	private User user;

	public SpecificUserExpectedException(String message) {
		super(message);
	}

	public SpecificUserExpectedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SpecificUserExpectedException(Throwable cause) {
		super(cause);
	}

	public SpecificUserExpectedException(User user) {
		super(user.toString());
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}
}
