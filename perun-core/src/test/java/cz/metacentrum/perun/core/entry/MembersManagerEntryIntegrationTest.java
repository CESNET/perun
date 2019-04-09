package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AlreadySponsorException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotInRoleException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration tests for MembersManager
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MembersManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private static final String CLASS_NAME = "MembersManager.";
	private static final String EXT_SOURCE_NAME = "MembersManagerEntryExtSource";
	private Vo createdVo = null;
	private ExtSource extSource = new ExtSource(0, EXT_SOURCE_NAME, ExtSourcesManager.EXTSOURCE_INTERNAL);
	private Group createdGroup;
	private Group g1;
	private Group g2;
	private Group g3ing1;
	private Candidate candidate;
	private UserExtSource ues;
	private Member createdMember;
	private MembersManager membersManagerEntry;
	private AttributesManager attributesManagerEntry;
	private GroupsManager groupsManagerEntry;
	private UsersManager usersManagerEntry;

	@Before
	public void setUp() throws Exception {

		extSource = perun.getExtSourcesManager().createExtSource(sess, extSource, null);

		usersManagerEntry = perun.getUsersManager();
		attributesManagerEntry = perun.getAttributesManager();
		membersManagerEntry = perun.getMembersManager();
		final Vo vo = new Vo(0, "m3mb3r r00m", "m3mber-room");
		VosManager vosManagerEntry = perun.getVosManager();
		createdVo = vosManagerEntry.createVo(sess, vo);
		assertNotNull(createdVo);

		groupsManagerEntry = perun.getGroupsManager();
		final Group group = new Group("Test_Group_123456", "TestGroupDescr");
		createdGroup = groupsManagerEntry.createGroup(sess, createdVo, group);

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

		candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		ues = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(ues);
		candidate.setAttributes(new HashMap<>());

		createdMember = perun.getMembersManagerBl().createMemberSync(sess, createdVo, candidate);
		assertNotNull("No member created", createdMember);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, createdMember));
		// save user for deletion after test

		//need for testing creating members
		g1 = perun.getGroupsManagerBl().createGroup(sess, createdVo, new Group("TESTINGGROUP1", "TESTINGGROUP1"));
		g2 = perun.getGroupsManagerBl().createGroup(sess, createdVo, new Group("TESTINGGROUP2", "TESTINGGROUP2"));
		g3ing1 = perun.getGroupsManagerBl().createGroup(sess, g1, new Group("TESTINGGROUP3", "TESTINGGROUP3"));
	}

	@Test
	public void getSponsoredMembers() throws Exception {
		System.out.println(CLASS_NAME + "getSponsoredMembers");

		Member sponsorMember = setUpSponsor(createdVo);
		User sponsorUser = perun.getUsersManagerBl().getUserByMember(sess, sponsorMember);
		Group sponsors = new Group("sponsors","users able to sponsor");
		sponsors = perun.getGroupsManagerBl().createGroup(sess,createdVo,sponsors);
		AuthzResolverBlImpl.setRole(sess, sponsors, createdVo, Role.SPONSOR);
		perun.getGroupsManagerBl().addMember(sess,sponsors,sponsorMember);

		//no sponsored member has been created yet
		assertTrue(perun.getMembersManagerBl().getSponsoredMembers(sess, createdVo).size() == 0);

		Member sponsoredMember1 = perun.getMembersManagerBl().createSponsoredMember(sess, createdVo, "dummy", "Ing. Petr Draxler", "secret", sponsorUser, false);

		//should contain one sponsored member
		List<Member> sponsoredMembers = perun.getMembersManagerBl().getSponsoredMembers(sess, createdVo);

		assertTrue(sponsoredMembers.size() == 1);
		assertTrue(sponsoredMembers.contains(sponsoredMember1));

		Member sponsoredMember2 = perun.getMembersManagerBl().createSponsoredMember(sess, createdVo, "dummy", "Miloš Zeman", "password", sponsorUser, false);

		sponsoredMembers = perun.getMembersManagerBl().getSponsoredMembers(sess, createdVo);

		//now should contain two sponsored members
		assertTrue(sponsoredMembers.size() == 2);
		assertTrue(sponsoredMembers.contains(sponsoredMember1));
		assertTrue(sponsoredMembers.contains(sponsoredMember2));
	}

	@Test
	public void createMember() throws Exception {
		System.out.println(CLASS_NAME + "createMemberSync");

		final Member m;
		//createdMember should be initialized in setUp method. if not, do it on my own
		m = (createdMember != null)   ? createdMember : membersManagerEntry.createMember(sess,
				createdVo, candidate);

		assertNotNull(m);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, m));
		// save user for deletion after test
		assertTrue(m.getId() > 0);
	}

	@Test
	public void createMemberFromCandidateInGroup() throws Exception {
		System.out.println(CLASS_NAME + "createMember");

		//Create vo and groups

		//g3in1 - direct, g1 indirect
		List<Group> groups = new ArrayList<>(Collections.singletonList(g3ing1));

		//Create new locale member for puprose of this method
		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));
		Candidate candidate = new Candidate();
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		UserExtSource ues = new UserExtSource(new ExtSource(0, "testExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL), extLogin);
		candidate.setUserExtSource(ues);
		candidate.setAttributes(new HashMap<>());

		Member member = perun.getMembersManager().createMember(sess, createdVo, candidate, groups);

		//test if member is in vo and also in defined groups
		assertTrue(perun.getMembersManagerBl().getMembers(sess, createdVo).contains(member));
		List<Group> returnedGroups = perun.getGroupsManagerBl().getMemberGroups(sess, member);

		assertTrue(returnedGroups.contains(g1));
		assertTrue(!returnedGroups.contains(g2));
		assertTrue(returnedGroups.contains(g3ing1));

		// save user for deletion after test
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
	}

	@Test
	public void getCompleteRichMembers() throws Exception {
		System.out.println(CLASS_NAME + "getCompleteRichMembers");

		User user = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		Facility facility = new Facility(0, "TESTING Facility", "TESTING Facility");
		facility = perun.getFacilitiesManagerBl().createFacility(sess, facility);
		Resource resource = new Resource(0, "TESTING Resource", "TESTING Resource", facility.getId(), createdVo.getId());
		resource = perun.getResourcesManagerBl().createResource(sess, resource, createdVo, facility);
		perun.getResourcesManagerBl().assignGroupToResource(sess, createdGroup, resource);
		perun.getGroupsManagerBl().addMember(sess, createdGroup, createdMember);

		Attribute userAttribute1 = setUpAttribute(String.class.getName(), "testUserAttribute1", AttributesManager.NS_USER_ATTR_DEF, "TEST VALUE");
		Attribute userAttribute2 = setUpAttribute(String.class.getName(), "testUserAttribute2", AttributesManager.NS_USER_ATTR_DEF, "TEST VALUE");
		perun.getAttributesManagerBl().setAttributes(sess, user, new ArrayList<>(Arrays.asList(userAttribute1, userAttribute1)));
		Attribute memberAttribute1 = setUpAttribute(Integer.class.getName(), "testMemberAttribute1", AttributesManager.NS_MEMBER_ATTR_DEF, 15);
		perun.getAttributesManagerBl().setAttributes(sess, createdMember, new ArrayList<>(Collections.singletonList(memberAttribute1)));
		Attribute userFacilityAttribute1 = setUpAttribute(ArrayList.class.getName(), "testUserFacilityAttribute1", AttributesManager.NS_USER_FACILITY_ATTR_DEF, new ArrayList<>(Arrays.asList("A", "B")));
		perun.getAttributesManagerBl().setAttributes(sess, facility, user, new ArrayList<>(Collections.singletonList(userFacilityAttribute1)));
		Map<String, String> map = new LinkedHashMap<>();
		map.put("A", "B");
		map.put("C", "D");
		Attribute memberResourceAttribute1 = setUpAttribute(LinkedHashMap.class.getName(), "testMemberResourceAttribute1", AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, map);
		perun.getAttributesManagerBl().setAttributes(sess, createdMember, resource, new ArrayList<>(Collections.singletonList(memberResourceAttribute1)));

		List<String> attrNames = new ArrayList<>(Arrays.asList(userAttribute1.getName(), memberAttribute1.getName(), userFacilityAttribute1.getName(), memberResourceAttribute1.getName()));
		List<RichMember> richMembers = membersManagerEntry.getCompleteRichMembers(sess, createdGroup, resource, attrNames, Arrays.asList("INVALID", "DISABLED", "SUSPENDED", "EXPIRED"));
		assertTrue(richMembers.isEmpty());
		richMembers = membersManagerEntry.getCompleteRichMembers(sess, createdGroup, resource, attrNames, Collections.singletonList("VALID"));

		List<Attribute> userAttributes = richMembers.get(0).getUserAttributes();
		List<Attribute> memberAttributes = richMembers.get(0).getMemberAttributes();
		assertTrue(richMembers.size() == 1);
		assertTrue(userAttributes.size() == 2);
		assertTrue(memberAttributes.size() == 2);
		assertTrue(userAttributes.contains(userAttribute1));
		assertTrue(userAttributes.contains(userFacilityAttribute1));
		assertTrue(memberAttributes.contains(memberAttribute1));
		assertTrue(memberAttributes.contains(memberResourceAttribute1));
	}

	@Test
	public void getRichMembersWithAttributesByNames() throws Exception {
		System.out.println(CLASS_NAME + "getRichMembersWithAttributesByNames");

		User user = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		Facility facility = new Facility(0, "TESTING Facility", "TESTING Facility");
		facility = perun.getFacilitiesManagerBl().createFacility(sess, facility);
		Resource resource = new Resource(0, "TESTING Resource", "TESTING Resource", facility.getId(), createdVo.getId());
		resource = perun.getResourcesManagerBl().createResource(sess, resource, createdVo, facility);
		perun.getResourcesManagerBl().assignGroupToResource(sess, createdGroup, resource);
		perun.getGroupsManagerBl().addMember(sess, createdGroup, createdMember);

		Attribute userAttribute1 = setUpAttribute(String.class.getName(), "testUserAttribute1", AttributesManager.NS_USER_ATTR_DEF, "TEST VALUE");
		Attribute userAttribute2 = setUpAttribute(String.class.getName(), "testUserAttribute2", AttributesManager.NS_USER_ATTR_DEF, "TEST VALUE");
		perun.getAttributesManagerBl().setAttributes(sess, user, new ArrayList<>(Arrays.asList(userAttribute1, userAttribute1)));
		Attribute memberAttribute1 = setUpAttribute(Integer.class.getName(), "testMemberAttribute1", AttributesManager.NS_MEMBER_ATTR_DEF, 15);
		perun.getAttributesManagerBl().setAttributes(sess, createdMember, new ArrayList<>(Collections.singletonList(memberAttribute1)));
		Attribute userFacilityAttribute1 = setUpAttribute(ArrayList.class.getName(), "testUserFacilityAttribute1", AttributesManager.NS_USER_FACILITY_ATTR_DEF, new ArrayList<>(Arrays.asList("A", "B")));
		perun.getAttributesManagerBl().setAttributes(sess, facility, user, new ArrayList<>(Collections.singletonList(userFacilityAttribute1)));
		Map<String, String> map = new LinkedHashMap<>();
		map.put("A", "B");
		map.put("C", "D");
		Attribute memberResourceAttribute1 = setUpAttribute(LinkedHashMap.class.getName(), "testMemberResourceAttribute1", AttributesManager.NS_MEMBER_RESOURCE_ATTR_DEF, map);
		//test of member-group attributes
		perun.getAttributesManagerBl().setAttributes(sess, createdMember, resource, new ArrayList<>(Collections.singletonList(memberResourceAttribute1)));
		Map<String, String> groupMap = new LinkedHashMap<>();
		groupMap.put("E", "F");
		groupMap.put("G", "H");
		Attribute memberGroupAttribute1 = setUpAttribute(LinkedHashMap.class.getName(), "testMemberGroupAttribute1", AttributesManager.NS_MEMBER_GROUP_ATTR_DEF, groupMap);
		perun.getAttributesManagerBl().setAttributes(sess, createdMember, createdGroup, new ArrayList<>(Collections.singletonList(memberGroupAttribute1)));

		List<String> attrNames = new ArrayList<>(Arrays.asList(userAttribute1.getName(), memberAttribute1.getName(), userFacilityAttribute1.getName(), memberResourceAttribute1.getName(), memberGroupAttribute1.getName()));
		List<RichMember> richMembers = perun.getMembersManagerBl().getRichMembersWithAttributesByNames(sess, createdGroup, resource, attrNames);

		List<Attribute> userAttributes = richMembers.get(0).getUserAttributes();
		List<Attribute> memberAttributes = richMembers.get(0).getMemberAttributes();

		assertTrue(richMembers.size() == 1);
		assertTrue(userAttributes.size() == 2);
		assertTrue(memberAttributes.size() == 3);
		assertTrue(userAttributes.contains(userAttribute1));
		assertTrue(userAttributes.contains(userFacilityAttribute1));
		assertTrue(memberAttributes.contains(memberAttribute1));
		assertTrue(memberAttributes.contains(memberResourceAttribute1));
		assertTrue(memberAttributes.contains(memberGroupAttribute1));
	}

	@Test
	public void findCompleteRichMembers() throws Exception {
		System.out.println(CLASS_NAME + "findCompleteRichMembers");

		User user = perun.getUsersManagerBl().getUserByMember(sess, createdMember);

		List<RichMember> richMembers = perun.getMembersManager().findCompleteRichMembers(sess, new ArrayList<>(), new ArrayList<>(), user.getFirstName());

		List<Integer> ids = new ArrayList<>();
		for(RichMember rm: richMembers) {
			ids.add(rm.getId());
		}

		assertTrue(ids.contains(createdMember.getId()));
	}

	@Test
	public void createMemberFromUserInGroup() throws Exception {
		System.out.println(CLASS_NAME + "createMember");

		//get user of existing member
		User user = perun.getUsersManagerBl().getUserByMember(sess, createdMember);
		perun.getMembersManager().deleteMember(sess, createdMember);

		List<Group> groups = new ArrayList<>(Collections.singletonList(g3ing1));

		Member member = perun.getMembersManager().createMember(sess, createdVo, user, groups);

		//test if member is in vo and also in defined groups
		assertTrue(perun.getMembersManagerBl().getMembers(sess, createdVo).contains(member));
		List<Group> returnedGroups = perun.getGroupsManagerBl().getMemberGroups(sess, member);

		assertTrue(returnedGroups.contains(g1));
		assertTrue(!returnedGroups.contains(g2));
		assertTrue(returnedGroups.contains(g3ing1));
	}

	@Test
	public void createMemberFromCandidateWithExtSourceInGroup() throws Exception {
		System.out.println(CLASS_NAME + "createMember");

		List<Group> groups = new ArrayList<>(Collections.singletonList(g3ing1));

		//Create new locale member for puprose of this method
		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));
		Candidate candidate = new Candidate();
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		UserExtSource ues = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), extLogin);
		candidate.setAttributes(new HashMap<>());

		Member member = perun.getMembersManager().createMember(sess, createdVo, ues.getExtSource().getName(), ues.getExtSource().getType(), ues.getLogin(), candidate, groups);

		//test if member is in vo and also in defined groups
		assertTrue(perun.getMembersManagerBl().getMembers(sess, createdVo).contains(member));
		List<Group> returnedGroups = perun.getGroupsManagerBl().getMemberGroups(sess, member);

		assertTrue(returnedGroups.contains(g1));
		assertTrue(!returnedGroups.contains(g2));
		assertTrue(returnedGroups.contains(g3ing1));

		// save user for deletion after test
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
	}

	@Test
	public void createMemberFromCandidateWithExtSourceAndLoaInGroup() throws Exception {
		System.out.println(CLASS_NAME + "createMember");

		//g3in1 - direct, g1 indirect
		List<Group> groups = new ArrayList<>(Collections.singletonList(g3ing1));

		//Create new locale member for puprose of this method
		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));
		Candidate candidate = new Candidate();
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		UserExtSource ues = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), extLogin);
		candidate.setAttributes(new HashMap<>());

		Member member = perun.getMembersManager().createMember(sess, createdVo, ues.getExtSource().getName(), ues.getExtSource().getType(), 2, ues.getLogin(), candidate, groups);

		//test if member is in vo and also in defined groups
		assertTrue(perun.getMembersManagerBl().getMembers(sess, createdVo).contains(member));
		List<Group> returnedGroups = perun.getGroupsManagerBl().getMemberGroups(sess, member);

		assertTrue(returnedGroups.contains(g1));
		assertTrue(!returnedGroups.contains(g2));
		assertTrue(returnedGroups.contains(g3ing1));

		// save user for deletion after test
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
	}

	@Test
	public void createServiceMemberFromCandidateInGroup() throws Exception {
		System.out.println(CLASS_NAME + "createServiceMember");

		//g3in1 - direct, g1 indirect
		List<Group> groups = new ArrayList<>(Collections.singletonList(g3ing1));

		//Create new locale member for puprose of this method
		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));
		Candidate candidate = new Candidate();
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		UserExtSource ues = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), extLogin);
		candidate.setUserExtSource(ues);
		candidate.setAttributes(new HashMap<>());

		List<User> specificUserOwners = new ArrayList<>();
		specificUserOwners.add(perun.getUsersManagerBl().getUserByMember(sess, createdMember));

		Member member = perun.getMembersManager().createSpecificMember(sess, createdVo, candidate, specificUserOwners, SpecificUserType.SERVICE, groups);

		//test if member is in vo and also in defined groups
		assertTrue(perun.getMembersManagerBl().getMembers(sess, createdVo).contains(member));
		List<Group> returnedGroups = perun.getGroupsManagerBl().getMemberGroups(sess, member);

		assertTrue(returnedGroups.contains(g1));
		assertTrue(!returnedGroups.contains(g2));
		assertTrue(returnedGroups.contains(g3ing1));

		// save user for deletion after test
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
	}

	@Test (expected=VoNotExistsException.class)
	public void createMemeberWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "createMemberSyncWhenVoNotExists");

		membersManagerEntry.createMember(sess, new Vo(), candidate);

	}

	@Test (expected=AlreadyMemberException.class)
	public void createMemeberWhenAlreadyMember() throws Exception {
		System.out.println(CLASS_NAME + "createMemberSyncWhenAlreadyMember");

		membersManagerEntry.createMember(sess, createdVo, candidate);
		// shouldn't add member twice

	}

	@Test
	public void getMemberById() throws Exception {
		System.out.println(CLASS_NAME + "getMemberById");


		final Member m = membersManagerEntry.getMemberById(sess,
				createdMember.getId());

		assertNotNull("unable to get member", m);
		assertEquals("returned member is not same as stored", createdMember.getId(), m.getId());

	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberByIdWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberByIdWhenMemberNotExists");

		membersManagerEntry.getMemberById(sess, 0);
		// shouldn't find member with ID 0

	}

	@Test
	public void getMemberByExtAuth() throws Exception {
		System.out.println(CLASS_NAME + "getMemberByExtAuth");

		final Member expectedMember = membersManagerEntry.getMemberByUserExtSource(sess, createdVo, ues);
		assertNotNull("unable to return member by Ext auth", expectedMember);
		assertEquals("created and returned member is not the same", createdMember, expectedMember);

	}

	@Test (expected=VoNotExistsException.class)
	public void getMemberByExtAuthWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberByExtAuthWhenVoNotExists");

		membersManagerEntry.getMemberByUserExtSource(sess, new Vo(), ues);
		// shouldn't find VO

	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberByExtAuthWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberByExtAuthWhenMemberNotExists");

		final UserExtSource ues2 = ues;
		ues2.setLogin("neexistuje");
		membersManagerEntry.getMemberByUserExtSource(sess, createdVo, ues2);
		// shouldn't find Member

	}

	@Test
	public void getMemberByUser() throws Exception {
		System.out.println(CLASS_NAME + "getMemberByUser");

		final User u = perun.getUsersManager().getUserByUserExtSource(sess, ues);

		final Member expectedMember = membersManagerEntry.getMemberByUser(sess, createdVo, u);
		assertNotNull(expectedMember);
		assertEquals(createdMember, expectedMember);

	}

	@Test (expected=VoNotExistsException.class)
	public void getMemberByUserWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberByUserWhenVoNotExists");

		final User u = perun.getUsersManager().getUserByUserExtSource(sess, ues);

		membersManagerEntry.getMemberByUser(sess, new Vo(), u);
		// shouldn't find VO

	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberByUserWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberByUserWhenMemberNotExists");

		final User u = perun.getUsersManager().getUserByUserExtSource(sess, ues);
		membersManagerEntry.deleteMember(sess, createdMember);
		membersManagerEntry.getMemberByUser(sess, createdVo, u);
		// shouldn't find member

	}

	@Test (expected=UserNotExistsException.class)
	public void getMemberByUserWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberByUserWhenUserNotExists");

		final User u = perun.getUsersManager().getUserByUserExtSource(sess, ues);
		u.setId(0);
		membersManagerEntry.getMemberByUser(sess, createdVo, u);
		// shouldn't find user

	}

	@Test
	public void getMemberVo() throws Exception {
		System.out.println(CLASS_NAME + "getMemberVo");

		Vo vo = membersManagerEntry.getMemberVo(sess, createdMember);
		assertNotNull("unable to get VO by member", vo);
		assertEquals("saved and returned member's Vo is not the same", vo, createdVo);

	}

	@Test (expected=MemberNotExistsException.class)
	public void getMemberVoWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMemberVoWhenMemberNotExists");

		membersManagerEntry.getMemberVo(sess, new Member());
		// shouldn't find Member

	}

	@Test
	public void getMembersCount() throws Exception {
		System.out.println(CLASS_NAME + "getMembersCount");

		final int count = membersManagerEntry.getMembersCount(sess, createdVo);
		assertTrue("testing VO should have only 1 member", count == 1);

	}

	@Test (expected=VoNotExistsException.class)
	public void getMembersCountWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMembersCountWhenVoNotExists");

		membersManagerEntry.getMembersCount(sess, new Vo());
		// shouldn't find VO

	}

	@Test
	public void getMembersCountByStatus() throws Exception {
		System.out.println(CLASS_NAME + "getMembersCountByStatus");

		final int count = membersManagerEntry.getMembersCount(sess, createdVo, Status.SUSPENDED);
		assertTrue("testing VO should have 0 members with SUSPENDED status", count == 0);
		final int count2 = membersManagerEntry.getMembersCount(sess, createdVo, Status.VALID);
		assertTrue("testing VO should have 1 member with VALID status", count2 == 1);

	}

	@Test
	public void getMembers() throws Exception {
		System.out.println(CLASS_NAME + "getMembers");

		List<Member> members = membersManagerEntry.getMembers(sess, createdVo);
		assertTrue("should return only 1 member",members.size() == 1);
		assertTrue("should return our member",members.contains(createdMember));

	}

	@Test (expected=VoNotExistsException.class)
	public void getMembersWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMembersWhenVoNotExists");

		membersManagerEntry.getMembers(sess, new Vo());
		// shouldn't find VO

	}

	@Test
	public void getMembersByUser() throws Exception {
		System.out.println(CLASS_NAME + "getMembersByUser");

		final User u = perun.getUsersManager().getUserByMember(sess, createdMember);
		List<Member> members = membersManagerEntry.getMembersByUser(sess, u);
		assertNotNull("unable to return members by user", members);
		assertTrue("should return 1 member by user", members.contains(createdMember));

	}

	@Test (expected=UserNotExistsException.class)
	public void getMembersByUserWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getMembersByUserWhenUserNotExists");

		final User u = perun.getUsersManager().getUserByMember(sess, createdMember);
		u.setId(0);
		membersManagerEntry.getMembersByUser(sess, u);
		// shouldn't find user

	}

	@Test(expected=MemberNotExistsException.class)
	public void deleteMember() throws Exception {
		System.out.println(CLASS_NAME + "deleteMember");

		//ensure that the test-member already exists..
		assertNotNull(membersManagerEntry.getMemberById(sess, createdMember.getId()));
		membersManagerEntry.deleteMember(sess, createdMember);

		membersManagerEntry.getMemberById(sess, createdMember.getId());

	}

	@Test(expected=MemberNotExistsException.class)
	public void deleteMemberWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteMemberWhenMemberNotExists");

		membersManagerEntry.deleteMember(sess, new Member());

	}

	@Test(expected=MemberNotExistsException.class)
	public void deleteAllMembers() throws Exception {
		System.out.println(CLASS_NAME + "deleteAllMembers");

		//ensure that the test-member already exists..
		assertNotNull(membersManagerEntry.getMemberById(sess, createdMember.getId()));
		membersManagerEntry.deleteAllMembers(sess, createdVo);
		membersManagerEntry.getMemberById(sess, createdMember.getId());

	}

	@Test(expected=VoNotExistsException.class)
	public void deleteAllMembersWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteAllMembersWhenVoNotExists");

		membersManagerEntry.deleteAllMembers(sess, new Vo());

	}

	@Test
	public void extendMembershipToParticularDate() throws Exception {
		System.out.println(CLASS_NAME + "extendMembershipToParticularDate");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		LocalDate extendedDate = LocalDate.parse((String) membershipAttribute.getValue());

		// Set to 1.1. next year
		LocalDate requiredDate = LocalDate.of(LocalDate.now().getYear()+1, 1, 1);

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredDate.getYear(), extendedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), extendedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), extendedDate.getDayOfMonth());
	}

	@Test
	public void extendMembershipBy10Days() throws Exception {
		System.out.println(CLASS_NAME + "extendMembershipBy10Days");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "+10d");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		LocalDate extendedDate = LocalDate.parse((String) membershipAttribute.getValue());

		LocalDate requiredDate = LocalDate.now().plusDays(10); // Add 10 days to today

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredDate.getYear(), extendedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), extendedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), extendedDate.getDayOfMonth());
	}

	@Test
	public void extendMembershipInGracePeriod() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipInGracePeriod");

		// Period will be set to the next day
		LocalDate date = LocalDate.now().plusDays(1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		// Set period to day after today
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, date.getDayOfMonth() + "." + date.getMonthValue() + ".");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "1m");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		LocalDate extendedDate = LocalDate.parse((String) membershipAttribute.getValue());

		LocalDate requiredDate = LocalDate.now().plusDays(1).plusYears(1);

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredDate.getYear(), extendedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonth(), extendedDate.getMonth());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), extendedDate.getDayOfMonth());
	}

	@Test
	public void extendMembershipOutsideGracePeriod() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipOutsideGracePeriod");

		// Set period to three months later
		LocalDate date = LocalDate.now().plusMonths(3);


		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		// Set perid to day after today
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, date.getDayOfMonth() + "." + date.getMonthValue() + ".");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "1m");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		LocalDate extendedDate = LocalDate.parse((String) membershipAttribute.getValue());

		LocalDate requiredDate = LocalDate.now().plusMonths(3);

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredDate.getYear(), extendedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), extendedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), extendedDate.getDayOfMonth());
	}

	@Test
	public void extendMembershipForMemberWithSufficientLoa() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipForMemberWithSufficientLoa");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0,1");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set LOA 2 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, EXT_SOURCE_NAME);
		ues = new UserExtSource(es, "abc");
		ues.setLoa(2);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		usersManagerEntry.addUserExtSource(sess, user, ues);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		LocalDate extendedDate = LocalDate.parse((String) membershipAttribute.getValue());

		// Set to 1.1. next year
		LocalDate requiredDate = LocalDate.of(LocalDate.now().getYear()+1, 1, 1);

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredDate.getYear(), extendedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), extendedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), extendedDate.getDayOfMonth());
	}

	@Test
	public void extendMembershipForMemberWithInsufficientLoa() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipForMemberWithInsufficientLoa");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0,1");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		Attribute membershipExpirationAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":membershipExpiration"));
		LocalDate date = LocalDate.now();
		membershipExpirationAttribute.setValue(date.toString());
		attributesManagerEntry.setAttribute(sess, createdMember, membershipExpirationAttribute);

		// Set LOA 1 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, EXT_SOURCE_NAME);
		ues = new UserExtSource(es, "abc");
		ues.setLoa(1);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		usersManagerEntry.addUserExtSource(sess, user, ues);

		// Try to extend membership
		try {
			membersManagerEntry.extendMembership(sess, createdMember);
		} catch (ExtendMembershipException e) {
			assertTrue(e.getReason().equals(ExtendMembershipException.Reason.INSUFFICIENTLOAFOREXTENSION));
		}

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertEquals("membership attribute value must contains same value as before extension.",
				date.toString(), membershipAttribute.getValue()); // Attribute cannot contain any value
	}

	@Test
	public void extendMembershipForDefinedLoaAllowed() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipForDefinedLoaAllowed");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName, "1|+1m");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set LOA 1 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, EXT_SOURCE_NAME);
		ues = new UserExtSource(es, "abc");
		ues.setLoa(1);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		usersManagerEntry.addUserExtSource(sess, user, ues);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		// Try to extend membership once again
		membersManagerEntry.extendMembership(sess, createdMember);

		membershipAttribute = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		LocalDate extendedDate = LocalDate.parse((String) membershipAttribute.getValue());

		LocalDate requiredDate = LocalDate.now().plusMonths(1);

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());
		assertEquals("Year must match", requiredDate.getYear(), extendedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), extendedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), extendedDate.getDayOfMonth());
	}

	@Test
	public void canExtendMembershipForDefinedLoaNotAllowed() throws Exception {
		System.out.println(CLASS_NAME + "extendMembershipForDefinedLoaNotAllowed");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName, "1|+1m.");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set LOA 1 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, EXT_SOURCE_NAME);
		ues = new UserExtSource(es, "abc");
		ues.setLoa(0);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		usersManagerEntry.addUserExtSource(sess, user, ues);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttributeFirst = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttributeFirst);
		assertNotNull("membership attribute value must be set", membershipAttributeFirst.getValue());

		// Try to extend membership
		assertFalse(membersManagerEntry.canExtendMembership(sess, createdMember));
	}

	@Test
	public void canExtendMembershipForDefinedLoaNotAllowedAndServiceUser() throws Exception {
		System.out.println(CLASS_NAME + "extendMembershipForDefinedLoaNotAllowedAndServiceUser");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName, "1|+1m.");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set LOA 1 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, EXT_SOURCE_NAME);
		UserExtSource uesService = new UserExtSource(es, "abc");
		uesService.setLoa(0);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		//usersManagerEntry.addUserExtSource(sess, user, ues);

		Candidate serviceCandidate = new Candidate();
		serviceCandidate.setServiceUser(true);
		serviceCandidate.setFirstName("");
		serviceCandidate.setLastName("");
		serviceCandidate.setId(0);
		serviceCandidate.setUserExtSource(uesService);
		serviceCandidate.setAttributes(new HashMap<>());

		Member serviceMember = perun.getMembersManager().createSpecificMember(sess, createdVo, serviceCandidate, Collections.singletonList(user), SpecificUserType.SERVICE);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, serviceMember);

		Attribute membershipAttributeFirst = attributesManagerEntry.getAttribute(sess, serviceMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttributeFirst);
		assertNotNull("membership attribute value must be set", membershipAttributeFirst.getValue());

		// Try to extend membership - must pass since user is service user
		assertTrue(membersManagerEntry.canExtendMembership(sess, serviceMember));

	}

	@Test
	// It extend membership and try to extend it again, it must decline another expiration
	public void canExtendMembershipInGracePeriod() throws Exception {
		System.out.println(CLASS_NAME + "canExtendMembershipInGracePeriod");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName, "1|+1m.");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set LOA 1 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, EXT_SOURCE_NAME);
		ues = new UserExtSource(es, "abc");
		ues.setLoa(1);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);
		usersManagerEntry.addUserExtSource(sess, user, ues);

		// Try to extend membership
		membersManagerEntry.extendMembership(sess, createdMember);

		Attribute membershipAttributeFirst = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttributeFirst);
		assertNotNull("membership attribute value must be set", membershipAttributeFirst.getValue());

		// Try to extend membership
		assertFalse(membersManagerEntry.canExtendMembership(sess, createdMember));
	}


	@Test
	public void canExtendMembershipInGracePeriodRelativeDate() throws Exception {
		System.out.println(CLASS_NAME + "canExtendMembershipInGracePeriodRelativeDate");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "+6m");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "2d");
		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);
		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set expiration date to tomorrow (one day after grace period begun)
		LocalDate date = LocalDate.now().plusDays(1);
		Attribute mebershipExpiration = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":membershipExpiration"));
		mebershipExpiration.setValue(date.toString());
		attributesManagerEntry.setAttribute(sess, createdMember, mebershipExpiration);

		// Check if enviroment is set properly
		mebershipExpiration = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
		assertNotNull("membership attribute must be set", mebershipExpiration);
		assertNotNull("membership attribute value must be set", mebershipExpiration.getValue());
		extendMembershipRulesAttribute = attributesManagerEntry.getAttribute(sess, createdVo, AttributesManager.NS_VO_ATTR_DEF + ":membershipExpirationRules");
		assertNotNull("membership rules must be set", extendMembershipRulesAttribute);
		assertNotNull("membership rules value must be set", extendMembershipRulesAttribute.getValue());

		// Check if membership can be extended
		assertTrue(membersManagerEntry.canExtendMembership(sess, createdMember));
	}

	@Test
	public void canExtendMembershipOutOfGracePeriodRelativeDate() throws Exception {
		System.out.println(CLASS_NAME + "canExtendMembershipOutOfGracePeriodRelativeDate");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "+6m");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "2d");
		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);
		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set expiration date to three days after. (one day untill grace period begins)
		LocalDate date = LocalDate.now().plusDays(3);
		Attribute mebershipExpiration = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":membershipExpiration"));
		mebershipExpiration.setValue(date.toString());
		attributesManagerEntry.setAttribute(sess, createdMember, mebershipExpiration);

		// Check if enviroment is set properly
		mebershipExpiration = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
		assertNotNull("membership attribute must be set", mebershipExpiration);
		assertNotNull("membership attribute value must be set", mebershipExpiration.getValue());
		extendMembershipRulesAttribute = attributesManagerEntry.getAttribute(sess, createdVo, AttributesManager.NS_VO_ATTR_DEF + ":membershipExpirationRules");
		assertNotNull("membership rules must be set", extendMembershipRulesAttribute);
		assertNotNull("membership rules value must be set", extendMembershipRulesAttribute.getValue());

		// Check if membership can be extended
		assertFalse(membersManagerEntry.canExtendMembership(sess, createdMember));
	}

	@Test
	public void canExtendMembershipInGracePeriodAbsoluteDate() throws Exception {
		System.out.println(CLASS_NAME + "canExtendMembershipInGracePeriodAbsoluteDate");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "2d");
		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);
		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set expiration date to tomorrow (one day after grace period begun)
		LocalDate date = LocalDate.now().plusDays(1);
		Attribute mebershipExpiration = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":membershipExpiration"));
		mebershipExpiration.setValue(date.toString());
		attributesManagerEntry.setAttribute(sess, createdMember, mebershipExpiration);

		// Check if enviroment is set properly
		mebershipExpiration = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
		assertNotNull("membership attribute must be set", mebershipExpiration);
		assertNotNull("membership attribute value must be set", mebershipExpiration.getValue());
		extendMembershipRulesAttribute = attributesManagerEntry.getAttribute(sess, createdVo, AttributesManager.NS_VO_ATTR_DEF + ":membershipExpirationRules");
		assertNotNull("membership rules must be set", extendMembershipRulesAttribute);
		assertNotNull("membership rules value must be set", extendMembershipRulesAttribute.getValue());

		// Check if membership can be extended
		assertTrue(membersManagerEntry.canExtendMembership(sess, createdMember));
	}

	@Test
	public void canExtendMembershipOutOfGracePeriodAbsoluteDate() throws Exception {
		System.out.println(CLASS_NAME + "canExtendMembershipOutOfGracePeriodAbsoluteDate");

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "2d");
		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);
		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		// Set expiration date to three days after. (one day untill grace period begins)
		LocalDate date = LocalDate.now().plusDays(3);
		Attribute mebershipExpiration = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":membershipExpiration"));
		mebershipExpiration.setValue(date.toString());
		attributesManagerEntry.setAttribute(sess, createdMember, mebershipExpiration);

		// Check if enviroment is set properly
		mebershipExpiration = attributesManagerEntry.getAttribute(sess, createdMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
		assertNotNull("membership attribute must be set", mebershipExpiration);
		assertNotNull("membership attribute value must be set", mebershipExpiration.getValue());
		extendMembershipRulesAttribute = attributesManagerEntry.getAttribute(sess, createdVo, AttributesManager.NS_VO_ATTR_DEF + ":membershipExpirationRules");
		assertNotNull("membership rules must be set", extendMembershipRulesAttribute);
		assertNotNull("membership rules value must be set", extendMembershipRulesAttribute.getValue());

		// Check if membership can be extended
		assertFalse(membersManagerEntry.canExtendMembership(sess, createdMember));
	}

	@Test
	// It checks if user with insufficient LoA won't be allowed in to the VO
	public void canBeMemberLoaNotAllowed() throws Exception {
		System.out.println(CLASS_NAME + "canBeMemberLoaNotAllowed");

		String loa = "1";

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotAllowLoaKeyName, loa);

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);

		// Try to extend membership
		assertFalse(membersManagerEntry.canBeMember(sess, createdVo, user, loa));

	}

	@Test
	// It checks if service user can be member independent of his LoA
	public void canBeMemberLoaNotAllowedServiceUser() throws Exception {
		System.out.println(CLASS_NAME + "canBeMemberLoaNotAllowedServiceUser");

		String loa = "1";

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotAllowLoaKeyName, loa);

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);

		// Set LOA 1 for member
		ExtSource es = perun.getExtSourcesManagerBl().getExtSourceByName(sess, EXT_SOURCE_NAME);
		UserExtSource uesService = new UserExtSource(es, "abc");
		uesService.setLoa(1);

		Candidate serviceCandidate = new Candidate();
		serviceCandidate.setServiceUser(true);
		serviceCandidate.setFirstName("");
		serviceCandidate.setLastName("");
		serviceCandidate.setId(0);
		serviceCandidate.setUserExtSource(uesService);
		serviceCandidate.setAttributes(new HashMap<>());

		Member serviceMember = perun.getMembersManager().createSpecificMember(sess, createdVo, serviceCandidate, Collections.singletonList(user), SpecificUserType.SERVICE);
		User serviceUser = usersManagerEntry.getUserByMember(sess, serviceMember);

		// Must return true even if loa is not allowed
		assertTrue(membersManagerEntry.canBeMember(sess, createdVo, serviceUser, loa));

	}

	@Test
	// It checks if user with sufficient LoA will be allowed in to the VO
	public void canBeMemberLoaAllowed() throws Exception {
		System.out.println(CLASS_NAME + "canBeMemberLoaAllowed");

		String loa = "1";

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotAllowLoaKeyName, loa);

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_VO_ATTR_DEF+":membershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManagerEntry.setAttribute(sess, createdVo, extendMembershipRulesAttribute);

		User user = usersManagerEntry.getUserByMember(sess, createdMember);

		// Try to extend membership
		String allowedLoa = "2";
		assertTrue(membersManagerEntry.canBeMember(sess, createdVo, user, allowedLoa));
	}

	@Test
	public void findMembersInGroup() throws Exception {
		System.out.println(CLASS_NAME + "findMembersInGroup");
		Member member = setUpMember(createdVo);
		Member member2 = setUpMember2(createdVo);
		groupsManagerEntry.addMember(sess, createdGroup, member);
		groupsManagerEntry.addMember(sess, createdGroup, member2);

		List<Member> foundMembers = membersManagerEntry.findMembersInGroup(sess, createdGroup, "FirstTest");
		assertTrue(foundMembers.contains(member));
		foundMembers.remove(member);
		assertTrue(foundMembers.isEmpty());

		foundMembers = membersManagerEntry.findMembersInGroup(sess, createdGroup, "123@456.cz");
		assertTrue(foundMembers.contains(member2));
		foundMembers.remove(member2);
		assertTrue(foundMembers.isEmpty());

		foundMembers = membersManagerEntry.findMembersInGroup(sess, createdGroup, "stsdg");
		assertTrue(foundMembers.isEmpty());

		foundMembers = membersManagerEntry.findMembersInGroup(sess, createdGroup, "TestovaciLogin");
		assertTrue(foundMembers.contains(member));
		foundMembers.remove(member);
		assertTrue(foundMembers.isEmpty());
	}

	@Ignore
	@Test
	public void findMembersByName() throws Exception {
		System.out.println(CLASS_NAME + "findMembersByName");

		List<Member> members = membersManagerEntry.findMembersByName(sess, "Pepa Z Depa");

		assertTrue("results must contain at least one member",members.size() >= 1);
		assertTrue("results must contain member \"Pepa z Depa\"", members.contains(createdMember));
	}

	@Test
	public void getMemberByExtSourceNameAndExtLogin() throws Exception {
		System.out.println(CLASS_NAME + "getMemberByExtSourceNameAndExtLogin");

		Member member = setUpMember(createdVo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);

		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));
		UserExtSource userExtSource = new UserExtSource();
		ExtSource externalSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
		externalSource = perun.getExtSourcesManagerBl().createExtSource(sess, externalSource, null);
		// gets real external source object from database
		userExtSource.setExtSource(externalSource);
		// put real external source into user's external source
		userExtSource.setLogin(extLogin);
		// set users login in his ext source
		userExtSource = perun.getUsersManagerBl().addUserExtSource(sess, user, userExtSource);

		String extSourceName = userExtSource.getExtSource().getName();
		extLogin = userExtSource.getLogin();
		Member returnedMember = membersManagerEntry.getMemberByExtSourceNameAndExtLogin(sess, createdVo, extSourceName, extLogin);
		assertEquals("members should be the same",member, returnedMember);

	}

	@Test
	public void createSponsoredMember() throws Exception {
		System.out.println(CLASS_NAME + "createSponsoredMember");
		//create user in group sponsors with role SPONSOR
		Member sponsorMember = setUpSponsor(createdVo);
		User sponsorUser = perun.getUsersManagerBl().getUserByMember(sess, sponsorMember);
		Group sponsors = new Group("sponsors","users able to sponsor");
		sponsors = perun.getGroupsManagerBl().createGroup(sess,createdVo,sponsors);
		AuthzResolverBlImpl.setRole(sess, sponsors, createdVo, Role.SPONSOR);
		perun.getGroupsManagerBl().addMember(sess,sponsors,sponsorMember);
		//create guest
		assertTrue("user must have SPONSOR role", perun.getVosManagerBl().isUserInRoleForVo(sess, sponsorUser, Role.SPONSOR, createdVo, true));
		Member sponsoredMember = perun.getMembersManagerBl().createSponsoredMember(sess, createdVo, "dummy", "Ing. Jiří Novák, CSc.", "secret", sponsorUser, false);
		assertNotNull("sponsored member must not be null",sponsoredMember);
		assertTrue("sponsored memer must have flag 'sponsored' set",sponsoredMember.isSponsored());
		assertTrue("sponsored member should have status VALID",sponsoredMember.getStatus()==Status.VALID);
		//remove sponsor from sponsor group, thus the user loses role and member get expired
		perun.getGroupsManagerBl().removeMember(sess,sponsors,sponsorMember);
		//refresh from DB
		sponsoredMember = perun.getMembersManagerBl().getMemberById(sess,sponsoredMember.getId());
		assertTrue("sponsored member without sponsors should still have flag sponsored",sponsoredMember.isSponsored());
		assertTrue("sponsored member without sponsors must be expired",sponsoredMember.getStatus()==Status.EXPIRED);
	}

	@Test
	public void setAndUnsetSponsorshipForMember() throws Exception {
		System.out.println(CLASS_NAME + "setAndUnsetSponsorshipForMember");
		Member sponsorMember = setUpSponsor(createdVo);
		User sponsorUser = perun.getUsersManagerBl().getUserByMember(sess, sponsorMember);
		Group sponsors = new Group("sponsors","users able to sponsor");
		sponsors = perun.getGroupsManagerBl().createGroup(sess,createdVo,sponsors);
		AuthzResolverBlImpl.setRole(sess, sponsors, createdVo, Role.SPONSOR);
		perun.getGroupsManagerBl().addMember(sess, sponsors, sponsorMember);

		Member memberToBeSponsored = setUpMember(createdVo);
		assertTrue("member shouldn't be created as sponsored one", !memberToBeSponsored.isSponsored());

		perun.getMembersManager().setSponsorshipForMember(sess, memberToBeSponsored, sponsorUser);

		Member newSponsoredMember = perun.getMembersManagerBl().getMemberById(sess, memberToBeSponsored.getId());
		assertTrue("member should be sponsored now", newSponsoredMember.isSponsored());
		assertEquals("there should be exactly 1 sponsor at this moment",1, perun.getUsersManagerBl().getSponsors(sess, newSponsoredMember).size());

		perun.getMembersManager().unsetSponsorshipForMember(sess, memberToBeSponsored);

		newSponsoredMember = perun.getMembersManagerBl().getMemberById(sess, memberToBeSponsored.getId());
		assertTrue("member shouldn't be sponsored any more",!newSponsoredMember.isSponsored());
	}

	@Test
	public void addSponsor() throws Exception {
		System.out.println(CLASS_NAME + "addSponsor");
		//create user which can sponsor
		User sponsorUser = perun.getUsersManagerBl().getUserByMember(sess, setUpSponsor(createdVo));
		AuthzResolverBlImpl.setRole(sess, sponsorUser, createdVo, Role.SPONSOR);
		assertTrue("user must have SPONSOR role", perun.getVosManagerBl().isUserInRoleForVo(sess, sponsorUser, Role.SPONSOR, createdVo, true));
		//create another user which can sponsor
		User sponsorUser2 = perun.getUsersManagerBl().getUserByMember(sess, setUpSponsor2(createdVo));
		AuthzResolverBlImpl.setRole(sess, sponsorUser2, createdVo, Role.SPONSOR);
		assertTrue("user must have SPONSOR role", perun.getVosManagerBl().isUserInRoleForVo(sess, sponsorUser2, Role.SPONSOR, createdVo, true));
		//create user that cannot sponsor
		User notsponsorUser = perun.getUsersManagerBl().getUserByMember(sess, setUpNotSponsor(createdVo));
		assertFalse("user must not have SPONSOR role", perun.getVosManagerBl().isUserInRoleForVo(sess, notsponsorUser, Role.SPONSOR, createdVo, true));
		//create sponsored member
		Member sponsoredMember = perun.getMembersManagerBl().createSponsoredMember(sess, createdVo, "dummy", "Ing. Jiří Novák, CSc.", "secret", sponsorUser, false);
		assertNotNull("sponsored member must not be null", sponsoredMember);
		assertTrue("sponsored memer must have flag 'sponsored' set", sponsoredMember.isSponsored());
		assertTrue("sponsored member should have status VALID", sponsoredMember.getStatus() == Status.VALID);
		//try add user that cannot sponsor, should fail
		try {
			perun.getMembersManager().sponsorMember(sess, sponsoredMember, notsponsorUser);
			fail("user cannot sponsor but was added as sponsor");
		} catch (UserNotInRoleException ex) {
			//expected
		}
		//try to add user that already is sponsor, should fail
		try {
			perun.getMembersManager().sponsorMember(sess, sponsoredMember, sponsorUser);
			fail("user cannot sponsor twice a single member");
		} catch (AlreadySponsorException ex) {
			//expected
		}
		//try to add sponsor, should succeed
		perun.getMembersManager().sponsorMember(sess, sponsoredMember, sponsorUser2);
		List<User> sponsors = perun.getUsersManagerBl().getSponsors(sess, sponsoredMember);
		assertTrue("sponsor 1 is not reported as sponsor", sponsors.contains(sponsorUser));
		assertTrue("sponsor 2 is not reported as sponsor", sponsors.contains(sponsorUser2));
		assertTrue("unexpected sponsors", sponsors.size() == 2);
		//check that it is reported
		List<RichMember> sponsoredMembers1 = perun.getMembersManager().getSponsoredMembers(sess, createdVo, sponsorUser);
		assertTrue("member is not in list of sponsored members for sponsor 1", sponsoredMembers1.stream().map(PerunBean::getId).anyMatch(id -> id == sponsoredMember.getId()));
		List<RichMember> sponsoredMembers2 = perun.getMembersManager().getSponsoredMembers(sess, createdVo, sponsorUser2);
		assertTrue("member is not in list of sponsored members for sponsor 2", sponsoredMembers2.stream().map(PerunBean::getId).anyMatch(id -> id == sponsoredMember.getId()));
	}


	private Attribute setUpAttribute(String type, String friendlyName, String namespace, Object value) throws Exception {
		Attribute attr = new Attribute();
		attr.setNamespace(namespace);
		attr.setFriendlyName(friendlyName);
		attr.setType(type);
		attr.setValue(value);
		attr.setDescription("TEST DESCRIPTION");
		assertNotNull("unable to create " + attr.getName() + " attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		return attr;
	}


	private Member setUpMember(Vo vo) throws Exception {

		Candidate candidate = setUpCandidate();
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		Attribute attrEmail = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":mail"));
		attrEmail.setValue("test@test.test");
		attributesManagerEntry.setAttribute(sess, member, attrEmail);

		User user = usersManagerEntry.getUserByMember(sess, member);
		Attribute attrLogin = new Attribute();
		attrLogin.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin.setFriendlyName("login-namespace:testMichal");
		attrLogin.setType(String.class.getName());
		attrLogin = new Attribute(attributesManagerEntry.createAttribute(sess, attrLogin));
		attrLogin.setValue("TestovaciLogin");
		attributesManagerEntry.setAttribute(sess, user, attrLogin);
		return member;

	}

	private Member setUpMember2(Vo vo) throws Exception {

		Candidate candidate = setUpCandidate2();
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		Attribute attrEmail = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":mail"));
		attrEmail.setValue("123@456.cz");
		attributesManagerEntry.setAttribute(sess, member, attrEmail);

		User user = usersManagerEntry.getUserByMember(sess, member);
		Attribute attrLogin = new Attribute();
		attrLogin.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin.setFriendlyName("login-namespace:testMichal2");
		attrLogin.setType(String.class.getName());
		attrLogin = new Attribute(attributesManagerEntry.createAttribute(sess, attrLogin));
		attrLogin.setValue("123456");
		attributesManagerEntry.setAttribute(sess, user, attrLogin);
		return member;

	}

	private Member setUpSponsor(Vo vo) throws Exception {
		Candidate candidate = setUpCandidateSponsor();
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		Attribute attrEmail = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":mail"));
		attrEmail.setValue("jan@sponsor.cz");
		attributesManagerEntry.setAttribute(sess, member, attrEmail);

		User user = usersManagerEntry.getUserByMember(sess, member);
		Attribute attrLogin = new Attribute();
		attrLogin.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrLogin.setFriendlyName("login-namespace:dummy");
		attrLogin.setType(String.class.getName());
		attrLogin = new Attribute(attributesManagerEntry.createAttribute(sess, attrLogin));
		attrLogin.setValue("111111");
		attributesManagerEntry.setAttribute(sess, user, attrLogin);
		return member;
	}

	private Member setUpSponsor2(Vo vo) throws Exception {
		Candidate candidate = setUpCandidateSponsor2();
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		Attribute attrEmail = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":mail"));
		attrEmail.setValue("jan@sponsor.cz");
		attributesManagerEntry.setAttribute(sess, member, attrEmail);
		return member;
	}

	private Member setUpNotSponsor(Vo vo) throws Exception {
		Candidate candidate = setUpCandidateNotSponsor();
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		Attribute attrEmail = new Attribute(attributesManagerEntry.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_ATTR_DEF+":mail"));
		attrEmail.setValue("petr@sponsored.cz");
		attributesManagerEntry.setAttribute(sess, member, attrEmail);
		return member;
	}

	private Candidate setUpCandidate() {

		String userFirstName = "FirstTest";
		String userLastName = "LastTest";
		String extLogin = "ExtLoginTest";

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;

	}

	private Candidate setUpCandidate2() {

		String userFirstName = "Abcd";
		String userLastName = "Efgh";
		String extLogin = "Ijkl";

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;

	}

	private Candidate setUpCandidateSponsor() {

		String userFirstName = "Jan";
		String userLastName = "Sponzor";
		String extLogin = "aaaaaaa";

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;
	}

	private Candidate setUpCandidateSponsor2() {

		String userFirstName = "Pavel";
		String userLastName = "Sponzor2";
		String extLogin = "bbbbb";

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;
	}
	private Candidate setUpCandidateNotSponsor() {

		String userFirstName = "Petr";
		String userLastName = "Nesponzor";
		String extLogin = "cccccc";

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("RNDr.");
		candidate.setTitleAfter("Ph.D.");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());
		return candidate;
	}
}
