package cz.metacentrum.perun.engine.job.impl;

import cz.metacentrum.perun.engine.job.PerunEngineJob;
import cz.metacentrum.perun.engine.scheduling.PropagationMaintainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Job called periodically using cron (see `perun-engine-scheduler.xml`) to prompt PropagationMaintainer to clean up
 * stuck tasks.
 *
 * @author Michal Karm Babacek
 */
@org.springframework.stereotype.Service(value = "propagationMaintainerJob")
public class PropagationMaintainerJob implements PerunEngineJob {

  private static final Logger LOG = LoggerFactory.getLogger(PropagationMaintainerJob.class);

  @Autowired
  private PropagationMaintainer propagationMaintainer;

  @Override
  public void doTheJob() {
    LOG.debug("Beginning cleanup of stuck Tasks.");
    propagationMaintainer.endStuckTasks();
    LOG.info("Stuck Tasks cleanup finished.");
  }

  public PropagationMaintainer getPropagationMaintainer() {
    return propagationMaintainer;
  }

  public void setPropagationMaintainer(PropagationMaintainer propagationMaintainer) {
    this.propagationMaintainer = propagationMaintainer;
  }
}
