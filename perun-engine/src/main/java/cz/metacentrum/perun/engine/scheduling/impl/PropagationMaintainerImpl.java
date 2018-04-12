package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.PropagationMaintainer;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.SendTask;
import cz.metacentrum.perun.taskslib.model.SendTask.SendTaskStatus;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;

@org.springframework.stereotype.Service(value = "propagationMaintainer")
public class PropagationMaintainerImpl implements PropagationMaintainer {

	private final static Logger log = LoggerFactory.getLogger(PropagationMaintainerImpl.class);

	/**
	 * After how many minutes is processing Task considered as stuck and re-scheduled.
	 */
	private final static int rescheduleTime = 180;

	@Autowired
	private BlockingBoundedHashMap<Integer, Task> generatingTasks;
	@Autowired
	private BlockingBoundedHashMap<Pair<Integer, Destination>, SendTask> sendingSendTasks;

	private SchedulingPool schedulingPool;
	private JMSQueueManager jmsQueueManager;

	// ----- setters ------------------------------

	public JMSQueueManager getJmsQueueManager() {
		return jmsQueueManager;
	}

	@Autowired
	public void setJmsQueueManager(JMSQueueManager jmsQueueManager) {
		this.jmsQueueManager = jmsQueueManager;
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	@Autowired
	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	// ----- methods ------------------------------

	public void endStuckTasks() {

		// handle stuck GEN tasks
		for (Task task : generatingTasks.values()) {
			Date startTime = task.getGenStartTime();
			int howManyMinutesAgo = 0;
			if(startTime != null) {
				howManyMinutesAgo = (int) (System.currentTimeMillis() - startTime.getTime()) / 1000 / 60;
			}
			// If too much time has passed something is broken
			if (startTime == null || howManyMinutesAgo >= rescheduleTime) {
				abortTask(task, TaskStatus.GENERROR);
			}
		}

		// handle stuck SEND tasks
		for (SendTask sendTask : sendingSendTasks.values()) {
			Date startTime = sendTask.getStartTime();
			int howManyMinutesAgo = 0;
			if(startTime != null) {
				howManyMinutesAgo = (int) (System.currentTimeMillis() - startTime.getTime()) / 1000 / 60;
			}
			// If too much time has passed something is broken
			if (startTime == null || howManyMinutesAgo >= rescheduleTime) {
				sendTask.setStatus(SendTaskStatus.ERROR);
				Future<SendTask> sendTaskFuture = null;
				try {
					sendTaskFuture = schedulingPool.removeSendTaskFuture(
							sendTask.getId().getLeft(), sendTask.getId().getRight());
				} catch (TaskStoreException e) {
					log.error("[{}] Failed during removal of SendTaskFuture for {} from SchedulingPool: {}", sendTask.getId().getLeft(), sendTask, e);
				}
				if (sendTaskFuture == null) {
					log.error("[{}] Stale SendTask {} was not removed. For some reason, SendTask is kept in 'sendingSendTasks' but actually has not SendTask futures.", sendTask.getId().getLeft(), sendTask);
					log.error("  - Probably SendCollector failed and SendTask is kept in the structure, removing !!");
					// probably because when we cancel send tasks, we might not be able to remove them all
					// as mentioned in SchedulingPoolImpl TODOs in cancelSendTasks() method
					sendingSendTasks.remove(sendTask.getId());
				} else {
					// report Task result only for Tasks, which had future
					TaskResult taskResult = null;
					try {
						taskResult = schedulingPool.createTaskResult(sendTask.getId().getLeft(),
								sendTask.getDestination().getId(),
								sendTask.getStderr(), sendTask.getStdout(),
								sendTask.getReturnCode(), sendTask.getTask().getService());
						jmsQueueManager.reportTaskResult(taskResult);
					} catch (JMSException e) {
						log.error("[{}] Error trying to reportTaskResult {} of {} to Dispatcher: {}", sendTask.getId().getLeft(), taskResult, sendTask.getTask(), e);
					}
				}
			}
		}

		// check all known tasks
		Collection<Task> allTasks = schedulingPool.getAllTasks();
		if(allTasks == null) {
			return;
		}

		for(Task task : allTasks) {
			switch(task.getStatus()) {

				case WAITING:
					/*
					Such Tasks should never be in Engine, (only in Dispatcher) since when they are sent to Engine,
					status is set to PLANNED in both components. If they are already present in SchedulingPool
					(Engine), then adding of new (same) Task is skipped and previous processing is finished first.
					=> just remove such nonsense from SchedulingPool and don't spam Dispatcher
					 */
					try {
						// TODO - can such Task be in any structure like generating/sending/newTasks/generatedTasks ?
						schedulingPool.removeTask(task.getId());
						log.error("[{}] Task in WAITING state shouldn't be in Engine at all, silently removing from SchedulingPool.", task.getId());
					} catch (TaskStoreException ex) {
						log.error("[{}] Failed during removal of WAITING Task from SchedulingPool. Such Task shouldn't be in Engine at all: {}", task.getId(), ex);
					}

				case PLANNED:
					/*
					Check tasks, that should be put to scheduling pool by EventProcessorImpl and taken by GenPlanner.
					Tasks might be like that, because adding to BlockingDeque has limit on Integer#MAX_SIZE
					(while EventProcessorImpl adds Task to the scheduling pool).
					Also if GenPlanner implementation fails it might take Task from the BlockingDeque but doesn't change
					its status or doesn't put it between generatingTasks.
					 */
					BlockingDeque<Task> newTasks = schedulingPool.getNewTasksQueue();
					if(!newTasks.contains(task)) {
						try {
							schedulingPool.addTask(task);
						} catch (TaskStoreException e) {
							log.error("Could not save Task {} into Engine SchedulingPool because of {}, setting to ERROR",
									task, e);
							abortTask(task, TaskStatus.ERROR);
						}
					}
					break;

				case GENERATING:
					/*
					This is basically the same check as for the GENERATING Tasks above,
					but now for Tasks missing in "generatingTasks" blocking bounded hash map.
					!! We can't abort GENERATING Tasks with startTime=NULL here,
					because they are waiting to be started at genCompletionService#blockingSubmit() !!
					*/
					Date startTime = task.getGenStartTime();
					int howManyMinutesAgo = 0;
					if(startTime != null) {
						howManyMinutesAgo = (int) (System.currentTimeMillis() - startTime.getTime()) / 1000 / 60;
					}
					// If task started too long ago and is not in generating structure anymore
					if (howManyMinutesAgo >= rescheduleTime && !generatingTasks.values().contains(task)) {
						// probably GenCollector failed to pick task -> abort
						abortTask(task, TaskStatus.GENERROR);
					}
					break;

				case GENERROR:
				case GENERATED:
					/*
					Check Tasks, which should be processed by GenCollector and taken by SendPlanner or reported as GENERROR to Dispatcher.
					Task must have endTime set by GenWorker, otherwise it failed completely and should be reported as error.
					If either of GenCollector and SendPlanner fails to process generated tasks, it's missing in generatedTasksQueue.
					*/
					Date genEndTime = task.getGenEndTime();
					howManyMinutesAgo = 0;
					if(genEndTime != null) {
						howManyMinutesAgo = (int) (System.currentTimeMillis() - genEndTime.getTime()) / 1000 / 60;
					}
					// If too much time has passed for Task and its not present in generatedTasksQueue, something is broken
					if((genEndTime == null || howManyMinutesAgo >= rescheduleTime) && !schedulingPool.getGeneratedTasksQueue().contains(task)) {
						abortTask(task, TaskStatus.GENERROR);
					}
					break;

				case SENDING:
					// TODO - if SendPlanner fails, we might need to switch SENDING Tasks to SENDERROR and cancel any running SendTaskFutures
					// TODO   since Task is switched to SENDING before blockingSubmit() of any SendWorker.
					break;

				case SENDERROR:
					Date endTime = task.getSendEndTime();
					howManyMinutesAgo = 0;
					if(endTime != null) {
						howManyMinutesAgo = (int) (System.currentTimeMillis() - endTime.getTime()) / 1000 / 60;
					}
					// If too much time has passed something is broken
					if(endTime == null || howManyMinutesAgo >= rescheduleTime) {
						abortTask(task, TaskStatus.SENDERROR);
					}
					break;

				case ERROR:
					break;

				case DONE:
					// report it
					try {
						jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), System.currentTimeMillis());
					} catch (JMSException e) {
						log.error("[{}] Error trying to reportTaskStatus of {} to Dispatcher: {}", task.getId(), task, e);
					}
					try {
						schedulingPool.removeTask(task.getId());
					} catch (TaskStoreException e) {
						log.error("[{}] Task could not be removed from SchedulingPool: {}", task.getId(), e);
					}
					break;

				default:
					// unknown state
					log.debug("[{}] Failing to default, status was: {}", task.getId(), task.getStatus());
					abortTask(task, TaskStatus.ERROR);
			}
		}
	}


	private void abortTask(Task task, TaskStatus status) {
		log.warn("[{}] Task {} found in unexpected state, switching to {} ", task.getId(), task, status);
		task.setStatus(status);
		Task removed = null;
		try {
			removed = schedulingPool.removeTask(task);
			// the removal from pool also cancels all task futures, which in turn
			// makes the completion service collect the task and remove it from its executingTasks map
		} catch (TaskStoreException e) {
			log.error("[{}] Failed during removal of Task {} from SchedulingPool: {}", task.getId(), task, e);
		}
		if (removed != null) {
			// report status only if Task was actually removed from the pool, otherwise its
			// some kind of inconsistency and we don't want to spam dispatcher - it will mark it as error on its own
			try {
				jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), System.currentTimeMillis());
			} catch (JMSException e) {
				log.error("[{}] Error trying to reportTaskStatus of {} to Dispatcher: {}", task.getId(), task, e);
			}
		} else {
			log.error("[{}] Stale Task {} was not removed and not reported to dispatcher.", task.getId(), task);
			log.error("  - This is nonsense - why we abort the Task taken from AllTasks but we can remove it from it ??");
		}

	}
}
