package cz.metacentrum.perun.dispatcher.exceptions;

import org.slf4j.LoggerFactory;

/**
 * The base of Perun-Dispatcher checked exceptions.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public abstract class DispatcherException extends Exception {

  static final long serialVersionUID = 0;
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DispatcherException.class);
  private String errorId = Long.toHexString(System.currentTimeMillis());

  public DispatcherException() {
    super();
    LOGGER.error("Error ID: " + errorId, this);
  }

  public DispatcherException(String message) {
    super(message);
    LOGGER.error("Error ID: " + errorId, this);
  }

  public DispatcherException(String message, Throwable cause) {
    super(message, cause);
    LOGGER.error("Error ID: " + errorId, this);
  }

  public DispatcherException(Throwable cause) {
    super(cause != null ? cause.getMessage() : null, cause);
    LOGGER.error("Error ID: " + errorId, this);
  }

  public String getErrorId() {
    return errorId;
  }

  @Override
  public String getMessage() {
    return "Error " + errorId + ": " + super.getMessage();
  }
}
