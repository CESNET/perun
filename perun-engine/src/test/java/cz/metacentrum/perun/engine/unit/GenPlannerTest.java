package cz.metacentrum.perun.engine.unit;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.scheduling.GenWorker;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingGenExecutorCompletionService;
import cz.metacentrum.perun.engine.runners.GenPlanner;
import cz.metacentrum.perun.taskslib.model.Task;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERATING;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GenPlannerTest extends AbstractEngineTest{
	private BlockingGenExecutorCompletionService genCompletionServiceMock;
	private BlockingDeque<Task> newTasksQueue;
	private GenPlanner spy;
	private Date genStartTime;

	@Before
	public void setUp() throws Exception {
		super.mockSetUp();
		genCompletionServiceMock = mock(BlockingGenExecutorCompletionService.class);
		newTasksQueue = new LinkedBlockingDeque<>();
		GenPlanner genPlanner = new GenPlanner(schedulingPoolMock, genCompletionServiceMock, jmsQueueManagerMock);
		spy = spy(genPlanner);
		genStartTime = new Date(System.currentTimeMillis());
		task1.setGenStartTime(genStartTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
	}

	@Test
	public void testGenPlanner() throws Exception {
		Future<Task> futureMock = mock(Future.class);
		newTasksQueue.add(task1);


		when(schedulingPoolMock.getNewTasksQueue()).thenReturn(newTasksQueue);
		when(genCompletionServiceMock.blockingSubmit(any(GenWorker.class))).thenReturn(futureMock);
		doReturn(false, true).when(spy).shouldStop();

		spy.run();

		verify(genCompletionServiceMock, times(1)).blockingSubmit(any(GenWorker.class));
		verify(jmsQueueManagerMock, times(1)).reportTaskStatus(
				eq(task1.getId()), eq(task1.getStatus()), eq(task1.getGenStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

		assertEquals(GENERATING, task1.getStatus());
	}

}
