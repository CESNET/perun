package cz.metacentrum.perun.dispatcher.service;

import cz.metacentrum.perun.dispatcher.exceptions.PerunHornetQServerException;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface DispatcherManager {

	// /HornetQ server///
	void startPerunHornetQServer();

	void stopPerunHornetQServer();

	// /Prefetch rules and Dispatcher queues///
	void prefetchRulesAndDispatcherQueues() throws PerunHornetQServerException;

	// /System Queue Processor///
	void startProcessingSystemMessages();

	void stopProcessingSystemMessages();

	// /Parsing data///
	void startParsingData();

	void stopParsingData();

	// /Event Processor///
	void startProcessingEvents();

	void stopProcessingEvents();

	// /Task database///
	void loadSchedulingPool();
	
	void cleanOldTaskResults();
}
