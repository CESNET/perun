package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Role;

/**
 * Checked version of RoleNotSupportedException.
 *
 * This exception is thrown when somewhere in code is object Role but
 * this role is not supported there for some reason.
 *
 * @author Michal Stava
 */
public class RoleNotSupportedException extends PerunException {
	static final long serialVersionUID = 0;

	private Role role;

	public RoleNotSupportedException(String message) {
		super(message);
	}

	public RoleNotSupportedException(String message, Role role) {
		super(message);
		this.role = role;
	}

	public RoleNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public RoleNotSupportedException(String message, Role role, Throwable cause) {
		super(message, cause);
		this.role = role;
	}

	public RoleNotSupportedException(Throwable cause) {
		super(cause);
	}

	public RoleNotSupportedException(Throwable cause, Role role) {
		super(cause);
		this.role = role;
	}

	public Role getRole() {
		return this.role;
	}
}
