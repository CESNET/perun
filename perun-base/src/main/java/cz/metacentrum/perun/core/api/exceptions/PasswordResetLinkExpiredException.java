package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when password reset link expired (after couple of hours).
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class PasswordResetLinkExpiredException extends PerunException {

  public PasswordResetLinkExpiredException(String message) {
    super(message);
  }

  public PasswordResetLinkExpiredException(String message, Throwable cause) {
    super(message, cause);
  }

  public PasswordResetLinkExpiredException(Throwable cause) {
    super(cause);
  }
}
