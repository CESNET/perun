package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.List;

/**
 * This interface serves as a template for checking and fillind in attribute
 * for a member at a specified Resource.
 *
 * @author Lukáš Pravda <luky.pravda@gmail.com>
 */
public interface MemberResourceAttributesModuleImplApi extends AttributesModuleImplApi{

	/**
	 * This method checks Member's attributes at a specified resource.
	 *
	 * @param perunSession Perun session
	 * @param member Member
	 * @param resource Resource
	 * @param attribute Attribute to be checked.
	 *
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException if an referenced attribute against
	 *         the parameter is to be compared is not available
	 * @throws WrongAttributeAssignmentException
	 */
	void checkAttributeValue(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * This method MAY fill Member's attributes at a specified resource.
	 *
	 * @param perunSession Perun session
	 * @param member Member
	 * @param resource Resource
	 * @param attribute Attribute to be filled in
	 *
	 * @return Attribute which MAY be filled in.
	 *
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSessionImpl perunSession, Member member, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *  @param session session
	 * @param member the member
	 * @param resource the resource
	 * @param attribute the attribute
	 */
	void changedAttributeHook(PerunSessionImpl session, Member member, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;
}
