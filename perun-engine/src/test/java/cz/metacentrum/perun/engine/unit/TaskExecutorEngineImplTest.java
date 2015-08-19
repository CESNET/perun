package cz.metacentrum.perun.engine.unit;

import cz.metacentrum.perun.engine.scheduling.impl.ExecutorEngineWorkerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskExecutorEngine;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

import java.io.File;

/**
 * Tests of TaskExecutorEngineImpl which is responsible for starting of planned Tasks.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class TaskExecutorEngineImplTest extends AbstractEngineTest implements TaskExecutor {

	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private TaskExecutorEngine taskExecutorEngine;
	@Autowired
	TaskResultDao taskResultDao;

	@Before
	public void setup() throws Exception {
		super.setup();
		task1.setStatus(TaskStatus.NONE);
		schedulingPool.addToPool(task1);
		schedulingPool.setTaskStatus(task1, TaskStatus.PLANNED);
	}

	@Test
	public void beginExecutingTest() throws Exception {
		System.out.println("TaskExecutorEngineImpl.beginExecutingTest");
		taskExecutorEngine.setTaskExecutorSendWorkers(this);
		taskExecutorEngine.beginExecuting();
	}

	@Override
	public void execute(Runnable arg0) {
		// for test there is no send subfolder, let's fake root
		((ExecutorEngineWorkerImpl) arg0).setSendDirectory(new File("/"));
		arg0.run();
	}

	@After
	public void cleanup() {
		schedulingPool.removeTask(task1);
	}

}
