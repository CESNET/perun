package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * Attribute is already marked as unique.
 *
 * @author Martin Kuba
 */
public class AttributeAlreadyMarkedUniqueException extends PerunException {

	private AttributeDefinition attribute;

	/**
	 *
	 * @param message message with details about the cause
	 * @param attributeDefinition attributeDefinition that has already been marked as unique
	 */
	public AttributeAlreadyMarkedUniqueException(String message, AttributeDefinition attributeDefinition) {
		super(message + " " + attributeDefinition.toString());
		this.attribute = attributeDefinition;

	}

	/**
	 * Getter for the attribute that has already been marked as unique
	 * @return attributeDefinition that has already been marked as unique
	 */
	public AttributeDefinition getAttribute() {
		return attribute;
	}

}
