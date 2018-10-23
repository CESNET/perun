package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeRights;

public class AttributeRightsSet extends AuditEvent {

	private AttributeRights rights;
	private String message;

	@SuppressWarnings("unused") // used by jackson mapper
	public AttributeRightsSet() {
	}

	public AttributeRightsSet(AttributeRights rights) {
		this.rights = rights;
		this.message = formatMessage("Attribute right set: %s", rights);
	}

	public AttributeRights getRights() {
		return rights;
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
