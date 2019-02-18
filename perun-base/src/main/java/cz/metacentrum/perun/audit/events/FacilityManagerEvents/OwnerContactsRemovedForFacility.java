package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.Owner;

public class OwnerContactsRemovedForFacility extends AuditEvent implements EngineIgnoreEvent {

	private Owner owner;
	private ContactGroup contactGroup;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public OwnerContactsRemovedForFacility() {
	}

	public OwnerContactsRemovedForFacility(Owner owner, ContactGroup contactGroup) {
		this.owner = owner;
		this.contactGroup = contactGroup;
		this.message = formatMessage("Owner (%d) successfully removed from contact groups %s.", owner.getId(), contactGroup);
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
