package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for Member Group Virtual Attributes modules.
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * author: Oliver Mrazik
 * version: 2015-04-16
 */
public abstract class MemberGroupVirtualAttributesModuleAbstract extends MemberGroupAttributesModuleAbstract implements MemberGroupVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException {
		return new Attribute(attribute);
	}

	@Override
	public boolean setAttributeValue(PerunSessionImpl sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		return false;
	}

	@Override
	public boolean removeAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		return false;
	}

	@Override
	public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) throws InternalErrorException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeAssignmentException {
		return new ArrayList<>();
	}

	@Override
	public List<String> getStrongDependencies() {
		List<String> dependecies = new ArrayList<>();
		return dependecies;
	}
}