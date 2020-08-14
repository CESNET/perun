package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.EnrichedHost;
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
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityContactNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
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
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.FacilitiesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.FacilitiesManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
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
		throw new InternalErrorException("Unsupported method!");
	}

	@Override
	public Facility getFacilityById(PerunSession sess, int id) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Facility facility = getFacilitiesManagerBl().getFacilityById(sess, id);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getFacilityById_int_policy", facility)) {
			throw new PrivilegeException(sess, "getFacilityById");
				}

		return facility;
	}

	@Override
	public Facility getFacilityByName(PerunSession sess, String name) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(name, "name");

		Facility facility = getFacilitiesManagerBl().getFacilityByName(sess, name);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getFacilityByName_String_policy", facility)) {
			throw new PrivilegeException(sess, "getFacilityByName");
				}

		return facility;
	}

	@Override
	public List<RichFacility> getRichFacilities(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichFacilities_policy")) {
			throw new PrivilegeException(sess, "getRichFacilities");
		}
		List<Facility> facilities = getFacilitiesManagerBl().getFacilities(sess);
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getRichFacilities_policy", facility));

		return getFacilitiesManagerBl().getRichFacilities(sess, facilities);
	}

	@Override
	public List<Facility> getFacilitiesByDestination(PerunSession sess, String destination) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(destination, "destination");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getFacilitiesByDestination_String_policy")) {
			throw new PrivilegeException(sess, "getFacilitiesByDestination");
		}
		List<Facility> facilities = getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination);
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getFacilitiesByDestination_String_policy", facility));

		return facilities;
	}

	@Override
	public List<Facility> getFacilitiesByAttribute(PerunSession sess, String attributeName, String attributeValue)
			throws PrivilegeException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getFacilitiesByAttribute_String_String_policy")) {
			throw new PrivilegeException(sess, "getFacilitiesByAttribute");
		}

		// Get attribute definition and throw exception if the attribute does not exists
		AttributeDefinition attributeDef = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);

		List<Facility> facilities = getFacilitiesManagerBl().getFacilitiesByAttribute(sess, attributeName, attributeValue);

		// Filter attributes, which user can read
		Iterator<Facility> it = facilities.iterator();
		while (it.hasNext()) {
			Facility facility = it.next();
			if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attributeDef, facility)) {
				it.remove();
			}
		}

		return facilities;
	}

	@Override
	public int getFacilitiesCount(PerunSession sess) {
		Utils.checkPerunSession(sess);

		return getFacilitiesManagerBl().getFacilitiesCount(sess);
	}

	@Override
	public List<Facility> getFacilities(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getFacilities_policy")) {
			throw new PrivilegeException(sess, "getFacilities");
		}
		List<Facility> facilities = getFacilitiesManagerBl().getFacilities(sess);
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getFacilities_policy", facility));

		return facilities;
	}

	@Override
	public List<Owner> getOwners(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getOwners_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getOwners");
				}

		return getFacilitiesManagerBl().getOwners(sess, facility);
	}

	@Override
	public void setOwners(PerunSession sess, Facility facility, List<Owner> owners) throws PrivilegeException, FacilityNotExistsException, OwnerNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		Utils.notNull(owners, "owners");
		for (Owner owner: owners) {
			getPerunBl().getOwnersManagerBl().checkOwnerExists(sess, owner);
		}

		// Authorization
		List<PerunBean> beans = new ArrayList<>(owners);
		beans.add(facility);
		if (!AuthzResolver.authorizedInternal(sess, "setOwners_Facility_List<Owner>_policy", beans)) {
			throw new PrivilegeException(sess, "setOwners");
		}

		getFacilitiesManagerBl().setOwners(sess, facility, owners);
	}

	@Override
	public void addOwner(PerunSession sess, Facility facility, Owner owner) throws PrivilegeException, OwnerNotExistsException, FacilityNotExistsException, OwnerAlreadyAssignedException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getOwnersManagerBl().checkOwnerExists(sess, owner);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addOwner_Facility_Owner_policy", Arrays.asList(facility, owner))) {
			throw new PrivilegeException(sess, "addOwner");
		}

		getFacilitiesManagerBl().addOwner(sess, facility, owner);
	}

	@Override
	public void removeOwner(PerunSession sess, Facility facility, Owner owner) throws PrivilegeException, OwnerNotExistsException, FacilityNotExistsException, OwnerAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getOwnersManagerBl().checkOwnerExists(sess, owner);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeOwner_Facility_Owner_policy", Arrays.asList(facility, owner))) {
			throw new PrivilegeException(sess, "removeOwner");
		}

		getFacilitiesManagerBl().removeOwner(sess, facility, owner);
	}

	@Override
	public void copyOwners(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, sourceFacility);
		getFacilitiesManagerBl().checkFacilityExists(sess, destinationFacility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "copyOwners_Facility_Facility_policy", Arrays.asList(sourceFacility, destinationFacility))) {
			throw new PrivilegeException(sess, "copyOwners");
		}

		getFacilitiesManagerBl().copyOwners(sess, sourceFacility, destinationFacility);
	}

	@Override
	public List<Vo> getAllowedVos(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllowedVos_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getAllowedVos");
		}

		return getFacilitiesManagerBl().getAllowedVos(sess, facility);

	}

	@Override
	public List<Group> getAllowedGroups(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(perunSession);

		List<PerunBean> beans = createListOfBeans(perunSession, facility, specificVo, specificService);

		//Authrorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getAllowedGroups_Facility_Vo_Service_policy", beans)) {
			throw new PrivilegeException(perunSession, "getGroupsWhereUserIsActive");
		}

		return getFacilitiesManagerBl().getAllowedGroups(perunSession, facility, specificVo, specificService);
	}

	@Override
	public List<RichGroup> getAllowedRichGroupsWithAttributes(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService, List<String> attrNames) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(perunSession);

		List<PerunBean> beans = createListOfBeans(perunSession, facility, specificVo, specificService);

		//Authrorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getAllowedRichGroupsWithAttributes_Facility_Vo_Service_List<String>_policy", beans)) {
			throw new PrivilegeException(perunSession, "getAllowedRichGroupsWithAttributes");
		}

		List<RichGroup> richGroups = getFacilitiesManagerBl().getAllowedRichGroupsWithAttributes(perunSession, facility, specificVo, specificService, attrNames);
		return getPerunBl().getGroupsManagerBl().filterOnlyAllowedAttributes(perunSession, richGroups, null, true);

	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllowedUsers_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getAllowedUsers");
		}

		return getFacilitiesManagerBl().getAllowedUsers(sess, facility);
	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Facility facility, Vo specificVo, Service specificService) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		List<PerunBean> beans = createListOfBeans(sess, facility, specificVo, specificService);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllowedUsers_Facility_Vo_Service_policy", beans)) {
			throw new PrivilegeException(sess, "getAllowedUsers");
		}

		return getFacilitiesManagerBl().getAllowedUsers(sess, facility, specificVo, specificService);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedResources_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getAssignedResources");
		}

		return getFacilitiesManagerBl().getAssignedResources(sess, facility);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedRichResources_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getAssignedRichResources");
		}

		return getFacilitiesManagerBl().getAssignedRichResources(sess, facility);

	}

	@Override
	public Facility createFacility(PerunSession sess, Facility facility) throws PrivilegeException, FacilityExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createFacility_Facility_policy")) {
			throw new PrivilegeException(sess, "createFacility");
		}

		return getFacilitiesManagerBl().createFacility(sess, facility);
	}

	@Override
	public void deleteFacility(PerunSession sess, Facility facility, Boolean force) throws RelationExistsException, FacilityNotExistsException, PrivilegeException, FacilityAlreadyRemovedException, HostAlreadyRemovedException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteFacility_Facility_Boolean_policy", facility)) {
			throw new PrivilegeException(sess, "deleteFacility");
		}

		getFacilitiesManagerBl().deleteFacility(sess, facility, force);
	}

	@Override
	public Facility updateFacility(PerunSession sess, Facility facility) throws FacilityNotExistsException, FacilityExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		Utils.notNull(facility, "facility");
		Utils.notNull(facility.getName(), "facility.name");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "updateFacility_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "updateFacility");
		}

		return getFacilitiesManagerBl().updateFacility(sess, facility);
	}

	@Override
	public List<Facility> getOwnerFacilities(PerunSession sess, Owner owner) throws OwnerNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getOwnersManagerBl().checkOwnerExists(sess, owner);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getOwnerFacilities_Owner_policy", owner)) {
			throw new PrivilegeException(sess, "getOwnerFacilities");
		}

		return getFacilitiesManagerBl().getOwnerFacilities(sess, owner);
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Facility> facilities = getFacilitiesManagerBl().getAssignedFacilities(sess, group);

		// Authorization
		List<PerunBean> beans = new ArrayList<>(facilities);
		beans.add(group);
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedFacilities_Group_policy", beans)) {
			throw new PrivilegeException(sess, "getAssignedFacilities");
		}
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getAssignedFacilities_Group_policy", Arrays.asList(facility, group)));

		return facilities;
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		List<Facility> facilities = getFacilitiesManagerBl().getAssignedFacilities(sess, member);

		// Authorization
		List<PerunBean> beans = new ArrayList<>(facilities);
		beans.add(member);
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedFacilities_Member_policy", beans)) {
			throw new PrivilegeException(sess, "getAssignedFacilities");
		}
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getAssignedFacilities_Member_policy", Arrays.asList(facility, member)));

		return facilities;
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, User user) throws PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		List<Facility> facilities = getFacilitiesManagerBl().getAssignedFacilities(sess, user);

		// Authorization
		List<PerunBean> beans = new ArrayList<>(facilities);
		beans.add(user);
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedFacilities_User_policy", beans)) {
			throw new PrivilegeException(sess, "getAssignedFacilities");
		}
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getAssignedFacilities_User_policy", Arrays.asList(facility, user)));

		return facilities;
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, Service service) throws PrivilegeException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);

		List<Facility> facilities = getFacilitiesManagerBl().getAssignedFacilities(sess, service);

		// Authorization
		List<PerunBean> beans = new ArrayList<>(facilities);
		beans.add(service);
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedFacilities_Service_policy", beans)) {
			throw new PrivilegeException(sess, "getAssignedFacilities");
		}
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getAssignedFacilities_Service_policy", Arrays.asList(facility, service)));

		return facilities;
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, SecurityTeam securityTeam) throws PrivilegeException, SecurityTeamNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

		List<Facility> facilities = getFacilitiesManagerBl().getAssignedFacilities(sess, securityTeam);

		// Authorization
		List<PerunBean> beans = new ArrayList<>(facilities);
		beans.add(securityTeam);
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedFacilities_SecurityTeam_policy", beans)) {
			throw new PrivilegeException(sess, "getAssignedFacilities");
		}
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getAssignedFacilities_SecurityTeam_policy", Arrays.asList(securityTeam, facility)));

		return facilities;
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

	@Override
	public List<Host> getHosts(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER) &&
			!AuthzResolver.isAuthorized(sess, Role.ENGINE)) {
			throw new PrivilegeException(sess, "getHosts");
		}

		return getFacilitiesManagerBl().getHosts(sess, facility);
	}

	@Override
	public List<EnrichedHost> getEnrichedHosts(PerunSession sess, Facility facility, List<String> attrNames) throws AttributeNotExistsException, FacilityNotExistsException, PrivilegeException {
		List<Host> hosts = getHosts(sess, facility);
		List<EnrichedHost> enrichedHosts = new ArrayList<>();

		if (hosts.isEmpty()) return enrichedHosts;

		Host host1 = hosts.get(0);
		List<String> allowedAttributes = new ArrayList<>();

		//Filtering attributes
		for (String attrName : attrNames) {
			if (AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attrName), host1)) {
				allowedAttributes.add(attrName);
			}
		}

		for (Host host : hosts) {
			List<Attribute> hostAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, host, allowedAttributes);
			enrichedHosts.add(new EnrichedHost(host, hostAttributes));
		}
		return enrichedHosts;
	}

	@Override
	public int getHostsCount(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getHostsCount_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getHostsCount");
		}

		return getFacilitiesManagerBl().getHostsCount(sess, facility);
	}

	@Override
	public List<Host> addHosts(PerunSession sess, List<Host> hosts, Facility facility) throws FacilityNotExistsException, PrivilegeException, HostExistsException {
		Utils.checkPerunSession(sess);

		Utils.notNull(hosts, "hosts");
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addHosts_List<Host>_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "addHosts");
		}

		for(Host host: hosts) {

			List<Facility> facilitiesByHostname = getFacilitiesManagerBl().getFacilitiesByHostName(sess, host.getHostname());
			List<Facility> facilitiesByDestination = getFacilitiesManagerBl().getFacilitiesByDestination(sess, host.getHostname());

			if(facilitiesByHostname.isEmpty() && facilitiesByDestination.isEmpty()) {
				continue;
			}
			if(!facilitiesByHostname.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByHostname: facilitiesByHostname) {
					if(AuthzResolver.authorizedInternal(sess, "addHosts_List<Host>_Facility_policy", facilityByHostname)) {
						hasRight = true;
						break;
					}
				}
				if(hasRight) continue;
			}
			if(!facilitiesByDestination.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByDestination: facilitiesByDestination) {
					if(AuthzResolver.authorizedInternal(sess, "addHosts_List<Host>_Facility_policy", facilityByDestination)) {
						hasRight = true;
						break;
					}
				}
				if(hasRight) continue;
			}

			throw new PrivilegeException(sess, "You can't add host " + host + ", because you don't have privileges to use this hostName");
		}

		return getFacilitiesManagerBl().addHosts(sess, hosts, facility);
	}

	@Override
	public List<Host> addHosts(PerunSession sess, Facility facility, List<String> hosts) throws FacilityNotExistsException, PrivilegeException, HostExistsException, WrongPatternException {
		Utils.checkPerunSession(sess);

		Utils.notNull(hosts, "hosts");
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addHosts_Facility_List<String>_policy", facility)) {
			throw new PrivilegeException(sess, "addHosts");
		}

		List<String> allHostnames = new ArrayList<>();
		for(String host: hosts) {
			allHostnames.addAll(Utils.generateStringsByPattern(host));
		}

		for(String hostname: allHostnames) {

			List<Facility> facilitiesByHostname = getFacilitiesManagerBl().getFacilitiesByHostName(sess, hostname);
			List<Facility> facilitiesByDestination = getFacilitiesManagerBl().getFacilitiesByDestination(sess, hostname);

			if(facilitiesByHostname.isEmpty() && facilitiesByDestination.isEmpty()) {
				continue;
			}
			if(!facilitiesByHostname.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByHostname: facilitiesByHostname) {
					if(AuthzResolver.authorizedInternal(sess, "addHosts_Facility_List<String>_policy", facilityByHostname)) {
						hasRight = true;
						break;
					}
				}
				if(hasRight) continue;
			}
			if(!facilitiesByDestination.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByDestination: facilitiesByDestination) {
					if(AuthzResolver.authorizedInternal(sess, "addHosts_Facility_List<String>_policy", facilityByDestination)) {
						hasRight = true;
						break;
					}
				}
				if(hasRight) continue;
			}

			throw new PrivilegeException(sess, "You can't add host " + hostname + ", because you don't have privileges to use this hostName");
		}

		return getFacilitiesManagerBl().addHosts(sess, facility, hosts);
	}

	@Override
	public void removeHosts(PerunSession sess, List<Host> hosts, Facility facility) throws FacilityNotExistsException, PrivilegeException, HostAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		Utils.notNull(hosts, "hosts");
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		List<PerunBean> beans = new ArrayList<>(hosts);
		beans.add(facility);
		if (!AuthzResolver.authorizedInternal(sess, "removeHosts_List<Host>_Facility_policy", beans)) {
			throw new PrivilegeException(sess, "removeHosts");
		}

		getFacilitiesManagerBl().removeHosts(sess, hosts, facility);
	}

	@Override
	public void addAdmin(PerunSession sess, Facility facility, User user) throws FacilityNotExistsException, UserNotExistsException, PrivilegeException, AlreadyAdminException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addAdmin_Facility_User_policy", Arrays.asList(facility, user))) {
			throw new PrivilegeException(sess, "addAdmin");
		}

		AuthzResolverBlImpl.setRole(sess, user, facility, Role.FACILITYADMIN);
	}

	@Override
	public void addAdmin(PerunSession sess, Facility facility, Group group) throws FacilityNotExistsException, GroupNotExistsException, PrivilegeException, AlreadyAdminException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addAdmin_Facility_Group_policy", Arrays.asList(facility, group))) {
			throw new PrivilegeException(sess, "addAdmin");
		}

		AuthzResolverBlImpl.setRole(sess, group, facility, Role.FACILITYADMIN);
	}

	@Override
	public void removeAdmin(PerunSession sess, Facility facility, User user) throws FacilityNotExistsException, UserNotExistsException, PrivilegeException, UserNotAdminException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeAdmin_Facility_User_policy", Arrays.asList(facility, user))) {
			throw new PrivilegeException(sess, "deleteAdmin");
		}

		AuthzResolverBlImpl.unsetRole(sess, user, facility, Role.FACILITYADMIN);

	}

	@Override
	public void removeAdmin(PerunSession sess, Facility facility, Group group) throws FacilityNotExistsException, GroupNotExistsException, PrivilegeException, GroupNotAdminException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeAdmin_Facility_Group_policy", Arrays.asList(facility, group))) {
			throw new PrivilegeException(sess, "deleteAdmin");
		}

		AuthzResolverBlImpl.unsetRole(sess, group, facility, Role.FACILITYADMIN);

	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Facility facility, boolean onlyDirectAdmins) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(perunSession);
		getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getAdmins_Facility_boolean_policy", facility)) {
			throw new PrivilegeException(perunSession, "getAdmins");
		}

		return getFacilitiesManagerBl().getAdmins(perunSession, facility, onlyDirectAdmins);
	}


	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Facility facility, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws UserNotExistsException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(perunSession);
		getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		if(!allUserAttributes) Utils.notNull(specificAttributes, "specificAttributes");

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getRichAdmins_Facility_List<String>_boolean_boolean_policy", facility)) {
			throw new PrivilegeException(perunSession, "getRichAdmins");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getFacilitiesManagerBl().getRichAdmins(perunSession, facility, specificAttributes, allUserAttributes, onlyDirectAdmins));
	}

	@Override
	@Deprecated
	public List<User> getAdmins(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAdmins");
		}

		return getFacilitiesManagerBl().getAdmins(sess, facility);
	}

	@Deprecated
	@Override
	public List<User> getDirectAdmins(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getDirectAdmins");
		}

		return getFacilitiesManagerBl().getDirectAdmins(sess, facility);
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAdminGroups_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getAdminGroups");
		}

		return getFacilitiesManagerBl().getAdminGroups(sess, facility);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichAdmins");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getFacilitiesManagerBl().getRichAdmins(sess, facility));
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Facility facility) throws UserNotExistsException, FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichAdminsWithAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(sess, getFacilitiesManagerBl().getRichAdminsWithAttributes(sess, facility));
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(perunSession);

		getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getRichAdminsWithSpecificAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getFacilitiesManagerBl().getRichAdminsWithSpecificAttributes(perunSession, facility, specificAttributes));
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(perunSession);

		getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getDirectRichAdminsWithSpecificAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getFacilitiesManagerBl().getDirectRichAdminsWithSpecificAttributes(perunSession, facility, specificAttributes));
	}

	@Override
	public List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) throws UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getFacilitiesWhereUserIsAdmin_User_policy", user)) {
			throw new PrivilegeException(sess, "getFacilitiesWhereUserIsAdmin");
		}

		return getFacilitiesManagerBl().getFacilitiesWhereUserIsAdmin(sess, user);
	}

	@Override
	public void copyManagers(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, sourceFacility);
		getFacilitiesManagerBl().checkFacilityExists(sess, destinationFacility);

		// Authorization - facility admin of the both facilities required
		if (!AuthzResolver.authorizedInternal(sess, "copyManagers_Facility_Facility_policy", Arrays.asList(sourceFacility, destinationFacility))) {
			throw new PrivilegeException(sess, "copyManager");
		}

		getFacilitiesManagerBl().copyManagers(sess, sourceFacility, destinationFacility);
	}

	@Override
	public void copyAttributes(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws PrivilegeException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, sourceFacility);
		getFacilitiesManagerBl().checkFacilityExists(sess, destinationFacility);

		// Authorization - facility admin of the both facilities required
		if (!AuthzResolver.authorizedInternal(sess, "copyAttributes_Facility_Facility_policy", Arrays.asList(sourceFacility, destinationFacility))) {
			throw new PrivilegeException(sess, "copyAttributes");
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

	@Override
	public Host addHost(PerunSession sess, Host host, Facility facility) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Utils.notNull(host, "host");
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addHost_Host_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "addHost");
		}

		List<Facility> facilitiesByHostname = getFacilitiesManagerBl().getFacilitiesByHostName(sess, host.getHostname());
		List<Facility> facilitiesByDestination = getFacilitiesManagerBl().getFacilitiesByDestination(sess, host.getHostname());

		if(facilitiesByHostname.isEmpty() && facilitiesByDestination.isEmpty()) {
			return getFacilitiesManagerBl().addHost(sess, host, facility);
		}
		if(!facilitiesByHostname.isEmpty()) {
			boolean hasRight = false;
			for(Facility facilityByHostname: facilitiesByHostname) {
				if(AuthzResolver.authorizedInternal(sess, "addHost_Host_Facility_policy", facilityByHostname)) {
					hasRight = true;
					break;
				}
			}
			if(hasRight) return getFacilitiesManagerBl().addHost(sess, host, facility);
		}
		if(!facilitiesByDestination.isEmpty()) {
			boolean hasRight = false;
			for(Facility facilityByDestination: facilitiesByDestination) {
				if(AuthzResolver.authorizedInternal(sess, "addHost_Host_Facility_policy", facilityByDestination)) {
					hasRight = true;
					break;
				}
			}
			if(hasRight) return getFacilitiesManagerBl().addHost(sess, host, facility);
		}

		throw new PrivilegeException(sess, "You can't add host " + host + ", because you don't have privileges to use this hostName");
	}

	@Override
	public void removeHost(PerunSession sess, Host host) throws HostNotExistsException, PrivilegeException, HostAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkHostExists(sess, host);
		Facility facility = getFacilitiesManagerBl().getFacilityForHost(sess, host);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeHost_Host_policy", Arrays.asList(facility, host))) {
			throw new PrivilegeException(sess, "removeHost");
		}

		getFacilitiesManagerBl().removeHost(sess, host);
	}

	@Override
	public void removeHostByHostname(PerunSession sess, String hostname) throws HostNotExistsException, HostAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		List<Host> hosts = getFacilitiesManagerBl().getHostsByHostname(sess, hostname);

		Iterator<Host> hostIterator = hosts.iterator();
		while (hostIterator.hasNext()) {
			Host host = hostIterator.next();
			Facility facility = getFacilitiesManagerBl().getFacilityForHost(sess, host);

			// Authorization
			if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
				hostIterator.remove();
			}
		}

		if (hosts.size() != 1) throw new HostNotExistsException("There is no unique host with this hostname: " + hostname);
		Host host = hosts.get(0);

		getFacilitiesManagerBl().removeHost(sess, host);
	}

	@Override
	public Host getHostById(PerunSession sess, int hostId) throws HostNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getHostById_int_policy")) {
			throw new PrivilegeException(sess, "getHostById");
		}

		return getFacilitiesManagerBl().getHostById(sess, hostId);
	}

	@Override
	public List<Host> getHostsByHostname(PerunSession sess, String hostname) throws PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(hostname, "hostname");

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getHostsByHostname_String_policy")) {
			throw new PrivilegeException(sess, "getHostsByHostname");
		}

		List<Host> hostsByHostname = getFacilitiesManagerBl().getHostsByHostname(sess, hostname);

		//remove hosts which has not facility from authorized facilities
		Iterator<Host> hostIterator = hostsByHostname.iterator();
		while(hostIterator.hasNext()) {
			Host host = hostIterator.next();
			Facility fac = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			if(!AuthzResolver.authorizedInternal(sess, "filter-getHostsByHostname_String_policy", Arrays.asList(host, fac))) hostIterator.remove();
		}

		return hostsByHostname;
	}

	@Override
	public Facility getFacilityForHost(PerunSession sess, Host host) throws PrivilegeException, HostNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkHostExists(sess, host);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getFacilityForHost_Host_policy", host)) {
			throw new PrivilegeException(sess, "getFacilityForHost");
		}

		return getFacilitiesManagerBl().getFacilityForHost(sess, host);
	}

	@Override
	public List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname) {
		Utils.checkPerunSession(sess);

		List<Facility> facilities = getFacilitiesManagerBl().getFacilitiesByHostName(sess, hostname);

		//Authorization
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "getFacilitiesByHostName_String_policy", facility));

		return facilities;
	}

	@Override
	public List<User> getAssignedUsers(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedUsers_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getAssignedUser");
		}

		return this.getPerunBl().getFacilitiesManagerBl().getAssignedUsers(sess,facility);
	}

	@Override
	public List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAssignedUsers_Facility_Service_policy", Arrays.asList(facility, service))) {
			throw new PrivilegeException(sess, "getAssignedUser");
		}

		return this.getPerunBl().getFacilitiesManagerBl().getAssignedUsers(sess,facility,service);
	}

	// FACILITY CONTACTS METHODS

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Owner owner) throws OwnerNotExistsException {
		Utils.checkPerunSession(sess);
		perunBl.getOwnersManagerBl().checkOwnerExists(sess, owner);
		List<ContactGroup> contactGroups = this.getFacilitiesManagerBl().getFacilityContactGroups(sess, owner);

		if(contactGroups == null) return new ArrayList<>();

		//Authorization
		contactGroups.removeIf(contactGroup -> !AuthzResolver.authorizedInternal(sess, "getFacilityContactGroups_Owner_policy", Arrays.asList(owner, contactGroup.getFacility())));


		return contactGroups;
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, User user) throws UserNotExistsException {
		Utils.checkPerunSession(sess);
		perunBl.getUsersManagerBl().checkUserExists(sess, user);
		List<ContactGroup> contactGroups = this.getFacilitiesManagerBl().getFacilityContactGroups(sess, user);

		if(contactGroups == null) return new ArrayList<>();

		//Authorization
		contactGroups.removeIf(contactGroup -> !AuthzResolver.authorizedInternal(sess, "getFacilityContactGroups_User_policy", Arrays.asList(user, contactGroup.getFacility())));

		return contactGroups;
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Group group) throws GroupNotExistsException {
		Utils.checkPerunSession(sess);
		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
		List<ContactGroup> contactGroups = this.getFacilitiesManagerBl().getFacilityContactGroups(sess, group);

		if(contactGroups == null) return new ArrayList<>();

		//Authorization
		contactGroups.removeIf(contactGroup -> !AuthzResolver.authorizedInternal(sess, "getFacilityContactGroups_Group_policy", Arrays.asList(group, contactGroup.getFacility())));

		return contactGroups;
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		perunBl.getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getFacilityContactGroups_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getFacilityContactGroups");
		}

		return this.getFacilitiesManagerBl().getFacilityContactGroups(sess, facility);
	}

	@Override
	public ContactGroup getFacilityContactGroup(PerunSession sess, Facility facility, String name) throws FacilityContactNotExistsException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(name, "name");
		this.getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getFacilityContactGroup_Facility_String_policy", facility)) {
			throw new PrivilegeException(sess, "getFacilityContactGroup");
		}

		return this.getFacilitiesManagerBl().getFacilityContactGroup(sess, facility, name);
	}

	@Override
	public List<String> getAllContactGroupNames(PerunSession sess) {
		Utils.checkPerunSession(sess);
		return this.getFacilitiesManagerBl().getAllContactGroupNames(sess);
	}

	@Override
	public void addFacilityContacts(PerunSession sess, List<ContactGroup> contactGroupsToAdd) throws FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		this.checkFacilityContactsEntitiesExist(sess, contactGroupsToAdd);

		//Authorization
		contactGroupsToAdd.removeIf(contactGroupToAdd -> !AuthzResolver.authorizedInternal(sess, "addFacilityContacts_List<ContactGroup>_policy", contactGroupToAdd.getFacility()));

		if(!contactGroupsToAdd.isEmpty()) {
			this.facilitiesManagerBl.addFacilityContacts(sess, contactGroupsToAdd);
		}
	}

	@Override
	public void addFacilityContact(PerunSession sess, ContactGroup contactGroupToAdd) throws PrivilegeException, FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		this.checkFacilityContactEntitiesExists(sess, contactGroupToAdd);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "addFacilityContact_ContactGroup_policy", contactGroupToAdd.getFacility())) {
			throw new PrivilegeException(sess, "addFacilityContact");
		}

		this.getFacilitiesManagerBl().addFacilityContact(sess, contactGroupToAdd);
	}

	@Override
	public void removeFacilityContacts(PerunSession sess, List<ContactGroup> contactGroupsToRemove) throws PrivilegeException, FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		this.checkFacilityContactsEntitiesExist(sess, contactGroupsToRemove);

		//Authorization
		for (ContactGroup contactGroupToRemove : contactGroupsToRemove) {
			if (!AuthzResolver.authorizedInternal(sess, "removeFacilityContacts_List<ContactGroup>_policy", contactGroupToRemove.getFacility())) {
				throw new PrivilegeException(sess, "removeFacilityContacts");
			}
		}

		this.getFacilitiesManagerBl().removeFacilityContacts(sess, contactGroupsToRemove);
	}

	@Override
	public void removeFacilityContact(PerunSession sess, ContactGroup contactGroupToRemove) throws PrivilegeException, FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		this.checkFacilityContactEntitiesExists(sess, contactGroupToRemove);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "removeFacilityContact_ContactGroup_policy", contactGroupToRemove.getFacility())) {
			throw new PrivilegeException(sess, "contactGroupToRemove");
		}

		this.getFacilitiesManagerBl().removeFacilityContact(sess, contactGroupToRemove);
	}

	@Override
	public List<SecurityTeam> getAssignedSecurityTeams(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getAssignedSecurityTeams_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getAssignedSecurityTeams");
		}

		return this.getFacilitiesManagerBl().getAssignedSecurityTeams(sess, facility);
	}

	@Override
	public void assignSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws PrivilegeException, SecurityTeamNotExistsException, FacilityNotExistsException, SecurityTeamAlreadyAssignedException {
		Utils.checkPerunSession(sess);
		getPerunBl().getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getFacilitiesManagerBl().checkSecurityTeamNotAssigned(sess, facility, securityTeam);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "assignSecurityTeam_Facility_SecurityTeam_policy", Arrays.asList(facility, securityTeam))) {
			throw new PrivilegeException(sess, "assignSecurityTeam");
		}

		this.getFacilitiesManagerBl().assignSecurityTeam(sess, facility, securityTeam);
	}

	@Override
	public void removeSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws PrivilegeException, FacilityNotExistsException, SecurityTeamNotExistsException, SecurityTeamNotAssignedException {
		Utils.checkPerunSession(sess);
		getPerunBl().getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getFacilitiesManagerBl().checkSecurityTeamAssigned(sess, facility, securityTeam);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "removeSecurityTeam_Facility_SecurityTeam_policy", Arrays.asList(facility, securityTeam))) {
			throw new PrivilegeException(sess, "removeSecurityTeam");
		}

		this.getFacilitiesManagerBl().removeSecurityTeam(sess, facility, securityTeam);
	}

	@Override
	public BanOnFacility setBan(PerunSession sess, BanOnFacility banOnFacility) throws PrivilegeException, BanAlreadyExistsException, FacilityNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(banOnFacility, "banOnFacility");
		User user = getPerunBl().getUsersManagerBl().getUserById(sess, banOnFacility.getUserId());
		Facility facility = this.getFacilitiesManagerBl().getFacilityById(sess, banOnFacility.getFacilityId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "setBan_BanOnFacility_policy", Arrays.asList(facility, user, banOnFacility))) {
			throw new PrivilegeException(sess, "setBan");
		}

		return getFacilitiesManagerBl().setBan(sess, banOnFacility);
	}

	@Override
	public BanOnFacility getBanById(PerunSession sess, int banId) throws BanNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		BanOnFacility ban = getFacilitiesManagerBl().getBanById(sess, banId);

		Facility facility = new Facility();
		facility.setId(ban.getId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getBanById_int_policy", Arrays.asList(ban, facility))) {
			throw new PrivilegeException(sess, "getBanById");
		}

		return ban;
	}

	@Override
	public BanOnFacility getBan(PerunSession sess, int userId, int faclityId) throws BanNotExistsException, PrivilegeException, UserNotExistsException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		User user = getPerunBl().getUsersManagerBl().getUserById(sess, userId);
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityById(sess, faclityId);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getBan_int_int_policy", Arrays.asList(user, facility))) {
			throw new PrivilegeException(sess, "getBan");
		}

		return getFacilitiesManagerBl().getBan(sess, userId, faclityId);
	}

	@Override
	public List<BanOnFacility> getBansForUser(PerunSession sess, int userId) throws UserNotExistsException {
		Utils.checkPerunSession(sess);
		User user = getPerunBl().getUsersManagerBl().getUserById(sess, userId);

		List<BanOnFacility> usersBans = getFacilitiesManagerBl().getBansForUser(sess, userId);

		//Authorization
		Iterator<BanOnFacility> iterator = usersBans.iterator();
		while(iterator.hasNext()) {
			BanOnFacility banForFiltering = iterator.next();
			Facility facility = new Facility();
			facility.setId(banForFiltering.getFacilityId());
			if(!AuthzResolver.authorizedInternal(sess, "getBansForUser_int_policy", Arrays.asList(banForFiltering, facility))) {
				iterator.remove();
			}
		}

		return usersBans;
	}

	@Override
	public List<BanOnFacility> getBansForFacility(PerunSession sess, int facilityId) throws PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		Facility facility = this.getFacilitiesManagerBl().getFacilityById(sess, facilityId);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getBansForFacility_int_policy", facility)) {
			throw new PrivilegeException(sess, "getBansForFacility");
		}

		return getFacilitiesManagerBl().getBansForFacility(sess, facilityId);
	}

	@Override
	public BanOnFacility updateBan(PerunSession sess, BanOnFacility banOnFacility) throws PrivilegeException, FacilityNotExistsException, UserNotExistsException, BanNotExistsException {
		Utils.checkPerunSession(sess);
		this.getFacilitiesManagerBl().checkBanExists(sess, banOnFacility.getId());
		Facility facility = this.getFacilitiesManagerBl().getFacilityById(sess, banOnFacility.getFacilityId());
		User user = getPerunBl().getUsersManagerBl().getUserById(sess, banOnFacility.getUserId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "updateBan_BanOnFacility_policy", Arrays.asList(facility, user, banOnFacility))) {
			throw new PrivilegeException(sess, "updateBan");
		}

		banOnFacility = getFacilitiesManagerBl().updateBan(sess, banOnFacility);
		return banOnFacility;
	}

	@Override
	public void removeBan(PerunSession sess, int banId) throws PrivilegeException, BanNotExistsException {
		Utils.checkPerunSession(sess);
		BanOnFacility ban = this.getFacilitiesManagerBl().getBanById(sess, banId);

		Facility facility = new Facility();
		facility.setId(ban.getId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeBan_int_policy", Arrays.asList(ban, facility))) {
			throw new PrivilegeException(sess, "removeBan");
		}

		getFacilitiesManagerBl().removeBan(sess, banId);
	}

	@Override
	public void removeBan(PerunSession sess, int userId, int facilityId) throws BanNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		BanOnFacility ban = this.getFacilitiesManagerBl().getBan(sess, userId, facilityId);

		Facility facility = new Facility();
		facility.setId(ban.getId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeBan_int_int_policy", Arrays.asList(ban, facility))) {
			throw new PrivilegeException(sess, "removeBan");
		}

		getFacilitiesManagerBl().removeBan(sess, userId, facilityId);
	}

	/**
	 * Check existence of every entity in contactGroup
	 *
	 * @param sess
	 * @param contactGroup
	 * @throws FacilityNotExistsException
	 * @throws UserNotExistsException
	 * @throws OwnerNotExistsException
	 * @throws GroupNotExistsException
	 * @throws InternalErrorException
	 */
	private void checkFacilityContactEntitiesExists(PerunSession sess, ContactGroup contactGroup) throws FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException {
		Utils.notNull(contactGroup, "contactGroup");
		Utils.notNull(contactGroup.getFacility(), "facility");
		Utils.notNull(contactGroup.getName(), "name");

		this.getFacilitiesManagerBl().checkFacilityExists(sess, contactGroup.getFacility());

		if(contactGroup.getUsers() != null) {
			for(RichUser user: contactGroup.getUsers()) {
				getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
			}
		}

		if(contactGroup.getGroups() != null) {
			for(Group group: contactGroup.getGroups()) {
				getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
			}
		}

		if(contactGroup.getOwners() != null) {
			for(Owner owner: contactGroup.getOwners()) {
				getPerunBl().getOwnersManagerBl().checkOwnerExists(sess, owner);
			}
		}
	}

	/**
	 * Check existence of every entity in list of contactGroups
	 *
	 * @param sess
	 * @param contactGroups
	 * @throws FacilityNotExistsException
	 * @throws UserNotExistsException
	 * @throws OwnerNotExistsException
	 * @throws GroupNotExistsException
	 * @throws InternalErrorException
	 */
	private void checkFacilityContactsEntitiesExist(PerunSession sess, List<ContactGroup> contactGroups) throws FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException {
		Utils.notNull(contactGroups, "contactGroups");

		for(ContactGroup contactGroup: contactGroups) {
			this.checkFacilityContactEntitiesExists(sess, contactGroup);
		}
	}

	/**
	 * Create a list of PerunBeans from facility, vo and service.
	 * If beans are not null it also checks if they exist. It will skip them otherwise.
	 *
	 * @param sess
	 * @param facility
	 * @param specificVo
	 * @param specificService
	 * @return list of PerunBeans created from the given parameters
	 * @throws FacilityNotExistsException
	 * @throws VoNotExistsException
	 * @throws ServiceNotExistsException
	 */
	private List<PerunBean> createListOfBeans(PerunSession sess, Facility facility, Vo specificVo, Service specificService) throws FacilityNotExistsException, VoNotExistsException, ServiceNotExistsException {
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		List<PerunBean> beans = new ArrayList<>();
		beans.add(facility);
		if(specificVo != null) {
			getPerunBl().getVosManagerBl().checkVoExists(sess, specificVo);
			beans.add(specificVo);
		}
		if(specificService != null) {
			getPerunBl().getServicesManagerBl().checkServiceExists(sess, specificService);
			beans.add(specificService);
		}
		return beans;
	}
}
