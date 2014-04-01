package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.List;

/**
 * Interface for checking and filling in user's attributes.
 *
 * @author Lukáš Pravda <luky.pravda@gmail.com>
 */
public interface UserAttributesModuleImplApi extends AttributesModuleImplApi {

	/**
	 * Checks if assigned attribute to the user is valid.
	 *
	 * @param perunSession PerunSession
	 * @param user User
	 * @param attribute Attribute of the user.
	 *
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException if an referenced attribute against
	 *         the parameter is to be compared is not available
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	void checkAttributeValue(PerunSessionImpl perunSession, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Tries to fill an attribute to the specified user.
	 *
	 * @param perunSession PerunSession
	 * @param user User
	 * @param attribute Attribute in relationship between facility and user to be filled in.
	 * @return Attribute which MAY be filled in
	 *
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param session session
	 * @param user the user
	 * @param attribute the attribute
	 */
	void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;
}
