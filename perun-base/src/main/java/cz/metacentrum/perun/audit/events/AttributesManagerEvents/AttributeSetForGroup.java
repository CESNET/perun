package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForGroup extends AuditEvent {

  private Attribute attribute;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeSetForGroup() {
  }

  public AttributeSetForGroup(Attribute attribute, Group group) {
    this.attribute = attribute;
    this.group = group;
    this.message = formatMessage("%s set for %s.", attribute, group);
  }

  public Attribute getAttribute() {
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
