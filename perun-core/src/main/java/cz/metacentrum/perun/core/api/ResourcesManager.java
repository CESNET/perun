package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.*;

import java.util.List;

/**
 * Manages resources.
 *
 * @author  Slavek Licehammer
 * @author
 */
public interface ResourcesManager {

	/**
	 * Searches for the Resource with specified id.
	 *
	 * @param perunSession
	 * @param id
	 *
	 * @return Resource with specified id
	 *
	 * @throws ResourceNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	Resource getResourceById(PerunSession perunSession, int id) throws InternalErrorException, PrivilegeException, ResourceNotExistsException;

	/**
	 * Search for the RichResource with specific id.
	 *
	 * @param perunSession
	 * @param id
	 *
	 * @return RichResource with specified id
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ResourceNotExistsException
	 */
	RichResource getRichResourceById(PerunSession perunSession, int id) throws InternalErrorException, PrivilegeException, ResourceNotExistsException;

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
	Resource getResourceByName(PerunSession sess, Vo vo, Facility facility, String name) throws InternalErrorException, ResourceNotExistsException, PrivilegeException, VoNotExistsException, FacilityNotExistsException;

	/**
	 * Inserts resource into DB.
	 *
	 * @param resource resource to create
	 * @param vo virtual organization
	 * @param facility facility
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	Resource createResource(PerunSession perunSession, Resource resource, Vo vo, Facility facility) throws InternalErrorException, PrivilegeException, VoNotExistsException, FacilityNotExistsException, ResourceExistsException;

	/**
	 * Copy "template" settings from user's another existing resource and create new resource with this template.
	 * The settings are attributes, services, tags (if exists), groups and their members (if the resources are from the same VO and withGroups is true)
	 * Template Resource can be from any of user's facilities.
	 *
	 * @param perunSession
	 * @param templateResource template resource to copy
	 * @param destinationResource destination resource containing destination facility, VO and resource name.
	 * @param withGroups if set to true and resources ARE from the same VO we also - copy all group-resource and member-resource attributes
	 *                   														   - assign all groups same as on templateResource
	 *	                 if set to true and resources ARE NOT from the same VO InternalErrorException is thrown,
	 *                   if set to false we will NOT copy groups and group related attributes.
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 */
	Resource copyResource(PerunSession perunSession, Resource templateResource, Resource destinationResource, boolean withGroups) throws InternalErrorException, ResourceNotExistsException, FacilityNotExistsException, PrivilegeException, VoNotExistsException, ResourceExistsException;

