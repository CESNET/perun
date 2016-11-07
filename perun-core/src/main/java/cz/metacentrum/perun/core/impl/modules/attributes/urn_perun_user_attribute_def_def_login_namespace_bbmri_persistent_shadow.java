package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class for checking logins uniqueness in the namespace and filling bbmri-persistent id.
 * It is only storage! Use module login bbmri_persistent for access the value.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 * @author Jakub Hruska <jhruska@mail.muni.cz>
 *
 * @date 07.11.2016
 */
public class urn_perun_user_attribute_def_def_login_namespace_bbmri_persistent_shadow extends urn_perun_user_attribute_def_def_login_namespace {

        private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_bbmri_persistent_shadow.class);
        private final String extSourceNameBbmri = "https://login.bbmri-eric.eu/idp/";
        private final String domainNameBbmri = "@bbmri.eu";
        private final String attrNameBbmri = "login-namespace:bbmri-persistent-shadow";

        /**
	 * Filling implemented for login:namespace:bbmri-persistent attribute
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

		if (attribute.getFriendlyName().equals(this.attrNameBbmri)) {
			return generateLoginValueBbmri(perunSession, user, filledAttribute);
		} else {
			// without value
			return filledAttribute;
		}
	}

        /**
	 * GenerateLoginValueBbmri() fills login-namespace:bbmri-persistent attribute with generated value.
	 * 	Format is: "[hash]@bbmri-europe.org" where [hash] represents sha1hash counted from user's id.
	 *
	 * @param sess PerunSession
	 * @param user User to fill attribute for
	 * @param attribute Attribute to fill value with
	 * @return Filled attribute
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	private Attribute generateLoginValueBbmri(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		try {
			attribute.setValue(sha1HashCount(user).toString() + this.domainNameBbmri);
			checkAttributeValue(sess, user, attribute);
			return attribute;
		} catch (WrongAttributeValueException ex) {
			return attribute;
		}
	}

        /**
	 * Sha1HashCount() counts sha1hash for bbmri-persistent namespace from user's id
	 *
	 * @param user user with the id
	 * @return counted hash
	 */
	private StringBuilder sha1HashCount(User user) throws InternalErrorException {
		try {
			MessageDigest mDigest = MessageDigest.getInstance("SHA1");
			// counts sha1hash and converts output to hex
			byte[] result = mDigest.digest(ByteBuffer.allocate(4).putInt(user.getId()).array());
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < result.length; i++) {
				sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
			}

			return sb;
		} catch (NoSuchAlgorithmException ex) {
			throw new InternalErrorException("Algorithm for sha1hash was not found.", ex);
		}
	}

        /**
	 * ChangedAttributeHook() sets UserExtSource with following properties:
	 *  - extSourceType is IdP
	 *  - extSourceName is https://login.bbmri-eric.eu/idp/
	 *  - user's extSource login is the same as his bbmri-persistent attribute
	 *
	 * @param session PerunSession
	 * @param user User to set UserExtSource for
	 * @param attribute Attribute containing bbmriID
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException
	 */
	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		try {
			String userNamespace = attribute.getFriendlyNameParameter();

			if(userNamespace.equals("bbmri-persistent-shadow") && attribute.getValue() != null){
				ExtSource extSource = session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, extSourceNameBbmri);
				UserExtSource userExtSource = new UserExtSource(extSource, 0, attribute.getValue().toString());

				session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSource);
			}
		} catch (UserExtSourceExistsException ex) {
			log.warn("BBMRI IdP external source already exists for the user.", ex);
		} catch (ExtSourceNotExistsException ex) {
			throw new InternalErrorException("IdP external source for BBMRI doesn't exist.", ex);
		}
	}

        public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:bbmri-persistent-shadow");
		attr.setDisplayName("BBMRI login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to BBMRI. Do not use it directly! " +
				"Use instead virt:bbmri-persistent attribute.");
		return attr;
	}
}
