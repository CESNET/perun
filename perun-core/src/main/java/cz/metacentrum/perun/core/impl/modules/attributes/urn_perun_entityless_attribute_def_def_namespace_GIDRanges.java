package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.EntitylessAttributesModuleImplApi;

import java.util.LinkedHashMap;

/**
 * GID Ranges for specific namespace which can be managed by Perun
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class urn_perun_entityless_attribute_def_def_namespace_GIDRanges extends EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, String key, Attribute attribute) throws WrongAttributeValueException {
		//Check if gid ranges are in correct format (we don't need to use the output of the method there, we want to just check it)
		perunSession.getPerunBl().getModulesUtilsBl().checkAndConvertGIDRanges(attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("namespace-GIDRanges");
		attr.setDisplayName("GID ranges in namespace");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Manageable GID ranges in a namespace - key of map is minimum and assigned value is maximum of one range, minimum and maximum can be equal");
		return attr;
	}
}
