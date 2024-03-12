package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Facility;

/**
 * Exception thrown when wrong facility provided.
 */
public class FacilityMismatchException extends PerunException {

  private Facility facility1;
  private Facility facility2;

  /**
   * Constructor with no arguments
   */
  public FacilityMismatchException() {
  }

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public FacilityMismatchException(String message) {
    super(message);
  }

  public FacilityMismatchException(String message, Facility facility1, Facility facility2) {
    super(message);

    this.facility1 = facility1;
    this.facility2 = facility2;
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public FacilityMismatchException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public FacilityMismatchException(Throwable cause) {
    super(cause);
  }

  public Facility getFacility1() {
    return facility1;
  }

  public Facility getFacility2() {
    return facility2;
  }
}
