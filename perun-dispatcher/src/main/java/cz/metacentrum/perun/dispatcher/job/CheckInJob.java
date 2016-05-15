package cz.metacentrum.perun.dispatcher.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import cz.metacentrum.perun.dispatcher.service.DispatcherManager;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public class CheckInJob extends QuartzJobBean {

	private final static Logger log = LoggerFactory.getLogger(CheckInJob.class);

	private DispatcherManager dispatcherManager;

	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
		log.debug("Entering CheckInJob...");
		// dispatcherManager.checkIn();
		log.debug("CheckInJob done");
	}

	public DispatcherManager getDispatcherManager() {
		return dispatcherManager;
	}

	public void setDispatcherManager(DispatcherManager dispatcherManager) {
		this.dispatcherManager = dispatcherManager;
	}
}
