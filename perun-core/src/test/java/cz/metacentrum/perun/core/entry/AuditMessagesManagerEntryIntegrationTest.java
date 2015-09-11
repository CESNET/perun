package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuditMessage;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.WrongRangeOfCountException;
import java.util.List;

/**
 * Integration tests of AuditMessagesManager.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class AuditMessagesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private final String textMismatch = "!@#$%^<<&*()_+<\\><:{[}][]{>} sd";
	private final String CLASS_NAME = "AuditMessagesManager.";
	private AuditMessage createdAuditMessage = new AuditMessage();

	public AuditMessagesManagerEntryIntegrationTest(){
		super();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		createdAuditMessage.setMsg("Tested Message");
	}

	/*
	@Test
	public void testPollConsumerMessages() throws Exception {

		List<String> messages = perun.getAuditer().pollConsumerMessages("test");
		for(String m: messages) {
			System.out.println(m);
		}
	}*/

	/**
	 * Check if method getMessages(sess) return right number of messages
	 */
	@Test
	public void testGetFixedNumberOfMessages() throws Exception {
		System.out.println(CLASS_NAME + "testGetFixedNumberOfMessages");
		int count = AuditMessagesManager.COUNTOFMESSAGES;

		for (int i = 0; i < count; i++) {
			perun.getAuditer().logWithoutTransaction(sess, "Test cislo: "+ i, null);
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
			perun.getAuditer().logWithoutTransaction(sess, "Test cislo: "+ i, null);
		}
		List<AuditMessage> messages = perun.getAuditMessagesManager().getMessages(sess, count);
		assertEquals("getMessage(sess, count) returns wrong count of messages", count , messages.size());
	}

	/**
	 * Check if method getMessages(sess, count) return correct message, which was inserted manually
	 */
	/* Temporary disabled
		 @Test
		 public void testGetCorrectMessageFromBulkOfMessages() throws Exception {
		 System.out.println(CLASS_NAME + ":testGetCorrectMessageFromBulkOfMessages()");
		 final String HASH="njasdnjasnduneunu#&Y&*#jknsdj2315";
		 int count = 100;

		 for (int i = 0; i < count; i++) {
		 if(i==9) perun.getAuditer().log(sess, HASH);
		 else perun.getAuditer().log(sess, "Test cislo: "+ i);
		 }
		 List<AuditMessage> messages = perun.getAuditMessagesManager().getMessages(sess, count);
		 for (AuditMessage m:messages){
		 if(m.getMsg().equals(HASH)) {
		 return;
		 }
		 }
		 fail("One of messages need to contain specific message.");
		 }*/

	/*
	 * Wrong Range of count exception if count is less than 1 message
	 */
	@Test (expected=WrongRangeOfCountException.class)
	public void testLessThanZeroCountOfMessages() throws Exception {
		System.out.println(CLASS_NAME + "testLessThanZeroCountOfMessages");
		perun.getAuditMessagesManager().getMessages(sess, -1);
	}

}
