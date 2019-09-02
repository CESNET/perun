package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * Attribute already exists in underlying data source. Thrown when creating attribute that already exists.
 *
 */
public class AttributeDefinitionExistsException extends EntityExistsException {

	static final long serialVersionUID = 0;

	private final AttributeDefinition attributeDefinition;

	/**
	 *
	 * @param message message with details about the cause
	 * @param attributeDefinition attributeDefinition that already exists
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AttributeDefinitionExistsException(String message, AttributeDefinition attributeDefinition, Throwable cause) {
		super(message, cause);
		this.attributeDefinition = attributeDefinition;
	}

	/**
	 *
	 * @return attributeDefinition that already exists
	 */
	public AttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}
}
