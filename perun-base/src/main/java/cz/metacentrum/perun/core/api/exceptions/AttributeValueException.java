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

	public AttributeValueException(String message) {
		super(message);
	}

	public AttributeValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public AttributeValueException(Throwable cause) {
		super(cause);
	}

	public AttributeValueException(Attribute attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	public AttributeValueException(Attribute attribute, Throwable cause) {
		super(attribute.toString(), cause);
		this.attribute = attribute;
	}

	public AttributeValueException(Attribute attribute, String message) {
		super(message + " " + attribute.toString());
		this.attribute = attribute;

	}

	public AttributeValueException(Attribute attribute, String message, Throwable cause) {
		super(message + " " + attribute.toString(), cause);
		this.attribute = attribute;

	}

	public AttributeDefinition getAttribute() {
		return attribute;
	}

}
