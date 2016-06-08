package cz.metacentrum.perun.dispatcher.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import cz.metacentrum.perun.dispatcher.scheduling.PropagationMaintainer;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.dispatcher.service.DispatcherManager;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public class MaintenanceJob extends QuartzJobBean {

	private final static Logger log = LoggerFactory
			.getLogger(MaintenanceJob.class);

	private SchedulingPool schedulingPool;

	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
		log.debug("Entering MaintenanceJob...");
		schedulingPool.checkTasksDb();
		log.debug("MaintenanceJob done.");
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

}
