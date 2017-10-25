package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Interface for checking and filling in host's attributes.
 *
 * @author Peter Balcirak <peter.balcirak@gmail.com>
 */
public interface HostAttributesModuleImplApi extends AttributesModuleImplApi {
	/**
	 * Checks if assigned attribute to the member is valid.
	 *
	 * @param session Perun session
	 * @param host Host
	 * @param attribute Attribute of the member.
	 *
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value is wrong/illegal
	 * @throws WrongReferenceAttributeValueException if an referenced attribute against
	 *         the parameter is to be compared is not available
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	void checkAttributeValue(PerunSessionImpl session, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

	/**
	 * Tries to fill an attribute to the specified host.
	 *
	 * @param session Perun Session
	 * @param host Host
	 * @param attribute Attribute of the member
	 * @return Attribute which MAY be filled in
	 *
	 * @throws InternalErrorException if an exception is raised in particular
	 *         implementation, the exception is wrapped in InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	Attribute fillAttribute(PerunSessionImpl session, Host host, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * If you need to do some further work with other modules, this method do that
	 *
	 * @param session session
	 * @param host Host
	 * @param attribute the attribute
	 */
	void changedAttributeHook(PerunSessionImpl session, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

}
