package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.List;

/**
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_user_attribute_def_def_preferredShells extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl sess, User user, Attribute attribute) throws WrongAttributeValueException {
		List<String> pshell = (List<String>) attribute.getValue();

		if (pshell != null){
			for(String shell : pshell){
				if(shell != null){
					if(shell.isEmpty()){
						throw new WrongAttributeValueException(attribute, user, "shell cannot be empty string");
					}else{
						sess.getPerunBl().getModulesUtilsBl().checkFormatOfShell(shell, attribute);
					}
				}else{
					throw new WrongAttributeValueException(attribute, user, "shell cannot be null");
				}
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("preferredShells");
		attr.setDisplayName("Preferred shells");
		attr.setType(List.class.getName());
		attr.setDescription("User preferred shells, ordered by user's personal preferences");
		return attr;
	}
}

