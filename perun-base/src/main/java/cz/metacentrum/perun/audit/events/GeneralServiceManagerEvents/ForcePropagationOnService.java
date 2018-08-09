package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ForcePropagationOnService extends AuditEvent {

	private final Service service;
	private final String message;

	public ForcePropagationOnService(Service service) {
		this.service = service;
		this.message = String.format("force propagation: On %s.", service);
	}

	public Service getService() {
		return service;
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
