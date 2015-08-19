package cz.metacentrum.perun.engine.unit;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskStatus;
import cz.metacentrum.perun.engine.scheduling.TaskStatusManager;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.taskslib.model.Task;

/**
 * Tests of TaskStatusManagerImpl which is used to store Task status for each Destination
 * which was processed by Engine during single run of propagation.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class TaskStatusManagerImplTest extends AbstractEngineTest {

	@Autowired
	TaskStatusManager taskStatusManager;
	@Autowired
	SchedulingPool schedulingPool;

	@Test
	public void getTaskStatusTest() {
		System.out.println("TaskStatusManagerImpl.getTaskStatusTest");

		TaskStatus taskStatus1 = taskStatusManager.getTaskStatus(task1);
		TaskStatus taskStatus2 = taskStatusManager.getTaskStatus(task1);
		Assert.isTrue(taskStatus1 == taskStatus2);
	}

	@Test
	public void clearTaskStatusTest() {
		System.out.println("TaskStatusManagerImpl.getTaskStatusTest");

		TaskStatus taskStatus1 = taskStatusManager.getTaskStatus(task1);
		taskStatusManager.clearTaskStatus(task1);
		TaskStatus taskStatus2 = taskStatusManager.getTaskStatus(task1);
		Assert.isTrue(taskStatus1 != taskStatus2);
	}

	@Test
	public void onTaskDestinationTest() throws InternalErrorException {
		System.out.println("TaskStatusManagerImpl.onTaskDestinationTest");

		taskStatusManager.clearTaskStatus(task1);
		schedulingPool.addToPool(task1);
		// TaskStatus taskStatus = taskStatusManager.getTaskStatus(task1);
		((TaskResultListener) taskStatusManager).onTaskDestinationDone(task1, destination1, null);
		((TaskResultListener) taskStatusManager).onTaskDestinationDone(task1, destination3, null);
		((TaskResultListener) taskStatusManager).onTaskDestinationError(task1, destination2, null);
		Assert.isTrue(task1.getStatus().equals(Task.TaskStatus.ERROR), "task1 status");
	}

	@After
	public void cleanup() {
		schedulingPool.removeTask(task1);
	}

}
