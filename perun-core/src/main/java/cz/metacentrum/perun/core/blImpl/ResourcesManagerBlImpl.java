package cz.metacentrum.perun.core.blImpl;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanRemovedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanSetForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.BanUpdatedForResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.GroupAssignedToResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.GroupRemovedFromResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ResourceCreated;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ResourceDeleted;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ResourceSelfServiceAddedForGroup;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ResourceSelfServiceRemovedForGroup;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ResourceSelfServiceRemovedForUser;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ResourceUpdated;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ServiceAssignedToResource;
import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ServiceRemovedFromResource;
import cz.metacentrum.perun.core.api.AssignedGroup;
import cz.metacentrum.perun.core.api.AssignedMember;
import cz.metacentrum.perun.core.api.AssignedResource;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.EnrichedBanOnResource;
import cz.metacentrum.perun.core.api.EnrichedResource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupResourceAssignment;
import cz.metacentrum.perun.core.api.GroupResourceStatus;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceStatusException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeSetException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ResourcesManagerBl;
import cz.metacentrum.perun.core.implApi.ResourcesManagerImplApi;
import cz.metacentrum.perun.taskslib.model.Task;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

/**
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class ResourcesManagerBlImpl implements ResourcesManagerBl {

  static final Logger LOG = LoggerFactory.getLogger(ResourcesManagerBlImpl.class);

  private final ResourcesManagerImplApi resourcesManagerImpl;
  private PerunBl perunBl;

  public ResourcesManagerBlImpl(ResourcesManagerImplApi resourcesManagerImpl) {
    this.resourcesManagerImpl = resourcesManagerImpl;
  }

  @Override
  public void activateGroupResourceAssignment(PerunSession sess, Group group, Resource resource, boolean async)
      throws WrongReferenceAttributeValueException, GroupResourceMismatchException, WrongAttributeValueException,
      GroupNotDefinedOnResourceException {
    GroupResourceStatus status = getResourcesManagerImpl().getGroupResourceStatus(sess, group, resource);
    if (status == GroupResourceStatus.ACTIVE) {
      // silently skip
      return;
    }

    if (async) {
      getResourcesManagerImpl().setGroupResourceStatus(sess, group, resource, GroupResourceStatus.PROCESSING);
      getPerunBl().getResourcesManagerBl().processGroupResourceActivationAsync(sess, group, resource);
    } else {
      processGroupResourceActivation(sess, group, resource);
    }
  }

  @Override
  public void addResourceSelfServiceGroup(PerunSession sess, Resource resource, Group group)
      throws AlreadyAdminException {
    try {
      AuthzResolverBlImpl.setRole(sess, group, resource, Role.RESOURCESELFSERVICE);
    } catch (RoleCannotBeManagedException | RoleCannotBeSetException e) {
      throw new InternalErrorException(e);
    }
    getPerunBl().getAuditer().log(sess, new ResourceSelfServiceAddedForGroup(resource, group));
  }

  @Override
  public void addResourceSelfServiceUser(PerunSession sess, Resource resource, User user) throws AlreadyAdminException {
    try {
      AuthzResolverBlImpl.setRole(sess, user, resource, Role.RESOURCESELFSERVICE);
    } catch (RoleCannotBeManagedException | RoleCannotBeSetException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void assignAutomaticGroupToResource(PerunSession perunSession, Group sourceGroup, Group groupToAssign,
                                             Resource resource)
      throws GroupResourceMismatchException, GroupAlreadyAssignedException, WrongReferenceAttributeValueException,
      WrongAttributeValueException {
    getPerunBl().getAttributesManagerBl().checkGroupIsFromTheSameVoLikeResource(perunSession, sourceGroup, resource);

    getResourcesManagerImpl().assignAutomaticGroupToResource(perunSession, groupToAssign, resource, sourceGroup);

    // subgroups should not be assigned as inactive, thus 'assignInactive' flag is always false
    setAssignedGroupStatusAndActivate(perunSession, resource, true, false, groupToAssign);
  }

  @Override
  public void assignGroupToResource(PerunSession sess, Group group, Resource resource, boolean async,
                                    boolean assignInactive, boolean autoAssignSubgroups)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
    assignGroupsToResource(sess, Collections.singletonList(group), resource, async, assignInactive,
        autoAssignSubgroups);
  }

  @Override
  public void assignGroupToResources(PerunSession perunSession, Group group, List<Resource> resources, boolean async,
                                     boolean assignInactive, boolean autoAssignSubgroups)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
    for (Resource r : resources) {
      this.assignGroupToResource(perunSession, group, r, async, assignInactive, autoAssignSubgroups);
    }
  }

  @Override
  public void assignGroupsToResource(PerunSession perunSession, Iterable<Group> groups, Resource resource,
                                     boolean async, boolean assignInactive, boolean autoAssignSubgroups)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
    for (Group g : groups) {
      getPerunBl().getAttributesManagerBl().checkGroupIsFromTheSameVoLikeResource(perunSession, g, resource);

      // assign source group
      try {
        getResourcesManagerImpl().assignGroupToResource(perunSession, g, resource, autoAssignSubgroups);
        setAssignedGroupStatusAndActivate(perunSession, resource, async, assignInactive, g);
      } catch (GroupAlreadyAssignedException e) {
        // silently skip
      }

      if (autoAssignSubgroups) {
        GroupsManagerBl groupsManager = getPerunBl().getGroupsManagerBl();
        List<Group> subgroups;

        // If it is 'members' group, use all groups from corresponding VO (except for members, they've been assigned
        // already)
        if (g.getName().equals(VosManager.MEMBERS_GROUP)) {
          subgroups = groupsManager.getAllGroups(perunSession, groupsManager.getVo(perunSession, g));
          subgroups.removeIf((group -> group.getName().equals(VosManager.MEMBERS_GROUP)));
        } else {
          subgroups = groupsManager.getAllSubGroups(perunSession, g);
        }

        for (Group subgroup : subgroups) {

          try {
            assignAutomaticGroupToResource(perunSession, g, subgroup, resource);
          } catch (GroupAlreadyAssignedException e) {
            // silently skip
          }
        }
      }
    }
  }

  @Override
  public void assignResourceTagToResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource)
      throws ResourceTagAlreadyAssignedException {
    List<ResourceTag> allResourceTags = this.getAllResourcesTagsForResource(perunSession, resource);
    if (allResourceTags.contains(resourceTag)) {
      throw new ResourceTagAlreadyAssignedException(resourceTag);
    }
    getResourcesManagerImpl().assignResourceTagToResource(perunSession, resourceTag, resource);
  }

  @Override
  public void assignResourceTagsToResource(PerunSession perunSession, List<ResourceTag> resourceTags, Resource resource)
      throws ResourceTagAlreadyAssignedException {
    for (ResourceTag tag : resourceTags) {
      assignResourceTagToResource(perunSession, tag, resource);
    }
  }

  @Override
  public void assignService(PerunSession sess, Resource resource, Service service)
      throws ServiceAlreadyAssignedException, WrongAttributeValueException, WrongReferenceAttributeValueException {
    assignServices(sess, resource, Collections.singletonList(service));
  }

  @Override
  public void assignServices(PerunSession sess, Resource resource, List<Service> services)
      throws ServiceAlreadyAssignedException, WrongAttributeValueException, WrongReferenceAttributeValueException {
    for (Service service : services) {
      getResourcesManagerImpl().assignService(sess, resource, service);
      getPerunBl().getAuditer().log(sess, new ServiceAssignedToResource(service, resource));
    }

    boolean requiresAttributes = services.stream()
        .anyMatch(s -> !getPerunBl().getAttributesManagerBl().getRequiredAttributesDefinition(sess, s).isEmpty());

    if (!requiresAttributes) {
      // there are new no attributes to check or add
      return;
    }

    try {
      fillAndSetRequiredAttributesForGroups(sess, services, resource);

      checkSemanticsOfFacilityAndResourceRequiredAttributes(sess, resource);

      updateAllRequiredAttributesForAllowedMembers(sess, resource, services);
    } catch (WrongAttributeAssignmentException | GroupResourceMismatchException | MemberResourceMismatchException |
             AttributeNotExistsException e) {
      throw new ConsistencyErrorException(e);
    }
  }

  @Override
  public boolean banExists(PerunSession sess, int memberId, int resourceId) {
    return getResourcesManagerImpl().banExists(sess, memberId, resourceId);
  }

  @Override
  public boolean banExists(PerunSession sess, int banId) {
    return getResourcesManagerImpl().banExists(sess, banId);
  }

  @Override
  public void checkBanExists(PerunSession sess, int memberId, int resourceId) throws BanNotExistsException {
    if (!getResourcesManagerImpl().banExists(sess, memberId, resourceId)) {
      throw new BanNotExistsException("Ban for member " + memberId + " and resource " + resourceId + " not exists!");
    }
  }

  @Override
  public void checkBanExists(PerunSession sess, int banId) throws BanNotExistsException {
    if (!getResourcesManagerImpl().banExists(sess, banId)) {
      throw new BanNotExistsException("Ban with id " + banId + " not exists!");
    }
  }

  @Override
  public void checkResourceExists(PerunSession sess, Resource resource) throws ResourceNotExistsException {
    getResourcesManagerImpl().checkResourceExists(sess, resource);
  }

  @Override
  public void checkResourceTagExists(PerunSession sess, ResourceTag resourceTag) throws ResourceTagNotExistsException {
    getResourcesManagerImpl().checkResourceTagExists(sess, resourceTag);
  }

  /**
   * Checks semantics of all required attributes of given resource and its facility.
   *
   * @param sess     session
   * @param resource resource used to get facility and attributes
   * @throws WrongReferenceAttributeValueException WrongReferenceAttributeValueException
   * @throws WrongAttributeAssignmentException     WrongAttributeAssignmentException
   */
  public void checkSemanticsOfFacilityAndResourceRequiredAttributes(PerunSession sess, Resource resource)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    AttributesManagerBl attributesManagerBl = getPerunBl().getAttributesManagerBl();
    Facility facility = getFacility(sess, resource);

    List<Attribute> facilityRequiredAttributes = attributesManagerBl.getRequiredAttributes(sess, facility);
    List<Attribute> resourceRequiredAttributes = attributesManagerBl.getRequiredAttributes(sess, resource);

    attributesManagerBl.checkAttributesSemantics(sess, facility, facilityRequiredAttributes);
    attributesManagerBl.checkAttributesSemantics(sess, resource, resourceRequiredAttributes);
  }

  @Override
  public EnrichedResource convertToEnrichedResource(PerunSession sess, Resource resource, List<String> attrNames) {
    List<Attribute> attributes;
    if (attrNames == null || attrNames.isEmpty()) {
      attributes = perunBl.getAttributesManagerBl().getAttributes(sess, resource);
    } else {
      attributes = perunBl.getAttributesManagerBl().getAttributes(sess, resource, attrNames);
    }
    return new EnrichedResource(resource, attributes);
  }

  @Override
  public void copyAttributes(PerunSession sess, Resource sourceResource, Resource destinationResource)
      throws WrongReferenceAttributeValueException {
    List<Attribute> sourceAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, sourceResource);
    List<Attribute> destinationAttributes =
        getPerunBl().getAttributesManagerBl().getAttributes(sess, destinationResource);

    // do not get virtual attributes from source resource, they can't be set to destination
    sourceAttributes.removeIf(
        attribute -> attribute.getNamespace().startsWith(AttributesManager.NS_RESOURCE_ATTR_VIRT));

    // create intersection of destination and source attributes
    List<Attribute> intersection = new ArrayList<>(destinationAttributes);
    intersection.retainAll(sourceAttributes);

    try {
      // delete all common attributes from destination resource
      getPerunBl().getAttributesManagerBl().removeAttributes(sess, destinationResource, intersection);
      // add all attributes from the source resource to the destination resource
      getPerunBl().getAttributesManagerBl().setAttributes(sess, destinationResource, sourceAttributes);
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException("Copying of attributes failed, wrong assignment.", ex);
    } catch (WrongAttributeValueException ex) {
      throw new ConsistencyErrorException("Copying of attributes failed.", ex);
    }

  }

  @Override
  public void copyGroups(PerunSession sess, Resource sourceResource, Resource destinationResource) {
    for (AssignedGroup group : getGroupAssignments(sess, sourceResource, List.of())) {
      try {

        //assign only direct group-resource assignments
        if (group.getSourceGroupId() == null) {
          assignGroupToResource(sess, group.getEnrichedGroup().getGroup(), destinationResource, false,
              group.getStatus().equals(GroupResourceStatus.INACTIVE), group.isAutoAssignSubgroups());
        }
      } catch (GroupResourceMismatchException ex) {
        // we can ignore the exception in this particular case, group can exists in both of the resources
      } catch (WrongAttributeValueException | WrongReferenceAttributeValueException ex) {
        throw new InternalErrorException("Copying of groups failed.", ex);
      }
    }
  }

  @Override
  public Resource copyResource(PerunSession sess, Resource templateResource, Resource destinationResource,
                               boolean withGroups) throws ResourceExistsException {
    Resource newResource = new Resource();
    Vo destinationVo = this.getVo(sess, destinationResource);
    Facility destinationFacility = this.getFacility(sess, destinationResource);

    newResource.setName(destinationResource.getName());
    newResource = this.createResource(sess, newResource, destinationVo, destinationFacility);

    //resource attributes
    List<Attribute> templateResourceAttributes = perunBl.getAttributesManagerBl().getAttributes(sess, templateResource);
    //Remove all virt and core attributes before setting
    templateResourceAttributes.removeIf(
        resourceAttribute -> resourceAttribute.getNamespace().startsWith(AttributesManager.NS_RESOURCE_ATTR_VIRT) ||
                             resourceAttribute.getNamespace().startsWith(AttributesManager.NS_RESOURCE_ATTR_CORE));
    try {
      perunBl.getAttributesManagerBl().setAttributes(sess, newResource, templateResourceAttributes);
    } catch (WrongAttributeValueException | WrongAttributeAssignmentException |
             WrongReferenceAttributeValueException ex) {
      throw new ConsistencyErrorException(
          "DB inconsistency while copying attributes from one resource to another. Cause:{}", ex);
    }

    //if withGroups is true we also copy groups and group-resource/member-resource attributes
    if (withGroups) {
      List<AssignedGroup> templateResourceGroups =
          perunBl.getResourcesManagerBl().getGroupAssignments(sess, templateResource, List.of());
      try {
        for (AssignedGroup assignedGroup : templateResourceGroups) {

          //assign only direct group-resource assignments
          if (assignedGroup.getSourceGroupId() == null) {
            assignGroupToResource(sess, assignedGroup.getEnrichedGroup().getGroup(), newResource, false,
                assignedGroup.getStatus().equals(GroupResourceStatus.INACTIVE), assignedGroup.isAutoAssignSubgroups());
          }

          List<Attribute> templateGroupResourceAttributes = perunBl.getAttributesManagerBl()
              .getAttributes(sess, templateResource, assignedGroup.getEnrichedGroup().getGroup());
          //Remove all virt attributes before setting
          templateGroupResourceAttributes.removeIf(groupResourceAttribute -> groupResourceAttribute.getNamespace()
              .startsWith(AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT));
          perunBl.getAttributesManagerBl().setAttributes(sess, newResource, assignedGroup.getEnrichedGroup().getGroup(),
              templateGroupResourceAttributes);
        }
      } catch (GroupResourceMismatchException | WrongAttributeValueException | WrongAttributeAssignmentException |
               WrongReferenceAttributeValueException ex) {
        throw new ConsistencyErrorException("DB inconsistency while copying group-resource attributes. Cause:{}", ex);
      }

      List<Member> templateResourceMembers = perunBl.getResourcesManagerBl().getAssignedMembers(sess, templateResource);
      try {
        for (Member member : templateResourceMembers) {
          List<Attribute> templateMemberResourceAttributes =
              perunBl.getAttributesManagerBl().getAttributes(sess, member, templateResource);
          //Remove all virt attributes before setting
          templateMemberResourceAttributes.removeIf(memberResourceAttribute -> memberResourceAttribute.getNamespace()
              .startsWith(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT));
          perunBl.getAttributesManagerBl().setAttributes(sess, member, newResource, templateMemberResourceAttributes);
        }
      } catch (MemberResourceMismatchException | WrongAttributeValueException | WrongAttributeAssignmentException |
               WrongReferenceAttributeValueException ex) {
        throw new ConsistencyErrorException("DB inconsistency while copying group-resource attributes. Cause:{}", ex);
      }
    }

    //services
    List<Service> services = getAssignedServices(sess, templateResource);
    for (Service service : services) {
      try {
        getResourcesManagerImpl().assignService(sess, newResource, service);
      } catch (ServiceAlreadyAssignedException ex) {
        throw new ConsistencyErrorException("Service was already assigned to this resource. {}", ex);
      }
    }

    //tags
    List<ResourceTag> templateResourceTags = getAllResourcesTagsForResource(sess, templateResource);
    for (ResourceTag resourceTag : templateResourceTags) {
      getResourcesManagerImpl().assignResourceTagToResource(sess, resourceTag, newResource);
    }

    return newResource;
  }

  @Override
  public void copyServices(PerunSession sess, Resource sourceResource, Resource destinationResource)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException {
    for (Service owner : getAssignedServices(sess, sourceResource)) {
      try {
        assignService(sess, destinationResource, owner);
      } catch (ServiceAlreadyAssignedException ex) {
        // we can ignore the exception in this particular case, service can exists in both of the resources
      }
    }
  }

  @Override
  public Resource createResource(PerunSession sess, Resource resource, Vo vo, Facility facility)
      throws ResourceExistsException {
    try {
      Resource existingResource = getResourcesManagerImpl().getResourceByName(sess, vo, facility, resource.getName());
      throw new ResourceExistsException(existingResource);
    } catch (ResourceNotExistsException e) {
      resource = getResourcesManagerImpl().createResource(sess, vo, resource, facility);
      getPerunBl().getAuditer().log(sess, new ResourceCreated(resource));
    }

    return resource;
  }

  @Override
  public ResourceTag createResourceTag(PerunSession perunSession, ResourceTag resourceTag, Vo vo) {
    return getResourcesManagerImpl().createResourceTag(perunSession, resourceTag, vo);
  }

  @Override
  public void deactivateGroupResourceAssignment(PerunSession sess, Group group, Resource resource)
      throws GroupNotDefinedOnResourceException, GroupResourceStatusException {
    GroupResourceStatus status = getResourcesManagerImpl().getGroupResourceStatus(sess, group, resource);
    if (status == GroupResourceStatus.PROCESSING) {
      throw new GroupResourceStatusException("Cannot deactivate an assignment in PROCESSING state.");
    }

    getResourcesManagerImpl().setGroupResourceStatus(sess, group, resource, GroupResourceStatus.INACTIVE);
    getPerunBl().getAuditer().log(sess, new GroupRemovedFromResource(group, resource));

    // reset assignment failure cause
    getResourcesManagerImpl().setFailedGroupResourceAssignmentCause(sess, group, resource, null);
  }

  @Override
  public void deleteAllResources(PerunSession sess, Vo vo)
      throws ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
    for (Resource r : this.getResources(sess, vo)) {
      deleteResource(sess, r);
    }
  }

  @Override
  public void deleteAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws ResourceTagAlreadyAssignedException {
    List<ResourceTag> resourcesTagForVo = this.getAllResourcesTagsForVo(perunSession, vo);
    for (ResourceTag rt : resourcesTagForVo) {
      List<Resource> tagResources = this.getAllResourcesByResourceTag(perunSession, rt);
      if (!tagResources.isEmpty()) {
        throw new ResourceTagAlreadyAssignedException("The resourceTag is alreadyUsed for some resources.", rt);
      }
    }
    getResourcesManagerImpl().deleteAllResourcesTagsForVo(perunSession, vo);
  }

  @Override
  public void deleteResource(PerunSession sess, Resource resource)
      throws ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
    //Get facility for audit messages
    Facility facility = this.getFacility(sess, resource);

    //remove admins of this resource
    List<Group> adminGroups = getResourcesManagerImpl().getAdminGroups(sess, resource);
    for (Group adminGroup : adminGroups) {
      try {
        AuthzResolverBlImpl.unsetRole(sess, adminGroup, resource, Role.RESOURCEADMIN);
      } catch (GroupNotAdminException e) {
        LOG.warn("When trying to unsetRole ResourceAdmin for group {} in the resource {} the exception was thrown {}",
            adminGroup, resource, e);
        //skip and log as warning
      } catch (RoleCannotBeManagedException e) {
        throw new InternalErrorException(e);
      }
    }

    List<User> adminUsers = getResourcesManagerImpl().getAdmins(sess, resource);

    for (User adminUser : adminUsers) {
      try {
        AuthzResolverBlImpl.unsetRole(sess, adminUser, resource, Role.RESOURCEADMIN);
      } catch (UserNotAdminException e) {
        LOG.warn("When trying to unsetRole ResourceAdmin for user {} in the resource {} the exception was thrown {}",
            adminUser, resource, e);
        //skip and log as warning
      } catch (RoleCannotBeManagedException e) {
        throw new InternalErrorException(e);
      }
    }

    // Remove binding between resource and service
    List<Service> services = getAssignedServices(sess, resource);
    for (Service service : services) {
      try {
        this.removeService(sess, resource, service);
      } catch (ServiceNotAssignedException e) {
        throw new ConsistencyErrorException(e);
      }
    }

    List<AssignedGroup> assignedGroups = getGroupAssignments(sess, resource, List.of());
    for (AssignedGroup assignedGroup : assignedGroups) {
      if (assignedGroup.getSourceGroupId() == null) {
        try {
          removeGroupFromResource(sess, assignedGroup.getEnrichedGroup().getGroup(), resource);
        } catch (GroupNotDefinedOnResourceException ex) {
          throw new GroupAlreadyRemovedFromResourceException(ex);
        }
      }
    }

    // Remove attr values for the resource
    try {
      perunBl.getAttributesManagerBl().removeAllAttributes(sess, resource);
    } catch (AttributeValueException ex) {
      throw new ConsistencyErrorException(
          "All services are removed from this resource. There is no required attribute. So all attribtes for this " +
          "resource can be removed withou problem.", ex);
    }

    // Remove group-resource attr values for all group and resource
    try {
      this.perunBl.getAttributesManagerBl().removeAllGroupResourceAttributes(sess, resource);
    } catch (WrongAttributeValueException | GroupResourceMismatchException | WrongReferenceAttributeValueException ex) {
      throw new InternalErrorException(ex);
    }
    //Remove all resources tags
    this.removeAllResourcesTagFromResource(sess, resource);

    //Remove all resource bans
    List<BanOnResource> bansOnResource = this.getBansForResource(sess, resource.getId());
    for (BanOnResource banOnResource : bansOnResource) {
      try {
        this.removeBan(sess, banOnResource.getId());
      } catch (BanNotExistsException ex) {
        //it is ok, we just want to remove it anyway
      }
    }

    //Because resource will be tottaly deleted, we can also delete all member-resource attributes
    this.perunBl.getAttributesManagerBl().removeAllMemberResourceAttributes(sess, resource);

    // Get the resource VO
    Vo vo = this.getVo(sess, resource);
    getResourcesManagerImpl().deleteResource(sess, vo, resource);
    getPerunBl().getAuditer().log(sess, new ResourceDeleted(resource, facility));
  }

  @Override
  public void deleteResourceTag(PerunSession perunSession, ResourceTag resourceTag)
      throws ResourceTagAlreadyAssignedException {
    List<Resource> tagResources = this.getAllResourcesByResourceTag(perunSession, resourceTag);
    if (!tagResources.isEmpty()) {
      throw new ResourceTagAlreadyAssignedException("The resourceTag is alreadyUsed for some resources.", resourceTag);
    }
    getResourcesManagerImpl().deleteResourceTag(perunSession, resourceTag);
  }

  /**
   * Fill and set group and group-resource attributes required by given services for all groups which are assigned to
   * the given resource.
   *
   * @param sess     session
   * @param services services of which required attributes are set
   * @param resource resource
   * @throws GroupResourceMismatchException        GroupResourceMismatchException
   * @throws WrongAttributeAssignmentException     WrongAttributeAssignmentException
   * @throws WrongAttributeValueException          WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException WrongReferenceAttributeValueException
   */
  public void fillAndSetRequiredAttributesForGroups(PerunSession sess, List<Service> services, Resource resource)
      throws GroupResourceMismatchException, WrongAttributeAssignmentException, WrongAttributeValueException,
      WrongReferenceAttributeValueException {
    AttributesManagerBl attributesManagerBl = getPerunBl().getAttributesManagerBl();
    List<Group> groups = getAssignedGroups(sess, resource);
    for (Group group : groups) {
      List<Attribute> attributes;
      attributes = attributesManagerBl.getRequiredAttributes(sess, services, resource, group, true);
      attributes = attributesManagerBl.fillAttributes(sess, resource, group, attributes, true);
      attributesManagerBl.setAttributes(sess, resource, group, attributes, true);
    }
  }

  @Override
  public EnrichedResource filterOnlyAllowedAttributes(PerunSession sess, EnrichedResource enrichedResource) {
    enrichedResource.setAttributes(AuthzResolverBlImpl.filterNotAllowedAttributes(sess, enrichedResource.getResource(),
        enrichedResource.getAttributes()));
    return enrichedResource;
  }

  @Override
  public List<Group> getAdminGroups(PerunSession sess, Resource resource) {
    return resourcesManagerImpl.getAdminGroups(sess, resource);
  }

  @Override
  public List<User> getAdmins(PerunSession perunSession, Resource resource, boolean onlyDirectAdmins) {
    if (onlyDirectAdmins) {
      return getResourcesManagerImpl().getDirectAdmins(perunSession, resource);
    } else {
      return getResourcesManagerImpl().getAdmins(perunSession, resource);
    }
  }

  @Override
  public List<BanOnResource> getAllExpiredBansOnResources(PerunSession sess) {
    return getResourcesManagerImpl().getAllExpiredBansOnResources(sess);
  }

  @Override
  public List<Resource> getAllResources(PerunSession sess) {
    return getResourcesManagerImpl().getAllResources(sess);
  }

  @Override
  public List<Resource> getAllResourcesByResourceTag(PerunSession perunSession, ResourceTag resourceTag) {
    return getResourcesManagerImpl().getAllResourcesByResourceTag(perunSession, resourceTag);
  }

  @Override
  public List<ResourceTag> getAllResourcesTagsForResource(PerunSession perunSession, Resource resource) {
    return getResourcesManagerImpl().getAllResourcesTagsForResource(perunSession, resource);
  }

  @Override
  public List<ResourceTag> getAllResourcesTagsForVo(PerunSession perunSession, Vo vo) {
    return getResourcesManagerImpl().getAllResourcesTagsForVo(perunSession, vo);
  }

  @Override
  public List<Member> getAllowedMembers(PerunSession sess, Resource resource) {
    return getResourcesManagerImpl().getAllowedMembers(sess, resource);
  }

  @Override
  public List<Member> getAllowedMembersNotExpiredInGroups(PerunSession sess, Resource resource) {
    return getResourcesManagerImpl().getAllowedMembersNotExpiredInGroup(sess, resource);
  }

  @Override
  public List<Resource> getAllowedResources(PerunSession sess, Member member) {
    if (!getPerunBl().getMembersManagerBl().haveStatus(sess, member, Status.INVALID) &&
        !getPerunBl().getMembersManagerBl().haveStatus(sess, member, Status.DISABLED)) {
      return getAssignedResources(sess, member);
    } else {
      return new ArrayList<>();
    }
  }

  @Override
  public List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) {
    return getResourcesManagerImpl().getAllowedResources(sess, facility, user);
  }

  @Override
  public List<User> getAllowedUsers(PerunSession sess, Resource resource) {
    return getResourcesManagerImpl().getAllowedUsers(sess, resource);
  }

  @Override
  public List<User> getAllowedUsersNotExpiredInGroups(PerunSession sess, Resource resource) {
    return getResourcesManagerImpl().getAllowedUsersNotExpiredInGroup(sess, resource);
  }

  @Override
  public List<Group> getAssignedGroups(PerunSession sess, Resource resource) {
    return getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resource);
  }

  @Override
  public List<Group> getAssignedGroups(PerunSession sess, Resource resource, Member member) {
    return getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resource, member);
  }

  @Override
  public List<Member> getAssignedMembers(PerunSession sess, Resource resource) {
    return getResourcesManagerImpl().getAssignedMembers(sess, resource);
  }

  @Override
  public List<AssignedMember> getAssignedMembersWithStatus(PerunSession sess, Resource resource) {
    List<AssignedMember> assignedMembers = getResourcesManagerImpl().getAssignedMembersWithStatus(sess, resource);

    // fill incomplete RichMembers
    assignedMembers.forEach(a -> a.setRichMember(
        getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, List.of(a.getRichMember())).get(0)));

    return assignedMembers;

  }

  @Override
  public List<Resource> getAssignedResources(PerunSession sess, Group group) {
    return getResourcesManagerImpl().getAssignedResources(sess, group);
  }

  @Override
  public List<Resource> getAssignedResources(PerunSession sess, Member member) {
    return getResourcesManagerImpl().getAssignedResources(sess, member);
  }

  @Override
  public List<Resource> getAssignedResources(PerunSession sess, Member member, Service service) {
    return getResourcesManagerImpl().getAssignedResources(sess, member, service);
  }

  @Override
  public List<Resource> getAssignedResources(PerunSession sess, User user, Vo vo) {
    return getResourcesManagerImpl().getAssignedResources(sess, user, vo);
  }

  @Override
  public List<AssignedResource> getAssignedResourcesWithStatus(PerunSession sess, Member member) {
    return getResourcesManagerImpl().getAssignedResourcesWithStatus(sess, member);
  }

  @Override
  public List<RichMember> getAssignedRichMembers(PerunSession sess, Resource resource) {
    List<Member> listOfMembers = getResourcesManagerImpl().getAssignedMembers(sess, resource);
    return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, listOfMembers);
  }

  @Override
  public List<RichResource> getAssignedRichResources(PerunSession sess, Group group) {
    return getResourcesManagerImpl().getAssignedRichResources(sess, group);
  }

  @Override
  public List<RichResource> getAssignedRichResources(PerunSession sess, Member member) {
    return getResourcesManagerImpl().getAssignedRichResources(sess, member);
  }

  @Override
  public List<RichResource> getAssignedRichResources(PerunSession sess, Member member, Service service) {
    return getResourcesManagerImpl().getAssignedRichResources(sess, member, service);
  }

  @Override
  public List<Service> getAssignedServices(PerunSession sess, Resource resource) {
    return getResourcesManagerImpl().getAssignedServices(sess, resource);
  }

  @Override
  public List<User> getAssignedUsers(PerunSession sess, Resource resource) {
    return getResourcesManagerImpl().getAssignedUsers(sess, resource);
  }

  @Override
  public List<Group> getAssociatedGroups(PerunSession sess, Resource resource, Member member) {
    return getPerunBl().getGroupsManagerBl().getAssociatedGroupsToResource(sess, resource, member);
  }

  @Override
  public List<Member> getAssociatedMembers(PerunSession sess, Resource resource) {
    return getResourcesManagerImpl().getAssociatedMembers(sess, resource);
  }

  @Override
  public List<Resource> getAssociatedResources(PerunSession sess, Group group) {
    return getResourcesManagerImpl().getAssociatedResources(sess, group);
  }

  @Override
  public List<Resource> getAssociatedResources(PerunSession sess, Member member) {
    return getResourcesManagerImpl().getAssociatedResources(sess, member);
  }

  @Override
  public List<User> getAssociatedUsers(PerunSession sess, Resource resource) {
    return getResourcesManagerImpl().getAssociatedUsers(sess, resource);
  }

  @Override
  public BanOnResource getBan(PerunSession sess, int memberId, int resourceId) throws BanNotExistsException {
    return getResourcesManagerImpl().getBan(sess, memberId, resourceId);
  }

  @Override
  public BanOnResource getBanById(PerunSession sess, int banId) throws BanNotExistsException {
    return getResourcesManagerImpl().getBanById(sess, banId);
  }

  @Override
  public List<BanOnResource> getBansForMember(PerunSession sess, int memberId) {
    return getResourcesManagerImpl().getBansForMember(sess, memberId);
  }

  @Override
  public List<BanOnResource> getBansForResource(PerunSession sess, int resourceId) {
    return getResourcesManagerImpl().getBansForResource(sess, resourceId);
  }

  @Override
  public List<EnrichedBanOnResource> getEnrichedBansForResource(PerunSession sess, Resource resource,
                                                                List<String> attrNames)
      throws AttributeNotExistsException {
    List<BanOnResource> bans = getResourcesManagerImpl().getBansForResource(sess, resource.getId());
    List<EnrichedBanOnResource> enrichedBans = new ArrayList<>();
    List<AttributeDefinition> attrDefs = new ArrayList<>();

    if (attrNames != null && !attrNames.isEmpty()) {
      attrDefs = perunBl.getAttributesManagerBl().getAttributesDefinition(sess, attrNames);
    }

    for (BanOnResource ban : bans) {
      EnrichedBanOnResource newBan = new EnrichedBanOnResource();
      newBan.setBan(ban);
      newBan.setResource(resource);
      try {
        Member member = perunBl.getMembersManagerBl().getMemberById(sess, ban.getMemberId());
        RichMember richMember = perunBl.getMembersManagerBl().getRichMember(sess, member);
        if (attrDefs.isEmpty()) {
          richMember =
              perunBl.getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, List.of(richMember)).get(0);
        } else {
          richMember = perunBl.getMembersManagerBl()
              .convertMembersToRichMembersWithAttributes(sess, List.of(richMember), attrDefs).get(0);
        }
        newBan.setMember(richMember);
      } catch (MemberNotExistsException e) {
        // should not happen
      }
      enrichedBans.add(newBan);
    }

    return enrichedBans;
  }

  @Override
  public List<EnrichedBanOnResource> getEnrichedBansForUser(PerunSession sess, User user, List<String> attrNames)
      throws AttributeNotExistsException {
    List<Member> members = perunBl.getMembersManagerBl().getMembersByUser(sess, user);
    List<EnrichedBanOnResource> enrichedBans = new ArrayList<>();
    List<AttributeDefinition> attrDefs = new ArrayList<>();

    if (attrNames != null && !attrNames.isEmpty()) {
      attrDefs = perunBl.getAttributesManagerBl().getAttributesDefinition(sess, attrNames);
    }

    for (Member member : members) {
      List<BanOnResource> bans = getResourcesManagerImpl().getBansForMember(sess, member.getId());

      if (!bans.isEmpty()) {

        RichMember richMember = perunBl.getMembersManagerBl().getRichMember(sess, member);
        if (attrDefs.isEmpty()) {
          richMember =
              perunBl.getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, List.of(richMember)).get(0);
        } else {
          richMember = perunBl.getMembersManagerBl()
              .convertMembersToRichMembersWithAttributes(sess, List.of(richMember), attrDefs).get(0);
        }

        for (BanOnResource ban : bans) {
          EnrichedBanOnResource newBan = new EnrichedBanOnResource();
          newBan.setBan(ban);
          newBan.setMember(richMember);
          try {
            newBan.setResource(perunBl.getResourcesManagerBl().getResourceById(sess, ban.getResourceId()));
          } catch (ResourceNotExistsException e) {
            // should not happen
          }
          enrichedBans.add(newBan);
        }
      }
    }

    return enrichedBans;
  }

  @Override
  public EnrichedResource getEnrichedResourceById(PerunSession sess, int id, List<String> attrNames)
      throws ResourceNotExistsException {
    Resource resource = getResourceById(sess, id);
    return convertToEnrichedResource(sess, resource, attrNames);
  }

  @Override
  public List<EnrichedResource> getEnrichedRichResourcesForFacility(PerunSession sess, Facility facility,
                                                                    List<String> attrNames) {
    return perunBl.getFacilitiesManagerBl().getAssignedResources(sess, facility).stream()
        .map(resource -> convertToEnrichedResource(sess, resource, attrNames)).collect(Collectors.toList());
  }

  @Override
  public List<EnrichedResource> getEnrichedRichResourcesForVo(PerunSession sess, Vo vo, List<String> attrNames) {
    return getResources(sess, vo).stream().map(resource -> convertToEnrichedResource(sess, resource, attrNames))
        .collect(Collectors.toList());
  }

  @Override
  public Facility getFacility(PerunSession sess, Resource resource) {
    try {
      return getPerunBl().getFacilitiesManagerBl().getFacilityById(sess, resource.getFacilityId());
    } catch (FacilityNotExistsException e) {
      throw new ConsistencyErrorException("Resource doesn't have assigned facility", e);
    }
  }

  @Override
  public List<AssignedGroup> getGroupAssignments(PerunSession sess, Resource resource, List<String> attrNames) {
    List<AssignedGroup> assignedGroups = getResourcesManagerImpl().getGroupAssignments(sess, resource);

    // set group attributes in EnrichedGroup
    assignedGroups.forEach(assignedGroup -> assignedGroup.setEnrichedGroup(getPerunBl().getGroupsManagerBl()
        .convertToEnrichedGroup(sess, assignedGroup.getEnrichedGroup().getGroup(), attrNames)));

    return assignedGroups;
  }

  @Override
  public List<GroupResourceAssignment> getGroupResourceAssignments(PerunSession sess,
                                                                   List<GroupResourceStatus> statuses) {
    if (isEmpty(statuses)) {
      statuses = Arrays.asList(GroupResourceStatus.values());
    }

    return getResourcesManagerImpl().getGroupResourceAssignments(sess, statuses);
  }

  @Override
  public List<RichResource> getMailingServiceRichResourcesWithMember(PerunSession sess, Member member) {
    AttributeDefinition ad;
    try {
      ad = getPerunBl().getAttributesManagerBl()
          .getAttributeDefinition(sess, AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF + ":optOutMailingList");
    } catch (AttributeNotExistsException e) {
      return new ArrayList<>();
    }
    return getResourcesManagerImpl().getRichResourcesWithMemberAndAttribute(sess, member, ad);
  }

  /**
   * Gets the perunBl.
   *
   * @return The perunBl.
   */
  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  @Override
  public List<AssignedResource> getResourceAssignments(PerunSession sess, Group group, List<String> attrNames) {
    List<AssignedResource> assignedResources = getResourcesManagerImpl().getResourceAssignments(sess, group);

    // set resource attributes in EnrichedResource
    assignedResources.forEach(assignedResource -> assignedResource.setEnrichedResource(
        convertToEnrichedResource(sess, assignedResource.getEnrichedResource().getResource(), attrNames)));

    assignedResources.forEach(assignedResource -> assignedResource.setResourceTags(
        getAllResourcesTagsForResource(sess, assignedResource.getEnrichedResource().getResource())));

    return assignedResources;
  }

  @Override
  public Resource getResourceById(PerunSession sess, int id) throws ResourceNotExistsException {
    return getResourcesManagerImpl().getResourceById(sess, id);
  }

  @Override
  public Resource getResourceByName(PerunSession sess, Vo vo, Facility facility, String name)
      throws ResourceNotExistsException {
    return getResourcesManagerImpl().getResourceByName(sess, vo, facility, name);
  }

  @Override
  public List<Resource> getResources(PerunSession sess, Vo vo) {
    return getResourcesManagerImpl().getResources(sess, vo);
  }

  @Override
  public List<Resource> getResources(PerunSession sess, User user, List<Status> memberStatuses,
                                     List<MemberGroupStatus> memberGroupStatuses,
                                     List<GroupResourceStatus> groupResourceStatuses) {
    return getResourcesManagerImpl().getResources(sess, user, memberStatuses, memberGroupStatuses,
        groupResourceStatuses);
  }

  @Override
  public List<Resource> getResources(PerunSession sess) {
    return getResourcesManagerImpl().getResources(sess);
  }

  @Override
  public List<Resource> getResourcesByAttribute(PerunSession sess, Attribute attribute)
      throws WrongAttributeAssignmentException {
    getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_RESOURCE_ATTR);
    if (!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) ||
          getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute))) {
      throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
    }
    return getResourcesManagerImpl().getResourcesByAttribute(sess, attribute);

  }

  @Override
  public List<Resource> getResourcesByIds(PerunSession sess, List<Integer> ids) {
    return getResourcesManagerImpl().getResourcesByIds(sess, ids);
  }

  @Override
  public int getResourcesCount(PerunSession sess, Vo vo) {
    return getResourcesManagerImpl().getResourcesCount(sess, vo);
  }

  @Override
  public int getResourcesCount(PerunSession sess) {
    return getResourcesManagerImpl().getResourcesCount(sess);
  }

  /**
   * Gets the resourcesManagerImpl.
   *
   * @return The resourcesManagerImpl.
   */
  public ResourcesManagerImplApi getResourcesManagerImpl() {
    return this.resourcesManagerImpl;
  }

  @Override
  public List<Resource> getResourcesWhereGroupIsAdmin(PerunSession sess, Facility facility, Vo vo,
                                                      Group authorizedGroup) {
    return getResourcesManagerImpl().getResourcesWhereGroupIsAdmin(sess, facility, vo, authorizedGroup);
  }

  @Override
  public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, User user) {
    return resourcesManagerImpl.getResourcesWhereUserIsAdmin(sess, user);
  }

  @Override
  public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Facility facility, Vo vo, User authorizedUser) {
    return getResourcesManagerImpl().getResourcesWhereUserIsAdmin(sess, facility, vo, authorizedUser);
  }

  @Override
  public List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Vo vo, User authorizedUser) {
    return getResourcesManagerImpl().getResourcesWhereUserIsAdmin(sess, vo, authorizedUser);
  }

  @Override
  public List<RichUser> getRichAdmins(PerunSession perunSession, Resource resource, List<String> specificAttributes,
                                      boolean allUserAttributes, boolean onlyDirectAdmins)
      throws UserNotExistsException {
    List<User> users = this.getAdmins(perunSession, resource, onlyDirectAdmins);
    List<RichUser> richUsers;

    if (allUserAttributes) {
      richUsers = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
    } else {
      try {
        richUsers = getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession,
            perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users),
            getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
      } catch (AttributeNotExistsException ex) {
        throw new InternalErrorException("One of Attribute not exist.", ex);
      }
    }
    return richUsers;
  }

  @Override
  public RichResource getRichResourceById(PerunSession sess, int id) throws ResourceNotExistsException {
    return getResourcesManagerImpl().getRichResourceById(sess, id);
  }

  @Override
  public List<RichResource> getRichResources(PerunSession sess, Vo vo) {
    return getResourcesManagerImpl().getRichResources(sess, vo);
  }

  @Override
  public List<RichResource> getRichResourcesByIds(PerunSession sess, List<Integer> ids) {
    return getResourcesManagerImpl().getRichResourcesByIds(sess, ids);
  }

  @Override
  public Vo getVo(PerunSession sess, Resource resource) {
    try {
      return getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId());
    } catch (VoNotExistsException e) {
      throw new ConsistencyErrorException("Resource is assigned to the non-existent VO.", e);
    }
  }

  @Override
  public boolean groupResourceAssignmentExists(PerunSession sess, Resource resource, Group group) {
    return getResourcesManagerImpl().groupResourceAssignmentExists(sess, resource, group);
  }

  @Override
  public boolean isGroupAssigned(PerunSession sess, Resource resource, Group group) {
    return getResourcesManagerImpl().isGroupAssigned(sess, resource, group);
  }

  @Override
  public boolean isGroupManuallyAssigned(PerunSession sess, Group group, Resource resource) {
    return getResourcesManagerImpl().isGroupManuallyAssigned(sess, group, resource);
  }

  @Override
  public List<Service> isResourceLastAssignedServices(PerunSession sess, Resource resource, List<Service> services)
      throws FacilityNotExistsException {
    Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityById(sess, resource.getFacilityId());
    List<Service> result = new ArrayList<>();

    for (Service service : services) {
      List<RichResource> assignedResources = getPerunBl().getFacilitiesManagerBl()
                                                             .getAssignedRichResources(sess, facility, service);
      if (assignedResources.size() == 1 && assignedResources.get(0).getId() == resource.getId()) {
        result.add(service);
      }
    }
    return result;
  }

  @Override
  public boolean isUserAllowed(PerunSession sess, User user, Resource resource) {
    return getResourcesManagerImpl().isUserAllowed(sess, user, resource);
  }

  @Override
  public boolean isUserAssigned(PerunSession sess, User user, Resource resource) {
    return getResourcesManagerImpl().isUserAssigned(sess, user, resource);
  }

  /**
   * Sets assignment status of given group and resource to ACTIVE. Check if attributes for each member from group are
   * valid. Fill members' attributes with missing values.
   *
   * @param sess     session
   * @param group    group
   * @param resource resource
   * @throws WrongAttributeValueException          when an attribute value has wrong/illegal syntax
   * @throws WrongReferenceAttributeValueException when an attribute value has wrong/illegal semantics
   * @throws GroupResourceMismatchException        when the given group and resource are not from the same VO
   * @throws GroupNotDefinedOnResourceException    when there is no such group-resource assignment
   */
  private void processGroupResourceActivation(PerunSession sess, Group group, Resource resource)
      throws GroupResourceMismatchException, WrongReferenceAttributeValueException, WrongAttributeValueException,
      GroupNotDefinedOnResourceException {
    getPerunBl().getAttributesManagerBl().checkGroupIsFromTheSameVoLikeResource(sess, group, resource);

    // set status as ACTIVE first because methods checkAttributesSemantics and fillAttribute need active state to
    // work correctly
    getResourcesManagerImpl().setGroupResourceStatus(sess, group, resource, GroupResourceStatus.ACTIVE);

    // reset assignment failure cause
    getResourcesManagerImpl().setFailedGroupResourceAssignmentCause(sess, group, resource, null);

    // if there are no services, the members are empty and there is nothing more to process
    if (getAssignedServices(sess, resource).isEmpty()) {
      getPerunBl().getAuditer().log(sess, new GroupAssignedToResource(group, resource));
      return;
    }

    // get/fill/set all required group and group-resource attributes
    try {
      List<Attribute> attributes =
          getPerunBl().getAttributesManagerBl().getResourceRequiredAttributes(sess, resource, resource, group, true);
      attributes = getPerunBl().getAttributesManagerBl().fillAttributes(sess, resource, group, attributes, true);
      getPerunBl().getAttributesManagerBl().setAttributes(sess, resource, group, attributes, true);
    } catch (WrongAttributeAssignmentException | GroupResourceMismatchException ex) {
      throw new ConsistencyErrorException(ex);
    }

    List<Member> members = getPerunBl().getGroupsManagerBl().getGroupMembersExceptInvalidAndDisabled(sess, group);

    // get all "allowed" group members and get/fill/set required attributes for them
    Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
    for (Member member : members) {
      User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
      try {
        getPerunBl().getAttributesManagerBl().setRequiredAttributes(sess, facility, resource, user, member, true);
      } catch (WrongAttributeAssignmentException | MemberResourceMismatchException | AttributeNotExistsException ex) {
        throw new ConsistencyErrorException(ex);
      }
    }

    getPerunBl().getAuditer().log(sess, new GroupAssignedToResource(group, resource));
    // TODO: set and check member-group attributes
  }

  @Async
  @Override
  public void processGroupResourceActivationAsync(PerunSession sess, Group group, Resource resource) {
    try {
      // try activation for 5 times after 5 seconds because the assignment
      // might not be committed in DB yet
      boolean successful = false;
      for (int i = 0; i < 5; ++i) {
        try {
          processGroupResourceActivation(sess, group, resource);
          successful = true;
          break;
        } catch (GroupNotDefinedOnResourceException ex) {
          Thread.sleep(5000);
        }
      }

      if (!successful) {
        processGroupResourceActivation(sess, group, resource);
      }
    } catch (Exception e) {
      LOG.error("Activation of group-resource assignment failed.", e);
      try {
        getResourcesManagerImpl().setGroupResourceStatus(sess, group, resource, GroupResourceStatus.FAILED);
        getResourcesManagerImpl().setFailedGroupResourceAssignmentCause(sess, group, resource, e.getMessage());
      } catch (GroupNotDefinedOnResourceException groupNotDefinedOnResourceException) {
        LOG.error("Group-resource assignment doesn't exist.", groupNotDefinedOnResourceException);
      }
    }
  }

  @Override
  public void removeAllExpiredBansOnResources(PerunSession sess) {
    List<BanOnResource> expiredBans = this.getAllExpiredBansOnResources(sess);
    for (BanOnResource expiredBan : expiredBans) {
      try {
        this.removeBan(sess, expiredBan.getId());
      } catch (BanNotExistsException ex) {
        LOG.warn("Ban {} can't be removed because it not exists yet.", expiredBan);
        //Skipt this, probably already removed
      }
    }
  }

  @Override
  public void removeAllResourcesTagFromResource(PerunSession perunSession, Resource resource) {
    getResourcesManagerImpl().removeAllResourcesTagFromResource(perunSession, resource);
  }

  @Override
  public void removeAutomaticGroupFromResource(PerunSession sess, Group group, Resource resource, int sourceGroupId)
      throws GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
    removeGroupFromResource(sess, group, resource, sourceGroupId);
  }

  @Override
  public void removeBan(PerunSession sess, int banId) throws BanNotExistsException {
    BanOnResource ban = this.getBanById(sess, banId);
    getResourcesManagerImpl().removeBan(sess, banId);
    getPerunBl().getAuditer().log(sess, new BanRemovedForResource(ban, ban.getMemberId(), ban.getResourceId()));
  }

  @Override
  public void removeBan(PerunSession sess, int memberId, int resourceId) throws BanNotExistsException {
    BanOnResource ban = this.getBan(sess, memberId, resourceId);
    getResourcesManagerImpl().removeBan(sess, memberId, resourceId);
    getPerunBl().getAuditer().log(sess, new BanRemovedForResource(ban, memberId, resourceId));
  }

  @Override
  public void removeGroupFromResource(PerunSession sess, Group group, Resource resource)
      throws GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
    removeGroupFromResource(sess, group, resource, null);
  }

  /**
   * Remove group from a resource. Remove subgroups automatic assignments. After removing, check attributes and fix them
   * if it is needed.
   *
   * @param sess
   * @param group
   * @param resource
   * @param sourceGroupId id of a source group if an automatic assignment should be deleted, null otherwise
   * @throws GroupNotDefinedOnResourceException       when there is no such group-resource assignment
   * @throws GroupAlreadyRemovedFromResourceException when the assignment was already removed
   */
  private void removeGroupFromResource(PerunSession sess, Group group, Resource resource, Integer sourceGroupId)
      throws GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
    Vo groupVo = getPerunBl().getGroupsManagerBl().getVo(sess, group);

    // Check if the group and resource belongs to the same VO
    if (!groupVo.equals(this.getVo(sess, resource))) {
      throw new InternalErrorException(
          "Group " + group + " and resource " + resource + " belongs to the different VOs");
    }

    // Check if the group-resource assignment is defined
    Optional<AssignedGroup> assignmentToRemove = getResourcesManagerImpl().getGroupAssignments(sess, resource).stream()
        .filter(assignedGroup -> assignedGroup.getEnrichedGroup().getGroup().equals(group) &&
                                 Objects.equals(assignedGroup.getSourceGroupId(), sourceGroupId)).findFirst();

    if (assignmentToRemove.isEmpty()) {
      // Group is not defined on the resource
      throw new GroupNotDefinedOnResourceException(group.getName());
    }

    // Remove group
    if (sourceGroupId != null) {
      getResourcesManagerImpl().removeAutomaticGroupFromResource(sess, group, resource, sourceGroupId);
    } else {
      getResourcesManagerImpl().removeGroupFromResource(sess, group, resource);

      // Remove automatically assigned subgroups
      List<AssignedGroup> subgroupsAssignments = getResourcesManagerImpl().getGroupAssignments(sess, resource).stream()
          .filter(assignedGroup -> Objects.equals(assignedGroup.getSourceGroupId(), group.getId()))
          .collect(Collectors.toList());

      for (AssignedGroup assignedSubgroup : subgroupsAssignments) {
        try {
          removeAutomaticGroupFromResource(sess, assignedSubgroup.getEnrichedGroup().getGroup(), resource,
              group.getId());
        } catch (GroupAlreadyRemovedFromResourceException e) {
          // skip silently
        }
      }
    }

    // If it was the last ACTIVE assignment, we can delete group-resource attributes and audit the removal
    if (!isGroupAssigned(sess, resource, group)) {
      getPerunBl().getAuditer().log(sess, new GroupRemovedFromResource(group, resource));

      // Remove group-resource attributes
      try {
        getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, resource, group);
      } catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
        throw new InternalErrorException(e);
      } catch (GroupResourceMismatchException ex) {
        throw new ConsistencyErrorException(ex);
      }
    }

    // FIXME - here we should call checkSemantics() and on error re-fill/set user-facility attributes
    //  for the group members of removed group, which are still allowed on the facility, since we removed
    //  one relation and attribute constraints might have changed (eg. for shell / default gid/group).
    //  We don't do this for performance reasons.

  }

  @Override
  public void removeGroupFromResources(PerunSession perunSession, Group group, List<Resource> resources)
      throws GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
    for (Resource r : resources) {
      this.removeGroupFromResource(perunSession, group, r);
    }
  }

  @Override
  public void removeGroupsFromResource(PerunSession perunSession, List<Group> groups, Resource resource)
      throws GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException {
    for (Group g : groups) {
      this.removeGroupFromResource(perunSession, g, resource);
    }
  }

  @Override
  public void removeResourceSelfServiceGroup(PerunSession sess, Resource resource, Group group)
      throws GroupNotAdminException {
    try {
      AuthzResolverBlImpl.unsetRole(sess, group, resource, Role.RESOURCESELFSERVICE);
    } catch (RoleCannotBeManagedException e) {
      throw new InternalErrorException(e);
    }
    getPerunBl().getAuditer().log(sess, new ResourceSelfServiceRemovedForGroup(resource, group));
  }

  @Override
  public void removeResourceSelfServiceUser(PerunSession sess, Resource resource, User user)
      throws UserNotAdminException {
    try {
      AuthzResolverBlImpl.unsetRole(sess, user, resource, Role.RESOURCESELFSERVICE);
    } catch (RoleCannotBeManagedException e) {
      throw new InternalErrorException(e);
    }
    getPerunBl().getAuditer().log(sess, new ResourceSelfServiceRemovedForUser(resource, user));
  }

  @Override
  public void removeResourceTagFromResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource)
      throws ResourceTagNotAssignedException {
    List<ResourceTag> allResourceTags = this.getAllResourcesTagsForResource(perunSession, resource);
    if (!allResourceTags.contains(resourceTag)) {
      throw new ResourceTagNotAssignedException(resourceTag);
    }
    getResourcesManagerImpl().removeResourceTagFromResource(perunSession, resourceTag, resource);
  }

  @Override
  public void removeResourceTagsFromResource(PerunSession perunSession, List<ResourceTag> resourceTags,
                                             Resource resource) throws ResourceTagNotAssignedException {
    for (ResourceTag tag : resourceTags) {
      removeResourceTagFromResource(perunSession, tag, resource);
    }
  }

  @Override
  public void removeService(PerunSession sess, Resource resource, Service service) throws ServiceNotAssignedException {
    getResourcesManagerImpl().removeService(sess, resource, service);
    getPerunBl().getAuditer().log(sess, new ServiceRemovedFromResource(service, resource));
  }

  @Override
  public void removeServices(PerunSession sess, Resource resource, List<Service> services, boolean removeTasks,
                      boolean removeTaskResults, boolean removeDestinations)
      throws ServiceNotAssignedException, FacilityNotExistsException {
    Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
    for (Service service : services) {
      boolean isLastAssignedToResource = isResourceLastAssignedServices(sess, resource, List.of(service)).size() == 1;
      if (isLastAssignedToResource) {
        Task task = getPerunBl().getTasksManagerBl().getTask(sess, service, facility);
        if (removeTasks && task != null) {
          getPerunBl().getTasksManagerBl().deleteTask(sess, task);
        } else if (removeTaskResults && task != null) {
          getPerunBl().getTasksManagerBl().deleteTaskResults(sess, task.getId());
        }
        if (removeDestinations) {
          getPerunBl().getServicesManagerBl().removeAllDestinations(sess, service, facility);
        }
      }
      removeService(sess, resource, service);
    }
  }

  /**
   * Sets status to group-resource assignment. Does not overwrite ACTIVE status. Puts new status to database if no
   * status is saved, overwrites existing one otherwise. Activates group-resource assignment according to async flag if
   * assignInactive is not set to false.
   *
   * @param perunSession   perun session
   * @param resource       resource
   * @param async          if true, runs in asynchronous mode, synchronously otherwise
   * @param assignInactive if true, sets INACTIVE status and does not activate assignment
   * @param g              group
   * @throws GroupNotDefinedOnResourceException
   * @throws WrongReferenceAttributeValueException
   * @throws GroupResourceMismatchException
   * @throws WrongAttributeValueException
   */
  private void setAssignedGroupStatusAndActivate(PerunSession perunSession, Resource resource, boolean async,
                                                 boolean assignInactive, Group g)
      throws WrongReferenceAttributeValueException, GroupResourceMismatchException, WrongAttributeValueException {
    GroupResourceStatus status = assignInactive ? GroupResourceStatus.INACTIVE : GroupResourceStatus.PROCESSING;

    // if group is assigned as ACTIVE, don't change status
    if (isGroupAssigned(perunSession, resource, g)) {
      return;
    }

    try {
      if (groupResourceAssignmentExists(perunSession, resource, g)) {
        getResourcesManagerImpl().setGroupResourceStatus(perunSession, g, resource, status);
      } else {
        getResourcesManagerImpl().assignGroupToResourceState(perunSession, g, resource, status);
      }

      // if status is PROCESSING, activate group-resource assignment
      if (!assignInactive) {
        activateGroupResourceAssignment(perunSession, g, resource, async);
      }
    } catch (GroupNotDefinedOnResourceException e) {
      throw new ConsistencyErrorException(e);
    }
  }

  @Override
  public BanOnResource setBan(PerunSession sess, BanOnResource banOnResource) throws BanAlreadyExistsException {
    if (this.banExists(sess, banOnResource.getMemberId(), banOnResource.getResourceId())) {
      throw new BanAlreadyExistsException(banOnResource);
    }
    banOnResource = getResourcesManagerImpl().setBan(sess, banOnResource);
    getPerunBl().getAuditer()
        .log(sess, new BanSetForResource(banOnResource, banOnResource.getMemberId(), banOnResource.getResourceId()));
    return banOnResource;
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

  /**
   * For given resource, finds all allowed members and sets them attributes, (member-resource, member, user-facility and
   * user) that are required by given services.
   *
   * @param sess     session
   * @param resource resource from where allowed members are taken
   * @param services services, for which the required attributes are set
   * @throws MemberResourceMismatchException       MemberResourceMismatchException
   * @throws WrongAttributeAssignmentException     WrongAttributeAssignmentException
   * @throws AttributeNotExistsException           AttributeNotExistsException
   * @throws WrongReferenceAttributeValueException WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException          WrongAttributeValueException
   */
  public void updateAllRequiredAttributesForAllowedMembers(PerunSession sess, Resource resource, List<Service> services)
      throws MemberResourceMismatchException, WrongAttributeAssignmentException, AttributeNotExistsException,
      WrongReferenceAttributeValueException, WrongAttributeValueException {
    Facility facility = getFacility(sess, resource);
    List<Member> members = getAllowedMembers(sess, resource);
    for (Member member : members) {
      User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
      getPerunBl().getAttributesManagerBl()
          .setRequiredAttributes(sess, services, facility, resource, user, member, true);
    }
  }

  @Override
  public BanOnResource updateBan(PerunSession sess, BanOnResource banOnResource) {
    banOnResource = getResourcesManagerImpl().updateBan(sess, banOnResource);
    getPerunBl().getAuditer().log(sess,
        new BanUpdatedForResource(banOnResource, banOnResource.getMemberId(), banOnResource.getResourceId()));
    return banOnResource;
  }

  @Override
  public Resource updateResource(PerunSession sess, Resource resource) throws ResourceExistsException {
    Facility facility = getFacility(sess, resource);
    Vo vo = getVo(sess, resource);

    try {
      Resource existingResource = getResourcesManagerImpl().getResourceByName(sess, vo, facility, resource.getName());

      // if it is the same resource which is updated but the name stayed the same.
      if (existingResource.getId() == resource.getId()) {
        resource = getResourcesManagerImpl().updateResource(sess, resource);
        getPerunBl().getAuditer().log(sess, new ResourceUpdated(resource));
        return resource;
      }
      // if it is not the same resource - throw the exception. Resource can not be updated,
      // because there is already a resource with this name but with different id.
      throw new ResourceExistsException(existingResource);
    } catch (ResourceNotExistsException e) {
      resource = getResourcesManagerImpl().updateResource(sess, resource);
      getPerunBl().getAuditer().log(sess, new ResourceUpdated(resource));
    }

    return resource;
  }

  @Override
  public ResourceTag updateResourceTag(PerunSession perunSession, ResourceTag resourceTag) {
    return getResourcesManagerImpl().updateResourceTag(perunSession, resourceTag);
  }
}
