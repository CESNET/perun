package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class GroupsAddedToContactGroupOfFacility extends AuditEvent {

	private final List<Integer> groupsId;
	private final ContactGroup contactGroup;
	private final String message;

	public GroupsAddedToContactGroupOfFacility(List<Integer> groupsId, ContactGroup contactGroup) {
		this.groupsId = groupsId;
		this.contactGroup = contactGroup;
		this.message = String.format("Groups (%s) successfully added from contact groups %s.", groupsId, contactGroup);
	}

	public List<Integer> getGroupsId() {
		return groupsId;
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
