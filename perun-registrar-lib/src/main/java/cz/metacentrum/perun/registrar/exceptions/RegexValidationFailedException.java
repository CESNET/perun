package cz.metacentrum.perun.registrar.exceptions;


/**
 * Thrown when submitted item data does not pass the regex validation of the item
 */
public class RegexValidationFailedException extends RegistrarException {
  public RegexValidationFailedException(String message) {
    super(message);
  }
}
