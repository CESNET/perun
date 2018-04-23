package cz.metacentrum.perun.engine.unit;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.impl.SchedulingPoolImpl;
import cz.metacentrum.perun.taskslib.service.impl.TaskStoreImpl;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Tests of SchedulingPool which represent local storage of Tasks which are processed by Engine.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class SchedulingPoolTest extends AbstractEngineTest {

	private SchedulingPool schedulingPool;

	@Before
	public void setup() throws Exception {
		super.setup();
		schedulingPool = new SchedulingPoolImpl(
				new TaskStoreImpl(),
				mock(JMSQueueManager.class));
		schedulingPool.addTask(task1);
	}

	@After
	public void cleanup() throws Exception {
		schedulingPool.removeTask(task1);
		schedulingPool.removeTask(task2);
	}

	@Test
	public void addToPoolTest() throws Exception {
		assertEquals("Original size should be 1", 1, schedulingPool.getSize());

		System.out.println(schedulingPool);

		schedulingPool.addTask(task1); // pool already contains this task
		assertEquals("New size should be 1 because the added Task was already in.", 1, schedulingPool.getSize());

		schedulingPool.addTask(task2);
		assertEquals("New size should be 2.", 2, schedulingPool.getSize());
	}

	@Test(expected = IllegalArgumentException.class)
	public void doNotAddNotPlannedTasks() throws Exception {
		task2.setStatus(TaskStatus.GENERATING);
		schedulingPool.addTask(task2);
	}

	@Test
	public void getPlannedFromPoolTest() throws Exception {
		Collection<Task> tasks = schedulingPool.getTasksWithStatus(TaskStatus.PLANNED);
		assertTrue("Task task1 should be in the collection.", tasks.contains(task1));

		schedulingPool.addTask(task2);
		tasks = schedulingPool.getTasksWithStatus(TaskStatus.PLANNED);
		assertTrue("Both Tasks should be in the collection.", tasks.contains(task1) && tasks.contains(task2));
	}

	@Test
	public void getGeneratingFromPoolTest() throws Exception {
		Collection<Task> tasks = schedulingPool.getTasksWithStatus(TaskStatus.GENERATING);
		assertTrue("There should be no generating Tasks", tasks.isEmpty());

		schedulingPool.addTask(task2);
		task2.setStatus(TaskStatus.GENERATING);
		//generatingTasks.blockingPut(task2.getId(), task2);
		tasks = schedulingPool.getTasksWithStatus(TaskStatus.GENERATING);
		assertTrue("Task task1 should be in the collection.", tasks.contains(task2));
	}

	@Test
	public void getGeneratedFromPool() throws Exception {
		Collection<Task> tasks = schedulingPool.getTasksWithStatus(TaskStatus.GENERATED);
		assertTrue("There should be no generated Tasks", tasks.isEmpty());

		schedulingPool.addTask(task2);
		task2.setStatus(TaskStatus.GENERATED);
		schedulingPool.getGeneratedTasksQueue().add(task2);
		tasks = schedulingPool.getTasksWithStatus(TaskStatus.GENERATED);
		assertTrue("Task task1 should be in the collection.", tasks.contains(task2));
	}

	@Test
	public void getTaskByIdTest() throws Exception {
		Task task = schedulingPool.getTask(task1.getId());
		assertEquals(task1, task);
	}

	@Test
	public void removeSentTaskTest() throws Exception {
		schedulingPool.addSendTaskCount(task1, 2);
		assertEquals(1, schedulingPool.getSize());

		schedulingPool.decreaseSendTaskCount(task1, 1);
		schedulingPool.decreaseSendTaskCount(task1, 1);

		assertEquals("Task should be removed from pool when associated sendTask count reaches zero",
				0, schedulingPool.getSize());
	}

}
