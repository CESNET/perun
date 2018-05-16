package cz.metacentrum.perun.engine.unit;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.runners.SendPlanner;
import cz.metacentrum.perun.engine.scheduling.SendWorker;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingSendExecutorCompletionService;
import cz.metacentrum.perun.taskslib.model.Task;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERATED;
import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.SENDING;
import static org.mockito.Mockito.*;

public class SendPlannerTest extends AbstractEngineTest {
	private BlockingSendExecutorCompletionService sendCompletionServiceMock;
	private SendPlanner spy;
	private BlockingQueue<Task> generatedTasks;

	@Before
	public void setUp() throws Exception {
		super.mockSetUp();
		sendCompletionServiceMock = mock(BlockingSendExecutorCompletionService.class);
		SendPlanner sendPlanner = new SendPlanner(sendCompletionServiceMock, schedulingPoolMock, jmsQueueManagerMock);
		spy = spy(sendPlanner);
		generatedTasks = new LinkedBlockingDeque<>();
	}

	@Test
	public void testSendPlanner() throws Exception {
		task1.setStatus(GENERATED);
		generatedTasks.add(task1);

		doReturn(generatedTasks).when(schedulingPoolMock).getGeneratedTasksQueue();
		doReturn(false, true).when(spy).shouldStop();

		spy.run();

		verify(schedulingPoolMock, times(1)).addSendTaskCount(task1, task1.getDestinations().size());
		verify(sendCompletionServiceMock, times(4)).blockingSubmit(any(SendWorker.class));
		verify(jmsQueueManagerMock, times(1)).reportTaskStatus(eq(task1.getId()), eq(SENDING),
				anyLong());
	}
}
