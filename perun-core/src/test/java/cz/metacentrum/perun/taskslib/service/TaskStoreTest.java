package cz.metacentrum.perun.taskslib.service;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.service.impl.TaskStoreImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaskStoreTest {
	private TaskStore taskStore;
	private Task taskW;
	private Task taskG;
	private Task taskD;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {
		taskStore = new TaskStoreImpl();
		Facility facility = new Facility();
		facility.setName("Test");
		facility.setDescription("Test");
		facility.setId(1);

		Service service1 = new Service();
		service1.setId(1);

		Service service2 = new Service();
		service2.setId(2);

		Service service3 = new Service();
		service3.setId(3);

		taskW = new Task();
		taskW.setFacility(facility);
		taskW.setService(service1);
		taskW.setId(1);
		taskW.setStatus(Task.TaskStatus.WAITING);

		taskG = new Task();
		taskG.setFacility(facility);
		taskG.setService(service2);
		taskG.setId(2);
		taskG.setStatus(Task.TaskStatus.GENERATED);

		taskD = new Task();
		taskD.setFacility(facility);
		taskD.setService(service3);
		taskD.setId(3);
		taskD.setStatus(Task.TaskStatus.DONE);
	}

	@Test
	public void testAddEqualTasks() throws Exception {
		Task taskWW = new Task();
		taskWW.setFacility(taskW.getFacility());
		taskWW.setService(taskW.getService());
		taskWW.setId(10);
		taskStore.addTask(taskW);
		exception.expect(TaskStoreException.class);
		taskStore.addTask(taskWW);
	}

	@Test
	public void testGetTaskWithStatus() throws Exception {
		taskStore.addTask(taskW);
		taskStore.addTask(taskG);
		taskStore.addTask(taskD);

		List<Task> tasks = taskStore.getTasksWithStatus(Task.TaskStatus.WAITING);
		assertEquals(1, tasks.size());
		assertTrue(tasks.contains(taskW));

		tasks = taskStore.getTasksWithStatus(Task.TaskStatus.GENERATED);
		assertEquals(1, tasks.size());
		assertTrue(tasks.contains(taskG));

		tasks = taskStore.getTasksWithStatus(Task.TaskStatus.WAITING, Task.TaskStatus.GENERATED);
		assertEquals(2, tasks.size());
		assertTrue(tasks.contains(taskW));
		assertTrue(tasks.contains(taskG));

		tasks = taskStore.getTasksWithStatus(Task.TaskStatus.WAITING, Task.TaskStatus.GENERATED, Task.TaskStatus.DONE);
		assertEquals(3  , tasks.size());
		assertTrue(tasks.contains(taskW));
		assertTrue(tasks.contains(taskG));
		assertTrue(tasks.contains(taskD));
	}
}
