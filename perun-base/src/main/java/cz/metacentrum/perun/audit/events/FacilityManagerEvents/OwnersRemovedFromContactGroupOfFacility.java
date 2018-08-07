package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class OwnersRemovedFromContactGroupOfFacility extends AuditEvent {

	private final List<Integer> ownersId;
	private final ContactGroup contactGroup;
	private final String message;

	public OwnersRemovedFromContactGroupOfFacility(List<Integer> ownersId, ContactGroup contactGroup) {
		this.ownersId = ownersId;
		this.contactGroup = contactGroup;
		this.message = String.format("Owners (%s) successfully removed from contact group %s.", ownersId, contactGroup);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public List<Integer> getOwnersId() {
		return ownersId;
	}

	public ContactGroup getContactGroup() {
		return contactGroup;
	}

	@Override
	public String toString() {
		return message;
	}
}
