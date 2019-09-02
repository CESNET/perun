package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * Something is wrong with attribute value. See exception which extends this exception.
 *
 * @author Slavek Licehammer
 */
public class AttributeValueException extends PerunException {

	static final long serialVersionUID = 0;
	private AttributeDefinition attribute;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public AttributeValueException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AttributeValueException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public AttributeValueException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with an attribute in which the problem occured
	 * @param attribute attribute in which the problem occured
	 */
	public AttributeValueException(Attribute attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	/**
	 * Constructor with an attribute in which the problem occured and the throwable
	 * @param attribute attribute in which the problem occured
	 * @param cause the cause
	 */
	public AttributeValueException(Attribute attribute, Throwable cause) {
		super(attribute.toString(), cause);
		this.attribute = attribute;
	}

	/**
	 * Constructor with an attribute in which the problem occured and the message with details
	 * @param attribute attribute in which the problem occured
	 * @param message message with details
	 */
	public AttributeValueException(Attribute attribute, String message) {
		super(message + " " + attribute.toString());
		this.attribute = attribute;

	}

	/**
	 * Constructor with an attribute in which the problem occured, the throwable and the message with details
	 * @param attribute attribute in which the problem occured
	 * @param message message with details
	 * @param cause the cause
	 */
	public AttributeValueException(Attribute attribute, String message, Throwable cause) {
		super(message + " " + attribute.toString(), cause);
		this.attribute = attribute;

	}

	/**
	 * Getter for attribute
	 * @return attribute in which the problem occured
	 */
	public AttributeDefinition getAttribute() {
		return attribute;
	}

}
