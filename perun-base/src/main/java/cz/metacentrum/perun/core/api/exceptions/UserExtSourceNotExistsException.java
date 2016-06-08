package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.rt.UserExtSourceNotExistsRuntimeException;

/**
 * Checked version of UserExtSourceNotExistsException
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.UserExtSourceNotExistsRuntimeException
 * @author Slavek Licehammer
 */
public class UserExtSourceNotExistsException extends EntityNotExistsException {
	static final long serialVersionUID = 0;

	private UserExtSource userExtSource;

	public UserExtSourceNotExistsException(UserExtSourceNotExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

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
