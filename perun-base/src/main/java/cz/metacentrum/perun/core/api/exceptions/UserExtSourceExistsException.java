package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.UserExtSource;

/**
 * Checked version of UserExtSourceExistsException
 *
 * @author Slavek Licehammer
 */
public class UserExtSourceExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private UserExtSource userExtSource;

	public UserExtSourceExistsException(String message) {
		super(message);
	}

	public UserExtSourceExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserExtSourceExistsException(Throwable cause) {
		super(cause);
	}

	public UserExtSourceExistsException(UserExtSource userExtSource) {
		super(userExtSource.toString());
		this.userExtSource = userExtSource;
	}

	public UserExtSource getExtSource() {
		return this.userExtSource;
	}
}
