package cz.metacentrum.perun.dispatcher.unit;

import java.util.Collection;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.dispatcher.TestBase;
import cz.metacentrum.perun.dispatcher.TestDataSourcePopulator;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueue;
import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventLogger;
import cz.metacentrum.perun.dispatcher.processing.EventQueue;
import cz.metacentrum.perun.dispatcher.processing.SmartMatcher;
import cz.metacentrum.perun.dispatcher.processing.impl.EventProcessorImpl;
import cz.metacentrum.perun.dispatcher.scheduling.impl.SchedulingPoolImpl;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

public class EventProcessorTest extends TestBase {
	private final static Logger log = LoggerFactory.getLogger(EventProcessorTest.class);

	@Autowired
	private TestDataSourcePopulator testDataPopulator;	
	@Autowired
	private ExecService execservice1;
	@Autowired
	private ExecService execservice2;
	@Autowired
	private Facility facility1;
	
	@Autowired
	private EventProcessorImpl eventProcessor;

	private EventProcessorImpl.EvProcessor evProcessor;
	private Task createdTask;
	
	private class EventQueueMock implements EventQueue {
		private boolean eventConsumed = false;
		
		@Override
		public void add(Event event) {
		}

		@Override
		public Event poll() {
			if(eventConsumed) {
				return null;
			}
			Event event = new Event();
			event.setTimeStamp(System.currentTimeMillis());
			event.setHeader("portishead");
			event.setData(testDataPopulator.getMember1().serializeToString() + " added to " + testDataPopulator.getGroup1().serializeToString() + ".");
			eventConsumed = true;
			return event;
		}

		@Override
		public int size() {
			if(eventConsumed) {
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

		@Override
		public int addToPool(Task task, DispatcherQueue dispatcherQueue) {
			createdTask = task;
			evProcessor.stop();
			return 1;
		}
		
	}
	
	@Test(timeout=1000)
	public void eventProcessorTest() {
		DispatcherQueue dispatcherQueue = new DispatcherQueueMock(1, "testQueue");
		eventProcessor.getDispatcherQueuePool().addDispatcherQueue(dispatcherQueue);
		eventProcessor.setEventQueue(new EventQueueMock());
		eventProcessor.getSmartMatcher().loadAllRulesFromDB();
		eventProcessor.setSchedulingPool(new SchedulingPoolMock());
		evProcessor = eventProcessor.new EvProcessor();
		// runs inside this thread, should end when message is delivered
		// this necessitates the use of test timeout
		evProcessor.run();
		log.debug("createdTask: " + createdTask);
		Assert.isTrue(execservice2.equals(createdTask.getExecService()), "task execService is different");
		Assert.isTrue(facility1.equals(createdTask.getFacility()), "task Facility is different");
		Assert.isTrue(createdTask.getStatus().equals(TaskStatus.NONE));
		
	}
}
