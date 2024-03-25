package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Service;

/**
 * This exception is thrown when trying to add attributes which are forbidden unless the service is globally disabled.
 * If such attribute would be added, consents for facility would be invalid.
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class ServiceAttributesCannotExtend extends PerunException {

  static final long serialVersionUID = 0;

  private Service service;
  private int facilityId;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ServiceAttributesCannotExtend(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public ServiceAttributesCannotExtend(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public ServiceAttributesCannotExtend(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with the service and the facility
   *
   * @param service    service which cannot be extended by new attribute
   * @param facilityId id of facility on which the service would cause invalidation of consents
   */
  public ServiceAttributesCannotExtend(Service service, int facilityId) {
    this("Adding attribute to " + service.toString() + " would invalidate consents on facility with id " + facilityId);
    this.service = service;
    this.facilityId = facilityId;
  }

  /**
   * Getter for the facilityId
   *
   * @return facility on which the service would cause invalidation of consents
   */
  public int getFacilityId() {
    return facilityId;
  }

  /**
   * Getter for the service
   *
   * @return service which cannot be extended by new attribute
   */
  public Service getService() {
    return service;
  }

}
