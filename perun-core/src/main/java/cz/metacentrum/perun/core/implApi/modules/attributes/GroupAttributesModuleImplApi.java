package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.List;

/**
 * This interface serves as a template for checking facilities attributes.
 *
 * @author Milan Halenar <mhalenar@gmail.com>
 */
public interface GroupAttributesModuleImplApi extends AttributesModuleImplApi{

	/**
	 * Checks if value of this facility attribute is valid.
	 *
	 * @param perunSession perun session
	 * @param group group
	 * @param attribute attribute to check
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongAttributeAssignmentException
	 */

	void checkAttributeValue(PerunSessionImpl perunSession, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * This method MAY fill an attribute at the specified resource.
	 *
	 * @param perunSession perun session
	 * @param group group
	 * @param attribute attribute to fill in
	 * @return
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSessionImpl perunSession, Group group, AttributeDefinition attribute) throws InternalErrorException,WrongAttributeAssignmentException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param session session
	 * @param group the group
	 * @param attribute the attribute
	 */
	void changedAttributeHook(PerunSessionImpl session, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;
}
