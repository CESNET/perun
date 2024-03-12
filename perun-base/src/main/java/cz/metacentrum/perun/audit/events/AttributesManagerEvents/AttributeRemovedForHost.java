package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Host;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForHost extends AuditEvent {

  private AttributeDefinition attribute;
  private Host host;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeRemovedForHost() {
  }

  public AttributeRemovedForHost(AttributeDefinition attribute, Host host) {
    this.attribute = attribute;
    this.host = host;
    this.message = formatMessage("%s removed for %s.", attribute, host);
  }

  public AttributeDefinition getAttribute() {
    return attribute;
  }

  public Host getHost() {
    return host;
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
