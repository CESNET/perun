package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForResourceAndMember extends AuditEvent {

  private AttributeDefinition attribute;
  private Resource resource;
  private Member member;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeRemovedForResourceAndMember() {
  }

  public AttributeRemovedForResourceAndMember(AttributeDefinition attribute, Resource resource, Member member) {
    this.attribute = attribute;
    this.resource = resource;
    this.member = member;
    this.message = formatMessage("%s removed for %s and %s.", attribute, resource, member);
  }

  public AttributeDefinition getAttribute() {
    return attribute;
  }

  public Resource getResource() {
    return resource;
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
