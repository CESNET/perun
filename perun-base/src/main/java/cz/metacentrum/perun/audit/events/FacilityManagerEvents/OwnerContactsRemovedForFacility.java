package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.Owner;

public class OwnerContactsRemovedForFacility extends AuditEvent {

	private final Owner owner;
	private final ContactGroup contactGroup;
	private final String message;

	public OwnerContactsRemovedForFacility(Owner owner, ContactGroup contactGroup) {
		this.owner = owner;
		this.contactGroup = contactGroup;
		this.message = String.format("Owner (%d) successfully removed from contact groups %s.", owner.getId(), contactGroup);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Owner getOwner() {
		return owner;
	}

	public ContactGroup getContactGroup() {
		return contactGroup;
	}

	@Override
	public String toString() {
		return message;
	}
}
