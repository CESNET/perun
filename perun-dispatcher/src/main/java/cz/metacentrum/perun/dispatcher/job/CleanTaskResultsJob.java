package cz.metacentrum.perun.dispatcher.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import cz.metacentrum.perun.dispatcher.service.DispatcherManager;

/**
 * Cleans TaskResults from DB (older than 3 days, always keeps at least one for destination/service).
 *
 * @author Michal Karm Babacek
 */
@org.springframework.stereotype.Service(value = "cleanTaskResultsJob")
public class CleanTaskResultsJob  extends QuartzJobBean {

	private final static Logger log = LoggerFactory.getLogger(CleanTaskResultsJob.class);

	private DispatcherManager dispatcherManager;

	public DispatcherManager getDispatcherManager() {
		return dispatcherManager;
	}

	@Autowired
	public void setDispatcherManager(DispatcherManager dispatcherManager) {
		this.dispatcherManager = dispatcherManager;
	}

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		log.debug("Entering CleanTaskResultsJob...");
		dispatcherManager.cleanOldTaskResults();
		log.debug("CleanTaskResultsJob done.");
	}

}
