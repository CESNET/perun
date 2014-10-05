package cz.metacentrum.perun.engine.unit;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.TestBase;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

public class TaskSchedulerImplTest extends TestBase {

	@Autowired
	private Task task1;
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private TaskScheduler taskScheduler;
	
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void processPoolTest() throws InternalErrorException {
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
