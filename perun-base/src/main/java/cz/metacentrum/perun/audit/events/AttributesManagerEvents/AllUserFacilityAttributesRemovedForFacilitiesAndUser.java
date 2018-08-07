package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class AllUserFacilityAttributesRemovedForFacilitiesAndUser extends AuditEvent {

	private final User user;
	private final String message;

	public AllUserFacilityAttributesRemovedForFacilitiesAndUser(User user) {
		this.user = user;
		this.message = String.format("All non-virtual user-facility attributes removed for all facilities and %s", user);
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
