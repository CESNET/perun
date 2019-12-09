package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

import java.util.List;

/**
 * Manages resources.
 *
 * @author  Slavek Licehammer
 */
public interface ResourcesManagerBl {

	/**
	 * Searches for the Resource with specified id.
	 *
	 * @param perunSession
	 * @param id
	 *
	 * @return Resource with specified id
	 *
	 * @throws InternalErrorException
	 */
	Resource getResourceById(PerunSession perunSession, int id) throws InternalErrorException, ResourceNotExistsException;

	/**
	 * Searches for the RichResource with specified id.
	 *
	 * @param perunSession
	 * @param id
	 *
	 * @return RichResource with specified id
	 *
	 * @throws InternalErrorException
	 */
	RichResource getRichResourceById(PerunSession perunSession, int id) throws InternalErrorException, ResourceNotExistsException;

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
	 */
	Resource getResourceByName(PerunSession sess, Vo vo, Facility facility, String name) throws InternalErrorException, ResourceNotExistsException;

	/**
	 * Inserts resource into DB.
	 *
	 * @param resource resource to create
	 * @throws InternalErrorException
	 */
	Resource createResource(PerunSession perunSession, Resource resource, Vo vo, Facility facility) throws InternalErrorException, ResourceExistsException;

	/**
	 * Copy "template" settings from user's another existing resource and create new resource with this template.
	 * The settings are attributes, services, tags (if exists), groups and their members (if the resources are from the same VO and withGroups is true)
	 * Template Resource can be from any of user's facilities.
	 *
	 * @param perunSession
	 * @param templateResource template resource to copy
	 * @param destinationResource destination resource containing IDs of destination facility, VO and resource name.
	 * @param withGroups if set to true and resources ARE from the same VO we also
	 *                      copy all group-resource and member-resource attributes and assign all groups same as on templateResource,
	 *                   if set to true and resources ARE NOT from the same VO InternalErrorException is thrown,
	 *                   if set to false we will NOT copy groups and group related attributes.
	 * @throws ResourceExistsException
	 * @throws InternalErrorException
	 */
	Resource copyResource(PerunSession perunSession, Resource templateResource, Resource destinationResource, boolean withGroups) throws ResourceExistsException, InternalErrorException;

	/**
	 *  Deletes resource by id.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @throws InternalErrorException
	 * @throws ResourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group not affected by deleting from DB
	 */
	void deleteResource(PerunSession perunSession, Resource resource) throws InternalErrorException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

	/**
	 *  Deletes all resources for the VO.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @throws InternalErrorException
	 * @throws ResourceAlreadyRemovedException if there is at least 1 resource not affected by deleting from DB
	 * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group not affected by deleting from DB
	 */
	void deleteAllResources(PerunSession perunSession, Vo vo) throws InternalErrorException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

