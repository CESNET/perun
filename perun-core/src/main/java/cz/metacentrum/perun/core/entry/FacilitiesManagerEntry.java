package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.ContactGroup;
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
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.bl.FacilitiesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.FacilitiesManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
		throw new InternalErrorRuntimeException("Unsupported method!");
	}

	@Override
	public Facility getFacilityById(PerunSession sess, int id) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Facility facility = getFacilitiesManagerBl().getFacilityById(sess, id);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getFacilityById");
				}

		return facility;
	}

	@Override
	public Facility getFacilityByName(PerunSession sess, String name) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(name, "name");

		Facility facility = getFacilitiesManagerBl().getFacilityByName(sess, name);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getFacilityByName");
				}

		return facility;
	}

	@Override
	public List<RichFacility> getRichFacilities(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Perun admin can see everything
		if (AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) || AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return getFacilitiesManagerBl().getRichFacilities(sess);
		} else if (AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
			// Cast complementary object to Facility
			List<Facility> facilities = new ArrayList<>();
			for (PerunBean facility: AuthzResolver.getComplementaryObjectsForRole(sess, Role.FACILITYADMIN, Facility.class)) {
				facilities.add((Facility) facility);
			}
			//Now I create list of richFacilities from facilities
			return getFacilitiesManagerBl().getRichFacilities(sess, facilities);
		} else {
			throw new PrivilegeException(sess, "getRichFacilities");
		}
	}

	@Override
	public List<Facility> getFacilitiesByDestination(PerunSession sess, String destination) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(destination, "destination");

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getFacilitiesByDestination");
		}

		List<Facility> facilities = getFacilitiesManagerBl().getFacilitiesByDestination(sess, destination);

		if (!facilities.isEmpty()) {
			Iterator<Facility> facilityByDestination = facilities.iterator();
			while(facilityByDestination.hasNext()) {
				if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityByDestination.next())) facilityByDestination.remove();
			}
		}

		return facilities;
	}

	@Override
	public List<Facility> getFacilitiesByAttribute(PerunSession sess, String attributeName, String attributeValue)
			throws InternalErrorException, PrivilegeException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
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
	public int getFacilitiesCount(PerunSession sess) throws InternalErrorException {
		Utils.checkPerunSession(sess);

		return getFacilitiesManagerBl().getFacilitiesCount(sess);
	}

	@Override
	public List<Facility> getFacilities(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Perun admin can see everything
		if (AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) ||
				AuthzResolver.isAuthorized(sess, Role.ENGINE) ||
				AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return getFacilitiesManagerBl().getFacilities(sess);
		} else if (AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
			// Cast complementary object to Facility
			List<Facility> facilities = new ArrayList<>();
			for (PerunBean facility: AuthzResolver.getComplementaryObjectsForRole(sess, Role.FACILITYADMIN, Facility.class)) {
				facilities.add((Facility) facility);
			}
			return facilities;
		} else {
			throw new PrivilegeException(sess, "getFacilities");
		}
	}

	@Override
	public List<Owner> getOwners(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getOwners");
				}

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getFacilitiesManagerBl().getOwners(sess, facility);
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
	public List<Vo> getAllowedVos(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAlloewdVos");
		}

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getFacilitiesManagerBl().getAllowedVos(sess, facility);

	}

	@Override
	public List<Group> getAllowedGroups(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(perunSession);

		//Authrorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
			!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getGroupsWhereUserIsActive");
		}

		getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		if(specificVo != null) getPerunBl().getVosManagerBl().checkVoExists(perunSession, specificVo);
		if(specificService != null) getPerunBl().getServicesManagerBl().checkServiceExists(perunSession, specificService);

		return getFacilitiesManagerBl().getAllowedGroups(perunSession, facility, specificVo, specificService);
	}

	@Override
	public List<RichGroup> getAllowedRichGroupsWithAttributes(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService, List<String> attrNames) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException {

		Utils.checkPerunSession(perunSession);

		//Authrorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
			!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getAllowedRichGroupsWithAttributes");
		}

		getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		if(specificVo != null) getPerunBl().getVosManagerBl().checkVoExists(perunSession, specificVo);
		if(specificService != null) getPerunBl().getServicesManagerBl().checkServiceExists(perunSession, specificService);

		List<RichGroup> richGroups = getFacilitiesManagerBl().getAllowedRichGroupsWithAttributes(perunSession, facility, specificVo, specificService, attrNames);
		return getPerunBl().getGroupsManagerBl().filterOnlyAllowedAttributes(perunSession, richGroups, null, true);

	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException{
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAllowedUsers");
		}

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getFacilitiesManagerBl().getAllowedUsers(sess, facility);
	}

	@Override
	public List<User> getAllowedUsers(PerunSession sess, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAllowedUsers");
		}

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		if(specificVo != null) getPerunBl().getVosManagerBl().checkVoExists(sess, specificVo);
		if(specificService != null) getPerunBl().getServicesManagerBl().checkServiceExists(sess, specificService);

		return getFacilitiesManagerBl().getAllowedUsers(sess, facility, specificVo, specificService);
	}

	@Override
	public List<Resource> getAssignedResources(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
			!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedResources");
		}

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getFacilitiesManagerBl().getAssignedResources(sess, facility);
	}

	@Override
	public List<RichResource> getAssignedRichResources(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedRichResources");
		}

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		return getFacilitiesManagerBl().getAssignedRichResources(sess, facility);

	}

	@Override
	public Facility createFacility(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
			throw new PrivilegeException(sess, "createFacility");
		}

		return getFacilitiesManagerBl().createFacility(sess, facility);
	}

	@Override
	public void deleteFacility(PerunSession sess, Facility facility, Boolean force) throws InternalErrorException, RelationExistsException, FacilityNotExistsException, PrivilegeException, FacilityAlreadyRemovedException, HostAlreadyRemovedException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "deleteFacility");
		}

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		getFacilitiesManagerBl().deleteFacility(sess, facility, force);
	}

	@Override
	public Facility updateFacility(PerunSession sess, Facility facility) throws FacilityNotExistsException, FacilityExistsException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		Utils.notNull(facility, "facility");
		Utils.notNull(facility.getName(), "facility.name");

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "updateFacility");
		}

		return getFacilitiesManagerBl().updateFacility(sess, facility);
	}

	@Override
	public List<Facility> getOwnerFacilities(PerunSession sess, Owner owner) throws InternalErrorException, OwnerNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getOwnerFacilities");
		}

		getPerunBl().getOwnersManagerBl().checkOwnerExists(sess, owner);

		return getFacilitiesManagerBl().getOwnerFacilities(sess, owner);
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Facility> facilities = getFacilitiesManagerBl().getAssignedFacilities(sess, group);

		// Authorization
		if (AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) ||
				AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) ||
				AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) ||
				AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) ||
				AuthzResolver.isAuthorized(sess, Role.ENGINE) ||
				AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return facilities;
		}

		if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.FACILITYADMIN)) {
			Iterator<Facility> iterator = facilities.iterator();
			while(iterator.hasNext()) {
				Facility facility = iterator.next();
				if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
					iterator.remove();
				}
			}
			return facilities;
		}
		throw new PrivilegeException(sess, "getAssignedFacilities");
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		List<Facility> facilities = getFacilitiesManagerBl().getAssignedFacilities(sess, member);

		// Authorization
		if (AuthzResolver.isAuthorized(sess, Role.SELF, member) ||
				AuthzResolver.isAuthorized(sess, Role.ENGINE) ||
				AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return facilities;
		}

		if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.FACILITYADMIN)) {
			Iterator<Facility> iterator = facilities.iterator();
			while(iterator.hasNext()) {
				Facility facility = iterator.next();
				if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
					iterator.remove();
				}
			}
			return facilities;
		}
		throw new PrivilegeException(sess, "getAssignedFacilities");


	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		List<Facility> facilities = getFacilitiesManagerBl().getAssignedFacilities(sess, user);

		// Authorization
		if (AuthzResolver.isAuthorized(sess, Role.SELF, user) ||
				AuthzResolver.isAuthorized(sess, Role.ENGINE) ||
				AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return facilities;
		}

		if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.FACILITYADMIN)) {
			Iterator<Facility> iterator = facilities.iterator();
			while(iterator.hasNext()) {
				Facility facility = iterator.next();
				if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
					iterator.remove();
				}
			}
			return facilities;
		}
		throw new PrivilegeException(sess, "getAssignedFacilities");
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, Service service) throws InternalErrorException, PrivilegeException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);

		List<Facility> facilities = getFacilitiesManagerBl().getAssignedFacilities(sess, service);

		// Authorization
		if (AuthzResolver.isAuthorized(sess, Role.ENGINE) ||
				AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return facilities;
		}

		if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.FACILITYADMIN)) {
			Iterator<Facility> iterator = facilities.iterator();
			while(iterator.hasNext()) {
				Facility facility = iterator.next();
				if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
					iterator.remove();
				}
			}
			return facilities;
		}
		throw new PrivilegeException(sess, "getAssignedFacilities");
	}

	@Override
	public List<Facility> getAssignedFacilities(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

		List<Facility> facilities = getFacilitiesManagerBl().getAssignedFacilities(sess, securityTeam);

		// Authorization
		if (AuthzResolver.isAuthorized(sess, Role.SECURITYADMIN, securityTeam) ||
				AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return facilities;
		}

		if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.FACILITYADMIN)) {
			Iterator<Facility> iterator = facilities.iterator();
			while(iterator.hasNext()) {
				Facility facility = iterator.next();
				if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
					iterator.remove();
				}
			}
			return facilities;
		}
		throw new PrivilegeException(sess, "getAssignedFacilities");
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
	public List<Host> getHosts(PerunSession sess, Facility facility) throws FacilityNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		//TODO authorization

		return getFacilitiesManagerBl().getHosts(sess, facility);
	}

	@Override
	public int getHostsCount(PerunSession sess, Facility facility) throws FacilityNotExistsException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getHostsCount");
		}

		return getFacilitiesManagerBl().getHostsCount(sess, facility);
	}

	@Override
	public List<Host> addHosts(PerunSession sess, List<Host> hosts, Facility facility) throws FacilityNotExistsException, InternalErrorException, PrivilegeException, HostExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "addHosts");
		}

		Utils.notNull(hosts, "hosts");

		for(Host host: hosts) {

			List<Facility> facilitiesByHostname = getFacilitiesManagerBl().getFacilitiesByHostName(sess, host.getHostname());
			List<Facility> facilitiesByDestination = getFacilitiesManagerBl().getFacilitiesByDestination(sess, host.getHostname());

			if(facilitiesByHostname.isEmpty() && facilitiesByDestination.isEmpty()) {
				continue;
			}
			if(!facilitiesByHostname.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByHostname: facilitiesByHostname) {
					if(AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityByHostname)) {
						hasRight = true;
						break;
					}
				}
				if(hasRight) continue;
			}
			if(!facilitiesByDestination.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByDestination: facilitiesByDestination) {
					if(AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityByDestination)) {
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
	public List<Host> addHosts(PerunSession sess, Facility facility, List<String> hosts) throws FacilityNotExistsException, InternalErrorException, PrivilegeException, HostExistsException, WrongPatternException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "addHosts");
		}

		Utils.notNull(hosts, "hosts");

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
					if(AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityByHostname)) {
						hasRight = true;
						break;
					}
				}
				if(hasRight) continue;
			}
			if(!facilitiesByDestination.isEmpty()) {
				boolean hasRight = false;
				for(Facility facilityByDestination: facilitiesByDestination) {
					if(AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityByDestination)) {
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

	@Override
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

	@Override
	public void removeAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, FacilityNotExistsException, UserNotExistsException, PrivilegeException, UserNotAdminException {
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
	public void removeAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, FacilityNotExistsException, GroupNotExistsException, PrivilegeException, GroupNotAdminException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "deleteAdmin");
		}

		getFacilitiesManagerBl().removeAdmin(sess, facility, group);

	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Facility facility, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(perunSession);
		getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getAdmins");
		}

		return getFacilitiesManagerBl().getAdmins(perunSession, facility, onlyDirectAdmins);
	}


	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Facility facility, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(perunSession);
		getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		if(!allUserAttributes) Utils.notNull(specificAttributes, "specificAttributes");

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getRichAdmins");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getFacilitiesManagerBl().getRichAdmins(perunSession, facility, specificAttributes, allUserAttributes, onlyDirectAdmins));
	}

	@Override
	@Deprecated
	public List<User> getAdmins(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
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
	public List<User> getDirectAdmins(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
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
	public List<Group> getAdminGroups(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAdminGroups");
		}

		return getFacilitiesManagerBl().getAdminGroups(sess, facility);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
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
	public List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException, FacilityNotExistsException, PrivilegeException {
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
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
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
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
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
	public List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if(!AuthzResolver.isAuthorized(sess, Role.SELF, user) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getFacilitiesWhereUserIsAdmin");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		return getFacilitiesManagerBl().getFacilitiesWhereUserIsAdmin(sess, user);
	}

	@Override
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

	@Override
	public void copyAttributes(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, sourceFacility);
		getFacilitiesManagerBl().checkFacilityExists(sess, destinationFacility);

		// Authorization - facility admin of the both facilities required
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, sourceFacility)) {
			throw new PrivilegeException(sess, "copyAttributes");
		}
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, destinationFacility)) {
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
	public Host addHost(PerunSession sess, Host host, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "addHost");
		}

		Utils.notNull(host, "hosts");

		List<Facility> facilitiesByHostname = getFacilitiesManagerBl().getFacilitiesByHostName(sess, host.getHostname());
		List<Facility> facilitiesByDestination = getFacilitiesManagerBl().getFacilitiesByDestination(sess, host.getHostname());

		if(facilitiesByHostname.isEmpty() && facilitiesByDestination.isEmpty()) {
			return getFacilitiesManagerBl().addHost(sess, host, facility);
		}
		if(!facilitiesByHostname.isEmpty()) {
			boolean hasRight = false;
			for(Facility facilityByHostname: facilitiesByHostname) {
				if(AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityByHostname)) {
					hasRight = true;
					break;
				}
			}
			if(hasRight) return getFacilitiesManagerBl().addHost(sess, host, facility);
		}
		if(!facilitiesByDestination.isEmpty()) {
			boolean hasRight = false;
			for(Facility facilityByDestination: facilitiesByDestination) {
				if(AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityByDestination)) {
					hasRight = true;
					break;
				}
			}
			if(hasRight) return getFacilitiesManagerBl().addHost(sess, host, facility);
		}

		throw new PrivilegeException(sess, "You can't add host " + host + ", because you don't have privileges to use this hostName");
	}

	@Override
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

	@Override
	public Host getHostById(PerunSession sess, int hostId) throws HostNotExistsException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getHostById");
				}

		return getFacilitiesManagerBl().getHostById(sess, hostId);
	}

	@Override
	public List<Host> getHostsByHostname(PerunSession sess, String hostname) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(hostname, "hostname");

		if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getHostsByHostname");
		}

		List<Host> hostsByHostname = getFacilitiesManagerBl().getHostsByHostname(sess, hostname);

		//need to remove those hosts, which are not from facilities of this facility admin
		if(!AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.PERUNADMIN) && AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.FACILITYADMIN)) {
			//get all complementary facilities for this perunPrincipal
			List<Facility> authorizedFacilities = new ArrayList<>();
			List<PerunBean> complementaryObjects =  AuthzResolver.getComplementaryObjectsForRole(sess, Role.FACILITYADMIN);
			for(PerunBean pb: complementaryObjects) {
				if(pb instanceof Facility) authorizedFacilities.add((Facility) pb);
			}

			//remove hosts which has not facility from authorized facilities
			Iterator<Host> hostIterator = hostsByHostname.iterator();
			while(hostIterator.hasNext()) {
				Host host = hostIterator.next();
				Facility fac = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
				if(!authorizedFacilities.contains(fac)) hostIterator.remove();
			}
		}

		return hostsByHostname;
	}

	@Override
	public Facility getFacilityForHost(PerunSession sess, Host host) throws InternalErrorException, PrivilegeException, HostNotExistsException {
		Utils.checkPerunSession(sess);

		getFacilitiesManagerBl().checkHostExists(sess, host);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getFacilityForHost");
		}

		return getFacilitiesManagerBl().getFacilityForHost(sess, host);
	}

	@Override
	public List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname) throws InternalErrorException {
		Utils.checkPerunSession(sess);

		List<Facility> facilities = getFacilitiesManagerBl().getFacilitiesByHostName(sess, hostname);

		if (AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return facilities;
		}

		if (!facilities.isEmpty()) {
			Iterator<Facility> facilityByHostname = facilities.iterator();
			while(facilityByHostname.hasNext()) {
				if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityByHostname.next())) {
					facilityByHostname.remove();
				}
			}
		}
		return facilities;
	}

	@Override
	public List<User> getAssignedUsers(PerunSession sess, Facility facility) throws PrivilegeException, InternalErrorException
	{
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedUser");
		}

		return this.getPerunBl().getFacilitiesManagerBl().getAssignedUsers(sess,facility);
	}

	@Override
	public List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service) throws PrivilegeException, InternalErrorException{
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedUser");
		}

		return this.getPerunBl().getFacilitiesManagerBl().getAssignedUsers(sess,facility,service);
	}

	// FACILITY CONTACTS METHODS

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Owner owner) throws InternalErrorException, OwnerNotExistsException {
		Utils.checkPerunSession(sess);
		perunBl.getOwnersManagerBl().checkOwnerExists(sess, owner);
		List<ContactGroup> contactGroups = this.getFacilitiesManagerBl().getFacilityContactGroups(sess, owner);

		if(contactGroups == null) return new ArrayList<>();

		//perunobserver can see anything
		if (AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return contactGroups;
		}

		Iterator<ContactGroup> facilityContactGroup = contactGroups.iterator();
		while(facilityContactGroup.hasNext()) {
			if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityContactGroup.next().getFacility())) {
				facilityContactGroup.remove();
			}
		}

		return contactGroups;
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		perunBl.getUsersManagerBl().checkUserExists(sess, user);
		List<ContactGroup> contactGroups = this.getFacilitiesManagerBl().getFacilityContactGroups(sess, user);

		if(contactGroups == null) return new ArrayList<>();

		//perunobserver can see anything
		if (AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return contactGroups;
		}

		Iterator<ContactGroup> facilityContactGroup = contactGroups.iterator();
		while(facilityContactGroup.hasNext()) {
			if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityContactGroup.next().getFacility())) {
				facilityContactGroup.remove();
			}
		}

		return contactGroups;
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
		List<ContactGroup> contactGroups = this.getFacilitiesManagerBl().getFacilityContactGroups(sess, group);

		if(contactGroups == null) return new ArrayList<>();

		//perunobserver can see anything
		if (AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return contactGroups;
		}

		Iterator<ContactGroup> facilityContactGroup = contactGroups.iterator();
		while(facilityContactGroup.hasNext()) {
			if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facilityContactGroup.next().getFacility())) {
				facilityContactGroup.remove();
			}
		}

		return contactGroups;
	}

	@Override
	public List<ContactGroup> getFacilityContactGroups(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		perunBl.getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getFacilityContactGroups");
		}

		return this.getFacilitiesManagerBl().getFacilityContactGroups(sess, facility);
	}

	@Override
	public ContactGroup getFacilityContactGroup(PerunSession sess, Facility facility, String name) throws InternalErrorException, FacilityContactNotExistsException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(name, "name");
		this.getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getFacilityContactGroup");
		}

		return this.getFacilitiesManagerBl().getFacilityContactGroup(sess, facility, name);
	}

	@Override
	public List<String> getAllContactGroupNames(PerunSession sess) throws InternalErrorException {
		Utils.checkPerunSession(sess);
		return this.getFacilitiesManagerBl().getAllContactGroupNames(sess);
	}

	@Override
	public void addFacilityContacts(PerunSession sess, List<ContactGroup> contactGroupsToAdd) throws InternalErrorException, FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		this.checkFacilityContactsEntitiesExist(sess, contactGroupsToAdd);

		Iterator<ContactGroup> iter = contactGroupsToAdd.iterator();
		while(iter.hasNext()) {
			ContactGroup contactGroupToAdd = iter.next();
			if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, contactGroupToAdd.getFacility())) {
				iter.remove();
			}
 		}

		if(!contactGroupsToAdd.isEmpty()) {
			this.facilitiesManagerBl.addFacilityContacts(sess, contactGroupsToAdd);
		}
	}

	@Override
	public void addFacilityContact(PerunSession sess, ContactGroup contactGroupToAdd) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		this.checkFacilityContactEntitiesExists(sess, contactGroupToAdd);

		if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, contactGroupToAdd.getFacility())) {
			throw new PrivilegeException(sess, "addFacilityContact");
		}

		this.getFacilitiesManagerBl().addFacilityContact(sess, contactGroupToAdd);
	}

	@Override
	public void removeFacilityContacts(PerunSession sess, List<ContactGroup> contactGroupsToRemove) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		this.checkFacilityContactsEntitiesExist(sess, contactGroupsToRemove);

		for (ContactGroup contactGroupToRemove : contactGroupsToRemove) {
			if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, contactGroupToRemove.getFacility())) {
				throw new PrivilegeException(sess, "removeFacilityContacts");
			}

		}

		this.getFacilitiesManagerBl().removeFacilityContacts(sess, contactGroupsToRemove);
	}

	@Override
	public void removeFacilityContact(PerunSession sess, ContactGroup contactGroupToRemove) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		this.checkFacilityContactEntitiesExists(sess, contactGroupToRemove);

		if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, contactGroupToRemove.getFacility())) {
			throw new PrivilegeException(sess, "contactGroupToRemove");
		}

		this.getFacilitiesManagerBl().removeFacilityContact(sess, contactGroupToRemove);
	}

	@Override
	public List<SecurityTeam> getAssignedSecurityTeams(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAssignedSecurityTeams");
		}

		return this.getFacilitiesManagerBl().getAssignedSecurityTeams(sess, facility);
	}

	@Override
	public void assignSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, SecurityTeamNotExistsException, FacilityNotExistsException, SecurityTeamAlreadyAssignedException {
		Utils.checkPerunSession(sess);
		getPerunBl().getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getFacilitiesManagerBl().checkSecurityTeamNotAssigned(sess, facility, securityTeam);

		if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "assignSecurityTeam");
		}

		this.getFacilitiesManagerBl().assignSecurityTeam(sess, facility, securityTeam);
	}

	@Override
	public void removeSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, SecurityTeamNotExistsException, SecurityTeamNotAssignedException {
		Utils.checkPerunSession(sess);
		getPerunBl().getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
		getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getFacilitiesManagerBl().checkSecurityTeamAssigned(sess, facility, securityTeam);

		if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "removeSecurityTeam");
		}

		this.getFacilitiesManagerBl().removeSecurityTeam(sess, facility, securityTeam);
	}

	@Override
	public BanOnFacility setBan(PerunSession sess, BanOnFacility banOnFacility) throws InternalErrorException, PrivilegeException, BanAlreadyExistsException, FacilityNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(banOnFacility, "banOnFacility");
		User user = getPerunBl().getUsersManagerBl().getUserById(sess, banOnFacility.getUserId());
		Facility facility = this.getFacilitiesManagerBl().getFacilityById(sess, banOnFacility.getFacilityId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "setBan");
		}

		return getFacilitiesManagerBl().setBan(sess, banOnFacility);
	}

	@Override
	public BanOnFacility getBanById(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		BanOnFacility ban = getFacilitiesManagerBl().getBanById(sess, banId);

		Facility facility = new Facility();
		facility.setId(ban.getId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getBanById");
		}

		return ban;
	}

	@Override
	public BanOnFacility getBan(PerunSession sess, int userId, int faclityId) throws InternalErrorException, BanNotExistsException, PrivilegeException, UserNotExistsException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		User user = getPerunBl().getUsersManagerBl().getUserById(sess, userId);
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityById(sess, faclityId);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getBan");
		}

		return getFacilitiesManagerBl().getBan(sess, userId, faclityId);
	}

	@Override
	public List<BanOnFacility> getBansForUser(PerunSession sess, int userId) throws InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		User user = getPerunBl().getUsersManagerBl().getUserById(sess, userId);

		List<BanOnFacility> usersBans = getFacilitiesManagerBl().getBansForUser(sess, userId);

		if (AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			return usersBans;
		}

		//filtering
		Iterator<BanOnFacility> iterator = usersBans.iterator();
		while(iterator.hasNext()) {
			BanOnFacility banForFiltering = iterator.next();
			Facility facility = new Facility();
			facility.setId(banForFiltering.getFacilityId());
			if(!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
				iterator.remove();
			}
		}

		return usersBans;
	}

	@Override
	public List<BanOnFacility> getBansForFacility(PerunSession sess, int facilityId) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		Facility facility = this.getFacilitiesManagerBl().getFacilityById(sess, facilityId);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getBansForFacility");
		}

		return getFacilitiesManagerBl().getBansForFacility(sess, facilityId);
	}

	@Override
	public BanOnFacility updateBan(PerunSession sess, BanOnFacility banOnFacility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, UserNotExistsException, BanNotExistsException {
		Utils.checkPerunSession(sess);
		this.getFacilitiesManagerBl().checkBanExists(sess, banOnFacility.getId());
		Facility facility = this.getFacilitiesManagerBl().getFacilityById(sess, banOnFacility.getFacilityId());
		User user = getPerunBl().getUsersManagerBl().getUserById(sess, banOnFacility.getUserId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "updateBan");
		}

		banOnFacility = getFacilitiesManagerBl().updateBan(sess, banOnFacility);
		return banOnFacility;
	}

	@Override
	public void removeBan(PerunSession sess, int banId) throws InternalErrorException, PrivilegeException, BanNotExistsException {
		Utils.checkPerunSession(sess);
		BanOnFacility ban = this.getFacilitiesManagerBl().getBanById(sess, banId);

		Facility facility = new Facility();
		facility.setId(ban.getId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "removeBan");
		}

		getFacilitiesManagerBl().removeBan(sess, banId);
	}

	@Override
	public void removeBan(PerunSession sess, int userId, int facilityId) throws InternalErrorException, BanNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		BanOnFacility ban = this.getFacilitiesManagerBl().getBan(sess, userId, facilityId);

		Facility facility = new Facility();
		facility.setId(ban.getId());

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
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
	private void checkFacilityContactEntitiesExists(PerunSession sess, ContactGroup contactGroup) throws FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException, InternalErrorException {
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
	private void checkFacilityContactsEntitiesExist(PerunSession sess, List<ContactGroup> contactGroups) throws FacilityNotExistsException, UserNotExistsException, OwnerNotExistsException, GroupNotExistsException, InternalErrorException {
		Utils.notNull(contactGroups, "contactGroups");

		for(ContactGroup contactGroup: contactGroups) {
			this.checkFacilityContactEntitiesExists(sess, contactGroup);
		}
	}
}
