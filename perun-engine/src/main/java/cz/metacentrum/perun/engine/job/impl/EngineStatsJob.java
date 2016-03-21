package cz.metacentrum.perun.engine.job.impl;

import cz.metacentrum.perun.engine.job.PerunEngineJob;
import cz.metacentrum.perun.engine.scheduling.MonitoringTaskExecutor;
import cz.metacentrum.perun.engine.scheduling.impl.MonitoringThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author David Šarman david.sarman@gmail.com
 */
@org.springframework.stereotype.Service(value = "engineStatsJob")
public class EngineStatsJob implements PerunEngineJob {
	private final static Logger log = LoggerFactory
			.getLogger(ProcessPoolJob.class);

	@Autowired
	private MonitoringTaskExecutor taskExecutorGenWorkers;
	@Autowired
	private MonitoringTaskExecutor taskExecutorSendWorkers;

	@Override
	public void doTheJob() {
		MonitoringThreadPoolExecutor sendExecutor = taskExecutorSendWorkers.getThreadPoolExecutor();
		MonitoringThreadPoolExecutor genExecutor = taskExecutorGenWorkers.getThreadPoolExecutor();
		log.info("GEN workers:\n" + sendExecutor.toString());
		log.info("SEND workers:\n" + genExecutor.toString());
	}
}
