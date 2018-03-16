package cz.metacentrum.perun.engine.unit;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.springframework.util.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.processing.EventParser;
import cz.metacentrum.perun.taskslib.model.Task;

/**
 * Tests of EventParserImpl which is responsible for parsing events received from JSM.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class EventParserImplTest extends AbstractEngineTest {

	private final static Logger log = LoggerFactory.getLogger(EventParserImplTest.class);

	@Autowired
	private EventParser eventParser;
	@Autowired
	PerunBl perun;

	@Test
	public void parseEventTest() throws Exception {
		System.out.println("EventParserImpl.parseEventTest");

		String testEvent = "task|"+engineId+"|[" + task1.getId() + "][false]|["
				+ task1.getService().serializeToString() + "]|["
				+ task1.getFacility().serializeToString() + "]|[Destinations [";

		for (Destination destination : task1.getDestinations()) {
			testEvent = testEvent.concat(destination.serializeToString() + ", ");
		}
		testEvent = testEvent.concat("]]");

		Task task2 = eventParser.parseEvent(testEvent);

		Assert.isTrue(task1.equals(task2), "task1 equals task2");

	}

}
