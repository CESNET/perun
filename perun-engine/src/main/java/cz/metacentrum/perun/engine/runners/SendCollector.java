package cz.metacentrum.perun.engine.runners;

import cz.metacentrum.perun.core.api.Destination;
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
import java.util.Objects;

/**
 * This class represents permanently running thread, which should run in a single instance.
 *
 * It takes all done SEND SendTasks (both successfully and not) from BlockingSendExecutorCompletionService.
 * For each SendTask its outcome is reported to Dispatcher as a TaskResult.
 *
 * If any of SendTasks fails its processing (has ERROR status), whole Task is set to SENDERROR.
 * Otherwise SENDING or DONE is kept for whole Task.
 * Once all SendTasks are finished Task status is reported to Dispatcher.
 *
 * Expected Task status change is SENDING -> DONE | SENDERROR based on all SendWorkers outcome.
 *
 * @see BlockingSendExecutorCompletionService
 * @see SchedulingPool#createTaskResult(int, int, String, String, int, Service)
 * @see SchedulingPool#decreaseSendTaskCount(Task, int)
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
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

			SendTask sendTask = null;
			Task task = null;
			Service service = null;
			Destination destination = null;
			String stderr;
			String stdout;
			int returnCode;

			// FIXME - doesn't provide nice output and clog the log
			log.debug(schedulingPool.getReport());

			try {

				sendTask = sendCompletionService.blockingTake();
				task = sendTask.getTask();
				/*
				 Set Task "sendEndTime" immediately for each done SendTask, so it's not considered as stuck
				 by PropagationMaintainer#endStuckTasks().
				 Like this we can maximally propagate for "rescheduleTime" for each Destination and not
				 all Destinations (whole Task). Default rescheduleTime is 3 hours * no.of destinations.
				 */
				task.setSendEndTime(new Date(System.currentTimeMillis()));
				if (!Objects.equals(task.getStatus(), Task.TaskStatus.SENDERROR)) {
					// keep SENDING status only if if task previously doesn't failed
					task.setStatus(Task.TaskStatus.SENDING);
				}
				destination = sendTask.getDestination();
				stderr = sendTask.getStderr();
				stdout = sendTask.getStdout();
				returnCode = sendTask.getReturnCode();
				service = sendTask.getTask().getService();

			} catch (InterruptedException e) {

				String errorStr = "Thread collecting sent SendTasks was interrupted.";
				log.error(errorStr + ": {}", e);
				throw new RuntimeException(errorStr, e);

			} catch (TaskExecutionException e) {

				task = e.getTask();
				/*
				 Set Task "sendEndTime" immediately for each done SendTask, so it's not considered as stuck
				 by PropagationMaintainer#endStuckTasks().
				 Like this we can maximally propagate for "rescheduleTime" for each Destination and not
				 all Destinations (whole Task). Default rescheduleTime is 3 hours * no.of destinations.
				 */
				task.setSendEndTime(new Date(System.currentTimeMillis()));
				// set SENDERROR status immediately as first SendTask (Destination) fails
				task.setStatus(Task.TaskStatus.SENDERROR);
				destination = e.getDestination();
				stderr = e.getStderr();
				stdout = e.getStdout();
				returnCode = e.getReturnCode();
				service = task.getService();

				log.error("[{}] Error occurred while sending Task to destination {}", task.getId(), e.getDestination());

			} catch (Throwable ex) {
				log.error("Unexpected exception in SendCollector thread. Stuck Tasks will be cleaned by PropagationMaintainer#endStuckTasks() later. {}", ex);
				continue;
			}

			// this is just interesting cross-check
			if (schedulingPool.getTask(task.getId()) == null) {
				log.warn("[{}] Task retrieved from SendTask is no longer in SchedulingPool. Probably cleaning thread removed it before completion. " +
						"This might create possibility of running GEN and SEND of same Task together!", task.getId());
			}

			try {

				// report TaskResult to Dispatcher for this SendTask (Destination)
				jmsQueueManager.reportTaskResult(schedulingPool.createTaskResult(task.getId(), destination.getId(), stderr, stdout, returnCode, service));

			} catch (JMSException e1) {
				log.error("[{}] Error trying to reportTaskResult for Destination: {} to Dispatcher: {}", task.getId(), destination, e1);
			}

			try {

				// Decrease SendTasks count for Task
				// Consequently, if count is <=1, Task is reported to Dispatcher
				// as DONE/SENDERROR and removed from SchedulingPool (Engine).
				schedulingPool.decreaseSendTaskCount(task, 1);

			} catch (TaskStoreException e) {
				log.error("[{}] Task {} could not be removed from SchedulingPool: {}", task.getId(), task, e);
			}
		}
	}

}
