package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Resource;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForResource extends AuditEvent {

  private AttributeDefinition attribute;
  private Resource resource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeRemovedForResource() {
  }

  public AttributeRemovedForResource(AttributeDefinition attribute, Resource resource) {
    this.attribute = attribute;
    this.resource = resource;
    this.message = formatMessage("%s removed for %s.", attribute, resource);
  }

  public AttributeDefinition getAttribute() {
    return attribute;
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
