package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to unset a role which was not set for given entities.
 */
public class RoleNotSetException extends PerunException {

	String role;

	public RoleNotSetException(String role) {
		super("The role: "+ role +" is not set so it cannot be unset.");
		this.role = role;
	}

	public String getRole() {
		return role;
	}
}