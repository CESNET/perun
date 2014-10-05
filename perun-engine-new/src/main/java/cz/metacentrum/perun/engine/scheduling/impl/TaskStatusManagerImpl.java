package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.engine.scheduling.TaskStatus;
import cz.metacentrum.perun.engine.scheduling.TaskStatusManager;
import cz.metacentrum.perun.engine.scheduling.TaskStatus.TaskDestinationStatus;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;

@org.springframework.stereotype.Service(value = "taskStatusManager")
public class TaskStatusManagerImpl implements TaskStatusManager, TaskResultListener {
    private final static Logger log = LoggerFactory.getLogger(TaskStatusManagerImpl.class);
	
    @Autowired
    private SchedulingPool schedulingPool;
    
    private Map<Integer, TaskStatus> taskToStatusMap;  

	public TaskStatusManagerImpl() {
		taskToStatusMap = new HashMap<Integer, TaskStatus>();
	}
	
	@Override
	public TaskStatus getTaskStatus(Task task) {
		TaskStatus taskStatus;
		synchronized(taskToStatusMap) {
			if(taskToStatusMap.containsKey(task.getId())) {
				taskStatus = taskToStatusMap.get(task.getId());
			} else {
				taskStatus = new TaskStatusImpl(task);
				taskToStatusMap.put(task.getId(), taskStatus);
			}
		}
		// TODO: persist created taskStatus in storage
		return taskStatus;
	}

	@Override
	public void clearTaskStatus(Task task) {
		synchronized(taskToStatusMap) {
			taskToStatusMap.remove(task.getId());
		}
	}
	
	@Override
	public void onTaskDestinationDone(Task task, Destination destination, TaskResult result) {
		TaskStatus taskStatus = this.getTaskStatus(task);
		try {
			taskStatus.setDestinationStatus(destination, TaskDestinationStatus.DONE);
			taskStatus.setDestinationResult(destination, result);
		} catch (InternalErrorException e) {
			log.error("Error setting DONE status for task " + task.toString() + ": " + e.getMessage());
		}
		if(taskStatus.isTaskFinished()) {
			schedulingPool.setTaskStatus(task, taskStatus.getTaskStatus());
		}
	}


	@Override
	public void onTaskDestinationError(Task task, Destination destination, TaskResult result) {
		TaskStatus taskStatus = this.getTaskStatus(task);
		try {
			taskStatus.setDestinationStatus(destination, TaskDestinationStatus.ERROR);
			taskStatus.setDestinationResult(destination, result);
		} catch (InternalErrorException e) {
			log.error("Error setting ERROR status for task " + task.toString() + ": " +  e.getMessage());
		}
		if(taskStatus.isTaskFinished()) {
			schedulingPool.setTaskStatus(task, taskStatus.getTaskStatus());
		}
	}
    
	

}
