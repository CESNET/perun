package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;

@Deprecated
public class AttributeAuthzDeleted extends AuditEvent implements EngineIgnoreEvent {

  private AttributeDefinition attributeDefinition;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeAuthzDeleted() {
  }

  public AttributeAuthzDeleted(AttributeDefinition attribute) {
    attributeDefinition = attribute;
    this.message = formatMessage("All authorization information were deleted for %s.", attributeDefinition);
  }

  public AttributeDefinition getAttributeDefinition() {
    return attributeDefinition;
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
