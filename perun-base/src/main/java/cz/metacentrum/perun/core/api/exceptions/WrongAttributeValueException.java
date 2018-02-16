package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.rt.WrongAttributeValueRuntimeException;

/**
 * Attribute has wrong/illegal value.
 *
 * @author Slavek Licehammer
 */
public class WrongAttributeValueException extends AttributeValueException {

	static final long serialVersionUID = 0;
	private Attribute attribute;
	private Object attributeHolder;
	private Object attributeHolderSecondary;

	public WrongAttributeValueException(String message) {
		super(message);
	}

	public WrongAttributeValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrongAttributeValueException(Throwable cause) {
		super(cause);
	}

	public WrongAttributeValueException(Attribute attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	public WrongAttributeValueException(Attribute attribute, Object attributeHolder, Object attributeHolderSecondary, String message) {
		super(attribute.toString() + " Set for: " + attributeHolder + " and " + attributeHolderSecondary + " - " + message);
		this.attribute = attribute;
		this.attributeHolder = attributeHolder;
		this.attributeHolderSecondary = attributeHolderSecondary;
	}

	public WrongAttributeValueException(Attribute attribute, Object attributeHolder, Object attributeHolderSecondary) {
		super(attribute.toString() + " Set for: " + attributeHolder + " and " + attributeHolderSecondary);
		this.attribute = attribute;
		this.attributeHolder = attributeHolder;
		this.attributeHolderSecondary = attributeHolderSecondary;
	}

	public WrongAttributeValueException(Attribute attribute, Object attributeHolder, String message){
		super(attribute.toString() + " Set for: " + attributeHolder + " - " + message);
		this.attribute = attribute;
		this.attributeHolder = attributeHolder;
	}

	public WrongAttributeValueException(Attribute attribute, Object attributeHolder, String message, Throwable cause){
		super(attribute.toString() + " Set for: " + attributeHolder + " - " + message, cause);
		this.attribute = attribute;
		this.attributeHolder = attributeHolder;
	}

	public WrongAttributeValueException(Attribute attribute, Throwable cause) {
		super(attribute.toString(), cause);
		this.attribute = attribute;
	}

	public WrongAttributeValueException(Attribute attribute, String message) {
		super(attribute.toString() + " " + message);
		this.attribute = attribute;

	}

	public WrongAttributeValueException(Attribute attribute, String message, Throwable cause) {
		super(attribute.toString() + " " + message, cause);
		this.attribute = attribute;

	}

	public Attribute getAttribute() {
		return attribute;
	}

	public Object getAttributeHolder() {
		return attributeHolder;
	}

	public Object getAttributeHolderSecondary() {
		return attributeHolderSecondary;
	}

	public WrongAttributeValueException(WrongAttributeValueRuntimeException rt) {
		super(rt.getMessage(), rt);
	}
}
