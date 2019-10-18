package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * Thrown while assigning attribute to wrong entity. For example if you try to set value for the facility to attribute which is only for resources.
 *
 * @author Slavek Licehammer
 */
public class WrongAttributeAssignmentException extends PerunException {
	static final long serialVersionUID = 0;

	private AttributeDefinition attribute;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public WrongAttributeAssignmentException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongAttributeAssignmentException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongAttributeAssignmentException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the attribute
	 * @param attribute attribute that was supposed to be assigned to the wrong entity
	 */
	public WrongAttributeAssignmentException(AttributeDefinition attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	/**
	 * Getter for the attribute
	 * @return attribute that was supposed to be assigned to the wrong entity
	 */
	public AttributeDefinition getAttribute() {
		return attribute;
	}
}
