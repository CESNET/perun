package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for virtual attributes.
 *
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public interface FacilityVirtualAttributesModuleImplApi extends FacilityAttributesModuleImplApi, VirtualAttributesModuleImplApi {

	/**
	 * This method will return computed value.
	 *
	 * @param perunSession perun session
	 * @param facility facility which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute getAttributeValue(PerunSessionImpl perunSession, Facility facility, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Method sets attributes' values which are dependent on this virtual attribute.
	 *
	 * @param perunSession
	 * @param facility facility which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	boolean setAttributeValue(PerunSessionImpl perunSession, Facility facility, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException;

	/**
	 * Currently do nothing.
	 *
	 * @param perunSession
	 * @param facility facility which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	void removeAttributeValue(PerunSessionImpl perunSession, Facility facility, AttributeDefinition attribute) throws InternalErrorException;
}
