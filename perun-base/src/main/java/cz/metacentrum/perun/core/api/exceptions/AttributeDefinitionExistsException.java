package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * Attribute already exists in underlaying data source. Thrown where creating atribute thats already exists.
 *
 */
public class AttributeDefinitionExistsException extends EntityExistsException {

	static final long serialVersionUID = 0;

	private final AttributeDefinition attributeDefinition;

	public AttributeDefinitionExistsException(String message, AttributeDefinition attributeDefinition, Throwable cause) {
		super(message, cause);
		this.attributeDefinition = attributeDefinition;
	}

	public AttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}
}
