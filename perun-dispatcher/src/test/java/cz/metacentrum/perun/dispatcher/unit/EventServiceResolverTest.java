package cz.metacentrum.perun.dispatcher.unit;

import java.util.Map;
import java.util.Set;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import cz.metacentrum.perun.dispatcher.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventServiceResolver;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class EventServiceResolverTest extends AbstractDispatcherTest {

	@Autowired
	private EventServiceResolver eventServiceResolver;

	@Test
	public void parseEventTest() throws ServiceNotExistsException, InvalidEventMessageException, InternalErrorException, PrivilegeException {
		System.out.println("EventServiceResolver.parseEventTest()");

		String message = member1.serializeToString() + " added to " + group1.serializeToString() + ".";

		Event event = new Event();
		event.setTimeStamp(System.currentTimeMillis());
		event.setHeader("portishead");
		event.setData(message);
		Map<Facility, Set<Service>> resolvedServices = eventServiceResolver.parseEvent(event.toString());

		Assert.assertTrue("We should resolved only one facility-service", resolvedServices.size() == 1);

		Set<Service> resolved = resolvedServices.get(facility1);
		Assert.assertTrue("We should have 2 service", resolved.size() == 2);
		Assert.assertTrue("Our Service 1 is missing", resolved.contains(service1));
		Assert.assertTrue("Our Service 2 is missing", resolved.contains(service2));

	}

}
