package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.BanOnFacility;

public class BanUpdatedForFacility extends AuditEvent {

  private BanOnFacility banOnFacility;
  private int userId;
  private int facilityId;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public BanUpdatedForFacility() {
  }

  public BanUpdatedForFacility(BanOnFacility banOnFacility, int userId, int facilityId) {
    this.banOnFacility = banOnFacility;
    this.userId = userId;
    this.facilityId = facilityId;
    this.message =
        formatMessage("Ban %s was updated for userId %s on facilityId %s.", banOnFacility, userId, facilityId);
  }

  public BanOnFacility getBanOnFacility() {
    return banOnFacility;
  }

  public int getUserId() {
    return userId;
  }

  public int getFacilityId() {
    return facilityId;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return message;
  }
}
