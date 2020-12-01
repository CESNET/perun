package cz.metacentrum.perun.core.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.ExpirationNotifScheduler.SponsorshipExpired;
import cz.metacentrum.perun.audit.events.FacilityManagerEvents.FacilityCreated;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.SponsorshipEstablished;
import cz.metacentrum.perun.audit.events.StringMessageEvent;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.EnrichedSponsorship;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongRangeOfCountException;
import cz.metacentrum.perun.core.impl.AuditMessagesManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Integration tests of AuditMessagesManager.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class AuditMessagesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private final String CLASS_NAME = "AuditMessagesManager.";
	private final AuditMessage createdAuditMessage = new AuditMessage();

	public AuditMessagesManagerEntryIntegrationTest(){
		super();
	}

	/**
	 */
	@Before
	public void setUp() {
		Facility testFacility = new Facility(0,"AuditMessageManagerEntryIntegrationTestFacility");
		FacilityCreated facilityCreatedEvent = new FacilityCreated(testFacility);
		createdAuditMessage.setEvent(facilityCreatedEvent);
	}

	/**
	 * Check if method getMessages(sess) return right number of messages
	 */
	@Test
	public void testGetFixedNumberOfMessages() throws Exception {
		System.out.println(CLASS_NAME + "testGetFixedNumberOfMessages");
		int count = AuditMessagesManager.COUNTOFMESSAGES;

		for (int i = 0; i < count; i++) {
			perun.getAuditer().logWithoutTransaction(sess, new StringMessageEvent("Test cislo: "+ i));
		}

		List<AuditMessage> messages = perun.getAuditMessagesManager().getMessages(sess);
		assertEquals("getMessage(sess) returns wrong count of messages", count , messages.size());
	}

	/**
	 * Check if method getMessages(sess, count) return right number of messages
	 */
	@Test
	public void testGetVariableNumberOfMessages() throws Exception {
		System.out.println(CLASS_NAME + "testGetVariableNumberOfMessages");
		int count = 33;

		for (int i = 0; i < count; i++) {
			perun.getAuditer().logWithoutTransaction(sess, new StringMessageEvent("Test cislo: "+ i));
		}
		List<AuditMessage> messages = perun.getAuditMessagesManager().getMessages(sess, count);
		assertEquals("getMessage(sess, count) returns wrong count of messages", count , messages.size());
	}

	/*
	 * Wrong Range of count exception if count is less than 1 message
	 */
	@Test (expected=WrongRangeOfCountException.class)
	public void testLessThanZeroCountOfMessages() throws Exception {
		System.out.println(CLASS_NAME + "testLessThanZeroCountOfMessages");
		perun.getAuditMessagesManager().getMessages(sess, -1);
	}

	@Test
	public void testLocalDate() throws Exception {
		System.out.println(CLASS_NAME + "testLocalDate");

		AuditMessagesManagerImpl auditMessagesManagerImpl = (AuditMessagesManagerImpl)ReflectionTestUtils
				.getField(perun.getAuditMessagesManagerBl(), "auditMessagesManagerImpl");
		assertThat(auditMessagesManagerImpl).isNotNull();

		ObjectMapper mapper = (ObjectMapper)ReflectionTestUtils.getField(auditMessagesManagerImpl, "mapper");
		assertThat(mapper).isNotNull();

		AuditEvent event = new SponsorshipEstablished(null, null, LocalDate.MIN);

		testAuditEventMapper(mapper, event);

		EnrichedSponsorship enrichedSponsorship = new EnrichedSponsorship();
		enrichedSponsorship.setValidityTo(LocalDate.MAX);
		AuditEvent event2 = new SponsorshipExpired();

		testAuditEventMapper(mapper, event2);
	}

	private void testAuditEventMapper(ObjectMapper mapper, AuditEvent event) throws Exception {
		String value = mapper.writeValueAsString(event);

		AuditEvent deserializedEvent = mapper.readValue(value, AuditEvent.class);

		assertThat(deserializedEvent).isEqualTo(event);
	}

}
