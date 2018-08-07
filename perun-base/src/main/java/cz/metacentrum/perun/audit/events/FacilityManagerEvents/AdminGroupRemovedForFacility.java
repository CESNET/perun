package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;

public class AdminGroupRemovedForFacility extends AuditEvent {

	private final Group group;
	private final Facility facility;
	private final String message;

	public AdminGroupRemovedForFacility(Group group, Facility facility) {
		this.group = group;
		this.facility = facility;
		this.message = String.format("Group %s was removed from admins of %s.", group, facility);
	}

	public Group getGroup() {
		return group;
	}

	public Facility getFacility() {
		return facility;
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
