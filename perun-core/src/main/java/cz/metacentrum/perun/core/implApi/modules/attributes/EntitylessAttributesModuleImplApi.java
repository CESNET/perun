package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * This interface serves as a template for checking entityless attributes.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public interface EntitylessAttributesModuleImplApi extends AttributesModuleImplApi {

	/**
	 * Checks if value of this facility attribute is valid.
	 *
	 * @param perunSession perun session
	 * @param key string for which you want to check validity of attribute
	 * @param attribute attribute to check
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException if an referenced attribute against
	 *         the parameter is to be compared is not available
	 * @throws WrongAttributeAssignmentException
	 */

	void checkAttributeValue(PerunSessionImpl perunSession, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

	/**
	 * This method MAY fill an attribute at the specified resource.
	 *
	 * @param perunSession perun session
	 * @param key string for which you want to check validity of attribute
	 * @param attribute attribute to fill in
	 * @return
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSessionImpl perunSession, String key, AttributeDefinition attribute) throws InternalErrorException,WrongAttributeAssignmentException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param session session
	 * @param key the key for entityless attribute
	 * @param attribute the attribute
	 */
	void changedAttributeHook(PerunSessionImpl session, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;
}
