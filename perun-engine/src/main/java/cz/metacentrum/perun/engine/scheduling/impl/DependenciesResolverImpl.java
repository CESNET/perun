package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.engine.scheduling.DependenciesResolver;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.Task;

/**
 * Local resolver of Task dependencies (typically GEN->SEND), but
 * can by dependency between other services too.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "dependenciesResolver")
public class DependenciesResolverImpl implements DependenciesResolver {

	private final static Logger log = LoggerFactory.getLogger(DependenciesResolverImpl.class);

	private Map<Task, HashSet<Integer>> taskDependencies = new ConcurrentHashMap<Task, HashSet<Integer>>();

	@Autowired
	private SchedulingPool schedulingPool;

	@Override
	public List<Task> getDependencies(Task task) {
		if (!taskDependencies.containsKey(task)) {
			return new ArrayList<Task>();
		}
		List<Task> results = new ArrayList<Task>(taskDependencies.get(task).size());
		for (int id : taskDependencies.get(task)) {
			Task dependant = schedulingPool.getTaskById(id);
			if (dependant != null) {
				results.add(dependant);
			}
		}
		return results;
	}

	@Override
	public List<Task> getDependants(Task task) {
		// now this is a bit harder than simply getting the value out of hash map
		List<Task> results = new ArrayList<Task>();
		for (Map.Entry<Task, HashSet<Integer>> entry : taskDependencies.entrySet()) {
			if (entry.getValue().contains(task.getId())) {
				results.add(entry.getKey());
			}
		}
		return results;
	}

	@Override
	public void addDependency(Task task, int dependency) {
		if (taskDependencies.containsKey(task)) {
			taskDependencies.get(task).add(dependency);
		} else {
			HashSet<Integer> dependencies = new HashSet<Integer>();
			dependencies.add(dependency);
			taskDependencies.put(task, dependencies);
		}
	}

}
