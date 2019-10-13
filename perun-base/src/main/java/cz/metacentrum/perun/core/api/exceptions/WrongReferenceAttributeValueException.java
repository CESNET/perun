package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;

/**
 * Attribute which is reference for used attribute has illegal value. Because of it, we can't determinate if used attribute have correct value.
 *
 * @author Slavek Licehammer
 * @author Michal Prochazka
 */
public class WrongReferenceAttributeValueException extends AttributeValueException {

	static final long serialVersionUID = 0;
	private AttributeDefinition attribute;
	private AttributeDefinition referenceAttribute;

	/**
	 * Simple constructor with a message
	 * @param message message with details about the cause
	 */
	public WrongReferenceAttributeValueException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and Throwable object
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongReferenceAttributeValueException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with a Throwable object
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongReferenceAttributeValueException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the attribute
	 * @param attribute attribute whose referenceAttribute has illegal value
	 */
	public WrongReferenceAttributeValueException(AttributeDefinition attribute) {
		super(attribute == null ? "Attribute: null" : attribute.toString());
		this.attribute = attribute;
	}

	/**
	 * Constructor with the attribute and a message
	 * @param attribute attribute whose referenceAttribute has illegal value
	 * @param message message with details about the cause
	 */
	public WrongReferenceAttributeValueException(AttributeDefinition attribute, String message) {
		super(attribute == null ? "Attribute: null" : attribute + "  " + message);
		this.attribute = attribute;
	}

