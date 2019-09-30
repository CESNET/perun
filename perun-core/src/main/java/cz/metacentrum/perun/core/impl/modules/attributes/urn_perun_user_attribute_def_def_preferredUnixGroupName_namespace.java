package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_user_attribute_def_def_preferredUnixGroupName_namespace extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	private static final Pattern pattern = Pattern.compile("^[-_.a-zA-Z0-9]+$");

	@Override
	public void checkAttributeSyntax(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {
		if(attribute.getValue()!= null) {
			for(String groupName: attribute.valueAsList()) {
				Matcher matcher = pattern.matcher(groupName);
				if (!matcher.matches()) throw new WrongAttributeValueException(attribute, user, "GroupName: " + groupName + " content invalid characters. Allowed are only letters, numbers and characters _ and - and .");
			}
		}
	}

	/*
	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("preferredUnixGroupName-namespace");
		attr.setType(List.class.getName());
		attr.setDescription("User preferred unix group name, ordered by user's personal preferrences.");
		return attr;
	}
	*/
}

