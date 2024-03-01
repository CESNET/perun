package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForKey extends AuditEvent {

  private Attribute attribute;
  private String key;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeSetForKey() {
  }

  public AttributeSetForKey(Attribute attribute, String key) {
    this.attribute = attribute;
    this.key = key;
    this.message = formatMessage("%s set for %s.", attribute, key);
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public String getKey() {
    return key;
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
