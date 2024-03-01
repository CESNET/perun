package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractApplicationExpirationRulesModule;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

import java.util.LinkedHashMap;

/**
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class urn_perun_group_attribute_def_def_applicationExpirationRules
    extends AbstractApplicationExpirationRulesModule<Group> implements GroupAttributesModuleImplApi {
  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, Group group, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException {
    return new Attribute(attribute);
  }

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Group group, Attribute attribute)
      throws WrongAttributeValueException {
    super.checkAttributeSyntax(perunSession, group, attribute);
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException {

  }

  @Override
  public void changedAttributeHook(PerunSessionImpl session, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException {

  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attr.setFriendlyName("applicationExpirationRules");
    attr.setDisplayName("Application expiration rules");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription("Rules which define when auto reject application to Group.");
    return attr;
  }
}
