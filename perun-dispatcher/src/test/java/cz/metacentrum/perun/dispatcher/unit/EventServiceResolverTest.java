package cz.metacentrum.perun.dispatcher.unit;

import java.util.Map;
import java.util.Set;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberAddedToGroup;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import cz.metacentrum.perun.dispatcher.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventServiceResolver;

import org.junit.Assert;
import org.junit.Before;
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

	@Autowired
	PerunBl perun;

	@Before
	public void setupTests() throws Exception {

		super.setupTests();

		// create some group in there
		group1 = new Group("falcon", "desc");
		group1 = perun.getGroupsManager().createGroup(sess, vo1, group1);
		// create user in the VO
		// skip the xEntry (authorization check),
		// could skip the xBl a go directly to xImpl to avoid writing audit
		// log
		user1 = new User(0, "firstName", "lastName", "", "", "");
		user1 = perun.getUsersManagerBl().createUser(sess, user1);
		// make the user the member of the group
		member1 = perun.getMembersManagerBl().createMember(sess, vo1, user1);
		member1.setStatus("VALID");
		perun.getGroupsManagerBl().addMember(sess, group1, member1);

		// assign the group to resource
		perun.getResourcesManagerBl().assignGroupToResource(sess, group1, resource1, false);

	}

	@Test
	public void parseEventTest() throws ServiceNotExistsException, InvalidEventMessageException, PrivilegeException {
		System.out.println("EventServiceResolver.parseEventTest()");

		AuditEvent auditEvent = new DirectMemberAddedToGroup(member1, group1);

		Event event = new Event();
		event.setTimeStamp(System.currentTimeMillis());
		event.setHeader("portishead");
		event.setData(auditEvent);
		Map<Facility, Set<Service>> resolvedServices = eventServiceResolver.resolveEvent(event.getData());

		Assert.assertTrue("We should resolved only one facility-service", resolvedServices.size() == 1);

		Set<Service> resolved = resolvedServices.get(facility1);
		Assert.assertTrue("We should have 2 service", resolved.size() == 2);
		Assert.assertTrue("Our Service 1 is missing", resolved.contains(service1));
		Assert.assertTrue("Our Service 2 is missing", resolved.contains(service2));

	}

}
