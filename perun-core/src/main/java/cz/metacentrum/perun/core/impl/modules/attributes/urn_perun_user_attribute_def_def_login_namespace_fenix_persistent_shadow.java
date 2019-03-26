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
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking logins uniqueness in the namespace and filling fenix-persistent id.
 * It is only storage! Use module login fenix_persistent for access the value.
 *
 */
public class urn_perun_user_attribute_def_def_login_namespace_fenix_persistent_shadow extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_fenix_persistent_shadow.class);
	private final static String extSourceNameFenix = "https://proxy-fenix.pilot.eduteams.org/proxy";
	private final static String domainNameFenix = "@fenix.pilot.eduteams.org";
	private final static String attrNameFenix = "login-namespace:fenix-persistent-shadow";

	/**
	 * Filling implemented for login:namespace:fenix-persistent attribute
	 * Format is: "[hash]@fenix-europe.org" where [hash] represents sha1hash counted from user's id and perun instance id a login-namespace name
	 *
	 * @param perunSession PerunSession
	 * @param user User to fill attribute for
	 * @param attribute Attribute to fill value to
	 * @return Filled attribute
	 * @throws InternalErrorException
	 */
	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, User user, AttributeDefinition attribute) throws InternalErrorException {

		Attribute filledAttribute = new Attribute(attribute);

		if (attribute.getFriendlyName().equals(attrNameFenix)) {
			filledAttribute.setValue(sha1HashCount(user, domainNameFenix).toString() + domainNameFenix);
			return filledAttribute;
		} else {
			// without value
			return filledAttribute;
		}
	}

	/**
	 * ChangedAttributeHook() sets UserExtSource with following properties:
	 *  - extSourceType is IdP
	 *  - extSourceName is https://engine.fenix-idp.ics.muni.cz/authentication/idp/metadata
	 *  - user's extSource login is the same as his fenix-persistent attribute
	 *
	 * @param session PerunSession
	 * @param user User to set UserExtSource for
	 * @param attribute Attribute containing fenixID
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException {
		try {
			String userNamespace = attribute.getFriendlyNameParameter();

			if(userNamespace.equals("fenix-persistent-shadow") && attribute.getValue() != null){
				ExtSource extSource = session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, extSourceNameFenix);
				UserExtSource userExtSource = new UserExtSource(extSource, 0, attribute.getValue().toString());

				session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSource);
			}
		} catch (UserExtSourceExistsException ex) {
			log.warn("Fenix IdP external source already exists for the user.", ex);
		} catch (ExtSourceNotExistsException ex) {
			throw new InternalErrorException("IdP external source for fenix doesn't exist.", ex);
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:fenix-persistent-shadow");
		attr.setDisplayName("FENIX login");
		attr.setType(String.class.getName());
		attr.setDescription("Login for FENIX. Do not use it directly! " +
				"Use \"user:virt:login-namespace:fenix-persistent\" attribute instead.");
		return attr;
	}

}
