package cz.metacentrum.perun.core.api.exceptions;

/**
 * Validation of SSH key failed for some reason
 *
 * @author David Flor <davidflor@seznam.cz>
 */
public class SshKeyNotValidException extends PerunException {

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public SshKeyNotValidException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public SshKeyNotValidException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public SshKeyNotValidException(Throwable cause) {
    super(cause);
  }
}
