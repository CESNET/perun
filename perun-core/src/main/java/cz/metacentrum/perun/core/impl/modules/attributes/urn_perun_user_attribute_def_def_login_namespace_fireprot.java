package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for checking logins uniqueness in the namespace and filling login value.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_fireprot extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_fireprot.class);
	private final static String domainName = "@fireprot";

	/**
	 * Filling implemented for login-namespace:fireprot attribute
	 * Format is: "[hash]@fireprot" where [hash] represents sha1hash counted from user's id, domain and salt.
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
		if (filledAttribute.getValue() == null) {
			filledAttribute.setValue(sha1HashCount(user, domainName).toString() + domainName);
			return filledAttribute;
		} else {
			return filledAttribute;
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:fireprot");
		attr.setDisplayName("Fireprot login");
		attr.setType(String.class.getName());
		attr.setDescription("Login in Fireprot.");
		return attr;
	}

}
