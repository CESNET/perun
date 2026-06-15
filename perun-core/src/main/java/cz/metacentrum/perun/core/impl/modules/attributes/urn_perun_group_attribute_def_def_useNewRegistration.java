package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;

public class urn_perun_group_attribute_def_def_useNewRegistration extends GroupAttributesModuleAbstract
    implements GroupAttributesModuleImplApi {

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    super.checkAttributeSemantics(perunSession, group, attribute);
    if (attribute.getValue() != null && attribute.valueAsBoolean() &&
            perunSession.getPerunBl().getGroupsManagerBl().getApplicationFormForGroup(group) == null) {
      throw new WrongReferenceAttributeValueException(attribute, "Group " + group.getName() + " has no application " +
                                                                     "form. A form is required to send notifications " +
                                                                     "for applications.");
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attr.setType(Boolean.class.getName());
    attr.setFriendlyName("useNewRegistration");
    attr.setDisplayName("Use new registration");
    attr.setDescription(
        "A flag determining whether the new Registrar component is used to submit and manage application and forms.");
    return attr;
  }
}
