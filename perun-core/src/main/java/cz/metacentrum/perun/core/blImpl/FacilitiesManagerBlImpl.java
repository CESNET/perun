package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanSetForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanUpdatedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityCreated;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityDeleted;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityUpdated;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.HostAddedToFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.HostRemovedFromFacility;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.EnrichedBanOnFacility;
import cz.metacentrum.perun.core.api.EnrichedFacility;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.FacilityWithAttributes;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichFacility;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeSetException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.FacilitiesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.FacilitiesManagerImplApi;
import cz.metacentrum.perun.taskslib.model.Task;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class FacilitiesManagerBlImpl implements FacilitiesManagerBl {

  static final Logger LOG = LoggerFactory.getLogger(FacilitiesManagerBlImpl.class);
  private static final List<String> MANDATORY_ATTRIBUTES_FOR_USER_IN_CONTACT = new ArrayList<>(
      Arrays.asList(AttributesManager.NS_USER_ATTR_DEF + ":organization",
          AttributesManager.NS_USER_ATTR_DEF + ":preferredMail"));
  private final FacilitiesManagerImplApi facilitiesManagerImpl;
  private PerunBl perunBl;
  private AtomicBoolean initialized = new AtomicBoolean(false);

  public FacilitiesManagerBlImpl(FacilitiesManagerImplApi facilitiesManagerImpl) {
    this.facilitiesManagerImpl = facilitiesManagerImpl;
  }

  @Override
  public Host addHost(PerunSession sess, Host host, Facility facility) {
    getPerunBl().getAuditer().log(sess, new HostAddedToFacility(host, facility));
    return facilitiesManagerImpl.addHost(sess, host, facility);
  }

  @Override
  public List<Host> addHosts(PerunSession sess, List<Host> hosts, Facility facility) throws HostExistsException {
    //check if hosts not exist in cluster
    List<Host> alreadyAssignedHosts = getHosts(sess, facility);

    Set<String> alreadyAssignedHostnames = new HashSet<>();
    Set<String> newHostnames = new HashSet<>();
    for (Host h : alreadyAssignedHosts) {
      alreadyAssignedHostnames.add(h.getHostname());
    }
    for (Host h : hosts) {
      newHostnames.add(h.getHostname());
    }
    newHostnames.retainAll(alreadyAssignedHostnames);
    if (!newHostnames.isEmpty()) {
      throw new HostExistsException(newHostnames.toString());
    }

    for (Host host : hosts) {
      getFacilitiesManagerImpl().addHost(sess, host, facility);
      getPerunBl().getAuditer().log(sess, new HostAddedToFacility(host, facility));
    }

    return hosts;
  }

  @Override
  public List<Host> addHosts(PerunSession sess, Facility facility, List<String> hosts)
      throws HostExistsException, WrongPatternException {
    // generate hosts by pattern
    List<Host> generatedHosts = new ArrayList<>();
    for (String host : hosts) {
      List<String> listOfStrings = Utils.generateStringsByPattern(host);
      List<Host> listOfHosts = new ArrayList<>();
      for (String hostName : listOfStrings) {
        Host newHost = new Host();
        newHost.setHostname(hostName);
        listOfHosts.add(newHost);
      }
      generatedHosts.addAll(listOfHosts);
    }
    // add generated hosts
    return addHosts(sess, generatedHosts, facility);
  }

  @Override
  @Deprecated
  public void addOwner(PerunSession sess, Facility facility, Owner owner) throws OwnerAlreadyAssignedException {
    getFacilitiesManagerImpl().addOwner(sess, facility, owner);
  }

  @Override
  public boolean banExists(PerunSession sess, int userId, int facilityId) {
    return getFacilitiesManagerImpl().banExists(sess, userId, facilityId);
  }

  @Override
  public boolean banExists(PerunSession sess, int banId) {
    return getFacilitiesManagerImpl().banExists(sess, banId);
  }

  @Override
  public void checkBanExists(PerunSession sess, int userId, int facilityId) throws BanNotExistsException {
    if (!banExists(sess, userId, facilityId)) {
      throw new BanNotExistsException("Ban for user " + userId + " and facility " + facilityId + " not exists!");
    }
  }

  @Override
  public void checkBanExists(PerunSession sess, int banId) throws BanNotExistsException {
    if (!banExists(sess, banId)) {
      throw new BanNotExistsException("Ban with id " + banId + " not exists!");
    }
  }

  @Override
  public void checkFacilityExists(PerunSession sess, Facility facility) throws FacilityNotExistsException {
    getFacilitiesManagerImpl().checkFacilityExists(sess, facility);
  }

  @Override
  public void checkHostExists(PerunSession sess, Host host) throws HostNotExistsException {
    getFacilitiesManagerImpl().checkHostExists(sess, host);
  }

  /**
   * Converts given facility into enriched facility.
   *
   * @param sess
   * @param facility facility to be converted
   * @return converted EnrichedFacility
   */
  private EnrichedFacility convertToEnrichedFacility(PerunSession sess, Facility facility) {
    List<Owner> owners = this.getOwners(sess, facility);
    List<Destination> destinations = getPerunBl().getServicesManagerBl().getDestinations(sess, facility);
    List<Host> hosts = this.getHosts(sess, facility);
    return new EnrichedFacility(facility, owners, destinations, hosts);
  }

  @Override
  public void copyAttributes(PerunSession sess, Facility sourceFacility, Facility destinationFacility)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
    List<Attribute> sourceAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, sourceFacility);
    List<Attribute> destinationAttributes =
        getPerunBl().getAttributesManagerBl().getAttributes(sess, destinationFacility);

    // do not get virtual attributes from source facility, they can't be set to destination
    sourceAttributes.removeIf(
        attribute -> attribute.getNamespace().startsWith(AttributesManager.NS_FACILITY_ATTR_VIRT));

    // create intersection of destination and source attributes
    List<Attribute> intersection = new ArrayList<>(destinationAttributes);
    intersection.retainAll(sourceAttributes);

    // delete all common attributes from destination facility
    getPerunBl().getAttributesManagerBl().removeAttributes(sess, destinationFacility, intersection);
    // add all attributes from source facility to destination facility
    getPerunBl().getAttributesManagerBl().setAttributes(sess, destinationFacility, sourceAttributes);
  }

  @Override
  public void copyManagers(PerunSession sess, Facility sourceFacility, Facility destinationFacility) {
    for (User admin : getDirectAdmins(sess, sourceFacility)) {
      try {
        AuthzResolverBlImpl.setRole(sess, admin, destinationFacility, Role.FACILITYADMIN);
      } catch (AlreadyAdminException ex) {
        // we can ignore the exception in this particular case, user can be admin in both of the facilities
      } catch (RoleCannotBeManagedException | RoleCannotBeSetException e) {
        throw new InternalErrorException(e);
      }
    }

    for (Group adminGroup : getAdminGroups(sess, sourceFacility)) {
      try {
        AuthzResolverBlImpl.setRole(sess, adminGroup, destinationFacility, Role.FACILITYADMIN);
      } catch (AlreadyAdminException ex) {
        // we can ignore the exception in this particular case, group can be admin in both of the facilities
      } catch (RoleCannotBeManagedException | RoleCannotBeSetException e) {
        throw new InternalErrorException(e);
      }
    }
  }

  @Override
  @Deprecated
  public void copyOwners(PerunSession sess, Facility sourceFacility, Facility destinationFacility) {
    for (Owner owner : getOwners(sess, sourceFacility)) {
      try {
        addOwner(sess, destinationFacility, owner);
      } catch (OwnerAlreadyAssignedException ex) {
        // we can ignore the exception in this particular case, owner can exists in both of the facilities
      }
    }
  }

  @Override
  public Facility createFacility(PerunSession sess, Facility facility)
      throws FacilityExistsException, ConsentHubExistsException {

    //check facility name, it can contain only a-zA-Z.0-9_-
    if (!facility.getName().matches("^[ a-zA-Z.0-9_-]+$")) {
      throw new IllegalArgumentException(
          "Wrong facility name, facility name can contain only a-Z0-9.-_ and space characters");
    }

    if (!facility.getName().equals(facility.getName().trim())) {
      throw new IllegalArgumentException(
          "Wrong facility name, facility name cannot contain leading and trailing spaces");
    }

    //check if facility have uniq name
    try {
      this.getFacilityByName(sess, facility.getName());
      throw new FacilityExistsException(facility);
    } catch (FacilityNotExistsException ex) { /* OK */ }

    // create facility
    facility = getFacilitiesManagerImpl().createFacility(sess, facility);
    getPerunBl().getAuditer().log(sess, new FacilityCreated(facility));
    //set creator as Facility manager
    if (sess.getPerunPrincipal().getUser() != null) {
      try {
        AuthzResolverBlImpl.setRole(sess, sess.getPerunPrincipal().getUser(), facility, Role.FACILITYADMIN);
      } catch (AlreadyAdminException ex) {
        throw new ConsistencyErrorException(
            "Add manager to newly created Facility failed because there is particular manager already assigned", ex);
      } catch (RoleCannotBeManagedException | RoleCannotBeSetException e) {
        throw new InternalErrorException(e);
      }
    } else {
      LOG.warn("Can't set Facility manager during creating of the Facility. User from perunSession is null. {} {}",
          facility, sess);
    }

    perunBl.getConsentsManagerBl()
        .createConsentHub(sess, new ConsentHub(0, facility.getName(), true, List.of(facility)));

    return facility;
  }

  @Override
  public void deleteFacility(PerunSession sess, Facility facility, Boolean force)
      throws RelationExistsException, FacilityAlreadyRemovedException, HostAlreadyRemovedException,
      ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {

    if (force) {
      List<Resource> resources = this.getAssignedResources(sess, facility);
      for (Resource resource : resources) {
        getPerunBl().getResourcesManagerBl().deleteResource(sess, resource);
      }
      List<Task> tasks = perunBl.getTasksManagerBl().listAllTasksForFacility(sess, facility.getId());
      for (Task task : tasks) {
        perunBl.getTasksManagerBl().deleteTaskResults(sess, task.getId());
        perunBl.getTasksManagerBl().removeTask(sess, task.getId());
      }
    } else {
      if (getFacilitiesManagerImpl().getAssignedResources(sess, facility).size() > 0) {
        throw new RelationExistsException("Facility is still used as a resource");
      }
    }

    //remove admins of this facility
    List<Group> adminGroups = getFacilitiesManagerImpl().getAdminGroups(sess, facility);
    for (Group adminGroup : adminGroups) {
      try {
        AuthzResolverBlImpl.unsetRole(sess, adminGroup, facility, Role.FACILITYADMIN);
      } catch (GroupNotAdminException e) {
        LOG.warn("When trying to unsetRole FacilityAdmin for group {} in the facility {} the exception was thrown {}",
            adminGroup, facility, e);
        //skip and log as warning
      } catch (RoleCannotBeManagedException e) {
        throw new InternalErrorException(e);
      }
    }

    List<User> adminUsers = getFacilitiesManagerImpl().getAdmins(sess, facility);

    for (User adminUser : adminUsers) {
      try {
        AuthzResolverBlImpl.unsetRole(sess, adminUser, facility, Role.FACILITYADMIN);
      } catch (UserNotAdminException e) {
        LOG.warn("When trying to unsetRole FacilityAdmin for user {} in the facility {} the exception was thrown {}",
            adminUser, facility, e);
        //skip and log as warning
      } catch (RoleCannotBeManagedException e) {
        throw new InternalErrorException(e);
      }
    }

    //remove consent hub with consents
    try {
      ConsentHub hub = getPerunBl().getConsentsManagerBl().getConsentHubByFacility(sess, facility.getId());
      getPerunBl().getConsentsManagerBl().removeFacility(sess, hub, facility);
    } catch (ConsentHubNotExistsException e) {
      LOG.warn("When removing facility {} no related consent hub was found", facility);
    } catch (ConsentHubAlreadyRemovedException e) {
      LOG.warn("When removing facility {} consent hub could not be removed", facility);
    } catch (RelationNotExistsException e) {
      LOG.warn("Facility {} is not assigned to the consent hub.", facility);
    }

    //remove hosts
    List<Host> hosts = this.getHosts(sess, facility);
    for (Host host : hosts) {
      this.removeHost(sess, host, facility);
    }

    //remove destinations
    getPerunBl().getServicesManagerBl().removeAllDestinations(sess, facility);

    // remove associated attributes
    try {
      getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, facility);
    } catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
      throw new InternalErrorException(e);
    }

    //Remove all facility bans
    List<BanOnFacility> bansOnFacility = this.getBansForFacility(sess, facility.getId());
    for (BanOnFacility banOnFacility : bansOnFacility) {
      try {
        this.removeBan(sess, banOnFacility.getId());
      } catch (BanNotExistsException ex) {
        //it is ok, we just want to remove it anyway
      }
    }

    //Remove all service denials
    getFacilitiesManagerImpl().removeAllServiceDenials(facility.getId());

    // delete facility
    getFacilitiesManagerImpl().deleteFacilityOwners(sess, facility);
    getFacilitiesManagerImpl().deleteFacility(sess, facility);
    getPerunBl().getAuditer().log(sess, new FacilityDeleted(facility));
  }

  @Override
  public List<Group> getAdminGroups(PerunSession sess, Facility facility) {
    return facilitiesManagerImpl.getAdminGroups(sess, facility);
  }

  @Override
  public List<User> getAdmins(PerunSession perunSession, Facility facility, boolean onlyDirectAdmins) {
    if (onlyDirectAdmins) {
      return getFacilitiesManagerImpl().getDirectAdmins(perunSession, facility);
    } else {
      return getFacilitiesManagerImpl().getAdmins(perunSession, facility);
    }
  }

  @Override
  @Deprecated
  public List<User> getAdmins(PerunSession sess, Facility facility) {
    return facilitiesManagerImpl.getAdmins(sess, facility);
  }

  @Override
  public List<BanOnFacility> getAllExpiredBansOnFacilities(PerunSession sess) {
    return getFacilitiesManagerImpl().getAllExpiredBansOnFacilities(sess);
  }

  @Override
  public List<Facility> getAllowedFacilities(PerunSession sess, User user) {
    return getFacilitiesManagerImpl().getAllowedFacilities(sess, user);
  }

  @Override
  public List<Facility> getAllowedFacilities(PerunSession sess, Member member) {
    return getFacilitiesManagerImpl().getAllowedFacilities(sess, member);
  }

  @Override
  public List<Group> getAllowedGroups(PerunSession perunSession, Facility facility, Vo specificVo,
                                      Service specificService) {
    //Get all facilities resources
    List<Resource> facilityResources =
        getPerunBl().getFacilitiesManagerBl().getAssignedResources(perunSession, facility, specificVo, specificService);

    //GetAll Groups for resulted Resources
    Set<Group> allowedGroups = new HashSet<>();
    for (Resource r : facilityResources) {
      allowedGroups.addAll(getPerunBl().getResourcesManagerBl().getAssignedGroups(perunSession, r));
    }
    return new ArrayList<>(allowedGroups);
  }

  @Override
  public List<Member> getAllowedMembers(PerunSession sess, Facility facility) {
    return getFacilitiesManagerImpl().getAllowedMembers(sess, facility);
  }

  @Override
  public List<Member> getAllowedMembers(PerunSession sess, Facility facility, Service service) {
    return getFacilitiesManagerImpl().getAllowedMembers(sess, facility, service);
  }

  @Override
  public List<RichGroup> getAllowedRichGroupsWithAttributes(PerunSession perunSession, Facility facility, Vo specificVo,
                                                            Service specificService, List<String> attrNames) {

    List<Group> allowedGroups = getAllowedGroups(perunSession, facility, specificVo, specificService);
    return perunBl.getGroupsManagerBl().convertGroupsToRichGroupsWithAttributes(perunSession, allowedGroups, attrNames);

  }

  @Override
  public List<User> getAllowedUsers(PerunSession sess, Facility facility) {
    return getFacilitiesManagerImpl().getAllowedUsers(sess, facility);
  }

  @Override
  public List<User> getAllowedUsers(PerunSession sess, Facility facility, Vo specificVo, Service specificService) {

    //Get all facilities resources
    List<Resource> resources = getAssignedResources(sess, facility, specificVo, specificService);

    Set<User> users = new TreeSet<>();
    for (Resource resource : resources) {
      users.addAll(getPerunBl().getResourcesManagerBl().getAllowedUsers(sess, resource));
    }

    return new ArrayList<>(users);
  }

  @Override
  public List<User> getAllowedUsersNotExpiredInGroups(PerunSession sess, Facility facility, Vo specificVo,
                                                      Service specificService) {

    //Get all facilities resources
    List<Resource> resources = getAssignedResources(sess, facility, specificVo, specificService);

    Set<User> users = new TreeSet<>();
    for (Resource resource : resources) {
      users.addAll(getPerunBl().getResourcesManagerBl().getAllowedUsersNotExpiredInGroups(sess, resource));
    }

    return new ArrayList<>(users);
  }

  @Override
  public List<Vo> getAllowedVos(PerunSession sess, Facility facility) {
    return getFacilitiesManagerImpl().getAllowedVos(sess, facility);
  }

  @Override
  public List<Facility> getAssignedFacilities(PerunSession sess, Group group) {
    // Get all assigned resources for the group and the derive the facilities
    List<Resource> assignedResources = perunBl.getResourcesManagerBl().getAssignedResources(sess, group);

    List<Facility> assignedFacilities = new ArrayList<>();
    for (Resource resource : assignedResources) {
      assignedFacilities.add(perunBl.getResourcesManagerBl().getFacility(sess, resource));
    }

    return assignedFacilities;
  }

  @Override
  public List<Facility> getAssignedFacilities(PerunSession sess, Member member) {
    Set<Facility> assignedFacilities = new HashSet<>();

    for (Resource resource : getPerunBl().getResourcesManagerBl().getAssignedResources(sess, member)) {
      assignedFacilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resource));
    }

    return new ArrayList<>(assignedFacilities);
  }

  @Override
  public List<Facility> getAssignedFacilities(PerunSession sess, User user) {
    List<Member> members = perunBl.getMembersManagerBl().getMembersByUser(sess, user);

    Set<Facility> assignedFacilities = new HashSet<>();
    for (Member member : members) {
      assignedFacilities.addAll(this.getAssignedFacilities(sess, member));
    }

    return new ArrayList<>(assignedFacilities);
  }

  @Override
  public List<Facility> getAssignedFacilities(PerunSession sess, Service service) {
    List<Resource> resources = perunBl.getServicesManagerBl().getAssignedResources(sess, service);

    Set<Facility> assignedFacilities = new HashSet<>();
    for (Resource resource : resources) {
      assignedFacilities.add(perunBl.getResourcesManagerBl().getFacility(sess, resource));
    }

    return new ArrayList<>(assignedFacilities);
  }

  @Override
  public List<Resource> getAssignedResources(PerunSession sess, Facility facility) {
    return getFacilitiesManagerImpl().getAssignedResources(sess, facility);
  }

  @Override
  public List<Resource> getAssignedResources(PerunSession sess, Facility facility, Vo specificVo,
                                             Service specificService) {
    if (specificVo == null && specificService == null) {
      return getAssignedResources(sess, facility);
    }
    return getFacilitiesManagerImpl().getAssignedResources(sess, facility, specificVo, specificService);
  }

  @Override
  public List<RichResource> getAssignedRichResources(PerunSession sess, Facility facility) {
    return getFacilitiesManagerImpl().getAssignedRichResources(sess, facility);
  }

  @Override
  public List<RichResource> getAssignedRichResources(PerunSession sess, Facility facility, Service service) {
    return getFacilitiesManagerImpl().getAssignedRichResources(sess, facility, service);
  }

  @Override
  public List<User> getAssignedUsers(PerunSession sess, Facility facility) {
    return this.getFacilitiesManagerImpl().getAssignedUsers(sess, facility);
  }

  @Override
  public List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service) {
    return this.getFacilitiesManagerImpl().getAssignedUsers(sess, facility, service);
  }

  @Override
  public List<Member> getAssociatedMembers(PerunSession sess, Facility facility, User user) {
    return getFacilitiesManagerImpl().getAssociatedMembers(sess, facility, user);
  }

  @Override
  public List<User> getAssociatedUsers(PerunSession sess, Facility facility) {
    return getFacilitiesManagerImpl().getAssociatedUsers(sess, facility);
  }

  @Override
  public BanOnFacility getBan(PerunSession sess, int userId, int faclityId) throws BanNotExistsException {
    return getFacilitiesManagerImpl().getBan(sess, userId, faclityId);
  }

  @Override
  public BanOnFacility getBanById(PerunSession sess, int banId) throws BanNotExistsException {
    return getFacilitiesManagerImpl().getBanById(sess, banId);
  }

  @Override
  public List<BanOnFacility> getBansForFacility(PerunSession sess, int facilityId) {
    return getFacilitiesManagerImpl().getBansForFacility(sess, facilityId);
  }

  @Override
  public List<BanOnFacility> getBansForUser(PerunSession sess, int userId) {
    return getFacilitiesManagerImpl().getBansForUser(sess, userId);
  }

  @Deprecated
  @Override
  public List<User> getDirectAdmins(PerunSession sess, Facility facility) {
    return facilitiesManagerImpl.getDirectAdmins(sess, facility);
  }

  @Override
  @Deprecated
  public List<RichUser> getDirectRichAdmins(PerunSession sess, Facility facility) {
    return getPerunBl().getUsersManagerBl().convertUsersToRichUsers(sess, this.getDirectAdmins(sess, facility));
  }

  @Override
  @Deprecated
  public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility,
                                                                  List<String> specificAttributes) {
    try {
      return getPerunBl().getUsersManagerBl()
          .convertUsersToRichUsersWithAttributes(perunSession, getDirectRichAdmins(perunSession, facility),
              getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException("One of Attribute not exist.", ex);
    }
  }

  @Override
  public List<EnrichedBanOnFacility> getEnrichedBansForFacility(PerunSession sess, Facility facility,
                                                                List<String> attrNames)
      throws AttributeNotExistsException {
    List<BanOnFacility> bans = getFacilitiesManagerImpl().getBansForFacility(sess, facility.getId());
    List<EnrichedBanOnFacility> enrichedBans = new ArrayList<>();
    List<AttributeDefinition> attrDefs = new ArrayList<>();

    if (attrNames != null && !attrNames.isEmpty()) {
      attrDefs = perunBl.getAttributesManagerBl().getAttributesDefinition(sess, attrNames);
    }

    for (BanOnFacility ban : bans) {
      EnrichedBanOnFacility enrichedBan = new EnrichedBanOnFacility();
      enrichedBan.setBan(ban);
      enrichedBan.setFacility(facility);
      try {
        User user = perunBl.getUsersManagerBl().getUserById(sess, ban.getUserId());
        RichUser richUser = perunBl.getUsersManagerBl().getRichUser(sess, user);

        if (attrDefs.isEmpty()) {
          richUser =
              perunBl.getUsersManagerBl().convertRichUsersToRichUsersWithAttributes(sess, List.of(richUser)).get(0);
        } else {
          richUser =
              perunBl.getUsersManagerBl().convertUsersToRichUsersWithAttributes(sess, List.of(richUser), attrDefs)
                  .get(0);
        }
        enrichedBan.setUser(richUser);
      } catch (UserNotExistsException e) {
        // should not happen
      }
      enrichedBans.add(enrichedBan);
    }

    return enrichedBans;
  }

  @Override
  public List<EnrichedBanOnFacility> getEnrichedBansForUser(PerunSession sess, User user, List<String> attrNames)
      throws AttributeNotExistsException {
    List<BanOnFacility> bans = perunBl.getFacilitiesManagerBl().getBansForUser(sess, user.getId());
    List<EnrichedBanOnFacility> enrichedBans = new ArrayList<>();
    List<AttributeDefinition> attrDefs = new ArrayList<>();


    if (attrNames != null && !attrNames.isEmpty()) {
      attrDefs = perunBl.getAttributesManagerBl().getAttributesDefinition(sess, attrNames);
    }

    RichUser richUser = perunBl.getUsersManagerBl().getRichUser(sess, user);
    try {
      if (attrDefs.isEmpty()) {
        richUser =
            perunBl.getUsersManagerBl().convertRichUsersToRichUsersWithAttributes(sess, List.of(richUser)).get(0);
      } else {
        richUser =
            perunBl.getUsersManagerBl().convertUsersToRichUsersWithAttributes(sess, List.of(richUser), attrDefs).get(0);
      }
    } catch (UserNotExistsException e) {
      // should not happen
    }

    for (BanOnFacility ban : bans) {
      EnrichedBanOnFacility enrichedBan = new EnrichedBanOnFacility();
      enrichedBan.setBan(ban);
      enrichedBan.setUser(richUser);

      try {
        enrichedBan.setFacility(perunBl.getFacilitiesManagerBl().getFacilityById(sess, ban.getFacilityId()));
      } catch (FacilityNotExistsException e) {
        // should not happen
      }
      enrichedBans.add(enrichedBan);
    }

    return enrichedBans;
  }

  @Override
  public List<EnrichedFacility> getEnrichedFacilities(PerunSession sess) {
    return getFacilities(sess).stream().map(facility -> convertToEnrichedFacility(sess, facility))
        .collect(Collectors.toList());
  }

  @Override
  public List<Facility> getFacilities(PerunSession sess) {
    List<Facility> facilities = getFacilitiesManagerImpl().getFacilities(sess);

    Collections.sort(facilities);

    return facilities;
  }

  @Override
  public List<Facility> getFacilitiesByAttribute(PerunSession sess, String attributeName, String attributeValue)
      throws WrongAttributeAssignmentException {
    try {
      AttributeDefinition attributeDef =
          getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
      Attribute attribute = new Attribute(attributeDef);
      attribute.setValue(BeansUtils.stringToAttributeValue(attributeValue, attribute.getType()));
      getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_FACILITY_ATTR);
      if (!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) ||
            getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute))) {
        throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
      }
      return getFacilitiesManagerImpl().getFacilitiesByAttribute(sess, attribute);
    } catch (AttributeNotExistsException e) {
      throw new ConsistencyErrorException(
          "Attribute name:'" + attributeName + "', value:'" + attributeValue + "' not exists ", e);
    }
  }

  @Override
  public List<FacilityWithAttributes> getFacilitiesByAttributeWithAttributes(PerunSession sess,
                                                                             String searchAttributeName,
                                                                             String searchAttributeValue,
                                                                             List<String> attrNames)
      throws AttributeNotExistsException {
    List<Facility> facilities;
    List<FacilityWithAttributes> result = new ArrayList<>();
    AttributeDefinition attrDef =
        getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, searchAttributeName);
    facilities = getFacilitiesManagerImpl().getFacilitiesByAttributePartialMatch(sess, attrDef, searchAttributeValue);

    for (Facility facility : facilities) {
      try {
        List<Attribute> attr = getPerunBl().getAttributesManager().getAttributes(sess, facility, attrNames);
        FacilityWithAttributes facWithAttr = new FacilityWithAttributes(facility, attr);
        result.add(facWithAttr);
      } catch (FacilityNotExistsException ex) {
        throw new InternalErrorException(ex);
      }
    }
    return result;
  }

  @Override
  public List<Facility> getFacilitiesByDestination(PerunSession sess, String destination)
      throws FacilityNotExistsException {
    return getFacilitiesManagerImpl().getFacilitiesByDestination(sess, destination);
  }

  @Override
  public List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname) {
    return facilitiesManagerImpl.getFacilitiesByHostName(sess, hostname);
  }

  @Override
  public List<Facility> getFacilitiesByIds(PerunSession sess, List<Integer> ids) {
    return getFacilitiesManagerImpl().getFacilitiesByIds(sess, ids);
  }

  @Override
  public List<Facility> getFacilitiesByPerunBean(PerunSession sess, Group group) {
    List<Facility> facilities = new ArrayList<>();
    List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
    for (Resource resourceElemenet : resources) {
      facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElemenet));
    }
    facilities = new ArrayList<>(new HashSet<>(facilities));
    return facilities;
  }

  @Override
  public List<Facility> getFacilitiesByPerunBean(PerunSession sess, Member member) {
    List<Facility> facilities = new ArrayList<>();
    List<Group> groupsForMember = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
    List<Resource> resourcesFromMember = new ArrayList<>();
    for (Group groupElement : groupsForMember) {
      resourcesFromMember.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, groupElement));
    }
    for (Resource resourceElement : resourcesFromMember) {
      facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElement));
    }
    facilities = new ArrayList<>(new HashSet<>(facilities));
    return facilities;
  }

  @Override
  public List<Facility> getFacilitiesByPerunBean(PerunSession sess, Resource resource) {
    return Collections.singletonList(getPerunBl().getResourcesManagerBl().getFacility(sess, resource));
  }

  @Override
  public List<Facility> getFacilitiesByPerunBean(PerunSession sess, User user) {
    List<Facility> facilities = new ArrayList<>();
    List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
    List<Resource> resourcesFromMembers = new ArrayList<>();
    for (Member memberElement : membersFromUser) {
      resourcesFromMembers.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, memberElement));
    }
    for (Resource resourceElement : resourcesFromMembers) {
      facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElement));
    }
    facilities = new ArrayList<>(new HashSet<>(facilities));
    return facilities;
  }

  @Override
  public List<Facility> getFacilitiesByPerunBean(PerunSession sess, Host host) {
    return Collections.singletonList(getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host));
  }

  @Override
  public List<Facility> getFacilitiesByPerunBean(PerunSession sess, Vo vo) {
    List<Facility> facilities = new ArrayList<>();
    List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
    for (Resource resourceElemenet : resources) {
      facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElemenet));
    }

    facilities = new ArrayList<>(new HashSet<>(facilities));
    return facilities;
  }

  @Override
  public int getFacilitiesCount(PerunSession sess) {
    return getFacilitiesManagerImpl().getFacilitiesCount(sess);
  }

  /**
   * Gets the facilitiesManagerImpl for this instance.
   *
   * @return The facilitiesManagerImpl.
   */
  public FacilitiesManagerImplApi getFacilitiesManagerImpl() {
    return this.facilitiesManagerImpl;
  }

  @Override
  public List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) {
    return facilitiesManagerImpl.getFacilitiesWhereUserIsAdmin(sess, user);
  }

  @Override
  public Facility getFacilityById(PerunSession sess, int id) throws FacilityNotExistsException {
    return getFacilitiesManagerImpl().getFacilityById(sess, id);
  }

  @Override
  public Facility getFacilityByName(PerunSession sess, String name) throws FacilityNotExistsException {
    return getFacilitiesManagerImpl().getFacilityByName(sess, name);
  }

  @Override
  public Facility getFacilityForHost(PerunSession sess, Host host) {
    return facilitiesManagerImpl.getFacilityForHost(sess, host);
  }

  @Override
  public Host getHostById(PerunSession sess, int id) throws HostNotExistsException {
    return facilitiesManagerImpl.getHostById(sess, id);
  }

  @Override
  public List<Host> getHosts(PerunSession sess, Facility facility) {
    return getFacilitiesManagerImpl().getHosts(sess, facility);
  }

  @Override
  public List<Host> getHostsByHostname(PerunSession sess, String hostname) {
    return facilitiesManagerImpl.getHostsByHostname(sess, hostname);
  }

  @Override
  public int getHostsCount(PerunSession sess, Facility facility) {
    return getFacilitiesManagerImpl().getHostsCount(sess, facility);
  }

  /**
   * Create list of attribute definitions from list of attribute names. List is defined like a constant with name
   * 'MANDATORY_ATTRIBUTES_FOR_USER_IN_CONTACT' These attributes will be returned like attribute definitions
   *
   * @param sess
   * @return list of attribute definitions from attrNames in constant
   * @throws InternalErrorException
   */
  private List<AttributeDefinition> getListOfMandatoryAttributes(PerunSession sess) {
    List<AttributeDefinition> mandatoryAttrs = new ArrayList<>();
    for (String attrName : MANDATORY_ATTRIBUTES_FOR_USER_IN_CONTACT) {
      try {
        mandatoryAttrs.add(perunBl.getAttributesManagerBl().getAttributeDefinition(sess, attrName));
      } catch (AttributeNotExistsException ex) {
        throw new InternalErrorException("Some of mandatory attributes for users in facility contacts not exists.", ex);
      }
    }
    return mandatoryAttrs;
  }

  @Override
  public List<Facility> getOwnerFacilities(PerunSession sess, Owner owner) {
    return getFacilitiesManagerImpl().getOwnerFacilities(sess, owner);
  }

  @Override
  @Deprecated
  public List<Owner> getOwners(PerunSession sess, Facility facility) {
    return getFacilitiesManagerImpl().getOwners(sess, facility);
  }

  /**
   * Gets the perunBl.
   *
   * @return The perunBl.
   */
  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  @Override
  public List<RichUser> getRichAdmins(PerunSession perunSession, Facility facility, List<String> specificAttributes,
                                      boolean allUserAttributes, boolean onlyDirectAdmins)
      throws UserNotExistsException {
    List<User> users = this.getAdmins(perunSession, facility, onlyDirectAdmins);
    List<RichUser> richUsers;

    if (allUserAttributes) {
      richUsers = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
    } else {
      try {
        richUsers = getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession,
            perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users),
            getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
      } catch (AttributeNotExistsException ex) {
        throw new InternalErrorException("One of Attribute not exist.", ex);
      }
    }

    return richUsers;
  }

  @Override
  @Deprecated
  public List<RichUser> getRichAdmins(PerunSession sess, Facility facility) {
    return getPerunBl().getUsersManagerBl().convertUsersToRichUsers(sess, this.getAdmins(sess, facility));
  }

  @Override
  @Deprecated
  public List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Facility facility)
      throws UserNotExistsException {
    return getPerunBl().getUsersManagerBl()
        .convertRichUsersToRichUsersWithAttributes(sess, this.getRichAdmins(sess, facility));
  }

  @Override
  @Deprecated
  public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility,
                                                            List<String> specificAttributes) {
    try {
      return getPerunBl().getUsersManagerBl()
          .convertUsersToRichUsersWithAttributes(perunSession, getRichAdmins(perunSession, facility),
              getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
    } catch (AttributeNotExistsException ex) {
      throw new InternalErrorException("One of Attribute not exist.", ex);
    }
  }

  @Override
  public List<RichFacility> getRichFacilities(PerunSession perunSession) {
    List<Facility> facilities = getFacilities(perunSession);
    return this.getRichFacilities(perunSession, facilities);
  }

  @Override
  public List<RichFacility> getRichFacilities(PerunSession perunSession, List<Facility> facilities) {
    List<RichFacility> richFacilities = new ArrayList<>();
    if (facilities == null || facilities.isEmpty()) {
      return richFacilities;
    } else {
      for (Facility f : facilities) {
        List<Owner> facilityOwners = this.getOwners(perunSession, f);
        RichFacility rf = new RichFacility(f, facilityOwners);
        richFacilities.add(rf);
      }
    }
    return richFacilities;
  }

  public boolean hostExists(PerunSession sess, Host host) {
    return getFacilitiesManagerImpl().hostExists(sess, host);
  }

  @Override
  public void removeAllExpiredBansOnFacilities(PerunSession sess) {
    List<BanOnFacility> expiredBans = this.getAllExpiredBansOnFacilities(sess);
    for (BanOnFacility expiredBan : expiredBans) {
      try {
        this.removeBan(sess, expiredBan.getId());
      } catch (BanNotExistsException ex) {
        LOG.warn("Ban {} can't be removed because it not exists yet.", expiredBan);
        //Skipt this, probably already removed
      }
    }
  }

  @Override
  public void removeBan(PerunSession sess, int banId) throws BanNotExistsException {
    BanOnFacility ban = this.getBanById(sess, banId);
    getFacilitiesManagerImpl().removeBan(sess, banId);
    getPerunBl().getAuditer().log(sess, new BanRemovedForFacility(ban, ban.getUserId(), ban.getFacilityId()));
  }

  @Override
  public void removeBan(PerunSession sess, int userId, int facilityId) throws BanNotExistsException {
    BanOnFacility ban = this.getBan(sess, userId, facilityId);
    getFacilitiesManagerImpl().removeBan(sess, userId, facilityId);
    getPerunBl().getAuditer().log(sess, new BanRemovedForFacility(ban, userId, facilityId));
  }

  @Override
  public void removeHost(PerunSession sess, Host host, Facility facility) throws HostAlreadyRemovedException {
    try {
      perunBl.getAttributesManagerBl().removeAllAttributes(sess, host);
    } catch (WrongAttributeValueException e) {
      throw new InternalErrorException(e);
    }
    facilitiesManagerImpl.removeHost(sess, host);
    getPerunBl().getAuditer().log(sess, new HostRemovedFromFacility(host, facility));
  }

  @Override
  public void removeHosts(PerunSession sess, List<Host> hosts, Facility facility) throws HostAlreadyRemovedException {
    for (Host host : hosts) {
      // Remove hosts attributes
      try {
        getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, host);
      } catch (WrongAttributeValueException e) {
        throw new InternalErrorException(e);
      }

      getFacilitiesManagerImpl().removeHost(sess, host);
      getPerunBl().getAuditer().log(sess, new HostRemovedFromFacility(host, facility));
    }

  }

  @Override
  @Deprecated
  public void removeOwner(PerunSession sess, Facility facility, Owner owner) throws OwnerAlreadyRemovedException {
    getFacilitiesManagerImpl().removeOwner(sess, facility, owner);
  }

  @Override
  public BanOnFacility setBan(PerunSession sess, BanOnFacility banOnFacility) throws BanAlreadyExistsException {
    if (this.banExists(sess, banOnFacility.getUserId(), banOnFacility.getFacilityId())) {
      throw new BanAlreadyExistsException(banOnFacility);
    }
    banOnFacility = getFacilitiesManagerImpl().setBan(sess, banOnFacility);
    getPerunBl().getAuditer()
        .log(sess, new BanSetForFacility(banOnFacility, banOnFacility.getUserId(), banOnFacility.getFacilityId()));
    return banOnFacility;
  }

  @Override
  @Deprecated
  public void setOwners(PerunSession sess, Facility facility, List<Owner> owners) {
    getFacilitiesManagerImpl().setOwners(sess, facility, owners);
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

  @Override
  public BanOnFacility updateBan(PerunSession sess, BanOnFacility banOnFacility) {
    banOnFacility = getFacilitiesManagerImpl().updateBan(sess, banOnFacility);
    getPerunBl().getAuditer()
        .log(sess, new BanUpdatedForFacility(banOnFacility, banOnFacility.getUserId(), banOnFacility.getFacilityId()));
    return banOnFacility;
  }

  @Override
  public Facility updateFacility(PerunSession sess, Facility facility)
      throws FacilityExistsException, ConsentHubExistsException {
    //check facility name, it can contain only a-zA-Z.0-9_-
    if (!facility.getName().matches("^[ a-zA-Z.0-9_-]+$")) {
      throw new IllegalArgumentException(
          "Wrong facility name, facility name can contain only a-Z0-9.-_ and space characters");
    }

    if (!facility.getName().equals(facility.getName().trim())) {
      throw new IllegalArgumentException(
          "Wrong facility name, facility name cannot contain leading and trailing spaces");
    }

    // if renaming the facility and consent hub's name match the old facility's name, then also rename the
    // corresponding consent hub
    try {
      Facility dbFacility = getFacilitiesManagerImpl().getFacilityById(sess, facility.getId());
      ConsentHub dbHub = getPerunBl().getConsentsManagerBl().getConsentHubByFacility(sess, facility.getId());

      if (!facility.getName().equals(dbFacility.getName()) && dbFacility.getName().equals(dbHub.getName())) {
        dbHub.setName(facility.getName());
        getPerunBl().getConsentsManagerBl().updateConsentHub(sess, dbHub);
      }
    } catch (ConsentHubNotExistsException | FacilityNotExistsException e) {
      throw new InternalErrorException(e);
    }

    getPerunBl().getAuditer().log(sess, new FacilityUpdated(facility));
    return getFacilitiesManagerImpl().updateFacility(sess, facility);
  }
}
