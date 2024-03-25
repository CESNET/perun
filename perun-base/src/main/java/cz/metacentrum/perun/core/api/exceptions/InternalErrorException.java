package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class representing an exception caused by an unexpected error.
 *
 * @author Martin Kuba
 */
public class InternalErrorException extends PerunRuntimeException {
  static final long serialVersionUID = 0;
  private static final Logger LOG = LoggerFactory.getLogger(InternalErrorException.class);

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public InternalErrorException(String message) {
    super(message);

    LOG.error("Internal Error Exception:", this);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public InternalErrorException(String message, Throwable cause) {
    super(message, cause);

    LOG.error("Internal Error Exception:", this);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public InternalErrorException(Throwable cause) {
    super(cause);

    LOG.error("Internal Error Exception:", this);
  }
}
