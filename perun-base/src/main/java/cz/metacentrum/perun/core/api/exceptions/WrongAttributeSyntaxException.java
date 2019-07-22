package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Attribute;

/**
 * Attribute's value has incorrect syntax.
 *
 * @author Metodej Klang
 */
public class WrongAttributeSyntaxException extends WrongAttributeValueException {
	public WrongAttributeSyntaxException(String message) {
		super(message);
	}

	public WrongAttributeSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrongAttributeSyntaxException(Throwable cause) {
		super(cause);
	}

	public WrongAttributeSyntaxException(Attribute attribute) {
		super(attribute);
	}

	public WrongAttributeSyntaxException(Attribute attribute, Object attributeHolder, Object attributeHolderSecondary, String message) {
		super(attribute, attributeHolder, attributeHolderSecondary, message);
	}

	public WrongAttributeSyntaxException(Attribute attribute, Object attributeHolder, Object attributeHolderSecondary) {
		super(attribute, attributeHolder, attributeHolderSecondary);
	}

	public WrongAttributeSyntaxException(Attribute attribute, Object attributeHolder, String message) {
		super(attribute, attributeHolder, message);
	}

	public WrongAttributeSyntaxException(Attribute attribute, Object attributeHolder, String message, Throwable cause) {
		super(attribute, attributeHolder, message, cause);
	}

	public WrongAttributeSyntaxException(Attribute attribute, Throwable cause) {
		super(attribute, cause);
	}

	public WrongAttributeSyntaxException(Attribute attribute, String message) {
		super(attribute, message);
	}

	public WrongAttributeSyntaxException(Attribute attribute, String message, Throwable cause) {
		super(attribute, message, cause);
	}
}
