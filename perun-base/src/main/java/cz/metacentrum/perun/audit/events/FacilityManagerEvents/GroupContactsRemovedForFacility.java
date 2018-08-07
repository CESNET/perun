package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.Group;

public class GroupContactsRemovedForFacility extends AuditEvent {

	private final Group group;
	private final ContactGroup contactGroup;
	private final String message;

	public GroupContactsRemovedForFacility(Group group, ContactGroup contactGroup) {
		this.group = group;
		this.contactGroup = contactGroup;
		this.message = String.format("Group (%d) successfully removed from contact groups %s.", group.getId(), contactGroup);
	}

	public Group getGroup() {
		return group;
	}

	public ContactGroup getContactGroup() {
		return contactGroup;
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
