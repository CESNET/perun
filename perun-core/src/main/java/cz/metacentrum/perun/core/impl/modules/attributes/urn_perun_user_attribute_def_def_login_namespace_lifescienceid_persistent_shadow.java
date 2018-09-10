package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking logins uniqueness in the namespace and filling lifescienceid-persistent id.
 * It is only storage! Use module login lifescienceid_persistent for access the value.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_lifescienceid_persistent_shadow extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_lifescienceid_persistent_shadow.class);
	private final static String extSourceNamelifescienceid = "https://saml.pilot.lifescienceid.org/proxy/saml2/idp/metadata.php";
	private final static String domainNamelifescienceid = "@lifescienceid.org";
	private final static String attrNamelifescienceid = "login-namespace:lifescienceid-persistent-shadow";

	/**
	 * Filling implemented for login:namespace:lifescienceid-persistent attribute
	 * Format is: "[hash]@lifescienceid.eu" where [hash] represents sha1hash counted from user's id and perun instance id a login-namespace name
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

		if (attribute.getFriendlyName().equals(attrNamelifescienceid)) {
			filledAttribute.setValue(sha1HashCount(user, domainNamelifescienceid).toString() + domainNamelifescienceid);
			return filledAttribute;
		} else {
			// without value
			return filledAttribute;
		}
	}

	/**
	 * ChangedAttributeHook() sets UserExtSource with following properties:
	 *  - extSourceType is IdP
	 *  - extSourceName is https://saml.pilot.lifescienceid.org/proxy/saml2/idp/metadata.php
	 *  - user's extSource login is the same as his lifescienceid-persistent attribute
	 *
	 * @param session PerunSession
	 * @param user User to set UserExtSource for
	 * @param attribute Attribute containing lifescienceidID
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 *
	 */
	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		try {
			String userNamespace = attribute.getFriendlyNameParameter();

			if(userNamespace.equals("lifescienceid-persistent-shadow") && attribute.getValue() != null){
				ExtSource extSource = session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, extSourceNamelifescienceid);
				UserExtSource userExtSource = new UserExtSource(extSource, 0, attribute.getValue().toString());

				session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSource);
			}
		} catch (UserExtSourceExistsException ex) {
			log.warn("lifescienceid IdP external source already exists for the user.", ex);
		} catch (ExtSourceNotExistsException ex) {
			throw new InternalErrorException("IdP external source for lifescienceid doesn't exist.", ex);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:lifescienceid-persistent-shadow");
		attr.setDisplayName("Lifescienceid login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to Lifescienceid. Do not use it directly! " +
				"Use \"user:virt:login-namespace:lifescienceid-persistent\" attribute instead.");
		return attr;
	}

}
