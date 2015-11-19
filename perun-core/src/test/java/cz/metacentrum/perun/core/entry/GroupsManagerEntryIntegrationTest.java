package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import org.junit.Before;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

/**
 * Integration tests of GroupsManager
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GroupsManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private final static String CLASS_NAME = "GroupsManager.";

	// these must be setUp"type" before every method to be in DB
	final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
	final Group group = new Group("GroupsManagerTestGroup1","testovaci1");
	final Group group2 = new Group("GroupsManagerTestGroup2","testovaci2");
	final Group group21 = new Group("GroupsManagerTestGroup21","testovaci21");
	final Group group3 = new Group("GroupsManagerTestGroup3","testovaci3");
	final Group group4 = new Group("GroupsManagerTestGroup4","testovaci4");
	final Group group5 = new Group("GroupsManagerTestGroup5","testovaci5");
	final Group group6 = new Group("GroupsManagerTestGroup6","testovaci6");
	final Group group7 = new Group("GroupsManagerTestGroup7","testovaci7");
	final Group group8 = new Group("GroupsManagerTestGroup8","testovaci8");
	final Group group9 = new Group("GroupsManagerTestGroup9","testovaci9");
	final Group group10 = new Group("GroupsManagerTestGroup10","testovaci10");
	final Group group11 = new Group("GroupsManagerTestGroup11","testovaci11");
	final Group group12 = new Group("GroupsManagerTestGroup12","testovaci12");
	final Group group13 = new Group("GroupsManagerTestGroup13","testovaci13");
	final Group group14 = new Group("GroupsManagerTestGroup14","testovaci14");
	private Vo vo;
	private List<Attribute> attributesList = new ArrayList<>();

	// exists before every method
	private GroupsManager groupsManager;
	private GroupsManagerBl groupsManagerBl;
	private AttributesManager attributesManager;

	@Before
	public void setUpBeforeEveryMethod() throws Exception {

		groupsManager = perun.getGroupsManager();
		groupsManagerBl = perun.getGroupsManagerBl();
		attributesManager = perun.getAttributesManager();
		// vo = setUpVo();
		// setUpGroup(vo);
		// moved to every method to save testing time

	}

	@Test
	public void getGroupsWithAssignedExtSourceInVo() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsWithAssignedExtSourceInVo");

		vo = setUpVo();
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.createGroup(sess, vo, group2);

		ExtSource newExtSource = new ExtSource("ExtSourcesManagerEntryIntegrationTest1", ExtSourcesManager.EXTSOURCE_INTERNAL);
		newExtSource = perun.getExtSourcesManager().createExtSource(sess, newExtSource, null);
		
		perun.getExtSourcesManagerBl().addExtSource(sess, vo, newExtSource);

		perun.getExtSourcesManagerBl().addExtSource(sess, group, newExtSource);
		perun.getExtSourcesManagerBl().addExtSource(sess, group2, newExtSource);

		final List<Group> groups = groupsManagerBl.getGroupsWithAssignedExtSourceInVo(sess, newExtSource, vo);

		assertTrue(groups.contains(group));
		assertTrue(groups.contains(group2));
	}

	@Test
	public void getGroupsUsers() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsUsers");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManagerBl.addMember(sess, group, member);
		User u = perun.getUsersManager().getUserByMember(sess, member);

		List<User> users = groupsManagerBl.getGroupUsers(sess, group);
		assertTrue("Users of group can't be null", users != null);
		assertTrue("Group must have exactly 1 user", users.size() == 1);
		assertTrue("User of group is not same as originally added", users.contains(u));

	}

	@Test
	public void testGroupNameLength() throws Exception {
		System.out.println(CLASS_NAME + "testGroupNameLength");

		Vo vo = setUpVo();

		Group g1 = new Group();
		Group g2 = new Group();

		String name1 = "";
		String name2 = "";

		for (int i=0; i<1999; i++) {
			name1 += "a";
			name2 += "b";
		}

		g1.setName(name1);
		g2.setName(name2);

		g1.setDescription("desc");
		g2.setDescription("desc2");

		// create group
		g1 = groupsManager.createGroup(sess, vo , g1);
		// create sub-group
		g2 = groupsManager.createGroup(sess, g1, g2);

		Group g3 = groupsManager.getGroupById(sess, g2.getId());

		assertTrue("Result name must be made from parentGroup name and group name.", g3.getName().equals(name1+":"+name2));
		assertTrue("Name is longer than 128 chars.", g3.getName().length() >= 128);


	}

	@Test
	public void createGroup() throws Exception {
		System.out.println(CLASS_NAME + "createGroup");

		vo = setUpVo();

		Group returnedGroup = groupsManager.createGroup(sess, vo, group2);
		assertNotNull(returnedGroup);
		assertNotNull(groupsManager.createGroup(sess, returnedGroup, group21));

	}

	@Test (expected=GroupNotExistsException.class)
	public void createGroupWhenParentNotExist() throws Exception {
		System.out.println(CLASS_NAME + "createGroupWhenParentNotExists");

		groupsManager.createGroup(sess, new Group(), new Group("GroupsManagerTestGroup2","testovaci2"));
	}

	@Test (expected=GroupExistsException.class)
	public void createGroupWhenTheSameWithTheSameParentGroupExists() throws Exception {
		System.out.println(CLASS_NAME + "createGroupWhenTheSameWithTheSameParentGroupExists");

		vo = setUpVo();
		Group parentG = new Group("TestingParentGroup01", "testingParentGroup01");
		groupsManager.createGroup(sess, vo, parentG);
		Group g1 = new Group("TestingChildGroup01","TestingChildGroup01");
		Group g2 = new Group("TestingChildGroup01","TestingChildGroup02");
		groupsManager.createGroup(sess, parentG, g1);
		groupsManager.createGroup(sess, parentG, g2);
	}

	@Test (expected=VoNotExistsException.class)
	public void createGroupWhenVoNotExist() throws Exception {
		System.out.println(CLASS_NAME + "createGroupWhenVoNotExists");

		groupsManager.createGroup(sess, new Vo(), new Group("GroupsManagerTestGroup2","testovaci2"));

	}

	@Test (expected=GroupExistsException.class)
	public void createGroupWhenSameGroupExist() throws Exception {
		System.out.println(CLASS_NAME + "createGroupWhenSameGroupExists");

		vo = setUpVo();
		setUpGroup(vo);

		groupsManager.createGroup(sess, vo, group);
		// shouldn't be able to create group with same name
	}

	@Test (expected=GroupNotExistsException.class)
	public void deleteGroup() throws Exception {
		System.out.println(CLASS_NAME + "deleteGroup");

		vo = setUpVo();
		setUpGroup(vo);
		// assertNotNull(groupsManager.createGroup(sess, group, group2));
		// create sub-group
		groupsManager.deleteGroup(sess, group);
		// delete group including sub-group
		groupsManager.getGroupById(sess, group.getId());
		// shouldn't find group

	}

	@Test
	public void deletesGroups() throws Exception {
		System.out.println(CLASS_NAME + "deletesGroups");
		Vo newVo = new Vo(0, "voForDeletingGroups", "voForDeletingGroups");
		newVo = perun.getVosManagerBl().createVo(sess, newVo);
		List<Group> groups = setUpGroupsWithSubgroups(newVo);

		this.groupsManager.deleteGroups(sess, groups, false);
	}

	@Test (expected=RelationExistsException.class)
	public void deleteGroupsWithSubgroupAndNoForceDelete() throws Exception {
		System.out.println(CLASS_NAME + "deleteGroupsWithSubgroupAndNoForceDelete");
		Vo newVo = new Vo(0, "voForDeletingGroups", "voForDeletingGroups");
		newVo = perun.getVosManagerBl().createVo(sess, newVo);
		List<Group> groups = setUpGroupsWithSubgroups(newVo);

		Group subgroup = new Group("Test", "test");
		subgroup = this.groupsManagerBl.createGroup(sess, groups.get(0), subgroup);
//		org.hsqldb.util.DatabaseManager.main(new String[] {
//				"--url", "jdbc:hsqldb:mem:dataSource", "--noexit"
//		});
		this.groupsManager.deleteGroups(sess, groups, false);
	}

	@Test
	public void deleteGroupsWithSubgroupAndForceDelete() throws Exception {
		System.out.println(CLASS_NAME + "deleteGroupsWithSubgroupAndForceDelete");
		Vo newVo = new Vo(0, "voForDeletingGroups", "voForDeletingGroups");
		newVo = perun.getVosManagerBl().createVo(sess, newVo);
		List<Group> groups = setUpGroupsWithSubgroups(newVo);

		Group subgroup = new Group("Test", "test");
		subgroup = this.groupsManagerBl.createGroup(sess, groups.get(0), subgroup);

		this.groupsManager.deleteGroups(sess, groups, true);
	}

	@Test
	public void deleteGroupWithSubgroupsAndForceDelete() throws Exception {
		System.out.println("GroupsManager.deleteGroupWithSubgroupsAndForceDelete");

		Vo newVo = new Vo(0, "voForDeletingGroups", "voForDeletingGroups");
		newVo = perun.getVosManagerBl().createVo(sess, newVo);
		List<Group> groups = setUpGroupsWithSubgroups(newVo);

		this.groupsManager.deleteGroup(sess, groups.get(5), true);
	}

	@Test (expected = GroupNotExistsException.class)
	public void deleteGroupWithRelations() throws Exception {
		System.out.println("GroupsManager.deleteGroupWithRelations");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);
		groupsManager.createGroup(sess, vo, group3);
		groupsManager.createGroup(sess, vo, group4);
		groupsManager.createGroup(sess, vo, group5);
		groupsManager.createGroup(sess, vo, group6);
		groupsManager.createGroup(sess, vo, group7);

		// first add members to groups
		Member member = setUpMember(vo);
		Member member2 = setUpMember(vo);
		groupsManager.addMember(sess, group, member2);
		groupsManager.addMember(sess, group2, member);
		groupsManager.addMember(sess, group3, member);
		groupsManager.addMember(sess, group5, member);
		groupsManager.addMember(sess, group6, member2);

		// then create relations between them
		groupsManager.groupUnion(sess, group2, group);
		groupsManager.groupDifference(sess, group3, group);
		groupsManager.groupUnion(sess, group4, group);
		groupsManager.groupUnion(sess, group, group5);
		groupsManager.groupDifference(sess, group, group6);
		groupsManager.groupUnion(sess, group7, group2);

		groupsManager.deleteGroup(sess, group, true);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group4).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group5).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group6).size() == 1);

		groupsManager.removeMember(sess, group5, member);

		groupsManager.deleteGroup(sess, group2, true);
		groupsManager.deleteGroup(sess, group3, true);
		groupsManager.deleteGroup(sess, group4, true);
		groupsManager.deleteGroup(sess, group5, true);
		groupsManager.deleteGroup(sess, group6, true);
		groupsManager.deleteGroup(sess, group7, true);

		//should throw exception
		try {
			groupsManagerBl.checkGroupExists(sess, group);
		} catch (GroupNotExistsException e) {
			try {
				groupsManagerBl.checkGroupExists(sess, group2);
			} catch (GroupNotExistsException ex) {
				try {
					groupsManagerBl.checkGroupExists(sess, group3);
				} catch (GroupNotExistsException exc) {
					try {
						groupsManagerBl.checkGroupExists(sess, group4);
					} catch (GroupNotExistsException exce) {
						try {
							groupsManagerBl.checkGroupExists(sess, group5);
						} catch (GroupNotExistsException excep) {
							try {
								groupsManagerBl.checkGroupExists(sess, group6);
							} catch (GroupNotExistsException except) {
								groupsManagerBl.checkGroupExists(sess, group7);
							}
						}
					}
				}
			}
		}
	}

	@Test (expected = GroupNotExistsException.class)
	public void deleteGroupWithSubgroupsAndRelations() throws Exception {
		System.out.println("GroupsManager.deleteGroupWithSubgroupsAndRelations");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, group, group2);
		groupsManager.createGroup(sess, group, group3);
		groupsManager.createGroup(sess, group, group4);
		groupsManager.createGroup(sess, group5, group);
		groupsManager.createGroup(sess, group6, group);

		// first add members to groups
		Member member = setUpMember(vo);
		Member member2 = setUpMember(vo);
		groupsManager.addMember(sess, group, member2);
		groupsManager.addMember(sess, group2, member);
		groupsManager.addMember(sess, group3, member);
		groupsManager.addMember(sess, group5, member);
		groupsManager.addMember(sess, group6, member2);

		// then create relations between them
		groupsManager.groupUnion(sess, group2, group);
		groupsManager.groupDifference(sess, group3, group);
		groupsManager.groupUnion(sess, group4, group);
		groupsManager.groupUnion(sess, group, group5);
		groupsManager.groupDifference(sess, group, group6);

		groupsManager.deleteGroup(sess, group, true);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group5).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group6).size() == 1);

		//should throw exception
		try {
			groupsManagerBl.checkGroupExists(sess, group);
		} catch (GroupNotExistsException e) {
			try {
				groupsManagerBl.checkGroupExists(sess, group2);
			} catch (GroupNotExistsException ex) {
				try {
					groupsManagerBl.checkGroupExists(sess, group3);
				} catch (GroupNotExistsException exc) {
					groupsManagerBl.checkGroupExists(sess, group4);
				}
			}
		}
	}

	@Test (expected=GroupNotExistsException.class)
	public void deleteGroupWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteGroupWhenGroupNotExists");

		groupsManager.deleteGroup(sess, new Group());
	}

	@Test (expected=RelationExistsException.class)
	public void deleteGroupWhenContainsMember() throws Exception {
		System.out.println(CLASS_NAME + "deleteGroupWhenContainsMember");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);

		groupsManager.deleteGroup(sess, group);
	}

	@Test (expected=GroupNotExistsException.class)
	public void deleteGroupForce() throws Exception {
		System.out.println(CLASS_NAME + "deleteGroupForce");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);
		assertNotNull(groupsManager.createGroup(sess, group, group2)); // create sub-group
		groupsManager.deleteGroup(sess, group, true); // force delete
		groupsManager.getGroupById(sess, group2.getId()); // shouldn't find our sub-group even when parent had member
	}

	@Test (expected=GroupNotExistsException.class)
	public void deleteGroupForceWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteGroupForceWhenGroupNotExists");

		groupsManager.deleteGroup(sess, new Group(), true);
	}

	@Test
	public void deleteAllGroups() throws Exception {
		System.out.println(CLASS_NAME + "deleteAllGroups");

		vo = setUpVo();
		setUpGroup(vo);

		groupsManager.deleteAllGroups(sess, vo);

		List<Group> groups = groupsManager.getAllGroups(sess, vo);
		assertEquals("groups should contains only 'members' group", 1, groups.size());

	}

	@Test(expected=VoNotExistsException.class)
	public void deleteAllGroupsWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteAllGroupsWhenVoNotExists");

		groupsManager.deleteAllGroups(sess, new Vo());

	}

	@Test
	public void updateGroup() throws Exception {
		System.out.println(CLASS_NAME + "updateGroup");

		vo = setUpVo();
		setUpGroup(vo);

		group.setName("GroupsManagerTestGroup1Updated");
		Group returnedGroup = groupsManager.updateGroup(sess, group);
		assertNotNull(returnedGroup);
		assertEquals("Groups should be the same after update", returnedGroup, group);

	}

	@Test (expected=GroupNotExistsException.class)
	public void updateGroupWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "updateGroupWhenGroupNotExists");

		groupsManager.updateGroup(sess, new Group());
		// shouldn't be able to update

	}

	@Test
	public void getGroupById() throws Exception {
		System.out.println(CLASS_NAME + "getGroupById");

		vo = setUpVo();
		setUpGroup(vo);

		Group returnedGroup = groupsManager.getGroupById(sess, group.getId());
		assertNotNull(returnedGroup);
		assertEquals("both groups should be the same", returnedGroup, group);

	}

	@Test
	public void getAssignedGroupsToResourceWithSubgroups() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedGroupsToResourceWithSubgroups");
		Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0, "testik123", "testik123"));
		Group parentGroup = new Group("TestGroupParent", "ParentGroup");
		Group subgroup1 = new Group("TestGroup1", "Test1");
		Group subgroup2 = new Group("TestGroup2", "Test2");
		parentGroup = groupsManagerBl.createGroup(sess, createdVo, parentGroup);
		subgroup1 = groupsManagerBl.createGroup(sess, parentGroup, subgroup1);
		subgroup2 = groupsManagerBl.createGroup(sess, parentGroup, subgroup2);
		Group subgroupOfSubgroup1 = new Group("TestGroupSubgroupOfSubgroup", "SubSubGroup");
		subgroupOfSubgroup1 = groupsManagerBl.createGroup(sess, subgroup2, subgroupOfSubgroup1);
		//Fake Group
		Group group3 = groupsManagerBl.createGroup(sess, createdVo, new Group("TaTamNemaByt", "Test3"));
		Facility facility = new Facility();
		facility.setName("TestForGetSubgroups");
		perun.getFacilitiesManager().createFacility(sess, facility);
		Resource resource = new Resource();
		resource.setName("TestForGetSubgroups");
		resource.setDescription("Testovaci");
		resource = sess.getPerun().getResourcesManager().createResource(sess, resource, createdVo, facility);
		sess.getPerun().getResourcesManager().assignGroupToResource(sess, parentGroup, resource);
		sess.getPerun().getResourcesManager().assignGroupToResource(sess, subgroup1, resource);

		List<Group> groupsList = groupsManagerBl.getAssignedGroupsToResource(sess, resource, true);
		assertNotNull(groupsList);
		assertTrue("Expected this group is contained in list.",groupsList.remove(subgroup1));
		assertTrue("Expected this group is contained in list.",groupsList.remove(subgroup2));
		assertTrue("Expected this group is contained in list.",groupsList.remove(parentGroup));
		assertTrue("Expected this group is contained in list.",groupsList.remove(subgroupOfSubgroup1));
		assertTrue(groupsList.isEmpty());
	}

	@Test
	public void getMemberGroupsByAttribute() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroupsByAttribute");
		Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0, "testik123456", "testik123456"));
		Member member = setUpMember(createdVo);

		Group group1 = new Group("Group1Test", "Group1Test");
		Group group2 = new Group("Group2Test", "Group2Test");
		Group group3 = new Group("Group3Test", "Group3Test");
		Group group4 = new Group("Group4Test", "Group4Test");

		group1 = groupsManagerBl.createGroup(sess, createdVo, group1);
		group2 = groupsManagerBl.createGroup(sess, createdVo, group2);
		group3 = groupsManagerBl.createGroup(sess, createdVo, group3);
		group4 = groupsManagerBl.createGroup(sess, createdVo, group4);
		groupsManagerBl.addMember(sess, group1, member);
		groupsManagerBl.addMember(sess, group2, member);
		groupsManagerBl.addMember(sess, group3, member);
		groupsManagerBl.addMember(sess, group4, member);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManagerEntry.NS_GROUP_ATTR_DEF);
		attrDef.setDescription("Test attribute description");
		attrDef.setFriendlyName("testingAttribute");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		Attribute attribute = new Attribute(attrDef);
		attribute.setValue("Testing value");

		perun.getAttributesManagerBl().setAttribute(sess, group1, attribute);
		perun.getAttributesManagerBl().setAttribute(sess, group3, attribute);

		AttributeDefinition attrDefBad = new AttributeDefinition();
		attrDefBad.setNamespace(AttributesManagerEntry.NS_GROUP_ATTR_DEF);
		attrDefBad.setDescription("Test attribute description 2");
		attrDefBad.setFriendlyName("testingAttribute2");
		attrDefBad.setType(String.class.getName());
		attrDefBad = perun.getAttributesManagerBl().createAttribute(sess, attrDefBad);
		Attribute attributeBad = new Attribute(attrDefBad);
		attributeBad.setValue("Testing value");

		perun.getAttributesManagerBl().setAttribute(sess, group2, attributeBad);
		perun.getAttributesManagerBl().setAttribute(sess, group4, attributeBad);

		List<Group> groups1 = perun.getGroupsManager().getMemberGroupsByAttribute(sess, member, attribute);
		List<Group> groups2 = perun.getGroupsManager().getMemberGroupsByAttribute(sess, member, attributeBad);

		assertEquals("groups must have only 2 mambers", 2, groups1.size());
		assertEquals("groups must have only 2 mambers", 2, groups2.size());
		assertTrue("list of groups must containt this group", groups1.contains(group1));
		assertTrue("list of groups must containt this group", groups1.contains(group3));
		assertTrue("list of groups must containt this group", groups2.contains(group2));
		assertTrue("list of groups must containt this group", groups2.contains(group4));
	}

	@Test
	public void getAssignedGroupsToResourceWithoutSubgroups() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedGroupsToResourceWithoutSubgroups");
		Vo createdVo = perun.getVosManager().createVo(sess, new Vo(0, "testik123", "testik123"));
		Group parentGroup = new Group("TestGroupParent", "ParentGroup");
		Group subgroup1 = new Group("TestGroup1", "Test1");
		Group subgroup2 = new Group("TestGroup2", "Test2");
		parentGroup = groupsManagerBl.createGroup(sess, createdVo, parentGroup);
		subgroup1 = groupsManagerBl.createGroup(sess, parentGroup, subgroup1);
		subgroup2 = groupsManagerBl.createGroup(sess, parentGroup, subgroup2);
		//Fake Group
		Group group3 = groupsManagerBl.createGroup(sess, createdVo, new Group("TaTamNemaByt", "Test3"));
		Facility facility = new Facility();
		facility.setName("TestForGetSubgroups");
		perun.getFacilitiesManager().createFacility(sess, facility);
		Resource resource = new Resource();
		resource.setName("TestForGetSubgroups");
		resource.setDescription("Testovaci");
		resource = sess.getPerun().getResourcesManager().createResource(sess, resource, createdVo, facility);
		sess.getPerun().getResourcesManager().assignGroupToResource(sess, parentGroup, resource);
		sess.getPerun().getResourcesManager().assignGroupToResource(sess, subgroup1, resource);

		List<Group> groupsList = groupsManagerBl.getAssignedGroupsToResource(sess, resource, false);
		assertNotNull(groupsList);
		assertTrue("Expected this group is contained in list.", groupsList.remove(subgroup1));
		assertTrue("Expected this group is contained in list.",groupsList.remove(parentGroup));
		assertTrue(groupsList.isEmpty());
	}

	@Test
	public void getGroupToSynchronize() throws Exception {
		System.out.println(CLASS_NAME + "getGroupToSynchronize");

		vo = setUpVo();
		setUpGroup(vo);
		perun.getGroupsManager().createGroup(sess, vo, group2);

		Attribute synchroAttr1 = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, "urn:perun:group:attribute-def:def:synchronizationEnabled"));
		synchroAttr1.setValue("true");
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr1);
		perun.getAttributesManager().setAttribute(sess, group2, synchroAttr1);

		Attribute synchroAttr2 = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, "urn:perun:group:attribute-def:def:synchronizationInterval"));
		synchroAttr2.setValue("5");
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr2);
		perun.getAttributesManager().setAttribute(sess, group2, synchroAttr2);

		Attribute synchroAttr3 = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, "urn:perun:group:attribute-def:def:groupExtSource"));
		synchroAttr3.setValue(extSource.getName());
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr3);
		perun.getAttributesManager().setAttribute(sess, group2, synchroAttr3);

		List<Group> groups = groupsManagerBl.getGroupsToSynchronize(sess);
		assertTrue("List of groups to synchronize contain group.", groups.contains(group));
		assertTrue("List of groups to synchronize contain group2.", groups.contains(group2));
	}

	@Test (expected=GroupNotExistsException.class)
	public void getGroupByIdWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupByIdWhenGroupNotExists");

		vo = setUpVo();
		setUpGroup(vo);

		group.setId(0);
		groupsManager.getGroupById(sess, group.getId());

	}

	@Test
	public void getGroupByName() throws Exception {
		System.out.println(CLASS_NAME + "getGroupByName");

		vo = setUpVo();
		setUpGroup(vo);

		Group returnedGroup = groupsManager.getGroupByName(sess, vo, group.getName());
		assertNotNull(returnedGroup);
		assertEquals("Both groups should be the same", returnedGroup, group);

	}

	@Test
	public void getSubGroupByName() throws Exception {
		System.out.println(CLASS_NAME + "getSubGroupByName");

		vo = setUpVo();
		setUpGroup(vo);
		Group subGroup = new Group(group.getId(), group.getName(), group.getDescription());
		subGroup = groupsManager.createGroup(sess, group, subGroup);
		assertEquals("SubGroup must have name like 'name:name'", group.getName() + ":" + group.getName(), subGroup.getName());

		Group returnedGroup = groupsManager.getGroupByName(sess, vo, group.getName());
		Group returnedSubGroup = groupsManager.getGroupByName(sess, vo, subGroup.getName());
		assertNotNull(returnedGroup);
		assertNotNull(returnedSubGroup);

		assertEquals("Both groups should be the same", returnedGroup, group);
		assertEquals("Both groups should be the same", returnedSubGroup, subGroup);
	}

	@Test (expected=VoNotExistsException.class)
	public void getGroupByNameWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupByNameWhenVoNotExists");

		vo = setUpVo();
		setUpGroup(vo);

		groupsManager.getGroupByName(sess, new Vo(), group.getName());

	}

	@Test (expected=GroupNotExistsException.class)
	public void getGroupByNameWhenGroupNotExists() throws Exception {

		vo = setUpVo();

		groupsManager.getGroupByName(sess, vo, "GroupsManagerEntryIntegrationTest:test:test:test");

	}

	@Test
	public void addMember() throws Exception {
		System.out.println(CLASS_NAME + "addMember");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);

		groupsManager.addMember(sess, group, member);

		List<Member> members = groupsManager.getGroupActiveMembers(sess, group);
		assertTrue("our member should be in group", members.contains(member));

	}

	@Test
	public void addAndRemoveMemberInGroupWithUnion() throws Exception {
		System.out.println("GroupsManager.addAndRemoveMemberInGroupWithUnion");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);
		groupsManager.createGroup(sess, vo, group3);

		groupsManager.groupUnion(sess, group, group2);
		groupsManager.groupUnion(sess, group2, group3);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group3, member);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 1);
		assertEquals(groupsManager.getGroupActiveMembers(sess, group3).get(0).getId(), member.getId());

		groupsManager.removeMember(sess, group3, member);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 0);

		groupsManager.addMember(sess, group3, member);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 1);
		assertEquals(groupsManager.getGroupActiveMembers(sess, group3).get(0).getId(), member.getId());
	}

	@Test
	public void addAndRemoveMemberInGroupWithDifference() throws Exception {
		System.out.println("GroupsManager.addAndRemoveMemberInGroupWithDifference");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);
		groupsManager.createGroup(sess, vo, group3);

		groupsManager.groupDifference(sess, group, group2);
		groupsManager.groupDifference(sess, group2, group3);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);
		groupsManager.addMember(sess, group3, member);
		groupsManager.addMember(sess, group2, member);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 1);
		assertEquals(groupsManager.getGroupActiveMembers(sess, group3).get(0).getId(), member.getId());

		groupsManager.removeMember(sess, group3, member);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 0);

		groupsManager.addMember(sess, group3, member);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 1);
		assertEquals(groupsManager.getGroupActiveMembers(sess, group3).get(0).getId(), member.getId());
	}

	// FIXME - vymyslet lepší výjímku

	@Test (expected=InternalErrorException.class)
	public void addMemberWhenMemberFromDifferentVo() throws Exception {
		System.out.println(CLASS_NAME + "addMemberWhenMemberFromDifferentVo");

		vo = setUpVo();
		setUpGroup(vo);

		Vo vo = new Vo();
		vo.setName("GroupManagerTestVo2");
		vo.setShortName("GrpManTest2");
		vo = perun.getVosManager().createVo(sess, vo);

		Member member = setUpMember(vo); // put member in different VO

		groupsManager.addMember(sess, group, member);
		// shouldn't add member

	}

	@Test (expected=GroupNotExistsException.class)
	public void addMemberWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addMemberWhenGroupNotExists");

		vo = setUpVo();
		setUpGroup(vo);
		Member member = setUpMember(vo);

		groupsManager.addMember(sess, new Group(), member);
		// shouldn't find group

	}

	@Test (expected=MemberNotExistsException.class)
	public void addMemberWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addMemberWhenGroupNotExists");

		vo = setUpVo();
		setUpGroup(vo);

		groupsManager.addMember(sess, group, new Member());
		// shouldn't find member

	}

	@Test (expected=AlreadyMemberException.class)
	public void addMemberWhenAlreadyMember() throws Exception {
		System.out.println(CLASS_NAME + "addMemberWhenAlreadyMember");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);

		groupsManager.addMember(sess, group, member);
		groupsManager.addMember(sess, group, member);
		// shouldn't be able to add same member twice

	}

	@Test
	public void removeMember() throws Exception {
		System.out.println(CLASS_NAME + "removeMember");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);
		groupsManager.removeMember(sess, group, member);

		List<Member> members = groupsManager.getGroupActiveMembers(sess, group);
		assertTrue(members.isEmpty());

	}

	@Test (expected=GroupNotExistsException.class)
	public void removeMemberWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberWhenGroupNotExists");

		vo = setUpVo();
		Member member = setUpMember(vo);
		groupsManager.removeMember(sess, new Group(), member);

	}

	@Test (expected=MemberNotExistsException.class)
	public void removeMemberWhenMemberNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberWhenMemberNotExists");

		vo = setUpVo();
		setUpGroup(vo);

		groupsManager.removeMember(sess, group, new Member());

	}

	@Test (expected=NotGroupMemberException.class)
	public void removeMemberWhenNotGroupMember() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberWhenNotGroupMember");

		vo = setUpVo();
		setUpGroup(vo);
		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);
		groupsManager.removeMember(sess, group, member);
		groupsManager.removeMember(sess, group, member);
		// shouldn't be able to remove member twice

	}

	@Test
	public void getGroupActiveMembers() throws Exception {
		System.out.println(CLASS_NAME + "getGroupActiveMembers");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);

		List<Member> members = groupsManager.getGroupActiveMembers(sess, group);
		assertTrue(members.size() == 1);
		assertTrue(members.contains(member));

	}

	@Test
	public void getGroupActiveMembersStatus() throws Exception {
		System.out.println(CLASS_NAME + "getGroupActiveMembersStatus");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);

		List<Member> members = groupsManager.getGroupActiveMembers(sess, group, Status.VALID);
		assertTrue(members.size() == 1);
		assertTrue(members.contains(member));
	}


	@Test
	public void getGroupActiveMembersInBiggerGroupStructure() throws Exception {
		System.out.println("GroupsManager.getGroupActiveMembersInBiggerGroupStructure");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, group, group2);
		groupsManager.createGroup(sess, group2, group3);
		groupsManager.createGroup(sess, group3, group4);
		groupsManager.createGroup(sess, group, group21);
		groupsManager.createGroup(sess, group21, group5);
		groupsManager.createGroup(sess, group5, group6);

		// first create relations between empty groups
		groupsManager.groupUnion(sess, group2, group);
		groupsManager.groupDifference(sess, group3, group2);
		groupsManager.groupDifference(sess, group4, group3);
		groupsManager.groupDifference(sess, group21, group);
		groupsManager.groupUnion(sess, group5, group21);
		groupsManager.groupUnion(sess, group6, group5);

		// then start adding members
		Member member = setUpMember(vo);
		Member member1 = setUpMember(vo);
		groupsManager.addMember(sess, group, member);
		groupsManager.addMember(sess, group, member1);
		groupsManager.addMember(sess, group3, member);
		groupsManager.addMember(sess, group4, member);
		groupsManager.addMember(sess, group5, member);
		groupsManager.addMember(sess, group6, member);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 2);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 2);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group4).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group21).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group5).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group6).size() == 1);
	}

	@Test
	public void getGroupActiveMembersInBiggerGroupStructureReverted() throws Exception {
		System.out.println("GroupsManager.getGroupActiveMembersInBiggerGroupStructureReverted");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, group, group2);
		groupsManager.createGroup(sess, group2, group3);
		groupsManager.createGroup(sess, group3, group4);
		groupsManager.createGroup(sess, group, group21);
		groupsManager.createGroup(sess, group21, group5);
		groupsManager.createGroup(sess, group5, group6);

		// first add members to groups
		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);
		groupsManager.addMember(sess, group3, member);
		groupsManager.addMember(sess, group4, member);
		groupsManager.addMember(sess, group5, member);
		groupsManager.addMember(sess, group6, member);

		// then create relations between them
		groupsManager.groupUnion(sess, group2, group);
		groupsManager.groupDifference(sess, group3, group2);
		groupsManager.groupDifference(sess, group4, group3);
		groupsManager.groupDifference(sess, group21, group);
		groupsManager.groupUnion(sess, group5, group21);
		groupsManager.groupUnion(sess, group6, group5);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group4).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group21).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group5).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group6).size() == 1);
	}
	
	@Test
	public void getAllGroupMembers() throws Exception {
		System.out.println("GroupsManager.getAllGroupMembers");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);
		groupsManager.createGroup(sess, vo, group3);

		Member direct = setUpMember(vo);
		Member directAsExcluded = setUpMember(vo);
		Member randomExcluded = setUpMember(vo);
		Member indirect = setUpMember(vo);
		Member indirectAsExcluded = setUpMember(vo);
		
		groupsManager.addMember(sess, group, direct);
		groupsManager.addMember(sess, group, directAsExcluded);
		groupsManager.addMember(sess, group2, directAsExcluded);
		groupsManager.addMember(sess, group2, randomExcluded);
		groupsManager.addMember(sess, group3, indirect);
		groupsManager.addMember(sess, group3, indirectAsExcluded);
		groupsManager.addMember(sess, group2, indirectAsExcluded);
		
		groupsManager.groupDifference(sess, group, group2);
		groupsManager.groupUnion(sess, group, group3);
		
		List<Member> result = groupsManagerBl.getAllGroupMembers(sess, group);
		
		assertTrue(result.size() == 4);
		assertTrue(result.contains(direct));
		assertTrue(result.contains(directAsExcluded) 
				&& result.get(result.indexOf(directAsExcluded)).getMembershipType() == MembershipType.DIRECT_EXCLUDED);
		assertFalse(result.contains(randomExcluded));
		assertTrue(result.contains(indirect)
				&& result.get(result.indexOf(indirect)).getMembershipType() == MembershipType.INDIRECT);
		assertTrue(result.contains(indirectAsExcluded)
				&& result.get(result.indexOf(indirectAsExcluded)).getMembershipType() == MembershipType.INDIRECT_EXCLUDED);
	}

	@Test
	public void createGroupRelation() throws Exception {
		System.out.println("GroupsManager.createGroupRelation");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);
		groupsManager.createGroup(sess, vo, group3);
		groupsManager.createGroup(sess, vo, group4);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group2, member);
		groupsManager.addMember(sess, group3, member);
		groupsManager.addMember(sess, group4, member);

		groupsManager.groupUnion(sess, group, group2);
		groupsManager.groupDifference(sess, group3, group4);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 0);
	}

	@Test
	public void removeGroupRelation() throws Exception {
		System.out.println("GroupsManager.removeGroupRelation");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);
		groupsManager.createGroup(sess, vo, group3);
		groupsManager.createGroup(sess, vo, group4);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group2, member);
		groupsManager.addMember(sess, group3, member);
		groupsManager.addMember(sess, group4, member);

		groupsManager.groupUnion(sess, group, group2);
		groupsManager.groupDifference(sess, group3, group4);

		groupsManager.removeUnionRelation(sess, group, group2);
		groupsManager.removeDifferenceRelation(sess, group3, group4);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 1);
	}

	@Test(expected=InternalErrorException.class)
	public void createGroupUnionWhenUnionAlreadyExists() throws Exception {
		System.out.println("GroupsManager.createGroupUnionWhenUnionAlreadyExists");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.groupUnion(sess, group, group2);
		groupsManager.groupUnion(sess, group, group2);
	}

	@Test(expected=InternalErrorException.class)
	public void createGroupDifferenceWhenUnionAlreadyExists() throws Exception {
		System.out.println("GroupsManager.createGroupDifferenceWhenUnionAlreadyExists");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.groupUnion(sess, group, group2);
		groupsManager.groupDifference(sess, group, group2);
	}

	@Test(expected=InternalErrorException.class)
	public void createGroupDifferenceWhenDifferenceAlreadyExists() throws Exception {
		System.out.println("GroupsManager.createGroupDifferenceWhenDifferenceAlreadyExists");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.groupDifference(sess, group, group2);
		groupsManager.groupDifference(sess, group, group2);
	}

	@Test(expected=InternalErrorException.class)
	public void createGroupUnionWhenDifferenceAlreadyExists() throws Exception {
		System.out.println("GroupsManager.createGroupUnionWhenDifferenceAlreadyExists");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.groupDifference(sess, group, group2);
		groupsManager.groupUnion(sess, group, group2);
	}

	@Test(expected=InternalErrorException.class)
	public void createGroupRelationOnSameGroup() throws Exception {
		System.out.println("GroupsManager.createGroupUnionOnSameGroup");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);

		groupsManager.groupUnion(sess, group, group);
	}

	@Test(expected=InternalErrorException.class)
	public void removeGroupUnionThatDoesNotExist() throws Exception {
		System.out.println("GroupsManager.removeGroupUnionThatDoesNotExist");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.removeUnionRelation(sess, group, group2);
	}

	@Test(expected = InternalErrorException.class)
	public void removeGroupDifferenceThatDoesNotExist() throws Exception {
		System.out.println("GroupsManager.removeGroupDifferenceThatDoesNotExist");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.removeDifferenceRelation(sess, group, group2);
	}

	@Test(expected = InternalErrorException.class)
	public void createGroupTransitivity() throws Exception {
		System.out.println("GroupsManager.createGroupTransitivity");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);
		groupsManager.createGroup(sess, vo, group3);
		groupsManager.createGroup(sess, vo, group4);

		groupsManager.groupUnion(sess, group, group2);
		groupsManager.groupDifference(sess, group2, group3);
		groupsManager.groupDifference(sess, group3, group);
	}

	@Test(expected = GroupNotExistsException.class)
	public void createRelationWhenGroupNotExists() throws Exception {
		System.out.println("GroupsManager.createRelationWhenGroupNotExists");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);

		groupsManager.groupUnion(sess, group, group2);
	}

	@Test(expected = InternalErrorException.class)
	public void deleteUnionWithSwitchedGroups() throws Exception {
		System.out.println("GroupsManager.deleteUnionWithSwitchedGroups");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.groupUnion(sess, group, group2);

		groupsManager.removeUnionRelation(sess, group2, group);
	}

	@Test(expected = InternalErrorException.class)
	public void deleteDifferenceWithSwitchedGroups() throws Exception {
		System.out.println("GroupsManager.deleteDifferenceWithSwitchedGroups");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.groupDifference(sess, group, group2);

		groupsManager.removeDifferenceRelation(sess, group2, group);
	}

	@Test(expected = InternalErrorException.class)
	public void deleteDifferenceWhenThereIsUnion() throws Exception {
		System.out.println("GroupsManager.deleteDifferenceWhenThereIsUnion");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.groupUnion(sess, group, group2);

		groupsManager.removeDifferenceRelation(sess, group, group2);
	}

	@Test
	public void getGroupActiveMembersInHugeStructure() throws Exception {
		System.out.println("GroupsManager.getGroupActiveMembersInHugeStructure");

		vo = setUpVo();

		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);
		groupsManager.createGroup(sess, vo, group3);
		groupsManager.createGroup(sess, vo, group4);
		groupsManager.createGroup(sess, vo, group5);
		groupsManager.createGroup(sess, vo, group6);
		groupsManager.createGroup(sess, vo, group7);
		groupsManager.createGroup(sess, vo, group8);
		groupsManager.createGroup(sess, vo, group9);
		groupsManager.createGroup(sess, vo, group10);
		groupsManager.createGroup(sess, vo, group11);
		groupsManager.createGroup(sess, vo, group12);
		groupsManager.createGroup(sess, vo, group13);
		groupsManager.createGroup(sess, vo, group14);

		Member member1 = setUpMember(vo);
		Member member2 = setUpMember(vo);
		Member member3 = setUpMember(vo);
		Member member4 = setUpMember(vo);
		Member member5 = setUpMember(vo);

		//1
		groupsManager.addMember(sess, group, member1);
		//2
		groupsManager.addMember(sess, group2, member1);
		//3
		groupsManager.addMember(sess, group2, member2);

		groupsManager.groupDifference(sess, group2, group);
		groupsManager.groupDifference(sess, group5, group2);
		groupsManager.groupDifference(sess, group4, group3);
		groupsManager.groupUnion(sess, group3, group);
		groupsManager.groupUnion(sess, group6, group2);
		groupsManager.groupUnion(sess, group7, group3);

		//1 Basic structure
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group7).size() == 1);
		//2 Basic structure
		assertFalse(groupsManager.getGroupActiveMembers(sess, group2).contains(member1));
		assertFalse(groupsManager.getGroupActiveMembers(sess, group5).contains(member1));
		assertFalse(groupsManager.getGroupActiveMembers(sess, group6).contains(member1));
		//3 Basic structure
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).contains(member2));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group6).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group6).contains(member2));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group5).size() == 0);

		//4 Adding difference relation
		groupsManager.addMember(sess, group8, member1);
		groupsManager.groupDifference(sess, group, group8);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group7).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 2);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).contains(member1));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group6).size() == 2);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group6).contains(member1));

		//5 Adding members into difference relation group chain
		groupsManager.groupDifference(sess, group12, group5);
		groupsManager.groupDifference(sess, group13, group12);

		groupsManager.addMember(sess, group2, member3);
		groupsManager.addMember(sess, group5, member3);
		groupsManager.addMember(sess, group12, member3);
		groupsManager.addMember(sess, group13, member3);

		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 3);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group5).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group12).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group12).contains(member3));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group13).size() == 0);
		assertFalse(groupsManager.getGroupActiveMembers(sess, group13).contains(member3));

		//6 Adding union relation
		groupsManager.groupUnion(sess, group, group9);

		groupsManager.addMember(sess, group9, member3);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 2);
		assertFalse(groupsManager.getGroupActiveMembers(sess, group2).contains(member3));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group5).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group5).contains(member3));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group12).size() == 0);
		assertFalse(groupsManager.getGroupActiveMembers(sess, group12).contains(member3));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group13).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group13).contains(member3));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group6).size() == 2);

		//7 Adding member to group in which the member is excluded
		groupsManager.addMember(sess, group4, member3);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group4).size() == 0);

		//8 Removing difference
		groupsManager.removeDifferenceRelation(sess, group, group8);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 2);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).contains(member1));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).contains(member3));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group3).contains(member1));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group7).contains(member1));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).contains(member2));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group5).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group12).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group13).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group6).size() == 1);

		//9 Removing union
		groupsManager.removeUnionRelation(sess, group7, group3);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group7).size() == 0);

		//10 Removing group with member
		try {
			groupsManager.deleteGroup(sess, group2);
		} catch (RelationExistsException e) {
			//this is ok, group with members cannot be deleted this way
		}

		//11 Removing group
		groupsManager.deleteGroup(sess, group2, true);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group).size() == 2);
		try {
			groupsManagerBl.checkGroupExists(sess, group2);
		} catch (GroupNotExistsException e) {
			//this is ok, group was deleted, it should not exist
		}
		assertTrue(groupsManager.getGroupActiveMembers(sess, group5).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group12).size() == 0);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group13).size() == 1);
	}

	@Test
	public void getDirectGroupMembers() throws Exception {
		System.out.println("GroupsManager.getDirectGroupMembers");

		vo = setUpVo();

		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		Member member1 = setUpMember(vo);
		Member member2 = setUpMember(vo);

		groupsManager.addMember(sess, group, member1);
		groupsManager.addMember(sess, group2, member2);

		groupsManager.groupUnion(sess, group2, group);

		assertTrue(groupsManager.getDirectGroupMembers(sess, group2).size() == 1);
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).contains(member2));
		assertTrue(groupsManager.getGroupActiveMembers(sess, group2).contains(member1));
	}

	@Test
	public void getMemberGroups() throws Exception {
		System.out.println(CLASS_NAME + "getMemberGroups");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);

		List<Group> groups = groupsManager.getAllMemberGroups(sess, member);
		assertEquals(1, groups.size()); //group "members"

		groupsManager.addMember(sess, group, member);

		groups = groupsManager.getAllMemberGroups(sess, member);
		assertEquals(2, groups.size());
		assertTrue(groups.contains(group));

	}

	@Test (expected=GroupNotExistsException.class)
	public void getGroupActiveMembersWhenGroupNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getGroupActiveMembersWhenGroupNotExist");

		groupsManager.getGroupActiveMembers(sess, new Group());

	}

	@Test (expected=GroupNotExistsException.class)
	public void getGroupActiveMembersPageWhenGroupNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getGroupActiveMembersPageWhenGroupNotExist");

		groupsManager.getGroupActiveMembers(sess, new Group());

	}

	@Test
	public void getGroupActiveMembersCount() throws Exception {
		System.out.println(CLASS_NAME + "getGroupActiveMembersCount");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);

		int count = groupsManager.getGroupMembersCount(sess, group);
		assertTrue(count == 1);

	}

	@Test (expected=GroupNotExistsException.class)
	public void getGroupActiveMembersCountWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupActiveMembersCountWhenGroupNotExists");

		groupsManager.getGroupMembersCount(sess, new Group());

	}

	@Test
	public void getAllGroups() throws Exception {
		System.out.println(CLASS_NAME + "getAllGroups");

		vo = setUpVo();

		List<Group> groups = groupsManager.getAllGroups(sess, vo);
		assertEquals(1, groups.size()); //Group "members"

		setUpGroup(vo);

		assertNotNull(groupsManager.createGroup(sess, vo, group2));

		groups = groupsManager.getAllGroups(sess, vo);
		assertEquals(3, groups.size());
		assertTrue(groups.contains(group));
		assertTrue(groups.contains(group2));

	}

	@Test (expected=VoNotExistsException.class)
	public void getAllGroupsWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAllGroupsWhenVoNotExists");

		groupsManager.getAllGroups(sess, new Vo());

	}

	@Test
	public void getParentGroup() throws Exception {
		System.out.println(CLASS_NAME + "getParentGroup");

		vo = setUpVo();
		setUpGroup(vo);

		assertNotNull(groupsManager.createGroup(sess, group, group21));

		Group parentGroup = groupsManager.getParentGroup(sess, group21);

		assertEquals("created and returned group should be the same",group, parentGroup);

	}

	@Test
	public void getParentGroupWhenParentGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getParentGroupWhenParentGroupNotExists");

		vo = setUpVo();
		setUpGroup(vo);

		Group parentGroup = groupsManager.getParentGroup(sess, group);
		// should find members group
		Group memebrsGroup = groupsManager.getGroupByName(sess, vo, VosManagerEntry.MEMBERS_GROUP);
		assertEquals("returned group should be members group of the vo",parentGroup,memebrsGroup);
	}

	@Test
	public void getSubGroups() throws Exception {
		System.out.println(CLASS_NAME + "getSubGroups");

		vo = setUpVo();
		setUpGroup(vo);

		Group createdGroup21 = groupsManager.createGroup(sess, group, group21);
		assertNotNull(createdGroup21);



		List<Group> groups = groupsManager.getSubGroups(sess, group);

		assertTrue(groups.size() == 1);
		assertTrue(groups.contains(createdGroup21));

	}

	@Test
	public void getAllSubGroups() throws Exception {
		System.out.println(CLASS_NAME + "getAllSubGroups");

		vo = setUpVo();
		setUpGroup(vo);

		Group createdGroup21 = groupsManager.createGroup(sess, group, group21);
		Group createdGroup2 = groupsManager.createGroup(sess, group, group2);
		Group createdGroup3 = groupsManager.createGroup(sess, group21, group3);
		Group createdGroup4 = groupsManager.createGroup(sess, group3, group4);

		List<Group> groups = groupsManager.getAllSubGroups(sess, group);

		assertTrue(groups.size() == 4);
		assertTrue(groups.contains(createdGroup21));
		assertTrue(groups.contains(createdGroup2));
		assertTrue(groups.contains(createdGroup3));
		assertTrue(groups.contains(createdGroup4));

	}

	@Test (expected=GroupNotExistsException.class)
	public void getSubGroupsWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getSubGroupsWhenGroupNotExists");

		groupsManager.getSubGroups(sess, new Group());

	}

	@Test (expected=GroupNotExistsException.class)
	public void getSubGroupsPageWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getSubGroupsPageWhenGroupNotExists");

		groupsManager.getSubGroups(sess, new Group());

	}

	@Test
	public void addAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addAdmin");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		groupsManager.addAdmin(sess, group, user);

		List<User> admins = groupsManager.getAdmins(sess, group);
		assertTrue("group should have 1 admin",admins.size() == 1);
		assertTrue("our user should be admin",admins.contains(user));

	}

	@Test (expected=GroupNotExistsException.class)
	public void addAdminWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addAdminWhenGroupNotExists");

		vo = setUpVo();

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		groupsManager.addAdmin(sess, new Group(), user);

	}

	@Test (expected=UserNotExistsException.class)
	public void addAdminWhenUserNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addAdminWhenGroupNotExists");

		vo = setUpVo();
		setUpGroup(vo);

		groupsManager.addAdmin(sess, group, new User());

	}

	@Test (expected=AlreadyAdminException.class)
	public void addAdminWhenAlreadyAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addAdminWhenAlreadyAdmin");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);

		groupsManager.addAdmin(sess, group, user);
		groupsManager.addAdmin(sess, group, user);
		// shouldn't add admin twice !!

	}

	@Test
	public void addAdminWithGroup() throws Exception {
		System.out.println(CLASS_NAME + "addAdminWithGroup");

		vo = setUpVo();
		setUpGroup(vo);

		Group authorizedGroup = new Group("authorizedGroup","testovaciGroup");
		Group returnedGroup = groupsManager.createGroup(sess, vo, authorizedGroup);


		groupsManager.addAdmin(sess, group, returnedGroup);

		List<Group> admins = groupsManager.getAdminGroups(sess, group);
		assertTrue("group should have 1 admin",admins.size() == 1);
		assertTrue("our user should be admin",admins.contains(authorizedGroup));

	}

	@Test
	public void removeAdmin() throws Exception {
		System.out.println(CLASS_NAME + "removeAdmins");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		groupsManager.addAdmin(sess, group, user);

		groupsManager.removeAdmin(sess, group, user);
		List<User> admins = groupsManager.getAdmins(sess, group);
		assertTrue("admin not deleted!",admins.isEmpty());

	}

	@Test (expected=GroupNotExistsException.class)
	public void removeAdminWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAdminsWhenGroupNotExists");

		vo = setUpVo();

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		groupsManager.removeAdmin(sess, new Group(), user);

	}

	@Test (expected=UserNotExistsException.class)
	public void removeAdminWhenUserNotExist() throws Exception {
		System.out.println(CLASS_NAME + "removeAdminsWhenMemberNotExists");

		vo = setUpVo();
		setUpGroup(vo);

		groupsManager.removeAdmin(sess, group, new User());

	}

	@Test (expected=UserNotAdminException.class)
	public void removeAdminWhenNotAdminException() throws Exception {
		System.out.println(CLASS_NAME + "removeAdminsWhenMemberNotAdmin");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		groupsManager.removeAdmin(sess, group, user);

	}

	@Test
	public void removeAdminWithGroup() throws Exception {
		System.out.println(CLASS_NAME + "removeAdminWithGroup");

		vo = setUpVo();
		setUpGroup(vo);

		Group authorizedGroup = new Group("authorizedGroup","testovaciGroup");
		Group returnedGroup = groupsManager.createGroup(sess, vo, authorizedGroup);


		groupsManager.addAdmin(sess, group, returnedGroup);

		groupsManager.removeAdmin(sess, group, returnedGroup);
		List<Group> admins = groupsManager.getAdminGroups(sess, group);
		assertTrue("admin not deleted!",admins.isEmpty());

	}

	@Test
	public void getAdmins() throws Exception {
		System.out.println(CLASS_NAME + "getAdmins");

		vo = setUpVo();
		setUpGroup(vo);

		// set up first user
		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		groupsManager.addAdmin(sess, group, user);

		// set up authorized group
		Group authorizedGroup = new Group("authorizedGroup","testovaciGroup");
		Group returnedGroup = groupsManager.createGroup(sess, vo, authorizedGroup);
		groupsManager.addAdmin(sess, group, returnedGroup);

		// set up second user
		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("Josef");
		candidate.setId(4);
		candidate.setMiddleName("");
		candidate.setLastName("Novak");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		UserExtSource userExtSource = new UserExtSource(extSource, Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());

		Member member2 = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
		User user2 = perun.getUsersManagerBl().getUserByMember(sess, member2);
		groupsManager.addMember(sess, returnedGroup, member2);

		// test
		List<User> admins = groupsManager.getAdmins(sess, group);
		assertTrue("group should have 2 admins",admins.size() == 2);
		assertTrue("our member as direct user should be admin",admins.contains(user));
		assertTrue("our member as member of admin group should be admin",admins.contains(user2));
	}

	@Test
	public void getDirectAdmins() throws Exception {
		System.out.println(CLASS_NAME + "getDirectAdmins");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		groupsManager.addAdmin(sess, group, user);

		List<User> admins = groupsManager.getDirectAdmins(sess, group);
		assertTrue("group should have 1 admin",admins.size() == 1);
		assertTrue("our member should be admin",admins.contains(user));

	}

	@Test
	public void getAdminGroups() throws Exception {
		System.out.println(CLASS_NAME + "getAdminGroups");

		vo = setUpVo();
		setUpGroup(vo);

		// setting second group
		Group authorizedGroup = groupsManager.createGroup(sess, vo, new Group("New group", "just for testing"));

		groupsManager.addAdmin(sess, group, authorizedGroup);

		assertTrue(groupsManager.getAdminGroups(sess, group).contains(authorizedGroup));

	}

	@Test (expected=GroupNotExistsException.class)
	public void getAdminsWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAdminsWhenGroupNotExists");

		groupsManager.getAdmins(sess, new Group());

	}

	@Test
	public void getGroups() throws Exception {
		System.out.println(CLASS_NAME + "getGroups");

		vo = setUpVo();
		setUpGroup(vo);

		List<Group> groups = groupsManager.getGroups(sess, vo);
		assertEquals(2, groups.size());
		assertTrue("our group should be in our VO",groups.contains(group));

	}

	@Test (expected=VoNotExistsException.class)
	public void getGroupsWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsWhenVoNotExists");

		groupsManager.getGroups(sess, new Vo());

	}

	@Test (expected=VoNotExistsException.class)
	public void getGroupsPageWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsPageWhenVoNotExists");

		groupsManager.getGroups(sess, new Vo());

	}

	@Test
	public void getVoGroupsCount() throws Exception {
		System.out.println(CLASS_NAME + "getVoGroupsCount");

		vo = setUpVo();
		setUpGroup(vo);

		int count = groupsManager.getGroupsCount(sess, vo);
		assertEquals(2, count);

	}

	@Test
	public void getGroupsCount() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsCount");

		vo = setUpVo();
		setUpGroup(vo);

		int count = groupsManager.getGroupsCount(sess);
		assertTrue(count>0);

	}

	@Test
	public void getParentGroupMembersCount() throws Exception{
		System.out.println(CLASS_NAME + "getParentGroupMembersCount");
		vo = setUpVo();
		this.groupsManager.createGroup(sess, vo, group2);
		this.groupsManager.createGroup(sess, group2, group);
		groupsManager.groupUnion(sess, group2, group);

		Member member;
		for (int i=0;i<5;i++)
		{
			Candidate candidate = setUpCandidate(i);
			member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
			assertNotNull("No member created", member);
			usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
			groupsManager.addMember(sess, group, member);
			groupsManager.addMember(sess,group2,member);
		}
		int count = groupsManager.getGroupMembersCount(sess, group2);
		assertTrue(count == 5);
		Candidate candidate = setUpCandidate(5);
		member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		groupsManager.addMember(sess, group, member);
		count = groupsManager.getGroupMembersCount(sess, group2);
		assertTrue(count == 6);
		count = groupsManager.getGroupMembersCount(sess, group);
		assertTrue(count == 6);
	}

	@Test
	public void getGroupCountInBiggerGroupStructure() throws Exception{
		System.out.println(CLASS_NAME + "getGroupCountInBiggerGroupStructure");

		vo = setUpVo();
		this.groupsManager.createGroup(sess, vo, group);
		this.groupsManager.createGroup(sess, group, group2);
		this.groupsManager.createGroup(sess, group, group21);
		this.groupsManager.createGroup(sess, group2, group3);

		groupsManager.groupUnion(sess, group, group2);
		groupsManager.groupUnion(sess, group, group21);
		groupsManager.groupUnion(sess, group2, group3);

		List<Member> members = new ArrayList<Member>();
		for (int i = 0; i < 4; i++) {
			members.add(setUpMemberWithDifferentParam(vo, i));
		}

		this.groupsManager.addMember(sess, group, members.get(0));
		this.groupsManager.addMember(sess, group2, members.get(1));
		this.groupsManager.addMember(sess, group21, members.get(2));
		this.groupsManager.addMember(sess, group3, members.get(3));

		assertEquals(4, this.groupsManager.getGroupMembersCount(sess, group));
		assertEquals(2, this.groupsManager.getGroupMembersCount(sess, group2));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group21));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group3));
		this.groupsManager.removeMember(sess, group3, members.get(3));
		assertEquals(3, this.groupsManager.getGroupMembersCount(sess, group));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group2));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group21));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group3));
		this.groupsManager.removeMember(sess, group21, members.get(2));
		assertEquals(2, this.groupsManager.getGroupMembersCount(sess, group));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group2));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group21));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group3));
		this.groupsManager.removeMember(sess, group2, members.get(1));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group2));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group21));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group3));
		this.groupsManager.removeMember(sess, group, members.get(0));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group2));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group21));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group3));

		this.groupsManager.addMember(sess, group, members.get(0));
		this.groupsManager.addMember(sess, group2, members.get(1));
		this.groupsManager.addMember(sess, group21, members.get(2));
		this.groupsManager.addMember(sess, group3, members.get(3));
		this.groupsManager.addMember(sess, group, members.get(3));

		assertEquals(4, this.groupsManager.getGroupMembersCount(sess, group));
		assertEquals(2, this.groupsManager.getGroupMembersCount(sess, group2));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group21));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group3));
		this.groupsManager.removeMember(sess, group3, members.get(3));
		assertEquals(4, this.groupsManager.getGroupMembersCount(sess, group));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group2));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group21));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group3));
		this.groupsManager.removeMember(sess, group21, members.get(2));
		assertEquals(3, this.groupsManager.getGroupMembersCount(sess, group));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group2));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group21));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group3));
		this.groupsManager.removeMember(sess, group2, members.get(1));
		assertEquals(2, this.groupsManager.getGroupMembersCount(sess, group));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group2));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group21));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group3));
		this.groupsManager.removeMember(sess, group, members.get(0));
		assertEquals(1, this.groupsManager.getGroupMembersCount(sess, group));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group2));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group21));
		assertEquals(0, this.groupsManager.getGroupMembersCount(sess, group3));

	}

	@Test (expected=VoNotExistsException.class)
	public void getGroupsCountWhenVoNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsCountWhenVoNotExists");

		groupsManager.getGroupsCount(sess, new Vo());

	}

	@Test
	public void getSubGroupsCount() throws Exception {
		System.out.println(CLASS_NAME + "getSubGroupsCount");

		vo = setUpVo();
		setUpGroup(vo);

		assertNotNull(groupsManager.createGroup(sess, group, group21));

		int count = groupsManager.getSubGroupsCount(sess, group);
		assertTrue("our group should have one sub-group",count == 1);

	}

	@Test (expected=GroupNotExistsException.class)
	public void getSubGroupsCountWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getSubGroupsCountWhenGroupNotExists");

		groupsManager.getSubGroupsCount(sess, new Group());

	}

	@Test
	public void getVo() throws Exception {
		System.out.println(CLASS_NAME + "getVo");

		vo = setUpVo();
		setUpGroup(vo);

		Vo returnedVo = groupsManager.getVo(sess, group);
		assertNotNull("unable to get VO from DB",returnedVo);
		assertEquals("both VOs should be the same",returnedVo,vo);

	}

	@Test (expected=GroupNotExistsException.class)
	public void getVoWhenGroupNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getVoWhenGroupNotExists");

		groupsManager.getVo(sess, new Group());
	}

	@Test
	public void getMembersMembershipType() throws Exception{
		System.out.println("GroupsManager.getMembersMembershipType");
		vo = setUpVo();
		Member member = this.setUpMember(vo);

		this.groupsManager.createGroup(sess, vo, group);
		this.groupsManager.createGroup(sess, group, group2);
		this.groupsManager.createGroup(sess, group2, group3);
		this.groupsManager.createGroup(sess, group3, group4);

		this.groupsManager.groupUnion(sess, group, group2);
		this.groupsManager.groupDifference(sess, group3, group2);
		this.groupsManager.groupDifference(sess, group3, group4);

		this.groupsManager.addMember(sess, group2, member);
		this.groupsManager.addMember(sess, group3, member);
		this.groupsManager.addMember(sess, group4, member);

		assertTrue(this.groupsManager.getGroupActiveMembers(sess, group).size() == 1);
		assertEquals(this.groupsManager.getGroupActiveMembers(sess, group).get(0).getMembershipType(), MembershipType.INDIRECT);
		assertEquals(this.groupsManager.getGroupActiveMembers(sess, group2).get(0).getMembershipType(), MembershipType.DIRECT);
		// the member was excluded so it should not be returned by getGroupActiveMembers
		assertTrue(this.groupsManager.getGroupActiveMembers(sess, group3).size() == 0);
	}

	@Test
	public void convertGroupToRichGroupWithAttributesTest() throws Exception {
		System.out.println("GroupsManagerBl.convertGroupToRichGroupWithAttributes");

		vo = setUpVo();
		attributesList = setUpGroupAttributes();
		setUpGroup(vo);
		attributesManager.setAttributes(sess, group, attributesList);

		RichGroup richGroup = new RichGroup(group, attributesManager.getAttributes(sess, group));
		assertEquals("Both rich groups should be same", richGroup, groupsManagerBl.convertGroupToRichGroupWithAttributes(sess, group));
	}

	@Test
	public void convertGroupToRichGroupWithAttributesByNameTest() throws Exception {
		System.out.println("GroupsManagerBl.convertGroupToRichGroupWithAttributesByName");

		vo = setUpVo();
		attributesList = setUpGroupAttributes();
		this.groupsManager.createGroup(sess, vo, group);
		attributesManager.setAttributes(sess, group, attributesList);
		List<String> attrNames = new ArrayList<>();

		//take names from this list of attributes
		for (Attribute a : attributesList) {
			attrNames.add(a.getName());
		}

		RichGroup richGroup = new RichGroup(group, attributesList);
		assertEquals("Both rich groups should be same", richGroup, groupsManagerBl.convertGroupToRichGroupWithAttributesByName(sess, group, attrNames));
	}

	@Test
	public void convertGroupsToRichGroupsWithAttributesTest() throws Exception {
		System.out.println("groupsManagerBl.convertGroupsToRichGroupsWithAttributes");

		vo = setUpVo();
		attributesList = setUpGroupAttributes();

		this.groupsManager.createGroup(sess, vo, group);
		attributesManager.setAttributes(sess, group, attributesList);

		this.groupsManager.createGroup(sess, vo, group2);
		attributesManager.setAttributes(sess, group2, attributesList);

		RichGroup richGroup1 = new RichGroup(group, attributesManager.getAttributes(sess, group));
		RichGroup richGroup2 = new RichGroup(group2, attributesManager.getAttributes(sess, group2));

		assertEquals("Both lists should be same", Arrays.asList(richGroup1, richGroup2), groupsManagerBl.convertGroupsToRichGroupsWithAttributes(sess, Arrays.asList(group, group2)));
	}

	@Test
	public void convertGroupsToRichGroupsWithAttributesWithListOfNamesTest() throws Exception {
		System.out.println("GroupsManagerBl.convertGroupsToRichGroupsWithAttributesWithListOfNamesTest");

		vo = setUpVo();
		attributesList = setUpGroupAttributes();

		this.groupsManager.createGroup(sess, vo, group);
		attributesManager.setAttributes(sess, group, attributesList);

		this.groupsManager.createGroup(sess, vo, group2);
		attributesManager.setAttributes(sess, group2, attributesList);
		List<String> attrNames = new ArrayList<>();

		RichGroup richGroup1 = new RichGroup(group, attributesList);
		RichGroup richGroup2 = new RichGroup(group2, attributesList);

		//take names from this list of attributes
		for (Attribute a : attributesList) {
			attrNames.add(a.getName());
		}

		assertEquals("Both lists should be same", Arrays.asList(richGroup1, richGroup2), groupsManagerBl.convertGroupsToRichGroupsWithAttributes(sess, Arrays.asList(group, group2), attrNames));
	}

	// PRIVATE METHODS -------------------------------------------------------------

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "UserManagerTestVo", "UMTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		// create test VO in database
		assertNotNull("unable to create testing Vo",returnedVo);

		//ExtSource es = perun.getExtSourcesManager().getExtSourceByName(sess, "LDAPMETA");
		// get real external source from DB
		//perun.getExtSourcesManager().addExtSource(sess, returnedVo, es);
		// add real ext source to our VO

		return returnedVo;

	}

	private Member setUpMember(Vo vo) throws Exception {

		// List<Candidate> candidates = perun.getVosManager().findCandidates(sess, vo, extLogin);
		// find candidates from ext source based on extLogin
		// assertTrue(candidates.size() > 0);

		Candidate candidate = setUpCandidate(0);
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate); // candidates.get(0)
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		// save user for deletion after test
		return member;

	}

	private Candidate setUpCandidate(int i) {

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0+i);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}

	private void setUpGroup(Vo vo) throws Exception {

		Group returnedGroup = groupsManager.createGroup(sess, vo, group);
		assertNotNull("unable to create a group",returnedGroup);
		assertEquals("created group should be same as returned group",group,returnedGroup);

	}

	private List<Group> setUpGroupsWithSubgroups(Vo vo) throws Exception {
		Group groupA = new Group("A", "A");
		Group groupB = new Group("B", "B");
		Group groupC = new Group("C", "C");
		Group groupD = new Group("D", "D");
		Group groupE = new Group("E", "E");
		Group groupF = new Group("F", "F");
		Group groupG = new Group("G", "G");

		groupA = this.groupsManagerBl.createGroup(sess, vo, groupA);
		groupD = this.groupsManagerBl.createGroup(sess, vo, groupD);

		groupB = this.groupsManagerBl.createGroup(sess, groupA, groupB);
		groupG = this.groupsManagerBl.createGroup(sess, groupB, groupG);

		groupC = this.groupsManagerBl.createGroup(sess, groupD, groupC);
		groupE = this.groupsManagerBl.createGroup(sess, groupC, groupE);

		groupF = this.groupsManagerBl.createGroup(sess, groupE, groupF);

		List<Group> groups = new ArrayList<>();
		groups.add(groupC);
		groups.add(groupA);
		groups.add(groupF);
		groups.add(groupG);
		groups.add(groupB);
		groups.add(groupD);
		groups.add(groupE);

		return groups;
	}

	private Member setUpMemberWithDifferentParam(Vo vo, int i) throws Exception {

		// List<Candidate> candidates = perun.getVosManager().findCandidates(sess, vo, extLogin);
		// find candidates from ext source based on extLogin
		// assertTrue(candidates.size() > 0);

		Candidate candidate = setUpCandidate(i);
		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
		// set first candidate as member of test VO
		assertNotNull("No member created", member);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, member));
		// save user for deletion after test
		return member;

	}

	private List<Attribute> setUpGroupAttributes() throws Exception {

		List<Attribute> attributes = new ArrayList<Attribute>();

		// attribute1
		Attribute attr = new Attribute();
		String namespace = "group-test-uniqueattribute:specialNamespace";
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_OPT);
		attr.setFriendlyName(namespace + "1");
		attr.setType(String.class.getName());
		attr.setValue("GroupAttribute");
		assertNotNull("unable to create group attribute", attributesManager.createAttribute(sess, attr));

		attributes.add(attr);

		// attribute2
		Attribute attr2 = new Attribute(attr);
		attr2.setFriendlyName(namespace + "2");
		attr2.setValue("next2");
		assertNotNull("unable to create group attribute", attributesManager.createAttribute(sess, attr2));

		attributes.add(attr2);

		return attributes;
	}

}
