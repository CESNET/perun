package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.MemberResourceAttributesModuleImplApi;

import java.util.LinkedHashMap;

/**
 * Attribute for setting override of the member's quota on the resource.
 * This override is always used instead of defaultDataQuota on resource or specific member-resource DataQuota
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_member_resource_attribute_def_def_dataQuotasOverride extends MemberResourceAttributesModuleAbstract implements MemberResourceAttributesModuleImplApi {

	@Override
	public void checkAttributeSyntax(PerunSessionImpl perunSession, Member member, Resource resource, Attribute attribute) throws WrongAttributeValueException {
		//attribute can be null, it means there are no default settings on resource
		if(attribute.getValue() == null) {
			return;
		}

		//Check if every part of this map has the right pattern
		//And also check if every quota part has right settings (softQuota<=hardQuota)
		perunSession.getPerunBl().getModulesUtilsBl().checkAndTransferQuotas(attribute, resource, member, true);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF);
		attr.setFriendlyName("dataQuotasOverride");
		attr.setDisplayName("Override of data quotas for member on resource");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Override has the highest priority for setting data quotas of member on resource. " +
				"Every record is the path (to volume) and the quota in format 'SoftQuota:HardQuota'. Example: '1000:2000'.");
		return attr;
	}
}
