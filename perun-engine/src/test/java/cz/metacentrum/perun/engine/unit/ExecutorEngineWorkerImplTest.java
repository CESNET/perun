package cz.metacentrum.perun.engine.unit;

import cz.metacentrum.perun.engine.scheduling.impl.ExecutorEngineWorkerImpl;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.scheduling.ExecutorEngineWorker;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskManager;

import java.io.File;

/**
 * Tests of ExecutorEngineWorkerImpl for processing GEN and SEND Tasks.
 * Tests use bash script located in project sources: /gen/passwd and /send/passwd.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ExecutorEngineWorkerImplTest extends AbstractEngineTest implements TaskResultListener {

	private final static Logger log = LoggerFactory.getLogger(ExecutorEngineWorkerImplTest.class);

	@Autowired
	private BeanFactory beanFactory;
	@Autowired
	SchedulingPool schedulingPool;
	@Autowired
	TaskManager taskManager;
	@Autowired
	TaskResultDao taskResultDao;

	private int count = 0;

	@Test
	public void runGenTest() throws Exception {
		System.out.println("ExecutorEngineWorkerImpl.runGenTest");

		ExecutorEngineWorker worker = (ExecutorEngineWorker) beanFactory.getBean("executorEngineWorker");
		log.debug("task " + task_gen.toString());
		schedulingPool.addToPool(task_gen);
		for (Task task : taskManager.listAllTasks(engineId)) {
			log.debug("task in db " + ((task == null) ? "null" : task.toString()));
		}
		count = 0;
		// for test there is no send subfolder, let's fake root
		((ExecutorEngineWorkerImpl)worker).setGenDirectory(new File("/"));
		worker.setTask(task_gen);
		worker.setExecService(task_gen.getExecService());
		worker.setFacility(task_gen.getFacility());
		worker.setDestination(destination1);
		worker.setResultListener(this);
		worker.run();
		Assert.isTrue(count == 1, "count 1");
	}

	@Test
	public void runSendTest() throws Exception {
		System.out.println("ExecutorEngineWorkerImpl.runSendTest");

		ExecutorEngineWorker worker = (ExecutorEngineWorker) beanFactory.getBean("executorEngineWorker");
		log.debug("task " + task1.toString());
		// add task to local db
		schedulingPool.addToPool(task1);
		for (Task task : taskManager.listAllTasks(engineId)) {
			log.debug("task in db " + ((task == null) ? "null" : task.toString()));
		}
		count = 0;
		// for test there is no send subfolder, let's fake root
		((ExecutorEngineWorkerImpl)worker).setSendDirectory(new File("/"));
		worker.setTask(task1);
		worker.setExecService(task1.getExecService());
		worker.setFacility(task1.getFacility());
		worker.setDestination(destination1);
		worker.setResultListener(this);
		worker.run();
		Assert.isTrue(count == 1, "count 1");
	}

	@Override
	public void onTaskDestinationDone(Task task, Destination destination, TaskResult result) {
		count += 1;
	}

	@Override
	public void onTaskDestinationError(Task task, Destination destination, TaskResult result) {
		Assert.isTrue(false);
	}

	@After
	public void cleanup() {
		schedulingPool.removeTask(task1);
		schedulingPool.removeTask(task_gen);
	}
}
