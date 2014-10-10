package cz.metacentrum.perun.engine.unit;

import java.util.Properties;

import org.springframework.test.annotation.IfProfileValue;
import org.springframework.util.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.TestBase;
import cz.metacentrum.perun.engine.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.engine.processing.EventParser;
import cz.metacentrum.perun.taskslib.model.Task;

public class EventParserImplTest extends TestBase {
	private final static Logger log = LoggerFactory
			.getLogger(EventParserImplTest.class);

	@Autowired
	private EventParser eventParser;
	@Autowired
	private Task task1;
	@Autowired
	private Properties propertiesBean;

	@IfProfileValue(name = "perun.test.groups", values = ("unit-tests"))
	@Test
	public void testParseEvent() throws ServiceNotExistsException,
			InvalidEventMessageException, InternalErrorException,
			PrivilegeException {
		// NO!!!
		// propertiesBean.setProperty("engine.unique.id", "1");
		String testEvent = "task|0|[" + task1.getId() + "]["
				+ task1.getExecServiceId() + "]["
				+ task1.getFacility().serializeToString() + "]|[Destinations [";
		for (Destination destination : task1.getDestinations()) {
			testEvent = testEvent
					.concat(destination.serializeToString() + ", ");
		}
		testEvent = testEvent.concat("]]|[]");
		Task task2 = eventParser.parseEvent(testEvent);
		Assert.isTrue(task1.equals(task2), "task1 equals task2");
	}

}