	/**
	 * Constructor with the attribute and the reference attribute
	 * @param attribute attribute whose referenceAttribute has illegal value
	 * @param referenceAttribute the attribute whose value is illegal
	 */
	public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute) {
		super((attribute == null ? "Attribute: null" : attribute) + " reference attribute " + (referenceAttribute == null ? "null" : referenceAttribute));
		this.attribute = attribute;
		this.referenceAttribute = referenceAttribute;
	}
	/**
	 * Constructor with the attribute, the reference attribute and the Throwable object
	 * @param attribute attribute whose referenceAttribute has illegal value
	 * @param referenceAttribute the attribute whose value is illegal
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute, Throwable cause) {
		super((attribute == null ? "Attribute: null" : attribute) + " reference attribute " + (referenceAttribute == null ? "null" : referenceAttribute) +  " cause: " + cause.getMessage(), cause);
		this.attribute = attribute;
		this.referenceAttribute = referenceAttribute;
	}

	/**
	 * Constructor with the attribute, the reference attribute and a message
	 * @param attribute attribute whose referenceAttribute has illegal value
	 * @param referenceAttribute the attribute whose value is illegal
	 * @param message message with details about the cause
	 */
	public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute, String message) {
		super(attribute == null ? "Attribute: null" : attribute + " reference attribute " + referenceAttribute == null ? "null" : referenceAttribute + " " + message);
		this.attribute = attribute;
		this.referenceAttribute = referenceAttribute;
	}

	/**
	 * Constructor with the attribute, the reference attribute, a message and the Throwable object
	 * @param attribute attribute whose referenceAttribute has illegal value
	 * @param referenceAttribute the attribute whose value is illegal
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute, String message, Throwable cause) {
		super(attribute == null ? "Attribute: null" : attribute + " reference attribute " + referenceAttribute == null ? "null" : referenceAttribute + " " + message + " cause:" + cause.getMessage());
		this.attribute = attribute;
		this.referenceAttribute = referenceAttribute;
	}

	/**
	 * Constructor with the attribute, the reference attribute, primary attributHolder, secondary attributeHolder and a message
	 * @param attribute attribute whose referenceAttribute has illegal value
	 * @param referenceAttribute the attribute whose value is illegal
	 * @param attributeHolderPrimary primary entity holding the attribute
	 * @param attributeHolderSecondary secondary entity holding the attribute
	 * @param message message with details about the cause
	 */
	public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute, Object attributeHolderPrimary, Object attributeHolderSecondary, String message) {
		super((attribute == null ? "Attribute: null" : attribute) + " reference attribute: " + (referenceAttribute == null ? "null" : referenceAttribute) + " " + (attributeHolderPrimary == null ? "AttributeHolderPrimary: null" : attributeHolderPrimary) + " " + (attributeHolderSecondary == null ? "" : attributeHolderSecondary) + " " + message);
		this.attribute = attribute;
		this.referenceAttribute = referenceAttribute;
	}

	/**
	 * Constructor with the attribute, the reference attribute, primary attributeHolder, secondary attributeHolder,
	 * primary attributeHolder of the referenceAttribute, secondary attributeHolder of the referenceAttribute and a message
	 * @param attribute attribute whose referenceAttribute has illegal value
	 * @param referenceAttribute the attribute whose value is illegal
	 * @param attributeHolderPrimary primary entity holding the attribute
	 * @param attributeHolderSecondary secondary entity holding the attribute
	 * @param refAttributeHolderPrimary primary entity holding the attribute of the referenceAttribute
	 * @param refAttributeHolderSecondary secondary entity holding the attribute of the referenceAttribute
	 * @param message message with details about the cause
	 */
	public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute, Object attributeHolderPrimary, Object attributeHolderSecondary, Object refAttributeHolderPrimary, Object refAttributeHolderSecondary, String message) {
		super("Attribute: " + (attribute == null ? "null" : attribute) +
				", reference attribute: " + (referenceAttribute == null ? "null" : referenceAttribute) +
				", attributePrimary holder: " + (attributeHolderPrimary == null ? "null" : attributeHolderPrimary) +
				", attributeSecondary holder: " + (attributeHolderSecondary == null ? "null" : attributeHolderSecondary) +
				", referenceAttributePrimary holder: " + (refAttributeHolderPrimary == null ? "null" : attributeHolderSecondary) +
				", referenceAttributeSecondary holder: " + (refAttributeHolderSecondary == null ? "null" : attributeHolderSecondary) +
				", " + message);
		this.attribute = attribute;
		this.referenceAttribute = referenceAttribute;
	}

	/**
	 * Constructor with the attribute, the reference attribute, primary attributeHolder, secondary attributeHolder,
	 * primary attributeHolder of the referenceAttribute, secondary attributeHolder of the referenceAttribute, a message and the Throwable object
	 * @param attribute attribute whose referenceAttribute has illegal value
	 * @param referenceAttribute the attribute whose value is illegal
	 * @param attributeHolderPrimary primary entity holding the attribute
	 * @param attributeHolderSecondary secondary entity holding the attribute
	 * @param refAttributeHolderPrimary primary entity holding the attribute of the referenceAttribute
	 * @param refAttributeHolderSecondary secondary entity holding the attribute of the referenceAttribute
	 * @param message message with details about the cause
	 * @param cause Throwable that caused throwing of this exception
	 */
	public WrongReferenceAttributeValueException(AttributeDefinition attribute, AttributeDefinition referenceAttribute, Object attributeHolderPrimary, Object attributeHolderSecondary, Object refAttributeHolderPrimary, Object refAttributeHolderSecondary, String message, Throwable cause) {
		super("Attribute: " + (attribute == null ? "null" : attribute) +
				", reference attribute: " + (referenceAttribute == null ? "null" : referenceAttribute) +
				", attributePrimary holder: " + (attributeHolderPrimary == null ? "null" : attributeHolderPrimary) +
				", attributeSecondary holder: " + (attributeHolderSecondary == null ? "null" : attributeHolderSecondary) +
				", referenceAttributePrimary holder: " + (refAttributeHolderPrimary == null ? "null" : attributeHolderSecondary) +
				", referenceAttributeSecondary holder: " + (refAttributeHolderSecondary == null ? "null" : attributeHolderSecondary) +
				", " + message, cause);
		this.attribute = attribute;
		this.referenceAttribute = referenceAttribute;
	}

	/**
	 * Getter for the attribute
	 * @return the attribute
	 */
	public AttributeDefinition getAttribute() {
		return attribute;
	}

	/**
	 * Getter for the referenceAttribute
	 * @return the attribute whose value is illegal
	 */
	public AttributeDefinition getReferenceAttribute() {
		return referenceAttribute;
	}

}
