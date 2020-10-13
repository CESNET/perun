package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to set a role which is already set for given entities.
 */
public class RoleAlreadySetException  extends PerunException {

	String role;

	public RoleAlreadySetException(String role) {
		super("The role: "+ role +" is already set. It cannot be set again");
		this.role = role;
	}

	public String getRole() {
		return role;
	}
}