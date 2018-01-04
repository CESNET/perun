package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Attribute for setting maximum of file quotas for any volume on defined resource.
 * By this attribute, facility manager is able to set maximum for defined resource
 * which must be adhere by Vo Manager of assigned Vo.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_resource_attribute_def_def_maxUserFileQuotas extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

	@Override
	public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//attribute can be null, it means there are no max user settings on resource
		if(attribute.getValue() == null) {
			return;
		}

		//Check if every part of this map has the right pattern
		//And also check if every quota part has right settings (softQuota<=hardQuota)
		perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(attribute, resource, null, false);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("maxUserFileQuotas");
		attr.setDisplayName("Maximum of file quotas of user on any volumes.");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Maximum file quota for each user on this resource. " +
				"Every record is the path (to volume) and the quota in format 'SoftQuota:HardQuota'. Example: '1000:2000'.");
		return attr;
	}
}
