package cz.metacentrum.perun.engine.unit;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.TestBase;
import cz.metacentrum.perun.engine.scheduling.TaskStatus;
import cz.metacentrum.perun.engine.scheduling.TaskStatus.TaskDestinationStatus;
import cz.metacentrum.perun.engine.scheduling.impl.TaskStatusImpl;
import cz.metacentrum.perun.taskslib.model.Task;

public class TaskStatusImplTest extends TestBase {
	private final static Logger log = LoggerFactory.getLogger(TaskStatusImplTest.class);
	
	@Autowired
	Destination destination1;
	@Autowired
	Destination destination2;
	@Autowired
	Destination destination3;
	@Autowired
	Destination destination4;
	@Autowired
	Task task1;
	
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void getWaitingDestinationsTest() throws InternalErrorException {
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
	
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void getDestinationStatusTest() throws InternalErrorException {
		TaskStatus taskStatus = new TaskStatusImpl(task1);
		Assert.isTrue(taskStatus.getDestinationStatus(destination1).equals(TaskDestinationStatus.WAITING));
	}
	
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void setDestinationStatusTest() throws InternalErrorException {
		TaskStatus taskStatus = new TaskStatusImpl(task1);
		taskStatus.setDestinationStatus(destination1, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getDestinationStatus(destination1).equals(TaskDestinationStatus.DONE));
	}
	
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void isTaskFinishedTest() throws InternalErrorException {
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

    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void getTaskStatusTest() throws InternalErrorException {
		TaskStatus taskStatus = new TaskStatusImpl(task1);
		Assert.isTrue(taskStatus.getTaskStatus().equals(cz.metacentrum.perun.taskslib.model.Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination1, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getTaskStatus().equals(cz.metacentrum.perun.taskslib.model.Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination3, TaskDestinationStatus.ERROR);
		Assert.isTrue(taskStatus.getTaskStatus().equals(cz.metacentrum.perun.taskslib.model.Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination4, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getTaskStatus().equals(cz.metacentrum.perun.taskslib.model.Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination2, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getTaskStatus().equals(cz.metacentrum.perun.taskslib.model.Task.TaskStatus.DONE));

		taskStatus = new TaskStatusImpl(task1);
		Assert.isTrue(taskStatus.getTaskStatus().equals(cz.metacentrum.perun.taskslib.model.Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination1, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getTaskStatus().equals(cz.metacentrum.perun.taskslib.model.Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination3, TaskDestinationStatus.DONE);
		Assert.isTrue(taskStatus.getTaskStatus().equals(cz.metacentrum.perun.taskslib.model.Task.TaskStatus.PROCESSING));
		taskStatus.setDestinationStatus(destination2, TaskDestinationStatus.ERROR);
		Assert.isTrue(taskStatus.getTaskStatus().equals(cz.metacentrum.perun.taskslib.model.Task.TaskStatus.ERROR));

	}
}
