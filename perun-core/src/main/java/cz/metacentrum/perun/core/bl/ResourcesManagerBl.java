package cz.metacentrum.perun.core.bl;

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
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceTagNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

/**
 * Manages resources.
 * 
 * @author  Slavek Licehammer
 * @version $Id: 8abee750709dc3602bac09b9de38746ebcf7555a $
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
  Resource createResource(PerunSession perunSession, Resource resource, Vo vo, Facility facility) throws InternalErrorException, FacilityNotExistsException;

  /**
   *  Deletes resource by id.
   *
   * @param perunSession
   * @param resource
   * 
   * @throws InternalErrorException
   * @throws RelationExistsException
   * @throws ResourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
   * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group not affected by deleting from DB
   */
  void deleteResource(PerunSession perunSession, Resource resource) throws InternalErrorException, RelationExistsException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;
  
  /**
   *  Deletes all resources for the VO.
   *  
   * @param perunSession
   * @param vo
   * 
   * @throws InternalErrorException
   * @throws RelationExistsException
   * @throws ResourceAlreadyRemovedException if there is at least 1 resource not affected by deleting from DB
   * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group not affected by deleting from DB
   */
  void deleteAllResources(PerunSession perunSession, Vo vo) throws InternalErrorException, RelationExistsException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupAlreadyRemovedFromResourceException;

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
   * Get all users, who can assess the resource.
   * 
   * @param sess
   * @param resource
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> getAllowedUsers(PerunSession sess, Resource resource) throws InternalErrorException;
  
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
   * @throws ServiceNotExistsException
   * @throws ServiceAlreadyAssignedException
   */
  void assignService(PerunSession perunSession, Resource resource, Service service) throws InternalErrorException, ServiceNotExistsException, ServiceAlreadyAssignedException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Assign all services from services package to resouce.
   * 
   * @param perunSession
   * @param resource
   * @param servicesPackage
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException
   * @throws ServicesPackageNotExistsException
   * @throws InternalErrorException
   */
  void assignServicesPackage(PerunSession perunSession, Resource resource, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

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
    ServiceNotExistsException, ServiceNotAssignedException;

  /**
   * Remove from resource all services from services package.
   * 
   * @param perunSession
   * @param resource
   * @param servicesPackage
   * 
   * @throws InternalErrorException
   */
  void removeServicesPackage(PerunSession perunSession, Resource resource, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException;
  
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
   * @return count fo vo resources
   */
  int getResourcesCount(PerunSession perunSession, Vo vo) throws InternalErrorException;

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
   * @return list of resources which have the member acess on
   * 
   * @throws InternalErrorException
   */
  List<Resource> getAllowedResources(PerunSession sess, Member member) throws InternalErrorException;

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
  * Copy all attributes of the source resource to the destionation resource.
  * The attributes, that are in the destination resource and aren't in the source resource, are retained.
  * The common attributes are replaced with attributes from the source resource.
  * 
  * @param sess
  * @param sourceResource
  * @param destinationResource
  * @throws InternalErrorException
  * @throws WrongAttributeAssignmentException if there is no resource attribute
  * @throws WrongAttributeValueException if the attribute value is illegal
  * @throws WrongReferenceAttributeValueException if the attribute value is illegal
  */
  public void copyAttributes(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Copy all services of the source resource to the destionation resource.
   * The services, that are in the destination resource and aren't in the source resource, are retained.
   * The common services are replaced with services from source resource.
   * 
   * @param sourceResource 
   * @param destinationResource 
   * @throws InternalErrorException
   */
  public void copyServices(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException;

  /**
   * Copy all groups of the source resource to the destionation resource.
   * The groups, that are in the destination resource and aren't in the source resource, are retained.
   * The common groups are replaced with the groups from source resource.
   * 
   * @param sourceResource 
   * @param destinationResource 
   * @throws InternalErrorException
   */
  public void copyGroups(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException;

  
  void checkResourceExists(PerunSession sess, Resource resource) throws InternalErrorException, ResourceNotExistsException;
  
  void checkResourceTagExists(PerunSession sess, ResourceTag resourceTag) throws InternalErrorException, ResourceTagNotExistsException;
}
