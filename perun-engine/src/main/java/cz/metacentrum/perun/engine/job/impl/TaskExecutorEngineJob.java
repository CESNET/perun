package cz.metacentrum.perun.engine.job.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.engine.job.PerunEngineJob;
import cz.metacentrum.perun.engine.scheduling.TaskExecutorEngine;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
// TODO: Service, should not be concurrent...???
@org.springframework.stereotype.Service(value = "taskExecutorEngineJob")
public class TaskExecutorEngineJob implements PerunEngineJob {

	private final static Logger log = LoggerFactory
			.getLogger(TaskExecutorEngineJob.class);

	@Autowired
	private TaskExecutorEngine taskExecutorEngine;

	@Override
	public void doTheJob() {
		log.info("Entering TaskExecutorEngineJob: taskExecutorEngine.beginExecuting().");
		taskExecutorEngine.beginExecuting();
		log.info("TaskExecutorEngineJob done: taskExecutorEngine.beginExecuting() has completed.");
	}

	public TaskExecutorEngine getTaskExecutorEngine() {
		return taskExecutorEngine;
	}

	public void setTaskExecutorEngine(TaskExecutorEngine taskExecutorEngine) {
		this.taskExecutorEngine = taskExecutorEngine;
	}

}
