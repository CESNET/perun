package cz.metacentrum.perun.core.implApi;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;

/**
 * Manages resources.
 *
 * @author  Slavek Licehammer
 */
public interface ResourcesManagerImplApi {

	/**
	 * Searches for the Resource with specified id.
	 *
	 * @param perunSession
	 * @param id
	 * @return Resource with specified id
	 * @throws ResourceNotExistsException
	 * @throws InternalErrorException
	 */
	Resource getResourceById(PerunSession perunSession, int id) throws InternalErrorException, ResourceNotExistsException;

	/**
	 * Searches for the Rich Resource with specified id.
	 *
	 * @param perunSession
	 * @param id
	 * @return RichResource with specified id
	 * @throws ResourceNotExistsException
	 * @throws InternalErrorException
	 */
	RichResource getRichResourceById(PerunSession perunSession, int id) throws InternalErrorException, ResourceNotExistsException;

	/**
	 * Return resource by its name.
	 *
	 * @param sess
	 * @param name
	 * @param facility
	 * @param vo
	 * @return resource
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 */
	Resource getResourceByName(PerunSession sess, Vo vo, Facility facility, String name) throws InternalErrorException, ResourceNotExistsException;

	/**
	 * Creates the resource.
	 *
	 * @param perunSession
	 * @param vo
	 * @param resource
	 * @param facility
	 * @return newly created resource with id
	 * @throws InternalErrorException
	 */
	Resource createResource(PerunSession perunSession, Vo vo, Resource resource, Facility facility) throws InternalErrorException;

