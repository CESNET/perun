package cz.metacentrum.perun.engine.scheduling.impl;

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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;

@org.springframework.stereotype.Service(value = "propagationMaintainer")
public class PropagationMaintainerImpl implements PropagationMaintainer {

	private final static Logger log = LoggerFactory.getLogger(PropagationMaintainerImpl.class);

	/**
	 * After how many minutes is processing Task considered as stuck and re-scheduled.
	 */
	private final static int rescheduleTime = 180;

	private BlockingGenExecutorCompletionService generatingTasks;
	private BlockingSendExecutorCompletionService sendingSendTasks;
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

	public BlockingGenExecutorCompletionService getGeneratingTasks() {
		return generatingTasks;
	}

	@Autowired
	public void setGeneratingTasks(BlockingGenExecutorCompletionService generatingTasks) {
		this.generatingTasks = generatingTasks;
	}

	public BlockingSendExecutorCompletionService getSendingSendTasks() {
		return sendingSendTasks;
	}

	@Autowired
	public void setSendingSendTasks(BlockingSendExecutorCompletionService sendingSendTasks) {
		this.sendingSendTasks = sendingSendTasks;
	}

	// ----- methods ------------------------------

	public void endStuckTasks() {

		// handle stuck GEN tasks
		for (Map.Entry<Future<Task>,Task> generatingTask : generatingTasks.getRunningTasks().entrySet()) {

			Task task = generatingTask.getValue();
			Future<Task> future = generatingTask.getKey();

			LocalDateTime startTime = task.getGenStartTime();
			long howManyMinutesAgo = 0;
			if(startTime != null) {
				howManyMinutesAgo = ChronoUnit.MINUTES.between(startTime, LocalDateTime.now());
			}
			if (startTime == null) {

				// by implementation can't happen, we set time before adding to the generatingTasksMap
				log.error("[{}] Task in generatingTasks has no start time. Shouldn't happen by implementation.", task.getId());

			} else if (howManyMinutesAgo >= rescheduleTime) {

				if (!future.isCancelled()) {
					// Cancel running GEN Task - we expect that it will be picked by GenCollector
					// and removed from the Engine.
					log.debug("[{}] Cancelling stuck generating Future<Task>.", task.getId());
					future.cancel(true);
				} else {
					// We cancelled Task in previous run, but it wasn't picked by GenCollector
					// GenCollector probably doesn't run -> abort task manually
					log.debug("[{}] Cancelled stuck generating Future<Task> was not picked by GenCollector, forcefully removing from Engine.", task.getId());
					generatingTasks.removeStuckTask(future); // to release semaphore
					abortTask(task, TaskStatus.GENERROR);
				}

			}

		}

		// handle stuck SEND tasks
		for (Map.Entry<Future<SendTask>,SendTask> sendingSendTask : sendingSendTasks.getRunningTasks().entrySet()) {

			SendTask sendTask = sendingSendTask.getValue();
			Future<SendTask> future = sendingSendTask.getKey();
			Task task = sendTask.getTask();

			Date startTime = sendTask.getStartTime();
			int howManyMinutesAgo = 0;
			if(startTime != null) {
				howManyMinutesAgo = (int) (System.currentTimeMillis() - startTime.getTime()) / 1000 / 60;
			}
			if (startTime == null) {

				// by implementation can't happen, we set time before adding to the generatingTasksMap
				log.error("[{}] SendTask in sendingSendTask has no start time for Destination {}. Shouldn't happen by implementation.", task.getId(), sendTask.getDestination());

			} else if (howManyMinutesAgo >= rescheduleTime) {

				sendTask.setStatus(SendTaskStatus.ERROR);
				if (!future.isCancelled()) {
					// Cancel running Send Task - we expect that it will be picked by SendCollector
					// and removed from the Engine if all SendTasks are done
					log.debug("[{}] Cancelling stuck sending Future<SendTask> for Destination: {}.", task.getId(), sendTask.getDestination());
					future.cancel(true);
				} else {

					log.debug("[{}] Cancelled stuck sending Future<SendTask> for Destination: {} was not picked by SendCollector, forcefully removing from Engine.", task.getId(), sendTask.getDestination());

					// We cancelled Task in previous run, but it wasn't picked by SendCollector
					// SendCollector probably doesn't run
					sendingSendTasks.removeStuckTask(future); // to release semaphore

					// make sure Task is switched to SENDERROR
					task.setSendEndTime(LocalDateTime.now());
					task.setStatus(TaskStatus.SENDERROR);

					// report result
					TaskResult taskResult = null;
					try {
						taskResult = schedulingPool.createTaskResult(task.getId(),
								sendTask.getDestination().getId(),
								sendTask.getStderr(), sendTask.getStdout(),
								sendTask.getReturnCode(), task.getService());
						jmsQueueManager.reportTaskResult(taskResult);
					} catch (JMSException e) {
						log.error("[{}] Error trying to reportTaskResult {} of {} to Dispatcher: {}", task.getId(), taskResult, task, e);
					}

					// lower counter for stuck SendTask if count <= 1 remove from Engine
					try {
						schedulingPool.decreaseSendTaskCount(task, 1);
					} catch (TaskStoreException e) {
						log.error("[{}] Task {} could not be removed from SchedulingPool: {}", task.getId(), task, e);
					}

				}

			}
		}

		// check all known Tasks

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
						log.warn("[{}] Task in WAITING state shouldn't be in Engine at all, silently removing from SchedulingPool.", task.getId());
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
							log.debug("[{}] Re-adding PLANNED Task back to pool and newTasks queue. Probably GenPlanner failed.", task.getId());
							schedulingPool.addTask(task);
						} catch (TaskStoreException e) {
							log.error("Could not save Task {} into Engine SchedulingPool because of {}, setting to ERROR", task, e);
							abortTask(task, TaskStatus.ERROR);
						}
					}
					break;

				case GENERATING:
					/*
					This is basically the same check as for the GENERATING Tasks above,
					but now for Tasks missing in "generatingTasks".
					!! We can't abort GENERATING Tasks with startTime=NULL here,
					because they are waiting to be started at genCompletionService#blockingSubmit() !!
					*/
					LocalDateTime startTime = task.getGenStartTime();
					long howManyMinutesAgo = 0;
					if(startTime != null) {
						howManyMinutesAgo = ChronoUnit.MINUTES.between(startTime, LocalDateTime.now());
					}
					// If task started too long ago and is not in generating structure anymore
					// somebody probably wrongly manipulated the structure
					if (howManyMinutesAgo >= rescheduleTime && !generatingTasks.getRunningTasks().values().contains(task)) {
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
					LocalDateTime genEndTime = task.getGenEndTime();
					howManyMinutesAgo = 0;
					if(genEndTime != null) {
						howManyMinutesAgo = ChronoUnit.MINUTES.between(genEndTime, LocalDateTime.now());
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

					LocalDateTime endTime = task.getSendEndTime();
					howManyMinutesAgo = 0;
					if(endTime != null) {
						howManyMinutesAgo = ChronoUnit.MINUTES.between(endTime, LocalDateTime.now());
					}
					// If too much time has passed something is broken
					if(endTime == null || howManyMinutesAgo >= rescheduleTime) {
						abortTask(task, TaskStatus.SENDERROR);
					}

					break;

				case ERROR:
					break;

				case DONE:

					/*
					 DONE Tasks are almost immediately reported to Dispatcher by schedulingPool#decreaseSendTaskCount().
					 Only way Task is stuck in DONE in scheduling pool is that implementation of removal from schedulingPool fails.
					 And in such case we can't fix it here anyway and spamming Dispatcher about finished state is pointless.
					 */

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
