package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Destination;

public class DestinationCreated extends AuditEvent {

	private final Destination destination;
	private final String message;

	public DestinationCreated(Destination destination) {
		this.destination = destination;
		this.message = String.format("%s created.", destination);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Destination getDestination() {
		return destination;
	}

	@Override
	public String toString() {
		return message;
	}
}
