package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

/**
 * Tests for limiting results of protocol calls.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-voot-applicationcontext.xml","classpath:perun-beans.xml"})
@TransactionConfiguration(defaultRollback=true)
@Transactional

public class VOOTLimitResultIntegrationTest {
    @Autowired
    private PerunBl perun;

    private PerunSession session;
    private VOOT voot;

    private Vo vo1;
    private Group group1;
    private Group group2; //group 2 is subgroup of group1

    private User user1;

    private Member member1;

    @Before
    public void setUpSession() throws Exception{
        session = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));
        user1 = setUpUser1();
        setUpBackground();
        session.getPerunPrincipal().setUser(user1);
    }

    @Test
    public void isMemberOfTestStartIndex() throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException, VOOTException, GroupNotExistsException {
        System.out.println("IsMemberOfTestStartIndex");
        VOOT voot = new VOOT();
        Response response = (Response) voot.process(session, "groups/@me/", "startIndex=1");

        assertEquals(Integer.valueOf(3), response.getTotalResults());
        assertEquals(Integer.valueOf(1), response.getStartIndex());
        assertEquals(Integer.valueOf(2), response.getItemsPerPage());
        assertEquals(2, response.getEntry().length);
        System.out.println(response);
    }

    @Test
    public void isMemberOfTestWrongStartIndex() throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException, VOOTException, GroupNotExistsException {
        System.out.println("IsMemberOfTestWrongStartIndex");
        VOOT voot = new VOOT();
        Response response = (Response) voot.process(session, "groups/@me/", "startIndex=10");

        assertEquals(Integer.valueOf(3), response.getTotalResults());
        assertEquals(Integer.valueOf(0), response.getStartIndex());
        assertEquals(Integer.valueOf(3), response.getItemsPerPage());
        assertEquals(3, response.getEntry().length);
        System.out.println(response);
    }

    @Test
    public void isMemberOfTestStartIndexCount() throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException, VOOTException, GroupNotExistsException {
        System.out.println("IsMemberOfTestStartIndexCount");
        VOOT voot = new VOOT();
        Response response = (Response) voot.process(session, "groups/@me", "startIndex=1,count=1");

        assertEquals(Integer.valueOf(3), response.getTotalResults());
        assertEquals(Integer.valueOf(1), response.getStartIndex());
        assertEquals(Integer.valueOf(1), response.getItemsPerPage());
        assertEquals(1, response.getEntry().length);
        System.out.println(response);
    }

    private void setUpBackground() throws VoExistsException, InternalErrorException, GroupExistsException, AlreadyMemberException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException, AlreadyAdminException, AttributeNotExistsException {
        vo1 = perun.getVosManagerBl().createVo(session, new Vo(1, "vo1", "vo1"));

        group1 = perun.getGroupsManagerBl().createGroup(session, vo1, new Group("group1", "group1 in vo1"));
        group2 = perun.getGroupsManagerBl().createGroup(session, group1, new Group("group2", "group2 is subgroup of group1"));

        member1 = perun.getMembersManagerBl().createMember(session, vo1, user1);

        perun.getGroupsManagerBl().addMember(session, group2, member1);
    }

     private User setUpUser1() throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
         User user = new User();
         user.setFirstName("James");
	 user.setMiddleName("");
	 user.setLastName("Bond");
	 user.setTitleBefore("");
	 user.setTitleAfter("");

         return perun.getUsersManagerBl().createUser(session, user);
     }

}
