package cz.metacentrum.perun.dispatcher.scheduling;

import cz.metacentrum.perun.taskslib.model.Task;

public interface TaskScheduler extends Runnable {

	int getPoolSize();

	void scheduleTask(Task task);

	void setSchedulingPool(SchedulingPool schedulingPool);
}
