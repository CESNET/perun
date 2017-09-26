package cz.metacentrum.perun.core.api.exceptions;

/**
 * User is not in required role.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class UserNotInRoleException extends PerunException {

	public UserNotInRoleException(String message) {
		super(message);
	}

}
