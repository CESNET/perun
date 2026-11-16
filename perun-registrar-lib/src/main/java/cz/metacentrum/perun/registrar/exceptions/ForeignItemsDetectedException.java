package cz.metacentrum.perun.registrar.exceptions;


/**
 * Thrown when submitted data contains item data for items not defined in the VO's/Group's form.
 */
public class ForeignItemsDetectedException extends RegistrarException {

  public ForeignItemsDetectedException(String message) {
    super(message);
  }
}
