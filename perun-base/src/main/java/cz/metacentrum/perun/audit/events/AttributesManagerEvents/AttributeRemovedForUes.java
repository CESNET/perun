package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.UserExtSource;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForUes extends AuditEvent {

  private AttributeDefinition attribute;
  private UserExtSource ues;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeRemovedForUes() {
  }

  public AttributeRemovedForUes(AttributeDefinition attribute, UserExtSource ues) {
    this.attribute = attribute;
    this.ues = ues;
    this.message = formatMessage("%s removed for %s.", attribute, ues);
  }

  public AttributeDefinition getAttribute() {
    return attribute;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public UserExtSource getUes() {
    return ues;
  }

  @Override
  public String toString() {
    return message;
  }
}
