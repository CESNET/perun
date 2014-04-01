package cz.metacentrum.perun.dispatcher.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.dispatcher.dao.DispatcherDao;
import cz.metacentrum.perun.dispatcher.exceptions.PerunHornetQServerException;
import cz.metacentrum.perun.dispatcher.hornetq.PerunHornetQServer;
import cz.metacentrum.perun.dispatcher.jms.SystemQueueProcessor;
import cz.metacentrum.perun.dispatcher.parser.ParserManager;
import cz.metacentrum.perun.dispatcher.processing.EventProcessor;
import cz.metacentrum.perun.dispatcher.processing.SmartMatcher;
import cz.metacentrum.perun.dispatcher.service.DispatcherManager;

/**
 *
 * @author Michal Karm Babacek
 * JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "dispatcherManager")
public class DispatcherManagerImpl implements DispatcherManager {

	@Autowired
	private DispatcherDao dispatcherDao;
	@Autowired
	private PerunHornetQServer perunHornetQServer;
	@Autowired
	private SystemQueueProcessor systemQueueProcessor;
	@Autowired
	private ParserManager parserManager;
	@Autowired
	private EventProcessor eventProcessor;
	@Autowired
	private SmartMatcher smartMatcher;

	@Override
	public void registerDispatcher() {
		dispatcherDao.registerDispatcher();
	}

	@Override
	public void checkIn() {
		dispatcherDao.checkIn();
	}

	@Override
	public void startPerunHornetQServer() {
		perunHornetQServer.startServer();
	}

	@Override
	public void stopPerunHornetQServer() {
		perunHornetQServer.stopServer();
	}

	@Override
	public void prefetchRulesAndDispatcherQueues() throws PerunHornetQServerException{
		smartMatcher.loadAllRulesFromDB();
		systemQueueProcessor.createDispatcherQueuesForClients(smartMatcher.getClientsWeHaveRulesFor());
	}

	@Override
	public void startProcessingSystemMessages() {
		systemQueueProcessor.startProcessingSystemMessages();
	}

	@Override
	public void stopProcessingSystemMessages() {
		systemQueueProcessor.stopProcessingSystemMessages();
	}

	@Override
	public void startParsingData() {
		parserManager.summonParsers();
	}

	@Override
	public void stopParsingData() {
		parserManager.disposeParsers();
	}

	@Override
	public void startPocessingEvents() {
		eventProcessor.startPocessingEvents();
	}

	@Override
	public void stopPocessingEvents() {
		eventProcessor.stopPocessingEvents();
	}

	public void setDispatcherDao(DispatcherDao dispatcherDao) {
		this.dispatcherDao = dispatcherDao;
	}

	public void setPerunHornetQServer(PerunHornetQServer perunHornetQServer) {
		this.perunHornetQServer = perunHornetQServer;
	}

	public void setSystemQueueProcessor(SystemQueueProcessor systemQueueProcessor) {
		this.systemQueueProcessor = systemQueueProcessor;
	}

	public void setParserManager(ParserManager parserManager) {
		this.parserManager = parserManager;
	}

	public void setEventProcessor(EventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}

	public void setSmartMatcher(SmartMatcher smartMatcher) {
		this.smartMatcher = smartMatcher;
	}

}
