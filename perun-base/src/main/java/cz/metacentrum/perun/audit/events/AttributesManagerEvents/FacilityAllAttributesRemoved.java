package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Facility;

public class FacilityAllAttributesRemoved extends AuditEvent implements EngineIgnoreEvent {

  private Facility facility;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public FacilityAllAttributesRemoved() {
  }

  public FacilityAllAttributesRemoved(Facility facility) {
    this.facility = facility;
    this.message = formatMessage("All attributes removed for %s.", facility);
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
