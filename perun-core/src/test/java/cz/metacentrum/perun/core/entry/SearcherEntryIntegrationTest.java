package cz.metacentrum.perun.core.entry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.bl.SearcherBl;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests of Searcher
 *
 * @author Michal Šťava <stavamichal@gmail.com>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class SearcherEntryIntegrationTest extends AbstractPerunIntegrationTest {

  private static final String CLASS_NAME = "Searcher.";

  final String extSourceName = "SearcherEntryIntegrationTest";
  final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
  // these are in DB only when needed and must be setUp"type"() in right order before use !!
  private User user1;                             // our User
  private User user2;
  private Member member1;
  private Member member2;
  private Group group;
  private Candidate candidate1;
  private Candidate candidate2;
  private Vo vo;
  private SearcherBl searcherBl;
  private Attribute memberStringAttribute;
  private Attribute integerAttr;
  private Attribute stringAttr;
  private Attribute listAttr;
  private Attribute mapAttr;

  // setUp methods moved to every test method to save testing time !!

  @Test
  public void getFacilitiesByCoreAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByCoreAttributeValue");

    Facility facility1 = setUpFacility("testFacility01");
    setUpFacility("testFacility02");
    setUpFacility("testFacility03");

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_FACILITY_ATTR_CORE + ":name", facility1.getName());

    List<Facility> foundFacilities = searcherBl.getFacilities(sess, searchParams);

    assertEquals("Found invalid number of facilities", 1, foundFacilities.size());
    assertTrue("Found facilities did not contain facility it should.", foundFacilities.contains(facility1));
  }

  @Test
  public void getFacilitiesByIntAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByIntAttributeValue");

    Facility facility1 = setUpFacility("testFacility01");
    Facility facility2 = setUpFacility("testFacility02");
    Facility facility3 = setUpFacility("testFacility03");
    Facility facility4 = setUpFacility("testFacility04");

    int searchedValue = 14;
    int otherValue = 4;
    String attributeName = "testAttribute";

    AttributeDefinition ad = setUpFacilityAttribute(attributeName, Integer.class.getName());
    Attribute searchedAttribute = new Attribute(ad, searchedValue);
    Attribute otherAttribute = new Attribute(ad, otherValue);

    perun.getAttributesManagerBl().setAttribute(sess, facility1, searchedAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, facility2, otherAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, facility3, otherAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, facility4, searchedAttribute);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_FACILITY_ATTR_DEF + ":" + attributeName, Integer.toString(searchedValue));

    List<Facility> foundFacilities = searcherBl.getFacilities(sess, searchParams);

    assertEquals("Found invalid number of facilities", 2, foundFacilities.size());
    assertTrue("Found facilities did not contain facility it should.", foundFacilities.contains(facility1));
    assertTrue("Found facilities did not contain facility it should.", foundFacilities.contains(facility4));
  }

  @Test
  public void getFacilitiesByListAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByListAttributeValue");

    Facility facility1 = setUpFacility("testFacility01");
    Facility facility2 = setUpFacility("testFacility02");
    Facility facility3 = setUpFacility("testFacility03");
    Facility facility4 = setUpFacility("testFacility04");

    String searchedString = "searchedValue";
    String otherString = "otherValue";
    String attributeName = "testAttribute";

    List<String> matchingList1 = new ArrayList<>();
    List<String> matchingList2 = new ArrayList<>();
    List<String> notMatchingList1 = new ArrayList<>();
    List<String> notMatchingList2 = new ArrayList<>();

    matchingList1.add(searchedString);
    matchingList2.add(searchedString);
    matchingList2.add(otherString);

    notMatchingList1.add(otherString);

    AttributeDefinition ad = setUpFacilityAttribute(attributeName, ArrayList.class.getName());

    Attribute searchedAttribute1 = new Attribute(ad, matchingList1);
    Attribute searchedAttribute2 = new Attribute(ad, matchingList2);
    Attribute otherAttribute1 = new Attribute(ad, notMatchingList1);
    Attribute otherAttribute2 = new Attribute(ad, notMatchingList2);

    perun.getAttributesManagerBl().setAttribute(sess, facility1, searchedAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, facility2, otherAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, facility3, otherAttribute2);
    perun.getAttributesManagerBl().setAttribute(sess, facility4, searchedAttribute2);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_FACILITY_ATTR_DEF + ":" + attributeName, searchedString);

    List<Facility> foundFacilities = searcherBl.getFacilities(sess, searchParams);

    assertEquals("Found invalid number of facilities", 2, foundFacilities.size());
    assertTrue("Found facilities did not contain facility it should.", foundFacilities.contains(facility1));
    assertTrue("Found facilities did not contain facility it should.", foundFacilities.contains(facility4));
  }

  @Test
  public void getFacilitiesByMapAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByMapAttributeValue");

    Facility facility1 = setUpFacility("testFacility01");
    Facility facility2 = setUpFacility("testFacility02");
    Facility facility3 = setUpFacility("testFacility03");
    Facility facility4 = setUpFacility("testFacility04");

    String searchedString = "searchedValue";
    String searchedKeyString = "searchedKey";
    String otherString = "otherValue";
    String otherKeyString = "otherKey";
    String attributeName = "testAttribute";

    Map<String, String> matchingMap1 = new LinkedHashMap<>();
    Map<String, String> matchingMap2 = new LinkedHashMap<>();
    Map<String, String> notMatchingMap1 = new LinkedHashMap<>();
    Map<String, String> notMatchingMap2 = new LinkedHashMap<>();

    matchingMap1.put(searchedKeyString, searchedString);
    matchingMap2.put(searchedKeyString, searchedString);
    matchingMap2.put(otherKeyString, otherString);
    notMatchingMap1.put(otherKeyString, otherString);
    notMatchingMap1.put(otherKeyString, searchedString);
    notMatchingMap2.put(searchedKeyString, otherString);

    AttributeDefinition ad = setUpFacilityAttribute(attributeName, LinkedHashMap.class.getName());

    Attribute searchedAttribute1 = new Attribute(ad, matchingMap1);
    Attribute searchedAttribute2 = new Attribute(ad, matchingMap2);
    Attribute otherAttribute1 = new Attribute(ad, notMatchingMap1);
    Attribute otherAttribute2 = new Attribute(ad, notMatchingMap2);

    perun.getAttributesManagerBl().setAttribute(sess, facility1, searchedAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, facility2, otherAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, facility3, otherAttribute2);
    perun.getAttributesManagerBl().setAttribute(sess, facility4, searchedAttribute2);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_FACILITY_ATTR_DEF + ":" + attributeName,
        searchedKeyString + "=" + searchedString);

    List<Facility> foundFacilities = searcherBl.getFacilities(sess, searchParams);

    assertEquals("Found invalid number of facilities", 2, foundFacilities.size());
    assertTrue("Found facilities did not contain facility it should.", foundFacilities.contains(facility1));
    assertTrue("Found facilities did not contain facility it should.", foundFacilities.contains(facility4));
  }

  @Test
  public void getFacilitiesByStringAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByStringAttributeValue");

    Facility facility1 = setUpFacility("testFacility01");
    Facility facility2 = setUpFacility("testFacility02");
    Facility facility3 = setUpFacility("testFacility03");
    Facility facility4 = setUpFacility("testFacility04");

    String searchedValue = "searchedValue";
    String otherValue = "otherValue";
    String attributeName = "testAttribute";

    AttributeDefinition ad = setUpFacilityAttribute(attributeName, String.class.getName());
    Attribute searchedAttribute = new Attribute(ad, searchedValue);
    Attribute otherAttribute = new Attribute(ad, otherValue);

    perun.getAttributesManagerBl().setAttribute(sess, facility1, searchedAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, facility2, otherAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, facility3, otherAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, facility4, searchedAttribute);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_FACILITY_ATTR_DEF + ":" + attributeName, searchedValue);

    List<Facility> foundFacilities = searcherBl.getFacilities(sess, searchParams);

    assertEquals("Found invalid number of facilities", 2, foundFacilities.size());
    assertTrue("Found facilities did not contain facility it should.", foundFacilities.contains(facility1));
    assertTrue("Found facilities did not contain facility it should.", foundFacilities.contains(facility4));
  }

  @Test
  public void getFacilitiesByTwoAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getFacilitiesByTwoAttributeValue");

    Facility facility1 = setUpFacility("testFacility01");
    Facility facility2 = setUpFacility("testFacility02");
    Facility facility3 = setUpFacility("testFacility03");
    Facility facility4 = setUpFacility("testFacility04");

    String searchedValue1 = "searchedValue1";
    String searchedValue2 = "searchedValue2";
    String attributeName1 = "testAttribute1";
    String attributeName2 = "testAttribute2";

    AttributeDefinition ad1 = setUpFacilityAttribute(attributeName1, String.class.getName());
    AttributeDefinition ad2 = setUpFacilityAttribute(attributeName2, String.class.getName());
    Attribute searchedAttribute1 = new Attribute(ad1, searchedValue1);
    Attribute searchedAttribute2 = new Attribute(ad2, searchedValue2);

    perun.getAttributesManagerBl().setAttribute(sess, facility1, searchedAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, facility2, searchedAttribute2);
    perun.getAttributesManagerBl().setAttribute(sess, facility3, searchedAttribute2);
    perun.getAttributesManagerBl().setAttribute(sess, facility4, searchedAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, facility4, searchedAttribute2);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_FACILITY_ATTR_DEF + ":" + attributeName1, searchedValue1);
    searchParams.put(AttributesManager.NS_FACILITY_ATTR_DEF + ":" + attributeName2, searchedValue2);

    List<Facility> foundFacilities = searcherBl.getFacilities(sess, searchParams);

    assertEquals("Found invalid number of facilities", 1, foundFacilities.size());
    assertTrue("Found facilities did not contain facility it should.", foundFacilities.contains(facility4));
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
    resource1 = perun.getResourcesManagerBl().createResource(sess, resource1, vo, facility);
    Resource resource2 = new Resource(0, "testName02", "testName02", facility.getId(), vo.getId());
    resource2 = perun.getResourcesManagerBl().createResource(sess, resource2, vo, facility);
    Resource resource3 = new Resource(0, "testName03", "testName03", facility.getId(), vo.getId());
    resource3 = perun.getResourcesManagerBl().createResource(sess, resource3, vo, facility);

    perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource1, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource2, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource1, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource2, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group2, resource3, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group3, resource2, false, false, false);
    perun.getResourcesManagerBl().assignGroupToResource(sess, group3, resource3, false, false, false);

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

    List<Group> returnedGroups =
        perun.getSearcherBl().getGroupsByGroupResourceSetting(sess, groupResourceAttr01, resourceAttr01);
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

  @Test
  public void getMembersByExpiration() throws Exception {
    System.out.println(CLASS_NAME + "getMembersByExpiration");

    // setup required attribute if not exists
    try {
      perun.getAttributesManager()
          .getAttributeDefinition(sess, "urn:perun:member:attribute-def:def:membershipExpiration");
    } catch (AttributeNotExistsException ex) {
      setUpMembershipExpirationAttribute();
    }

    // setup expiration dates
    LocalDate date = LocalDate.now();
    String today = date.toString();
    date = date.minusDays(1);
    String yesterday = date.toString();

    // set attributes
    Attribute attribute = new Attribute(perun.getAttributesManager()
                                            .getAttributeDefinition(sess,
                                                "urn:perun:member:attribute-def:def:membershipExpiration"));
    attribute.setValue(today);
    perun.getAttributesManager().setAttribute(sess, member1, attribute);

    Attribute attribute2 = new Attribute(perun.getAttributesManager()
                                             .getAttributeDefinition(sess,
                                                 "urn:perun:member:attribute-def:def:membershipExpiration"));
    attribute2.setValue(yesterday);
    perun.getAttributesManager().setAttribute(sess, member2, attribute2);

    // check members by expiration
    assertTrue("Member with expiration today was not found for = today.",
        perun.getSearcher().getMembersByExpiration(sess, "=", 0).contains(member1));
    assertTrue("Member with expiration today was not found for <= today.",
        perun.getSearcher().getMembersByExpiration(sess, "<=", 0).contains(member1));
    assertTrue("Member with expiration yesterday was not found for <= today.",
        perun.getSearcher().getMembersByExpiration(sess, "<=", 0).contains(member2));

    assertTrue("Member with expiration yesterday was not found for = yesterday.",
        perun.getSearcher().getMembersByExpiration(sess, "=", -1).contains(member2));
    assertTrue("Member with expiration today was not found for > yesterday.",
        perun.getSearcher().getMembersByExpiration(sess, ">", -1).contains(member1));
    assertTrue("Member with expiration today was not found for >= yesterday.",
        perun.getSearcher().getMembersByExpiration(sess, ">=", -1).contains(member1));
    assertTrue("Member with expiration yesterday was not found for >= yesterday.",
        perun.getSearcher().getMembersByExpiration(sess, ">=", -1).contains(member2));

    assertTrue("Member with expiration today was found for = tomorrow.",
        !perun.getSearcher().getMembersByExpiration(sess, "=", 1).contains(member1));
    assertTrue("Member with expiration yesterday was found for = tomorrow.",
        !perun.getSearcher().getMembersByExpiration(sess, "=", 1).contains(member2));
    assertTrue("Member with expiration today was found for > tomorrow.",
        !perun.getSearcher().getMembersByExpiration(sess, ">", 1).contains(member1));
    assertTrue("Member with expiration yesterday was not found for < tomorrow.",
        perun.getSearcher().getMembersByExpiration(sess, "<", 1).contains(member2));

    // check members by expiration date
    assertTrue("Member with expiration yesterday was not found for = yesterday.",
        perun.getSearcher().getMembersByExpiration(sess, "=", date).contains(member2));
    assertTrue("Member with expiration today was not found for > yesterday.",
        perun.getSearcher().getMembersByExpiration(sess, ">", date).contains(member1));
    assertTrue("Member with expiration today was not found for >= yesterday.",
        perun.getSearcher().getMembersByExpiration(sess, ">=", date).contains(member1));
    assertTrue("Member with expiration yesterday was not found for >= yesterday.",
        perun.getSearcher().getMembersByExpiration(sess, ">=", date).contains(member2));

    date = date.plusDays(1);

    assertTrue("Member with expiration today was not found for = today.",
        perun.getSearcher().getMembersByExpiration(sess, "=", date).contains(member1));
    assertTrue("Member with expiration today was not found for <= today.",
        perun.getSearcher().getMembersByExpiration(sess, "<=", date).contains(member1));
    assertTrue("Member with expiration yesterday was not found for <= today.",
        perun.getSearcher().getMembersByExpiration(sess, "<=", date).contains(member2));

    date = date.plusDays(1);

    assertTrue("Member with expiration today was found for = tomorrow.",
        !perun.getSearcher().getMembersByExpiration(sess, "=", date).contains(member1));
    assertTrue("Member with expiration yesterday was found for = tomorrow.",
        !perun.getSearcher().getMembersByExpiration(sess, "=", date).contains(member2));
    assertTrue("Member with expiration today was found for > tomorrow.",
        !perun.getSearcher().getMembersByExpiration(sess, ">", date).contains(member1));
    assertTrue("Member with expiration yesterday was not found for < tomorrow.",
        perun.getSearcher().getMembersByExpiration(sess, "<", date).contains(member2));

  }

  @Test
  public void getMembersByGroupExpiration() throws Exception {
    System.out.println(CLASS_NAME + "getMembersByGroupExpiration");

    // setup required attribute if not exists
    try {
      perun.getAttributesManager()
          .getAttributeDefinition(sess, "urn:perun:member_group:attribute-def:def:groupMembershipExpiration");
    } catch (AttributeNotExistsException ex) {
      setUpGroupMembershipExpirationAttribute();
    }

    // setup expiration dates
    LocalDate date = LocalDate.now();
    String today = date.toString();
    date = date.minusDays(1);
    String yesterday = date.toString();

    // set attributes
    Attribute attribute = new Attribute(perun.getAttributesManager()
                                            .getAttributeDefinition(sess,
                                                "urn:perun:member_group:attribute-def:def:groupMembershipExpiration"));
    attribute.setValue(today);
    perun.getAttributesManager().setAttribute(sess, member1, group, attribute);

    Attribute attribute2 = new Attribute(perun.getAttributesManager()
                                             .getAttributeDefinition(sess,
                                                 "urn:perun:member_group:attribute-def:def:groupMembershipExpiration"));
    attribute2.setValue(yesterday);
    perun.getAttributesManager().setAttribute(sess, member2, group, attribute2);

    assertTrue("Member with expiration yesterday was not found for = yesterday.",
        perun.getSearcher().getMembersByGroupExpiration(sess, group, "=", date).contains(member2));
    assertTrue("Member with expiration today was not found for > yesterday.",
        perun.getSearcher().getMembersByGroupExpiration(sess, group, ">", date).contains(member1));
    assertTrue("Member with expiration today was not found for >= yesterday.",
        perun.getSearcher().getMembersByGroupExpiration(sess, group, ">=", date).contains(member1));
    assertTrue("Member with expiration yesterday was not found for >= yesterday.",
        perun.getSearcher().getMembersByGroupExpiration(sess, group, ">=", date).contains(member2));
    assertTrue("Member with expiration today was found for = yesterday.",
        !perun.getSearcher().getMembersByGroupExpiration(sess, group, "=", date).contains(member1));
    assertTrue("Member with expiration yesterday was found for > yesterday.",
        !perun.getSearcher().getMembersByGroupExpiration(sess, group, ">", date).contains(member2));

    // check sub-group logic if it resolve correct status for members

    Group subGroup = new Group("subgroup", "subgroup of test group");
    perun.getGroupsManager().createGroup(sess, group, subGroup);
    perun.getGroupsManager().addMember(sess, subGroup, member1);
    perun.getGroupsManager().setMemberGroupStatus(sess, member1, group, MemberGroupStatus.EXPIRED);
    perun.getGroupsManager().setMemberGroupStatus(sess, member1, subGroup, MemberGroupStatus.EXPIRED);
    List<Member> mems = perun.getSearcher().getMembersByGroupExpiration(sess, group, ">", date);
    assertEquals("Should have found single member", mems.size(), 1);
    assertTrue("Member1 not found between soon to be expired in a group", mems.contains(member1));
    assertEquals("Member soon to be expired in a group hadn't have a correct status", MemberGroupStatus.EXPIRED,
        mems.get(0).getGroupStatus());

    perun.getGroupsManager().setMemberGroupStatus(sess, member1, group, MemberGroupStatus.VALID);
    mems = perun.getSearcher().getMembersByGroupExpiration(sess, group, ">", date);
    assertEquals("Should have found single member", mems.size(), 1);
    assertTrue("Member1 not found between soon to be expired in a group", mems.contains(member1));
    assertEquals("Member soon to be expired in a group hadn't have a correct status", MemberGroupStatus.VALID,
        mems.get(0).getGroupStatus());

  }

  @Test
  public void getMembersByMemberAttribute() throws Exception {
    System.out.println(CLASS_NAME + "getMembersByMemberAttribute");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(memberStringAttribute.getName(), "MemberStringAttribute test value");
    List<Member> members = perun.getSearcher().getMembers(sess, vo, attributesWithSearchingValues);
    assertTrue("member1 should have been returned", members.contains(member1));
    assertFalse("member2 should not have been returned", members.contains(member2));
  }

  @Test
  public void getMembersByCoreMemberAttribute() throws Exception {
    System.out.println(CLASS_NAME + "getMembersByCoreMemberAttribute");
    Attribute attr =
        perun.getAttributesManagerBl().getAttribute(sess, member1, "urn:perun:member:attribute-def:core:id");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(attr.getName(), attr.getValue().toString());
    List<Member> members = perun.getSearcher().getMembers(sess, vo, attributesWithSearchingValues);
    assertTrue("member1 should have been returned", members.contains(member1));
    assertTrue("member2 should not have been returned", !members.contains(member2));
  }

  @Test
  public void getMembersByMemberAndUserAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getMembersByMemberAndUserAttributes");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(memberStringAttribute.getName(), "MemberStringAttribute test value");
    attributesWithSearchingValues.put(mapAttr.getName(), "UserLargeAttribute=test value");

    List<Member> members = perun.getSearcher().getMembers(sess, vo, attributesWithSearchingValues);
    assertTrue("member1 should have been returned", members.contains(member1));
    assertFalse("member2 should not have been returned", members.contains(member2));
  }

  @Test
  public void getMembersByMemberAndUserAndCoreAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getMembersByMemberAndUserAndCoreAttributes");
    Attribute memberCoreAttr = perun.getAttributesManagerBl().getAttribute(
        sess, member1, "urn:perun:member:attribute-def:core:id");
    Attribute userCoreAttr = perun.getAttributesManagerBl().getAttribute(
        sess, user1, "urn:perun:user:attribute-def:core:id");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(memberStringAttribute.getName(), "MemberStringAttribute test value");
    attributesWithSearchingValues.put(mapAttr.getName(), "UserLargeAttribute=test value");
    attributesWithSearchingValues.put(memberCoreAttr.getName(), memberCoreAttr.getValue().toString());
    attributesWithSearchingValues.put(userCoreAttr.getName(), userCoreAttr.getValue().toString());

    List<Member> members = perun.getSearcher().getMembers(sess, vo, attributesWithSearchingValues);
    assertTrue("member1 should have been returned", members.contains(member1));
    assertFalse("member2 should not have been returned", members.contains(member2));
  }

  @Test
  public void getMembersForCoreAttribute() throws Exception {
    System.out.println(CLASS_NAME + "getMembersForCoreAttribute");
    Attribute attr = perun.getAttributesManagerBl().getAttribute(sess, user1, "urn:perun:user:attribute-def:core:id");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(attr.getName(), attr.getValue().toString());
    List<Member> members = perun.getSearcher().getMembersByUserAttributes(sess, vo, attributesWithSearchingValues);
    assertTrue("member1 have to be found", members.contains(member1));
    assertTrue("member2 have not to be found", !members.contains(member2));
  }

  @Test
  public void getMembersForIntegerValue() throws Exception {
    System.out.println(CLASS_NAME + "getMembersForIntegerValue");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(integerAttr.getName(), "100");
    AttributeDefinition attrDef =
        sess.getPerun().getAttributesManager().getAttributeDefinition(sess, integerAttr.getName());
    Attribute attr = new Attribute(attrDef);
    List<Member> members = perun.getSearcher().getMembersByUserAttributes(sess, vo, attributesWithSearchingValues);
    assertTrue("member1 have to be found", members.contains(member1));
    assertTrue("member2 have not to be found", !members.contains(member2));
  }

  @Test
  public void getMembersForListValue() throws Exception {
    System.out.println(CLASS_NAME + "getMembersForListValue");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(listAttr.getName(), "ttribute2");
    List<Member> members = perun.getSearcher().getMembersByUserAttributes(sess, vo, attributesWithSearchingValues);
    assertTrue("member2 have to be found", members.contains(member2));
    assertTrue("member1 have to be found", members.contains(member1));
  }

  @Test
  public void getMembersForMapValue() throws Exception {
    System.out.println(CLASS_NAME + "getMembersForMapValue");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(mapAttr.getName(), "UserLargeAttribute=test value");
    List<Member> members = perun.getSearcher().getMembersByUserAttributes(sess, vo, attributesWithSearchingValues);
    assertTrue("member2 have not to be found", !members.contains(member2));
    assertTrue("member1 have to be found", members.contains(member1));
  }

  @Test
  public void getMembersForStringValue() throws Exception {
    System.out.println(CLASS_NAME + "getMembersForStringValue");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(stringAttr.getName(), "UserStringAttribute test value");
    List<Member> members = perun.getSearcher().getMembersByUserAttributes(sess, vo, attributesWithSearchingValues);
    assertTrue("member1 have not to be found", !members.contains(member1));
    assertTrue("member2 have to be found", members.contains(member2));
  }

  @Test
  public void getResourcesByCoreAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesByCoreAttributeValue");

    Facility facility = setUpFacility("testFacility");

    Resource resource1 = setUpResource("testResource01", vo, facility);
    Resource resource2 = setUpResource("testResource02", vo, facility);
    Resource resource3 = setUpResource("testResource03", vo, facility);

    Map<String, String> searchParams = new HashMap<>();
    //exact string match
    searchParams.put(AttributesManager.NS_RESOURCE_ATTR_CORE + ":name", resource1.getName());
    List<Resource> foundResources = searcherBl.getResources(sess, searchParams, false);

    assertThat(foundResources).containsOnlyOnce(resource1);

    //partial string match
    searchParams.put(AttributesManager.NS_RESOURCE_ATTR_CORE + ":name", "testResource");
    foundResources = searcherBl.getResources(sess, searchParams, true);
    assertThat(foundResources).containsOnlyOnce(resource1, resource2, resource3);
  }

  @Test
  public void getResourcesByIntegerAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesByIntegerAttributeValue");

    Facility facility = setUpFacility("testFacility");

    Resource resource1 = setUpResource("testResource01", vo, facility);
    Resource resource2 = setUpResource("testResource02", vo, facility);
    Resource resource3 = setUpResource("testResource03", vo, facility);
    Resource resource4 = setUpResource("testResource04", vo, facility);

    int searchedValue = 14;
    int otherValue = 4;
    String attributeName = "testAttribute";

    AttributeDefinition ad = setUpResourceAttribute(attributeName, Integer.class.getName());
    Attribute searchedAttribute = new Attribute(ad, searchedValue);
    Attribute otherAttribute = new Attribute(ad, otherValue);

    perun.getAttributesManagerBl().setAttribute(sess, resource1, searchedAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, resource2, otherAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, resource3, otherAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, resource4, searchedAttribute);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_RESOURCE_ATTR_DEF + ":" + attributeName, String.valueOf(searchedValue));

    List<Resource> foundResources = searcherBl.getResources(sess, searchParams, false);

    assertEquals("Found invalid number of resources", 2, foundResources.size());
    assertTrue("Found resources did not contain resource it should.", foundResources.contains(resource1));
    assertTrue("Found resources did not contain resource it should.", foundResources.contains(resource4));
  }

  @Test
  public void getResourcesByListAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesByListAttributeValue");

    Facility facility = setUpFacility("testFacility");

    Resource resource1 = setUpResource("testResource01", vo, facility);
    Resource resource2 = setUpResource("testResource02", vo, facility);
    Resource resource3 = setUpResource("testResource03", vo, facility);
    Resource resource4 = setUpResource("testResource04", vo, facility);

    String searchedString = "searchedValue";
    String otherString = "otherValue";
    String attributeName = "testAttribute";

    List<String> matchingList1 = new ArrayList<>();
    List<String> matchingList2 = new ArrayList<>();
    List<String> notMatchingList1 = new ArrayList<>();
    List<String> notMatchingList2 = new ArrayList<>();

    matchingList1.add(searchedString);
    matchingList2.add(searchedString);
    matchingList2.add(otherString);

    notMatchingList1.add(otherString);

    AttributeDefinition ad = setUpResourceAttribute(attributeName, ArrayList.class.getName());

    Attribute searchedAttribute1 = new Attribute(ad, matchingList1);
    Attribute searchedAttribute2 = new Attribute(ad, matchingList2);
    Attribute otherAttribute1 = new Attribute(ad, notMatchingList1);
    Attribute otherAttribute2 = new Attribute(ad, notMatchingList2);

    perun.getAttributesManagerBl().setAttribute(sess, resource1, searchedAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, resource2, otherAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, resource3, otherAttribute2);
    perun.getAttributesManagerBl().setAttribute(sess, resource4, searchedAttribute2);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_RESOURCE_ATTR_DEF + ":" + attributeName, searchedString);

    List<Resource> foundResources = searcherBl.getResources(sess, searchParams, false);

    assertEquals("Found invalid number of resources", 2, foundResources.size());
    assertTrue("Found resources did not contain resource it should.", foundResources.contains(resource1));
    assertTrue("Found resources did not contain resource it should.", foundResources.contains(resource4));
  }

  @Test
  public void getResourcesByMapAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesByMapAttributeValue");

    Facility facility = setUpFacility("testFacility");

    Resource resource1 = setUpResource("testResource01", vo, facility);
    Resource resource2 = setUpResource("testResource02", vo, facility);
    Resource resource3 = setUpResource("testResource03", vo, facility);
    Resource resource4 = setUpResource("testResource04", vo, facility);

    String searchedString = "searchedValue";
    String searchedKeyString = "searchedKey";
    String otherString = "otherValue";
    String otherKeyString = "otherKey";
    String attributeName = "testAttribute";

    Map<String, String> matchingMap1 = new LinkedHashMap<>();
    Map<String, String> matchingMap2 = new LinkedHashMap<>();
    Map<String, String> notMatchingMap1 = new LinkedHashMap<>();
    Map<String, String> notMatchingMap2 = new LinkedHashMap<>();

    matchingMap1.put(searchedKeyString, searchedString);
    matchingMap2.put(searchedKeyString, searchedString);
    matchingMap2.put(otherKeyString, otherString);
    notMatchingMap1.put(otherKeyString, otherString);
    notMatchingMap1.put(otherKeyString, searchedString);
    notMatchingMap2.put(searchedKeyString, otherString);

    AttributeDefinition ad = setUpResourceAttribute(attributeName, LinkedHashMap.class.getName());

    Attribute searchedAttribute1 = new Attribute(ad, matchingMap1);
    Attribute searchedAttribute2 = new Attribute(ad, matchingMap2);
    Attribute otherAttribute1 = new Attribute(ad, notMatchingMap1);
    Attribute otherAttribute2 = new Attribute(ad, notMatchingMap2);

    perun.getAttributesManagerBl().setAttribute(sess, resource1, searchedAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, resource2, otherAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, resource3, otherAttribute2);
    perun.getAttributesManagerBl().setAttribute(sess, resource4, searchedAttribute2);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_RESOURCE_ATTR_DEF + ":" + attributeName,
        searchedKeyString + "=" + searchedString);

    List<Resource> foundResources = searcherBl.getResources(sess, searchParams, false);

    assertEquals("Found invalid number of resources", 2, foundResources.size());
    assertTrue("Found resources did not contain resources it should.", foundResources.contains(resource1));
    assertTrue("Found resources did not contain resources it should.", foundResources.contains(resource4));
  }

  @Test
  public void getResourcesByStringAttributeValueAndExactMatch() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesByStringAttributeValue");

    Facility facility = setUpFacility("testFacility");

    Resource resource1 = setUpResource("testResource01", vo, facility);
    Resource resource2 = setUpResource("testResource02", vo, facility);
    Resource resource3 = setUpResource("testResource03", vo, facility);
    Resource resource4 = setUpResource("testResource04", vo, facility);

    String searchedValue = "searchedValue";
    String otherValue = "otherValue";
    String attributeName = "testAttribute";

    AttributeDefinition ad = setUpResourceAttribute(attributeName, String.class.getName());
    Attribute searchedAttribute = new Attribute(ad, searchedValue);
    Attribute otherAttribute = new Attribute(ad, otherValue);

    perun.getAttributesManagerBl().setAttribute(sess, resource1, searchedAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, resource2, otherAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, resource3, otherAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, resource4, searchedAttribute);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_RESOURCE_ATTR_DEF + ":" + attributeName, searchedValue);

    List<Resource> foundResources = searcherBl.getResources(sess, searchParams, false);

    assertThat(foundResources).containsOnlyOnce(resource1, resource4);
  }

  @Test
  public void getResourcesByStringAttributeValueAndPartialMatch() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesByStringAttributeValue");

    Facility facility = setUpFacility("testFacility");

    Resource resource1 = setUpResource("testResource01", vo, facility);
    Resource resource2 = setUpResource("testResource02", vo, facility);
    Resource resource3 = setUpResource("testResource03", vo, facility);
    Resource resource4 = setUpResource("testResource04", vo, facility);

    String searchedValue = "searchedValue";
    String otherValue = "otherValue";
    String attributeName = "testAttribute";

    AttributeDefinition ad = setUpResourceAttribute(attributeName, String.class.getName());
    Attribute searchedAttribute = new Attribute(ad, searchedValue);
    Attribute otherAttribute = new Attribute(ad, otherValue);

    perun.getAttributesManagerBl().setAttribute(sess, resource1, searchedAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, resource2, otherAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, resource3, otherAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, resource4, searchedAttribute);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_RESOURCE_ATTR_DEF + ":" + attributeName, "value");

    List<Resource> foundResources = searcherBl.getResources(sess, searchParams, true);

    assertThat(foundResources).containsOnlyOnce(resource1, resource2, resource3, resource4);
  }

  @Test
  public void getResourcesByTwoAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "getResourcesByTwoAttributeValue");

    Facility facility = setUpFacility("testFacility");

    Resource resource1 = setUpResource("testResource01", vo, facility);
    Resource resource2 = setUpResource("testResource02", vo, facility);
    Resource resource3 = setUpResource("testResource03", vo, facility);
    Resource resource4 = setUpResource("testResource04", vo, facility);

    String searchedValue1 = "searchedValue1";
    String searchedValue2 = "searchedValue2";
    String attributeName1 = "testAttribute1";
    String attributeName2 = "testAttribute2";

    AttributeDefinition ad1 = setUpResourceAttribute(attributeName1, String.class.getName());
    AttributeDefinition ad2 = setUpResourceAttribute(attributeName2, String.class.getName());
    Attribute searchedAttribute1 = new Attribute(ad1, searchedValue1);
    Attribute searchedAttribute2 = new Attribute(ad2, searchedValue2);

    perun.getAttributesManagerBl().setAttribute(sess, resource1, searchedAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, resource2, searchedAttribute2);
    perun.getAttributesManagerBl().setAttribute(sess, resource3, searchedAttribute2);
    perun.getAttributesManagerBl().setAttribute(sess, resource4, searchedAttribute1);
    perun.getAttributesManagerBl().setAttribute(sess, resource4, searchedAttribute2);

    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(AttributesManager.NS_RESOURCE_ATTR_DEF + ":" + attributeName1, searchedValue1);
    searchParams.put(AttributesManager.NS_RESOURCE_ATTR_DEF + ":" + attributeName2, searchedValue2);

    List<Resource> foundResources = searcherBl.getResources(sess, searchParams, false);

    assertEquals("Found invalid number of resources", 1, foundResources.size());
    assertTrue("Found resources did not contain resource it should.", foundResources.contains(resource4));
  }

  @Test
  public void getUsersForCoreAttribute() throws Exception {
    System.out.println(CLASS_NAME + "getUsersForCoreAttribute");
    Attribute attr = perun.getAttributesManagerBl().getAttribute(sess, user1, "urn:perun:user:attribute-def:core:id");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(attr.getName(), attr.getValue().toString());
    List<User> users = searcherBl.getUsers(sess, attributesWithSearchingValues);
    assertTrue("user2 have not to be found", !users.contains(user2));
    assertTrue("user1 have to be found", users.contains(user1));
  }

  @Test
  public void getUsersForCoreAttributeDifferentCase() throws Exception {
    System.out.println(CLASS_NAME + "getUsersForCoreAttribute");
    Attribute attr =
        perun.getAttributesManagerBl().getAttribute(sess, user1, "urn:perun:user:attribute-def:core:firstName");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(attr.getName(), attr.getValue().toString().toUpperCase());
    List<User> users = searcherBl.getUsers(sess, attributesWithSearchingValues);
    assertTrue("user2 have not to be found", !users.contains(user2));
    assertTrue("user1 have to be found", users.contains(user1));
  }

  @Test
  public void getUsersForIntegerValue() throws Exception {
    System.out.println(CLASS_NAME + "getUsersForIntegerValue");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(integerAttr.getName(), "100");
    AttributeDefinition attrDef =
        sess.getPerun().getAttributesManager().getAttributeDefinition(sess, integerAttr.getName());
    Attribute attr = new Attribute(attrDef);
    List<User> users = searcherBl.getUsers(sess, attributesWithSearchingValues);
    assertTrue("user1 have to be found", users.contains(user1));
    assertTrue("user2 have not to be found", !users.contains(user2));
  }

  @Test
  public void getUsersForListValue() throws Exception {
    System.out.println(CLASS_NAME + "getUsersForListValue");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(listAttr.getName(), "ttribute2");
    List<User> users = searcherBl.getUsers(sess, attributesWithSearchingValues);
    assertTrue("user2 have to be found", users.contains(user2));
    assertTrue("user1 have to be found", users.contains(user1));
  }

  @Test
  public void getUsersForMapValue() throws Exception {
    System.out.println(CLASS_NAME + "getUsersForMapValue");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(mapAttr.getName(), "UserLargeAttribute=test value");
    List<User> users = searcherBl.getUsers(sess, attributesWithSearchingValues);
    assertTrue("user2 have not to be found", !users.contains(user2));
    assertTrue("user1 have to be found", users.contains(user1));
  }

  @Test
  public void getUsersForStringValue() throws Exception {
    System.out.println(CLASS_NAME + "getUsersForStringValue");
    Map<String, String> attributesWithSearchingValues = new HashMap<>();
    attributesWithSearchingValues.put(stringAttr.getName(), "UserStringAttribute test value");
    List<User> users = searcherBl.getUsers(sess, attributesWithSearchingValues);
    assertTrue("user1 have not to be found", !users.contains(user1));
    assertTrue("user2 have to be found", users.contains(user2));
  }

  @Test
  public void globalSearch() throws Exception {
    System.out.println(CLASS_NAME + "globalSearch");

    Vo vo1 = perun.getVosManagerBl().createVo(sess, new Vo(0, "globalTestVo1", "globalTestVo1"));
    Vo vo2 = perun.getVosManagerBl().createVo(sess, new Vo(0, "globalTestVo2", "globalTestVo2"));

    Group group1 = setUpGroupInVo("globalTestGroup1", vo1);
    Group group2 = setUpGroupInVo("globalTestGroup2", vo2);

    Facility facility1 = setUpFacility("globalTestFacility1");
    Facility facility2 = setUpFacility("globalTestFacility2");

    User user1 = setUpUser("Global", "Test1", "user1");
    User user2 = setUpUser("Global", "Test2", "user2");

    Map<String, List<PerunBean>> results = searcherBl.globalSearchPerunAdmin(sess, "global");

    assertThat(results.get("users")).containsExactlyInAnyOrder(user1, user2);
    assertThat(results.get("facilities")).containsExactlyInAnyOrder(facility1, facility2);
    // will also include created Members groups
    assertThat(results.get("groups")).contains(group1, group2);
    assertThat(results.get("groups").size()).isEqualTo(4);
    assertThat(results.get("vos")).containsExactlyInAnyOrder(vo1, vo2);

  }

  @Test
  public void globalSearchUsersWithAttributes() throws Exception {
    System.out.println(CLASS_NAME + "globalSearchUsersWithAttributes");

    User user1 = setUpUser("Global", "Test1", "user1");
    User user2 = setUpUser("Global", "Test2", "user2");

    AttributeDefinition prefMailAttrDef =
        perun.getAttributesManagerBl().getAttributeDefinition(sess,
            AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
    Attribute prefMail = new Attribute(prefMailAttrDef);
    prefMail.setValue("mail@mail.com");

    perun.getAttributesManagerBl().setAttribute(sess, user1, prefMail);

    prefMail.setValue("mail2@mail.com");
    perun.getAttributesManagerBl().setAttribute(sess, user2, prefMail);

    Map<String, List<PerunBean>> results = searcherBl.globalSearchPerunAdmin(sess, "mail@mail.com");
    assertThat(results.get("users")).containsExactly(user1);
  }

  @Before
  public void setUp() throws Exception {
    searcherBl = perun.getSearcherBl();
    vo = setUpVo();
    candidate1 = setUpCandidate("aaa1", "bbb1", "aaa");
    candidate2 = setUpCandidate("aaa2", "bbb2", "bbb");
    setUpUser1();
    setUpUser2();
    group = setUpGroupInVo("test group", vo);
    perun.getGroupsManagerBl().addMember(sess, group, member1);
    perun.getGroupsManagerBl().addMember(sess, group, member2);
    memberStringAttribute = setUpMemberAttributeWithStringValue();
    integerAttr = setUpUserAttributeWithIntegerValue();
    stringAttr = setUpUserAttributeWithStringValue();
    listAttr = setUpUserAttributeWithListValue();
    mapAttr = setUpUserLargeAttributeWithMapValue();
    perun.getAttributesManagerBl().setAttribute(sess, member1, memberStringAttribute);
    perun.getAttributesManagerBl().setAttribute(sess, user1, integerAttr);
    perun.getAttributesManagerBl().setAttribute(sess, user1, mapAttr);
    perun.getAttributesManagerBl().setAttribute(sess, user1, listAttr);
    perun.getAttributesManagerBl().setAttribute(sess, user2, stringAttr);
    perun.getAttributesManagerBl().setAttribute(sess, user2, listAttr);
  }

  // PRIVATE METHODS -----------------------------------------------------------

  private AttributeDefinition setUpAttribute(String name, String type, String nameSpace) throws Exception {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(nameSpace);
    attr.setFriendlyName(name);
    attr.setType(type);
    attr.setDisplayName(name);
    attr.setDescription(name + " testing attribute");

    return perun.getAttributesManagerBl().createAttribute(sess, attr);
  }

  private Candidate setUpCandidate(String firstName, String lastName, String extLogin) {
    Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
    candidate.setFirstName(firstName);
    candidate.setId(0);
    candidate.setMiddleName("");
    candidate.setLastName(lastName);
    candidate.setTitleBefore("");
    candidate.setTitleAfter("");
    final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
    candidate.setUserExtSource(userExtSource);
    candidate.setAttributes(new HashMap<>());
    return candidate;
  }

  private Facility setUpFacility(String name) throws Exception {
    Facility facility = new Facility(0, name, name);

    return perun.getFacilitiesManagerBl().createFacility(sess, facility);
  }

  private AttributeDefinition setUpFacilityAttribute(String name, String type) throws Exception {
    return setUpAttribute(name, type, AttributesManager.NS_FACILITY_ATTR_DEF);
  }

  private Group setUpGroupInVo(String name, Vo vo) throws Exception {
    group = new Group();
    group.setName(name);
    group = perun.getGroupsManagerBl().createGroup(sess, vo, group);

    return group;
  }

  private AttributeDefinition setUpGroupMembershipExpirationAttribute() throws Exception {

    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
    attr.setFriendlyName("groupMembershipExpiration");
    attr.setType(String.class.getName());
    attr.setDisplayName("Group membership expiration");
    attr.setDescription("When the member expires in group, format YYYY-MM-DD.");

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

  private AttributeDefinition setUpMembershipExpirationAttribute() throws Exception {

    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace("urn:perun:member:attribute-def:def");
    attr.setFriendlyName("membershipExpiration");
    attr.setType(String.class.getName());
    attr.setDisplayName("Membership expiration");
    attr.setDescription("Membership expiration date.");

    return perun.getAttributesManager().createAttribute(sess, attr);

  }

  private Resource setUpResource(String name, Vo vo, Facility facility) throws Exception {
    Resource resource = new Resource(0, name, name, facility.getId());

    return perun.getResourcesManagerBl().createResource(sess, resource, vo, facility);
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

  private AttributeDefinition setUpResourceAttribute(String name, String type) throws Exception {
    return setUpAttribute(name, type, AttributesManager.NS_RESOURCE_ATTR_DEF);
  }

  private Attribute setUpMemberAttributeWithStringValue() throws Exception {
    Attribute attr = new Attribute();
    attr.setNamespace("urn:perun:member:attribute-def:opt");
    attr.setFriendlyName("member-string-test-attribute");
    attr.setType(String.class.getName());
    attr.setValue("MemberStringAttribute test value");
    assertNotNull("unable to create member attribute", perun.getAttributesManagerBl().createAttribute(sess, attr));
    return attr;
  }

  private User setUpUser(String firstName, String lastName, String login) throws Exception {
    Candidate candidate = setUpCandidate(firstName, lastName, login);
    Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    return perun.getUsersManagerBl().getUserByMember(sess, member);
  }

  private void setUpUser1() throws Exception {
    member1 = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate1);
    user1 = perun.getUsersManagerBl().getUserByMember(sess, member1);
  }

  private void setUpUser2() throws Exception {
    member2 = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate2);
    user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
  }

  private Attribute setUpUserAttributeWithIntegerValue() throws Exception {

    Attribute attr = new Attribute();
    attr.setNamespace("urn:perun:user:attribute-def:opt");
    attr.setFriendlyName("user-integer-test-attribute");
    attr.setType(Integer.class.getName());
    attr.setValue(100);
    assertNotNull("unable to create user attribute", perun.getAttributesManagerBl().createAttribute(sess, attr));
    // create new resource member attribute
    return attr;

  }

  private Attribute setUpUserAttributeWithListValue() throws Exception {

    Attribute attr = new Attribute();
    attr.setNamespace("urn:perun:user:attribute-def:opt");
    attr.setFriendlyName("user-list-test-attribute");
    attr.setType(ArrayList.class.getName());
    List<String> value = new ArrayList<>();
    value.add("UserStringAttribute test value");
    value.add("UserStringAttribute2 test2 value2");
    attr.setValue(value);
    assertNotNull("unable to create user attribute", perun.getAttributesManagerBl().createAttribute(sess, attr));
    // create new resource member attribute
    return attr;

  }

  private Attribute setUpUserAttributeWithStringValue() throws Exception {

    Attribute attr = new Attribute();
    attr.setNamespace("urn:perun:user:attribute-def:opt");
    attr.setFriendlyName("user-string-test-attribute");
    attr.setType(String.class.getName());
    attr.setValue("UserStringAttribute test value");
    assertNotNull("unable to create user attribute", perun.getAttributesManagerBl().createAttribute(sess, attr));
    // create new resource member attribute
    return attr;
  }

  private Attribute setUpUserLargeAttributeWithMapValue() throws Exception {

    Attribute attr = new Attribute();
    attr.setNamespace("urn:perun:user:attribute-def:opt");
    attr.setFriendlyName("user-map-test-large-attribute");
    attr.setType(LinkedHashMap.class.getName());
    Map<String, String> value = new LinkedHashMap<>();
    value.put("UserLargeAttribute", "test value");
    attr.setValue(value);
    assertNotNull("unable to create user attribute", perun.getAttributesManagerBl().createAttribute(sess, attr));
    return attr;

  }

  private Vo setUpVo() throws Exception {

    Vo newVo = new Vo(0, "UserManagerTestVo", "UMTestVo");
    Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
    // create test VO in database
    assertNotNull("unable to create testing Vo", returnedVo);
    newVo.setId(returnedVo.getId());
    assertEquals("both VOs should be the same", newVo, returnedVo);
    ExtSource newExtSource = new ExtSource(extSourceName, ExtSourcesManager.EXTSOURCE_INTERNAL);
    ExtSource es = perun.getExtSourcesManager().createExtSource(sess, newExtSource, null);
    // get real external source from DB
    perun.getExtSourcesManager().addExtSource(sess, returnedVo, es);
    // add real ext source to our VO
    return returnedVo;
  }
}
