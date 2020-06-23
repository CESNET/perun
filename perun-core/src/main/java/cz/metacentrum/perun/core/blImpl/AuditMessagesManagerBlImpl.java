package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.StringMessageEvent;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.AuditMessagesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.implApi.AuditMessagesManagerImplApi;

import java.util.List;
import java.util.Map;

/**
 * AuditMessagesManager manages audit messages (logs). Implementation of Business Logic.
 *
 * @author Michal Stava
 */
public class AuditMessagesManagerBlImpl implements AuditMessagesManagerBl {

	private Auditer auditer;
	private PerunBl perunBl;
	private AuditMessagesManagerImplApi auditMessagesManagerImpl;

	public void setAuditer(Auditer auditer) {
		this.auditer = auditer;
	}

	public Auditer getAuditer() {
		return auditer;
	}

	public PerunBl getPerunBl() {
		return perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	public AuditMessagesManagerImplApi getAuditMessagesManagerImpl() {
		return auditMessagesManagerImpl;
	}

	public AuditMessagesManagerBlImpl(AuditMessagesManagerImplApi auditMessagesManagerImpl) {
		this.auditMessagesManagerImpl = auditMessagesManagerImpl;
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
	public List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName) {
		return getAuditMessagesManagerImpl().pollConsumerMessages(perunSession, consumerName);
	}

	@Override
	public List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName, int lastProcessedId) {
		return getAuditMessagesManagerImpl().pollConsumerMessages(perunSession, consumerName, lastProcessedId);
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
	public void createAuditerConsumer(PerunSession perunSession, String consumerName) {
		getAuditMessagesManagerImpl().createAuditerConsumer(perunSession, consumerName);
	}

	@Override
	public void log(PerunSession perunSession, String message) {
		perunBl.getAuditer().log(perunSession, new StringMessageEvent(message));
	}

	@Override
	public Map<String, Integer> getAllAuditerConsumers(PerunSession perunSession) {
		return getAuditMessagesManagerImpl().getAllAuditerConsumers(perunSession);
	}

	@Override
	public int getLastMessageId(PerunSession perunSession) {
		return getAuditMessagesManagerImpl().getLastMessageId(perunSession);
	}

	@Override
	public void setLastProcessedId(PerunSession perunSession, String consumerName, int lastProcessedId) {
		getAuditMessagesManagerImpl().setLastProcessedId(perunSession, consumerName, lastProcessedId);
	}

	@Override
	public int getAuditerMessagesCount(PerunSession perunSession) {
		return getAuditMessagesManagerImpl().getAuditerMessagesCount(perunSession);
	}

}
