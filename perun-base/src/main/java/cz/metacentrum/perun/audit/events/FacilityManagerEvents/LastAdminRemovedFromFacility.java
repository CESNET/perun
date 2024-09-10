package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;

public class LastAdminRemovedFromFacility extends AuditEvent {
  private Facility facility;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public LastAdminRemovedFromFacility() {
  }

  public LastAdminRemovedFromFacility(Facility facility) {
    this.facility = facility;
    this.message = formatMessage("Last admin removed from %s", facility);
  }

  public Facility getFacility() {
    return facility;
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
