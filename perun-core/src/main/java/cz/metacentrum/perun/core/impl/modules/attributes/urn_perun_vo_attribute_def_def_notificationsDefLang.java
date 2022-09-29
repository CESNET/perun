package cz.metacentrum.perun.core.impl.modules.attributes;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;

import java.util.ArrayList;
import java.util.List;

public class urn_perun_vo_attribute_def_def_notificationsDefLang extends VoAttributesModuleAbstract implements VoAttributesModuleImplApi {
	private final List<String> defaultLanguages = new ArrayList<>(List.of("en", "cs"));


	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Vo vo, Attribute attribute) throws WrongAttributeValueException {
		String value = attribute.valueAsString();
		if (!defaultLanguages.contains(value)) {
			throw new WrongAttributeValueException(attribute, "Language " + value + " is not supported for notifications.");
		}
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
		attr.setFriendlyName("notificationsDefLang");
		attr.setDisplayName("Notifications language");
		attr.setType(String.class.getName());
		attr.setDescription("Default notification language");
		return attr;
	}
}
