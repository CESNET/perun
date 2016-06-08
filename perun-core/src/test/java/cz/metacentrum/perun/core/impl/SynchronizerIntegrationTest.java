package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Tests for Synchronizer component.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class SynchronizerIntegrationTest extends AbstractPerunIntegrationTest {

	private final static String CLASS_NAME = "Synchronizer.";

	private final static String EXPIRATION_URN = "urn:perun:member:attribute-def:def:membershipExpiration";
	private ExtSource extSource = new ExtSource(0, "testExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL);
	private Vo vo = new Vo(0, "SynchronizerTestVo", "SyncTestVo");

	@Before
	public void setUp() throws Exception {

		setUpExtSource();
		setUpVo();

		try {
			perun.getAttributesManager().getAttributeDefinition(sess, EXPIRATION_URN);
		} catch (AttributeNotExistsException ex) {
			setUpMembershipExpirationAttribute();
		}

	}

	@Test
	public void checkMembersState() throws Exception {
		System.out.println(CLASS_NAME + "checkMembersState");

		// setup expiration date
		Calendar calendar = Calendar.getInstance();
		String today = BeansUtils.getDateFormatterWithoutTime().format(calendar.getTime());

		calendar.add(Calendar.DAY_OF_MONTH, 1);
		String tomorrow = BeansUtils.getDateFormatterWithoutTime().format(calendar.getTime());

		calendar.add(Calendar.DAY_OF_MONTH, -2);
		String yesterday = BeansUtils.getDateFormatterWithoutTime().format(calendar.getTime());

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
		Attribute expiration = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, EXPIRATION_URN));
		expiration.setValue(today);
		perun.getAttributesManager().setAttribute(sess, member1, expiration);
		perun.getAttributesManager().setAttribute(sess, member2, expiration);
		perun.getAttributesManager().setAttribute(sess, member3, expiration);
		perun.getAttributesManager().setAttribute(sess, member4, expiration);
		perun.getAttributesManager().setAttribute(sess, member5, expiration);

		// set tomorrow expiration
		Attribute expirationTomorrow = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, EXPIRATION_URN));
		expirationTomorrow.setValue(tomorrow);
		perun.getAttributesManager().setAttribute(sess, member6, expirationTomorrow);
		perun.getAttributesManager().setAttribute(sess, member7, expirationTomorrow);
		perun.getAttributesManager().setAttribute(sess, member8, expirationTomorrow);
		perun.getAttributesManager().setAttribute(sess, member9, expirationTomorrow);
		perun.getAttributesManager().setAttribute(sess, member10, expirationTomorrow);

		// set yesterday expiration
		Attribute expirationYesterday = new Attribute(perun.getAttributesManager().getAttributeDefinition(sess, EXPIRATION_URN));
		expirationYesterday.setValue(yesterday);
		perun.getAttributesManager().setAttribute(sess, member11, expirationYesterday);
		perun.getAttributesManager().setAttribute(sess, member12, expirationYesterday);
		perun.getAttributesManager().setAttribute(sess, member13, expirationYesterday);
		perun.getAttributesManager().setAttribute(sess, member14, expirationYesterday);
		perun.getAttributesManager().setAttribute(sess, member15, expirationYesterday);

		// set status synchronizer should switch
		perun.getMembersManager().setStatus(sess, member1, Status.VALID);   // expiration today
		perun.getMembersManager().setStatus(sess, member6, Status.EXPIRED); // expiration tomorrow
		perun.getMembersManager().setStatus(sess, member11, Status.VALID);  // expiration yesterday

		// set statuses synchronizer should ignore
		perun.getMembersManager().setStatus(sess, member2, Status.DISABLED);
		perun.getMembersManager().setStatus(sess, member3, Status.INVALID);
		perun.getMembersManager().setStatus(sess, member4, Status.SUSPENDED);
		perun.getMembersManager().setStatus(sess, member7, Status.DISABLED);
		perun.getMembersManager().setStatus(sess, member8, Status.INVALID);
		perun.getMembersManager().setStatus(sess, member9, Status.SUSPENDED);
		perun.getMembersManager().setStatus(sess, member12, Status.DISABLED);
		perun.getMembersManager().setStatus(sess, member13, Status.INVALID);
		perun.getMembersManager().setStatus(sess, member14, Status.SUSPENDED);

		// set status synchronizer should keep
		perun.getMembersManager().setStatus(sess, member5, Status.EXPIRED);  // expiration today
		perun.getMembersManager().setStatus(sess, member10, Status.VALID);   // expiration tomorrow
		perun.getMembersManager().setStatus(sess, member15, Status.EXPIRED); // expiration yesterday

		// start switching members based on their state
		Synchronizer synchronizer = new Synchronizer(perun);
		synchronizer.checkMembersState();

		// check results

		Member returnedMember1 = perun.getMembersManager().getMemberById(sess, member1.getId());
		assertEquals("Member1 should be expired now (from valid)!", returnedMember1.getStatus(), Status.EXPIRED);

		Member returnedMember2 = perun.getMembersManager().getMemberById(sess, member2.getId());
		assertEquals("Member2 should be kept disabled!", returnedMember2.getStatus(), Status.DISABLED);

		Member returnedMember3 = perun.getMembersManager().getMemberById(sess, member3.getId());
		assertEquals("Member3 should be kept invalid!", returnedMember3.getStatus(), Status.INVALID);

		Member returnedMember4 = perun.getMembersManager().getMemberById(sess, member4.getId());
		assertEquals("Member4 should be kept suspended!", returnedMember4.getStatus(), Status.SUSPENDED);

		Member returnedMember5 = perun.getMembersManager().getMemberById(sess, member5.getId());
		assertEquals("Member5 should be kept expired!", returnedMember5.getStatus(), Status.EXPIRED);

		Member returnedMember6 = perun.getMembersManager().getMemberById(sess, member6.getId());
		assertEquals("Member6 should be valid now (from expired)!", returnedMember6.getStatus(), Status.VALID);

		Member returnedMember7 = perun.getMembersManager().getMemberById(sess, member7.getId());
		assertEquals("Member7 should be kept disabled!", returnedMember7.getStatus(), Status.DISABLED);

		Member returnedMember8 = perun.getMembersManager().getMemberById(sess, member8.getId());
		assertEquals("Member8 should be kept invalid!", returnedMember8.getStatus(), Status.INVALID);

		Member returnedMember9 = perun.getMembersManager().getMemberById(sess, member9.getId());
		assertEquals("Member9 should be kept suspended!", returnedMember9.getStatus(), Status.SUSPENDED);

		Member returnedMember10 = perun.getMembersManager().getMemberById(sess, member10.getId());
		assertEquals("Member10 should be kept valid!", returnedMember10.getStatus(), Status.VALID);

		Member returnedMember11 = perun.getMembersManager().getMemberById(sess, member11.getId());
		assertEquals("Member11 should be expired now (from valid)!", returnedMember11.getStatus(), Status.EXPIRED);

		Member returnedMember12 = perun.getMembersManager().getMemberById(sess, member12.getId());
		assertEquals("Member12 should be kept disabled!", returnedMember12.getStatus(), Status.DISABLED);

		Member returnedMember13 = perun.getMembersManager().getMemberById(sess, member13.getId());
		assertEquals("Member13 should be kept invalid!", returnedMember13.getStatus(), Status.INVALID);

		Member returnedMember14 = perun.getMembersManager().getMemberById(sess, member14.getId());
		assertEquals("Member14 should be kept suspended!", returnedMember14.getStatus(), Status.SUSPENDED);

		Member returnedMember15 = perun.getMembersManager().getMemberById(sess, member15.getId());
		assertEquals("Member15 should be kept expired!", returnedMember15.getStatus(), Status.EXPIRED);

	}

	// ----------------- PRIVATE METHODS -------------------------------------------

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
		candidate.setAttributes(new HashMap<String,String>());

		return perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);

	}

	private void setUpExtSource() throws Exception {
		extSource = perun.getExtSourcesManager().createExtSource(sess, extSource, null);
	}

	private void setUpVo() throws Exception {
		vo = perun.getVosManager().createVo(sess, vo);
		perun.getExtSourcesManager().addExtSource(sess, vo, extSource);
	}

	private AttributeDefinition setUpMembershipExpirationAttribute() throws Exception {

		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace("urn:perun:member:attribute-def:def");
		attr.setFriendlyName("membershipExpiration");
		attr.setType(String.class.getName());
		attr.setDisplayName("Membership expiration");
		attr.setDescription("Membership expiration date.");

		return perun.getAttributesManager().createAttribute(sess, attr);

	}

}
