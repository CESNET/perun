package cz.metacentrum.perun.dispatcher.service;

import cz.metacentrum.perun.dispatcher.exceptions.PerunHornetQServerException;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface DispatcherManager {

	// /Database///
	void registerDispatcher();

	void checkIn();

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
	void startPocessingEvents();

	void stopPocessingEvents();

	// /Task database///
	void loadSchedulingPool();
}
