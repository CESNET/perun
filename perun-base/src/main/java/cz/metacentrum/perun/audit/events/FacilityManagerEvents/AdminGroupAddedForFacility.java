package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;

public class AdminGroupAddedForFacility extends AuditEvent implements EngineIgnoreEvent {

	private Group group;
	private Facility facility;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AdminGroupAddedForFacility() {
	}

	public AdminGroupAddedForFacility(Group group, Facility facility) {
		this.group = group;
		this.facility = facility;
		this.message = formatMessage("%s was added as admin of %s.", group, facility);
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
