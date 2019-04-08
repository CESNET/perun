package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;

import java.util.LinkedHashMap;

/**
 * @author Michal Prochazka &lt;michalp@ics.muni.cz&gt;
 */
public class urn_perun_vo_attribute_def_def_membershipExpirationRules extends AbstractMembershipExpirationRulesModule<Vo> implements VoAttributesModuleImplApi {

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
	public void changedAttributeHook(PerunSessionImpl session, Vo vo, Attribute attribute) {

	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl sess, Vo vo, AttributeDefinition attribute) {
		return new Attribute(attribute);
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
		attr.setFriendlyName("membershipExpirationRules");
		attr.setDisplayName("Membership expiration rules");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Rules which define how the membership is extended.");
		return attr;
	}
}
