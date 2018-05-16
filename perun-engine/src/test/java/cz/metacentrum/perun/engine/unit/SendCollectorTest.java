package cz.metacentrum.perun.engine.unit;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.runners.SendCollector;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingSendExecutorCompletionService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus.ERROR;
import static cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus.SENDING;
import static cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus.SENT;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class SendCollectorTest extends AbstractEngineTest {
	private BlockingSendExecutorCompletionService sendCompletionServiceMock;
	private SendCollector spy;

	@Before
	public void setUp() throws Exception {
		super.mockSetUp();
		sendCompletionServiceMock = mock(BlockingSendExecutorCompletionService.class);
		SendCollector sendCollector = new SendCollector(sendCompletionServiceMock,
				schedulingPoolMock, jmsQueueManagerMock);
		spy = spy(sendCollector);
	}

	@Test
	public void sendCollectorTest() throws Exception {
		when(sendCompletionServiceMock.blockingTake()).thenReturn(sendTask1, sendTask2, sendTask4, sendTask3);
		doReturn(false, false, false, false, true).when(spy).shouldStop();

		spy.run();

		// SendTask status is no longer modified by SendCollector -> keep SENDING instead of SENT
		// Only real SendWorker switches its initial state
		assertEquals(SENDING, sendTask1.getStatus());
		assertEquals(SENDING, sendTask2.getStatus());
		assertEquals(SENDING, sendTask3.getStatus());
		assertEquals(SENDING, sendTask4.getStatus());

		verify(schedulingPoolMock, times(4)).decreaseSendTaskCount(task1, 1);
		verify(jmsQueueManagerMock, times(4)).reportTaskResult(null);

		// since scheduling pool is mocked, it doesn't switch status to DONE (or ERROR) anymore
		// when we call removeSendTaskFuture()
		assertEquals(Task.TaskStatus.SENDING, task1.getStatus());

	}

	@Test
	public void sendCollectorTaskExceptionTest() throws Exception {

		task1.setStatus(Task.TaskStatus.SENDING);

		doReturn(sendTask1)
				.doThrow(new TaskExecutionException(sendTask2.getTask(), sendTask2.getDestination(), "Test error"))
				.when(sendCompletionServiceMock).blockingTake();
		doReturn(false, false, true).when(spy).shouldStop();
		doReturn(task1).when(schedulingPoolMock).getTask(task1.getId());

		spy.run();

		// SENDING instead of SENT, because only real SendWorker switches state
		assertEquals(SENDING, sendTask1.getStatus());
		// since one of SendTasks failed, Task status is changed to SENDERROR
		assertEquals(Task.TaskStatus.SENDERROR, task1.getStatus());

		verify(schedulingPoolMock, times(2)).decreaseSendTaskCount(task1, 1);
		verify(jmsQueueManagerMock, times(2)).reportTaskResult(null);

	}
}
