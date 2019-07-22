package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Attribute;

/**
 * Attribute's value has incorrect semantics.
 *
 * @author Metodej Klang
 */
public class WrongAttributeSemanticsException extends WrongAttributeValueException {
	public WrongAttributeSemanticsException(String message) {
		super(message);
	}

	public WrongAttributeSemanticsException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrongAttributeSemanticsException(Throwable cause) {
		super(cause);
	}

	public WrongAttributeSemanticsException(Attribute attribute) {
		super(attribute);
	}

	public WrongAttributeSemanticsException(Attribute attribute, Object attributeHolder, Object attributeHolderSecondary, String message) {
		super(attribute, attributeHolder, attributeHolderSecondary, message);
	}

	public WrongAttributeSemanticsException(Attribute attribute, Object attributeHolder, Object attributeHolderSecondary) {
		super(attribute, attributeHolder, attributeHolderSecondary);
	}

	public WrongAttributeSemanticsException(Attribute attribute, Object attributeHolder, String message) {
		super(attribute, attributeHolder, message);
	}

	public WrongAttributeSemanticsException(Attribute attribute, Object attributeHolder, String message, Throwable cause) {
		super(attribute, attributeHolder, message, cause);
	}

	public WrongAttributeSemanticsException(Attribute attribute, Throwable cause) {
		super(attribute, cause);
	}

	public WrongAttributeSemanticsException(Attribute attribute, String message) {
		super(attribute, message);
	}

	public WrongAttributeSemanticsException(Attribute attribute, String message, Throwable cause) {
		super(attribute, message, cause);
	}
}
