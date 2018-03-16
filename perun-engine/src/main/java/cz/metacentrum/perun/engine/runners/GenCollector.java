package cz.metacentrum.perun.engine.runners;


import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingGenExecutorCompletionService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import java.util.Date;
import java.util.concurrent.BlockingDeque;

import static cz.metacentrum.perun.taskslib.model.Task.TaskStatus.GENERROR;

/**
 * This class takes Task which were generated (both successfully and not), reports their state change to the Dispatcher
 * and puts them in queue of Tasks waiting to be sent to their Destinations.
 */
public class GenCollector extends AbstractRunner {
	private final static Logger log = LoggerFactory
			.getLogger(GenCollector.class);
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private BlockingGenExecutorCompletionService genCompletionService;
	@Autowired
	private JMSQueueManager jmsQueueManager;

	public GenCollector() {
	}

	public GenCollector(SchedulingPool schedulingPool, BlockingGenExecutorCompletionService genCompletionService, JMSQueueManager jmsQueueManager) {
		this.schedulingPool = schedulingPool;
		this.genCompletionService = genCompletionService;
		this.jmsQueueManager = jmsQueueManager;
	}

	@Override
	public void run() {
		BlockingDeque<Task> generatedTasks = schedulingPool.getGeneratedTasksQueue();
		while (!shouldStop()) {
			try {
				Task task = genCompletionService.blockingTake();
				if (task.isPropagationForced()) {
					generatedTasks.putFirst(task);
				} else {
					generatedTasks.put(task);
				}
				try {
					jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), task.getGenEndTime().getTime());
				} catch (JMSException e) {
					jmsErrorLog(task.getId());
				}
			} catch (InterruptedException e) {
				String errorStr = "Thread collecting generated Tasks was interrupted.";
				log.error(errorStr);
				throw new RuntimeException(errorStr, e);
			} catch (TaskExecutionException e) {
				Integer id = (Integer) e.getId();
				try {
					jmsQueueManager.reportTaskStatus(id, GENERROR, System.currentTimeMillis());
				} catch (JMSException e1) {
					jmsErrorLog(id);
				}
				try {
					schedulingPool.removeTask(id);
				} catch (TaskStoreException e1) {
					log.error("Could not remove Task with id {} from SchedulingPool", id, e1);
				}
			}
		}
	}

	private void jmsErrorLog(Integer id) {
		log.warn("Could not send GEN status update to task with id {}.", id);
	}
}
