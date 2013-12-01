package cz.metacentrum.perun.core.entry;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.FacilitiesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichFacility;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.bl.FacilitiesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.FacilitiesManagerImplApi;
import java.util.Iterator;

/**
 * 
 * @author Slavek Licehammer glory@ics.muni.cz
 * @version $Id$
 */
public class FacilitiesManagerEntry implements FacilitiesManager {

  final static Logger log = LoggerFactory.getLogger(FacilitiesManagerEntry.class);

  private FacilitiesManagerBl facilitiesManagerBl;
  private PerunBl perunBl;

  public FacilitiesManagerEntry(PerunBl perunBl) {
    this.perunBl = perunBl;
    this.facilitiesManagerBl = perunBl.getFacilitiesManagerBl();
  }

  public FacilitiesManagerEntry() {}

  public FacilitiesManagerImplApi getFacilitiesManagerImpl() {
    throw new InternalErrorRuntimeException("Unsupported method!");
  }

  public Facility getFacilityById(PerunSession sess, int id) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    Facility facility = getFacilitiesManagerBl().getFacilityById(sess, id);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
        !AuthzResolver.isAuthorized(sess, Role.SERVICE) &&
        !AuthzResolver.isAuthorized(sess, Role.RPC)) {
      throw new PrivilegeException(sess, "getFacilityById");
    }

    return facility;
  }

  public Facility getFacilityByName(PerunSession sess, String name, String type) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    Utils.notNull(name, "name");

    Facility facility = getFacilitiesManagerBl().getFacilityByName(sess, name, type);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
        !AuthzResolver.isAuthorized(sess, Role.SERVICE) &&
        !AuthzResolver.isAuthorized(sess, Role.RPC)) {
      throw new PrivilegeException(sess, "getFacilityByName");
    }

    return facility;
  }
  
  public List<RichFacility> getRichFacilities(PerunSession sess) throws InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Perun admin can see everything
    if (AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
      return getFacilitiesManagerBl().getRichFacilities(sess);
    } else if (AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
      // Cast complementary object to Facility
      List<Facility> facilities = new ArrayList<Facility>();
      for (PerunBean facility: AuthzResolver.getComplementaryObjectsForRole(sess, Role.FACILITYADMIN, Facility.class)) {
        facilities.add((Facility) facility);
      }
      //Now I create list of richFacilities from facilities 
      return getFacilitiesManagerBl().getRichFacilities(sess, facilities);
    } else {
      throw new PrivilegeException(sess, "getRichFacilities");
    }
  }

  public List<Facility> getFacilitiesByDestination(PerunSession sess, String destination) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    Utils.notNull(destination, "destination");

    List<Facility> facilities = getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.SERVICE) &&
        !AuthzResolver.isAuthorized(sess, Role.RPC)) {
      throw new PrivilegeException(sess, "getFacilitiesByDestination");
    }

    return facilities;
  }

  public List<Facility> getFacilitiesByType(PerunSession sess, String type) throws InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);
    Utils.notNull(type, "type");

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
      throw new PrivilegeException(sess, "getFacilitiesByType");
    }

    return getFacilitiesManagerBl().getFacilitiesByType(sess, type);
  }

  public int getFacilitiesCountByType(PerunSession sess, String type) throws InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);
    Utils.notNull(type, "type");

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
      throw new PrivilegeException(sess, "getFacilitiesCountByType");
    }

    return getFacilitiesManagerBl().getFacilitiesCountByType(sess, type);
  }

  public int getFacilitiesCount(PerunSession sess) throws InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
      throw new PrivilegeException(sess, "getFacilitiesCount");
    }

    return getFacilitiesManagerBl().getFacilitiesCount(sess);
  }

  public List<Facility> getFacilities(PerunSession sess) throws InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Perun admin can see everything
    if (AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
      return getFacilitiesManagerBl().getFacilities(sess);
    } else if (AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
      // Cast complementary object to Facility
      List<Facility> facilities = new ArrayList<Facility>();
      for (PerunBean facility: AuthzResolver.getComplementaryObjectsForRole(sess, Role.FACILITYADMIN, Facility.class)) {
        facilities.add((Facility) facility);
      }
      return facilities;
    } else {
      throw new PrivilegeException(sess, "getFacilities");
    }
  }

  public List<Owner> getOwners(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
        !AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
      throw new PrivilegeException(sess, "getOwners");
    }

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    return getFacilitiesManagerBl().getOwners(sess, facility);
  }

  public void setOwners(PerunSession sess, Facility facility, List<Owner> owners) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, OwnerNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
      throw new PrivilegeException(sess, "setOwners");
    }

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    Utils.notNull(owners, "owners");
    for (Owner owner: owners) {
      getPerunBl().getOwnersManagerBl().checkOwnerExists(sess, owner);
    }

    getFacilitiesManagerBl().setOwners(sess, facility, owners);
  }

  public void addOwner(PerunSession sess, Facility facility, Owner owner) throws InternalErrorException, PrivilegeException, OwnerNotExistsException, FacilityNotExistsException, OwnerAlreadyAssignedException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "addOwner");
    }

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    getPerunBl().getOwnersManagerBl().checkOwnerExists(sess, owner);

    getFacilitiesManagerBl().addOwner(sess, facility, owner);
  }

  public void removeOwner(PerunSession sess, Facility facility, Owner owner) throws InternalErrorException, PrivilegeException, OwnerNotExistsException, FacilityNotExistsException, OwnerAlreadyRemovedException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "removeOwner");
    }

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    getPerunBl().getOwnersManagerBl().checkOwnerExists(sess, owner);

    getFacilitiesManagerBl().removeOwner(sess, facility, owner);
  }
  
  public void copyOwners(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
      Utils.checkPerunSession(sess);
      
      getFacilitiesManagerBl().checkFacilityExists(sess, sourceFacility);
      getFacilitiesManagerBl().checkFacilityExists(sess, destinationFacility);
      
      // Authorization - facility admin of the both facilities required
      if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, sourceFacility)) {
          throw new PrivilegeException(sess, "copyOwners");
      }
      if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, destinationFacility)) {
          throw new PrivilegeException(sess, "copyOwners");
      }
        
      getFacilitiesManagerBl().copyOwners(sess, sourceFacility, destinationFacility);
  }

  public List<Vo> getAllowedVos(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getAlloewdVos");
    }

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    return getFacilitiesManagerBl().getAllowedVos(sess, facility);

  }
  
  public List<Group> getAllowedGroups(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException {
     Utils.checkPerunSession(perunSession);
     
     //Authrorization
     if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(perunSession, "getAlloewdGroups");
     }
     
     getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
     if(specificVo != null) getPerunBl().getVosManagerBl().checkVoExists(perunSession, specificVo);
     if(specificService != null) getPerunBl().getServicesManagerBl().checkServiceExists(perunSession, specificService);
     
     return getFacilitiesManagerBl().getAllowedGroups(perunSession, facility, specificVo, specificService);
  }

   public List<User> getAllowedUsers(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException{
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getAllowedUsers");
    }

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    
    return getFacilitiesManagerBl().getAllowedUsers(sess, facility);
  }
  
  public List<User> getAllowedUsers(PerunSession sess, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getAllowedUsers");
    }

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    if(specificVo != null) getPerunBl().getVosManagerBl().checkVoExists(sess, specificVo);
    if(specificService != null) getPerunBl().getServicesManagerBl().checkServiceExists(sess, specificService);
    
    return getFacilitiesManagerBl().getAllowedUsers(sess, facility, specificVo, specificService);
  }

  public List<Resource> getAssignedResources(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getAssignedResources");
    }

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    return getFacilitiesManagerBl().getAssignedResources(sess, facility);
  }

  public List<RichResource> getAssignedRichResources(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getAssignedRichResources");
    }

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    return getFacilitiesManagerBl().getAssignedRichResources(sess, facility);

  }

  public Facility createFacility(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityExistsException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
      throw new PrivilegeException(sess, "createFacility");
    }

    return getFacilitiesManagerBl().createFacility(sess, facility);
  }

  public void deleteFacility(PerunSession sess, Facility facility) throws InternalErrorException, RelationExistsException, FacilityNotExistsException, PrivilegeException, FacilityAlreadyRemovedException, HostAlreadyRemovedException, GroupAlreadyRemovedException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
      throw new PrivilegeException(sess, "deleteFacility");
    }

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    getFacilitiesManagerBl().deleteFacility(sess, facility);
  }


  public List<Facility> getOwnerFacilities(PerunSession sess, Owner owner) throws InternalErrorException, OwnerNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
      throw new PrivilegeException(sess, "getOwnerFacilities");
    }

    getPerunBl().getOwnersManagerBl().checkOwnerExists(sess, owner);

    return getFacilitiesManagerBl().getOwnerFacilities(sess, owner);
  }

  public List<Facility> getAssignedFacilities(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
        !AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
        !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
        !AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
      throw new PrivilegeException(sess, "getAssignedFacilities");
    }

    return getFacilitiesManagerBl().getAssignedFacilities(sess, group);
  }

  public List<Facility> getAssignedFacilities(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.SELF, member) &&
        !AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
      throw new PrivilegeException(sess, "getAssignedFacilities");
    }

    return getFacilitiesManagerBl().getAssignedFacilities(sess, member);
  }

  public List<Facility> getAssignedFacilities(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.SELF, user) &&
        !AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
      throw new PrivilegeException(sess, "getAssignedFacilities");
    }

    return getFacilitiesManagerBl().getAssignedFacilities(sess, user);
  }

  public List<Facility> getAssignedFacilities(PerunSession sess, Service service) throws InternalErrorException, PrivilegeException, ServiceNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
      throw new PrivilegeException(sess, "getAssignedFacilities");
    }

    return getFacilitiesManagerBl().getAssignedFacilities(sess, service);
  }

  /**
   * Gets the facilitiesManagerBl for this instance.
   *
   * @return The facilitiesManagerBl.
   */
  public FacilitiesManagerBl getFacilitiesManagerBl() {
    return this.facilitiesManagerBl;
  }

  /**
   * Sets the perunBl.
   *
   * @param perunBl The perunBl.
   */
  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

  public List<Host> getHosts(PerunSession sess, Facility facility) throws FacilityNotExistsException, InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);
    getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    //TODO authorization

    return getFacilitiesManagerBl().getHosts(sess, facility);
  }

  public int getHostsCount(PerunSession sess, Facility facility) throws FacilityNotExistsException, InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getHostsCount");
    }

    return getFacilitiesManagerBl().getHostsCount(sess, facility);
  }

  public List<Host> addHosts(PerunSession sess, List<Host> hosts, Facility facility) throws FacilityNotExistsException, InternalErrorException, PrivilegeException, HostExistsException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "addHosts");
    }

    Utils.notNull(hosts, "hosts");

    return getFacilitiesManagerBl().addHosts(sess, hosts, facility);
  }

  public void removeHosts(PerunSession sess, List<Host> hosts, Facility facility) throws FacilityNotExistsException, InternalErrorException, PrivilegeException, HostAlreadyRemovedException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "removeHosts");
    }

    Utils.notNull(hosts, "hosts");

    getFacilitiesManagerBl().removeHosts(sess, hosts, facility);
  }
  
  public void addAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, FacilityNotExistsException, UserNotExistsException, PrivilegeException, AlreadyAdminException {
    Utils.checkPerunSession(sess);
    
    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
    
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "addAdmin");
    }
    
    getFacilitiesManagerBl().addAdmin(sess, facility, user);
  }
  
  @Override
    public void addAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, FacilityNotExistsException, GroupNotExistsException, PrivilegeException, AlreadyAdminException {
    Utils.checkPerunSession(sess);
    
    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
    
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "addAdmin");
    }
    
    getFacilitiesManagerBl().addAdmin(sess, facility, group);
  }
  
  public void removeAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, FacilityNotExistsException, UserNotExistsException, PrivilegeException, UserNotAdminException{
    Utils.checkPerunSession(sess);
    
    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "deleteAdmin");
    }
    
    getFacilitiesManagerBl().removeAdmin(sess, facility, user);
    
  }
  
  @Override
  public void removeAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, FacilityNotExistsException, GroupNotExistsException, PrivilegeException, GroupNotAdminException{
    Utils.checkPerunSession(sess);
    
    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "deleteAdmin");
    }
    
    getFacilitiesManagerBl().removeAdmin(sess, facility, group);
    
  }
  
  public List<User> getAdmins(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getAdmins");
    }

    return getFacilitiesManagerBl().getAdmins(sess, facility);
  }
  
  @Override
  public List<User> getDirectAdmins(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getDirectAdmins");
    }

    return getFacilitiesManagerBl().getDirectAdmins(sess, facility);
  }
  
  @Override
   public List<Group> getAdminGroups(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getAdminGroups");
    }

    return getFacilitiesManagerBl().getAdminGroups(sess, facility);
  }
  
  public List<RichUser> getRichAdmins(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException, FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getRichAdmins");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getFacilitiesManagerBl().getRichAdmins(sess, facility));
  }
  
  public List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException, FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "getRichAdminsWithAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getFacilitiesManagerBl().getRichAdminsWithAttributes(sess, facility));
  }  
  
  public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
    Utils.checkPerunSession(perunSession);

    getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
    // Authorization
    if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(perunSession, "getRichAdminsWithSpecificAttributes");
    }

    return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getFacilitiesManagerBl().getRichAdminsWithSpecificAttributes(perunSession, facility, specificAttributes));
  }
  
  public List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
	  Utils.checkPerunSession(sess);

	  // Authorization
	  if(!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
		  throw new PrivilegeException(sess, "getFacilitiesWhereUserIsAdmin");
	  }

	  getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

	  return getFacilitiesManagerBl().getFacilitiesWhereUserIsAdmin(sess, user);
  }
  
    public void copyManagers(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
        Utils.checkPerunSession(sess);

        getFacilitiesManagerBl().checkFacilityExists(sess, sourceFacility);
        getFacilitiesManagerBl().checkFacilityExists(sess, destinationFacility);
        
        // Authorization - facility admin of the both facilities required
        if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, sourceFacility)) {
            throw new PrivilegeException(sess, "copyManager");
        }
        if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, destinationFacility)) {
            throw new PrivilegeException(sess, "copyManager");
        }
        
        getFacilitiesManagerBl().copyManagers(sess, sourceFacility, destinationFacility);
    }
    
    public void copyAttributes(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
        Utils.checkPerunSession(sess);

        getFacilitiesManagerBl().checkFacilityExists(sess, sourceFacility);
        getFacilitiesManagerBl().checkFacilityExists(sess, destinationFacility);
        
        // Authorization - facility admin of the both facilities required
        if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, sourceFacility)) {
            throw new PrivilegeException(sess, "copyManager");
        }
        if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, destinationFacility)) {
            throw new PrivilegeException(sess, "copyManager");
        }
        
        getFacilitiesManagerBl().copyAttributes(sess, sourceFacility, destinationFacility);
    }

  
  /**
   * Sets the facilitiesManagerBl for this instance.
   *
   * @param facilitiesManagerBl The facilitiesManagerBl.
   */
  public void setFacilitiesManagerBl(FacilitiesManagerBl facilitiesManagerBl)
  {
    this.facilitiesManagerBl = facilitiesManagerBl;
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  public Host addHost(PerunSession sess, Host host, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkFacilityExists(sess, facility);
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "addHost");
    }

    Utils.notNull(host, "hosts");

    return getFacilitiesManagerBl().addHost(sess, host, facility);
  }

  public void removeHost(PerunSession sess, Host host) throws InternalErrorException, HostNotExistsException, PrivilegeException, HostAlreadyRemovedException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkHostExists(sess, host);
    Facility facility = getFacilitiesManagerBl().getFacilityForHost(sess, host);
    
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
      throw new PrivilegeException(sess, "removeHost");
    }

    getFacilitiesManagerBl().removeHost(sess, host);
  }

  public Host getHostById(PerunSession sess, int hostId) throws HostNotExistsException, InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);

    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) && 
        !AuthzResolver.isAuthorized(sess, Role.RPC)) {
      throw new PrivilegeException(sess, "getHostById");
    }

    return getFacilitiesManagerBl().getHostById(sess, hostId);
  }

  @Override
  public Facility getFacilityForHost(PerunSession sess, Host host) throws InternalErrorException, PrivilegeException, HostNotExistsException {
    Utils.checkPerunSession(sess);

    getFacilitiesManagerBl().checkHostExists(sess, host);
    
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
      throw new PrivilegeException(sess, "getFacilityForHost");
    }

    return getFacilitiesManagerBl().getFacilityForHost(sess, host);
  }
  
  public List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname) throws InternalErrorException, PrivilegeException {
    Utils.checkPerunSession(sess);
    
    List<Facility> facilities = getFacilitiesManagerBl().getFacilitiesByHostName(sess, hostname);
    
    if (!facilities.isEmpty()) {
      Iterator<Facility> iterator = facilities.iterator();
      while(iterator.hasNext()) {
        if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, iterator.next())) iterator.remove();
      }
    }
    
    return facilities;
  }
  
  public List<User> getAssignedUsers(PerunSession sess, Facility facility) throws PrivilegeException, InternalErrorException
  {
    Utils.checkPerunSession(sess);
    
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
      throw new PrivilegeException(sess, "getAssignedUser");
    }
      
    return this.getPerunBl().getFacilitiesManagerBl().getAssignedUsers(sess,facility);  
  }
  
  public List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service) throws PrivilegeException, InternalErrorException{
     Utils.checkPerunSession(sess);
    
    // Authorization
    if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
      throw new PrivilegeException(sess, "getAssignedUser");
    }
      
    return this.getPerunBl().getFacilitiesManagerBl().getAssignedUsers(sess,facility,service);   
  }
}
