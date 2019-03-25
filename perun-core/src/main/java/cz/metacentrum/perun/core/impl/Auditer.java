package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.implApi.modules.attributes.AttributesModuleImplApi;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import cz.metacentrum.perun.rpclib.impl.JsonDeserializer;
import net.jcip.annotations.GuardedBy;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used for logging audit events. It get messages and stored it in asociation with current transaction. If there's no transaction currently running, message is immediateli flushed out.
 * When transaction ends, transaction manager must call method flush in this clas for the ending transaction.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class Auditer {

	@GuardedBy("Auditer.class")
	private static volatile Auditer selfInstance;

	protected final static String auditMessageMappingSelectQuery = "id, msg, actor, created_at, created_by_uid";

	public final static String engineForceKeyword = "forceit";

	private final static ObjectMapper mapper = new ObjectMapper();

	private final static Logger log = LoggerFactory.getLogger(Auditer.class);
	private final static Logger transactionLogger = LoggerFactory.getLogger("transactionLogger");
	private JdbcPerunTemplate jdbc;

	private int lastProcessedId;

	private static final Object LOCK_DB_TABLE_AUDITER_LOG = new Object();
	private static final Object LOCK_DB_TABLE_AUDITER_LOG_JSON = new Object();

	private static final Set<AttributesModuleImplApi> registeredAttributesModules = new HashSet<>();

	static {

		// configure JSON deserializer for auditer log
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enableDefaultTyping();

		mapper.getDeserializationConfig().addMixInAnnotations(Attribute.class, JsonDeserializer.AttributeMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(AttributeDefinition.class, JsonDeserializer.AttributeDefinitionMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(User.class, JsonDeserializer.UserMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Member.class, JsonDeserializer.MemberMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(AuditMessage.class, JsonDeserializer.AuditMessageMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PerunBean.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Candidate.class, JsonDeserializer.CandidateMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PerunException.class, JsonDeserializer.PerunExceptionMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Destination.class, JsonDeserializer.DestinationMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Group.class, JsonDeserializer.GroupMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Group.class, JsonDeserializer.UserExtSourceMixIn.class);

		// we probably do not log these objects to auditer log, but to be sure we could read them later they are included

		mapper.getDeserializationConfig().addMixInAnnotations(Application.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationForm.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationFormItem.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationFormItemWithPrefilledValue.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationMail.class, JsonDeserializer.PerunBeanMixIn.class);

		mapper.getDeserializationConfig().addMixInAnnotations(Author.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Category.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Publication.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PublicationForGUI.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PublicationSystem.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Thanks.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ThanksForGUI.class, JsonDeserializer.PerunBeanMixIn.class);

	}

	public static void registerAttributeModule(AttributesModuleImplApi attributesModuleImplApi) {
		log.trace("Auditer: Try to register module {}", (attributesModuleImplApi == null) ? null : attributesModuleImplApi.getClass().getName());
		if(attributesModuleImplApi != null && !registeredAttributesModules.contains(attributesModuleImplApi)) {
			registeredAttributesModules.add(attributesModuleImplApi);
			log.debug("Auditer: Module {} was registered for audit message listening.", attributesModuleImplApi.getClass().getName());
		}

	}

	protected static final RowMapper<AuditMessage> AUDITMESSAGE_MAPPER = new RowMapper<AuditMessage>() {
		public AuditMessage mapRow(ResultSet rs, int i) throws SQLException {
			AuditMessage auditMessage = AUDITMESSAGE_MAPPER_FOR_PARSER.mapRow(rs, i);
			auditMessage.setMsg(BeansUtils.eraseEscaping(BeansUtils.replaceEscapedNullByStringNull(BeansUtils.replacePointyBracketsByApostrophe(auditMessage.getMsg()))));
			return auditMessage;
		}
	};

	protected static final RowMapper<AuditMessage> AUDITMESSAGE_MAPPER_FOR_PARSER = (resultSet, i) -> {

		String msg;
		if (Compatibility.isOracle()) {
			Clob clob = resultSet.getClob("msg");
			char[] cbuf = null;
			if (clob == null) {
				msg = null;
			} else {
				try {
					cbuf = new char[(int) clob.length()];
					clob.getCharacterStream().read(cbuf);
				} catch (IOException ex) {
					throw new InternalErrorRuntimeException(ex);
				}
				msg = new String(cbuf);
			}
		} else {
			msg = resultSet.getString("msg");
		}
		// Get principal User and his ID (null, if no user exist)
		Integer principalUserId = null;
		if (resultSet.getInt("created_by_uid") != 0) principalUserId = resultSet.getInt("created_by_uid");
		AuditMessage auditMessage = new AuditMessage(resultSet.getInt("id"), msg, resultSet.getString("actor"), resultSet.getString("created_at"), principalUserId);
		return auditMessage;
	};

	protected static final RowMapper<String> AUDITER_FULL_LOG_MAPPER = (resultSet, i) -> {
		AuditMessage auditMessage = AUDITMESSAGE_MAPPER.mapRow(resultSet, i);
		return auditMessage.getFullMessage();
	};

	protected static final RowMapper<String> AUDITER_LOG_MAPPER = (resultSet, i) -> {
		AuditMessage auditMessage = AUDITMESSAGE_MAPPER.mapRow(resultSet, i);
		return auditMessage.getMsg();
	};

	protected static final RowMapper<String> AUDITER_LOG_MAPPER_FOR_PARSER = (rs, i) -> {
		AuditMessage auditMessage = AUDITMESSAGE_MAPPER_FOR_PARSER.mapRow(rs, i);
		return auditMessage.getMsg();
	};

	protected static final ResultSetExtractor<Map<String, Integer>> AUDITER_CONSUMER_EXTRACTOR = resultSet -> {
		Map<String, Integer> auditerConsumers = new HashMap<>();
		while (resultSet.next()) {
			// fetch from map by ID
			String name = resultSet.getString("name");
			Integer lastProcessedId = resultSet.getInt("last_processed_id");
			auditerConsumers.put(name, lastProcessedId);
		}
		return auditerConsumers;
	};

	public Auditer() {
	}

	public void setPerunPool(DataSource perunPool) throws InternalErrorException {
		this.jdbc = new JdbcPerunTemplate(perunPool);
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

	public List<AuditMessage> getMessages(int count) throws InternalErrorException {
		try {
			return jdbc.query("select " + auditMessageMappingSelectQuery + " from (select " + auditMessageMappingSelectQuery + Compatibility.getRowNumberOver() + " from auditer_log) "+Compatibility.getAsAlias("temp")+" where rownumber <= ?",
					AUDITMESSAGE_MAPPER, count);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	/**
	 * Gets last count messages from auditer_log_json
	 *
	 * @param count
	 * @return list of last count messages from auditer_log_json
	 * @throws InternalErrorException
	 * @author Richard Husár 445238@mail.muni.cz
	 */
	public List<AuditMessage> getJSONMessages(int count) throws InternalErrorException {
		try {
			return jdbc.query("select " + auditMessageMappingSelectQuery + " from (select " + auditMessageMappingSelectQuery + Compatibility.getRowNumberOver() + " from auditer_log_json ORDER BY id Desc limit ?) " + Compatibility.getAsAlias("temp"),
					AUDITMESSAGE_MAPPER, count);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public List<AuditMessage> getMessagesByCount(int count) throws InternalErrorException {
		try {
			return jdbc.query("select " + auditMessageMappingSelectQuery + " from auditer_log where id > ((select max(id) from auditer_log)-?) order by id desc", AUDITMESSAGE_MAPPER, count);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public int getLastMessageId() throws InternalErrorException {
		try {
			return jdbc.queryForInt("select max(id) from auditer_log");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


	public void setLastProcessedId(String consumerName, int lastProcessedId) throws InternalErrorException {
		try {
			jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId ,consumerName);
		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

	public int getAuditerMessagesCount(PerunSession perunSession) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(id) from auditer_log");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<AuditMessage> getMessageForParser(int count) throws InternalErrorException {
		try {
			return jdbc.query("select " + auditMessageMappingSelectQuery + " from (select " + auditMessageMappingSelectQuery + ",row_number() over (ORDER BY id DESC) as rownumber from auditer_log) "+Compatibility.getAsAlias("temp")+" where rownumber <= ?",
					AUDITMESSAGE_MAPPER_FOR_PARSER, count);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
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
	 * Stores the list of auditer messages to the DB in batch.
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

		final List<Integer> ids = new ArrayList<>();
		final List<Pair<AuditerMessage, Integer>> msgs = new ArrayList<>();
		synchronized (LOCK_DB_TABLE_AUDITER_LOG) {

			//Add all possible resolving messages to the bulk
			try {

				//Get perun session from the first message (all sessions should be same from the same principal)
				PerunSessionImpl session = (PerunSessionImpl) auditerMessages.get(0).getOriginaterPerunSession();

				//Check recursively all messages if they can create any resolving message
				auditerMessages.addAll(checkRegisteredAttributesModules(session, auditerMessages, new ArrayList<>()));
			} catch (Throwable ex) {
				log.error("There is a problem with processing resolving messages! It will be forcibly skipped to prevent unexpected behavior of auditer log!", ex);
			}

			//Write all messages to the database
			try {
				jdbc.batchUpdate("insert into auditer_log (id, msg, actor, created_at, created_by_uid) values (?,?,?," + Compatibility.getSysdate() + ",?)",
						new BatchPreparedStatementSetter() {
							@Override
							public void setValues(PreparedStatement ps, int i) throws SQLException {
								final AuditerMessage auditerMessage = auditerMessages.get(i);
								final String message = auditerMessage.getEvent().getMessage();
								final PerunSession session = auditerMessage.getOriginaterPerunSession();
								log.trace("AUDIT: {}", message);
								try {
									final int msgId = Utils.getNewId(jdbc, "auditer_log_id_seq");
									ps.setInt(1, msgId);
									ids.add(msgId);
									msgs.add(new Pair<>(auditerMessage, msgId));
								} catch (InternalErrorException e) {
									throw new SQLException("Cannot get unique id for new auditer log message ['" + message + "']", e);
								}
								ps.setString(2, message);
								ps.setString(3, session.getPerunPrincipal().getActor());
								ps.setInt(4, session.getPerunPrincipal().getUserId());
							}

							@Override
							public int getBatchSize() {
								return auditerMessages.size();
							}
						});
			} catch (RuntimeException e) {
				log.error("Cannot store auditer log message in batch for list ['{}'], exception: {}", auditerMessages, e);
			} catch (InternalErrorException e) {
				log.error("Could not get system date identifier for the DB", e);
			}
		}

		this.storeMessagesToDbJson(msgs);
	}

	/**
	 * Stores messages to audit_log_JSON table
	 *
	 * @param messages takes pair of auditer message to be stored and id of message from audit_log
	 * @author Richard Husár 445238@mail.muni.cz
	 */
	public void storeMessagesToDbJson(final List<Pair<AuditerMessage, Integer>> messages) {
		synchronized (LOCK_DB_TABLE_AUDITER_LOG_JSON) {
			try {
				jdbc.batchUpdate("insert into auditer_log_json (id, msg, actor, created_at, created_by_uid) values (?,?,?," + Compatibility.getSysdate() + ",?)",
						new BatchPreparedStatementSetter() {
							@Override
							public void setValues(PreparedStatement ps, int i) throws SQLException {
								ObjectMapper mapper = new ObjectMapper();
								mapper.enableDefaultTyping();
								AuditerMessage message = messages.get(i).getLeft();
								String jsonString = "";
								try {
									jsonString = mapper.writeValueAsString(message.getEvent());

								} catch (IOException e) {
									log.error("Could not map event {} to JSON.", message.getEvent().getClass().getSimpleName());
								}
								//store message without duplicit message attribute because it is stored in separate table
								final PerunSession session = messages.get(i).getLeft().getOriginaterPerunSession();
								log.info("AUDIT_JSON: {}", jsonString);
								ps.setInt(1, messages.get(i).getRight());
								ps.setString(2, jsonString);
								ps.setString(3, session.getPerunPrincipal().getActor());
								ps.setInt(4, session.getPerunPrincipal().getUserId());
							}

							@Override
							public int getBatchSize() {
								return messages.size();
							}
						});
			} catch (RuntimeException e) {
				log.error("Cannot store auditer log JSON message in batch for list ['{}'], exception: {}", messages, e);
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

	public void createAuditerConsumer(String consumerName) throws InternalErrorException {
		try {
			int lastProcessedId = jdbc.queryForInt("select max(id) from auditer_log");

			int consumerId = Utils.getNewId(jdbc, "auditer_consumers_id_seq");
			jdbc.update("insert into auditer_consumers (id, name, last_processed_id) values (?,?,?)", consumerId, consumerName, lastProcessedId);
			log.debug("New consumer [name: '{}', lastProcessedId: '{}'] created.", consumerName, lastProcessedId);
		} catch(Exception e) {
			throw new InternalErrorException(e);
		}
	}

	private int getLastProcessedId(String consumerName) throws InternalErrorException {
		int lastProcessedId;

		try {
			lastProcessedId = jdbc.queryForInt("select last_processed_id from auditer_consumers where name=? for update", consumerName);
		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}

		return lastProcessedId;

	}

	public List<String> pollConsumerMessages(String consumerName) throws InternalErrorException {

		if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");

		try {
			if(jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
				throw new InternalErrorException("Auditer consumer doesn't exist.");
			}

			int lastProcessedId = getLastProcessedId(consumerName);

			int maxId = jdbc.queryForInt("select max(id) from auditer_log");
			if(maxId > lastProcessedId) {
				List<String> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITER_LOG_MAPPER, lastProcessedId, maxId);
				lastProcessedId = maxId;
				jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
				return messages;
			}
			return new ArrayList<>();
		} catch(Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<String> pollConsumerFullMessages(String consumerName) throws InternalErrorException {

		if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");

		try {
			if(jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
				throw new InternalErrorException("Auditer consumer doesn't exist.");
			}

			int lastProcessedId = getLastProcessedId(consumerName);

			int maxId = jdbc.queryForInt("select max(id) from auditer_log");
			if(maxId > lastProcessedId) {
				List<String> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITER_FULL_LOG_MAPPER, lastProcessedId, maxId);
				lastProcessedId = maxId;
				jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
				return messages;
			}
			return new ArrayList<>();
		} catch(Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<String> pollConsumerMessagesForParserSimple(String consumerName) throws InternalErrorException {

		if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");

		try {
			if(jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
				throw new InternalErrorException("Auditer consumer doesn't exist.");
			}

			int lastProcessedId = getLastProcessedId(consumerName);

			int maxId = jdbc.queryForInt("select max(id) from auditer_log");
			if(maxId > lastProcessedId) {
				List<String> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITER_LOG_MAPPER_FOR_PARSER, lastProcessedId, maxId);
				lastProcessedId = maxId;
				jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
				return messages;
			}
			return new ArrayList<>();
		} catch(Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<AuditMessage> pollConsumerMessagesForParser(String consumerName) throws InternalErrorException {
		if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");

		try {
			if(jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
				throw new InternalErrorException("Auditer consumer doesn't exist.");
			}

			int lastProcessedId = getLastProcessedId(consumerName);

			int maxId = jdbc.queryForInt("select max(id) from auditer_log");
			if(maxId > lastProcessedId) {
				List<AuditMessage> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITMESSAGE_MAPPER_FOR_PARSER, lastProcessedId, maxId);
				lastProcessedId = maxId;
				jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
				return messages;
			}
			return new ArrayList<>();
		} catch(Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

	/**
	 * Deserialize messages from JSON to the list of AuditEvent objects.
	 *
	 * @return List of AuditEvents parsed from JSON auditer log
	 */
	public List<AuditEvent> pollConsumerEvents(String consumerName) throws InternalErrorException {
		if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");

		try {

			if(jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
				throw new InternalErrorException("Auditer consumer doesn't exist.");
			}

			List<AuditEvent> eventList = new ArrayList<>();

			int lastProcessedId = getLastProcessedId(consumerName);
			int maxId = jdbc.queryForInt("select max(id) from auditer_log_json");
			if (maxId > lastProcessedId) {

				List<String> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log_json where id > ? and id <= ? order by id", AUDITER_LOG_MAPPER_FOR_PARSER, lastProcessedId, maxId);

				for (String m : messages) {
					AuditEvent event;
					// if deserialization fails, we don't return any more messages
					Class clazz = Class.forName(getNameOfClassAttribute(m));
					event = (AuditEvent) mapper.readValue(m, clazz);
					eventList.add(event);
				}

				// we successfully de-serialized all messages, bump counter
				jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", maxId, consumerName);

			}

			return eventList;

		} catch (JsonParseException | JsonMappingException ex) {
			log.error("Can't parse JSON auditer log!", ex);
			throw new InternalErrorException(ex);
		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}

	}

	/**
	 * Get name of class from json string. Used when AuditEvents are deserialized.
	 *
	 * @param jsonString audit message in json format
	 * @return name of class included in json string
	 */
	private String getNameOfClassAttribute(String jsonString) throws InternalErrorException {
		try {
			//get everything from in between of next quotes
			String message;
			int index = jsonString.indexOf("\"name\":\"cz");
			if (index != -1) {
				message = jsonString.substring(index + 8);
				message = message.substring(0, message.indexOf("\""));
				return message;
			}
		} catch (Exception e) {
			log.error("Could not get name from json string: \"{}\".", jsonString);
		}

		throw new InternalErrorException("Failed to read attribute 'name' from string: '" + jsonString + "'");
	}

	public Map<String, Integer> getAllAuditerConsumers(PerunSession sess) throws InternalErrorException {
		Map<String, Integer> auditerConsumers = new HashMap<>();

		try {
			auditerConsumers = jdbc.query("select name, last_processed_id from auditer_consumers", AUDITER_CONSUMER_EXTRACTOR);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		return auditerConsumers;
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
	 * @param alreadyResolvedMessages list of all already generated audit messages (used for checking a cycle between two or more registered modules)
	 *
	 * @return list of all resolved messages generated by registered attr modules
	 */
	private List<AuditerMessage> checkRegisteredAttributesModules(PerunSession session, List<AuditerMessage> messages, List<AuditerMessage> alreadyResolvedMessages) {
		List<AuditerMessage> addedResolvedMessages = new ArrayList<>();

		for (AuditerMessage message : messages) {
			for (AttributesModuleImplApi attributesModuleImplApi : registeredAttributesModules) {
				log.info("Message {} is given to module {}", message, attributesModuleImplApi.getClass().getSimpleName());

				try {
					List<AuditEvent> auditEvents = attributesModuleImplApi.resolveVirtualAttributeValueChange((PerunSessionImpl) session, message.getEvent());
					for (AuditEvent auditEvent : auditEvents) {
						addedResolvedMessages.add(new AuditerMessage(session, auditEvent));
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
