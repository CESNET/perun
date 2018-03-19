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
import java.util.concurrent.Future;

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
			Service service;
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
				String errorStr = "Thread collecting sent SendTasks was interrupted.";
				log.error(errorStr);
				throw new RuntimeException(errorStr, e);
			} catch (TaskExecutionException e) {
				log.error("Execution exception: {}", e);
				Pair<Integer, Destination> id = (Pair<Integer, Destination>) e.getId();
				Task task = schedulingPool.getTask(id.getLeft());
				log.warn("Error occurred while sending {} to destination {}", task, id.getRight());
				taskId = task.getId();
				destination = id.getRight();
				stderr = e.getStderr();
				stdout = e.getStdout();
				returnCode = e.getReturnCode();
				service = task.getService();
			} catch (Exception ex) {
				log.error("Unexpected exception in SendCollector thread: {}.", ex);
				// TODO - determine, what should be done since we might not get TaskID here
				throw ex;
			}
			Task task = schedulingPool.getTask(taskId);

			try {
				log.debug("TESTSTR --> Sending TaskResult: taskid {}, destionationId {}, stderr {}, stdout {}, " +
						"returnCode {}, service {}", new Object[]{taskId, destination.getId(), stderr, stdout, returnCode, service});
				jmsQueueManager.reportTaskResult(schedulingPool.createTaskResult(taskId, destination.getId(), stderr, stdout,
						returnCode, service));
			} catch (JMSException e1) {
				jmsErrorLog(taskId, destination.getId());
			}

			if (status != null) {
				task.setStatus(status);
				task.setSendEndTime(new Date(System.currentTimeMillis()));
			}
			try {
				//schedulingPool.decreaseSendTaskCount(taskId, 1);
				schedulingPool.removeSendTaskFuture(taskId, destination);
			} catch (TaskStoreException e) {
				log.error("Task {} could not be removed from SchedulingPool", e);
			}
		}
	}

	private void jmsErrorLog(Integer id, Integer destinationId) {
		log.warn("Could not send status update to SendTask with id {} and destination with id {}.", id, destinationId);
	}
}
