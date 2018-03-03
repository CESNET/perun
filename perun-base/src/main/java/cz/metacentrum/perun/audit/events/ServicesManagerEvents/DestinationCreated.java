package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Destination;

public class DestinationCreated implements AuditEvent {

	private Destination destination;
	private String name = this.getClass().getName();
	private String message;

	public DestinationCreated() {
	}

	public DestinationCreated(Destination destination) {
		this.destination = destination;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return destination + " created.";
	}
}
