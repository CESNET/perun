package cz.metacentrum.perun.engine.exceptions;

/**
 * Checked version of InvalidEventMessageException.
 *
 * @author Michal Karm Babacek
 */
public class InvalidEventMessageException extends EngineException {

  private static final long serialVersionUID = 1L;

  public InvalidEventMessageException(String message) {
    super(message);
  }

  public InvalidEventMessageException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidEventMessageException(Throwable cause) {
    super(cause);
  }
}
