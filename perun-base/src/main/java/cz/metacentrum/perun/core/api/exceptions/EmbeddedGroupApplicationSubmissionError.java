package cz.metacentrum.perun.core.api.exceptions;


import java.util.Map;

/**
 * This exception is thrown when submitting embedded groups' applications fail
 *
 * @author Johana Supíková
 */
public class EmbeddedGroupApplicationSubmissionError extends PerunException {
  static final long serialVersionUID = 0;

  private Map<Integer, String> failedGroups;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public EmbeddedGroupApplicationSubmissionError(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public EmbeddedGroupApplicationSubmissionError(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public EmbeddedGroupApplicationSubmissionError(Throwable cause) {
    super(cause);
  }

  public EmbeddedGroupApplicationSubmissionError(Map<Integer, String> failedGroups) {
    super(String.format("Embedded groups submission failed for %d group(s).", failedGroups.size()));
    this.failedGroups = failedGroups;
  }

  public Map<Integer, String> getFailedGroups() {
    return this.failedGroups;
  }
}
