package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;
import java.util.List;

public class AttributesAddedAsRequiredToService extends AuditEvent {

  private List<? extends AttributeDefinition> attributes;
  private Service service;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributesAddedAsRequiredToService() {
  }

  public AttributesAddedAsRequiredToService(List<? extends AttributeDefinition> attributes, Service service) {
    this.attributes = attributes;
    this.service = service;
    this.message = formatMessage("%s added to %s as required attributes.", attributes, service);
  }

  public List<? extends AttributeDefinition> getAttributes() {
    return attributes;
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
