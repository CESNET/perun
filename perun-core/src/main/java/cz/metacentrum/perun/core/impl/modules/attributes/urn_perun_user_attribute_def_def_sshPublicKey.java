package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Peschel <jakubpeschel@gmail.com>
 */
public class urn_perun_user_attribute_def_def_sshPublicKey extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//Null in value is ok here
		if(attribute.getValue() == null) return;
		
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
		attr.setFriendlyName("sshPublicKey");
		attr.setDisplayName("Public ssh key");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("User's SSH public keys.");
		return attr;
	}
}
