package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForMemberAndGroup extends AuditEvent {

  private AttributeDefinition attribute;
  private Member member;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeRemovedForMemberAndGroup() {
  }

  public AttributeRemovedForMemberAndGroup(AttributeDefinition attribute, Member member, Group group) {
    this.attribute = attribute;
    this.member = member;
    this.group = group;
    this.message = formatMessage("%s removed for %s and %s.", attribute, member, group);
  }

  public AttributeDefinition getAttribute() {
    return attribute;
  }

  public Member getMember() {
    return member;
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
