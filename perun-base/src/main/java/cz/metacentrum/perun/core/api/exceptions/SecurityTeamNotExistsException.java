package cz.metacentrum.perun.core.api.exceptions;

/**
 * Security team not exists in perun system yet
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamNotExistsException extends EntityNotExistsException {

	public SecurityTeamNotExistsException(String message) {
		super(message);
	}

	public SecurityTeamNotExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecurityTeamNotExistsException(Throwable cause) {
		super(cause);
	}
}
