package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class GroupsRemovedFromContactGroupOfFacility extends AuditEvent {

	private List<Integer> groupsId;
	private ContactGroup contactGroup;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public GroupsRemovedFromContactGroupOfFacility() {
	}

	public GroupsRemovedFromContactGroupOfFacility(List<Integer> groupsId, ContactGroup contactGroup) {
		this.groupsId = groupsId;
		this.contactGroup = contactGroup;
		this.message = formatMessage("Groups (%s) successfully removed from contact group %s.", groupsId, contactGroup);
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
