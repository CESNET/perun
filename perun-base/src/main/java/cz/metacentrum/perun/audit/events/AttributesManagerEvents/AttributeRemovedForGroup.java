package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForGroup extends AuditEvent {

  private AttributeDefinition attribute;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeRemovedForGroup() {
  }

  public AttributeRemovedForGroup(AttributeDefinition attribute, Group group) {
    this.attribute = attribute;
    this.group = group;
    this.message = formatMessage("%s removed for %s.", attribute, group);
  }

  public AttributeDefinition getAttribute() {
    return attribute;
  }

  public Group getGroup() {
    return group;
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
