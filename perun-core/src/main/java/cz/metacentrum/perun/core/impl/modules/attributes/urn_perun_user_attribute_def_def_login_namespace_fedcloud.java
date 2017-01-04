package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class for checking logins uniqueness and filling random unique ID.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_def_login_namespace_fedcloud extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_fedcloud.class);

	/**
	 * Fill unique (not used) login for user defined as number starting from 1
	 *
	 * @param perunSession PerunSession
	 * @param user User to fill attribute for
	 * @param attribute Attribute to fill value to
	 * @return Filled attribute
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		Attribute filledAttribute = new Attribute(attribute);

		// Get all attributes urn:perun:user:attribute-def:def:login-namespace:[login-namespace], then we can get the new login
		List<Attribute> loginAttributes = perunSession.getPerunBl().getAttributesManagerBl().getAttributesByAttributeDefinition(perunSession, attribute);
		Set<String> values = new HashSet<>();
		for (Attribute loginAttribute: loginAttributes) {
			values.add((String) loginAttribute.getValue());
		}

		int iterator = 0;
		while (iterator < Integer.MAX_VALUE) {
			iterator++;
			String login = String.valueOf(iterator);

			if(values.contains(login)) {
				//already used login
				continue;
			} else {
				//this one is free
				filledAttribute.setValue(login);
				return filledAttribute;
			}
		}

		//we can't find any suitable login for fedcloud (all are already used or there is not allowed to use pure number format in common login module anymore), return empty value instead
		return super.fillAttribute(perunSession, user, attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:fedcloud");
		attr.setDisplayName("Login in namespace: fedcloud");
		attr.setType(String.class.getName());
		attr.setDescription("Logname in namespace 'fedcloud'.");
		return attr;
	}

}
