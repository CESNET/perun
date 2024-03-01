package cz.metacentrum.perun.core.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Exception, which is used for bad range of messages count for audit messages.
 *
 * @author Michal Šťava
 */
public class WrongRangeOfCountException extends IllegalArgumentException {
  static final long serialVersionUID = 0;
  private static final Logger LOG = LoggerFactory.getLogger(IllegalArgumentException.class);

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public WrongRangeOfCountException(String message) {
    super(message);

    LOG.error("Illegal Argument Exception:", this);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public WrongRangeOfCountException(String message, Throwable cause) {
    super(message, cause);

    LOG.error("Illegal Argument Exception:", this);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public WrongRangeOfCountException(Throwable cause) {
    super(cause);

    LOG.error("Illegal Argument Exception:", this);
  }
}
