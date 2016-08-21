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
					log.error("Failed during removal of SendTaskFuture {} from SchedulingPool", sendTaskFuture);
				}	
				if (sendTaskFuture == null) {
					log.error("Stale SendTask {} was not removed.", sendTask);
				}
				TaskResult taskResult = null;
				try {
					taskResult = schedulingPool.createTaskResult(sendTask.getId().getLeft(),
							sendTask.getDestination().getId(), 
							sendTask.getStderr(), sendTask.getStdout(),
							sendTask.getReturnCode(), sendTask.getTask().getService());
					jmsQueueManager.reportTaskResult(taskResult);
				} catch (JMSException e) {
					log.warn("Error trying to send {} to Dispatcher: {}", taskResult, e);
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
			case PLANNED:
				// should be taken by GenPlanner
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
				// this is basically the same check as for the generating tasks above,
				// but now for tasks missing in generatingTasks collection
				Date startTime = task.getGenStartTime();
				int howManyMinutesAgo = 0;
				if(startTime != null) { 
					howManyMinutesAgo = (int) (System.currentTimeMillis() - startTime.getTime()) / 1000 / 60;
				}
				// If too much time has passed something is broken
				if ((startTime == null || howManyMinutesAgo >= rescheduleTime) &&
						!generatingTasks.values().contains(task)) {
					abortTask(task, TaskStatus.GENERROR);
				}
				break;
				
			case SENDING:
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

			case GENERROR:
			case GENERATED:
				// should be taken by SendPlanner or reported
				endTime = task.getGenEndTime();
				howManyMinutesAgo = 0;
				if(endTime != null) { 
					howManyMinutesAgo = (int) (System.currentTimeMillis() - endTime.getTime()) / 1000 / 60;
				}
				// If too much time has passed something is broken
				if((endTime == null || howManyMinutesAgo >= rescheduleTime) && 
						!schedulingPool.getGeneratedTasksQueue().contains(task)) {
					abortTask(task, TaskStatus.GENERROR);
				}
				break;

			case DONE:
				// report it
				try {
					jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), System.currentTimeMillis());
				} catch (JMSException e) {
					log.error("Error while sending final status update for Task with ID {} to Dispatcher", task.getId());
				}
				try {
					schedulingPool.removeTask(task.getId());
				} catch (TaskStoreException e) {
					log.error("Task {} could not be removed from SchedulingPool", e);
				}
				break;

			default:
				// unknown state
				abortTask(task, TaskStatus.ERROR);
			}
		}
	}	


	private void abortTask(Task task, TaskStatus status) {
		log.warn("Task {} found in unexpected state, switching to {} ", task, status);
		task.setStatus(status);
		Task removed = null;
		try {
			removed = schedulingPool.removeTask(task);
			// the removal from pool also cancels all task futures, which in turn
			// makes the completion service collect the task and remove it from its executingTasks map
		} catch (TaskStoreException e) {
			log.error("Failed during removal of Task {} from SchedulingPool", task);
		}
		if (removed == null) {
			log.error("Stale Task {} was not removed.", task);
		}
		try {
			jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), System.currentTimeMillis());
		} catch (JMSException e) {
			log.warn("Error trying to send {} to Dispatcher: {}", task, e);
		}
		
	}
}
