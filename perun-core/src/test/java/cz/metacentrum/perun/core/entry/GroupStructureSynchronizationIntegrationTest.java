package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.ExtSourcesManagerBl;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.impl.ExtSourceLdap;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests of group structure synchronization.
 *
 * @author Peter Balčirák
 * @author Erik Horváth
 */
public class GroupStructureSynchronizationIntegrationTest extends AbstractPerunIntegrationTest{
	private final static String CLASS_NAME = "GroupsManager.";
	private static final String EXT_SOURCE_NAME = "GroupSyncExtSource";

	private final Group baseGroup = new Group("baseGroup", "I am base group");
	private Vo vo;
	private ExtSource extSource = new ExtSource(0, EXT_SOURCE_NAME, ExtSourcesManager.EXTSOURCE_LDAP);

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
	//Mocked extSource, so we can simulate obtaining real extSource data
	private final ExtSourceSimpleApi essa = mock(ExtSourceLdap.class);


	@Before
	public void setUpBeforeEveryMethod() throws Exception {
		//perun from AbstractPerunIntegrationTest need to be assigned to our perun object in which we are injecting mocks.
		this.perun = (PerunBlImpl)super.perun;

		//Injected mocks would preserve in the perun object even after this test class,
		//because it is created by Spring in parent class and it is is used by other tests classes again.
		//To prevent this situation, we need to back up the real extSourceManagerBl and set it back to the perun after tests finish.
		extSourceManagerBlBackup = perun.getExtSourcesManagerBl();

		groupsManagerBl = perun.getGroupsManagerBl();
		extSourceManagerBl = perun.getExtSourcesManagerBl();
		attributesManagerBl = perun.getAttributesManagerBl();

		vo = setUpVo();
		setUpBaseGroup(vo);

		MockitoAnnotations.initMocks(this);

		doReturn(essa).when(extSourceManagerBl).getExtSourceByName(any(PerunSession.class), any(String.class));
		//noinspection ResultOfMethodCallIgnored
		doReturn(EXT_SOURCE_NAME).when((ExtSourceLdap)essa).getName();
		doNothing().when(extSourceManagerBl).addExtSource(any(PerunSession.class), any(Group.class), any(ExtSource.class));
	}

	@After
	public void cleanUp() {
		perun.setExtSourcesManagerBl(extSourceManagerBlBackup);
		Mockito.reset(extSourceManagerBl);
	}

