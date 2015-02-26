package cz.metacentrum.perun.core.entry;

import java.util.List;

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
import java.util.Map;




/**
 * AuditMessagesManager manages audit messages (logs). Entry Logic
 *
 * @author Michal Stava
 */
public class AuditMessagesManagerEntry implements AuditMessagesManager {

	private AuditMessagesManagerBl auditMessagesManagerBl;
	private PerunBl perunBl;

	public AuditMessagesManagerEntry() {
	}

	public List<AuditMessage> getMessages(PerunSession perunSession) throws InternalErrorException, WrongRangeOfCountException {
		return this.getMessages(perunSession, AuditMessagesManager.COUNTOFMESSAGES);
	}

	public List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException, WrongRangeOfCountException {
		if(count<1) throw new WrongRangeOfCountException("Count of messages is less than 1. Can't be returned less than 1 message.");
		return getAuditMessagesManagerBl().getMessages(perunSession, count);
	}

	public List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) throws InternalErrorException, WrongRangeOfCountException {
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
	public void createAuditerConsumer(PerunSession perunSession, String consumerName) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) {
			throw new PrivilegeException(perunSession, "createAuditerConsumer");
		}

		getAuditMessagesManagerBl().createAuditerConsumer(consumerName);
	}

	public void log(PerunSession sess, String message) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "log");
		}

		getAuditMessagesManagerBl().log(sess, message);
	}

	public Map<String, Integer> getAllAuditerConsumers(PerunSession sess) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getAllAuditerConsumers");
		}

		return getAuditMessagesManagerBl().getAllAuditerConsumers(sess);
	}

	public int getLastMessageId(PerunSession sess) throws InternalErrorException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getLastMessageId");
		}

		return getAuditMessagesManagerBl().getLastMessageId();
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
