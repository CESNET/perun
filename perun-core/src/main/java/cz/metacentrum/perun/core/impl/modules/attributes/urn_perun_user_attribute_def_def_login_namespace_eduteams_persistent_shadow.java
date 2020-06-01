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
import cz.metacentrum.perun.core.impl.modules.ModulesConfigLoader;
import cz.metacentrum.perun.core.impl.modules.ModulesYamlConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking logins uniqueness in the namespace and filling eduteams-persistent id.
 * It is only storage! Use module login eduteams_persistent for access the value.
 *
 */
public class urn_perun_user_attribute_def_def_login_namespace_eduteams_persistent_shadow extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_eduteams_persistent_shadow.class);
	private final static String attrNameEduTeams = "login-namespace:eduteams-persistent-shadow";

	private final static String CONFIG_EXT_SOURCE_NAME_EDUTEAMS = "extSourceNameEduTeams";
	private final static String CONFIG_DOMAIN_NAME_EDUTEAMS = "domainNameEduTeams";

	private ModulesConfigLoader loader = new ModulesYamlConfigLoader();
	private String extSourceNameEduTeams = null;
	private String domainNameEduTeams = null;

	public urn_perun_user_attribute_def_def_login_namespace_eduteams_persistent_shadow() { }

	public urn_perun_user_attribute_def_def_login_namespace_eduteams_persistent_shadow(ModulesConfigLoader loader) {
		this.loader = loader;
	}

	/**
	 * Filling implemented for login:namespace:eduteams-persistent attribute
	 * Format is: "[hash]@eduteams-europe.org" where [hash] represents sha1hash counted from user's id and perun instance id a login-namespace name
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

		if (attribute.getFriendlyName().equals(attrNameEduTeams)) {
			String domain = "@" + getDomainNameEduTeams();
			filledAttribute.setValue(sha1HashCount(user, domain).toString() + domain);
			return filledAttribute;
		} else {
			// without value
			return filledAttribute;
		}
	}

	/**
	 * ChangedAttributeHook() sets UserExtSource with following properties:
	 *  - extSourceType is IdP
	 *  - extSourceName is https://engine.eduteams-idp.ics.muni.cz/authentication/idp/metadata
	 *  - user's extSource login is the same as his eduteams-persistent attribute
	 *
	 * @param session PerunSession
	 * @param user User to set UserExtSource for
	 * @param attribute Attribute containing eduteamsID
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException {
		try {
			String userNamespace = attribute.getFriendlyNameParameter();

			if(userNamespace.equals("eduteams-persistent-shadow") && attribute.getValue() != null){
				ExtSource extSource = session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, getExtSourceNameEduTeams());
				UserExtSource userExtSource = new UserExtSource(extSource, 0, attribute.getValue().toString());

				session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSource);
			}
		} catch (UserExtSourceExistsException ex) {
			log.warn("Eduteams IdP external source already exists for the user.", ex);
		} catch (ExtSourceNotExistsException ex) {
			throw new InternalErrorException("IdP external source for eduteams doesn't exist.", ex);
		}
	}

	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:eduteams-persistent-shadow");
		attr.setDisplayName("eduTEAMS login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to eduTEAMS. Do not use it directly! " +
				"Use \"user:virt:login-namespace:eduteams-persistent\" attribute instead.");
		return attr;
	}

	public String getExtSourceNameEduTeams() {
		if (extSourceNameEduTeams == null) {
			extSourceNameEduTeams = loader.loadString(getClass().getSimpleName(), CONFIG_EXT_SOURCE_NAME_EDUTEAMS);
		}
		return extSourceNameEduTeams;
	}

	public String getDomainNameEduTeams() {
		if (domainNameEduTeams == null) {
			domainNameEduTeams = loader.loadString(getClass().getSimpleName(), CONFIG_DOMAIN_NAME_EDUTEAMS);
		}
		return domainNameEduTeams;
	}

}
