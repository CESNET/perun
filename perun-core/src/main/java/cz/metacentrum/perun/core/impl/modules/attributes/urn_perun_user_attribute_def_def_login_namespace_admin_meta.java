package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsAlreadyBlockedException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.List;

/**
 * Module for admin-meta login namespace.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class urn_perun_user_attribute_def_def_login_namespace_admin_meta extends urn_perun_user_attribute_def_def_login_namespace {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute) throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		String userLogin = attribute.valueAsString();
		if (userLogin == null) throw new WrongReferenceAttributeValueException(attribute, null, user, null, "Value can't be null");

		// Get all users who have set attribute urn:perun:member:attribute-def:def:login-namespace:[login-namespace], with the value.
		List<User> usersWithSameLogin = sess.getPerunBl().getUsersManagerBl().getUsersByAttribute(sess, attribute, true);

		usersWithSameLogin.remove(user); //remove self
		if (!usersWithSameLogin.isEmpty()) {
			if(usersWithSameLogin.size() > 1) throw new ConsistencyErrorException("FATAL ERROR: Duplicated Login detected." +  attribute + " " + usersWithSameLogin);
			throw new WrongReferenceAttributeValueException(attribute, attribute, user, null, usersWithSameLogin.get(0), null, "This login " + attribute.getValue() + " is already occupied.");
		}

		try {
			String namespace = attribute.getFriendlyNameParameter();
			sess.getPerunBl().getUsersManagerBl().checkReservedLogins(sess, namespace, userLogin, true);
			sess.getPerunBl().getUsersManagerBl().checkBlockedLogins(sess, namespace, userLogin, true);
		} catch (AlreadyReservedLoginException ex) {
			throw new WrongReferenceAttributeValueException(attribute, null, user, null, null, null, "Login in specific namespace already reserved.", ex);
		} catch (LoginIsAlreadyBlockedException ex) {
			throw new WrongReferenceAttributeValueException(attribute, null, "Login is blocked.", ex);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:admin-meta");
		attr.setDisplayName("Login in namespace: admin-meta");
		attr.setType(String.class.getName());
		attr.setDescription("Logname in namespace 'admin-meta'");
		return attr;
	}
}
