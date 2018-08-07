package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.BanOnFacility;

public class BanSetForFacility extends AuditEvent {

	private final BanOnFacility banOnFacility;
	private final int userId;
	private final int facilityId;
	private final String message;

	public BanSetForFacility(BanOnFacility banOnFacility, int userId, int facilityId) {
		this.banOnFacility = banOnFacility;
		this.userId = userId;
		this.facilityId = facilityId;
		this.message = String.format("Ban %s was set for userId %s on facilityId %s.", banOnFacility, userId, facilityId);
	}

	public BanOnFacility getBanOnFacility() {
		return banOnFacility;
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
