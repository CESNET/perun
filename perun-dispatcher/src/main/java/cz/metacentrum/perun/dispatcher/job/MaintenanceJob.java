package cz.metacentrum.perun.dispatcher.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;

/**
 * Job to fix runtime Tasks state against their DB state.
 *
 * @author Michal Karm Babacek
 */
@org.springframework.stereotype.Service(value = "maintenanceJob")
public class MaintenanceJob extends QuartzJobBean {

	private final static Logger log = LoggerFactory.getLogger(MaintenanceJob.class);

	@Autowired
	private SchedulingPool schedulingPool;

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		log.debug("Entering MaintenanceJob...");
		schedulingPool.checkTasksDb();
		log.debug("MaintenanceJob done.");
	}

}
