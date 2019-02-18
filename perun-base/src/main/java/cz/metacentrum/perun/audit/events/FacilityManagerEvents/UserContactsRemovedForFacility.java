package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.User;

public class UserContactsRemovedForFacility extends AuditEvent implements EngineIgnoreEvent {

	private User user;
	private ContactGroup contactGroup;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public UserContactsRemovedForFacility() {
	}

	public UserContactsRemovedForFacility(User user, ContactGroup contactGroup) {
		this.user = user;
		this.contactGroup = contactGroup;
		this.message = formatMessage("User (%d) successfully removed from contact groups %s.", user.getId(), contactGroup);
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
