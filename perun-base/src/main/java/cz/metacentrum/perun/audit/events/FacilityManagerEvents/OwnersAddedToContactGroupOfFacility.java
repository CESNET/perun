package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class OwnersAddedToContactGroupOfFacility extends AuditEvent {

	private List<Integer> ownersId;
	private ContactGroup contactGroup;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public OwnersAddedToContactGroupOfFacility() {
	}

	public OwnersAddedToContactGroupOfFacility(List<Integer> ownersId, ContactGroup contactGroup) {
		this.ownersId = ownersId;
		this.contactGroup = contactGroup;
		this.message = formatMessage("Owners (%s) successfully added to contact group %s.", ownersId, contactGroup);
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
