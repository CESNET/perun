package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.exceptions.rt.WrongAttributeAssignmentRuntimeException;

/**
 * Thrown while assigning atribute to wrong entity. For example if you try to set value for the facility to atrribut whis is only for resources.
 *
 * @author Slavek Licehammer
 */
public class WrongAttributeAssignmentException extends PerunException {
	static final long serialVersionUID = 0;

	private AttributeDefinition attribute;

	public WrongAttributeAssignmentException(String message) {
		super(message);
	}

	public WrongAttributeAssignmentException(String message, Throwable cause) {
		super(message, cause);
	}

	public WrongAttributeAssignmentException(Throwable cause) {
		super(cause);
	}

	public WrongAttributeAssignmentException(AttributeDefinition attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	public AttributeDefinition getAttribute() {
		return attribute;
	}

	public WrongAttributeAssignmentException(WrongAttributeAssignmentRuntimeException rt) {
		super(rt.getMessage(),rt);
	}


}
