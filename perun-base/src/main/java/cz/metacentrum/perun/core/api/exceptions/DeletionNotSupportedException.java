package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when the deletion is not supported on any instance - the config
 * property userDeletionForced is set to false
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class DeletionNotSupportedException extends PerunException {

  public DeletionNotSupportedException(String message) {
    super(message);
  }

  public DeletionNotSupportedException(String message, Throwable cause) {
    super(message, cause);
  }

  public DeletionNotSupportedException(Throwable cause) {
    super(cause);
  }
}
