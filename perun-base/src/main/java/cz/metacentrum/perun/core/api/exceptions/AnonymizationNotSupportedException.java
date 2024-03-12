package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to get anonymized value of the attribute, whose module doesn't specify the
 * anonymization process in method getAnonymizedValue(). This exception is also thrown when the anonymization is not
 * supported on any instance - the config property userDeletionForced is set to true
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class AnonymizationNotSupportedException extends PerunException {

  public AnonymizationNotSupportedException(String message) {
    super(message);
  }

  public AnonymizationNotSupportedException(String message, Throwable cause) {
    super(message, cause);
  }

  public AnonymizationNotSupportedException(Throwable cause) {
    super(cause);
  }
}
