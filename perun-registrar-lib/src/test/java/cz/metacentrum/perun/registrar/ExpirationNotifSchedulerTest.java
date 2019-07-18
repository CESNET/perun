package cz.metacentrum.perun.registrar;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.registrar.impl.ExpirationNotifScheduler;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Tests for Synchronizer component.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ExpirationNotifSchedulerTest extends RegistrarBaseIntegrationTest {

	private final static String CLASS_NAME = "ExpirationNotifSchedulerTest.";

	private final static String EXPIRATION_URN = "urn:perun:member:attribute-def:def:membershipExpiration";
	private final static String GROUP_EXPIRATION_URN = "urn:perun:member_group:attribute-def:def:groupMembershipExpiration";
	private ExtSource extSource = new ExtSource(0, "testExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL);
	private Vo vo = new Vo(0, "SynchronizerTestVo", "SyncTestVo");

	private ExpirationNotifScheduler scheduler;

	public ExpirationNotifScheduler getScheduler() {
		return scheduler;
	}

	@Autowired
	public void setScheduler(ExpirationNotifScheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Before
	public void setUp() throws Exception {

		setUpExtSource();
		setUpVo();

		//set up member expiration attribute
		try {
			perun.getAttributesManager().getAttributeDefinition(session, EXPIRATION_URN);
		} catch (AttributeNotExistsException ex) {
			setUpMembershipExpirationAttribute();
		}

		//set up member group expiration attribute

		try {
			perun.getAttributesManager().getAttributeDefinition(session, GROUP_EXPIRATION_URN);
		} catch (AttributeNotExistsException ex) {
			setUpGroupMembershipExpirationAttribute();
		}

	}

	@Test
	public void checkMembersState() throws Exception {
		System.out.println(CLASS_NAME + "checkMembersState");

		// setup expiration date
		String today = LocalDate.now().toString();

		String tomorrow = LocalDate.now().plusDays(1).toString();

		String yesterday = LocalDate.now().minusDays(1).toString();

		Member member1 = setUpMember();
		Member member2 = setUpMember();
		Member member3 = setUpMember();
		Member member4 = setUpMember();
		Member member5 = setUpMember();
		Member member6 = setUpMember();
		Member member7 = setUpMember();
		Member member8 = setUpMember();
		Member member9 = setUpMember();
		Member member10 = setUpMember();
		Member member11 = setUpMember();
		Member member12 = setUpMember();
		Member member13 = setUpMember();
		Member member14 = setUpMember();
		Member member15 = setUpMember();

		// set expiration for today
		Attribute expiration = new Attribute(perun.getAttributesManager().getAttributeDefinition(session, EXPIRATION_URN));
		expiration.setValue(today);
		perun.getAttributesManager().setAttribute(session, member1, expiration);
		perun.getAttributesManager().setAttribute(session, member2, expiration);
		perun.getAttributesManager().setAttribute(session, member3, expiration);
		perun.getAttributesManager().setAttribute(session, member4, expiration);
		perun.getAttributesManager().setAttribute(session, member5, expiration);

		// set tomorrow expiration
		Attribute expirationTomorrow = new Attribute(perun.getAttributesManager().getAttributeDefinition(session, EXPIRATION_URN));
		expirationTomorrow.setValue(tomorrow);
		perun.getAttributesManager().setAttribute(session, member6, expirationTomorrow);
		perun.getAttributesManager().setAttribute(session, member7, expirationTomorrow);
		perun.getAttributesManager().setAttribute(session, member8, expirationTomorrow);
		perun.getAttributesManager().setAttribute(session, member9, expirationTomorrow);
		perun.getAttributesManager().setAttribute(session, member10, expirationTomorrow);

		// set yesterday expiration
		Attribute expirationYesterday = new Attribute(perun.getAttributesManager().getAttributeDefinition(session, EXPIRATION_URN));
		expirationYesterday.setValue(yesterday);
		perun.getAttributesManager().setAttribute(session, member11, expirationYesterday);
		perun.getAttributesManager().setAttribute(session, member12, expirationYesterday);
		perun.getAttributesManager().setAttribute(session, member13, expirationYesterday);
		perun.getAttributesManager().setAttribute(session, member14, expirationYesterday);
		perun.getAttributesManager().setAttribute(session, member15, expirationYesterday);

		// set status synchronizer should switch
		perun.getMembersManager().setStatus(session, member1, Status.VALID);   // expiration today
		perun.getMembersManager().setStatus(session, member6, Status.EXPIRED); // expiration tomorrow
		perun.getMembersManager().setStatus(session, member11, Status.VALID);  // expiration yesterday

		// set statuses synchronizer should ignore
		perun.getMembersManager().setStatus(session, member2, Status.DISABLED);
		perun.getMembersManager().setStatus(session, member3, Status.INVALID);
		perun.getMembersManager().setStatus(session, member4, Status.SUSPENDED);
		perun.getMembersManager().setStatus(session, member7, Status.DISABLED);
		perun.getMembersManager().setStatus(session, member8, Status.INVALID);
		perun.getMembersManager().setStatus(session, member9, Status.SUSPENDED);
		perun.getMembersManager().setStatus(session, member12, Status.DISABLED);
		perun.getMembersManager().setStatus(session, member13, Status.INVALID);
		perun.getMembersManager().setStatus(session, member14, Status.SUSPENDED);

		// set status synchronizer should keep
		perun.getMembersManager().setStatus(session, member5, Status.EXPIRED);  // expiration today
		perun.getMembersManager().setStatus(session, member10, Status.VALID);   // expiration tomorrow
		perun.getMembersManager().setStatus(session, member15, Status.EXPIRED); // expiration yesterday

		// start switching members based on their state
		scheduler.checkMembersState();

		// check results

		Member returnedMember1 = perun.getMembersManager().getMemberById(session, member1.getId());
		assertEquals("Member1 should be expired now (from valid)!", returnedMember1.getStatus(), Status.EXPIRED);

		Member returnedMember2 = perun.getMembersManager().getMemberById(session, member2.getId());
		assertEquals("Member2 should be kept disabled!", returnedMember2.getStatus(), Status.DISABLED);

		Member returnedMember3 = perun.getMembersManager().getMemberById(session, member3.getId());
		assertEquals("Member3 should be kept invalid!", returnedMember3.getStatus(), Status.INVALID);

		Member returnedMember4 = perun.getMembersManager().getMemberById(session, member4.getId());
		assertEquals("Member4 should be kept suspended!", returnedMember4.getStatus(), Status.SUSPENDED);

		Member returnedMember5 = perun.getMembersManager().getMemberById(session, member5.getId());
		assertEquals("Member5 should be kept expired!", returnedMember5.getStatus(), Status.EXPIRED);

		Member returnedMember6 = perun.getMembersManager().getMemberById(session, member6.getId());
		assertEquals("Member6 should be valid now (from expired)!", returnedMember6.getStatus(), Status.VALID);

		Member returnedMember7 = perun.getMembersManager().getMemberById(session, member7.getId());
		assertEquals("Member7 should be kept disabled!", returnedMember7.getStatus(), Status.DISABLED);

		Member returnedMember8 = perun.getMembersManager().getMemberById(session, member8.getId());
		assertEquals("Member8 should be kept invalid!", returnedMember8.getStatus(), Status.INVALID);

		Member returnedMember9 = perun.getMembersManager().getMemberById(session, member9.getId());
		assertEquals("Member9 should be kept suspended!", returnedMember9.getStatus(), Status.SUSPENDED);

		Member returnedMember10 = perun.getMembersManager().getMemberById(session, member10.getId());
		assertEquals("Member10 should be kept valid!", returnedMember10.getStatus(), Status.VALID);

		Member returnedMember11 = perun.getMembersManager().getMemberById(session, member11.getId());
		assertEquals("Member11 should be expired now (from valid)!", returnedMember11.getStatus(), Status.EXPIRED);

		Member returnedMember12 = perun.getMembersManager().getMemberById(session, member12.getId());
		assertEquals("Member12 should be kept disabled!", returnedMember12.getStatus(), Status.DISABLED);

		Member returnedMember13 = perun.getMembersManager().getMemberById(session, member13.getId());
		assertEquals("Member13 should be kept invalid!", returnedMember13.getStatus(), Status.INVALID);

		Member returnedMember14 = perun.getMembersManager().getMemberById(session, member14.getId());
		assertEquals("Member14 should be kept suspended!", returnedMember14.getStatus(), Status.SUSPENDED);

		Member returnedMember15 = perun.getMembersManager().getMemberById(session, member15.getId());
		assertEquals("Member15 should be kept expired!", returnedMember15.getStatus(), Status.EXPIRED);

	}

	@Test
	public void checkMembersGroupStateShouldBeValidatedToday() throws Exception {
		System.out.println(CLASS_NAME + "checkMembersGroupStateShouldBeValidatedToday");

		// setup expiration date
		String tomorrow = LocalDate.now().plusDays(1).toString();

		// set up member in group
		Member member1 = setUpMember();
		Group group = setUpGroup();
		perun.getGroupsManagerBl().addMember(session, group, member1);

		// set group expiration for tomorrow
		Attribute expiration = new Attribute(perun.getAttributesManager().getAttributeDefinition(session, GROUP_EXPIRATION_URN));
		expiration.setValue(tomorrow);
		perun.getAttributesManager().setAttribute(session, member1, group, expiration);

		perun.getGroupsManagerBl().expireMemberInGroup(session, member1, group);

		// Check init state
		MemberGroupStatus initMemberGroupStatus = perun.getGroupsManagerBl().getDirectMemberGroupStatus(session, member1, group);
		assertEquals("Member should be set to expired state before testing!", MemberGroupStatus.EXPIRED, initMemberGroupStatus);

		scheduler.checkMembersState();

		// Check if state was switched
		MemberGroupStatus memberGroupStatus = perun.getGroupsManagerBl().getDirectMemberGroupStatus(session, member1, group);
		assertEquals("Member should be valid now (from expired)!", MemberGroupStatus.VALID, memberGroupStatus);
	}

	@Test
	public void checkMembersGroupStateShouldBeValidatedTodayDoesNotAffectOthers() throws Exception {
		System.out.println(CLASS_NAME + "checkMembersGroupStateShouldBeValidatedToday");

		// setup expiration date
		String tomorrow = LocalDate.now().plusDays(1).toString();

		// set up member in group
		Member member1 = setUpMember();
		Member member2 = setUpMember();
		Group group = setUpGroup();
		perun.getGroupsManagerBl().addMember(session, group, member1);
		perun.getGroupsManagerBl().addMember(session, group, member2);

		// set group expiration for tomorrow
		Attribute m1Expiration = new Attribute(perun.getAttributesManager().getAttributeDefinition(session, GROUP_EXPIRATION_URN));
		m1Expiration.setValue(tomorrow);
		perun.getAttributesManager().setAttribute(session, member1, group, m1Expiration);

		// set group expiration for yesterday
		String yesterday = LocalDate.now().minusDays(1).toString();

		Attribute m2Expiration = new Attribute(perun.getAttributesManager().getAttributeDefinition(session, GROUP_EXPIRATION_URN));
		m2Expiration.setValue(yesterday);
		perun.getAttributesManager().setAttribute(session, member2, group, m2Expiration);

		perun.getGroupsManagerBl().expireMemberInGroup(session, member1, group);
		perun.getGroupsManagerBl().expireMemberInGroup(session, member2, group);

		scheduler.checkMembersState();

		// Check if state was not switched
		MemberGroupStatus memberGroupStatus = perun.getGroupsManagerBl().getDirectMemberGroupStatus(session, member2, group);
		assertEquals("Member should not be validated!", MemberGroupStatus.EXPIRED, memberGroupStatus);
	}

	@Test
	public void checkMembersGroupStateShouldExpireToday() throws Exception {
		System.out.println(CLASS_NAME + "checkMembersGroupStateShouldExpireToday");

		// setup expiration date to today
		String today = LocalDate.now().toString();

		// set up member in group
		Member member1 = setUpMember();
		Group group = setUpGroup();
		perun.getGroupsManagerBl().addMember(session, group, member1);

		// set group expiration for today
		Attribute expiration = new Attribute(perun.getAttributesManager().getAttributeDefinition(session, GROUP_EXPIRATION_URN));
		expiration.setValue(today);
		perun.getAttributesManager().setAttribute(session, member1, group, expiration);

		scheduler.checkMembersState();

		MemberGroupStatus memberGroupStatus = perun.getGroupsManagerBl().getDirectMemberGroupStatus(session, member1, group);

		assertEquals("Member should be expired now (from valid)!", MemberGroupStatus.EXPIRED, memberGroupStatus);
	}

	@Test
	public void checkMembersGroupStateShouldExpireTodayDoesNotAffectOthers() throws Exception {
		System.out.println(CLASS_NAME + "checkMembersGroupStateShouldExpireToday");

		// setup expiration date to today
		String today = LocalDate.now().toString();

		// set up member in group
		// set up member in group
		Member member1 = setUpMember();
		Member member2 = setUpMember();
		Group group = setUpGroup();
		perun.getGroupsManagerBl().addMember(session, group, member1);
		perun.getGroupsManagerBl().addMember(session, group, member2);

		// set group expiration for today
		Attribute m1Expiration = new Attribute(perun.getAttributesManager().getAttributeDefinition(session, GROUP_EXPIRATION_URN));
		m1Expiration.setValue(today);
		perun.getAttributesManager().setAttribute(session, member1, group, m1Expiration);

		// set group expiration for tomorrow
		String tomorrow = LocalDate.now().plusDays(1).toString();
		Attribute m2Expiration = new Attribute(perun.getAttributesManager().getAttributeDefinition(session, GROUP_EXPIRATION_URN));
		m1Expiration.setValue(tomorrow);
		perun.getAttributesManager().setAttribute(session, member2, group, m2Expiration);

		scheduler.checkMembersState();

		MemberGroupStatus memberGroupStatus = perun.getGroupsManagerBl().getDirectMemberGroupStatus(session, member2, group);

		assertEquals("Member should not be expired!", MemberGroupStatus.VALID, memberGroupStatus);
	}

	// ----------------- PRIVATE METHODS -------------------------------------------

	private Group setUpGroup() throws Exception {

		Vo vo = new Vo();
		vo.setName("test Vo");
		vo.setShortName("testVo");
		perun.getVosManagerBl().createVo(session, vo);

		Group group = new Group();
		group.setName("Test group");
		return perun.getGroupsManagerBl().createGroup(session, vo, group);
	}

	private Member setUpMember() throws Exception {

		Candidate candidate = new Candidate();
		candidate.setFirstName(Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, Long.toHexString(Double.doubleToLongBits(Math.random())));
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<>());

		return perun.getMembersManagerBl().createMemberSync(session, vo, candidate);

	}

	private void setUpExtSource() throws Exception {
		extSource = perun.getExtSourcesManager().createExtSource(session, extSource, null);
	}

	private void setUpVo() throws Exception {
		vo = perun.getVosManager().createVo(session, vo);
		perun.getExtSourcesManager().addExtSource(session, vo, extSource);
	}

	private AttributeDefinition setUpMembershipExpirationAttribute() throws Exception {

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace("urn:perun:member:attribute-def:def");
		attr.setFriendlyName("membershipExpiration");
		attr.setType(String.class.getName());
		attr.setDisplayName("Membership expiration");
		attr.setDescription("Membership expiration date.");

		return perun.getAttributesManager().createAttribute(session, attr);

	}

	private AttributeDefinition setUpGroupMembershipExpirationAttribute() throws Exception {

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_GROUP_ATTR_DEF);
		attr.setFriendlyName("groupMembershipExpiration");
		attr.setType(String.class.getName());
		attr.setDisplayName("Group membership expiration");
		attr.setDescription("When the member expires in group, format YYYY-MM-DD.");

		return perun.getAttributesManager().createAttribute(session, attr);
	}
}
