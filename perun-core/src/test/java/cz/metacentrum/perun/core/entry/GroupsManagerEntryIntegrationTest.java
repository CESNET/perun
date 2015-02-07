package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
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
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import org.junit.Before;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
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
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GroupsManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	// these must be setUp"type" before every method to be in DB
	final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
	final Group group = new Group("GroupsManagerTestGroup1","testovaci1");
	final Group group2 = new Group("GroupsManagerTestGroup2","testovaci2");
	final Group group21 = new Group("GroupsManagerTestGroup21","testovaci21");
	final Group group3 = new Group("GroupsManagerTestGroup3","testovaci3");
	final Group group4 = new Group("GroupsManagerTestGroup4","testovaci4");
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
	public void getGroupsUsers() throws Exception {
		System.out.println("GroupsManager.getGroupsUsers");

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
		System.out.println("GroupsManager.testGroupNameLength");

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
		System.out.println("GroupsManager.createGroup");

		vo = setUpVo();

		Group returnedGroup = groupsManager.createGroup(sess, vo, group2);
		assertNotNull(returnedGroup);
		assertNotNull(groupsManager.createGroup(sess, returnedGroup, group21));

	}

	@Test (expected=GroupNotExistsException.class)
		public void createGroupWhenParentNotExist() throws Exception {
			System.out.println("GroupsManager.createGroupWhenParentNotExists");

			groupsManager.createGroup(sess, new Group(), new Group("GroupsManagerTestGroup2","testovaci2"));

		}
	
	@Test (expected=GroupExistsException.class)
		public void createGroupWhenTheSameWithTheSameParentGroupExists() throws Exception {
			System.out.println("GroupsManager.createGroupWhenTheSameWithTheSameParentGroupExists");

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
			System.out.println("GroupsManager.createGroupWhenVoNotExists");

			groupsManager.createGroup(sess, new Vo(), new Group("GroupsManagerTestGroup2","testovaci2"));

		}

	@Test (expected=GroupExistsException.class)
		public void createGroupWhenSameGroupExist() throws Exception {
			System.out.println("GroupsManager.createGroupWhenSameGroupExists");

			vo = setUpVo();
			setUpGroup(vo);

			groupsManager.createGroup(sess, vo, group);
			// shouldn't be able to create group with same name
		}

	@Test (expected=GroupNotExistsException.class)
		public void deleteGroup() throws Exception {
			System.out.println("GroupsManager.deleteGroup");

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
		System.out.println("GroupsManager.deletesGroups");
		Vo newVo = new Vo(0, "voForDeletingGroups", "voForDeletingGroups");
		newVo = perun.getVosManagerBl().createVo(sess, newVo);
		List<Group> groups = setUpGroupsWithSubgroups(newVo);
		
		this.groupsManager.deleteGroups(sess, groups, false);
	}

	@Test (expected=RelationExistsException.class)
	public void deleteGroupsWithSubgroupAndNoForceDelete() throws Exception {
		System.out.println("GroupsManager.deleteGroupsWithSubgroupAndNoForceDelete");
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
		System.out.println("GroupsManager.deleteGroupsWithSubgroupAndForceDelete");
		Vo newVo = new Vo(0, "voForDeletingGroups", "voForDeletingGroups");
		newVo = perun.getVosManagerBl().createVo(sess, newVo);
		List<Group> groups = setUpGroupsWithSubgroups(newVo);
	
		Group subgroup = new Group("Test", "test");
		subgroup = this.groupsManagerBl.createGroup(sess, groups.get(0), subgroup);
		
		this.groupsManager.deleteGroups(sess, groups, true);
	}
	
	@Test (expected=GroupNotExistsException.class)
		public void deleteGroupWhenGroupNotExists() throws Exception {
			System.out.println("GroupsManager.deleteGroupWhenGroupNotExists");

			groupsManager.deleteGroup(sess, new Group());

		}

	@Test
	public void trywhat(){

	}

	@Test (expected=RelationExistsException.class)
		public void deleteGroupWhenContainsMember() throws Exception {
			System.out.println("GroupsManager.deleteGroupWhenContainsMember");

			vo = setUpVo();
			setUpGroup(vo);

			Member member = setUpMember(vo);
			groupsManager.addMember(sess, group, member);

			groupsManager.deleteGroup(sess, group);

		}

	@Test (expected=GroupNotExistsException.class)
		public void deleteGroupForce() throws Exception {
			System.out.println("GroupsManager.deleteGroupForce");

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
			System.out.println("GroupsManager.deleteGroupForceWhenGroupNotExists");

			groupsManager.deleteGroup(sess, new Group(), true);

		}

	@Test
	public void deleteAllGroups() throws Exception {
		System.out.println("GroupsManager.deleteAllGroups");

		vo = setUpVo();
		setUpGroup(vo);

		groupsManager.deleteAllGroups(sess, vo);

		List<Group> groups = groupsManager.getAllGroups(sess, vo);
		assertEquals("groups should contains only 'members' group", 1, groups.size());

	}

	@Test(expected=VoNotExistsException.class)
	public void deleteAllGroupsWhenVoNotExists() throws Exception {
		System.out.println("GroupsManager.deleteAllGroupsWhenVoNotExists");

		groupsManager.deleteAllGroups(sess, new Vo());

	}

	@Test
	public void updateGroup() throws Exception {
		System.out.println("GroupsManager.updateGroup");

		vo = setUpVo();
		setUpGroup(vo);

		group.setName("GroupsManagerTestGroup1Updated");
		Group returnedGroup = groupsManager.updateGroup(sess, group);
		assertNotNull(returnedGroup);
		assertEquals("Groups should be the same after update", returnedGroup, group);

	}

	@Test (expected=GroupNotExistsException.class)
		public void updateGroupWhenGroupNotExists() throws Exception {
			System.out.println("GroupsManager.updateGroupWhenGroupNotExists");

			groupsManager.updateGroup(sess, new Group());
			// shouldn't be able to update

		}

	@Test
	public void getGroupById() throws Exception {
		System.out.println("GroupsManager.getGroupById");

		vo = setUpVo();
		setUpGroup(vo);

		Group returnedGroup = groupsManager.getGroupById(sess, group.getId());
		assertNotNull(returnedGroup);
		assertEquals("both groups should be the same",returnedGroup,group);

	}

	@Test
	public void getAssignedGroupsToResourceWithSubgroups() throws Exception {
		System.out.println("GroupsManager.getAssignedGroupsToResourceWithSubgroups");
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
		System.out.println("GroupsManager.getMemberGroupsByAttribute");
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
		System.out.println("GroupsManager.getAssignedGroupsToResourceWithoutSubgroups");
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
		assertTrue("Expected this group is contained in list.",groupsList.remove(subgroup1));
		assertTrue("Expected this group is contained in list.",groupsList.remove(parentGroup));
		assertTrue(groupsList.isEmpty());
	}

	@Test
	public void getGroupToSynchronize() throws Exception {
		System.out.println("GroupsManager.getGroupToSynchronize");

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
			System.out.println("GroupsManager.getGroupByIdWhenGroupNotExists");

			vo = setUpVo();
			setUpGroup(vo);

			group.setId(0);
			groupsManager.getGroupById(sess, group.getId());

		}

	@Test
	public void getGroupByName() throws Exception {
		System.out.println("GroupsManager.getGroupByName");

		vo = setUpVo();
		setUpGroup(vo);

		Group returnedGroup = groupsManager.getGroupByName(sess, vo, group.getName());
		assertNotNull(returnedGroup);
		assertEquals("Both groups should be the same",returnedGroup,group);

	}

	@Test
	public void getSubGroupByName() throws Exception {
		System.out.println("GroupsManager.getSubGroupByName");

		vo = setUpVo();
		setUpGroup(vo);
		Group subGroup = new Group(group.getId(), group.getName(), group.getDescription());
		subGroup = groupsManager.createGroup(sess, group, subGroup);
		assertEquals("SubGroup must have name like 'name:name'", group.getName() + ":" + group.getName(), subGroup.getName());

		Group returnedGroup = groupsManager.getGroupByName(sess, vo, group.getName());
		Group returnedSubGroup = groupsManager.getGroupByName(sess, vo, subGroup.getName());
		assertNotNull(returnedGroup);
		assertNotNull(returnedSubGroup);

		assertEquals("Both groups should be the same",returnedGroup,group);
		assertEquals("Both groups should be the same",returnedSubGroup,subGroup);
	}

	@Test (expected=VoNotExistsException.class)
	public void getGroupByNameWhenVoNotExists() throws Exception {
		System.out.println("GroupsManager.getGroupByNameWhenVoNotExists");

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
		System.out.println("GroupsManager.addMember");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);

		groupsManager.addMember(sess, group, member);

		List<Member> members = groupsManager.getGroupMembers(sess, group);
		assertTrue("our member should be in group",members.contains(member));

	}

	// FIXME - vymyslet lepší výjímku

	@Test (expected=InternalErrorException.class)
		public void addMemberWhenMemberFromDifferentVo() throws Exception {
			System.out.println("GroupsManager.addMemberWhenMemberFromDifferentVo");

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
			System.out.println("GroupsManager.addMemberWhenGroupNotExists");

			vo = setUpVo();
			setUpGroup(vo);
			Member member = setUpMember(vo);

			groupsManager.addMember(sess, new Group(), member);
			// shouldn't find group

		}

	@Test (expected=MemberNotExistsException.class)
		public void addMemberWhenMemberNotExists() throws Exception {
			System.out.println("GroupsManager.addMemberWhenGroupNotExists");

			vo = setUpVo();
			setUpGroup(vo);

			groupsManager.addMember(sess, group, new Member());
			// shouldn't find member

		}

	@Test (expected=AlreadyMemberException.class)
		public void addMemberWhenAlreadyMember() throws Exception {
			System.out.println("GroupsManager.addMemberWhenAlreadyMember");

			vo = setUpVo();
			setUpGroup(vo);

			Member member = setUpMember(vo);

			groupsManager.addMember(sess, group, member);
			groupsManager.addMember(sess, group, member);
			// shouldn't be able to add same member twice

		}

	@Test
	public void removeMember() throws Exception {
		System.out.println("GroupsManager.removeMember");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);
		groupsManager.removeMember(sess, group, member);

		List<Member> members = groupsManager.getGroupMembers(sess, group);
		assertTrue(members.isEmpty());

	}

	@Test (expected=GroupNotExistsException.class)
		public void removeMemberWhenGroupNotExists() throws Exception {
			System.out.println("GroupsManager.removeMemberWhenGroupNotExists");

			vo = setUpVo();
			Member member = setUpMember(vo);
			groupsManager.removeMember(sess, new Group(), member);

		}

	@Test (expected=MemberNotExistsException.class)
		public void removeMemberWhenMemberNotExists() throws Exception {
			System.out.println("GroupsManager.removeMemberWhenMemberNotExists");

			vo = setUpVo();
			setUpGroup(vo);

			groupsManager.removeMember(sess, group, new Member());

		}

	@Test (expected=NotGroupMemberException.class)
		public void removeMemberWhenNotGroupMember() throws Exception {
			System.out.println("GroupsManager.removeMemberWhenNotGroupMember");

			vo = setUpVo();
			setUpGroup(vo);
			Member member = setUpMember(vo);
			groupsManager.addMember(sess, group, member);
			groupsManager.removeMember(sess, group, member);
			groupsManager.removeMember(sess, group, member);
			// shouldn't be able to remove member twice

		}

	@Test
	public void getGroupMembers() throws Exception {
		System.out.println("GroupsManager.getGroupMembers");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);

		List<Member> members = groupsManager.getGroupMembers(sess, group);
		assertTrue(members.size() == 1);
		assertTrue(members.contains(member));

	}

	@Test
	public void getMemberGroups() throws Exception {
		System.out.println("GroupsManager.getMemberGroups");

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
		public void getGroupMembersWhenGroupNotExist() throws Exception {
			System.out.println("GroupsManager.getGroupMembersWhenGroupNotExist");

			groupsManager.getGroupMembers(sess, new Group());

		}

	@Test (expected=GroupNotExistsException.class)
		public void getGroupMembersPageWhenGroupNotExist() throws Exception {
			System.out.println("GroupsManager.getGroupMembersPageWhenGroupNotExist");

			groupsManager.getGroupMembers(sess, new Group());

		}

	@Test
	public void getGroupMembersCount() throws Exception {
		System.out.println("GroupsManager.getGroupMembersCount");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);

		int count = groupsManager.getGroupMembersCount(sess, group);
		assertTrue(count == 1);

	}

	@Test (expected=GroupNotExistsException.class)
		public void getGroupMembersCountWhenGroupNotExists() throws Exception {
			System.out.println("GroupsManager.getGroupMembersCountWhenGroupNotExists");

			groupsManager.getGroupMembersCount(sess, new Group());

		}

	@Test
	public void getAllGroups() throws Exception {
		System.out.println("GroupsManager.getAllGroups");

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
			System.out.println("GroupsManager.getAllGroupsWhenVoNotExists");

			groupsManager.getAllGroups(sess, new Vo());

		}

	@Test
	public void getParentGroup() throws Exception {
		System.out.println("GroupsManager.getParentGroup");

		vo = setUpVo();
		setUpGroup(vo);

		assertNotNull(groupsManager.createGroup(sess, group, group21));

		Group parentGroup = groupsManager.getParentGroup(sess, group21);

		assertEquals("created and returned group should be the same",group, parentGroup);

	}

	@Test
	public void getParentGroupWhenParentGroupNotExists() throws Exception {
		System.out.println("GroupsManager.getParentGroupWhenParentGroupNotExists");

		vo = setUpVo();
		setUpGroup(vo);

		Group parentGroup = groupsManager.getParentGroup(sess, group);
		// should find members group
		Group memebrsGroup = groupsManager.getGroupByName(sess, vo, VosManagerEntry.MEMBERS_GROUP);
		assertEquals("returned group should be members group of the vo",parentGroup,memebrsGroup);
	}

	@Test
	public void getSubGroups() throws Exception {
		System.out.println("GroupsManager.getSubGroups");

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
		System.out.println("GroupsManager.getAllSubGroups");

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
			System.out.println("GroupsManager.getSubGroupsWhenGroupNotExists");

			groupsManager.getSubGroups(sess, new Group());

		}

	@Test (expected=GroupNotExistsException.class)
		public void getSubGroupsPageWhenGroupNotExists() throws Exception {
			System.out.println("GroupsManager.getSubGroupsPageWhenGroupNotExists");

			groupsManager.getSubGroups(sess, new Group());

		}

	@Test
	public void addAdmin() throws Exception {
		System.out.println("GroupsManager.addAdmin");

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
			System.out.println("GroupsManager.addAdminWhenGroupNotExists");

			vo = setUpVo();

			Member member = setUpMember(vo);
			User user = perun.getUsersManagerBl().getUserByMember(sess, member);
			groupsManager.addAdmin(sess, new Group(), user);

		}

	@Test (expected=UserNotExistsException.class)
		public void addAdminWhenUserNotExists() throws Exception {
			System.out.println("GroupsManager.addAdminWhenGroupNotExists");

			vo = setUpVo();
			setUpGroup(vo);

			groupsManager.addAdmin(sess, group, new User());

		}

	@Test (expected=AlreadyAdminException.class)
		public void addAdminWhenAlreadyAdmin() throws Exception {
			System.out.println("GroupsManager.addAdminWhenAlreadyAdmin");
			
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
		System.out.println("GroupsManager.addAdminWithGroup");

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
		System.out.println("GroupsManager.removeAdmins");

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
			System.out.println("GroupsManager.removeAdminsWhenGroupNotExists");

			vo = setUpVo();

			Member member = setUpMember(vo);
			User user = perun.getUsersManagerBl().getUserByMember(sess, member);
			groupsManager.removeAdmin(sess, new Group(), user);

		}

	@Test (expected=UserNotExistsException.class)
		public void removeAdminWhenUserNotExist() throws Exception {
			System.out.println("GroupsManager.removeAdminsWhenMemberNotExists");

			vo = setUpVo();
			setUpGroup(vo);

			groupsManager.removeAdmin(sess, group, new User());

		}

	@Test (expected=UserNotAdminException.class)
		public void removeAdminWhenNotAdminException() throws Exception {
			System.out.println("GroupsManager.removeAdminsWhenMemberNotAdmin");

			vo = setUpVo();
			setUpGroup(vo);

			Member member = setUpMember(vo);
			User user = perun.getUsersManagerBl().getUserByMember(sess, member);
			groupsManager.removeAdmin(sess, group, user);

		}

	@Test
	public void removeAdminWithGroup() throws Exception {
		System.out.println("GroupsManager.removeAdminWithGroup");

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
		System.out.println("GroupsManager.getAdmins");

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
		assertTrue("group shoud have 2 admins",admins.size() == 2);
		assertTrue("our member as direct user should be admin",admins.contains(user));
		assertTrue("our member as member of admin group should be admin",admins.contains(user2));
	}

	@Test
	public void getDirectAdmins() throws Exception {
		System.out.println("GroupsManager.getDirectAdmins");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		User user = perun.getUsersManagerBl().getUserByMember(sess, member);
		groupsManager.addAdmin(sess, group, user);

		List<User> admins = groupsManager.getDirectAdmins(sess, group);
		assertTrue("group shoud have 1 admin",admins.size() == 1);
		assertTrue("our member should be admin",admins.contains(user));

	}

	@Test
	public void getAdminGroups() throws Exception {
		System.out.println("GroupsManager.getAdminGroups");

		vo = setUpVo();
		setUpGroup(vo);

		// setting second group
		Group authorizedGroup = groupsManager.createGroup(sess, vo, new Group("New group", "just for testing"));

		groupsManager.addAdmin(sess, group, authorizedGroup);

		assertTrue(groupsManager.getAdminGroups(sess, group).contains(authorizedGroup));

	}

	@Test (expected=GroupNotExistsException.class)
		public void getAdminsWhenGroupNotExists() throws Exception {
			System.out.println("GroupsManager.getAdminsWhenGroupNotExists");

			groupsManager.getAdmins(sess, new Group());

		}

	@Test
	public void getGroups() throws Exception {
		System.out.println("GroupsManager.getGroups");

		vo = setUpVo();
		setUpGroup(vo);

		List<Group> groups = groupsManager.getGroups(sess, vo);
		assertEquals(2, groups.size());
		assertTrue("our group should be in our VO",groups.contains(group));

	}

	@Test (expected=VoNotExistsException.class)
		public void getGroupsWhenVoNotExists() throws Exception {
			System.out.println("GroupsManager.getGroupsWhenVoNotExists");

			groupsManager.getGroups(sess, new Vo());

		}

	@Test (expected=VoNotExistsException.class)
		public void getGroupsPageWhenVoNotExists() throws Exception {
			System.out.println("GroupsManager.getGroupsPageWhenVoNotExists");

			groupsManager.getGroups(sess, new Vo());

		}

	@Test
	public void getGroupsCount() throws Exception {
		System.out.println("GroupsManager.getGroupsCount");

		vo = setUpVo();
		setUpGroup(vo);

		int count = groupsManager.getGroupsCount(sess, vo);
		assertEquals(2, count);

	}

	@Test
	public void getParentGroupMembersCount() throws Exception{
		System.out.println("GroupsManager.getParentGroupMembersCount");
		vo = setUpVo();
		this.groupsManager.createGroup(sess, vo, group2);
		this.groupsManager.createGroup(sess, group2, group);

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
		System.out.println("GroupsManager.getGroupCountInBiggerGroupStructure");

		vo = setUpVo();
		this.groupsManager.createGroup(sess, vo, group);
		this.groupsManager.createGroup(sess, group, group2);
		this.groupsManager.createGroup(sess, group, group21);
		this.groupsManager.createGroup(sess, group2, group3);

		List<Member> members = new ArrayList<Member>();
		for(int i=0;i<4;i++)
			members.add(setUpMemberWithDifferentParam(vo, i));

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
			System.out.println("GroupsManager.getGroupsCountWhenVoNotExists");

			groupsManager.getGroupsCount(sess, new Vo());

		}

	@Test
	public void getSubGroupsCount() throws Exception {
		System.out.println("GroupsManager.getSubGroupsCount");

		vo = setUpVo();
		setUpGroup(vo);

		assertNotNull(groupsManager.createGroup(sess, group, group21));

		int count = groupsManager.getSubGroupsCount(sess, group);
		assertTrue("our group should have one sub-group",count == 1);

	}

	@Test (expected=GroupNotExistsException.class)
		public void getSubGroupsCountWhenGroupNotExists() throws Exception {
			System.out.println("GroupsManager.getSubGroupsCountWhenGroupNotExists");

			groupsManager.getSubGroupsCount(sess, new Group());

		}

	@Test
	public void getVo() throws Exception {
		System.out.println("GroupsManager.getVo");

		vo = setUpVo();
		setUpGroup(vo);

		Vo returnedVo = groupsManager.getVo(sess, group);
		assertNotNull("unable to get VO from DB",returnedVo);
		assertEquals("both VOs should be the same",returnedVo,vo);

	}

	@Test (expected=GroupNotExistsException.class)
		public void getVoWhenGroupNotExist() throws Exception {
			System.out.println("GroupsManager.getVoWhenGroupNotExists");

			groupsManager.getVo(sess, new Group());

		}


	@Test ()
		public void getMembersMembershipType() throws Exception{
			System.out.println("GroupsManager.getMembersMembershipType");
			vo = setUpVo();
			Member member = this.setUpMember(vo);
			this.groupsManager.createGroup(sess, vo, group);
			this.groupsManager.createGroup(sess, group, group2);
			this.groupsManager.addMember(sess, group2, member);
			assertTrue(this.groupsManager.getGroupMembers(sess, group).size()==1);
			assertEquals(this.groupsManager.getGroupMembers(sess, group).get(0).getMembershipType(), MembershipType.INDIRECT);
			assertEquals(this.groupsManager.getGroupMembers(sess, group2).get(0).getMembershipType(), MembershipType.DIRECT);
		}

	@Test
	public void convertGroupToRichGroupWithAttributesTest() throws Exception {
		System.out.println("groupsManagerBl.convertGroupToRichGroupWithAttributes");

		vo = setUpVo();
		attributesList = setUpGroupAttributes();
		setUpGroup(vo);
		attributesManager.setAttributes(sess, group, attributesList);

		RichGroup richGroup = new RichGroup(group, attributesManager.getAttributes(sess, group));
		assertEquals("Both rich groups should be same", richGroup, groupsManagerBl.convertGroupToRichGroupWithAttributes(sess, group));
	}

	@Test
	public void convertGroupToRichGroupWithAttributesByNameTest() throws Exception {
		System.out.println("groupsManagerBl.convertGroupToRichGroupWithAttributesByName");

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
		System.out.println("groupsManagerBl.convertGroupsToRichGroupsWithAttributesWithListOfNamesTest");

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
		String namespace = "group_test_uniqueattribute:specialNamespace";
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
