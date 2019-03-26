package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.WrongRangeOfCountException;
import cz.metacentrum.perun.core.bl.AuditMessagesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;

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
	public List<AuditMessage> getMessages(PerunSession perunSession) throws InternalErrorException {
		return this.getMessages(perunSession, AuditMessagesManager.COUNTOFMESSAGES);
	}

	@Override
	public List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException {
		if(count<1) throw new WrongRangeOfCountException("Count of messages is less than 1. Can't be returned less than 1 message.");
		return getAuditMessagesManagerBl().getMessages(perunSession, count);
	}

	@Override
	public List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) throws InternalErrorException {
		if(count<1) throw new WrongRangeOfCountException("Count of messages is less than 1. Can't be returned less than 1 message.");
		return getAuditMessagesManagerBl().getMessagesByCount(perunSession, count);
	}

	@Override
	public List<String> pollConsumerMessages(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) {
			throw new PrivilegeException(perunSession, "pollConsumerMessages");
		}

		return getAuditMessagesManagerBl().pollConsumerMessages(consumerName);
	}

	@Override
	public List<String> pollConsumerFullMessages(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) {
			throw new PrivilegeException(perunSession, "pollConsumerFullMessages");
		}

		return getAuditMessagesManagerBl().pollConsumerFullMessages(consumerName);
	}

	@Override
	public List<String> pollConsumerMessagesForParserSimple(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) {
			throw new PrivilegeException(perunSession, "pollConsumerMessagesForParserSimple");
		}

		return getAuditMessagesManagerBl().pollConsumerMessagesForParserSimple(consumerName);
	}

	@Override
	public List<AuditMessage> pollConsumerMessagesForParser(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) {
			throw new PrivilegeException(perunSession, "pollConsumerMessagesForParser");
		}

		return getAuditMessagesManagerBl().pollConsumerMessagesForParser(consumerName);
	}

	@Override
	public List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) {
			throw new PrivilegeException(perunSession, "pollConsumerEvents");
		}

		return getAuditMessagesManagerBl().pollConsumerEvents(consumerName);
	}

	@Override
	public void createAuditerConsumer(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) {
			throw new PrivilegeException(perunSession, "createAuditerConsumer");
		}

		getAuditMessagesManagerBl().createAuditerConsumer(consumerName);
	}

	@Override
	public void log(PerunSession perunSession, String message) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.REGISTRAR)) {
			throw new PrivilegeException(perunSession, "log");
		}

		getAuditMessagesManagerBl().log(perunSession, message);
	}

	@Override
	public Map<String, Integer> getAllAuditerConsumers(PerunSession perunSession) throws InternalErrorException {
		// anybody can call this method
		return getAuditMessagesManagerBl().getAllAuditerConsumers(perunSession);
	}

	@Override
	public int getLastMessageId(PerunSession perunSession) throws InternalErrorException {
		// anybody can call this method
		return getAuditMessagesManagerBl().getLastMessageId();
	}

	@Override
	public void setLastProcessedId(PerunSession perunSession, String consumerName, int lastProcessedId) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) {
			throw new PrivilegeException(perunSession, "setLastProcessedId");
		}

		getAuditMessagesManagerBl().setLastProcessedId(consumerName, lastProcessedId);
	}

	@Override
	public int getAuditerMessagesCount(PerunSession perunSession) throws InternalErrorException {
		Utils.checkPerunSession(perunSession);
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