	@Test
	public void addGroupUnderBaseGroupTest() throws Exception {
		System.out.println(CLASS_NAME + "addGroupUnderBaseGroupTest");

		// setup
		final TestGroup testGroup = new TestGroup("createdGroup", baseGroup.getShortName(), "group is child of base group");
		List<Map<String, String>> subjects = Collections.singletonList(testGroup.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		// tested method
		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		// asserts
		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

		assertTrue("New sub group should be created under base group!", 1 == subGroups.size());

		Group createdGroup = subGroups.get(0);
		assertGroup(testGroup.getGroupName(), baseGroup.getId(), testGroup.getDescription(), createdGroup);
	}

	@Test
	public void addGroupNoParentGroupTest() throws Exception {
		System.out.println(CLASS_NAME + "addGroupNoParentGroupTest");

		final TestGroup testGroup = new TestGroup("createdGroup", null, "group without parent");
		List<Map<String, String>> subjects = Collections.singletonList(testGroup.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

		assertTrue("New sub group under base group should be created!", 1 == subGroups.size());

		Group createdGroup = subGroups.get(0);
		assertGroup(testGroup.getGroupName(), baseGroup.getId(), testGroup.getDescription(), createdGroup);
	}

	@Test
	public void addGroupParentDoesNotExist() throws Exception {
		System.out.println(CLASS_NAME + "addGroupParentDoesNotExist");

		final TestGroup testGroup = new TestGroup("createdGroup", "nonExistingParent", "group's parent does not exist");
		List<Map<String, String>> subjects = Collections.singletonList(testGroup.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

		assertTrue("New sub group under base group should be created!", 1 == subGroups.size());

		Group createdGroup = subGroups.get(0);
		assertGroup(testGroup.getGroupName(), baseGroup.getId(), testGroup.getDescription(), createdGroup);
	}

	@Test
	public void addMultipleGroupsToBaseGroup() throws Exception {
		System.out.println(CLASS_NAME + "addMultipleGroupsToBaseGroup");

		final TestGroup testGroupA = new TestGroup("groupA", baseGroup.getShortName(), "description of group A");
		final TestGroup testGroupB = new TestGroup("groupB", baseGroup.getShortName(), "description of group B");
		List<Map<String, String>> subjects = Arrays.asList(testGroupA.toMap(), testGroupB.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

		assertTrue("2 new sub groups under base group should be created!", 2 == subGroups.size());
	}

	@Test
	public void addGroupsToBaseGroupInDifferentOrder() throws Exception {
		System.out.println(CLASS_NAME + "addGroupsToBaseGroupInDifferentOrder");

		final TestGroup testGroupA = new TestGroup("groupA", null, "description of group A");
		final TestGroup testGroupB = new TestGroup("groupB", "groupA", "description of group B");
		List<Map<String, String>> subjects = Arrays.asList(testGroupB.toMap(), testGroupA.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

		Group groupA = groupsManagerBl.getGroupByName(sess, vo, "baseGroup:groupA");
		Group groupB = groupsManagerBl.getGroupByName(sess, vo, "baseGroup:groupA:groupB");

		assertTrue("1 new sub group under base group should be created!", 1 == subGroups.size());
		assertTrue("groupA should be created under base group!", subGroups.contains(groupA));
		assertTrue("groupB should be created under groupA!", groupsManagerBl.getSubGroups(sess, groupA).contains(groupB));
	}

	@Test
	public void addGroupAsSubGroupTest() throws Exception {
		System.out.println(CLASS_NAME + "addGroupAsSubGroupTest");

		// setup
		final Group subGroup = new Group("baseSubGroup", "child of base group");
		groupsManagerBl.createGroup(sess, baseGroup, subGroup);
		final TestGroup subTestGroup = new TestGroup(subGroup.getShortName(), baseGroup.getShortName(), subGroup.getDescription());
		List<Map<String, String>> subjects = new ArrayList<>();
		subjects.add(subTestGroup.toMap());

		final TestGroup testGroup = new TestGroup("createdGroup", subGroup.getShortName(), "child of subgroup (baseGroup -> subGroup -> [this group])");
		subjects.add(testGroup.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		// tested method
		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		// assertions
		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, subGroup);

		assertTrue("New sub group under base group should be created!", 1 == subGroups.size());

		Group createdGroup = subGroups.get(0);
		assertGroup(testGroup.getGroupName(), subGroup.getId(), testGroup.getDescription(), createdGroup);
	}

	@Test
	public void addComplexSubTreeTest() throws Exception {
		System.out.println(CLASS_NAME + "addComplexSubTreeTest");

		List<Map<String, String>> subjects = new ArrayList<>();

		final Group childOfBaseGroup = new Group("childOfBaseGroup", "child of base group");
		groupsManagerBl.createGroup(sess, baseGroup, childOfBaseGroup);
		final TestGroup subTestGroup = new TestGroup(childOfBaseGroup.getShortName(), baseGroup.getShortName(), childOfBaseGroup.getDescription());
		subjects.add(subTestGroup.toMap());

		List<Map<String, String>> complexGroupTree = makeComplexGroupTreeSample(childOfBaseGroup.getShortName());
		subjects.addAll(complexGroupTree);
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		assertTrue("No users should be skipped!", skipped.isEmpty());

		// assert structure
		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

		assertTrue("No direct children should be added or removed from base group!", 1 == subGroups.size());
		Group checkedGroup = subGroups.get(0);
		assertTrue("Short name of existing group should not change!", childOfBaseGroup.getShortName().equals(checkedGroup.getShortName()));
		assertTrue("Name of existing group should not change!", childOfBaseGroup.getName().equals(checkedGroup.getName()));
		assertTrue("Description of existing group should no change!", childOfBaseGroup.getDescription().equals(checkedGroup.getDescription()));

		subGroups = groupsManagerBl.getAllSubGroups(sess, childOfBaseGroup);
		assertComplexGroupTree(childOfBaseGroup, subGroups);
	}

	@Test
	public void removeGroupUnderBaseGroupTest() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupUnderBaseGroupTest");

		final Group subGroup = new Group("baseSubGroup", "child of base group");
		groupsManagerBl.createGroup(sess, baseGroup, subGroup);

		List<Map<String, String>> subjects = new ArrayList<>();
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

		assertTrue("List of sub groups of base group should be empty!", subGroups.isEmpty());
	}

	@Test
	public void removeLeafGroupTest() throws Exception {
		System.out.println(CLASS_NAME + "removeLeafGroupTest");

		// setup
		final Group subBaseGroup = new Group("baseSubGroup", "child of base group");
		groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);

		final Group subGroup = new Group("subGroup", "child of sub base group");
		groupsManagerBl.createGroup(sess, subBaseGroup, subGroup);

		final TestGroup subBaseTestGroup = new TestGroup("subGroup", baseGroup.getShortName(), "child of base group");
		List<Map<String, String>> subjects = Collections.singletonList(subBaseTestGroup.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		// tested method
		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		// asserts
		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

		assertTrue("Leaf group should be removed!", 1 == subGroups.size());

		Group returnedGroup = subGroups.get(0);
		assertGroup(subBaseTestGroup.getGroupName(), baseGroup.getId(), subBaseTestGroup.getDescription(), returnedGroup);
	}

	@Test
	public void removeIntermediaryGroupTest() throws Exception {
		System.out.println(CLASS_NAME + "removeIntermediaryGroupTest");

		final Group subBaseGroup = new Group("baseSubGroup", "child of base group");
		groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);
		final Group interGroup = new Group("interGroup", "child of sub base group");
		groupsManagerBl.createGroup(sess, subBaseGroup, interGroup);
		final Group leafGroup = new Group("leafGroup", "leaf group");
		groupsManagerBl.createGroup(sess, interGroup, leafGroup);

		final TestGroup subBaseTestGroup = new TestGroup("baseSubGroup", baseGroup.getShortName(), "child of base group");
		final TestGroup leafTestGroup = new TestGroup("leafGroup", subBaseGroup.getShortName(), "leaf group");
		List<Map<String, String>> subjects = Arrays.asList(subBaseTestGroup.toMap(), leafTestGroup.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

		assertTrue("Base group should have exactly one child!", 1 == subGroups.size());
		Group baseGroupChild = subGroups.get(0);
		assertGroup(subBaseTestGroup.getGroupName(), baseGroup.getId(), subBaseTestGroup.getDescription(), baseGroupChild);

		subGroups = groupsManagerBl.getSubGroups(sess, baseGroupChild);

		assertTrue("Child of base group should have only one child!", 1 == subGroups.size());
		Group subBaseGroupChild = subGroups.get(0);
		assertGroup(leafTestGroup.getGroupName(), baseGroupChild.getId(), leafTestGroup.getDescription(), subBaseGroupChild);

		assertTrue("Leaf group should not have any children!", 0 == groupsManagerBl.getSubGroupsCount(sess, subBaseGroupChild));
	}

	@Test
	public void modifyGroupNameTest() throws Exception {
		System.out.println(CLASS_NAME + "modifyGroupNameTest");

		final Group subBaseGroup = new Group("group1", "child of base group");
		groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);

		final TestGroup modifiedSubBaseTestGroup = new TestGroup("modified", baseGroup.getShortName(), "child of base group");
		List<Map<String, String>> subjects = Collections.singletonList(modifiedSubBaseTestGroup.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);
		assertTrue("Base group should have exactly one child!", 1 == subGroups.size());
		Group subBaseGroupChild = subGroups.get(0);
		assertGroup(modifiedSubBaseTestGroup.getGroupName(), baseGroup.getId(), modifiedSubBaseTestGroup.getDescription(), subBaseGroupChild);
	}

	@Test
	public void modifyGroupDescriptionTest() throws Exception {
		System.out.println(CLASS_NAME + "modifyGroupDescriptionTest");

		final Group subBaseGroup = new Group("group1", "child of base group");
		groupsManagerBl.createGroup(sess, baseGroup, subBaseGroup);

		final TestGroup modifiedSubBaseTestGroup = new TestGroup("group1", baseGroup.getShortName(), "modified");
		List<Map<String, String>> subjects = Collections.singletonList(modifiedSubBaseTestGroup.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);
		assertTrue("Base group should have exactly one child!", 1 == subGroups.size());
		Group subBaseGroupChild = subGroups.get(0);
		assertGroup(modifiedSubBaseTestGroup.getGroupName(), baseGroup.getId(), modifiedSubBaseTestGroup.getDescription(), subBaseGroupChild);
	}

	@Test
	public void modifyParentGroupTest() throws Exception {
		System.out.println(CLASS_NAME + "modifyParentGroupTest");

		final Group groupA = new Group("groupA", "group A");
		groupsManagerBl.createGroup(sess, baseGroup, groupA);

		final Group groupB = new Group("groupB", "group B");
		groupsManagerBl.createGroup(sess, baseGroup, groupB);

		final TestGroup testGroupA = new TestGroup("groupA", baseGroup.getShortName(), "group A");
		final TestGroup testGroupB = new TestGroup("groupB", groupA.getShortName(), "group B");
		List<Map<String, String>> subjects = Arrays.asList(testGroupA.toMap(), testGroupB.toMap());
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);
		assertTrue("Base group should have exactly one child!",1 == subGroups.size());

		Group subBaseGroup = subGroups.get(0);
		assertGroup(testGroupA.getGroupName(), baseGroup.getId(), testGroupA.getDescription(), subBaseGroup);

		subGroups = groupsManagerBl.getSubGroups(sess, subBaseGroup);
		assertTrue("Child of base group should have exactly one child!", 1 == subGroups.size());

		Group childOfBaseGroup = subGroups.get(0);
		assertGroup(testGroupB.getGroupName(), groupA.getId(), testGroupB.getDescription(), childOfBaseGroup);
	}

	@Test
	public void removeAllGroupsTest() throws Exception {
		System.out.println(CLASS_NAME + "removeAllGroupsTest");

		final Group subGroupA = new Group("baseSubGroupA", "child A of base group");
		final Group subGroupB = new Group("baseSubGroupB", "child B of base group");
		final Group subSubGroup1 = new Group("subGroup1", "child 1 of sub group A");
		groupsManagerBl.createGroup(sess, baseGroup, subGroupA);
		groupsManagerBl.createGroup(sess, baseGroup, subGroupB);
		groupsManagerBl.createGroup(sess, subGroupA, subSubGroup1);

		List<Map<String, String>> subjects = new ArrayList<>();
		when(essa.getSubjectGroups(anyMap())).thenReturn(subjects);
		when(essa.getGroupSubjects(anyMap())).thenReturn(Collections.emptyList());

		List<String> skipped = groupsManagerBl.synchronizeGroupStructure(sess, baseGroup);

		assertTrue("No users should be skipped!", skipped.isEmpty());

		List<Group> subGroups = groupsManagerBl.getSubGroups(sess, baseGroup);

		assertTrue("List of sub groups of base group should be empty!", subGroups.isEmpty());
	}


	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "UserManagerTestVo", "UMTestVo");
		Vo returnedVo = perun.getVosManagerBl().createVo(sess, newVo);
		// create test VO in database
		assertNotNull("unable to create testing Vo",returnedVo);

		return returnedVo;

	}

