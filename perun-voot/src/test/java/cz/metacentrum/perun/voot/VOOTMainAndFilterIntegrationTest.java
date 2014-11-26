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
import static org.junit.Assert.assertNotNull;

/**
 * Tests for voot protocol calls without parameters and tests with calling request parameters.
 *
 * @author Martin Malik <374128@mail.muni.cz>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-voot-applicationcontext.xml","classpath:perun-beans.xml"})
@TransactionConfiguration(defaultRollback=true)
@Transactional

public class VOOTMainAndFilterIntegrationTest{
	@Autowired
	private PerunBl perun;

	private PerunSession session;
	private VOOT voot;

	Vo vo1;
	Vo vo2;
	Vo vo3;

	Group group1OfVo1;
	Group group2OfVo1;

	Group group1OfVo2;
	Group group2OfVo2;

	Group group1OfVo3;

	User user1;
	User user2;
	User user3;

	Member member1OfUser1;
	Member member2OfUser1;

	Member member1OfUser2;
	Member member2OfUser2;

	Member member1OfUser3;

	@Before
	public void setUpSession() throws Exception{
		session = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));
		user1 = setUpUser1();
		setMail(user1,"james.bond@mi6.co.uk");
		setUpBackground();
		session.getPerunPrincipal().setUser(user1);
	}

	@Test
	public void getPersonTest() throws VOOTException {
		System.out.println("getPersonTest");
		voot = new VOOT();
		VOOTPerson response = (VOOTPerson) voot.process(session,"people/@me", "");
		assertEquals("James Bond", response.getDisplayName());
		assertEquals(1, response.getEmails().length);
		assertEquals("james.bond@mi6.co.uk", response.getEmails()[0].getValue());

		System.out.println(response);
	}

	@Test
	public void isMemberOfTest() throws VOOTException {
		System.out.println("isMemberOfTest");
		voot = new VOOT();
		Response response = (Response) voot.process(session,"groups/@me", "");

		VOOTGroup[] vootGroups = (VOOTGroup[]) response.getEntry();

		assertEquals(Integer.valueOf(0), response.getStartIndex());
		assertEquals(Integer.valueOf(5), response.getItemsPerPage());
		assertEquals(Integer.valueOf(5), response.getTotalResults());
		assertEquals(5, vootGroups.length);

		assertNotNull(((VOOTGroup[])response.getEntry())[0].getId());
		assertNotNull(((VOOTGroup[])response.getEntry())[1].getId());
		assertNotNull(((VOOTGroup[])response.getEntry())[2].getId());
		assertNotNull(((VOOTGroup[])response.getEntry())[3].getId());
		assertNotNull(((VOOTGroup[])response.getEntry())[4].getId());

		assertNotNull(((VOOTGroup[])response.getEntry())[0].getTitle());
		assertNotNull(((VOOTGroup[])response.getEntry())[1].getTitle());
		assertNotNull(((VOOTGroup[])response.getEntry())[2].getTitle());
		assertNotNull(((VOOTGroup[])response.getEntry())[3].getTitle());
		assertNotNull(((VOOTGroup[])response.getEntry())[4].getTitle());

		assertNotNull(((VOOTGroup[])response.getEntry())[0].getDescription());
		assertNotNull(((VOOTGroup[])response.getEntry())[1].getDescription());
		assertNotNull(((VOOTGroup[])response.getEntry())[2].getDescription());
		assertNotNull(((VOOTGroup[])response.getEntry())[3].getDescription());
		assertNotNull(((VOOTGroup[])response.getEntry())[4].getDescription());

		assertNotNull(((VOOTGroup[])response.getEntry())[0].getVoot_membership_role());
		assertNotNull(((VOOTGroup[])response.getEntry())[1].getVoot_membership_role());
		assertNotNull(((VOOTGroup[])response.getEntry())[2].getVoot_membership_role());
		assertNotNull(((VOOTGroup[])response.getEntry())[3].getVoot_membership_role());
		assertNotNull(((VOOTGroup[])response.getEntry())[4].getVoot_membership_role());

		System.out.println(response);
	}

	@Test
	public void getGroupMembersTest() throws VOOTException {
		System.out.println("getGroupMembersTest");
		voot = new VOOT();
		String groupName = ("vo1:group2");
		Response response = (Response) voot.process(session,"people/@me/" + groupName, "");

		assertNotNull(response);
		assertEquals(Integer.valueOf(2), response.getTotalResults());
		assertEquals(2, response.getEntry().length);

		assertNotNull(((VOOTMember[])response.getEntry())[0].getId());
		assertNotNull(((VOOTMember[])response.getEntry())[1].getId());

		assertNotNull(((VOOTMember[])response.getEntry())[0].getDisplayName());
		assertNotNull(((VOOTMember[])response.getEntry())[1].getDisplayName());

		System.out.println(response);
	}

	@Test
	public void getGroupMembersEmptyAfterFilter() throws VOOTException {
		System.out.println("getGroupMembersEmptyAfterFilter");
		voot = new VOOT();
		String groupName = ("vo1:group2");
		Response response = (Response) voot.process(session,"people/@me/" + groupName, "sortOrder=descending,filterBy=displayName,filterOp=equals,filterValue=karol");
		assertEquals(0, response.getEntry().length);

		System.out.println(response);
	}

	@Test
	public void isMemberOfWrongFilterOp(){
		System.out.println("isMemberOfWrongFilterOp");
		voot = new VOOT();
		Response response;
		try {
			response = (Response) voot.process(session,"groups/@me", "filterBy=description,fitlerOp=equalssss,filterValue=group1 in vo1");
		} catch (VOOTException ex) {
			System.out.println(ex);
			//ok
		}
	}

	@Test
	public void isMemberOfFilterOpEquals() throws VOOTException {
		System.out.println("isMemberOfFilterOpEquals");
		voot = new VOOT();
		Response response = (Response) voot.process(session,"groups/@me", "filterBy=description,filterOp=equals,filterValue=group1 in vo1");
		assertNotNull(response);
		assertEquals(1, ((VOOTGroup[])response.getEntry()).length);
		assertEquals("group1 in vo1", ((VOOTGroup[])response.getEntry())[0].getDescription());
		System.out.println(response);
	}

	@Test
	public void isMemberOfWithWrongParametersFiled(){
		System.out.println("isMemberOfWithWrongParametersField");
		voot = new VOOT();
		Response response;
		try{
			response = (Response) voot.process(session,"groups/@me", "filterBy=name, filterValue=group");
		}catch(VOOTException ex){
			//ok
			System.out.println(ex);
		}
	}

	@Test
	public void isMemberOfWithParameterFilter() throws VOOTException {
		System.out.println("isMemberOfWithParameterFilter");
		voot = new VOOT();
		Response response = (Response) voot.process(session,"groups/@me", "filterBy=title,filterValue=group");
		assertNotNull(response);
		assertEquals(Integer.valueOf(3), response.getTotalResults());
		assertEquals(3, response.getEntry().length);

		for(int i=0;i<response.getEntry().length;i++){
			assertEquals(true, ((VOOTGroup[])response.getEntry())[i].getTitle().contains("group"));
		}

		System.out.println(response);
	}

	@Test
	public void getGroupMembersTestSort() throws VOOTException {
		System.out.println("getGroupMembersTestSort");
		voot = new VOOT();
		String groupName = ("vo1:group2");
		Response response = (Response) voot.process(session,"people/@me/" + groupName, "sortOrder=descending");
		VOOTMember[] response1Entry = (VOOTMember[])response.getEntry();

		Response response2 = (Response) voot.process(session,"people/@me/" + groupName, "");

		VOOTMember[] response2Entry = (VOOTMember[])response2.getEntry();

		for(int i=0;i<response1Entry.length;i++){
			assertEquals(response2Entry[response2Entry.length-1-i],(response1Entry[i]));
		}

		System.out.println(response);
	}

	@Test
	public void isMemberOfTestSort() throws VOOTException {
		System.out.println("isMemberOfTestSort");
		voot = new VOOT();
		Response response = (Response) voot.process(session,"groups/@me", "sortOrder=descending");
		VOOTGroup[] response1Entry = (VOOTGroup[]) response.getEntry();

		Response response2 = (Response) voot.process(session,"groups/@me", "");
		VOOTGroup[] response2Entry = (VOOTGroup[]) response2.getEntry();

		for(int i=0;i<response1Entry.length;i++){
			assertEquals(response2Entry[response2Entry.length-1-i],(response1Entry[i]));
		}

		System.out.println(response);
	}

	private void setUpBackground() throws VoExistsException, InternalErrorException, GroupExistsException, AlreadyMemberException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException, AlreadyAdminException, AttributeNotExistsException, ExtendMembershipException {
		vo1 = perun.getVosManagerBl().createVo(session, new Vo(1, "vo1", "vo1"));
		vo2 = perun.getVosManagerBl().createVo(session, new Vo(2, "vo2", "vo2"));

		group1OfVo1 = perun.getGroupsManagerBl().createGroup(session, vo1, new Group("group1", "group1 in vo1"));
		group2OfVo1 = perun.getGroupsManagerBl().createGroup(session, vo1, new Group("group2", "group2 in vo1"));

		group1OfVo2 = perun.getGroupsManagerBl().createGroup(session, vo2, new Group("group1", "group1 in vo2"));

		member1OfUser1 = perun.getMembersManagerBl().createMember(session, vo1, user1);
		member2OfUser1 = perun.getMembersManagerBl().createMember(session, vo2, user1);

		perun.getGroupsManagerBl().addMember(session, group1OfVo1, member1OfUser1);
		perun.getGroupsManagerBl().addAdmin(session, group1OfVo1, user1);
		perun.getGroupsManagerBl().addMember(session, group2OfVo1, member1OfUser1);
		perun.getGroupsManagerBl().addAdmin(session, group2OfVo1, user1);
		perun.getGroupsManagerBl().addMember(session, group1OfVo2, member2OfUser1);

		user2 = new User(2, "user2-firstName", "user2-lastName", null, null, null);
		perun.getUsersManagerBl().createUser(session, user2);
		setMail(user2, "user2@muni.cz");

		member1OfUser2 = perun.getMembersManagerBl().createMember(session, vo1, user2);
		perun.getGroupsManagerBl().addMember(session, group1OfVo1, member1OfUser2);
		perun.getGroupsManagerBl().addMember(session, group2OfVo1, member1OfUser2);
		member2OfUser2 = perun.getMembersManagerBl().createMember(session, vo2, user2);
		perun.getGroupsManagerBl().addMember(session, group1OfVo2, member2OfUser2);
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

	private void setMail(User user, String mailValue) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		AttributeDefinition attributeDefinition = new AttributeDefinition(perun.getAttributesManagerBl().getAttributeDefinition(session, "urn:perun:user:attribute-def:def:preferredMail"));
		Attribute mail = new Attribute(attributeDefinition);
		mail.setValue(mailValue);
		perun.getAttributesManagerBl().setAttribute(session, user, mail);
	}
}
