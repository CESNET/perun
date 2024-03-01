package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractApplicationExpirationRulesModule;
import cz.metacentrum.perun.core.implApi.modules.attributes.VoAttributesModuleImplApi;
import java.util.LinkedHashMap;

/**
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class urn_perun_vo_attribute_def_def_applicationExpirationRules
    extends AbstractApplicationExpirationRulesModule<Vo> implements VoAttributesModuleImplApi {
  @Override
  public void changedAttributeHook(PerunSessionImpl session, Vo vo, Attribute attribute) {

  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Vo vo, Attribute attribute)
      throws WrongReferenceAttributeValueException {

  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Vo vo, Attribute attribute)
      throws WrongAttributeValueException {
    super.checkAttributeSyntax(perunSession, vo, attribute);
  }

  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, Vo vo, AttributeDefinition attribute) {
    return new Attribute(attribute);
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_VO_ATTR_DEF);
    attr.setFriendlyName("applicationExpirationRules");
    attr.setDisplayName("Application expiration rules");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription("Rules which define when auto reject application to Vo.");
    return attr;
  }
}
