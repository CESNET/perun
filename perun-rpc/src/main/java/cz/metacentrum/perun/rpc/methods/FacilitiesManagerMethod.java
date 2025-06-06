package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.EnrichedBanOnFacility;
import cz.metacentrum.perun.core.api.EnrichedFacility;
import cz.metacentrum.perun.core.api.EnrichedHost;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.FacilityWithAttributes;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichFacility;
import cz.metacentrum.perun.core.api.RichGroup;
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

public enum FacilitiesManagerMethod implements ManagerMethod {

  /*#
   * Searches for the Facility with specified id.
   *
   * @param id int Facility <code>id</code>
   * @return Facility Found facility
   */
  getFacilityById {
    @Override
    public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getFacilityById(ac.getSession(), parms.readInt("id"));
    }
  },

  /*#
   * Searches the Facility by its name.
   *
   * @param name String Facility name
   * @return Facility Found facility
   */
  getFacilityByName {
    @Override
    public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getFacilityByName(ac.getSession(), parms.readString("name"));
    }
  },

  /*#
   * Returns facilities by their IDs.
   *
   * @param ids List<Integer> list of facilities IDs
   * @return List<Facility> facilities with specified IDs
   */
  getFacilitiesByIds {
    @Override
    public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getFacilitiesByIds(ac.getSession(), parms.readList("ids", Integer.class));
    }
  },

  /*#
   * Lists all users assigned to facility containing resources where service is assigned.
   *
   * @param service int Service <code>id</code>
   * @param facilityName String Facility name
   * @return List<User> assigned users
   */
  /*#
   * Lists all users assigned to facility.
   *
   * @param facilityName String Facility name
   * @return List<User> assigned users
   */
  /*#
   * Lists all users assigned to facility containing resources where service is assigned.
   *
   * @param service int Service <code>id</code>
   * @param facility int Facility <code>id</code>
   * @return List<User> assigned users
   */
  /*#
   * Lists all users assigned to facility.
   *
   * @param facility int Facility <code>id</code>
   * @return List<User> assigned users
   */
  getAssignedUsers {
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("service")) {
        return ac.getFacilitiesManager()
            .getAssignedUsers(ac.getSession(), getFacility(ac, parms), ac.getServiceById(parms.readInt("service")));
      } else {
        return ac.getFacilitiesManager().getAssignedUsers(ac.getSession(), getFacility(ac, parms));
      }
    }
  },

  /*#
   * Gets all possible rich facilities with all their owners.
   *
   * @return List<RichFacility> rich facilities
   */
  getRichFacilities {
    @Override
    public List<RichFacility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getRichFacilities(ac.getSession());
    }
  },

  /*#
   * Searches for the Facilities by theirs destination.
   *
   * @param destination String Destination
   * @return Facility Found facility
   */
  getFacilitiesByDestination {
    @Override
    public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getFacilitiesByDestination(ac.getSession(), parms.readString("destination"));
    }
  },

  /*#
   * Returns all facilities that have set the attribute 'attributeName' with the value 'attributeValue'.
   * Searching only def and opt attributes.
   *
   * @param attributeName String
   * @param attributeValue String
   * @return List<Facility> facilities with the specified attribute
   */
  getFacilitiesByAttribute {
    @Override
    public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getFacilitiesByAttribute(ac.getSession(), parms.readString("attributeName"),
          parms.readString("attributeValue"));
    }
  },

  /*#
   * Searches (partially!) for facilities with the attribute 'attributeName' and its value 'attributeValue'.
   * Found Facilities are returned along with attributes listed in 'attrNames'.
   *
   * @param attributeName String
   * @param attributeValue String
   * @param attrNames List<String>
   * @return List<FacilityWithAttribute> facilities with attributes
   * @throw AttributeNotExistsException when the attribute to search by does not exist
   */
  getFacilitiesByAttributeWithAttributes {
    @Override
    public List<FacilityWithAttributes> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager()
          .getFacilitiesByAttributeWithAttributes(ac.getSession(), parms.readString("attributeName"),
              parms.readString("attributeValue"), parms.readList("attrNames", String.class));
    }
  },

  /*#
   * List all facilities.
   *
   * @return List<Facility> All facilities
   */
  getFacilities {
    @Override
    public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getFacilities(ac.getSession());
    }
  },

  /*#
   * Gets count of all facilities.
   * @return int Facilities count
   */
  getFacilitiesCount {
    @Override
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getFacilitiesCount(ac.getSession());
    }
  },

  /*#
   * Gets all enriched facilities user has access rights to.
   * If User is:
   * - PERUNADMIN : all facilities
   * - FACILITYADMIN : only facilities where user is facility admin
   * - FACILITYOBSERVER: only facilities where user is facility observer
   *
   * @return List<EnrichedFacility> All enriched facilities
   */
  getEnrichedFacilities {
    @Override
    public List<EnrichedFacility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getEnrichedFacilities(ac.getSession());
    }
  },

  /*#
   * Returns owners of a facility.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @return List<Owner> Facility owners
   */
  /*#
   * Returns owners of a facility.
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @return List<Owner> Facility owners
   */
  getOwners {
    @Override
    public List<Owner> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getOwners(ac.getSession(), getFacility(ac, parms));
    }
  },

  /*#
   * Add owner of a facility.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @param ownerName String Owner name
   */
  /*#
   * Add owner of a facility.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @param owner int Owner <code>id</code>
   */
  /*#
   * Add owner of a facility.
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @param ownerName String Owner name
   */
  /*#
   * Add owner of a facility.
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @param owner int Owner <code>id</code>
   */
  addOwner {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      Owner owner;
      if (parms.contains("ownerName")) {
        owner = ac.getOwnerByName(parms.readString("ownerName"));
      } else {
        owner = ac.getOwnerById(parms.readInt("owner"));
      }

      ac.getFacilitiesManager().addOwner(ac.getSession(), getFacility(ac, parms), owner);
      return null;
    }
  },

  /*#
   * Add owners of a facility.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @param ownerNames List<String> Owner name
   */
  /*#
   * Add owners of a facility.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @param owners List<int> Owner <code>id</code>
   */
  /*#
   * Add owners of a facility.
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @param ownerNames List<String> Owner name
   */
  /*#
   * Add owners of a facility.
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @param owners List<int> Owner <code>id</code>
   */
  addOwners {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<Owner> owners = new ArrayList<>();
      if (parms.contains("ownerNames")) {
        for (String ownerName : parms.readList("ownerNames", String.class)) {
          owners.add(ac.getOwnerByName(ownerName));
        }
      } else {
        for (Integer ownerId : parms.readList("owners", Integer.class)) {
          owners.add(ac.getOwnerById(ownerId));
        }
      }

      ac.getFacilitiesManager().addOwners(ac.getSession(), getFacility(ac, parms), owners);
      return null;
    }
  },

  /*#
   * Remove owner of a facility.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @param ownerName String Owner name
   */
  /*#
   * Remove owner of a facility.
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @param ownerName String Owner name
   */
  /*#
   * Remove owner of a facility.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @param owner int Owner <code>id</code>
   */
  /*#
   * Remove owner of a facility.
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @param owner int Owner <code>id</code>
   */
  removeOwner {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      Owner owner;
      if (parms.contains("ownerName")) {
        owner = ac.getOwnerByName(parms.readString("ownerName"));
      } else {
        owner = ac.getOwnerById(parms.readInt("owner"));
      }

      ac.getFacilitiesManager().removeOwner(ac.getSession(), getFacility(ac, parms), owner);
      return null;
    }
  },

  /*#
   * Remove owners of a facility.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @param ownerNames List<String> Owner name
   */
  /*#
   * Remove owners of a facility.
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @param ownerNames List<String> Owner name
   */
  /*#
   * Remove owners of a facility.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @param owners List<int> Owner <code>id</code>
   */
  /*#
   * Remove owners of a facility.
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @param owners List<int> Owner <code>id</code>
   */
  removeOwners {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<Owner> owners = new ArrayList<>();
      if (parms.contains("ownerNames")) {
        for (String ownerName : parms.readList("ownerNames", String.class)) {
          owners.add(ac.getOwnerByName(ownerName));
        }
      } else {
        for (Integer ownerId : parms.readList("owners", Integer.class)) {
          owners.add(ac.getOwnerById(ownerId));
        }
      }

      ac.getFacilitiesManager().removeOwners(ac.getSession(), getFacility(ac, parms), owners);
      return null;
    }
  },

  /*#
   * Return all VO which can use a facility. (VO must have the resource which belongs to this facility.)
   *
   * @param facilityName String Facility name
   * @return List<Vo> List of VOs
   */
  /*#
   * Return all VO which can use a facility. (VO must have the resource which belongs to this facility.)
   *
   * @param facility int Facility <code>id</code>
   * @return List<Vo> List of VOs
   */
  getAllowedVos {
    @Override
    public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getAllowedVos(ac.getSession(), getFacility(ac, parms));
    }
  },

  /*#
   * Get all assigned groups on Facility.
   *
   * @param facilityName String Facility name
   * @return List<Group> assigned groups
   */
  /*#
   * Get all assigned groups on Facility filtered by VO.
   *
   * @param facilityName String Facility name
   * @param vo int Vo <code>id</code> to filter groups by
   * @return List<Group> assigned groups
   */
  /*#
   * Get all assigned groups on Facility filtered by Service.
   *
   * @param facilityName String Facility name
   * @param service int Service <code>id</code> to filter groups by
   * @return List<Group> assigned groups
   */
  /*#
   * Get all assigned groups on Facility filtered by VO and Service.
   *
   * @param facilityName String Facility name
   * @param vo int Vo <code>id</code> to filter groups by
   * @param service int Service <code>id</code> to filter groups by
   * @return List<Group> assigned groups
   */
  /*#
   * Get all assigned groups on Facility.
   *
   * @param facility int Facility <code>id</code>
   * @return List<Group> assigned groups
   */
  /*#
   * Get all assigned groups on Facility filtered by VO.
   *
   * @param facility int Facility <code>id</code>
   * @param vo int Vo <code>id</code> to filter groups by
   * @return List<Group> assigned groups
   */
  /*#
   * Get all assigned groups on Facility filtered by Service.
   *
   * @param facility int Facility <code>id</code>
   * @param service int Service <code>id</code> to filter groups by
   * @return List<Group> assigned groups
   */
  /*#
   * Get all assigned groups on Facility filtered by VO and Service.
   *
   * @param facility int Facility <code>id</code>
   * @param vo int Vo <code>id</code> to filter groups by
   * @param service int Service <code>id</code> to filter groups by
   * @return List<Group> assigned groups
   */
  getAllowedGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      Facility facility = getFacility(ac, parms);
      Service service = null;
      Vo vo = null;
      if (parms.contains("vo")) {
        vo = ac.getVoById(parms.readInt("vo"));
      }
      if (parms.contains("service")) {
        service = ac.getServiceById(parms.readInt("service"));
      }
      return ac.getFacilitiesManager().getAllowedGroups(ac.getSession(), facility, vo, service);
    }
  },

  /*#
   * Get all assigned RichGroups on Facility with specified set of attributes.
   *
   * @param facilityName String Facility name
   * @param attrNames List<String> Attribute names
   * @return List<RichGroup> assigned groups
   * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" ,
   * "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
   */
  /*#
   * Get all assigned RichGroups on Facility filtered by VO with specified set of attributes.
   *
   * @param facilityName String Facility name
   * @param vo int Vo <code>id</code> to filter groups by
   * @param attrNames List<String> Attribute names
   * @return List<RichGroup> assigned groups
   * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" ,
   * "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
   */
  /*#
   * Get all assigned RichGroups on Facility filtered by Service with specified set of attributes.
   *
   * @param facilityName String Facility name
   * @param service int Service <code>id</code> to filter groups by
   * @param attrNames List<String> Attribute names
   * @return List<RichGroup> assigned groups
   * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" ,
   * "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
   */
  /*#
   * Get all assigned RichGroups on Facility filtered by VO and Service with specified set of attributes.
   *
   * @param facilityName String Facility name
   * @param vo int Vo <code>id</code> to filter groups by
   * @param service int Service <code>id</code> to filter groups by
   * @param attrNames List<String> Attribute names
   * @return List<RichGroup> assigned groups
   * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" ,
   * "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
   */
  /*#
   * Get all assigned RichGroups on Facility with specified set of attributes.
   *
   * @param facility int Facility <code>id</code>
   * @param attrNames List<String> Attribute names
   * @return List<RichGroup> assigned groups
   * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" ,
   * "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
   */
  /*#
   * Get all assigned RichGroups on Facility filtered by VO with specified set of attributes.
   *
   * @param facility int Facility <code>id</code>
   * @param vo int Vo <code>id</code> to filter groups by
   * @param attrNames List<String> Attribute names
   * @return List<RichGroup> assigned groups
   * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" ,
   * "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
   */
  /*#
   * Get all assigned RichGroups on Facility filtered by Service with specified set of attributes.
   *
   * @param facility int Facility <code>id</code>
   * @param service int Service <code>id</code> to filter groups by
   * @param attrNames List<String> Attribute names
   * @return List<RichGroup> assigned groups
   * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" ,
   * "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
   */
  /*#
   * Get all assigned RichGroups on Facility filtered by VO and Service with specified set of attributes.
   *
   * @param facility int Facility <code>id</code>
   * @param vo int Vo <code>id</code> to filter groups by
   * @param service int Service <code>id</code> to filter groups by
   * @param attrNames List<String> Attribute names
   * @return List<RichGroup> assigned groups
   * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" ,
   * "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
   */
  getAllowedRichGroupsWithAttributes {
    @Override
    public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {
      Facility facility = getFacility(ac, parms);

      Service service = null;
      Vo vo = null;
      if (parms.contains("vo")) {
        vo = ac.getVoById(parms.readInt("vo"));
      }
      if (parms.contains("service")) {
        service = ac.getServiceById(parms.readInt("service"));
      }
      return ac.getFacilitiesManager().getAllowedRichGroupsWithAttributes(ac.getSession(), facility, vo, service,
          parms.readList("attrNames", String.class));
    }
  },

  /*#
   * Returns all resources assigned to a facility.
   *
   * @param facilityName String Facility name
   * @return List<Resource> Resources
   */
  /*#
   * Returns all resources assigned to a facility.
   *
   * @param facility int Facility <code>id</code>
   * @return List<Resource> Resources
   */
  getAssignedResources {
    @Override
    public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getAssignedResources(ac.getSession(), getFacility(ac, parms));
    }
  },

  /*#
   * Returns resources with specific service assigned to the facility.
   *
   * @param facility int Facility <code>id</code>
   * @param service int Service <code>id</code>
   * @return List<Resource> Resources
   */
  getAssignedResourcesByAssignedService {
    @Override
    public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager()
          .getAssignedResourcesByAssignedService(ac.getSession(), ac.getFacilityById(parms.readInt("facility")),
              ac.getServiceById(parms.readInt("service")));
    }
  },

  /*#
   * Returns all rich resources assigned to a facility with VO property filled.
   * @param facilityName String Facility name
   * @return List<RichResource> Resources
   */
  /*#
   * Returns all rich resources assigned to a facility with VO property filled.
   * @param facility int Facility <code>id</code>
   * @return List<RichResource> Resources
   */
  /*#
   * Returns all rich resources assigned to a facility and service with VO property filled.
   * @param facility int Facility <code>id</code>
   * @param service int Service <code>id</code>
   * @return List<RichResource> Resources
   */
  getAssignedRichResources {
    @Override
    public List<RichResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("service")) {
        return ac.getFacilitiesManager().getAssignedRichResources(ac.getSession(), getFacility(ac, parms),
            ac.getServiceById(parms.readInt("service")));
      }
      return ac.getFacilitiesManager().getAssignedRichResources(ac.getSession(), getFacility(ac, parms));
    }
  },

  /*#
   * Creates a facility. Caller is automatically set as facility manager.
   * Facility Object must contain name which can contain only a-Z0-9.-_ and space characters.
   * Parameter description is optional.
   * Other parameters are ignored.
   * @param facility Facility JSON object
   * @throw ConsentHubExistsException if consent hub with facility name exists
   * @throw FacilityExistsException if facility already exists
   * @return Facility Created Facility object
   * @exampleParam facility { "name" : "the best-facility_7" }
   */
  /*#
   * Creates a facility. Caller is automatically set as facility manager.
   * @param name String name of a facility - can contain only a-Z0-9.-_ and space characters.
   * @param description String description of a facility
   * @throw ConsentHubExistsException if consent hub with facility name exists
   * @throw FacilityExistsException if facility already exists
   * @return Facility Created Facility object
   * @exampleParam name "the best-facility_7"
   * @exampleParam description "A description with information."
   */
  createFacility {
    @Override
    public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("facility")) {
        return ac.getFacilitiesManager().createFacility(ac.getSession(), parms.read("facility", Facility.class));
      } else if (parms.contains("name") && parms.contains("description")) {
        String name = parms.readString("name");
        String description = parms.readString("description");
        Facility facility = new Facility(0, name, description);
        return ac.getFacilitiesManager().createFacility(ac.getSession(), facility);
      } else {
        throw new RpcException(RpcException.Type.WRONG_PARAMETER);
      }
    }
  },

  /*#
   * Deletes a facility.
   * @param facilityName String Facility name
   */
  /*#
   * Deletes a facility.
   * @param facilityName String Facility name
   * @param force Boolean if true deletes all constrains of facility before deleting facility
   */
  /*#
   * Deletes a facility.
   * @param facility int Facility <code>id</code>
   */
  /*#
   * Deletes a facility.
   * @param facility int Facility <code>id</code>
   * @param force Boolean if true deletes all constrains of facility before deleting facility
   */
  deleteFacility {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("force")) {
        ac.getFacilitiesManager().deleteFacility(ac.getSession(), getFacility(ac, parms), parms.readBoolean("force"));
        return null;
      } else {
        ac.getFacilitiesManager().deleteFacility(ac.getSession(), getFacility(ac, parms), false);
        return null;
      }
    }
  },

  /*#
   * Update a facility (facility name)
   *
   * @param facility Facility JSON object
   * @return Facility updated Facility object
   */
  updateFacility {
    @Override
    public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().updateFacility(ac.getSession(), parms.read("facility", Facility.class));
    }
  },

  /*#
   * Returns list of all facilities owned by the owner.
   *
   * @deprecated
   * @param owner int Owner <code>id</code>
   * @return List<Facility> Owner's facilities
   */
  getOwnerFacilities {
    @Override
    public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getOwnerFacilities(ac.getSession(), ac.getOwnerById(parms.readInt("owner")));
    }
  },

  /*#
   * Lists hosts of a Facility.
   * @param facilityName String Facility name
   * @return List<Host> Hosts
   */
  /*#
   * Lists hosts of a Facility.
   * @param facility int Facility <code>id</code>
   * @return List<Host> Hosts
   */
  getHosts {
    @Override
    public List<Host> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getHosts(ac.getSession(), getFacility(ac, parms));
    }
  },

  /*#
   * Returns a host by its <code>id</code>.
   * @param id int Host <code>id</code>
   * @return Host Host object
   */
  getHostById {
    @Override
    public Host call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getHostById(ac.getSession(), parms.readInt("id"));
    }
  },

  /*#
   * Returns hosts by hostname. (from all facilities)
   * @param hostname String hostname of hosts
   * @return List<Host> all hosts with this hostname, empty arrayList if none exists
   */
  getHostsByHostname {
    @Override
    public List<Host> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getHostsByHostname(ac.getSession(), parms.readString("hostname"));
    }
  },

  /*#
   * Return all enriched hosts of given facility. That is host with all its attributes.
   *
   * @param facility int Facility <code>id</code>
   * @param attrNames List<String> Attribute names
   * @return List<EnrichedHosts> enrichedHosts
   */
  getEnrichedHosts {
    @Override
    public List<EnrichedHost> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getEnrichedHosts(ac.getSession(), ac.getFacilityById(parms.readInt("facility")),
          parms.readList("attrNames", String.class));
    }
  },

  /*#
   * Return facility which has the host.
   * @param host int Host <code>id</code>
   * @return Facility Facility object
   */
  getFacilityForHost {
    @Override
    public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getFacilityForHost(ac.getSession(), ac.getHostById(parms.readInt("host")));
    }
  },

  /*#
   * Count hosts of Facility.
   * @param facilityName String Facility name
   * @return int Hosts count
   */
  /*#
   * Count hosts of Facility.
   * @param facility int Facility <code>id</code>
   * @return int Hosts count
   */
  getHostsCount {
    @Override
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getHostsCount(ac.getSession(), getFacility(ac, parms));
    }
  },

  /*#
   * Adds hosts to the Facility.
   *
   * @param hostnames List<String> Host names
   * @param facilityName String Facility name
   * @return List<Host> Hosts with <code>id</code>'s set.
   */
  /*#
   * Adds hosts to the Facility.
   *
   * @param hostnames List<String> Host names
   * @param facility int Facility <code>id</code>
   * @return List<Host> Hosts with <code>id</code>'s set.
   */
  addHosts {
    @Override
    public List<Host> call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      Facility facility = getFacility(ac, parms);

      List<String> hostnames = parms.readList("hostnames", String.class);

      return ac.getFacilitiesManager().addHosts(ac.getSession(), facility, hostnames);
    }
  },

  /*#
   * Remove hosts from a Facility.
   * @param hosts List<Integer> List of Host IDs
   * @param facilityName String Facility name
   */
  /*#
   * Remove hosts from a Facility.
   * @param hosts List<Integer> List of Host IDs
   * @param facility int Facility <code>id</code>
   */
  removeHosts {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      Facility facility = getFacility(ac, parms);

      //TODO: optimalizovat?
      int[] ids = parms.readArrayOfInts("hosts");
      List<Host> hosts = new ArrayList<Host>(ids.length);
      for (int i : ids) {
        hosts.add(ac.getHostById(i));
      }

      ac.getFacilitiesManager().removeHosts(ac.getSession(), hosts, facility);
      return null;
    }
  },

  /*#
   * Adds host to a Facility.
   * @param hostname String Hostname
   * @param facilityName String Facility name
   * @return Host Host with <code>id</code> set.
   */
  /*#
   * Adds host to a Facility.
   * @param hostname String Hostname
   * @param facility int Facility <code>id</code>
   * @return Host Host with <code>id</code> set.
   */
  addHost {
    @Override
    public Host call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      Facility facility = getFacility(ac, parms);

      String hostname = parms.readString("hostname");
      Host host = new Host();
      host.setHostname(hostname);

      return ac.getFacilitiesManager().addHost(ac.getSession(), host, facility);
    }
  },

  /*#
   * Removes a host.
   * @param host int Host <code>id</code>
   */
  removeHost {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      int id = parms.readInt("host");

      Host host = ac.getFacilitiesManager().getHostById(ac.getSession(), id);

      ac.getFacilitiesManager().removeHost(ac.getSession(), host);
      return null;
    }
  },

  /*#
   * Remove host from the Facility based on hostname. If there is ambiguity, method throws exception and no host is
   * removed.
   *
   * @param hostname String hostname
   * @throw HostNotExistsException When host doesn't exist or is not unique by name
   */
  removeHostByHostname {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      ac.getFacilitiesManager().removeHostByHostname(ac.getSession(), parms.readString("hostname"));
      return null;
    }
  },

  /*#
   * Get facilities where the service is defined..
   *
   * @param service int Service <code>id</code>
   * @return List<Facility> Assigned facilities
   */
  /*#
   * Get facilities which are assigned to a Group (via resource).
   *
   * @param group int Group <code>id</code>
   * @return List<Facility> Assigned facilities
   */
  /*#
   * Get facilities which have the member access on.
   *
   * @param member int Member <code>id</code>
   * @return List<Facility> Assigned facilities
   */
  /*#
   * Get facilities which have the user access on.
   *
   * @param user int User <code>id</code>
   * @return List<Facility> Assigned facilities
   */
  getAssignedFacilities {
    @Override
    public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("service")) {
        return ac.getFacilitiesManager()
            .getAssignedFacilities(ac.getSession(), ac.getServiceById(parms.readInt("service")));
      } else if (parms.contains("group")) {
        return ac.getFacilitiesManager()
            .getAssignedFacilities(ac.getSession(), ac.getGroupById(parms.readInt("group")));
      } else if (parms.contains("member")) {
        return ac.getFacilitiesManager()
            .getAssignedFacilities(ac.getSession(), ac.getMemberById(parms.readInt("member")));
      } else if (parms.contains("user")) {
        return ac.getFacilitiesManager().getAssignedFacilities(ac.getSession(), ac.getUserById(parms.readInt("user")));
      } else {
        throw new RpcException(RpcException.Type.MISSING_VALUE, "service or group or member of user");
      }
    }
  },

  /*#
   * Adds a Facility admin.
   *
   * @param facilityName String Facility name
   * @param user int User <code>id</code>
   */
  /*#
   *  Adds a group administrator to the Facility.
   *
   *  @param facilityName String Facility name
   *  @param authorizedGroup int Group <code>id</code>
   */
  /*#
   * Adds a Facility admin.
   *
   * @param facility int Facility <code>id</code>
   * @param user int User <code>id</code>
   */
  /*#
   *  Adds a group administrator to the Facility.
   *
   *  @param facility int Facility <code>id</code>
   *  @param authorizedGroup int Group <code>id</code>
   */
  addAdmin {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      if (parms.contains("user")) {
        ac.getFacilitiesManager()
            .addAdmin(ac.getSession(), getFacility(ac, parms), ac.getUserById(parms.readInt("user")));
      } else {
        ac.getFacilitiesManager()
            .addAdmin(ac.getSession(), getFacility(ac, parms), ac.getGroupById(parms.readInt("authorizedGroup")));
      }
      return null;
    }
  },

  /*#
   * Removes a Facility admin.
   *
   * @param facilityName String Facility name
   * @param user int User <code>id</code>
   */
  /*#
   *  Removes a group administrator of the Facility.
   *
   *  @param facilityName String Facility name
   *  @param authorizedGroup int Group <code>id</code>
   */
  /*#
   * Removes a Facility admin.
   *
   * @param facility int Facility <code>id</code>
   * @param user int User <code>id</code>
   */
  /*#
   *  Removes a group administrator of the Facility.
   *
   *  @param facility int Facility <code>id</code>
   *  @param authorizedGroup int Group <code>id</code>
   */
  removeAdmin {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      if (parms.contains("user")) {
        ac.getFacilitiesManager()
            .removeAdmin(ac.getSession(), getFacility(ac, parms), ac.getUserById(parms.readInt("user")));
      } else {
        ac.getFacilitiesManager()
            .removeAdmin(ac.getSession(), getFacility(ac, parms), ac.getGroupById(parms.readInt("authorizedGroup")));
      }
      return null;
    }
  },

  /*#
   * Get list of all facility administrators for supported role and given facility.
   * If some group is administrator of the given group, all VALID members are included in the list.
   * If onlyDirectAdmins is == true, return only direct admins of the group for supported role.
   *
   * Supported roles: FacilityAdmin
   *
   * @param facilityName String Facility name
   * @param onlyDirectAdmins boolean if true, get only direct facility administrators (if false, get both direct and
   * indirect)
   *
   * @return List<User> list of all facility administrators of the given facility for supported role
   */
  /*#
   * Get all Facility admins.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @return List<User> List of Users who are admins in the facility.
   */
  /*#
   * Get list of all facility administrators for supported role and given facility.
   * If some group is administrator of the given group, all VALID members are included in the list.
   * If onlyDirectAdmins is == true, return only direct admins of the group for supported role.
   *
   * Supported roles: FacilityAdmin
   *
   * @param facility int Facility <code>id</code>
   * @param onlyDirectAdmins boolean if true, get only direct facility administrators (if false, get both direct and
   * indirect)
   *
   * @return List<User> list of all facility administrators of the given facility for supported role
   */
  /*#
   * Get all Facility admins.
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @return List<User> List of Users who are admins in the facility.
   */
  getAdmins {
    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        if (parms.contains("onlyDirectAdmins")) {
          return AuthzResolver.getAdmins(ac.getSession(), getFacility(ac, parms), Role.FACILITYADMIN,
              parms.readBoolean("onlyDirectAdmins"));
        } else {
          return AuthzResolver.getAdmins(ac.getSession(), getFacility(ac, parms), Role.FACILITYADMIN, false);
        }
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get all Facility direct admins.
   *
   * @deprecated
   * @param facilityName String Facility name
   * @return List<User> list of admins of the facility
   */
  getDirectAdmins {
    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getAdmins(ac.getSession(), getFacility(ac, parms), Role.FACILITYADMIN, true);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get all Facility group admins.
   *
   * @param facilityName String Facility name
   * @return List<Group> admins
   */
  /*#
   * Get all Facility group admins.
   *
   * @param facility int Facility <code>id</code>
   * @return List<Group> admins
   */
  getAdminGroups {
    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getAdminGroups(ac.getSession(), getFacility(ac, parms), Role.FACILITYADMIN);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get list of all richUser administrators for the facility and supported role with specific attributes.
   * If some group is administrator of the given group, all VALID members are included in the list.
   *
   * Supported roles: FacilityAdmin
   *
   * If "onlyDirectAdmins" is true, return only direct admins of the facility for supported role with specific
   * attributes.
   * If "allUserAttributes" is true, do not specify attributes through list and return them all in objects richUser.
   * Ignoring list of specific attributes.
   *
   * @param facilityName String Facility name
   * @param specificAttributes List<String> list of specified attributes which are needed in object richUser
   * @param allUserAttributes boolean if == true, get all possible user attributes and ignore list of
   * specificAttributes (if false, get only specific attributes)
   * @param onlyDirectAdmins boolean if == true, get only direct facility administrators (if false, get both direct
   * and indirect)
   *
   * @return List<RichUser> list of RichUser administrators for the facility and supported role with attributes
   */
  /*#
   * Get all Facility admins as RichUsers
   *
   * @deprecated
   * @param facilityName String Facility name
   * @return List<RichUser> admins
   */
  /*#
   * Get list of all richUser administrators for the facility and supported role with specific attributes.
   * If some group is administrator of the given group, all VALID members are included in the list.
   *
   * Supported roles: FacilityAdmin
   *
   * If "onlyDirectAdmins" is true, return only direct admins of the facility for supported role with specific
   * attributes.
   * If "allUserAttributes" is true, do not specify attributes through list and return them all in objects richUser.
   * Ignoring list of specific attributes.
   *
   * @param facility int Facility <code>id</code>
   * @param specificAttributes List<String> list of specified attributes which are needed in object richUser
   * @param allUserAttributes boolean if == true, get all possible user attributes and ignore list of
   * specificAttributes (if false, get only specific attributes)
   * @param onlyDirectAdmins boolean if == true, get only direct facility administrators (if false, get both direct
   * and indirect)
   *
   * @return List<RichUser> list of RichUser administrators for the facility and supported role with attributes
   */
  /*#
   * Get all Facility admins as RichUsers
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @return List<RichUser> admins
   */
  getRichAdmins {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        if (parms.contains("onlyDirectAdmins")) {
          return AuthzResolver.getRichAdmins(ac.getSession(), getFacility(ac, parms),
              parms.readList("specificAttributes", String.class), Role.FACILITYADMIN,
              parms.readBoolean("onlyDirectAdmins"), parms.readBoolean("allUserAttributes"));
        } else {
          return AuthzResolver.getRichAdmins(ac.getSession(), getFacility(ac, parms), new ArrayList<>(),
              Role.FACILITYADMIN, false, false);
        }
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get all Facility admins as RichUsers with all their non-null user attributes
   *
   * @deprecated
   * @param facilityName String Facility name
   * @return List<RichUser> admins with attributes
   */
  /*#
   * Get all Facility admins as RichUsers with all their non-null user attributes
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @return List<RichUser> admins with attributes
   */
  getRichAdminsWithAttributes {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getRichAdmins(ac.getSession(), getFacility(ac, parms), new ArrayList<>(),
            Role.FACILITYADMIN, false, true);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get all Facility admins as RichUsers with specific attributes (from user namespace)
   *
   * @deprecated
   * @param facilityName String Facility name
   * @param specificAttributes List<String> list of attributes URNs
   * @return List<RichUser> admins with attributes
   */
  /*#
   * Get all Facility admins as RichUsers with specific attributes (from user namespace)
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @param specificAttributes List<String> list of attributes URNs
   * @return List<RichUser> admins with attributes
   */
  getRichAdminsWithSpecificAttributes {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getRichAdmins(ac.getSession(), getFacility(ac, parms),
            parms.readList("specificAttributes", String.class), Role.FACILITYADMIN, false, false);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Get all Facility admins, which are assigned directly,
   * as RichUsers with specific attributes (from user namespace)
   *
   * @deprecated
   * @param facilityName String Facility name
   * @param specificAttributes List<String> list of attributes URNs
   * @return List<RichUser> direct admins with attributes
   */
  /*#
   * Get all Facility admins, which are assigned directly,
   * as RichUsers with specific attributes (from user namespace)
   *
   * @deprecated
   * @param facility int Facility <code>id</code>
   * @param specificAttributes List<String> list of attributes URNs
   * @return List<RichUser> direct admins with attributes
   */
  getDirectRichAdminsWithSpecificAttributes {
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
      try {
        return AuthzResolver.getRichAdmins(ac.getSession(), getFacility(ac, parms),
            parms.readList("specificAttributes", String.class), Role.FACILITYADMIN, true, false);
      } catch (RoleCannotBeManagedException ex) {
        throw new InternalErrorException(ex);
      }
    }
  },

  /*#
   * Returns list of Facilities, where the user is an Administrator.
   * Including facilities, where the user is a VALID member of authorized group.
   *
   * @param user int User <code>id</code>
   * @return List<Facility> Found Facilities
   */
  getFacilitiesWhereUserIsAdmin {
    @Override
    public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager()
          .getFacilitiesWhereUserIsAdmin(ac.getSession(), ac.getUserById(parms.readInt("user")));
    }
  },

  /*#
   * Return all facilities where exists host with the specific hostname
   *
   * @param hostname String specific hostname
   * @return List<Facility> Found Facilities
   */
  getFacilitiesByHostName {
    @Override
    public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getFacilitiesManager().getFacilitiesByHostName(ac.getSession(), parms.readString("hostname"));
    }
  },

  /*#
   * Return all users which can use this facility
   *
   * @param facilityName String Facility name
   * @param vo int VO <code>id</code>, if provided, filter out users who aren't in specific VO
   * @param service int Service <code>id</code>, if provided, filter out users who aren't allowed to use the service
   * on the facility
   * @return List<User> list of allowed users
   */
  /*#
   * Return all users which can use this facility
   *
   * @param facility int Facility <code>id</code>
   * @param vo int VO <code>id</code>, if provided, filter out users who aren't in specific VO
   * @param service int Service <code>id</code>, if provided, filter out users who aren't allowed to use the service
   * on the facility
   * @return List<User> list of allowed users
   */
  getAllowedUsers {
    @Override
    public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("vo")) {
        if (parms.contains("service")) {
          return ac.getFacilitiesManager()
              .getAllowedUsers(ac.getSession(), getFacility(ac, parms), ac.getVoById(parms.readInt("vo")),
                  ac.getServiceById(parms.readInt("service")));
        } else {
          return ac.getFacilitiesManager()
              .getAllowedUsers(ac.getSession(), getFacility(ac, parms), ac.getVoById(parms.readInt("vo")), null);
        }
      } else if (parms.contains("service")) {
        return ac.getFacilitiesManager().getAllowedUsers(ac.getSession(), getFacility(ac, parms), null,
            ac.getServiceById(parms.readInt("service")));
      } else {
        return ac.getFacilitiesManager().getAllowedUsers(ac.getSession(), getFacility(ac, parms));
      }
    }
  },

  /*#
   * Copy owners from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @deprecated
   * @param srcFacilityName String facility name
   * @param destFacilityName String facility name
   */
  /*#
   * Copy owners from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @deprecated
   * @param srcFacility int facility <code>id</code>
   * @param destFacilityName String facility name
   */
  /*#
   * Copy owners from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @deprecated
   * @param srcFacilityName String facility name
   * @param destFacility int facility <code>id</code>
   */
  /*#
   * Copy owners from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @deprecated
   * @param srcFacility int facility <code>id</code>
   * @param destFacility int facility <code>id</code>
   */
  copyOwners {
    @Override
    public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      Facility srcFacility;
      if (parms.contains("srcFacilityName")) {
        srcFacility = ac.getFacilityByName(parms.readString("srcFacilityName"));
      } else {
        srcFacility = ac.getFacilityById(parms.readInt("srcFacility"));
      }

      Facility destFacility;
      if (parms.contains("destFacilityName")) {
        destFacility = ac.getFacilityByName(parms.readString("destFacilityName"));
      } else {
        destFacility = ac.getFacilityById(parms.readInt("destFacility"));
      }

      ac.getFacilitiesManager().copyOwners(ac.getSession(), srcFacility, destFacility);

      return null;

    }
  },

  /*#
   * Copy managers from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @param srcFacilityName String facility name
   * @param destFacilityName String facility name
   */
  /*#
   * Copy managers from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @param srcFacility int facility <code>id</code>
   * @param destFacilityName String facility name
   */
  /*#
   * Copy managers from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @param srcFacilityName String facility name
   * @param destFacility int facility <code>id</code>
   */
  /*#
   * Copy managers from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @param srcFacility int facility <code>id</code>
   * @param destFacility int facility <code>id</code>
   */
  copyManagers {
    @Override
    public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      Facility srcFacility;
      if (parms.contains("srcFacilityName")) {
        srcFacility = ac.getFacilityByName(parms.readString("srcFacilityName"));
      } else {
        srcFacility = ac.getFacilityById(parms.readInt("srcFacility"));
      }

      Facility destFacility;
      if (parms.contains("destFacilityName")) {
        destFacility = ac.getFacilityByName(parms.readString("destFacilityName"));
      } else {
        destFacility = ac.getFacilityById(parms.readInt("destFacility"));
      }

      ac.getFacilitiesManager().copyManagers(ac.getSession(), srcFacility, destFacility);

      return null;

    }
  },

  /*#
   * Copy attributes (settings) from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @param srcFacilityName String facility name
   * @param destFacilityName String facility name
   */
  /*#
   * Copy attributes (settings) from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @param srcFacility int facility <code>id</code>
   * @param destFacilityName String facility name
   */
  /*#
   * Copy attributes (settings) from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @param srcFacilityName String facility name
   * @param destFacility int facility <code>id</code>
   */
  /*#
   * Copy attributes (settings) from source facility to destination facility.
   * You must be facility manager of both.
   *
   * @param srcFacility int facility <code>id</code>
   * @param destFacility int facility <code>id</code>
   */
  copyAttributes {
    @Override
    public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      Facility srcFacility;
      if (parms.contains("srcFacilityName")) {
        srcFacility = ac.getFacilityByName(parms.readString("srcFacilityName"));
      } else {
        srcFacility = ac.getFacilityById(parms.readInt("srcFacility"));
      }

      Facility destFacility;
      if (parms.contains("destFacilityName")) {
        destFacility = ac.getFacilityByName(parms.readString("destFacilityName"));
      } else {
        destFacility = ac.getFacilityById(parms.readInt("destFacility"));
      }

      ac.getFacilitiesManager().copyAttributes(ac.getSession(), srcFacility, destFacility);

      return null;

    }
  },

  /*#
   *  Set ban for user on facility.
   *
   * @param banOnFacility BanOnFacility JSON object
   * @return BanOnFacility Created banOnFacility
   */
  setBan {
    @Override
    public BanOnFacility call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getFacilitiesManager().setBan(ac.getSession(), parms.read("banOnFacility", BanOnFacility.class));

    }
  },

  /*#
   *  Get Ban for user on facility by it's id.
   *
   * @param banId int BanOnFacility <code>id</code>
   * @return BanOnFacility banOnFacility
   */
  getBanById {
    @Override
    public BanOnFacility call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getFacilitiesManager().getBanById(ac.getSession(), parms.readInt("banId"));

    }
  },

  /*#
   *  Get ban by userId and facilityId.
   *
   * @param userId int User <code>id</code>
   * @param facilityId int Facility <code>id</code>
   * @return BanOnFacility banOnFacility
   */
  getBan {
    @Override
    public BanOnFacility call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getFacilitiesManager().getBan(ac.getSession(), parms.readInt("userId"), parms.readInt("facilityId"));

    }
  },

  /*#
   * Get all bans for user on any facility.
   *
   * @param userId int User <code>id</code>
   * @return List<BanOnFacility> userBansOnFacilities
   */
  getBansForUser {
    @Override
    public List<BanOnFacility> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getFacilitiesManager().getBansForUser(ac.getSession(), parms.readInt("userId"));

    }
  },

  /*#
   * Get all bans for user on the facility.
   *
   * @param facilityId int Facility <code>id</code>
   * @return List<BanOnFacility> usersBansOnFacility
   */
  getBansForFacility {
    @Override
    public List<BanOnFacility> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getFacilitiesManager().getBansForFacility(ac.getSession(), parms.readInt("facilityId"));

    }
  },

  /*#
   *  Get all enriched bans for users on the facility.
   *
   * @param facility int Facility <code>id</code>
   * @param attrNames List<String> list of attribute names, if empty or null returns all user and member attributes
   * @return List<EnrichedBanOnFacility> enriched bans on facility
   * @throw FacilityNotExistsException
   */
  getEnrichedBansForFacility {
    @Override
    public List<EnrichedBanOnFacility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<String> attrNames = null;
      if (parms.contains("attrNames")) {
        attrNames = parms.readList("attrNames", String.class);
      }
      return ac.getFacilitiesManager()
          .getEnrichedBansForFacility(ac.getSession(), parms.readInt("facility"), attrNames);
    }
  },

  /*#
   *  Get all user's enriched bans on assigned facilities.
   *
   * @param user int user <code>id</code>
   * @param attrNames List<String> list of attribute names, if empty or null returns all user and member attributes
   * @return List<EnrichedBanOnFacility> enriched bans for user
   * @throw UserNotExistsException
   */
  getEnrichedBansForUser {
    @Override
    public List<EnrichedBanOnFacility> call(ApiCaller ac, Deserializer parms) throws PerunException {
      List<String> attrNames = null;
      if (parms.contains("attrNames")) {
        attrNames = parms.readList("attrNames", String.class);
      }
      return ac.getFacilitiesManager().getEnrichedBansForUser(ac.getSession(), parms.readInt("user"), attrNames);
    }
  },

  /*#
   * Update existing ban (description, validation timestamp)
   *
   * @param banOnFacility BanOnFacility JSON object
   * @return BanOnFacility updated banOnFacility
   */
  updateBan {
    @Override
    public BanOnFacility call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      return ac.getFacilitiesManager().updateBan(ac.getSession(), parms.read("banOnFacility", BanOnFacility.class));

    }
  },

  /*#
   * Remove specific ban by it's id.
   *
   * @param banId int BanOnFacility <code>id</code>
   */
  /*#
   * Remove specific ban by userId and facilityId.
   *
   * @param userId int User <code>id</code>
   * @param facilityId int Facility <code>id</code>
   */
  removeBan {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      if (parms.contains("banId")) {
        ac.getFacilitiesManager().removeBan(ac.getSession(), parms.readInt("banId"));
      } else {
        ac.getFacilitiesManager().removeBan(ac.getSession(), parms.readInt("userId"), parms.readInt("facilityId"));
      }
      return null;
    }
  };

  private static Facility getFacility(ApiCaller ac, Deserializer parms) throws PerunException {
    if (parms.contains("facilityName")) {
      return ac.getFacilityByName(parms.readString("facilityName"));
    } else {
      return ac.getFacilityById(parms.readInt("facility"));
    }
  }
}
