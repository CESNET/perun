package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.PerunSession;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.AuditMessagesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Auditer;

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

	public List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException {

		return perunBl.getAuditer().getMessages(count);
	}

	public List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) throws InternalErrorException {

		return perunBl.getAuditer().getMessagesByCount(count);
	}

	public List<String> pollConsumerMessages(String consumerName) throws InternalErrorException {

		return perunBl.getAuditer().pollConsumerMessages(consumerName);
	}

	public List<String> pollConsumerFullMessages(String consumerName) throws InternalErrorException {

		return perunBl.getAuditer().pollConsumerFullMessages(consumerName);
	}

	public List<String> pollConsumerMessagesForParserSimple(String consumerName) throws InternalErrorException {

		return perunBl.getAuditer().pollConsumerMessagesForParserSimple(consumerName);
	}

	public List<AuditMessage> pollConsumerMessagesForParser(String consumerName) throws InternalErrorException {

		return perunBl.getAuditer().pollConsumerMessagesForParser(consumerName);
	}

	public void createAuditerConsumer(String consumerName) throws InternalErrorException {

		createAuditerConsumer(consumerName);
	}

	public void log(PerunSession perunSession, String message) throws InternalErrorException {

		perunBl.getAuditer().log(perunSession, message);
	}

	public Map<String, Integer> getAllAuditerConsumers(PerunSession perunSession) throws InternalErrorException {

		return perunBl.getAuditer().getAllAuditerConsumers(perunSession);
	}

	public int getLastMessageId() throws InternalErrorException {

		return perunBl.getAuditer().getLastMessageId();
	}

	public void setLastProcessedId(String consumerName, int lastProcessedId) throws InternalErrorException {

		perunBl.getAuditer().setLastProcessedId(consumerName, lastProcessedId);
	}
	
	public int getAuditerMessagesCount(PerunSession perunSession) throws InternalErrorException {
		return perunBl.getAuditer().getAuditerMessagesCount(perunSession);
	}
}
