package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for Resource Member Attributes modules.
 * -----------------------------------------------------------------------------
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 *
 */
public abstract class ResourceMemberAttributesModuleAbstract extends AttributesModuleAbstract implements ResourceMemberAttributesModuleImplApi {

	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, MemberResourceMismatchException {

	}

	public Attribute fillAttribute(PerunSessionImpl session, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		return new Attribute(attribute);
	}

	public void changedAttributeHook(PerunSessionImpl session, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {

	}
}
