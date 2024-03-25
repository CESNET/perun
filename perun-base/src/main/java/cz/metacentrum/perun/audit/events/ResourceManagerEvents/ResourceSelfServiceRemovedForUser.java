package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;

/**
 * Event for removing the ResourceSelfService role for user.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ResourceSelfServiceRemovedForUser extends AuditEvent implements EngineIgnoreEvent {

  private Resource resource;
  private User user;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ResourceSelfServiceRemovedForUser() {
  }

  public ResourceSelfServiceRemovedForUser(Resource resource, User user) {
    this.resource = resource;
    this.user = user;
    this.message = formatMessage("%s was removed as ResourceSelfService for %s.", user, resource);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Resource getResource() {
    return resource;
  }

  public User getUser() {
    return user;
  }

  @Override
  public String toString() {
    return message;
  }
}
