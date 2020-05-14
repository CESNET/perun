package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanSetForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.BanUpdatedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityCreated;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityDeleted;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityUpdated;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.GroupContactsRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.GroupsAddedToContactGroupOfFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.GroupsRemovedFromContactGroupOfFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.HostAddedToFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.HostRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.HostsAddedToFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.HostsRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.OwnerContactsRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.OwnersAddedToContactGroupOfFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.OwnersRemovedFromContactGroupOfFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.SecurityTeamAssignedToFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.SecurityTeamRemovedFromFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.UserContactsRemovedForFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.UsersAddedToContactGroupOfFacility;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.UsersRemovedFromContactGroupOfFacility;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.Facility;
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
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityContactNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotAssignedException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class FacilitiesManagerBlImpl implements FacilitiesManagerBl {

	final static Logger log = LoggerFactory.getLogger(FacilitiesManagerBlImpl.class);

	private final FacilitiesManagerImplApi facilitiesManagerImpl;
	private PerunBl perunBl;
	private AtomicBoolean initialized = new AtomicBoolean(false);

	private static final List<String> MANDATORY_ATTRIBUTES_FOR_USER_IN_CONTACT = new ArrayList<>(Arrays.asList(
	  AttributesManager.NS_USER_ATTR_DEF + ":organization",
	  AttributesManager.NS_USER_ATTR_DEF + ":preferredMail"));

	public FacilitiesManagerBlImpl(FacilitiesManagerImplApi facilitiesManagerImpl) {
		this.facilitiesManagerImpl = facilitiesManagerImpl;
	}

	@Override
	public Facility getFacilityById(PerunSession sess, int id) throws InternalErrorException, FacilityNotExistsException {
		return getFacilitiesManagerImpl().getFacilityById(sess, id);
	}

	@Override
	public Facility getFacilityByName(PerunSession sess, String name) throws InternalErrorException, FacilityNotExistsException {
		return getFacilitiesManagerImpl().getFacilityByName(sess, name);
	}

	@Override
	public List<RichFacility> getRichFacilities(PerunSession perunSession) throws InternalErrorException {
		List<Facility> facilities = getFacilities(perunSession);
		return this.getRichFacilities(perunSession, facilities);
	}

	@Override
	public List<RichFacility> getRichFacilities(PerunSession perunSession, List<Facility> facilities) throws InternalErrorException {
		List<RichFacility> richFacilities = new ArrayList<>();
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

	@Override
	public List<Facility> getFacilitiesByDestination(PerunSession sess, String destination) throws InternalErrorException, FacilityNotExistsException {
		return getFacilitiesManagerImpl().getFacilitiesByDestination(sess, destination);
	}

	@Override
	public List<Facility> getFacilities(PerunSession sess) throws InternalErrorException {
		List<Facility> facilities = getFacilitiesManagerImpl().getFacilities(sess);

		Collections.sort(facilities);

		return facilities;
	}

	@Override
	public int getFacilitiesCount(PerunSession sess) throws InternalErrorException {
		return getFacilitiesManagerImpl().getFacilitiesCount(sess);
	}

	@Override
	public List<Owner> getOwners(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getOwners(sess, facility);
	}

	@Override
	public void setOwners(PerunSession sess, Facility facility, List<Owner> owners) throws InternalErrorException {
		getFacilitiesManagerImpl().setOwners(sess, facility, owners);
	}

	@Override
	public void addOwner(PerunSession sess, Facility facility, Owner owner) throws InternalErrorException, OwnerAlreadyAssignedException {
		getFacilitiesManagerImpl().addOwner(sess, facility, owner);
	}

	@Override
	public void removeOwner(PerunSession sess, Facility facility, Owner owner) throws InternalErrorException, OwnerAlreadyRemovedException {
		getFacilitiesManagerImpl().removeOwner(sess, facility, owner);
	}

	@Override
	public void copyOwners(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException {
		for (Owner owner: getOwners(sess, sourceFacility)) {
			try {
				addOwner(sess, destinationFacility, owner);
			} catch (OwnerAlreadyAssignedException ex) {
				// we can ignore the exception in this particular case, owner can exists in both of the facilities
			}
		}
	}

	@Override
	public List<Vo> getAllowedVos(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getAllowedVos(sess, facility);
	}

	@Override
	public List<Group> getAllowedGroups(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException {
		//Get all facilities resources
		List<Resource> facilityResources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(perunSession, facility, specificVo, specificService);

		//GetAll Groups for resulted Resources
		Set<Group> allowedGroups = new HashSet<>();
		for(Resource r: facilityResources) {
			allowedGroups.addAll(getPerunBl().getResourcesManagerBl().getAssignedGroups(perunSession, r));
		}
		return new ArrayList<>(allowedGroups);
	}

	@Override
	public List<RichGroup> getAllowedRichGroupsWithAttributes(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService, List<String> attrNames) throws InternalErrorException {

		List<Group> allowedGroups = getAllowedGroups(perunSession, facility, specificVo, specificService);
		return perunBl.getGroupsManagerBl().convertGroupsToRichGroupsWithAttributes(perunSession, allowedGroups, attrNames);

	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Facility facility) throws InternalErrorException {
		//Get all facilities resources
		List<Resource> resources = this.getAssignedResources(sess, facility);

		Set<User> users = new TreeSet<>();
		for (Resource resource: resources) {
			users.addAll(getPerunBl().getResourcesManagerBl().getAllowedUsers(sess, resource));
		}

		return new ArrayList<>(users);
	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException {

		//Get all facilities resources
		List<Resource> resources = getAssignedResources(sess, facility, specificVo, specificService);

		Set<User> users = new TreeSet<>();
		for (Resource resource: resources) {
			users.addAll(getPerunBl().getResourcesManagerBl().getAllowedUsers(sess, resource));
		}

		return new ArrayList<>(users);
	}

	@Override
	public List<User> getAllowedUsersNotExpired(PerunSession sess, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException {

		//Get all facilities resources
		List<Resource> resources = getAssignedResources(sess, facility, specificVo, specificService);

		Set<User> users = new TreeSet<>();
		for (Resource resource: resources) {
			users.addAll(getPerunBl().getResourcesManagerBl().getAllowedUsersNotExpired(sess, resource));
		}

		return new ArrayList<>(users);
	}

	@Override
	public List<Member> getAllowedMembers(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getAllowedMembers(sess, facility);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getAssignedResources(sess, facility);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException {
		if (specificVo == null && specificService == null) return getAssignedResources(sess, facility);
		return getFacilitiesManagerImpl().getAssignedResources(sess, facility, specificVo, specificService);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getAssignedRichResources(sess, facility);
	}

	@Override
	public Facility createFacility(PerunSession sess, Facility facility) throws InternalErrorException, FacilityExistsException {

		//check facility name, it can contain only a-zA-Z.0-9_-
		if (!facility.getName().matches("^[ a-zA-Z.0-9_-]+$")) {
			throw new IllegalArgumentException("Wrong facility name, facility name can contain only a-Z0-9.-_ and space characters");
		}

		//check if facility have uniq name
		try {
			this.getFacilityByName(sess, facility.getName());
			throw new FacilityExistsException(facility);
		} catch(FacilityNotExistsException ex) { /* OK */ }

		// create facility
		facility = getFacilitiesManagerImpl().createFacility(sess, facility);
		getPerunBl().getAuditer().log(sess, new FacilityCreated(facility));
		//set creator as Facility manager
		if(sess.getPerunPrincipal().getUser() != null) {
			try {
				AuthzResolverBlImpl.setRole(sess, sess.getPerunPrincipal().getUser(), facility, Role.FACILITYADMIN);
			} catch(AlreadyAdminException ex) {
				throw new ConsistencyErrorException("Add manager to newly created Facility failed because there is particular manager already assigned", ex);
			}
		} else {
			log.warn("Can't set Facility manager during creating of the Facility. User from perunSession is null. {} {}", facility, sess);
		}

		return facility;
	}

	@Override
	public void deleteFacility(PerunSession sess, Facility facility, Boolean force) throws InternalErrorException, RelationExistsException, FacilityAlreadyRemovedException, HostAlreadyRemovedException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {

		if (force) {
			List<Resource> resources = this.getAssignedResources(sess, facility);
			for (Resource resource : resources) {
				getPerunBl().getResourcesManagerBl().deleteResource(sess, resource);
			}
			List<Task> tasks = perunBl.getTasksManagerBl().listAllTasksForFacility(facility.getId());
			for (Task task : tasks) {
				perunBl.getTasksManagerBl().clearByTask(task.getId());
				perunBl.getTasksManagerBl().removeTask(task.getId());
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
				log.warn("When trying to unsetRole FacilityAdmin for group {} in the facility {} the exception was thrown {}", adminGroup, facility, e);
				//skip and log as warning
			}
		}

		List<User> adminUsers = getFacilitiesManagerImpl().getAdmins(sess, facility);

		for (User adminUser : adminUsers) {
			try {
				AuthzResolverBlImpl.unsetRole(sess, adminUser, facility, Role.FACILITYADMIN);
			} catch (UserNotAdminException e) {
				log.warn("When trying to unsetRole FacilityAdmin for user {} in the facility {} the exception was thrown {}", adminUser, facility, e);
				//skip and log as warning
			}
		}

		//remove hosts
		List<Host> hosts = this.getHosts(sess, facility);
		for (Host host: hosts) {
			this.removeHost(sess, host);
		}

		//remove destinations
		getPerunBl().getServicesManagerBl().removeAllDestinations(sess, facility);

		// remove assigned security teams
		List<SecurityTeam> teams = getAssignedSecurityTeams(sess, facility);
		for (SecurityTeam team : teams) {
			removeSecurityTeam(sess, facility, team);
		}

		// remove assigned facility contacts
		List<ContactGroup> contacts = getFacilityContactGroups(sess, facility);
		if (contacts != null && !contacts.isEmpty()) {
			removeFacilityContacts(sess, contacts);
		}

		// remove associated attributes
		try {
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, facility);
		} catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
			throw new InternalErrorException(e);
		}

		//Remove all facility bans
		List<BanOnFacility> bansOnFacility = this.getBansForFacility(sess, facility.getId());
		for(BanOnFacility banOnFacility : bansOnFacility) {
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
	public Facility updateFacility(PerunSession sess, Facility facility) throws InternalErrorException, FacilityExistsException {
		//check facility name, it can contain only a-zA-Z.0-9_-
		if (!facility.getName().matches("^[ a-zA-Z.0-9_-]+$")) {
			throw new IllegalArgumentException("Wrong facility name, facility name can contain only a-Z0-9.-_ and space characters");
		}

		getPerunBl().getAuditer().log(sess, new FacilityUpdated(facility));
		return getFacilitiesManagerImpl().updateFacility(sess, facility);
	}

	@Override
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

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, Group group) throws InternalErrorException {
		// Get all assigned resources for the group and the derive the facilities
		List<Resource> assignedResources = perunBl.getResourcesManagerBl().getAssignedResources(sess, group);

		List<Facility> assignedFacilities = new ArrayList<>();
		for (Resource resource: assignedResources) {
			assignedFacilities.add(perunBl.getResourcesManagerBl().getFacility(sess, resource));
		}

		return assignedFacilities;
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, Member member) throws InternalErrorException {
		Set<Facility> assignedFacilities = new HashSet<>();

		for (Resource resource : getPerunBl().getResourcesManagerBl().getAssignedResources(sess, member)) {
			assignedFacilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resource));
		}

		return new ArrayList<>(assignedFacilities);
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, User user) throws InternalErrorException {
		List<Member> members = perunBl.getMembersManagerBl().getMembersByUser(sess, user);

		Set<Facility> assignedFacilities = new HashSet<>();
		for (Member member: members) {
			assignedFacilities.addAll(this.getAssignedFacilities(sess, member));
		}

		return new ArrayList<>(assignedFacilities);
	}

	@Override
	public List<Facility> getAllowedFacilities(PerunSession sess, User user) throws InternalErrorException {
		return getFacilitiesManagerImpl().getAllowedFacilities(sess, user);
	}

	@Override
	public List<Facility> getAllowedFacilities(PerunSession sess, Member member) throws InternalErrorException {
		return getFacilitiesManagerImpl().getAllowedFacilities(sess, member);
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, Service service) throws InternalErrorException {
		List<Resource> resources = perunBl.getServicesManagerBl().getAssignedResources(sess, service);

		Set<Facility> assignedFacilities = new HashSet<>();
		for (Resource resource: resources) {
			assignedFacilities.add(perunBl.getResourcesManagerBl().getFacility(sess, resource));
		}

		return new ArrayList<>(assignedFacilities);
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException {
		return getFacilitiesManagerImpl().getAssignedFacilities(sess, securityTeam);
	}


	@Override
	public List<Facility> getFacilitiesByAttribute(PerunSession sess, String attributeName, String attributeValue) throws InternalErrorException, WrongAttributeAssignmentException {
		try {
			AttributeDefinition attributeDef = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
			if (Utils.isLargeAttribute(sess, attributeDef)) {
				throw new InternalErrorException("Attribute " + attributeName + " is large attribute, which is not supported.");
			}
			Attribute attribute = new Attribute(attributeDef);
			attribute.setValue(BeansUtils.stringToAttributeValue(attributeValue, attribute.getType()));
			getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_FACILITY_ATTR);
			if (!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) || getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute)))
				throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
			return getFacilitiesManagerImpl().getFacilitiesByAttribute(sess, attribute);
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("Attribute name:'" + attributeName + "', value:'" + attributeValue + "' not exists ", e);
		}
	}

	@Override
	public void checkFacilityExists(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException {
		getFacilitiesManagerImpl().checkFacilityExists(sess, facility);
	}

	@Override
	public void checkHostExists(PerunSession sess, Host host) throws InternalErrorException, HostNotExistsException {
		getFacilitiesManagerImpl().checkHostExists(sess, host);
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public List<Host> getHosts(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getHosts(sess, facility);
	}

	@Override
	public int getHostsCount(PerunSession sess, Facility facility) throws InternalErrorException {
		return getFacilitiesManagerImpl().getHostsCount(sess, facility);
	}

	@Override
	public List<Host> addHosts(PerunSession sess, List<Host> hosts, Facility facility) throws InternalErrorException, HostExistsException {
		//check if hosts not exist in cluster
		List<Host> alreadyAssignedHosts = getHosts(sess, facility);

		Set<String> alreadyAssignedHostnames = new HashSet<>();
		Set<String> newHostnames = new HashSet<>();
		for(Host h : alreadyAssignedHosts) alreadyAssignedHostnames.add(h.getHostname());
		for(Host h : hosts) newHostnames.add(h.getHostname());
		newHostnames.retainAll(alreadyAssignedHostnames);
		if(!newHostnames.isEmpty()) throw new HostExistsException(newHostnames.toString());

		for(Host host : hosts) {
			getFacilitiesManagerImpl().addHost(sess, host, facility);
		}
		getPerunBl().getAuditer().log(sess, new HostsAddedToFacility(hosts, facility));

		return hosts;
	}

	@Override
	public List<Host> addHosts(PerunSession sess, Facility facility, List<String> hosts) throws InternalErrorException, HostExistsException, WrongPatternException {
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
	public void removeHosts(PerunSession sess, List<Host> hosts, Facility facility) throws InternalErrorException, HostAlreadyRemovedException {
		for(Host host : hosts) {
			// Remove hosts attributes
			try {
				getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, host);
			} catch (WrongAttributeValueException e) {
				throw new InternalErrorException(e);
			}

			getFacilitiesManagerImpl().removeHost(sess, host);
		}

		getPerunBl().getAuditer().log(sess, new HostsRemovedForFacility(hosts, facility));
	}

	public boolean hostExists(PerunSession sess, Host host) throws InternalErrorException {
		return getFacilitiesManagerImpl().hostExists(sess, host);
	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Facility facility, boolean onlyDirectAdmins) throws InternalErrorException {
		if(onlyDirectAdmins) {
			return getFacilitiesManagerImpl().getDirectAdmins(perunSession, facility);
		} else {
			return getFacilitiesManagerImpl().getAdmins(perunSession, facility);
		}
	}

	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Facility facility, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, facility, onlyDirectAdmins);
		List<RichUser> richUsers;

		if(allUserAttributes) {
			richUsers = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
		} else {
			try {
				richUsers = getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("One of Attribute not exist.", ex);
			}
		}

		return richUsers;
	}

	@Override
	@Deprecated
	public List<User> getAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		return facilitiesManagerImpl.getAdmins(sess, facility);
	}

	@Deprecated
	@Override
	public List<User> getDirectAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		return facilitiesManagerImpl.getDirectAdmins(sess, facility);
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Facility facility) throws InternalErrorException {
		return facilitiesManagerImpl.getAdminGroups(sess, facility);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		return getPerunBl().getUsersManagerBl().convertUsersToRichUsers(sess, this.getAdmins(sess, facility));
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdmins(PerunSession sess, Facility facility) throws InternalErrorException {
		return getPerunBl().getUsersManagerBl().convertUsersToRichUsers(sess, this.getDirectAdmins(sess, facility));
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException {
		return getPerunBl().getUsersManagerBl().convertRichUsersToRichUsersWithAttributes(sess, this.getRichAdmins(sess, facility));
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, getRichAdmins(perunSession, facility), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, getDirectRichAdmins(perunSession, facility), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Override
	public List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException {
		return  facilitiesManagerImpl.getFacilitiesWhereUserIsAdmin(sess, user);
	}

	@Override
	public Host addHost(PerunSession sess, Host host, Facility facility) throws InternalErrorException {
		getPerunBl().getAuditer().log(sess, new HostAddedToFacility(host, facility));
		return facilitiesManagerImpl.addHost(sess, host, facility);
	}

	@Override
	public void removeHost(PerunSession sess, Host host) throws InternalErrorException, HostAlreadyRemovedException {
		try {
			perunBl.getAttributesManagerBl().removeAllAttributes(sess, host);
		} catch (WrongAttributeValueException e) {
			throw new InternalErrorException(e);
		}
		facilitiesManagerImpl.removeHost(sess, host);
		getPerunBl().getAuditer().log(sess, new HostRemovedForFacility(host));
	}

	@Override
	public Host getHostById(PerunSession sess, int id) throws HostNotExistsException, InternalErrorException {
		return facilitiesManagerImpl.getHostById(sess, id);
	}

	@Override
	public List<Host> getHostsByHostname(PerunSession sess, String hostname) throws InternalErrorException {
		return facilitiesManagerImpl.getHostsByHostname(sess, hostname);
	}

	@Override
	public Facility getFacilityForHost(PerunSession sess, Host host) throws InternalErrorException {
		return facilitiesManagerImpl.getFacilityForHost(sess, host);
	}

	@Override
	public List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname) throws InternalErrorException {
		return facilitiesManagerImpl.getFacilitiesByHostName(sess, hostname);
	}

	@Override
	public List<Facility> getFacilitiesByPerunBean(PerunSession sess, Group group) throws InternalErrorException {
		List<Facility> facilities = new ArrayList<>();
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		for(Resource resourceElemenet: resources) {
			facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElemenet));
		}
		facilities = new ArrayList<>(new HashSet<>(facilities));
		return facilities;
	}

	@Override
	public List<Facility> getFacilitiesByPerunBean(PerunSession sess, Member member) throws InternalErrorException {
		List<Facility> facilities = new ArrayList<>();
		List<Group> groupsForMember = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
		List<Resource> resourcesFromMember = new ArrayList<>();
		for(Group groupElement: groupsForMember) {
			resourcesFromMember.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, groupElement));
		}
		for(Resource resourceElement: resourcesFromMember) {
			facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElement));
		}
		facilities = new ArrayList<>(new HashSet<>(facilities));
		return facilities;
	}

	@Override
	public List<Facility> getFacilitiesByPerunBean(PerunSession sess, Resource resource) throws InternalErrorException {
		return Collections.singletonList(getPerunBl().getResourcesManagerBl().getFacility(sess, resource));
	}

	@Override
	public List<Facility> getFacilitiesByPerunBean(PerunSession sess, User user) throws InternalErrorException {
		List<Facility> facilities = new ArrayList<>();
		List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		List<Resource> resourcesFromMembers = new ArrayList<>();
		for(Member memberElement: membersFromUser) {
			resourcesFromMembers.addAll(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, memberElement));
		}
		for(Resource resourceElement: resourcesFromMembers) {
			facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElement));
		}
		facilities = new ArrayList<>(new HashSet<>(facilities));
		return facilities;
	}

	@Override
	public List<Facility> getFacilitiesByPerunBean(PerunSession sess, Host host) throws InternalErrorException {
		return Collections.singletonList(getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host));
	}

	@Override
	public List<Facility> getFacilitiesByPerunBean(PerunSession sess, Vo vo) throws InternalErrorException {
		List<Facility> facilities = new ArrayList<>();
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
		for(Resource resourceElemenet: resources) {
			facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElemenet));
		}

		facilities = new ArrayList<>(new HashSet<>(facilities));
		return facilities;
	}

	@Override
	public List<User> getAssignedUsers(PerunSession sess, Facility facility) throws InternalErrorException{
		return this.getFacilitiesManagerImpl().getAssignedUsers(sess, facility);
	}
	@Override
	public List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service) throws InternalErrorException{
		return this.getFacilitiesManagerImpl().getAssignedUsers(sess, facility,service);
	}

	@Override
	public void copyManagers(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException {
		for (User admin: getDirectAdmins(sess, sourceFacility)) {
			try {
				AuthzResolverBlImpl.setRole(sess, admin, destinationFacility, Role.FACILITYADMIN);
			} catch (AlreadyAdminException ex) {
				// we can ignore the exception in this particular case, user can be admin in both of the facilities
			}
		}

		for (Group adminGroup: getAdminGroups(sess, sourceFacility)) {
			try {
				AuthzResolverBlImpl.setRole(sess, adminGroup, destinationFacility, Role.FACILITYADMIN);
			} catch (AlreadyAdminException ex) {
				// we can ignore the exception in this particular case, group can be admin in both of the facilities
			}
		}
	}

	@Override
	public void copyAttributes(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> sourceAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, sourceFacility);
		List<Attribute> destinationAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, destinationFacility);

		// do not get virtual attributes from source facility, they can't be set to destination
		sourceAttributes.removeIf(attribute -> attribute.getNamespace().startsWith(AttributesManager.NS_FACILITY_ATTR_VIRT));

		// create intersection of destination and source attributes
		List<Attribute> intersection = new ArrayList<>(destinationAttributes);
		intersection.retainAll(sourceAttributes);

		// delete all common attributes from destination facility
		getPerunBl().getAttributesManagerBl().removeAttributes(sess, destinationFacility, intersection);
		// add all attributes from source facility to destination facility
		getPerunBl().getAttributesManagerBl().setAttributes(sess, destinationFacility, sourceAttributes);
	}

	// FACILITY CONTACTS METHODS

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Owner owner) throws InternalErrorException {
		//no users there, no need to set attributes for them
		return this.getFacilitiesManagerImpl().getFacilityContactGroups(sess, owner);
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, User user) throws InternalErrorException {
		//need to get richUsers with attributes
		List<AttributeDefinition> mandatoryAttributes = this.getListOfMandatoryAttributes(sess);
		List<ContactGroup> cgs = this.getFacilitiesManagerImpl().getFacilityContactGroups(sess, user);
		return this.setAttributesForRichUsersInContactGroups(sess, cgs, mandatoryAttributes);
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Group group) throws InternalErrorException {
		//no users there, no need to set attributes for them
		return this.getFacilitiesManagerImpl().getFacilityContactGroups(sess, group);
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Facility facility) throws InternalErrorException {
		//need to get richUsers with attributes
		List<AttributeDefinition> mandatoryAttributes = this.getListOfMandatoryAttributes(sess);
		List<ContactGroup> cgs = this.getFacilitiesManagerImpl().getFacilityContactGroups(sess, facility);
		return this.setAttributesForRichUsersInContactGroups(sess, cgs, mandatoryAttributes);
	}

	@Override
	public ContactGroup getFacilityContactGroup(PerunSession sess, Facility facility, String name) throws InternalErrorException, FacilityContactNotExistsException {
		//need to get richUsers with attributes
		List<AttributeDefinition> mandatoryAttributes = this.getListOfMandatoryAttributes(sess);
		ContactGroup cg = this.getFacilitiesManagerImpl().getFacilityContactGroup(sess, facility, name);
		return this.setAttributesForRichUsersInContactGroup(sess, cg, mandatoryAttributes);
	}

	@Override
	public List<String> getAllContactGroupNames(PerunSession sess) throws InternalErrorException {
		return this.getFacilitiesManagerImpl().getAllContactGroupNames(sess);
	}

	@Override
	public void addFacilityContacts(PerunSession sess, List<ContactGroup> contactGroupsToAdd) throws InternalErrorException {
		if(contactGroupsToAdd != null) {
			for(ContactGroup cg: contactGroupsToAdd) {
				this.addFacilityContact(sess, cg);
			}
		}
	}

	@Override
	public void addFacilityContact(PerunSession sess, ContactGroup contactGroupToAdd) throws InternalErrorException {
		if(contactGroupToAdd != null) {
			if(contactGroupToAdd.getUsers() != null) {
				List<Integer> usersId = new ArrayList<>();
				for(RichUser user: contactGroupToAdd.getUsers()) {
					usersId.add(user.getId());
					this.facilitiesManagerImpl.addFacilityContact(sess, contactGroupToAdd.getFacility(), contactGroupToAdd.getName(), user);
				}
				sess.getPerun().getAuditer().log(sess, new UsersAddedToContactGroupOfFacility(usersId, contactGroupToAdd));
			}

			if(contactGroupToAdd.getGroups()!= null) {
				List<Integer> groupsId = new ArrayList<>();
				for(Group group: contactGroupToAdd.getGroups()) {
					groupsId.add(group.getId());
					this.facilitiesManagerImpl.addFacilityContact(sess, contactGroupToAdd.getFacility(), contactGroupToAdd.getName(), group);
				}
				sess.getPerun().getAuditer().log(sess, new GroupsAddedToContactGroupOfFacility(groupsId, contactGroupToAdd));
			}

			if(contactGroupToAdd.getOwners() != null) {
				List<Integer> ownersId = new ArrayList<>();
				for(Owner owner: contactGroupToAdd.getOwners()) {
					ownersId.add(owner.getId());
					this.facilitiesManagerImpl.addFacilityContact(sess, contactGroupToAdd.getFacility(), contactGroupToAdd.getName(), owner);
				}
				sess.getPerun().getAuditer().log(sess, new OwnersAddedToContactGroupOfFacility(ownersId, contactGroupToAdd));
			}
		}
	}

	@Override
	public void removeAllOwnerContacts(PerunSession sess, Owner owner) throws InternalErrorException {
		List<ContactGroup> contactGroups = getFacilityContactGroups(sess, owner);
		this.facilitiesManagerImpl.removeAllOwnerContacts(sess, owner);

		for (ContactGroup contactGroup : contactGroups) {
			sess.getPerun().getAuditer().log(sess, new OwnerContactsRemovedForFacility(owner,contactGroup));
		}
	}

	@Override
	public void removeAllUserContacts(PerunSession sess, User user) throws InternalErrorException {
		List<ContactGroup> contactGroups = getFacilityContactGroups(sess, user);
		this.facilitiesManagerImpl.removeAllUserContacts(sess, user);

		for (ContactGroup contactGroup : contactGroups) {
			sess.getPerun().getAuditer().log(sess, new UserContactsRemovedForFacility(user,contactGroup));
		}
	}

	@Override
	public void removeAllGroupContacts(PerunSession sess, Group group) throws InternalErrorException {
		List<ContactGroup> contactGroups = getFacilityContactGroups(sess, group);
		this.facilitiesManagerImpl.removeAllGroupContacts(sess, group);

		for (ContactGroup contactGroup : contactGroups) {
			sess.getPerun().getAuditer().log(sess, new GroupContactsRemovedForFacility(group, contactGroup));
		}
	}

	@Override
	public void removeFacilityContacts(PerunSession sess, List<ContactGroup> contactGroupsToRemove) throws InternalErrorException {
		if(contactGroupsToRemove != null) {
			for(ContactGroup cg: contactGroupsToRemove) {
				this.removeFacilityContact(sess, cg);
			}
		}
	}

	@Override
	public void removeFacilityContact(PerunSession sess, ContactGroup contactGroupToRemove) throws InternalErrorException {
		if(contactGroupToRemove != null) {
			if(contactGroupToRemove.getUsers() != null) {
				List<Integer> usersId = new ArrayList<>();
				for(RichUser user: contactGroupToRemove.getUsers()) {
					usersId.add(user.getId());
					this.facilitiesManagerImpl.removeFacilityContact(sess, contactGroupToRemove.getFacility(), contactGroupToRemove.getName(), user);
				}
				sess.getPerun().getAuditer().log(sess, new UsersRemovedFromContactGroupOfFacility(usersId, contactGroupToRemove));
			}

			if(contactGroupToRemove.getGroups()!= null) {
				List<Integer> groupsId = new ArrayList<>();
				for(Group group: contactGroupToRemove.getGroups()) {
					groupsId.add(group.getId());
					this.facilitiesManagerImpl.removeFacilityContact(sess, contactGroupToRemove.getFacility(), contactGroupToRemove.getName(), group);
				}
				sess.getPerun().getAuditer().log(sess, new GroupsRemovedFromContactGroupOfFacility(groupsId, contactGroupToRemove));
			}

			if(contactGroupToRemove.getOwners() != null) {
				List<Integer> ownersId = new ArrayList<>();
				for(Owner owner: contactGroupToRemove.getOwners()) {
					ownersId.add(owner.getId());
					this.facilitiesManagerImpl.removeFacilityContact(sess, contactGroupToRemove.getFacility(), contactGroupToRemove.getName(), owner);
				}
				sess.getPerun().getAuditer().log(sess, new OwnersRemovedFromContactGroupOfFacility(ownersId, contactGroupToRemove));
			}
		}
	}

	@Override
	public void checkFacilityContactExists(PerunSession sess, Facility facility, String name, User user) throws InternalErrorException, FacilityContactNotExistsException {
		this.getFacilitiesManagerImpl().checkFacilityContactExists(sess, facility, name, user);
	}

	@Override
	public List<SecurityTeam> getAssignedSecurityTeams(PerunSession sess, Facility facility) throws InternalErrorException {
		return facilitiesManagerImpl.getAssignedSecurityTeams(sess, facility);
	}

	@Override
	public void assignSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException {
		facilitiesManagerImpl.assignSecurityTeam(sess, facility, securityTeam);
		getPerunBl().getAuditer().log(sess, new SecurityTeamAssignedToFacility(securityTeam, facility));
	}

	@Override
	public void removeSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException {
		facilitiesManagerImpl.removeSecurityTeam(sess, facility, securityTeam);
		getPerunBl().getAuditer().log(sess, new SecurityTeamRemovedFromFacility(securityTeam, facility));
	}

	@Override
	public void checkFacilityContactExists(PerunSession sess, Facility facility, String name, Group group) throws InternalErrorException, FacilityContactNotExistsException {
		this.getFacilitiesManagerImpl().checkFacilityContactExists(sess, facility, name, group);
	}

	@Override
	public void checkFacilityContactExists(PerunSession sess, Facility facility, String name, Owner owner) throws InternalErrorException, FacilityContactNotExistsException {
		this.getFacilitiesManagerImpl().checkFacilityContactExists(sess, facility, name, owner);
	}

	@Override
	public void checkSecurityTeamNotAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws SecurityTeamAlreadyAssignedException, InternalErrorException {
		getFacilitiesManagerImpl().checkSecurityTeamNotAssigned(sess, facility, securityTeam);
	}

	@Override
	public void checkSecurityTeamAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws SecurityTeamNotAssignedException, InternalErrorException {
		getFacilitiesManagerImpl().checkSecurityTeamAssigned(sess, facility, securityTeam);
	}

	@Override
	public BanOnFacility setBan(PerunSession sess, BanOnFacility banOnFacility) throws InternalErrorException, BanAlreadyExistsException {
		if(this.banExists(sess, banOnFacility.getUserId(), banOnFacility.getFacilityId())) throw new BanAlreadyExistsException(banOnFacility);
		banOnFacility = getFacilitiesManagerImpl().setBan(sess, banOnFacility);
		getPerunBl().getAuditer().log(sess, new BanSetForFacility(banOnFacility, banOnFacility.getUserId(), banOnFacility.getFacilityId()));
		return banOnFacility;
	}

	@Override
	public BanOnFacility getBanById(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException {
		return getFacilitiesManagerImpl().getBanById(sess, banId);
	}

	@Override
	public boolean banExists(PerunSession sess, int userId, int facilityId) throws InternalErrorException {
		return getFacilitiesManagerImpl().banExists(sess, userId, facilityId);
	}

	@Override
	public boolean banExists(PerunSession sess, int banId) throws InternalErrorException {
		return getFacilitiesManagerImpl().banExists(sess, banId);
	}

	@Override
	public void checkBanExists(PerunSession sess, int userId, int facilityId) throws InternalErrorException, BanNotExistsException {
		if(!banExists(sess, userId, facilityId)) throw new BanNotExistsException("Ban for user " + userId + " and facility " + facilityId + " not exists!");
	}

	@Override
	public void checkBanExists(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException {
		if(!banExists(sess, banId)) throw new BanNotExistsException("Ban with id " + banId + " not exists!");
	}

	@Override
	public BanOnFacility getBan(PerunSession sess, int userId, int faclityId) throws InternalErrorException, BanNotExistsException {
		return getFacilitiesManagerImpl().getBan(sess, userId, faclityId);
	}

	@Override
	public List<BanOnFacility> getBansForUser(PerunSession sess, int userId) throws InternalErrorException {
		return getFacilitiesManagerImpl().getBansForUser(sess, userId);
	}

	@Override
	public List<BanOnFacility> getBansForFacility(PerunSession sess, int facilityId) throws InternalErrorException {
		return getFacilitiesManagerImpl().getBansForFacility(sess, facilityId);
	}

	@Override
	public List<BanOnFacility> getAllExpiredBansOnFacilities(PerunSession sess) throws InternalErrorException {
		return getFacilitiesManagerImpl().getAllExpiredBansOnFacilities(sess);
	}

	@Override
	public BanOnFacility updateBan(PerunSession sess, BanOnFacility banOnFacility) throws InternalErrorException {
		banOnFacility = getFacilitiesManagerImpl().updateBan(sess, banOnFacility);
		getPerunBl().getAuditer().log(sess, new BanUpdatedForFacility(banOnFacility, banOnFacility.getUserId(), banOnFacility.getFacilityId()));
		return banOnFacility;
	}

	@Override
	public void removeBan(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException {
		BanOnFacility ban = this.getBanById(sess, banId);
		getFacilitiesManagerImpl().removeBan(sess, banId);
		getPerunBl().getAuditer().log(sess, new BanRemovedForFacility(ban, ban.getUserId(), ban.getFacilityId()));
	}

	@Override
	public void removeBan(PerunSession sess, int userId, int facilityId) throws InternalErrorException, BanNotExistsException {
		BanOnFacility ban = this.getBan(sess, userId, facilityId);
		getFacilitiesManagerImpl().removeBan(sess, userId, facilityId);
		getPerunBl().getAuditer().log(sess, new BanRemovedForFacility(ban, userId, facilityId));
	}

	@Override
	public void removeAllExpiredBansOnFacilities(PerunSession sess) throws InternalErrorException {
		List<BanOnFacility> expiredBans = this.getAllExpiredBansOnFacilities(sess);
		for(BanOnFacility expiredBan: expiredBans) {
			try {
				this.removeBan(sess, expiredBan.getId());
			} catch (BanNotExistsException ex) {
				log.warn("Ban {} can't be removed because it not exists yet.",expiredBan);
				//Skipt this, probably already removed
			}
		}
	}

	/**
	 * Change all richUsers in contactGroup to richUsersWithAttributes.
	 *
	 * @param sess
	 * @param contactGroup
	 * @param attributesToSet
	 * @return contactGroup with richUsers with attributes (if there is any contact, user or attribute to set)
	 * @throws InternalErrorException
	 */
	private ContactGroup setAttributesForRichUsersInContactGroup(PerunSession sess, ContactGroup contactGroup, List<AttributeDefinition> attributesToSet) throws InternalErrorException {
		if(contactGroup == null) return null;
		if(contactGroup.getUsers() == null || contactGroup.getUsers().isEmpty()) return contactGroup;
		if(attributesToSet == null || attributesToSet.isEmpty()) return contactGroup;

		List<RichUser> richUsers = contactGroup.getUsers();
		richUsers = getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(sess, richUsers, attributesToSet);
		contactGroup.setUsers(richUsers);
		return contactGroup;
	}

	/**
	 * Change all richUsers in all contactGroups to richUsersWithAttributes.
	 *
	 * @param sess
	 * @param contactGroups
	 * @param attributesToSet
	 * @return list of modified contactGroups with richUsers with attributes (if there is any not null contact, user for contact or attribute to set)
	 * @throws InternalErrorException
	 */
	private List<ContactGroup> setAttributesForRichUsersInContactGroups(PerunSession sess, List<ContactGroup> contactGroups, List<AttributeDefinition> attributesToSet) throws InternalErrorException {
		if(contactGroups == null || contactGroups.isEmpty()) return contactGroups;
		if(attributesToSet == null || attributesToSet.isEmpty()) return contactGroups;

		for(ContactGroup cg: contactGroups) {
			setAttributesForRichUsersInContactGroup(sess, cg, attributesToSet);
		}

		return contactGroups;
	}

	/**
	 * Create list of attribute definitions from list of attribute names.
	 * List is defined like a constant with name 'MANDATORY_ATTRIBUTES_FOR_USER_IN_CONTACT'
	 * These attributes will be returned like attribute definitions
	 *
	 * @param sess
	 * @return list of attribute definitions from attrNames in constant
	 * @throws InternalErrorException
	 */
	private List<AttributeDefinition> getListOfMandatoryAttributes(PerunSession sess) throws InternalErrorException {
		List<AttributeDefinition> mandatoryAttrs = new ArrayList<>();
		for(String attrName: MANDATORY_ATTRIBUTES_FOR_USER_IN_CONTACT) {
			try {
				mandatoryAttrs.add(perunBl.getAttributesManagerBl().getAttributeDefinition(sess, attrName));
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("Some of mandatory attributes for users in facility contacts not exists.",ex);
			}
		}
		return mandatoryAttrs;
	}
}
