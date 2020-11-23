package cz.metacentrum.perun.auditlogger.service.impl;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.auditlogger.logger.EventLogger;
import cz.metacentrum.perun.auditlogger.service.AuditLoggerManager;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;

@org.springframework.stereotype.Service(value = "auditLoggerManager")
public class AuditLoggerManagerImpl implements AuditLoggerManager {

	private final static Logger log = LoggerFactory.getLogger(AuditLoggerManagerImpl.class);

	private final static String DEFAULT_CONSUMER_NAME = "auditlogger";
	private final static String DEFAULT_STATE_FILE = "/tmp/auditlogger.state";
	
	private Thread eventProcessorThread;
	@Autowired
	private EventLogger eventLogger;
	@Autowired 
	private Properties propertiesBean;
	
	private PerunPrincipal perunPrincipal;
	private Perun perunBl;
	private PerunSession perunSession;

	
	public void startProcessingEvents() {
		eventProcessorThread = new Thread(eventLogger);
		eventProcessorThread.start();

		log.debug("Event processor thread started.");
		System.out.println("Event processor thread started.");
	}

	public void stopProcessingEvents() {
		eventProcessorThread.interrupt();
		log.debug("Event processor thread interrupted.");
		System.out.println("Event processor thread interrupted.");
	}

	public EventLogger getEventDispatcher() {
		return eventLogger;
	}

	public void setEventDispatcher(EventLogger eventLogger) {
		this.eventLogger = eventLogger;
	}

	public Perun getPerunBl() {
		return perunBl;
	}

	public void setPerunBl(Perun perunBl) {
		this.perunBl = perunBl;
	}

	public PerunSession getPerunSession() {
		if (perunSession == null) {
			this.perunSession = perunBl.getPerunSession(perunPrincipal, new PerunClient());
		}
		return perunSession;
	}

	public PerunPrincipal getPerunPrincipal() {
		return perunPrincipal;
	}

	public void setPerunPrincipal(PerunPrincipal perunPrincipal) {
		this.perunPrincipal = perunPrincipal;
	}

	@Override
	public void setLastProcessedId(int lastProcessedId) {
		eventLogger.setLastProcessedIdNumber(lastProcessedId);
	}

	@Override
	public String getConsumerName() {
		return this.propertiesBean.getProperty("auditlogger.consumer", DEFAULT_CONSUMER_NAME);
	}

	@Override
	public String getStateFile() {
		return this.propertiesBean.getProperty("auditlogger.statefile", DEFAULT_STATE_FILE);
	}


}
