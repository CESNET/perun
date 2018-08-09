package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class FreeAllDenialsOnDestination extends AuditEvent {

	private final int destinationId;
	private final String message;

	public FreeAllDenialsOnDestination(int destinationId) {
		this.destinationId = destinationId;
		this.message = String.format("free all denials: on %d.", destinationId);
	}

	public int getDestinationId() {
		return destinationId;
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
