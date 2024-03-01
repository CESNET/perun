package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;

public class ResourceDeleted extends AuditEvent {

  private Resource resource;
  private Facility facility;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ResourceDeleted() {
  }

  public ResourceDeleted(Resource resource, Facility facility) {
    this.resource = resource;
    this.facility = facility;
    this.message = formatMessage("%s deleted.#%s.", resource, facility);
  }

  public Facility getFacility() {
    return facility;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Resource getResource() {
    return resource;
  }

  @Override
  public String toString() {
    return message;
  }
}
