package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for virtual attributes.
 *
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public interface GroupResourceVirtualAttributesModuleImplApi extends GroupResourceAttributesModuleImplApi, VirtualAttributesModuleImplApi {

	/**
	 * This method will return computed value.
	 *
	 * @param sess perun session
	 * @param group group which is needed for computing the value
	 * @param resource resource which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	Attribute getAttributeValue(PerunSessionImpl sess, Group group, Resource resource, AttributeDefinition attribute) throws InternalErrorException;

	/**
	 * Method sets attributes' values which are dependent on this virtual attribute.
	 *
	 * @param sess
	 * @param group group which is needed for computing the value
	 * @param resource resource which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return true if attribute was really changed
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 */
	boolean setAttributeValue(PerunSessionImpl sess, Group group, Resource resource, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException;

	/**
	 * Currently do nothing.
	 *
	 * @param sess
	 * @param group group which is needed for computing the value
	 * @param resource resource which is needed for computing the value
	 * @param attribute attribute to operate on
	 * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first place
	 */
	boolean removeAttributeValue(PerunSessionImpl sess, Group group, Resource resource, AttributeDefinition attribute);
}
