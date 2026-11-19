package cz.metacentrum.perun.registrar.exceptions;


/**
 * Thrown when submitted item data contains values that are not allowed in an item with limited options,
 * e.g. checkbox or selection items.
 */
public class SelectionValidationFailedException extends RegistrarException {
  public SelectionValidationFailedException(String message) {
    super(message);
  }
}
