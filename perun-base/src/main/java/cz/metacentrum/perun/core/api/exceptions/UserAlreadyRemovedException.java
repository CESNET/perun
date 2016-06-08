package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of UserAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class UserAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public UserAlreadyRemovedException(String message) {
		super(message);
	}

	public UserAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
