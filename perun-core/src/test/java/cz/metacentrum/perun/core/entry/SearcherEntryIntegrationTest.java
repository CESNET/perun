package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.*;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import org.junit.Before;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.bl.SearcherBl;

/**
 * Integration tests of Searcher
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class SearcherEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private final static String CLASS_NAME = "Searcher.";

	// these are in DB only when needed and must be setUp"type"() in right order before use !!
	private User user1;                             // our User
	private User user2;
	private Member member1;
	private Member member2;
	private Candidate candidate1;
	private Candidate candidate2;
	private Vo vo;
	String extLogin = "aaa";              // his login in external source
	String extLogin2 = "bbb";
	String extSourceName = "SearcherEntryIntegrationTest";
	final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
	private SearcherBl searcherBl;
	private Attribute integerAttr;
	private Attribute stringAttr;
	private Attribute listAttr;
	private Attribute mapAttr;

	// setUp methods moved to every test method to save testing time !!

	@Before
	public void setUp() throws Exception {
		searcherBl = perun.getSearcherBl();
		vo = setUpVo();
		candidate1 = setUpCandidate1();
		candidate2 = setUpCandidate2();
		setUpUser1();
		setUpUser2();
		integerAttr = setUpUserAttributeWithIntegerValue();
		stringAttr = setUpUserAttributeWithStringValue();
		listAttr = setUpUserAttributeWithListValue();
		mapAttr = setUpUserLargeAttributeWithMapValue();
		perun.getAttributesManagerBl().setAttribute(sess, user1, integerAttr);
		perun.getAttributesManagerBl().setAttribute(sess, user1, mapAttr);
		perun.getAttributesManagerBl().setAttribute(sess, user1, listAttr);
		perun.getAttributesManagerBl().setAttribute(sess, user2, stringAttr);
		perun.getAttributesManagerBl().setAttribute(sess, user2, listAttr);
	}

	@Test
	public void getUsersForIntegerValue() throws Exception {
		System.out.println(CLASS_NAME + "getUsersForIntegerValue");
		Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
		attributesWithSearchingValues.put(integerAttr.getName(), "100");
		AttributeDefinition attrDef = sess.getPerun().getAttributesManager().getAttributeDefinition(sess, integerAttr.getName());
		Attribute attr = new Attribute(attrDef);
		List<User> users = new ArrayList<User>();
		users = searcherBl.getUsers(sess, attributesWithSearchingValues);
		assertTrue("user1 have to be found", users.contains(user1));
		assertTrue("user2 have not to be found", !users.contains(user2));
	}

	@Test
	public void getUsersForStringValue() throws Exception {
		System.out.println(CLASS_NAME + "getUsersForStringValue");
		Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
		attributesWithSearchingValues.put(stringAttr.getName(), "UserStringAttribute test value");
		List<User> users = new ArrayList<User>();
		users = searcherBl.getUsers(sess, attributesWithSearchingValues);
		assertTrue("user1 have not to be found", !users.contains(user1));
		assertTrue("user2 have to be found", users.contains(user2));
	}

	@Test
	public void getUsersForListValue() throws Exception {
		System.out.println(CLASS_NAME + "getUsersForListValue");
		Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
		attributesWithSearchingValues.put(listAttr.getName(), "ttribute2");
		List<User> users = new ArrayList<User>();
		users = searcherBl.getUsers(sess, attributesWithSearchingValues);
		assertTrue("user2 have to be found", users.contains(user2));
		assertTrue("user1 have to be found", users.contains(user1));
	}

	@Test
	public void getUsersForCoreAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getUsersForCoreAttribute");
		Attribute attr = perun.getAttributesManagerBl().getAttribute(sess, user1, "urn:perun:user:attribute-def:core:id");
		Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
		attributesWithSearchingValues.put(attr.getName(), attr.getValue().toString());
		List<User> users = new ArrayList<User>();
		users = searcherBl.getUsers(sess, attributesWithSearchingValues);
		assertTrue("user2 have not to be found", !users.contains(user2));
		assertTrue("user1 have to be found", users.contains(user1));
	}

	@Test
	public void getUsersForMapValue() throws Exception {
		System.out.println(CLASS_NAME + "getUsersForMapValue");
		Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
		attributesWithSearchingValues.put(mapAttr.getName(), "UserLargeAttribute=test value");
		List<User> users = new ArrayList<User>();
		users = searcherBl.getUsers(sess, attributesWithSearchingValues);
		assertTrue("user2 have not to be found", !users.contains(user2));
		assertTrue("user1 have to be found", users.contains(user1));
	}

	@Test
	public void getMembersForIntegerValue() throws Exception {
		System.out.println(CLASS_NAME + "getUsersForIntegerValue");
		Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
		attributesWithSearchingValues.put(integerAttr.getName(), "100");
		AttributeDefinition attrDef = sess.getPerun().getAttributesManager().getAttributeDefinition(sess, integerAttr.getName());
		Attribute attr = new Attribute(attrDef);
		List<Member> members = new ArrayList<>();
		members = perun.getSearcher().getMembersByUserAttributes(sess, vo, attributesWithSearchingValues);
		assertTrue("member1 have to be found", members.contains(member1));
		assertTrue("member2 have not to be found", !members.contains(member2));
	}

	@Test
	public void getMembersForStringValue() throws Exception {
		System.out.println(CLASS_NAME + "getUsersForStringValue");
		Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
		attributesWithSearchingValues.put(stringAttr.getName(), "UserStringAttribute test value");
		List<Member> members = new ArrayList<>();
		members = perun.getSearcher().getMembersByUserAttributes(sess, vo, attributesWithSearchingValues);
		assertTrue("member1 have not to be found", !members.contains(member1));
		assertTrue("member2 have to be found", members.contains(member2));
	}

	@Test
	public void getMembersForListValue() throws Exception {
		System.out.println(CLASS_NAME + "getUsersForListValue");
		Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
		attributesWithSearchingValues.put(listAttr.getName(), "ttribute2");
		List<Member> members = new ArrayList<>();
		members = perun.getSearcher().getMembersByUserAttributes(sess, vo, attributesWithSearchingValues);
		assertTrue("member2 have to be found", members.contains(member2));
		assertTrue("member1 have to be found", members.contains(member1));
	}

	@Test
	public void getMembersForCoreAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getUsersForCoreAttribute");
		Attribute attr = perun.getAttributesManagerBl().getAttribute(sess, user1, "urn:perun:user:attribute-def:core:id");
		Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
		attributesWithSearchingValues.put(attr.getName(), attr.getValue().toString());
		List<Member> members = new ArrayList<>();
		members = perun.getSearcher().getMembersByUserAttributes(sess, vo, attributesWithSearchingValues);
		assertTrue("member1 have to be found", members.contains(member1));
		assertTrue("member2 have not to be found", !members.contains(member2));
	}

	@Test
	public void getMembersForMapValue() throws Exception {
		System.out.println(CLASS_NAME + "getUsersForMapValue");
		Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
		attributesWithSearchingValues.put(mapAttr.getName(), "UserLargeAttribute=test value");
		List<Member> members = new ArrayList<>();
		members = perun.getSearcher().getMembersByUserAttributes(sess, vo, attributesWithSearchingValues);
		assertTrue("member2 have not to be found", !members.contains(member2));
		assertTrue("member1 have to be found", members.contains(member1));
	}

	@Test
	public void getMembersByExpiration() throws Exception {
		System.out.println(CLASS_NAME + "getMembersByExpiration");

		// setup required attribute if not exists
		try {
			perun.getAttributesManager().getAttributeDefinition(sess, "urn:perun:member:attribute-def:def:membershipExpiration");
		} catch (AttributeNotExistsException ex) {
			setUpMembershipExpirationAttribute();
		}

		// setup expiration dates
		Calendar calendar = Calendar.getInstance();
		String today = BeansUtils.getDateFormatterWithoutTime().format(calendar.getTime());
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		String yesterday = BeansUtils.getDateFormatterWithoutTime().format(calendar.getTime());

		// set attributes
		Attribute attribute = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, "urn:perun:member:attribute-def:def:membershipExpiration"));
		attribute.setValue(today);
		perun.getAttributesManager().setAttribute(sess, member1, attribute);

		Attribute attribute2 = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, "urn:perun:member:attribute-def:def:membershipExpiration"));
		attribute2.setValue(yesterday);
		perun.getAttributesManager().setAttribute(sess, member2, attribute2);

		// check members by expiration
		assertTrue("Member with expiration today was not found for = today.", perun.getSearcher().getMembersByExpiration(sess, "=", 0).contains(member1));
		assertTrue("Member with expiration today was not found for <= today.", perun.getSearcher().getMembersByExpiration(sess, "<=", 0).contains(member1));
		assertTrue("Member with expiration yesterday was not found for <= today.", perun.getSearcher().getMembersByExpiration(sess, "<=", 0).contains(member2));

		assertTrue("Member with expiration yesterday was not found for = yesterday.", perun.getSearcher().getMembersByExpiration(sess, "=", -1).contains(member2));
		assertTrue("Member with expiration today was not found for > yesterday.", perun.getSearcher().getMembersByExpiration(sess, ">", -1).contains(member1));
		assertTrue("Member with expiration today was not found for >= yesterday.", perun.getSearcher().getMembersByExpiration(sess, ">=", -1).contains(member1));
		assertTrue("Member with expiration yesterday was not found for >= yesterday.", perun.getSearcher().getMembersByExpiration(sess, ">=", -1).contains(member2));

		assertTrue("Member with expiration today was found for = tomorrow.", !perun.getSearcher().getMembersByExpiration(sess, "=", 1).contains(member1));
		assertTrue("Member with expiration yesterday was found for = tomorrow.", !perun.getSearcher().getMembersByExpiration(sess, "=", 1).contains(member2));
		assertTrue("Member with expiration today was found for > tomorrow.", !perun.getSearcher().getMembersByExpiration(sess, ">", 1).contains(member1));
		assertTrue("Member with expiration yesterday was not found for < tomorrow.", perun.getSearcher().getMembersByExpiration(sess, "<", 1).contains(member2));

		// check members by expiration date
		assertTrue("Member with expiration yesterday was not found for = yesterday.", perun.getSearcher().getMembersByExpiration(sess, "=", calendar).contains(member2));
		assertTrue("Member with expiration today was not found for > yesterday.", perun.getSearcher().getMembersByExpiration(sess, ">", calendar).contains(member1));
		assertTrue("Member with expiration today was not found for >= yesterday.", perun.getSearcher().getMembersByExpiration(sess, ">=", calendar).contains(member1));
		assertTrue("Member with expiration yesterday was not found for >= yesterday.", perun.getSearcher().getMembersByExpiration(sess, ">=", calendar).contains(member2));

		calendar.add(Calendar.DAY_OF_MONTH, 1);

		assertTrue("Member with expiration today was not found for = today.", perun.getSearcher().getMembersByExpiration(sess, "=", calendar).contains(member1));
		assertTrue("Member with expiration today was not found for <= today.", perun.getSearcher().getMembersByExpiration(sess, "<=", calendar).contains(member1));
		assertTrue("Member with expiration yesterday was not found for <= today.", perun.getSearcher().getMembersByExpiration(sess, "<=", calendar).contains(member2));

		calendar.add(Calendar.DAY_OF_MONTH, 1);

		assertTrue("Member with expiration today was found for = tomorrow.", !perun.getSearcher().getMembersByExpiration(sess, "=", calendar).contains(member1));
		assertTrue("Member with expiration yesterday was found for = tomorrow.", !perun.getSearcher().getMembersByExpiration(sess, "=", calendar).contains(member2));
		assertTrue("Member with expiration today was found for > tomorrow.", !perun.getSearcher().getMembersByExpiration(sess, ">", calendar).contains(member1));
		assertTrue("Member with expiration yesterday was not found for < tomorrow.", perun.getSearcher().getMembersByExpiration(sess, "<", calendar).contains(member2));

	}

	@Test
	public void getGroupsByGroupResourceSetting() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsByGroupResourceSetting");

		Facility facility = new Facility(0, "testName01", "testName01");
		facility = perun.getFacilitiesManagerBl().createFacility(sess, facility);

		Group group1 = new Group("testName01", "testName01");
		group1 = perun.getGroupsManagerBl().createGroup(sess, vo, group1);
		Group group2 = new Group("testName02", "testName02");
		group2 = perun.getGroupsManagerBl().createGroup(sess, vo, group2);
		Group group3 = new Group("testName03", "testName03");
		group3 = perun.getGroupsManagerBl().createGroup(sess, vo, group3);

		Resource resource1 = new Resource(0, "testName01", "testName01", facility.getId(), vo.getId());
		resource1 = perun.getResourcesManagerBl().createResource(sess,resource1, vo, facility);
		Resource resource2 = new Resource(0, "testName02", "testName02", facility.getId(), vo.getId());
		resource2 = perun.getResourcesManagerBl().createResource(sess,resource2, vo, facility);
		Resource resource3 = new Resource(0, "testName03", "testName03", facility.getId(), vo.getId());
		resource3 = perun.getResourcesManagerBl().createResource(sess,resource3, vo, facility);

		perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource1);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource2);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource1);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource2);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource3);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group3, resource2);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group3, resource3);

		Attribute groupResourceAttr01 = new Attribute(setUpGroupResourceAttribute());
		groupResourceAttr01.setValue("VALUE01");
		Attribute groupResourceAttr02 = new Attribute(groupResourceAttr01);
		groupResourceAttr02.setValue("VALUE02");
		Attribute resourceAttr01 = new Attribute(setUpResourceAttribute());
		resourceAttr01.setValue("VALUE01");
		Attribute resourceAttr02 = new Attribute(resourceAttr01);
		resourceAttr02.setValue("VALUE02");

		perun.getAttributesManagerBl().setAttribute(sess, resource1, resourceAttr01);
		perun.getAttributesManagerBl().setAttribute(sess, resource2, resourceAttr02);
		perun.getAttributesManagerBl().setAttribute(sess, resource3, resourceAttr01);

		perun.getAttributesManagerBl().setAttribute(sess, resource1, group1, groupResourceAttr01);
		perun.getAttributesManagerBl().setAttribute(sess, resource1, group2, groupResourceAttr02);
		perun.getAttributesManagerBl().setAttribute(sess, resource2, group1, groupResourceAttr02);
		perun.getAttributesManagerBl().setAttribute(sess, resource2, group2, groupResourceAttr01);
		perun.getAttributesManagerBl().setAttribute(sess, resource2, group3, groupResourceAttr01);
		perun.getAttributesManagerBl().setAttribute(sess, resource3, group2, groupResourceAttr02);
		perun.getAttributesManagerBl().setAttribute(sess, resource3, group3, groupResourceAttr02);

		List<Group> returnedGroups = perun.getSearcherBl().getGroupsByGroupResourceSetting(sess, groupResourceAttr01, resourceAttr01);
		assertEquals(1, returnedGroups.size());
		assertTrue(returnedGroups.contains(group1));
		returnedGroups = perun.getSearcherBl().getGroupsByGroupResourceSetting(sess, groupResourceAttr01, resourceAttr02);
		assertEquals(2, returnedGroups.size());
		assertTrue(returnedGroups.contains(group2));
		assertTrue(returnedGroups.contains(group3));
		returnedGroups = perun.getSearcherBl().getGroupsByGroupResourceSetting(sess, groupResourceAttr02, resourceAttr01);
		assertEquals(2, returnedGroups.size());
		assertTrue(returnedGroups.contains(group2));
		assertTrue(returnedGroups.contains(group3));
		returnedGroups = perun.getSearcherBl().getGroupsByGroupResourceSetting(sess, groupResourceAttr02, resourceAttr02);
		assertEquals(1, returnedGroups.size());
		assertTrue(returnedGroups.contains(group1));
	}

	// PRIVATE METHODS -----------------------------------------------------------

	private void setUpUser1() throws Exception {
		member1 = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate1);
		user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);
	}

	private void setUpUser2() throws Exception {
		member2 = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate2);
		user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
	}

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "UserManagerTestVo", "UMTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		// create test VO in database
		assertNotNull("unable to create testing Vo",returnedVo);
		assertEquals("both VOs should be the same",newVo,returnedVo);
		ExtSource newExtSource = new ExtSource(extSourceName, ExtSourcesManager.EXTSOURCE_INTERNAL);
		ExtSource es = perun.getExtSourcesManager().createExtSource(sess, newExtSource, null);
		// get real external source from DB
		perun.getExtSourcesManager().addExtSource(sess, returnedVo, es);
		// add real ext source to our VO
		return returnedVo;
	}

	private Candidate setUpCandidate1(){

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("aaa1");
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName("bbb1");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}

	private Candidate setUpCandidate2(){

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("aaa2");
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName("bbb2");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin2);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}

	private Attribute setUpUserAttributeWithIntegerValue() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user-integer-test-attribute");
		attr.setType(Integer.class.getName());
		attr.setValue(100);
		assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource member attribute
		return attr;

	}

	private Attribute setUpUserAttributeWithStringValue() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user-string-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("UserStringAttribute test value");
		assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource member attribute
		return attr;
	}

	private Attribute setUpUserAttributeWithListValue() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user-list-test-attribute");
		attr.setType(ArrayList.class.getName());
		List<String> value = new ArrayList<String>();
		value.add("UserStringAttribute test value");
		value.add("UserStringAttribute2 test2 value2");
		attr.setValue(value);
		assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource member attribute
		return attr;

	}

	private Attribute setUpUserLargeAttributeWithMapValue() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user-map-test-large-attribute");
		attr.setType(LinkedHashMap.class.getName());
		Map<String, String> value = new LinkedHashMap<String, String>();
		value.put("UserLargeAttribute", "test value");
		attr.setValue(value);
		assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		return attr;

	}

	private AttributeDefinition setUpMembershipExpirationAttribute() throws Exception {

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace("urn:perun:member:attribute-def:def");
		attr.setFriendlyName("membershipExpiration");
		attr.setType(String.class.getName());
		attr.setDisplayName("Membership expiration");
		attr.setDescription("Membership expiration date.");

		return perun.getAttributesManager().createAttribute(sess, attr);

	}

	private AttributeDefinition setUpResourceAttribute() throws Exception {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace("urn:perun:resource:attribute-def:def");
		attr.setFriendlyName("testingAttribute01");
		attr.setType(String.class.getName());
		attr.setDisplayName("Testing attribute1");
		attr.setDescription("Testing attribute1");

		return perun.getAttributesManager().createAttribute(sess, attr);
	}

	private AttributeDefinition setUpGroupResourceAttribute() throws Exception {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace("urn:perun:group_resource:attribute-def:def");
		attr.setFriendlyName("testingAttribute02");
		attr.setType(String.class.getName());
		attr.setDisplayName("Testing attribute2");
		attr.setDescription("Testing attribute2");

		return perun.getAttributesManager().createAttribute(sess, attr);
	}
}
