package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.regex.Matcher;

/**
 * Checks generic mail for mail aliases
 *
 * @author Michal Šťava   <stava.michal@gmail.com>
 */
public class urn_perun_user_attribute_def_def_mailaliasesGenericMail extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {
		String attributeValue;

		if(attribute.getValue() == null) return;
		else attributeValue = (String) attribute.getValue();

		Matcher emailMatcher = Utils.emailPattern.matcher(attributeValue);
		if(!emailMatcher.find()) throw new WrongAttributeValueException(attribute, user, "Email is not in correct form.");
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("mailaliasesGenericMail");
		attr.setDisplayName("Generic mailaliases mail");
		attr.setType(String.class.getName());
		attr.setDescription("User's generic mailaliases mail.");
		return attr;
	}
}
