package cz.metacentrum.perun.taskslib.service.impl;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.service.TaskStore;

import cz.metacentrum.perun.taskslib.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of TaskStore as in-memory pool.
 *
 * @see cz.metacentrum.perun.taskslib.service.TaskStore
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class TaskStoreImpl implements TaskStore {

	private final static Logger log = LoggerFactory.getLogger(TaskStoreImpl.class);
	private final Map<Integer, Task> tasksById = new HashMap<>();
	private final Map<Pair<Facility, Service>, Task> tasksByFacilityAndService = new HashMap<>();

	public TaskStoreImpl() {
	}

	@Override
	public Task getTask(int id) {
		return tasksById.get(id);
	}

	@Override
	public Task getTask(Facility facility, Service service) {
		return tasksByFacilityAndService.get(new Pair<>(facility, service));
	}

	@Override
	public int getSize() {
		return tasksById.size();
	}

	@Override
	public Task addTask(Task task) throws TaskStoreException {
		if (task.getService() == null) {
			log.error("Tried to insert Task {} with no Service", task);
			throw new IllegalArgumentException("Tasks Service not set.");
		} else if (task.getFacility() == null) {
			log.error("Tried to insert Task {} with no Facility", task);
			throw new IllegalArgumentException("Tasks Facility not set.");
		}
		Task idAdded;
		Task otherAdded;
		synchronized (this) {
			idAdded = tasksById.put(task.getId(), task);
			otherAdded = tasksByFacilityAndService.put(
					new Pair<>(task.getFacility(), task.getService()), task);
		}
		if (idAdded != otherAdded) {
			log.error("Task returned from both Maps after insert differ. taskById {}, taskByFacilityAndService {}", idAdded, otherAdded);
			throw new TaskStoreException("Tasks returned after insert into both Maps differ.");
		} else {
			return idAdded;
		}
	}

	@Override
	public Collection<Task> getAllTasks() {
		return tasksById.values();
	}

	@Override
	public synchronized List<Task> getTasksWithStatus(Task.TaskStatus... status) {
		Collection<Task> tasks = tasksById.values();
		synchronized (this) {
			List<Task> result = new ArrayList<>();
			List<Task.TaskStatus> statuses = Arrays.asList(status);
			for (Task t : tasks) {
				if (statuses.contains(t.getStatus())) result.add(t);
			}
			return result;
		}
	}

	@Override
	public Task removeTask(Task task) throws TaskStoreException {
		Task idRemoved;
		Task otherRemoved;
		synchronized (this) {
			idRemoved = tasksById.remove(task.getId());
			otherRemoved = tasksByFacilityAndService.remove(new Pair<>(task.getFacility(), task.getService()));
		}
		if (idRemoved != otherRemoved) {
			log.error("Inconsistent state occurred after removing Task {} from TaskStore", task);
			throw new TaskStoreException("Unable to remove Task properly.");
		}
		return idRemoved;
	}

	@Override
	public Task removeTask(int id) throws TaskStoreException {
		Task task = getTask(id);
		if (task != null) {
			task = removeTask(getTask(id));
		}
		return task;
	}

	@Override
	public void clear() {
		tasksById.clear();
		tasksByFacilityAndService.clear();
	}

}
