package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception thrown when user tries to join two identities, but both are assigned to different users.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class IdentityAlreadyInUseException extends PerunException {

	private static final long serialVersionUID = 1L;

	private User user;
	private User secondUser;

	public IdentityAlreadyInUseException(String message) {
		super(message);
	}

	public IdentityAlreadyInUseException(String message, User user, User secondUser) {
		super(message);
		this.user = user;
		this.secondUser = secondUser;
	}

	public IdentityAlreadyInUseException(String message, Throwable ex) {
		super(message, ex);
	}

	public IdentityAlreadyInUseException(String message, Throwable ex, User user, User secondUser) {
		super(message, ex);
		this.user = user;
		this.secondUser = secondUser;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getSecondUser() {
		return secondUser;
	}

	public void setSecondUser(User secondUser) {
		this.secondUser = secondUser;
	}
}
