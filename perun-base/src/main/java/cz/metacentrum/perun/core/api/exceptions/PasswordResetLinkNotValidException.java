package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when password reset link is not valid because it was either
 * already used or has never existed.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class PasswordResetLinkNotValidException extends PerunException {

  public PasswordResetLinkNotValidException(String message) {
    super(message);
  }

  public PasswordResetLinkNotValidException(String message, Throwable cause) {
    super(message, cause);
  }

  public PasswordResetLinkNotValidException(Throwable cause) {
    super(cause);
  }
}
