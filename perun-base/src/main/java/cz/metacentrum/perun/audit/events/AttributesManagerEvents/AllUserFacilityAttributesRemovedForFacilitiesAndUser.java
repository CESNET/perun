package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.User;

public class AllUserFacilityAttributesRemovedForFacilitiesAndUser extends AuditEvent implements EngineIgnoreEvent {

  private User user;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AllUserFacilityAttributesRemovedForFacilitiesAndUser() {
  }

  public AllUserFacilityAttributesRemovedForFacilitiesAndUser(User user) {
    this.user = user;
    this.message = formatMessage("All non-virtual user-facility attributes removed for all facilities and %s", user);
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
