package cz.metacentrum.perun.dispatcher.processing.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import cz.metacentrum.perun.dispatcher.model.Event;
import cz.metacentrum.perun.dispatcher.processing.EventLogger;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "eventLogger")
public class EventLoggerDummy implements EventLogger {

	private final static Logger log = LoggerFactory
			.getLogger(EventLogger.class);

	/**
	 * This method is to be called asynchronously.
	 */
	@Override
	@Async
	public void logEvent(Event event, int clientID) {
		if (clientID != -1) {
			log.info("[" + clientID + "]" + event.toString());
		} else {
			log.info("[orphan]" + event.toString());
		}
	}

}
