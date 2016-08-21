package cz.metacentrum.perun.engine.job.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.engine.job.PerunEngineJob;
import cz.metacentrum.perun.engine.scheduling.PropagationMaintainer;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "propagationMaintainerJob")
public class PropagationMaintainerJob implements PerunEngineJob {

	private final static Logger log = LoggerFactory.getLogger(PropagationMaintainerJob.class);

	@Autowired
	private PropagationMaintainer propagationMaintainer;

	@Override
	public void doTheJob() {
		log.info("Beginning cleanup of stuck Tasks.");
		propagationMaintainer.endStuckTasks();
		log.info("Stuck Tasks cleanup finished.");
	}

	public PropagationMaintainer getPropagationMaintainer() {
		return propagationMaintainer;
	}

	public void setPropagationMaintainer(
			PropagationMaintainer propagationMaintainer) {
		this.propagationMaintainer = propagationMaintainer;
	}
}
