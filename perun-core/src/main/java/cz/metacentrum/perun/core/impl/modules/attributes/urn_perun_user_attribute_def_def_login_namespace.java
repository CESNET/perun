package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Class for checking login uniqueness in the namespace and filling login value in namespace
 *
 * @author Michal Prochazka  &lt;michalp@ics.muni.cz&gt;
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 *
 * @date 3.6.2011 11:02:22
 */
public class urn_perun_user_attribute_def_def_login_namespace extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace.class);

	protected final static List<String> generatedNamespaces = BeansUtils.getCoreConfig().getGeneratedLoginNamespaces();

	/**
	 * Checks if the user's login is in the correct format and if it is permitted to use
	 *
	 * @param sess PerunSession
	 * @param user user
	 * @param attribute Attribute of the user
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException if the attribute value has wrong/illegal syntax
	 */
	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException {
		if (attribute.getValue() == null) return;

		//Check attribute regex
		sess.getPerunBl().getModulesUtilsBl().checkAttributeRegex(attribute, "^[a-zA-Z0-9_][-A-z0-9_.@/]*$");
		//Check if user login is permitted or not permitted
		sess.getPerunBl().getModulesUtilsBl().checkUnpermittedUserLogins(attribute);
	}

	/**
	 * Checks if the user's login is unique in the namespace organization
	 *
	 * @param sess PerunSession
	 * @param user User to check attribute for
	 * @param attribute Attribute to check value to
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException
	 * @throws cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException
	 */
	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String userLogin = attribute.valueAsString();
		if (userLogin == null) throw new WrongReferenceAttributeValueException(attribute, null, user, null, "Value can't be null");

		// Get all users who have set attribute urn:perun:member:attribute-def:def:login-namespace:[login-namespace], with the value.
		List<User> usersWithSameLogin = sess.getPerunBl().getUsersManagerBl().getUsersByAttribute(sess, attribute);

		usersWithSameLogin.remove(user); //remove self
		if (!usersWithSameLogin.isEmpty()) {
			if(usersWithSameLogin.size() > 1) throw new ConsistencyErrorException("FATAL ERROR: Duplicated Login detected." +  attribute + " " + usersWithSameLogin);
			throw new WrongReferenceAttributeValueException(attribute, attribute, user, null, usersWithSameLogin.get(0), null, "This login " + attribute.getValue() + " is already occupied.");
		}

		try {
			sess.getPerunBl().getUsersManagerBl().checkReservedLogins(sess, attribute.getFriendlyNameParameter(), userLogin);
		} catch (AlreadyReservedLoginException ex) {
			throw new WrongReferenceAttributeValueException(attribute, null, user, null, null, null, "Login in specific namespace already reserved.", ex);
		}
	}

	/**
	 * Filling implemented for:
	 * - namespaces configured in /etc/perun/perun.properties as property: "perun.loginNamespace.generated"
	 * - You must create own attribute module for that namespace to define filling function
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
		return filledAttribute;

	}

	/*public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace");
		attr.setType(String.class.getName());
		attr.setDescription("Login namespace.");
		return attr;
	}*/

	/**
	 * Generate unique ID as SHA1 hash from users ID and domain. Used to generate unique IDs for ProxyIdP login namespaces.
	 *
	 * @param user User to generate ID for
	 * @param domain Login namespace domain, eg. @einfra.cesnet.cz or @bbmri.eu etc.
	 * @return Builder to get string ID
	 * @throws InternalErrorException When generation fails
	 */
	protected StringBuilder sha1HashCount(User user, String domain) throws InternalErrorException {
		try {
			String salt = BeansUtils.getCoreConfig().getInstanceId();
			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			// counts sha1hash and converts output to hex
			int length = 4+salt.getBytes(StandardCharsets.UTF_8).length+domain.getBytes(StandardCharsets.UTF_8).length;
			byte[] result = mDigest.digest(ByteBuffer.allocate(length).putInt(user.getId()).put(domain.getBytes(StandardCharsets.UTF_8)).put(salt.getBytes(StandardCharsets.UTF_8)).array());
			StringBuilder sb = new StringBuilder();
			for (byte b : result) {
				sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
			}

			return sb;
		} catch (NoSuchAlgorithmException ex) {
			throw new InternalErrorException("Algorithm for sha1hash was not found.", ex);
		}
	}

}
