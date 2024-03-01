package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;
import java.util.LinkedHashMap;

/**
 * @author Michal Prochazka &lt;michalp@ics.muni.cz&gt;
 */
public class urn_perun_vo_attribute_def_def_membershipExpirationRules
    extends AbstractMembershipExpirationRulesModule<Vo> implements VoAttributesModuleImplApi {

  public static final String VO_EXPIRATION_RULES_ATTR = AttributesManager.NS_VO_ATTR_DEF + ":membershipExpirationRules";

  @Override
  public void changedAttributeHook(PerunSessionImpl session, Vo vo, Attribute attribute) {

  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Vo entity, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    super.checkAttributeSemantics(perunSession, entity, attribute);
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

  @Override
  protected boolean isAllowedParameter(String parameter) {
    if (parameter == null) {
      return false;
    }
    return parameter.equals(MEMBERSHIP_PERIOD_KEY_NAME) || parameter.equals(MEMBERSHIP_DO_NOT_EXTEND_LOA_KEY_NAME) ||
           parameter.equals(MEMBERSHIP_GRACE_PERIOD_KEY_NAME) || parameter.equals(MEMBERSHIP_PERIOD_LOA_KEY_NAME) ||
           parameter.equals(MEMBERSHIP_DO_NOT_ALLOW_LOA_KEY_NAME) || parameter.equals(AUTO_EXTENSION_EXT_SOURCES) ||
           parameter.equals(AUTO_EXTENSION_LAST_LOGIN_PERIOD) || parameter.equals(EXPIRE_SPONSORED_MEMBERS);
  }
}
