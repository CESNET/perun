package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupMoveNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationAlreadyExists;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationCannotBeRemoved;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationDoesNotExist;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationNotAllowed;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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

	private Vo vo;
	private List<Attribute> attributesList = new ArrayList<>();

	// exists before every method
	private GroupsManager groupsManager;
	private GroupsManagerBl groupsManagerBl;
	private AttributesManager attributesManager;
	private UsersManagerBl usersManagerBl;

	@Before
	public void setUpBeforeEveryMethod() {

		groupsManager = perun.getGroupsManager();
		groupsManagerBl = perun.getGroupsManagerBl();
		attributesManager = perun.getAttributesManager();
		usersManagerBl = perun.getUsersManagerBl();
		// vo = setUpVo();
		// setUpGroup(vo);
		// moved to every method to save testing time

	}


	@Test
	public void initMembersStatusInGroup() throws Exception {
		System.out.println(CLASS_NAME + "initMembersStatusInGroup");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member = setUpMemberInGroup(vo);

		//load member with group context
		List<Member> groupMembers = groupsManagerBl.getGroupMembers(sess, group);
		Optional<Member> optionalFoundMember = groupMembers.stream().filter(m -> m.getId() == member.getId()).findFirst();
		assertTrue(optionalFoundMember.isPresent());
		Member foundMember = optionalFoundMember.get();

		//validate init status
		assertSame("Member does not have VALID init group status", foundMember.getGroupStatus(), MemberGroupStatus.VALID);
		assertTrue("Member's init group statuses did not contain status for specific group",
				foundMember.getGroupStatuses().containsKey(group.getId()));
		assertEquals("Member's init status for specific group was not VALID",
				foundMember.getGroupStatuses().get(group.getId()), MemberGroupStatus.VALID);
	}

	@Test
	public void expireMemberInGroup() throws Exception {
		System.out.println(CLASS_NAME + "expireMemberInGroup");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member = setUpMemberInGroup(vo);

		groupsManagerBl.expireMemberInGroup(sess, member, group);

		//load member with group context
		List<Member> groupMembers = groupsManagerBl.getGroupMembers(sess, group);
		Optional<Member> optionalFoundMember = groupMembers.stream().filter(m -> m.getId() == member.getId()).findFirst();
		assertTrue(optionalFoundMember.isPresent());
		Member foundMember = optionalFoundMember.get();

		//validate expired status
		assertSame("Member does not have EXPIRED status.", foundMember.getGroupStatus(), MemberGroupStatus.EXPIRED);
		assertTrue("Member's group statuses did not contain status for specific group",
				foundMember.getGroupStatuses().containsKey(group.getId()));
		assertEquals("Member's status for specific group was not EXPIRED",
				foundMember.getGroupStatuses().get(group.getId()), MemberGroupStatus.EXPIRED);
	}

	@Test
	public void expireMemberInGroupWithActiveInSubGroup() throws Exception {
		System.out.println(CLASS_NAME + "expireMemberInGroupWithActiveInSubGroup");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member = setUpMemberInGroup(vo);

		//set up subgroup and add member to it
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.addMember(sess, group2, member);
		groupsManagerBl.moveGroup(sess, group, group2);

		//expire member in upper group
		groupsManagerBl.expireMemberInGroup(sess, member, group);

		//load member with upper group context
		List<Member> groupMembers = groupsManagerBl.getGroupMembers(sess, group);
		Optional<Member> optionalFoundMember = groupMembers.stream().filter(m -> m.getId() == member.getId()).findFirst();
		assertTrue(optionalFoundMember.isPresent());
		Member foundMember = optionalFoundMember.get();

		//validate status after expiration with being VALID in subgroup
		assertSame("Member does not have VALID group status", foundMember.getGroupStatus(), MemberGroupStatus.VALID);
		assertTrue("Member's group statuses did not contain status for specific group",
				foundMember.getGroupStatuses().containsKey(group.getId()));
		assertEquals("Member's status for specific group was not VALID",
				foundMember.getGroupStatuses().get(group.getId()), MemberGroupStatus.VALID);
	}

	@Test
	public void expireMemberInSubGroupAndStillActiveInUpperGroup() throws Exception {
		System.out.println(CLASS_NAME + "expireMemberInSubGroupAndStillActiveInUpperGroup");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member = setUpMemberInGroup(vo);

		//set up subgroup and add member to it
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.addMember(sess, group2, member);
		groupsManagerBl.moveGroup(sess, group, group2);

		//expire member in upper group
		groupsManagerBl.expireMemberInGroup(sess, member, group2);

		//load member with upper group context
		List<Member> groupMembers = groupsManagerBl.getGroupMembers(sess, group);
		Optional<Member> optionalFoundMember = groupMembers.stream().filter(m -> m.getId() == member.getId()).findFirst();
		assertTrue(optionalFoundMember.isPresent());
		Member foundMember = optionalFoundMember.get();

		//validate status after expiration with being expired in subgroup
		assertSame("Member does not have VALID group status", foundMember.getGroupStatus(), MemberGroupStatus.VALID);
		assertTrue("Member's group statuses did not contain status for specific group",
				foundMember.getGroupStatuses().containsKey(group.getId()));
		assertEquals("Member's status for specific group was not VALID",
				foundMember.getGroupStatuses().get(group.getId()), MemberGroupStatus.VALID);
	}

	@Test
	public void expireMemberInSubGroupAndInUpperGroup() throws Exception {
		System.out.println(CLASS_NAME + "expireMemberInSubGroupAndInUpperGroup");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member = setUpMemberInGroup(vo);

		//set up subgroup and add member to it
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.addMember(sess, group2, member);
		groupsManagerBl.moveGroup(sess, group, group2);

		//expire member in upper group
		groupsManagerBl.expireMemberInGroup(sess, member, group2);
		groupsManagerBl.expireMemberInGroup(sess, member, group);

		//load member with upper group context
		List<Member> groupMembers = groupsManagerBl.getGroupMembers(sess, group);
		Optional<Member> optionalFoundMember = groupMembers.stream().filter(m -> m.getId() == member.getId()).findFirst();
		assertTrue(optionalFoundMember.isPresent());
		Member foundMember = optionalFoundMember.get();

		//validate status after expiration with being expired in subgroup
		assertSame("Member does not have EXPIRED group status", foundMember.getGroupStatus(), MemberGroupStatus.EXPIRED);
		assertTrue("Member's group statuses did not contain status for specific group",
				foundMember.getGroupStatuses().containsKey(group.getId()));
		assertEquals("Member's status for specific group was not EXPIRED",
				foundMember.getGroupStatuses().get(group.getId()), MemberGroupStatus.EXPIRED);
	}

	@Test
	public void validateMemberRecursively() throws Exception {
		System.out.println(CLASS_NAME + "validateMemberRecursively");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member = setUpMemberInGroup(vo);

		//set up sub groups
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.createGroup(sess, vo, group3);
		groupsManagerBl.createGroup(sess, vo, group4);
		groupsManagerBl.createGroup(sess, vo, group5);
		groupsManagerBl.addMember(sess, group2, member);
		groupsManagerBl.addMember(sess, group3, member);
		groupsManagerBl.addMember(sess, group4, member);
		groupsManagerBl.addMember(sess, group5, member);

		// create realtions between groups
		groupsManagerBl.moveGroup(sess, group, group2);
		groupsManagerBl.moveGroup(sess, group2, group3);
		groupsManagerBl.createGroupUnion(sess, group3, group4, false);
		groupsManagerBl.createGroupUnion(sess, group4, group5, true);

		// expire member everywhere
		groupsManagerBl.expireMemberInGroup(sess, member, group);
		groupsManagerBl.expireMemberInGroup(sess, member, group2);
		groupsManagerBl.expireMemberInGroup(sess, member, group3);
		groupsManagerBl.expireMemberInGroup(sess, member, group4);
		groupsManagerBl.expireMemberInGroup(sess, member, group5);

		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group));
		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group2));
		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group3));
		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group4));
		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group5));

		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group5));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group4));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group3));
		assertEquals("Member's inti group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group2));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group));

		// validate member in the very bottom group
		groupsManagerBl.validateMemberInGroup(sess, member, group5);

		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group));
		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group2));
		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group3));
		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group4));
		assertEquals("Member's direct init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group5));

		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group5));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group4));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group3));
		assertEquals("Member's inti group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group));
	}

	@Test
	public void expireMemberRecursively() throws Exception {
		System.out.println(CLASS_NAME + "expireMemberRecursively");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member = setUpMemberInGroup(vo);

		//set up sub groups
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.createGroup(sess, vo, group3);
		groupsManagerBl.createGroup(sess, vo, group4);
		groupsManagerBl.createGroup(sess, vo, group5);
		groupsManagerBl.addMember(sess, group2, member);
		groupsManagerBl.addMember(sess, group3, member);
		groupsManagerBl.addMember(sess, group4, member);
		groupsManagerBl.addMember(sess, group5, member);

		// create relations between groups
		groupsManagerBl.moveGroup(sess, group, group2);
		groupsManagerBl.moveGroup(sess, group2, group3);
		groupsManagerBl.createGroupUnion(sess, group3, group4, false);
		groupsManagerBl.createGroupUnion(sess, group4, group5, true);

		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group5));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group4));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group3));
		assertEquals("Member's inti group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group));

		// expire member in all groups except the very bottom group
		groupsManagerBl.expireMemberInGroup(sess, member, group);
		groupsManagerBl.expireMemberInGroup(sess, member, group2);
		groupsManagerBl.expireMemberInGroup(sess, member, group3);
		groupsManagerBl.expireMemberInGroup(sess, member, group4);

		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group));
		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group2));
		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group3));
		assertEquals("Member's direct init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group4));
		assertEquals("Member's direct init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group5));

		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group5));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group4));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group3));
		assertEquals("Member's inti group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group));

		// expire member in the very bottom group
		groupsManagerBl.expireMemberInGroup(sess, member, group5);

		assertEquals("Member's direct group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group5));
		assertEquals("Member's direct group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group4));
		assertEquals("Member's direct group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group3));
		assertEquals("Member's direct group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group2));
		assertEquals("Member's direct group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getDirectMemberGroupStatus(sess, member, group));

		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group5));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group4));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group3));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group2));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member, group));
	}

	@Test
	public void moveGroupCorrectMemberGroupStatuses() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupCorrectMemberGroupStatuses");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);
		Member member2 = setUpMemberWithDifferentParam(vo, 222);
		Member member3 = setUpMemberWithDifferentParam(vo, 333);

		//set up sub groups
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.createGroup(sess, vo, group3);
		groupsManagerBl.createGroup(sess, vo, group4);
		groupsManagerBl.createGroup(sess, vo, group5);

		groupsManagerBl.moveGroup(sess, group2, group);
		groupsManagerBl.moveGroup(sess, group3, group2);
		groupsManagerBl.moveGroup(sess, group5, group4);

		// set up members with statuses
		groupsManagerBl.addMember(sess, group, member1);
		groupsManagerBl.addMember(sess, group4, member1);
		groupsManagerBl.addMember(sess, group, member2);
		groupsManagerBl.addMember(sess, group2, member2);
		groupsManagerBl.addMember(sess, group, member3);
		groupsManagerBl.expireMemberInGroup(sess, member1, group4);
		groupsManagerBl.expireMemberInGroup(sess, member2, group2);
		groupsManagerBl.expireMemberInGroup(sess, member3, group);

		// verify init statuses
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group4));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group5));

		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group3));
		assertEquals("Member's init group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group4));
		assertEquals("Member's init group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group5));

		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group2));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group3));
		assertEquals("Member's init group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group4));
		assertEquals("Member's init group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group5));

		groupsManagerBl.moveGroup(sess, group4, group);

		// verify after statuses
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group4));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group5));

		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group2));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group3));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group4));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group5));

		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group));
		assertEquals("Member's group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group2));
		assertEquals("Member's group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group3));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group4));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group5));
	}

	@Test
	public void moveGroupCorrectMemberGroupStatuses2() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupCorrectMemberGroupStatuses2");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		//set up sub groups
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.createGroup(sess, vo, group3);

		groupsManagerBl.moveGroup(sess, group, group2);
		groupsManagerBl.moveGroup(sess, group2, group3);

		// set up members with statuses
		groupsManagerBl.addMember(sess, group, member1);
		groupsManagerBl.addMember(sess, group3, member1);
		groupsManagerBl.expireMemberInGroup(sess, member1, group);

		// verify init statuses
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));

		groupsManagerBl.moveGroup(sess, null, group3);

		// verify after statuses
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));
	}

	@Test
	public void addMemberCorrectMemberGroupStatusesAreSet() throws Exception {
		System.out.println(CLASS_NAME + "addMemberCorrectMemberGroupStatusesAreSet");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		//set up sub groups
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.createGroup(sess, vo, group3);

		groupsManagerBl.moveGroup(sess, group2, group);
		groupsManagerBl.moveGroup(sess, group3, group2);

		// set up members with statuses
		groupsManagerBl.addMember(sess, group2, member1);
		groupsManagerBl.expireMemberInGroup(sess, member1, group2);

		// verify init statuses
		assertEquals("Member's init group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));

		// add member
		groupsManagerBl.addMember(sess, group, member1);

		// verify statuses after adding member
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));
	}

	@Test
	public void testMemberGroupExpirationMGAttribute() throws Exception {
		System.out.println(CLASS_NAME + "testMemberGroupExpirationMGAttribute");

		User user = new User();
		user.setFirstName("test");
		user.setMiddleName("");
		user.setLastName("test");
		user.setTitleBefore("");
		user.setTitleAfter("");
		user = perun.getUsersManagerBl().createUser(sess, user);

		Vo vo = setUpVo();
		Member m = perun.getMembersManager().createMember(sess, vo, user);
		Group g = perun.getGroupsManager().createGroup(sess, vo, new Group("test","test" ));

		Facility f = perun.getFacilitiesManager().createFacility(sess, new Facility(0, "test", "test"));
		Resource r = perun.getResourcesManager().createResource(sess, new Resource(0, "test", "test", f.getId()), vo, f);

		perun.getResourcesManager().assignGroupToResource(sess, g, r);
		perun.getGroupsManager().addMember(sess, g, m);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_VIRT);
		attrDef.setFriendlyName("groupStatus");
		attrDef.setDescription("groupStatus");
		attrDef.setFriendlyName("groupStatus");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);

		Attribute withValue = perun.getAttributesManager().getAttribute(sess, m, g, attrDef.getName());
		assertTrue("Wrong member_group status for valid member", "VALID".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, m, g, withValue);

		// expire him
		perun.getGroupsManagerBl().expireMemberInGroup(sess, m, g);

		withValue = perun.getAttributesManager().getAttribute(sess, m, g, attrDef.getName());
		assertTrue("Wrong member_group status for expired member", "EXPIRED".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, m, g, withValue);

		// remove him (will lost assignment)
		perun.getGroupsManager().removeMember(sess, g, m);

		withValue = perun.getAttributesManager().getAttribute(sess, m, g, attrDef.getName());
		assertTrue(withValue.getValue() == null);
		perun.getAttributesManager().checkAttributeValue(sess, m, g, withValue);

	}

	@Test
	public void testMemberGroupExpirationMRAttribute() throws Exception {
		System.out.println(CLASS_NAME + "testMemberGroupExpirationMRAttribute");

		User user = new User();
		user.setFirstName("test");
		user.setMiddleName("");
		user.setLastName("test");
		user.setTitleBefore("");
		user.setTitleAfter("");
		user = perun.getUsersManagerBl().createUser(sess, user);

		Vo vo = setUpVo();
		Member m = perun.getMembersManager().createMember(sess, vo, user);
		Group g = perun.getGroupsManager().createGroup(sess, vo, new Group("test","test" ));
		Group g2 = perun.getGroupsManager().createGroup(sess, vo, new Group("test2","test2" ));

		Facility f = perun.getFacilitiesManager().createFacility(sess, new Facility(0, "test", "test"));
		Resource r = perun.getResourcesManager().createResource(sess, new Resource(0, "test", "test", f.getId()), vo, f);

		perun.getResourcesManager().assignGroupToResource(sess, g, r);
		perun.getResourcesManager().assignGroupToResource(sess, g2, r);
		perun.getGroupsManager().addMember(sess, g, m);
		perun.getGroupsManager().addMember(sess, g2, m);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_MEMBER_RESOURCE_ATTR_VIRT);
		attrDef.setFriendlyName("groupStatus");
		attrDef.setDescription("groupStatus");
		attrDef.setFriendlyName("groupStatus");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);

		Attribute withValue = perun.getAttributesManager().getAttribute(sess, m, r, attrDef.getName());
		assertTrue("Wrong member_resource status for valid member", "VALID".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, m, r, withValue);

		// expire him in first group
		perun.getGroupsManagerBl().expireMemberInGroup(sess, m, g);
		// should be still valid from second
		withValue = perun.getAttributesManager().getAttribute(sess, m, r, attrDef.getName());
		assertTrue("Wrong member_resource status for valid member", "VALID".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, m, r, withValue);

		// expire him in second group
		perun.getGroupsManagerBl().expireMemberInGroup(sess, m, g2);
		// should be expired
		withValue = perun.getAttributesManager().getAttribute(sess, m, r, attrDef.getName());
		assertTrue("Wrong member_resource status for expired member", "EXPIRED".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, m, r, withValue);

		// validate him in first group
		perun.getGroupsManagerBl().validateMemberInGroup(sess, m, g);
		// remove him from second
		perun.getGroupsManager().removeMember(sess, g2, m);
		// should be valid from first group
		withValue = perun.getAttributesManager().getAttribute(sess, m, r, attrDef.getName());
		assertTrue("Wrong member_resource status for valid member", "VALID".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, m, r, withValue);

		// remove him (will lost assignment)
		perun.getGroupsManager().removeMember(sess, g, m);
		// value should be null
		withValue = perun.getAttributesManager().getAttribute(sess, m, r, attrDef.getName());
		assertTrue(withValue.getValue() == null);
		perun.getAttributesManager().checkAttributeValue(sess, m, r, withValue);

	}

	@Test
	public void testMemberGroupExpirationUFAttribute() throws Exception {
		System.out.println(CLASS_NAME + "testMemberGroupExpirationUFAttribute");

		User user = new User();
		user.setFirstName("test");
		user.setMiddleName("");
		user.setLastName("test");
		user.setTitleBefore("");
		user.setTitleAfter("");
		user = perun.getUsersManagerBl().createUser(sess, user);

		Vo vo = setUpVo();
		Vo vo2 = perun.getVosManager().createVo(sess, new Vo(0, "secondVo", "secondVo"));
		Member m = perun.getMembersManager().createMember(sess, vo, user);
		Member m2 = perun.getMembersManager().createMember(sess, vo2, user);
		Group g = perun.getGroupsManager().createGroup(sess, vo, new Group("test","test" ));
		Group g2 = perun.getGroupsManager().createGroup(sess, vo2, new Group("test","test" ));

		Facility f = perun.getFacilitiesManager().createFacility(sess, new Facility(0, "test", "test"));
		Resource r = perun.getResourcesManager().createResource(sess, new Resource(0, "test", "test", f.getId()), vo, f);
		Resource r2 = perun.getResourcesManager().createResource(sess, new Resource(0, "test", "test", f.getId()), vo2, f);

		perun.getResourcesManager().assignGroupToResource(sess, g, r);
		perun.getGroupsManager().addMember(sess, g, m);

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		attrDef.setFriendlyName("groupStatus");
		attrDef.setDescription("groupStatus");
		attrDef.setFriendlyName("groupStatus");
		attrDef.setType(String.class.getName());
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);

		Attribute withValue = perun.getAttributesManager().getAttribute(sess, f, user, attrDef.getName());
		assertTrue("Wrong user_facility status for valid member", "VALID".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, f, user, withValue);

		// expire him
		perun.getGroupsManagerBl().expireMemberInGroup(sess, m, g);

		withValue = perun.getAttributesManager().getAttribute(sess, f, user, attrDef.getName());
		assertTrue("Wrong user_facility status for expired member", "EXPIRED".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, f, user, withValue);

		// remove him (will lost assignment)
		perun.getGroupsManager().removeMember(sess, g, m);

		withValue = perun.getAttributesManager().getAttribute(sess, f, user, attrDef.getName());
		assertTrue(withValue.getValue() == null);
		perun.getAttributesManager().checkAttributeValue(sess, f, user, withValue);

		// now multiple resources context

		// put member back
		perun.getGroupsManager().addMember(sess, g, m);
		// create second assignment
		perun.getResourcesManager().assignGroupToResource(sess, g2, r2);
		perun.getGroupsManager().addMember(sess, g2, m2);

		withValue = perun.getAttributesManager().getAttribute(sess, f, user, attrDef.getName());
		assertTrue("Wrong user_facility status for valid member", "VALID".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, f, user, withValue);

		// expire him in first
		perun.getGroupsManagerBl().expireMemberInGroup(sess, m, g);
		// is still valid
		withValue = perun.getAttributesManager().getAttribute(sess, f, user, attrDef.getName());
		assertTrue("Wrong user_facility status for valid member", "VALID".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, f, user, withValue);

		// expire him in second
		perun.getGroupsManagerBl().expireMemberInGroup(sess, m2, g2);
		// should be expired
		withValue = perun.getAttributesManager().getAttribute(sess, f, user, attrDef.getName());
		assertTrue("Wrong user_facility status for expired member", "EXPIRED".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, f, user, withValue);

		// remove from first group
		perun.getGroupsManager().removeMember(sess, g, m);
		// should be expired
		withValue = perun.getAttributesManager().getAttribute(sess, f, user, attrDef.getName());
		assertTrue("Wrong user_facility status for expired member", "EXPIRED".equals(withValue.getValue()));
		perun.getAttributesManager().checkAttributeValue(sess, f, user, withValue);

		// remove him from second (will lost assignment)
		perun.getGroupsManager().removeMember(sess, g2, m2);
		withValue = perun.getAttributesManager().getAttribute(sess, f, user, attrDef.getName());
		assertTrue(withValue.getValue() == null);
		perun.getAttributesManager().checkAttributeValue(sess, f, user, withValue);

	}

	@Test
	public void addMemberToGroupWithGroupMembershipExpirationSet() throws Exception {
		System.out.println(CLASS_NAME + "addMemberToGroupWithGroupMembershipExpirationSet");

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);

		// set up expiration rules attribute
		createGroupExpirationRulesAttribute(group);

		// add member to group where is expiration set
		groupsManagerBl.addMember(sess, group, member1);

		Attribute membershipAttribute = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		LocalDate expectedDate = LocalDate.parse((String) membershipAttribute.getValue());

		// Set to 1.1. next year
		LocalDate requiredDate = LocalDate.of(LocalDate.now().getYear()+1, 1, 1);

		assertEquals("Year must match", requiredDate.getYear(), expectedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), expectedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), expectedDate.getDayOfMonth());
	}

	@Test
	public void extendGroupMembership() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembership");

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// set up expiration rules attribute
		createGroupExpirationRulesAttribute(group);

		// try to extend membership
		groupsManagerBl.extendMembershipInGroup(sess, member1, group);

		Attribute membershipAttribute = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		LocalDate expectedDate = LocalDate.parse((String) membershipAttribute.getValue());

		// Set to 1.1. next year
		LocalDate requiredDate = LocalDate.of(LocalDate.now().getYear()+1, 1, 1);

		assertEquals("Year must match", requiredDate.getYear(), expectedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), expectedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), expectedDate.getDayOfMonth());
	}

	@Test
	public void extendGroupMembershipBy10Days() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipBy10Days");

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "+10d");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Try to extend membership
		groupsManagerBl.extendMembershipInGroup(sess, member1, group);

		Attribute membershipAttribute = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		LocalDate expectedDate = LocalDate.parse((String) membershipAttribute.getValue());

		LocalDate requiredDate = LocalDate.now().plusDays(10);

		assertEquals("Year must match", requiredDate.getYear(), expectedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), expectedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), expectedDate.getDayOfMonth());
	}

	@Test
	public void extendGroupMembershipInGracePeriod() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipInGracePeriod");

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Period will be set to the next day
		LocalDate date = LocalDate.now().plusDays(1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		// Set perid to day after today
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, date.getDayOfMonth() + "." + date.getMonthValue() + ".");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "1m");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Try to extend membership
		groupsManagerBl.extendMembershipInGroup(sess, member1, group);

		Attribute membershipAttribute = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		LocalDate expectedDate = LocalDate.parse((String) membershipAttribute.getValue());

		LocalDate requiredDate = LocalDate.now().plusDays(1).plusYears(1);

		assertEquals("Year must match", requiredDate.getYear(), expectedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), expectedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), expectedDate.getDayOfMonth());
	}

	@Test
	public void extendGroupMembershipOutsideGracePeriod() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipOutsideGracePeriod");

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set period to three months later
		LocalDate date = LocalDate.now().plusMonths(3);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		// Set perid to day after today
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, date.getDayOfMonth() + "." + date.getMonthValue() + ".");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "1m");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Try to extend membership
		groupsManagerBl.extendMembershipInGroup(sess, member1, group);

		Attribute membershipAttribute = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		LocalDate expectedDate = LocalDate.parse((String) membershipAttribute.getValue());

		LocalDate requiredDate = LocalDate.now().plusMonths(3);

		assertEquals("Year must match", requiredDate.getYear(), expectedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), expectedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), expectedDate.getDayOfMonth());
	}

	@Test
	public void extendGroupMembershipForMemberWithSufficientLoa() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipForMemberWithSufficientLoa");

		ExtSource es = perun.getExtSourcesManagerBl().createExtSource(sess, extSource, null);

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0,1");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Set LOA 2 for member
		UserExtSource ues = new UserExtSource(es, "abc");
		ues.setLoa(2);

		User user = usersManagerBl.getUserByMember(sess, member1);
		usersManagerBl.addUserExtSource(sess, user, ues);

		// Try to extend membership
		groupsManagerBl.extendMembershipInGroup(sess, member1, group);

		Attribute membershipAttribute = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		LocalDate expectedDate = LocalDate.parse((String) membershipAttribute.getValue());

		// Set to 1.1. next year
		LocalDate requiredDate = LocalDate.of(LocalDate.now().getYear()+1, 1, 1);

		assertEquals("Year must match", requiredDate.getYear(), expectedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), expectedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), expectedDate.getDayOfMonth());
	}

	@Test
	public void extendGroupMembershipForMemberWithInsufficientLoa() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipForMemberWithInsufficientLoa");

		ExtSource es = perun.getExtSourcesManagerBl().createExtSource(sess, extSource, null);

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0,1");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		Attribute membershipExpirationAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF+":groupMembershipExpiration"));
		LocalDate now = LocalDate.now();
		membershipExpirationAttribute.setValue(now.toString());
		attributesManager.setAttribute(sess, member1, group, membershipExpirationAttribute);

		// Set LOA 1 for member
		UserExtSource ues = new UserExtSource(es, "abc");
		ues.setLoa(1);

		User user = usersManagerBl.getUserByMember(sess, member1);
		usersManagerBl.addUserExtSource(sess, user, ues);

		// Try to extend membership
		try {
			groupsManagerBl.extendMembershipInGroup(sess, member1, group);
		} catch (ExtendMembershipException e) {
			assertEquals(e.getReason(), ExtendMembershipException.Reason.INSUFFICIENTLOAFOREXTENSION);
		}

		Attribute membershipAttribute = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertEquals("membership attribute value must contains same value as before extension.",
				now.toString(), membershipAttribute.getValue()); // Attribute cannot contain any value
	}

	@Test
	public void extendGroupMembershipForDefinedLoaAllowed() throws Exception {
		System.out.println(CLASS_NAME + "extendGroupMembershipForDefinedLoaAllowed");

		ExtSource es = perun.getExtSourcesManagerBl().createExtSource(sess, extSource, null);

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName, "1|+1m");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Set LOA 1 for member
		UserExtSource ues = new UserExtSource(es, "abc");
		ues.setLoa(1);

		User user = usersManagerBl.getUserByMember(sess, member1);
		usersManagerBl.addUserExtSource(sess, user, ues);

		// Try to extend membership
		groupsManagerBl.extendMembershipInGroup(sess, member1, group);

		Attribute membershipAttribute = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		// Try to extend membership once again
		groupsManagerBl.extendMembershipInGroup(sess, member1, group);

		membershipAttribute = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		LocalDate expectedDate = LocalDate.parse((String) membershipAttribute.getValue());

		LocalDate requiredDate = LocalDate.now().plusMonths(1);

		assertEquals("Year must match", requiredDate.getYear(), expectedDate.getYear());
		assertEquals("Month must match", requiredDate.getMonthValue(), expectedDate.getMonthValue());
		assertEquals("Day must match", requiredDate.getDayOfMonth(), expectedDate.getDayOfMonth());
	}

	@Test
	public void canExtendGroupMembershipForDefinedLoaNotAllowed() throws Exception {
		System.out.println(CLASS_NAME + "extendMembershipForDefinedLoaNotAllowed");

		ExtSource es = perun.getExtSourcesManagerBl().createExtSource(sess, extSource, null);

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName, "1|+1m.");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Set LOA 1 for member
		UserExtSource ues = new UserExtSource(es, "abc");
		ues.setLoa(0);

		User user = usersManagerBl.getUserByMember(sess, member1);
		usersManagerBl.addUserExtSource(sess, user, ues);

		// Try to extend membership
		groupsManagerBl.extendMembershipInGroup(sess, member1, group);

		Attribute membershipAttribute = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttribute);
		assertNotNull("membership attribute value must be set", membershipAttribute.getValue());

		// Try to extend membership
		assertFalse(groupsManagerBl.canExtendMembershipInGroup(sess, member1, group));
	}

	@Test
	// It extend membership and try to extend it again, it must decline another expiration
	public void canExtendMembershipInGracePeriod() throws Exception {
		System.out.println(CLASS_NAME + "canExtendMembershipInGracePeriod");

		ExtSource es = perun.getExtSourcesManagerBl().createExtSource(sess, extSource, null);

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName, "0");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName, "1|+1m.");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Set LOA 1 for member
		UserExtSource ues = new UserExtSource(es, "abc");
		ues.setLoa(0);

		User user = usersManagerBl.getUserByMember(sess, member1);
		usersManagerBl.addUserExtSource(sess, user, ues);

		// Try to extend membership
		groupsManagerBl.extendMembershipInGroup(sess, member1, group);

		Attribute membershipAttributeFirst = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");

		assertNotNull("membership attribute must be set", membershipAttributeFirst);
		assertNotNull("membership attribute value must be set", membershipAttributeFirst.getValue());

		// Try to extend membership
		assertFalse(groupsManagerBl.canExtendMembershipInGroup(sess, member1, group));
	}


	@Test
	public void canExtendMembershipInGracePeriodRelativeDate() throws Exception {
		System.out.println(CLASS_NAME + "canExtendMembershipInGracePeriodRelativeDate");

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "+6m");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "2d");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Set expiration date to tomorrow (one day after grace period begun)
		LocalDate today = LocalDate.now().plusDays(1);

		Attribute expirationAttr = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF+":groupMembershipExpiration"));
		expirationAttr.setValue(today.toString());
		attributesManager.setAttribute(sess, member1, group, expirationAttr);

		// Check if enviroment is set properly
		expirationAttr = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");
		assertNotNull("membership attribute must be set", expirationAttr);
		assertNotNull("membership attribute value must be set", expirationAttr.getValue());

		// Try to extend membership
		assertTrue(groupsManagerBl.canExtendMembershipInGroup(sess, member1, group));
	}

	@Test
	public void canExtendMembershipOutOfGracePeriodRelativeDate() throws Exception {
		System.out.println(CLASS_NAME + "canExtendMembershipOutOfGracePeriodRelativeDate");

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "+6m");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "2d");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Set expiration date to three days after. (one day untill grace period begins)
		LocalDate today = LocalDate.now().plusDays(3);

		Attribute expirationAttr = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF+":groupMembershipExpiration"));
		expirationAttr.setValue(today.toString());
		attributesManager.setAttribute(sess, member1, group, expirationAttr);

		// Check if enviroment is set properly
		expirationAttr = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");
		assertNotNull("membership attribute must be set", expirationAttr);
		assertNotNull("membership attribute value must be set", expirationAttr.getValue());

		// Check if membership can be extended
		assertFalse(groupsManagerBl.canExtendMembershipInGroup(sess, member1, group));
	}

	@Test
	public void canExtendMembershipInGracePeriodAbsoluteDate() throws Exception {
		System.out.println(CLASS_NAME + "canExtendMembershipInGracePeriodAbsoluteDate");

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "2d");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Set expiration date to tomorrow (one day after grace period begun)
		LocalDate today = LocalDate.now().plusDays(1);

		Attribute expirationAttr = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF+":groupMembershipExpiration"));
		expirationAttr.setValue(today.toString());
		attributesManager.setAttribute(sess, member1, group, expirationAttr);

		// Check if enviroment is set properly
		expirationAttr = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");
		assertNotNull("membership attribute must be set", expirationAttr);
		assertNotNull("membership attribute value must be set", expirationAttr.getValue());

		// Try to extend membership
		assertTrue(groupsManagerBl.canExtendMembershipInGroup(sess, member1, group));
	}

	@Test
	public void canExtendMembershipOutOfGracePeriodAbsoluteDate() throws Exception {
		System.out.println(CLASS_NAME + "canExtendMembershipOutOfGracePeriodAbsoluteDate");

		// set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		// set up group
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member1);

		// Set membershipExpirationRules attribute
		HashMap<String, String> extendMembershipRules = new LinkedHashMap<>();
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");
		extendMembershipRules.put(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName, "2d");

		Attribute extendMembershipRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF+":groupMembershipExpirationRules"));
		extendMembershipRulesAttribute.setValue(extendMembershipRules);

		attributesManager.setAttribute(sess, group, extendMembershipRulesAttribute);

		// Set expiration date to three days after. (one day untill grace period begins)
		LocalDate today = LocalDate.now().plusDays(3);

		Attribute expirationAttr = new Attribute(attributesManager.getAttributeDefinition(sess, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF+":groupMembershipExpiration"));
		expirationAttr.setValue(today.toString());
		attributesManager.setAttribute(sess, member1, group, expirationAttr);

		// Check if enviroment is set properly
		expirationAttr = attributesManager.getAttribute(sess, member1, group, AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration");
		assertNotNull("membership attribute must be set", expirationAttr);
		assertNotNull("membership attribute value must be set", expirationAttr.getValue());

		// Check if membership can be extended
		assertFalse(groupsManagerBl.canExtendMembershipInGroup(sess, member1, group));
	}

	@Test
	public void removeMemberCorrectMemberGroupStatusesAreSet() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberCorrectMemberGroupStatusesAreSet");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);

		//set up sub groups
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.createGroup(sess, vo, group3);
		groupsManagerBl.createGroup(sess, vo, group4);

		groupsManagerBl.moveGroup(sess, group2, group);
		groupsManagerBl.moveGroup(sess, group3, group2);
		groupsManagerBl.moveGroup(sess, group4, group3);

		// set up members with statuses
		groupsManagerBl.addMember(sess, group2, member1);
		groupsManagerBl.expireMemberInGroup(sess, member1, group2);

		// add member
		groupsManagerBl.addMember(sess, group, member1);

		// verify init statuses
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group4));

		groupsManagerBl.removeMember(sess, group, member1);

		assertEquals("Member's init group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group4));
	}

	@Test
	public void createGroupUnionCorrectMemberGroupStatusesAreSet() throws Exception {
		System.out.println(CLASS_NAME + "createGroupUnionCorrectMemberGroupStatusesAreSet");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);
		Member member2 = setUpMemberWithDifferentParam(vo, 222);
		Member member3 = setUpMemberWithDifferentParam(vo, 333);

		//set up sub groups
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.createGroup(sess, vo, group3);
		groupsManagerBl.createGroup(sess, vo, group4);
		groupsManagerBl.createGroup(sess, vo, group5);

		groupsManagerBl.moveGroup(sess, group2, group);
		groupsManagerBl.moveGroup(sess, group3, group2);
		groupsManagerBl.moveGroup(sess, group5, group4);

		// set up members with statuses
		groupsManagerBl.addMember(sess, group, member1);
		groupsManagerBl.addMember(sess, group4, member1);
		groupsManagerBl.addMember(sess, group, member2);
		groupsManagerBl.addMember(sess, group2, member2);
		groupsManagerBl.addMember(sess, group, member3);
		groupsManagerBl.expireMemberInGroup(sess, member1, group4);
		groupsManagerBl.expireMemberInGroup(sess, member2, group2);
		groupsManagerBl.expireMemberInGroup(sess, member3, group);

		// verify init statuses
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group4));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group5));

		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group3));
		assertEquals("Member's init group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group4));
		assertEquals("Member's init group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group5));

		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group2));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group3));
		assertEquals("Member's init group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group4));
		assertEquals("Member's init group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group5));

		groupsManagerBl.createGroupUnion(sess, group4, group, false);

		// verify after statuses
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group4));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group5));

		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group2));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group3));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group4));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group5));

		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group2));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group3));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group4));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group5));
	}

	@Test
	public void removeGroupUnionCorrectMemberGroupStatusesAreSet() throws Exception {
		System.out.println(CLASS_NAME + "removeGroupUnionCorrectMemberGroupStatusesAreSet");

		//set up member in group and vo
		Vo vo = setUpVo();
		Member member1 = setUpMemberWithDifferentParam(vo, 111);
		Member member2 = setUpMemberWithDifferentParam(vo, 222);
		Member member3 = setUpMemberWithDifferentParam(vo, 333);

		//set up sub groups
		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.createGroup(sess, vo, group2);
		groupsManagerBl.createGroup(sess, vo, group3);
		groupsManagerBl.createGroup(sess, vo, group4);
		groupsManagerBl.createGroup(sess, vo, group5);

		groupsManagerBl.moveGroup(sess, group2, group);
		groupsManagerBl.moveGroup(sess, group3, group2);
		groupsManagerBl.moveGroup(sess, group5, group4);

		// set up members with statuses
		groupsManagerBl.addMember(sess, group, member1);
		groupsManagerBl.addMember(sess, group4, member1);
		groupsManagerBl.addMember(sess, group, member2);
		groupsManagerBl.addMember(sess, group2, member2);
		groupsManagerBl.addMember(sess, group, member3);
		groupsManagerBl.expireMemberInGroup(sess, member1, group4);
		groupsManagerBl.expireMemberInGroup(sess, member2, group2);
		groupsManagerBl.expireMemberInGroup(sess, member3, group);
		groupsManagerBl.createGroupUnion(sess, group4, group, false);

		// verify init statuses
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group4));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group5));

		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group2));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group3));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group4));
		assertEquals("Member's init group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group5));

		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group2));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group3));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group4));
		assertEquals("Member's init group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group5));

		groupsManagerBl.removeGroupUnion(sess, group4, group, false);

		// verify after statuses
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group2));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group3));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group4));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member1, group5));

		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group2));
		assertEquals("Member's group status is not VALID", MemberGroupStatus.VALID, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group3));
		assertEquals("Member's group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group4));
		assertEquals("Member's group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member2, group5));

		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group2));
		assertEquals("Member's group status is not EXPIRED", MemberGroupStatus.EXPIRED, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group3));
		assertEquals("Member's group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group4));
		assertEquals("Member's group status is not null", null, groupsManagerBl.getTotalMemberGroupStatus(sess, member3, group5));
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
	public void deleteGroupWithSubGroups() throws Exception {
		System.out.println(CLASS_NAME + "deleteGroup");

		vo = setUpVo();
		List<Group> groups = setUpGroupsWithSubgroups(vo);

		List<Group> topLevels = new ArrayList<>();
		for (Group group : groups) {
			// get only top-level groups
			if (!group.getName().contains(":")) topLevels.add(group);
		}
		assertTrue(!topLevels.isEmpty());

		// try to delete from the top
		groupsManager.deleteGroups(sess, topLevels, true);

		// there should be only "members" group left
		List<Group> retrievedGroups = groupsManager.getGroups(sess, vo);
		assertTrue(retrievedGroups != null);
		assertTrue(retrievedGroups.size() == 1);
		assertTrue(retrievedGroups.get(0).getName().equals(VosManager.MEMBERS_GROUP));

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
		this.groupsManagerBl.createGroup(sess, groups.get(0), subgroup);
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
		this.groupsManagerBl.createGroup(sess, groups.get(0), subgroup);

		this.groupsManager.deleteGroups(sess, groups, true);
	}

	@Test (expected=GroupNotExistsException.class)
	public void deleteGroupWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteGroupWhenGroupNotExists");

		groupsManager.deleteGroup(sess, new Group());

	}

	@Test
	public void deleteGroupWithRelations() throws Exception {
		System.out.println("GroupsManager.deleteGroupWithRelations");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, group, group2);
		groupsManager.createGroup(sess, group, group3);
		groupsManager.createGroup(sess, group2, group4);
		groupsManager.createGroup(sess, vo, group5);
		groupsManager.createGroup(sess, vo, group6);
		groupsManager.createGroup(sess, vo, group7);

		// first add members to groups
		Member member = setUpMember(vo);
		Member member2 = setUpMember(vo);
		Member member3 = setUpMember(vo);
		Member member4 = setUpMember(vo);
		groupsManager.addMember(sess, group4, member);
		groupsManager.addMember(sess, group7, member2);
		groupsManager.addMember(sess, group5, member3);
		groupsManager.addMember(sess, group3, member4);

		// then create relations between them
		groupsManager.createGroupUnion(sess, group4, group3);
		groupsManager.createGroupUnion(sess, group6, group4);
		groupsManager.createGroupUnion(sess, group4, group5);
		groupsManager.createGroupUnion(sess, group2, group7);

		assertTrue(groupsManager.getGroupMembers(sess, group).size() == 4);
		assertTrue(groupsManager.getGroupMembers(sess, group2).size() == 4);
		assertTrue(groupsManager.getGroupMembers(sess, group3).size() == 1);
		assertTrue(groupsManager.getGroupMembers(sess, group4).size() == 3);
		assertTrue(groupsManager.getGroupMembers(sess, group5).size() == 1);
		assertTrue(groupsManager.getGroupMembers(sess, group6).size() == 3);
		assertTrue(groupsManager.getGroupMembers(sess, group7).size() == 1);

		groupsManager.deleteGroup(sess, group, true);

		assertTrue(groupsManager.getGroupMembers(sess, group5).size() == 1);
		assertTrue(groupsManager.getGroupMembers(sess, group6).size() == 0);
		assertTrue(groupsManager.getGroupMembers(sess, group7).size() == 1);
	}

	@Test
	public void addAndRemoveMemberInGroupWithUnion() throws Exception {
		System.out.println("GroupsManager.addAndRemoveMemberInGroupWithUnion");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, group, group2);
		groupsManager.createGroup(sess, vo, group3);

		groupsManager.createGroupUnion(sess, group2, group3);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group3, member);

		assertTrue(groupsManager.getGroupMembers(sess, group).size() == 1);
		assertTrue(groupsManager.getGroupMembers(sess, group2).size() == 1);
		assertTrue(groupsManager.getGroupMembers(sess, group3).size() == 1);
		assertEquals(groupsManager.getGroupMembers(sess, group3).get(0).getId(), member.getId());

		groupsManager.removeMember(sess, group3, member);

		assertTrue(groupsManager.getGroupMembers(sess, group3).size() == 0);
		assertTrue(groupsManager.getGroupMembers(sess, group2).size() == 0);
		assertTrue(groupsManager.getGroupMembers(sess, group).size() == 0);
	}

	@Test
	public void createGroupUnion() throws Exception {
		System.out.println("GroupsManager.createGroupUnion");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group2, member);

		groupsManager.createGroupUnion(sess, group, group2);

		assertTrue(groupsManager.getGroupMembers(sess, group).size() == 1);
		Member returnMember = groupsManager.getGroupMembers(sess, group).get(0);
		assertEquals(returnMember.getMembershipType(), MembershipType.INDIRECT);
		assertEquals(returnMember.getSourceGroupId(), Integer.valueOf(group2.getId()));

		assertTrue(groupsManagerBl.getGroupUnions(sess, group, false).size() == 1);
		assertTrue(groupsManagerBl.getGroupUnions(sess, group2, true).size() == 1);
		assertTrue(groupsManagerBl.getGroupUnions(sess, group, false).get(0).getId() == group2.getId());
		assertTrue(groupsManagerBl.getGroupUnions(sess, group2, true).get(0).getId() == group.getId());
	}

	@Test
	public void removeGroupUnion() throws Exception {
		System.out.println("GroupsManager.removeGroupUnion");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group2, member);

		groupsManager.createGroupUnion(sess, group, group2);

		assertTrue(groupsManager.getGroupMembers(sess, group).size() == 1);

		groupsManager.removeGroupUnion(sess, group, group2);

		assertTrue(groupsManager.getGroupMembers(sess, group).size() == 0);
		assertTrue(groupsManagerBl.getGroupUnions(sess, group, false).size() == 0);
		assertTrue(groupsManagerBl.getGroupUnions(sess, group2, true).size() == 0);
	}

	@Test(expected=GroupRelationAlreadyExists.class)
	public void createGroupUnionWhenUnionAlreadyExists() throws Exception {
		System.out.println("GroupsManager.createGroupUnionWhenUnionAlreadyExists");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.createGroupUnion(sess, group, group2);
		groupsManager.createGroupUnion(sess, group, group2);
	}

	@Test(expected=GroupRelationNotAllowed.class)
	public void createGroupRelationOnSameGroup() throws Exception {
		System.out.println("GroupsManager.createGroupUnionOnSameGroup");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);

		groupsManager.createGroupUnion(sess, group, group);
	}

	@Test(expected=GroupRelationDoesNotExist.class)
	public void removeGroupUnionThatDoesNotExist() throws Exception {
		System.out.println("GroupsManager.removeGroupUnionThatDoesNotExist");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.removeGroupUnion(sess, group, group2);
	}

	@Test(expected = GroupRelationNotAllowed.class)
	public void createGroupCycle() throws Exception {
		System.out.println("GroupsManager.createGroupCycle");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);
		groupsManager.createGroup(sess, vo, group3);
		groupsManager.createGroup(sess, vo, group4);

		groupsManager.createGroupUnion(sess, group, group2);
		groupsManager.createGroupUnion(sess, group2, group3);
		groupsManager.createGroupUnion(sess, group3, group);
	}

	@Test(expected = GroupRelationNotAllowed.class)
	public void createHierarchicalGroupCycle() throws Exception {
		System.out.println("GroupsManager.createHierarchicalGroupCycle");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, group, group2);
		groupsManager.createGroup(sess, group2, group3);
		groupsManager.createGroup(sess, group3, group4);

		groupsManager.createGroupUnion(sess, group3, group);
	}

	@Test(expected = GroupNotExistsException.class)
	public void createUnionWhenGroupNotExists() throws Exception {
		System.out.println("GroupsManager.createUnionWhenGroupNotExists");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);

		groupsManager.createGroupUnion(sess, group, group2);
	}

	@Test(expected = GroupRelationDoesNotExist.class)
	public void deleteUnionWithSwitchedGroups() throws Exception {
		System.out.println("GroupsManager.deleteUnionWithSwitchedGroups");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, vo, group2);

		groupsManager.createGroupUnion(sess, group, group2);

		groupsManager.removeGroupUnion(sess, group2, group);
	}

	@Test(expected = GroupRelationCannotBeRemoved.class)
	public void deleteUnionBetweenGroupsInHierarchy() throws Exception {
		System.out.println("GroupsManager.deleteUnionBetweenGroupsInHierarchy");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, group, group2);

		groupsManager.removeGroupUnion(sess, group, group2);
	}

	@Test(expected = GroupRelationAlreadyExists.class)
	public void createUnionBetweenGroupsInHierarchy() throws Exception {
		System.out.println("GroupsManager.createUnionBetweenGroupsInHierarchy");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, group, group2);

		groupsManager.createGroupUnion(sess, group, group2);
	}

	@Test
	public void transitiveGroupMembershipCheck() throws Exception {
		System.out.println(CLASS_NAME + "transitiveGroupMembershipCheck");

		Vo newVo = new Vo(0, "voForGroupMembershipTransitiveCheck", "voForGroupMembership");
		newVo = perun.getVosManagerBl().createVo(sess, newVo);
		List<Group> groups = setUpGroupsWithSubgroups(newVo);

		Member member = setUpMember(newVo);

		Group topLevel = null;
		Group secondLevel = null;
		Group thirdLevel = null;
		Group fourthLevel = null;

		for (Group group : groups) {
			if (Objects.equals(group.getName(), "D")) {
				topLevel = group;
				//perun.getGroupsManager().addMember(sess, group, member);
				//System.out.println(perun.getGroupsManagerBl().getResultGroups(sess, group.getId()));
			} else if (Objects.equals(group.getName(), "D:C")) {
				//perun.getGroupsManager().addMember(sess, group, member);
				secondLevel = group;
				//System.out.println(perun.getGroupsManagerBl().getResultGroups(sess, group.getId()));
			} else if (Objects.equals(group.getName(), "D:C:E")) {
				//perun.getGroupsManager().addMember(sess, group, member);
				thirdLevel = group;
				//System.out.println(perun.getGroupsManagerBl().getResultGroups(sess, group.getId()));
			} else if (Objects.equals(group.getName(), "D:C:E:F")) {
				fourthLevel = group;
				//perun.getGroupsManager().addMember(sess, group, member);
				//System.out.println(perun.getGroupsManagerBl().getResultGroups(sess, group.getId()));
			}
		}

		perun.getGroupsManager().addMember(sess, secondLevel, member);
		perun.getGroupsManager().addMember(sess, fourthLevel, member);

		List<Member> topMembers = perun.getGroupsManager().getGroupMembers(sess, topLevel);
		List<Member> secondMembers = perun.getGroupsManager().getGroupMembers(sess, secondLevel);
		List<Member> thirdMembers = perun.getGroupsManager().getGroupMembers(sess, thirdLevel);
		List<Member> fourthMembers = perun.getGroupsManager().getGroupMembers(sess, fourthLevel);

		assertTrue(topMembers != null);
		assertTrue(secondMembers != null);
		assertTrue(thirdMembers != null);
		assertTrue(fourthMembers != null);

		assertTrue(topMembers.size() == 1);
		assertTrue(secondMembers.size() == 1);
		assertTrue(thirdMembers.size() == 1);
		assertTrue(fourthMembers.size() == 1);

		assertTrue(topMembers.get(0).getMembershipType().equals(MembershipType.INDIRECT));
		assertTrue(secondMembers.get(0).getMembershipType().equals(MembershipType.DIRECT));
		assertTrue(thirdMembers.get(0).getMembershipType().equals(MembershipType.INDIRECT));
		assertTrue(fourthMembers.get(0).getMembershipType().equals(MembershipType.DIRECT));

		perun.getGroupsManager().removeMember(sess, secondLevel, member);

		topMembers = perun.getGroupsManager().getGroupMembers(sess, topLevel);
		secondMembers = perun.getGroupsManager().getGroupMembers(sess, secondLevel);
		thirdMembers = perun.getGroupsManager().getGroupMembers(sess, thirdLevel);
		fourthMembers = perun.getGroupsManager().getGroupMembers(sess, fourthLevel);

		assertTrue(topMembers.size() == 1);
		assertTrue(secondMembers.size() == 1);
		assertTrue(thirdMembers.size() == 1);
		assertTrue(fourthMembers.size() == 1);

		assertTrue(topMembers.get(0).getMembershipType().equals(MembershipType.INDIRECT));
		assertTrue(secondMembers.get(0).getMembershipType().equals(MembershipType.INDIRECT));
		assertTrue(thirdMembers.get(0).getMembershipType().equals(MembershipType.INDIRECT));
		assertTrue(fourthMembers.get(0).getMembershipType().equals(MembershipType.DIRECT));

		perun.getGroupsManager().addMember(sess, secondLevel, member);
		perun.getGroupsManager().removeMember(sess, fourthLevel, member);

		topMembers = perun.getGroupsManager().getGroupMembers(sess, topLevel);
		secondMembers = perun.getGroupsManager().getGroupMembers(sess, secondLevel);
		thirdMembers = perun.getGroupsManager().getGroupMembers(sess, thirdLevel);
		fourthMembers = perun.getGroupsManager().getGroupMembers(sess, fourthLevel);

		assertTrue(topMembers.size() == 1);
		assertTrue(secondMembers.size() == 1);
		assertTrue(thirdMembers.size() == 0);
		assertTrue(fourthMembers.size() == 0);

		assertTrue(topMembers.get(0).getMembershipType().equals(MembershipType.INDIRECT));
		assertTrue(secondMembers.get(0).getMembershipType().equals(MembershipType.DIRECT));

		perun.getGroupsManager().addMember(sess, topLevel, member);

		topMembers = perun.getGroupsManager().getGroupMembers(sess, topLevel);
		secondMembers = perun.getGroupsManager().getGroupMembers(sess, secondLevel);
		thirdMembers = perun.getGroupsManager().getGroupMembers(sess, thirdLevel);
		fourthMembers = perun.getGroupsManager().getGroupMembers(sess, fourthLevel);

		assertTrue(topMembers.size() == 1);
		assertTrue(secondMembers.size() == 1);
		assertTrue(thirdMembers.size() == 0);
		assertTrue(fourthMembers.size() == 0);

		assertTrue(topMembers.get(0).getMembershipType().equals(MembershipType.DIRECT));
		assertTrue(secondMembers.get(0).getMembershipType().equals(MembershipType.DIRECT));

		perun.getGroupsManager().removeMember(sess, secondLevel, member);

		topMembers = perun.getGroupsManager().getGroupMembers(sess, topLevel);
		secondMembers = perun.getGroupsManager().getGroupMembers(sess, secondLevel);
		thirdMembers = perun.getGroupsManager().getGroupMembers(sess, thirdLevel);
		fourthMembers = perun.getGroupsManager().getGroupMembers(sess, fourthLevel);

		assertTrue(topMembers.size() == 1);
		assertTrue(secondMembers.size() == 0);
		assertTrue(thirdMembers.size() == 0);
		assertTrue(fourthMembers.size() == 0);

		assertTrue(topMembers.get(0).getMembershipType().equals(MembershipType.DIRECT));

		perun.getGroupsManager().addMember(sess, fourthLevel, member);

		topMembers = perun.getGroupsManager().getGroupMembers(sess, topLevel);
		secondMembers = perun.getGroupsManager().getGroupMembers(sess, secondLevel);
		thirdMembers = perun.getGroupsManager().getGroupMembers(sess, thirdLevel);
		fourthMembers = perun.getGroupsManager().getGroupMembers(sess, fourthLevel);

		assertTrue(topMembers.size() == 1);
		assertTrue(secondMembers.size() == 1);
		assertTrue(thirdMembers.size() == 1);
		assertTrue(fourthMembers.size() == 1);

		assertTrue(topMembers.get(0).getMembershipType().equals(MembershipType.DIRECT));
		assertTrue(secondMembers.get(0).getMembershipType().equals(MembershipType.INDIRECT));
		assertTrue(thirdMembers.get(0).getMembershipType().equals(MembershipType.INDIRECT));
		assertTrue(fourthMembers.get(0).getMembershipType().equals(MembershipType.DIRECT));

	}

	@Test
	public void getGroupUnions() throws Exception {
		System.out.println(CLASS_NAME + "getGroupUnions");

		vo = setUpVo();
		groupsManager.createGroup(sess, vo, group);
		groupsManager.createGroup(sess, group, group2);
		groupsManager.createGroup(sess, vo, group3);
		groupsManager.createGroup(sess, vo, group4);
		groupsManager.createGroup(sess, group4, group5);

		groupsManager.createGroupUnion(sess, group3, group);
		groupsManager.createGroupUnion(sess, group3, group2);
		groupsManager.createGroupUnion(sess, group4, group3);
		groupsManager.createGroupUnion(sess, group5, group3);

		assertEquals("Wrong number of operand groups.", 2, groupsManagerBl.getGroupUnions(sess, group3, false).size());
		assertEquals("Wrong number of result groups.", 2, groupsManagerBl.getGroupUnions(sess, group3, true).size());
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
		assertEquals("both groups should be the same",returnedGroup,group);

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
		groupsManagerBl.createGroup(sess, createdVo, parentGroup);
		groupsManagerBl.createGroup(sess, parentGroup, subgroup1);
		groupsManagerBl.createGroup(sess, parentGroup, subgroup2);
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
		System.out.println(CLASS_NAME + "getGroupToSynchronize");

		vo = setUpVo();
		setUpGroup(vo);
		ExtSource es = perun.getExtSourcesManagerBl().createExtSource(sess, extSource, null);
		perun.getExtSourcesManagerBl().addExtSource(sess, vo, es);
		perun.getGroupsManager().createGroup(sess, vo, group2);

		Attribute synchroAttr1 = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME));
		synchroAttr1.setValue("5");
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr1);
		perun.getAttributesManager().setAttribute(sess, group2, synchroAttr1);

		Attribute synchroAttr2 = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPEXTSOURCE_ATTRNAME));
		synchroAttr2.setValue(es.getName());
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr2);
		perun.getAttributesManager().setAttribute(sess, group2, synchroAttr2);

		Attribute synchroAttr3 = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME));
		synchroAttr3.setValue("testVal");
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr3);
		perun.getAttributesManager().setAttribute(sess, group2, synchroAttr3);

		Attribute synchroAttr4 = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME));
		synchroAttr4.setValue("true");
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr4);
		perun.getAttributesManager().setAttribute(sess, group2, synchroAttr4);

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
		assertEquals("Both groups should be the same",returnedGroup,group);

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

		assertEquals("Both groups should be the same",returnedGroup,group);
		assertEquals("Both groups should be the same",returnedSubGroup,subGroup);
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

		List<Member> members = groupsManager.getGroupMembers(sess, group);
		assertTrue("our member should be in group",members.contains(member));

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

		List<Member> members = groupsManager.getGroupMembers(sess, group);
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
	public void getGroupMembers() throws Exception {
		System.out.println(CLASS_NAME + "getGroupMembers");

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
	public void getGroupMembersWhenGroupNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getGroupMembersWhenGroupNotExist");

		groupsManager.getGroupMembers(sess, new Group());

	}

	@Test (expected=GroupNotExistsException.class)
	public void getGroupMembersPageWhenGroupNotExist() throws Exception {
		System.out.println(CLASS_NAME + "getGroupMembersPageWhenGroupNotExist");

		groupsManager.getGroupMembers(sess, new Group());

	}

	@Test
	public void getGroupMembersCount() throws Exception {
		System.out.println(CLASS_NAME + "getGroupMembersCount");

		vo = setUpVo();
		setUpGroup(vo);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);

		int count = groupsManager.getGroupMembersCount(sess, group);
		assertTrue(count == 1);

	}

	@Test (expected=GroupNotExistsException.class)
	public void getGroupMembersCountWhenGroupNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getGroupMembersCountWhenGroupNotExists");

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
		candidate.setAttributes(new HashMap<>());

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
	public void testInteractionBetweenDirectAndIndirectMembershipTypeInGroups() throws Exception {
		System.out.println(CLASS_NAME + "testInteractionBetweenDirectAndIndirectMembershipTypeInGroups");

		vo = setUpVo();
		Group topGroup = this.groupsManager.createGroup(sess, vo, group);
		Group firstLayerSubGroup = this.groupsManager.createGroup(sess, group, group2);
		Group secondLayerSubGroup = this.groupsManager.createGroup(sess, group2, group3);

		List<Member> members = new ArrayList<>();
		members.add(setUpMemberWithDifferentParam(vo, 1));

		this.groupsManager.addMember(sess, group2, members.get(0));
		this.groupsManager.addMember(sess, group3, members.get(0));
		this.groupsManager.removeMember(sess, group2, members.get(0));

		List<Member> membersOfTopGroup = this.groupsManager.getGroupMembers(sess, group);
		List<Member> membersOfFirstLayerSubGroup = this.groupsManager.getGroupMembers(sess, group2);
		List<Member> membersOfSecondLayerSubGroup = this.groupsManager.getGroupMembers(sess, group3);

		assertTrue(membersOfTopGroup.contains(members.get(0)));
		assertEquals(membersOfTopGroup.get(0).getMembershipType(), MembershipType.INDIRECT);
		assertTrue(membersOfFirstLayerSubGroup.contains(members.get(0)));
		assertEquals(membersOfFirstLayerSubGroup.get(0).getMembershipType(), MembershipType.INDIRECT);
		assertTrue(membersOfSecondLayerSubGroup.contains(members.get(0)));
		assertEquals(membersOfSecondLayerSubGroup.get(0).getMembershipType(), MembershipType.DIRECT);
	}

	@Test
	public void removeMemberFromSubGroup() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberFromSubGroup");
		vo = setUpVo();
		Group topGroup = this.groupsManager.createGroup(sess, vo, group);
		Group subGroup = this.groupsManager.createGroup(sess, group, group2);

		List<Member> members = new ArrayList<>();
		members.add(setUpMemberWithDifferentParam(vo, 1));

		this.groupsManager.addMember(sess, group, members.get(0));
		this.groupsManager.addMember(sess, group2, members.get(0));
		this.groupsManager.removeMember(sess, group, members.get(0));

		List<Member> membersOfTopGroup = this.groupsManager.getGroupMembers(sess, group);
		List<Member> membersOfSubGroup = this.groupsManager.getGroupMembers(sess, group2);

		assertTrue(membersOfTopGroup.contains(members.get(0)));
		assertEquals(membersOfTopGroup.get(0).getMembershipType(), MembershipType.INDIRECT);
		assertTrue(membersOfSubGroup.contains(members.get(0)));
		assertEquals(membersOfSubGroup.get(0).getMembershipType(), MembershipType.DIRECT);
	}

	@Test
	public void addDirectMemberToHierarchy() throws Exception {
		System.out.println(CLASS_NAME + "addDirectMemberToHierarchy");
		vo = setUpVo();
		Group topGroup = this.groupsManager.createGroup(sess, vo, group);
		Group subGroup = this.groupsManager.createGroup(sess, group, group2);
		Group subSubGroup = this.groupsManager.createGroup(sess, group2, group3);

		List<Member> members = new ArrayList<>();
		members.add(setUpMemberWithDifferentParam(vo, 1));

		this.groupsManager.addMember(sess, group3, members.get(0));
		this.groupsManager.addMember(sess, group2, members.get(0));

		List<Member> membersOfTopGroup = this.groupsManager.getGroupMembers(sess, group);
		List<Member> membersOfSubGroup = this.groupsManager.getGroupMembers(sess, group2);
		List<Member> membersOfSubSubGroup = this.groupsManager.getGroupMembers(sess, group3);

		assertTrue(membersOfTopGroup.contains(members.get(0)));
		assertEquals(membersOfTopGroup.size(), 1);
		assertEquals(membersOfTopGroup.get(0).getMembershipType(), MembershipType.INDIRECT);
		assertTrue(membersOfSubGroup.contains(members.get(0)));
		assertEquals(membersOfSubGroup.size(), 1);
		assertEquals(membersOfSubGroup.get(0).getMembershipType(), MembershipType.DIRECT);
		assertTrue(membersOfSubSubGroup.contains(members.get(0)));
		assertEquals(membersOfSubSubGroup.size(), 1);
		assertEquals(membersOfSubSubGroup.get(0).getMembershipType(), MembershipType.DIRECT);
	}

	@Test
	public void getGroupCountInBiggerGroupStructure() throws Exception{
		System.out.println(CLASS_NAME + "getGroupCountInBiggerGroupStructure");

		vo = setUpVo();
		this.groupsManager.createGroup(sess, vo, group);
		this.groupsManager.createGroup(sess, group, group2);
		this.groupsManager.createGroup(sess, group, group21);
		this.groupsManager.createGroup(sess, group2, group3);

		List<Member> members = new ArrayList<>();
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


	@Test ()
	public void getMembersMembershipType() throws Exception{
		System.out.println(CLASS_NAME + "getMembersMembershipType");
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
		System.out.println("GroupsManagerBl.convertGroupToRichGroupWithAttributes");

		vo = setUpVo();
		attributesList = setUpGroupAttributes();
		setUpGroup(vo);
		attributesManager.setAttributes(sess, group, attributesList);

		RichGroup richGroup = new RichGroup(group, attributesManager.getAttributes(sess, group));
		assertEquals("Both rich groups should be same", richGroup, groupsManagerBl.convertGroupToRichGroupWithAttributes(sess, group));
	}

	@Test
	public void convertGroupToRichGroupWithAlsoGroupResourceAttributesTest() throws Exception {
		System.out.println("GroupsManagerBl.convertGroupToRichGroupWithAlsoGroupResourceAttributesTest");

		vo = setUpVo();
		setUpGroup(vo);
		Facility facility = setUpFacility();
		Resource resource = setUpResource(vo, facility);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);
		List<Attribute> groupAttributes = setUpGroupAttributes();
		List<Attribute> groupResourceAttributes = setUpGroupResourceAttribute();
		List<Attribute> allAttributes = new ArrayList<>();
		allAttributes.addAll(groupAttributes);
		allAttributes.addAll(groupResourceAttributes);

		attributesManager.setAttributes(sess, resource, group, allAttributes, true);
		RichGroup richGroup = new RichGroup(group, allAttributes);

		List<Group> groupList = new ArrayList<>();
		groupList.add(group);

		List<String> attrNames = new ArrayList<>();
		for(Attribute attr: allAttributes) attrNames.add(attr.getName());

		List<RichGroup> returnedGroups = groupsManagerBl.convertGroupsToRichGroupsWithAttributes(sess, resource, groupList, attrNames);
		RichGroup returnedGroup = returnedGroups.get(0);

		assertEquals("Both rich groups should be the same", richGroup, returnedGroup);
	}

	@Test
	public void getRichGroupsWithAttributesAssignedToResource() throws Exception {
		System.out.println("GroupsManagerBl.getRichGroupsAssignedToResourceWithAttributesByNames");

		vo = setUpVo();
		List<Group> groups = setUpGroups(vo);
		Facility facility = setUpFacility();
		Resource resource = setUpResource(vo, facility);
		for(Group group: groups) perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource);

		List<Attribute> groupAttributes = setUpGroupAttributes();
		List<Attribute> groupResourceAttributes = setUpGroupResourceAttribute();
		List<Attribute> allAttributes = new ArrayList<>();
		allAttributes.addAll(groupAttributes);
		allAttributes.addAll(groupResourceAttributes);
		List<String> attrNames = new ArrayList<>();
		for(Attribute attr: allAttributes) attrNames.add(attr.getName());

		for(Group group: groups) attributesManager.setAttributes(sess, resource, group, allAttributes, true);
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			RichGroup richGroup = new RichGroup(group, allAttributes);
			richGroups.add(richGroup);
		}

		List<RichGroup> returnedGroups = groupsManager.getRichGroupsAssignedToResourceWithAttributesByNames(sess, resource, attrNames);

		assertTrue(returnedGroups.size() == groups.size());
		assertTrue(returnedGroups.containsAll(richGroups));
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

	@Test(expected = ExternallyManagedException.class)
	public void addMemberToSynchronizedGroup() throws Exception {
		System.out.println(CLASS_NAME + "addMemberToSynchronizedGroup");

		vo = setUpVo();
		setUpGroup(vo);

		Attribute query = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME));
		query.setValue("fakeValue");
		perun.getAttributesManager().setAttribute(sess, group, query);

		Attribute interval = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME));
		interval.setValue("1");
		perun.getAttributesManager().setAttribute(sess, group, interval);

		perun.getExtSourcesManager().createExtSource(sess, extSource, new HashMap<>());
		perun.getExtSourcesManager().addExtSource(sess, vo, extSource);
		perun.getExtSourcesManager().addExtSource(sess, group, extSource);

		Attribute extSource = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPEXTSOURCE_ATTRNAME));
		extSource.setValue("testExtSource");
		perun.getAttributesManager().setAttribute(sess, group, extSource);

		Attribute synchroAttr = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME));
		synchroAttr.setValue("true");
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);
	}

	@Test
	public void addAndRemoveMemberInNonSynchronizedGroup() throws Exception {
		System.out.println(CLASS_NAME + "addAndRemoveMemberInNonSynchronizedGroup");

		vo = setUpVo();
		setUpGroup(vo);

		Attribute synchroAttr = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME));
		synchroAttr.setValue("false");
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);
		List<Member> members = groupsManager.getGroupMembers(sess, group);
		assertTrue("List of members should contain member", members.contains(member));

		groupsManager.removeMember(sess, group, member);
		members = groupsManager.getGroupMembers(sess, group);
		assertTrue("List of members should be empty", members.isEmpty());
	}

	@Test(expected = ExternallyManagedException.class)
	public void removeMemberInSynchronizedGroup() throws Exception {
		System.out.println(CLASS_NAME + "removeMemberInSynchronizedGroup");

		vo = setUpVo();
		setUpGroup(vo);

		Attribute query = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME));
		query.setValue("fakeValue");
		perun.getAttributesManager().setAttribute(sess, group, query);

		Attribute interval = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME));
		interval.setValue("1");
		perun.getAttributesManager().setAttribute(sess, group, interval);

		perun.getExtSourcesManager().createExtSource(sess, extSource, new HashMap<>());
		perun.getExtSourcesManager().addExtSource(sess, vo, extSource);
		perun.getExtSourcesManager().addExtSource(sess, group, extSource);

		Attribute extSource = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPEXTSOURCE_ATTRNAME));
		extSource.setValue("testExtSource");
		perun.getAttributesManager().setAttribute(sess, group, extSource);

		Attribute synchroAttr = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME));
		synchroAttr.setValue("false");
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr);

		Member member = setUpMember(vo);
		groupsManager.addMember(sess, group, member);

		synchroAttr.setValue("true");
		perun.getAttributesManager().setAttribute(sess, group, synchroAttr);
		groupsManager.removeMember(sess, group, member);
	}

	@Test
	public void getAssignedGroupsToFacility() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedGroupsToFacility");

		// Test that new method returns same data as old behavior
		vo = setUpVo();

		Vo vo2 = new Vo(0, "facilityTestVo002", "facilityTestVo002");
		vo2 = perun.getVosManagerBl().createVo(sess, vo2);

		Facility facility = new Facility(0, "groupsTestFacility01", "groupsTestFacility01");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		Resource resource1 = setUpResource(vo, facility);
		Resource resource2 = setUpResource(vo2, facility);

		Group group11 = new Group("Group11", "testGroup1");
		Group group12 = new Group("Group12", "testGroup2");
		Group group21 = new Group("Group21", "testGroup3");
		Group group22 = new Group("Group22", "testGroup4");

		group11 = perun.getGroupsManager().createGroup(sess, vo, group11);
		group12 = perun.getGroupsManager().createGroup(sess, vo, group12);
		group21 = perun.getGroupsManager().createGroup(sess, vo2, group21);
		group22 = perun.getGroupsManager().createGroup(sess, vo2, group22);

		perun.getResourcesManager().assignGroupToResource(sess, group11, resource1);
		perun.getResourcesManager().assignGroupToResource(sess, group21, resource2);

		// test new way - single select
		List<Group> groups = perun.getGroupsManagerBl().getAssignedGroupsToFacility(sess, facility);
		Assert.notNull(groups);
		assertTrue(groups.size() == 2);
		assertTrue(groups.contains(group11));
		assertTrue(groups.contains(group21));
		assertTrue(!groups.contains(group12));
		assertTrue(!groups.contains(group22));

		// test old way - iterate over resources
		List<Resource> resources = perun.getFacilitiesManager().getAssignedResources(sess, facility);
		List<Group> oldGroups = new ArrayList<>();
		for (Resource r : resources) {
			oldGroups.addAll(perun.getResourcesManager().getAssignedGroups(sess, r));
		}
		Assert.notNull(oldGroups);
		assertTrue(oldGroups.contains(group11));
		assertTrue(oldGroups.contains(group21));
		assertTrue(!oldGroups.contains(group12));
		assertTrue(!oldGroups.contains(group22));

		assertEquals(new HashSet<>(groups), new HashSet<>(oldGroups));

	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveGroupToDiffVo() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupToDiffVo");

		vo = setUpVo();
		Vo newVo = new Vo(0, "GroupManagerTestVo2", "GMTestVo2");
		Vo vo2 = perun.getVosManager().createVo(sess, newVo);

		perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().createGroup(sess, vo2, group2);

		groupsManagerBl.moveGroup(sess , group, group2);

	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveGroupAlreadyInDestination() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupAlreadyInDestination");

		vo = setUpVo();
		perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().createGroup(sess, group, group2);

		groupsManagerBl.moveGroup(sess , group, group2);

	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveGroupAlreadyTopLevel() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupAlreadyTopLevel");

		vo = setUpVo();
		perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().createGroup(sess, group, group2);

		groupsManagerBl.moveGroup(sess , null, group);

	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveGroupSameGroups() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupSameGroups");

		vo = setUpVo();
		perun.getGroupsManager().createGroup(sess, vo, group2);

		groupsManagerBl.moveGroup(sess , group2, group2);

	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveGroupNullMoving() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupNullMoving");

		vo = setUpVo();
		perun.getGroupsManager().createGroup(sess, vo, group);

		groupsManagerBl.moveGroup(sess , group, null);

	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveGroupToSubGroup() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupToSubGroup");

		vo = setUpVo();
		perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().createGroup(sess, group, group2);

		groupsManagerBl.moveGroup(sess , group2, group);

	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveGroupCreateCycle() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupCreateCycle");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().createGroup(sess, vo, group2);
		perun.getGroupsManager().createGroup(sess, vo, group3);

		perun.getGroupsManager().createGroupUnion(sess, group2, group);
		perun.getGroupsManager().createGroupUnion(sess, group3, group2);

		groupsManagerBl.moveGroup(sess , group, group3);
	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveGroupToMembersGroup() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupToMembersGroup");

		vo = setUpVo();
		perun.getGroupsManager().createGroup(sess, vo, group);

		groupsManagerBl.moveGroup(sess , perun.getGroupsManager().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP), group);
	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveMembersGroup() throws Exception {
		System.out.println(CLASS_NAME + "moveMembersGroup");

		vo = setUpVo();
		perun.getGroupsManager().createGroup(sess, vo, group);

		groupsManagerBl.moveGroup(sess, group, perun.getGroupsManager().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP));
	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveGroupUniqueName() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupUniqueName");

		vo = setUpVo();

		Group groupSameNameAs4 = new Group("GroupsManagerTestGroup4", "testovaci4");
		perun.getGroupsManager().createGroup(sess, vo, groupSameNameAs4);

		perun.getGroupsManager().createGroup(sess, vo, group3);

		perun.getGroupsManager().createGroup(sess, group3, group4);
		perun.getGroupsManager().createGroup(sess, group4, group5);

		groupsManagerBl.moveGroup(sess, null, group4);
	}

	@Test (expected = GroupMoveNotAllowedException.class)
	public void moveGroupUnionExists() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupUnionExists");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().createGroup(sess, vo, group2);

		perun.getGroupsManager().createGroupUnion(sess, group, group2);

		groupsManagerBl.moveGroup(sess, group, group2);
	}

	@Test
	public void moveGroup() throws Exception {
		System.out.println(CLASS_NAME + "moveGroup");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().createGroup(sess, group, group2);

		perun.getGroupsManager().createGroup(sess, vo, group3);
		perun.getGroupsManager().createGroup(sess, group3, group4);
		perun.getGroupsManager().createGroup(sess, group4, group5);
		perun.getGroupsManager().createGroup(sess, group4, group6);
		perun.getGroupsManager().createGroup(sess, group5, group7);
		perun.getGroupsManager().createGroup(sess, group6, group8);

		groupsManagerBl.moveGroup(sess, group2, group4);

		assertTrue("group4 has different parent id than group2 id", group2.getId() == groupsManager.getGroupById(sess, group4.getId()).getParentGroupId());

		assertEquals("Group4 has wrong name after moving", "GroupsManagerTestGroup1:GroupsManagerTestGroup2:GroupsManagerTestGroup4", groupsManager.getGroupById(sess, group4.getId()).getName());
		assertEquals("Group5 has wrong name after moving", "GroupsManagerTestGroup1:GroupsManagerTestGroup2:GroupsManagerTestGroup4:GroupsManagerTestGroup5", groupsManager.getGroupById(sess, group5.getId()).getName());
		assertEquals("Group6 has wrong name after moving", "GroupsManagerTestGroup1:GroupsManagerTestGroup2:GroupsManagerTestGroup4:GroupsManagerTestGroup6", groupsManager.getGroupById(sess, group6.getId()).getName());
		assertEquals("Group7 has wrong name after moving", "GroupsManagerTestGroup1:GroupsManagerTestGroup2:GroupsManagerTestGroup4:GroupsManagerTestGroup5:GroupsManagerTestGroup7", groupsManager.getGroupById(sess, group7.getId()).getName());
		assertEquals("Group8 has wrong name after moving", "GroupsManagerTestGroup1:GroupsManagerTestGroup2:GroupsManagerTestGroup4:GroupsManagerTestGroup6:GroupsManagerTestGroup8", groupsManager.getGroupById(sess, group8.getId()).getName());

		List<Group> subGroups = groupsManager.getAllSubGroups(sess, group3);
		assertEquals("Group3 cannot has subGroups after moving group4 structure", 0, subGroups.size());

		subGroups = groupsManager.getAllSubGroups(sess, group2);
		assertEquals("Group2 subGroups size has to be 5", 5, subGroups.size());
	}

	@Test
	public void moveGroupNullDestination() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupNullDestination");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group3);
		perun.getGroupsManager().createGroup(sess, group3, group4);
		perun.getGroupsManager().createGroup(sess, group4, group5);
		perun.getGroupsManager().createGroup(sess, group4, group6);
		perun.getGroupsManager().createGroup(sess, group5, group7);
		perun.getGroupsManager().createGroup(sess, group6, group8);

		groupsManagerBl.moveGroup(sess, null, group4);

		assertTrue("group4 doesn't have null parent id ", groupsManager.getGroupById(sess, group4.getId()).getParentGroupId() == null);

		assertEquals("Group4 has wrong name after moving", "GroupsManagerTestGroup4", groupsManager.getGroupById(sess, group4.getId()).getName());
		assertEquals("Group5 has wrong name after moving", "GroupsManagerTestGroup4:GroupsManagerTestGroup5", groupsManager.getGroupById(sess, group5.getId()).getName());
		assertEquals("Group6 has wrong name after moving", "GroupsManagerTestGroup4:GroupsManagerTestGroup6", groupsManager.getGroupById(sess, group6.getId()).getName());
		assertEquals("Group7 has wrong name after moving", "GroupsManagerTestGroup4:GroupsManagerTestGroup5:GroupsManagerTestGroup7", groupsManager.getGroupById(sess, group7.getId()).getName());
		assertEquals("Group8 has wrong name after moving", "GroupsManagerTestGroup4:GroupsManagerTestGroup6:GroupsManagerTestGroup8", groupsManager.getGroupById(sess, group8.getId()).getName());

		List<Group> subGroups = groupsManager.getAllSubGroups(sess, group3);
		assertEquals("Group3 cannot has subGroups after moving group4 structure", 0, subGroups.size());
	}

	@Test
	public void moveTopLevelGroup() throws Exception {
		System.out.println(CLASS_NAME + "moveTopLevelGroup");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group3);
		perun.getGroupsManager().createGroup(sess, group3, group4);
		perun.getGroupsManager().createGroup(sess, vo, group5);

		groupsManagerBl.moveGroup(sess, group5, group3);

		assertEquals("Group4 has wrong name after moving", "GroupsManagerTestGroup5:GroupsManagerTestGroup3:GroupsManagerTestGroup4", groupsManager.getGroupById(sess, group4.getId()).getName());
	}

	@Test
	public void moveGroupIndirectMembers() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupIndirectMembers");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().createGroup(sess, group, group2);

		perun.getGroupsManager().createGroup(sess, vo, group3);
		perun.getGroupsManager().createGroup(sess, group3, group4);
		perun.getGroupsManager().createGroup(sess, group4, group5);
		perun.getGroupsManager().createGroup(sess, group4, group6);
		perun.getGroupsManager().createGroup(sess, group5, group7);
		perun.getGroupsManager().createGroup(sess, group6, group8);

		Member member1 = setUpMember(vo);
		Member member2 = setUpMember(vo);
		Member member3 = setUpMember(vo);
		Member member4 = setUpMember(vo);
		Member member5 = setUpMember(vo);

		groupsManagerBl.addMember(sess, group2, member1);
		groupsManagerBl.addMember(sess, group3, member2);
		groupsManagerBl.addMember(sess, group4, member3);
		groupsManagerBl.addMember(sess, group5, member4);
		groupsManagerBl.addMember(sess, group6, member5);

		groupsManagerBl.moveGroup(sess, group2, group4);

		List<Member> members = groupsManagerBl.getGroupMembers(sess, group3);
		assertEquals("Indirect members have to be moved from group3", 1, members.size());

		members = groupsManagerBl.getGroupMembers(sess, group2);
		assertEquals("Indirect members have to be moved to group2", 4, members.size());

		members = groupsManagerBl.getGroupMembers(sess, group);
		assertEquals("Indirect members have to be moved to group", 4, members.size());

	}

	@Test
	public void moveTopLevelGroupIndirectMembers() throws Exception {
		System.out.println(CLASS_NAME + "moveTopLevelGroupIndirectMembers");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().createGroup(sess, group, group2);

		perun.getGroupsManager().createGroup(sess, vo, group3);
		perun.getGroupsManager().createGroup(sess, group3, group4);

		Member member1 = setUpMember(vo);
		Member member2 = setUpMember(vo);
		Member member3 = setUpMember(vo);

		groupsManagerBl.addMember(sess, group2, member1);
		groupsManagerBl.addMember(sess, group3, member2);
		groupsManagerBl.addMember(sess, group4, member3);

		groupsManagerBl.moveGroup(sess, group2, group3);

		List<Member> members = groupsManagerBl.getGroupMembers(sess, group);
		assertEquals("Indirect members have to be moved to group", 3, members.size());

	}

	@Test
	public void moveGroupIndirectMembersNullDestination() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupIndirectMembersNullDestination");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group3);
		perun.getGroupsManager().createGroup(sess, group3, group4);

		Member member1 = setUpMember(vo);
		Member member2 = setUpMember(vo);
		Member member3 = setUpMember(vo);

		groupsManagerBl.addMember(sess, group3, member1);
		groupsManagerBl.addMember(sess, group4, member2);
		groupsManagerBl.addMember(sess, group4, member3);

		groupsManagerBl.moveGroup(sess, null, group4);

		List<Member> members = groupsManagerBl.getGroupMembers(sess, group3);
		assertEquals("Indirect members have to be removed from group3", 1, members.size());

	}

	@Test
	public void moveGroupCheckRelations() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupCheckRelations");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group);
		perun.getGroupsManager().createGroup(sess, group, group2);

		perun.getGroupsManager().createGroup(sess, vo, group3);
		perun.getGroupsManager().createGroup(sess, group3, group4);
		perun.getGroupsManager().createGroup(sess, group4, group5);
		perun.getGroupsManager().createGroup(sess, group4, group6);
		perun.getGroupsManager().createGroup(sess, group5, group7);
		perun.getGroupsManager().createGroup(sess, group6, group8);

		groupsManagerBl.moveGroup(sess, group2, group4);

		List<Group> relationsGroup2 = groupsManagerBl.getGroupUnions(sess, group2, false);
		List<Group> relationsGroup3 = groupsManagerBl.getGroupUnions(sess, group3, false);
		List<Group> relationsGroup4 = groupsManagerBl.getGroupUnions(sess, group4, true);

		assertTrue("There should be relation between group2 and group4", relationsGroup2.contains(groupsManager.getGroupById(sess, group4.getId())));
		assertTrue("There should not be relation between group3 and group4", !relationsGroup3.contains(groupsManager.getGroupById(sess, group4.getId())));
		assertTrue("There should be reverse relation between group2 and group4", relationsGroup4.contains(groupsManager.getGroupById(sess, group2.getId())));

	}

	@Test
	public void moveGroupCheckRelationsNullDestination() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupCheckRelationsNullDestination");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group3);
		perun.getGroupsManager().createGroup(sess, group3, group4);
		perun.getGroupsManager().createGroup(sess, group4, group5);
		perun.getGroupsManager().createGroup(sess, group4, group6);
		perun.getGroupsManager().createGroup(sess, group5, group7);
		perun.getGroupsManager().createGroup(sess, group6, group8);

		groupsManagerBl.moveGroup(sess, null, group4);

		List<Group> relationsGroup3 = groupsManagerBl.getGroupUnions(sess, group3, false);
		List<Group> relationsGroup4 = groupsManagerBl.getGroupUnions(sess, group4, true);

		assertTrue("There should not be relation between group3 and group4", !relationsGroup3.contains(groupsManager.getGroupById(sess, group4.getId())));
		assertTrue("There should not be relation between group3 and group4", !relationsGroup4.contains(groupsManager.getGroupById(sess, group3.getId())));

	}

	@Test
	public  void moveGroupComplexTest() throws Exception {
		System.out.println(CLASS_NAME + "moveGroupComplexTest");

		vo = setUpVo();

		perun.getGroupsManager().createGroup(sess, vo, group);

		perun.getGroupsManager().createGroup(sess, vo, group6);
		perun.getGroupsManager().createGroup(sess, group6, group7);

		perun.getGroupsManager().createGroup(sess, vo, group2);
		perun.getGroupsManager().createGroup(sess, group2, group3);
		perun.getGroupsManager().createGroup(sess, group2, group4);
		perun.getGroupsManager().createGroup(sess, group4, group5);

		Member member1 = setUpMember(vo);
		Member member2 = setUpMember(vo);
		Member member3 = setUpMember(vo);
		Member member4 = setUpMember(vo);
		Member member5 = setUpMember(vo);
		Member member6 = setUpMember(vo);
		Member member7 = setUpMember(vo);

		groupsManagerBl.addMember(sess, group, member1);
		groupsManagerBl.addMember(sess, group2, member2);
		groupsManagerBl.addMember(sess, group3, member3);
		groupsManagerBl.addMember(sess, group4, member4);
		groupsManagerBl.addMember(sess, group5, member5);
		groupsManagerBl.addMember(sess, group6, member6);
		groupsManagerBl.addMember(sess, group7, member7);

		groupsManagerBl.moveGroup(sess, groupsManager.getGroupById(sess, group3.getId()), groupsManager.getGroupById(sess, group6.getId()));
		groupsManagerBl.moveGroup(sess, groupsManager.getGroupById(sess, group.getId()), groupsManager.getGroupById(sess, group2.getId()));
		groupsManagerBl.moveGroup(sess, groupsManager.getGroupById(sess, group6.getId()), groupsManager.getGroupById(sess, group5.getId()));

		List<Group> relationsGroup4 = groupsManagerBl.getGroupUnions(sess, group4, false);
		List<Group> relationsGroup5 = groupsManagerBl.getGroupUnions(sess, group5, true);
		List<Group> relationsGroup6 = groupsManagerBl.getGroupUnions(sess, group6, false);

		assertTrue("There should not be relation between group4 and group5", !relationsGroup4.contains(groupsManager.getGroupById(sess, group5.getId())));
		assertTrue("There should not be relation between group4 and group5", !relationsGroup5.contains(groupsManager.getGroupById(sess, group4.getId())));
		assertTrue("There should be relation between group5 and group6", relationsGroup5.contains(groupsManager.getGroupById(sess, group6.getId())));
		assertTrue("There should be relation between group5 and group6", relationsGroup6.contains(groupsManager.getGroupById(sess, group5.getId())));
		assertTrue("There should be 7 members in group1", groupsManagerBl.getGroupMembers(sess, group).size() == 7);
		assertTrue("There should be just member4 in group4", groupsManagerBl.getGroupMembers(sess, group4).contains(member4) && groupsManagerBl.getGroupMembers(sess, group4).size() == 1);
		assertEquals("Group5 has wrong name after moving", "GroupsManagerTestGroup1:GroupsManagerTestGroup2:GroupsManagerTestGroup3:GroupsManagerTestGroup6:GroupsManagerTestGroup5", groupsManager.getGroupById(sess, group5.getId()).getName());

	}

	@Test
	public void getGroupDirectMembers() throws Exception {
		System.out.println(CLASS_NAME + "getGroupDirectMembers");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");

		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);
		Member m2 = setUpMemberWithDifferentParam(vo, 1);

		groupsManagerBl.addMember(sess, g1, m1);
		groupsManagerBl.addMember(sess, g2, m2);

		List<Member> foundMembers = groupsManagerBl.getGroupDirectMembers(sess, g1);

		assertEquals("Found more members than expected", foundMembers.size(), 1);
		assertEquals("Found invalid member.", foundMembers.get(0), m1);
	}

	@Test
	public void getGroupDirectRichMembers() throws Exception {
		System.out.println(CLASS_NAME + "getGroupDirectRichMembers");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");

		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);
		Member m2 = setUpMemberWithDifferentParam(vo, 1);

		groupsManagerBl.addMember(sess, g1, m1);
		groupsManagerBl.addMember(sess, g2, m2);

		List<RichMember> foundMembers = groupsManagerBl.getGroupDirectRichMembers(sess, g1);

		RichMember expectedRichMember = perun.getMembersManagerBl()
				.convertMembersToRichMembers(sess, Collections.singletonList(m1)).get(0);

		assertEquals("Found more members than expected", foundMembers.size(), 1);
		assertEquals("Found invalid member.", foundMembers.get(0), expectedRichMember);
	}

	@Test
	public void getActiveGroupMembers() throws Exception {
		System.out.println(CLASS_NAME + "getActiveGroupMembers");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");
		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);
		Member m2 = setUpMemberWithDifferentParam(vo, 1);
		Member m3 = setUpMemberWithDifferentParam(vo, 2);

		groupsManagerBl.addMember(sess, g1, m1);
		groupsManagerBl.addMember(sess, g2, m2);
		groupsManagerBl.addMember(sess, g1, m3);
		groupsManagerBl.addMember(sess, g2, m3);

		groupsManagerBl.expireMemberInGroup(sess, m2, g2);
		groupsManagerBl.expireMemberInGroup(sess, m3, g1);

		List<Member> activeMembers = groupsManagerBl.getActiveGroupMembers(sess, g1);
		assertTrue(activeMembers.contains(m1));
		assertFalse(activeMembers.contains(m2));
		assertTrue(activeMembers.contains(m3));
	}

	@Test
	public void getInactiveGroupMembers() throws Exception {
		System.out.println(CLASS_NAME + "getInactiveGroupMembers");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");
		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);
		Member m2 = setUpMemberWithDifferentParam(vo, 1);
		Member m3 = setUpMemberWithDifferentParam(vo, 2);

		groupsManagerBl.addMember(sess, g1, m1);
		groupsManagerBl.addMember(sess, g2, m2);
		groupsManagerBl.addMember(sess, g1, m3);
		groupsManagerBl.addMember(sess, g2, m3);

		groupsManagerBl.expireMemberInGroup(sess, m2, g2);
		groupsManagerBl.expireMemberInGroup(sess, m3, g1);

		List<Member> inactiveMembers = groupsManagerBl.getInactiveGroupMembers(sess, g1);
		assertFalse(inactiveMembers.contains(m1));
		assertTrue(inactiveMembers.contains(m2));
		assertFalse(inactiveMembers.contains(m3));
	}

	@Test
	public void allMembersEqualsInactivePlusActiveMembersInGroup() throws Exception {
		System.out.println(CLASS_NAME + "allMembersEqualsInactivePlusActiveMembersInGroup");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");
		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);
		Member m2 = setUpMemberWithDifferentParam(vo, 1);
		Member m3 = setUpMemberWithDifferentParam(vo, 2);

		groupsManagerBl.addMember(sess, g1, m1);
		groupsManagerBl.addMember(sess, g2, m2);
		groupsManagerBl.addMember(sess, g1, m3);
		groupsManagerBl.addMember(sess, g2, m3);

		groupsManagerBl.expireMemberInGroup(sess, m2, g2);
		groupsManagerBl.expireMemberInGroup(sess, m3, g1);

		List<Member> inactiveAndActiveMembers = groupsManagerBl.getActiveGroupMembers(sess, g1);
		inactiveAndActiveMembers.addAll(groupsManagerBl.getInactiveGroupMembers(sess, g1));
		List<Member> allMembers = groupsManagerBl.getGroupMembers(sess, g1);

		Collections.sort(allMembers);
		Collections.sort(inactiveAndActiveMembers);

		assertEquals(allMembers, inactiveAndActiveMembers);
	}

	@Test
	public void getValidGroupMembersInGroupAndInVo() throws Exception {
		System.out.println(CLASS_NAME + "getValidGroupMembersInGroupAndInVo");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");
		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);
		Member m2 = setUpMemberWithDifferentParam(vo, 1);
		Member m3 = setUpMemberWithDifferentParam(vo, 2);

		groupsManagerBl.addMember(sess, g1, m1);
		groupsManagerBl.addMember(sess, g2, m2);
		groupsManagerBl.addMember(sess, g1, m3);
		groupsManagerBl.addMember(sess, g2, m3);

		groupsManagerBl.expireMemberInGroup(sess, m2, g2);
		groupsManagerBl.expireMemberInGroup(sess, m3, g1);
		perun.getMembersManagerBl().expireMember(sess, m1);

		List<Member> filteredMembers = groupsManagerBl.getGroupMembers(sess, g1, MemberGroupStatus.VALID, Status.VALID);
		assertFalse(filteredMembers.contains(m1));
		assertFalse(filteredMembers.contains(m2));
		assertTrue(filteredMembers.contains(m3));
	}

	@Test
	public void getValidGroupMembersInGroupAndExpiredInVo() throws Exception {
		System.out.println(CLASS_NAME + "getValidGroupMembersInGroupAndExpiredInVo");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");
		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);
		Member m2 = setUpMemberWithDifferentParam(vo, 1);
		Member m3 = setUpMemberWithDifferentParam(vo, 2);

		groupsManagerBl.addMember(sess, g1, m1);
		groupsManagerBl.addMember(sess, g2, m2);
		groupsManagerBl.addMember(sess, g1, m3);
		groupsManagerBl.addMember(sess, g2, m3);

		groupsManagerBl.expireMemberInGroup(sess, m2, g2);
		groupsManagerBl.expireMemberInGroup(sess, m3, g1);
		perun.getMembersManagerBl().expireMember(sess, m1);

		List<Member> filteredMembers = groupsManagerBl.getGroupMembers(sess, g1, MemberGroupStatus.VALID, Status.EXPIRED);
		assertTrue(filteredMembers.contains(m1));
		assertFalse(filteredMembers.contains(m2));
		assertFalse(filteredMembers.contains(m3));
	}

	@Test
	public void getExpiredGroupMembersInGroupAndVo() throws Exception {
		System.out.println(CLASS_NAME + "getExpiredGroupMembersInGroupAndVo");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");
		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);
		Member m2 = setUpMemberWithDifferentParam(vo, 1);
		Member m3 = setUpMemberWithDifferentParam(vo, 2);

		groupsManagerBl.addMember(sess, g1, m1);
		groupsManagerBl.addMember(sess, g2, m2);
		groupsManagerBl.addMember(sess, g1, m3);
		groupsManagerBl.addMember(sess, g2, m3);

		groupsManagerBl.expireMemberInGroup(sess, m2, g2);
		groupsManagerBl.expireMemberInGroup(sess, m1, g1);
		perun.getMembersManagerBl().expireMember(sess, m1);

		List<Member> filteredMembers = groupsManagerBl.getGroupMembers(sess, g1, MemberGroupStatus.EXPIRED, Status.EXPIRED);
		assertTrue(filteredMembers.contains(m1));
		assertFalse(filteredMembers.contains(m2));
		assertFalse(filteredMembers.contains(m3));
	}

	@Test
	public void getAllGroupsWhereMemberIsActive() throws Exception {
		System.out.println(CLASS_NAME + "getAllGroupsWhereMemberIsActive");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");
		Group g3 = new Group("G3", "G3");
		Group g4 = new Group("G4", "G4");
		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);
		g3 = groupsManagerBl.createGroup(sess, g2, g3);
		g4 = groupsManagerBl.createGroup(sess, vo, g4);
		Group membersGroup = perun.getGroupsManagerBl().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);

		groupsManagerBl.addMember(sess, g2, m1);
		groupsManagerBl.addMember(sess, g3, m1);
		groupsManagerBl.addMember(sess, g4, m1);

		groupsManagerBl.expireMemberInGroup(sess, m1, g3);

		List<Group> activeGroups = groupsManagerBl.getAllGroupsWhereMemberIsActive(sess, m1);

		assertTrue(activeGroups.contains(g1));
		assertTrue(activeGroups.contains(g2));
		assertFalse(activeGroups.contains(g3));
		assertTrue(activeGroups.contains(g4));
		assertTrue(activeGroups.contains(membersGroup));
	}

	@Test
	public void getGroupsWhereMemberIsActive() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsWhereMemberIsActive");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");
		Group g3 = new Group("G3", "G3");
		Group g4 = new Group("G4", "G4");
		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);
		g3 = groupsManagerBl.createGroup(sess, g2, g3);
		g4 = groupsManagerBl.createGroup(sess, vo, g4);
		Group membersGroup = perun.getGroupsManagerBl().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);

		groupsManagerBl.addMember(sess, g2, m1);
		groupsManagerBl.addMember(sess, g3, m1);
		groupsManagerBl.addMember(sess, g4, m1);

		groupsManagerBl.expireMemberInGroup(sess, m1, g3);

		List<Group> activeGroups = groupsManagerBl.getGroupsWhereMemberIsActive(sess, m1);

		assertTrue(activeGroups.contains(g1));
		assertTrue(activeGroups.contains(g2));
		assertFalse(activeGroups.contains(g3));
		assertTrue(activeGroups.contains(g4));
		assertFalse(activeGroups.contains(membersGroup));
	}

	@Test
	public void getGroupsWhereMemberIsInactive() throws Exception {
		System.out.println(CLASS_NAME + "getGroupsWhereMemberIsInactive");

		vo = setUpVo();

		Group g1 = new Group("G1", "G1");
		Group g2 = new Group("G2", "G2");
		Group g3 = new Group("G3", "G3");
		Group g4 = new Group("G4", "G4");
		g1 = groupsManagerBl.createGroup(sess, vo, g1);
		g2 = groupsManagerBl.createGroup(sess, g1, g2);
		g3 = groupsManagerBl.createGroup(sess, g2, g3);
		g4 = groupsManagerBl.createGroup(sess, vo, g4);
		Group membersGroup = perun.getGroupsManagerBl().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);

		Member m1 = setUpMemberWithDifferentParam(vo, 0);

		groupsManagerBl.addMember(sess, g2, m1);
		groupsManagerBl.addMember(sess, g3, m1);
		groupsManagerBl.addMember(sess, g4, m1);

		groupsManagerBl.expireMemberInGroup(sess, m1, g3);

		List<Group> activeGroups = groupsManagerBl.getGroupsWhereMemberIsInactive(sess, m1);

		assertFalse(activeGroups.contains(g1));
		assertFalse(activeGroups.contains(g2));
		assertTrue(activeGroups.contains(g3));
		assertFalse(activeGroups.contains(g4));
		assertFalse(activeGroups.contains(membersGroup));
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
		candidate.setAttributes(new HashMap<>());
		return candidate;

	}

	private void setUpGroup(Vo vo) throws Exception {

		Group returnedGroup = groupsManager.createGroup(sess, vo, group);
		assertNotNull("unable to create a group",returnedGroup);
		assertEquals("created group should be same as returned group",group,returnedGroup);

	}

	private List<Group> setUpGroups(Vo vo) throws Exception {
		List<Group> groups = new ArrayList<>();
		groups.add(groupsManager.createGroup(sess, vo, group3));
		groups.add(groupsManager.createGroup(sess, vo, group4));
		return groups;
	}

	private Resource setUpResource(Vo vo, Facility facility) throws Exception {

		Resource resource = new Resource();
		resource.setName("GroupsManagerTestResource");
		resource.setDescription("testing resource");
		assertNotNull("unable to create resource",perun.getResourcesManager().createResource(sess, resource, vo, facility));

		return resource;

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

		List<Attribute> attributes = new ArrayList<>();

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

	private List<Attribute> setUpGroupResourceAttribute() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:group_resource:attribute-def:opt");
		attr.setFriendlyName("group-resource-test-attribute");
		attr.setType(String.class.getName());
		attr.setValue("GroupResourceAttribute");

		assertNotNull("unable to create Group_Resource attribute",attributesManager.createAttribute(sess, attr));
		// create new group resource attribute

		List<Attribute> attributes = new ArrayList<>();
		attributes.add(attr);
		// put attribute into list because setAttributes requires it

		return attributes;
	}

	private Facility setUpFacility() throws Exception {
		Facility facility = new Facility();
		facility.setName("AttributesManagerTestFacility");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, facility));
		return facility;
	}

	private Member setUpMemberInGroup(Vo vo) throws Exception {
		Candidate candidate = new Candidate();
		candidate.setFirstName("TestName");
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName("TestLastName");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));
		UserExtSource ues = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(ues);
		candidate.setAttributes(new HashMap<>());

		Member member = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);

		groupsManagerBl.createGroup(sess, vo, group);
		groupsManagerBl.addMember(sess, group, member);

		return member;
	}

	private void createGroupExpirationRulesAttribute(Group group) throws Exception {
		Attribute expirationRulesAttribute = new Attribute(attributesManager.getAttributeDefinition(sess,
				AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembershipExpirationRules"));
		Map<String, String> values = new LinkedHashMap<>();
		values.put(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName, "1.1.");

		expirationRulesAttribute.setValue(values);

		attributesManager.setAttribute(sess, group, expirationRulesAttribute);
	}
}
