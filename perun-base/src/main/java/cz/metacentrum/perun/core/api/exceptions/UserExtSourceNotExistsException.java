package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.UserExtSource;

/**
 * Checked version of UserExtSourceNotExistsException
 *
 * @author Slavek Licehammer
 */
public class UserExtSourceNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private UserExtSource userExtSource;

	public UserExtSourceNotExistsException(String message) {
		super(message);
	}

	public UserExtSourceNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserExtSourceNotExistsException(Throwable cause) {
		super(cause);
	}

	public UserExtSourceNotExistsException(UserExtSource userExtSource) {
		super(userExtSource.toString());
		this.userExtSource = userExtSource;
	}

	public UserExtSource getExtSource() {
		return this.userExtSource;
	}
}
