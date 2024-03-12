package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Group structure synchronization
 * <p>
 * true if structure synchronization is enabled
 * false if not
 * empty if there is no setting
 *
 * @author Erik Horváth <horvatherik3@gmail.com>
 */
public class urn_perun_group_attribute_def_def_groupStructureSynchronizationEnabled
    extends GroupAttributesModuleAbstract implements GroupAttributesModuleImplApi {
  private static final Logger log =
      LoggerFactory.getLogger(urn_perun_group_attribute_def_def_groupStructureSynchronizationEnabled.class);

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Group group, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    //Null value is ok, means no settings for group
    if (attribute.getValue() == null) {
      return;
    }

    if (attribute.valueAsBoolean()) {

      if (perunSession.getPerunBl().getGroupsManagerBl().isGroupSynchronizedFromExternallSource(perunSession, group)) {
        throw new InternalErrorException("Synchronization is already enabled for one of the parent groups.");
      }

      if (perunSession.getPerunBl().getGroupsManagerBl().isGroupForAnyAutoRegistration(perunSession, group)) {
        throw new WrongReferenceAttributeValueException(attribute,
            "Structure synchronization cannot be enabled for group in auto registration.");
      }

      for (Group subgroup : perunSession.getPerunBl().getGroupsManagerBl().getAllSubGroups(perunSession, group)) {
        if (perunSession.getPerunBl().getGroupsManagerBl().isGroupForAnyAutoRegistration(perunSession, subgroup)) {
          throw new WrongReferenceAttributeValueException(attribute,
              "Structure synchronization cannot be enabled for group with subgroup in auto registration: " +
                  subgroup.toString());
        }
      }

      try {
        Attribute attrSynchronizeEnabled = perunSession.getPerunBl().getAttributesManagerBl()
            .getAttribute(perunSession, group, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME);
        if (Objects.equals("true", attrSynchronizeEnabled.getValue())) {
          throw new WrongReferenceAttributeValueException(attribute, attrSynchronizeEnabled);
        }

        Attribute requiredAttribute = perunSession.getPerunBl().getAttributesManagerBl()
            .getAttribute(perunSession, group, GroupsManager.GROUPSQUERY_ATTRNAME);
        if (requiredAttribute.getValue() == null) {
          throw new WrongReferenceAttributeValueException(attribute, requiredAttribute,
              requiredAttribute.toString() + " must be set in order to enable synchronization.");
        }

        requiredAttribute = perunSession.getPerunBl().getAttributesManagerBl()
            .getAttribute(perunSession, group, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
        if (requiredAttribute.getValue() == null) {
          throw new WrongReferenceAttributeValueException(attribute, requiredAttribute,
              requiredAttribute.toString() + " must be set in order to enable synchronization.");
        }

        requiredAttribute = perunSession.getPerunBl().getAttributesManagerBl()
            .getAttribute(perunSession, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
        if (requiredAttribute.getValue() == null) {
          throw new WrongReferenceAttributeValueException(attribute, requiredAttribute,
              requiredAttribute.toString() + " must be set in order to enable synchronization.");
        }
      } catch (AttributeNotExistsException e) {
        throw new ConsistencyErrorException(e);
      }
    }
  }

  @Override
  public List<String> getDependencies() {
    List<String> dependencies = new ArrayList<>();
    dependencies.add(GroupsManager.GROUPSQUERY_ATTRNAME);
    dependencies.add(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
    dependencies.add(GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    dependencies.add(GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME);
    return dependencies;
  }

  @Override
  public void changedAttributeHook(PerunSessionImpl session, Group group, Attribute attribute) {
    if (attribute.getValue() == null) {
      try {
        AttributesManagerBl attributesManager = session.getPerunBl().getAttributesManagerBl();
        AttributeDefinition removeAttrDef = attributesManager.getAttributeDefinition(session,
            new urn_perun_group_attribute_def_def_flatGroupStructureEnabled().getAttributeDefinition().getName());
        attributesManager.removeAttributeWithoutCheck(session, group, removeAttrDef);
      } catch (WrongAttributeAssignmentException ex) {
        throw new InternalErrorException(ex);
      } catch (AttributeNotExistsException ex) {
        log.debug("Attribute for flat group structure synchronization does not exist", ex);
      }
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attr.setFriendlyName("groupStructureSynchronizationEnabled");
    attr.setDisplayName("Group Structure Synchronization Enabled");
    attr.setType(Boolean.class.getName());
    attr.setDescription("Enables group structure synchronization from external source.");
    return attr;
  }
}
