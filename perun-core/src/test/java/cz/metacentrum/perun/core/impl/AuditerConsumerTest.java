package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.audit.events.VoManagerEvents.VoCreated;
import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Vo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AuditerConsumerTest extends AbstractPerunIntegrationTest {

	private static final String CLASS_NAME = AuditerConsumer.class.getSimpleName() + ".";

	private AuditerConsumer auditerConsumer;

	@Autowired
	private DataSource dataSource;

	@Before
	public void setUp() throws Exception {
		auditerConsumer = new AuditerConsumer("TestAuditerConsumer", dataSource);
	}

	@Test
	public void getMessagesInJson() throws Exception {
		System.out.println(CLASS_NAME + "getMessagesInJson");

		Vo vo = new Vo(0, "AuditerPublisher testVo", "AP TestVo");
		VoCreated voCreatedEvent = new VoCreated(vo);
		perun.getAuditer().logWithoutTransaction(sess, voCreatedEvent);

		List<String> messages = auditerConsumer.getMessagesInJson();
		assertEquals("Invalid number of messages received.", 1, messages.size());
	}
}
