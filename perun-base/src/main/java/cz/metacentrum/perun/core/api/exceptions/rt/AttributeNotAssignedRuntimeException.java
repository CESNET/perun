package cz.metacentrum.perun.core.api.exceptions.rt;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.exceptions.EntityNotAssignedException;

public class AttributeNotAssignedRuntimeException extends EntityNotAssignedException {
	static final long serialVersionUID = 0;

	private AttributeDefinition attribute;


	public AttributeNotAssignedRuntimeException(AttributeDefinition attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	public AttributeNotAssignedRuntimeException(Throwable cause) {
		super(cause);
	}

	public AttributeNotAssignedRuntimeException(Throwable cause, AttributeDefinition attribute) {
		super(attribute.toString(), cause);

		this.attribute = attribute;
	}

	public AttributeDefinition getUserId() {
		return attribute;
	}
}