	/**
	 * Get facility which belongs to the concrete resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return facility belonging to the resource
	 *
	 * @throws InternalErrorException
	 */
	Facility getFacility(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Get Vo which is tied to specified resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return vo tied to specified resource
	 *
	 * @throws InternalErrorException
	 */
	Vo getVo(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Returns true if the user is assigned to the current resource, false otherwise.
	 * @param sess
	 * @param user
	 * @param resource
	 * @return true if the user is assigned to the current resource.
	 * @throws InternalErrorException
	 */
	boolean isUserAssigned(PerunSession sess, User user, Resource resource) throws InternalErrorException;

	/**
	 * Returns true if the user is allowed to the current resource, false otherwise.
	 * @param sess
	 * @param user
	 * @param resource
	 * @return true if the user is allowed to the current resource.
	 * @throws InternalErrorException
	 */
	boolean isUserAllowed(PerunSession sess, User user, Resource resource) throws InternalErrorException;

	/**
	 * Returns true if the group is assigned to the current resource, false otherwise.
	 * @param sess
	 * @param group
	 * @param resource
	 * @return true if the group is assigned to the current resource.
	 * @throws InternalErrorException
	 */
	boolean isGroupAssigned(PerunSession sess, Group group, Resource resource) throws InternalErrorException;

	/**
	 * Returns all members who can access the resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return list of members assigned to the resource
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getAllowedMembers(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Returns all members who can access the resource and who are also valid in at least one group associated to the resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return list of members assigned to the resource
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getAllowedMembersNotExpired(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Returns all members assigned to the resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return list of members assigned to the resource
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getAssignedMembers(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Returns all members assigned to the resource as RichMembers.
	 *
	 * @param perunSession
	 * @param resource
	 * @return list of rich members assigned to the resource
	 *
	 * @throws InternalErrorException
	 */
	List<RichMember> getAssignedRichMembers(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Get all users, who can assess the resource.
	 *
	 * @param sess
	 * @param resource
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getAllowedUsers(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Get all users, who can assess the resource and who are not expired in at least one group associated to the resource.
	 *
	 * @param sess
	 * @param resource
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getAllowedUsersNotExpired(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Assign group to a resource. Check if attributes for each member form group are valid. Fill members' attributes with missing value.
	 *
	 * @param perunSession
	 * @param group
	 * @param resource

	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws GroupAlreadyAssignedException
	 * @throws WrongReferenceAttributeValueException
	 */
	void assignGroupToResource(PerunSession perunSession, Group group, Resource resource) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException;

	/**
	 * Assign groups to a resource. Check if attributes for each member from all groups are valid. Fill members' attributes with missing values.
	 *
	 * @param perunSession
	 * @param groups list of resources
	 * @param resource
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws GroupAlreadyAssignedException
	 */
	void assignGroupsToResource(PerunSession perunSession, List<Group> groups, Resource resource) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException;

	/**
	 * Assign group to the resources. Check if attributes for each member from group are valid. Fill members' attributes with missing values.
	 *
	 * @param perunSession
	 * @param group the group
	 * @param resources list of resources
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws GroupAlreadyAssignedException
	 */
	void assignGroupToResources(PerunSession perunSession, Group group, List<Resource> resources) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException;

	/**
	 * Remove group from a resource.
	 * After removing, check attributes and fix them if it is needed.
	 *
	 * @param perunSession
	 * @param group
	 * @param resource

	 * @throws InternalErrorException Raise when group and resource not belong to the same VO or cant properly fix attributes of group's members after removing group from resource.
	 * @throws ResourceNotExistsException
	 * @throws GroupNotDefinedOnResourceException Group was never assigned to this resource
	 * @throws GroupAlreadyRemovedFromResourceException there are 0 rows affected by deleting from DB
	 */
	void removeGroupFromResource(PerunSession perunSession, Group group, Resource resource) throws InternalErrorException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException;

	/**
	 * Remove groups from a resource.
	 * After removing, check attributes and fix them if it is needed.
	 *
	 * @param perunSession
	 * @param groups list of groups
	 * @param resource
	 *
	 * @throws InternalErrorException
	 * @throws GroupNotDefinedOnResourceException
	 * @throws GroupAlreadyRemovedFromResourceException
	 */
	void removeGroupsFromResource(PerunSession perunSession, List<Group> groups, Resource resource) throws InternalErrorException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException;

	/**
	 * Remove group from resources.
	 * After removing, check attributes and fix them if it is needed.
	 *
	 * @param perunSession
	 * @param group the group
	 * @param resources list of resources
	 *
	 * @throws InternalErrorException
	 * @throws GroupNotDefinedOnResourceException
	 * @throws GroupAlreadyRemovedFromResourceException
	 */
	void removeGroupFromResources(PerunSession perunSession, Group group, List<Resource> resources) throws InternalErrorException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException;

	/**
	 * List all groups associated with the resource.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @return list of assigned group
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroups(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * List all groups associated with the resource where Member is a member.
	 *
	 * @param perunSession
	 * @param resource
	 * @param member
	 *
	 * @return list of assigned groups
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroups(PerunSession perunSession, Resource resource, Member member) throws InternalErrorException;

	/**
	 * List all resources associated with the group.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @return list of assigned resources
	 *
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession perunSession, Group group) throws InternalErrorException;

	/**
	 * List all rich resources associated with the group with facility property filled.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @return list of assigned rich resources
	 *
	 * @throws InternalErrorException
	 */
	List<RichResource> getAssignedRichResources(PerunSession perunSession, Group group) throws InternalErrorException;

	/**
	 * List all services associated with the resource.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @throws InternalErrorException
	 * @return list of assigned resources
	 */
	List<Service> getAssignedServices(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Assign service to resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @param service
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws ServiceAlreadyAssignedException
	 */
	void assignService(PerunSession perunSession, Resource resource, Service service) throws InternalErrorException, ServiceAlreadyAssignedException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Assign all services from services package to resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @param servicesPackage
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws InternalErrorException
	 */
	void assignServicesPackage(PerunSession perunSession, Resource resource, ServicesPackage servicesPackage) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Remove service from resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @param service
	 *
	 * @throws InternalErrorException
	 * @throws ServiceNotAssignedException
	 */
	void removeService(PerunSession perunSession, Resource resource, Service service) throws InternalErrorException,
		ServiceNotAssignedException;

	/**
	 * Remove from resource all services from services package.
	 *
	 * @param perunSession
	 * @param resource
	 * @param servicesPackage
	 *
	 * @throws InternalErrorException
	 */
	void removeServicesPackage(PerunSession perunSession, Resource resource, ServicesPackage servicesPackage) throws InternalErrorException;

	/**
	 * Get all VO resources.
	 *
	 * @param perunSession
	 * @param vo

	 * @throws InternalErrorException
	 * @return list of resources
	 */
	List<Resource> getResources(PerunSession perunSession, Vo vo) throws InternalErrorException;

	/**
	 * Get all VO rich resources with facility property filled.
	 *
	 * @param perunSession
	 * @param vo

	 * @throws InternalErrorException
	 * @return list of rich resources
	 */
	List<RichResource> getRichResources(PerunSession perunSession, Vo vo) throws InternalErrorException;

	/**
	 * Get all VO resources count.
	 *
	 * @param perunSession
	 * @param vo

	 * @throws InternalErrorException
	 * @return count fo vo resources
	 */
	int getResourcesCount(PerunSession perunSession, Vo vo) throws InternalErrorException;

	/**
	 * Get count of all resources.
	 *
	 * @param perunSession
	 *
	 * @return count of all resources
	 *
	 * @throws InternalErrorException
	 */
	int getResourcesCount(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Returns all resource which have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Resource> getResourcesByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Get all resources which have the member access on.
	 *
	 * @param sess
	 * @param member
	 * @return list of resources which have the member access on
	 *
	 * @throws InternalErrorException
	 */
	List<Resource> getAllowedResources(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Return all resources which are under the facility and has member of the user with status other than INVALID.
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 *
	 * @return list of resources allowed for user (user has there member with status other than INVALID)
	 * @throws InternalErrorException
	 */
	List<Resource> getAllowedResources(PerunSession sess, Facility facility, User user) throws InternalErrorException;

	/**
	 * Get all resources where the member is assigned.
	 *
	 * @param sess
	 * @param member
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Get all resources where the member and the service are assigned.
	 *
	 * @param sess
	 * @param member
	 * @param service
	 * @return list of resources
	 *
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Member member, Service service) throws InternalErrorException;

	/**
	 * Return List of assigned resources to user on the vo.
	 * If user is not member of Vo, return empty List;
	 *
	 * @param sess
	 * @param user
	 * @param vo
	 * @return return list of assigned resources or empty list if user is not member of Vo
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, User user, Vo vo) throws InternalErrorException;

	/**
	 * Get all rich resources where the member is assigned with facility property filled.
	 *
	 * @param sess
	 * @param member
	 * @return list of resources
	 *
	 * @throws InternalErrorException
	 */
	List<RichResource> getAssignedRichResources(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Get all rich resources where the service and the member are assigned with facility property filled.
	 *
	 * @param sess
	 * @param member
	 * @param service
	 * @return list of resources
	 *
	 * @throws InternalErrorException
	 */
	List<RichResource> getAssignedRichResources(PerunSession sess, Member member, Service service) throws InternalErrorException;

	/**
	 * Updates Resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return returns updated Resource
	 * @throws InternalErrorException
	 * @throws ResourceExistsException
	 */
	Resource updateResource(PerunSession perunSession, Resource resource) throws InternalErrorException, ResourceExistsException;


	/**
	 * Create new Resource tag.
	 *
	 * @param perunSession
	 * @param resourceTag
	 * @param vo
	 * @return new created resourceTag
	 * @throws InternalErrorException
	 */
	ResourceTag createResourceTag(PerunSession perunSession, ResourceTag resourceTag, Vo vo) throws InternalErrorException;

	/**
	 * Update existing Resource tag.
	 *
	 * @param perunSession
	 * @param resourceTag
	 * @return updated ResourceTag
	 * @throws InternalErrorException
	 */
	ResourceTag updateResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException;

	/**
	 * Delete existing Resource tag.
	 *
	 * @param perunSession
	 * @param resourceTag
	 * @throws InternalErrorException
	 * @throws ResourceTagAlreadyAssignedException
	 */
	void deleteResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException, ResourceTagAlreadyAssignedException;

	/**
	 * Delete all ResourcesTags for specific VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @throws InternalErrorException
	 * @throws ResourceTagAlreadyAssignedException
	 */
	void deleteAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException, ResourceTagAlreadyAssignedException;

	/**
	 * Assign existing ResourceTag on existing Resource.
	 *
	 * @param perunSession
	 * @param resourceTag
	 * @param resource
	 * @throws InternalErrorException
	 * @throws ResourceTagAlreadyAssignedException
	 */
	void assignResourceTagToResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException, ResourceTagAlreadyAssignedException;

	/**
	 * Remove specific ResourceTag from existing Resource.
	 *
	 * @param perunSession
	 * @param resourceTag
	 * @param resource
	 * @throws InternalErrorException
	 * @throws ResourceTagNotAssignedException
	 */
	void removeResourceTagFromResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException, ResourceTagNotAssignedException;

	/**
	 * Remove all existing Resource tags for specific resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @throws InternalErrorException
	 */
	void removeAllResourcesTagFromResource(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Get all resources in specific Vo (specific by resourceTag.getVoId) for existing resourceTag
	 *
	 * @param perunSession
	 * @param resourceTag
	 * @return list of Resources
	 * @throws InternalErrorException
	 */
	List<Resource> getAllResourcesByResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException;

	/**
	 * Get all resourcesTags for existing Vo.
	 *
	 * @param perunSession
	 * @param vo
	 * @return list of all resourcesTags for existing Vo
	 * @throws InternalErrorException
	 */
	List<ResourceTag> getAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException;

	/**
	 * Get all resourcesTags for existing Resource
	 *
	 * @param perunSession
	 * @param resource
	 * @return list of ResourcesTags
	 * @throws InternalErrorException
	 */
	List<ResourceTag> getAllResourcesTagsForResource(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Copy all attributes of the source resource to the destination resource.
	 * The attributes, that are in the destination resource and aren't in the source resource, are retained.
	 * The common attributes are replaced with attributes from the source resource.
	 * The virtual attributes are not copied.
	 *
	 * @param sess
	 * @param sourceResource
	 * @param destinationResource
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 */
	void copyAttributes(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, WrongReferenceAttributeValueException;

	/**
	 * Copy all services of the source resource to the destination resource.
	 * The services, that are in the destination resource and aren't in the source resource, are retained.
	 * The common services are replaced with services from source resource.
	 *
	 * @param sourceResource
	 * @param destinationResource
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	void copyServices(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Copy all groups of the source resource to the destination resource.
	 * The groups, that are in the destination resource and aren't in the source resource, are retained.
	 * The common groups are replaced with the groups from source resource.
	 *
	 * @param sourceResource
	 * @param destinationResource
	 * @throws InternalErrorException
	 */
	void copyGroups(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException;


	void checkResourceExists(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException;

	void checkResourceTagExists(PerunSession sess, ResourceTag resourceTag) throws InternalErrorException, ResourceTagNotExistsException;

	/**
	 * Get list of all user administrators for supported role and given resource.
	 *
	 * If onlyDirectAdmins is true, return only direct users of the group for supported role.
	 *
	 * Supported roles: ResourceAdmin, VOAdmin
	 *
	 * @param perunSession
	 * @param resource
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of all user administrators of the given resource for supported role
	 *
	 * @throws InternalErrorException
	 */
	List<User> getAdmins(PerunSession perunSession, Resource resource, boolean onlyDirectAdmins) throws InternalErrorException;

	/**
	 * Get list of all richUser administrators for the resource and supported role with specific attributes.
	 *
	 * Supported roles: ResourceAdmin, VOAdmin
	 *
	 * If "onlyDirectAdmins" is "true", return only direct users of the group for supported role with specific attributes.
	 * If "allUserAttributes" is "true", do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @param specificAttributes list of specified attributes which are needed in object richUser
	 * @param allUserAttributes if true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of RichUser administrators for the resource and supported role with attributes
	 *
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichAdmins(PerunSession perunSession, Resource resource, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException;

	/**
	 * Returns list of resources, where the user is an admin.
	 *
	 * @param sess
	 * @param user
	 * @return list of resources, where the user is an admin.
	 * @throws InternalErrorException
	 */
	List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Return all resources for the facility and the vo where user is authorized as resource manager.
	 *
	 * @param sess
	 * @param facility the facility to which resources should be assigned to
	 * @param vo the vo to which resources should be assigned to
	 * @param authorizedUser user with resource manager role for all those resources
	 * @return list of defined resources where user has role resource manager
	 *
	 * @throws InternalErrorException
	 */
	List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Facility facility, Vo vo, User authorizedUser) throws InternalErrorException;

	/**
	 * Return all resources for the vo where user is authorized as resource manager.
	 * Including resources, where the user is a member of authorized group.
	 *
	 * @param sess
	 * @param vo the vo to which resources should be assigned to
	 * @param authorizedUser user with resource manager role for all those resources
	 * @return list of defined resources where user has role resource manager
	 *
	 * @throws InternalErrorException
	 */
	List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, Vo vo, User authorizedUser) throws InternalErrorException;

	/**
	 * Return all resources for the facility and the vo where the group is authorized as resource manager.
	 *
	 * @param sess
	 * @param facility the facility to which resources should be assigned to
	 * @param vo the vo to which resources should be assigned to
	 * @param authorizedGroup group with resource manager role for all those resources
	 * @return list of defined resources where groups has role resource manager
	 *
	 * @throws InternalErrorException
	 */
	List<Resource> getResourcesWhereGroupIsAdmin(PerunSession sess, Facility facility, Vo vo, Group authorizedGroup) throws InternalErrorException;

	/**
	 * Gets list of all group administrators of the Resource.
	 *
	 * @param sess
	 * @param resource
	 * @return list of Groups that are admins in the resource
	 * @throws InternalErrorException
	 */
	List<Group> getAdminGroups(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Set ban for member on resource
	 *
	 * @param sess
	 * @param banOnresource the ban
	 * @return ban on resource
	 * @throws InternalErrorException
	 * @throws BanAlreadyExistsException
	 */
	BanOnResource setBan(PerunSession sess, BanOnResource banOnresource) throws InternalErrorException, BanAlreadyExistsException;

	/**
	 * Get Ban for member on resource by it's id
	 *
	 * @param sess
	 * @param banId the ban id
	 * @return resource ban by it's id
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	BanOnResource getBanById(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Get true if any ban for member and resource exists.
	 *
	 * @param sess
	 * @param memberId id of member
	 * @param resourceId id of resource
	 * @return true if ban exists
	 * @throws InternalErrorException
	 */
	boolean banExists(PerunSession sess, int memberId, int resourceId) throws InternalErrorException;

	/**
	 * Get true if any band defined by id exists for any user and facility.
	 *
	 * @param sess
	 * @param banId id of ban
	 * @return true if ban exists
	 * @throws InternalErrorException
	 */
	boolean banExists(PerunSession sess, int banId) throws InternalErrorException;

	/**
	 * Check if ban already exists.
	 *
	 * Throw exception if no.
	 *
	 * @param sess
	 * @param memberId user id
	 * @param resourceId facility id
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	void checkBanExists(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Check if ban already exists.
	 *
	 * Throw exception if no.
	 *
	 * @param sess
	 * @param banId ban id
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	void checkBanExists(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Get specific resource ban.
	 *
	 * @param sess
	 * @param memberId the member id
	 * @param resourceId the resource id
	 * @return specific resource ban
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	BanOnResource getBan(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Get all resources bans for member.
	 *
	 * @param sess
	 * @param memberId the member id
	 * @return list of bans for member on any resource
	 * @throws InternalErrorException
	 */
	List<BanOnResource> getBansForMember(PerunSession sess, int memberId) throws InternalErrorException;

	/**
	 * Get all members bans for resource
	 *
	 * @param sess
	 * @param resourceId the resource id
	 * @return list of all members bans on resource
	 * @throws InternalErrorException
	 */
	List<BanOnResource> getBansForResource(PerunSession sess, int resourceId) throws InternalErrorException;

	/**
	 * Get all expired bans on any resource to now date
	 *
	 * @param sess
	 * @return list of expired bans for any resource
	 * @throws InternalErrorException
	 */
	List<BanOnResource> getAllExpiredBansOnResources(PerunSession sess) throws InternalErrorException;

	/**
	 * Update description and validity timestamp of specific ban.
	 *
	 * @param sess
	 * @param banOnResource ban to be updated
	 * @return updated ban
	 * @throws InternalErrorException
	 */
	BanOnResource updateBan(PerunSession sess, BanOnResource banOnResource) throws InternalErrorException;

	/**
	 * Remove ban by id from resources bans.
	 *
	 * @param sess
	 * @param banId id of specific ban
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	void removeBan(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Remove ban by member_id and facility_id
	 *
	 * @param sess
	 * @param memberId the id of member
	 * @param resourceId the id of resource
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	void removeBan(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Remove all expired bans on resources to now date.
	 *
	 * Get all expired bans and remove them one by one with auditing process.
	 * This method is for purpose of removing expired bans using some cron tool.
	 *
	 * @param sess
	 * @throws InternalErrorException
	 */
	void removeAllExpiredBansOnResources(PerunSession sess) throws InternalErrorException;

	/**
	 * Finds all resources.
	 *
	 * @param sess session
	 * @return list of all resources
	 * @throws InternalErrorException internal error
	 */
	List<Resource> getResources(PerunSession sess) throws InternalErrorException;

	/**
	 * Sets ResourceSelfService role to given user for given resource.
	 *
	 * @param sess     session
	 * @param resource resource
	 * @param user     user
	 * @throws AlreadyAdminException  already has role
	 * @throws InternalErrorException internal error
	 */
	void addResourceSelfServiceUser(PerunSession sess, Resource resource, User user) throws AlreadyAdminException, InternalErrorException;

	/**
	 * Sets ResourceSelfService role to given group for given resource.
	 *
	 * @param sess     session
	 * @param resource resource
	 * @param group    group
	 * @throws AlreadyAdminException  already has role
	 * @throws InternalErrorException internal error
	 */
	void addResourceSelfServiceGroup(PerunSession sess, Resource resource, Group group) throws AlreadyAdminException, InternalErrorException;

	/**
	 * Unset ResourceSelfService role to given user for given resource.
	 *
	 * @param sess     session
	 * @param resource resource
	 * @param user     user
	 * @throws UserNotAdminException  user did not have the role
	 * @throws InternalErrorException internal error
	 */
	void removeResourceSelfServiceUser(PerunSession sess, Resource resource, User user) throws UserNotAdminException, InternalErrorException;

	/**
	 * Unset ResourceSelfService role to given group for given resource.
	 *
	 * @param sess     session
	 * @param resource resource
	 * @param group    group
	 * @throws GroupNotAdminException group did not have the role
	 * @throws InternalErrorException internal error
	 */
	void removeResourceSelfServiceGroup(PerunSession sess, Resource resource, Group group) throws GroupNotAdminException, InternalErrorException;
}
