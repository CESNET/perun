package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;

/**
 * This exception is thrown when principal has role always requiring MFA but is not authenticated with Multi-Factor
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class MfaRolePrivilegeException extends MfaPrivilegeException {
	static final long serialVersionUID = 0;

	public MfaRolePrivilegeException(String message) {
		super(message);
	}

	public MfaRolePrivilegeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MfaRolePrivilegeException(Throwable cause) {
		super(cause);
	}

	public MfaRolePrivilegeException(PerunSession sess) {
		super("Principal " + sess.getPerunPrincipal().getActor() + " is not authorized by MFA");
	}

	public MfaRolePrivilegeException(PerunSession sess, String role) {
		super("Principal " + sess.getPerunPrincipal().getActor() + " has role " + role + " requiring MFA, but is not authorized by MFA");
	}
}