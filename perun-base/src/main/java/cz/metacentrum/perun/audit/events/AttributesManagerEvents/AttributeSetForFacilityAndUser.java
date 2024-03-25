package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForFacilityAndUser extends AuditEvent {

  private Attribute attribute;
  private Facility facility;
  private User user;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AttributeSetForFacilityAndUser() {
  }

  public AttributeSetForFacilityAndUser(Attribute attribute, Facility facility, User user) {
    this.attribute = attribute;
    this.facility = facility;
    this.user = user;
    this.message = formatMessage("%s set for %s and %s.", attribute, facility, user);
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

  public User getUser() {
    return user;
  }

  @Override
  public String toString() {
    return message;
  }
}
