package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForGroupAndResource extends AuditEvent {

  private AttributeDefinition attribute;
  private Group group;
  private Resource resource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeRemovedForGroupAndResource() {
  }

  public AttributeRemovedForGroupAndResource(AttributeDefinition attribute, Group group, Resource resource) {
    this.attribute = attribute;
    this.group = group;
    this.resource = resource;
    this.message = formatMessage("%s removed for %s and %s.", attribute, group, resource);
  }

  public AttributeDefinition getAttribute() {
    return attribute;
  }

  public Group getGroup() {
    return group;
  }

  public Resource getResource() {
    return resource;
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
