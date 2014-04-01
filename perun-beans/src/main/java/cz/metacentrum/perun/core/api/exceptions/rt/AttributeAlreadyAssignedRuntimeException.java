package cz.metacentrum.perun.core.api.exceptions.rt;

import cz.metacentrum.perun.core.api.AttributeDefinition;

public class AttributeAlreadyAssignedRuntimeException extends PerunRuntimeException {
	static final long serialVersionUID = 0;

	private AttributeDefinition attribute;

	public AttributeAlreadyAssignedRuntimeException() {
		super();
	}

	public AttributeAlreadyAssignedRuntimeException(AttributeDefinition attribute) {
		super(attribute.toString());
		this.attribute = attribute;
	}

	public AttributeAlreadyAssignedRuntimeException(Throwable cause) {
		super(cause);
	}

	public AttributeAlreadyAssignedRuntimeException(Throwable cause, AttributeDefinition attribute) {
		super(attribute.toString(), cause);

		this.attribute = attribute;
	}

	public AttributeDefinition getUserId() {
		return attribute;
	}
}
