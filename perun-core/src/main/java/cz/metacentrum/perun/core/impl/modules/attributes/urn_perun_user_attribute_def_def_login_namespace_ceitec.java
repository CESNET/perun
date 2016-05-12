package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Class for checking logins uniqueness in the namespace and filling ceitec id.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_ceitec extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_ceitec.class);

	/**
	 * Checks if the user's login is unique in the namespace organization.
	 * Check if maximum length is 20 chars, because of MSAD limitations.
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

		// check uniqueness
		super.checkAttributeValue(sess, user, attribute);

		// plus check, that login is max 20 chars.
		if (attribute.getValue() != null) {
			if (((String)attribute.getValue()).length() > 20) throw new WrongAttributeValueException(attribute, user, "Login '" + attribute.getValue() + "' exceeds 20 chars limit.");
		}

	}

	/**
	 * Filling implemented for:
	 * - namespaces configured in /etc/perun/perun.properties as property: "perun.loginNamespace.generated"
	 *
	 * Resulting format/rules:
	 * - "firstName.lastName[number]" where number is opt and start with 1 when same login is already present.
	 * - Only first part of "firstName" and last part of "lastName" is taken.
	 * - All accented chars are unaccented and all non (a-z,A-Z) chars are removed from name and value is lowered.
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

		if (generatedNamespaces.contains(attribute.getFriendlyNameParameter())) {

			String login = generateLoginValue(user);
			if (login == null) return filledAttribute;

			// fill value
			int iterator = 0;
			while (iterator >= 0) {
				if (iterator > 0) {
					int iteratorLength = String.valueOf(iterator).length();
					if (login.length() + iteratorLength > 20) {
						// if login+iterator > 20 => crop login & reset iterator
						login = login.substring(0, login.length()-1);
						iterator = 0;
						filledAttribute.setValue(login);
					} else {
						filledAttribute.setValue(login + iterator);
					}

				} else {
					filledAttribute.setValue(login);
				}
				try {
					checkAttributeValue(perunSession, user, filledAttribute);
					return filledAttribute;
				} catch (WrongAttributeValueException ex) {
					// continue in a WHILE cycle
					iterator++;
				}
			}

			return filledAttribute;

		} else {
			// without value
			return filledAttribute;
		}
	}

	/**
	 * Generate login in login-namespace by using defined rules/format: "firstName.lastName"
	 * Only first part of "firstName" and last part of "lastName" is taken.
	 * All accented chars are unaccented and all non (a-z,A-Z) chars are removed from name and value is lowered.
	 *
	 * @param user User to generate login for
	 * @return Base part of users login in login-namespace
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	private String generateLoginValue(User user) throws InternalErrorException, WrongAttributeAssignmentException {

		String firstName = user.getFirstName();
		String lastName = user.getLastName();

		// get only first part of first name and remove spec. chars
		if (firstName != null && !firstName.isEmpty()) {
			firstName = ModulesUtilsBlImpl.normalizeStringForLogin(firstName.split(" ")[0]);
		}

		// get only last part of last name and remove spec. chars
		if (lastName != null && !lastName.isEmpty()) {
			List<String> names = Arrays.asList(lastName.split(" "));
			lastName = names.get(names.size() - 1);
			firstName = ModulesUtilsBlImpl.normalizeStringForLogin(lastName.split(" ")[0]);
		}

		// unable to fill login for users without name or with partial name
		if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
			return null;
		}

		// LOGIN must be < 20 chars, because of MS ActiveDirectory limitations
		String login = firstName+ "." + lastName;
		if (login.length()>20) {
			login = login.substring(0, 20);
		}

		return login;

	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:ceitec");
		attr.setDisplayName("Login in namespace: ceitec");
		attr.setType(String.class.getName());
		attr.setDescription("Logname in namespace 'ceitec'.");
		return attr;
	}

}
