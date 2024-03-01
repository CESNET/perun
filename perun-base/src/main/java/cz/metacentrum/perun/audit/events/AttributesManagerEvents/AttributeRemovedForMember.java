package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Member;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForMember extends AuditEvent {

  private AttributeDefinition attribute;
  private Member member;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeRemovedForMember() {
  }

  public AttributeRemovedForMember(AttributeDefinition attribute, Member member) {
    this.attribute = attribute;
    this.member = member;
    this.message = formatMessage("%s removed for %s.", attribute, member);
  }

  public AttributeDefinition getAttribute() {
    return attribute;
  }

  public Member getMember() {
    return member;
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
