package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;

public class AllAttributesRemovedForResourceAndMember extends AuditEvent implements EngineIgnoreEvent {

  private Resource resource;
  private Member member;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AllAttributesRemovedForResourceAndMember() {
  }

  public AllAttributesRemovedForResourceAndMember(Resource resource, Member member) {
    this.resource = resource;
    this.member = member;
    this.message = formatMessage("All attributes removed for %s and %s.", resource, member);
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
