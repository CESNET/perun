package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.User;

public class UserContactsRemovedForFacility implements AuditEvent {

	private User userId;
	private ContactGroup contactGroup;
	private String name = this.getClass().getName();
	private String message;

	public UserContactsRemovedForFacility(User id, ContactGroup contactGroup) {
		userId = id;
		this.contactGroup = contactGroup;
	}

	public UserContactsRemovedForFacility() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public User getUserId() {
		return userId;
	}

	public void setUserId(User userId) {
		this.userId = userId;
	}

	public ContactGroup getContactGroup() {
		return contactGroup;
	}

	public void setContactGroup(ContactGroup contactGroup) {
		this.contactGroup = contactGroup;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "User (" + userId.getId() + ") successfully removed from contact groups " + contactGroup.toString() + ".";
	}
}
