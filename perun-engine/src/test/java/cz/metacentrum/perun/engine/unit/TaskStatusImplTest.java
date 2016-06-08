package cz.metacentrum.perun.engine.unit;

import java.util.List;

import cz.metacentrum.perun.taskslib.model.Task;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.scheduling.TaskStatus;
import cz.metacentrum.perun.engine.scheduling.TaskStatus.TaskDestinationStatus;
import cz.metacentrum.perun.engine.scheduling.impl.TaskStatusImpl;

/**
 * Tests of TaskStatusImpl which is used to determine Task status on specific destination.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class TaskStatusImplTest extends AbstractEngineTest {

	private final static Logger log = LoggerFactory.getLogger(TaskStatusImplTest.class);

	@Test
	public void getWaitingDestinationsTest() throws InternalErrorException {
		System.out.println("TaskStatusImpl.getWaitingDestinationsTest");

		TaskStatus taskStatus = new TaskStatusImpl(task1);
		List<Destination> destinations = taskStatus.getWaitingDestinations();
		log.debug("destinations: " + destinations.toString());
		Assert.isTrue(destinations.size() == 3, "size == 3");
		Assert.isTrue(destinations.contains(destination1), "destination1");
		Assert.isTrue(destinations.contains(destination2), "destination2");
		Assert.isTrue(destinations.contains(destination3) || destinations.contains(destination4), "destination3 or destination4");
		destinations = taskStatus.getWaitingDestinations();
		Assert.isTrue(destinations.size() == 1, "size == 1");
		taskStatus.setDestinationStatus(destination3, TaskDestinationStatus.DONE);
		destinations = taskStatus.getWaitingDestinations();
		Assert.isTrue(destinations.size() == 0, "size == 0");
	}

	@Test
	public void getDestinationStatusTest() throws InternalErrorException {
		System.out.println("TaskStatusImpl.getDestinationStatusTest");

		TaskStatus taskStatus = new TaskStatusImpl(task1);
		Assert.isTrue(taskStatus.getDestinationStatus(destination1).equals(TaskDestinationStatus.WAITING));
	}

	@Test
	public void setDestinationStatusTest() throws InternalErrorException {
		System.out.println("TaskStatusImpl.setDestinationStatusTest");

		TaskStatus taskStatus = new TaskStatusImpl(task1);
		taskStatus.setDestinationStatus(destination1, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getDestinationStatus(destination1).equals(TaskDestinationStatus.DONE));
	}

	@Test
	public void isTaskFinishedTest() throws InternalErrorException {
		System.out.println("TaskStatusImpl.isTaskFinishedTest");

		TaskStatus taskStatus = new TaskStatusImpl(task1);
		Assert.isTrue(!taskStatus.isTaskFinished());
		taskStatus.setDestinationStatus(destination1, TaskDestinationStatus.DONE);
		Assert.isTrue(!taskStatus.isTaskFinished());
		taskStatus.setDestinationStatus(destination3, TaskDestinationStatus.DONE);
		Assert.isTrue(!taskStatus.isTaskFinished());
		taskStatus.setDestinationStatus(destination2, TaskDestinationStatus.ERROR);
		Assert.isTrue(taskStatus.isTaskFinished());
		taskStatus.setDestinationStatus(destination4, TaskDestinationStatus.ERROR);
		Assert.isTrue(taskStatus.isTaskFinished());
	}

	@Test
	public void getTaskStatusTest() throws InternalErrorException {
		System.out.println("TaskStatusImpl.getTaskStatusTest");

		TaskStatus taskStatus = new TaskStatusImpl(task1);
		Assert.isTrue(taskStatus.getTaskStatus().equals(Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination1, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getTaskStatus().equals(Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination3, TaskDestinationStatus.ERROR);
		Assert.isTrue(taskStatus.getTaskStatus().equals(Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination4, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getTaskStatus().equals(Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination2, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getTaskStatus().equals(Task.TaskStatus.DONE));

		taskStatus = new TaskStatusImpl(task1);
		Assert.isTrue(taskStatus.getTaskStatus().equals(Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination1, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getTaskStatus().equals(Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination3, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getTaskStatus().equals(Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination2, TaskDestinationStatus.ERROR);
		Assert.isTrue(taskStatus.getTaskStatus().equals(Task.TaskStatus.ERROR));
	}

}
