package cz.metacentrum.perun.rpc.methods;


import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.core.api.BanOnResource;

public enum ResourcesManagerMethod implements ManagerMethod {

	/*#
	 * Returns resource by its <code>id</code>.
	 *
	 * @param id int Resource <code>id</code>
	 * @return Resource Found Resource
	 */
	getResourceById {

		@Override
		public Resource call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getResourceById(parms.readInt("id"));
		}
	},

	/*#
     * Returns resource by its name, Vo <code>id</code> and Facility <code>id</code>.
     *
     * @param vo int VO <code>id</code>
     * @param facility int Facility <code>id</code>
     * @param name String resource name
     * @return Resource Found Resource based on the input.
     */
	getResourceByName {
		@Override
		public Resource call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getResourcesManager().getResourceByName(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							ac.getFacilityById(parms.readInt("facility")),
							parms.readString("name"));
		}
	},

	/*#
	 * Returns RichResource by its <code>id</code> (also containing facility and VO inside)
	 *
	 * @param id int RichResource <code>id</code>
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
	 * @param vo int virtual organization <code>id</code>
	 * @param facility int Facility <code>id</code>
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
	 * @param resource int Resource <code>id</code>
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
	 * @param resource int Resource <code>id</code>
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
	 * @param resource int Resource <code>id</code>
	 * @param facility int Facility <code>id</code>
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
	 * @param resource int Resource <code>id</code>
	 * @return Vo VirtualOrganization
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
	 * @param resource int Resource <code>id</code>
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
	 * @param resource int Resource <code>id</code>
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
	 * Assign group to a resource. Check if attributes for each member from group are valid. Fill members' attributes with missing value.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
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
	 * Assign groups to a resource. Check if attributes for each member from groups are valid. Fill members' attributes with missing values.
	 *
	 * @param groups List<Integer> list of groups IDs
	 * @param resource int Resource <code>id</code>
	 */
	assignGroupsToResource {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			List<Integer> ids = parms.readList("groups", Integer.class);
			List<Group> groups = new ArrayList<Group>();
			for (Integer i : ids) {
				groups.add(ac.getGroupById(i));
			}
			ac.getResourcesManager().assignGroupsToResource(ac.getSession(),
					groups,
					ac.getResourceById(parms.readInt("resource")));
			return null;
		}
	},

	/*#
	 * Assign group to resources. Check if attributes for each member from group are valid. Fill members' attributes with missing values.
	 *
	 * @param group int Group <code>id</code>
	 * @param resources List<Integer> list of resources IDs
	 */
	assignGroupToResources {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			List<Integer> ids = parms.readList("resources", Integer.class);
			List<Resource> resources = new ArrayList<Resource>();
			for (Integer i : ids) {
				resources.add(ac.getResourceById(i));
			}
			ac.getResourcesManager().assignGroupToResources(ac.getSession(),
					ac.getGroupById(parms.readInt("group")),
					resources);
			return null;
		}
	},

	/*#
	 * Remove group from a resource.
	 * After removing, check attributes and fix them if it is needed.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
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
	 * Remove groups from a resource.
	 * After removing, check attributes and fix them if it is needed.
	 *
	 * @param groups List<Group> list of group
	 * @param resource int Resource <code>id</code>
	 */
	removeGroupsFromResource {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			List<Integer> ids = parms.readList("groups", Integer.class);
			List<Group> groups = new ArrayList<Group>();
			for (Integer i : ids) {
				groups.add(ac.getGroupById(i));
			}
			ac.getResourcesManager().removeGroupsFromResource(ac.getSession(),
					groups,
					ac.getResourceById(parms.readInt("resource")));
			return null;
		}
	},

	/*#
	 * Remove group from resources.
	 * After removing, check attributes and fix them if it is needed.
	 *
	 * @param group int Group <code>id</code>
	 * @param resources List<Integer> list of resources IDs
	 */
	removeGroupFromResources {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			List<Integer> ids = parms.readList("resources", Integer.class);
			List<Resource> resources = new ArrayList<Resource>();
			for (Integer i : ids) {
				resources.add(ac.getResourceById(i));
			}
			ac.getResourcesManager().removeGroupFromResources(ac.getSession(),
					ac.getGroupById(parms.readInt("group")),
					resources);
			return null;
		}
	},

	/*#
	 * List all groups associated with the resource.
	 *
	 * @param resource int Resource <code>id</code>
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
	 * @param group int Group <code>id</code>
	 * @return List<Resource> Resources
	 */
	/*#
	 * List all resources associated with a member's group.
	 *
	 * @param member int Member <code>id</code>
	 * @return List<Resource> Resources
	 */
	getAssignedResources {

		@Override
		public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("member")) {
				if (parms.contains("service")) {
					return ac.getResourcesManager().getAssignedResources(ac.getSession(),
							ac.getMemberById(parms.readInt("member")), ac.getServiceById(parms.readInt("service")));
				} else {
					return ac.getResourcesManager().getAssignedResources(ac.getSession(),
							ac.getMemberById(parms.readInt("member")));
				}
			} else {
				return ac.getResourcesManager().getAssignedResources(ac.getSession(),
						ac.getGroupById(parms.readInt("group")));
			}
		}
	},

	/*#
	 * Get all rich resources where the service and the member are assigned with facility property filled.
	 *
	 * @param member int Member <code>id</code>
	 * @param service int Service <code>id</code>
	 * @return List<RichResource> List of rich resources
	 */
	/*#
	 * List all rich resources associated with a member's group.
	 *
	 * @param member int Member <code>id</code>
	 * @return List<RichResource> List of rich resources
	 */
	/*#
	 * List all rich resources associated with a group.
	 *
	 * @param group int Group <code>id</code>
	 * @return List<RichResource> List of rich resources
	 */
	getAssignedRichResources {

		@Override
		public List<RichResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("member")) {
				if (parms.contains("service")) {
					return ac.getResourcesManager().getAssignedRichResources(ac.getSession(),
							ac.getMemberById(parms.readInt("member")), ac.getServiceById(parms.readInt("service")));
				} else {
					return ac.getResourcesManager().getAssignedRichResources(ac.getSession(),
							ac.getMemberById(parms.readInt("member")));
				}
			} else {
				return ac.getResourcesManager().getAssignedRichResources(ac.getSession(),
						ac.getGroupById(parms.readInt("group")));
			}
		}
	},

	/*#
	 * Returns all members assigned to the resource.
	 *
	 * @param resource int Resource <code>id</code>
	 * @return List<Member> list of assigned members
	 */
	getAssignedMembers {
		@Override
		public List<Member> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getResourcesManager().getAssignedMembers(ac.getSession(),
				ac.getResourceById(parms.readInt("resource")));
		}
	},

	/*#
	 * Returns all members assigned to the resource as RichMembers.
	 *
	 * @param resource int Resource <code>id</code>
	 * @return List<RichMember> list of assigned rich members
	 */
	getAssignedRichMembers {
		@Override
		public List<RichMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getResourcesManager().getAssignedRichMembers(ac.getSession(),
				ac.getResourceById(parms.readInt("resource")));
		}
	},

	/*#
	 * Assign service to resource.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param service int Service <code>id</code>
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
	 * @param resource int Resource <code>id</code>
	 * @param servicesPackage int Services package <code>id</code>
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
	 * @param resource int Resource <code>id</code>
	 * @param service int Service <code>id</code>
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
	 * @param resource int Resource <code>id</code>
	 * @param servicesPackage int Services package <code>id</code>
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
	 * @param vo int VO <code>id</code>
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
	 * @param vo int VO <code>id</code>
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
	 *
	 * @param vo int VO <code>id</code>
	 * @return int VO resources count
	 */
	/*#
	 * Gets count of all users.

	 * @return int resources count
	 */
	getResourcesCount {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("vo")) {
				return ac.getResourcesManager().getResourcesCount(ac.getSession(), ac.getVoById(parms.readInt("vo")));
			} else {
				return ac.getResourcesManager().getResourcesCount(ac.getSession());
			}
		}
	},

	/*#
	 * Deletes all VO resources
	 *
	 * @param vo int VO <code>id</code>
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
	 * @param member int Member <code>id</code>
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
	 * @param resourceTag ResourceTag ResourceTag with tagName set
	 * @param vo int <code>id</code> of VO to create tag for
	 *
	 * @return ResourceTag created ResourceTag with <code>id</code> and VO_ID set
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
	 * Update resource tag name by it's <code>id</code> and VO_ID
	 *
	 * @param resourceTag ResourceTag ResourceTag with new tagName set
	 *
	 * @return ResourceTag updated ResourceTag with new tagName
	 */
	updateResourceTag {
		@Override
		public ResourceTag call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getResourcesManager().updateResourceTag(ac.getSession(),
					parms.read("resourceTag", ResourceTag.class));
		}
	},

	/*#
	 * Delete resource tag by it's <code>id</code> and VO_ID
	 *
	 * @param resourceTag ResourceTag ResourceTag to delete
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
	 * @param vo int <code>id</code> of VO to delete all resources tags for
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
	 * @param resourceTag ResourceTag ResourceTag to assign
	 * @param resource int <code>id</code> of Resource to assign tags for
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
	 * @param resourceTag ResourceTag ResourceTag to remove
	 * @param resource int <code>id</code> of Resource to remove tags for
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
	 * @param resource int <code>id</code> of Resource to remove all tags for
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
	 * @param resourceTag ResourceTag ResourceTag to get resources for
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
	 * @param vo int <code>id</code> of VO to get all resource tags for
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
	 * @param resource int <code>id</code> of Resource to get all resource tags for
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
	 * @param resource int Resource <code>id</code>
	 * @return List<Service> Services
	 */
	getAssignedServices {

		@Override
		public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getResourcesManager().getAssignedServices(ac.getSession(),
					ac.getResourceById(parms.readInt("resource")));

		}
	},

	/*#
	 *  Set ban for member on resource.
	 *
	 * @param banOnResource BanOnResource JSON object
	 * @return BanOnResource Created banOnResource
	 */
	setBan {

		@Override
		public BanOnResource call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getResourcesManager().setBan(ac.getSession(),
					parms.read("banOnResource", BanOnResource.class));

		}
	},

	/*#
	 *  Get Ban for member on resource by it's id.
	 *
	 * @param banId int BanOnResource <code>id</code>
	 * @return BanOnResource banOnResource
	 */
	getBanById {

		@Override
		public BanOnResource call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getResourcesManager().getBanById(ac.getSession(),
					parms.readInt("banId"));

		}
	},

	/*#
	 *  Get ban by memberId and resource id.
	 *
	 * @param memberId int Member <code>id</code>
	 * @param resourceId int Resource <code>id</code>
	 * @return BanOnResource banOnResource
	 */
	getBan {

		@Override
		public BanOnResource call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getResourcesManager().getBan(ac.getSession(),
					parms.readInt("memberId"), parms.readInt("resourceId"));

		}
	},

	/*#
	 * Get all bans for member on any resource.
	 *
	 * @param memberId int Member <code>id</code>
	 * @return List<BanOnResource> memberBansOnResources
	 */
	getBansForMember {

		@Override
		public List<BanOnResource> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getResourcesManager().getBansForMember(ac.getSession(),
					parms.readInt("memberId"));

		}
	},

	/*#
	 * Get all bans for members on the resource.
	 *
	 * @param resourceId int Resource <code>id</code>
	 * @return List<BanOnResource> membersBansOnResource
	 */
	getBansForResource {

		@Override
		public List<BanOnResource> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getResourcesManager().getBansForResource(ac.getSession(),
					parms.readInt("resourceId"));

		}
	},

	/*#
	 * Update existing ban (description, validation timestamp)
	 *
	 * @param banOnResource BanOnResource JSON object
	 * @return BanOnResource updated banOnResource
	 */
	updateBan {

		@Override
		public BanOnResource call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			
			return ac.getResourcesManager().updateBan(ac.getSession(),
					parms.read("banOnResource", BanOnResource.class));

		}
	},

	/*#
	 * Remove specific ban by it's id.
	 *
	 * @param banId int BanOnResource <code>id</code>
	 */
	/*#
	 * Remove specific ban by memberId and resourceId.
	 *
	 * @param memberId int Member <code>id</code>
	 * @param resourceId int Resource <code>id</code>
	 */
	removeBan {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if(parms.contains("banId")) {
				ac.getResourcesManager().removeBan(ac.getSession(),
					parms.readInt("banId"));
			} else {
				ac.getResourcesManager().removeBan(ac.getSession(),
					parms.readInt("memberId"), parms.readInt("resourceId"));
			}
			return null;
		}
	};
}