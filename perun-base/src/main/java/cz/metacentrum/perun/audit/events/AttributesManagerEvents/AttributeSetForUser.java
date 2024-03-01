package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForUser extends AuditEvent {

  private Attribute attribute;
  private User user;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeSetForUser() {
  }

  public AttributeSetForUser(Attribute attribute, User user) {
    this.attribute = attribute;
    this.user = user;
    this.message = formatMessage("%s set for %s.", attribute, user);
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public User getUser() {
    return user;
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
