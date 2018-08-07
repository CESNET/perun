package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;

public class AllAttributesRemovedForFacilityAndUser extends AuditEvent {

	private final Facility facility;
	private final User user;
	private final String message;

	public AllAttributesRemovedForFacilityAndUser(Facility facility, User user) {
		this.facility = facility;
		this.user = user;
		this.message = String.format("All attributes removed for %s and %s.", facility, user);
	}

	public Facility getFacility() {
		return facility;
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
