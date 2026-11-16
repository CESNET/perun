package cz.metacentrum.perun.registrar.exceptions;


/**
 * Thrown when the submitted embedded groups item contains groups that are not defined as auto-registration
 * for that VO or group
 */
public class IllegalEmbeddedGroupException extends RegistrarException {
  public IllegalEmbeddedGroupException(String message) {
    super(message);
  }
}
