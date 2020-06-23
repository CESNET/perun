package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Abstract class for Member Group Attributes modules.
 * -----------------------------------------------------------------------------
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * author: Oliver Mrázik
 * version: 2015-03-22
 */
public abstract class MemberGroupAttributesModuleAbstract extends AttributesModuleAbstract implements MemberGroupAttributesModuleImplApi {

	public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Group group, Attribute attribute) throws WrongAttributeValueException {

	}

	public void checkAttributeSemantics(PerunSessionImpl perunSession, Member member, Group group, Attribute attribute) {

	}

	public Attribute fillAttribute(PerunSessionImpl session, Member member, Group group, AttributeDefinition attribute) {
		return new Attribute(attribute);
	}

	public void changedAttributeHook(PerunSessionImpl session, Member member, Group group, Attribute attribute) {

	}
}
