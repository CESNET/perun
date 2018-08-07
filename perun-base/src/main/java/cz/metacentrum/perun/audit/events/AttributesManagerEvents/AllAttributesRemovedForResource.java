package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;

public class AllAttributesRemovedForResource extends AuditEvent {

	private final Resource resource;
	private final String message;

	public AllAttributesRemovedForResource(Resource resource) {
		this.resource = resource;
		this.message = String.format("All attributes removed for %s.", resource);
	}

	public Resource getResource() {
		return resource;
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
