package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.AttributePolicyCollection;

public class AttributePolicyCollectionSet extends AuditEvent implements EngineIgnoreEvent {

  private AttributePolicyCollection policyCollection;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributePolicyCollectionSet() {
  }

  public AttributePolicyCollectionSet(AttributePolicyCollection apc) {
    this.policyCollection = apc;
    this.message = formatMessage("Attribute policy collection set: %s for %s", policyCollection, apc.getAttributeId());
  }

  @Override
  public String getMessage() {
    return message;
  }

  public AttributePolicyCollection getPolicyCollection() {
    return policyCollection;
  }

  @Override
  public String toString() {
    return message;
  }
}
