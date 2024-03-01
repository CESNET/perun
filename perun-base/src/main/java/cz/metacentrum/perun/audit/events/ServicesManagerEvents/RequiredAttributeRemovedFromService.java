package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Service;

public class RequiredAttributeRemovedFromService extends AuditEvent {

  private AttributeDefinition attributeDefinition;
  private Service service;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public RequiredAttributeRemovedFromService() {
  }

  public RequiredAttributeRemovedFromService(AttributeDefinition attribute, Service service) {
    this.attributeDefinition = attribute;
    this.service = service;
    this.message = formatMessage("%s removed from %s as required attribute.", attribute, service);
  }

  public AttributeDefinition getAttributeDefinition() {
    return attributeDefinition;
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
