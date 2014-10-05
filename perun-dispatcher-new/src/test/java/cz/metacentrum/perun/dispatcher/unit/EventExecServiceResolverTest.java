package cz.metacentrum.perun.dispatcher.unit;

import java.util.List;


import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.TestBase;
import cz.metacentrum.perun.dispatcher.TestDataSourcePopulator;
import cz.metacentrum.perun.dispatcher.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventExecServiceResolver;
import cz.metacentrum.perun.taskslib.model.ExecService;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * @author michal
 *
 */
public final class EventExecServiceResolverTest extends TestBase {
	private final static Logger log = LoggerFactory.getLogger(EventExecServiceResolverTest.class);

	@Autowired
	private TestDataSourcePopulator testDataPopulator;	
	@Autowired
	private EventExecServiceResolver eventExecServiceResolver;
	
	@Test
	public void parseEventTest() throws ServiceNotExistsException, InvalidEventMessageException, InternalErrorException, PrivilegeException {
		
		String messages[] = {
				testDataPopulator.getMember1().serializeToString() + " added to " + testDataPopulator.getGroup1().serializeToString() + ".",
				
		};
		
		for(String message : messages) {

			Event event = new Event();
			event.setTimeStamp(System.currentTimeMillis());
			event.setHeader("portishead");
			event.setData(message);
			List<Pair<List<ExecService>, Facility>> resolvedServices = eventExecServiceResolver.parseEvent(event.toString());

			for(Pair<List<ExecService>, Facility> service : resolvedServices) {
				log.debug("Resolved: " + service.toString());
			}
		}
	}
}
