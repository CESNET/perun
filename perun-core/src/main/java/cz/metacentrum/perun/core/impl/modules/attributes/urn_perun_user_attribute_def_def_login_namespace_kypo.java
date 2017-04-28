package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class for checking logins uniqueness in the namespace and filling login value.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_kypo extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_kypo.class);
	private final String domainName = "@kypo";

	/**
	 * Filling implemented for login-namespace:kypo attribute
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
		if (filledAttribute.getValue() == null) {
			return generateLoginValue(perunSession, user, filledAttribute);
		} else {
			return filledAttribute;
		}
	}

	/**
	 * generateLoginValue() fills login-namespace:kypo attribute with generated value.
	 * Format is: "[hash]@kypo" where [hash] represents sha1hash counted from user's id, domain and salt.
	 *
	 * @param sess PerunSession
	 * @param user User to fill attribute for
	 * @param attribute Attribute to fill value with
	 * @return Filled attribute
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	private Attribute generateLoginValue(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		try {
			attribute.setValue(sha1HashCount(user).toString() + this.domainName);
			checkAttributeValue(sess, user, attribute);
			return attribute;
		} catch (WrongAttributeValueException ex) {
			return attribute;
		}
	}

	/**
	 * Sha1HashCount() counts sha1hash for kypo namespace from user's id, domain and salt
	 *
	 * @param user user with the id
	 * @return counted hash
	 */
	private StringBuilder sha1HashCount(User user) throws InternalErrorException {
		try {
			String salt = BeansUtils.getCoreConfig().getIdSalt();
			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			// counts sha1hash and converts output to hex
			byte[] result = new byte[0];
			try {
				int length = 4+salt.getBytes("UTF-8").length+domainName.getBytes("UTF-8").length;
				result = mDigest.digest(ByteBuffer.allocate(length).putInt(user.getId()).put(domainName.getBytes("UTF-8")).put(salt.getBytes("UTF-8")).array());
			} catch (UnsupportedEncodingException e) {
				log.error("Unable to get UTF-8 bytes from domainName and perun.id.salt.", e);
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < result.length; i++) {
				sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
			}

			return sb;
		} catch (NoSuchAlgorithmException ex) {
			throw new InternalErrorException("Algorithm for sha1hash was not found.", ex);
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:kypo");
		attr.setDisplayName("KYPO login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to KYPO.");
		return attr;
	}

}
