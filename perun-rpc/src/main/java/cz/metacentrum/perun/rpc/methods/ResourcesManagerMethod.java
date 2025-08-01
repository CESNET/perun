package cz.metacentrum.perun.rpc.methods;


import cz.metacentrum.perun.core.api.AssignedGroup;
import cz.metacentrum.perun.core.api.AssignedMember;
import cz.metacentrum.perun.core.api.AssignedResource;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.EnrichedBanOnResource;
import cz.metacentrum.perun.core.api.EnrichedResource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourceTag;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.ArrayList;
import java.util.List;

public enum ResourcesManagerMethod implements ManagerMethod {

  /*#
   * Returns all resources.
   *
   * @return List<Resource> All resources
   */
  getAllResources {
    @Override
    public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getResourcesManager().getAllResources(ac.getSession());
    }
  },

  /*#
   * Returns resource by its <code>id</code>.
   *
   * @param id int Resource <code>id</code>
   * @return Resource Found Resource
   */
  getResourceById {
    @Override
    public Resource call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getResourcesManager().getResourceById(ac.getSession(), parms.readInt("id"));
    }
  },

  /*#
   * Returns resources by their ids.
   *
   * @param ids List<Integer> list of resources IDs
   * @return List<Resource> resources with specified IDs
   */
  getResourcesByIds {
    @Override
    public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getResourcesManager().getResourcesByIds(ac.getSession(), parms.readList("ids", Integer.class));
    }
  },

  /*#
   * Find resource for given id and returns it with given attributes.
   * If attrNames are null or empty, all resource attributes are returned.
   *
   * @param id int resource id
   * @param attrNames List<String> names of attributes to return
   * @return EnrichedResource resource for given id with desired attributes
   * @throw ResourceNotExistsException if there is no resource with given id
   */
  /*#
   * Find resource for given id and returns it with all attributes.
   *
   * @param id int resource id
   * @return EnrichedResource resource for given id with desired attributes
   * @throw ResourceNotExistsException if there is no resource with given id
   */
  getEnrichedResourceById {
    @Override
    public EnrichedResource call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<String> attrNames = null;
      if (parms.contains("attrNames")) {
        attrNames = parms.readList("attrNames", String.class);
      }
      return ac.getResourcesManager().getEnrichedResourceById(ac.getSession(), parms.readInt("id"), attrNames);
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
      return ac.getResourcesManager().getResourceByName(ac.getSession(), ac.getVoById(parms.readInt("vo")),
          ac.getFacilityById(parms.readInt("facility")), parms.readString("name"));
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
   * Returns rich resources by their ids.
   *
   * @param ids List<Integer> list of rich resources IDs
   * @return List<RichResource> rich resources with specified IDs
   */
  getRichResourcesByIds {
    @Override
    public List<RichResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getResourcesManager().getRichResourcesByIds(ac.getSession(), parms.readList("ids", Integer.class));
    }
  },

  /*#
   * Creates a new resource.
   *
   * Resource object must contain name. Parameter description is optional. Other parameters are ignored.
   *
   * @param resource Resource JSON object
   * @param vo int virtual organization <code>id</code>
   * @param facility int Facility <code>id</code>
   * @return Resource Created resource
   * @exampleParam resource { "name" : "my new resource" }
   * @exampleParam vo 1
   * @exampleParam facility 12
   */
  /*#
   * Creates a new resource.
   *
   * @param name String name of a new resource
   * @param description String description of a new resource
   * @param vo int virtual organization <code>id</code>
   * @param facility int Facility <code>id</code>
   * @return Resource Created resource
   * @exampleParam name "my new resource"
   * @exampleParam description "New resource with information"
   * @exampleParam vo 1
   * @exampleParam facility 12
   */
  createResource {
    @Override
    public Resource call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("resource")) {
        return ac.getResourcesManager()
            .createResource(ac.getSession(), parms.read("resource", Resource.class), ac.getVoById(parms.readInt("vo")),
                ac.getFacilityById(parms.readInt("facility")));
      } else if (parms.contains("name") && parms.contains("description")) {
        String name = parms.readString("name");
        String description = parms.readString("description");
        Vo vo = ac.getVoById(parms.readInt("vo"));
        Facility facility = ac.getFacilityById(parms.readInt("facility"));
        Resource resource = new Resource(0, name, description, facility.getId(), vo.getId());
        return ac.getResourcesManager().createResource(ac.getSession(), resource, vo, facility);
      } else {
        throw new RpcException(RpcException.Type.WRONG_PARAMETER);
      }
    }
  },

  /*#
   * Copy "template" settings from user's another existing resource and create new resource with this template.
   * The settings are attributes, services, tags (if exists), groups and their members (if the resources are from the
   *  same VO and withGroups is true)
   * Template Resource can be from any of user's facilities.
   *
   * @param templateResource Resource template resource to copy
   * @param destinationResource Resource destination resource containing IDs of destination facility,
   *  VO and resource name.
   * @param withGroups boolean if set to true and resources ARE from the same VO we also
   *                      copy all group-resource and member-resource attributes and assign all groups same as on
   * templateResource
   *                   if set to true and resources ARE NOT from the same VO InternalErrorException is thrown,
   *                   if set to false we will NOT copy groups and group related attributes.
   * @return Resource new Resource with copied settings based on withGroups parameter.
   */
  copyResource {
    @Override
    public Resource call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getResourcesManager().copyResource(ac.getSession(), parms.read("templateResource", Resource.class),
          parms.read("destinationResource", Resource.class), parms.readBoolean("withGroups"));
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
      parms.stateChangingCheck();

      return ac.getResourcesManager().updateResource(ac.getSession(), parms.read("resource", Resource.class));
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
      parms.stateChangingCheck();

      ac.getResourcesManager().deleteResource(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
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
      return ac.getResourcesManager().getFacility(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
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
      return ac.getResourcesManager().getVo(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
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
      return ac.getResourcesManager().getAllowedMembers(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
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
      return ac.getResourcesManager().getAllowedUsers(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
    }
  },

  /*#
   * Assign group to a resource. Check if attributes for each member from group are valid. Fill members' attributes
   * with missing value. Work in sync/async mode.Provide options for creating inactive or automatic subgroups
   * group-resource assignments.
   *
   * @param group int Group <code>id</code>
   * @param resource int Resource <code>id</code>
   * @param async boolean asynchronous flag
   * @param assignInactive boolean flag for inactive group-resource assignment
   * @param autoAssignSubgroups boolean flag for automatic assignment of all subgroups
   */
  /*#
   * Assign group to a resource. Check if attributes for each member from group are valid. Fill members' attributes
   * with missing value.
   *
   * @param group int Group <code>id</code>
   * @param resource int Resource <code>id</code>
   */
  assignGroupToResource {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      boolean async = parms.contains("async") ? parms.readBoolean("async") : false;
      boolean assignInactive = parms.contains("assignInactive") ? parms.readBoolean("assignInactive") : false;
      boolean autoAssignSubgroups =
          parms.contains("autoAssignSubgroups") ? parms.readBoolean("autoAssignSubgroups") : false;

      ac.getResourcesManager().assignGroupToResource(ac.getSession(), ac.getGroupById(parms.readInt("group")),
          ac.getResourceById(parms.readInt("resource")), async, assignInactive, autoAssignSubgroups);
      return null;
    }
  },

  /*#
   * Assign groups to a resource. Check if attributes for each member from groups are valid. Fill members' attributes
   *  with missing values. Work in sync/async mode.Provide options for creating inactive or automatic subgroups
   * group-resource assignments.
   *
   * @param groups List<Integer> list of groups IDs
   * @param resource int Resource <code>id</code>
   * @param async boolean asynchronous flag
   * @param assignInactive boolean flag for inactive group-resource assignment
   * @param autoAssignSubgroups boolean flag for automatic assignment of all subgroups
   */
  /*#
   * Assign groups to a resource. Check if attributes for each member from groups are valid. Fill members' attributes
   *  with missing values.
   *
   * @param groups List<Integer> list of groups IDs
   * @param resource int Resource <code>id</code>
   */
  assignGroupsToResource {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      boolean async = parms.contains("async") ? parms.readBoolean("async") : false;
      boolean assignInactive = parms.contains("assignInactive") ? parms.readBoolean("assignInactive") : false;
      boolean autoAssignSubgroups =
          parms.contains("autoAssignSubgroups") ? parms.readBoolean("autoAssignSubgroups") : false;

      List<Integer> ids = parms.readList("groups", Integer.class);
      List<Group> groups = new ArrayList<Group>();
      for (Integer i : ids) {
        groups.add(ac.getGroupById(i));
      }
      ac.getResourcesManager()
          .assignGroupsToResource(ac.getSession(), groups, ac.getResourceById(parms.readInt("resource")), async,
              assignInactive, autoAssignSubgroups);
      return null;
    }
  },

  /*#
   * Assign group to resources. Check if attributes for each member from group are valid. Fill members' attributes
   * with missing values. Work in sync/async mode.Provide options for creating inactive or automatic subgroups
   * group-resource assignments.
   *
   * @param group int Group <code>id</code>
   * @param resources List<Integer> list of resources IDs
   * @param async boolean asynchronous flag
   * @param assignInactive boolean flag for inactive group-resource assignment
   * @param autoAssignSubgroups boolean flag for automatic assignment of all subgroups
   */
  /*#
   * Assign group to resources. Check if attributes for each member from group are valid. Fill members' attributes
   * with missing values.
   *
   * @param group int Group <code>id</code>
   * @param resources List<Integer> list of resources IDs
   */
  assignGroupToResources {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      boolean async = parms.contains("async") ? parms.readBoolean("async") : false;
      boolean assignInactive = parms.contains("assignInactive") ? parms.readBoolean("assignInactive") : false;
      boolean autoAssignSubgroups =
          parms.contains("autoAssignSubgroups") ? parms.readBoolean("autoAssignSubgroups") : false;

      List<Integer> ids = parms.readList("resources", Integer.class);
      List<Resource> resources = new ArrayList<Resource>();
      for (Integer i : ids) {
        resources.add(ac.getResourceById(i));
      }
      ac.getResourcesManager()
          .assignGroupToResources(ac.getSession(), ac.getGroupById(parms.readInt("group")), resources, async,
              assignInactive, autoAssignSubgroups);
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
      parms.stateChangingCheck();

      ac.getResourcesManager().removeGroupFromResource(ac.getSession(), ac.getGroupById(parms.readInt("group")),
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
      parms.stateChangingCheck();

      List<Integer> ids = parms.readList("groups", Integer.class);
      List<Group> groups = new ArrayList<Group>();
      for (Integer i : ids) {
        groups.add(ac.getGroupById(i));
      }
      ac.getResourcesManager()
          .removeGroupsFromResource(ac.getSession(), groups, ac.getResourceById(parms.readInt("resource")));
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
      parms.stateChangingCheck();

      List<Integer> ids = parms.readList("resources", Integer.class);
      List<Resource> resources = new ArrayList<Resource>();
      for (Integer i : ids) {
        resources.add(ac.getResourceById(i));
      }
      ac.getResourcesManager()
          .removeGroupFromResources(ac.getSession(), ac.getGroupById(parms.readInt("group")), resources);
      return null;
    }
  },

  /*#
   * List all groups associated with the resource.
   *
   * @param resource int Resource <code>id</code>
   * @return List<Group> Resource groups
   */
  /*#
   * List all groups associated with the resource and member
   *
   * @param resource int Resource <code>id</code>
   * @param member int Member <code>id</code>
   * @return List<Group> Resource groups with specified member
   */
  getAssignedGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("member")) {
        return ac.getResourcesManager()
            .getAssignedGroups(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
                ac.getMemberById(parms.readInt("member")));
      } else {
        return ac.getResourcesManager()
            .getAssignedGroups(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
      }
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
          return ac.getResourcesManager()
              .getAssignedResources(ac.getSession(), ac.getMemberById(parms.readInt("member")),
                  ac.getServiceById(parms.readInt("service")));
        } else {
          return ac.getResourcesManager()
              .getAssignedResources(ac.getSession(), ac.getMemberById(parms.readInt("member")));
        }
      } else {
        return ac.getResourcesManager().getAssignedResources(ac.getSession(), ac.getGroupById(parms.readInt("group")));
      }
    }
  },

  /*#
   * Returns all assigned resources with statuses where member is assigned through the groups.
   *
   * @param member int Member <code>id</code>
   * @return List<AssignedResource> Resources with statuses
   */
  getAssignedResourcesWithStatus {
    @Override
    public List<AssignedResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getResourcesManager()
          .getAssignedResourcesWithStatus(ac.getSession(), ac.getMemberById(parms.readInt("member")));
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
      if (parms.contains("member")) {
        if (parms.contains("service")) {
          return ac.getResourcesManager()
              .getAssignedRichResources(ac.getSession(), ac.getMemberById(parms.readInt("member")),
                  ac.getServiceById(parms.readInt("service")));
        } else {
          return ac.getResourcesManager()
              .getAssignedRichResources(ac.getSession(), ac.getMemberById(parms.readInt("member")));
        }
      } else {
        return ac.getResourcesManager()
            .getAssignedRichResources(ac.getSession(), ac.getGroupById(parms.readInt("group")));
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
      return ac.getResourcesManager()
          .getAssignedMembers(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
    }
  },

  /*#
   * Returns members of groups assigned to resource with status of group-resource assignment.
   * @param sess perunSession
   * @param resource resource
   * @return list of members of groups assigned to given resource
   *
   * @throw PrivilegeException insufficient permissions
   */
  getAssignedMembersWithStatus {
    @Override
    public List<AssignedMember> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getResourcesManager()
          .getAssignedMembersWithStatus(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
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
      return ac.getResourcesManager()
          .getAssignedRichMembers(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
    }
  },

  /*#
   * Adds a Resource admin.
   *
   * @param resource int Resource <code>id</code>
   * @param user int User <code>id</code>
   */
  /*#
   *  Adds a group administrator to the Resource.
   *
   *  @param resource int Resource <code>id</code>
   *  @param authorizedGroup int Group <code>id</code>
   */
  addAdmin {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      if (parms.contains("user")) {
        ac.getResourcesManager().addAdmin(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
            ac.getUserById(parms.readInt("user")));
      } else {
        ac.getResourcesManager().addAdmin(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
            ac.getGroupById(parms.readInt("authorizedGroup")));
      }
      return null;
    }
  },

  /*#
   * Checks whether the resource is the last one on the facility to have the provided services assigned.
   * Returns the services where this is the case.
   *
   * @param resource int Resource <code>id</code>
   * @param services List<Integer> list of services IDs
   * @return List<Service> list of services which the resource is the last to have assigned on its facility
   */
  isResourceLastAssignedServices {
    @Override
    public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<Integer> ids = parms.readList("services", Integer.class);
      List<Service> services = new ArrayList<>();

      for (Integer id : ids) {
        services.add(ac.getServiceById(id));
      }

      return ac.getResourcesManager().isResourceLastAssignedServices(ac.getSession(),
          ac.getResourceById(parms.readInt("resource")), services);
    }
  },

  /*#
   * Removes a Resource admin.
   *
   * @param resource int Resource <code>id</code>
   * @param user int User <code>id</code>
   */
  /*#
   *  Removes a group administrator of the Resource.
   *
   *  @param resource int Resource <code>id</code>
   *  @param authorizedGroup int Group <code>id</code>
   */
  removeAdmin {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      if (parms.contains("user")) {
        ac.getResourcesManager().removeAdmin(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
            ac.getUserById(parms.readInt("user")));
      } else {
        ac.getResourcesManager().removeAdmin(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
            ac.getGroupById(parms.readInt("authorizedGroup")));
      }
      return null;
    }
  },

  /*#
   * Get list of all resource administrators for supported role and given resource.
   * If some group is administrator of the given resource, all VALID members are included in the list.
   *
   * If onlyDirectAdmins is == true, return only direct admins of the group for supported role.
   *
   * @param resource int Resource <code>id</code>
   * @param onlyDirectAdmins boolean if true, get only direct resource administrators (if false, get both direct and
   * indirect)
   *
   * @return List<User> list of all resource administrators of the given resource for supported role
   */
  getAdmins {
    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getAdmins(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
            Role.RESOURCEADMIN, parms.readBoolean("onlyDirectAdmins"));
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get all Resource group admins.
   *
   * @param resource int Resource <code>id</code>
   * @return List<Group> admins
   */
  getAdminGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getAdminGroups(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
            Role.RESOURCEADMIN);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get list of all richUser administrators for the resource and supported role with specific attributes.
   * If some group is administrator of the given group, all VALID members are included in the list.
   *
   * If "onlyDirectAdmins" is true, return only direct admins of the resource for supported role with specific
   * attributes.
   * If "allUserAttributes" is true, do not specify attributes through list and return them all in objects richUser.
   * Ignoring list of specific attributes.
   *
   * @param resource int Resource <code>id</code>
   * @param specificAttributes List<String> list of specified attributes which are needed in object richUser
   * @param allUserAttributes int if == true, get all possible user attributes and ignore list of specificAttributes
   * (if false, get only specific attributes)
   * @param onlyDirectAdmins int if == true, get only direct resource administrators (if false, get both direct and
   * indirect)
   *
   * @return List<RichUser> list of RichUser administrators for the resource and supported role with attributes
   */
  getRichAdmins {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getRichAdmins(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
            parms.readList("specificAttributes", String.class), Role.RESOURCEADMIN,
            parms.readBoolean("onlyDirectAdmins"), parms.readBoolean("allUserAttributes"));
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Returns list of Resources for specified VO and Facility, where the user is an Administrator
   * Including resources, where the user is a VALID member of authorized group.
   *
   * @param facility int Facility <code>id</code>
   * @param vo int Vo <code>id</code>
   * @param user int User <code>id</code>
   * @return List<Resource> Found Resources
   */
  /*#
   * Returns list of Resources for specified VO and Facility, where the group is an Administrator.
   *
   * @param facility int Facility <code>id</code>
   * @param vo int Vo <code>id</code>
   * @param group int Group <code>id</code>
   * @return List<Resource> Found Resources
   */
  /*#
   * Returns list of Resources for specified VO, where the user is an Administrator.
   * Including resources, where the user is a VALID member of authorized group.
   *
   * @param vo int Vo <code>id</code>
   * @param user int User <code>id</code>
   * @return List<Resource> Found Resources
   */
  /*#
   * Returns list of all Resources, where the user is an Administrator.
   * Including resources, where the user is a VALID member of authorized group.
   *
   * @param user int User <code>id</code>
   * @return List<Resource> Found Resources
   */
  getResourcesWhereUserIsAdmin {
    @Override
    public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("facility") && parms.contains("vo")) {
        if (parms.contains("user")) {
          return ac.getResourcesManager()
              .getResourcesWhereUserIsAdmin(ac.getSession(), ac.getFacilityById(parms.readInt("facility")),
                  ac.getVoById(parms.readInt("vo")), ac.getUserById(parms.readInt("user")));
        } else if (parms.contains("group")) {
          return ac.getResourcesManager()
              .getResourcesWhereGroupIsAdmin(ac.getSession(), ac.getFacilityById(parms.readInt("facility")),
                  ac.getVoById(parms.readInt("vo")), ac.getGroupById(parms.readInt("group")));
        } else {
          throw new RpcException(RpcException.Type.MISSING_VALUE, "group or user");
        }
      } else if (parms.contains("user") && parms.contains("vo")) {
        return ac.getResourcesManager().getResourcesWhereUserIsAdmin(ac.getSession(), ac.getVoById(parms.readInt("vo")),
            ac.getUserById(parms.readInt("user")));
      } else if (parms.contains("user")) {
        return ac.getResourcesManager()
            .getResourcesWhereUserIsAdmin(ac.getSession(), ac.getUserById(parms.readInt("user")));
      } else {
        throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo or user");
      }
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
      parms.stateChangingCheck();

      ac.getResourcesManager().assignService(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
          ac.getServiceById(parms.readInt("service")));
      return null;
    }
  },

  /*#
   * Assign services to resource.
   *
   * @param resource int Resource <code>id</code>
   * @param services List<Integer> list of services IDs
   */
  assignServices {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      List<Integer> ids = parms.readList("services", Integer.class);
      List<Service> services = new ArrayList<>();

      for (Integer id : ids) {
        services.add(ac.getServiceById(id));
      }

      ac.getResourcesManager().assignServices(ac.getSession(), ac.getResourceById(parms.readInt("resource")), services);
      return null;
    }
  },

  /*#
   * Removes a service from a resource.
   *
   * @param resource int Resource <code>id</code>
   * @param service int Service <code>id</code>
   */

  /*#
   * Removes service from multiple resources in the same facility.
   *
   * @param resources List<Integer> Resource <code>id</code>
   * @param service int Service <code>id</code>
   */
  removeService {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("resources")) {
        List<Resource> resources = new ArrayList<>();

        for (Integer resourceId : parms.readList("resources", Integer.class)) {
          resources.add(ac.getResourceById(resourceId));
        }

        ac.getResourcesManager().removeService(ac.getSession(), resources, ac.getServiceById(parms.readInt("service")));
        return null;
      }

      ac.getResourcesManager().removeService(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
          ac.getServiceById(parms.readInt("service")));
      return null;
    }
  },

  /*#
   * Removes services from resource.
   *
   * @param resource int Resource <code>id</code>
   * @param services List<Integer> list of services IDs
   */

  /*#
   * Removes services from resource. Optionally also removes tasks, their results or destinations associated with the
   * services on the resource's facility. This only happens for services which are not assigned to other resources on
   * the facility.
   *
   * @param resource int Resource <code>id</code>
   * @param services List<Integer> list of services IDs
   * @param removeTasks boolean whether to also remove tasks (and results) associated with the services on the facility
   * @param removeTaskResults boolean whether to also remove task results associated with the services on the facility
   * @param removeDestinations boolean whether to also remove destinations associated with the services on the facility
   */
  removeServices {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      List<Integer> ids = parms.readList("services", Integer.class);
      List<Service> services = new ArrayList<>();

      for (Integer id : ids) {
        services.add(ac.getServiceById(id));
      }

      boolean removeTasks = false;
      boolean removeTaskResults = false;
      boolean removeDestinations = false;

      if (parms.contains("removeTasks")) {
        removeTasks = parms.readBoolean("removeTasks");
      }

      if (parms.contains("removeTaskResults")) {
        removeTaskResults = parms.readBoolean("removeTaskResults");
      }

      if (parms.contains("removeDestinations")) {
        removeDestinations = parms.readBoolean("removeDestinations");
      }

      ac.getResourcesManager().removeServices(ac.getSession(), ac.getResourceById(parms.readInt("resource")),
          services, removeTasks, removeTaskResults, removeDestinations);
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
   * Return all rich resources with mailing service(s) where given member is assigned.
   *
   * @param member int VO <code>id</code>
   * @return List<RichResource> member resources with mailing service
   * @throw MemberNotExistsException if member is not present (does not exist)
   */
  getMailingServiceRichResourcesWithMember {
    @Override
    public List<RichResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getResourcesManager()
          .getMailingServiceRichResourcesWithMember(ac.getSession(), ac.getMemberById(parms.readInt("member")));
    }
  },

  /*#
   * Find resources for given vo and attributes for given names. If the
   * attrNames are empty or null, return all attributes.
   *
   * @param vo int vo
   * @param attrNames List<String> names of attributes to return
   * @return EnrichedResource resources with desired attributes
   * @throw VoNotExistsException if there is no vo with given id
   */
  /*#
   * Find resources for given vo and all attributes.
   *
   * @param vo int vo
   * @return EnrichedResource resources with desired attributes
   * @throw VoNotExistsException if there is no vo with given id
   */
  getEnrichedResourcesForVo {
    @Override
    public List<EnrichedResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<String> attrNames = null;
      if (parms.contains("attrNames")) {
        attrNames = parms.readList("attrNames", String.class);
      }
      return ac.getResourcesManager()
          .getEnrichedResourcesForVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), attrNames);
    }
  },

  /*#
   * Find resources for given facility and attributes for given names. If the
   * attrNames are empty or null, return all attributes.
   *
   * @param facility int facility
   * @param attrNames List<String> names of attributes to return
   * @return List<EnrichedResource> resources with desired attributes
   * @throw FacilityNotExistsException if there is not facility with given id
   */
  /*#
   * Find resources for given facility and all attributes.
   *
   * @param facility int facility
   * @return List<EnrichedResource> resources with desired attributes
   * @throw FacilityNotExistsException if there is not facility with given id
   */
  getEnrichedResourcesForFacility {
    @Override
    public List<EnrichedResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<String> attrNames = null;
      if (parms.contains("attrNames")) {
        attrNames = parms.readList("attrNames", String.class);
      }
      return ac.getResourcesManager()
          .getEnrichedResourcesForFacility(ac.getSession(), ac.getFacilityById(parms.readInt("facility")), attrNames);
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
      if (parms.contains("vo")) {
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
      parms.stateChangingCheck();

      ac.getResourcesManager().deleteAllResources(ac.getSession(), ac.getVoById(parms.readInt("vo")));
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
      return ac.getResourcesManager().getAllowedResources(ac.getSession(), ac.getMemberById(parms.readInt("member")));
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
  /*#
   * Create new resource tag defined by tag name in VO
   *
   * @param tagName String tagName
   * @param vo int <code>id</code> of VO to create tag for
   *
   * @return ResourceTag created ResourceTag with <code>id</code> and VO_ID set
   */
  createResourceTag {
    @Override
    public ResourceTag call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("resourceTag")) {
        return ac.getResourcesManager().createResourceTag(ac.getSession(), parms.read("resourceTag", ResourceTag.class),
            ac.getVoById(parms.readInt("vo")));
      } else if (parms.contains("tagName")) {
        String tagName = parms.readString("tagName");
        Vo vo = ac.getVoById(parms.readInt("vo"));
        ResourceTag resourceTag = new ResourceTag(0, tagName, vo.getId());
        return ac.getResourcesManager().createResourceTag(ac.getSession(), resourceTag, vo);
      } else {
        throw new RpcException(RpcException.Type.WRONG_PARAMETER);
      }
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
      return ac.getResourcesManager().updateResourceTag(ac.getSession(), parms.read("resourceTag", ResourceTag.class));
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
      ac.getResourcesManager().deleteResourceTag(ac.getSession(), parms.read("resourceTag", ResourceTag.class));
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
      ac.getResourcesManager().deleteAllResourcesTagsForVo(ac.getSession(), ac.getVoById(parms.readInt("vo")));
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
      ac.getResourcesManager()
          .assignResourceTagToResource(ac.getSession(), parms.read("resourceTag", ResourceTag.class),
              ac.getResourceById(parms.readInt("resource")));
      return null;
    }
  },

  /*#
   * Assign resource tags to resource
   *
   * @param resourceTags List<ResourceTag> ResourceTags to assign
   * @param resource int <code>id</code> of Resource to assign tags for
   */
  assignResourceTagsToResource {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getResourcesManager()
          .assignResourceTagsToResource(ac.getSession(), parms.readList("resourceTags", ResourceTag.class),
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
      ac.getResourcesManager()
          .removeResourceTagFromResource(ac.getSession(), parms.read("resourceTag", ResourceTag.class),
              ac.getResourceById(parms.readInt("resource")));
      return null;
    }
  },

  /*#
   * Remove resource tags from resource
   *
   * @param resourceTags List<ResourceTag> ResourceTags to remove
   * @param resource int <code>id</code> of Resource to remove tags for
   */
  removeResourceTagsFromResource {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getResourcesManager()
          .removeResourceTagsFromResource(ac.getSession(), parms.readList("resourceTags", ResourceTag.class),
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
      ac.getResourcesManager()
          .removeAllResourcesTagFromResource(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
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
      return ac.getResourcesManager()
          .getAllResourcesByResourceTag(ac.getSession(), parms.read("resourceTag", ResourceTag.class));
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
      return ac.getResourcesManager().getAllResourcesTagsForVo(ac.getSession(), ac.getVoById(parms.readInt("vo")));
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
      return ac.getResourcesManager()
          .getAllResourcesTagsForResource(ac.getSession(), ac.getResourceById(parms.readInt("resource")));
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

      return ac.getResourcesManager()
          .getAssignedServices(ac.getSession(), ac.getResourceById(parms.readInt("resource")));

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
      parms.stateChangingCheck();

      return ac.getResourcesManager().setBan(ac.getSession(), parms.read("banOnResource", BanOnResource.class));

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

      return ac.getResourcesManager().getBanById(ac.getSession(), parms.readInt("banId"));

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

      return ac.getResourcesManager().getBan(ac.getSession(), parms.readInt("memberId"), parms.readInt("resourceId"));

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

      return ac.getResourcesManager().getBansForMember(ac.getSession(), parms.readInt("memberId"));

    }
  },

  /*#
   * Get all bans for members on the resource.
   *
   * @param resource int Resource <code>id</code>
   * @return List<BanOnResource> membersBansOnResource
   */
  getBansForResource {
    @Override
    public List<BanOnResource> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getResourcesManager().getBansForResource(ac.getSession(), parms.readInt("resource"));

    }
  },

  /*#
   *  Get all enriched bans for members on the resource.
   *
   * @param resource int Resource <code>id</code>
   * @param attrNames List<String> list of attribute names, if empty or null returns all user and member attributes
   * @return List<BanOnResource> enriched bans on resource
   * @throw ResourceNotExistsException
   */
  getEnrichedBansForResource {
    @Override
    public List<EnrichedBanOnResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<String> attrNames = null;
      if (parms.contains("attrNames")) {
        attrNames = parms.readList("attrNames", String.class);
      }
      return ac.getResourcesManager().getEnrichedBansForResource(ac.getSession(), parms.readInt("resource"), attrNames);
    }
  },

  /*#
   *  Get all enriched bans for user's members on resources.
   *
   * @param user int user <code>id</code>
   * @param attrNames List<String> list of attribute names, if empty or null returns all user and member attributes
   * @return List<BanOnResource> enriched bans for user
   * @throw UserNotExistsException
   */
  getEnrichedBansForUser {
    @Override
    public List<EnrichedBanOnResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<String> attrNames = null;
      if (parms.contains("attrNames")) {
        attrNames = parms.readList("attrNames", String.class);
      }
      return ac.getResourcesManager().getEnrichedBansForUser(ac.getSession(), parms.readInt("user"), attrNames);
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
      parms.stateChangingCheck();

      return ac.getResourcesManager().updateBan(ac.getSession(), parms.read("banOnResource", BanOnResource.class));

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
      parms.stateChangingCheck();

      if (parms.contains("banId")) {
        ac.getResourcesManager().removeBan(ac.getSession(), parms.readInt("banId"));
      } else {
        ac.getResourcesManager().removeBan(ac.getSession(), parms.readInt("memberId"), parms.readInt("resourceId"));
      }
      return null;
    }
  },

  /*#
   * Sets ResourceSelfService role to given user for given resource.
   *
   * @param resourceId int Resource <code>id</code>
   * @param userId int User <code>id</code>
   */
  addResourceSelfServiceUser {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getResourcesManager()
          .addResourceSelfServiceUser(ac.getSession(), ac.getResourceById(parms.readInt("resourceId")),
              ac.getUserById(parms.readInt("userId")));

      return null;
    }
  },

  /*#
   * Sets ResourceSelfService role to given group for given resource.
   *
   * @param resourceId int Resource <code>id</code>
   * @param groupId int Group <code>id</code>
   */
  addResourceSelfServiceGroup {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getResourcesManager()
          .addResourceSelfServiceGroup(ac.getSession(), ac.getResourceById(parms.readInt("resourceId")),
              ac.getGroupById(parms.readInt("groupId")));

      return null;
    }
  },

  /*#
   * Unset ResourceSelfService role to given user for given resource.
   *
   * @param resourceId int Resource <code>id</code>
   * @param userId int User <code>id</code>
   */
  removeResourceSelfServiceUser {
    @Override
    public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getResourcesManager()
          .removeResourceSelfServiceUser(ac.getSession(), ac.getResourceById(parms.readInt("resourceId")),
              ac.getUserById(parms.readInt("userId")));

      return null;
    }
  },

  /*#
   * Unset ResourceSelfService role to given group for given resource.
   *
   * @param resourceId int Resource <code>id</code>
   * @param groupId int Group <code>id</code>
   */
  removeResourceSelfServiceGroup {
    @Override
    public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getResourcesManager()
          .removeResourceSelfServiceGroup(ac.getSession(), ac.getResourceById(parms.readInt("resourceId")),
              ac.getGroupById(parms.readInt("groupId")));

      return null;
    }
  },

  /*#
   * Lists all of the resource assignments for the given group. Also, returns specified attributes and resource tags
   * for the resources. If attrNames are null or empty, all resource attributes are returned.
   *
   * @param group int Group <code>id</code>
   * @param attrNames List<String> names of attributes to return
   * @return List<AssignedResource> resources for given group with specified attributes and resource tags
   * @throw GroupNotExistsException when the group doesn't exist
   */
  getResourceAssignments {
    @Override
    public List<AssignedResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<String> attrNames = null;
      if (parms.contains("attrNames")) {
        attrNames = parms.readList("attrNames", String.class);
      }

      return ac.getResourcesManager()
          .getResourceAssignments(ac.getSession(), ac.getGroupById(parms.readInt("group")), attrNames);
    }
  },

  /*#
   * Lists all of the assigned groups for the given resource. Also, returns specified attributes
   * for the groups. If attrNames are null, all group attributes are returned.
   *
   * @param resource int Resource <code>id</code>
   * @param attrNames List<String> names of attributes to return
   * @return List<AssignedGroup> groups for given resource with specified attributes
   * @throw ResourceNotExistsException when the resource doesn't exist
   */
  getGroupAssignments {
    @Override
    public List<AssignedGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<String> attrNames = null;
      if (parms.contains("attrNames")) {
        attrNames = parms.readList("attrNames", String.class);
      }

      return ac.getResourcesManager()
          .getGroupAssignments(ac.getSession(), ac.getResourceById(parms.readInt("resource")), attrNames);
    }
  },

  /*#
   * Try to activate the group-resource status. If the async is set to false, the validation is performed
   * synchronously. The assignment will be either ACTIVE, in case of a successful synchronous call, or it will be
   * PROCESSING in case of an asynchronous call. After the async validation, the state can be either ACTIVE or
   * FAILED.
   *
   * @param group int Group <code>id</code>
   * @param resource int Resource <code>id</code>
   * @param async boolean if true the validation is performed asynchronously, default value is false
   * @throw GroupNotExistsException when the group doesn't exist
   * @throw ResourceNotExistsException when the resource doesn't exist
   * @throw WrongAttributeValueException when an attribute value has wrong/illegal syntax
   * @throw WrongReferenceAttributeValueException when an attribute value has wrong/illegal semantics
   * @throw GroupResourceMismatchException when the given group and resource are not from the same VO
   * @throw GroupNotDefinedOnResourceException when the group-resource assignment doesn't exist
   */
  activateGroupResourceAssignment {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getResourcesManager().activateGroupResourceAssignment(ac.getSession(), ac.getGroupById(parms.readInt("group")),
          ac.getResourceById(parms.readInt("resource")), parms.contains("async") ? parms.readBoolean("async") : false);

      return null;
    }
  },

  /*#
   * Deactivates the group-resource assignment. The assignment will become INACTIVE and will not be used to
   * allow users from the given group to the resource.
   *
   * @param group int Group <code>id</code>
   * @param resource int Resource <code>id</code>
   * @throw GroupNotExistsException when the group doesn't exist
   * @throw ResourceNotExistsException when the resource doesn't exist
   * @throw GroupNotDefinedOnResourceException when the group-resource assignment doesn't exist
   * @throw GroupResourceStatusException when trying to deactivate an assignment in PROCESSING state
   */
  deactivateGroupResourceAssignment {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getResourcesManager()
          .deactivateGroupResourceAssignment(ac.getSession(), ac.getGroupById(parms.readInt("group")),
              ac.getResourceById(parms.readInt("resource")));

      return null;
    }
  }
}
