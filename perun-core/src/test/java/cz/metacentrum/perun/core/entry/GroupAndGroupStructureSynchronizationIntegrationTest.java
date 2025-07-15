package cz.metacentrum.perun.core.entry;

import static cz.metacentrum.perun.core.impl.PerunLocksUtils.lockGroupMembership;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.CandidateSync;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.ExtSourcesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.impl.ExtSourceLdap;
import cz.metacentrum.perun.core.impl.ExtSourcesManagerImpl;
import cz.metacentrum.perun.core.impl.PerunLocksUtils;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * Integration tests of group structure synchronization.
 *
 * @author Peter Balčirák
 * @author Erik Horváth
 */
public class GroupAndGroupStructureSynchronizationIntegrationTest extends AbstractPerunIntegrationTest {
  private static final String CLASS_NAME = "GroupsManager.";
  private static final String EXT_SOURCE_NAME = "GroupSyncExtSource";
  private static final String ADDITIONAL_STRING = "additionalString";
  private static final String ADDITIONAL_LIST = "additionalList";
  private static final String A_G_D_SYNC_RESOURCES = AttributesManager.NS_GROUP_ATTR_DEF + ":groupStructureResources";
  private static final String A_MG_D_MEMBERSHIP_EXPIRATION =
      AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration";

  private final Group baseGroup = new Group("baseGroup", "I am base group");
  private final ExtSource extSourceForUserCreation =
      new ExtSource(0, "testExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL);
  //Mocked extSource, so we can simulate obtaining real extSource data
  private final ExtSourceSimpleApi essa = mock(ExtSourceLdap.class);
  private Vo vo;
  private ExtSource extSource = new ExtSource(0, EXT_SOURCE_NAME, ExtSourcesManager.EXTSOURCE_LDAP);
  private Resource resource1;
  private Resource resource2;
  private Facility facility;
  private Member member;
  private Group group;
  //This annotation is used so spied extSourceManagerBl is used in the perun object.
  //Mocks are not injected yet. They are injected to perun when initMocks method is called.
  @InjectMocks
  private PerunBlImpl perun;
  private GroupsManagerBl groupsManagerBl;
  private ExtSourcesManagerBl extSourceManagerBlBackup;
  private AttributesManagerBl attributesManagerBl;
  //ExtSource manager has to work as usual except one case so we use spy annotation.
  //Be aware that spy annotation does not work for all managers!
  @Spy
  private ExtSourcesManagerBl extSourceManagerBl;

  @Test
  public void addComplexSubTreeTest() throws Exception {
    System.out.println(CLASS_NAME + "addComplexSubTreeTest");

    List<Map<String, String>> subjects = new ArrayList<>();

    final Group childOfBaseGroup = new Group("childOfBaseGroup", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, childOfBaseGroup);
    setLoginToGroup(baseGroup, childOfBaseGroup, "childOfBaseGroup");

    final TestGroup subTestGroup =
        new TestGroup(childOfBaseGroup.getShortName(), "childOfBaseGroup", null, childOfBaseGroup.getDescription());
    subjects.add(subTestGroup.toMap());

    List<Map<String, String>> complexGroupTree = makeComplexGroupTreeSample(childOfBaseGroup.getShortName());
    subjects.addAll(complexGroupTree);
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    // assert structure
    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

    assertTrue("No direct children should be added or removed from base group!", 1 == subGroups.size());
    Group checkedGroup = subGroups.get(0);
    assertTrue("Short name of existing group should not change!",
        childOfBaseGroup.getShortName().equals(checkedGroup.getShortName()));
    assertTrue("Name of existing group should not change!", childOfBaseGroup.getName().equals(checkedGroup.getName()));
    assertTrue("Description of existing group should no change!",
        childOfBaseGroup.getDescription().equals(checkedGroup.getDescription()));

    subGroups = groupsManagerBl.getAllSubGroups(sess, childOfBaseGroup);
    assertComplexGroupTree(childOfBaseGroup, subGroups);
  }

  @Test
  public void addGroupAsSubGroupTest() throws Exception {
    System.out.println(CLASS_NAME + "addGroupAsSubGroupTest");

    // setup
    final Group subGroup = new Group("baseSubGroup", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subGroup);
    setLoginToGroup(baseGroup, subGroup, "baseSubGroup");

    final TestGroup subTestGroup =
        new TestGroup(subGroup.getShortName(), "baseSubGroup", null, subGroup.getDescription());
    List<Map<String, String>> subjects = new ArrayList<>();
    subjects.add(subTestGroup.toMap());

    final TestGroup testGroup = new TestGroup("createdGroup", "createdGroup", "baseSubGroup",
        "child of subgroup (baseGroup -> subGroup -> [this group])");
    subjects.add(testGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    // tested method
    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    // assertions
    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, subGroup);

    assertTrue("New sub group under base group should be created!", 1 == subGroups.size());

    Group createdGroup = subGroups.get(0);
    assertGroup(testGroup.getGroupName(), subGroup.getId(), testGroup.getDescription(), createdGroup);
  }

  @Test
  public void addGroupParentDoesNotExist() throws Exception {
    System.out.println(CLASS_NAME + "addGroupParentDoesNotExist");

    final TestGroup testGroup =
        new TestGroup("createdGroup", "createdGroup", "nonExistingParent", "group's parent does not exist");
    List<Map<String, String>> subjects = Collections.singletonList(testGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

    assertTrue("New sub group under base group should be created!", 1 == subGroups.size());

    Group createdGroup = subGroups.get(0);
    assertGroup(testGroup.getGroupName(), baseGroup.getId(), testGroup.getDescription(), createdGroup);
  }

  @Test
  public void addGroupUnderBaseGroupTest() throws Exception {
    System.out.println(CLASS_NAME + "addGroupUnderBaseGroupTest");

    // setup
    final TestGroup testGroup = new TestGroup("createdGroup", "createdGroup", null, "group is child of base group");
    List<Map<String, String>> subjects = Collections.singletonList(testGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    // tested method
    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    // asserts
    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

    assertTrue("New sub group should be created under base group!", 1 == subGroups.size());

    Group createdGroup = subGroups.get(0);
    assertGroup(testGroup.getGroupName(), baseGroup.getId(), testGroup.getDescription(), createdGroup);
  }

  @Test
  public void addGroupsToBaseGroupInDifferentOrder() throws Exception {
    System.out.println(CLASS_NAME + "addGroupsToBaseGroupInDifferentOrder");

    final TestGroup testGroupA = new TestGroup("groupA", "groupA", null, "description of group A");
    final TestGroup testGroupB = new TestGroup("groupB", "groupB", "groupA", "description of group B");
    List<Map<String, String>> subjects = Arrays.asList(testGroupB.toMap(), testGroupA.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

    Group groupA = groupsManagerBl.getGroupByName(sess, vo, "baseGroup:groupA");
    Group groupB = groupsManagerBl.getGroupByName(sess, vo, "baseGroup:groupA:groupB");

    assertTrue("1 new sub group under base group should be created!", 1 == subGroups.size());
    assertTrue("groupA should be created under base group!", subGroups.contains(groupA));
    assertTrue("groupB should be created under groupA!", groupsManagerBl.getSubGroups(sess, groupA).contains(groupB));
  }

  @Test
  public void addMultipleGroupsToBaseGroup() throws Exception {
    System.out.println(CLASS_NAME + "addMultipleGroupsToBaseGroup");

    final TestGroup testGroupA = new TestGroup("groupA", "groupA", null, "description of group A");
    final TestGroup testGroupB = new TestGroup("groupB", "groupB", null, "description of group B");
    List<Map<String, String>> subjects = Arrays.asList(testGroupA.toMap(), testGroupB.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

    assertTrue("2 new sub groups under base group should be created!", 2 == subGroups.size());
  }

  @Test
  public void additionalAttributeIsUpdated() throws Exception {
    System.out.println(CLASS_NAME + "modifyGroupDescriptionTest");
    String updatedValue = "updatedValue";

    AttributeDefinition additionalAttr = setGroupAttribute(ADDITIONAL_STRING);

    final Group subBaseGroup = new Group("group1", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
    setLoginToGroup(baseGroup, subBaseGroup, "group1");
    attributesManagerBl.setAttribute(sess, subBaseGroup, new Attribute(additionalAttr, "old"));

    final TestGroup modifiedSubBaseTestGroup = new TestGroup("group1", "group1", null, "modified");
    List<Map<String, String>> subjects = Collections.singletonList(modifiedSubBaseTestGroup.toMap());
    subjects.get(0).put(additionalAttr.getName(), updatedValue);
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);
    assertThat(subGroups).hasSize(1);

    Group updatedGroup = subGroups.get(0);

    Attribute updatedAttribute = attributesManagerBl.getAttribute(sess, updatedGroup, additionalAttr.getName());

    assertThat(updatedAttribute.getValue()).isEqualTo(updatedValue);
  }

  @Test
  public void additionalListAttributeIsSet() throws Exception {
    System.out.println(CLASS_NAME + "additionalListAttributeIsSet");

    AttributeDefinition additionalAttr = setGroupAttribute(ADDITIONAL_LIST, ArrayList.class.getName());

    // setup
    final TestGroup testGroup = new TestGroup("createdGroup", "createdGroup", null, "group is child of base group");
    List<Map<String, String>> subjects = Collections.singletonList(testGroup.toMap());
    subjects.get(0).put(additionalAttr.getName(), "val1,val2,");
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    // tested method
    groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);
    Group createdGroup = subGroups.get(0);

    Attribute attribute = attributesManagerBl.getAttribute(sess, createdGroup, additionalAttr.getName());
    assertThat(attribute.valueAsList()).containsExactly("val1", "val2");
  }

  @Test
  public void additionalStringAttributeIsNotSetForOtherGroup() throws Exception {
    System.out.println(CLASS_NAME + "additionalStringAttributeIsNotSetForOtherGroup");

    AttributeDefinition additionalAttr = setGroupAttribute(ADDITIONAL_STRING);

    // setup
    final TestGroup testGroup = new TestGroup("createdGroup", "createdGroup", null, "group is child of base group");
    final TestGroup otherGroup = new TestGroup("otherGroup", "otherGroup", null, "group is child of base group");
    List<Map<String, String>> subjects = Stream.of(testGroup, otherGroup).map(TestGroup::toMap).collect(toList());

    subjects.get(0).put(additionalAttr.getName(), "val1");
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    // tested method
    groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);
    Group otherCreatedGroup = subGroups.get(1);

    Attribute attr = attributesManagerBl.getAttribute(sess, otherCreatedGroup, additionalAttr.getName());
    assertThat(attr.getValue()).isNull();
  }

  @Test
  public void additionalStringAttributeIsSet() throws Exception {
    System.out.println(CLASS_NAME + "additionalStringAttributeIsSet");

    AttributeDefinition additionalAttr = setGroupAttribute(ADDITIONAL_STRING);

    // setup
    final TestGroup testGroup = new TestGroup("createdGroup", "createdGroup", null, "group is child of base group");
    List<Map<String, String>> subjects = Collections.singletonList(testGroup.toMap());
    subjects.get(0).put(additionalAttr.getName(), "val1");
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    // tested method
    groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);
    Group createdGroup = subGroups.get(0);

    Attribute attribute = perun.getAttributesManagerBl().getAttribute(sess, createdGroup, additionalAttr.getName());
    assertThat(attribute.getValue()).isEqualTo("val1");
  }

  private void assertComplexGroupTree(Group rootGroup, List<Group> allGroupsInTree) {
    // 1st layer
    List<Group> foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("groupA")).collect(toList());
    assertEquals("Only one group with name groupA should be created!", 1, foundGroups.size());
    Group groupA = foundGroups.get(0);
    assertGroup("groupA", rootGroup.getId(), "group A is child of root group", groupA);

    foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("groupB")).collect(toList());
    assertEquals("Only one group with name groupB should be created!", 1, foundGroups.size());
    Group groupB = foundGroups.get(0);
    assertGroup("groupB", rootGroup.getId(), "group B is child of root group", groupB);

    // 2nd layer
    foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("group1")).collect(toList());
    assertEquals("Only one group with name group1 should be created!", 1, foundGroups.size());
    Group group1 = foundGroups.get(0);
    assertGroup("group1", groupA.getId(), "group 1 is child of group A", group1);

    foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("group2")).collect(toList());
    assertEquals("Only one group with name group2 should be created!", 1, foundGroups.size());
    Group group2 = foundGroups.get(0);
    assertGroup("group2", groupA.getId(), "group 2 is child of group A", group2);

    foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("group3")).collect(toList());
    assertEquals("Only one group with name group3 should be created!", 1, foundGroups.size());
    Group group3 = foundGroups.get(0);
    assertGroup("group3", groupA.getId(), "group 3 is child of group A", group3);

    foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("group4")).collect(toList());
    assertEquals("Only one group with name group4 should be created!", 1, foundGroups.size());
    Group group4 = foundGroups.get(0);
    assertGroup("group4", groupB.getId(), "group 4 is child of group B", group4);

    // 3rd layer
    foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("groupI")).collect(toList());
    assertEquals("Only one group with name groupI should be created!", 1, foundGroups.size());
    Group groupI = foundGroups.get(0);
    assertGroup("groupI", group2.getId(), "group I is child of group 2", groupI);

    foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("groupII")).collect(toList());
    assertEquals("Only one group with name groupII should be created!", 1, foundGroups.size());
    Group groupII = foundGroups.get(0);
    assertGroup("groupII", group3.getId(), "group II is child of group 3", groupII);

    // 4th layer
    foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("groupRed")).collect(toList());
    assertEquals("Only one group with name groupRed should be created!", 1, foundGroups.size());
    Group groupRed = foundGroups.get(0);
    assertGroup("groupRed", groupI.getId(), "group Red is child of group I", groupRed);
  }

  private void assertGroup(String expectedName, Integer expectedParentId, String expectedDescription, Group group) {
    assertTrue("Group should not have null name!", group.getShortName() != null);
    assertEquals("Group should have correct name!", expectedName, group.getShortName());
    if (expectedParentId == null) {
      assertTrue("Group A should have null as parent group!", group.getParentGroupId() == null);
    } else {
      assertTrue("Group A should not have null parent group!", group.getParentGroupId() != null);
      assertEquals("Group A should have tree root as parent group!", (int) expectedParentId,
          (int) group.getParentGroupId());
    }

    assertTrue("Group A should not have null description!", group.getDescription() != null);
    assertEquals("Group A should have correct description!", expectedDescription, group.getDescription());
  }

  @After
  public void cleanUp() {
    perun.setExtSourcesManagerBl(extSourceManagerBlBackup);
    Mockito.reset(extSourceManagerBl);
  }

  private String getLoginNameForBaseGroup(Group baseGroup) throws Exception {
    return attributesManagerBl.getAttribute(sess, baseGroup, GroupsManager.GROUPS_STRUCTURE_LOGIN_ATTRNAME)
               .valueAsString();
  }

  @Test
  public void lightweightSynchronizationAddExpiredGroupMember() throws Exception {
    System.out.println(CLASS_NAME + "lightweightSynchronizationAddExpiredGroupMember");

    String synchronizedUserLogin = "metodej";
    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute lightweightSynchronizationAttr =
        attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPLIGHTWEIGHTSYNCHRONIZATION_ATTRNAME);
    lightweightSynchronizationAttr.setValue(true);
    attributesManagerBl.setAttribute(sess, group, lightweightSynchronizationAttr);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", synchronizedUserLogin);
    attributes.put("status", "EXPIRED");
    // add the ues to additional ueses to get the user in the lightweight synchronization
    String uesName = "additionalUesName";
    attributes.put(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING,
        uesName + "|NonEmptyString|" + synchronizedUserLogin + ";;");
    subjects.add(attributes);

    Candidate candidate = setUpCandidate();

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);

    UserExtSource userExtSource = new UserExtSource(extSourceForUserCreation, synchronizedUserLogin);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    user.setFirstName(synchronizedUserLogin);
    perun.getUsersManagerBl().addUserExtSource(sess, user, userExtSource);

    when(extSourceManagerBl.getExtSourceByName(sess, "additionalUesName")).thenReturn(userExtSource.getExtSource());
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(MemberGroupStatus.EXPIRED, groupsManagerBl.getGroupMembers(sess, group).get(0).getGroupStatus());
  }

  @Test
  public void lightweightSynchronizationAddExpiredGroupMemberSetsLifecycleTimestamp() throws Exception {
    System.out.println(CLASS_NAME + "lightweightSynchronizationAddExpiredGroupMemberSetsLifecycleTimestamp");

    String synchronizedUserLogin = "metodej";
    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute lightweightSynchronizationAttr =
        attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPLIGHTWEIGHTSYNCHRONIZATION_ATTRNAME);
    lightweightSynchronizationAttr.setValue(true);
    attributesManagerBl.setAttribute(sess, group, lightweightSynchronizationAttr);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", synchronizedUserLogin);
    attributes.put("status", "EXPIRED");
    // add the ues to additional ueses to get the user in the lightweight synchronization
    String uesName = "additionalUesName";
    attributes.put(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING,
        uesName + "|NonEmptyString|" + synchronizedUserLogin + ";;");
    subjects.add(attributes);

    Candidate candidate = setUpCandidate();

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);

    UserExtSource userExtSource = new UserExtSource(extSourceForUserCreation, synchronizedUserLogin);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    user.setFirstName(synchronizedUserLogin);
    perun.getUsersManagerBl().addUserExtSource(sess, user, userExtSource);

    when(extSourceManagerBl.getExtSourceByName(sess, "additionalUesName")).thenReturn(userExtSource.getExtSource());
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroup(sess, group);
    Member foundMember = groupsManagerBl.getGroupMembers(sess, group).get(0);
    assertEquals(MemberGroupStatus.EXPIRED, foundMember.getGroupStatus());

    Attribute lifecycleAttr = perun.getAttributesManagerBl().getAttribute(sess, foundMember, group,
        AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":lifecycleTimestamps");
    assertEquals(lifecycleAttr.valueAsMap().get("expiredAt"),
        BeansUtils.getDateFormatterWithoutTime().format(new Date()));
  }

  @Test
  public void lightweightSynchronizationAlreadyGroupMemberExtendGroupStatus() throws Exception {
    System.out.println(CLASS_NAME + "lightweightSynchronizationAlreadyGroupMemberExtendGroupStatus");

    String synchronizedUserLogin = "metodej";
    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Group groupWithExpiration = new Group("GroupsManagerTestGroup2", "testovaci2");
    groupWithExpiration = groupsManagerBl.createGroup(sess, vo, groupWithExpiration);

    Attribute lightweightSynchronizationAttr = attributesManagerBl.getAttribute(sess, groupWithExpiration,
        GroupsManager.GROUPLIGHTWEIGHTSYNCHRONIZATION_ATTRNAME);
    lightweightSynchronizationAttr.setValue(true);
    attributesManagerBl.setAttribute(sess, groupWithExpiration, lightweightSynchronizationAttr);

    Attribute attr = attributesManagerBl.getAttribute(sess, groupWithExpiration, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, groupWithExpiration, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", synchronizedUserLogin);
    attributes.put("status", "VALID");
    // add the ues to additional ueses to get the user in the lightweight synchronization
    String uesName = "additionalUesName";
    attributes.put(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING,
        uesName + "|NonEmptyString|" + synchronizedUserLogin + ";;");
    subjects.add(attributes);

    Candidate candidate = setUpCandidate();

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);

    UserExtSource userExtSource = new UserExtSource(extSourceForUserCreation, synchronizedUserLogin);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    user.setFirstName(synchronizedUserLogin);
    perun.getUsersManagerBl().addUserExtSource(sess, user, userExtSource);
    groupsManagerBl.addMember(sess, groupWithExpiration, member);
    groupsManagerBl.expireMemberInGroup(sess, member, groupWithExpiration);

    setUpGroupWithExpiration(groupWithExpiration);

    when(extSourceManagerBl.getExtSourceByName(sess, "additionalUesName")).thenReturn(userExtSource.getExtSource());
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroup(sess, groupWithExpiration);
    assertEquals(MemberGroupStatus.VALID,
        groupsManagerBl.getGroupMembers(sess, groupWithExpiration).get(0).getGroupStatus());
    assertEquals(LocalDate.now().plusMonths(1).toString(),
        attributesManagerBl.getAttribute(sess, member, groupWithExpiration, A_MG_D_MEMBERSHIP_EXPIRATION).getValue());
  }

  @Test
  public void lightweightSynchronizationMemberNotInVO() throws Exception {
    System.out.println(CLASS_NAME + "lightweightSynchronizationMemberNotInVO");

    String synchronizedUserLogin = "metodej";
    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute lightweightSynchronizationAttr =
        attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPLIGHTWEIGHTSYNCHRONIZATION_ATTRNAME);
    lightweightSynchronizationAttr.setValue(true);
    attributesManagerBl.setAttribute(sess, group, lightweightSynchronizationAttr);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", synchronizedUserLogin);
    attributes.put("status", "VALID");
    // add the ues to additional ueses to get the user in the lightweight synchronization
    String uesName = "additionalUesName";
    attributes.put(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING,
        uesName + "|NonEmptyString|" + synchronizedUserLogin + ";;");
    subjects.add(attributes);

    Candidate candidate = setUpCandidate();

    Vo newVo = new Vo(1, "OtherVO", "OtherVo");
    Vo otherVo = perun.getVosManagerBl().createVo(sess, newVo);

    member = perun.getMembersManagerBl().createMemberSync(sess, otherVo, candidate);

    UserExtSource userExtSource = new UserExtSource(extSourceForUserCreation, synchronizedUserLogin);
    User user = perun.getUsersManagerBl().getUserByMember(sess, member);
    user.setFirstName(synchronizedUserLogin);
    perun.getUsersManagerBl().addUserExtSource(sess, user, userExtSource);

    when(extSourceManagerBl.getExtSourceByName(sess, "additionalUesName")).thenReturn(userExtSource.getExtSource());
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(0, groupsManagerBl.getGroupMembers(sess, group).size());
  }

  /**
   * Created structure text visualization:
   * <p>
   * Layer 0:                       [rootGroup] /         \ Layer 1:        --------[groupA]     [groupB] /         / \
   * \ Layer 2: [group1] [group2]   [group3]   [group4] |          | Layer 3:          [groupI]   [groupII] | Layer 4:
   * [groupRed]
   */
  private List<Map<String, String>> makeComplexGroupTreeSample(String rootGroupLogin) {
    // 1st layer
    TestGroup groupA = new TestGroup("groupA", "groupA", rootGroupLogin, "group A is child of root group");
    TestGroup groupB = new TestGroup("groupB", "groupB", rootGroupLogin, "group B is child of root group");
    // 2nd layer
    TestGroup group1 = new TestGroup("group1", "group1", groupA.getLogin(), "group 1 is child of group A");
    TestGroup group2 = new TestGroup("group2", "group2", groupA.getLogin(), "group 2 is child of group A");
    TestGroup group3 = new TestGroup("group3", "group3", groupA.getLogin(), "group 3 is child of group A");
    TestGroup group4 = new TestGroup("group4", "group4", groupB.getLogin(), "group 4 is child of group B");
    // 3rd layer
    TestGroup groupI = new TestGroup("groupI", "groupI", group2.getLogin(), "group I is child of group 2");
    TestGroup groupII = new TestGroup("groupII", "groupII", group3.getLogin(), "group II is child of group 3");
    // 4th layer
    TestGroup groupRed = new TestGroup("groupRed", "groupRed", groupI.getLogin(), "group Red is child of group I");

    return Arrays.asList(groupA.toMap(), groupB.toMap(), group1.toMap(), group2.toMap(), group3.toMap(), group4.toMap(),
        groupI.toMap(), groupII.toMap(), groupRed.toMap());
  }

  @Test
  public void mergeGroupAttributeTestList() throws Exception {
    System.out.println(CLASS_NAME + "mergeGroupAttributeTestList");

    final Group subBaseGroup = new Group("group1", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
    setLoginToGroup(baseGroup, subBaseGroup, "group1");

    AttributeDefinition additionalAttr = setGroupAttribute("list", ArrayList.class.getName());

    Attribute attribute = new Attribute(additionalAttr, new ArrayList<>(List.of("val0", "val1")));
    attributesManagerBl.setAttribute(sess, subBaseGroup, attribute);

    final TestGroup modifiedSubBaseTestGroup = new TestGroup("group1", "group1", null, "child of base group");
    List<Map<String, String>> subjects = Collections.singletonList(modifiedSubBaseTestGroup.toMap());
    subjects.get(0).put(additionalAttr.getName(), "val1,val2,");
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
    doReturn(Map.of("mergeGroupAttributes", attribute.getName())).when(extSourceManagerBl)
        .getAttributes((ExtSourceLdap) essa);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    attribute = attributesManagerBl.getAttribute(sess, subBaseGroup, additionalAttr.getName());
    assertThat(attribute.valueAsList()).containsExactly("val0", "val1", "val2");
  }

  @Test
  public void modifyGroupDetailsTest() throws Exception {
    System.out.println(CLASS_NAME + "modifyGroupDetailsTest");

    final Group subBaseGroup = new Group("group1", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
    setLoginToGroup(baseGroup, subBaseGroup, "group1");

    final TestGroup modifiedSubBaseTestGroup = new TestGroup("modifiedName", "group1", null, "modifiedDescription");
    List<Map<String, String>> subjects = Collections.singletonList(modifiedSubBaseTestGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);
    assertTrue("Base group should have exactly one child!", 1 == subGroups.size());
    Group subBaseGroupChild = subGroups.get(0);
    assertGroup(modifiedSubBaseTestGroup.getGroupName(), baseGroup.getId(), modifiedSubBaseTestGroup.getDescription(),
        subBaseGroupChild);
  }

  @Test
  public void modifyGroupLoginTest() throws Exception {
    System.out.println(CLASS_NAME + "modifyGroupLoginTest");

    final Group subBaseGroup = new Group("group1", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
    setLoginToGroup(baseGroup, subBaseGroup, "group1");

    final TestGroup modifiedSubBaseTestGroup = new TestGroup("group1", "modified", null, "child of base group");
    List<Map<String, String>> subjects = Collections.singletonList(modifiedSubBaseTestGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);
    assertTrue("Base group should have exactly one child!", 1 == subGroups.size());
    Group subBaseGroupChild = subGroups.get(0);
    assertGroup(modifiedSubBaseTestGroup.getGroupName(), baseGroup.getId(), modifiedSubBaseTestGroup.getDescription(),
        subBaseGroupChild);
  }

  @Test
  public void modifyParentGroupTest() throws Exception {
    System.out.println(CLASS_NAME + "modifyParentGroupTest");

    final Group groupA = new Group("groupA", "group A");
    groupsManagerBl.createGroup(sess, baseGroup, groupA);
    setLoginToGroup(baseGroup, groupA, "groupA");

    final Group groupB = new Group("groupB", "group B");
    groupsManagerBl.createGroup(sess, baseGroup, groupB);
    setLoginToGroup(baseGroup, groupB, "groupB");

    final TestGroup testGroupA = new TestGroup("groupA", "groupA", null, "group A");
    final TestGroup testGroupB = new TestGroup("groupB", "groupB", "groupA", "group B");
    List<Map<String, String>> subjects = Arrays.asList(testGroupA.toMap(), testGroupB.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);
    assertTrue("Base group should have exactly one child!", 1 == subGroups.size());

    Group subBaseGroup = subGroups.get(0);
    assertGroup(testGroupA.getGroupName(), baseGroup.getId(), testGroupA.getDescription(), subBaseGroup);

    subGroups = groupsManagerBl.getSubGroups(sess, subBaseGroup);
    assertTrue("Child of base group should have exactly one child!", 1 == subGroups.size());

    Group childOfBaseGroup = subGroups.get(0);
    assertGroup(testGroupB.getGroupName(), groupA.getId(), testGroupB.getDescription(), childOfBaseGroup);
  }

  @Test
  public void multipleResourcesAreSetToMultipleTrees() throws Exception {
    System.out.println(CLASS_NAME + "resourceIsSetToMultipleTrees");

    final Group subGroup = new Group("group1", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subGroup);
    setLoginToGroup(baseGroup, subGroup, "group1");

    final Group otherSubGroup = new Group("group2", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, otherSubGroup);
    setLoginToGroup(baseGroup, otherSubGroup, "group2");

    setSynchronizationResourcesAttribute(resource1.getId(), "group1");
    setSynchronizationResourcesAttribute(resource2.getId(), "group2");

    final TestGroup subBaseTestGroup = new TestGroup("group1", "group1", null, subGroup.getDescription());
    final TestGroup otherSubBaseTestGroup = new TestGroup("group2", "group2", null, otherSubGroup.getDescription());
    List<Map<String, String>> subjects = Arrays.asList(subBaseTestGroup.toMap(), otherSubBaseTestGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    List<Resource> baseGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, baseGroup);
    List<Resource> subGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, subGroup);
    List<Resource> otherSubGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, otherSubGroup);

    assertThat(baseGroupResources).doesNotContain(resource1, resource2);
    assertThat(subGroupResources).containsOnly(resource1);
    assertThat(otherSubGroupResources).containsOnly(resource2);
  }

  @Test
  public void overwriteGroupAttributeTestList() throws Exception {
    System.out.println(CLASS_NAME + "overwriteGroupAttributeTestList");

    final Group subBaseGroup = new Group("group1", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
    setLoginToGroup(baseGroup, subBaseGroup, "group1");

    AttributeDefinition additionalAttr = setGroupAttribute("list", ArrayList.class.getName());

    Attribute attribute = new Attribute(additionalAttr, new ArrayList<>(List.of("val0", "val1")));
    attributesManagerBl.setAttribute(sess, subBaseGroup, attribute);

    final TestGroup modifiedSubBaseTestGroup = new TestGroup("group1", "group1", null, "child of base group");
    List<Map<String, String>> subjects = Collections.singletonList(modifiedSubBaseTestGroup.toMap());
    subjects.get(0).put(additionalAttr.getName(), "val1,val2,");
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    attribute = attributesManagerBl.getAttribute(sess, subBaseGroup, additionalAttr.getName());
    assertThat(attribute.valueAsList()).containsExactly("val1", "val2");
  }

  @Test
  public void removeAllGroupsTest() throws Exception {
    System.out.println(CLASS_NAME + "removeAllGroupsTest");

    final Group subGroupA = new Group("baseSubGroupA", "child A of base group");
    final Group subGroupB = new Group("baseSubGroupB", "child B of base group");
    final Group subSubGroup1 = new Group("subGroup1", "child 1 of sub group A");
    groupsManagerBl.createGroup(sess, baseGroup, subGroupA);
    setLoginToGroup(baseGroup, subGroupA, "subGroupA");
    groupsManagerBl.createGroup(sess, baseGroup, subGroupB);
    setLoginToGroup(baseGroup, subGroupB, "subGroupB");
    groupsManagerBl.createGroup(sess, subGroupA, subSubGroup1);
    setLoginToGroup(baseGroup, subSubGroup1, "subSubGroup1");

    List<Map<String, String>> subjects = new ArrayList<>();
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

    assertTrue("List of sub groups of base group should be empty!", subGroups.isEmpty());
  }

  @Test
  public void removeGroupUnderBaseGroupTest() throws Exception {
    System.out.println(CLASS_NAME + "removeGroupUnderBaseGroupTest");

    final Group subGroup = new Group("baseSubGroup", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subGroup);

    List<Map<String, String>> subjects = new ArrayList<>();
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

    assertTrue("List of sub groups of base group should be empty!", subGroups.isEmpty());
  }

  @Test
  public void removeIntermediaryGroupTest() throws Exception {
    System.out.println(CLASS_NAME + "removeIntermediaryGroupTest");

    final Group subBaseGroup = new Group("baseSubGroup", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
    setLoginToGroup(baseGroup, subBaseGroup, "baseSubGroup");

    final Group interGroup = new Group("interGroup", "child of sub base group");
    groupsManagerBl.createGroup(sess, subBaseGroup, interGroup);
    setLoginToGroup(baseGroup, interGroup, "interGroup");

    final Group leafGroup = new Group("leafGroup", "leaf group");
    groupsManagerBl.createGroup(sess, interGroup, leafGroup);
    setLoginToGroup(baseGroup, leafGroup, "leafGroup");

    final TestGroup subBaseTestGroup = new TestGroup("baseSubGroup", "baseSubGroup", null, "child of base group");
    final TestGroup leafTestGroup = new TestGroup("leafGroup", "leafGroup", "baseSubGroup", "leaf group");
    List<Map<String, String>> subjects = Arrays.asList(subBaseTestGroup.toMap(), leafTestGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

    assertTrue("Base group should have exactly one child!", 1 == subGroups.size());
    Group baseGroupChild = subGroups.get(0);
    assertGroup(subBaseTestGroup.getGroupName(), baseGroup.getId(), subBaseTestGroup.getDescription(), baseGroupChild);

    subGroups = groupsManagerBl.getSubGroups(sess, baseGroupChild);

    assertTrue("Child of base group should have only one child!", 1 == subGroups.size());
    Group subBaseGroupChild = subGroups.get(0);
    assertGroup(leafTestGroup.getGroupName(), baseGroupChild.getId(), leafTestGroup.getDescription(),
        subBaseGroupChild);

    assertTrue("Leaf group should not have any children!",
        0 == groupsManagerBl.getSubGroupsCount(sess, subBaseGroupChild));
  }

  @Test
  public void removeLeafGroupTest() throws Exception {
    System.out.println(CLASS_NAME + "removeLeafGroupTest");

    // setup
    final Group subBaseGroup = new Group("baseSubGroup", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
    setLoginToGroup(baseGroup, subBaseGroup, "subBaseGroup");

    final Group subGroup = new Group("subGroup", "child of sub base group");
    groupsManagerBl.createGroup(sess, subBaseGroup, subGroup);
    setLoginToGroup(baseGroup, subGroup, "subGroup");

    final TestGroup subBaseTestGroup = new TestGroup("subGroup", "subGroup", null, "child of base group");
    List<Map<String, String>> subjects = Collections.singletonList(subBaseTestGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    // tested method
    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    // asserts
    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

    assertTrue("Leaf group should be removed!", 1 == subGroups.size());

    Group returnedGroup = subGroups.get(0);
    assertGroup(subBaseTestGroup.getGroupName(), baseGroup.getId(), subBaseTestGroup.getDescription(), returnedGroup);
  }

  @Test
  public void replaceStructureWithPrefix() throws Exception {
    System.out.println(CLASS_NAME + "removeIntermediaryGroupTestWithPrefix");

    String loginPrefix = "prefix-";
    setLoginPrefixForStructure(baseGroup, loginPrefix);

    final Group subBaseGroup = new Group("baseSubGroup", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
    setLoginToGroup(baseGroup, subBaseGroup, "baseSubGroup");

    final Group interGroup = new Group("interGroup", "child of sub base group");
    groupsManagerBl.createGroup(sess, subBaseGroup, interGroup);
    setLoginToGroup(baseGroup, interGroup, "interGroup");

    final Group leafGroup = new Group("leafGroup", "leaf group");
    groupsManagerBl.createGroup(sess, interGroup, leafGroup);
    setLoginToGroup(baseGroup, leafGroup, loginPrefix + "leafGroup");

    final TestGroup subBaseTestGroup = new TestGroup("baseSubGroup", "baseSubGroup", null, "child of base group");
    final TestGroup leafTestGroup = new TestGroup("leafGroup", "leafGroup", "baseSubGroup", "leaf group");
    List<Map<String, String>> subjects = Arrays.asList(subBaseTestGroup.toMap(), leafTestGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    assertTrue("No groups should be skipped!", skipped.isEmpty());

    List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

    assertTrue("Base group should have exactly one child!", 1 == subGroups.size());
    Group baseGroupChild = subGroups.get(0);
    assertGroup(subBaseTestGroup.getGroupName(), baseGroup.getId(), subBaseTestGroup.getDescription(), baseGroupChild);
    assertEquals(loginPrefix + "baseSubGroup",
        perun.getAttributesManagerBl().getAttribute(sess, baseGroupChild, getLoginNameForBaseGroup(baseGroup))
            .valueAsString());

    subGroups = groupsManagerBl.getSubGroups(sess, baseGroupChild);

    assertTrue("Child of base group should have only one child!", 1 == subGroups.size());
    Group subBaseGroupChild = subGroups.get(0);
    assertGroup(leafTestGroup.getGroupName(), baseGroupChild.getId(), leafTestGroup.getDescription(),
        subBaseGroupChild);
    assertEquals(loginPrefix + "leafGroup",
        perun.getAttributesManagerBl().getAttribute(sess, subBaseGroupChild, getLoginNameForBaseGroup(baseGroup))
            .valueAsString());

    assertTrue("Leaf group should not have any children!",
        0 == groupsManagerBl.getSubGroupsCount(sess, subBaseGroupChild));
  }

  @Test
  public void resourceIsNotSetToTheBaseGroupWhenNoLoginIsSpecified() throws Exception {
    System.out.println(CLASS_NAME + "resourceIsSetToTheBaseGroupWhenNoLoginIsSpecified");

    final Group subBaseGroup = new Group("group1", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
    setLoginToGroup(baseGroup, subBaseGroup, "group1");
    setSynchronizationResourcesAttribute(resource1.getId());

    final TestGroup subBaseTestGroup = new TestGroup("group1", "group1", null, subBaseGroup.getDescription());
    List<Map<String, String>> subjects = Collections.singletonList(subBaseTestGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    List<Resource> baseGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, baseGroup);

    assertThat(baseGroupResources).doesNotContain(resource1);
  }

  @Test
  public void resourceIsSetToASubGroupInTheStructure() throws Exception {
    System.out.println(CLASS_NAME + "resourceIsSetToASubGroupInTheStructure");

    final Group subGroup = new Group("group1", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subGroup);
    setLoginToGroup(baseGroup, subGroup, "group1");

    final Group otherSubGroup = new Group("group2", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, otherSubGroup);
    setLoginToGroup(baseGroup, otherSubGroup, "group2");

    setSynchronizationResourcesAttribute(resource1.getId(), "group1");

    final TestGroup subBaseTestGroup = new TestGroup("group1", "group1", null, subGroup.getDescription());
    final TestGroup otherSubBaseTestGroup = new TestGroup("group2", "group2", null, otherSubGroup.getDescription());
    List<Map<String, String>> subjects = Arrays.asList(subBaseTestGroup.toMap(), otherSubBaseTestGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    List<Resource> baseGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, baseGroup);
    List<Resource> subGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, subGroup);
    List<Resource> otherSubGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, otherSubGroup);

    assertThat(baseGroupResources).doesNotContain(resource1);
    assertThat(subGroupResources).contains(resource1);
    assertThat(otherSubGroupResources).doesNotContain(resource1);
  }

  @Test
  public void resourceIsSetToMultipleTrees() throws Exception {
    System.out.println(CLASS_NAME + "resourceIsSetToMultipleTrees");

    final Group subGroup = new Group("group1", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subGroup);
    setLoginToGroup(baseGroup, subGroup, "group1");

    final Group otherSubGroup = new Group("group2", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, otherSubGroup);
    setLoginToGroup(baseGroup, otherSubGroup, "group2");

    setSynchronizationResourcesAttribute(resource1.getId(), "group1", "group2");

    final TestGroup subBaseTestGroup = new TestGroup("group1", "group1", null, subGroup.getDescription());
    final TestGroup otherSubBaseTestGroup = new TestGroup("group2", "group2", null, otherSubGroup.getDescription());
    List<Map<String, String>> subjects = Arrays.asList(subBaseTestGroup.toMap(), otherSubBaseTestGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    List<Resource> baseGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, baseGroup);
    List<Resource> subGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, subGroup);
    List<Resource> otherSubGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, otherSubGroup);

    assertThat(baseGroupResources).doesNotContain(resource1);
    assertThat(subGroupResources).contains(resource1);
    assertThat(otherSubGroupResources).contains(resource1);
  }

  @Test
  public void resourceIsSetToTheSubgroupsOfTheBaseGroup() throws Exception {
    System.out.println(CLASS_NAME + "resourceIsSetToTheSubgroupsOfTheBaseGroup");

    final Group subBaseGroup = new Group("group1", "child of base group");
    groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
    setLoginToGroup(baseGroup, subBaseGroup, "group1");

    setSynchronizationResourcesAttribute(resource1.getId());

    final TestGroup subBaseTestGroup = new TestGroup("group1", "group1", null, subBaseGroup.getDescription());
    List<Map<String, String>> subjects = Collections.singletonList(subBaseTestGroup.toMap());
    when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

    List<Resource> subGroupResources = perun.getResourcesManagerBl().getAssignedResources(sess, subBaseGroup);

    assertThat(subGroupResources).contains(resource1);
  }

  private AttributeDefinition setGroupAttribute(String name) throws Exception {
    return setGroupAttribute(name, String.class.getName());
  }

  private AttributeDefinition setGroupAttribute(String name, String type) throws Exception {
    AttributeDefinition attrDef = new AttributeDefinition();
    attrDef.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attrDef.setDescription("Test attribute description");
    attrDef.setFriendlyName(name);
    attrDef.setType(type);
    attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
    Attribute attribute = new Attribute(attrDef);
    attribute.setValue("Testing value");
    return attribute;
  }

  private void setLoginPrefixForStructure(Group baseGroup, String prefix) throws Exception {
    Attribute loginPrefixAttribute = new Attribute(
        attributesManagerBl.getAttributeDefinition(sess, GroupsManager.GROUPS_STRUCTURE_LOGIN_PREFIX_ATTRNAME));
    loginPrefixAttribute.setValue(prefix);
    attributesManagerBl.setAttribute(sess, baseGroup, loginPrefixAttribute);
  }

  private void setLoginToGroup(Group baseGroup, Group groupToSetLoginFor, String login) throws Exception {
    String baseGroupAttrLoginName = getLoginNameForBaseGroup(baseGroup);
    Attribute loginAttr = new Attribute(attributesManagerBl.getAttributeDefinition(sess, baseGroupAttrLoginName));
    loginAttr.setValue(login);
    attributesManagerBl.setAttribute(sess, groupToSetLoginFor, loginAttr);
  }

  private void setSynchronizationResourcesAttribute(int resourceId, String... logins) throws Exception {
    Attribute attribute = perun.getAttributesManagerBl().getAttribute(sess, baseGroup, A_G_D_SYNC_RESOURCES);
    if (attribute.getValue() == null) {
      attribute.setValue(new LinkedHashMap<>());
    }
    StringBuilder groupLogins = new StringBuilder();
    for (String login : logins) {
      groupLogins.append(login).append(",");
    }
    attribute.valueAsMap().put(String.valueOf(resourceId), groupLogins.toString());
    perun.getAttributesManagerBl().setAttribute(sess, baseGroup, attribute);
  }

  private void setUpBaseGroup(Vo vo) throws Exception {
    extSource = extSourceManagerBl.createExtSource(sess, extSource, null);

    Group returnedGroup = groupsManagerBl.createGroup(sess, vo, baseGroup);

    extSourceManagerBl.addExtSource(sess, vo, extSource);
    Attribute attr = attributesManagerBl.getAttribute(sess, baseGroup, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, baseGroup, attr);
    extSourceManagerBl.addExtSource(sess, baseGroup, extSource);

    Attribute membersQuery = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl()
                                               .getAttributeDefinition(sess, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME));
    membersQuery.setValue("SELECT * from members where groupName='?';");
    attributesManagerBl.setAttribute(sess, baseGroup, membersQuery);

    Attribute interval = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl()
                                           .getAttributeDefinition(sess, GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME));
    interval.setValue("1");
    attributesManagerBl.setAttribute(sess, baseGroup, interval);

    AttributeDefinition loginAttrDef = setGroupAttribute("groupLogin");

    Attribute structureLoginName = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl()
                                                     .getAttributeDefinition(sess,
                                                         GroupsManager.GROUPS_STRUCTURE_LOGIN_ATTRNAME));
    structureLoginName.setValue(loginAttrDef.getName());
    attributesManagerBl.setAttribute(sess, baseGroup, structureLoginName);

    // create test Group in database
    assertNotNull("unable to create testing Group", returnedGroup);
  }

  @Before
  public void setUpBeforeEveryMethod() throws Exception {
    //perun from AbstractPerunIntegrationTest need to be assigned to our perun object in which we are injecting mocks.
    this.perun = (PerunBlImpl) super.perun;

    //Injected mocks would preserve in the perun object even after this test class,
    //because it is created by Spring in parent class and it is is used by other tests classes again.
    //To prevent this situation, we need to back up the real extSourceManagerBl and set it back to the perun after
    // tests finish.
    extSourceManagerBlBackup = perun.getExtSourcesManagerBl();

    groupsManagerBl = perun.getGroupsManagerBl();
    extSourceManagerBl = perun.getExtSourcesManagerBl();
    attributesManagerBl = perun.getAttributesManagerBl();

    vo = setUpVo();
    setUpBaseGroup(vo);
    setUpFacility();
    setUpResources();
    setUpGroup(vo);
    setUpMember(vo);

    MockitoAnnotations.initMocks(this);

    doReturn(essa).when(extSourceManagerBl).getExtSourceByName(any(PerunSession.class), any(String.class));
    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);
    when(extSourceManagerBl.getExtSourceByName(sess, extSourceForUserCreation.getName())).thenReturn(
        extSourceForUserCreation);
    //noinspection ResultOfMethodCallIgnored
    doReturn(EXT_SOURCE_NAME).when((ExtSourceLdap) essa).getName();
    doNothing().when(extSourceManagerBl).addExtSource(any(PerunSession.class), any(Group.class), any(ExtSource.class));
  }

  private Candidate setUpCandidate() {
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
    final UserExtSource userExtSource = new UserExtSource(extSourceForUserCreation, extLogin);
    candidate.setUserExtSource(userExtSource);
    candidate.setAttributes(new HashMap<>());
    return candidate;
  }

  private Facility setUpFacility() throws Exception {
    facility = new Facility(-1, "Facility");
    facility = perun.getFacilitiesManagerBl().createFacility(sess, facility);
    return facility;
  }

  private void setUpGroup(Vo vo) throws Exception {
    group = new Group("GroupsManagerTestGroup1", "testovaci1");
    group = groupsManagerBl.createGroup(sess, vo, group);
    assertNotNull("unable to create testing Group", group);
  }

  private void setUpGroupWithExpiration(Group groupWithExpiration)
      throws AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException,
                 WrongReferenceAttributeValueException {
    Attribute expirationRulesAttribute = new Attribute(attributesManagerBl.getAttributeDefinition(sess,
        AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembershipExpirationRules"));
    Map<String, String> values = new LinkedHashMap<>();
    values.put(AbstractMembershipExpirationRulesModule.MEMBERSHIP_PERIOD_KEY_NAME, "+1m");
    expirationRulesAttribute.setValue(values);
    attributesManagerBl.setAttribute(sess, groupWithExpiration, expirationRulesAttribute);
  }

  private void setUpMember(Vo vo) throws Exception {
    Candidate candidate = setUpCandidate();
    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    assertNotNull("No member created", member);
  }

  // PRIVATE METHODS

  private void setUpResources() throws Exception {
    resource1 = new Resource(-1, "resource1", "", facility.getId());
    resource1 = perun.getResourcesManagerBl().createResource(sess, resource1, vo, facility);

    resource2 = new Resource(-1, "resource2", "", facility.getId());
    resource2 = perun.getResourcesManagerBl().createResource(sess, resource2, vo, facility);
  }

  private Vo setUpVo() throws Exception {

    Vo newVo = new Vo(0, "UserManagerTestVo", "UMTestVo");
    Vo returnedVo = perun.getVosManagerBl().createVo(sess, newVo);
    // create test VO in database
    assertNotNull("unable to create testing Vo", returnedVo);

    return returnedVo;

  }

  @Test
  public void synchronizeExpiredGroupStatusMemberNotInVO() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeExpiredGroupStatusMemberNotInVO");

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    attributes.put("status", "EXPIRED");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();
    candidate.setExpectedSyncGroupStatus("EXPIRED");

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(MemberGroupStatus.EXPIRED, groupsManagerBl.getGroupMembers(sess, group).get(0).getGroupStatus());
  }

  @Test
  public void synchronizeExpiredGroupStatusMemberNotInVOSetsLifecycleTimestamps() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeExpiredGroupStatusMemberNotInVOSetsLifecycleTimestamps");

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    attributes.put("status", "EXPIRED");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();
    candidate.setExpectedSyncGroupStatus("EXPIRED");

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroup(sess, group);
    Member foundMember = groupsManagerBl.getGroupMembers(sess, group).get(0);
    assertEquals(MemberGroupStatus.EXPIRED, foundMember.getGroupStatus());

    Attribute lifecycleAttr = perun.getAttributesManagerBl().getAttribute(sess, foundMember, group,
        AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":lifecycleTimestamps");
    assertEquals(lifecycleAttr.valueAsMap().get("expiredAt"),
        BeansUtils.getDateFormatterWithoutTime().format(new Date()));
  }

  @Test
  public void synchronizeGroupAddMissingMember() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupAddMissingMember");

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    assertEquals(0, groupsManagerBl.getGroupMembers(sess, group).size());
    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(1, groupsManagerBl.getGroupMembers(sess, group).size());
  }

  @Test
  public void synchronizeGroupAddMissingMemberWhileCandidateAlreadyMember() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupAddMissingMemberWhileCandidateAlreadyMember");

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    assertEquals(0, groupsManagerBl.getGroupMembers(sess, group).size());
    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(1, groupsManagerBl.getGroupMembers(sess, group).size());
  }

  @Test
  public void synchronizeGroupMergeMemberAttributeValue() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupMergeMemberAttributeValue");

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    groupsManagerBl.addMember(sess, group, member);
    candidate.setFirstName("metodej");

    Attribute attribute = new Attribute();
    String namespace = "member-test-unique-attribute:specialNamespace";
    attribute.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
    attribute.setFriendlyName(namespace + "1");
    attribute.setType(ArrayList.class.getName());
    attribute.setValue(Stream.of("value1", "value2").collect(Collectors.toList()));
    assertNotNull("unable to create member attribute", attributesManagerBl.createAttribute(sess, attribute));

    Map<String, String> candidateAttrs = new HashMap<>();
    candidateAttrs.put(attribute.getName(), "value2,value3");
    candidate.setAttributes(candidateAttrs);
    attributesManagerBl.setAttribute(sess, member, attribute);

    Map<String, String> map = new HashMap<>();
    map.put("mergeMemberAttributes", attribute.getName());

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);
    doReturn(map).when(extSourceManagerBl).getAttributes((ExtSourceLdap) essa);

    assertEquals(2, attributesManagerBl.getAttribute(sess, member, attribute.getName()).valueAsList().size());
    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(3, attributesManagerBl.getAttribute(sess, member, attribute.getName()).valueAsList().size());
  }

  @Test
  public void synchronizeGroupRemoveInvalidMemberInMembersGroup() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupRemoveInvalidMemberInMembersGroup");

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    perun.getMembersManagerBl().setStatus(sess, member, Status.INVALID);
    groupsManagerBl.addMember(sess, group, member);
    group.setName(VosManager.MEMBERS_GROUP);

    assertTrue(groupsManagerBl.getGroupMembers(sess, group).contains(member));
    groupsManagerBl.synchronizeGroup(sess, group);
    assertFalse(groupsManagerBl.getGroupMembers(sess, group).contains(member));
  }

  @Test
  public void synchronizeGroupRemoveMember() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupRemoveMember");

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    groupsManagerBl.addMember(sess, group, member);

    assertTrue(groupsManagerBl.getGroupMembers(sess, group).contains(member));
    groupsManagerBl.synchronizeGroup(sess, group);
    assertFalse(groupsManagerBl.getGroupMembers(sess, group).contains(member));
  }

  @Test
  public void synchronizeGroupRemoveMemberInAuthoritativeGroup() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupRemoveMemberInAuthoritativeGroup");

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    groupsManagerBl.addMember(sess, group, member);

    Attribute attribute = perun.getAttributesManagerBl()
                              .getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF + ":authoritativeGroup");
    attribute.setValue(1);
    attributesManagerBl.setAttribute(sess, group, attribute);

    assertTrue(groupsManagerBl.getGroupMembers(sess, group).contains(member));
    groupsManagerBl.synchronizeGroup(sess, group);
    assertFalse(groupsManagerBl.getGroupMembers(sess, group).contains(member));
  }

  @Test
  public void synchronizeGroupRemoveMemberInMembersGroup() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupRemoveMemberInMembersGroup");

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    groupsManagerBl.addMember(sess, group, member);
    group.setName(VosManager.MEMBERS_GROUP);

    assertEquals(Status.VALID, groupsManagerBl.getGroupMembers(sess, group).get(0).getStatus());
    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(Status.DISABLED, groupsManagerBl.getGroupMembers(sess, group).get(0).getStatus());
  }

  @Test
  public void synchronizeGroupUpdateExistingMemberStatus() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupUpdateExistingMemberStatus");

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    perun.getMembersManagerBl().setStatus(sess, member, Status.DISABLED);
    groupsManagerBl.addMember(sess, group, member);

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    assertEquals(Status.DISABLED, groupsManagerBl.getGroupMembers(sess, group).get(0).getStatus());
    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(Status.VALID, groupsManagerBl.getGroupMembers(sess, group).get(0).getStatus());
  }

  @Test
  public void synchronizeGroupUpdateLifecycleTimestampAttribute() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupUpdateLifecycleTimestampAttribute");

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    perun.getMembersManagerBl().setStatus(sess, member, Status.DISABLED);

    groupsManagerBl.addMember(sess, group, member);

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    Member foundMember = groupsManagerBl.getGroupMembers(sess, group).get(0);

    assertEquals(Status.DISABLED, foundMember.getStatus());
    Attribute lifecycleAttr = perun.getAttributesManagerBl().getAttribute(sess, member,
        AttributesManager.NS_MEMBER_ATTR_DEF + ":lifecycleTimestamps");
    assertEquals(lifecycleAttr.valueAsMap().get("archivedAt"),
        BeansUtils.getDateFormatterWithoutTime().format(new Date()));


    groupsManagerBl.synchronizeGroup(sess, group);
    foundMember = groupsManagerBl.getGroupMembers(sess, group).get(0);
    assertEquals(Status.VALID, foundMember.getStatus());
    lifecycleAttr = perun.getAttributesManagerBl().getAttribute(sess, foundMember,
        AttributesManager.NS_MEMBER_ATTR_DEF + ":lifecycleTimestamps");
    assertNull(lifecycleAttr.getValue());
  }

  @Test
  public void synchronizeGroupUpdateFirstNameOfMember() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupUpdateFirstNameOfMember");

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    groupsManagerBl.addMember(sess, group, member);
    candidate.setFirstName("metodej");

    Attribute attribute = perun.getAttributesManagerBl()
                              .getAttribute(sess, perun.getUsersManagerBl().getUserByMember(sess, member),
                                  AttributesManager.NS_USER_ATTR_CORE + ":firstName");

    Map<String, String> candidateAttrs = new HashMap<>();
    candidateAttrs.put(attribute.getName(), attribute.valueAsString());
    candidate.setAttributes(candidateAttrs);

    Map<String, String> map = new HashMap<>();
    map.put("overwriteUserAttributes", attribute.getName());

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);
    doReturn(map).when(extSourceManagerBl).getAttributes((ExtSourceLdap) essa);

    assertNotEquals(candidate.getFirstName(), perun.getUsersManagerBl().getUserByMember(sess, member).getFirstName());
    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(candidate.getFirstName(), perun.getUsersManagerBl().getUserByMember(sess, member).getFirstName());
  }

  @Test
  public void synchronizeGroupUpdateMemberOrganizationsAttribute() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupUpdateMemberOrganizationsAttribute");

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();

    Vo memberVo = new Vo(0, "UserManagerTestVo2", "memberVo");
    memberVo = perun.getVosManagerBl().createVo(sess, memberVo);

    member = perun.getMembersManagerBl().createMemberSync(sess, memberVo, candidate);
    perun.getVosManagerBl().addMemberVo(sess, vo, memberVo);

    Member newMember =
        perun.getMembersManagerBl().getMemberByUser(sess, vo, perun.getUsersManagerBl().getUserByMember(sess, member));

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    Attribute memberOrganizations = perun.getAttributesManager()
                                        .getAttribute(sess, newMember,
                                            AttributesManager.NS_MEMBER_ATTR_DEF + ":memberOrganizations");
    ArrayList<String> currentMemberOrganizations = memberOrganizations.valueAsList();
    assertThat(currentMemberOrganizations).containsOnly(memberVo.getShortName());

    Attribute memberOrganizationsHistory = perun.getAttributesManager()
                                               .getAttribute(sess, newMember,
                                                   AttributesManager.NS_MEMBER_ATTR_DEF + ":memberOrganizations");
    ArrayList<String> currentMemberOrganizationsHistory = memberOrganizationsHistory.valueAsList();
    assertThat(currentMemberOrganizationsHistory).containsOnly(memberVo.getShortName());

    groupsManagerBl.synchronizeGroup(sess, group);

    memberOrganizations = perun.getAttributesManager()
                              .getAttribute(sess, newMember,
                                  AttributesManager.NS_MEMBER_ATTR_DEF + ":memberOrganizations");
    currentMemberOrganizations = memberOrganizations.valueAsList();
    assertThat(currentMemberOrganizations).containsOnly(vo.getShortName(), memberVo.getShortName());

    memberOrganizationsHistory = perun.getAttributesManager()
                                     .getAttribute(sess, newMember,
                                         AttributesManager.NS_MEMBER_ATTR_DEF + ":memberOrganizations");
    currentMemberOrganizationsHistory = memberOrganizationsHistory.valueAsList();
    assertThat(currentMemberOrganizationsHistory).containsOnly(vo.getShortName(), memberVo.getShortName());
  }

  @Test
  public void synchronizeGroupUpdateMembershipStatusFromExpiredToValid() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupUpdateMembershipStatusFromExpiredToValid");

    Group groupWithExpiration = new Group("GroupsManagerTestGroup2", "testovaci2");
    groupWithExpiration = groupsManagerBl.createGroup(sess, vo, groupWithExpiration);

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, groupWithExpiration, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, groupWithExpiration, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();
    // status is null but default should be valid
    candidate.setExpectedSyncGroupStatus(null);

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    groupsManagerBl.addMember(sess, groupWithExpiration, member);
    groupsManagerBl.expireMemberInGroup(sess, member, groupWithExpiration);

    setUpGroupWithExpiration(groupWithExpiration);

    Attribute groupMembExpirAttr =
        attributesManagerBl.getAttribute(sess, member, groupWithExpiration, A_MG_D_MEMBERSHIP_EXPIRATION);
    groupMembExpirAttr.setValue(LocalDate.now().toString());
    attributesManagerBl.setAttribute(sess, member, groupWithExpiration, groupMembExpirAttr);

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroup(sess, groupWithExpiration);
    assertEquals(MemberGroupStatus.VALID,
        groupsManagerBl.getGroupMembers(sess, groupWithExpiration).get(0).getGroupStatus());
    assertEquals(LocalDate.now().plusMonths(1).toString(),
        attributesManagerBl.getAttribute(sess, member, groupWithExpiration, A_MG_D_MEMBERSHIP_EXPIRATION).getValue());
  }

  @Test
  public void synchronizeGroupUpdateMembershipStatusToDefaultValid() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupUpdateMembershipStatusToDefaultValid");

    Group groupWithExpiration = new Group("GroupsManagerTestGroup2", "testovaci2");
    groupWithExpiration = groupsManagerBl.createGroup(sess, vo, groupWithExpiration);

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, groupWithExpiration, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, groupWithExpiration, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();
    // status is null but default should be valid
    candidate.setExpectedSyncGroupStatus(null);

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    groupsManagerBl.addMember(sess, groupWithExpiration, member);
    groupsManagerBl.expireMemberInGroup(sess, member, groupWithExpiration);

    setUpGroupWithExpiration(groupWithExpiration);

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroup(sess, groupWithExpiration);
    assertEquals(MemberGroupStatus.VALID,
        groupsManagerBl.getGroupMembers(sess, groupWithExpiration).get(0).getGroupStatus());
    assertEquals(LocalDate.now().plusMonths(1).toString(),
        attributesManagerBl.getAttribute(sess, member, groupWithExpiration, A_MG_D_MEMBERSHIP_EXPIRATION).getValue());
  }

  @Test
  public void synchronizeGroupUpdateMembershipStatusToExpired() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupUpdateMembershipStatusToExpired");
    String groupMembershipExpirationAttr = AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration";

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    attributes.put("status", "EXPIRED");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();
    candidate.setExpectedSyncGroupStatus("EXPIRED");

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    groupsManagerBl.addMember(sess, group, member);

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);

    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(MemberGroupStatus.EXPIRED, groupsManagerBl.getGroupMembers(sess, group).get(0).getGroupStatus());
    assertEquals(LocalDate.now().toString(),
        attributesManagerBl.getAttribute(sess, member, group, groupMembershipExpirationAttr).getValue());
  }

  @Test
  public void synchronizeGroupUpdateUserAttributeOfMember() throws Exception {
    System.out.println(CLASS_NAME + "synchronizeGroupUpdateUserAttributeOfMember");

    when(extSourceManagerBl.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN)).thenReturn(
        extSourceForUserCreation);

    Attribute attr = attributesManagerBl.getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
    attr.setValue(extSource.getName());
    attributesManagerBl.setAttribute(sess, group, attr);

    List<Map<String, String>> subjects = new ArrayList<>();
    Map<String, String> attributes = new HashMap<>();
    attributes.put("login", "metodej");
    subjects.add(attributes);
    Candidate candidate = setUpCandidate();

    member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
    groupsManagerBl.addMember(sess, group, member);

    Attribute attribute = new Attribute();
    String namespace = "user-test-unique-attribute:specialNamespace";
    attribute.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attribute.setFriendlyName(namespace + "1");
    attribute.setType(String.class.getName());
    attribute.setValue("UserAttribute");
    assertNotNull("unable to create user attribute", attributesManagerBl.createAttribute(sess, attribute));

    Map<String, String> candidateAttrs = new HashMap<>();
    candidateAttrs.put(attribute.getName(), attribute.valueAsString());
    candidate.setAttributes(candidateAttrs);

    Map<String, String> map = new HashMap<>();
    map.put("overwriteUserAttributes", attribute.getName());

    when(extSourceManagerBl.getCandidate(sess, attributes, (ExtSourceLdap) essa, "metodej")).thenReturn(
        new CandidateSync(candidate));
    when(essa.getGroupSubjects(anyMap())).thenReturn(subjects);
    doReturn(map).when(extSourceManagerBl).getAttributes((ExtSourceLdap) essa);

    User user = perun.getUsersManagerBl().getUserByMember(sess, member);

    assertNotEquals(candidate.getAttributes().get(attribute.getName()),
        attributesManagerBl.getAttribute(sess, user, attribute.getName()).valueAsString());
    groupsManagerBl.synchronizeGroup(sess, group);
    assertEquals(candidate.getAttributes().get(attribute.getName()),
        attributesManagerBl.getAttribute(sess, user, attribute.getName()).valueAsString());
  }

  @Test
  public void addMissingMemberWhileSynchronizationRollsBackCorrectly() throws Exception {
    System.out.println("GroupsManager.addMissingMemberWhileSynchronizationRollsBackCorrectly");

    Group group2 = new Group("GroupsManagerTestGroup2", "testovaci2");
    Group group3 = new Group("GroupsManagerTestGroup3", "testovaci3");
    Group group4 = new Group("GroupsManagerTestGroup4", "testovaci4");
    Group group5 = new Group("GroupsManagerTestGroup5", "testovaci5");
    Group group6 = new Group("GroupsManagerTestGroup6", "testovaci6");
    Group group7 = new Group("GroupsManagerTestGroup7", "testovaci7");
    groupsManagerBl.createGroup(sess, vo, group2);
    groupsManagerBl.createGroup(sess, vo, group3);
    groupsManagerBl.createGroup(sess, vo, group4);
    groupsManagerBl.createGroup(sess, vo, group5);
    groupsManagerBl.createGroup(sess, vo, group6);
    groupsManagerBl.createGroup(sess, vo, group7);

    groupsManagerBl.createGroupUnion(sess, group6, group7, false);
    groupsManagerBl.createGroupUnion(sess, group5, group7, false);
    groupsManagerBl.createGroupUnion(sess, group4, group7, false);
    groupsManagerBl.createGroupUnion(sess, group3, group4, false);
    groupsManagerBl.createGroupUnion(sess, group2, group4, false);
    groupsManagerBl.createGroupUnion(sess, group, group4, false);

    Candidate candidate = setUpCandidate();

    try (MockedStatic<PerunLocksUtils> mockedLockUtils = mockStatic(PerunLocksUtils.class)) {
      mockedLockUtils.when(() -> lockGroupMembership(eq(group3), any())).thenThrow(new RuntimeException());
      assertThrows(RuntimeException.class, () -> groupsManagerBl.addMissingMemberWhileSynchronization(
          sess, group7, candidate, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
      assertEquals(0, groupsManagerBl.getGroupMembers(sess, group7).size());
    }
  }

  private class TestGroup {
    private final String groupName;
    private final String login;
    private final String parentGroupLogin;
    private final String description;

    public TestGroup(String groupName, String login, String parentGroupLogin, String description) {
      this.groupName = groupName;
      this.login = login;
      this.parentGroupLogin = parentGroupLogin;
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    public String getGroupName() {
      return groupName;
    }

    public String getLogin() {
      return login;
    }

    public String getParentGroupLogin() {
      return parentGroupLogin;
    }

    public Map<String, String> toMap() {
      Map<String, String> subject = new HashMap<>();
      subject.put(GroupsManagerBlImpl.GROUP_NAME, groupName);
      subject.put(GroupsManagerBlImpl.GROUP_LOGIN, login);
      if (parentGroupLogin != null) {
        subject.put(GroupsManagerBlImpl.PARENT_GROUP_LOGIN, parentGroupLogin);
      }
      subject.put(GroupsManagerBlImpl.GROUP_DESCRIPTION, description);
      return subject;
    }
  }
}
