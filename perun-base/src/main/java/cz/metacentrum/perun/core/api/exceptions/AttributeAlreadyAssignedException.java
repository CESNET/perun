package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * Attribute is already assigned to some object. This exception raises when you assign (or add) attribute to some object which had the attribute assigned before.
 *
 * @author Slavek Licehammer
 */
public class AttributeAlreadyAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private AttributeDefinition attribute;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public AttributeAlreadyAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AttributeAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AttributeAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with an attribute
	 * @param attribute that has already been assigned
	 */
	public AttributeAlreadyAssignedException(AttributeDefinition attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	/**
	 *
	 * @return attribute that has already been assigned
	 */
	public AttributeDefinition getAttribute() {
		return attribute;
	}
}
