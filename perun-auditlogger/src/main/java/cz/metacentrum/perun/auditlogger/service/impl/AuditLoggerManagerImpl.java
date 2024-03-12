package cz.metacentrum.perun.auditlogger.service.impl;

import cz.metacentrum.perun.auditlogger.logger.EventLogger;
import cz.metacentrum.perun.auditlogger.service.AuditLoggerManager;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Service(value = "auditLoggerManager")
public class AuditLoggerManagerImpl implements AuditLoggerManager {

  private static final Logger LOG = LoggerFactory.getLogger(AuditLoggerManagerImpl.class);

  private static final String DEFAULT_CONSUMER_NAME = "auditlogger";
  private static final String DEFAULT_STATE_FILE = "./auditlogger.state";

  private Thread eventProcessorThread;
  @Autowired
  private EventLogger eventLogger;
  @Autowired
  private Properties propertiesBean;

  private PerunPrincipal perunPrincipal;
  private Perun perunBl;
  private PerunSession perunSession;

  @Override
  public String getConsumerName() {
    return this.propertiesBean.getProperty("auditlogger.consumer", DEFAULT_CONSUMER_NAME);
  }

  public EventLogger getEventDispatcher() {
    return eventLogger;
  }

  public Perun getPerunBl() {
    return perunBl;
  }

  public PerunPrincipal getPerunPrincipal() {
    return perunPrincipal;
  }

  public PerunSession getPerunSession() {
    if (perunSession == null) {
      this.perunSession = perunBl.getPerunSession(perunPrincipal, new PerunClient());
    }
    return perunSession;
  }

  @Override
  public String getStateFile() {
    return this.propertiesBean.getProperty("auditlogger.statefile", DEFAULT_STATE_FILE);
  }

  public void setEventDispatcher(EventLogger eventLogger) {
    this.eventLogger = eventLogger;
  }

  @Override
  public void setLastProcessedId(int lastProcessedId) {
    eventLogger.setLastProcessedIdNumber(lastProcessedId);
  }

  public void setPerunBl(Perun perunBl) {
    this.perunBl = perunBl;
  }

  public void setPerunPrincipal(PerunPrincipal perunPrincipal) {
    this.perunPrincipal = perunPrincipal;
  }

  public void startProcessingEvents() {
    eventProcessorThread = new Thread(eventLogger);
    eventProcessorThread.start();

    LOG.info("Event processor thread started.");
    System.out.println("Event processor thread started.");
  }

  public void stopProcessingEvents() {
    eventProcessorThread.interrupt();
    LOG.info("Event processor thread interrupted.");
    System.out.println("Event processor thread interrupted.");
  }


}
