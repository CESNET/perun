package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when the user is not in required role.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class UserNotInRoleException extends PerunException {

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public UserNotInRoleException(String message) {
		super(message);
	}

}
