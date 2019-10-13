package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Attribute;

/**
 * Thrown when the attribute has wrong/illegal value.
 *
 * @author Slavek Licehammer
 */
public class WrongAttributeValueException extends AttributeValueException {

	static final long serialVersionUID = 0;
	private Attribute attribute;
	private Object attributeHolder;
	private Object attributeHolderSecondary;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public WrongAttributeValueException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongAttributeValueException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongAttributeValueException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the attribute
	 * @param attribute the attribute whose value is illegal
	 */
	public WrongAttributeValueException(Attribute attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	/**
	 * Constructor with the attribute, primary attributeHolder, secondary attributeHolder and a message
	 * @param attribute the attribute whose value is illegal
	 * @param attributeHolder primary entity holding the attribute
	 * @param attributeHolderSecondary secondary entity holding the attribute
	 * @param message message with details about the cause
	 */
	public WrongAttributeValueException(Attribute attribute, Object attributeHolder, Object attributeHolderSecondary, String message) {
		super(attribute.toString() + " Set for: " + attributeHolder + " and " + attributeHolderSecondary + " - " + message);
		this.attribute = attribute;
		this.attributeHolder = attributeHolder;
		this.attributeHolderSecondary = attributeHolderSecondary;
	}

	/**
	 * Constructor with the attribute, primary attributeHolder and secondary attributeHolder
	 * @param attribute the attribute whose value is illegal
	 * @param attributeHolder primary entity holding the attribute
	 * @param attributeHolderSecondary secondary entity holding the attribute
	 */
	public WrongAttributeValueException(Attribute attribute, Object attributeHolder, Object attributeHolderSecondary) {
		super(attribute.toString() + " Set for: " + attributeHolder + " and " + attributeHolderSecondary);
		this.attribute = attribute;
		this.attributeHolder = attributeHolder;
		this.attributeHolderSecondary = attributeHolderSecondary;
	}

	/**
	 * Constructor with the attribute, attributeHolder and a message
	 * @param attribute the attribute whose value is illegal
	 * @param attributeHolder entity holding the attribute
	 * @param message message with details about the cause
	 */
	public WrongAttributeValueException(Attribute attribute, Object attributeHolder, String message){
		super(attribute.toString() + " Set for: " + attributeHolder + " - " + message);
		this.attribute = attribute;
		this.attributeHolder = attributeHolder;
	}

	/**
	 * Constructor with the attribute, attributeHolder, a message and the Throwable object
	 * @param attribute the attribute whose value is illegal
	 * @param attributeHolder entity holding the attribute
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongAttributeValueException(Attribute attribute, Object attributeHolder, String message, Throwable cause){
		super(attribute.toString() + " Set for: " + attributeHolder + " - " + message, cause);
		this.attribute = attribute;
		this.attributeHolder = attributeHolder;
	}

	/**
	 * Constructor with the attribute and the Throwable object
	 * @param attribute attribute whose value is not correct
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongAttributeValueException(Attribute attribute, Throwable cause) {
		super(attribute.toString(), cause);
		this.attribute = attribute;
	}

	/**
	 * Constructor with the attribute and a message
	 * @param attribute attribute whose value is not correct
	 * @param message message with details about the cause
	 */
	public WrongAttributeValueException(Attribute attribute, String message) {
		super(attribute.toString() + " " + message);
		this.attribute = attribute;

	}


	/**
	 * Constructor with the attribute, the message and the Throwable object
	 * @param attribute attribute whose value is not correct
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongAttributeValueException(Attribute attribute, String message, Throwable cause) {
		super(attribute.toString() + " " + message, cause);
		this.attribute = attribute;

	}

	/**
	 * Getter for the attribute
	 * @return the attribute whose value is not correct
	 */
	public Attribute getAttribute() {
		return attribute;
	}

	/**
	 * Getter for the primary attributeHolder
	 * @return primary entity holding the attribute
	 */
	public Object getAttributeHolder() {
		return attributeHolder;
	}

	/**
	 * Getter for the secondary attributeHolder
	 * @return secondary entity holding the attribute
	 */
	public Object getAttributeHolderSecondary() {
		return attributeHolderSecondary;
	}

}
