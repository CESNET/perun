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

/**
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class urn_perun_user_attribute_def_def_sshPublicAdminKey extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {
		if(attribute.getValue() == null) throw new WrongAttributeValueException(attribute, user,"Cant be null.");
		
		//Testing if some ssh key contains new line character
		List<String> sshKeys = (ArrayList<String>) attribute.getValue();
		for(String sshKey: sshKeys) {
			if(sshKey != null) {
				if(sshKey.contains("\n")) throw new WrongAttributeValueException(attribute, user, "One of keys in attribute contains new line character. New line character is not allowed here.");
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("sshPublicAdminKey");
		attr.setDisplayName("Public ssh admin key");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("User's SSH public keys used from root access.");
		return attr;
	}
}
