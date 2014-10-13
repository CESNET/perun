package cz.metacentrum.perun.engine.scheduling;

import java.util.List;

import cz.metacentrum.perun.taskslib.model.Task;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface DependenciesResolver {

	List<Task> getDependencies(Task task);

	List<Task> getDependants(Task task);

	void addDependency(Task task, int dependency);

}
