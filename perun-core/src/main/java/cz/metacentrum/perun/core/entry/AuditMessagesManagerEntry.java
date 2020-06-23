package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongRangeOfCountException;
import cz.metacentrum.perun.core.bl.AuditMessagesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;

import java.util.List;
import java.util.Map;

/**
 * AuditMessagesManager manages audit messages (logs). Implementation of Entry Logic.
 *
 * @author Michal Stava
 */
public class AuditMessagesManagerEntry implements AuditMessagesManager {

	private AuditMessagesManagerBl auditMessagesManagerBl;
	private PerunBl perunBl;

	public AuditMessagesManagerEntry() {
	}

	@Override
	public List<AuditMessage> getMessages(PerunSession perunSession) {
		return this.getMessages(perunSession, AuditMessagesManager.COUNTOFMESSAGES);
	}

	@Override
	public List<AuditMessage> getMessages(PerunSession perunSession, int count) {
		if(count<1) throw new WrongRangeOfCountException("Count of messages is less than 1. Can't be returned less than 1 message.");
		return getAuditMessagesManagerBl().getMessages(perunSession, count);
	}

	@Override
	public List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) {
		if(count<1) throw new WrongRangeOfCountException("Count of messages is less than 1. Can't be returned less than 1 message.");
		return getAuditMessagesManagerBl().getMessagesByCount(perunSession, count);
	}

	@Override
	public List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName) throws PrivilegeException {
		if (!AuthzResolver.authorizedInternal(perunSession, "pollConsumerMessages_String_policy")) {
			throw new PrivilegeException(perunSession, "pollConsumerMessages");
		}
		return getAuditMessagesManagerBl().pollConsumerMessages(perunSession, consumerName);
	}

	@Override
	public List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName, int lastProcessedId) throws PrivilegeException {
		if (!AuthzResolver.authorizedInternal(perunSession, "pollConsumerMessages_String_int_policy")) {
			throw new PrivilegeException(perunSession, "pollConsumerMessages");
		}
		return getAuditMessagesManagerBl().pollConsumerMessages(perunSession, consumerName, lastProcessedId);
	}

	@Override
	public List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName) throws PrivilegeException {
		if (!AuthzResolver.authorizedInternal(perunSession, "pollConsumerEvents_String_policy")) {
			throw new PrivilegeException(perunSession, "pollConsumerEvents");
		}
		return getAuditMessagesManagerBl().pollConsumerEvents(perunSession, consumerName);
	}

	@Override
	public List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName, int lastProcessedId) throws PrivilegeException {
		if (!AuthzResolver.authorizedInternal(perunSession, "pollConsumerEvents_String_int_policy")) {
			throw new PrivilegeException(perunSession, "pollConsumerEvents");
		}
		return getAuditMessagesManagerBl().pollConsumerEvents(perunSession, consumerName, lastProcessedId);
	}

	@Override
	public void createAuditerConsumer(PerunSession perunSession, String consumerName) throws PrivilegeException {
		if (!AuthzResolver.authorizedInternal(perunSession, "createAuditerConsumer_String_policy")) {
			throw new PrivilegeException(perunSession, "createAuditerConsumer");
		}
		getAuditMessagesManagerBl().createAuditerConsumer(perunSession, consumerName);
	}

	@Override
	public void log(PerunSession perunSession, String message) throws PrivilegeException {
		if (!AuthzResolver.authorizedInternal(perunSession, "log_String_policy")) {
			throw new PrivilegeException(perunSession, "log");
		}
		getAuditMessagesManagerBl().log(perunSession, message);
	}

	@Override
	public Map<String, Integer> getAllAuditerConsumers(PerunSession perunSession) {
		// anybody can call this method
		return getAuditMessagesManagerBl().getAllAuditerConsumers(perunSession);
	}

	@Override
	public int getLastMessageId(PerunSession perunSession) {
		// anybody can call this method
		return getAuditMessagesManagerBl().getLastMessageId(perunSession);
	}

	@Override
	public void setLastProcessedId(PerunSession perunSession, String consumerName, int lastProcessedId) throws PrivilegeException {
		if (!AuthzResolver.authorizedInternal(perunSession, "setLastProcessedId_String_int_policy")) {
			throw new PrivilegeException(perunSession, "setLastProcessedId");
		}
		getAuditMessagesManagerBl().setLastProcessedId(perunSession, consumerName, lastProcessedId);
	}

	@Override
	public int getAuditerMessagesCount(PerunSession perunSession) {
		return getAuditMessagesManagerBl().getAuditerMessagesCount(perunSession);
	}

	/**
	 * Gets the AuditMessagesManagerBl for this instance.
	 *
	 * @return The AuditMessagesManagerBl.
	 */
	public AuditMessagesManagerBl getAuditMessagesManagerBl() {
		return this.auditMessagesManagerBl;
	}

	/**
	 * Sets the AuditMessagesManagerBl for this instance.
	 *
	 * @param auditMessagesManagerBl The AuditMessagesManagerBl.
	 */
	public void setAuditMessagesManagerBl(AuditMessagesManagerBl auditMessagesManagerBl)
	{
		this.auditMessagesManagerBl = auditMessagesManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl)
	{
		this.perunBl = perunBl;
	}
}
