package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;

/**
 * This exception is thrown when principal is performing MFA-requiring action but is not authenticated with
 * Multi-Factor
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class MfaPrivilegeException extends PerunRuntimeException {
  static final long serialVersionUID = 0;

  public MfaPrivilegeException(String message) {
    super(message);
  }

  public MfaPrivilegeException(String message, Throwable cause) {
    super(message, cause);
  }

  public MfaPrivilegeException(Throwable cause) {
    super(cause);
  }

  public MfaPrivilegeException(PerunSession sess) {
    super("Principal " + sess.getPerunPrincipal().getActor() + " is not authorized by MFA");
  }

  public MfaPrivilegeException(PerunSession sess, String action) {
    super("Principal " + sess.getPerunPrincipal().getActor() + " is not authorized to perform action '" + action +
          "' without MFA");
  }
}
