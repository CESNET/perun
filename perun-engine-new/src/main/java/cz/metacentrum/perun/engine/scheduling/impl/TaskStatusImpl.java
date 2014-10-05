package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.scheduling.TaskStatus;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;

public class TaskStatusImpl implements TaskStatus {
    private final static Logger log = LoggerFactory.getLogger(TaskStatusImpl.class);

    private final static Destination fakeGenDestination = new Destination(0, "gen", "ONE");

    private Map<Destination, TaskDestinationStatus> allDestinations;
    private Map<Destination, TaskDestinationStatus> oneOfAllDestinations;
    private Map<Integer, TaskResult> destinationResults;
    private boolean allProcessing;
    private boolean oneOfAllSuccess;
    private int countAllDone;
    private int countAllError;
    private Task task;
    
	public TaskStatusImpl(Task task) {
		this.task = task;
		if(task.getExecService().getExecServiceType().equals(ExecServiceType.GENERATE)) {
			// one fake destination for the GEN task
			// if this one succeeds, the task succeeds
			allDestinations = new ConcurrentHashMap<Destination, TaskDestinationStatus>();
			oneOfAllDestinations = new ConcurrentHashMap<Destination, TaskDestinationStatus>(1);
			destinationResults = new ConcurrentHashMap<Integer, TaskResult>(1);
			oneOfAllSuccess = false;
			allProcessing = false;
			countAllDone = 0;
			countAllError = 0;
			oneOfAllDestinations.put(fakeGenDestination, TaskDestinationStatus.WAITING);
		} else {
			allDestinations = new ConcurrentHashMap<Destination, TaskDestinationStatus>(task.getDestinations().size());
			oneOfAllDestinations = new ConcurrentHashMap<Destination, TaskDestinationStatus>(task.getDestinations().size());
			destinationResults = new ConcurrentHashMap<Integer, TaskResult>(task.getDestinations().size());
			oneOfAllSuccess = false;
			allProcessing = false;
			countAllDone = 0;
			countAllError = 0;
			for(Destination destination : task.getDestinations()) {
				if(destination.getPropagationType().equals("PARALLEL")) {
					allDestinations.put(destination, TaskDestinationStatus.WAITING);
				} else {
					oneOfAllDestinations.put(destination, TaskDestinationStatus.WAITING);
				}
			}
			if(oneOfAllDestinations.isEmpty()) {
				oneOfAllSuccess = true;
			}
		}
	}

	@Override
	public List<Destination> getWaitingDestinations() {
		List<Destination> result = new ArrayList<Destination>();
		if(!allProcessing) {
			result.addAll(allDestinations.keySet());
			allProcessing = true;
		}
		// add next waiting destination from the oneOfAllDestinations list, unless there is one DONE already
		if(!oneOfAllSuccess) {
			for(Map.Entry<Destination, TaskDestinationStatus> entry : oneOfAllDestinations.entrySet()) {
				if(entry.getValue().equals(TaskDestinationStatus.WAITING)) {
						result.add(entry.getKey());
						break;
				}
			}
		}
		return result;
	}

	@Override
	public List<Destination> getSuccessfulDestinations() {
		List<Destination> result = new ArrayList<Destination>();
		if(oneOfAllSuccess) {
			result.addAll(oneOfAllDestinations.keySet());
		}
		for(Map.Entry<Destination, TaskDestinationStatus> entry : allDestinations.entrySet()) {
			if(entry.getValue().equals(TaskDestinationStatus.DONE)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	@Override
	public TaskDestinationStatus getDestinationStatus(Destination destination) throws InternalErrorException {
		Map<Destination, TaskDestinationStatus> map = findMapForDestination(destination);
		return map.get(destination);
	}

	@Override
	public void setDestinationStatus(Destination destination, TaskDestinationStatus status) throws InternalErrorException {
		Map<Destination, TaskDestinationStatus> map = findMapForDestination(destination);
		// we have to synchronize when accessing the counters
		synchronized(this) {
			switch(status) {
				case DONE:
					if(destination.getPropagationType().equals("PARALLEL")) {
						countAllDone += 1;
					} else {
						oneOfAllSuccess = true;
					}
					break;
				
				case ERROR:
					if(destination.getPropagationType().equals("PARALLEL")) {
						countAllError += 1;
					}
					break;
				
				default:
					break;
			}
		}
		map.put(destination, status);
	}

	private Map<Destination, TaskDestinationStatus> findMapForDestination(Destination destination) throws InternalErrorException {
		Map<Destination, TaskDestinationStatus> map;
		if(destination.getPropagationType().equals("PARALLEL")) {
			map = allDestinations;
		} else {
			map = oneOfAllDestinations;
		}
		if(!map.containsKey(destination)) {
			throw new InternalErrorException("no status for destination");
		}
		return map;
	}

	@Override
	public void setDestinationResult(Destination destination, TaskResult result) {
		if(result != null) {
			destinationResults.put(destination.getId(), result);
		}
		// TODO: cross check destination status
		// TaskResult.DENIED counts as TaskDestinationStatus.DONE
		// XXX - but where it may come from?
	}

	@Override
	public boolean isTaskFinished() {
		return oneOfAllSuccess && (countAllDone + countAllError >= allDestinations.size());
	}

	@Override
	public cz.metacentrum.perun.taskslib.model.Task.TaskStatus getTaskStatus() {
		if(!isTaskFinished()) {
			return cz.metacentrum.perun.taskslib.model.Task.TaskStatus.PROCESSING;
		}
		if(oneOfAllSuccess && countAllDone == allDestinations.size()) {
			return cz.metacentrum.perun.taskslib.model.Task.TaskStatus.DONE;
		} 
		// this involves the weird case of countAllDone + countAllError > allDestinations.size()
		return cz.metacentrum.perun.taskslib.model.Task.TaskStatus.ERROR;
	}

}
