package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityMismatchException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceStatusException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
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
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import java.util.List;

/**
 * Manages resources.
 *
 * @author Slavek Licehammer
 * @author
 */
public interface ResourcesManager {

  /**
   * Try to activate the group-resource status. If the async is set to false, the validation is performed synchronously.
   * The assignment will be either ACTIVE, in case of a successful synchronous call, or it will be PROCESSING in case of
   * an asynchronous call. After the async validation, the state can be either ACTIVE or FAILED.
   *
   * @param session
   * @param group    group
   * @param resource resource
   * @param async    if true the validation is performed asynchronously
   * @throws PrivilegeException                    insufficient permissions
   * @throws GroupNotExistsException               when the group doesn't exist
   * @throws ResourceNotExistsException            when the resource doesn't exist
   * @throws WrongAttributeValueException          when an attribute value has wrong/illegal syntax
   * @throws WrongReferenceAttributeValueException when an attribute value has wrong/illegal semantics
   * @throws GroupResourceMismatchException        when the given group and resource are not from the same VO
   * @throws GroupNotDefinedOnResourceException    when the group-resource assignment doesn't exist
   */
  void activateGroupResourceAssignment(PerunSession session, Group group, Resource resource, boolean async)
      throws ResourceNotExistsException, GroupNotExistsException, PrivilegeException,
      WrongReferenceAttributeValueException, GroupNotDefinedOnResourceException, GroupResourceMismatchException,
      WrongAttributeValueException;

  /**
   * Add role resource admin to user for the selected resource.
   *
   * @param sess
   * @param resource
   * @param user
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws AlreadyAdminException
   * @throws ResourceNotExistsException
   * @throws RoleCannotBeManagedException
   * @throws RoleCannotBeSetException
   */
  void addAdmin(PerunSession sess, Resource resource, User user)
          throws UserNotExistsException, PrivilegeException, AlreadyAdminException, ResourceNotExistsException,
          RoleCannotBeManagedException, RoleCannotBeSetException;

  /**
   * Add role resource admin to group for the selected resource.
   *
   * @param sess
   * @param resource
   * @param group
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws AlreadyAdminException
   * @throws ResourceNotExistsException
   * @throws RoleCannotBeManagedException
   */
  void addAdmin(PerunSession sess, Resource resource, Group group)
          throws GroupNotExistsException, PrivilegeException, AlreadyAdminException, ResourceNotExistsException,
          RoleCannotBeManagedException, RoleCannotBeSetException;

  /**
   * Sets ResourceSelfService role to given group for given resource.
   *
   * @param sess     session
   * @param resource resource
   * @param group    group
   * @throws InternalErrorException internal error
   * @throws PrivilegeException     insufficient permissions
   * @throws AlreadyAdminException  already has the role
   */
  void addResourceSelfServiceGroup(PerunSession sess, Resource resource, Group group)
      throws PrivilegeException, AlreadyAdminException, ResourceNotExistsException, GroupNotExistsException;

  /**
   * Sets ResourceSelfService role to given user for given resource.
   *
   * @param sess     session
   * @param resource resource
   * @param user     user id
   * @throws InternalErrorException internal error
   * @throws PrivilegeException     insufficient permissions
   * @throws AlreadyAdminException  already has the role
   */
  void addResourceSelfServiceUser(PerunSession sess, Resource resource, User user)
      throws PrivilegeException, AlreadyAdminException, ResourceNotExistsException, UserNotExistsException;

  /**
   * Assign group to a resource. Check if attributes for each member form group are valid. Fill members' attributes with
   * missing value. Work in sync/async mode. Provide options for creating inactive or automatic subgroups group-resource
   * assignments.
   * <p>
   * If the group is already assigned, nothing it performed.
   *
   * @param perunSession
   * @param group
   * @param resource
   * @param async
   * @param assignInactive
   * @param autoAssignSubgroups
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws GroupResourceMismatchException
   */
  void assignGroupToResource(PerunSession perunSession, Group group, Resource resource, boolean async,
                             boolean assignInactive, boolean autoAssignSubgroups)
      throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException,
      WrongReferenceAttributeValueException, GroupResourceMismatchException;

  /**
   * Assign group to the resources. Check if attributes for each member from group are valid. Fill members' attributes
   * with missing values. Work in sync/async mode. Provide options for creating inactive or automatic subgroups
   * group-resource assignments.
   *
   * @param perunSession
   * @param group               the group
   * @param resources           list of resources
   * @param async
   * @param assignInactive
   * @param autoAssignSubgroups
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws ResourceNotExistsException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws GroupResourceMismatchException
   */
  void assignGroupToResources(PerunSession perunSession, Group group, List<Resource> resources, boolean async,
                              boolean assignInactive, boolean autoAssignSubgroups)
      throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException,
      WrongReferenceAttributeValueException, GroupResourceMismatchException;

