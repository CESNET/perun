package cz.metacentrum.perun.core.api.exceptions;

/**
 * User is already blacklisted by security team
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class UserAlreadyBlacklistedException extends PerunException {

	public UserAlreadyBlacklistedException(String message) {
		super(message);
	}

	public UserAlreadyBlacklistedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserAlreadyBlacklistedException(Throwable cause) {
		super(cause);
	}

}
