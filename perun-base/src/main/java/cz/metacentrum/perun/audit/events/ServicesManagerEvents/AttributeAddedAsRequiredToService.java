package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

public class AttributeAddedAsRequiredToService extends AuditEvent {

  private AttributeDefinition attribute;
  private Service service;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeAddedAsRequiredToService() {
  }

  public AttributeAddedAsRequiredToService(AttributeDefinition attribute, Service service) {
    this.attribute = attribute;
    this.service = service;
    this.message = formatMessage("%s added to %s as required attribute.", attribute, service);
  }

  public AttributeDefinition getAttribute() {
    return attribute;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Service getService() {
    return service;
  }

  @Override
  public String toString() {
    return message;
  }
}
