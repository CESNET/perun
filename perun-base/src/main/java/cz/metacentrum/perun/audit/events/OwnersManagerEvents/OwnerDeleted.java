package cz.metacentrum.perun.audit.events.OwnersManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Owner;

public class OwnerDeleted extends AuditEvent {

	private final Owner owner;
	private final String message;

	public OwnerDeleted(Owner owner) {
		this.owner = owner;
		this.message = String.format("%s deleted.", owner);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Owner getOwner() {
		return owner;
	}

	@Override
	public String toString() {
		return message;
	}
}
