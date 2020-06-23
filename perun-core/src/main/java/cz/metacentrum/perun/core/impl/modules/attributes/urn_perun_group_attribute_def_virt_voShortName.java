package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.SkipValueCheckDuringDependencyCheck;

/**
 * Short name of VO, that group belongs to
 *
 * @author Dano Fecko <dano9500@gmail.com>
 */
@SkipValueCheckDuringDependencyCheck
public class urn_perun_group_attribute_def_virt_voShortName extends GroupVirtualAttributesModuleAbstract implements GroupVirtualAttributesModuleImplApi {

	@Override
	public Attribute getAttributeValue(PerunSessionImpl perunSession, Group group, AttributeDefinition attributeDefinition) {
		Attribute attribute = new Attribute(attributeDefinition);
		attribute.setValue(perunSession.getPerunBl().getGroupsManagerBl().getVo(perunSession, group).getShortName());
		return attribute;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition definition = new AttributeDefinition();
		definition.setNamespace(AttributesManager.NS_GROUP_ATTR_VIRT);
		definition.setDisplayName("VO shortName");
		definition.setFriendlyName("voShortName");
		definition.setDescription("Short name of VO, that group belongs to");
		definition.setType(String.class.getName());
		return definition;
	}
}
