package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.BanOnResource;

public class BanRemovedForResource extends AuditEvent {

  private BanOnResource banOnResource;
  private int memberId;
  private int resourceId;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public BanRemovedForResource() {
  }

  public BanRemovedForResource(BanOnResource ban, int memberId, int resourceId) {
    this.banOnResource = ban;
    this.memberId = memberId;
    this.resourceId = resourceId;
    this.message =
        formatMessage("Ban %s was removed for memberId %d on resourceId %d.", banOnResource, memberId, resourceId);
  }

  public BanOnResource getBanOnResource() {
    return banOnResource;
  }

  public int getMemberId() {
    return memberId;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public int getResourceId() {
    return resourceId;
  }

  @Override
  public String toString() {
    return message;
  }
}
