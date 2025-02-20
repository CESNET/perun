package cz.metacentrum.perun.dispatcher.exceptions;

/**
 * Checked version of PerunActiveMQServerException.
 *
 * @author Michal Karm Babacek
 */
public class PerunActiveMQServerException extends DispatcherException {

  private static final long serialVersionUID = 1L;

  public PerunActiveMQServerException(String message) {
    super(message);
  }

  public PerunActiveMQServerException(String message, Throwable cause) {
    super(message, cause);
  }

  public PerunActiveMQServerException(Throwable cause) {
    super(cause);
  }
}
