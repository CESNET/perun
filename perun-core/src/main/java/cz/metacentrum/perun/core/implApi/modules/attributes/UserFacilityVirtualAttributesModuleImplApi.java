package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for virtual attributes.
 *
 * @author Michal Prochazka <michalp@ics.muni.cz>
 */
public interface UserFacilityVirtualAttributesModuleImplApi extends UserFacilityAttributesModuleImplApi, VirtualAttributesModuleImplApi{

	/**
	 * This method will return computed value.
	 *
	 * @param perunSession perun session
	 * @param user user which is needed for computing the value
	 * @param facility facility which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute getAttributeValue(PerunSessionImpl perunSession, User user, Facility facility, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Method sets attributes' values which are dependent on this virtual attribute.
	 *
	 * @param perunSession
	 * @param user user which is needed for computing the value
	 * @param facility facility which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	boolean setAttributeValue(PerunSessionImpl perunSession, User user, Facility facility, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException;

	/**
	 * Currently do nothing.
	 *
	 * @param perunSession
	 * @param user user which is needed for computing the value
	 * @param facility facility which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 */
	boolean removeAttributeValue(PerunSessionImpl perunSession, User user, Facility facility, AttributeDefinition attribute);
}
