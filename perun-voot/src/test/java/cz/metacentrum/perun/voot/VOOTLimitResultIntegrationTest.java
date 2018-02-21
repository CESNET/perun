package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for limiting results of protocol calls.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOTLimitResultIntegrationTest extends AbstractVOOTTest {

	private Vo vo1;
	private Group group1;
	private Group group2; //group 2 is subgroup of group1

	private Member member1;

	@Test
	public void isMemberOfTestStartIndex() throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VOOTException, GroupNotExistsException {
		System.out.println("VootManager.isMemberOfTestStartIndex");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "groups/@me/", "startIndex=1");

		assertEquals(Integer.valueOf(3), response.getTotalResults());
		assertEquals(Integer.valueOf(1), response.getStartIndex());
		assertEquals(Integer.valueOf(2), response.getItemsPerPage());
		assertEquals(2, response.getEntry().length);
		//System.out.println(response);
	}

	@Test
	public void isMemberOfTestWrongStartIndex() throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VOOTException, GroupNotExistsException {
		System.out.println("VootManager.IsMemberOfTestWrongStartIndex");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "groups/@me/", "startIndex=10");

		assertEquals(Integer.valueOf(3), response.getTotalResults());
		assertEquals(Integer.valueOf(0), response.getStartIndex());
		assertEquals(Integer.valueOf(3), response.getItemsPerPage());
		assertEquals(3, response.getEntry().length);
		//System.out.println(response);
	}

	@Test
	public void isMemberOfTestStartIndexCount() throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VOOTException, GroupNotExistsException {
		System.out.println("VootManager.IsMemberOfTestStartIndexCount");
		VOOT voot = new VOOT();
		Response response = (Response) voot.process(session, "groups/@me", "startIndex=1,count=1");

		assertEquals(Integer.valueOf(3), response.getTotalResults());
		assertEquals(Integer.valueOf(1), response.getStartIndex());
		assertEquals(Integer.valueOf(1), response.getItemsPerPage());
		assertEquals(1, response.getEntry().length);
		//System.out.println(response);
	}

	@Override
	public void setUpBackground() throws VoExistsException, InternalErrorException, GroupExistsException, AlreadyMemberException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AlreadyAdminException, AttributeNotExistsException, ExtendMembershipException, GroupNotExistsException, GroupRelationNotAllowed, GroupRelationAlreadyExists {
		vo1 = perun.getVosManagerBl().createVo(session, new Vo(0, "vo1", "vo1"));

		group1 = perun.getGroupsManagerBl().createGroup(session, vo1, new Group("group1", "group1 in vo1"));
		group2 = perun.getGroupsManagerBl().createGroup(session, group1, new Group("group2", "group2 is subgroup of group1"));

		member1 = perun.getMembersManagerBl().createMember(session, vo1, user1);

		perun.getGroupsManagerBl().addMember(session, group2, member1);
	}

}
