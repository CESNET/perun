package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to get an OidcConfig, but OIDC config file does not exist.
 *
 * @author David Flor <493294@muni.cz>
 */

public class OidcConfigFileNotExistsException extends PerunException {

  static final long serialVersionUID = 0;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public OidcConfigFileNotExistsException(String message) {
    super(message);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public OidcConfigFileNotExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public OidcConfigFileNotExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
