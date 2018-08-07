package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.BanOnFacility;

public class BanRemovedForFacility extends AuditEvent {

	private final BanOnFacility ban;
	private final int userId;
	private final int facilityId;
	private final String message;

	public BanRemovedForFacility(BanOnFacility ban, int userId, int facilityId) {
		this.ban = ban;
		this.userId = userId;
		this.facilityId = facilityId;
		this.message = String.format("Ban %s was removed for userId %s on facilityId %s.", ban, userId, facilityId);
	}

	public BanOnFacility getBan() {
		return ban;
	}

	public int getUserId() {
		return userId;
	}

	public int getFacilityId() {
		return facilityId;
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
