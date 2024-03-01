package cz.metacentrum.perun.core.api;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ServiceDenial extends Auditable {
  private int serviceId;
  private int facilityId;
  private int destinationId;

  public int getServiceId() {
    return serviceId;
  }

  public void setServiceId(int serviceId) {
    this.serviceId = serviceId;
  }

  public int getFacilityId() {
    return facilityId;
  }

  public void setFacilityId(int facilityId) {
    this.facilityId = facilityId;
  }

  public int getDestinationId() {
    return destinationId;
  }

  public void setDestinationId(int destinationId) {
    this.destinationId = destinationId;
  }

  @Override
  public String toString() {
    return "ServiceDenial: [" +
        "id='" + getId() +
        "', serviceId='" + serviceId +
        "', facilityId='" + facilityId +
        "', destinationId='" + destinationId +
        "']";
  }

  @Override
  public String serializeToString() {
    return this.getClass().getSimpleName() + ":[" +
        "id=<" + getId() + ">" +
        ", facilityId=<" + getFacilityId() + ">" +
        ", serviceId=<" + getServiceId() + ">" +
        ", destinationId=<" + getDestinationId() + ">" +
        "]";
  }
}
