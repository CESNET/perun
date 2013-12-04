package cz.metacentrum.perun.core.entry;

import java.lang.Class;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.FacilitiesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;

/**
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class FacilitiesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	final Facility facility = new Facility(); // always in DB
	final Owner owner = new Owner(); // always in DB and always own "facility" facility

        private static final String FACILITIES_MANAGER = "FacilitiesManager";

	private Host createdHost;
	private List<Host> hosts;
	private FacilitiesManager facilitiesManagerEntry;
	private Facility emptyFac;
	private Vo facAdminsVo;
        
	@Before
	public void setUp() throws Exception {

		facility.setName("FacilitiesManagerTestFacility");
		facility.setType("Testing");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, facility));
		owner.setName("FacilityManagerTestOwner");
		owner.setContact("testingContact");
                owner.setType(OwnerType.technical);
		assertNotNull("unable to create owner",perun.getOwnersManager().createOwner(sess, owner));
		perun.getFacilitiesManager().addOwner(sess, facility, owner);

                facilitiesManagerEntry = perun.getFacilitiesManager();

		// create list of hosts with 1 host
		createdHost = new Host();
		createdHost.setHostname("FacilitiesManagerTest");
		hosts = new ArrayList<Host>();
		hosts.add(createdHost);

		//create empty facility
		emptyFac = new Facility();
		emptyFac.setType("Testing");
		
		facAdminsVo = perun.getVosManagerBl().getVoByShortName(sess, "facadmins");
	}

	@Test
	public void getFacilityById() throws Exception {
		System.out.println("FacilitiesManager.getFacilityById");

		Facility returnedFacility = perun.getFacilitiesManager().getFacilityById(sess, facility.getId());
		assertNotNull("unable to get Facility by ID",returnedFacility);
		assertEquals("created and returned facility should be the same", returnedFacility, facility);

	}
        
        @Test
	public void getFacilitiesByHostName() throws Exception {
		System.out.println("FacilitiesManager.getFacilitiesByHostname");
                
                String hostname = "testHostname";
                Host host = new Host(15, hostname);
                perun.getFacilitiesManagerBl().addHost(sess, host, facility);
                
		List<Facility> facilities = perun.getFacilitiesManager().getFacilitiesByHostName(sess, hostname);
		assertNotNull("unable to get facilities by Hostname", facilities);
		assertEquals("There is only one facility with host with specific hsotname", 1, facilities.size());
	}

	@Test (expected=FacilityNotExistsException.class)
	public void getFacilityByIdWhenFacilityNotExists() throws Exception {
		System.out.println("FacilitiesManager.getFacilityByIdWhenFacilityNotExists");

		facility.setId(0);
		perun.getFacilitiesManager().getFacilityById(sess, facility.getId());
		// shouldn't find facility

	}

	@Test
	public void getFacilityByName() throws Exception {
		System.out.println("FacilitiesManager.getFacilityByName");

		Facility returnedFacility = perun.getFacilitiesManager().getFacilityByName(sess, facility.getName(), facility.getType());
		assertNotNull("unable to get Facility by Name",returnedFacility);
		assertEquals("created and returned facility should be the same", returnedFacility, facility);

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getFacilityByNameWhenFacilityNotExists() throws Exception {
		System.out.println("FacilitiesManager.getFacilityByNameWhenFacilityNotExists");

		facility.setName("");
		perun.getFacilitiesManager().getFacilityByName(sess, facility.getName(), facility.getType());
		// shouldn't find facility

	}
        
        @Test
	public void getFacilitiesByDestination() throws Exception {
		System.out.println("FacilitiesManager.getFacilitiesByDestination");
                
                Service serv = new Service();
                serv.setName("TestovaciSluzba");
                perun.getServicesManager().createService(sess, serv, owner);
                
                Destination dest = new Destination();
                dest.setType("TestovaciTyp");
                dest.setDestination("TestovaciDestinace");
                perun.getServicesManager().addDestination(sess, serv, facility, dest);
                
		List<Facility> facilities = perun.getFacilitiesManager().getFacilitiesByDestination(sess,"TestovaciDestinace");
		assertTrue("At least one facility with destinatnion " + dest.getDestination() + " should exists",facilities.size() > 0);
		assertTrue("Created facility with destinantion " + dest.getDestination() + " should exist between others", facilities.contains(facility));
	}

        @Test
	public void getFacilitiesByDestinationWhenFacilityNotExist() throws Exception {
		System.out.println("FacilitiesManager.getFacilitiesByDestinationWhenFacilityNotExist");
		List<Facility> facilities = perun.getFacilitiesManager().getFacilitiesByDestination(sess,"TestovaciDestinace neexistujici.");
                assertTrue("No facility with such destination exist.",facilities.isEmpty());
        }
        
	@Test
	public void getFacilities() throws Exception {
		System.out.println("FacilitiesManager.getFacilities");

		List<Facility> facilities = perun.getFacilitiesManager().getFacilities(sess);
		assertTrue("at least one facility should exists",facilities.size() > 0);
		assertTrue("created facility should exist between others", facilities.contains(facility));

	}

	@Test
	public void getOwners() throws Exception {
		System.out.println("FacilitiesManager.getOwners");

		List<Owner> owners = perun.getFacilitiesManager().getOwners(sess, facility);
		assertTrue("there should be 1 owner",owners.size() == 1);
		assertTrue("facility should be owned by our owner", owners.contains(owner));

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getOwnersWhenFacilityNotExists() throws Exception {
		System.out.println("FacilitiesManager.getOwnersWhenFacilityNotExists");

		perun.getFacilitiesManager().getOwners(sess, new Facility());
		// shouldn't find facility

	}

	@Test
	public void addOwner() throws Exception {
		System.out.println("FacilitiesManager.addOwner");

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
		System.out.println("FacilitiesManager.addOwnerWhenOwnerNotExists");

		perun.getFacilitiesManager().addOwner(sess, facility, new Owner());
		// shouldn't be able to add not existing owner
	}

	@Test (expected=FacilityNotExistsException.class)
	public void addOwnerWhenFacilityNotExists() throws Exception {
		System.out.println("FacilitiesManager.addOwnerWhenFacilityNotExists");

		Owner secondOwner = new Owner();
		secondOwner.setName("SecondTestOwner");
		secondOwner.setContact("testingSecondOwner");
                secondOwner.setType(OwnerType.technical);
		perun.getFacilitiesManager().addOwner(sess, new Facility(), secondOwner);
		// shouldn't facility

	}

	@Test (expected=OwnerAlreadyAssignedException.class)
	public void addOwnerWhenOwnerAlreadyAssigned() throws Exception {
		System.out.println("FacilitiesManager.addOwnerWhenOwnerAlreadyAssigned");

		perun.getFacilitiesManager().addOwner(sess, facility, owner);
		// shouldn't be able to add same owner

	}

	@Test
	public void removeOwner() throws Exception {
		System.out.println("FacilitiesManager.removeOwner");

		perun.getFacilitiesManager().removeOwner(sess, facility, owner);

		List<Owner> owners = perun.getFacilitiesManager().getOwners(sess, facility);
		assertTrue("facility shouldn't have owner", owners.isEmpty());

	}

	@Test (expected=OwnerAlreadyRemovedException.class)
	public void removeOwnerWhenOwnerAlreadyRemoved() throws Exception {
		System.out.println("FacilitiesManager.removeOwnerWhenOwnerAlreadyRemoved");

		perun.getFacilitiesManager().removeOwner(sess, facility, owner);
		perun.getFacilitiesManager().removeOwner(sess, facility, owner);
		// shouldn't be able to remove owner twice

	}

	@Test (expected=OwnerNotExistsException.class)
	public void removeOwnerWhenOwnerNotExists() throws Exception {
		System.out.println("FacilitiesManager.removeOwnerWhenOwnerNotExists");

		perun.getFacilitiesManager().removeOwner(sess, facility, new Owner());
		// shouldn't be able to remove not existing owner

	}

	@Test (expected=FacilityNotExistsException.class)
	public void removeOwnerWhenFacilityNotExists() throws Exception {
		System.out.println("FacilitiesManager.removeOwnerWhenFacilityNotExists");

		perun.getFacilitiesManager().removeOwner(sess, new Facility(), owner);
		// shouldn't find facility

	}

	@Test
	public void getAllowedVos() throws Exception {
		System.out.println("FacilitiesManager.getAllowedVos");

		Vo vo = setUpVo();
		setUpResource(vo);

		List<Vo> allowedVos = perun.getFacilitiesManager().getAllowedVos(sess, facility);
		// Be aware that there is facility administrators VO
		assertTrue("our facility should have 1 allowed VO", allowedVos.size() == 2);
		assertTrue("our facility should have our VO as allowed", allowedVos.contains(vo));

	}

	@Test
	public void getAllowedUsers() throws Exception {
		System.out.println("FacilitiesManager.getAllowedUsers");

		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		List<User> users = perun.getFacilitiesManager().getAllowedUsers(sess, facility);
		assertTrue("our facility should have 1 allowed user",users.size() == 1);
		assertTrue("our user should be between allowed on facility",users.contains(user));

	}
        
        @Test
	public void getAllowedUsersWithVoAndServiceFilter() throws Exception {
		System.out.println("FacilitiesManager.getAllowedUsers");
                
		Vo vo = setUpVo();
               
		Resource resource = setUpResource(vo);
                
                Service serv = new Service();
                serv.setName("TestService");
                perun.getServicesManager().createService(sess, serv, owner);
                perun.getResourcesManager().assignService(sess, resource, serv);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		Group group = setUpGroup(vo, member);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);
                
		List<User> users = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, serv);
		assertTrue("our facility should have 1 allowed user",users.size() == 1);
		assertTrue("our user should be between allowed on facility",users.contains(user));

	}


	@Test (expected=FacilityNotExistsException.class)
	public void getAllowedVosWhenFacilityNotExists() throws Exception {
		System.out.println("FacilitiesManager.getAllowedVosWhenFacilityNotExists");

		perun.getFacilitiesManager().getAllowedVos(sess, new Facility());
		//shouldn't find facility
	}

	@Test
	public void getAssignedResources() throws Exception {
		System.out.println("FacilitiesManager.getAssignedResources");

		Vo vo = setUpVo();
		Resource resource = setUpResource(vo);

		List<Resource> assignedResources = perun.getFacilitiesManager().getAssignedResources(sess, facility);
		// Be aware that there is also facility administrators VO
		assertTrue("our facility should have 1 assigned Resource", assignedResources.size() == 2);
		assertTrue("our facility should have our Resource assigned", assignedResources.contains(resource));

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getAssignedResourcesWhenFacilityNotExists() throws Exception {
		System.out.println("FacilitiesManager.getAssignedResourcesWhenFacilityNotExists");

		perun.getFacilitiesManager().getAssignedResources(sess, new Facility());
		// shouldn't find facility

	}

	@Test
	public void getAssignedRichResources() throws Exception {
		System.out.println("FacilitiesManager.getAssignedRichResources");

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
		assertTrue("Our VO must be between RichResources VOs",vos.contains(vo));

		// Be aware that there is also facility administrators VO
		assertTrue("our facility should have 1 assigned Resource", assignedResources.size() == 2);
		assertTrue("our facility should have our Resource assigned", assignedResources.contains(rresource));

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getAssignedRichResourcesWhenFacilityNotExists() throws Exception {
		System.out.println("FacilitiesManager.getAssignedRichResourcesWhenFacilityNotExists");

		perun.getFacilitiesManager().getAssignedRichResources(sess, new Facility());
		// shouldn't find facility

	}

	@Test
	public void createFacility() throws Exception {
		System.out.println("FacilitiesManager.createFacility");

		Facility facility = new Facility();
		facility.setName("FacilitiesManagerTestSecondFacility");
		facility.setType("testingSecondFacility");

		Facility returnedFacility = perun.getFacilitiesManager().createFacility(sess, facility);
		assertNotNull("unable to create Facility",returnedFacility);
		assertEquals("created and returned facility should be the same",returnedFacility,facility);

	}

	@Test (expected=FacilityExistsException.class)
	public void createFacilityWhenFacilityExists() throws Exception {
		System.out.println("FacilitiesManager.createFacilityWhenFacilityExists");

		Facility facility = new Facility();
		facility.setName("FacilitiesManagerTestFacility");
		facility.setType("Testing");

		perun.getFacilitiesManager().createFacility(sess, facility);
		// shouldn't create same facility twice

	}



	@Test (expected=FacilityNotExistsException.class)
	public void deleteFacility() throws Exception {
		System.out.println("FacilitiesManager.deleteFacility");

		perun.getFacilitiesManager().deleteFacility(sess, facility);
		perun.getFacilitiesManager().deleteFacility(sess, facility);
		// shouldn't find and delete "deleted facility"

	}

	@Test (expected=RelationExistsException.class)
	public void deleteFacilityWhenRelationExist() throws Exception {
		System.out.println("FacilitiesManager.deleteFacilityWhenRelationExist");

		Vo vo = setUpVo();
		// create VO
		setUpResource(vo);
		// create Resource for our facility
		perun.getFacilitiesManager().deleteFacility(sess, facility);
		// shouldn't delete facility with resource

	}

	@Test
	public void getOwnerFacilities() throws Exception {
		System.out.println("FacilitiesManager.getOwnerFacilities");

		List<Facility> facilities = perun.getFacilitiesManager().getOwnerFacilities(sess, owner);
		assertTrue("our owner should own 1 facility", facilities.size() == 1);
		assertTrue("owner should own his facility", facilities.contains(facility));

	}

	@Test (expected=OwnerNotExistsException.class)
	public void getOwnerFacilitiesWhenOwnerNotExists() throws Exception {
		System.out.println("FacilitiesManager.getOwnerFacilitiesWhenOwnerNotExists");

		perun.getFacilitiesManager().getOwnerFacilities(sess, new Owner());
		// shouldn't find owner

	}
        
        @Test
	public void addHosts()throws Exception {
		System.out.println(FACILITIES_MANAGER + ".addHosts()");

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
		System.out.println(FACILITIES_MANAGER + ".addHostsWhenHostExistsException()");

		hosts = facilitiesManagerEntry.addHosts(sess, hosts, facility);
		// set this host for deletion - host is created after adding to facility !!
		hostsForDeletion.add(hosts.get(0));
		// shouldn't add same host twice
		facilitiesManagerEntry.addHosts(sess, hosts, facility);

	}

	@Test (expected=FacilityNotExistsException.class)
	public void addHostsWhenFacilityNotExists()throws Exception {
		System.out.println(FACILITIES_MANAGER + ".addHostsWhenFacilityNotExists()");

		facilitiesManagerEntry.addHosts(sess, hosts, emptyFac);
		// shouldn't find facility

	}

	@Test
	public void getHosts()throws Exception{
		System.out.println(FACILITIES_MANAGER + ".getHosts()");

		createdHost = facilitiesManagerEntry.addHosts(sess, hosts, facility).get(0);
		// set this host for deletion - host is created after adding to facility !!
		hostsForDeletion.add(hosts.get(0));
		final List<Host> expectedHosts = facilitiesManagerEntry.getHosts(sess, facility);
		final Host expectedHost = expectedHosts.get(0);
		assertEquals("Created and returned host should be the same",expectedHost, createdHost);

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getHostsWhenFacilityNotExists()throws Exception{
		System.out.println(FACILITIES_MANAGER + ".getHostsFacilityNotExists()");

		facilitiesManagerEntry.getHosts(sess, emptyFac);
		// shouldn't find facility (facility)

	}

	@Test
	public void removeHosts()throws Exception{
		System.out.println(FACILITIES_MANAGER + ".removeHosts()");

		facilitiesManagerEntry.addHosts(sess, hosts, facility);
		assertEquals("Unable to create add host to facility", facilitiesManagerEntry.getHostsCount(sess, facility), 1);
		// set this host for deletion - host is created after adding to facility !!
		hostsForDeletion.add(hosts.get(0));
		facilitiesManagerEntry.removeHosts(sess, hosts, facility);
		assertEquals("Unable to remove host from facility",facilitiesManagerEntry.getHostsCount(sess, facility), 0);

	}

	@Test (expected=FacilityNotExistsException.class)
	public void removeHostsWhenFacilityNotExists()throws Exception{
		System.out.println(FACILITIES_MANAGER + ".removeHostsWhenFacilityNotExists()");

		facilitiesManagerEntry.removeHosts(sess, hosts, emptyFac);
		// shouldn't find facility
	}

	@Test
	public void getHostsCount()throws Exception{
		System.out.println(FACILITIES_MANAGER + ".getHostsCount()");

		facilitiesManagerEntry.addHosts(sess, hosts, facility);
		// set this host for deletion - host is created after adding to facility !!
		hostsForDeletion.add(hosts.get(0));
		assertEquals(facilitiesManagerEntry.getHostsCount(sess, facility), 1);

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getHostsCountWhenFacilityNotExists()throws Exception{
		System.out.println(FACILITIES_MANAGER + ".getHostsCountWhenFacilityNotExists()");

		assertEquals(facilitiesManagerEntry.getHostsCount(sess, emptyFac), 1);
		// shouldn't find facility

	}

        
        @Test
        public void addAdmin() throws Exception {
                System.out.println(FACILITIES_MANAGER + ".addAdmin()");

                final Member member = setUpMember(facAdminsVo);
                User u = perun.getUsersManagerBl().getUserByMember(sess, member);

                facilitiesManagerEntry.addAdmin(sess, facility, u);
                final List<User> admins = facilitiesManagerEntry.getAdmins(sess, facility);

                assertNotNull(admins);
                assertTrue(admins.size() > 0);
        }
        
        @Test
        public void addAdminWithGroup() throws Exception {
                System.out.println(FACILITIES_MANAGER + ".addAdminWithGroup()");

                final Group group = new Group("testGroup", "just for testing");
                perun.getGroupsManager().createGroup(sess, facAdminsVo, group);
                facilitiesManagerEntry.addAdmin(sess, facility, group);
                
                final List<Group> admins = facilitiesManagerEntry.getAdminGroups(sess, facility);

                assertNotNull(admins);
                assertTrue(admins.size() > 0);
                assertTrue(admins.contains(group));
        }

        @Test
        public void getAdmins() throws Exception {
                System.out.println(FACILITIES_MANAGER + ".getAdmins()");

                // set up first user
                final Member member = setUpMember(facAdminsVo);
                User user = perun.getUsersManagerBl().getUserByMember(sess, member);
                facilitiesManagerEntry.addAdmin(sess, facility, user);

                // set up authorized group
                Group authorizedGroup = new Group("authorizedGroup","testovaciGroup");
                Group returnedGroup = perun.getGroupsManager().createGroup(sess, facAdminsVo, authorizedGroup);		
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
                
                Member member2 = perun.getMembersManagerBl().createMemberSync(sess, facAdminsVo, candidate);
                User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
                perun.getGroupsManager().addMember(sess, returnedGroup, member2);
                
                // test
		List<User> admins = facilitiesManagerEntry.getAdmins(sess, facility);
		assertTrue("group shoud have 2 admins",admins.size() == 2);
		assertTrue("our member as direct user should be admin",admins.contains(user));
                assertTrue("our member as member of admin group should be admin",admins.contains(user2));
        }
        
        @Test
        public void getDirectAdmins() throws Exception {
                System.out.println(FACILITIES_MANAGER + ".getDirectAdmins()");

                final Member member = setUpMember(facAdminsVo);
                User u = perun.getUsersManagerBl().getUserByMember(sess, member);

                facilitiesManagerEntry.addAdmin(sess, facility, u);
                assertTrue(facilitiesManagerEntry.getDirectAdmins(sess, facility).contains(u));
        }

        @Test
        public void getAdminsIfNotExist() throws Exception {
                System.out.println(FACILITIES_MANAGER + ".getAdminsIfNotExist()");

                final Member member = setUpMember(facAdminsVo);
                User u = perun.getUsersManagerBl().getUserByMember(sess, member);

                assertTrue(facilitiesManagerEntry.getAdmins(sess, facility).isEmpty());
        }
        
        @Test
        public void getAdminGroups() throws Exception {
            System.out.println(FACILITIES_MANAGER + ".getAdminGroups()");
            
            final Group group = new Group("testGroup", "just for testing");
            perun.getGroupsManager().createGroup(sess, facAdminsVo, group);
            facilitiesManagerEntry.addAdmin(sess, facility, group);
            
            assertTrue(facilitiesManagerEntry.getAdminGroups(sess, facility).contains(group));
        }
        
        @Test(expected=UserNotAdminException.class)
        public void removeAdminWhichNotExists() throws Exception {
                System.out.println(FACILITIES_MANAGER + ".removeAdminWhichNotExists()");

                final Member member = setUpMember(facAdminsVo);
                User u = perun.getUsersManagerBl().getUserByMember(sess, member);
                
                facilitiesManagerEntry.removeAdmin(sess, facility, u);
        }

        @Test
        public void removeAdmin() throws Exception {
                System.out.println(FACILITIES_MANAGER + ".removeAdmin()");

                final Member member = setUpMember(facAdminsVo);
                User u = perun.getUsersManagerBl().getUserByMember(sess, member);

                final RichUser richUser = new RichUser(u, perun.getUsersManagerBl().getUserExtSources(sess, u));
                               
                facilitiesManagerEntry.addAdmin(sess, facility, u);
                assertEquals(u, facilitiesManagerEntry.getAdmins(sess, facility).get(0));
                
                facilitiesManagerEntry.removeAdmin(sess, facility, u);
                assertFalse(facilitiesManagerEntry.getAdmins(sess, facility).contains(u));
        }
        
        @Test
        public void removeAdminWithGroup() throws Exception {
                System.out.println(FACILITIES_MANAGER + ".removeAdminWithGroup()");

                final Group group = new Group("testGroup", "just for testing");
                perun.getGroupsManager().createGroup(sess, facAdminsVo, group);
                facilitiesManagerEntry.addAdmin(sess, facility, group);
                
                facilitiesManagerEntry.removeAdmin(sess, facility, group);
                assertFalse(facilitiesManagerEntry.getAdminGroups(sess, facility).contains(group));
        }
        
        @Test
        public void getFacilitiesWhereUserIsAdmin() throws Exception {
        	System.out.println(FACILITIES_MANAGER + ".getFacilitiesWhereUserIsAdmin()");
        	
        	final Member member = setUpMember(facAdminsVo);
            User u = perun.getUsersManagerBl().getUserByMember(sess, member);

            facilitiesManagerEntry.addAdmin(sess, facility, u);
            
            List<Facility> facilities = facilitiesManagerEntry.getFacilitiesWhereUserIsAdmin(sess, u);
            assertNotNull(facilities);
            assertTrue(facilities.contains(facility));
        }
        
        @Test
        public void copyManagers() throws Exception {
            System.out.println(FACILITIES_MANAGER + ".copyManagers");
            
            // add user as admin in facility
            final Member member = setUpMember(facAdminsVo);
            User u = perun.getUsersManagerBl().getUserByMember(sess, member);
            facilitiesManagerEntry.addAdmin(sess, facility, u);
            
            // set up second facility
            Facility newFacility = new Facility();
            newFacility.setName("FacilitiesManagerTestSecondFacility");
            newFacility.setType("testingSecondFacility");
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
            System.out.println(FACILITIES_MANAGER + ".copyOwners");
            
            // set up second facility
            Facility newFacility = new Facility();
            newFacility.setName("FacilitiesManagerTestSecondFacility");
            newFacility.setType("testingSecondFacility");
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
            System.out.println(FACILITIES_MANAGER + ".copyAttributes");
            
            // set up second facility
            Facility newFacility = new Facility();
            newFacility.setName("FacilitiesManagerTestSecondFacility");
            newFacility.setType("testingSecondFacility");
            Facility secondFacility = perun.getFacilitiesManager().createFacility(sess, newFacility);
            
            // add first attribute to source
            Attribute firstAttribute = setUpAttribute1();
            perun.getAttributesManager().setAttribute(sess, facility, firstAttribute);
            
            // add second attribute to both
            Attribute secondAttribute = setUpAttribute2();
            perun.getAttributesManager(). setAttribute(sess, facility, secondAttribute);
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
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}

	private Group setUpGroup(Vo vo, Member member) throws Exception {

		Group group = new Group("ResourcesManagerTestGroup","");
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

}
