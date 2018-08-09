package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributeRemovedForFacility extends AuditEvent {

	private final AttributeDefinition attribute;
	private final Facility facility;
	private final String message;

	public AttributeRemovedForFacility(AttributeDefinition attribute, Facility facility) {
		this.attribute = attribute;
		this.facility = facility;
		this.message = String.format("%s removed for %s.", attribute, facility);
	}

	public AttributeDefinition getAttribute() {
		return attribute;
	}

	public Facility getFacility() {
		return facility;
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
