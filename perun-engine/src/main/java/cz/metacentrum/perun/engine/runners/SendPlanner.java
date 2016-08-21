package cz.metacentrum.perun.engine.runners;


import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.SendWorker;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingSendExecutorCompletionService;
import cz.metacentrum.perun.engine.scheduling.impl.SendWorkerImpl;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.SendTask;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

import static cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus.SENDING;

/**
 * Takes all Tasks planned for sending, creates a SendTask for every one of their Destinations and puts it into queue,
 * where they wait to be sent.
 */
public class SendPlanner extends AbstractRunner {
	private final static Logger log = LoggerFactory
			.getLogger(SendPlanner.class);
	@Autowired
	private BlockingSendExecutorCompletionService sendCompletionService;
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private JMSQueueManager jmsQueueManager;
	private File directory;

	public SendPlanner() {
	}

	public SendPlanner(BlockingSendExecutorCompletionService sendCompletionService, SchedulingPool schedulingPool,
	                   JMSQueueManager jmsQueueManager) {
		this.sendCompletionService = sendCompletionService;
		this.schedulingPool = schedulingPool;
		this.jmsQueueManager = jmsQueueManager;
	}

	@Override
	public void run() {
		BlockingQueue<Task> generatedTasks = schedulingPool.getGeneratedTasksQueue();
		while (!shouldStop()) {
			try {
				Task task = generatedTasks.take();
				if (task.getDestinations().isEmpty()) {
					task.setStatus(Task.TaskStatus.ERROR);
					try {
						jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), System.currentTimeMillis());
					} catch (JMSException e) {
						jmsLogError(task);
					}
					try {
						schedulingPool.removeTask(task);
					} catch (TaskStoreException e) {
						log.error("Task {} could not be removed from SchedulingPool", e);
					}
					continue;
				}
				task.setStatus(Task.TaskStatus.SENDING);
				task.setSendStartTime(new Date(System.currentTimeMillis()));
				schedulingPool.addSendTaskCount(task.getId(), task.getDestinations().size());
				try {
					jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), task.getSendStartTime().getTime());
				} catch (JMSException e) {
					jmsLogError(task);
				}


				for (Destination destination : task.getDestinations()) {
					SendTask sendTask = new SendTask(task, destination);
					SendWorker worker = new SendWorkerImpl(sendTask, directory);

					Future<SendTask> sendFuture = sendCompletionService.blockingSubmit(worker);
					schedulingPool.addSendTaskFuture(sendTask, sendFuture);
					sendTask.setStartTime(new Date(System.currentTimeMillis()));
					sendTask.setStatus(SENDING);
				}

			} catch (InterruptedException e) {
				String errorStr = "Thread planning SendTasks was interrupted.";
				log.error(errorStr);
				throw new RuntimeException(errorStr, e);
			}
		}
	}

	private void jmsLogError(Task task) {
		log.warn("Could not send status update to Dispatcher for [{}] .", task);
	}

	@Autowired
	public void setPropertiesBean(Properties propertiesBean) {
		log.debug("TESTSTR --> Send property bean set");
		if (propertiesBean != null) {
			log.debug("TESTSTR --> Send script path from properties is {}", propertiesBean.getProperty("engine.sendscript.path"));
			directory = new File(propertiesBean.getProperty("engine.sendscript.path"));
		}
	}
}
