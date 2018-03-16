package cz.metacentrum.perun.dispatcher.unit;

import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.dispatcher.jms.EngineMessageProcessor;

/**
 *
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class EngineMessageProcessorTest extends AbstractDispatcherTest {

	@Autowired
	private EngineMessageProcessor engineMessageProcessor;

	@Test
	public void processDispatcherQueueAndMatchingRuleTest() {
		System.out.println("SystemQueueProcessor.processDispatcherQueueAndMatchingRule()");
		String testMessage_task = "";
		String testMessage_register = "";
		String testMessage_goodbye = "";
	}

}
