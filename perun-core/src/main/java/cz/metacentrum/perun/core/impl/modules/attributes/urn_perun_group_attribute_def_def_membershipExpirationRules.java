package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.LinkedHashMap;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_group_attribute_def_def_membershipExpirationRules extends AbstractMembershipExpirationRulesModule<Group> implements GroupAttributesModuleImplApi {

	@Override
	protected boolean isAllowedParameter(String parameter) {
		if(parameter == null) return false;
		return parameter.equals(MembersManager.membershipPeriodKeyName) ||
				parameter.equals(MembersManager.membershipDoNotExtendLoaKeyName) ||
				parameter.equals(MembersManager.membershipGracePeriodKeyName) ||
				parameter.equals(MembersManager.membershipPeriodLoaKeyName)	||
				parameter.equals(MembersManager.membershipDoNotAllowLoaKeyName);
	}

	@Override
	public Attribute fillAttribute(PerunSessionImpl perunSession, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		return null;
	}

	@Override
	public void changedAttributeHook(PerunSessionImpl session, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {

	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setFriendlyName("membershipExpirationRules");
		attr.setDisplayName("Membership expiration rules");
		attr.setType(LinkedHashMap.class.getName());
		attr.setDescription("Rules which define how the membership is extended.");
		return attr;
	}
}
