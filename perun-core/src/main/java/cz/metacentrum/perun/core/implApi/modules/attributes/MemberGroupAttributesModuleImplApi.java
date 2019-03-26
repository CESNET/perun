package cz.metacentrum.perun.core.implApi.modules.attributes;


import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for checking and filling in attribute
 * for a member in a specified group.
 *
 * author: Oliver Mrázik
 * version: 2015-03-22
 */
public interface MemberGroupAttributesModuleImplApi extends AttributesModuleImplApi {
	/**
	 * This method checks Member's attributes in a specified group.
	 *
	 * @param perunSession Perun session
	 * @param member Member
	 * @param group Group
	 * @param attribute Attribute to be checked.
	 *
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 */
	void checkAttributeValue(PerunSessionImpl perunSession, Member member, Group group, Attribute attribute) throws WrongAttributeValueException;

	/**
	 * This method MAY fill Member's attributes in a specified group.
	 *
	 * @param perunSession Perun session
	 * @param member Member
	 * @param group Group
	 * @param attribute Attribute to be filled in
	 *
	 * @return Attribute which MAY be filled in.
	 *
	 */
	Attribute fillAttribute(PerunSessionImpl perunSession, Member member, Group group, AttributeDefinition attribute);

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param session session
	 * @param member Member
	 * @param group Group
	 * @param attribute the attribute
	 */
	void changedAttributeHook(PerunSessionImpl session, Member member, Group group, Attribute attribute) throws InternalErrorException;
}