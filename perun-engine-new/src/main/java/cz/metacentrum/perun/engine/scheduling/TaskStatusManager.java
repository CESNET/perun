package cz.metacentrum.perun.engine.scheduling;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult.TaskResultStatus;

public interface TaskStatusManager {

	TaskStatus getTaskStatus(Task task);

	void clearTaskStatus(Task task);

}
