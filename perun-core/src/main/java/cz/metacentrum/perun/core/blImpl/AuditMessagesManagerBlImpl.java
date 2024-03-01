package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.StringMessageEvent;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.MessagesPageQuery;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.AuditMessagesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.implApi.AuditMessagesManagerImplApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.reflections.Reflections;

/**
 * AuditMessagesManager manages audit messages (logs). Implementation of Business Logic.
 *
 * @author Michal Stava
 */
public class AuditMessagesManagerBlImpl implements AuditMessagesManagerBl {

  private Auditer auditer;
  private PerunBl perunBl;
  private AuditMessagesManagerImplApi auditMessagesManagerImpl;

  public AuditMessagesManagerBlImpl(AuditMessagesManagerImplApi auditMessagesManagerImpl) {
    this.auditMessagesManagerImpl = auditMessagesManagerImpl;
  }

  @Override
  public void createAuditerConsumer(PerunSession perunSession, String consumerName) {
    getAuditMessagesManagerImpl().createAuditerConsumer(perunSession, consumerName);
  }

  @Override
  public List<String> findAllPossibleEvents(PerunSession sess) {
    try {
      Reflections reflections = new Reflections(AuditEvent.class);
      List<String> events =
          new ArrayList<>((reflections.getSubTypesOf(AuditEvent.class).stream().map(Class::getSimpleName).toList()));

      // Remove subclasses outside subpackages.
      events.remove("StringMessageEvent");

      return events;
    } catch (Exception e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public Map<String, Integer> getAllAuditerConsumers(PerunSession perunSession) {
    return getAuditMessagesManagerImpl().getAllAuditerConsumers(perunSession);
  }

  public AuditMessagesManagerImplApi getAuditMessagesManagerImpl() {
    return auditMessagesManagerImpl;
  }

  public Auditer getAuditer() {
    return auditer;
  }

  @Override
  public int getAuditerMessagesCount(PerunSession perunSession) {
    return getAuditMessagesManagerImpl().getAuditerMessagesCount(perunSession);
  }

  @Override
  public int getLastMessageId(PerunSession perunSession) {
    return getAuditMessagesManagerImpl().getLastMessageId(perunSession);
  }

  @Override
  public List<AuditMessage> getMessages(PerunSession perunSession, int count) {
    return getAuditMessagesManagerImpl().getMessages(perunSession, count);
  }

  @Override
  public List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) {
    return getAuditMessagesManagerImpl().getMessagesByCount(perunSession, count);
  }

  @Override
  public List<AuditMessage> getMessagesByIdAndCount(PerunSession perunSession, int id, int count) {
    return getAuditMessagesManagerImpl().getMessagesByIdAndCount(perunSession, id, count);
  }

  @Override
  public Paginated<AuditMessage> getMessagesPage(PerunSession perunSession, MessagesPageQuery query) {
    return getAuditMessagesManagerImpl().getMessagesPage(perunSession, query);
  }

  public PerunBl getPerunBl() {
    return perunBl;
  }

  @Override
  public void log(PerunSession perunSession, String message) {
    perunBl.getAuditer().log(perunSession, new StringMessageEvent(message));
  }

  @Override
  public List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName) {
    return getAuditMessagesManagerImpl().pollConsumerEvents(perunSession, consumerName);
  }

  @Override
  public List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName, int lastProcessedId) {
    return getAuditMessagesManagerImpl().pollConsumerEvents(perunSession, consumerName, lastProcessedId);
  }

  @Override
  public List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName) {
    return getAuditMessagesManagerImpl().pollConsumerMessages(perunSession, consumerName);
  }

  @Override
  public List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName, int lastProcessedId) {
    return getAuditMessagesManagerImpl().pollConsumerMessages(perunSession, consumerName, lastProcessedId);
  }

  public void setAuditer(Auditer auditer) {
    this.auditer = auditer;
  }

  @Override
  public void setLastProcessedId(PerunSession perunSession, String consumerName, int lastProcessedId) {
    getAuditMessagesManagerImpl().setLastProcessedId(perunSession, consumerName, lastProcessedId);
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

}
