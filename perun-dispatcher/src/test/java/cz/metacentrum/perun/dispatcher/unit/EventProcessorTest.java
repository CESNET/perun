package cz.metacentrum.perun.dispatcher.unit;

import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberAddedToGroup;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventProcessor;
import cz.metacentrum.perun.dispatcher.scheduling.impl.SchedulingPoolImpl;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class EventProcessorTest extends AbstractDispatcherTest {

	private final static Logger log = LoggerFactory.getLogger(EventProcessorTest.class);

	@Autowired
	private EventProcessor eventProcessor;

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
	public void eventProcessorTest() {
		System.out.println("EventProcessor.eventProcessorTest()");

		EngineMessageProducer engineMessageProducer = new EngineMessageProducerMock("testQueue");
		eventProcessor.getEngineMessageProducerFactory().setProducer(engineMessageProducer);

		LinkedBlockingQueue<Event> mockQueue = new LinkedBlockingQueue<>();

		Event event = new Event();
		event.setTimeStamp(System.currentTimeMillis());
		event.setHeader("portishead");
		event.setData(new DirectMemberAddedToGroup(member1, group1));

		mockQueue.add(event);

		eventProcessor.setEventQueue(mockQueue);
		SchedulingPoolMock pool = new SchedulingPoolMock(2);
		eventProcessor.setSchedulingPool(pool);
		// runs inside this thread, should end when message is delivered
		// this necessitates the use of test timeout
		eventProcessor.run();
		List<Task> addedTasks = pool.getTasks();
		List<Facility> facilities = new LinkedList<>();

		for (Task task : addedTasks) {
			Assert.isTrue(service1.equals(task.getService()) || service2.equals(task.getService()),
					"task service is different");
			Assert.isTrue(facility1.equals(task.getFacility()), "task Facility is different");
			Assert.isTrue(task.getStatus().equals(TaskStatus.WAITING), "task status is not waiting");
		}
	}


	private class EngineMessageProducerMock extends EngineMessageProducer {

		public EngineMessageProducerMock(String queueName) {
			super(queueName);
		}

	}

	private class SchedulingPoolMock extends SchedulingPoolImpl {

		List<Task> tasks = new LinkedList<>();
		int expectedAdds;
		int adds = 0;

		public SchedulingPoolMock(int expectedAdds) {
			this.expectedAdds = expectedAdds;
		}

		@Override
		public int addToPool(Task task) {
			tasks.add(task);
			adds += 1;
			if (adds == expectedAdds) {
				eventProcessor.stop();
			}
			return 1;
		}

		@Override
		public void scheduleTask(Task task, int delayCount) {
		}

		@Override
		public Task getTask(Facility facility, Service service) {
			return null;
		}

		public List<Task> getTasks() {
			return tasks;
		}
	}
}
