package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for virtual attributes.
 *
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface MemberVirtualAttributesModuleImplApi extends MemberAttributesModuleImplApi, VirtualAttributesModuleImplApi {

	/**
	 * This method will return computed value.
	 *
	 * @param perunSession perun session
	 * @param member member which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute getAttributeValue(PerunSessionImpl perunSession, Member member, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Method sets attributes' values which are dependent on this virtual attribute.
	 *
	 * @param perunSession
	 * @param member member which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return
	 */
	boolean setAttributeValue(PerunSessionImpl perunSession, Member member, Attribute attribute);

	/**
	 * Currently do nothing.
	 *
	 * @param perunSession
	 * @param member member which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAttributeValue(PerunSessionImpl perunSession, Member member, AttributeDefinition attribute) throws InternalErrorException;
}
