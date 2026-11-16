package cz.metacentrum.perun.registrar.exceptions;


/**
 * Thrown when submitted data is missing data for form items that are marked as required
 */
public class RequiredItemMissingException extends RegistrarException {
  public RequiredItemMissingException(String message) {
    super(message);
  }
}
