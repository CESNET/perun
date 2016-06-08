package cz.metacentrum.perun.engine.job.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.job.PerunEngineJob;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
// TODO: Service, however, it can be Prototype so as to run concurrently...
@org.springframework.stereotype.Service(value = "processPoolJob")
public class ProcessPoolJob implements PerunEngineJob {

	private final static Logger log = LoggerFactory
			.getLogger(ProcessPoolJob.class);

	@Autowired
	private TaskScheduler taskScheduler;

	@Override
	public void doTheJob() {
		log.info("Entering ProcessPoolJob: taskScheduler.processPool().");
		try {
			taskScheduler.processPool();
		} catch (InternalErrorException e) {
			log.error(e.toString(), e);
		}
		log.info("ProcessPoolJob done: taskScheduler.processPool() has completed.");
	}

	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

}
