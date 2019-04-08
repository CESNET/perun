package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Slavek Licehammer &lt;glory@ics.muni.cz&gt;
 */
public class urn_perun_user_attribute_def_def_kerberosLogins extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[-/_.a-zA-Z0-9@]+@[-_.A-z0-9]+$");

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {
		if(attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user, "Attribute's value can't be null.");
		List<String> value = (List<String>) attribute.getValue();
		if(value.isEmpty()) throw new WrongAttributeValueException(attribute, user, "Attribute's value can't be empty list");
		for(String login : value) {
			Matcher matcher = pattern.matcher(login);
			if(!matcher.matches()) throw new WrongAttributeValueException(attribute, user, "Attribute's value is not in correct format. format: login@realm");
		}
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, User user, AttributeDefinition attribute) {
		return new Attribute(attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("kerberosLogins");
		attr.setDisplayName("KERBEROS principals");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Logins in kerberos (including realm).");
		return attr;
	}
}
