package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.rt.UserExtSourceExistsRuntimeException;

/**
 * Checked version of UserExtSourceExistsException
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.UserExtSourceExistsRuntimeException
 * @author Slavek Licehammer
 */
public class UserExtSourceExistsException extends EntityExistsException {
	static final long serialVersionUID = 0;

	private UserExtSource userExtSource;

	public UserExtSourceExistsException(UserExtSourceExistsRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

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
