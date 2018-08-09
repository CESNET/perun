package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeSetForGroup extends AuditEvent {

	private final Attribute attribute;
	private final Group group;
	private final String message;

	public AttributeSetForGroup(Attribute attribute, Group group) {
		this.attribute = attribute;
		this.group = group;
		this.message = String.format("%s set for %s.", attribute, group);
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public Group getGroup() {
		return group;
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
