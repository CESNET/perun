package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;

public class AllAttributesRemovedForFacilityAndUser extends AuditEvent {

	private Facility facility;
	private User user;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AllAttributesRemovedForFacilityAndUser() {
	}

	public AllAttributesRemovedForFacilityAndUser(Facility facility, User user) {
		this.facility = facility;
		this.user = user;
		this.message = formatMessage("All attributes removed for %s and %s.", facility, user);
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
