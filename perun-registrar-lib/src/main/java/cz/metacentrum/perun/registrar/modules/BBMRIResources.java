package cz.metacentrum.perun.registrar.modules;

import com.google.common.base.Strings;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registration module for BBMRI Collections, Networks, canSERV services, etc.
 * Module:
 * 1. reads input with IDs of the resources and checks, whether groups representing resources exist
 * - uses the content of attribute RESOURCE_ID_ATTR_NAME as the name of the attribute containing IDs of the
 * resources on groups
 * - parses the root group, under which the groups representing specified resources are located
 * - if RESOURCE_ORIGIN_ENABLED_ATTR_NAME is set to true, the root group will be parsed from input from the form
 * (where input is select named "resourceOrigin")
 * - else if RESOURCES_ROOT_GROUP_ATTR_NAME is set, the root group will be parsed from the value of this attribute
 * - else the root group will be considered the target group of the application
 * 2. adds users to the appropriate groups
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class BBMRIResources extends DefaultRegistrarModule {

  private static final Logger log = LoggerFactory.getLogger(BBMRIResources.class);

  // field names
  private static final String RESOURCE_IDS = "resourceIds";
  private static final String RESOURCE_ORIGIN = "resourceOrigin";

  // configuration attributes
  private static final String RESOURCE_ID_ATTR_NAME = "urn:perun:group:attribute-def:def:resourceIDAttrName";
  private static final String RESOURCE_ORIGIN_ENABLED_ATTR_NAME =
      "urn:perun:group:attribute-def:def:resourceOriginEnabled";
  private static final String RESOURCES_ROOT_GROUP_ATTR_NAME = "urn:perun:group:attribute-def:def:resourcesRootGroup";

  /**
   * Finds groups representing resources by provided input, adds user into these groups and removes from the
   * group where this application form is used.
   *
   * @param session who approves the application
   * @param app     application
   * @return unchanged application
   */
  @Override
  public Application approveApplication(PerunSession session, Application app)
      throws VoNotExistsException, UserNotExistsException, PrivilegeException,
      MemberNotExistsException, RegistrarException, GroupNotExistsException,
      AttributeNotExistsException, WrongAttributeAssignmentException, ExternallyManagedException,
      WrongAttributeValueException, WrongReferenceAttributeValueException, NotGroupMemberException {
    if (app.getGroup() == null) {
      throw new RegistrarException(
          "Invalid usage of registrar module - module '" + this.getClass().getName() +
              "' should be used on group level only");
    }

    // get perun and beans from session
    PerunBl perun = (PerunBl) session.getPerun();
    Vo vo = app.getVo();
    User user = app.getUser();
    Member member;
    try {
      member = perun.getMembersManagerBl().getMemberByUser(session, vo, user);
    } catch (MemberNotExistsException ex) {
      log.error("User {} is not member in the VO {}", user, vo);
      throw new RegistrarException("Cannot approve application - user is not member of the VO");
    }

    // get IDs of resources specified by the user
    Set<String> resourceIDsInApplication = getResourceIDsFromApplication(session, app);
    // get IDs of resources represented as groups in the system
    Map<String, Group> resourceIDToGroupMapsInSystem = getPerunResourceIdToGroupMap(session, app, perun);

    // add user to all groups from the field on application
    for (String resourceId : resourceIDsInApplication) {
      Group resource = resourceIDToGroupMapsInSystem.getOrDefault(resourceId, null);
      if (resource == null) {
        log.debug("There is no group for resource with ID: '{}'", resourceId);
      } else {
        try {
          perun.getGroupsManagerBl().addMember(session, resource, member);
        } catch (AlreadyMemberException ex) {
          // ignore
        }
      }
    }
    try {
      perun.getGroupsManagerBl().removeMember(session, app.getGroup(), member);
    } catch (NotGroupMemberException e) {
      //we can ignore this exception
    }

    return app;
  }

  /**
   * Checks whether all resource IDs found in user input really exists in Perun.
   * If not, CantBeApproved exception is thrown.
   *
   * @param session who approves the application
   * @param app     unchanged application
   * @throws CantBeApprovedException if at least one resource ID does not exist in Perun
   */
  @Override
  public void canBeApproved(PerunSession session, Application app) throws PerunException {
    // get perun and beans from session
    PerunBl perun = (PerunBl) session.getPerun();

    // get IDs of resources specified by the user
    Set<String> resourceIDsInApplication = getResourceIDsFromApplication(session, app);
    // get IDs of resources represented as groups in the system
    Set<String> resourceIDsInSystem = getPerunResourceIdToGroupMap(session, app, perun).keySet();
    // remove existing resources, so we get invalid inputs
    resourceIDsInApplication.removeAll(resourceIDsInSystem);

    // difference must be empty
    if (!resourceIDsInApplication.isEmpty()) {
      throw new CantBeApprovedException("Resources " + resourceIDsInApplication + " do not exist." +
          "If you approve the application, these resources will be skipped.", "", "", "", true, app.getId());
    }
  }

  /**
   * Gets Resources present in the system as a map of the Resource ID to the group representing particular resource.
   *
   * @return Map of String to Group, where key is the ID of the resource and Group is the representation
   */
  private Map<String, Group> getPerunResourceIdToGroupMap(PerunSession session, Application app, PerunBl perun)
      throws PrivilegeException, RegistrarException, WrongAttributeAssignmentException,
      AttributeNotExistsException {
    // get root group for resources hierarchy
    Group resourceOriginGroup = getResourceOriginGroup(session, app, perun);
    // get name of the attribute (stored in RESOURCE_ID_ATTR_NAME attribute) containing ID of the resource
    String resourceIdAttributeName = getResourceIdAttributeName(session, app, perun);

    // get map of ResourceID -> group (representing resource)
    return getResourceIDsToGroupsMap(session, perun, resourceOriginGroup, resourceIdAttributeName);
  }

  /**
   * Gets name of the attribute containing Resource IDs. This attribute should be used at the groups
   * identifying resources.
   *
   * @return name of the attribute where ID of the resource is stored, null if value is not set
   */
  private String getResourceIdAttributeName(PerunSession session, Application app, PerunBl perun)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    return perun.getAttributesManagerBl()
        .getAttribute(session, app.getGroup(), RESOURCE_ID_ATTR_NAME)
        .valueAsString();
  }

  /**
   * Gets root group, under which subgroups representing resources are placed.
   *
   * @return resource IDs set
   */
  private Group getResourceOriginGroup(PerunSession session, Application app, PerunBl perun)
      throws PrivilegeException, RegistrarException {
    try {
      Boolean resourceOriginEnabled = perun.getAttributesManagerBl()
          .getAttribute(session, app.getGroup(), RESOURCE_ORIGIN_ENABLED_ATTR_NAME)
          .valueAsBoolean();
      if (resourceOriginEnabled != null && resourceOriginEnabled) {
        try {
          String resourceOriginGroupName = getResourceOriginGroupNameFromApplication(session, app);
          return perun.getGroupsManagerBl().getGroupByName(session, app.getVo(), resourceOriginGroupName);
        } catch (GroupNotExistsException e) {
          throw new InternalErrorException("Target group does not exist");
        }
      }
    } catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
      //OK, we consider it as disabled, try manually configured resource root group
    }

    try {
      try {
        String resourceOriginGroupName = perun.getAttributesManagerBl()
            .getAttribute(session, app.getGroup(), RESOURCES_ROOT_GROUP_ATTR_NAME)
            .valueAsString();
        return perun.getGroupsManagerBl().getGroupByName(session, app.getVo(), resourceOriginGroupName);
      } catch (GroupNotExistsException e) {
        throw new InternalErrorException("Target group does not exist");
      }
    } catch (AttributeNotExistsException | WrongAttributeAssignmentException exc) {
      // OK, root will be the app group
    }

    return app.getGroup();
  }

  /**
   * Gets name of target group, where subgroups representing resources are placed.
   *
   * @return resource IDs set
   */
  private String getResourceOriginGroupNameFromApplication(PerunSession session, Application app)
      throws RegistrarException, PrivilegeException {
    String resourceOriginGroupName = null;
    List<ApplicationFormItemData> formData = registrar.getApplicationDataById(session, app.getId());
    for (ApplicationFormItemData field : formData) {
      if (RESOURCE_ORIGIN.equals(field.getShortname())) {
        resourceOriginGroupName = field.getValue();
        break;
      }
    }

    if (resourceOriginGroupName == null) {
      throw new InternalErrorException("There is no field with target group name on the registration form.");
    }

    return resourceOriginGroupName;
  }

  /**
   * Gets resource IDs from a field on the application form with short name.
   *
   * @return resource IDs set
   */
  private Set<String> getResourceIDsFromApplication(PerunSession session, Application app)
      throws RegistrarException, PrivilegeException {
    String resourceIdsString = null;
    List<ApplicationFormItemData> formData = registrar.getApplicationDataById(session, app.getId());
    for (ApplicationFormItemData field : formData) {
      if (RESOURCE_IDS.equals(field.getShortname())) {
        resourceIdsString = field.getValue();
        break;
      }
    }

    if (resourceIdsString == null) {
      throw new InternalErrorException("There is no field with resource IDs on the registration form.");
    }

    // get set of resource IDs from application
    Set<String> resourceIDsInApplication = new HashSet<>();
    for (String resource : resourceIdsString.split("[,\n ]+")) {
      resourceIDsInApplication.add(resource.trim());
    }

    return resourceIDsInApplication;
  }

  /**
   * Gets resources as map of resourceID => Group.
   *
   * @return Map of resource IDs to group.
   */
  private Map<String, Group> getResourceIDsToGroupsMap(PerunSession session,
                                                       PerunBl perun,
                                                       Group resourceOriginGroup,
                                                       String resourceIdContainingAttribute)
      throws WrongAttributeAssignmentException, AttributeNotExistsException {
    Map<String, Group> resourceIDsToGroupMap = new HashMap<>();

    List<Group> resourceGroups = perun.getGroupsManagerBl().getAllSubGroups(session, resourceOriginGroup);
    if (resourceGroups == null || resourceGroups.isEmpty()) {
      log.debug("No resource groups found, returning empty map.");
      return resourceIDsToGroupMap;
    }

    for (Group resourceGroup : resourceGroups) {
      Attribute resourceIDAttr = perun.getAttributesManagerBl()
          .getAttribute(session, resourceGroup, resourceIdContainingAttribute);

      if (resourceIDAttr == null || Strings.isNullOrEmpty(resourceIDAttr.valueAsString())) {
        log.warn("Found resource group ({}) without value in attr {}: ({})",
            resourceGroup, resourceIdContainingAttribute, resourceIDAttr);
      } else {
        resourceIDsToGroupMap.put(resourceIDAttr.valueAsString(), resourceGroup);
      }
    }

    return resourceIDsToGroupMap;
  }

}
