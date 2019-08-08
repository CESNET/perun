package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Stava &lt;stavamichal@gmail.com&gt;
 */
public class urn_perun_user_attribute_def_def_phone extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	//This regular expression requires international form of phone number starting with '+' without spaces
	//Due to technical reasons we support only numbers longer than 3 characters and shorter than 17 characters [4,16]
	//Example of correct number "+420123456789"
	private static final Pattern pattern = Pattern.compile("^[+][0-9]{4,16}$");

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Attribute attribute) throws WrongAttributeValueException {
		// null attribute
		if (attribute.getValue() == null) throw new WrongAttributeValueException(attribute, "User attribute phone cannot be null.");

		// wrong type of the attribute
		if (!(attribute.getValue() instanceof String)) throw new WrongAttributeValueException(attribute, "Wrong type of the attribute. Expected: String");

		String phone = attribute.valueAsString();

		Matcher matcher = pattern.matcher(phone);
		if (!matcher.matches()) {
			throw new WrongAttributeValueException(attribute, "Phone is not in correct format!");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("phone");
		attr.setDisplayName("Phone");
		attr.setType(String.class.getName());
		attr.setDescription("Phone number provided by IDP.");
		return attr;
	}
}
