package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.LinkedHashMap;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_group_attribute_def_def_groupMembershipExpirationRules extends AbstractMembershipExpirationRulesModule<Group> implements GroupAttributesModuleImplApi {

	@Override
	protected boolean isAllowedParameter(String parameter) {
		if(parameter == null) return false;
		return parameter.equals(membershipPeriodKeyName) ||
				parameter.equals(membershipDoNotExtendLoaKeyName) ||
				parameter.equals(membershipGracePeriodKeyName) ||
				parameter.equals(membershipPeriodLoaKeyName)	||
				parameter.equals(membershipDoNotAllowLoaKeyName);
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, Group group, AttributeDefinition attribute) {
		return null;
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Group group, Attribute attribute) {

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("groupMembershipExpirationRules");
		attr.setDisplayName("Group membership expiration rules");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Rules which define how the membership in group is extended.");
		return attr;
	}
}
