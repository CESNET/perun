package cz.metacentrum.perun.core.api.exceptions;

/**
 * Security team already exists in perun system
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamExistsException extends EntityExistsException {

	public SecurityTeamExistsException(String message) {
		super(message);
	}

	public SecurityTeamExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecurityTeamExistsException(Throwable cause) {
		super(cause);
	}
}
