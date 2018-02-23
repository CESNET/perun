package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * Attribute is already marked as unique.
 *
 * @author Martin Kuba
 */
public class AttributeAlreadyMarkedUniqueException extends PerunException {

	private AttributeDefinition attribute;

	public AttributeAlreadyMarkedUniqueException(String message, AttributeDefinition attributeDefinition) {
		super(message + " " + attributeDefinition.toString());
		this.attribute = attributeDefinition;

	}


	public AttributeDefinition getAttribute() {
		return attribute;
	}

}