  /**
   * Assign groups to a resource. Check if attributes for each member from all groups are valid. Fill members'
   * attributes with missing values. Work in sync/async mode. Provide options for creating inactive or automatic
   * subgroups group-resource assignments.
   *
   * @param perunSession
   * @param groups              list of resources
   * @param resource
   * @param async
   * @param assignInactive
   * @param autoAssignSubgroups
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws ResourceNotExistsException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws GroupResourceMismatchException
   */
  void assignGroupsToResource(PerunSession perunSession, List<Group> groups, Resource resource, boolean async,
                              boolean assignInactive, boolean autoAssignSubgroups)
      throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException,
      WrongReferenceAttributeValueException, GroupResourceMismatchException;

  /**
   * Assign existing ResourceTag on existing Resource.
   *
   * @param perunSession
   * @param resourceTag
   * @param resource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceTagNotExistsException
   * @throws ResourceNotExistsException
   * @throws ResourceTagAlreadyAssignedException
   */
  void assignResourceTagToResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource)
      throws PrivilegeException, ResourceTagNotExistsException, ResourceNotExistsException,
      ResourceTagAlreadyAssignedException;

  /**
   * Assign existing ResourceTags on existing Resource.
   *
   * @param perunSession
   * @param resourceTags
   * @param resource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceTagNotExistsException
   * @throws ResourceNotExistsException
   * @throws ResourceTagAlreadyAssignedException
   */
  void assignResourceTagsToResource(PerunSession perunSession, List<ResourceTag> resourceTags, Resource resource)
      throws PrivilegeException, ResourceTagNotExistsException, ResourceNotExistsException,
      ResourceTagAlreadyAssignedException;

  /**
   * Assign service to resource.
   *
   * @param perunSession
   * @param resource
   * @param service
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   * @throws ServiceNotExistsException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException
   * @throws ServiceAlreadyAssignedException
   */
  void assignService(PerunSession perunSession, Resource resource, Service service)
      throws PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceAlreadyAssignedException,
      WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Assign services to resource.
   *
   * @param perunSession perun session
   * @param resource     resource
   * @param services     services to be assigned
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   * @throws ServiceNotExistsException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException
   * @throws ServiceAlreadyAssignedException
   */
  void assignServices(PerunSession perunSession, Resource resource, List<Service> services)
      throws PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceAlreadyAssignedException,
      WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Copy all attributes of the source resource to the destination resource. The attributes, that are in the destination
   * resource and aren't in the source resource, are retained. The common attributes are replaced with the attributes
   * from the source resource. The virtual attributes are not copied.
   *
   * @param sess
   * @param sourceResource
   * @param destinationResource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceNotExistsException
   * @throws WrongReferenceAttributeValueException
   */
  void copyAttributes(PerunSession sess, Resource sourceResource, Resource destinationResource)
      throws PrivilegeException, ResourceNotExistsException, WrongReferenceAttributeValueException;

  /**
   * Copy all groups of the source resource to the destination resource. The groups, that are in the destination
   * resource and aren't in the source resource, are retained. The common groups are replaced with the groups from
   * source resource.
   *
   * @param sourceResource
   * @param destinationResource
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  void copyGroups(PerunSession sess, Resource sourceResource, Resource destinationResource)
      throws ResourceNotExistsException, PrivilegeException;

  /**
   * Copy "template" settings from user's another existing resource and create new resource with this template. The
   * settings are attributes, services, tags (if exists), groups and their members (if the resources are from the same
   * VO and withGroups is true) Template Resource can be from any of user's facilities.
   *
   * @param perunSession
   * @param templateResource    template resource to copy
   * @param destinationResource destination resource containing IDs of destination facility, VO and resource name.
   * @param withGroups          if set to true and resources ARE from the same VO we also copy all group-resource and
   *                            member-resource attributes and assign all groups same as on templateResource if set to
   *                            true and resources ARE NOT from the same VO InternalErrorException is thrown, if set to
   *                            false we will NOT copy groups and group related attributes.
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   */
  Resource copyResource(PerunSession perunSession, Resource templateResource, Resource destinationResource,
                        boolean withGroups)
      throws ResourceNotExistsException, PrivilegeException, ResourceExistsException;

  /**
   * Copy all services of the source resource to the destination resource. The services, that are in the destination
   * resource and aren't in the source resource, are retained. The common services are replaced with the services from
   * source resource.
   *
   * @param sourceResource
   * @param destinationResource
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   */
  void copyServices(PerunSession sess, Resource sourceResource, Resource destinationResource)
      throws ResourceNotExistsException, PrivilegeException, WrongAttributeValueException,
      WrongReferenceAttributeValueException;

  /**
   * Inserts resource into DB.
   *
   * @param resource resource to create
   * @param vo       virtual organization
   * @param facility facility
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  Resource createResource(PerunSession perunSession, Resource resource, Vo vo, Facility facility)
      throws PrivilegeException, VoNotExistsException, FacilityNotExistsException, ResourceExistsException;

  /**
   * Create new Resource tag for the vo.
   *
   * @param perunSession
   * @param resourceTag
   * @param vo
   * @return new created resourceTag
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws VoNotExistsException
   */
  ResourceTag createResourceTag(PerunSession perunSession, ResourceTag resourceTag, Vo vo)
      throws PrivilegeException, VoNotExistsException;

  /**
   * Deactivates the group-resource assignment. The assignment will become INACTIVE and will not be used to allow users
   * from the given group to the resource.
   *
   * @param group    group
   * @param resource resource
   * @throws PrivilegeException                 insufficient permissions
   * @throws GroupNotExistsException            when the group doesn't exist
   * @throws ResourceNotExistsException         when the resource doesn't exist
   * @throws GroupNotDefinedOnResourceException when the group-resource assignment doesn't exist
   * @throws GroupResourceStatusException       when trying to deactivate an assignment in PROCESSING state
   */
  void deactivateGroupResourceAssignment(PerunSession session, Group group, Resource resource)
      throws PrivilegeException, ResourceNotExistsException, GroupNotExistsException,
      GroupNotDefinedOnResourceException, GroupResourceStatusException;

  /**
   * Deletes all resources for the VO.
   *
   * @param perunSession
   * @param vo
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   * @throws ResourceAlreadyRemovedException          if there are at least 1 resource not affected by deleting from DB
   * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group which is not affected by removing
   *                                                  from DB
   */
  void deleteAllResources(PerunSession perunSession, Vo vo)
      throws VoNotExistsException, PrivilegeException, ResourceAlreadyRemovedException,
      GroupAlreadyRemovedFromResourceException;

  /**
   * Delete all ResourcesTags for specific VO.
   *
   * @param perunSession
   * @param vo
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceTagAlreadyAssignedException
   * @throws VoNotExistsException                ¨
   */
  void deleteAllResourcesTagsForVo(PerunSession perunSession, Vo vo)
      throws ResourceTagAlreadyAssignedException, PrivilegeException, VoNotExistsException;

  /**
   * Deletes resource by id.
   *
   * @param perunSession
   * @param resource
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   * @throws ResourceAlreadyRemovedException          if there are 0 rows affected by deleting from DB
   * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group which is not affected by removing
   *                                                  from DB
   * @throws FacilityNotExistsException               if facility of this resource not exists
   */
  void deleteResource(PerunSession perunSession, Resource resource)
      throws ResourceNotExistsException, PrivilegeException, ResourceAlreadyRemovedException,
      GroupAlreadyRemovedFromResourceException, FacilityNotExistsException;

  /**
   * Delete existing Resource tag.
   *
   * @param perunSession
   * @param resourceTag
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws VoNotExistsException
   * @throws ResourceTagAlreadyAssignedException
   */
  void deleteResourceTag(PerunSession perunSession, ResourceTag resourceTag)
      throws PrivilegeException, ResourceTagAlreadyAssignedException, VoNotExistsException,
      ResourceTagNotExistsException;

  /**
   * Gets list of all group administrators of the Resource.
   *
   * @param sess
   * @param resource
   * @return list of Group that are admins in the resource.
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  @Deprecated
  List<Group> getAdminGroups(PerunSession sess, Resource resource)
      throws PrivilegeException, ResourceNotExistsException;

  /**
   * Gets list of all user administrators of the Resource. If some group is administrator of the given resource, all
   * VALID members are included in the list.
   * <p>
   * If onlyDirectAdmins is true, return only direct users of the group for supported role.
   * <p>
   * Supported roles: ResourceAdmin, VOAdmin
   *
   * @param perunSession
   * @param resource
   * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
   * @return list of all user administrators of the given resource for supported role
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceNotExistsException
   */
  @Deprecated
  List<User> getAdmins(PerunSession perunSession, Resource resource, boolean onlyDirectAdmins)
      throws PrivilegeException, ResourceNotExistsException;

  /**
   * Get all resources from database. Returned resources are filtered based on the principal rights.
   *
   * @param sess Perun session
   * @return list of all resources
   * @throws PrivilegeException if the principal has insufficient permission
   */
  List<Resource> getAllResources(PerunSession sess) throws PrivilegeException;

  /**
   * Get all resources in specific Vo (specific by resourceTag.getVoId) for existing resourceTag
   *
   * @param perunSession
   * @param resourceTag
   * @return list of Resources
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws VoNotExistsException
   * @throws ResourceTagNotExistsException ¨
   */
  List<Resource> getAllResourcesByResourceTag(PerunSession perunSession, ResourceTag resourceTag)
      throws PrivilegeException, VoNotExistsException, ResourceTagNotExistsException;

  /**
   * Get all resourcesTags for existing Resource
   *
   * @param perunSession
   * @param resource
   * @return list of ResourcesTags
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  List<ResourceTag> getAllResourcesTagsForResource(PerunSession perunSession, Resource resource)
      throws ResourceNotExistsException, PrivilegeException;

  /**
   * Get all resourcesTags for existing Vo.
   *
   * @param perunSession
   * @param vo
   * @return list of all resourcesTags for existing Vo
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws VoNotExistsException   ¨
   */
  List<ResourceTag> getAllResourcesTagsForVo(PerunSession perunSession, Vo vo)
      throws PrivilegeException, VoNotExistsException;

  /**
   * Returns all members assigned to the resource.
   *
   * @param perunSession
   * @param resource
   * @return list of members assigned to the resource
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  List<Member> getAllowedMembers(PerunSession perunSession, Resource resource)
      throws ResourceNotExistsException, PrivilegeException;

  /**
   * Get all resources which have the member access on.
   *
   * @param sess
   * @param member
   * @return list of resources which have the member acess on
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws MemberNotExistsException
   */
  List<Resource> getAllowedResources(PerunSession sess, Member member)
      throws MemberNotExistsException, PrivilegeException;

  /**
   * Returns all users who is assigned with the resource.
   *
   * @param sess
   * @param resource
   * @return list of users
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  List<User> getAllowedUsers(PerunSession sess, Resource resource)
      throws ResourceNotExistsException, PrivilegeException;

  /**
   * List all groups associated with the resource.
   *
   * @param perunSession
   * @param resource
   * @return list of assigned group
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  List<Group> getAssignedGroups(PerunSession perunSession, Resource resource)
      throws PrivilegeException, ResourceNotExistsException;

  /**
   * List all groups associated with the resource and member
   *
   * @param perunSession
   * @param resource
   * @param member
   * @return list of assigned groups associated with the resource and member
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  List<Group> getAssignedGroups(PerunSession perunSession, Resource resource, Member member)
      throws PrivilegeException, ResourceNotExistsException, MemberNotExistsException;

  /**
   * Returns all members assigned to the resource.
   *
   * @param sess
   * @param resource
   * @return list of assigned members
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<Member> getAssignedMembers(PerunSession sess, Resource resource) throws PrivilegeException;

  /**
   * Returns members of groups assigned to resource with status of group-resource assignment.
   *
   * @param sess     perunSession
   * @param resource resource
   * @return list of members of groups assigned to given resource
   */
  List<AssignedMember> getAssignedMembersWithStatus(PerunSession sess, Resource resource) throws PrivilegeException;

  /**
   * List all resources to which the group is assigned.
   *
   * @param perunSession
   * @param group
   * @return list of assigned resources
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws PrivilegeException
   */
  List<Resource> getAssignedResources(PerunSession perunSession, Group group)
      throws GroupNotExistsException, PrivilegeException;

  /**
   * Get all resources where the member is assigned.
   *
   * @param sess
   * @param member
   * @return
   * @throws InternalErrorException
   * @throws MemberNotExistsException
   * @throws PrivilegeException
   */
  List<Resource> getAssignedResources(PerunSession sess, Member member)
      throws PrivilegeException, MemberNotExistsException;

  /**
   * Get all resources where the member and the service are assigned.
   *
   * @param sess
   * @param member
   * @param service
   * @return list of resources
   * @throws InternalErrorException
   * @throws MemberNotExistsException
   * @throws ServiceNotExistsException
   * @throws PrivilegeException
   */
  List<Resource> getAssignedResources(PerunSession sess, Member member, Service service)
      throws PrivilegeException, MemberNotExistsException, ServiceNotExistsException;

  /**
   * Returns all assigned resources where member is assigned through the groups.
   *
   * @param sess   perun session
   * @param member member
   * @return list of assigned resources
   * @throws MemberNotExistsException if the member does not exist
   * @throws PrivilegeException
   */
  List<AssignedResource> getAssignedResourcesWithStatus(PerunSession sess, Member member)
      throws PrivilegeException, MemberNotExistsException;

  /**
   * Returns all members assigned to the resource as RichMembers.
   *
   * @param sess
   * @param resource
   * @return list of assigned rich members
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<RichMember> getAssignedRichMembers(PerunSession sess, Resource resource) throws PrivilegeException;

  /**
   * List all rich resources associated with the group with facility property filled.
   *
   * @param perunSession
   * @param group
   * @return list of assigned rich resources
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws PrivilegeException
   */
  List<RichResource> getAssignedRichResources(PerunSession perunSession, Group group)
      throws GroupNotExistsException, PrivilegeException;

  /**
   * Get all rich resources where the member is assigned with facility property filled.
   *
   * @param sess
   * @param member
   * @return list of rich resources
   * @throws InternalErrorException
   * @throws MemberNotExistsException
   * @throws PrivilegeException
   */
  List<RichResource> getAssignedRichResources(PerunSession sess, Member member)
      throws PrivilegeException, MemberNotExistsException;

  /**
   * Get all rich resources where the service and the member are assigned with facility property filled.
   *
   * @param sess
   * @param member
   * @param service
   * @return list of rich resources
   * @throws InternalErrorException
   * @throws MemberNotExistsException
   * @throws ServiceNotExistsException
   * @throws PrivilegeException
   */
  List<RichResource> getAssignedRichResources(PerunSession sess, Member member, Service service)
      throws PrivilegeException, MemberNotExistsException, ServiceNotExistsException;

  /**
   * List all services associated with the resource.
   *
   * @param perunSession
   * @param resource
   * @return list of assigned resources
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  List<Service> getAssignedServices(PerunSession perunSession, Resource resource)
      throws ResourceNotExistsException, PrivilegeException;

  /**
   * Get ban by memberId and resource id
   *
   * @param sess
   * @param memberId   the id of member
   * @param resourceId the id of resource
   * @return specific ban for member on resource
   * @throws InternalErrorException
   * @throws BanNotExistsException
   * @throws PrivilegeException
   * @throws MemberNotExistsException
   * @throws ResourceNotExistsException
   */
  BanOnResource getBan(PerunSession sess, int memberId, int resourceId)
      throws BanNotExistsException, PrivilegeException, MemberNotExistsException, ResourceNotExistsException;

  /**
   * Get Ban for member on resource by it's id
   *
   * @param sess
   * @param banId the id of ban
   * @return resource ban by it's id
   * @throws InternalErrorException
   * @throws BanNotExistsException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  BanOnResource getBanById(PerunSession sess, int banId)
      throws BanNotExistsException, PrivilegeException, ResourceNotExistsException;

  /**
   * Get all bans for member on any resource.
   *
   * @param sess
   * @param memberId the id of member
   * @return list of bans for member on any resource
   * @throws InternalErrorException
   * @throws MemberNotExistsException
   * @throws ResourceNotExistsException
   */
  List<BanOnResource> getBansForMember(PerunSession sess, int memberId)
      throws MemberNotExistsException, ResourceNotExistsException;

  /**
   * Get all bans for members on the resource.
   *
   * @param sess
   * @param resourceId the id of resource
   * @return list of all members bans on the resource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceNotExistsException
   */
  List<BanOnResource> getBansForResource(PerunSession sess, int resourceId)
      throws PrivilegeException, ResourceNotExistsException;

  /**
   * Get all enriched bans for members on the resource.
   *
   * @param sess
   * @param resourceId the id of resource
   * @param attrNames  list of attributes names, returns all user/member attributes if null or empty
   * @return list of all enriched bans on resource
   * @throws PrivilegeException
   * @throws ResourceNotExistsException
   */
  List<EnrichedBanOnResource> getEnrichedBansForResource(PerunSession sess, int resourceId, List<String> attrNames)
      throws PrivilegeException, ResourceNotExistsException, AttributeNotExistsException;

  /**
   * Get all enriched bans on resources for user.
   *
   * @param sess
   * @param userId id of user
   * @return attrNames list of attributes names, returns all user/member attributes if null or empty
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<EnrichedBanOnResource> getEnrichedBansForUser(PerunSession sess, int userId, List<String> attrNames)
      throws PrivilegeException, UserNotExistsException, AttributeNotExistsException;

  /**
   * Find resource for given id and returns it with given attributes. If attrNames are null or empty, all resource
   * attributes are returned.
   *
   * @param sess      session
   * @param id        resource id
   * @param attrNames names of attributes to return
   * @return resource for given id with desired attributes
   * @throws PrivilegeException         insufficient permissions
   * @throws ResourceNotExistsException if there is no resource with given id
   */
  EnrichedResource getEnrichedResourceById(PerunSession sess, int id, List<String> attrNames)
      throws PrivilegeException, ResourceNotExistsException;

  /**
   * Find resources for given facility and attributes for given names. If the attrNames are empty or null, return all
   * attributes.
   *
   * @param sess      session
   * @param facility  facility
   * @param attrNames names of attributes to return
   * @return resources with desired attributes
   * @throws FacilityNotExistsException if there is not facility with given id
   * @throws PrivilegeException         insufficient permissions
   */
  List<EnrichedResource> getEnrichedResourcesForFacility(PerunSession sess, Facility facility, List<String> attrNames)
      throws FacilityNotExistsException, PrivilegeException;

  /**
   * Find resources for given vo and attributes for given names. If the attrNames are empty or null, return all
   * attributes.
   *
   * @param sess      session
   * @param vo        vo
   * @param attrNames names of attributes to return
   * @return resources with desired attributes
   * @throws VoNotExistsException if there is no vo with given id
   * @throws PrivilegeException   insufficient permissions
   */
  List<EnrichedResource> getEnrichedResourcesForVo(PerunSession sess, Vo vo, List<String> attrNames)
      throws VoNotExistsException, PrivilegeException;

  /**
   * Get facility which belongs to the concrete resource.
   *
   * @param perunSession
   * @param resource
   * @return facility belonging to the resource
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  Facility getFacility(PerunSession perunSession, Resource resource)
      throws ResourceNotExistsException, PrivilegeException;

  /**
   * Lists all of the assigned groups for the given resource. Also, returns specified attributes for the groups. If
   * attrNames are null, all group attributes are returned.
   *
   * @param session
   * @param resource  resource
   * @param attrNames names of attributes to return
   * @return list of assigned groups for given resource with specified attributes
   * @throws PrivilegeException         insufficient permissions
   * @throws ResourceNotExistsException when the resource doesn't exist
   */
  List<AssignedGroup> getGroupAssignments(PerunSession session, Resource resource, List<String> attrNames)
      throws PrivilegeException, ResourceNotExistsException;

  /**
   * Return all rich resources with mailing service(s) where given member is assigned.
   *
   * @param perunSession session
   * @param member       member
   * @return list of corresponding rich resources
   * @throws MemberNotExistsException if member is not present (does not exist)
   */
  List<RichResource> getMailingServiceRichResourcesWithMember(PerunSession perunSession, Member member)
      throws MemberNotExistsException, PrivilegeException;

  /**
   * Lists all of the resource assignments for the given group. Also, returns specified attributes and resource tags for
   * the resources. If attrNames are null or empty, all resource attributes are returned.
   *
   * @param session   session
   * @param group     group
   * @param attrNames names of attributes to return
   * @return list of assigned resources for given group with specified attributes and resource tags
   * @throws PrivilegeException      insufficient permissions
   * @throws GroupNotExistsException when the group doesn't exist
   */
  List<AssignedResource> getResourceAssignments(PerunSession session, Group group, List<String> attrNames)
      throws PrivilegeException, GroupNotExistsException;

  /**
   * Searches for the Resource with specified id.
   *
   * @param perunSession
   * @param id
   * @return Resource with specified id
   * @throws ResourceNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  Resource getResourceById(PerunSession perunSession, int id) throws PrivilegeException, ResourceNotExistsException;

  /**
   * Return resource by its name.
   *
   * @param sess
   * @param name
   * @param vo
   * @param facility
   * @return resource
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  Resource getResourceByName(PerunSession sess, Vo vo, Facility facility, String name)
      throws ResourceNotExistsException, PrivilegeException, VoNotExistsException, FacilityNotExistsException;

  /**
   * Get all VO resources. If called by resourceAdmin it returns only those resources of which is he admin.
   *
   * @param perunSession
   * @param vo
   * @return list of resources
   * @throws InternalErrorException
   * @throws VoNotExistsException
   * @throws PrivilegeException
   */
  List<Resource> getResources(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException;

  /**
   * Search for the Resources with specific ids.
   *
   * @param perunSession
   * @param ids
   * @return Resources with specified ids
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<Resource> getResourcesByIds(PerunSession perunSession, List<Integer> ids) throws PrivilegeException;

  /**
   * Get all VO resources count.
   *
   * @param perunSession
   * @param vo
   * @return count of vo resources
   * @throws InternalErrorException
   * @throws VoNotExistsException
   * @throws PrivilegeException
   */
  int getResourcesCount(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException;

  /**
   * Get count of all resources.
   *
   * @param perunSession
   * @return count of all resources
   * @throws InternalErrorException
   */
  int getResourcesCount(PerunSession perunSession);

  /**
   * Return all resources for the facility and the vo where the group is authorized as resource manager.
   *
   * @param sess
   * @param facility        the facility to which resources should be assigned to
   * @param vo              the vo to which resources should be assigned to
   * @param authorizedGroup group with resource manager role for all those resources
   * @return list of defined resources where groups has role resource manager
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws FacilityNotExistsException
   * @throws VoNotExistsException
   */
  List<Resource> getResourcesWhereGroupIsAdmin(PerunSession sess, Facility facility, Vo vo, Group authorizedGroup)
      throws PrivilegeException, GroupNotExistsException, FacilityNotExistsException, VoNotExistsException;

  /**
   * Returns list of resources, where the user is an admin. Including resources, where the user is a VALID member of
   * authorized group.
   *
   * @param sess
   * @param user
   * @return list of resources, where the user is an admin.
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Return all resources for the facility and the vo where user is authorized as resource manager. Including resources,
   * where the user is a VALID member of authorized group.
   *
   * @param sess
   * @param facility       the facility to which resources should be assigned to
   * @param vo             the vo to which resources should be assigned to
   * @param authorizedUser user with resource manager role for all those resources
   * @return list of defined resources where user has role resource manager
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   * @throws FacilityNotExistsException
   * @throws VoNotExistsException
   */
  List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Facility facility, Vo vo, User authorizedUser)
      throws PrivilegeException, UserNotExistsException, FacilityNotExistsException, VoNotExistsException;

  /**
   * Return all resources for the vo where user is authorized as resource manager. Including resources, where the user
   * is a VALID member of authorized group.
   *
   * @param sess
   * @param vo             the vo to which resources should be assigned to
   * @param authorizedUser user with resource manager role for all those resources
   * @return list of defined resources where user has role resource manager
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   * @throws VoNotExistsException
   */
  List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Vo vo, User authorizedUser)
      throws PrivilegeException, UserNotExistsException, VoNotExistsException;

  /**
   * Gets list of all richUser administrators of the Resource. If some group is administrator of the given resource, all
   * VALID members are included in the list.
   * <p>
   * Supported roles: ResourceAdmin, VOAdmin
   * <p>
   * If "onlyDirectAdmins" is "true", return only direct users of the group for supported role with specific attributes.
   * If "allUserAttributes" is "true", do not specify attributes through list and return them all in objects richUser .
   * Ignoring list of specific attributes.
   *
   * @param perunSession
   * @param resource
   * @param specificAttributes list of specified attributes which are needed in object richUser
   * @param allUserAttributes  if true, get all possible user attributes and ignore list of specificAttributes (if
   *                           false, get only specific attributes)
   * @param onlyDirectAdmins   if true, get only direct user administrators (if false, get both direct and indirect)
   * @return list of RichUser administrators for the resource and supported role with attributes
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   * @throws ResourceNotExistsException
   */
  @Deprecated
  List<RichUser> getRichAdmins(PerunSession perunSession, Resource resource, List<String> specificAttributes,
                               boolean allUserAttributes, boolean onlyDirectAdmins)
      throws UserNotExistsException, PrivilegeException, ResourceNotExistsException;

  /**
   * Search for the RichResource with specific id.
   *
   * @param perunSession
   * @param id
   * @return RichResource with specified id
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceNotExistsException
   */
  RichResource getRichResourceById(PerunSession perunSession, int id)
      throws PrivilegeException, ResourceNotExistsException;

  /**
   * Get all VO rich resources with facility property filled. If called by resourceAdmin it returns only those resources
   * of which is he admin.
   *
   * @param perunSession
   * @param vo
   * @return list of rich resources
   * @throws InternalErrorException
   * @throws VoNotExistsException
   * @throws PrivilegeException
   */
  List<RichResource> getRichResources(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException;

  /**
   * Search for the RichResources with specific ids.
   *
   * @param perunSession
   * @param ids
   * @return RichResources with specified ids
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<RichResource> getRichResourcesByIds(PerunSession perunSession, List<Integer> ids) throws PrivilegeException;

  /**
   * Get Vo which is tied to specified resource.
   *
   * @param perunSession
   * @param resource
   * @return vo tied to specified resource
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  Vo getVo(PerunSession perunSession, Resource resource) throws ResourceNotExistsException, PrivilegeException;

  /**
   * Checks whether the resource is the last one on the facility to have the provided services assigned.
   * Returns the services where this is the case.
   *
   * @param sess
   * @param resource
   * @param services
   * @return list of services where the provided resource is last to have them assigned on its facility.
   * @throws FacilityNotExistsException
   * @throws ResourceNotExistsException
   * @throws ServiceNotExistsException
   * @throws PrivilegeException
   */
  List<Service> isResourceLastAssignedServices(PerunSession sess, Resource resource, List<Service> services)
      throws FacilityNotExistsException, ResourceNotExistsException, ServiceNotExistsException, PrivilegeException;

  /**
   * Remove role resource admin from user for the selected resource.
   *
   * @param sess
   * @param resource
   * @param user
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws AlreadyAdminException
   * @throws ResourceNotExistsException
   */
  void removeAdmin(PerunSession sess, Resource resource, User user)
      throws UserNotExistsException, PrivilegeException, UserNotAdminException, ResourceNotExistsException,
      RoleCannotBeManagedException;

  /**
   * Remove role resource admin from group for the selected resource.
   *
   * @param sess
   * @param resource
   * @param group
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws PrivilegeException
   * @throws GroupNotAdminException
   * @throws ResourceNotExistsException
   */
  void removeAdmin(PerunSession sess, Resource resource, Group group)
      throws GroupNotExistsException, PrivilegeException, GroupNotAdminException, ResourceNotExistsException,
      RoleCannotBeManagedException;

  /**
   * Remove all Resource tags for specific resource.
   *
   * @param perunSession
   * @param resource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceNotExistsException
   */
  void removeAllResourcesTagFromResource(PerunSession perunSession, Resource resource)
      throws PrivilegeException, ResourceNotExistsException;

  /**
   * Remove specific ban by it's id.
   *
   * @param sess
   * @param banId the id of ban
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws BanNotExistsException
   * @throws ResourceNotExistsException
   */
  void removeBan(PerunSession sess, int banId)
      throws PrivilegeException, BanNotExistsException, ResourceNotExistsException;

  /**
   * Remove specific ban by memberId and resourceId.
   *
   * @param sess
   * @param memberId   the id of member
   * @param resourceId the id of resource
   * @throws InternalErrorException
   * @throws BanNotExistsException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  void removeBan(PerunSession sess, int memberId, int resourceId)
      throws BanNotExistsException, PrivilegeException, ResourceNotExistsException;

  /**
   * Remove group from a resource. After removing, check attributes and fix them if it is needed.
   *
   * @param perunSession
   * @param group
   * @param resource
   * @throws InternalErrorException                   Raise when group and resource not belong to the same VO or cant
   *                                                  properly fix attributes of group's members after removing group
   *                                                  from resource.
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   * @throws GroupNotDefinedOnResourceException       Group was never assigned to this resource
   * @throws GroupAlreadyRemovedFromResourceException if there are 0 rows affected by deleting from DB
   */
  void removeGroupFromResource(PerunSession perunSession, Group group, Resource resource)
      throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException,
      GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException;

  /**
   * Remove group from the resources. After removing, check attributes and fix them if it is needed.
   *
   * @param perunSession
   * @param groups       list of groups
   * @param resources    list of resources
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws ResourceNotExistsException
   * @throws GroupNotDefinedOnResourceException
   * @throws GroupAlreadyRemovedFromResourceException
   */
  void removeGroupFromResources(PerunSession perunSession, Group groups, List<Resource> resources)
      throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException,
      GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException;

  /**
   * Remove groups from a resource. After removing, check attributes and fix them if it is needed.
   *
   * @param perunSession
   * @param groups       list of groups
   * @param resource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws ResourceNotExistsException
   * @throws GroupNotDefinedOnResourceException
   * @throws GroupAlreadyRemovedFromResourceException
   */
  void removeGroupsFromResource(PerunSession perunSession, List<Group> groups, Resource resource)
      throws PrivilegeException, GroupNotExistsException, ResourceNotExistsException,
      GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException;

  /**
   * Unset ResourceSelfService role to given group for given resource.
   *
   * @param sess     session
   * @param resource resource
   * @param group    group
   * @throws InternalErrorException internal error
   * @throws PrivilegeException     insufficient permissions
   * @throws GroupNotAdminException group did not have the role
   */
  void removeResourceSelfServiceGroup(PerunSession sess, Resource resource, Group group)
      throws PrivilegeException, GroupNotAdminException, ResourceNotExistsException, GroupNotExistsException;

  /**
   * Unset ResourceSelfService role to given user for given resource.
   *
   * @param sess     session
   * @param resource resource
   * @param user     user
   * @throws InternalErrorException internal error
   * @throws PrivilegeException     insufficient permissions
   * @throws UserNotAdminException  user did not have the role
   */
  void removeResourceSelfServiceUser(PerunSession sess, Resource resource, User user)
      throws PrivilegeException, UserNotAdminException, ResourceNotExistsException, UserNotExistsException;

  /**
   * Remove specific ResourceTag from existing Resource.
   *
   * @param perunSession
   * @param resourceTag
   * @param resource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceTagNotExistsException
   * @throws ResourceNotExistsException
   * @throws ResourceTagNotAssignedException
   */
  void removeResourceTagFromResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource)
      throws PrivilegeException, ResourceTagNotExistsException, ResourceNotExistsException,
      ResourceTagNotAssignedException;

  /**
   * Remove specific ResourceTags from existing Resource.
   *
   * @param perunSession
   * @param resourceTags
   * @param resource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceTagNotExistsException
   * @throws ResourceNotExistsException
   * @throws ResourceTagNotAssignedException
   */
  void removeResourceTagsFromResource(PerunSession perunSession, List<ResourceTag> resourceTags, Resource resource)
      throws PrivilegeException, ResourceTagNotExistsException, ResourceNotExistsException,
      ResourceTagNotAssignedException;

  /**
   * Remove service from resource.
   *
   * @param perunSession
   * @param resource
   * @param service
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   * @throws ServiceNotExistsException
   * @throws ServiceNotAssignedException
   */
  void removeService(PerunSession perunSession, Resource resource, Service service)
      throws PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceNotAssignedException;

  /**
   * Remove service from multiple resources in the same facility
   *
   * @param perunSession
   * @param resources
   * @param service
   * @throws PrivilegeException
   * @throws ResourceNotExistsException
   * @throws ServiceNotExistsException
   * @throws ServiceNotAssignedException
   * @throws FacilityMismatchException
   * @throws FacilityNotExistsException
   */
  void removeService(PerunSession perunSession, List<Resource> resources, Service service)
      throws PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceNotAssignedException,
      FacilityNotExistsException, FacilityMismatchException;

  /**
   * Remove services from resource. Optionally also removes tasks, their results or destinations associated with the
   * services on the resource's facility. This only happens for services which are not assigned to other resources on
   * the facility.
   *
   * @param perunSession
   * @param resource
   * @param services
   * @param removeTasks
   * @param removeTaskResults
   * @param removeDestinations
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   * @throws ServiceNotExistsException
   * @throws ServiceNotAssignedException
   * @throws FacilityNotExistsException
   */
  void removeServices(PerunSession perunSession, Resource resource, List<Service> services, boolean removeTasks,
                      boolean removeTaskResults, boolean removeDestinations)
      throws PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceNotAssignedException,
                 FacilityNotExistsException;

  /**
   * Set ban for member on resource.
   *
   * @param sess
   * @param banOnResource the ban
   * @return ban on resource
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws BanAlreadyExistsException
   * @throws ResourceNotExistsException
   */
  BanOnResource setBan(PerunSession sess, BanOnResource banOnResource)
      throws PrivilegeException, BanAlreadyExistsException, ResourceNotExistsException;

  /**
   * Update existing ban (description, validation timestamp)
   *
   * @param sess
   * @param banOnResource the specific ban
   * @return updated ban
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws MemberNotExistsException
   * @throws BanNotExistsException
   * @throws ResourceNotExistsException
   */
  BanOnResource updateBan(PerunSession sess, BanOnResource banOnResource)
      throws PrivilegeException, MemberNotExistsException, BanNotExistsException, ResourceNotExistsException;

  /**
   * Updates Resource.
   *
   * @param perunSession
   * @param resource
   * @return returns updated Resource
   * @throws ResourceNotExistsException
   * @throws ResourceExistsException
   * @throws PrivilegeException
   * @throws InternalErrorException
   */
  Resource updateResource(PerunSession perunSession, Resource resource)
      throws ResourceNotExistsException, PrivilegeException, ResourceExistsException;

  /**
   * Update existing Resource tag.
   *
   * @param perunSession
   * @param resourceTag
   * @return updated ResourceTag
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ResourceTagNotExistsException
   * @throws VoNotExistsException
   */
  ResourceTag updateResourceTag(PerunSession perunSession, ResourceTag resourceTag)
      throws PrivilegeException, ResourceTagNotExistsException, VoNotExistsException;
}
