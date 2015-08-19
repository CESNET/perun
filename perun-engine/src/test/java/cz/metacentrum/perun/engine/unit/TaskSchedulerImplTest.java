package cz.metacentrum.perun.engine.unit;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

/**
 * Tests of TaskSchedulerImpl which process local storage for Tasks processed by Engine (SchedulingPool).
 * In production, data is stored in a local file. For tests they are in own in-memory db.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class TaskSchedulerImplTest extends AbstractEngineTest {

	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private TaskScheduler taskScheduler;

	@Test
	public void processPoolTest() throws InternalErrorException {
		System.out.println("TaskSchedulerImpl.processPoolTest");

		task1.setStatus(TaskStatus.NONE);
		schedulingPool.addToPool(task1);
		taskScheduler.processPool();
		List<Task> tasks = schedulingPool.getPlannedTasks();
		Assert.isTrue(tasks.size() == 1, "size is 1");
		Assert.isTrue(tasks.get(0) == task1, "task1 is planned");
		Assert.isTrue(task1.getStatus().equals(TaskStatus.PLANNED), "task1 status is planned");
	}

	@After
	public void cleanup() {
		schedulingPool.removeTask(task1);
	}

}
