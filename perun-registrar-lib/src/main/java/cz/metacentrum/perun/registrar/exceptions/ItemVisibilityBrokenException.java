package cz.metacentrum.perun.registrar.exceptions;


/**
 * Thrown when a submitted item that was supposed to be hidden/disabled was submitted with a modified value
 */
public class ItemVisibilityBrokenException extends RegistrarException {
  public ItemVisibilityBrokenException(String message) {
    super(message);
  }
}
