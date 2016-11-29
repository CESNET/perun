package cz.metacentrum.perun.dispatcher.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import cz.metacentrum.perun.dispatcher.scheduling.PropagationMaintainer;

/**
 * Perform checking on finished tasks, reschedules error tasks, stuck tasks and old done tasks.
 *
 * @author Michal Karm Babacek
 */
@org.springframework.stereotype.Service(value = "propagationMaintainerJob")
public class PropagationMaintainerJob extends QuartzJobBean {

	private final static Logger log = LoggerFactory.getLogger(PropagationMaintainerJob.class);

	private PropagationMaintainer propagationMaintainer;

	public PropagationMaintainer getPropagationMaintainer() {
		return propagationMaintainer;
	}

	@Autowired
	public void setPropagationMaintainer(PropagationMaintainer propagationMaintainer) {
		this.propagationMaintainer = propagationMaintainer;
	}

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		log.info("Entering PropagationMaintainerJob: propagationMaintainer.checkResults().");
		propagationMaintainer.checkResults();
		log.info("PropagationMaintainerJob done: propagationMaintainer.checkResults() has completed.");
	}

}
