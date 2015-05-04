package cz.metacentrum.perun.engine.scheduling;

import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface TaskScheduler {

	void propagateService(ExecService execService, Date time, Facility facility)
			throws InternalErrorException;

	void propagateService(Task task, Date date) throws InternalErrorException;

	void propagateServices(Pair<List<ExecService>, Facility> servicesFacility)
			throws InternalErrorException;

	void rescheduleTask(Task task);

	void processPool() throws InternalErrorException;

	int getPoolSize();

}
