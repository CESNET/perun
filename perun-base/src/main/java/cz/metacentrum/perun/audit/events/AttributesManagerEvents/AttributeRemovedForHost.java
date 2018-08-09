package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Host;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForHost extends AuditEvent {

	private final AttributeDefinition attribute;
	private final Host host;
	private final String message;

	public AttributeRemovedForHost(AttributeDefinition attribute, Host host) {
		this.attribute = attribute;
		this.host = host;
		this.message = String.format("%s removed for %s.", attribute, host);
	}

	public AttributeDefinition getAttribute() {
		return attribute;
	}

	public Host getHost() {
		return host;
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
