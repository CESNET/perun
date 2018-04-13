package cz.metacentrum.perun.engine.unit;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.scheduling.GenWorker;
import cz.metacentrum.perun.engine.scheduling.impl.GenWorkerImpl;
import cz.metacentrum.perun.taskslib.model.Task;
import org.junit.Test;

import java.util.Date;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.PLANNED;
import static org.junit.Assert.*;

public class GenWorkerImplTest extends AbstractEngineTest {

	@Test
	public void testGenWorkerSuccess() throws Exception {
		GenWorker worker = new GenWorkerImpl(task1, null);
		Task resultTask = worker.call();
		Date now = new Date(System.currentTimeMillis());
		assertTrue(resultTask.getGenEndTime().before(now) || resultTask.getGenEndTime().equals(now));
		// GenWorker itself doesn't change status anymore, since it caused race condition with endStuckTasks() process
		// Its now responsibility of GenCollector thread to switch status -> only genEndTime is set by worker.
		assertEquals(PLANNED, resultTask.getStatus());
	}

	@Test
	public void testGenWorkerFailure() throws Exception {
		GenWorker worker = new GenWorkerImpl(task2, null);
		try {
			worker.call();
			fail("TaskExecutionException should be thrown.");
		} catch (TaskExecutionException e) {
			assertEquals(task2.getId(), e.getTask().getId());
			assertEquals(1, e.getReturnCode());
		} catch (Exception e) {
			fail("Unexpected exception caught " + e);
		}
	}
}
