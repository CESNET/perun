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

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public RoleNotSupportedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and the role
	 * @param message message with details about the cause
	 * @param role role that is not supported
	 */
	public RoleNotSupportedException(String message, Role role) {
		super(message);
		this.role = role;
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public RoleNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a message, the role and a throwable
	 * @param message message with details about the cause
	 * @param role role that is not supported
	 * @param cause Throwable that caused throwing of this exception
	 */
	public RoleNotSupportedException(String message, Role role, Throwable cause) {
		super(message, cause);
		this.role = role;
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public RoleNotSupportedException(Throwable cause) {
		super(cause);
	}
	/**
	 * Constructor with the role and a throwable
	 * @param role role that is not supported
	 * @param cause Throwable that caused throwing of this exception
	 */
	public RoleNotSupportedException(Throwable cause, Role role) {
		super(cause);
		this.role = role;
	}

	/**
	 * Getter for the role
	 * @return role that is not supported
	 */
	public Role getRole() {
		return this.role;
	}
}
