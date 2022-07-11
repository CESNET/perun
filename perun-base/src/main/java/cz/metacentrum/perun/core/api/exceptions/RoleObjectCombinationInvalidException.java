package cz.metacentrum.perun.core.api.exceptions;


import cz.metacentrum.perun.core.api.AttributePolicy;

/**
 * The role + RoleObject combination of AttributePolicy is not supported
 *
 * @author David Flor
 */
public class RoleObjectCombinationInvalidException extends PerunException {
	private final AttributePolicy attributePolicy;

	public RoleObjectCombinationInvalidException(String message, AttributePolicy attributePolicy) {
		super(message + " " + attributePolicy.toString());
		this.attributePolicy = attributePolicy;
	}

	public AttributePolicy getAttributePolicy() {
		return attributePolicy;
	}
}
