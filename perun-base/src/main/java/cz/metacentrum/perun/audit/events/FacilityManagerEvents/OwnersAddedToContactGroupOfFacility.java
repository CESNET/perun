package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ContactGroup;

import java.util.List;

public class OwnersAddedToContactGroupOfFacility implements AuditEvent {

	private List<Integer> ownersId;
	private ContactGroup contactGroup;
	private String name = this.getClass().getName();
	private String message;

	public OwnersAddedToContactGroupOfFacility(List<Integer> ownersId, ContactGroup contactGroup) {
		this.ownersId = ownersId;
		this.contactGroup = contactGroup;
	}

	public OwnersAddedToContactGroupOfFacility() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<Integer> getOwnersId() {
		return ownersId;
	}

	public void setOwnersId(List<Integer> ownersId) {
		this.ownersId = ownersId;
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
		return "Owners (" + ownersId.toString() + ") successfully added to contact group " + contactGroup.toString() + ".";
	}
}
