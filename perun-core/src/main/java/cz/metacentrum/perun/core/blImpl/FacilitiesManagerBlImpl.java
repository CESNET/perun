package cz.metacentrum.perun.core.blImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
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
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.rt.ConsistencyErrorRuntimeException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.bl.FacilitiesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.FacilitiesManagerImplApi;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class FacilitiesManagerBlImpl implements FacilitiesManagerBl {

	final static Logger log = LoggerFactory.getLogger(FacilitiesManagerBlImpl.class);

	private final FacilitiesManagerImplApi facilitiesManagerImpl;
	private PerunBl perunBl;
	private AtomicBoolean initialized = new AtomicBoolean(false);

	public FacilitiesManagerBlImpl(FacilitiesManagerImplApi facilitiesManagerImpl) {
		this.facilitiesManagerImpl = facilitiesManagerImpl;
	}

	public Facility getFacilityById(PerunSession sess, int id) throws InternalErrorException, FacilityNotExistsException {
		return getFacilitiesManagerImpl().getFacilityById(sess, id);
	}

	public Facility getFacilityByName(PerunSession sess, String name) throws InternalErrorException, FacilityNotExistsException {
		return getFacilitiesManagerImpl().getFacilityByName(sess, name);
	}

	public List<RichFacility> getRichFacilities(PerunSession perunSession) throws InternalErrorException {
		List<Facility> facilities = getFacilities(perunSession);
		List<RichFacility> richFacilities = this.getRichFacilities(perunSession, facilities);
		return richFacilities;
	}

	public List<RichFacility> getRichFacilities(PerunSession perunSession, List<Facility> facilities) throws InternalErrorException {
		List<RichFacility> richFacilities = new ArrayList<RichFacility>();
		if(facilities == null || facilities.isEmpty()) return richFacilities;
		else {
			for(Facility f: facilities) {
				List<Owner> fOwners = this.getOwners(perunSession, f);
				RichFacility rf = new RichFacility(f, fOwners);
				richFacilities.add(rf);
			}
		}
		return richFacilities;
	}

	public List<Facility> getFacilitiesByDestination(PerunSession sess, String destination) throws InternalErrorException, FacilityNotExistsException {
		return getFacilitiesManagerImpl().getFacilitiesByDestination(sess, destination);
	}

	public List<Facility> getFacilities(PerunSession sess) throws InternalErrorException {
		List<Facility> facilities = getFacilitiesManagerImpl().getFacilities(sess);

		Collections.sort(facilities);

		return facilities;
	}

	public int getFacilitiesCount(PerunSession sess) throws InternalErrorException {
		return getFacilitiesManagerImpl().getFacilitiesCount(sess);
	}

	public List<Owner> getOwners(PerunSession sess, Facility facility) throws InternalErrorException {
		List<Integer> ownersIds = getFacilitiesManagerImpl().getOwnersIds(sess, facility);
		List<Owner> owners = new ArrayList<Owner>();

		for (Integer ownerId: ownersIds) {
			try {
				owners.add(getPerunBl().getOwnersManagerBl().getOwnerById(sess, ownerId));
			} catch (OwnerNotExistsException e) {
				throw new ConsistencyErrorException("Non-existent owner is assigned to the facility", e);
			}
		}

		return owners;
	}

	public void setOwners(PerunSession sess, Facility facility, List<Owner> owners) throws InternalErrorException {
		getFacilitiesManagerImpl().setOwners(sess, facility, owners);
	}

	public void addOwner(PerunSession sess, Facility facility, Owner owner) throws InternalErrorException, OwnerAlreadyAssignedException {
		getFacilitiesManagerImpl().addOwner(sess, facility, owner);
	}

	public void removeOwner(PerunSession sess, Facility facility, Owner owner) throws InternalErrorException, OwnerAlreadyRemovedException {
		getFacilitiesManagerImpl().removeOwner(sess, facility, owner);
	}

	public void copyOwners(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException {
		for (Owner owner: getOwners(sess, sourceFacility)) {
			try {
				addOwner(sess, destinationFacility, owner);
			} catch (OwnerAlreadyAssignedException ex) {
				// we can ignore the exception in this particular case, owner can exists in both of the facilities
			}
		}
	}

	public List<Vo> getAllowedVos(PerunSession sess, Facility facility) throws InternalErrorException {
		List<Vo> vos = new ArrayList<Vo>();
		List<Integer> voIds =  getFacilitiesManagerImpl().getAllowedVosIds(sess, facility);
		try {
			for(Integer id : voIds) {
				vos.add(getPerunBl().getVosManagerBl().getVoById(sess, id));
			}
		} catch(VoNotExistsException ex) {
			throw new ConsistencyErrorException("Non-existent VO is allowed on the facility", ex);
		}
		return vos;
	}

	public List<Group> getAllowedGroups(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException {
		//Get all facilities resources
		List<Resource> facilityResources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(perunSession, facility);

		//Remove all resources which are not in specific VO (if is specific)
		if(specificVo != null) {
			Iterator<Resource> iter = facilityResources.iterator();
			while(iter.hasNext()) {
				if(specificVo.getId() != iter.next().getVoId()) iter.remove();
			}
		}

		//Remove all resources which has not assigned specific service (if is specific)
		if(specificService != null) {
			List<Resource> resourcesWhereServiceIsAssigned = getPerunBl().getServicesManagerBl().getAssignedResources(perunSession, specificService);
			facilityResources.retainAll(resourcesWhereServiceIsAssigned);
		}

		//GetAll Groups for resulted Resources
		Set<Group> allowedGroups = new HashSet<Group>();
		for(Resource r: facilityResources) {
			allowedGroups.addAll(getPerunBl().getResourcesManagerBl().getAssignedGroups(perunSession, r));
		}
		return new ArrayList<Group>(allowedGroups);
	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Facility facility) throws InternalErrorException {
		//Get all facilities resources
		List<Resource> resources = this.getAssignedResources(sess, facility);

		Set<User> users =  new TreeSet<User>();
		for (Resource resource: resources) {
			users.addAll(getPerunBl().getResourcesManagerBl().getAllowedUsers(sess, resource));
		}

		return new ArrayList<>(users);
	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException {
		//Get all facilities resources
		List<Resource> resources = this.getAssignedResources(sess, facility);

		//Remove all resources which are not in specific VO (if is specific)
		if(specificVo != null) {
			Iterator<Resource> iter = resources.iterator();
			while(iter.hasNext()) {
				if(specificVo.getId() != iter.next().getVoId()) iter.remove();
			}
		}

		//Remove all resources which has not assigned specific service (if is specific)
		if(specificService != null) {
			List<Resource> resourcesWhereServiceIsAssigned = getPerunBl().getServicesManagerBl().getAssignedResources(sess, specificService);
			resources.retainAll(resourcesWhereServiceIsAssigned);
		}

		List<User> users =  new ArrayList<User>();
		for (Resource resource: resources) {
			users.addAll(getPerunBl().getResourcesManagerBl().getAllowedUsers(sess, resource));
		}

		return users;
	}

	public List<Resource> getAssignedResources(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getAssignedResources(sess, facility);
	}

	public List<RichResource> getAssignedRichResources(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getAssignedRichResources(sess, facility);
	}

	public Facility createFacility(PerunSession sess, Facility facility) throws InternalErrorException, FacilityExistsException {

		//check facility name, it can contain only a-zA-Z.0-9_-
		if (!facility.getName().matches("^[ a-zA-Z.0-9_-]+$")) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong facility name, facility name can contain only a-Z0-9.-_ and space characters"));
		}

		//check if facility have uniq name
		try {
			this.getFacilityByName(sess, facility.getName());
			throw new FacilityExistsException(facility);
		} catch(FacilityNotExistsException ex) { /* OK */ }

		// create facility
		facility = getFacilitiesManagerImpl().createFacility(sess, facility);
		getPerunBl().getAuditer().log(sess, "Facility created {}.", facility);

		//set creator as Facility manager
		if(sess.getPerunPrincipal().getUser() != null) {
			try {
				addAdmin(sess, facility, sess.getPerunPrincipal().getUser());
			} catch(AlreadyAdminException ex) {
				throw new ConsistencyErrorException("Add manager to newly created Facility failed because there is particular manager already assigned", ex);
			}
		} else {
			log.error("Can't set Facility manager during creating of the Facility. User from perunSession is null. {} {}", facility, sess);
		}

		return facility;
	}

	public void deleteFacility(PerunSession sess, Facility facility) throws InternalErrorException, RelationExistsException, FacilityAlreadyRemovedException, HostAlreadyRemovedException, GroupAlreadyRemovedException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		List<Host> hosts = this.getHosts(sess, facility);
		for (Host host: hosts) {
			this.removeHost(sess, host);
		}

		if (getFacilitiesManagerImpl().getAssignedResources(sess, facility).size() > 0) {
			throw new RelationExistsException("Facility is still used as a resource");
		}

		// remove associated attributes
		try {
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, facility);
		} catch (WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		} catch (WrongReferenceAttributeValueException e) {
			throw new InternalErrorException(e);
		}

		// delete facility
		getFacilitiesManagerImpl().deleteFacilityOwners(sess, facility);
		getFacilitiesManagerImpl().deleteFacility(sess, facility);
		getPerunBl().getAuditer().log(sess, "Facility deleted {}.", facility);
	}

	public Facility updateFacility(PerunSession sess, Facility facility) throws InternalErrorException {
		getPerunBl().getAuditer().log(sess, "{} updated.", facility);
		return getFacilitiesManagerImpl().updateFacility(sess, facility);
	}

	public List<Facility> getOwnerFacilities(PerunSession sess, Owner owner) throws InternalErrorException {
		return getFacilitiesManagerImpl().getOwnerFacilities(sess, owner);
	}

	/**
	 * Gets the facilitiesManagerImpl for this instance.
	 *
	 * @return The facilitiesManagerImpl.
	 */
	public FacilitiesManagerImplApi getFacilitiesManagerImpl() {
		return this.facilitiesManagerImpl;
	}

	/**
	 * Gets the perunBl.
	 *
	 * @return The perunBl.
	 */
	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public List<Facility> getAssignedFacilities(PerunSession sess, Group group) throws InternalErrorException {
		// Get all assigned resources for the group and the derive the facilities
		List<Resource> assignedResources = perunBl.getResourcesManagerBl().getAssignedResources(sess, group);

		List<Facility> assignedFacilities = new ArrayList<Facility>();
		for (Resource resource: assignedResources) {
			assignedFacilities.add(perunBl.getResourcesManagerBl().getFacility(sess, resource));
		}

		return assignedFacilities;
	}

	public List<Facility> getAssignedFacilities(PerunSession sess, Member member) throws InternalErrorException {
		Set<Facility> assignedFacilities = new HashSet<Facility>();

		for (Resource resource : getPerunBl().getResourcesManagerBl().getAssignedResources(sess, member)) {
			assignedFacilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resource));
		}

		return new ArrayList<Facility>(assignedFacilities);
	}

	public List<Facility> getAssignedFacilities(PerunSession sess, User user) throws InternalErrorException {
		List<Member> members = perunBl.getMembersManagerBl().getMembersByUser(sess, user);

		Set<Facility> assignedFacilities = new HashSet<Facility>();
		for (Member member: members) {
			assignedFacilities.addAll(this.getAssignedFacilities(sess, member));
		}

		return new ArrayList<Facility>(assignedFacilities);
	}

	public List<Facility> getAllowedFacilities(PerunSession sess, User user) throws InternalErrorException {
		List<Member> members = perunBl.getMembersManagerBl().getMembersByUser(sess, user);

		Set<Facility> assignedFacilities = new HashSet<Facility>();
		for(Member member : members) {
			if(!getPerunBl().getMembersManagerBl().haveStatus(sess, member, Status.INVALID)) {
				assignedFacilities.addAll(this.getAssignedFacilities(sess, member));
			}
		}

		return new ArrayList<Facility>(assignedFacilities);
	}

	public List<Facility> getAssignedFacilities(PerunSession sess, Service service) throws InternalErrorException {
		List<Resource> resources = perunBl.getServicesManagerBl().getAssignedResources(sess, service);

		Set<Facility> assignedFacilities = new HashSet<Facility>();
		for (Resource resource: resources) {
			assignedFacilities.add(perunBl.getResourcesManagerBl().getFacility(sess, resource));
		}

		return new ArrayList<Facility>(assignedFacilities);
	}

	public List<Facility> getFacilitiesByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_FACILITY_ATTR);
		if(!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) || getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute))) throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
		return getFacilitiesManagerImpl().getFacilitiesByAttribute(sess, attribute);
	}

	public void checkFacilityExists(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException {
		getFacilitiesManagerImpl().checkFacilityExists(sess, facility);
	}

	public void checkHostExists(PerunSession sess, Host host) throws InternalErrorException, HostNotExistsException {
		getFacilitiesManagerImpl().checkHostExists(sess, host);
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	public List<Host> getHosts(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getHosts(sess, facility);
	}

	public int getHostsCount(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getHostsCount(sess, facility);
	}

	public List<Host> addHosts(PerunSession sess, List<Host> hosts, Facility facility) throws InternalErrorException, HostExistsException {
		//check if hosts not exist in cluster
		List<Host> alreadyAssignedHosts = getHosts(sess, facility);	
		
		Set<String> alreadyAssignedHostnames = new HashSet<String>();
		Set<String> newHostnames = new HashSet<String>();
		for(Host h : alreadyAssignedHosts) alreadyAssignedHostnames.add(h.getHostname());
		for(Host h : hosts) newHostnames.add(h.getHostname());
		newHostnames.retainAll(alreadyAssignedHostnames);
		if(!newHostnames.isEmpty()) throw new HostExistsException(newHostnames.toString());

		for(Host host : hosts) {
			host = getFacilitiesManagerImpl().addHost(sess, host, facility);
		}

		getPerunBl().getAuditer().log(sess, "Hosts {} added to cluster {}", hosts, facility);

		return hosts;
	}

	public List<Host> addHosts(PerunSession sess, Facility facility, List<String> hosts) throws InternalErrorException, HostExistsException, WrongPatternException {
		// generate hosts by pattern
		List<Host> generatedHosts = new ArrayList<Host>();
		for (String host : hosts) {
			List<String> listOfStrings = Utils.generateStringsByPattern(host);
			List<Host> listOfHosts = new ArrayList<Host>();
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

	public void removeHosts(PerunSession sess, List<Host> hosts, Facility facility) throws InternalErrorException, HostAlreadyRemovedException {
		for(Host host : hosts) {
			// Remove hosts attributes
			try {
				getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, host);
			} catch (WrongAttributeValueException e) {
				throw new InternalErrorException(e);
			} catch (WrongReferenceAttributeValueException e) {
				throw new InternalErrorException(e);
			}

			getFacilitiesManagerImpl().removeHost(sess, host);
		}

		getPerunBl().getAuditer().log(sess, "Hosts {} removed from cluster {}", hosts, facility);
	}

	public boolean hostExists(PerunSession sess, Host host) throws InternalErrorException {
		return getFacilitiesManagerImpl().hostExists(sess, host);
	}

	public void addAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, AlreadyAdminException {
		AuthzResolverBlImpl.setRole(sess, user, facility, Role.FACILITYADMIN);
		getPerunBl().getAuditer().log(sess, "{} was added as admin of {}.", user, facility);
	}

	@Override
	public void addAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, AlreadyAdminException {
		List<Group> listOfAdmins = getAdminGroups(sess, facility);
		if (listOfAdmins.contains(group)) throw new AlreadyAdminException(group);
		
		AuthzResolverBlImpl.setRole(sess, group, facility, Role.FACILITYADMIN);
		getPerunBl().getAuditer().log(sess, "Group {} was added as admin of {}.", group, facility);
	}

	public void removeAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, UserNotAdminException {
		AuthzResolverBlImpl.unsetRole(sess, user, facility, Role.FACILITYADMIN);
		getPerunBl().getAuditer().log(sess, "{} was removed from admins of {}.", user, facility);
	}

	@Override
	public void removeAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, GroupNotAdminException {
		List<Group> listOfAdmins = getAdminGroups(sess, facility);
		if (!listOfAdmins.contains(group)) throw new GroupNotAdminException(group);
		
		AuthzResolverBlImpl.unsetRole(sess, group, facility, Role.FACILITYADMIN);
		getPerunBl().getAuditer().log(sess, "Group {} was removed from admins of {}.", group, facility);
	}

	public List<User> getAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		return facilitiesManagerImpl.getAdmins(sess, facility);
	}

	@Override
	public List<User> getDirectAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		return facilitiesManagerImpl.getDirectAdmins(sess, facility);
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Facility facility) throws InternalErrorException {
		return facilitiesManagerImpl.getAdminGroups(sess, facility);
	}

	public List<RichUser> getRichAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		return getPerunBl().getUsersManagerBl().convertUsersToRichUsers(sess, this.getAdmins(sess, facility));
	}
	
	public List<RichUser> getDirectRichAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		return getPerunBl().getUsersManagerBl().convertUsersToRichUsers(sess, this.getDirectAdmins(sess, facility));
	}

	public List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException {
		return getPerunBl().getUsersManagerBl().convertRichUsersToRichUsersWithAttributes(sess, this.getRichAdmins(sess, facility));
	}

	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, getRichAdmins(perunSession, facility), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, getDirectRichAdmins(perunSession, facility), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	public List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		return  facilitiesManagerImpl.getFacilitiesWhereUserIsAdmin(sess, user);
	}

	public Host addHost(PerunSession sess, Host host, Facility facility) throws InternalErrorException {
		getPerunBl().getAuditer().log(sess, "{} added to {}.", host, facility);
		return facilitiesManagerImpl.addHost(sess, host, facility);
	}

	public void removeHost(PerunSession sess, Host host) throws InternalErrorException, HostAlreadyRemovedException {
		facilitiesManagerImpl.removeHost(sess, host);
		getPerunBl().getAuditer().log(sess, "{} removed.", host);
	}

	public Host getHostById(PerunSession sess, int id) throws HostNotExistsException, InternalErrorException {
		return facilitiesManagerImpl.getHostById(sess, id);
	}

	public Facility getFacilityForHost(PerunSession sess, Host host) throws InternalErrorException {
		return facilitiesManagerImpl.getFacilityForHost(sess, host);
	}

	public List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname) throws InternalErrorException {
		return facilitiesManagerImpl.getFacilitiesByHostName(sess, hostname);
	}

	public List<Facility> getFacilitiesByPerunBean(PerunSession sess, PerunBean perunBean) throws InternalErrorException {
		List<Facility> facilities = new ArrayList<Facility>();

		//All possible useful objects
		Vo vo = null;
		Facility facility = null;
		Group group = null;
		Member member = null;
		User user = null;
		Host host = null;
		Resource resource = null;

		if(perunBean != null) {
			if(perunBean instanceof Vo) vo = (Vo) perunBean;
			else if(perunBean instanceof Facility) facility = (Facility) perunBean;
			else if(perunBean instanceof Group) group = (Group) perunBean;
			else if(perunBean instanceof Member) member = (Member) perunBean;
			else if(perunBean instanceof User) user = (User) perunBean;
			else if(perunBean instanceof Host) host = (Host) perunBean;
			else if(perunBean instanceof Resource) resource = (Resource) perunBean;
			else {
				throw new InternalErrorException("There is unrecognized object in primaryHolder of aidingAttr.");
			}
		} else {
			throw new InternalErrorException("Aiding attribtue must have primaryHolder which is not null.");
		}

		//Important For Groups not work with Subgroups! Invalid members are executed too.

		if(group != null) {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
			for(Resource resourceElemenet: resources) {
				facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElemenet));
			}
		} else if(member != null) {
			List<Group> groupsForMember = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
			List<Resource> resourcesFromMember = new ArrayList<Resource>();
			for(Group groupElement: groupsForMember) {
				resourcesFromMember.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, groupElement));
			}
			for(Resource resourceElement: resourcesFromMember) {
				facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElement));
			}
		} else if(resource != null) {
			facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resource));
		} else if(user != null) {
			List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
			List<Resource> resourcesFromMembers = new ArrayList<Resource>();
			for(Member memberElement: membersFromUser) {
				resourcesFromMembers.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, member));
			}
			for(Resource resourceElement: resourcesFromMembers) {
				facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElement));
			}
		} else if(host != null) {
			facilities.add(getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host));
		} else if(vo != null) {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
			for(Resource resourceElemenet: resources) {
				facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElemenet));
			}
		}

		facilities = new ArrayList<Facility>(new HashSet<Facility>(facilities));
		return facilities;
	}

	public List<User> getAssignedUsers(PerunSession sess, Facility facility) throws InternalErrorException{
		return this.getFacilitiesManagerImpl().getAssignedUsers(sess, facility);
	}
	public List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service) throws InternalErrorException{
		return this.getFacilitiesManagerImpl().getAssignedUsers(sess, facility,service);
	}

	public void copyManagers(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException {
		for (User admin: getDirectAdmins(sess, sourceFacility)) {
			try {
				addAdmin(sess, destinationFacility, admin);
			} catch (AlreadyAdminException ex) {
				// we can ignore the exception in this particular case, user can be admin in both of the facilities
			}
		}

		for (Group adminGroup: getAdminGroups(sess, sourceFacility)) {
			try {
				addAdmin(sess, destinationFacility, adminGroup);
			} catch (AlreadyAdminException ex) {
				// we can ignore the exception in this particular case, group can be admin in both of the facilities
			}
		}
	}

	public void copyAttributes(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> sourceAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, sourceFacility);
		List<Attribute> destinationAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, destinationFacility);

		// do not get virtual attributes from source facility, they can't be set to destination
		Iterator<Attribute> it = sourceAttributes.iterator();
		while (it.hasNext()) {
			if (it.next().getNamespace().startsWith(AttributesManager.NS_FACILITY_ATTR_VIRT)) {
				it.remove();
			}
		}

		// create intersection of destination and source attributes
		List<Attribute> intersection = new ArrayList<>();
		intersection.addAll(destinationAttributes);
		intersection.retainAll(sourceAttributes);

		// delete all common attributes from destination facility
		getPerunBl().getAttributesManagerBl().removeAttributes(sess, destinationFacility, intersection);
		// add all attributes from source facility to destination facility
		getPerunBl().getAttributesManagerBl().setAttributes(sess, destinationFacility, sourceAttributes);
	}

}
