package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;

public class AllAttributesRemovedForMemberAndGroup extends AuditEvent implements EngineIgnoreEvent {

  private Member member;
  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AllAttributesRemovedForMemberAndGroup() {
  }

  public AllAttributesRemovedForMemberAndGroup(Member member, Group group) {
    this.member = member;
    this.group = group;
    this.message = formatMessage("All attributes removed for %s and %s.", member, group);
  }

  public Group getGroup() {
    return group;
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
