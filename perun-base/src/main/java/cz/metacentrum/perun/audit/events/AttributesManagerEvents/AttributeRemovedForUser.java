package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForUser extends AuditEvent {

  private AttributeDefinition attribute;
  private User user;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeRemovedForUser() {
  }

  public AttributeRemovedForUser(AttributeDefinition attribute, User user) {
    this.attribute = attribute;
    this.user = user;
    this.message = formatMessage("%s removed for %s.", attribute, user);
  }

  public AttributeDefinition getAttribute() {
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
