package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * This exception is thrown when you try to assign attribute as required by the service but the attribute has already been assigned.
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
	 * Getter for the attribute that has already been assigned
	 * @return attribute that has already been assigned
	 */
	public AttributeDefinition getAttribute() {
		return attribute;
	}
}
