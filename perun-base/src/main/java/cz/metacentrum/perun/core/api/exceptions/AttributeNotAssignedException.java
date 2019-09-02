package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * This exception raises when you try to remove an attribute which was not assigned before.
 *
 * @author Slavek Licehammer
 */
public class AttributeNotAssignedException extends EntityNotAssignedException {
	static final long serialVersionUID = 0;

	private AttributeDefinition attribute;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public AttributeNotAssignedException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AttributeNotAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AttributeNotAssignedException(Throwable cause) {
		super(cause);
	}

	/**
	 *
	 * @param attribute that has not been assigned before
	 */
	public AttributeNotAssignedException(AttributeDefinition attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	/**
	 *
	 * @return attribute that has not been assigned before
	 */
	public AttributeDefinition getAttribute() {
		return attribute;
	}
}
