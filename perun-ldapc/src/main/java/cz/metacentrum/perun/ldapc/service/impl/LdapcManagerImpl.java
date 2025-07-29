package cz.metacentrum.perun.ldapc.service.impl;

import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.beans.FacilitySynchronizer;
import cz.metacentrum.perun.ldapc.beans.GroupSynchronizer;
import cz.metacentrum.perun.ldapc.beans.LdapProperties;
import cz.metacentrum.perun.ldapc.beans.ResourceSynchronizer;
import cz.metacentrum.perun.ldapc.beans.UserSynchronizer;
import cz.metacentrum.perun.ldapc.beans.VOSynchronizer;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * LdapcManager provides the Perun interface to other classes in perun-ldapc, handles synchronization by calling the
 * synchronizer classes, and starts eventDispatcher processing in a separate thread to handle real-time updates via
 * AuditEvents produced by Perun BE.
 */
@org.springframework.stereotype.Service(value = "ldapcManager")
public class LdapcManagerImpl implements LdapcManager {

  private static final Logger LOG = LoggerFactory.getLogger(LdapcManagerImpl.class);

  private Thread eventProcessorThread;
  @Autowired
  private EventDispatcher eventDispatcher;
  @Autowired
  private VOSynchronizer voSynchronizer;
  @Autowired
  private FacilitySynchronizer facilitySynchronizer;
  @Autowired
  private ResourceSynchronizer resourceSynchronizer;
  @Autowired
  private GroupSynchronizer groupSynchronizer;
  @Autowired
  private UserSynchronizer userSynchronizer;
  @Autowired
  private LdapProperties ldapProperties;

  private PerunPrincipal perunPrincipal;
  private Perun perunBl;
  private PerunSession perunSession;

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
  public void setLastProcessedId(int lastProcessedId) {
    eventDispatcher.setLastProcessedIdNumber(lastProcessedId);
  }

  public void setPerunBl(Perun perunBl) {
    this.perunBl = perunBl;
  }

  public void setPerunPrincipal(PerunPrincipal perunPrincipal) {
    this.perunPrincipal = perunPrincipal;
  }

  public void startProcessingEvents() {
    eventProcessorThread = new Thread(eventDispatcher);
    eventProcessorThread.start();

    LOG.debug("Event processor thread started.");
    System.out.println("Event processor thread started.");
  }

  public void stopProcessingEvents() {
    eventProcessorThread.interrupt();
    LOG.debug("Event processor thread interrupted.");
    System.out.println("Event processor thread interrupted.");
  }

  public void synchronize() {
    try {
      voSynchronizer.synchronizeVOs();
      facilitySynchronizer.synchronizeFacilities();
      userSynchronizer.synchronizeUsers();
      resourceSynchronizer.synchronizeResources();
      groupSynchronizer.synchronizeGroups();

      int lastProcessedMessageId = ((PerunBl) getPerunBl()).getAuditMessagesManagerBl().getLastMessageId(perunSession);
      // ((PerunBl)getPerunBl()).getAuditMessagesManagerBl().setLastProcessedId(perunSession, ldapProperties
      // .getLdapConsumerName(), lastProcessedMessageId);
      eventDispatcher.setLastProcessedIdNumber(lastProcessedMessageId);
    } catch (Exception e) {
      LOG.error("Error synchronizing to LDAP", e);
      throw new InternalErrorException(e);
    }
  }

  public void synchronizeReplica() {
    // let original method to do the work under our transaction settings
    synchronize();
  }
}
