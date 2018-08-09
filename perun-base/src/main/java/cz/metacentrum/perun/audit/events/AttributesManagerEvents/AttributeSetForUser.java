package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForUser extends AuditEvent {

	private final Attribute attribute;
	private final User user;
	private final String message;

	public AttributeSetForUser(Attribute attribute, User user) {
		this.attribute = attribute;
		this.user = user;
		this.message = String.format("%s set for %s.", attribute, user);
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public User getUser() {
		return user;
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
