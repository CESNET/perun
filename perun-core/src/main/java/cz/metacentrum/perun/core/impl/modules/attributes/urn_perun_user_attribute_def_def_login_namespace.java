package cz.metacentrum.perun.core.impl.modules.attributes;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

/**
 * Class for checking logins uniqueness in the namespace
 *
 * @author Michal Prochazka  &lt;michalp@ics.muni.cz&gt;
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 * @date 3.6.2011 11:02:22
 */
public class urn_perun_user_attribute_def_def_login_namespace extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace.class);

	/**
	 * Checks if the user's login is unique in the namespace
	 * organization
	 */
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException {

		String userLogin = (String) attribute.getValue();
		if (userLogin == null) throw new WrongAttributeValueException(attribute, user, "Value can't be null");
		if(!userLogin.matches("^[a-zA-Z0-9][-A-z0-9_.@/]*$")) throw new WrongAttributeValueException(attribute, user, "Wrong format. ^[A-z0-9][-A-z0-9_.@/]*$ expected.");

		//Check if user login is permitted or not permitted
		sess.getPerunBl().getModulesUtilsBl().checkUnpermittedUserLogins(attribute);

		// Get all users who have set attribute urn:perun:member:attribute-def:def:login-namespace:[login-namespace], with the value.
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
	 * Filling implemented only for namespaces configured in /etc/perun/perun.properties
	 * as property: "perun.loginNamespace.generated"
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {

		Attribute filledAttribute = new Attribute(attribute);

		// load namespaces to generate login for
		List<String> namespaces = new ArrayList<String>();
		try {
			String nmspc = BeansUtils.getPropertyFromConfiguration("perun.loginNamespace.generated");
			namespaces = Arrays.asList(nmspc.split(","));
			Iterator<String> nameIter = namespaces.listIterator();
			// trim and remove empty namespaces
			while (nameIter.hasNext()) {
				String namespace = nameIter.next();
				namespace = namespace.trim();
				if (namespace.isEmpty()) nameIter.remove();
			}
		} catch (InternalErrorException ex) {
			// without value
			return filledAttribute;
		}

		if (namespaces.contains(attribute.getFriendlyNameParameter())) {
			// with value
			return generateLoginValue(perunSession, user, filledAttribute);
		} else {
			// without value
			return filledAttribute;
		}

	}

	/**
	 *  Fill login-namespace attribute with generated value.
	 * 	Format is: "firstName.lastName[number]" where number is opt and start with 1 when same login is already present.
	 * 	All accented chars are unaccented and all non (a-z,A-Z) chars are removed from name and value is lowercased.
	 *
	 * @param sess PerunSession
	 * @param user User to fill attribute for
	 * @param attribute Attribute to fill value with
	 * @return Filled attribute
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	private Attribute generateLoginValue(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {

		String firstName = user.getFirstName();
		String lastName = user.getLastName();

		// get only first part of first name and remove spec. chars
		if (firstName != null && !firstName.isEmpty()) {
			firstName = firstName.split(" ")[0];
			firstName = firstName.toLowerCase();
			firstName = java.text.Normalizer.normalize(firstName, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
			firstName = firstName.replaceAll("[^a-zA-Z]+", "");
		}

		// get only last part of last name and remove spec. chars
		if (lastName != null && !lastName.isEmpty()) {
			List<String> names = Arrays.asList(lastName.split(" "));
			lastName = names.get(names.size() - 1);
			lastName = lastName.toLowerCase();
			lastName = java.text.Normalizer.normalize(lastName, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			lastName = lastName.replaceAll("[^a-zA-Z]+", "");
		}

		// unable to fill login for users without name or with partial name
		if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
			return attribute;
		}

		// fill value
		int iterator = 0;
		while (iterator >= 0) {
			if (iterator > 0) {
				attribute.setValue(firstName+ "." + lastName + iterator);
			} else {
				attribute.setValue(firstName + "." + lastName);
			}
			try {
				checkAttributeValue(sess, user, attribute);
				return attribute;
			} catch (WrongAttributeValueException ex) {
				// continue in a WHILE cycle
				iterator++;
			}
		}

		return attribute;

	}

	/*public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace");
		attr.setType(String.class.getName());
		attr.setDescription("Login namespace.");
		return attr;
	}*/

}
