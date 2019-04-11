package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Class for checking logins uniqueness in the namespace and filling eduroam login (same as všup).
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_login_namespace_eduroam_vsup extends urn_perun_user_attribute_def_def_login_namespace {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_def_login_namespace_eduroam_vsup.class);
	private final static Set<String> unpermittedLogins = new HashSet<>(Arrays.asList("administrator", "admin", "guest", "host", "vsup", "umprum", "root"));
	private final static String VSUP_NAMESPACE = AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:vsup";

	/**
	 * Checks if the user's login is unique in the namespace organization.
	 * Check if eduroam login is the same as institutional login
	 *
	 * @param sess PerunSession
	 * @param user User to check attribute for
	 * @param attribute Attribute to check value to
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 */
	@Override
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException {

		if (attribute != null && unpermittedLogins.contains(attribute.valueAsString())) throw new WrongAttributeValueException(attribute, user, "Login '" + attribute.getValue() + "' is not permitted.");

		// check is the same as VŠUP login
		try {
			Attribute a = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, VSUP_NAMESPACE);
			if (attribute != null && !Objects.equals(attribute.getValue(),a.getValue())) {
				throw new WrongAttributeValueException(attribute, user, "Eduroam login must match VŠUP login "+a.getValue());
			}
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Login namespace attribute for VŠUP must exists.", ex);
		}

		// check uniqueness
		super.checkAttributeValue(sess, user, attribute);

	}

	/**
	 * Fill eduroam login based on všup login. Values must be the same all the time.
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

		try {
			Attribute a = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, user, VSUP_NAMESPACE);
			if (a.getValue() != null) {
				// pre-fill if exists !
				filledAttribute.setValue(a.getValue());
			}
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException("Login namespace attribute for VŠUP must exists.", ex);
		}

		return filledAttribute;

	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>();
		dependencies.add(VSUP_NAMESPACE);
		return dependencies;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:eduroam-vsup");
		attr.setDisplayName("Login in namespace: eduroam-vsup");
		attr.setType(String.class.getName());
		attr.setDescription("Logname in namespace 'eduroam-vsup'.");
		return attr;
	}

}
