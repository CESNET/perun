package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.List;

/**
 * This interface serves as a template for checking and filling in resource's
 * attributes.
 *
 * @author Lukáš Pravda <luky.pravda@gmail.com>
 */
public interface ResourceAttributesModuleImplApi extends AttributesModuleImplApi {

	/**
	 * This method tries to fill a value of the resource attribute. Value may be copied from some facility attribute.
	 *
	 * @param perunSession perun session
	 * @param resource resource, attribute of which you want to fill
	 * @param attribute attribute to fill. If attributes already have set value, this value won't be overwriten. This means the attribute value must be empty otherwise this method won't fill it.
	 * @return attribute which MAY have filled value
	 *
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute) throws InternalErrorException,WrongAttributeAssignmentException;

	/**
	 * Checks if value of this resource attribute is valid.
	 *
	 * @param perunSession perun session
	 * @param resource resource for which you want to check validity of attribute
	 * @param attribute attribute to check
	 *
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong / illegal
	 * @throws WrongReferenceAttributeValueException if an referenced attribute against
	 *         the parameter one is to be compared is not available
	 * @throws WrongAttributeAssignmentException
	 */
	void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param session session
	 * @param resource resource
	 * @param attribute the attribute
	 */
	void changedAttributeHook(PerunSessionImpl session, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;
}
