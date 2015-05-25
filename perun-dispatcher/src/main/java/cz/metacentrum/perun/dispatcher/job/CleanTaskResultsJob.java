package cz.metacentrum.perun.dispatcher.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import cz.metacentrum.perun.dispatcher.service.DispatcherManager;

public class CleanTaskResultsJob  extends QuartzJobBean {

	private final static Logger log = LoggerFactory.getLogger(CleanTaskResultsJob.class);

	private DispatcherManager dispatcherManager;

	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		log.debug("Entering CleanTaskResults job...");
		dispatcherManager.cleanOldTaskResults();
		log.debug("CleanTaskResults done.");
	}

	public DispatcherManager getDispatcherManager() {
		return dispatcherManager;
	}

	public void setDispatcherManager(DispatcherManager dispatcherManager) {
		this.dispatcherManager = dispatcherManager;
	}
}
