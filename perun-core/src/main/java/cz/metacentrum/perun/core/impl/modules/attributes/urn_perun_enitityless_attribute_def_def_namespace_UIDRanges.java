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
 * UID Ranges for specific namespace which can be managed by Perun
 *
 * @author Metodej Klang <metodej.klang@gmail.com>
 */
public class urn_perun_enitityless_attribute_def_def_namespace_UIDRanges extends EntitylessAttributesModuleAbstract implements EntitylessAttributesModuleImplApi {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, String key, Attribute attribute) throws WrongAttributeValueException {
		//Check if uid ranges are in correct format (we don't need to use the output of the method there, we want to just check it)
		perunSession.getPerunBl().getModulesUtilsBl().checkAndConvertIDRanges(attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attr.setFriendlyName("namespace-UIDRanges");
		attr.setDisplayName("UID ranges in namespace");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Manageable UID ranges in a namespace - key of map is minimum and assigned value is maximum of one range, minimum and maximum can be equal");
		return attr;
	}
}
