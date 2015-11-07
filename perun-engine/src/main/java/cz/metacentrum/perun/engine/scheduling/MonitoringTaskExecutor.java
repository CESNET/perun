package cz.metacentrum.perun.engine.scheduling;

import org.springframework.core.task.TaskExecutor;

public interface MonitoringTaskExecutor extends TaskExecutor{
	@Override
	public void execute(Runnable r);

	public void printAndWait(int howManyTimes, int delayTime);
}
