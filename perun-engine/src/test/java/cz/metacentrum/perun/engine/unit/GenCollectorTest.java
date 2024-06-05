package cz.metacentrum.perun.engine.unit;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERATING;
import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.runners.GenCollector;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingGenExecutorCompletionService;
import cz.metacentrum.perun.taskslib.model.Task;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.junit.Before;
import org.junit.Test;


public class GenCollectorTest extends AbstractEngineTest {
  private BlockingGenExecutorCompletionService genCompletionServiceMock;
  private GenCollector spy;
  private BlockingDeque<Task> generatedTasksQueue;
  private Date genEndTime;

  @Before
  public void setUp() throws Exception {
    super.mockSetUp();
    genCompletionServiceMock = mock(BlockingGenExecutorCompletionService.class);
    GenCollector genCollector = new GenCollector(schedulingPoolMock, genCompletionServiceMock, jmsQueueManagerMock);
    spy = spy(genCollector);
    generatedTasksQueue = new LinkedBlockingDeque<>();
    genEndTime = new Date(System.currentTimeMillis());
  }

  @Test
  public void testGenCollector() throws Exception {
    task1.setStatus(GENERATING);
    task1.setGenEndTime(genEndTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

    when(schedulingPoolMock.getGeneratedTasksQueue()).thenReturn(generatedTasksQueue);
    when(genCompletionServiceMock.blockingTake()).thenReturn(task1);
    doReturn(false, true).when(spy).shouldStop();

    spy.run();

    verify(jmsQueueManagerMock, times(1)).reportTaskStatus(eq(task1), eq(task1.getStatus()),
        eq(task1.getGenEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
    assertTrue(generatedTasksQueue.contains(task1));
    assertEquals("There should be only one generated Task", 1, generatedTasksQueue.size());
  }

  @Test
  public void testGenCollectorTaskException() throws Exception {

    when(schedulingPoolMock.getGeneratedTasksQueue()).thenReturn(generatedTasksQueue);
    when(genCompletionServiceMock.blockingTake()).thenThrow(new TaskExecutionException(task1, "Test err"));
    doReturn(false, true).when(spy).shouldStop();

    spy.run();

    verify(jmsQueueManagerMock, times(1)).reportTaskStatus(eq(task1), eq(GENERROR), anyLong());
    verify(schedulingPoolMock, times(1)).removeTask(task1.getId(), task1.getRunId());
    assertTrue(generatedTasksQueue.isEmpty());
  }
}
