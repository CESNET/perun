package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForGroupAndResource extends AuditEvent {

  private Attribute attribute;
  private Group group;
  private Resource resource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeSetForGroupAndResource() {
  }

  public AttributeSetForGroupAndResource(Attribute attribute, Group group, Resource resource) {
    this.attribute = attribute;
    this.group = group;
    this.resource = resource;
    this.message = formatMessage("%s set for %s and %s.", attribute, group, resource);
  }

  public Attribute getAttribute() {
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
