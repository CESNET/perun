package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.BanOnResource;

public class BanRemovedForResource extends AuditEvent {

	private final BanOnResource banOnResource;
	private final int memberId;
	private final int resourceId;
	private final String message;

	public BanRemovedForResource(BanOnResource ban, int memberId, int resourceId) {
		this.banOnResource = ban;
		this.memberId = memberId;
		this.resourceId = resourceId;
		this.message = String.format("Ban %s was removed for memberId %d on resourceId %d.", banOnResource, memberId,
				resourceId);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public BanOnResource getBanOnResource() {
		return banOnResource;
	}

	public int getMemberId() {
		return memberId;
	}

	public int getResourceId() {
		return resourceId;
	}

	@Override
	public String toString() {
		return message;
	}
}
