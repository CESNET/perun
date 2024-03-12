package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when group is not embedded in VO
 *
 * @author Johana Supíková
 */
public class GroupNotEmbeddedException extends PerunException {
  static final long serialVersionUID = 0;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public GroupNotEmbeddedException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public GroupNotEmbeddedException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public GroupNotEmbeddedException(Throwable cause) {
    super(cause);
  }
}