	/**
	 *  Deletes resource by id.
	 *
	 * @param perunSession
	 * @param vo
	 * @param resource
	 * @throws InternalErrorException
	 * @throws ResourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	void deleteResource(PerunSession perunSession, Vo vo, Resource resource) throws InternalErrorException, ResourceAlreadyRemovedException;

	/**
	 * Get facility id which belongs to the concrete resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return facility id
	 *
	 * @throws InternalErrorException
	 */
	int getFacilityId(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Set Facility to resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @param facility
	 *
	 * @throws InternalErrorException
	 */
	void setFacility(PerunSession perunSession, Resource resource, Facility facility) throws InternalErrorException;

	/**
	 * Returns all user assigned to the resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return list of user  assigned to the resource
	 *
	 * @throws InternalErrorException
	 */
	List<User> getUsers(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Assign group to a resource.
	 *
	 * @param perunSession
	 * @param group
	 * @param resource

	 * @throws InternalErrorException
	 * @throws GroupAlreadyAssignedException
	 */
	void assignGroupToResource(PerunSession perunSession, Group group, Resource resource) throws InternalErrorException, GroupAlreadyAssignedException;

	/**
	 * Remove group from a resource.
	 *
	 * @param perunSession
	 * @param group
	 * @param resource

	 * @throws InternalErrorException
	 * @throws GroupAlreadyRemovedFromResourceException if there are 0 rows affected by removing group from resource
	 */
	void removeGroupFromResource(PerunSession perunSession, Group group, Resource resource) throws InternalErrorException, GroupAlreadyRemovedFromResourceException;

	/**
	 * List all groups' id associated with the resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @param withSubGroups get all group (and subgroups) if it's true
	 *                      get only immediate groups (without subgroups) if it's false
	 *
	 * @throws InternalErrorException
	 * @return list of assigned groups' id
	 */
	// GROUPER OUT
	//List<Integer> getAssignedGroupsIds(PerunSession perunSession, Resource resource, boolean withSubGroups) throws InternalErrorException;

	/**
	 * Check if the user is assigned as a member on the selected resource.
	 *
	 * @param sess
	 * @param user
	 * @param resource
	 * @return true if the user is assigned as a member on the selected resource.
	 * @throws InternalErrorException
	 */
	boolean isUserAssigned(PerunSession sess, User user, Resource resource) throws InternalErrorException;

	/**
	 * List all resources associated with the group.
	 *
	 * @param perunSession
	 * @param vo
	 * @param group
	 *
	 * @throws InternalErrorException
	 * @return list of assigned resources
	 */
	List<Resource> getAssignedResources(PerunSession perunSession, Vo vo, Group group) throws InternalErrorException;

	/**
	 * List of all rich resources associated with the group.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 * @return list of assigned rich resources
	 */
	List<RichResource> getAssignedRichResources(PerunSession perunSession, Group group) throws InternalErrorException;

	/**
	 * Returns all rich resources where the member is assigned through the groups.
	 *
	 * @param sess
	 * @param member
	 * @return list of rich resources
	 * @throws InternalErrorException
	 */
	List<RichResource> getAssignedRichResources(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Returns all rich resources where the service and the member are assigned through the groups.
	 *
	 * @param sess
	 * @param member
	 * @param service
	 * @return list of rich resources
	 * @throws InternalErrorException
	 */
	List<RichResource> getAssignedRichResources(PerunSession sess, Member member, Service service) throws InternalErrorException;

	/**
	 * List of all resources assigned to the member defined by user and vo.
	 *
	 * @param sess
	 * @param user
	 * @param vo
	 * @return list of assigned resources
	 * @throws InternalErrorException
	 */
	// GROUPER OUT
	//List<Resource> getAssignedResources(PerunSession sess, User user, Vo vo) throws InternalErrorException;

	/**
	 * List all services' id associated with the resource.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @throws InternalErrorException
	 * @return list of assigned service' id
	 */
	List<Integer> getAssignedServices(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Assign service to resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @param service
	 *
	 * @throws InternalErrorException
	 * @throws ServiceAlreadyAssignedException
	 */
	void assignService(PerunSession perunSession, Resource resource, Service service) throws InternalErrorException, ServiceAlreadyAssignedException;

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
	void removeService(PerunSession perunSession, Resource resource, Service service) throws InternalErrorException, ServiceNotAssignedException;

	/**
	 * Check if resource exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param resource
	 * @return true if resource exists in underlaying data source, false otherwise
	 *
	 * @throws InternalErrorException
	 */
	boolean resourceExists(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Check if resource exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 */
	void checkResourceExists(PerunSession perunSession, Resource resource) throws InternalErrorException, ResourceNotExistsException;

	/**
	 * Check if resource tag exists in underlaying data source.
	 *
	 * @param sess
	 * @param resourceTag
	 * @throws InternalErrorException
	 * @throws ResourceTagNotExistsException
	 */
	void checkResourceTagExists(PerunSession sess, ResourceTag resourceTag) throws InternalErrorException, ResourceTagNotExistsException;

	/**
	 * Get all VO resources.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @throws InternalErrorException
	 * @return list of resources
	 */
	List<Resource> getResources(PerunSession perunSession, Vo vo) throws InternalErrorException;

	/**
	 * Get all VO rich resources.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @throws InternalErrorException
	 * @return list of rich resources
	 */
	List<RichResource> getRichResources(PerunSession perunSession, Vo vo) throws InternalErrorException;

	/**
	 * Get list of resources by theirs IDs.
	 *
	 * @param sess
	 * @param resourcesIds
	 * @return list of resources
	 * @throws InternalErrorException
	 */
	List<Resource> getResourcesByIds(PerunSession sess, List<Integer> resourcesIds) throws InternalErrorException;

	/**
	 * Get all VO resources count.
	 *
	 * @param perunSession
	 * @param vo

	 * @throws InternalErrorException
	 * @return count of vo resources
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
	 * Returns all resources which have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Resource> getResourcesByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Returns all users who are allowed on the defined resource.
	 *
	 * @param sess
	 * @param resource
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getAllowedUsers(PerunSession sess, Resource resource) throws InternalErrorException;


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
	 * Returns all members who are assigned on the defined resource.
	 *
	 * @param sess
	 * @param resource
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> getAssignedMembers(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Returns all members who are allowed on the defined resource.
	 *
	 * @param sess
	 * @param resource
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> getAllowedMembers(PerunSession sess, Resource resource) throws InternalErrorException;

	/**
	 * Returns all resources where the member is assigned through the groups.
	 *
	 * @param sess
	 * @param member
	 * @return list of resources
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Returns all resources where the service and the member are assigned through the groups.
	 *
	 * @param sess
	 * @param member
	 * @param service
	 * @return list of resources
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, Member member, Service service) throws InternalErrorException;

	/**
	 * Returns all resources where the user is assigned through the vo and groups.
	 *
	 * @param sess
	 * @param user
	 * @param vo
	 * @return list of resources
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession sess, User user, Vo vo) throws InternalErrorException;


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
	 * Updates Resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @return returns updated Resource
	 * @throws InternalErrorException
	 */
	Resource updateResource(PerunSession perunSession, Resource resource) throws InternalErrorException;

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
	 */
	void deleteResourceTag(PerunSession perunSession, ResourceTag resourceTag) throws InternalErrorException;

	/**
	 * Delete all ResourcesTags for specific VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @throws InternalErrorException
	 */
	void deleteAllResourcesTagsForVo(PerunSession perunSession, Vo vo) throws InternalErrorException;

	/**
	 * Assign existing ResourceTag on existing Resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @throws InternalErrorException
	 */
	void assignResourceTagToResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException;

	/**
	 * Remove specific ResourceTag from existing Resource.
	 *
	 * @param perunSession
	 * @param resourceTag
	 * @param resource
	 * @throws InternalErrorException
	 */
	void removeResourceTagFromResource(PerunSession perunSession, ResourceTag resourceTag, Resource resource) throws InternalErrorException;

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
}
