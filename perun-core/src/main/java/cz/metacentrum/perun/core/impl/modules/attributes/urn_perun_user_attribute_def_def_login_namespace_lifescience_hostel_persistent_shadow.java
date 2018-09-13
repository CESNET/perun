package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

/**
 * Class for checking logins uniqueness in the namespace and filling lifescience-hostel-persistent id.
 * It is only storage! Use module login lifescience-hostel_persistent for access the value.
 *
 * @author Peter Jancus <p.jancus1996@gmail.com>
 */
public class urn_perun_user_attribute_def_def_login_namespace_lifescience_hostel_persistent_shadow extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_lifescience_hostel_persistent_shadow.class);
	private final static String extSourceNameLSH = "https://login.bbmri-eric.eu/hostel/";
	private final static String domainNameBbmri = "@bbmri.eu";
	private final static String attrNameBbmri = "login-namespace:bbmri-persistent-shadow";

	/**
	 * Filling implemented for login:namespace:bbmri-persistent attribute
	 * Format is: "[hash]@bbmri-europe.org" where [hash] represents sha1hash counted from users id, namespace and instance ID
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

		if (attribute.getFriendlyName().equals(attrNameBbmri)) {
			filledAttribute.setValue(sha1HashCount(user, domainNameBbmri).toString() + domainNameBbmri);
			return filledAttribute;
		} else {
			// without value
			return filledAttribute;
		}
	}

	/**
	 * ChangedAttributeHook() sets UserExtSource with following properties:
	 *  - extSourceType is IdP
	 *  - extSourceName is https://login.bbmri-eric.eu/hostel/
	 *  - user's extSource login is the same as his lifescience-hostel-persistent attribute
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

			if(userNamespace.equals("lifescience-hostel-persistent-shadow") && attribute.getValue() != null){
				ExtSource extSource = session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, extSourceNameLSH);
				UserExtSource userExtSource = new UserExtSource(extSource, 0, ((String)attribute.getValue()));

				session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSource);
			}
		} catch (UserExtSourceExistsException ex) {
			log.warn("LIFESCIENCE HOSTEL IdP external source already exists for the user.", ex);
		} catch (ExtSourceNotExistsException ex) {
			throw new InternalErrorException("IdP external source for LIFESCIENCE HOSTEL doesn't exist.", ex);
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:lifescience-hostel-persistent-shadow");
		attr.setDisplayName("LIFESCIENCE HOSTEL login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to LIFESCIENCE HOSTEL. Do not use it directly! " +
				"Use instead virt:lifescience-hostel-persistent attribute.");
		return attr;
	}
}
