package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to set or unset a role which cannot be managed by anyone.
 */
public class RoleCannotBeManagedException extends PerunException {

	String role;
	Object complementaryObject;
	Object entity;

	public RoleCannotBeManagedException(String role, Object complementaryObject, Object entity) {
		super("Combination of Role: "+ role +", Object: "+ complementaryObject +" and Entity: "+ entity +" cannot be managed.");
		this.role = role;
		this.complementaryObject = complementaryObject;
		this.entity = entity;
	}

	public RoleCannotBeManagedException(String role, Object complementaryObject) {
		super("Combination of Role: "+ role +" and Object: "+ complementaryObject +" cannot be managed.");
		this.role = role;
		this.complementaryObject = complementaryObject;
	}

	public String getRole() {
		return role;
	}

	public Object getComplementaryObject() {
		return complementaryObject;
	}

	public Object getEntity() {
		return entity;
	}
}