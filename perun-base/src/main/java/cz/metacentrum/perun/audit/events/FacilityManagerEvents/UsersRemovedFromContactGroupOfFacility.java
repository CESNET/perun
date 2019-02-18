package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class UsersRemovedFromContactGroupOfFacility extends AuditEvent implements EngineIgnoreEvent {

	private List<Integer> usersId;
	private ContactGroup contactGroup;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public UsersRemovedFromContactGroupOfFacility() {
	}

	public UsersRemovedFromContactGroupOfFacility(List<Integer> usersId, ContactGroup contactGroup) {
		this.usersId = usersId;
		this.contactGroup = contactGroup;
		this.message = formatMessage("Users (%s) successfully removed from contact group %s.", usersId, contactGroup);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public List<Integer> getUsersId() {
		return usersId;
	}

	public ContactGroup getContactGroup() {
		return contactGroup;
	}

	@Override
	public String toString() {
		return message;
	}
}
