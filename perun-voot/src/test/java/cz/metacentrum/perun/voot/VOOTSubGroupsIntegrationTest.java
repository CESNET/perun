package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests of VOOT protocol calls for subgroups.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOTSubGroupsIntegrationTest extends AbstractVOOTTest {

	private Vo vo1;
	private Group group1;
	private Group group2; //group 2 is subgroup of group1

	private Member member1;
	private Member member2;

	@Test
	public void isMemberOfSubGroupTest() throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VOOTException, GroupNotExistsException {
		System.out.println("VootManager.IsMemberOfSubGroupTest");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "groups/@me", "");
		assertEquals(3, response.getEntry().length);
		//System.out.println(response);
	}

	@Test
	public void groupMembersSubGroupTest() throws VOOTException {
		System.out.println("VootManager.groupMembersSubGroupTest");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "people/@me/vo1:group1:group2", "");
		assertEquals(2, response.getEntry().length);
		//System.out.println(response);
	}

	@Override
	public void setUpBackground() throws VoExistsException, InternalErrorException, GroupExistsException, AlreadyMemberException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AlreadyAdminException, AttributeNotExistsException, ExtendMembershipException, GroupNotExistsException, GroupRelationNotAllowed, GroupRelationAlreadyExists {
		vo1 = perun.getVosManagerBl().createVo(session, new Vo(0, "vo1", "vo1"));

		group1 = perun.getGroupsManagerBl().createGroup(session, vo1, new Group("group1", "group1 in vo1"));
		group2 = perun.getGroupsManagerBl().createGroup(session, group1, new Group("group2", "group2 is subgroup of group1"));

		member1 = perun.getMembersManagerBl().createMember(session, vo1, user1);

		User user2 = new User();
		user2.setFirstName("Karol");
		user2.setLastName("Druhy");
		user2 = perun.getUsersManagerBl().createUser(session, user2);
		member2 = perun.getMembersManagerBl().createMember(session, vo1, user2);

		perun.getGroupsManagerBl().addMember(session, group2, member1);
		perun.getGroupsManagerBl().addMember(session, group2, member2);
	}

}
