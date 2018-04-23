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

import static cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus.SENDING;

/**
 * This class represents permanently running thread, which should run in a single instance.
 *
 * It takes all GENERATED Tasks from generatedTasks blocking queue provided by GenCollector
 * and creates SendTask and SendWorker for each Destination and put them to BlockingSendExecutorCompletionService.
 * Processing waits on call of blockingSubmit() for each SendWorker.
 *
 * Expected Task status change GENERATED -> SENDING is reported to Dispatcher.
 * For Tasks without any Destination, status changes GENERATED -> ERROR and Task is removed from SchedulingPool (Engine).
 *
 * @see SchedulingPool#getGeneratedTasksQueue()
 * @see SendTask
 * @see SendWorkerImpl
 * @see BlockingSendExecutorCompletionService
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class SendPlanner extends AbstractRunner {

	private final static Logger log = LoggerFactory.getLogger(SendPlanner.class);

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
				// set Task status immediately
				// no destination -> ERROR
				// has destinations -> SENDING
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
						log.error("[{}] Generated Task without destinations could not be removed from SchedulingPool: {}", task.getId(), e);
					}
					// skip to next generated Task
					continue;
				}
				// Task has destinations
				task.setStatus(Task.TaskStatus.SENDING);
				// TODO - would be probably better to have this as one time call after first SendWorker is submitted
				// TODO   but then processing stuck tasks must reflect, that SENDING task might have sendStartTime=NULL
				task.setSendStartTime(new Date(System.currentTimeMillis()));

				schedulingPool.addSendTaskCount(task, task.getDestinations().size());
				try {
					jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), task.getSendStartTime().getTime());
				} catch (JMSException e) {
					jmsLogError(task);
				}

				// create SendTask and SendWorker for each Destination
				for (Destination destination : task.getDestinations()) {
					// submit for execution
					SendTask sendTask = new SendTask(task, destination);
					SendWorker worker = new SendWorkerImpl(sendTask, directory);
					sendCompletionService.blockingSubmit(worker);
				}

			} catch (InterruptedException e) {

				String errorStr = "Thread planning SendTasks was interrupted.";
				log.error(errorStr);
				throw new RuntimeException(errorStr, e);

			} catch (Throwable ex) {
				log.error("Unexpected exception in SendPlanner thread. Stuck Tasks will be cleaned by PropagationMaintainer#endStuckTasks() later. {}", ex);
			}
		}
	}

	private void jmsLogError(Task task) {
		log.warn("[{}] Could not send SEND status update to {} to Dispatcher.", task.getId(), task.getStatus());
	}

	@Autowired
	public void setPropertiesBean(Properties propertiesBean) {
		if (propertiesBean != null) {
			directory = new File(propertiesBean.getProperty("engine.sendscript.path"));
		}
	}

}
