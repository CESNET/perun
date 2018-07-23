package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.implApi.AuditerListener;
import cz.metacentrum.perun.core.implApi.modules.attributes.VirtualAttributesModuleImplApi;
import net.jcip.annotations.GuardedBy;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    private LobHandler lobHandler;

    public final static String engineForceKeyword = "forceit";

    private final static Logger log = LoggerFactory.getLogger(Auditer.class);
    private final static Logger transactionLogger = LoggerFactory.getLogger("transactionLogger");
    private JdbcPerunTemplate jdbc;

    private Map<AuditerListener, ListenerThread> listenersMap = new HashMap<AuditerListener, ListenerThread>();

    private int lastProcessedId;

    private static final Object LOCK_DB_TABLE_AUDITER_LOG = new Object();
    private static final Object LOCK_DB_TABLE_AUDITER_LOG_JSON = new Object();

    private static Set<VirtualAttributesModuleImplApi> registeredAttributesModules = new HashSet<>();

    public static void registerAttributeModule(VirtualAttributesModuleImplApi virtualAttributesModuleImplApi) {
        log.trace("Auditer: Try to register module {}", (virtualAttributesModuleImplApi == null) ? null : virtualAttributesModuleImplApi.getClass().getName());
        if (virtualAttributesModuleImplApi != null && !registeredAttributesModules.contains(virtualAttributesModuleImplApi)) {
            registeredAttributesModules.add(virtualAttributesModuleImplApi);
            log.debug("Auditer: Module {} was registered for audit message listening.", virtualAttributesModuleImplApi.getClass().getName());
        }

    }

    protected static final RowMapper<AuditMessage> AUDITMESSAGE_MAPPER = new RowMapper<AuditMessage>() {
        public AuditMessage mapRow(ResultSet rs, int i) throws SQLException {
            AuditMessage auditMessage = AUDITMESSAGE_MAPPER_FOR_PARSER.mapRow(rs, i);
            auditMessage.setMsg(BeansUtils.eraseEscaping(BeansUtils.replaceEscapedNullByStringNull(BeansUtils.replacePointyBracketsByApostrophe(auditMessage.getMsg()))));
            return auditMessage;
        }
    };

    protected static final RowMapper<AuditMessage> AUDITMESSAGE_MAPPER_FOR_PARSER = new RowMapper<AuditMessage>() {
        public AuditMessage mapRow(ResultSet rs, int i) throws SQLException {

            String msg;
            if (Compatibility.isOracle()) {
                Clob clob = rs.getClob("msg");
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
                msg = rs.getString("msg");
            }
            // Get principal User and his ID (null, if no user exist)
            Integer principalUserId = null;
            if (rs.getInt("created_by_uid") != 0) principalUserId = rs.getInt("created_by_uid");
            AuditMessage auditMessage = new AuditMessage(rs.getInt("id"), msg, rs.getString("actor"), rs.getString("created_at"), principalUserId);
            return auditMessage;
        }
    };

    protected static final RowMapper<String> AUDITER_FULL_LOG_MAPPER = new RowMapper<String>() {
        public String mapRow(ResultSet rs, int i) throws SQLException {
            AuditMessage auditMessage = AUDITMESSAGE_MAPPER.mapRow(rs, i);
            return auditMessage.getFullMessage();
        }
    };

    protected static final RowMapper<String> AUDITER_LOG_MAPPER = new RowMapper<String>() {
        public String mapRow(ResultSet rs, int i) throws SQLException {
            AuditMessage auditMessage = AUDITMESSAGE_MAPPER.mapRow(rs, i);
            return auditMessage.getMsg();
        }
    };

    protected static final RowMapper<String> AUDITER_LOG_MAPPER_FOR_PARSER = new RowMapper<String>() {
        public String mapRow(ResultSet rs, int i) throws SQLException {
            AuditMessage auditMessage = AUDITMESSAGE_MAPPER_FOR_PARSER.mapRow(rs, i);
            return auditMessage.getMsg();
        }
    };


    protected static final AuditerConsumerExtractor AUDITER_CONSUMER_EXTRACTOR = new AuditerConsumerExtractor();

    private static class AuditerConsumerExtractor implements ResultSetExtractor<Map<String, Integer>> {

        public Map<String, Integer> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<String, Integer> auditerConsumers = new HashMap<>();
            while (rs.next()) {
                // fetch from map by ID
                String name = rs.getString("name");
                Integer lastProcessedId = rs.getInt("last_processed_id");
                auditerConsumers.put(name, lastProcessedId);
            }
            return auditerConsumers;
        }
    }


    public Auditer() {
    }

    public void setPerunPool(DataSource perunPool) throws InternalErrorException {
        this.jdbc = new JdbcPerunTemplate(perunPool);
        lobHandler = new DefaultLobHandler();
    }


    /**
     * Log message.
     * Message is stored in actual transaction. If no transaction is active message will be immediatelly flushed out.
     *
     * @param message
     * @throws InternalErrorException
     */
    public void log(PerunSession sess, String message) throws InternalErrorException {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            log.trace("Auditer stores audit message to current transaction. Message: {}.", message);
            List<List<List<AuditerMessage>>> topLevelTransactions = (List<List<List<AuditerMessage>>>) TransactionSynchronizationManager.getResource(this);
            if (topLevelTransactions == null) {
                newTopLevelTransaction();
                topLevelTransactions = (List<List<List<AuditerMessage>>>) TransactionSynchronizationManager.getResource(this);
            }
            // pick last top-level messages chain
            List<List<AuditerMessage>> transactionChain = topLevelTransactions.get(topLevelTransactions.size() - 1);
            // pick last messages in that chain
            List<AuditerMessage> messages = transactionChain.get(transactionChain.size() - 1);
            messages.add(new AuditerMessage(sess, message));
        } else {
            this.storeMessageToDb(sess, message);
        }
    }

    /**
     * Log message.
     * Takes event object and maps it to JSON string.
     *
     * @param sess  Perun session
     * @param event Audit event to be logged.
     * @throws InternalErrorException
     * @author Richard Husár 445238@mail.muni.cz
     */
    public void log(PerunSession sess, Object event) throws InternalErrorException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        try {
            String jsonString = mapper.writeValueAsString(event);
            log(sess, jsonString);

        } catch (IOException e) {
            log.error("Could not map event {} to JSON.", event.getClass().getSimpleName());
        }

    }

    /**
     * @author Richard Husár 445238@mail.muni.cz
     * @param jsonString
     * @return message attribute from json formated string
     */
    public String getMessageFromJsonString(String jsonString) {
        try {
            //get everything from in between of next quotes
            String message;
            int index = jsonString.indexOf("\"message\":");
            if(index != -1){
                message = jsonString.substring(index+11);
                message = message.substring(0,message.indexOf("\""));
                return message;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get message from json string: \"{}\".",jsonString);
        }
        return "";
    }

    /**
     *
     * removes ',' characters as well as whitespaces and newline characters before message attribute and ',' directly after message attribute
     * @author Richard Husár 445238@mail.muni.cz
     * @param jsonString
     * @return json string without message attribute
     */
    public String getMessageWithoutMessageAttribute(String jsonString){
        return jsonString.replaceAll(",?(\\r\\n|\\r|\\n)?\\s*\"message\":\".*\",?","");
    }



    /**
     * Log mesage. Substitute first {} with arg1.toString().
     *
     * @param message
     * @param arg1
     * @throws InternalErrorException
     */
    public void log(PerunSession sess, String message, Object arg1) throws InternalErrorException {
        log(sess, message, arg1, null);
    }

    /**
     * Log mesage. Substitute first {} with arg1.toString().
     * <p>
     * IMPORTANT: This method stores the message aside from DB transaction.
     *
     * @param message
     * @param arg1
     * @throws InternalErrorException
     */
    public void logWithoutTransaction(PerunSession sess, String message, Object arg1) throws InternalErrorException {
        logWithoutTransaction(sess, message, arg1, null);
    }

    /**
     * Log mesage. Substitute first two {} with arg1.toString() and arg2.toString().
     * <p>
     * IMPORTANT: This method stores the message aside from DB transaction.
     *
     * @param message
     * @param arg1
     * @throws InternalErrorException
     */
    public void logWithoutTransaction(PerunSession sess, String message, Object arg1, Object arg2) throws InternalErrorException {
        message = BeansUtils.createEscaping(message);
        Object[] objects = new Object[2];
        objects[0] = serializeObject(arg1);
        objects[1] = serializeObject(arg2);
        String formatedMessage = MessageFormatter.arrayFormat(message, objects).getMessage();
        storeMessageToDb(sess, formatedMessage);
    }

    /**
     * Log mesage. Substitute first two {} with arg1.toString() and arg2.toString().
     *
     * @param message
     * @param arg1
     * @param arg2
     * @throws InternalErrorException
     */
    public void log(PerunSession sess, String message, Object arg1, Object arg2) throws InternalErrorException {
        message = BeansUtils.createEscaping(message);
        String formatedMessage = MessageFormatter.format(message, this.serializeObject(arg1), this.serializeObject(arg2)).getMessage();
        log(sess, formatedMessage);
    }

    public void log(PerunSession sess, String message, Object arg1, Object arg2, Object arg3) throws InternalErrorException {
        message = BeansUtils.createEscaping(message);
        Object[] objects = new Object[3];
        objects[0] = serializeObject(arg1);
        objects[1] = serializeObject(arg2);
        objects[2] = serializeObject(arg3);
        String formatedMessage = MessageFormatter.arrayFormat(message, objects).getMessage();
        log(sess, formatedMessage);
    }

    public void log(PerunSession sess, String message, Object arg1, Object arg2, Object arg3, Object arg4) throws InternalErrorException {
        message = BeansUtils.createEscaping(message);
        Object[] objects = new Object[4];
        objects[0] = serializeObject(arg1);
        objects[1] = serializeObject(arg2);
        objects[2] = serializeObject(arg3);
        objects[3] = serializeObject(arg4);
        String formatedMessage = MessageFormatter.arrayFormat(message, objects).getMessage();
        log(sess, formatedMessage);
    }

    /**
     * This method take Object and if its PerunBean so serialize it for Auditer and
     * if it is list of beans, so try to find perunBeans in it and serialize them
     * in the list.
     *
     * @param arg (object)
     * @return arg (object)
     */
    private Object serializeObject(Object arg) {
        if (arg instanceof List) {
            List<Object> argObjects = (List<Object>) arg;
            List<Object> newArg = new ArrayList<Object>();
            for (Object o : argObjects) {
                if (o instanceof PerunBean) newArg.add(((PerunBean) o).serializeToString());
                else newArg.add(o);
            }
            arg = newArg;
        } else if (arg instanceof PerunBean) {
            arg = ((PerunBean) arg).serializeToString();
        }
        return arg;
    }

    /**
     * Imidiately fluses mesage to output.
     *
     * @param message
     */
    @Deprecated
    private void flush(String message) {
        log.info("AUDIT: {}", message);
    }

    /**
     * Initialize new lists for sotring Audit messages.
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


        for (AuditerMessage message : messages) {
            for (VirtualAttributesModuleImplApi virtAttrModuleImplApi : registeredAttributesModules) {
                List<String> resolvingMessages = new ArrayList<String>();
                try {
                    resolvingMessages.addAll(virtAttrModuleImplApi.resolveVirtualAttributeValueChange((PerunSessionImpl) message.getOriginaterPerunSession(), message.getMessage()));
                } catch (InternalErrorException ex) {
                    log.error("Error when auditer trying to resolve messages in modules.", ex);
                } catch (WrongAttributeAssignmentException ex) {
                    log.error("Error when auditer trying to resolve messages in modules.", ex);
                } catch (WrongReferenceAttributeValueException ex) {
                    log.error("Error when auditer trying to resolve messages in modules.", ex);
                } catch (AttributeNotExistsException ex) {
                    log.error("Error when auditer trying to resolve messages in modules.", ex);
                }

                if (!resolvingMessages.isEmpty()) {
                    List<AuditerMessage> resolvingAuditerMessages = new ArrayList<>();
                    for (String msg : resolvingMessages) {
                        resolvingAuditerMessages.add(new AuditerMessage(message.getOriginaterPerunSession(), msg));
                    }
                    storeMessagesToDb(resolvingAuditerMessages);
                }
            }
        }
    }

    /**
     * All prepared auditer messages in the last top-level transaction are erased without storing into db.
     * Mostly wanted while rollbacking.
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
        for (List<AuditerMessage> list :transactionChain) {
            for (AuditerMessage message : list){
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
            return jdbc.query("select " + auditMessageMappingSelectQuery + " from (select " + auditMessageMappingSelectQuery + Compatibility.getRowNumberOver() + " from auditer_log) " + Compatibility.getAsAlias("temp") + " where rownumber <= ?",
                    AUDITMESSAGE_MAPPER, count);
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<AuditMessage>();
        } catch (RuntimeException err) {
            throw new InternalErrorException(err);
        }
    }

    /**
     * Gets last count messages from auditer_log_json
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
            return new ArrayList<AuditMessage>();
        } catch (RuntimeException err) {
            throw new InternalErrorException(err);
        }
    }

    public List<AuditMessage> getMessagesByCount(int count) throws InternalErrorException {
        try {
            return jdbc.query("select " + auditMessageMappingSelectQuery + " from auditer_log where id > ((select max(id) from auditer_log)-?) order by id desc", AUDITMESSAGE_MAPPER, count);
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<AuditMessage>();
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
            jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
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
            return jdbc.query("select " + auditMessageMappingSelectQuery + " from (select " + auditMessageMappingSelectQuery + ",row_number() over (ORDER BY id DESC) as rownumber from auditer_log) " + Compatibility.getAsAlias("temp") + " where rownumber <= ?",
                    AUDITMESSAGE_MAPPER_FOR_PARSER, count);
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<AuditMessage>();
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
     * Stores the auditer messages to the DB in batch.
     *
     * @param messages list of AuditerMessages
     */
    public void storeMessagesToDb(final List<AuditerMessage> messages) {
        final List<Integer> ids = new ArrayList<>();
        final List<PubsubMechanizm.Pair<AuditerMessage,Integer>> msgs = new ArrayList<>();
        synchronized (LOCK_DB_TABLE_AUDITER_LOG) {

            try {
                jdbc.batchUpdate("insert into auditer_log (id, msg, actor, created_at, created_by_uid) values (?,?,?," + Compatibility.getSysdate() + ",?)",
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                final AuditerMessage auditerMessage = messages.get(i);
                                //final String message = auditerMessage.getMessage();
                                final String message = getMessageFromJsonString(auditerMessage.getMessage());
                                final PerunSession session = auditerMessage.getOriginaterPerunSession();
                                log.info("AUDIT: {}", message);
                                try {
                                    final int msgId = Utils.getNewId(jdbc, "auditer_log_id_seq");
                                    ps.setInt(1, msgId);
                                    ids.add(msgId);
                                    msgs.add(new PubsubMechanizm.Pair<AuditerMessage,Integer>(auditerMessage,msgId));
                                } catch (InternalErrorException e) {
                                    throw new SQLException("Cannot get unique id for new auditer log message ['" + message + "']", e);
                                }
                                ps.setString(2, message);
                                ps.setString(3, session.getPerunPrincipal().getActor());
                                ps.setInt(4, session.getPerunPrincipal().getUserId());
                            }

                            @Override
                            public int getBatchSize() {
                                return messages.size();
                            }
                        });
            } catch (RuntimeException e) {
                log.error("Cannot store auditer log message in batch for list ['{}'], exception: {}, size: {}", messages,e.getMessage(), messages.size());
            } catch (InternalErrorException e) {
                log.error("Could not get system date identifier for the DB", e);
            }
        }

        this.storeMessagesToDbJson(msgs);
    }

    /**
     * Stores messages to audit_log_JSON table
     * @param messages takes pair of auditer message to be stored and id of message from audit_log
     * @author Richard Husár 445238@mail.muni.cz
     */
    public void storeMessagesToDbJson(final List<PubsubMechanizm.Pair<AuditerMessage,Integer>> messages) {
        synchronized (LOCK_DB_TABLE_AUDITER_LOG_JSON) {
            try {
                jdbc.batchUpdate("insert into auditer_log_json (id, msg, actor, created_at, created_by_uid) values (?,?,?," + Compatibility.getSysdate() + ",?)",
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                final AuditerMessage auditerMessage = messages.get(i).getLeft();
                                //store message without duplicit message attribute because it is stored in separate table
                                final String message = getMessageWithoutMessageAttribute(auditerMessage.getMessage());
                                final PerunSession session = auditerMessage.getOriginaterPerunSession();
                                log.info("AUDIT: {}", message);
                                ps.setInt(1, messages.get(i).getRight());
                                ps.setString(2, message);
                                ps.setString(3, session.getPerunPrincipal().getActor());
                                ps.setInt(4, session.getPerunPrincipal().getUserId());
                            }

                            @Override
                            public int getBatchSize() {
                                return messages.size();
                            }
                        });
            } catch (RuntimeException e) {
                log.error("Cannot store auditer log JSON message in batch for list ['{}'], exception: {}", messages, e.getMessage());
            } catch (InternalErrorException e) {
                log.error("Could not get system date identifier for the DB", e);
            }
        }
    }


    /**
     * Store the message to the DB.
     *
     * @param sess
     * @param message
     */
    public void storeMessageToDb(final PerunSession sess, final String message) {
        int id = -1;
        synchronized (LOCK_DB_TABLE_AUDITER_LOG) {
            try {
                final int msgId = Utils.getNewId(jdbc, "auditer_log_id_seq");
                id = msgId;
                jdbc.execute("insert into auditer_log (id, msg, actor, created_at, created_by_uid) values (?,?,?," + Compatibility.getSysdate() + ",?)",
                        new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                            public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                ps.setInt(1, msgId);
                                //lobCreator.setClobAsString(ps, 2, message);
                                lobCreator.setClobAsString(ps, 2, getMessageFromJsonString(message)); //old format message
                                ps.setString(3, sess.getPerunPrincipal().getActor());
                                ps.setInt(4, sess.getPerunPrincipal().getUserId());
                            }
                        }
                );
            } catch (RuntimeException e) {
                log.error("Cannot store auditer log message ['{}'], exception: {}", message, e);
            } catch (InternalErrorException e) {
                log.error("Cannot get unique id for new auditer log message ['{}'], exception: {}", message, e);
            }
        }

        //store parallelly to auditer_log_json in json formate
        this.storeMessageToDbJson(sess,message,id);

    }

    /**
     * Stores message to audit_log_JSON table
     * @param sess session
     * @param message message to be stored
     * @param id id of message in audit_log
     * @author Richard Husár 445238@mail.muni.cz
     */
    public void storeMessageToDbJson(final PerunSession sess, final String message, int id) {
        synchronized (LOCK_DB_TABLE_AUDITER_LOG_JSON) {
            try {
                jdbc.execute("insert into auditer_log_json (id, msg, actor, created_at, created_by_uid) values (?,?,?," + Compatibility.getSysdate() + ",?)",
                        new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                            public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                ps.setInt(1, id);
                                //store message without duplicit message attribute because it is stored in separate table
                                lobCreator.setClobAsString(ps, 2, getMessageWithoutMessageAttribute(message));
                                ps.setString(3, sess.getPerunPrincipal().getActor());
                                ps.setInt(4, sess.getPerunPrincipal().getUserId());
                            }
                        }
                );
            } catch (RuntimeException e) {
                log.error("Cannot store auditer log message ['{}'], exception: {}", message, e);
            } catch (InternalErrorException e) {
                log.error("Cannot get unique id for new auditer log message ['{}'], exception: {}", message, e);
            }
        }
    }

    public void createAuditerConsumer(String consumerName) throws InternalErrorException {
        try {
            int lastProcessedId = jdbc.queryForInt("select max(id) from auditer_log");

            int consumerId = Utils.getNewId(jdbc, "auditer_consumers_id_seq");
            jdbc.update("insert into auditer_consumers (id, name, last_processed_id) values (?,?,?)", consumerId, consumerName, lastProcessedId);
            log.debug("New consumer [name: '{}', lastProcessedId: '{}'] created.", consumerName, lastProcessedId);
        } catch (Exception e) {
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
            if (jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
                throw new InternalErrorException("Auditer consumer doesn't exist.");
            }

            int lastProcessedId = getLastProcessedId(consumerName);

            int maxId = jdbc.queryForInt("select max(id) from auditer_log");
            if (maxId > lastProcessedId) {
                List<String> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITER_LOG_MAPPER, lastProcessedId, maxId);
                lastProcessedId = maxId;
                jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
                return messages;
            }
            return new ArrayList<String>();
        } catch (Exception ex) {
            throw new InternalErrorException(ex);
        }
    }

    public List<String> pollConsumerFullMessages(String consumerName) throws InternalErrorException {

        if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");

        try {
            if (jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
                throw new InternalErrorException("Auditer consumer doesn't exist.");
            }

            int lastProcessedId = getLastProcessedId(consumerName);

            int maxId = jdbc.queryForInt("select max(id) from auditer_log");
            if (maxId > lastProcessedId) {
                List<String> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITER_FULL_LOG_MAPPER, lastProcessedId, maxId);
                lastProcessedId = maxId;
                jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
                return messages;
            }
            return new ArrayList<String>();
        } catch (Exception ex) {
            throw new InternalErrorException(ex);
        }
    }

    public List<String> pollConsumerMessagesForParserSimple(String consumerName) throws InternalErrorException {

        if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");

        try {
            if (jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
                throw new InternalErrorException("Auditer consumer doesn't exist.");
            }

            int lastProcessedId = getLastProcessedId(consumerName);

            int maxId = jdbc.queryForInt("select max(id) from auditer_log");
            if (maxId > lastProcessedId) {
                List<String> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITER_LOG_MAPPER_FOR_PARSER, lastProcessedId, maxId);
                lastProcessedId = maxId;
                jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
                return messages;
            }
            return new ArrayList<String>();
        } catch (Exception ex) {
            throw new InternalErrorException(ex);
        }
    }

    public List<AuditMessage> pollConsumerMessagesForParser(String consumerName) throws InternalErrorException {
        if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");

        try {
            if (jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
                throw new InternalErrorException("Auditer consumer doesn't exist.");
            }

            int lastProcessedId = getLastProcessedId(consumerName);

            int maxId = jdbc.queryForInt("select max(id) from auditer_log");
            if (maxId > lastProcessedId) {
                List<AuditMessage> messages = jdbc.query("select " + Auditer.auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDITMESSAGE_MAPPER_FOR_PARSER, lastProcessedId, maxId);
                lastProcessedId = maxId;
                jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
                return messages;
            }
            return new ArrayList<AuditMessage>();
        } catch (Exception ex) {
            throw new InternalErrorException(ex);
        }
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

    /**
     * Register the listener.
     *
     * @param listener
     * @return false if the listener is already registered
     */
    @Deprecated
    public boolean registerListener(AuditerListener listener, String name) throws InternalErrorException {
        if (listenersMap.containsKey(listener)) return false;

        ListenerThread listenerThread = new ListenerThread(listener);
        listenersMap.put(listener, listenerThread);
        listenerThread.setConsumerName(name);

        // Get the last processed id of the current consumer
        try {
            // Check if the consumer is registered
            if (0 == jdbc.queryForInt("select count(id) from auditer_consumers where name=?", name)) {
                // Create the consumer
                int consumerId = Utils.getNewId(jdbc, "auditer_consumers_id_seq");
                jdbc.update("insert into auditer_consumers (id, name, last_processed_id) values (?,?,?)", consumerId, name, this.lastProcessedId);
                log.debug("New consumer ['{}'] created.", name);
                listenerThread.setLastProcessedId(this.lastProcessedId);
            } else {
                listenerThread.setLastProcessedId(jdbc.queryForInt("select last_processed_id from auditer_consumers where name=?", name));
            }
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }

        listenerThread.start();
        log.debug("New Auditer listener registered. {}", listener);
        return true;
    }

    /**
     * Unregister the listener. All unprocessed messages is lost.
     *
     * @param listener
     * @return false if the listener wasn't registered
     */
    @Deprecated
    public boolean unregisterListener(AuditerListener listener) {
        if (!listenersMap.containsKey(listener)) return false;

        ListenerThread listenerThread = listenersMap.remove(listener);
        log.debug("Sending interrupt signal to listeners thread. Listener: {}", listener);
        listenerThread.interrupt();
        return true;
    }

    public void initialize() throws InternalErrorException {
        try {
            this.lastProcessedId = jdbc.queryForInt("select max(id) from auditer_log");
            log.debug("Auditer initialized with lastProcessedId [{}].", this.lastProcessedId);
        } catch (RuntimeException e) {
            throw new InternalErrorException(e);
        }
    }

    private static class ListenerThread extends Thread {

        private AuditerListener listener;
        private int lastProcessedId;
        private String consumerName;

        private BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

        public ListenerThread(AuditerListener listener) {
            this.listener = listener;
        }

        public void addMessage(String message) {
            queue.add(message);
        }

        public void addMessages(List<String> messages) {
            for (String message : messages) {
                queue.add(message);
            }
        }

        public int getLastProcessedId() {
            return lastProcessedId;
        }

        public void setLastProcessedId(int lastProcessedId) {
            this.lastProcessedId = lastProcessedId;
        }

        public String getConsumerName() {
            return consumerName;
        }

        public void setConsumerName(String consumerName) {
            this.consumerName = consumerName;
        }

        public void run() {
            try {
                while (!this.isInterrupted()) {
                    listener.notifyWith(queue.take());
                }
            } catch (InterruptedException ex) {
                //mark this thread as interrupted.
                this.interrupt();
            } finally {
                log.debug("ListenerThread stopped. Queue with unprocessed mesages: {}", queue);
            }
        }
    }
}
