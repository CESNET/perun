package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;

import java.util.Objects;

/**
 * Attribute represents "permanent visit" flag in K4 system.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class urn_perun_user_attribute_def_def_k4Staleakt extends UserAttributesModuleAbstract implements UserAttributesModuleImplApi {

	@Override
	public void checkAttributeSemantics(PerunSessionImpl perunSession, User user, Attribute attribute) throws WrongAttributeValueException {

		if (attribute.getValue() != null) {
			if (!Objects.equals(attribute.getValue(), "0") && !Objects.equals(attribute.getValue(), "1")) {
				throw new WrongAttributeValueException(attribute, user, "Flag of K4 permanent visit must be empty, 0 or 1.");
			}
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setFriendlyName("k4Staleakt");
		attr.setDisplayName("K4 Permanent Visit");
		attr.setType(String.class.getName());
		attr.setDescription("Flag of users permanent visit in K4 (1 = yes, 0 = no).");
		return attr;
	}

}