	public void setUpBaseGroup(Vo vo) throws Exception {
		extSource = extSourceManagerBl.createExtSource(sess, extSource, null);

		Group returnedGroup = groupsManagerBl.createGroup(sess, vo, baseGroup);

		extSourceManagerBl.addExtSource(sess, vo, extSource);
		Attribute attr = attributesManagerBl.getAttribute(sess, baseGroup, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
		attr.setValue(extSource.getName());
		attributesManagerBl.setAttribute(sess, baseGroup, attr);
		extSourceManagerBl.addExtSource(sess, baseGroup, extSource);

		Attribute membersQuery = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME));
		membersQuery.setValue("SELECT * from members where groupName='?';");
		attributesManagerBl.setAttribute(sess, baseGroup, membersQuery);

		// create test Group in database
		assertNotNull("unable to create testing Group",returnedGroup);
	}

	/**
	 * Created structure text visualization:
	 *
	 *  Layer 0:                       [rootGroup]
	 *                                 /         \
	 *  Layer 1:        --------[groupA]     [groupB]
	 *                 /         /     \          \
	 *  Layer 2: [group1] [group2]   [group3]   [group4]
	 *                      |          |
	 *  Layer 3:          [groupI]   [groupII]
	 *                      |
	 *  Layer 4:        [groupRed]
	 */
	private List<Map<String, String>> makeComplexGroupTreeSample(String rootGroupName) {
		// 1st layer
		TestGroup groupA = new TestGroup("groupA", rootGroupName, "group A is child of root group");
		TestGroup groupB = new TestGroup("groupB", rootGroupName, "group B is child of root group");
		// 2nd layer
		TestGroup group1 = new TestGroup("group1", groupA.getGroupName(), "group 1 is child of group A");
		TestGroup group2 = new TestGroup("group2", groupA.getGroupName(), "group 2 is child of group A");
		TestGroup group3 = new TestGroup("group3", groupA.getGroupName(), "group 3 is child of group A");
		TestGroup group4 = new TestGroup("group4", groupB.getGroupName(), "group 4 is child of group B");
		// 3rd layer
		TestGroup groupI = new TestGroup("groupI", group2.getGroupName(), "group I is child of group 2");
		TestGroup groupII = new TestGroup("groupII", group3.getGroupName(), "group II is child of group 3");
		// 4th layer
		TestGroup groupRed = new TestGroup("groupRed", groupI.getGroupName(), "group Red is child of group I");

		return Arrays.asList(groupA.toMap(), groupB.toMap(),
				group1.toMap(), group2.toMap(), group3.toMap(), group4.toMap(),
				groupI.toMap(), groupII.toMap(),
				groupRed.toMap());
	}

	private void assertComplexGroupTree(Group rootGroup, List<Group> allGroupsInTree) {
		// 1st layer
		List<Group> foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("groupA")).collect(Collectors.toList());
		assertEquals("Only one group with name groupA should be created!", 1, foundGroups.size());
		Group groupA = foundGroups.get(0);
		assertGroup("groupA", rootGroup.getId(), "group A is child of root group", groupA);

		foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("groupB")).collect(Collectors.toList());
		assertEquals("Only one group with name groupB should be created!", 1, foundGroups.size());
		Group groupB = foundGroups.get(0);
		assertGroup("groupB", rootGroup.getId(), "group B is child of root group", groupB);

		// 2nd layer
		foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("group1")).collect(Collectors.toList());
		assertEquals("Only one group with name group1 should be created!", 1, foundGroups.size());
		Group group1 = foundGroups.get(0);
		assertGroup("group1", groupA.getId(), "group 1 is child of group A", group1);

		foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("group2")).collect(Collectors.toList());
		assertEquals("Only one group with name group2 should be created!", 1, foundGroups.size());
		Group group2 = foundGroups.get(0);
		assertGroup("group2", groupA.getId(), "group 2 is child of group A", group2);

		foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("group3")).collect(Collectors.toList());
		assertEquals("Only one group with name group3 should be created!", 1, foundGroups.size());
		Group group3 = foundGroups.get(0);
		assertGroup("group3", groupA.getId(), "group 3 is child of group A", group3);

		foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("group4")).collect(Collectors.toList());
		assertEquals("Only one group with name group4 should be created!", 1, foundGroups.size());
		Group group4 = foundGroups.get(0);
		assertGroup("group4", groupB.getId(), "group 4 is child of group B", group4);

		// 3rd layer
		foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("groupI")).collect(Collectors.toList());
		assertEquals("Only one group with name groupI should be created!", 1, foundGroups.size());
		Group groupI = foundGroups.get(0);
		assertGroup("groupI", group2.getId(), "group I is child of group 2", groupI);

		foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("groupII")).collect(Collectors.toList());
		assertEquals("Only one group with name groupII should be created!", 1, foundGroups.size());
		Group groupII = foundGroups.get(0);
		assertGroup("groupII", group3.getId(), "group II is child of group 3", groupII);

		// 4th layer
		foundGroups = allGroupsInTree.stream().filter(g -> g.getShortName().equals("groupRed")).collect(Collectors.toList());
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
			assertEquals("Group A should have tree root as parent group!", (int) expectedParentId, (int) group.getParentGroupId());
		}

		assertTrue("Group A should not have null description!", group.getDescription() != null);
		assertEquals("Group A should have correct description!", expectedDescription, group.getDescription());
	}

	private class TestGroup {
		private final String groupName;
		private final String parentGroupName;
		private final String description;

		public TestGroup(String groupName, String parentGroupName, String description) {
			this.groupName = groupName;
			this.parentGroupName = parentGroupName;
			this.description = description;
		}

		public String getGroupName() {
			return groupName;
		}

		public String getDescription() {
			return description;
		}

		public Map<String, String> toMap() {
			Map<String, String> subject = new HashMap<>();
			subject.put(GroupsManagerBlImpl.GROUP_NAME, groupName);
			if (parentGroupName != null) {
				subject.put(GroupsManagerBlImpl.PARENT_GROUP_NAME, parentGroupName);
			}
			subject.put(GroupsManagerBlImpl.GROUP_DESCRIPTION, description);
			return subject;
		}
	}
}
