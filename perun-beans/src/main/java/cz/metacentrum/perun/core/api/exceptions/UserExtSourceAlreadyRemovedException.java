package cz.metacentrum.perun.core.api.exceptions;

/**
 * Checked version of UserExtSourceAlreadyRemovedException.
 *
 * @author Michal Stava
 */
public class UserExtSourceAlreadyRemovedException extends PerunException {
	static final long serialVersionUID = 0;

	public UserExtSourceAlreadyRemovedException(String message) {
		super(message);
	}

	public UserExtSourceAlreadyRemovedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserExtSourceAlreadyRemovedException(Throwable cause) {
		super(cause);
	}

}
