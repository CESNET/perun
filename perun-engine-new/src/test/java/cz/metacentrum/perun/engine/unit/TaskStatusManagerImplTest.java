package cz.metacentrum.perun.engine.unit;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.TestBase;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskStatus;
import cz.metacentrum.perun.engine.scheduling.TaskStatusManager;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.engine.scheduling.TaskStatus.TaskDestinationStatus;
import cz.metacentrum.perun.taskslib.model.Task;

public class TaskStatusManagerImplTest extends TestBase {

	@Autowired 
	TaskStatusManager taskStatusManager;
	@Autowired
	Task task1;
	@Autowired
	Destination destination1;
	@Autowired
	Destination destination2;
	@Autowired
	Destination destination3;
	@Autowired
	SchedulingPool schedulingPool;
	
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void getTaskStatusTest() {
		TaskStatus taskStatus1 = taskStatusManager.getTaskStatus(task1);
		TaskStatus taskStatus2 = taskStatusManager.getTaskStatus(task1);
		Assert.isTrue(taskStatus1 == taskStatus2);
	}
	
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void clearTaskStatusTest() {
		TaskStatus taskStatus1 = taskStatusManager.getTaskStatus(task1);
		taskStatusManager.clearTaskStatus(task1);
		TaskStatus taskStatus2 = taskStatusManager.getTaskStatus(task1);
		Assert.isTrue(taskStatus1 != taskStatus2);
	}
	
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void onTaskDestinationTest() throws InternalErrorException {
		taskStatusManager.clearTaskStatus(task1);
		schedulingPool.addToPool(task1);
		//TaskStatus taskStatus = taskStatusManager.getTaskStatus(task1);
		((TaskResultListener)taskStatusManager).onTaskDestinationDone(task1, destination1, null);
		((TaskResultListener)taskStatusManager).onTaskDestinationDone(task1, destination3, null);
		((TaskResultListener)taskStatusManager).onTaskDestinationError(task1, destination2, null);
		Assert.isTrue(task1.getStatus().equals(Task.TaskStatus.ERROR), "task1 status");
    }

    @After
    public void cleanup() {
    	schedulingPool.removeTask(task1);
    }
}
