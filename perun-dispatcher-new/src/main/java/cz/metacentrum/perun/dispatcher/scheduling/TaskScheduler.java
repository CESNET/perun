package cz.metacentrum.perun.dispatcher.scheduling;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.model.Task;

public interface TaskScheduler {

	/**
	 * Process waiting jobs in the pool, sort them to satisfy dependencies and
	 * sent them out to engine(s) for execution.
	 * 
	 * @throws InternalErrorException
	 */
	void processPool() throws InternalErrorException;

	int getPoolSize();

	void scheduleTask(Task task);

}
