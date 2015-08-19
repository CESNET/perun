package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.exceptions.rt.AttributeAlreadyAssignedRuntimeException;

/**
 * Attribute is already assigned to some object. This exception raises when you assign (or add) attribute to some object which had the attribute assigned before.
 *
 * @see cz.metacentrum.perun.core.api.exceptions.rt.AttributeAlreadyAssignedRuntimeException
 * @author Slavek Licehammer
 */
public class AttributeAlreadyAssignedException extends EntityAlreadyAssignedException {
	static final long serialVersionUID = 0;

	private AttributeDefinition attribute;

	public AttributeAlreadyAssignedException(AttributeAlreadyAssignedRuntimeException rt) {
		super(rt.getMessage(),rt);
	}

	public AttributeAlreadyAssignedException(String message) {
		super(message);
	}

	public AttributeAlreadyAssignedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AttributeAlreadyAssignedException(Throwable cause) {
		super(cause);
	}

	public AttributeAlreadyAssignedException(AttributeDefinition attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	public AttributeDefinition getAttribute() {
		return attribute;
	}
}
