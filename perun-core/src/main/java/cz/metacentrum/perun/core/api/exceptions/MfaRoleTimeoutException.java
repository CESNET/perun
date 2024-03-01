package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;

/**
 * This exception is thrown when principal has roles always requiring MFA and the auth time is older than the limit
 * defined in the config
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class MfaRoleTimeoutException extends PerunRuntimeException {
  static final long serialVersionUID = 0;

  public MfaRoleTimeoutException(String message) {
    super(message);
  }

  public MfaRoleTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  public MfaRoleTimeoutException(Throwable cause) {
    super(cause);
  }
}
