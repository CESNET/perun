package cz.metacentrum.perun.dispatcher.unit;

import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.dispatcher.jms.SystemQueueProcessor;

/**
 *
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class SystemQueueProcessorTest extends AbstractDispatcherTest {

	@Autowired
	private SystemQueueProcessor systemQueueProcessor;

	@Test
	public void processDispatcherQueueAndMatchingRuleTest() {
		System.out.println("SystemQueueProcessor.processDispatcherQueueAndMatchingRule()");
		String testMessage_task = "";
		String testMessage_register = "";
		String testMessage_goodbye = "";
	}

}
