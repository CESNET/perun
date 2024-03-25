package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_group_attribute_def_def_groupMembershipExpirationRules
    extends AbstractMembershipExpirationRulesModule<Group> implements GroupAttributesModuleImplApi {

  @Override
  public void changedAttributeHook(PerunSessionImpl session, Group group, Attribute attribute) {

  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl sess, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    if (attribute.getValue() == null) {
      return;
    }
    try {
      if (!attribute.valueAsMap().isEmpty()) {
        Attribute conflictingAttribute = sess.getPerunBl().getAttributesManagerBl()
            .getAttribute(sess, group, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME);
        if (conflictingAttribute.getValue() != null && conflictingAttribute.valueAsString().equals("true")) {
          throw new WrongReferenceAttributeValueException(attribute, conflictingAttribute, group, null, group, null,
              conflictingAttribute.toString() +
              " can not be enabled in order to create group membership expiration rules.");
        }
      }
    } catch (AttributeNotExistsException e) {
      throw new ConsistencyErrorException(e);
    }
  }

  @Override
  public Attribute fillAttribute(PerunSessionImpl perunSession, Group group, AttributeDefinition attribute) {
    return null;
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

  @Override
  public List<String> getDependencies() {
    List<String> dependencies = new ArrayList<>();
    dependencies.add(GroupsManager.GROUPSYNCHROENABLED_ATTRNAME);
    return dependencies;
  }

  @Override
  protected boolean isAllowedParameter(String parameter) {
    if (parameter == null) {
      return false;
    }
    return parameter.equals(MEMBERSHIP_PERIOD_KEY_NAME) || parameter.equals(MEMBERSHIP_DO_NOT_EXTEND_LOA_KEY_NAME) ||
           parameter.equals(MEMBERSHIP_GRACE_PERIOD_KEY_NAME) || parameter.equals(MEMBERSHIP_PERIOD_LOA_KEY_NAME) ||
           parameter.equals(MEMBERSHIP_DO_NOT_ALLOW_LOA_KEY_NAME);
  }
}