	/**
	 *  Deletes resource by id.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 * @throws ResourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group which is not affected by removing from DB
	 * @throws FacilityNotExistsException if facility of this resource not exists
	 */
	void deleteResource(PerunSession perunSession, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException, RelationExistsException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, FacilityNotExistsException;

	/**
	 *  Deletes all resources for the VO.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 * @throws ResourceAlreadyRemovedException if there are at least 1 resource not affected by deleting from DB
	 * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group which is not affected by removing from DB
	 */
	void deleteAllResources(PerunSession perunSession, Vo vo) throws InternalErrorException, VoNotExistsException, PrivilegeException, RelationExistsException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

	/**
	 * Get facility which belongs to the concrete resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return facility belonging to the resource
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	Facility getFacility(PerunSession perunSession, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException;

	/**
	 * Set Facility to resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @param facility
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 * @throws FacilityNotExistsException
	 */
	void setFacility(PerunSession perunSession, Resource resource, Facility facility) throws InternalErrorException, ResourceNotExistsException, PrivilegeException, FacilityNotExistsException;

	/**
	 * Get Vo which is tied to specified resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return vo tied to specified resource
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	Vo getVo(PerunSession perunSession, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException;

	/**
	 * Returns all members assigned to the resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return list of members assigned to the resource
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	List<Member> getAllowedMembers(PerunSession perunSession, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException;

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
	List<User> getAllowedUsers(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException;

	/**
	 * Assign group to a resource. Check if attributes for each member form group are valid. Fill members' attributes with missing value.
	 *
	 * @param perunSession
	 * @param group
	 * @param resource

	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 * @throws WrongAttributeValueException
	 * @throws GroupAlreadyAssignedException
	 * @throws WrongReferenceAttributeValueException
	 */
	void assignGroupToResource(PerunSession perunSession, Group group, Resource resource) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException;

	/**
	 * Assign groups to a resource. Check if attributes for each member from all groups are valid. Fill members' attributes with missing values.
	 *
	 * @param perunSession
	 * @param groups list of resources
	 * @param resource
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws GroupAlreadyAssignedException
	 */
	void assignGroupsToResource(PerunSession perunSession, List<Group> groups, Resource resource) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException;

	/**
	 * Assign group to the resources. Check if attributes for each member from group are valid. Fill members' attributes with missing values.
	 *
	 * @param perunSession
	 * @param group the group
	 * @param resources list of resources
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws GroupAlreadyAssignedException
	 */
	void assignGroupToResources(PerunSession perunSession, Group group, List<Resource> resources) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupAlreadyAssignedException;

	/**
	 * Remove group from a resource.
	 * After removing, check attributes and fix them if it is needed.
	 *
	 * @param perunSession
	 * @param group
	 * @param resource

	 * @throws InternalErrorException Raise when group and resource not belong to the same VO or cant properly fix attributes of group's members after removing group from resource.
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 * @throws GroupNotDefinedOnResourceException Group was never assigned to this resource
	 * @throws GroupAlreadyRemovedFromResourceException if there are 0 rows affected by deleting from DB
	 */
	void removeGroupFromResource(PerunSession perunSession, Group group, Resource resource) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException;

	/**
	 * Remove groups from a resource.
	 * After removing, check attributes and fix them if it is needed.
	 *
	 * @param perunSession
	 * @param groups list of groups
	 * @param resource
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotDefinedOnResourceException
	 * @throws GroupAlreadyRemovedFromResourceException
	 */
	void removeGroupsFromResource(PerunSession perunSession, List<Group> groups, Resource resource) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException;

	/**
	 * Remove group from the resources.
	 * After removing, check attributes and fix them if it is needed.
	 *
	 * @param perunSession
	 * @param groups list of groups
	 * @param resources list of resources
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotDefinedOnResourceException
	 * @throws GroupAlreadyRemovedFromResourceException
	 */
	void removeGroupFromResources(PerunSession perunSession, Group groups, List<Resource> resources) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupNotDefinedOnResourceException, GroupAlreadyRemovedFromResourceException;

	/**
	 * List all groups associated with the resource.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @return list of assigned group
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	List<Group> getAssignedGroups(PerunSession perunSession, Resource resource) throws InternalErrorException, PrivilegeException, ResourceNotExistsException;

	/**
	 * List all groups associated with the resource and member
	 *
	 * @param perunSession
	 * @param resource
	 * @param member
	 *
	 * @return list of assigned groups associated with the resource and member
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	List<Group> getAssignedGroups(PerunSession perunSession, Resource resource, Member member) throws InternalErrorException, PrivilegeException, ResourceNotExistsException;

	/**
	 * List all resources associated with the group.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @return list of assigned resources
	 *
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @throws PrivilegeException
	 */
	List<Resource> getAssignedResources(PerunSession perunSession, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException;

	/**
	 * List all rich resources associated with the group with facility property filled.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @return list of assigned rich resources
	 *
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @throws PrivilegeException
	 */
	List<RichResource> getAssignedRichResources(PerunSession perunSession, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException;

	/**
	 * List all services associated with the resource.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 * @return list of assigned resources
	 */
	List<Service> getAssignedServices(PerunSession perunSession, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException;

	/**
	 * Returns all members assigned to the resource.
	 *
	 * @param sess
	 * @param resource
	 * @return list of assigned members
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	public List<Member> getAssignedMembers(PerunSession sess, Resource resource) throws InternalErrorException, PrivilegeException;
	/**
	 * Returns all members assigned to the resource as RichMembers.
	 *
	 * @param sess
	 * @param resource
	 * @return list of assigned rich members
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	public List<RichMember> getAssignedRichMembers(PerunSession sess, Resource resource) throws InternalErrorException, PrivilegeException;

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
	void assignService(PerunSession perunSession, Resource resource, Service service) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceAlreadyAssignedException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Assign all services from services package to resouce.
	 *
	 * @param perunSession
	 * @param resource
	 * @param servicesPackage
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws ServicesPackageNotExistsException
	 */
	void assignServicesPackage(PerunSession perunSession, Resource resource, ServicesPackage servicesPackage) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, ServicesPackageNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Remove service from resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @param service
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 * @throws ServiceNotExistsException
	 * @throws ServiceNotAssignedException
	 */
	void removeService(PerunSession perunSession, Resource resource, Service service) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, ServiceNotExistsException, ServiceNotAssignedException;

	/**
	 * Remove from resource all services from services package.
	 *
	 * @param perunSession
	 * @param resource
	 * @param servicesPackage
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 * @throws ServicesPackageNotExistsException
	 */
	void removeServicesPackage(PerunSession perunSession, Resource resource, ServicesPackage servicesPackage) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, ServicesPackageNotExistsException;

	/**
	 * Get all VO resources. If called by resourceAdmin it returns
	 * only those resources of which is he admin.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @return list of resources
	 */
	List<Resource> getResources(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get all VO rich resources with facility property filled. If called by resourceAdmin
	 * it returns only those resources of which is he admin.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @return list of rich resources
	 */
	List<RichResource> getRichResources(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get all VO resources count.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @return count of vo resources
	 */
	int getResourcesCount(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get count of all resources.
	 *
	 * @param perunSession
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 *
	 * @return count of all resources
	 */
	int getResourcesCount(PerunSession perunSession) throws InternalErrorException, PrivilegeException;


	/**
	 * Get all resources which have the member access on.
	 *
	 * @param sess
	 * @param member
	 * @return list of resources which have the member acess on
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException
	 */
	List<Resource> getAllowedResources(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException;

	/**
	 * Get all resources where the member is assigned.
	 *
	 * @param sess
	 * @param member
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException;

	/**
	 * Get all resources where the member and the service are assigned.
	 *
	 * @param sess
	 * @param member
	 * @param service
	 * @return list of resources
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws ServiceNotExistsException
	 * @throws PrivilegeException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Member member, Service service) throws InternalErrorException, PrivilegeException, MemberNotExistsException, ServiceNotExistsException;

	/**
	 * Get all rich resources where the member is assigned with facility property filled.
	 *
	 * @param sess
	 * @param member
	 * @return list of rich resources
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 */
	List<RichResource> getAssignedRichResources(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException;

	/**
	 * Get all rich resources where the service and the member are assigned with facility property filled.
	 *
	 * @param sess
	 * @param member
	 * @param service
	 * @return list of rich resources
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws ServiceNotExistsException
	 * @throws PrivilegeException
	 */
	List<RichResource> getAssignedRichResources(PerunSession sess, Member member, Service service) throws InternalErrorException, PrivilegeException, MemberNotExistsException, ServiceNotExistsException;

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
	Resource updateResource(PerunSession perunSession, Resource resource) throws ResourceNotExistsException, InternalErrorException, PrivilegeException, ResourceExistsException;

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
	ResourceTag createResourceTag(PerunSession perunSession, ResourceTag resourceTag, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

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
	ResourceTag updateResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException, PrivilegeException, ResourceTagNotExistsException, VoNotExistsException;

	/**
	 * Delete existing Resource tag.
	 *
	 * @param perunSession
	 * @param resourceTag
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ResourceTagNotExistsException
	 * @throws VoNotExistsException
	 * @throws ResourceTagAlreadyAssignedException
	 */
	void deleteResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException, PrivilegeException, ResourceTagAlreadyAssignedException, ResourceTagNotExistsException, VoNotExistsException;

	/**
	 * Delete all ResourcesTags for specific VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ResourceTagAlreadyAssignedException
	 * @throws VoNotExistsException ¨
	 */
	void deleteAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException,ResourceTagAlreadyAssignedException, PrivilegeException, VoNotExistsException;

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
	void assignResourceTagToResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException, PrivilegeException, ResourceTagNotExistsException, ResourceNotExistsException, ResourceTagAlreadyAssignedException;

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
	void removeResourceTagFromResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException, PrivilegeException, ResourceTagNotExistsException, ResourceNotExistsException, ResourceTagNotAssignedException;

	/**
	 * Remove all Resource tags for specific resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ResourceNotExistsException
	 * @throws VoNotExistsException
	 */
	void removeAllResourcesTagFromResource(PerunSession perunSession, Resource resource) throws InternalErrorException, PrivilegeException, VoNotExistsException, ResourceNotExistsException;

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
	List<Resource> getAllResourcesByResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException, PrivilegeException, VoNotExistsException, ResourceTagNotExistsException;

	/**
	 * Get all resourcesTags for existing Vo.
	 *
	 * @param perunSession
	 * @param vo
	 * @return list of all resourcesTags for existing Vo
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException ¨
	 */
	List<ResourceTag> getAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

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
	List<ResourceTag> getAllResourcesTagsForResource(PerunSession perunSession, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException;


	/**
	 * Copy all attributes of the source resource to the destination resource.
	 * The attributes, that are in the destination resource and aren't in the source resource, are retained.
	 * The common attributes are replaced with the attributes from the source resource.
	 * The virtual attributes are not copied.
	 * @param sess
	 * @param sourceResource
	 * @param destinationResource
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ResourceNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 */
	public void copyAttributes(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, WrongReferenceAttributeValueException;

	/**
	 * Copy all services of the source resource to the destination resource.
	 * The services, that are in the destination resource and aren't in the source resource, are retained.
	 * The common services are replaced with the services from source resource.
	 *
	 * @param sourceResource
	 * @param destinationResource
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	public void copyServices(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Copy all groups of the source resource to the destination resource.
	 * The groups, that are in the destination resource and aren't in the source resource, are retained.
	 * The common groups are replaced with the groups from source resource.
	 *
	 * @param sourceResource
	 * @param destinationResource
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	public void copyGroups(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException;

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
	 * @throws PrivilegeException
	 * @throws ResourceNotExistsException
	 */
	List<User> getAdmins(PerunSession perunSession, Resource resource, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, ResourceNotExistsException;

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
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws ResourceNotExistsException
	 */
	List<RichUser> getRichAdmins(PerunSession perunSession, Resource resource, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException, PrivilegeException, ResourceNotExistsException;

	/**
	 * Returns list of resources, where the user is an admin.
	 *
	 * @param sess
	 * @param user
	 * @return list of resources, where the user is an admin.
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 */
	List<Resource> getResourcesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException;

	/**
	 * Gets list of all group administrators of the Resource.
	 *
	 * @param sess
	 * @param resource
	 * @return list of Group that are admins in the resource.
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Group> getAdminGroups(PerunSession sess, Resource resource) throws InternalErrorException, PrivilegeException, ResourceNotExistsException;

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
	 */
	void addAdmin(PerunSession sess, Resource resource, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException, AlreadyAdminException, ResourceNotExistsException;

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
	 */
	void addAdmin(PerunSession sess, Resource resource, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException, AlreadyAdminException, ResourceNotExistsException;

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
	void removeAdmin(PerunSession sess, Resource resource, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException, UserNotAdminException, ResourceNotExistsException;

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
	void removeAdmin(PerunSession sess, Resource resource, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupNotAdminException, ResourceNotExistsException;

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
	BanOnResource setBan(PerunSession sess, BanOnResource banOnResource) throws InternalErrorException, PrivilegeException, BanAlreadyExistsException, ResourceNotExistsException;

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
	BanOnResource getBanById(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException, PrivilegeException, ResourceNotExistsException;

	/**
	 * Get ban by memberId and resource id
	 *
	 * @param sess
	 * @param memberId the id of member
	 * @param resourceId the id of resource
	 * @return specific ban for member on resource
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException
	 * @throws ResourceNotExistsException
	 */
	BanOnResource getBan(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException, PrivilegeException, MemberNotExistsException, ResourceNotExistsException;

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
	List<BanOnResource> getBansForMember(PerunSession sess, int memberId) throws InternalErrorException, MemberNotExistsException, ResourceNotExistsException;

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
	List<BanOnResource> getBansForResource(PerunSession sess, int resourceId) throws InternalErrorException, PrivilegeException, ResourceNotExistsException;

	/**
	 * Update existing ban (description, validation timestamp)
	 *
	 * @param sess
	 * @param banOnResource the specific ban
	 * @return updated ban
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws FacilityNotExistsException
	 * @throws MemberNotExistsException
	 * @throws BanNotExistsException
	 * @throws ResourceNotExistsException
	 */
	BanOnResource updateBan(PerunSession sess, BanOnResource banOnResource) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, MemberNotExistsException, BanNotExistsException, ResourceNotExistsException;

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
	void removeBan(PerunSession sess, int banId) throws InternalErrorException, PrivilegeException, BanNotExistsException, ResourceNotExistsException;

	/**
	 * Remove specific ban by memberId and resourceId.
	 *
	 * @param sess
	 * @param memberId the id of member
	 * @param resourceId the id of resource
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	void removeBan(PerunSession sess, int memberId, int resourceId) throws InternalErrorException, BanNotExistsException, PrivilegeException, ResourceNotExistsException;
}
