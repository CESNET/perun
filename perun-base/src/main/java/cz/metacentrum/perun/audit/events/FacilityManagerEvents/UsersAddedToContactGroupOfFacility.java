package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class UsersAddedToContactGroupOfFacility extends AuditEvent {

	private final List<Integer> usersId;
	private final ContactGroup contactGroup;
	private final String message;

	public UsersAddedToContactGroupOfFacility(List<Integer> usersId, ContactGroup contactGroup) {
		this.usersId = usersId;
		this.contactGroup = contactGroup;
		this.message = String.format("Users (%s) successfully added to contact group %s.", usersId, contactGroup);
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
