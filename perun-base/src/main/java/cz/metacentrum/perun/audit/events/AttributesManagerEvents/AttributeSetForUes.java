package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.UserExtSource;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForUes extends AuditEvent {

	private final Attribute attribute;
	private final UserExtSource ues;
	private final String message;

	public AttributeSetForUes(Attribute attribute, UserExtSource ues) {
		this.attribute = attribute;
		this.ues = ues;
		this.message = String.format("%s set for %s.", attribute, ues);
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public UserExtSource getUes() {
		return ues;
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
