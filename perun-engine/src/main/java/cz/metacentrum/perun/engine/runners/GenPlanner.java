package cz.metacentrum.perun.engine.runners;


import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.GenWorker;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingGenExecutorCompletionService;
import cz.metacentrum.perun.engine.scheduling.impl.GenWorkerImpl;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERATING;

/**
 * This class takes all new Tasks received from Dispatcher, and puts in a queue where they wait to be generated.
 */
public class GenPlanner extends AbstractRunner {
	private final static Logger log = LoggerFactory
			.getLogger(GenPlanner.class);
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private BlockingGenExecutorCompletionService genCompletionService;
	@Autowired
	private JMSQueueManager jmsQueueManager;
	private File directory;

	public GenPlanner() {}

	public GenPlanner(SchedulingPool schedulingPool, BlockingGenExecutorCompletionService genCompletionService, JMSQueueManager jmsQueueManager) {
		this.schedulingPool = schedulingPool;
		this.genCompletionService = genCompletionService;
		this.jmsQueueManager = jmsQueueManager;
	}

	@Override
	public void run() {
		BlockingDeque<Task> newTasks = schedulingPool.getNewTasksQueue();
		while (!shouldStop()) {
			try {
				log.debug("Getting new Task in the newTasks BlockingDeque");
				Task task = newTasks.take();
				GenWorker worker = new GenWorkerImpl(task, directory);
				Future<Task> taskFuture = genCompletionService.blockingSubmit(worker);
				schedulingPool.addGenTaskFutureToPool(task.getId(), taskFuture);
				task.setStatus(GENERATING);
				task.setGenStartTime(new Date(System.currentTimeMillis()));
				try {
					jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), task.getGenStartTime().getTime());
				} catch (JMSException e) {
					log.warn("Could not send Tasks [{}] GEN status update.", task);
				}
			} catch (InterruptedException e) {
				String errorStr = "Thread executing GEN tasks was interrupted.";
				log.error(errorStr, e);
				throw new RuntimeException(errorStr, e);
			}
		}
	}

	@Autowired
	public void setPropertiesBean(Properties propertiesBean) {
		log.debug("TESTSTR --> Gen property bean set");
		if (propertiesBean != null) {
			log.debug("TESTSTR --> Gen script path from properties is {}", propertiesBean.getProperty("engine.genscript.path"));
			directory = new File(propertiesBean.getProperty("engine.genscript.path"));
		}
	}
}
