package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

public class urn_perun_user_attribute_def_def_login_namespace_lifescienceid_username extends urn_perun_user_attribute_def_def_login_namespace{
	private final static String elixirUsername = "urn:perun:user:attribute-def:def:login-namespace:elixir";
	private final static String bbmriUsername = "urn:perun:user:attribute-def:def:login-namespace:bbmri";

	@Override
	public void changedAttributeHook(PerunSessionImpl sess, User user, Attribute attribute) {
		trySetAttribute(sess, user, attribute, elixirUsername);
		trySetAttribute(sess, user, attribute, bbmriUsername);
	}

	/**
	 * Set attribute if it is not filled yet
	 */
	private void trySetAttribute(PerunSessionImpl sess, User user, Attribute lsAttribute, String attributeName) {
		Attribute newAttribute;
		try {
			newAttribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, attributeName);
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			return;
		}

		if (newAttribute.getValue() != null && !newAttribute.valueAsString().isBlank()) {
			return;
		}

		newAttribute.setValue(lsAttribute.getValue());

		try {
			sess.getPerunBl().getAttributesManagerBl().setAttribute(sess, user, newAttribute);
		} catch (WrongAttributeValueException | WrongAttributeAssignmentException | WrongReferenceAttributeValueException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("login-namespace:lifescienceid-username");
		attr.setDisplayName("Lifescience username (login)");
		attr.setType(String.class.getName());
		attr.setDescription("Login in namespaceid: lifescience");
		return attr;
	}
}
