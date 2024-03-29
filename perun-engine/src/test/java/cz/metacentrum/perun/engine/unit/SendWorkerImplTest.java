package cz.metacentrum.perun.engine.unit;

import static cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus.SENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.scheduling.SendWorker;
import cz.metacentrum.perun.engine.scheduling.impl.SendWorkerImpl;
import cz.metacentrum.perun.taskslib.model.SendTask;
import java.util.Date;
import org.junit.Test;

public class SendWorkerImplTest extends AbstractEngineTest {

  @Test
  public void testSendWorkerFailure() throws Exception {
    SendWorker worker = new SendWorkerImpl(sendTaskFalse, null);
    try {
      worker.call();
      fail("TaskExecutionException should be thrown");
    } catch (TaskExecutionException e) {
      assertEquals(sendTaskFalse.getTask(), e.getTask());
      assertEquals(sendTaskFalse.getDestination(), e.getDestination());
      assertEquals(1, e.getReturnCode());
    } catch (Exception e) {
      fail("Unknown exception caught " + e);
    }
  }

  @Test
  public void testSendWorkerSuccess() throws Exception {
    SendWorker worker = new SendWorkerImpl(sendTask1, null);
    SendTask resultSendTask = worker.call();

    assertEquals(SENT, resultSendTask.getStatus());
    assertEquals((long) 0, (long) resultSendTask.getReturnCode());
    Date now = new Date(System.currentTimeMillis());
    assertTrue(resultSendTask.getEndTime().before(now) || resultSendTask.getEndTime().equals(now));
  }
}
