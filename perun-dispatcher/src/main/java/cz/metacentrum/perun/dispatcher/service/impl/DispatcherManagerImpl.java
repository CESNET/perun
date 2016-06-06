package cz.metacentrum.perun.dispatcher.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Properties;

import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.dispatcher.dao.DispatcherDao;
import cz.metacentrum.perun.dispatcher.exceptions.PerunHornetQServerException;
import cz.metacentrum.perun.dispatcher.hornetq.PerunHornetQServer;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueue;
import cz.metacentrum.perun.dispatcher.jms.SystemQueueProcessor;
import cz.metacentrum.perun.dispatcher.jms.DispatcherQueuePool;
import cz.metacentrum.perun.dispatcher.parser.ParserManager;
import cz.metacentrum.perun.dispatcher.processing.EventProcessor;
import cz.metacentrum.perun.dispatcher.processing.SmartMatcher;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.dispatcher.service.DispatcherManager;
import cz.metacentrum.perun.taskslib.service.ResultManager;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "dispatcherManager")
public class DispatcherManagerImpl implements DispatcherManager {
	private final static Logger log = LoggerFactory.getLogger(DispatcherManagerImpl.class);

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
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private DispatcherQueuePool dispatcherQueuePool;
	@Autowired
	private ResultManager resultManager;
	@Autowired
	private Properties dispatcherPropertiesBean;

	@Override
	public void startPerunHornetQServer() {
		perunHornetQServer.startServer();
	}

	@Override
	public void stopPerunHornetQServer() {
		perunHornetQServer.stopServer();
	}

	@Override
	public void prefetchRulesAndDispatcherQueues()
			throws PerunHornetQServerException {
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
	public void startProcessingEvents() {
		eventProcessor.startProcessingEvents();
	}

	@Override
	public void stopProcessingEvents() {
		eventProcessor.stopProcessingEvents();
	}

	public void setPerunHornetQServer(PerunHornetQServer perunHornetQServer) {
		this.perunHornetQServer = perunHornetQServer;
	}

	public void setSystemQueueProcessor(
			SystemQueueProcessor systemQueueProcessor) {
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

	@Override
	public void loadSchedulingPool() {
		schedulingPool.reloadTasks();
	}

	/*
	 * public PerunSession getPerunSession() { if (this.dispatcherSession ==
	 * null) { try { String perunPrincipal =
	 * propertiesBean.getProperty("perun.principal.name"); String extSourceName
	 * = propertiesBean.getProperty("perun.principal.extSourceName"); String
	 * extSourceType =
	 * propertiesBean.getProperty("perun.principal.extSourceType");
	 * PerunPrincipal pp = new PerunPrincipal(perunPrincipal, extSourceName,
	 * extSourceType); this.dispatcherSession = perun.getPerunSession(pp); }
	 * catch (InternalErrorException e) { log.error(e.toString()); } } return
	 * this.dispatcherSession; }
	 */
	
	@Override
	public void cleanOldTaskResults() {
		for(DispatcherQueue queue: dispatcherQueuePool.getPool()) {
			try {
				int numRows = resultManager.clearOld(queue.getClientID(), 3);
				log.debug("Cleaned {} old task results for engine {}", numRows, queue.getClientID());
			} catch (Throwable e) {
				log.error("Error cleaning old task results for engine {} : {}", queue.getClientID(), e);
			}
		}
	}

}
