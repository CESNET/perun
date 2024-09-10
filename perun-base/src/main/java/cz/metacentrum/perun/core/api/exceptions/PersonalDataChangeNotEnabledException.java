package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when a change of the user's data is not enabled.
 *
 * @author Sarka Palkovicova
 */
public class PersonalDataChangeNotEnabledException extends PerunException {
  static final long serialVersionUID = 0;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public PersonalDataChangeNotEnabledException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public PersonalDataChangeNotEnabledException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public PersonalDataChangeNotEnabledException(Throwable cause) {
    super(cause);
  }

}
