package cz.metacentrum.perun.engine.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.annotation.IfProfileValue;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.engine.TestBase;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskExecutorEngine;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.engine.scheduling.TaskStatusManager;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;

public class TaskExecutorEngineImplTest extends TestBase implements
		TaskExecutor {

	@Autowired
	private Task task1;
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private TaskExecutorEngine taskExecutorEngine;
	@Autowired
	private TaskStatusManager taskStatusManager;
	@Autowired
	TaskResultDao taskResultDao;

	@Before
	public void setup() {
		task1.setStatus(TaskStatus.NONE);
		schedulingPool.addToPool(task1);
		schedulingPool.setTaskStatus(task1, TaskStatus.PLANNED);
	}

	@After
	public void cleanup() {
		taskResultDao.clearAll();
		schedulingPool.removeTask(task1);
	}

	@IfProfileValue(name = "perun.test.groups", values = ("unit-tests"))
	@Test
	public void beginExecutingTest() {
		taskExecutorEngine.setTaskExecutorSendWorkers(this);
		taskExecutorEngine.beginExecuting();
	}

	@Override
	public void execute(Runnable arg0) {
		arg0.run();
	}

}
