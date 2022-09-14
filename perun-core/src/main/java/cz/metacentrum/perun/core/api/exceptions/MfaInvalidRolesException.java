package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.PerunSession;

import java.util.List;

/**
 * This exception is thrown when principal has roles always requiring MFA and roles always skipping MFA at the same time
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class MfaInvalidRolesException extends MfaPrivilegeException {
	static final long serialVersionUID = 0;

	public MfaInvalidRolesException(String message) {
		super(message);
	}

	public MfaInvalidRolesException(String message, Throwable cause) {
		super(message, cause);
	}

	public MfaInvalidRolesException(Throwable cause) {
		super(cause);
	}

	public MfaInvalidRolesException(PerunSession sess, List<String> requiringMfa, List<String> skippingMfa) {
		super("Principal " + sess.getPerunPrincipal().getActor() + " cannot have roles skipping MFA " + skippingMfa.toString() + " and roles requiring MFA " +
			requiringMfa + " at the same time");
	}
}