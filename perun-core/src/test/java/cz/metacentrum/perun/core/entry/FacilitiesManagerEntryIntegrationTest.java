package cz.metacentrum.perun.core.entry;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.FacilitiesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import org.springframework.util.Assert;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Integration tests of FacilitiesManager
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilitiesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	final Facility facility = new Facility(); // always in DB
	private Owner owner = new Owner(); // always in DB and always own "facility" facility

	private static final String CLASS_NAME = "FacilitiesManager.";

	private Host createdHost;
	private List<Host> hosts;
	private FacilitiesManager facilitiesManagerEntry;
	private Facility emptyFac;
	private Vo vo;

	@Before
	public void setUp() throws Exception {

		facility.setName("FacilitiesManagerTestFacility");
		facility.setDescription("FacilityTestDescriptionText");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, facility));
		owner.setName("FacilityManagerTestOwner");
		owner.setContact("testingContact");
		owner.setType(OwnerType.technical);
		owner = perun.getOwnersManager().createOwner(sess, owner);
		assertNotNull("unable to create owner",owner);
		perun.getFacilitiesManager().addOwner(sess, facility, owner);

		facilitiesManagerEntry = perun.getFacilitiesManager();

		// create list of hosts with 1 host
		createdHost = new Host();
		createdHost.setHostname("FacilitiesManagerTest");
		hosts = new ArrayList<Host>();
		hosts.add(createdHost);

		//create empty facility
		emptyFac = new Facility();

		vo = new Vo(0, "facilityTestVo001", "facilityTestVo001");
		vo = perun.getVosManagerBl().createVo(sess, vo);
	}

	@Test
	public void getFacilityById() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityById");

		Facility returnedFacility = perun.getFacilitiesManager().getFacilityById(sess, facility.getId());
		assertNotNull("unable to get Facility by ID",returnedFacility);
		assertEquals("created and returned facility should be the same", returnedFacility, facility);

	}

	@Test
	public void getFacilitiesByHostName() throws Exception {
		System.out.println(CLASS_NAME + "getFacilitiesByHostname");

		String hostname = "testHostname";
		Host host = new Host(15, hostname);
		perun.getFacilitiesManagerBl().addHost(sess, host, facility);

		List<Facility> facilities = perun.getFacilitiesManager().getFacilitiesByHostName(sess, hostname);
		assertNotNull("unable to get facilities by Hostname", facilities);
		assertEquals("There is only one facility with host with specific hostname", 1, facilities.size());
	}

	@Test (expected=FacilityNotExistsException.class)
	public void getFacilityByIdWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityByIdWhenFacilityNotExists");

		facility.setId(0);
		perun.getFacilitiesManager().getFacilityById(sess, facility.getId());
		// shouldn't find facility

	}

	@Test
	public void getFacilityByName() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityByName");

		Facility returnedFacility = perun.getFacilitiesManager().getFacilityByName(sess, facility.getName());
		assertNotNull("unable to get Facility by Name", returnedFacility);
		assertEquals("created and returned facility should be the same", returnedFacility, facility);

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getFacilityByNameWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getFacilityByNameWhenFacilityNotExists");

		facility.setName("");
		perun.getFacilitiesManager().getFacilityByName(sess, facility.getName());
		// shouldn't find facility

	}

	@Test
	public void getFacilitiesByDestination() throws Exception {
		System.out.println(CLASS_NAME + "getFacilitiesByDestination");

		Service serv = new Service();
		serv.setName("TestovaciSluzba");
		perun.getServicesManager().createService(sess, serv);

		Destination dest = new Destination();
		dest.setType("service-specific");
		dest.setDestination("TestovaciDestinace");
		perun.getServicesManager().addDestination(sess, serv, facility, dest);

		List<Facility> facilities = perun.getFacilitiesManager().getFacilitiesByDestination(sess, "TestovaciDestinace");
		assertTrue("At least one facility with destinatnion " + dest.getDestination() + " should exists", facilities.size() > 0);
		assertTrue("Created facility with destinantion " + dest.getDestination() + " should exist between others", facilities.contains(facility));
	}

	@Test
	public void getFacilitiesByAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getFacilitiesByAttribute");

		// Check if the attribute already exists
		Attribute attr;
		AttributeDefinition attrDef;
		String attributeName = "urn:perun:facility:attribute-def:def:facility-test-attribute";
		try {
			attrDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
		} catch (AttributeNotExistsException e) {
			// Attribute doesn't exist, so create it
			attrDef = new AttributeDefinition();
			attrDef.setNamespace("urn:perun:facility:attribute-def:def");
			attrDef.setFriendlyName("facility-test-attribute");
			attrDef.setType(String.class.getName());

			attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		}

		attr = new Attribute(attrDef);
		attr.setValue("value");

		// Set the attribute to the facility
		perun.getAttributesManagerBl().setAttribute(sess, facility, attr);

		assertTrue("results must contain user", facilitiesManagerEntry.getFacilitiesByAttribute(sess, attributeName, "value").contains(facility));
	}

	@Test
	public void getFacilitiesByDestinationWhenFacilityNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getFacilitiesByDestinationWhenFacilityNotExist");
		List<Facility> facilities = perun.getFacilitiesManager().getFacilitiesByDestination(sess,"TestovaciDestinace neexistujici.");
		assertTrue("No facility with such destination exist.", facilities.isEmpty());
	}

	@Test
	public void getFacilities() throws Exception {
		System.out.println(CLASS_NAME + "getFacilities");

		List<Facility> facilities = perun.getFacilitiesManager().getFacilities(sess);
		assertTrue("at least one facility should exists", facilities.size() > 0);
		assertTrue("created facility should exist between others", facilities.contains(facility));

	}

	@Test
	public void getOwners() throws Exception {
		System.out.println(CLASS_NAME + "getOwners");

		List<Owner> owners = perun.getFacilitiesManager().getOwners(sess, facility);
		assertTrue("there should be 1 owner", owners.size() == 1);
		assertTrue("facility should be owned by our owner", owners.contains(owner));

		perun.getFacilitiesManager().removeOwner(sess, facility, owner);
		List<Owner> empty_owners = perun.getFacilitiesManager().getOwners(sess, facility);
		assertTrue("there shouldn't be any owner", empty_owners.isEmpty());

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getOwnersWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getOwnersWhenFacilityNotExists");

		perun.getFacilitiesManager().getOwners(sess, new Facility());
		// shouldn't find facility

	}

	@Test
	public void addOwner() throws Exception {
		System.out.println(CLASS_NAME + "addOwner");

		Owner secondOwner = new Owner();
		secondOwner.setName("SecondTestOwner");
		secondOwner.setContact("testingSecondOwner");
		secondOwner.setType(OwnerType.technical);
		perun.getOwnersManager().createOwner(sess, secondOwner);
		perun.getFacilitiesManager().addOwner(sess, facility, secondOwner);

		List<Owner> owners = perun.getFacilitiesManager().getOwners(sess, facility);
		assertTrue("facility should have 2 owners", owners.size() == 2);
		assertTrue("our owner should own our facility", owners.contains(secondOwner));

	}

	@Test (expected=OwnerNotExistsException.class)
	public void addOwnerWhenOwnerNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addOwnerWhenOwnerNotExists");

		perun.getFacilitiesManager().addOwner(sess, facility, new Owner());
		// shouldn't be able to add not existing owner
	}

	@Test (expected=FacilityNotExistsException.class)
	public void addOwnerWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addOwnerWhenFacilityNotExists");

		Owner secondOwner = new Owner();
		secondOwner.setName("SecondTestOwner");
		secondOwner.setContact("testingSecondOwner");
		secondOwner.setType(OwnerType.technical);
		perun.getFacilitiesManager().addOwner(sess, new Facility(), secondOwner);
		// shouldn't facility

	}

	@Test (expected=OwnerAlreadyAssignedException.class)
	public void addOwnerWhenOwnerAlreadyAssigned() throws Exception {
		System.out.println(CLASS_NAME + "addOwnerWhenOwnerAlreadyAssigned");

		perun.getFacilitiesManager().addOwner(sess, facility, owner);
		// shouldn't be able to add same owner

	}

	@Test
	public void removeOwner() throws Exception {
		System.out.println(CLASS_NAME + "removeOwner");

		perun.getFacilitiesManager().removeOwner(sess, facility, owner);

		List<Owner> owners = perun.getFacilitiesManager().getOwners(sess, facility);
		assertTrue("facility shouldn't have owner", owners.isEmpty());

	}

	@Test (expected=OwnerAlreadyRemovedException.class)
	public void removeOwnerWhenOwnerAlreadyRemoved() throws Exception {
		System.out.println(CLASS_NAME + "removeOwnerWhenOwnerAlreadyRemoved");

		perun.getFacilitiesManager().removeOwner(sess, facility, owner);
		perun.getFacilitiesManager().removeOwner(sess, facility, owner);
		// shouldn't be able to remove owner twice

	}

	@Test (expected=OwnerNotExistsException.class)
	public void removeOwnerWhenOwnerNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeOwnerWhenOwnerNotExists");

		perun.getFacilitiesManager().removeOwner(sess, facility, new Owner());
		// shouldn't be able to remove not existing owner

	}

	@Test (expected=FacilityNotExistsException.class)
	public void removeOwnerWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeOwnerWhenFacilityNotExists");

		perun.getFacilitiesManager().removeOwner(sess, new Facility(), owner);
		// shouldn't find facility

	}

	@Test
	public void getAllowedVos() throws Exception {
		System.out.println(CLASS_NAME + "getAllowedVos");

		Vo vo = setUpVo();
		setUpResource(vo);

		List<Vo> allowedVos = perun.getFacilitiesManager().getAllowedVos(sess, facility);

		assertTrue("our facility should have 1 allowed VO", allowedVos.size() == 1);
		assertTrue("our facility should have our VO as allowed", allowedVos.contains(vo));

	}

	@Test
	public void getAllowedUsers() throws Exception {
		System.out.println(CLASS_NAME + "getAllowedUsers");

		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		List<User> users = perun.getFacilitiesManager().getAllowedUsers(sess, facility);
		assertTrue("our facility should have 1 allowed user", users.size() == 1);
		assertTrue("our user should be between allowed on facility", users.contains(user));

	}

	@Test
	public void getAllowedUsersCheckUniqueness() throws Exception {
		System.out.println(CLASS_NAME + "getAllowedUsersCheckUniqueness");

		Vo vo = setUpVo();
		Resource resource1 = setUpResource(vo);
		Resource resource2 = setUpResource2(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		Group group2 = setUpGroup2(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource1);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource2);

		List<User> users = perun.getFacilitiesManager().getAllowedUsers(sess, facility);
		assertTrue("our facility should have 1 allowed user", users.size() == 1);
		assertTrue("our user should be between allowed on facility", users.contains(user));

	}

	@Test
	public void getAllowedUsersWithVoAndServiceFilter() throws Exception {
		System.out.println(CLASS_NAME + "getAllowedUsers");

		Vo vo = setUpVo();

		Resource resource = setUpResource(vo);

		Service serv = new Service();
		serv.setName("TestService");
		perun.getServicesManager().createService(sess, serv);
		perun.getResourcesManager().assignService(sess, resource, serv);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		// second vo and member, assign group but no service
		Vo vo2 = new Vo();
		vo2.setName("FacilitiesMangerTestVo2");
		vo2.setShortName("FMTVO2");
		assertNotNull("unable to create VO",perun.getVosManager().createVo(sess, vo2));

		Member member2 = setUpMember(vo2);
		User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
		Group group2 = setUpGroup(vo2, member2);
		Resource resource2 = setUpResource(vo2);
		perun.getResourcesManager().assignService(sess, resource2, serv);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource2);

		List<User> users = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, serv);
		assertTrue("our facility should have 1 allowed user",users.size() == 1);
		assertTrue("our user should be between allowed on facility",users.contains(user));

		List<User> users2 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, serv);
		assertTrue("our facility should have 1 allowed user",users2.size() == 1);
		assertTrue("our user should be between allowed on facility",users2.contains(user2));

		List<User> users3 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, null, serv);
		assertTrue("our facility should have 1 allowed user",users3.size() == 2);
		assertTrue("our user should be between allowed on facility",users3.contains(user));
		assertTrue("our user should be between allowed on facility",users3.contains(user2));

		List<User> users4 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, null);
		assertTrue("our facility should have 1 allowed user",users4.size() == 1);
		assertTrue("our user should be between allowed on facility",users4.contains(user));

		List<User> users5 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, null);
		assertTrue("our facility should have 1 allowed user",users5.size() == 1);
		assertTrue("our user should be between allowed on facility",users5.contains(user2));

		// remove service from resource2 to test other edge cases
		perun.getResourcesManager().removeService(sess, resource2, serv);

		List<User> users6 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, serv);
		assertTrue("our facility should have 1 allowed user",users6.size() == 1);
		assertTrue("our user should be between allowed on facility",users6.contains(user));

		List<User> users7 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, serv);
		assertTrue("our user shouldn't be allowed on facility with vo filter on", users7.size() == 0);

		List<User> users8 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, null, serv);
		assertTrue("our facility should have 1 allowed user",users8.size() == 1);
		assertTrue("our user should be between allowed on facility",users8.contains(user));
		assertTrue("our user shouldn't be between allowed on facility",!users8.contains(user2));

		List<User> users9 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, null);
		assertTrue("our facility should have 1 allowed user",users9.size() == 1);
		assertTrue("our user should be between allowed on facility",users9.contains(user));

		List<User> users10 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, null);
		assertTrue("our facility should have 1 allowed user",users10.size() == 1);
		assertTrue("our user should be between allowed on facility",users10.contains(user2));

		// create different service to test another edge cases

		Service serv2 = new Service();
		serv2.setName("TestService2");
		serv2 = perun.getServicesManager().createService(sess, serv2);
		perun.getResourcesManager().assignService(sess, resource2, serv2);

		List<User> users11 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, serv2);
		assertTrue("our facility shouldn't have allowed user with vo and service filter on",users11.size() == 0);

		List<User> users12 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, serv2);
		assertTrue("our facility should have 1 allowed user",users12.size() == 1);
		assertTrue("our user should be between allowed on facility",users12.contains(user2));

		List<User> users13 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, null, serv2);
		assertTrue("our facility should have 1 allowed user",users13.size() == 1);
		assertTrue("our user should be between allowed on facility",users13.contains(user2));

		List<User> users14 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, null);
		assertTrue("our facility should have 1 allowed user",users14.size() == 1);
		assertTrue("our user should be between allowed on facility",users14.contains(user));

		List<User> users15 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, null);
		assertTrue("our facility should have 1 allowed user",users15.size() == 1);
		assertTrue("our user should be between allowed on facility",users15.contains(user2));

		List<User> users16 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, null, null);
		assertTrue("our facility should have 2 allowed users",users16.size() == 2);
		assertTrue("our user should be between allowed on facility",users16.contains(user));
		assertTrue("our user should be between allowed on facility",users16.contains(user2));

		// disabled members shouldn't be allowed
		perun.getMembersManager().setStatus(sess, member, Status.DISABLED);

		List<User> users17 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, null, null);
		assertTrue("our facility should have 1 allowed user",users17.size() == 1);
		assertTrue("our user should be between allowed on facility",users17.contains(user2));

		List<User> users18 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, null);
		assertTrue("our facility shouldn't have allowed user with vo filter on",users18.size() == 0);

		List<User> users19 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, null);
		assertTrue("our facility should have 1 allowed user",users19.size() == 1);
		assertTrue("our user should be between allowed on facility",users19.contains(user2));

		List<User> users20 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, serv);
		assertTrue("our facility shouldn't have allowed user with vo and service filter on",users20.size() == 0);

		List<User> users21 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, serv2);
		assertTrue("our facility should have 1 allowed user",users21.size() == 1);
		assertTrue("our user should be between allowed on facility",users21.contains(user2));

	}


	@Test (expected=FacilityNotExistsException.class)
	public void getAllowedVosWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAllowedVosWhenFacilityNotExists");

		perun.getFacilitiesManager().getAllowedVos(sess, new Facility());
		//shouldn't find facility
	}

	@Test
	public void getAssignedResources() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedResources");

		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		List<Resource> assignedResources = perun.getFacilitiesManager().getAssignedResources(sess, facility);

		assertTrue("our facility should have 1 assigned Resource", assignedResources.size() == 1);
		assertTrue("our facility should have our Resource assigned", assignedResources.contains(resource));

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getAssignedResourcesWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedResourcesWhenFacilityNotExists");

		perun.getFacilitiesManager().getAssignedResources(sess, new Facility());
		// shouldn't find facility

	}

	@Test
	public void getAssignedRichResources() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedRichResources");

		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);
		RichResource rresource = new RichResource(resource);
		rresource.setVo(perun.getResourcesManager().getVo(sess, resource));

		List<RichResource> assignedResources = perun.getFacilitiesManager().getAssignedRichResources(sess, facility);

		List<Vo> vos = new ArrayList<Vo>();
		for (RichResource rr : assignedResources){
			assertTrue("RichResource must have VO value filled",rr.getVo() != null);
			vos.add(rr.getVo());
		}
		assertTrue("Our VO must be between RichResources VOs", vos.contains(vo));

		assertTrue("our facility should have 1 assigned Resource", assignedResources.size() == 1);
		assertTrue("our facility should have our Resource assigned", assignedResources.contains(rresource));

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getAssignedRichResourcesWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedRichResourcesWhenFacilityNotExists");

		perun.getFacilitiesManager().getAssignedRichResources(sess, new Facility());
		// shouldn't find facility

	}

	@Test
	public void createFacility() throws Exception {
		System.out.println(CLASS_NAME + "createFacility");

		Facility facility = new Facility();
		facility.setName("FacilitiesManagerTestSecondFacility");
		facility.setDescription("TestSecondFacilityDescriptionText");
		Facility returnedFacility = perun.getFacilitiesManager().createFacility(sess, facility);
		assertNotNull("unable to create Facility", returnedFacility);
		assertEquals("created and returned facility should be the same", returnedFacility, facility);

	}

	@Test (expected=FacilityExistsException.class)
	public void createFacilityWhenFacilityExists() throws Exception {
		System.out.println(CLASS_NAME + "createFacilityWhenFacilityExists");

		Facility facility = new Facility();
		facility.setName("FacilitiesManagerTestFacility");

		perun.getFacilitiesManager().createFacility(sess, facility);
		// shouldn't create same facility twice

	}



	@Test (expected=FacilityNotExistsException.class)
	public void deleteFacility() throws Exception {
		System.out.println(CLASS_NAME + "deleteFacility");

		perun.getFacilitiesManager().deleteFacility(sess, facility);
		perun.getFacilitiesManager().deleteFacility(sess, facility);
		// shouldn't find and delete "deleted facility"

	}

	@Test (expected=RelationExistsException.class)
	public void deleteFacilityWhenRelationExist() throws Exception {
		System.out.println(CLASS_NAME + "deleteFacilityWhenRelationExist");

		Vo vo = setUpVo();
		// create VO
		setUpResource(vo);
		// create Resource for our facility
		perun.getFacilitiesManager().deleteFacility(sess, facility);
		// shouldn't delete facility with resource

	}

	@Test
	public void getOwnerFacilities() throws Exception {
		System.out.println(CLASS_NAME + "getOwnerFacilities");

		List<Facility> facilities = perun.getFacilitiesManager().getOwnerFacilities(sess, owner);
		assertTrue("our owner should own 1 facility", facilities.size() == 1);
		assertTrue("owner should own his facility", facilities.contains(facility));

	}

	@Test (expected=OwnerNotExistsException.class)
	public void getOwnerFacilitiesWhenOwnerNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getOwnerFacilitiesWhenOwnerNotExists");

		perun.getFacilitiesManager().getOwnerFacilities(sess, new Owner());
		// shouldn't find owner

	}

	@Test
	public void addHosts()throws Exception {
		System.out.println(CLASS_NAME + "addHosts");

		hosts = facilitiesManagerEntry.addHosts(sess, hosts, facility);
		// set this host for deletion - host is created after adding to facility !!
		hostsForDeletion.add(hosts.get(0));
		// test
		assertNotNull("Unable to add hosts", hosts);
		assertEquals("There should be only 1 host in list", 1, hosts.size());
		assertNotNull("Our host shouldn't be null after adding", hosts.get(0));

	}

	// FIXME - cannot test it when host always gets a new ID when added to facility
	@Ignore
	@Test (expected=HostExistsException.class)
	public void addHostsWhenHostExistsException()throws Exception {
		System.out.println(CLASS_NAME + "addHostsWhenHostExistsException");

		hosts = facilitiesManagerEntry.addHosts(sess, hosts, facility);
		// set this host for deletion - host is created after adding to facility !!
		hostsForDeletion.add(hosts.get(0));
		// shouldn't add same host twice
		facilitiesManagerEntry.addHosts(sess, hosts, facility);

	}

	@Test (expected=FacilityNotExistsException.class)
	public void addHostsWhenFacilityNotExists()throws Exception {
		System.out.println(CLASS_NAME + "addHostsWhenFacilityNotExists");

		facilitiesManagerEntry.addHosts(sess, hosts, emptyFac);
		// shouldn't find facility

	}

	@Test
	public void addHostsWithPattern()throws Exception {
		System.out.println(CLASS_NAME + "addHostsWithPattern");

		String hostname = "name[00-01]surname[99-100]cz";
		List<String> listOfHosts = new ArrayList<String>();
		listOfHosts.add(hostname);
		hostname = "local";
		listOfHosts.add(hostname);
		hosts = facilitiesManagerEntry.addHosts(sess, facility, listOfHosts);
		// test
		assertNotNull("Unable to add hosts", hosts);
		assertEquals("There should be 5 hosts in list", 5, hosts.size());

		Set<String> hostNames = new HashSet<String>();
		for (Host h: hosts) {
			hostNames.add(h.getHostname());
		}
		assertTrue("List doesn't contain host with name 'name00surname99cz'.", hostNames.contains("name00surname99cz"));
		assertTrue("List doesn't contain host with name 'name00surname100cz'.", hostNames.contains("name00surname100cz"));
		assertTrue("List doesn't contain host with name 'name01surname99cz'.", hostNames.contains("name01surname99cz"));
		assertTrue("List doesn't contain host with name 'name01surname100cz'.", hostNames.contains("name01surname100cz"));
		assertTrue("List doesn't contain host with name 'local'.", hostNames.contains("local"));
	}

	@Test(expected = WrongPatternException.class)
	public void addHostsWithWrongPattern()throws Exception {
		System.out.println(CLASS_NAME + "addHostsWithWrongPattern");

		String hostname = "name[00]-01]surname[99-100]cz";
		List<String> listOfHosts = new ArrayList<String>();
		listOfHosts.add(hostname);
		hostname = "local";
		listOfHosts.add(hostname);
		hosts = facilitiesManagerEntry.addHosts(sess, facility, listOfHosts);
	}

	@Test(expected = WrongPatternException.class)
	public void addHostsWithWrongPattern2()throws Exception {
		System.out.println(CLASS_NAME + "addHostsWithWrongPattern2");

		String hostname = "name[00-a01]surname[99-100]cz";
		List<String> listOfHosts = new ArrayList<String>();
		listOfHosts.add(hostname);
		hostname = "local";
		listOfHosts.add(hostname);
		hosts = facilitiesManagerEntry.addHosts(sess, facility, listOfHosts);
	}

	@Test(expected = WrongPatternException.class)
	public void addHostsWithWrongPattern3()throws Exception {
		System.out.println(CLASS_NAME + "addHostsWithWrongPattern3");

		String hostname = "name[01-00]surname[99-100]cz";
		List<String> listOfHosts = new ArrayList<String>();
		listOfHosts.add(hostname);
		hostname = "local";
		listOfHosts.add(hostname);
		hosts = facilitiesManagerEntry.addHosts(sess, facility, listOfHosts);
	}

	@Test
	public void getHosts()throws Exception{
		System.out.println(CLASS_NAME + "getHosts");

		createdHost = facilitiesManagerEntry.addHosts(sess, hosts, facility).get(0);
		// set this host for deletion - host is created after adding to facility !!
		hostsForDeletion.add(hosts.get(0));
		final List<Host> expectedHosts = facilitiesManagerEntry.getHosts(sess, facility);
		final Host expectedHost = expectedHosts.get(0);
		assertEquals("Created and returned host should be the same", expectedHost, createdHost);

	}


	@Test
	public void getHostsByHostname()throws Exception{
		System.out.println(CLASS_NAME + "getHostsByHostname");

		Facility secondFacility = new Facility(0, "testFacilityGetHostsByHostnname");
		secondFacility = perun.getFacilitiesManagerBl().createFacility(sess, secondFacility);

		String hostname = "sameHostNameForAllHosts";
		Host host1 = new Host(0, hostname);
		Host host2 = new Host(0, hostname);
		Host host3 = new Host(0, hostname);
		host1 = perun.getFacilitiesManagerBl().addHost(sess, host1, facility);
		host2 = perun.getFacilitiesManagerBl().addHost(sess, host2, facility);
		host3 = perun.getFacilitiesManagerBl().addHost(sess, host3, secondFacility);

		List<Host> expectedHosts = facilitiesManagerEntry.getHostsByHostname(sess, hostname);
		assertEquals("There should be 3 hosts", 3, expectedHosts.size());
		assertTrue(expectedHosts.contains(host1));
		assertTrue(expectedHosts.contains(host2));
		assertTrue(expectedHosts.contains(host3));

	}


	@Test (expected=FacilityNotExistsException.class)
	public void getHostsWhenFacilityNotExists()throws Exception{
		System.out.println(CLASS_NAME + "getHostsFacilityNotExists");

		facilitiesManagerEntry.getHosts(sess, emptyFac);
		// shouldn't find facility (facility)

	}

	@Test
	public void removeHosts()throws Exception{
		System.out.println(CLASS_NAME + "removeHosts");

		facilitiesManagerEntry.addHosts(sess, hosts, facility);
		assertEquals("Unable to create add host to facility", facilitiesManagerEntry.getHostsCount(sess, facility), 1);
		// set this host for deletion - host is created after adding to facility !!
		hostsForDeletion.add(hosts.get(0));
		facilitiesManagerEntry.removeHosts(sess, hosts, facility);
		assertEquals("Unable to remove host from facility", facilitiesManagerEntry.getHostsCount(sess, facility), 0);

	}

	@Test (expected=FacilityNotExistsException.class)
	public void removeHostsWhenFacilityNotExists()throws Exception{
		System.out.println(CLASS_NAME + "removeHostsWhenFacilityNotExists");

		facilitiesManagerEntry.removeHosts(sess, hosts, emptyFac);
		// shouldn't find facility
	}

	@Test
	public void getFacilitiesCount() throws Exception {
		System.out.println(CLASS_NAME + "getFacilitiesCount");

		int count = facilitiesManagerEntry.getFacilitiesCount(sess);
		assertTrue(count > 0);
	}

	@Test
	public void getHostsCount()throws Exception{
		System.out.println(CLASS_NAME + "getHostsCount");

		facilitiesManagerEntry.addHosts(sess, hosts, facility);
		// set this host for deletion - host is created after adding to facility !!
		hostsForDeletion.add(hosts.get(0));
		assertEquals(facilitiesManagerEntry.getHostsCount(sess, facility), 1);

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getHostsCountWhenFacilityNotExists()throws Exception{
		System.out.println(CLASS_NAME + "getHostsCountWhenFacilityNotExists");

		assertEquals(facilitiesManagerEntry.getHostsCount(sess, emptyFac), 1);
		// shouldn't find facility

	}


	@Test
	public void addAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addAdmin");

		final Member member = setUpMember(vo);
		User u = perun.getUsersManagerBl().getUserByMember(sess, member);

		facilitiesManagerEntry.addAdmin(sess, facility, u);
		final List<User> admins = facilitiesManagerEntry.getAdmins(sess, facility);

		assertNotNull(admins);
		assertTrue(admins.size() > 0);
	}

	@Test
	public void addAdminWithGroup() throws Exception {
		System.out.println(CLASS_NAME + "addAdminWithGroup");

		final Group group = new Group("testGroup", "just for testing");
		perun.getGroupsManager().createGroup(sess, vo, group);
		facilitiesManagerEntry.addAdmin(sess, facility, group);

		final List<Group> admins = facilitiesManagerEntry.getAdminGroups(sess, facility);

		assertNotNull(admins);
		assertTrue(admins.size() > 0);
		assertTrue(admins.contains(group));
	}

	@Test
	public void getAdmins() throws Exception {
		System.out.println(CLASS_NAME + "getAdmins");

		// set up first user
		final Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		facilitiesManagerEntry.addAdmin(sess, facility, user);

		// set up authorized group
		Group authorizedGroup = new Group("authorizedGroup","testovaciGroup");
		Group returnedGroup = perun.getGroupsManager().createGroup(sess, vo, authorizedGroup);
		facilitiesManagerEntry.addAdmin(sess, facility, returnedGroup);

		// set up second user
		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("Josef");
		candidate.setId(4);
		candidate.setMiddleName("");
		candidate.setLastName("Novak");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		UserExtSource userExtSource = new UserExtSource(new ExtSource(0, "testExtSource",
				"cz.metacentrum.perun.core.impl.ExtSourceInternal"), Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());

		Member member2 = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
		User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
		perun.getGroupsManager().addMember(sess, returnedGroup, member2);

		// test
		List<User> admins = facilitiesManagerEntry.getAdmins(sess, facility);
		//assertTrue("group shoud have 2 admins",admins.size() == 2);
		assertThat("facility should have 2 admins",admins.size(),is(2));
		assertTrue("our member as direct user should be admin",admins.contains(user));
		assertTrue("our member as member of admin group should be admin",admins.contains(user2));
	}

	@Test
	public void getDirectAdmins() throws Exception {
		System.out.println(CLASS_NAME + "getDirectAdmins");

		final Member member = setUpMember(vo);
		User u = perun.getUsersManagerBl().getUserByMember(sess, member);

		facilitiesManagerEntry.addAdmin(sess, facility, u);
		assertTrue(facilitiesManagerEntry.getDirectAdmins(sess, facility).contains(u));
	}

	@Test
	public void getAdminsIfNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getAdminsIfNotExist");

		final Member member = setUpMember(vo);
		User u = perun.getUsersManagerBl().getUserByMember(sess, member);

		assertTrue(facilitiesManagerEntry.getAdmins(sess, facility).isEmpty());
	}

	@Test
	public void getAdminGroups() throws Exception {
		System.out.println(CLASS_NAME + "getAdminGroups");

		final Group group = new Group("testGroup", "just for testing");
		perun.getGroupsManager().createGroup(sess, vo, group);
		facilitiesManagerEntry.addAdmin(sess, facility, group);

		assertTrue(facilitiesManagerEntry.getAdminGroups(sess, facility).contains(group));
	}

	@Test(expected=UserNotAdminException.class)
	public void removeAdminWhichNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAdminWhichNotExists");

		final Member member = setUpMember(vo);
		User u = perun.getUsersManagerBl().getUserByMember(sess, member);

		facilitiesManagerEntry.removeAdmin(sess, facility, u);
	}

	@Test
	public void removeAdmin() throws Exception {
		System.out.println(CLASS_NAME + "removeAdmin");

		final Member member = setUpMember(vo);
		User u = perun.getUsersManagerBl().getUserByMember(sess, member);

		final RichUser richUser = new RichUser(u, perun.getUsersManagerBl().getUserExtSources(sess, u));

		facilitiesManagerEntry.addAdmin(sess, facility, u);
		assertTrue(facilitiesManagerEntry.getAdmins(sess, facility).contains(u));

		facilitiesManagerEntry.removeAdmin(sess, facility, u);
		assertFalse(facilitiesManagerEntry.getAdmins(sess, facility).contains(u));
	}

	@Test
	public void removeAdminWithGroup() throws Exception {
		System.out.println(CLASS_NAME + "removeAdminWithGroup");

		final Group group = new Group("testGroup", "just for testing");
		perun.getGroupsManager().createGroup(sess, vo, group);
		facilitiesManagerEntry.addAdmin(sess, facility, group);

		facilitiesManagerEntry.removeAdmin(sess, facility, group);
		assertFalse(facilitiesManagerEntry.getAdminGroups(sess, facility).contains(group));
	}

	@Test
	public void getFacilitiesWhereUserIsAdmin() throws Exception {
		System.out.println(CLASS_NAME + "getFacilitiesWhereUserIsAdmin");

		final Member member = setUpMember(vo);
		User u = perun.getUsersManagerBl().getUserByMember(sess, member);

		facilitiesManagerEntry.addAdmin(sess, facility, u);

		List<Facility> facilities = facilitiesManagerEntry.getFacilitiesWhereUserIsAdmin(sess, u);
		assertNotNull(facilities);
		assertTrue(facilities.contains(facility));
	}

	@Test
	public void getFacilitiesWhereUserIsNotAdminButHisGroupIs() throws Exception {
		System.out.println(CLASS_NAME + "getFacilitiesWhereUserIsNotAdminButHisGroupIs");

		final Member member = setUpMember(vo);
		User u = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);

		facilitiesManagerEntry.addAdmin(sess, facility, group);

		List<Facility> facilities = facilitiesManagerEntry.getFacilitiesWhereUserIsAdmin(sess, u);
		assertNotNull(facilities);
		assertTrue(facilities.contains(facility));
	}

	@Test
	public void copyManagers() throws Exception {
		System.out.println(CLASS_NAME + "copyManagers");

		// add user as admin in facility
		final Member member = setUpMember(vo);
		User u = perun.getUsersManagerBl().getUserByMember(sess, member);
		facilitiesManagerEntry.addAdmin(sess, facility, u);

		// set up second facility
		Facility newFacility = new Facility();
		newFacility.setName("FacilitiesManagerTestSecondFacility");
		Facility secondFacility = perun.getFacilitiesManager().createFacility(sess, newFacility);

		// copy admins
		facilitiesManagerEntry.copyManagers(sess, facility, secondFacility);

		// check
		List<User> admins = facilitiesManagerEntry.getAdmins(sess, secondFacility);
		assertNotNull(admins);
		assertTrue(admins.contains(u));
	}

	@Test
	public void copyOwners() throws Exception {
		System.out.println(CLASS_NAME + "copyOwners");

		// set up second facility
		Facility newFacility = new Facility();
		newFacility.setName("FacilitiesManagerTestSecondFacility");
		Facility secondFacility = perun.getFacilitiesManager().createFacility(sess, newFacility);

		// copy owners
		facilitiesManagerEntry.copyOwners(sess, facility, secondFacility);

		// check
		List<Owner> owners = facilitiesManagerEntry.getOwners(sess, secondFacility);
		assertNotNull(owners);
		assertTrue(owners.contains(owner));
	}

	@Test
	public void copyAttributes() throws Exception {
		System.out.println(CLASS_NAME + "copyAttributes");

		// set up second facility
		Facility newFacility = new Facility();
		newFacility.setName("FacilitiesManagerTestSecondFacility");
		newFacility.setDescription("TestSecondFacilityDescriptionText");
		Facility secondFacility = perun.getFacilitiesManager().createFacility(sess, newFacility);

		// add first attribute to source
		Attribute firstAttribute = setUpAttribute1();
		perun.getAttributesManager().setAttribute(sess, facility, firstAttribute);

		// add second attribute to both
		Attribute secondAttribute = setUpAttribute2();
		perun.getAttributesManager().setAttribute(sess, facility, secondAttribute);
		perun.getAttributesManager().setAttribute(sess, secondFacility, secondAttribute);

		// add third attribute to destination
		Attribute thirdAttribute = setUpAttribute3();
		perun.getAttributesManager().setAttribute(sess, secondFacility, thirdAttribute);

		// copy
		facilitiesManagerEntry.copyAttributes(sess, facility, secondFacility);

		// tests
		List<Attribute> destinationAttributes = perun.getAttributesManager().getAttributes(sess, secondFacility);
		assertNotNull(destinationAttributes);
		assertTrue((destinationAttributes.size() - perun.getAttributesManager().getAttributes(sess, facility).size()) == 1);
		assertTrue(destinationAttributes.contains(firstAttribute));
		assertTrue(destinationAttributes.contains(secondAttribute));
		assertTrue(destinationAttributes.contains(thirdAttribute));
	}

	// FACILITY CONTACTS TEST METHODS

	@Test
	public void addFacilityContactForUser() throws Exception {
		System.out.println(CLASS_NAME + "addFacilityContactForUser");

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);

		String contactGroupName = "testContactGroup01";
		ContactGroup cg = new ContactGroup(contactGroupName, facility);
		RichUser richUser = new RichUser(user, null);

		List<RichUser> users = perun.getUsersManagerBl().convertUsersToRichUsersWithAttributes(sess, Arrays.asList(richUser), getMandatoryAttrs());
		cg.setUsers(new ArrayList<>(users));
		facilitiesManagerEntry.addFacilityContact(sess, cg);
		perun.getFacilitiesManagerBl().checkFacilityContactExists(sess, facility, contactGroupName, user);

		List<ContactGroup> cgs = facilitiesManagerEntry.getFacilityContactGroups(sess, user);
		assertTrue(cg.equals(cgs.get(0)));
		assertEquals(user.getId(), cgs.get(0).getUsers().get(0).getId());
	}

	@Test
	public void addFacilityContactForGroup() throws Exception {
		System.out.println(CLASS_NAME + "addFacilityContactForGroup");

		Member member = setUpMember(vo);
		Group group = setUpGroup(vo, member);

		String contactGroupName = "testContactGroup01";
		ContactGroup cg = new ContactGroup(contactGroupName, facility);
		cg.setGroups(new ArrayList<>(Arrays.asList(group)));
		facilitiesManagerEntry.addFacilityContact(sess, cg);
		perun.getFacilitiesManagerBl().checkFacilityContactExists(sess, facility, contactGroupName, group);

		List<ContactGroup> cgs = facilitiesManagerEntry.getFacilityContactGroups(sess, group);
		assertTrue(cg.equalsGroup(cgs.get(0)));
		assertEquals(group.getId(), cgs.get(0).getGroups().get(0).getId());
	}

	@Test
	public void addFacilityContactForOwner() throws Exception {
		System.out.println(CLASS_NAME + "addFacilityContactForOwner");

		Member member = setUpMember(vo);

		String contactGroupName = "testContactGroup01";
		ContactGroup cg = new ContactGroup(contactGroupName, facility);
		cg.setOwners(new ArrayList<>(Arrays.asList(owner)));
		facilitiesManagerEntry.addFacilityContact(sess, cg);
		perun.getFacilitiesManagerBl().checkFacilityContactExists(sess, facility, contactGroupName, owner);

		List<ContactGroup> cgs = facilitiesManagerEntry.getFacilityContactGroups(sess, owner);
		assertTrue(cg.equals(cgs.get(0)));
		assertEquals(owner.getId(), cgs.get(0).getOwners().get(0).getId());
	}

	@Test
	public void addFacilityContactForAll1() throws Exception {
		System.out.println(CLASS_NAME + "addFacilityContactForAll1");

		Member member = setUpMember(vo);
		Group group = setUpGroup(vo, member);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);

		String contactGroupName = "testContactGroup01";
		ContactGroup cg = new ContactGroup(contactGroupName, facility);
		cg.setOwners(new ArrayList<>(Arrays.asList(owner)));
		cg.setGroups(new ArrayList<>(Arrays.asList(group)));
		RichUser richUser = new RichUser(user, null);

		List<RichUser> users = perun.getUsersManagerBl().convertUsersToRichUsersWithAttributes(sess, Arrays.asList(richUser), getMandatoryAttrs());
		cg.setUsers(new ArrayList<>(users));
		facilitiesManagerEntry.addFacilityContact(sess, cg);

		ContactGroup cgReturned = facilitiesManagerEntry.getFacilityContactGroup(sess, facility, contactGroupName);

		assertTrue(cg.equals(cgReturned));

		assertEquals(owner.getId(), cgReturned.getOwners().get(0).getId());
		assertEquals(group.getId(), cgReturned.getGroups().get(0).getId());
		assertEquals(user.getId(), cgReturned.getUsers().get(0).getId());
	}

	@Test
	public void addFacilityContactForAll2() throws Exception {
		System.out.println(CLASS_NAME + "addFacilityContactForAll2");

		Member member = setUpMember(vo);
		Group group = setUpGroup(vo, member);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);

		String contactGroupName = "testContactGroup01";
		ContactGroup cg = new ContactGroup(contactGroupName, facility);
		cg.setOwners(new ArrayList<>(Arrays.asList(owner)));
		cg.setGroups(new ArrayList<>(Arrays.asList(group)));
		RichUser richUser = new RichUser(user, null);

		List<RichUser> users = perun.getUsersManagerBl().convertUsersToRichUsersWithAttributes(sess, Arrays.asList(richUser), getMandatoryAttrs());
		cg.setUsers(new ArrayList<>(users));
		facilitiesManagerEntry.addFacilityContact(sess, cg);

		List<ContactGroup> cgs = facilitiesManagerEntry.getFacilityContactGroups(sess, facility);

		assertTrue(cg.equals(cgs.get(0)));

		assertEquals(owner.getId(), cgs.get(0).getOwners().get(0).getId());
		assertEquals(group.getId(), cgs.get(0).getGroups().get(0).getId());
		assertEquals(user.getId(), cgs.get(0).getUsers().get(0).getId());
	}

	@Test
	public void getAllContactGroupNames() throws Exception {
		System.out.println(CLASS_NAME + "getAllContactGroupNames");

		String contactGroupName1 = "testContactGroup01";
		String contactGroupName2 = "testContactGroup02";
		String contactGroupName3 = "testContactGroup03";
		ContactGroup cg1 = new ContactGroup(contactGroupName1, facility);
		ContactGroup cg2 = new ContactGroup(contactGroupName2, facility);
		ContactGroup cg3 = new ContactGroup(contactGroupName3, facility);
		cg1.setOwners(new ArrayList<>(Arrays.asList(owner)));
		cg2.setOwners(new ArrayList<>(Arrays.asList(owner)));
		cg3.setOwners(new ArrayList<>(Arrays.asList(owner)));
		List<ContactGroup> cgs = new ArrayList<>(Arrays.asList(cg1, cg2, cg3));

		facilitiesManagerEntry.addFacilityContacts(sess, cgs);

		List<String> cgnames = facilitiesManagerEntry.getAllContactGroupNames(sess);
		assertTrue(cgnames.contains(contactGroupName1));
		assertTrue(cgnames.contains(contactGroupName2));
		assertTrue(cgnames.contains(contactGroupName3));
	}

	@Test
	public void removeFacilityContactForUser() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityContactForUser");

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);

		String contactGroupName = "testContactGroup01";
		ContactGroup cg = new ContactGroup(contactGroupName, facility);
		RichUser richUser = new RichUser(user, null);

		List<RichUser> users = perun.getUsersManagerBl().convertUsersToRichUsersWithAttributes(sess, Arrays.asList(richUser), getMandatoryAttrs());
		cg.setUsers(new ArrayList<>(users));
		facilitiesManagerEntry.addFacilityContact(sess, cg);
		perun.getFacilitiesManagerBl().checkFacilityContactExists(sess, facility, contactGroupName, user);

		List<ContactGroup> cgs = facilitiesManagerEntry.getFacilityContactGroups(sess, user);
		assertTrue(cg.equals(cgs.get(0)));
		assertEquals(user.getId(), cgs.get(0).getUsers().get(0).getId());

		facilitiesManagerEntry.removeFacilityContact(sess, cg);
		cgs = facilitiesManagerEntry.getFacilityContactGroups(sess, user);
		assertTrue(cgs.isEmpty());
	}

	@Test
	public void removeFacilityContactForGroup() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityContactForGroup");

		Member member = setUpMember(vo);
		Group group = setUpGroup(vo, member);

		String contactGroupName = "testContactGroup01";
		ContactGroup cg = new ContactGroup(contactGroupName, facility);
		cg.setGroups(new ArrayList<>(Arrays.asList(group)));
		facilitiesManagerEntry.addFacilityContact(sess, cg);
		perun.getFacilitiesManagerBl().checkFacilityContactExists(sess, facility, contactGroupName, group);

		List<ContactGroup> cgs = facilitiesManagerEntry.getFacilityContactGroups(sess, group);
		assertTrue(cg.equalsGroup(cgs.get(0)));
		assertEquals(group.getId(), cgs.get(0).getGroups().get(0).getId());

		facilitiesManagerEntry.removeFacilityContact(sess, cg);
		cgs = facilitiesManagerEntry.getFacilityContactGroups(sess, group);
		assertTrue(cgs.isEmpty());
	}

	@Test
	public void removeFacilityContactForOwner() throws Exception {
		System.out.println(CLASS_NAME + "removeFacilityContactForOwner");

		Member member = setUpMember(vo);

		String contactGroupName = "testContactGroup01";
		ContactGroup cg = new ContactGroup(contactGroupName, facility);
		cg.setOwners(new ArrayList<>(Arrays.asList(owner)));
		facilitiesManagerEntry.addFacilityContact(sess, cg);
		perun.getFacilitiesManagerBl().checkFacilityContactExists(sess, facility, contactGroupName, owner);

		List<ContactGroup> cgs = facilitiesManagerEntry.getFacilityContactGroups(sess, owner);
		assertTrue(cg.equals(cgs.get(0)));
		assertEquals(owner.getId(), cgs.get(0).getOwners().get(0).getId());

		facilitiesManagerEntry.removeFacilityContact(sess, cg);
		cgs = facilitiesManagerEntry.getFacilityContactGroups(sess, owner);
		assertTrue(cgs.isEmpty());
	}

	@Test
	public void removeAllFacilityContacts() throws Exception {
		System.out.println(CLASS_NAME + "removeAllFacilityContacts");

		String contactGroupName1 = "testContactGroup01";
		String contactGroupName2 = "testContactGroup02";
		String contactGroupName3 = "testContactGroup03";
		ContactGroup cg1 = new ContactGroup(contactGroupName1, facility);
		ContactGroup cg2 = new ContactGroup(contactGroupName2, facility);
		ContactGroup cg3 = new ContactGroup(contactGroupName3, facility);
		cg1.setOwners(new ArrayList<>(Arrays.asList(owner)));
		cg2.setOwners(new ArrayList<>(Arrays.asList(owner)));
		cg3.setOwners(new ArrayList<>(Arrays.asList(owner)));
		List<ContactGroup> cgs = new ArrayList<>(Arrays.asList(cg1, cg2, cg3));

		facilitiesManagerEntry.addFacilityContacts(sess, cgs);

		List<ContactGroup> cgnames = facilitiesManagerEntry.getFacilityContactGroups(sess, facility);
		assertTrue(cgnames.contains(cg1));
		assertTrue(cgnames.contains(cg2));
		assertTrue(cgnames.contains(cg3));

		facilitiesManagerEntry.removeFacilityContacts(sess, cgs);

		cgnames = facilitiesManagerEntry.getFacilityContactGroups(sess, facility);
		assertTrue(cgnames.isEmpty());
	}

	@Test
	public void getAssignedSecurityTeams() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedSecurityTeams");

		List<SecurityTeam> expected = new ArrayList<>();
		expected.add(setUpSecurityTeam0());
		expected.add(setUpSecurityTeam1());
		setUpAssignSecurityTeams(facility, expected);
		setUpSecurityTeam2();
		List<SecurityTeam> actual = facilitiesManagerEntry.getAssignedSecurityTeams(sess, facility);
		Collections.sort(expected);
		Collections.sort(actual);
		assertEquals(expected, actual);
	}

	@Test
	public void getAssignedSecurityTeamsEmpty() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedSecurityTeamsEmpty");

		List<SecurityTeam> expected = new ArrayList<>();
		setUpAssignSecurityTeams(facility, expected);
		setUpSecurityTeam0();
		setUpSecurityTeam1();
		setUpSecurityTeam2();
		List<SecurityTeam> actual = facilitiesManagerEntry.getAssignedSecurityTeams(sess, facility);
		Collections.sort(expected);
		Collections.sort(actual);
		assertEquals(expected, actual);
	}

	@Test(expected = FacilityNotExistsException.class)
	public void getAssignedSecurityTeamsFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedSecurityTeamsFacilityNotExists");
		setUpSecurityTeam0();
		setUpSecurityTeam1();
		// should throw an exception
		facilitiesManagerEntry.getAssignedSecurityTeams(sess, new Facility(0, "Name"));
	}

	@Test
	public void assignSecurityTeam() throws Exception {
		System.out.println(CLASS_NAME + "assignSecurityTeam");

		SecurityTeam st0 = setUpSecurityTeam0();
		setUpSecurityTeam1();
		facilitiesManagerEntry.assignSecurityTeam(sess, facility, st0);

		List<SecurityTeam> actual = facilitiesManagerEntry.getAssignedSecurityTeams(sess, facility);
		assertTrue("Facility should have only 1 security team.", actual.size() == 1);
		assertTrue("Expected security team is not assigned to facility.", actual.contains(st0));
	}

	@Test(expected = FacilityNotExistsException.class)
	public void assignSecurityTeamFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "assignSecurityTeamFacilityNotExists");
		SecurityTeam st0 = setUpSecurityTeam0();
		setUpSecurityTeam1();
		// should throw an exception
		facilitiesManagerEntry.assignSecurityTeam(sess, new Facility(0, "Name"), st0);
	}

	@Test(expected = SecurityTeamNotExistsException.class)
	public void assignSecurityTeamSecurityTeamNotExists() throws Exception {
		System.out.println(CLASS_NAME + "assignSecurityTeamSecurityTeamNotExists");
		// should throw an exception
		facilitiesManagerEntry.assignSecurityTeam(sess, facility, new SecurityTeam(0, "name", "dsc"));
	}

	@Test(expected = SecurityTeamAlreadyAssignedException.class)
	public void assignSecurityTeamAlreadyAssigned() throws Exception {
		System.out.println(CLASS_NAME + "assignSecurityTeamAlreadyAssigned");

		SecurityTeam st0 = setUpSecurityTeam0();
		List<SecurityTeam> expected = new ArrayList<>();
		expected.add(st0);
		expected.add(setUpSecurityTeam1());
		setUpAssignSecurityTeams(facility, expected);
		setUpSecurityTeam2();
		// should throw an exception
		facilitiesManagerEntry.assignSecurityTeam(sess, facility, st0);
	}

	@Test
	public void removeSecurityTeam() throws Exception {
		System.out.println(CLASS_NAME + "removeSecurityTeam");

		SecurityTeam st0 = setUpSecurityTeam0();
		SecurityTeam st1 = setUpSecurityTeam1();
		List<SecurityTeam> expected = new ArrayList<>();
		expected.add(st0);
		expected.add(st1);
		setUpAssignSecurityTeams(facility, expected);
		setUpSecurityTeam2();
		facilitiesManagerEntry.removeSecurityTeam(sess, facility, st0);
		expected.remove(st0);

		List<SecurityTeam> actual = facilitiesManagerEntry.getAssignedSecurityTeams(sess, facility);
		assertTrue("Facility should have only 1 security team.", actual.size() == 1);
		assertTrue("Facility shouldn't have security team 0 assigned.", !actual.contains(st0));
		assertTrue("Facility should have security team 1 assigned.", actual.contains(st1));

	}

	@Test(expected = FacilityNotExistsException.class)
	public void removeSecurityTeamFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeSecurityTeamFacilityNotExists");

		SecurityTeam st0 = setUpSecurityTeam0();
		setUpSecurityTeam1();
		// should throw an exception
		facilitiesManagerEntry.removeSecurityTeam(sess, new Facility(0, "Name"), st0);
	}

	@Test(expected = SecurityTeamNotExistsException.class)
	public void removeSecurityTeamSecurityTeamNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeSecurityTeamSecurityTeamNotExists");

		List<SecurityTeam> expected = new ArrayList<>();
		expected.add(setUpSecurityTeam0());
		expected.add(setUpSecurityTeam1());
		setUpAssignSecurityTeams(facility, expected);
		setUpSecurityTeam2();
		// should throw an exception
		facilitiesManagerEntry.removeSecurityTeam(sess, facility, new SecurityTeam(0, "name", "dsc"));
	}

	@Test(expected = SecurityTeamNotAssignedException.class)
	public void removeSecurityTeamNotAssigned() throws Exception {
		System.out.println(CLASS_NAME + "removeSecurityTeamNotAssigned");

		List<SecurityTeam> expected = new ArrayList<>();
		expected.add(setUpSecurityTeam0());
		expected.add(setUpSecurityTeam1());
		setUpAssignSecurityTeams(facility, expected);
		// should throw an exception
		facilitiesManagerEntry.removeSecurityTeam(sess, facility, setUpSecurityTeam2());
	}

	@Test
	public void setBan() throws Exception {
		System.out.println(CLASS_NAME + "setBan");
		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setUserId(user.getId());
		banOnFacility.setFacilityId(facility.getId());
		banOnFacility.setDescription("Popisek");
		banOnFacility.setValidityTo(new Date());

		BanOnFacility returnedBan = facilitiesManagerEntry.setBan(sess, banOnFacility);
		banOnFacility.setId(returnedBan.getId());
		assertEquals(banOnFacility, returnedBan);
	}

	@Test
	public void getBanById() throws Exception {
		System.out.println(CLASS_NAME + "getBanById");
		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setUserId(user.getId());
		banOnFacility.setFacilityId(facility.getId());
		banOnFacility.setDescription("Popisek");
		banOnFacility.setValidityTo(new Date());
		banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);

		BanOnFacility returnedBan = facilitiesManagerEntry.getBanById(sess, banOnFacility.getId());
		assertEquals(banOnFacility, returnedBan);
	}

	@Test
	public void getBan() throws Exception {
		System.out.println(CLASS_NAME + "getBan");
		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setUserId(user.getId());
		banOnFacility.setFacilityId(facility.getId());
		banOnFacility.setDescription("Popisek");
		banOnFacility.setValidityTo(new Date());
		banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);

		BanOnFacility returnedBan = facilitiesManagerEntry.getBan(sess, banOnFacility.getUserId(), banOnFacility.getFacilityId());
		assertEquals(banOnFacility, returnedBan);
	}

	@Test
	public void getBansForUser() throws Exception {
		System.out.println(CLASS_NAME + "getBansForUser");
		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setUserId(user.getId());
		banOnFacility.setFacilityId(facility.getId());
		banOnFacility.setDescription("Popisek");
		banOnFacility.setValidityTo(new Date());
		banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);

		List<BanOnFacility> returnedBans = facilitiesManagerEntry.getBansForUser(sess, banOnFacility.getUserId());
		assertEquals(banOnFacility, returnedBans.get(0));
	}

	@Test
	public void getBansForFacility() throws Exception {
		System.out.println(CLASS_NAME + "getBansForFacility");
		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setUserId(user.getId());
		banOnFacility.setFacilityId(facility.getId());
		banOnFacility.setDescription("Popisek");
		banOnFacility.setValidityTo(new Date());
		banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);

		List<BanOnFacility> returnedBans = facilitiesManagerEntry.getBansForFacility(sess, banOnFacility.getFacilityId());
		assertEquals(banOnFacility, returnedBans.get(0));
	}

	@Test
	public void updateBan() throws Exception {
		System.out.println(CLASS_NAME + "updateBan");
		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setUserId(user.getId());
		banOnFacility.setFacilityId(facility.getId());
		banOnFacility.setDescription("Popisek");
		banOnFacility.setValidityTo(new Date());
		banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);
		banOnFacility.setDescription("New description");
		banOnFacility.setValidityTo(new Date(banOnFacility.getValidityTo().getTime() + 1000000));
		facilitiesManagerEntry.updateBan(sess, banOnFacility);

		BanOnFacility returnedBan = facilitiesManagerEntry.getBanById(sess, banOnFacility.getId());
		assertEquals(banOnFacility, returnedBan);
	}

	@Test
	public void removeBanById() throws Exception {
		System.out.println(CLASS_NAME + "removeBan");
		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setUserId(user.getId());
		banOnFacility.setFacilityId(facility.getId());
		banOnFacility.setDescription("Popisek");
		banOnFacility.setValidityTo(new Date());
		banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);

		List<BanOnFacility> bansOnFacility = facilitiesManagerEntry.getBansForFacility(sess, banOnFacility.getFacilityId());
		assertTrue(bansOnFacility.size() == 1);

		perun.getFacilitiesManagerBl().removeBan(sess, banOnFacility.getId());

		bansOnFacility = facilitiesManagerEntry.getBansForFacility(sess, banOnFacility.getFacilityId());
		assertTrue(bansOnFacility.isEmpty());
	}

	@Test
	public void removeBan() throws Exception {
		System.out.println(CLASS_NAME + "removeBan");
		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setUserId(user.getId());
		banOnFacility.setFacilityId(facility.getId());
		banOnFacility.setDescription("Popisek");
		banOnFacility.setValidityTo(new Date());
		banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);

		List<BanOnFacility> bansOnFacility = facilitiesManagerEntry.getBansForFacility(sess, banOnFacility.getFacilityId());
		assertTrue(bansOnFacility.size() == 1);

		perun.getFacilitiesManagerBl().removeBan(sess, banOnFacility.getUserId(), banOnFacility.getFacilityId());

		bansOnFacility = facilitiesManagerEntry.getBansForFacility(sess, banOnFacility.getFacilityId());
		assertTrue(bansOnFacility.isEmpty());
	}

	@Test
	public void removeExpiredBansIfExist() throws Exception {
		System.out.println(CLASS_NAME + "removeExpiredBansIfExist");
		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setUserId(user.getId());
		banOnFacility.setFacilityId(facility.getId());
		banOnFacility.setDescription("Popisek");
		Date now = new Date();
		Date yesterday = new Date(now.getTime() - (1000 * 60 * 60 * 24));
		banOnFacility.setValidityTo(yesterday);
		banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);

		List<BanOnFacility> bansOnFacility = facilitiesManagerEntry.getBansForFacility(sess, banOnFacility.getFacilityId());
		assertTrue(bansOnFacility.size() == 1);

		perun.getFacilitiesManagerBl().removeAllExpiredBansOnFacilities(sess);

		bansOnFacility = facilitiesManagerEntry.getBansForFacility(sess, banOnFacility.getFacilityId());
		assertTrue(bansOnFacility.isEmpty());
	}

	@Test
	public void removeExpiredBansIfNotExist() throws Exception {
		System.out.println(CLASS_NAME + "removeExpiredBansIfNotExist");
		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		BanOnFacility banOnFacility = new BanOnFacility();
		banOnFacility.setUserId(user.getId());
		banOnFacility.setFacilityId(facility.getId());
		banOnFacility.setDescription("Popisek");
		Date now = new Date();
		Date tommorow = new Date(now.getTime() + (1000 * 60 * 60 * 24));
		banOnFacility.setValidityTo(tommorow);
		banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);

		List<BanOnFacility> bansOnFacility = facilitiesManagerEntry.getBansForFacility(sess, banOnFacility.getFacilityId());
		assertTrue(bansOnFacility.size() == 1);

		perun.getFacilitiesManagerBl().removeAllExpiredBansOnFacilities(sess);

		bansOnFacility = facilitiesManagerEntry.getBansForFacility(sess, banOnFacility.getFacilityId());
		assertTrue(bansOnFacility.size() == 1);
	}

	@Test
	public void addHostAndDestinationSameNameSameAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addHostAndDestinationSameNameSameAdmin");

		// Initialize host, destination and service
	 	String hostName = "TestHost";
		Host hostOne = new Host(0, hostName);
		Destination destination = new Destination(0, hostName, Destination.DESTINATIONHOSTTYPE);
		Service service = new Service(0, "testService", null);
		ServicesManager servicesManagerEntry = perun.getServicesManager();
		service = servicesManagerEntry.createService(sess, service);
		// Creates second facility
		Facility secondFacility = new Facility(0, "TestSecondFacility", "TestDescriptionText");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, secondFacility));
		// Set up two members
		Member memberOne = setUpMember(vo);

		// Set userOne as admin for both facilities
		User userOne = perun.getUsersManagerBl().getUserByMember(sess, memberOne);
		facilitiesManagerEntry.addAdmin(sess, facility, userOne);
		facilitiesManagerEntry.addAdmin(sess, secondFacility, userOne);
		// Sets userOne as actor in this test with role facility admin for facility
		List<PerunBean> list = new ArrayList<PerunBean>();
		list.add(facility);
		list.add(secondFacility);
		AuthzRoles authzRoles = new AuthzRoles(Role.FACILITYADMIN, list);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userOne);

		// Adds host to facility
		facilitiesManagerEntry.addHost(sess, hostOne, facility);
		assertTrue(facilitiesManagerEntry.getHosts(sess, facility).size() == 1);
		// Adds destination with same name as host to facility
		servicesManagerEntry.addDestination(sess, service, facility, destination);
		assertTrue(servicesManagerEntry.getDestinations(sess, service, facility).size() == 1);
		// Adds same host to second facility
		facilitiesManagerEntry.addHost(sess, hostOne, secondFacility);
		assertTrue(facilitiesManagerEntry.getHosts(sess, secondFacility).size() == 1);
		// Adds destination with same name as host to secondFacility
		servicesManagerEntry.addDestination(sess, service, secondFacility, destination);
		assertTrue(servicesManagerEntry.getDestinations(sess, service, secondFacility).size() == 1);
	}

	@Test(expected = PrivilegeException.class)
	public void addHostSameHostDifferentAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addHostSameHostDifferentAdmin");

		// Initialize host
		Host host = new Host(0, "testHost");
		// Creates second facility
		Facility secondFacility = new Facility(0, "TestSecondFacility", "TestDescriptionText");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, secondFacility));
		// Set up two members
		Member memberOne = setUpMember(vo);
		Member memberTwo = setUpMember(vo);

		// Set users as admins of different facilities
		User userOne = perun.getUsersManagerBl().getUserByMember(sess, memberOne);
		facilitiesManagerEntry.addAdmin(sess, facility, userOne);
		User userTwo = perun.getUsersManagerBl().getUserByMember(sess, memberTwo);
		facilitiesManagerEntry.addAdmin(sess, secondFacility, userTwo);

		// Sets userOne as actor in this test with role facility admin for facility
		AuthzRoles authzRoles = new AuthzRoles(Role.FACILITYADMIN, facility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userOne);
		// Adds host to facility
		facilitiesManagerEntry.addHost(sess, host, facility);
		assertTrue(facilitiesManagerEntry.getHosts(sess, facility).size() == 1);

		// Change actor in this test to userTwo
		authzRoles = new AuthzRoles(Role.FACILITYADMIN, secondFacility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userTwo);
		// Adds same host to secondFacility with different admin -> should throw exception
		facilitiesManagerEntry.addHost(sess, host, secondFacility);
	}

	@Test(expected = PrivilegeException.class)
	public void addHostSameDestinationDifferentAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addHostSameDestinationDifferentAdmin");

		// Initialize host, destination and service
	 	String hostName = "TestHost";
		Host host = new Host(0, hostName);
		Destination destination = new Destination(0, hostName, Destination.DESTINATIONHOSTTYPE);
		Service service = new Service(0, "testService", null);
		ServicesManager servicesManagerEntry = perun.getServicesManager();
		service = servicesManagerEntry.createService(sess, service);

		// Creates second facility
		Facility secondFacility = new Facility(0, "TestSecondFacility", "TestDescriptionText");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, secondFacility));
		// Set up two members
		Member memberOne = setUpMember(vo);
		Member memberTwo = setUpMember(vo);

		// Set users as admins of different facilities
		User userOne = perun.getUsersManagerBl().getUserByMember(sess, memberOne);
		facilitiesManagerEntry.addAdmin(sess, facility, userOne);
		User userTwo = perun.getUsersManagerBl().getUserByMember(sess, memberTwo);
		facilitiesManagerEntry.addAdmin(sess, secondFacility, userTwo);

		// Sets userOne as actor in this test with role facility admin for facility
		AuthzRoles authzRoles = new AuthzRoles(Role.FACILITYADMIN, facility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userOne);
		// Adds destination to facility
		servicesManagerEntry.addDestination(sess, service, facility, destination);
		assertTrue(servicesManagerEntry.getDestinations(sess, service, facility).size() == 1);

		// Change actor in this test to userTwo
		authzRoles = new AuthzRoles(Role.FACILITYADMIN, secondFacility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userTwo);
		// Adds same host as destination to secondFacility with different admin -> should throw exception
		facilitiesManagerEntry.addHost(sess, host, secondFacility);
	}

	@Test(expected = PrivilegeException.class)
	public void addHostsStringsSameHostsDifferentAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addHostsStringsSameHostsDifferentAdmin");
		// Sets list of hostnames
		String hostName = "testHostOne";
		List<String> listOfHosts = new ArrayList<String>();
		listOfHosts.add(hostName);
		hostName = "testHostTwo";
		listOfHosts.add(hostName);
		// Set up two members
		Member memberOne = setUpMember(vo);
		Member memberTwo = setUpMember(vo);
		// Creates second facility
		Facility secondFacility = new Facility(0, "TestSecondFacility", "TestDescriptionText");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, secondFacility));

		// Set users as admins of different facilities
		User userOne = perun.getUsersManagerBl().getUserByMember(sess, memberOne);
		facilitiesManagerEntry.addAdmin(sess, facility, userOne);
		User userTwo = perun.getUsersManagerBl().getUserByMember(sess, memberTwo);
		facilitiesManagerEntry.addAdmin(sess, secondFacility, userTwo);

		// Sets userOne as actor in this test with role facility admin for facility
		AuthzRoles authzRoles = new AuthzRoles(Role.FACILITYADMIN, facility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userOne);
		// Adds hosts to facility
		facilitiesManagerEntry.addHosts(sess, facility, listOfHosts);

		// Change actor in this test to userTwo
		authzRoles = new AuthzRoles(Role.FACILITYADMIN, secondFacility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userTwo);
		// Adds same hosts to secondFacility with different admin -> should throw exception
		facilitiesManagerEntry.addHosts(sess, secondFacility, listOfHosts);
	}

	@Test(expected = PrivilegeException.class)
	public void addHostsStringsSameDestinationDifferentAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addHostsStringsSameDestinationDifferentAdmin");

	 	// Sets list of hostnames
		String hostName = "testHostOne";
		List<String> listOfHosts = new ArrayList<String>();
		listOfHosts.add(hostName);
		hostName = "testHostTwo";
		listOfHosts.add(hostName);
		// Initialize destination and service
		Destination destination = new Destination(0, hostName, Destination.DESTINATIONHOSTTYPE);
		Service service = new Service(0, "testService", null);
		ServicesManager servicesManagerEntry = perun.getServicesManager();
		service = servicesManagerEntry.createService(sess, service);

		// Creates second facility
		Facility secondFacility = new Facility(0, "TestSecondFacility", "TestDescriptionText");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, secondFacility));
		// Set up two members
		Member memberOne = setUpMember(vo);
		Member memberTwo = setUpMember(vo);

		// Set users as admins of different facilities
		User userOne = perun.getUsersManagerBl().getUserByMember(sess, memberOne);
		facilitiesManagerEntry.addAdmin(sess, facility, userOne);
		User userTwo = perun.getUsersManagerBl().getUserByMember(sess, memberTwo);
		facilitiesManagerEntry.addAdmin(sess, secondFacility, userTwo);

		// Sets userOne as actor in this test with role facility admin for facility
		AuthzRoles authzRoles = new AuthzRoles(Role.FACILITYADMIN, facility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userOne);
		// Adds destination to facility
		servicesManagerEntry.addDestination(sess, service, facility, destination);
		assertTrue(servicesManagerEntry.getDestinations(sess, service, facility).size() == 1);

		// Change actor in this test to userTwo
		authzRoles = new AuthzRoles(Role.FACILITYADMIN, secondFacility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userTwo);
		// Adds same host as destination to secondFacility with different admin -> should throw exception
		facilitiesManagerEntry.addHosts(sess, secondFacility, listOfHosts);
	}

	@Test(expected = PrivilegeException.class)
	public void addHostsSameHostsDifferentAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addHostsSameHostsDifferentAdmin");
		// Sets list of hosts
		List<Host> listOfHosts = new ArrayList<Host>();
		Host testHost = new Host(0, "testHostOne");
		listOfHosts.add(testHost);
		testHost = new Host(0, "testHostTwo");
		listOfHosts.add(testHost);
		// Set up two members
		Member memberOne = setUpMember(vo);
		Member memberTwo = setUpMember(vo);
		// Creates second facility
		Facility secondFacility = new Facility(0, "TestSecondFacility", "TestDescriptionText");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, secondFacility));

		// Set users as admins of different facilities
		User userOne = perun.getUsersManagerBl().getUserByMember(sess, memberOne);
		facilitiesManagerEntry.addAdmin(sess, facility, userOne);
		User userTwo = perun.getUsersManagerBl().getUserByMember(sess, memberTwo);
		facilitiesManagerEntry.addAdmin(sess, secondFacility, userTwo);

		// Sets userOne as actor in this test with role facility admin for facility
		AuthzRoles authzRoles = new AuthzRoles(Role.FACILITYADMIN, facility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userOne);
		// Adds hosts to facility
		facilitiesManagerEntry.addHosts(sess, listOfHosts, facility);

		// Change actor in this test to userTwo
		authzRoles = new AuthzRoles(Role.FACILITYADMIN, secondFacility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userTwo);
		// Adds same hosts to secondFacility with different admin -> should throw exception
		facilitiesManagerEntry.addHosts(sess, listOfHosts, secondFacility);
	}

	@Test(expected = PrivilegeException.class)
	public void addHostsSameDestinationDifferentAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addHostsStringsSameDestinationDifferentAdmin");

	 	// Sets list of hosts
		List<Host> listOfHosts = new ArrayList<Host>();
		Host testHost = new Host(0, "testHostOne");
		listOfHosts.add(testHost);
		String hostName = "testHostTwo";
		testHost = new Host(0, hostName);
		listOfHosts.add(testHost);
		// Initialize destination and service
		Destination destination = new Destination(0, hostName, Destination.DESTINATIONHOSTTYPE);
		Service service = new Service(0, "testService", null);
		ServicesManager servicesManagerEntry = perun.getServicesManager();
		service = servicesManagerEntry.createService(sess, service);

		// Creates second facility
		Facility secondFacility = new Facility(0, "TestSecondFacility", "TestDescriptionText");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, secondFacility));
		// Set up two members
		Member memberOne = setUpMember(vo);
		Member memberTwo = setUpMember(vo);

		// Set users as admins of different facilities
		User userOne = perun.getUsersManagerBl().getUserByMember(sess, memberOne);
		facilitiesManagerEntry.addAdmin(sess, facility, userOne);
		User userTwo = perun.getUsersManagerBl().getUserByMember(sess, memberTwo);
		facilitiesManagerEntry.addAdmin(sess, secondFacility, userTwo);

		// Sets userOne as actor in this test with role facility admin for facility
		AuthzRoles authzRoles = new AuthzRoles(Role.FACILITYADMIN, facility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userOne);
		// Adds destination to facility
		servicesManagerEntry.addDestination(sess, service, facility, destination);
		assertTrue(servicesManagerEntry.getDestinations(sess, service, facility).size() == 1);

		// Change actor in this test to userTwo
		authzRoles = new AuthzRoles(Role.FACILITYADMIN, secondFacility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userTwo);
		// Adds same host as destination to secondFacility with different admin -> should throw exception
		facilitiesManagerEntry.addHosts(sess, listOfHosts, secondFacility);
	}

	@Test
	public void getAllowedMembers() throws Exception {
		System.out.println(CLASS_NAME + "getAllowedMembers");

		// Test that new method returns same data as old behavior

		Vo vo2 = new Vo(0, "facilityTestVo002", "facilityTestVo002");
		vo2 = perun.getVosManagerBl().createVo(sess, vo2);

		Member member11 = setUpMember(vo);
		Member member12 = setUpMember(vo);
		Member member21 = setUpMember(vo2);
		Member member22 = setUpMember(vo2);

		Resource resource1 = setUpResource(vo);
		Resource resource2 = setUpResource(vo2);

		Group group1 = setUpGroup(vo, member11);
		Group group2 = setUpGroup(vo2, member21);

		// make them not-allowed
		perun.getMembersManager().setStatus(sess, member12, Status.INVALID);
		perun.getMembersManager().setStatus(sess, member22, Status.DISABLED);

		perun.getGroupsManager().addMember(sess, group1, member12);
		perun.getGroupsManager().addMember(sess, group2, member22);

		perun.getResourcesManager().assignGroupToResource(sess, group1, resource1);
		perun.getResourcesManager().assignGroupToResource(sess, group2, resource2);

		// test new way - single select
		List<Member> members = perun.getFacilitiesManagerBl().getAllowedMembers(sess, facility);
		Assert.notNull(members);
		assertTrue(members.size() == 2);
		assertTrue(members.contains(member11));
		assertTrue(members.contains(member21));
		assertTrue(!members.contains(member12));
		assertTrue(!members.contains(member22));

		// test old way - iterate over resources
		List<Resource> resources = perun.getFacilitiesManager().getAssignedResources(sess, facility);
		List<Member> oldMembers = new ArrayList<Member>();
		for (Resource r : resources) {
			oldMembers.addAll(perun.getResourcesManager().getAllowedMembers(sess, r));
		}
		Assert.notNull(oldMembers);
		assertTrue(oldMembers.contains(member11));
		assertTrue(oldMembers.contains(member21));
		assertTrue(!oldMembers.contains(member12));
		assertTrue(!oldMembers.contains(member22));

		assertEquals(new HashSet<>(members), new HashSet<>(members));

	}

	@Test
	public void getAssignedResourcesWithVoOrServiceFilter() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedResourcesWithVoOrServiceFilter");

		// Test that new method returns same data as old behavior

		Vo vo2 = new Vo(0, "facilityTestVo002", "facilityTestVo002");
		vo2 = perun.getVosManagerBl().createVo(sess, vo2);

		Member member11 = setUpMember(vo);
		Member member12 = setUpMember(vo);
		Member member21 = setUpMember(vo2);
		Member member22 = setUpMember(vo2);

		Resource resource1 = setUpResource(vo);
		Resource resource2 = setUpResource(vo2);

		Group group1 = setUpGroup(vo, member11);
		Group group2 = setUpGroup(vo2, member21);

		// make them not-allowed
		perun.getMembersManager().setStatus(sess, member12, Status.INVALID);
		perun.getMembersManager().setStatus(sess, member22, Status.DISABLED);

		perun.getGroupsManager().addMember(sess, group1, member12);
		perun.getGroupsManager().addMember(sess, group2, member22);

		perun.getResourcesManager().assignGroupToResource(sess, group1, resource1);
		perun.getResourcesManager().assignGroupToResource(sess, group2, resource2);

		// test new way - single select
		List<Member> members = perun.getFacilitiesManagerBl().getAllowedMembers(sess, facility);
		Assert.notNull(members);
		assertTrue(members.size() == 2);
		assertTrue(members.contains(member11));
		assertTrue(members.contains(member21));
		assertTrue(!members.contains(member12));
		assertTrue(!members.contains(member22));

		// check getting all
		List<Resource> resources = perun.getFacilitiesManager().getAssignedResources(sess, facility);
		Assert.notNull(resources);
		assertTrue(resources.size() == 2);
		assertTrue(resources.contains(resource1));
		assertTrue(resources.contains(resource2));

		// check getting by VO
		resources = perun.getFacilitiesManagerBl().getAssignedResources(sess, facility, vo, null);
		Assert.notNull(resources);
		assertTrue(resources.size() == 1);
		assertTrue(resources.contains(resource1));
		assertTrue(!resources.contains(resource2));

		Service service = new Service(0, "TestService01", null);
		service = perun.getServicesManager().createService(sess, service);

		perun.getResourcesManager().assignService(sess, resource1, service);

		// service should be only on 1 resource
		resources = perun.getFacilitiesManagerBl().getAssignedResources(sess, facility, null, service);
		Assert.notNull(resources);
		assertTrue(resources.size() == 1);
		assertTrue(resources.contains(resource1));
		assertTrue(!resources.contains(resource2));

		// vo-service should by only for 1 resource
		resources = perun.getFacilitiesManagerBl().getAssignedResources(sess, facility, vo, service);
		Assert.notNull(resources);
		assertTrue(resources.size() == 1);
		assertTrue(resources.contains(resource1));
		assertTrue(!resources.contains(resource2));

		// vo2-service shouldn't be assigned
		resources = perun.getFacilitiesManagerBl().getAssignedResources(sess, facility, vo2, service);
		Assert.notNull(resources);
		assertTrue(resources.isEmpty());

	}


	// PRIVATE METHODS -------------------------------------------------------

	private Vo setUpVo() throws Exception {

		Vo vo = new Vo();
		vo.setName("FacilitiesMangerTestVo");
		vo.setShortName("FMTVO");
		assertNotNull("unable to create VO",perun.getVosManager().createVo(sess, vo));
		//System.out.println(vo);
		return vo;

	}

	private Resource setUpResource(Vo vo) throws Exception {

		Resource resource = new Resource();
		resource.setName("FacilitiesManagerTestResource");
		resource.setDescription("testing resource");
		assertNotNull("unable to create resource",perun.getResourcesManager().createResource(sess, resource, vo, facility));

		return resource;

	}

	private Resource setUpResource2(Vo vo) throws Exception {

		Resource resource = new Resource();
		resource.setName("FacilitiesManagerTestSecondResource");
		resource.setDescription("testing second resource");
		assertNotNull("unable to create resource",perun.getResourcesManager().createResource(sess, resource, vo, facility));

		return resource;

	}

	private Member setUpMember(Vo vo) throws Exception {

		Candidate candidate = setUpCandidate();
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		assertEquals("Memer don't have VALID status.", Status.VALID, member.getStatus());
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		// save user for deletion after test
		return member;

	}

	private Candidate setUpCandidate(){

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
		UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String, String>());
		return candidate;

	}

	private Group setUpGroup(Vo vo, Member member) throws Exception {

		Group group = new Group("ResourcesManagerTestGroup","");
		group = perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().addMember(sess, group, member);
		return group;

	}

	private Group setUpGroup2(Vo vo, Member member) throws Exception {

		Group group = new Group("ResourcesManagerTestSecondGroup","");
		group = perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().addMember(sess, group, member);
		return group;

	}

	private Attribute setUpAttribute1() throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attrDef.setDescription("Test attribute description");
		attrDef.setFriendlyName("testingAttribute");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("Testing value");
		return attribute;
	}

	private Attribute setUpAttribute2() throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attrDef.setDescription("Test attribute2 description");
		attrDef.setFriendlyName("testingAttribute2");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("Testing value for second attribute");
		return attribute;
	}

	private Attribute setUpAttribute3() throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attrDef.setDescription("Test attribute3 description");
		attrDef.setFriendlyName("testingAttribute3");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("Testing value for third attribute");
		return attribute;
	}

	private SecurityTeam setUpSecurityTeam0() throws Exception {
		SecurityTeam st = new SecurityTeam("Security0", "Description test 0");
		perun.getSecurityTeamsManagerBl().createSecurityTeam(sess, st);
		return st;
	}
	private SecurityTeam setUpSecurityTeam1() throws Exception {
		SecurityTeam st = new SecurityTeam("Security1", "Description test 1");
		perun.getSecurityTeamsManagerBl().createSecurityTeam(sess, st);
		return st;
	}
	private SecurityTeam setUpSecurityTeam2() throws Exception {
		SecurityTeam st = new SecurityTeam("Security2", "Description test 2");
		perun.getSecurityTeamsManagerBl().createSecurityTeam(sess, st);
		return st;
	}

	private void setUpAssignSecurityTeams(Facility facility, List<SecurityTeam> securityTeams) throws Exception {
		for (SecurityTeam st : securityTeams) {
			facilitiesManagerEntry.assignSecurityTeam(sess, facility, st);
		}
	}

        private List<AttributeDefinition> getMandatoryAttrs() throws InternalErrorException{
		List<String> MANDATORY_ATTRIBUTES_FOR_USER_IN_CONTACT = new ArrayList<>(Arrays.asList(
                        AttributesManager.NS_USER_ATTR_DEF + ":organization",
                        AttributesManager.NS_USER_ATTR_DEF + ":preferredMail"));
		List<AttributeDefinition> mandatoryAttrs = new ArrayList<>();

		for(String attrName: MANDATORY_ATTRIBUTES_FOR_USER_IN_CONTACT) {
			try {
				mandatoryAttrs.add(perun.getAttributesManagerBl().getAttributeDefinition(sess, attrName));
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("Some of mandatory attributes for users in facility contacts not exists.",ex);
			}
		}

		return mandatoryAttrs;
        }

}
