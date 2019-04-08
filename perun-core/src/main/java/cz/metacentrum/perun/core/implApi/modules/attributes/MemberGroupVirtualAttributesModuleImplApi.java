package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for checking and filling in virtual attribute
 * for a member in a specified group.
 *
 * author: Oliver Mrazik
 * version: 2015-04-16
 */
public interface MemberGroupVirtualAttributesModuleImplApi extends MemberGroupAttributesModuleImplApi, VirtualAttributesModuleImplApi {

	/**
	 * This method will return computed value.
	 *
	 * @param sess perun session
	 * @param member member which is needed for computing the value
	 * @param group group which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return attribute value
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute getAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Method sets attributes' values which are dependent on this virtual attribute.
	 *
	 * @param sess perun session
	 * @param member member which is needed for computing the value
	 * @param group group which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return true if attribute was really changed
	 */
	boolean setAttributeValue(PerunSessionImpl sess, Member member, Group group, Attribute attribute);

	/**
	 * Method remove attributes' value which are dependent on this virtual attribute.
	 *
	 * @param sess perun session
	 * @param member member which is needed for computing the value
	 * @param group group which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 */
	boolean removeAttributeValue(PerunSessionImpl sess, Member member, Group group, AttributeDefinition attribute);
}
