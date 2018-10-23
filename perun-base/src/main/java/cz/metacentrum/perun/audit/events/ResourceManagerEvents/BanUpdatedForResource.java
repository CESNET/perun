package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.BanOnResource;

public class BanUpdatedForResource extends AuditEvent {

	private BanOnResource banOnResource;
	private int memberId;
	private int resourceId;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public BanUpdatedForResource() {
	}

	public BanUpdatedForResource(BanOnResource banOnResource, int memberId, int resourceId) {
		this.banOnResource = banOnResource;
		this.memberId = memberId;
		this.resourceId = resourceId;
		this.message = formatMessage("Ban %s was updated for memberId %d on resourceId %d.", banOnResource, memberId,
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
