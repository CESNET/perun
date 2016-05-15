package cz.metacentrum.perun.engine.job.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.engine.job.PerunEngineJob;
import cz.metacentrum.perun.engine.service.EngineManager;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
// Service, no need to be concurrent...
@org.springframework.stereotype.Service(value = "checkInJob")
public class CheckInJob implements PerunEngineJob {

	private final static Logger log = LoggerFactory.getLogger(CheckInJob.class);

	@Autowired
	private EngineManager engineManager;

	@Override
	public void doTheJob() {
		log.info("Entering CheckInJob: engineManager.checkIn()");
		//engineManager.checkIn();
		log.info("CheckInJob done: engineManager.checkIn() has completed.");
	}

	public EngineManager getEngineManager() {
		return engineManager;
	}

	public void setEngineManager(EngineManager engineManager) {
		this.engineManager = engineManager;
	}

}
