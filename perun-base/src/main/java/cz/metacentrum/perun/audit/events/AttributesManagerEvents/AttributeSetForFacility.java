package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForFacility extends AuditEvent {

  private Attribute attribute;
  private Facility facility;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeSetForFacility() {
  }

  public AttributeSetForFacility(Attribute attribute, Facility facility) {
    this.attribute = attribute;
    this.facility = facility;
    this.message = formatMessage("%s set for %s.", attribute, facility);
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public Facility getFacility() {
    return facility;
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
