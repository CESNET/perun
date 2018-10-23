package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;

public class AllAttributesRemovedForResource extends AuditEvent {

	private Resource resource;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AllAttributesRemovedForResource() {
	}

	public AllAttributesRemovedForResource(Resource resource) {
		this.resource = resource;
		this.message = formatMessage("All attributes removed for %s.", resource);
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
