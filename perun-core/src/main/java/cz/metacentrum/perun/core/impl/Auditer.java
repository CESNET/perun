package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.implApi.modules.attributes.AttributesModuleImplApi;
import net.jcip.annotations.GuardedBy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible for runtime logging of audit events. It gets messages and assocaites them with current transaction.
 * If there's no transaction currently running, message is immediately flushed out.
 * When transaction ends, transaction manager must call method flush in this class for the ending transaction.
 *
 * @see PerunTransactionManager
 * @see AuditEvent
 * @see AuditerMessage
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 * @author Jiri Mauritz
 * @author Michal Šťava
 * @author Pavel Zlámal
 * @author Martin Kuba
 * @author Vojtech Sassmann
 * @author Metodej Klang
 */
public class Auditer {

	@GuardedBy("Auditer.class")
	private static volatile Auditer selfInstance;

	public final static String engineForceKeyword = "forceit";

	private final static Logger log = LoggerFactory.getLogger(Auditer.class);
	private final static Logger transactionLogger = LoggerFactory.getLogger("transactionLogger");
	private JdbcPerunTemplate jdbc;

	private int lastProcessedId;
	private static final Map<Class<?>,Class<?>> mixinMap = new HashMap<>();
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.enableDefaultTyping();
		// TODO - skip any problematic properties using interfaces for mixins
		mapper.setMixIns(mixinMap);
	}

	private static final Object LOCK_DB_TABLE_AUDITER_LOG = new Object();

	private static final Set<AttributesModuleImplApi> registeredAttributesModules = new HashSet<>();

	public static void registerAttributeModule(AttributesModuleImplApi attributesModuleImplApi) {
		log.trace("Auditer: Try to register module {}", (attributesModuleImplApi == null) ? null : attributesModuleImplApi.getClass().getName());
		if(attributesModuleImplApi != null && !registeredAttributesModules.contains(attributesModuleImplApi)) {
			registeredAttributesModules.add(attributesModuleImplApi);
			log.debug("Auditer: Module {} was registered for audit message listening.", attributesModuleImplApi.getClass().getName());
		}

	}

	public Auditer() {
	}

	public void setPerunPool(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	/**
	 * Log message.
	 * Takes AuditEvent object and logs it to db.
	 *
	 * @param sess  Perun session
	 * @param event Audit event to be logged.
	 * @author Richard Husár 445238@mail.muni.cz
	 */
	public void log(PerunSession sess, AuditEvent event) {

		if(TransactionSynchronizationManager.isActualTransactionActive()) {
			log.trace("Auditer stores audit message to current transaction. Message: {}.", event.getMessage());
			List<List<List<AuditerMessage>>> topLevelTransactions = (List<List<List<AuditerMessage>>>) TransactionSynchronizationManager.getResource(this);
			if (topLevelTransactions == null) {
				newTopLevelTransaction();
				topLevelTransactions = (List<List<List<AuditerMessage>>>) TransactionSynchronizationManager.getResource(this);
				if (topLevelTransactions == null) {
					log.error("Failed to log event: " + event + " in Auditer because topLevelTransaction was null.");
					return;
				}
			}
			// pick last top-level messages chain
			List<List<AuditerMessage>> transactionChain = topLevelTransactions.get(topLevelTransactions.size() - 1);
			// pick last messages in that chain
			List<AuditerMessage> messages = transactionChain.get(transactionChain.size() - 1);
			messages.add(new AuditerMessage(sess, event));
		} else {
			this.storeMessageToDb(sess, event);
		}
	}

	/**
	 * Log message without checking current transactions.
	 *
	 * IMPORTANT: This method stores the message aside from DB transaction.
	 *
	 */
	public void logWithoutTransaction(PerunSession sess, AuditEvent event) {
		storeMessageToDb(sess, event);
	}

	/**
	 * Initialize new lists for sotring Audit messages.
	 *
	 */
	public void newTopLevelTransaction() {
		List<List<List<AuditerMessage>>> topLevelTransactions = (List<List<List<AuditerMessage>>>) TransactionSynchronizationManager.getResource(this);

		// prepare new messages chain
		List<List<AuditerMessage>> transactionChain = new ArrayList<>();
		List<AuditerMessage> messages = new ArrayList<>();
		transactionChain.add(messages);
		if (topLevelTransactions == null) {
			// there is no other top-level messages
			topLevelTransactions = new ArrayList<>();
			topLevelTransactions.add(transactionChain);
			TransactionSynchronizationManager.bindResource(this, topLevelTransactions);
		} else {
			topLevelTransactions.add(transactionChain);
		}
	}

	/**
	 * Creates new list for saving auditer messages of a new nested transactions.
	 *
	 */
	public void newNestedTransaction() {
		List<List<List<AuditerMessage>>> topLevelTransactions = getTopLevelTransactions();
		List<List<AuditerMessage>> transactionChain = topLevelTransactions.get(topLevelTransactions.size() - 1);
		List<AuditerMessage> messages = new ArrayList<>();
		transactionChain.add(messages);
	}

	/**
	 * Flush auditer messages of the last messages to the store of the outer transaction.
	 * There should be at least one nested transaction.
	 *
	 */
	public void flushNestedTransaction() {
		List<List<List<AuditerMessage>>> topLevelTransactions = getTopLevelTransactions();
		List<List<AuditerMessage>> transactionChain = topLevelTransactions.get(topLevelTransactions.size() - 1);
		if (transactionChain.size() < 2) {
			log.trace("No messages to flush");
			return;
		}
		List<AuditerMessage> messagesToFlush = transactionChain.get(transactionChain.size() - 1);

		transactionChain.get(transactionChain.size() - 2).addAll(messagesToFlush);
		List<AuditerMessage> messages = transactionChain.remove(transactionChain.size() - 1);
	}

	/**
	 * Erases the auditer messages for the last transaction.
	 *
	 */
	public void cleanNestedTransation() {
		List<List<List<AuditerMessage>>> topLevelTransactions = getTopLevelTransactions();
		List<List<AuditerMessage>> transactionChain = topLevelTransactions.get(topLevelTransactions.size() - 1);
		if (transactionChain.isEmpty()) {
			log.trace("No messages to clean");
			return;
		}
		List<AuditerMessage> messages = transactionChain.remove(transactionChain.size() - 1);
	}

	/**
	 * Imidiately flushes stored message for last top-level transaction into the log
	 *
	 */
	public void flush() {
		List<List<List<AuditerMessage>>> topLevelTransactions = getTopLevelTransactions();
		if (topLevelTransactions.isEmpty()) {
			log.trace("No messages to flush");
			return;
		}
		List<List<AuditerMessage>> transactionChain = topLevelTransactions.get(topLevelTransactions.size() - 1);
		if (transactionChain.isEmpty()) {
			log.trace("No messages to flush");
			topLevelTransactions.remove(topLevelTransactions.size() - 1);
			return;
		}

		if (transactionChain.size() != 1) {
			log.error("There should be only one list of messages while flushing representing the most outer transaction.");
		}

		List<AuditerMessage> messages = transactionChain.get(0);
		topLevelTransactions.remove(topLevelTransactions.size() - 1);
		if (topLevelTransactions.isEmpty()) {
			TransactionSynchronizationManager.unbindResourceIfPossible(this);
		}
		log.trace("Audit messages was flushed for current transaction.");
		synchronized (LOCK_DB_TABLE_AUDITER_LOG) {
			storeMessagesToDb(messages);
		}
	}

	/**
	 * All prepared auditer messages in the last top-level transaction are erased without storing into db.
	 * Mostly wanted while rollbacking.
	 *
	 */
	public void clean() {
		List<List<List<AuditerMessage>>> topLevelTransactions = getTopLevelTransactions();
		if (topLevelTransactions.isEmpty()) {
			log.trace("No messages to clean");
			return;
		}

		List<List<AuditerMessage>> transactionChain = topLevelTransactions.get(topLevelTransactions.size() - 1);
		if (transactionChain.isEmpty()) {
			log.trace("No messages to clean");
		}

		if (transactionChain.size() != 1) {
			log.error("There should be only one list of messages while cleaning.");
		}

		//log erased transactions to logfile
		for (List<AuditerMessage> list : transactionChain) {
			for (AuditerMessage message : list) {
				transactionLogger.info("Unstored transaction message: {}", message);
			}
		}


		topLevelTransactions.remove(topLevelTransactions.size() - 1);

		if (topLevelTransactions.isEmpty()) {
			TransactionSynchronizationManager.unbindResourceIfPossible(this);
		}
	}

	/**
	 * Get stored (not flushed) messages for current transaction. Messages remain stored.
	 *
	 * @return list of messages
	 */
	public List<AuditerMessage> getMessages() {
		List<List<List<AuditerMessage>>> topLevelTransactions = getTopLevelTransactions();
		List<List<AuditerMessage>> transactionChain = topLevelTransactions.get(topLevelTransactions.size() - 1);
		if (transactionChain.isEmpty()) return new ArrayList<>();
		List<AuditerMessage> messages = transactionChain.get(transactionChain.size() - 1);
		return messages;
	}

	private List<List<List<AuditerMessage>>> getTopLevelTransactions() {
		List<List<List<AuditerMessage>>> topLevelTransactions = (List<List<List<AuditerMessage>>>) TransactionSynchronizationManager.getResource(this);
		if (topLevelTransactions == null) {
			newTopLevelTransaction();
			topLevelTransactions = (List<List<List<AuditerMessage>>>) TransactionSynchronizationManager.getResource(this);
		}
		return topLevelTransactions;
	}

	/**
	 * Stores the list of AuditerMessages to the DB in batch.
	 *
	 * It also checks if there are any messages which can be resolved by registered attribute modules.
	 * Store these resolved messages too.
	 *
	 * @param auditerMessages list of AuditerMessages
	 */
	public void storeMessagesToDb(final List<AuditerMessage> auditerMessages) {

		//Avoid working with empty list of auditer-messages
		if(auditerMessages == null || auditerMessages.isEmpty()) {
			log.trace("Trying to store empty list of messages to DB!");
			return;
		}

		synchronized (LOCK_DB_TABLE_AUDITER_LOG) {

			// Resolve all additional message from attribute modules and add them to the bulk
			try {

				//Get perun session from the first message (all sessions should be same from the same principal)
				PerunSessionImpl session = (PerunSessionImpl) auditerMessages.get(0).getOriginatingSession();

				//Check recursively all messages if they can create any resolving message
				auditerMessages.addAll(checkRegisteredAttributesModules(session, auditerMessages, new LinkedHashSet<>()));

			} catch (Throwable ex) {
				log.error("There is a problem with processing resolving messages! It will be forcibly skipped to prevent unexpected behavior of auditer log!", ex);
			}

			//Write all messages to the database
			try {
				jdbc.batchUpdate("insert into auditer_log (id, msg, actor, created_at, created_by_uid) values ("+Compatibility.getSequenceNextval("auditer_log_id_seq")+",?,?," + Compatibility.getSysdate() + ",?)",
						new BatchPreparedStatementSetter() {
							@Override
							public void setValues(PreparedStatement ps, int i) throws SQLException {

								final AuditerMessage auditerMessage = auditerMessages.get(i);
								final PerunSession session = auditerMessage.getOriginatingSession();
								String jsonString = "";
								try {
									jsonString = mapper.writeValueAsString(auditerMessage.getEvent());
								} catch (IOException e) {
									log.error("Could not map event {} to JSON: {}", auditerMessage.getEvent().getClass().getSimpleName(), auditerMessage.getEvent().getMessage());
								}
								log.info("AUDIT_JSON: {}", jsonString);
								ps.setString(1, jsonString);
								ps.setString(2, session.getPerunPrincipal().getActor());
								ps.setInt(3, session.getPerunPrincipal().getUserId());
							}

							@Override
							public int getBatchSize() {
								return auditerMessages.size();
							}
						});

			} catch (RuntimeException e) {
				log.error("Cannot store auditer log json message in batch for list ['{}'], exception: {}", auditerMessages, e);
			} catch (InternalErrorException e) {
				log.error("Could not get system date identifier for the DB", e);
			}

		}

	}

	/**
	 * Stores the message to the DB.
	 *
	 * It also checks if there are any messages which can be resolved by registered attribute modules.
	 * Store these resolved messages too.
	 *
	 * @param sess
	 * @param event
	 */
	public void storeMessageToDb(final PerunSession sess, final AuditEvent event) {
		ArrayList<AuditerMessage> auditerMessages = new ArrayList<>();
		AuditerMessage auditerMessage = new AuditerMessage(sess, event);
		auditerMessages.add(auditerMessage);
		this.storeMessagesToDb(auditerMessages);
	}

	public void initialize() throws InternalErrorException {
		try {
			this.lastProcessedId = jdbc.queryForInt("select max(id) from auditer_log");
			log.debug("Auditer initialized with lastProcessedId [{}].", this.lastProcessedId);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Takes a list of input messages (messages) and list of already resolved messages (alreadyResolvedMessages). Then it process
	 * these messages by all registered modules and for every such module can generate new resolved messages. If any resolved message was
	 * generated by processing input messages it will check if there is a cycle (module X is generating message for module Y and otherwise)
	 * and if not, it will continue by processing these new input messages by calling itself in recursion. It also updates the list of
	 * already resolved messages and send this updated list too. If no new resolved message was generated, recursion will stop and
	 * returns the list of all resolved messages.
	 *
	 * This method is recursive.
	 *
	 * When cycle is detected, method will end the recursion and return already generated resolved messages. Also inform about
	 * this state to the error log. In this case list of resolved messages can be incomplete.
	 *
	 * @param session perun session
	 * @param messages input messages which can cause generating of new resolving messages by registered attr modules
	 * @param alreadyResolvedMessages LinkedHashSet of all already generated audit messages (used for checking a cycle between two or more registered modules)
	 *
	 * @return LinkedHashSet of all resolved messages generated by registered attr modules (unique set with preserved insertion order)
	 */
	private LinkedHashSet<AuditerMessage> checkRegisteredAttributesModules(PerunSession session, Collection<AuditerMessage> messages, LinkedHashSet<AuditerMessage> alreadyResolvedMessages) {
		LinkedHashSet<AuditerMessage> addedResolvedMessages = new LinkedHashSet<>();

		for (AuditerMessage message : messages) {
			for (AttributesModuleImplApi attributesModuleImplApi : registeredAttributesModules) {
				log.info("Message {} is given to module {}", message, attributesModuleImplApi.getClass().getSimpleName());

				try {
					List<AuditEvent> auditEvents = attributesModuleImplApi.resolveVirtualAttributeValueChange((PerunSessionImpl) session, message.getEvent());
					for (AuditEvent auditEvent : auditEvents) {
						AuditerMessage msg = new AuditerMessage(session, auditEvent);
						// do not store message duplicates created by this first pass through the message processing cycle
						addedResolvedMessages.add(msg);
					}
				} catch (InternalErrorException | WrongAttributeAssignmentException | AttributeNotExistsException | WrongReferenceAttributeValueException ex) {
					log.error("Error when auditer trying to resolve messages in modules.", ex);
				} catch (Exception ex) {
					log.error("An unexpected exception happened when trying to resolve message: {} in module {}, exception: {}",
							message, attributesModuleImplApi.getAttributeDefinition().getFriendlyName(), ex);
				}
			}
		}

		//We still have new resolving messages, so we need to detect if there isn't cycle and if not, continue the recursion
		if (!addedResolvedMessages.isEmpty()) {
			//Cycle detection
			Iterator<AuditerMessage> msgIterator = addedResolvedMessages.iterator();
			while (msgIterator.hasNext()) {
				AuditerMessage addedResolvedMessage = msgIterator.next();
				//If message is already present in the list of resolving messages, remove it from the list of added resolved messages, log it and continue
				if (alreadyResolvedMessages.contains(addedResolvedMessage)) {
					log.error("There is a cycle for resolving message {}. This message won't be processed more than once!", addedResolvedMessage);
					msgIterator.remove();
				}
			}
			//Update list of already resolved messages
			alreadyResolvedMessages.addAll(addedResolvedMessages);
			//Continue of processing newly generated messages
			addedResolvedMessages.addAll(checkRegisteredAttributesModules(session, addedResolvedMessages, alreadyResolvedMessages));
		}

		//Nothing new to resolve, we can return last state
		return addedResolvedMessages;
	}

}
