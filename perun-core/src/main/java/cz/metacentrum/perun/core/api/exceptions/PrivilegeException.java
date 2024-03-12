package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.PerunSession;

/**
 * Checked version of PrivilegeException.
 *
 * @author Martin Kuba
 */
public class PrivilegeException extends PerunException {
  static final long serialVersionUID = 0;

  public PrivilegeException(String message) {
    super(message);
  }

  public PrivilegeException(String message, Throwable cause) {
    super(message, cause);
  }

  public PrivilegeException(Throwable cause) {
    super(cause);
  }

  public PrivilegeException(PerunSession sess) {
    super("Principal " + sess.getPerunPrincipal().getActor() + " is not authorized");
  }

  public PrivilegeException(PerunSession sess, String action) {
    super("Principal " + sess.getPerunPrincipal().getActor() + " is not authorized to perform action '" + action + "'");
  }
}
