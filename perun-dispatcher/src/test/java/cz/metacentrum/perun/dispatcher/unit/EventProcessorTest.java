package cz.metacentrum.perun.dispatcher.unit;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueue;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventQueue;
import cz.metacentrum.perun.dispatcher.processing.impl.EventProcessorImpl;
import cz.metacentrum.perun.dispatcher.scheduling.impl.SchedulingPoolImpl;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class EventProcessorTest extends AbstractDispatcherTest {

	private final static Logger log = LoggerFactory.getLogger(EventProcessorTest.class);

	@Autowired
	private EventProcessorImpl eventProcessor;

	private EventProcessorImpl.EvProcessor evProcessor;

	@Test(timeout = 10000)
	public void eventProcessorTest() {
		System.out.println("EventProcessor.eventProcessorTest()");

		DispatcherQueue dispatcherQueue = new DispatcherQueueMock(1, "testQueue");
		eventProcessor.getDispatcherQueuePool().addDispatcherQueue(dispatcherQueue);
		eventProcessor.setEventQueue(new EventQueueMock());
		SchedulingPoolMock pool = new SchedulingPoolMock(2);
		eventProcessor.setSchedulingPool(pool);
		evProcessor = eventProcessor.new EvProcessor();
		// runs inside this thread, should end when message is delivered
		// this necessitates the use of test timeout
		evProcessor.run();
		List<Task> addedTasks = pool.getTasks();
		List<ExecService> execServices = new LinkedList<>();
		List<Facility> facilities = new LinkedList<>();

		for (Task task : addedTasks) {
			Assert.isTrue(execservice1.equals(task.getExecService()) || execservice2.equals(task.getExecService()),
					"task execService is different");
			Assert.isTrue(facility1.equals(task.getFacility()), "task Facility is different");
			Assert.isTrue(task.getStatus().equals(TaskStatus.NONE));
		}
	}

	private class EventQueueMock implements EventQueue {

		private boolean eventConsumed = false;

		@Override
		public void add(Event event) {
		}

		@Override
		public Event poll() {
			if (eventConsumed) {
				return null;
			}
			Event event = new Event();
			event.setTimeStamp(System.currentTimeMillis());
			event.setHeader("portishead");
			event.setData(member1.serializeToString() + " added to " + group1.serializeToString() + ".");
			eventConsumed = true;
			return event;
		}

		@Override
		public int size() {
			if (eventConsumed) {
				return 0;
			} else {
				return 1;
			}
		}

	}

	private class DispatcherQueueMock extends DispatcherQueue {

		public DispatcherQueueMock(int clientID, String queueName) {
			super(clientID, queueName);
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
		public int addToPool(Task task, DispatcherQueue dispatcherQueue) {
			tasks.add(task);
			adds += 1;
			if (adds == expectedAdds) {
				evProcessor.stop();
			}
			return 1;
		}

		@Override
		public void addTaskSchedule(Task task, int delayCount) {
		}

		public List<Task> getTasks() {
			return tasks;
		}
	}
}
