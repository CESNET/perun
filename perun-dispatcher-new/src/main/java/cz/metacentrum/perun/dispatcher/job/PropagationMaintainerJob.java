package cz.metacentrum.perun.dispatcher.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import cz.metacentrum.perun.dispatcher.scheduling.PropagationMaintainer;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public class PropagationMaintainerJob extends QuartzJobBean {

    private final static Logger log = LoggerFactory.getLogger(PropagationMaintainerJob.class);

    private PropagationMaintainer propagationMaintainer;

    public PropagationMaintainer getPropagationMaintainer() {
        return propagationMaintainer;
    }

    public void setPropagationMaintainer(PropagationMaintainer propagationMaintainer) {
        this.propagationMaintainer = propagationMaintainer;
    }

	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
        log.info("Entering PropagationMaintainerJob: propagationMaintainer.checkResults().");
        propagationMaintainer.checkResults();
        log.info("PropagationMaintainerJob: propagationMaintainer.checkResults() has completed.");
	}
}
