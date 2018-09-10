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

/**
 * Class for checking logins uniqueness in the namespace and filling elixir-persistent id.
 * It is only storage! Use module login elixir_persistent for access the value.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 *
 * @date 06.07.2015
 */
public class urn_perun_user_attribute_def_def_login_namespace_elixir_persistent_shadow extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_elixir_persistent_shadow.class);
	private final static String extSourceNameElixir = "https://login.elixir-czech.org/idp/";
	private final static String domainNameElixir = "@elixir-europe.org";
	private final static String attrNameElixir = "login-namespace:elixir-persistent-shadow";

	/**
	 * Filling implemented for login:namespace:elixir-persistent attribute
	 * Format is: "[hash]@elixir-europe.org" where [hash] represents sha1hash counted from user's id and perun instance id a login-namespace name
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

		if (attribute.getFriendlyName().equals(attrNameElixir)) {
			filledAttribute.setValue(sha1HashCount(user, domainNameElixir).toString() + domainNameElixir);
			return filledAttribute;
		} else {
			// without value
			return filledAttribute;
		}
	}

	/**
	 * ChangedAttributeHook() sets UserExtSource with following properties:
	 *  - extSourceType is IdP
	 *  - extSourceName is https://engine.elixir-idp.ics.muni.cz/authentication/idp/metadata
	 *  - user's extSource login is the same as his elixir-persistent attribute
	 *
	 * @param session PerunSession
	 * @param user User to set UserExtSource for
	 * @param attribute Attribute containing elixirID
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException
	 */
	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		try {
			String userNamespace = attribute.getFriendlyNameParameter();

			if(userNamespace.equals("elixir-persistent-shadow") && attribute.getValue() != null){
				ExtSource extSource = session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, extSourceNameElixir);
				UserExtSource userExtSource = new UserExtSource(extSource, 0, attribute.getValue().toString());

				session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSource);
			}
		} catch (UserExtSourceExistsException ex) {
			log.warn("Elixir IdP external source already exists for the user.", ex);
		} catch (ExtSourceNotExistsException ex) {
			throw new InternalErrorException("IdP external source for elixir doesn't exist.", ex);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:elixir-persistent-shadow");
		attr.setDisplayName("ELIXIR login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to ELIXIR. Do not use it directly! " +
				"Use \"user:virt:login-namespace:elixir-persistent\" attribute instead.");
		return attr;
	}
}
