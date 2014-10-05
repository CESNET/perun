package cz.metacentrum.perun.engine.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.TestBase;
import cz.metacentrum.perun.engine.scheduling.ExecutorEngineWorker;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.engine.scheduling.impl.TaskExecutorEngineImpl;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskManager;

public class ExecutorEngineWorkerImplTest extends TestBase implements TaskResultListener {
    private final static Logger log = LoggerFactory.getLogger(ExecutorEngineWorkerImplTest.class);

	@Autowired
	private Destination destination1;
	@Autowired
    private BeanFactory beanFactory;
	@Autowired
	private Task task1;
	@Autowired
	private Task task_gen;
	@Autowired
	SchedulingPool schedulingPool;
	@Autowired
	TaskManager taskManager;
	@Autowired 
	TaskResultDao taskResultDao;
	
	private int count = 0;
	
	@IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void runSendTest() throws InternalErrorException {
		ExecutorEngineWorker worker = (ExecutorEngineWorker) beanFactory.getBean("executorEngineWorker");
		log.debug("task " + task1.toString());
		schedulingPool.addToPool(task1);
		for(Task task : taskManager.listAllTasks(0)) {
				log.debug("task in db " + ((task == null) ? "null" : task.toString()));
		}
		count = 0;
		worker.setTask(task1);
		worker.setExecService(task1.getExecService());
		worker.setFacility(task1.getFacility());
		worker.setDestination(destination1);
		worker.setResultListener(this);
		worker.run();
		log.debug("count is {}", count);
		Assert.isTrue(count == 1, "count 1");
	}

	@IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void runGenTest() throws InternalErrorException {
		ExecutorEngineWorker worker = (ExecutorEngineWorker) beanFactory.getBean("executorEngineWorker");
		log.debug("task " + task_gen.toString());
		schedulingPool.addToPool(task_gen);
		for(Task task : taskManager.listAllTasks(0)) {
				log.debug("task in db " + ((task == null) ? "null" : task.toString()));
		}
		count = 0;
		worker.setTask(task_gen);
		worker.setExecService(task_gen.getExecService());
		worker.setFacility(task_gen.getFacility());
		worker.setDestination(destination1);
		worker.setResultListener(this);
		worker.run();
		Assert.isTrue(count == 1, "count 1");
	}

	@Override
	public void onTaskDestinationDone(Task task, Destination destination,
			TaskResult result) {
		count += 1;
	}

	@Override
	public void onTaskDestinationError(Task task, Destination destination,
			TaskResult result) {
		Assert.isTrue(false);;
	}
	
	@After
	public void cleanup() {
		taskResultDao.clearAll();
		schedulingPool.removeTask(task1);
		schedulingPool.removeTask(task_gen);
	}
}
