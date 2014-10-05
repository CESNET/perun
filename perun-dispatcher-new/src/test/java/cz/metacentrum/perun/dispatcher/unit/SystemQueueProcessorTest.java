package cz.metacentrum.perun.dispatcher.unit;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.dispatcher.TestBase;
import cz.metacentrum.perun.dispatcher.jms.SystemQueueProcessor;

public class SystemQueueProcessorTest extends TestBase {

	@Autowired
	private SystemQueueProcessor systemQueueProcessor;
	
	@Test
	public void processDispatcherQueueAndMatchingRuleTest() {
		String testMessage_task = "";
		String testMessage_register = "";
		String testMessage_goodbye = "";

	}
}
