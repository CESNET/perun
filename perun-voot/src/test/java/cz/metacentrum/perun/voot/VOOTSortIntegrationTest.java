package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test for sorting of groups and members by their singular attributes.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOTSortIntegrationTest extends AbstractVOOTTest {

	private Vo vo1;
	private Group group1;
	private Group group2; //group 2 is subgroup of group1
	private Group group3;

	private Member member1;
	private Member member2;
	private Member member3;

	@Test
	public void isMemberOfSortIdTest() throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VOOTException, GroupNotExistsException {
		System.out.println("VootManager.isMemberOfSortIdTest");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "groups/@me", "sortBy=id");
		VOOTGroup[] vootGroupsOfResponse = (VOOTGroup[])response.getEntry();

		//default sort by id
		Response response2 = (Response) voot.process(session, "groups/@me", "");
		VOOTGroup[] vootGroupsOfResponse2 = (VOOTGroup[])response2.getEntry();

		assertArrayEquals(vootGroupsOfResponse, vootGroupsOfResponse2);

		Response response3 = (Response) voot.process(session, "groups/@me", "sortBy=id,sortOrder=descending");
		VOOTGroup[] vootGroupsOfResponse3 = (VOOTGroup[]) response3.getEntry();
		ArrayUtils.reverse(vootGroupsOfResponse);
		assertArrayEquals(vootGroupsOfResponse, vootGroupsOfResponse3);
	}

	@Test
	public void isMemberOfTitleSortTest() throws VOOTException {
		System.out.println("VootManager.isMemberOfTitleSortTest");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "groups/@me", "sortBy=title");
		VOOTGroup[] vootGroupsOfResponse = (VOOTGroup[])response.getEntry();

		Response response2 = (Response) voot.process(session, "groups/@me", "");
		//title of group is without vo
		//vo1:group1, vo1:group1:group2, vo1:group3, vo1:members
		VOOTGroup[] vootGroupsOfResponse2 = (VOOTGroup[])response2.getEntry();

		assertEquals(vootGroupsOfResponse2[0], vootGroupsOfResponse[0]);
		assertEquals(vootGroupsOfResponse2[1], vootGroupsOfResponse[1]);
		assertEquals(vootGroupsOfResponse2[2], vootGroupsOfResponse[2]);
		assertEquals(vootGroupsOfResponse2[3], vootGroupsOfResponse[3]);

		Response response3 = (Response) voot.process(session, "groups/@me", "sortBy=title,sortOrder=descending");
		VOOTGroup[] vootGroupsOfResponse3 = (VOOTGroup[])response3.getEntry();
		ArrayUtils.reverse(vootGroupsOfResponse);
		assertArrayEquals(vootGroupsOfResponse, vootGroupsOfResponse3);
	}

	@Test
	public void isMemberOfDescriptionSortTest() throws VOOTException {
		System.out.println("VootManager.isMemberOfDescriptionSortTest");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "groups/@me", "sortBy=description");
		VOOTGroup[] vootGroupsOfResponse = (VOOTGroup[])response.getEntry();

		Response response2 = (Response) voot.process(session, "groups/@me", "");
		//group id and first character of descrption
		//vo1:group1 B, vo1:group1:group2 C, vo1:group3 A, vo1:members G
		VOOTGroup[] vootGroupsOfResponse2 = (VOOTGroup[])response2.getEntry();

		assertEquals(vootGroupsOfResponse2[0], vootGroupsOfResponse[1]);
		assertEquals(vootGroupsOfResponse2[1], vootGroupsOfResponse[2]);
		assertEquals(vootGroupsOfResponse2[2], vootGroupsOfResponse[0]);
		assertEquals(vootGroupsOfResponse2[3], vootGroupsOfResponse[3]);

		Response response3 = (Response) voot.process(session, "groups/@me", "sortBy=description,sortOrder=descending");
		VOOTGroup[] vootGroupsOfResponse3 = (VOOTGroup[])response3.getEntry();
		ArrayUtils.reverse(vootGroupsOfResponse);
		assertArrayEquals(vootGroupsOfResponse, vootGroupsOfResponse3);
	}

	@Test
	public void isMemberOfMembershipRoleSortTest() throws VOOTException {
		System.out.println("VootManager.isMemberOfMembershipRoleSortTest");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "groups/@me", "sortBy=voot_membership_role");
		//vo1:group1 member, vo1:group1:group2 admin, vo1:group3 admin, vo1:members member
		VOOTGroup[] vootGroupsOfResponse = (VOOTGroup[])response.getEntry();

		assertEquals("admin", vootGroupsOfResponse[0].getVoot_membership_role());
		assertEquals("admin", vootGroupsOfResponse[1].getVoot_membership_role());
		assertEquals("member", vootGroupsOfResponse[2].getVoot_membership_role());
		assertEquals("member", vootGroupsOfResponse[3].getVoot_membership_role());

		Response response2 = (Response) voot.process(session, "groups/@me", "sortBy=voot_membership_role,sortOrder=descending");
		VOOTGroup[] vootGroupsOfResponse2 = (VOOTGroup[]) response2.getEntry();

		assertEquals("admin", vootGroupsOfResponse2[3].getVoot_membership_role());
		assertEquals("admin", vootGroupsOfResponse2[2].getVoot_membership_role());
		assertEquals("member", vootGroupsOfResponse2[1].getVoot_membership_role());
		assertEquals("member", vootGroupsOfResponse2[0].getVoot_membership_role());
	}

	@Test
	public void getGroupMembersIdSortTest() throws VOOTException {
		System.out.println("VootManager.getGroupMembersIdSortTest");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "people/@me/vo1:group1", "sortBy=id");
		VOOTMember[] vootMembersOfResponse = (VOOTMember[])response.getEntry();

		//default sort by id
		Response response2 = (Response) voot.process(session, "people/@me/vo1:group1", "");
		VOOTMember[] vootMembersOfResponse2 = (VOOTMember[])response2.getEntry();

		assertArrayEquals(vootMembersOfResponse, vootMembersOfResponse2);

		Response response3 = (Response) voot.process(session, "people/@me/vo1:group1", "sortBy=id,sortOrder=descending");
		VOOTMember[] vootMembersOfResponse3 = (VOOTMember[]) response3.getEntry();
		ArrayUtils.reverse(vootMembersOfResponse);
		assertArrayEquals(vootMembersOfResponse, vootMembersOfResponse3);
	}

	@Test
	public void getGroupMembersDisplayNameSortTest() throws VOOTException {
		System.out.println("VootManager.getGroupMembersDisplayNameSortTest");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "people/@me/vo1:group1", "sortBy=displayName");
		VOOTMember[] vootMembersOfResponse = (VOOTMember[])response.getEntry();

		//James Bond, Karol Druhy, Jozef Treti
		assertEquals("James Bond", vootMembersOfResponse[0].getDisplayName());
		assertEquals("Jozef Treti", vootMembersOfResponse[1].getDisplayName());
		assertEquals("Karol Druhy", vootMembersOfResponse[2].getDisplayName());

		Response response2 = (Response) voot.process(session, "people/@me/vo1:group1", "sortBy=displayName,sortOrder=descending");
		VOOTMember[] vootMembersOfResponse2 = (VOOTMember[])response2.getEntry();
		ArrayUtils.reverse(vootMembersOfResponse);
		assertArrayEquals(vootMembersOfResponse, vootMembersOfResponse2);
	}

	@Test
	public void getGroupMembersMembershipRoleSortTest() throws VOOTException {
		System.out.println("VootManager.getGroupMembersMembershipRoleSortTest");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "people/@me/vo1:group1", "sortBy=voot_membership_role");
		VOOTMember[] vootMembersOfResponse = (VOOTMember[])response.getEntry();

		//System.out.println(response);

		//one admin, others memebers
		//Jozef Treti is admin
		assertEquals("admin", vootMembersOfResponse[0].getVoot_membership_role());
		assertEquals("Jozef Treti", vootMembersOfResponse[0].getDisplayName());
		assertEquals("member", vootMembersOfResponse[1].getVoot_membership_role());
		assertEquals("member", vootMembersOfResponse[2].getVoot_membership_role());

		Response response2 = (Response) voot.process(session, "people/@me/vo1:group1", "sortBy=voot_membership_role,sortOrder=descending");
		VOOTMember[] vootMembersOfResponse2 = (VOOTMember[]) response2.getEntry();
		ArrayUtils.reverse(vootMembersOfResponse);
		assertArrayEquals(vootMembersOfResponse, vootMembersOfResponse2);
	}

	@Override
	public void setUpBackground() throws VoExistsException, InternalErrorException, GroupExistsException, AlreadyMemberException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AlreadyAdminException, AttributeNotExistsException, ExtendMembershipException, GroupNotExistsException, GroupRelationNotAllowed, GroupRelationAlreadyExists {
		vo1 = perun.getVosManagerBl().createVo(session, new Vo(0, "vo1", "vo1"));

		group1 = perun.getGroupsManagerBl().createGroup(session, vo1, new Group("group1", "B group1 in vo1"));
		group2 = perun.getGroupsManagerBl().createGroup(session, group1, new Group("group2", "C group2 is subgroup of group1"));
		group3 = perun.getGroupsManagerBl().createGroup(session, vo1, new Group("group3", "A group3 in vo1"));

		member1 = perun.getMembersManagerBl().createMember(session, vo1, user1);

		User user2 = new User();
		user2.setFirstName("Karol");
		user2.setLastName("Druhy");
		user2 = perun.getUsersManagerBl().createUser(session, user2);
		member2 = perun.getMembersManagerBl().createMember(session, vo1, user2);

		User user3 = new User();
		user3.setFirstName("Jozef");
		user3.setLastName("Treti");
		user3 = perun.getUsersManagerBl().createUser(session, user3);
		member3 = perun.getMembersManagerBl().createMember(session, vo1, user3);

		perun.getGroupsManagerBl().addMember(session, group1, member1);
		perun.getGroupsManagerBl().addMember(session, group2, member1);
		perun.getGroupsManagerBl().addAdmin(session, group2, user1);
		perun.getGroupsManagerBl().addMember(session, group3, member1);
		perun.getGroupsManagerBl().addAdmin(session, group3, user1);

		perun.getGroupsManagerBl().addMember(session, group1, member2);
		perun.getGroupsManagerBl().addMember(session, group1, member3);
		perun.getGroupsManagerBl().addAdmin(session, group1, user3);

	}

}
