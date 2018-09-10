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
 * Class for checking logins uniqueness in the namespace and filling einfraid-persistent id.
 * It is only storage! Use module login elixir_persistent for access the value.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_einfraid_persistent_shadow extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_einfraid_persistent_shadow.class);
	private final static String extSourceNameEinfraid = "https://login.cesnet.cz/idp/";
	private final static String domainNameEinfraid = "@einfra.cesnet.cz";
	private final static String attrNameEinfraid = "login-namespace:einfraid-persistent-shadow";

	/**
	 * Filling implemented for login:namespace:einfraid-persistent attribute
	 * Format is: "[hash]@einfra.cesnet.cz" where [hash] represents sha1hash counted from users id, namespace and perun instance ID.
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

		if (attribute.getFriendlyName().equals(attrNameEinfraid)) {
			filledAttribute.setValue(sha1HashCount(user, domainNameEinfraid).toString() + domainNameEinfraid);
			return filledAttribute;
		} else {
			// without value
			return filledAttribute;
		}
	}

	/**
	 * ChangedAttributeHook() sets UserExtSource of IdP type for CESNET ProxyIdP (@einfra.cesnet.cz)
	 *
	 * @param session PerunSession
	 * @param user User to set UserExtSource for
	 * @param attribute Attribute containing einfraID
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 */
	@Override
	public void changedAttributeHook(PerunSessionImpl session, User user, Attribute attribute) throws InternalErrorException, WrongReferenceAttributeValueException {
		try {
			String userNamespace = attribute.getFriendlyNameParameter();

			if(userNamespace.equals("einfraid-persistent-shadow") && attribute.getValue() != null){
				ExtSource extSource = session.getPerunBl().getExtSourcesManagerBl().getExtSourceByName(session, extSourceNameEinfraid);
				UserExtSource userExtSource = new UserExtSource(extSource, 0, attribute.getValue().toString());

				session.getPerunBl().getUsersManagerBl().addUserExtSource(session, user, userExtSource);
			}
		} catch (UserExtSourceExistsException ex) {
			log.warn("EinfraID IdP external source already exists for the user.", ex);
		} catch (ExtSourceNotExistsException ex) {
			throw new InternalErrorException("IdP external source for EinfraID doesn't exist.", ex);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:einfraid-persistent-shadow");
		attr.setDisplayName("EINFRA ID login");
		attr.setType(String.class.getName());
		attr.setDescription("Login to EINFRA ID. Do not use it directly! Use virt:einfraid-persistent attribute instead.");
		return attr;
	}
}
