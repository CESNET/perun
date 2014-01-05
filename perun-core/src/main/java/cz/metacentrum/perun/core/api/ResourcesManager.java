package cz.metacentrum.perun.core.api;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
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
import cz.metacentrum.perun.core.api.exceptions.SubGroupCannotBeRemovedException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

/**
 * Manages resources.
 * 
 * @author  Slavek Licehammer
 * @author 
 * @version $Id$
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
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  Resource createResource(PerunSession perunSession, Resource resource, Vo vo, Facility facility) throws InternalErrorException, PrivilegeException, VoNotExistsException, FacilityNotExistsException;

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
   * Get all VO resources.
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
   * Get all VO rich resources with facility property filled.
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
   * Updates Resource.
   *
   * @param perunSession
   * @param resource
   * @return returns updated Resource
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   * @throws InternalErrorException
   */
  Resource updateResource(PerunSession perunSession, Resource resource) throws ResourceNotExistsException, InternalErrorException, PrivilegeException;
  
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
  * Copy all attributes of the source resource to the destionation resource.
  * The attributes, that are in the destination resource and aren't in the source resource, are retained.
  * The common attributes are replaced with the attributes from the source resource.
  * The virtual attributes are not copied.
  * @param sess
  * @param sourceResource
  * @param destinationResource
  * @throws InternalErrorException wrong values of attributes or assignment of attributes
  * @throws PrivilegeException
  * @throws ResourceNotExistsException
  * @throws WrongReferenceAttributeValueException
  */
  public void copyAttributes(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, WrongReferenceAttributeValueException;

  /**
   * Copy all services of the source resource to the destionation resource.
   * The services, that are in the destination resource and aren't in the source resource, are retained.
   * The common services are replaced with the services from source resource.
   * 
   * @param sourceResource 
   * @param destinationResource 
   * @throws InternalErrorException
   * @throws ResourceNotExistsException
   * @throws PrivilegeException
   */
  public void copyServices(PerunSession sess, Resource sourceResource, Resource destinationResource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException;

  /**
   * Copy all groups of the source resource to the destionation resource.
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

}
