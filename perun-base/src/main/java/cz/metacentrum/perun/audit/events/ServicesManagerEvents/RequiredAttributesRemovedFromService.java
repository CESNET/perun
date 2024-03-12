package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;
import java.util.List;

public class RequiredAttributesRemovedFromService extends AuditEvent {

  private Service service;
  private List<? extends AttributeDefinition> attributes;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public RequiredAttributesRemovedFromService() {
  }

  public RequiredAttributesRemovedFromService(List<? extends AttributeDefinition> attributes, Service service) {
    this.service = service;
    this.attributes = attributes;
    this.message = formatMessage("%s removed from %s as required attributes.", attributes, service);
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
