package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Destination;

public class DestinationCreated extends AuditEvent {

	private Destination destination;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public DestinationCreated() {
	}

	public DestinationCreated(Destination destination) {
		this.destination = destination;
		this.message = formatMessage("%s created.", destination);
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
