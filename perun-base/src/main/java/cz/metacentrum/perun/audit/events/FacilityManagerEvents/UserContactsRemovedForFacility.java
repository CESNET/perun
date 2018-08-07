package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.User;

public class UserContactsRemovedForFacility extends AuditEvent {

	private final User user;
	private final ContactGroup contactGroup;
	private final String message;

	public UserContactsRemovedForFacility(User user, ContactGroup contactGroup) {
		this.user = user;
		this.contactGroup = contactGroup;
		this.message = String.format("User (%d) successfully removed from contact groups %s.", user.getId(), contactGroup);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public User getUser() {
		return user;
	}

	public ContactGroup getContactGroup() {
		return contactGroup;
	}

	@Override
	public String toString() {
		return message;
	}
}
