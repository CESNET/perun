package cz.metacentrum.perun.engine.unit;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

/**
 * Tests of SchedulingPool which represent local storage of Tasks which are processed by Engine.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class SchedulingPoolTest extends AbstractEngineTest {

	private final static Logger log = LoggerFactory.getLogger(SchedulingPoolTest.class);

	@Autowired
	private SchedulingPool schedulingPool;

	@Before
	public void setup() throws Exception {
		super.setup();
		task1.setStatus(TaskStatus.NONE);
		task2.setStatus(TaskStatus.NONE);
		schedulingPool.addToPool(task1);
	}

	@After
	public void cleanup() {
		schedulingPool.removeTask(task1);
		schedulingPool.removeTask(task2);
	}

	@Test
	public void addToPoolTest() {
		System.out.println("SchedulingPool.addToPoolTest");

		Assert.isTrue(schedulingPool.getSize() == 1, "original size is 1");
		schedulingPool.addToPool(task1); // pool already contains this task
		Assert.isTrue(schedulingPool.getSize() == 1, "new size is 1");
		schedulingPool.addToPool(task2);
		Assert.isTrue(schedulingPool.getSize() == 2, "new size is 2");
	}

	@Test
	public void getFromPoolTest() {
		System.out.println("SchedulingPool.getFromPoolTest");

		List<Task> tasks = schedulingPool.getDoneTasks();
		Assert.isTrue(tasks.isEmpty(), "done list is empty");
		tasks = schedulingPool.getNewTasks();
		log.debug("new size: " + tasks.size());
		Assert.isTrue(tasks.size() == 1, "new list has size 1");
		Assert.isTrue(task1 == tasks.get(0), "task equals");
	}

	@Test
	public void setTaskStatusTest() {
		System.out.println("SchedulingPool.setTaskStatusTest");

		schedulingPool.setTaskStatus(task1, TaskStatus.PROCESSING);
		List<Task> tasks = schedulingPool.getNewTasks();
		Assert.isTrue(tasks.isEmpty());
		tasks = schedulingPool.getProcessingTasks();
		Assert.isTrue(tasks.size() == 1);
		Assert.isTrue(task1 == tasks.get(0));
	}

	@Test
	public void getTaskByIdTest() {
		System.out.println("SchedulingPool.getTaskByIdTest");

		Task task = schedulingPool.getTaskById(task1.getId());
		Assert.isTrue(task == task1);
	}

}
