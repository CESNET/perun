package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.BanOnFacility;

public class BanRemovedForFacility extends AuditEvent {

  private BanOnFacility ban;
  private int userId;
  private int facilityId;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public BanRemovedForFacility() {
  }

  public BanRemovedForFacility(BanOnFacility ban, int userId, int facilityId) {
    this.ban = ban;
    this.userId = userId;
    this.facilityId = facilityId;
    this.message = formatMessage("Ban %s was removed for userId %s on facilityId %s.", ban, userId, facilityId);
  }

  public BanOnFacility getBan() {
    return ban;
  }

  public int getFacilityId() {
    return facilityId;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public int getUserId() {
    return userId;
  }

  @Override
  public String toString() {
    return message;
  }
}
