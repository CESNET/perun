package cz.metacentrum.perun.engine.runners;


import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingSendExecutorCompletionService;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.SendTask;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import java.util.Date;

import static cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus.SENT;

/**
 * This class takes all executed SendTasks (both successfully and not) and reports their state to the Dispatcher.
 * It also watches for the number of SendTasks associated with their parent Task, which will be removed from Engine
 * once all its SendTask are executed.
 */
public class SendCollector extends AbstractRunner {

	private final static Logger log = LoggerFactory.getLogger(SendCollector.class);

	@Autowired
	private BlockingSendExecutorCompletionService sendCompletionService;
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private JMSQueueManager jmsQueueManager;

	public SendCollector() {
	}

	public SendCollector(BlockingSendExecutorCompletionService sendCompletionService, SchedulingPool schedulingPool, JMSQueueManager jmsQueueManager) {
		this.sendCompletionService = sendCompletionService;
		this.schedulingPool = schedulingPool;
		this.jmsQueueManager = jmsQueueManager;
	}

	@Override
	public void run() {
		while (!shouldStop()) {
			Task.TaskStatus status = Task.TaskStatus.SENDERROR;
			int taskId;
			String stderr;
			String stdout;
			int returnCode;
			Service service = null;
			log.debug(schedulingPool.getReport());
			SendTask sendTask = null;
			Destination destination = null;
			try {
				sendTask = sendCompletionService.blockingTake();
				status = null;
				taskId = sendTask.getId().getLeft();
				sendTask.setStatus(SENT);
				sendTask.setEndTime(new Date(System.currentTimeMillis()));
				destination = sendTask.getDestination();
				stderr = sendTask.getStderr();
				stdout = sendTask.getStdout();
				returnCode = sendTask.getReturnCode();
				service = sendTask.getTask().getService();
			} catch (InterruptedException e) {
				log.error("Thread collecting sent SendTasks was interrupted: {}", e);
				continue;
			} catch (TaskExecutionException e) {
				//log.error("Execution exception: {}", e); - is already logged as EngineException
				Pair<Integer, Destination> id = (Pair<Integer, Destination>) e.getId();
				Task task = schedulingPool.getTask(id.getLeft());
				if (task != null) {
					log.warn("[{}] Error occurred while sending {} to destination {}", task.getId(), task, id.getRight());
					taskId = task.getId();
					destination = id.getRight();
					stderr = e.getStderr();
					stdout = e.getStdout();
					returnCode = e.getReturnCode();
					service = task.getService();
				} else {
					log.error("[{}] Error occurred while sending {} to destination {}", id.getLeft(), task, id.getRight());
					log.error("[{}] Task no longer in pool after sending to destination {}", id.getLeft(), id.getRight());
					// we can't get service from exception on null task, hence we can't report TaskResult
					taskId = id.getLeft();
					destination = id.getRight();
					stderr = e.getStderr();
					stdout = e.getStdout();
					returnCode = e.getReturnCode();
				}
			} catch (Throwable ex) {
				log.error("Unexpected exception in SendCollector thread: {}.", ex);
				continue;
			}

			Task task = schedulingPool.getTask(taskId);

			try {
				//log.debug("TESTSTR --> Sending TaskResult: taskid {}, destionationId {}, stderr {}, stdout {}, " +
				//		"returnCode {}, service {}", new Object[]{taskId, destination.getId(), stderr, stdout, returnCode, service});
				if (service != null) {
					jmsQueueManager.reportTaskResult(schedulingPool.createTaskResult(taskId, destination.getId(), stderr, stdout, returnCode, service));
				}
			} catch (JMSException e1) {
				if (sendTask != null && sendTask.getId() != null) {
					log.error("[{}] Error trying to reportTaskResult of {} to Dispatcher: {}", sendTask.getId().getLeft(), sendTask, e1);
				} else if (sendTask != null && sendTask.getTask() != null) {
					log.error("[{}] Error trying to reportTaskResult of {} to Dispatcher: {}", sendTask.getTask().getId(), sendTask, e1);
				} else {

					log.error("[{}] Error trying to reportTaskResult of {} to Dispatcher: {}", (task != null) ? task.getId() : "unknown", sendTask, e1);
				}
			}

			if (status != null && task != null) {
				task.setStatus(status);
				task.setSendEndTime(new Date(System.currentTimeMillis()));
			}

			try {
				//schedulingPool.decreaseSendTaskCount(taskId, 1);
				// always try to remove send task future even on TaskExecutionException.
				schedulingPool.removeSendTaskFuture(taskId, destination);
			} catch (TaskStoreException e) {
				log.error("[{}] Task {} could not be removed from SchedulingPool: {}", taskId, task, e);
			}
		}
	}

}
