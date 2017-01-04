package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Class for checking logins uniqueness and filling random unique ID.
 * ID is defined as UUID.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_def_login_namespace_fedcloud extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_fedcloud.class);

	/**
	 * Checks if the user's login is unique in the namespace and pass the regular for UUID.
	 *
	 * @param sess PerunSession
	 * @param user User to check attribute for
	 * @param attribute Attribute to check value to
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException
	 * @throws cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException
	 */
	@Override
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException {

		String userLogin = (String) attribute.getValue();
		if (userLogin == null) throw new WrongAttributeValueException(attribute, user, "Value can't be null");

		//Check attribute regex
		sess.getPerunBl().getModulesUtilsBl().checkAttributeRegex(attribute, "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

		// Get all users who have set attribute urn:perun:user:attribute-def:def:login-namespace:[login-namespace], with the value.
		List<User> usersWithSameLogin = sess.getPerunBl().getUsersManagerBl().getUsersByAttribute(sess, attribute);

		usersWithSameLogin.remove(user); //remove self
		if (!usersWithSameLogin.isEmpty()) {
			if(usersWithSameLogin.size() > 1) throw new ConsistencyErrorException("FATAL ERROR: Duplicated Login detected." +  attribute + " " + usersWithSameLogin);
			throw new WrongAttributeValueException(attribute, user, "This login " + attribute.getValue() + " is already occupied.");
		}

		try {
			sess.getPerunBl().getUsersManagerBl().checkReservedLogins(sess, attribute.getFriendlyNameParameter(), userLogin);
		} catch (AlreadyReservedLoginException ex) {
			throw new WrongAttributeValueException(attribute, user, "Login in specific namespace already reserved.", ex);
		}
	}

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

		String login = UUID.randomUUID().toString();
		while(values.contains(login)) {
			login = UUID.randomUUID().toString();
		}

		filledAttribute.setValue(login);
		return filledAttribute;
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
