package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.AuditMessagesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Auditer;

import java.util.List;

/**
 * AuditMessagesManager manages audit messages (logs). Implementation of
 * Business logic.
 *
 * @author Michal Stava
 */
public class AuditMessagesManagerBlImpl implements AuditMessagesManagerBl {

	private final static Logger log = LoggerFactory.getLogger(UsersManagerBlImpl.class);
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

	public void log(PerunSession sess, String message) throws InternalErrorException {

		perunBl.getAuditer().log(sess, message);
	}
}
