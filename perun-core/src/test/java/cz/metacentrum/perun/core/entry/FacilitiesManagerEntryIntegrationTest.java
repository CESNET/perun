package cz.metacentrum.perun.core.entry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeAction;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributePolicy;
import cz.metacentrum.perun.core.api.AttributePolicyCollection;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.EnrichedBanOnFacility;
import cz.metacentrum.perun.core.api.EnrichedFacility;
import cz.metacentrum.perun.core.api.EnrichedHost;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.FacilitiesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.FacilityWithAttributes;
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
import cz.metacentrum.perun.core.api.RoleObject;
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
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Integration tests of FacilitiesManager
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class FacilitiesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

  private static final String CLASS_NAME = "FacilitiesManager.";
  final Facility facility = new Facility(); // always in DB
  private Owner owner = new Owner(); // always in DB and always own "facility" facility
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
    assertNotNull("unable to create owner", owner);
    perun.getFacilitiesManager().addOwner(sess, facility, owner);

    facilitiesManagerEntry = perun.getFacilitiesManager();

    // create list of hosts with 1 host
    createdHost = new Host();
    createdHost.setHostname("facilities.manager.test");
    hosts = new ArrayList<>();
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
    assertNotNull("unable to get Facility by ID", returnedFacility);
    assertEquals("created and returned facility should be the same", returnedFacility, facility);
    assertThat(returnedFacility.getUuid()).isNotNull();
    assertThat(returnedFacility.getUuid().version()).isEqualTo(4);

  }

  @Test
  public void getFacilitiesByHostName() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByHostname");

    String hostname = "test.hostname";
    Host host = new Host(15, hostname);
    perun.getFacilitiesManagerBl().addHost(sess, host, facility);

    List<Facility> facilities = perun.getFacilitiesManager().getFacilitiesByHostName(sess, hostname);
    assertNotNull("unable to get facilities by Hostname", facilities);
    assertEquals("There is only one facility with host with specific hostname", 1, facilities.size());
  }

  @Test(expected = FacilityNotExistsException.class)
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

  @Test(expected = FacilityNotExistsException.class)
  public void getFacilityByNameWhenFacilityNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getFacilityByNameWhenFacilityNotExists");

    facility.setName("");
    perun.getFacilitiesManager().getFacilityByName(sess, facility.getName());
    // shouldn't find facility

  }

  @Test
  public void getFacilitiesByIds() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByIds");

    List<Facility> facilities =
        facilitiesManagerEntry.getFacilitiesByIds(sess, Collections.singletonList(facility.getId()));
    assertEquals(facilities.size(), 1);
    assertTrue(facilities.contains(facility));

    Facility anotherFacility = facilitiesManagerEntry.createFacility(sess,
        new Facility(0, facility.getName() + "2nd", facility.getDescription() + "2nd"));
    facilities =
        facilitiesManagerEntry.getFacilitiesByIds(sess, Arrays.asList(facility.getId(), anotherFacility.getId()));
    assertEquals(facilities.size(), 2);
    assertTrue(facilities.contains(facility));
    assertTrue(facilities.contains(anotherFacility));

    facilities = facilitiesManagerEntry.getFacilitiesByIds(sess, Collections.singletonList(anotherFacility.getId()));
    assertEquals(facilities.size(), 1);
    assertTrue(facilities.contains(anotherFacility));
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
    assertTrue("At least one facility with destinatnion " + dest.getDestination() + " should exists",
        facilities.size() > 0);
    assertTrue("Created facility with destinantion " + dest.getDestination() + " should exist between others",
        facilities.contains(facility));
  }

  @Test
  public void getFacilitiesByAttribute() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByAttribute");

    Attribute attr = setUpAttribute4();
    attr.setValue("value");

    // Set the attribute to the facility
    perun.getAttributesManagerBl().setAttribute(sess, facility, attr);

    assertTrue("results must contain user",
        facilitiesManagerEntry.getFacilitiesByAttribute(sess, attr.getName(), "value").contains(facility));
  }

  @Test
  public void getFacilitiesByAttributeFilter() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByAttributeFilter");

    Facility otherFacility = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(-1, "Facility"));

    Attribute attr = setUpAttribute4();
    attr.setValue("value");

    perun.getAttributesManagerBl().setAttribute(sess, facility, attr);
    perun.getAttributesManagerBl().setAttribute(sess, otherFacility, attr);

    sess.getPerunPrincipal().setRoles(new AuthzRoles(Role.FACILITYADMIN, facility));

    List<Facility> result = facilitiesManagerEntry.getFacilitiesByAttribute(sess, attr.getName(), "value");

    assertEquals(1, result.size());
    assertTrue(result.contains(facility));
  }

  @Test
  public void getFacilitiesByAttributeWithAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByAttributeWithAttributes");

    Attribute attr = setUpAttribute4();
    attr.setValue("valueeeee");

    // Set the attribute to the facility
    perun.getAttributesManagerBl().setAttribute(sess, facility, attr);


    FacilityWithAttributes result =
        facilitiesManagerEntry.getFacilitiesByAttributeWithAttributes(sess, attr.getName(), "value",
            List.of(AttributesManager.NS_FACILITY_ATTR_CORE + ":name")).get(0);

    assertEquals(result.getFacility(), facility);
    assertEquals(result.getAttributes().get(0).getValue(), facility.getName());
  }

  @Test
  public void getFacilitiesByAttributeWithAttributesFilter() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByAttributeWithAttributesFilter");

    Facility otherFacility1 = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(-1, "Facility1"));
    Facility otherFacility2 = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(-2, "Facility2"));

    Attribute searchAttr = setUpAttribute4();
    searchAttr.setValue("value1");
    perun.getAttributesManagerBl().setAttribute(sess, facility, searchAttr);
    perun.getAttributesManagerBl().setAttribute(sess, otherFacility1, searchAttr);
    searchAttr.setValue("value2");
    perun.getAttributesManagerBl().setAttribute(sess, otherFacility2, searchAttr);

    AttributeDefinition getAttrDef =
        perun.getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_FACILITY_ATTR_CORE + ":name");
    List<AttributePolicyCollection> collections =
        perun.getAttributesManagerBl().getAttributePolicyCollections(sess, getAttrDef.getId());
    collections.add(new AttributePolicyCollection(-2, getAttrDef.getId(), AttributeAction.READ,
        List.of(new AttributePolicy(0, Role.FACILITYADMIN, RoleObject.Facility, -2))));
    perun.getAttributesManagerBl().setAttributePolicyCollections(sess, collections);

    sess.getPerunPrincipal().setRoles(new AuthzRoles(Role.FACILITYADMIN, List.of(facility, otherFacility2)));

    List<FacilityWithAttributes> result =
        facilitiesManagerEntry.getFacilitiesByAttributeWithAttributes(sess, searchAttr.getName(), "value1",
            List.of(getAttrDef.getName()));

    assertEquals(1, result.size());
    assertEquals(facility, result.get(0).getFacility());
    assertEquals(1, result.get(0).getAttributes().size());
    assertEquals(facility.getName(), result.get(0).getAttributes().get(0).getValue());
  }

  @Test
  public void getFacilitiesByDestinationWhenFacilityNotExist() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByDestinationWhenFacilityNotExist");
    List<Facility> facilities =
        perun.getFacilitiesManager().getFacilitiesByDestination(sess, "TestovaciDestinace neexistujici.");
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
  public void getEnrichedFacilities() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedFacilities");

    Service serv = new Service();
    serv.setName("TestovaciSluzba");
    perun.getServicesManager().createService(sess, serv);

    Destination dest = new Destination();
    dest.setType("service-specific");
    dest.setDestination("TestovaciDestinace");
    perun.getServicesManager().addDestination(sess, serv, facility, dest);

    facilitiesManagerEntry.addHosts(sess, hosts, facility);
    // set this host for deletion - host is created after adding to facility !!
    hostsForDeletion.add(hosts.get(0));

    EnrichedFacility expectedEnrichedFacility =
        new EnrichedFacility(facility, Collections.singletonList(owner), Collections.singletonList(dest), hosts);

    List<EnrichedFacility> enrichedFacilities = perun.getFacilitiesManager().getEnrichedFacilities(sess);
    assertTrue("The expected enriched facility should be returned",
        enrichedFacilities.contains(expectedEnrichedFacility));

    EnrichedFacility actualEnrichedFacility =
        enrichedFacilities.get(enrichedFacilities.indexOf(expectedEnrichedFacility));
    assertEquals("Returned enrichedFacility should have the same number of owners",
        expectedEnrichedFacility.getOwners().size(), actualEnrichedFacility.getOwners().size());
    assertEquals("Returned enrichedFacility should have the same number of destinations",
        expectedEnrichedFacility.getDestinations().size(), actualEnrichedFacility.getDestinations().size());
    assertEquals("Returned enrichedFacility should have the same number of hosts",
        expectedEnrichedFacility.getHosts().size(), actualEnrichedFacility.getHosts().size());
    assertTrue("Returned enrichedFacility should have the same owners",
        expectedEnrichedFacility.getOwners().containsAll(actualEnrichedFacility.getOwners()));
    assertTrue("Returned enrichedFacility should have the same destinations",
        expectedEnrichedFacility.getDestinations().containsAll(actualEnrichedFacility.getDestinations()));
    assertTrue("Returned enrichedFacility should have the same hosts",
        expectedEnrichedFacility.getHosts().containsAll(actualEnrichedFacility.getHosts()));
  }

  @Test
  public void getOwners() throws Exception {
    System.out.println(CLASS_NAME + "getOwners");

    List<Owner> owners = perun.getFacilitiesManager().getOwners(sess, facility);
    assertTrue("there should be 1 owner", owners.size() == 1);
    assertTrue("facility should be owned by our owner", owners.contains(owner));

    perun.getFacilitiesManager().removeOwner(sess, facility, owner);
    List<Owner> emptyOwners = perun.getFacilitiesManager().getOwners(sess, facility);
    assertTrue("there shouldn't be any owner", emptyOwners.isEmpty());

  }

  @Test(expected = FacilityNotExistsException.class)
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

  @Test
  public void addOwners() throws Exception {
    System.out.println(CLASS_NAME + "addOwners");

    Facility testFacility = perun.getFacilitiesManager().createFacility(sess, new Facility(123, "test"));

    Owner owner2 = new Owner();
    owner2.setName("SecondTestOwner");
    owner2.setContact("testingSecondOwner");
    owner2.setType(OwnerType.technical);
    perun.getOwnersManager().createOwner(sess, owner2);

    perun.getFacilitiesManager().addOwners(sess, testFacility, List.of(owner, owner2));

    assertEquals(2, perun.getFacilitiesManager().getOwners(sess, testFacility).size());
  }

  @Test(expected = OwnerNotExistsException.class)
  public void addOwnerWhenOwnerNotExists() throws Exception {
    System.out.println(CLASS_NAME + "addOwnerWhenOwnerNotExists");

    perun.getFacilitiesManager().addOwner(sess, facility, new Owner());
    // shouldn't be able to add not existing owner
  }

  @Test(expected = FacilityNotExistsException.class)
  public void addOwnerWhenFacilityNotExists() throws Exception {
    System.out.println(CLASS_NAME + "addOwnerWhenFacilityNotExists");

    Owner secondOwner = new Owner();
    secondOwner.setName("SecondTestOwner");
    secondOwner.setContact("testingSecondOwner");
    secondOwner.setType(OwnerType.technical);
    perun.getFacilitiesManager().addOwner(sess, new Facility(), secondOwner);
    // shouldn't facility

  }

  @Test(expected = OwnerAlreadyAssignedException.class)
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

  @Test
  public void removeOwners() throws Exception {
    System.out.println(CLASS_NAME + "removeOwners");

    Owner owner2 = new Owner();
    owner2.setName("SecondTestOwner");
    owner2.setContact("testingSecondOwner");
    owner2.setType(OwnerType.technical);
    perun.getOwnersManager().createOwner(sess, owner2);
    perun.getFacilitiesManager().addOwner(sess, facility, owner2);

    assertEquals(2, perun.getFacilitiesManager().getOwners(sess, facility).size());

    perun.getFacilitiesManager().removeOwners(sess, facility, List.of(owner, owner2));

    assertEquals(0, perun.getFacilitiesManager().getOwners(sess, facility).size());
  }

  @Test(expected = OwnerAlreadyRemovedException.class)
  public void removeOwnerWhenOwnerAlreadyRemoved() throws Exception {
    System.out.println(CLASS_NAME + "removeOwnerWhenOwnerAlreadyRemoved");

    perun.getFacilitiesManager().removeOwner(sess, facility, owner);
    perun.getFacilitiesManager().removeOwner(sess, facility, owner);
    // shouldn't be able to remove owner twice

  }

  @Test(expected = OwnerNotExistsException.class)
  public void removeOwnerWhenOwnerNotExists() throws Exception {
    System.out.println(CLASS_NAME + "removeOwnerWhenOwnerNotExists");

    perun.getFacilitiesManager().removeOwner(sess, facility, new Owner());
    // shouldn't be able to remove not existing owner

  }

  @Test(expected = FacilityNotExistsException.class)
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

    Vo vo2 = new Vo(1, "facilityTestVo002", "facilityTestVo002");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);
    setUpResource2(vo2);

    allowedVos = perun.getFacilitiesManager().getAllowedVos(sess, facility);

    assertTrue("our facility should have 2 allowed VO", allowedVos.size() == 2);
    assertTrue("our facility should have our VO as allowed", allowedVos.contains(vo2));

    setUpResource2(vo);

    allowedVos = perun.getFacilitiesManager().getAllowedVos(sess, facility);

    assertTrue("our facility should have 2 allowed VO", allowedVos.size() == 2);
  }

  @Test
  public void getAllowedUsers() throws Exception {
    System.out.println(CLASS_NAME + "getAllowedUsers");

    Vo vo = setUpVo();
    Resource resource = setUpResource(vo);

    Member member = setUpMember(vo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    Group group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    List<User> users = perun.getFacilitiesManager().getAllowedUsers(sess, facility);
    assertTrue("our facility should have 1 allowed user", users.size() == 1);
    assertTrue("our user should be between allowed on facility", users.contains(user));

  }

  @Test
  public void getAssociatedUsersForFacility() throws Exception {
    System.out.println(CLASS_NAME + "getAssociatedUsersForFacility");

    Vo vo2 = new Vo(1, "TestVO2", "TestVO2");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);

    Resource resource1 = setUpResource(vo);
    Resource resource2 = setUpResource(vo2);

    Member member1 = setUpMember(vo);
    Member member2 = setUpMember(vo2);
    Member member3 = setUpMember(vo2);

    User user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);
    User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
    User user3 = perun.getUsersManagerBl().getUserByMember(sess, member3);

    Group group1 = setUpGroup(vo, member1);
    Group group2 = setUpGroup(vo2, member2);
    Group group3 = setUpGroup2(vo2, member3);

    perun.getMembersManager().setStatus(sess, member1, Status.INVALID);
    perun.getMembersManager().setStatus(sess, member2, Status.EXPIRED);
    perun.getGroupsManagerBl().expireMemberInGroup(sess, member2, group2);

    perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource1, false, true, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource2, false, false, false);

    List<User> users = perun.getFacilitiesManagerBl().getAssociatedUsers(sess, facility);
    assertTrue("our facility should have 2 associated user", users.size() == 2);
    assertTrue("user not associated with resource should not be returned", !users.contains(user3));
    assertTrue("our users should be associated with facility", users.containsAll(List.of(user1, user2)));
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
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource1, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource2, false, false, false);

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
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    // second vo and member, assign group but no service
    Vo vo2 = new Vo();
    vo2.setName("FacilitiesMangerTestVo2");
    vo2.setShortName("FMTVO2");
    vo2 = perun.getVosManager().createVo(sess, vo2);
    assertNotNull("unable to create VO", vo2);

    Member member2 = setUpMember(vo2);
    User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
    Group group2 = setUpGroup(vo2, member2);
    Resource resource2 = setUpResource(vo2);
    perun.getResourcesManager().assignService(sess, resource2, serv);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource2, false, false, false);

    List<User> users = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, serv);
    assertTrue("our facility should have 1 allowed user", users.size() == 1);
    assertTrue("our user should be between allowed on facility", users.contains(user));

    List<User> users2 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, serv);
    assertTrue("our facility should have 1 allowed user", users2.size() == 1);
    assertTrue("our user should be between allowed on facility", users2.contains(user2));

    List<User> users3 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, null, serv);
    assertTrue("our facility should have 1 allowed user", users3.size() == 2);
    assertTrue("our user should be between allowed on facility", users3.contains(user));
    assertTrue("our user should be between allowed on facility", users3.contains(user2));

    List<User> users4 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, null);
    assertTrue("our facility should have 1 allowed user", users4.size() == 1);
    assertTrue("our user should be between allowed on facility", users4.contains(user));

    List<User> users5 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, null);
    assertTrue("our facility should have 1 allowed user", users5.size() == 1);
    assertTrue("our user should be between allowed on facility", users5.contains(user2));

    // remove service from resource2 to test other edge cases
    perun.getResourcesManager().removeService(sess, resource2, serv);

    List<User> users6 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, serv);
    assertTrue("our facility should have 1 allowed user", users6.size() == 1);
    assertTrue("our user should be between allowed on facility", users6.contains(user));

    List<User> users7 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, serv);
    assertTrue("our user shouldn't be allowed on facility with vo filter on", users7.size() == 0);

    List<User> users8 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, null, serv);
    assertTrue("our facility should have 1 allowed user", users8.size() == 1);
    assertTrue("our user should be between allowed on facility", users8.contains(user));
    assertTrue("our user shouldn't be between allowed on facility", !users8.contains(user2));

    List<User> users9 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, null);
    assertTrue("our facility should have 1 allowed user", users9.size() == 1);
    assertTrue("our user should be between allowed on facility", users9.contains(user));

    List<User> users10 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, null);
    assertTrue("our facility should have 1 allowed user", users10.size() == 1);
    assertTrue("our user should be between allowed on facility", users10.contains(user2));

    // create different service to test another edge cases

    Service serv2 = new Service();
    serv2.setName("TestService2");
    serv2 = perun.getServicesManager().createService(sess, serv2);
    perun.getResourcesManager().assignService(sess, resource2, serv2);

    List<User> users11 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, serv2);
    assertTrue("our facility shouldn't have allowed user with vo and service filter on", users11.size() == 0);

    List<User> users12 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, serv2);
    assertTrue("our facility should have 1 allowed user", users12.size() == 1);
    assertTrue("our user should be between allowed on facility", users12.contains(user2));

    List<User> users13 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, null, serv2);
    assertTrue("our facility should have 1 allowed user", users13.size() == 1);
    assertTrue("our user should be between allowed on facility", users13.contains(user2));

    List<User> users14 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, null);
    assertTrue("our facility should have 1 allowed user", users14.size() == 1);
    assertTrue("our user should be between allowed on facility", users14.contains(user));

    List<User> users15 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, null);
    assertTrue("our facility should have 1 allowed user", users15.size() == 1);
    assertTrue("our user should be between allowed on facility", users15.contains(user2));

    List<User> users16 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, null, null);
    assertTrue("our facility should have 2 allowed users", users16.size() == 2);
    assertTrue("our user should be between allowed on facility", users16.contains(user));
    assertTrue("our user should be between allowed on facility", users16.contains(user2));

    // disabled members shouldn't be allowed
    perun.getMembersManager().setStatus(sess, member, Status.DISABLED);

    List<User> users17 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, null, null);
    assertTrue("our facility should have 1 allowed user", users17.size() == 1);
    assertTrue("our user should be between allowed on facility", users17.contains(user2));

    List<User> users18 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo, null);
    assertTrue("our facility shouldn't have allowed user with vo filter on", users18.size() == 0);

    List<User> users19 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, null);
    assertTrue("our facility should have 1 allowed user", users19.size() == 1);
    assertTrue("our user should be between allowed on facility", users19.contains(user2));

    List<User> users20 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, serv);
    assertTrue("our facility shouldn't have allowed user with vo and service filter on", users20.size() == 0);

    List<User> users21 = perun.getFacilitiesManager().getAllowedUsers(sess, facility, vo2, serv2);
    assertTrue("our facility should have 1 allowed user", users21.size() == 1);
    assertTrue("our user should be between allowed on facility", users21.contains(user2));

  }


  @Test(expected = FacilityNotExistsException.class)
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

  @Test(expected = FacilityNotExistsException.class)
  public void getAssignedResourcesWhenFacilityNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedResourcesWhenFacilityNotExists");

    perun.getFacilitiesManager().getAssignedResources(sess, new Facility());
    // shouldn't find facility

  }

  @Test
  public void getAssignedResourcesByAssignedService() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedResourcesByAssignedService");

    Vo vo = setUpVo();
    Resource resource = setUpResource(vo);
    Resource resource2 = setUpResource2(vo);

    Service service = new Service();
    service.setName("TestovaciSluzba");
    perun.getServicesManager().createService(sess, service);

    perun.getResourcesManager().assignService(sess, resource, service);
    perun.getResourcesManager().assignService(sess, resource2, service);

    List<Resource> resources =
        perun.getFacilitiesManager().getAssignedResourcesByAssignedService(sess, facility, service);

    assertTrue("our facility should have 2 assigned Resource with this service", resources.size() == 2);

  }

  @Test
  public void getAssignedRichResources() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedRichResources");

    Vo vo = setUpVo();
    Resource resource = setUpResource(vo);
    RichResource rresource = new RichResource(resource);
    rresource.setVo(perun.getResourcesManager().getVo(sess, resource));

    List<RichResource> assignedResources = perun.getFacilitiesManager().getAssignedRichResources(sess, facility);

    List<Vo> vos = new ArrayList<>();
    for (RichResource rr : assignedResources) {
      assertTrue("RichResource must have VO value filled", rr.getVo() != null);
      vos.add(rr.getVo());
    }
    assertTrue("Our VO must be between RichResources VOs", vos.contains(vo));

    assertTrue("our facility should have 1 assigned Resource", assignedResources.size() == 1);
    assertTrue("our facility should have our Resource assigned", assignedResources.contains(rresource));

  }

  @Test
  public void getAssignedRichResourcesFilter() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedRichResourcesFilter");

    Vo vo = setUpVo();
    Resource resource = setUpResource(vo);
    Resource resource2 = setUpResource2(vo);
    RichResource rresource = new RichResource(resource);
    rresource.setVo(perun.getResourcesManager().getVo(sess, resource));

    sess.getPerunPrincipal().setRoles(new AuthzRoles(Role.RESOURCEBANMANAGER, List.of(resource, facility, vo)));


    List<RichResource> assignedResources = perun.getFacilitiesManager().getAssignedRichResources(sess, facility);
    assertEquals(assignedResources.size(), 1);

    sess.getPerunPrincipal().setRoles(new AuthzRoles(Role.FACILITYADMIN, List.of(facility)));

    assignedResources = perun.getFacilitiesManager().getAssignedRichResources(sess, facility);
    assertEquals(assignedResources.size(), 2);
  }

  @Test(expected = FacilityNotExistsException.class)
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

  @Test
  public void createFacilitySetsUUID() throws Exception {
    System.out.println(CLASS_NAME + "createFacilitySetsUUID");

    Facility facility = new Facility();
    facility.setName("FacilitiesManagerTestSecondFacility");
    facility.setDescription("TestSecondFacilityDescriptionText");
    Facility createdFacility = facilitiesManagerEntry.createFacility(sess, facility);
    assertThat(createdFacility.getUuid()).isNotNull();
    assertThat(createdFacility.getUuid().version()).isEqualTo(4);
  }

  @Test
  public void copyFacilityAdminAlreadyExists() throws Exception {
    System.out.println(CLASS_NAME + "copyFacilityAdminAlreadyExists");

    Facility f2 = new Facility();
    f2.setName("FacilitiesManagerTestSecondFacility");
    f2.setDescription("TestSecondFacilityDescription");
    Facility facility2 = perun.getFacilitiesManager().createFacility(sess, f2);

    Member member = setUpMember(vo);
    User u = perun.getUsersManagerBl().getUserByMember(sess, member);
    facilitiesManagerEntry.addAdmin(sess, facility, u);
    facilitiesManagerEntry.addAdmin(sess, facility2, u);

    Group group = setUpGroup(vo, member);
    facilitiesManagerEntry.addAdmin(sess, facility, group);
    facilitiesManagerEntry.addAdmin(sess, facility2, group);

    assertThatNoException().isThrownBy(() -> perun.getFacilitiesManager().copyManagers(sess, facility, facility2));
  }

  @Test(expected = FacilityExistsException.class)
  public void createFacilityWhenFacilityExists() throws Exception {
    System.out.println(CLASS_NAME + "createFacilityWhenFacilityExists");

    Facility facility = new Facility();
    facility.setName("FacilitiesManagerTestFacility");

    perun.getFacilitiesManager().createFacility(sess, facility);
    // shouldn't create same facility twice

  }


  @Test(expected = FacilityNotExistsException.class)
  public void deleteFacility() throws Exception {
    System.out.println(CLASS_NAME + "deleteFacility");

    perun.getFacilitiesManager().deleteFacility(sess, facility, false);
    perun.getFacilitiesManager().deleteFacility(sess, facility, false);
    // shouldn't find and delete "deleted facility"

  }

  @Test(expected = RelationExistsException.class)
  public void deleteFacilityWhenRelationExist() throws Exception {
    System.out.println(CLASS_NAME + "deleteFacilityWhenRelationExist");

    Vo vo = setUpVo();
    // create VO
    setUpResource(vo);
    // create Resource for our facility
    perun.getFacilitiesManager().deleteFacility(sess, facility, false);
    // shouldn't delete facility with resource

  }

  @Test(expected = FacilityNotExistsException.class)
  public void forceDeleteFacilityWhenRelationExist() throws Exception {
    System.out.println(CLASS_NAME + "forceDeleteFacilityWhenRelationExist");

    Vo vo = setUpVo();
    // create VO
    setUpResource(vo);
    // create Resource for our facility
    perun.getFacilitiesManager().deleteFacility(sess, facility, true);
    // deletes facility with resources
    perun.getFacilitiesManager().getFacilityById(sess, facility.getId());
    // shouldn't find the facility
  }

  @Test
  public void getOwnerFacilities() throws Exception {
    System.out.println(CLASS_NAME + "getOwnerFacilities");

    List<Facility> facilities = perun.getFacilitiesManager().getOwnerFacilities(sess, owner);
    assertTrue("our owner should own 1 facility", facilities.size() == 1);
    assertTrue("owner should own his facility", facilities.contains(facility));

  }

  @Test(expected = OwnerNotExistsException.class)
  public void getOwnerFacilitiesWhenOwnerNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getOwnerFacilitiesWhenOwnerNotExists");

    perun.getFacilitiesManager().getOwnerFacilities(sess, new Owner());
    // shouldn't find owner

  }

  @Test
  public void addHosts() throws Exception {
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
  @Test(expected = HostExistsException.class)
  public void addHostsWhenHostExistsException() throws Exception {
    System.out.println(CLASS_NAME + "addHostsWhenHostExistsException");

    hosts = facilitiesManagerEntry.addHosts(sess, hosts, facility);
    // set this host for deletion - host is created after adding to facility !!
    hostsForDeletion.add(hosts.get(0));
    // shouldn't add same host twice
    facilitiesManagerEntry.addHosts(sess, hosts, facility);

  }

  @Test(expected = FacilityNotExistsException.class)
  public void addHostsWhenFacilityNotExists() throws Exception {
    System.out.println(CLASS_NAME + "addHostsWhenFacilityNotExists");

    facilitiesManagerEntry.addHosts(sess, hosts, emptyFac);
    // shouldn't find facility

  }

  @Test
  public void addHostsWithPattern() throws Exception {
    System.out.println(CLASS_NAME + "addHostsWithPattern");

    String hostname = "name[00-01]surname[99-100].cz";
    List<String> listOfHosts = new ArrayList<>();
    listOfHosts.add(hostname);
    hostname = "local.cz";
    listOfHosts.add(hostname);
    hosts = facilitiesManagerEntry.addHosts(sess, facility, listOfHosts);
    // test
    assertNotNull("Unable to add hosts", hosts);
    assertEquals("There should be 5 hosts in list", 5, hosts.size());

    Set<String> hostNames = new HashSet<>();
    for (Host h : hosts) {
      hostNames.add(h.getHostname());
    }
    assertTrue("List doesn't contain host with name 'name00surname99.cz'.", hostNames.contains("name00surname99.cz"));
    assertTrue("List doesn't contain host with name 'name00surname100.cz'.", hostNames.contains("name00surname100.cz"));
    assertTrue("List doesn't contain host with name 'name01surname99.cz'.", hostNames.contains("name01surname99.cz"));
    assertTrue("List doesn't contain host with name 'name01surname100.cz'.", hostNames.contains("name01surname100.cz"));
    assertTrue("List doesn't contain host with name 'local.cz'.", hostNames.contains("local.cz"));
  }

  @Test(expected = WrongPatternException.class)
  public void addHostsWithWrongPattern() throws Exception {
    System.out.println(CLASS_NAME + "addHostsWithWrongPattern");

    String hostname = "name[00]-01]surname[99-100]cz";
    List<String> listOfHosts = new ArrayList<>();
    listOfHosts.add(hostname);
    hostname = "local";
    listOfHosts.add(hostname);
    hosts = facilitiesManagerEntry.addHosts(sess, facility, listOfHosts);
  }

  @Test(expected = WrongPatternException.class)
  public void addHostsWithWrongPattern2() throws Exception {
    System.out.println(CLASS_NAME + "addHostsWithWrongPattern2");

    String hostname = "name[00-a01]surname[99-100]cz";
    List<String> listOfHosts = new ArrayList<>();
    listOfHosts.add(hostname);
    hostname = "local";
    listOfHosts.add(hostname);
    hosts = facilitiesManagerEntry.addHosts(sess, facility, listOfHosts);
  }

  @Test(expected = WrongPatternException.class)
  public void addHostsWithWrongPattern3() throws Exception {
    System.out.println(CLASS_NAME + "addHostsWithWrongPattern3");

    String hostname = "name[01-00]surname[99-100]cz";
    List<String> listOfHosts = new ArrayList<>();
    listOfHosts.add(hostname);
    hostname = "local";
    listOfHosts.add(hostname);
    hosts = facilitiesManagerEntry.addHosts(sess, facility, listOfHosts);
  }

  @Test
  public void getHosts() throws Exception {
    System.out.println(CLASS_NAME + "getHosts");

    createdHost = facilitiesManagerEntry.addHosts(sess, hosts, facility).get(0);
    // set this host for deletion - host is created after adding to facility !!
    hostsForDeletion.add(hosts.get(0));
    final List<Host> expectedHosts = facilitiesManagerEntry.getHosts(sess, facility);
    final Host expectedHost = expectedHosts.get(0);
    assertEquals("Created and returned host should be the same", expectedHost, createdHost);

  }


  @Test
  public void getHostsByHostname() throws Exception {
    System.out.println(CLASS_NAME + "getHostsByHostname");

    Facility secondFacility = new Facility(0, "testFacilityGetHostsByHostnname");
    secondFacility = perun.getFacilitiesManagerBl().createFacility(sess, secondFacility);

    String hostname = "same.hostname.for.all.hosts";
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


  @Test(expected = FacilityNotExistsException.class)
  public void getHostsWhenFacilityNotExists() throws Exception {
    System.out.println(CLASS_NAME + "getHostsFacilityNotExists");

    facilitiesManagerEntry.getHosts(sess, emptyFac);
    // shouldn't find facility (facility)

  }

  @Test
  public void getEnrichedHosts() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedHosts");

    createdHost = facilitiesManagerEntry.addHosts(sess, hosts, facility).get(0);
    // set this host for deletion - host is created after adding to facility !!
    hostsForDeletion.add(hosts.get(0));

    Attribute attr = new Attribute();
    attr.setNamespace("urn:perun:host:attribute-def:opt");
    attr.setFriendlyName("host-test-for-list-of-names-attribute");
    attr.setType(String.class.getName());
    attr.setValue("HostAttributeForList");
    perun.getAttributesManagerBl().createAttribute(sess, attr);
    perun.getAttributesManagerBl().setAttribute(sess, createdHost, attr);

    List<String> attrNames = new ArrayList<>();
    attrNames.add(attr.getName());

    List<Attribute> hostAttributes = new ArrayList<>();
    hostAttributes.add(attr);
    EnrichedHost actualEnrichedHost = new EnrichedHost(createdHost, hostAttributes);

    List<EnrichedHost> expectedEnrichedHosts = facilitiesManagerEntry.getEnrichedHosts(sess, facility, attrNames);
    EnrichedHost expectedEnrichedHost = expectedEnrichedHosts.get(0);

    assertEquals("Created and returned enrichedHost should be the same", expectedEnrichedHost, actualEnrichedHost);
    assertEquals("Number of attributes should be same", expectedEnrichedHost.getHostAttributes().size(),
        actualEnrichedHost.getHostAttributes().size());
    assertTrue("Returned enrichedHost should have all the desired attributes",
        expectedEnrichedHost.getHostAttributes().containsAll(hostAttributes));
  }

  @Test
  public void removeHosts() throws Exception {
    System.out.println(CLASS_NAME + "removeHosts");

    facilitiesManagerEntry.addHosts(sess, hosts, facility);
    assertEquals("Unable to create add host to facility", facilitiesManagerEntry.getHostsCount(sess, facility), 1);
    // set this host for deletion - host is created after adding to facility !!
    hostsForDeletion.add(hosts.get(0));
    facilitiesManagerEntry.removeHosts(sess, hosts, facility);
    assertEquals("Unable to remove host from facility", facilitiesManagerEntry.getHostsCount(sess, facility), 0);

  }

  @Test(expected = FacilityNotExistsException.class)
  public void removeHostsWhenFacilityNotExists() throws Exception {
    System.out.println(CLASS_NAME + "removeHostsWhenFacilityNotExists");

    facilitiesManagerEntry.removeHosts(sess, hosts, emptyFac);
    // shouldn't find facility
  }

  @Test
  public void removeHostByHostname() throws Exception {
    System.out.println(CLASS_NAME + "removeHostByHostname");

    facilitiesManagerEntry.addHosts(sess, hosts, facility);
    // set this host for deletion - host is created after adding to facility !!
    hostsForDeletion.add(hosts.get(0));

    facilitiesManagerEntry.removeHostByHostname(sess, "facilities.manager.test");
  }

  @Test(expected = HostNotExistsException.class)
  public void removeHostByHostnameNotUniqueHostname() throws Exception {
    System.out.println(CLASS_NAME + "removeHostByHostnameNotUniqueHostname");

    Host createdHost2 = new Host();
    createdHost2.setHostname("facilities.manager.test");
    hosts.add(createdHost2);

    facilitiesManagerEntry.addHosts(sess, hosts, facility);
    // set this host for deletion - host is created after adding to facility !!
    hostsForDeletion.add(hosts.get(0));
    hostsForDeletion.add(hosts.get(1));

    facilitiesManagerEntry.removeHostByHostname(sess, "facilities.manager.test");
  }

  @Test(expected = HostNotExistsException.class)
  public void removeHostByHostnameNoneFound() throws Exception {
    System.out.println(CLASS_NAME + "removeHostByHostnameNoneFound");

    facilitiesManagerEntry.addHosts(sess, hosts, facility);
    // set this host for deletion - host is created after adding to facility !!
    hostsForDeletion.add(hosts.get(0));

    facilitiesManagerEntry.removeHostByHostname(sess, "facilities.manager.test2");
  }

  @Test
  public void getFacilitiesCount() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesCount");

    int count = facilitiesManagerEntry.getFacilitiesCount(sess);
    assertTrue(count > 0);
  }

  @Test
  public void getHostsCount() throws Exception {
    System.out.println(CLASS_NAME + "getHostsCount");

    facilitiesManagerEntry.addHosts(sess, hosts, facility);
    // set this host for deletion - host is created after adding to facility !!
    hostsForDeletion.add(hosts.get(0));
    assertEquals(facilitiesManagerEntry.getHostsCount(sess, facility), 1);

  }

  @Test(expected = FacilityNotExistsException.class)
  public void getHostsCountWhenFacilityNotExists() throws Exception {
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
    Group authorizedGroup = new Group("authorizedGroup", "testovaciGroup");
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
    candidate.setAttributes(new HashMap<>());

    Member member2 = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
    perun.getGroupsManager().addMember(sess, returnedGroup, member2);

    // test
    List<User> admins = facilitiesManagerEntry.getAdmins(sess, facility);
    //assertTrue("group shoud have 2 admins",admins.size() == 2);
    assertThat("facility should have 2 admins", admins.size(), is(2));
    assertTrue("our member as direct user should be admin", admins.contains(user));
    assertTrue("our member as member of admin group should be admin", admins.contains(user2));
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

  @Test(expected = UserNotAdminException.class)
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

  @Test
  public void setBan() throws Exception {
    System.out.println(CLASS_NAME + "setBan");
    Vo vo = setUpVo();
    Resource resource = setUpResource(vo);

    Member member = setUpMember(vo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    Group group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnFacility banOnFacility = setUpBan(new Date(), user.getId());

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
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnFacility banOnFacility = setUpBan(new Date(), user.getId());
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
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnFacility banOnFacility = setUpBan(new Date(), user.getId());
    banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);

    BanOnFacility returnedBan =
        facilitiesManagerEntry.getBan(sess, banOnFacility.getUserId(), banOnFacility.getFacilityId());
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
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnFacility banOnFacility = setUpBan(new Date(), user.getId());
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
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnFacility banOnFacility = setUpBan(new Date(), user.getId());
    banOnFacility = facilitiesManagerEntry.setBan(sess, banOnFacility);

    List<BanOnFacility> returnedBans = facilitiesManagerEntry.getBansForFacility(sess, banOnFacility.getFacilityId());
    assertEquals(banOnFacility, returnedBans.get(0));
  }

  @Test
  public void getEnrichedBansForFacility() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedBansForFacility");
    vo = setUpVo();
    Resource resource = setUpResource(vo);
    Member member = setUpMember(vo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    Group group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnFacility banOnFacility = setUpBan(new Date(), user.getId());
    facilitiesManagerEntry.setBan(sess, banOnFacility);
    AttributeDefinition usrAttrDef = setUpUserAttribute();
    Attribute userAttr = new Attribute(usrAttrDef, "user attribute value");
    perun.getAttributesManagerBl()
        .setAttribute(sess, perun.getUsersManagerBl().getUserByMember(sess, member), userAttr);

    List<EnrichedBanOnFacility> returnedBans =
        facilitiesManagerEntry.getEnrichedBansForFacility(sess, facility.getId(), List.of(userAttr.getName()));
    assertEquals(1, returnedBans.size());
    assertThat(returnedBans.get(0).getUser().getUserAttributes()).containsExactly(userAttr);
    assertThat(returnedBans.get(0).getBan()).isEqualTo(banOnFacility);
    assertThat(returnedBans.get(0).getFacility()).isEqualTo(facility);
  }

  @Test
  public void getEnrichedBansForUser() throws Exception {
    System.out.println(CLASS_NAME + "getEnrichedBansForUser");
    vo = setUpVo();
    Resource resource = setUpResource(vo);
    Member member = setUpMember(vo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    Group group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnFacility banOnFacility = setUpBan(new Date(), user.getId());
    facilitiesManagerEntry.setBan(sess, banOnFacility);
    AttributeDefinition usrAttrDef = setUpUserAttribute();
    Attribute userAttr = new Attribute(usrAttrDef, "user attribute value");
    perun.getAttributesManagerBl()
        .setAttribute(sess, perun.getUsersManagerBl().getUserByMember(sess, member), userAttr);

    List<EnrichedBanOnFacility> returnedBans =
        facilitiesManagerEntry.getEnrichedBansForUser(sess, user.getId(), List.of(userAttr.getName()));
    assertEquals(1, returnedBans.size());
    assertThat(returnedBans.get(0).getUser().getUserAttributes()).containsExactly(userAttr);
    assertThat(returnedBans.get(0).getBan()).isEqualTo(banOnFacility);
    assertThat(returnedBans.get(0).getFacility()).isEqualTo(facility);
  }

  @Test
  public void updateBan() throws Exception {
    System.out.println(CLASS_NAME + "updateBan");
    Vo vo = setUpVo();
    Resource resource = setUpResource(vo);

    Member member = setUpMember(vo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    Group group = setUpGroup(vo, member);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnFacility banOnFacility = setUpBan(new Date(), user.getId());
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
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnFacility banOnFacility = setUpBan(new Date(), user.getId());
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
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    BanOnFacility banOnFacility = setUpBan(new Date(), user.getId());
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
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    Date now = new Date();
    Date yesterday = new Date(now.getTime() - (1000 * 60 * 60 * 24));
    BanOnFacility banOnFacility = setUpBan(yesterday, user.getId());
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
    perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);

    Date now = new Date();
    Date tommorow = new Date(now.getTime() + (1000 * 60 * 60 * 24));
    BanOnFacility banOnFacility = setUpBan(tommorow, user.getId());
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
    String hostName = "test.host";
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
    List<PerunBean> list = new ArrayList<>();
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
    Host host = new Host(0, "test.host");
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
    String hostName = "test.host";
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
    String hostName = "test.host.one";
    List<String> listOfHosts = new ArrayList<>();
    listOfHosts.add(hostName);
    hostName = "test.host.two";
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
    String hostName = "test.host.one";
    List<String> listOfHosts = new ArrayList<>();
    listOfHosts.add(hostName);
    hostName = "test.host.two";
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
    List<Host> listOfHosts = new ArrayList<>();
    Host testHost = new Host(0, "test.host.one");
    listOfHosts.add(testHost);
    testHost = new Host(0, "test.host.two");
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
    List<Host> listOfHosts = new ArrayList<>();
    Host testHost = new Host(0, "testHostOne");
    listOfHosts.add(testHost);
    String hostName = "test.host.two";
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

    perun.getResourcesManager().assignGroupToResource(sess, group1, resource1, false, false, false);
    perun.getResourcesManager().assignGroupToResource(sess, group2, resource2, false, false, false);

    // test new way - single select
    List<Member> members = perun.getFacilitiesManagerBl().getAllowedMembers(sess, facility);
    assertNotNull(members);
    assertTrue(members.size() == 2);
    assertTrue(members.contains(member11));
    assertTrue(members.contains(member21));
    assertTrue(!members.contains(member12));
    assertTrue(!members.contains(member22));

    // test old way - iterate over resources
    List<Resource> resources = perun.getFacilitiesManager().getAssignedResources(sess, facility);
    List<Member> oldMembers = new ArrayList<>();
    for (Resource r : resources) {
      oldMembers.addAll(perun.getResourcesManager().getAllowedMembers(sess, r));
    }
    assertNotNull(oldMembers);
    assertTrue(oldMembers.contains(member11));
    assertTrue(oldMembers.contains(member21));
    assertTrue(!oldMembers.contains(member12));
    assertTrue(!oldMembers.contains(member22));

    assertEquals(new HashSet<>(members), new HashSet<>(members));

  }

  @Test
  public void getAllowedMembersForServiceAndFacility() throws Exception {
    System.out.println(CLASS_NAME + "getAllowedMembersForServiceAndFacility");

    Vo vo2 = new Vo(0, "facilityTestVo002", "facilityTestVo002");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);

    Member member11 = setUpMember(vo);
    Member member12 = setUpMember(vo);
    Member member21 = setUpMember(vo2);

    Resource resource1 = setUpResource(vo);
    Resource resource2 = setUpResource(vo2);

    Group group1 = setUpGroup(vo, member11);
    Group group2 = setUpGroup(vo2, member21);

    // make them not-allowed
    perun.getMembersManager().setStatus(sess, member12, Status.INVALID);

    perun.getGroupsManager().addMember(sess, group1, member12);

    // expired group members should be included
    perun.getGroupsManagerBl().expireMemberInGroup(sess, member11, group1);

    perun.getResourcesManager().assignGroupToResource(sess, group1, resource1, false, false, false);
    perun.getResourcesManager().assignGroupToResource(sess, group2, resource2, false, false, false);


    Service service = new Service(0, "TestService01", null);
    service = perun.getServicesManager().createService(sess, service);

    perun.getResourcesManager().assignService(sess, resource1, service);

    List<Member> members = perun.getFacilitiesManagerBl().getAllowedMembers(sess, facility, service);
    assertTrue(members.size() == 1);
    assertTrue("Expired group member must be returned!", members.contains(member11));
    assertTrue("Invalid vo member cannot be returned!", !members.contains(member12));
    assertTrue("Member not associated with service cannot be returned", !members.contains(member21));
  }

  @Test
  public void getAllowedMembersForServiceAndFacilityNoExpiredVoMembers() throws Exception {
    System.out.println(CLASS_NAME + "getAllowedMembersForServiceAndFacilityNoExpiredVoMembers");

    Vo vo2 = new Vo(0, "facilityTestVo002", "facilityTestVo002");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);

    Member member11 = setUpMember(vo);
    Member member12 = setUpMember(vo);
    Member member13 = setUpMember(vo);
    Member member21 = setUpMember(vo2);

    Resource resource1 = setUpResource(vo);
    Resource resource2 = setUpResource(vo2);

    Group group1 = setUpGroup(vo, member11);
    Group group2 = setUpGroup(vo2, member21);

    // make them not-allowed
    perun.getMembersManager().setStatus(sess, member12, Status.INVALID);

    perun.getGroupsManager().addMember(sess, group1, member12);

    // expired members should not be included
    perun.getMembersManager().setStatus(sess, member13, Status.EXPIRED);

    perun.getGroupsManager().addMember(sess, group1, member13);

    // expired group members should be included
    perun.getGroupsManagerBl().expireMemberInGroup(sess, member11, group1);

    perun.getResourcesManager().assignGroupToResource(sess, group1, resource1, false, false, false);
    perun.getResourcesManager().assignGroupToResource(sess, group2, resource2, false, false, false);


    Service service = new Service(0, "TestService01", null);
    service = perun.getServicesManager().createService(sess, service);

    perun.getResourcesManager().assignService(sess, resource1, service);

    List<Member> members = perun.getFacilitiesManagerBl().getAllowedMembers(sess, facility, service);
    assertTrue(members.size() == 1);
    assertTrue("Expired group member must be returned!", members.contains(member11));
    assertTrue("Invalid vo member cannot be returned!", !members.contains(member12));
    assertTrue("Expired vo member cannot be returned!", !members.contains(member13));
    assertTrue("Member not associated with service cannot be returned", !members.contains(member21));
  }

  @Test
  public void getAllowedMembersForServiceAndFacilityExpiredVoMembersIncluded() throws Exception {
    System.out.println(CLASS_NAME + "getAllowedMembersForServiceAndFacilityExpiredVoMembersIncluded");

    Vo vo2 = new Vo(0, "facilityTestVo002", "facilityTestVo002");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);

    Member member11 = setUpMember(vo);
    Member member12 = setUpMember(vo);
    Member member13 = setUpMember(vo);
    Member member21 = setUpMember(vo2);

    Resource resource1 = setUpResource(vo);
    Resource resource2 = setUpResource(vo2);

    Group group1 = setUpGroup(vo, member11);
    Group group2 = setUpGroup(vo2, member21);

    // make them not-allowed
    perun.getMembersManager().setStatus(sess, member12, Status.INVALID);

    perun.getGroupsManager().addMember(sess, group1, member12);

    // expired members should be included
    perun.getMembersManager().setStatus(sess, member13, Status.EXPIRED);

    perun.getGroupsManager().addMember(sess, group1, member13);

    // expired group members should be included
    perun.getGroupsManagerBl().expireMemberInGroup(sess, member11, group1);

    perun.getResourcesManager().assignGroupToResource(sess, group1, resource1, false, false, false);
    perun.getResourcesManager().assignGroupToResource(sess, group2, resource2, false, false, false);


    Service service = new Service(0, "TestService01", null);
    service.setUseExpiredVoMembers(true);
    service = perun.getServicesManager().createService(sess, service);

    perun.getResourcesManager().assignService(sess, resource1, service);

    List<Member> members = perun.getFacilitiesManagerBl().getAllowedMembers(sess, facility, service);
    assertTrue(members.size() == 2);
    assertTrue("Expired group member must be returned!", members.contains(member11));
    assertTrue("Invalid vo member cannot be returned!", !members.contains(member12));
    assertTrue("Expired vo member should be returned!", members.contains(member13));
    assertTrue("Member not associated with service cannot be returned", !members.contains(member21));
  }

  @Test
  public void getAllowedMembersForServiceAndFacilityNotExpiredInGroup() throws Exception {
    System.out.println(CLASS_NAME + "getAllowedMembersForServiceAndFacilityNotExpiredInGroup");

    Vo vo2 = new Vo(0, "facilityTestVo002", "facilityTestVo002");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);

    Member member11 = setUpMember(vo);
    Member member12 = setUpMember(vo);
    Member member13 = setUpMember(vo);
    Member member21 = setUpMember(vo2);

    Resource resource1 = setUpResource(vo);
    Resource resource2 = setUpResource(vo2);

    Group group1 = setUpGroup(vo, member11);
    Group group2 = setUpGroup(vo2, member21);

    // make them not-allowed
    perun.getMembersManager().setStatus(sess, member12, Status.INVALID);

    perun.getGroupsManager().addMember(sess, group1, member12);
    perun.getGroupsManager().addMember(sess, group1, member13);

    // expired group members should be not be included
    perun.getGroupsManagerBl().expireMemberInGroup(sess, member11, group1);

    perun.getResourcesManager().assignGroupToResource(sess, group1, resource1, false, false, false);
    perun.getResourcesManager().assignGroupToResource(sess, group2, resource2, false, false, false);


    Service service = new Service(0, "TestService01", null);
    service.setUseExpiredMembers(false);
    service = perun.getServicesManager().createService(sess, service);

    perun.getResourcesManager().assignService(sess, resource1, service);

    List<Member> members = perun.getFacilitiesManagerBl().getAllowedMembers(sess, facility, service);
    assertTrue(members.size() == 1);
    assertTrue("Expired group member cannot be returned!", !members.contains(member11));
    assertTrue("Invalid vo member cannot be returned!", !members.contains(member12));
    assertTrue("Valid member was not returned!", members.contains(member13));
    assertTrue("Member not associated with service cannot be returned", !members.contains(member21));
  }

  @Test
  public void getAllowedMembersForServiceAndFacilityNotExpiredInGroupNotExpiredInVo() throws Exception {
    System.out.println(CLASS_NAME + "getAllowedMembersForServiceAndFacilityNotExpiredInGroupNotExpiredInVo");

    Vo vo2 = new Vo(0, "facilityTestVo002", "facilityTestVo002");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);

    Member member11 = setUpMember(vo);
    Member member12 = setUpMember(vo);
    Member member13 = setUpMember(vo);
    Member member14 = setUpMember(vo);
    Member member21 = setUpMember(vo2);

    Resource resource1 = setUpResource(vo);
    Resource resource2 = setUpResource(vo2);

    Group group1 = setUpGroup(vo, member11);
    Group group2 = setUpGroup(vo2, member21);

    // make them not-allowed
    perun.getMembersManager().setStatus(sess, member12, Status.INVALID);
    perun.getMembersManager().setStatus(sess, member14, Status.EXPIRED);

    perun.getGroupsManager().addMember(sess, group1, member12);
    perun.getGroupsManager().addMember(sess, group1, member13);
    perun.getGroupsManager().addMember(sess, group1, member14);

    // expired group members should be not be included
    perun.getGroupsManagerBl().expireMemberInGroup(sess, member11, group1);

    perun.getResourcesManager().assignGroupToResource(sess, group1, resource1, false, false, false);
    perun.getResourcesManager().assignGroupToResource(sess, group2, resource2, false, false, false);


    Service service = new Service(0, "TestService01", null);
    service.setUseExpiredMembers(false);
    service = perun.getServicesManager().createService(sess, service);

    perun.getResourcesManager().assignService(sess, resource1, service);

    List<Member> members = perun.getFacilitiesManagerBl().getAllowedMembers(sess, facility, service);
    assertTrue(members.size() == 1);
    assertTrue("Expired group member cannot be returned!", !members.contains(member11));
    assertTrue("Invalid vo member cannot be returned!", !members.contains(member12));
    assertTrue("Valid member was not returned!", members.contains(member13));
    assertTrue("Member not associated with service cannot be returned", !members.contains(member21));
  }

  @Test
  public void getAllowedMembersForServiceAndFacilityNotExpiredInGroupExpiredInVo() throws Exception {
    System.out.println(CLASS_NAME + "getAllowedMembersForServiceAndFacilityNotExpiredInGroupExpiredInVo");

    Vo vo2 = new Vo(0, "facilityTestVo002", "facilityTestVo002");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);

    Member member11 = setUpMember(vo);
    Member member12 = setUpMember(vo);
    Member member13 = setUpMember(vo);
    Member member14 = setUpMember(vo);
    Member member21 = setUpMember(vo2);

    Resource resource1 = setUpResource(vo);
    Resource resource2 = setUpResource(vo2);

    Group group1 = setUpGroup(vo, member11);
    Group group2 = setUpGroup(vo2, member21);

    // make them not-allowed
    perun.getMembersManager().setStatus(sess, member12, Status.INVALID);
    perun.getMembersManager().setStatus(sess, member14, Status.EXPIRED);

    perun.getGroupsManager().addMember(sess, group1, member12);
    perun.getGroupsManager().addMember(sess, group1, member13);
    perun.getGroupsManager().addMember(sess, group1, member14);

    // expired group members should be not be included
    perun.getGroupsManagerBl().expireMemberInGroup(sess, member11, group1);

    perun.getResourcesManager().assignGroupToResource(sess, group1, resource1, false, false, false);
    perun.getResourcesManager().assignGroupToResource(sess, group2, resource2, false, false, false);


    Service service = new Service(0, "TestService01", null);
    service.setUseExpiredMembers(false);
    service.setUseExpiredVoMembers(true);
    service = perun.getServicesManager().createService(sess, service);

    perun.getResourcesManager().assignService(sess, resource1, service);

    List<Member> members = perun.getFacilitiesManagerBl().getAllowedMembers(sess, facility, service);
    assertTrue(members.size() == 2);
    assertTrue("Expired group member cannot be returned!", !members.contains(member11));
    assertTrue("Invalid vo member cannot be returned!", !members.contains(member12));
    assertTrue("Valid member was not returned!", members.contains(member13));
    assertTrue("Expired member was not returned!", members.contains(member14));
    assertTrue("Member not associated with service cannot be returned", !members.contains(member21));
  }


  @Test
  public void getAssociatedMembersForUserAndFacility() throws Exception {
    System.out.println(CLASS_NAME + "getAssociatedMembersForUserAndFacility");

    Vo vo2 = new Vo(0, "TestVO2", "TestVO2");
    vo2 = perun.getVosManagerBl().createVo(sess, vo2);

    Member member1 = setUpMember(vo);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member1);
    Member member2 = perun.getMembersManagerBl().createMember(sess, vo2, user);

    Resource resource1 = setUpResource(vo);
    Resource resource2 = setUpResource(vo2);

    Group group1 = setUpGroup(vo, member1);
    Group group2 = setUpGroup(vo2, member2);

    perun.getMembersManager().setStatus(sess, member1, Status.INVALID);
    perun.getMembersManager().setStatus(sess, member2, Status.EXPIRED);

    perun.getResourcesManager().assignGroupToResource(sess, group1, resource1, false, false, false);
    perun.getResourcesManager().assignGroupToResource(sess, group2, resource2, false, true, false);

    List<Member> members = perun.getFacilitiesManagerBl().getAssociatedMembers(sess, facility, user);

    assertTrue(members.size() == 2);
    assertTrue("Our members are not part of result list.", members.containsAll(List.of(member1, member2)));
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

    perun.getResourcesManager().assignGroupToResource(sess, group1, resource1, false, false, false);
    perun.getResourcesManager().assignGroupToResource(sess, group2, resource2, false, false, false);

    // test new way - single select
    List<Member> members = perun.getFacilitiesManagerBl().getAllowedMembers(sess, facility);
    assertNotNull(members);
    assertTrue(members.size() == 2);
    assertTrue(members.contains(member11));
    assertTrue(members.contains(member21));
    assertTrue(!members.contains(member12));
    assertTrue(!members.contains(member22));

    // check getting all
    List<Resource> resources = perun.getFacilitiesManager().getAssignedResources(sess, facility);
    assertNotNull(resources);
    assertTrue(resources.size() == 2);
    assertTrue(resources.contains(resource1));
    assertTrue(resources.contains(resource2));

    // check getting by VO
    resources = perun.getFacilitiesManagerBl().getAssignedResources(sess, facility, vo, null);
    assertNotNull(resources);
    assertTrue(resources.size() == 1);
    assertTrue(resources.contains(resource1));
    assertTrue(!resources.contains(resource2));

    Service service = new Service(0, "TestService01", null);
    service = perun.getServicesManager().createService(sess, service);

    perun.getResourcesManager().assignService(sess, resource1, service);

    // service should be only on 1 resource
    resources = perun.getFacilitiesManagerBl().getAssignedResources(sess, facility, null, service);
    assertNotNull(resources);
    assertTrue(resources.size() == 1);
    assertTrue(resources.contains(resource1));
    assertTrue(!resources.contains(resource2));

    // vo-service should by only for 1 resource
    resources = perun.getFacilitiesManagerBl().getAssignedResources(sess, facility, vo, service);
    assertNotNull(resources);
    assertTrue(resources.size() == 1);
    assertTrue(resources.contains(resource1));
    assertTrue(!resources.contains(resource2));

    // vo2-service shouldn't be assigned
    resources = perun.getFacilitiesManagerBl().getAssignedResources(sess, facility, vo2, service);
    assertNotNull(resources);
    assertTrue(resources.isEmpty());

  }

  @Test
  public void getAssignedRichResourcesByService() throws Exception {
    System.out.println(CLASS_NAME + "getAssignedRichResourcesByService");

    Resource resource = setUpResource(vo);
    Service service = new Service();
    service.setName("ServicesManagerTestService");
    service = perun.getServicesManager().createService(sess, service);
    perun.getResourcesManagerBl().assignService(sess, resource, service);

    Resource resource2 = setUpResource2(vo);
    Service service2 = new Service();
    service2.setName("ServicesManagerTestService2");
    service2 = perun.getServicesManager().createService(sess, service2);
    perun.getResourcesManagerBl().assignService(sess, resource2, service2);


    List<RichResource> result = perun.getFacilitiesManagerBl().getAssignedRichResources(sess, facility, service);

    assertEquals(2, perun.getFacilitiesManagerBl().getAssignedRichResources(sess, facility).size());
    assertEquals(1, result.size());
    assertEquals(resource.getName(), result.get(0).getName());
    assertEquals(vo, result.get(0).getVo());
  }

  @Test
  public void renameFacilityAndConsentHub() throws Exception {
    System.out.println(CLASS_NAME + "renameFacilityAndConsentHub");

    String oldHubName = perun.getConsentsManagerBl().getConsentHubByFacility(sess, facility.getId()).getName();

    facility.setName("renamedFacility");
    perun.getFacilitiesManagerBl().updateFacility(sess, facility);

    Facility updatedFacility = perun.getFacilitiesManager().getFacilityById(sess, facility.getId());
    ConsentHub updatedHub = perun.getConsentsManagerBl().getConsentHubByFacility(sess, facility.getId());

    assertNotEquals("Consent hub's name has not been changed.", oldHubName, updatedHub.getName());
    assertEquals("The names of the facility and the consent hub do not match.", updatedFacility.getName(),
        updatedHub.getName());
  }


  // PRIVATE METHODS -------------------------------------------------------

  private Vo setUpVo() throws Exception {

    Vo vo = new Vo();
    vo.setName("FacilitiesMangerTestVo");
    vo.setShortName("FMTVO");
    Vo createdVo = perun.getVosManager().createVo(sess, vo);
    assertNotNull("unable to create VO", createdVo);
    return createdVo;

  }

  private Resource setUpResource(Vo vo) throws Exception {

    Resource resource = new Resource();
    resource.setName("FacilitiesManagerTestResource");
    resource.setDescription("testing resource");
    assertNotNull("unable to create resource",
        perun.getResourcesManager().createResource(sess, resource, vo, facility));

    return resource;

  }

  private Resource setUpResource2(Vo vo) throws Exception {

    Resource resource = new Resource();
    resource.setName("FacilitiesManagerTestSecondResource");
    resource.setDescription("testing second resource");
    assertNotNull("unable to create resource",
        perun.getResourcesManager().createResource(sess, resource, vo, facility));

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

  private Candidate setUpCandidate() {

    String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
    String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
    String extLogin =
        Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

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
    candidate.setAttributes(new HashMap<>());
    return candidate;

  }

  private Group setUpGroup(Vo vo, Member member) throws Exception {

    Group group = new Group("ResourcesManagerTestGroup", "");
    group = perun.getGroupsManager().createGroup(sess, vo, group);
    perun.getGroupsManager().addMember(sess, group, member);
    return group;

  }

  private Group setUpGroup2(Vo vo, Member member) throws Exception {

    Group group = new Group("ResourcesManagerTestSecondGroup", "");
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

  private Attribute setUpAttribute4() throws Exception {
    // Check if the attribute already exists
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

      List<AttributePolicyCollection> collections =
          perun.getAttributesManagerBl().getAttributePolicyCollections(sess, attrDef.getId());
      collections.add(new AttributePolicyCollection(-1, attrDef.getId(), AttributeAction.READ,
          List.of(new AttributePolicy(0, Role.FACILITYADMIN, RoleObject.Facility, -1))));
      perun.getAttributesManagerBl().setAttributePolicyCollections(sess, collections);
    }

    return new Attribute(attrDef);
  }

  private Attribute setUpUserAttribute() throws Exception {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attrDef.setDescription("Test attribute description");
    attrDef.setFriendlyName("testingAttribute");
    attrDef.setType(String.class.getName());
    attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
    Attribute attribute = new Attribute(attrDef);
    attribute.setValue("Testing value");
    return attribute;
  }

  private BanOnFacility setUpBan(Date validity, int userId) {
    BanOnFacility banOnFacility = new BanOnFacility();
    banOnFacility.setUserId(userId);
    banOnFacility.setFacilityId(facility.getId());
    banOnFacility.setDescription("Popisek");
    banOnFacility.setValidityTo(validity);
    return banOnFacility;
  }

  private List<AttributeDefinition> getMandatoryAttrs() {
    List<String> mandatoryAttributesForUserInContact = new ArrayList<>(Arrays.asList(
        AttributesManager.NS_USER_ATTR_DEF + ":organization",
        AttributesManager.NS_USER_ATTR_DEF + ":preferredMail"));
    List<AttributeDefinition> mandatoryAttrs = new ArrayList<>();

    for (String attrName : mandatoryAttributesForUserInContact) {
      try {
        mandatoryAttrs.add(perun.getAttributesManagerBl().getAttributeDefinition(sess, attrName));
      } catch (AttributeNotExistsException ex) {
        throw new InternalErrorException("Some of mandatory attributes for users in facility contacts not exists.", ex);
      }
    }

    return mandatoryAttrs;
  }
}
