package cz.metacentrum.perun.rpc.methods;

import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum ResourcesManagerMethod implements ManagerMethod {

    /*#
     * Returns resource by its ID.
     *
     * @param id int Resource ID
     * @return Resource Found Resource
     */
    getResourceById {

        @Override
        public Resource call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourceById(parms.readInt("id"));
        }
    },

    /*#
    * Returns RichResource by its ID (also containing facility and VO inside)
    *
    * @param id int RichResource ID
    * @return RichResource Found RichResource
    */
    getRichResourceById {

        @Override
        public RichResource call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getRichResourceById(ac.getSession(), parms.readInt("id"));
        }
    },

    /*#
     * Creates a new resource.
     *
     * @param resource Resource JSON object
     * @param facility int Facility ID
     * @return Resource Created resource
     */
    createResource {

        @Override
        public Resource call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            return ac.getResourcesManager().createResource(ac.getSession(),
                    parms.read("resource", Resource.class),
                    ac.getVoById(parms.readInt("vo")),
                    ac.getFacilityById(parms.readInt("facility")));
        }
    },

    /*#
     * Updates a resource.
     *
     * @param resource Resource JSON object
     * @return Resource Updated resource
     */
    updateResource {

        @Override
        public Resource call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            return ac.getResourcesManager().updateResource(ac.getSession(),
                    parms.read("resource", Resource.class));
        }
    },

    /*#
     * Deletes a resource.
     *
     * @param resource int Resource ID
     */
    deleteResource {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getResourcesManager().deleteResource(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")));
            return null;
        }
    },

    /*#
     * Get facility which belongs to a specific resource.
     *
     * @param resource int Resource ID
     * @return Facility Found facility
     */
    getFacility {

        @Override
        public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getFacility(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")));
        }
    },

    /*#
     * Sets Facility to resource.
     *
     * @param resource int Resource ID
     * @param facility int Facility ID
     */
    setFacility {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getResourcesManager().setFacility(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")),
                    ac.getFacilityById(parms.readInt("facility")));
            return null;
        }
    },

    /*#
     * Returns Vo which is tied to a specific resource.
     *
     * @param resource int Resource ID
     * @return VirtualOrganization VirtualOrganization
     */
    getVo {

        @Override
        public Vo call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getVo(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")));
        }
    },

    /*#
     * Returns all members assigned to the resource.
     *
     * @param resource int Resource ID
     * @return List<Member> Members assigned to the resource
     */
    getAllowedMembers {

        @Override
        public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getAllowedMembers(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")));
        }
    },

    /*#
     * Returns all users assigned to the resource.
     *
     * @param resource int Resource ID
     * @return List<User> Users assigned to the resource
     */
    getAllowedUsers {

        @Override
        public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getAllowedUsers(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")));
        }
    },

    /*#
     * Assign group to a resource. Check if attributes for each member form group are valid. Fill members' attributes with missing value.
     *
     * @param group int Group ID
     * @param resource int Resource ID
     */
    assignGroupToResource {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getResourcesManager().assignGroupToResource(ac.getSession(),
                    ac.getGroupById(parms.readInt("group")),
                    ac.getResourceById(parms.readInt("resource")));
            return null;
        }
    },
    /*#
     * Remove group from a resource.
     * After removing, check attributes and fix them if it is needed.
     *
     * @param group int Group ID
     * @param resource int Resource ID
     */
    removeGroupFromResource {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getResourcesManager().removeGroupFromResource(ac.getSession(),
                    ac.getGroupById(parms.readInt("group")),
                    ac.getResourceById(parms.readInt("resource")));
            return null;
        }
    },

    /*#
     * List all groups associated with the resource.
     *
     * @param resource int Resource ID
     * @return List<Group> Resource groups
     */
    getAssignedGroups {

        @Override
        public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getAssignedGroups(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")));
        }
    },

    /*#
     * List all resources associated with a group.
     *
     * @param group int Group ID
     * @return List<Resource> Resources
     */
  /*#
   * List all resources associated with a member's group.
   * 
   * @param member int Member ID
   * @return List<Resource> Resources
   */
    getAssignedResources {

        @Override
        public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
            if(parms.contains("member")) {
                return ac.getResourcesManager().getAssignedResources(ac.getSession(),
                        ac.getMemberById(parms.readInt("member")));
            } else {
                return ac.getResourcesManager().getAssignedResources(ac.getSession(),
                        ac.getGroupById(parms.readInt("group")));
            }
        }
    },

    /*#
     * List all rich resources associated with a group.
     *
     * @param group int Group ID
     * @return List<RichResource> Resources
     */
    /*#
     * List all rich resources associated with a member's group.
     *
     * @param member int RichResource ID
     * @return List<Resource> Resources
     */
    getAssignedRichResources {

        @Override
        public List<RichResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
            if(parms.contains("member")) {
                return ac.getResourcesManager().getAssignedRichResources(ac.getSession(),
                        ac.getMemberById(parms.readInt("member")));
            } else {
                return ac.getResourcesManager().getAssignedRichResources(ac.getSession(),
                        ac.getGroupById(parms.readInt("group")));
            }
        }
    },

    /*#
     * Assign service to resource.
     *
     * @param resource int Resource ID
     * @param service int Service ID
     */
    assignService {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getResourcesManager().assignService(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")),
                    ac.getServiceById(parms.readInt("service")));
            return null;
        }
    },

    /*#
     * Assign all services from services package to resource.
     *
     * @param resource int Resource ID
     * @param servicesPackage int Services package ID
     */
    assignServicesPackage {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getResourcesManager().assignServicesPackage(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")),
                    ac.getServicesPackageById(parms.readInt("servicesPackage")));
            return null;
        }
    },

    /*#
     * Removes a service from a resource.
     *
     * @param resource int Resource ID
     * @param service int Service ID
     */
    removeService {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getResourcesManager().removeService(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")),
                    ac.getServiceById(parms.readInt("service")));
            return null;
        }
    },

    /*#
     * Remove from resource all services from services package.
     *
     * @param resource int Resource ID
     * @param servicesPackage int Services package ID
     */
    removeServicesPackage {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getResourcesManager().removeServicesPackage(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")),
                    ac.getServicesPackageById(parms.readInt("servicesPackage")));
            return null;
        }
    },

    /*#
     * Get all VO resources.
     *
     * @param vo int VO ID
     * @return List<Resource> VO resources
     */
    getResources {

        @Override
        public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getResources(ac.getSession(), ac.getVoById(parms.readInt("vo")));
        }
    },

    /*#
     * Get all VO rich resources.
     *
     * @param vo int VO ID
     * @return List<RichResource> VO resources
     */
    getRichResources {

        @Override
        public List<RichResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getRichResources(ac.getSession(), ac.getVoById(parms.readInt("vo")));
        }
    },

    /*#
     * Returns number of VO resources
     * @param vo int VO ID
     * @return int VO resources count
     */
    getResourcesCount {
        @Override
        public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getResourcesCount(ac.getSession(), ac.getVoById(parms.readInt("vo")));
        }
    },

    /*#
     * Deletes all VO resources
     *
     * @param vo int VO ID
     */
    deleteAllResources {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getResourcesManager().deleteAllResources(ac.getSession(),
                    ac.getVoById(parms.readInt("vo")));
            return null;
        }
    },

    /*#
     * Get all resources which have the member access on.
     *
     * @param member int Member ID
     * @return List<Resource> VO resources
     */
    getAllowedResources {
        @Override
        public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getAllowedResources(ac.getSession(),
                    ac.getMemberById(parms.readInt("member")));
        }
    },

    /*#
     * Create new resource tag in VO
     *
     * @param resourceTag ResourceTag with tagName set
     * @param vo ID of VO to create tag for
     *
     * @return created ResourceTag with ID and VO_ID set
     */
    createResourceTag {
        @Override
        public ResourceTag call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().createResourceTag(ac.getSession(),
                    parms.read("resourceTag", ResourceTag.class),
                    ac.getVoById(parms.readInt("vo")));
        }
    },

    /*#
     * Update resource tag name by it's ID and VO_ID
     *
     * @param resourceTag ResourceTag with new tagName set
     *
     * @return updated ResourceTag with new tagName
     */
    updateResourceTag {
        @Override
        public ResourceTag call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().updateResourceTag(ac.getSession(),
                    parms.read("resourceTag", ResourceTag.class));
        }
    },

    /*#
     * Delete resource tag by it's ID and VO_ID
     *
     * @param resourceTag ResourceTag to delete
     */
    deleteResourceTag {
        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.getResourcesManager().deleteResourceTag(ac.getSession(),
                    parms.read("resourceTag", ResourceTag.class));
            return null;
        }
    },

    /*#
     * Delete all resources tags of VO
     *
     * @param vo ID of VO to delete all resources tags for
     */
    deleteAllResourcesTagsForVo {
        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.getResourcesManager().deleteAllResourcesTagsForVo(ac.getSession(),
                    ac.getVoById(parms.readInt("vo")));
            return null;
        }
    },

    /*#
     * Assign resource tag to resource
     *
     * @param resourceTag ResourceTag to assign
     * @param resource ID of Resource to assign tags for
     */
    assignResourceTagToResource {
        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.getResourcesManager().assignResourceTagToResource(ac.getSession(),
                    parms.read("resourceTag", ResourceTag.class),
                    ac.getResourceById(parms.readInt("resource")));
            return null;
        }
    },

    /*#
     * Remove resource tag from resource
     *
     * @param resourceTag ResourceTag to remove
     * @param resource ID of Resource to remove tags for
     */
    removeResourceTagFromResource {
        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.getResourcesManager().removeResourceTagFromResource(ac.getSession(),
                    parms.read("resourceTag", ResourceTag.class),
                    ac.getResourceById(parms.readInt("resource")));
            return null;
        }
    },

    /*#
     * Remove all resources tags from resource
     *
     * @param resource ID of Resource to remove all tags for
     */
    removeAllResourcesTagFromResource {
        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.getResourcesManager().removeAllResourcesTagFromResource(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")));
            return null;
        }
    },

    /*#
     * Get all resources with specific tag assigned
     *
     * @param resourceTag ResourceTag to get resources for
     *
     * @return List<Resource> all resources with specific tag assigned
     */
    getAllResourcesByResourceTag {
        @Override
        public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getAllResourcesByResourceTag(ac.getSession(),
                    parms.read("resourceTag", ResourceTag.class));
        }
    },

    /*#
     * Get all resource tags of VO
     *
     * @param vo ID of VO to get all resource tags for
     *
     * @return List<ResourceTag> all resources tags of VO
     */
    getAllResourcesTagsForVo {
        @Override
        public List<ResourceTag> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getAllResourcesTagsForVo(ac.getSession(),
                    ac.getVoById(parms.readInt("vo")));
        }
    },

    /*#
     * Get all resource tags of Resource
     *
     * @param resource ID of Resource to get all resource tags for
     *
     * @return List<ResourceTag> all resources tags of Resource
     */
    getAllResourcesTagsForResource {
        @Override
        public List<ResourceTag> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getResourcesManager().getAllResourcesTagsForResource(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")));
        }
    },

    /*#
     * List all services associated with the resource.
     *
     * @param resource int Resource ID
     * @return List<Service> Services
     */
    getAssignedServices {

        @Override
        public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {

            return ac.getResourcesManager().getAssignedServices(ac.getSession(),
                    ac.getResourceById(parms.readInt("resource")));

        }
    };
}
