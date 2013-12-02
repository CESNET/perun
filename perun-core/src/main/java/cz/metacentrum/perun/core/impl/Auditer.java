package cz.metacentrum.perun.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.PreparedStatement;

import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;

import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jdbc.support.lob.OracleLobHandler;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor;

import cz.metacentrum.perun.core.implApi.AuditerListener;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.implApi.modules.attributes.VirtualAttributesModuleImplApi;

import net.jcip.annotations.GuardedBy;

/**
 * This class is used for logging audit events. It get messages and stored it in asociation with current transaction. If there's no transaction currently running, message is immediateli flushed out.
 * When transaction ends, transaction manager must call method flush in this clas for the ending transaction.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 * @version $Id$
 */
public class Auditer {

    @GuardedBy("Auditer.class")
    private static volatile Auditer selfInstance;

    protected final static String auditMessageMappingSelectQuery = "id, msg, actor, created_at, created_by_uid";
    private LobHandler lobHandler;

    public final static String engineForceKeyword = "forceit";

    private final static Logger log = LoggerFactory.getLogger(Auditer.class);
    private JdbcTemplate jdbc;

    private Map<AuditerListener, ListenerThread> listenersMap = new HashMap<AuditerListener, ListenerThread>();

    private int lastProcessedId;

    private static final Object LOCK_DB_TABLE_AUDITER_LOG = new Object();

    private static List<VirtualAttributesModuleImplApi> registeredAttributesModules = new ArrayList<VirtualAttributesModuleImplApi>();

    public static void registerAttributeModule(VirtualAttributesModuleImplApi virtualAttributesModuleImplApi) {
        log.trace("Auditer: Try to load module {}", virtualAttributesModuleImplApi.getClass().getName());
        if(virtualAttributesModuleImplApi != null && !registeredAttributesModules.contains(virtualAttributesModuleImplApi)) {
            registeredAttributesModules.add(virtualAttributesModuleImplApi);
            log.debug("Auditer: Module {} was loaded.", virtualAttributesModuleImplApi.getClass().getName());
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
            try {
                if (Compatibility.isOracle()) {
                    Clob clob = rs.getClob("msg");
                    char[] cbuf = null;
                    if(clob == null) {
                        msg = null;
                    } else {
                        try {
                            cbuf = new char[(int) clob.length()];
                            clob.getCharacterStream().read(cbuf);
                        } catch(IOException ex) {
                            throw new InternalErrorRuntimeException(ex);
                        }
                        msg = new String(cbuf);
                    }
                } else {
                    msg = rs.getString("msg");
                }
            } catch (InternalErrorException ex) {
                // As backup use postgress way
                msg = rs.getString("msg");
            }

            // Get principal User and his ID (null, if no user exist)
            Integer principalUserId = null;
            if(rs.getInt("created_by_uid") != 0) principalUserId = rs.getInt("created_by_uid");
            AuditMessage auditMessage = new AuditMessage(rs.getInt("id"), msg, rs.getString("actor"), rs.getString("created_at"), principalUserId);
            return auditMessage;
        }
    };

    public Auditer() {
    }

    public void setPerunPool(DataSource perunPool) throws InternalErrorException {
        this.jdbc = new JdbcTemplate(perunPool);
        if(Compatibility.isOracle()) {
            OracleLobHandler oracleLobHandler = new OracleLobHandler();
            oracleLobHandler.setNativeJdbcExtractor(new CommonsDbcpNativeJdbcExtractor());
            lobHandler = oracleLobHandler;
        } else {
            lobHandler = new DefaultLobHandler();
        }
    }

    /**
     * Log message.
     * Message is stored in actual transaction. If no transaction is active message will be immediatelly flushed out.
     *
     * @param message
     * @throws InternalErrorException
     */
    public void log(PerunSession sess, String message) throws InternalErrorException {
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            log.trace("Auditer stores audit message to current transaction. Message: {}.", message);
            List<AuditerMessage> messages = (List<AuditerMessage>) TransactionSynchronizationManager.getResource(this);
            if(messages == null) {
                messages = new ArrayList<AuditerMessage>();
                TransactionSynchronizationManager.bindResource(this, messages);
            }
            messages.add(new AuditerMessage(sess, message));
        } else {
            this.storeMessageToDb(sess, message);
        }
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
     * Log mesage. Substitute first two {} with arg1.toString() and arg2.toString().
     *
     * @param message
     * @param arg1
     * @param arg2
     * @throws InternalErrorException
     */
    public void log(PerunSession sess, String message, Object arg1, Object arg2) throws InternalErrorException {
        message = BeansUtils.createEscaping(message);
        String formatedMessage = MessageFormatter.format(message, this.serializeObject(arg1), this.serializeObject(arg2));
        log(sess, formatedMessage);
    }

    public void log(PerunSession sess, String message, Object arg1, Object arg2, Object arg3) throws InternalErrorException {
        message = BeansUtils.createEscaping(message);
        Object[] objects = new Object[3];
        objects[0] = serializeObject(arg1);
        objects[1] = serializeObject(arg2);
        objects[2] = serializeObject(arg3);
        String formatedMessage = MessageFormatter.arrayFormat(message, objects);
        log(sess, formatedMessage);
    }

    public void log(PerunSession sess, String message, Object arg1, Object arg2, Object arg3, Object arg4) throws InternalErrorException {
        message = BeansUtils.createEscaping(message);
        Object[] objects = new Object[4];
        objects[0] = serializeObject(arg1);
        objects[1] = serializeObject(arg2);
        objects[2] = serializeObject(arg3);
        objects[3] = serializeObject(arg4);
        String formatedMessage = MessageFormatter.arrayFormat(message, objects);
        log(sess, formatedMessage);
    }

    /**
     * This method take Object and if its PerunBean so serialize it for Auditer and
     * if it is list of beans, so try to find perunBeans in it and serialize them
     * in the list.
     *
     * @param arg (object)
     *
     * @return arg (object)
     */
    private Object serializeObject(Object arg) {
        if(arg instanceof List) {
            List<Object> argObjects = (List<Object>) arg;
            List<Object> newArg = new ArrayList<Object>();
            for(Object o: argObjects) {
                if(o instanceof PerunBean) newArg.add(((PerunBean)o).serializeToString());
                else newArg.add(o);
            }
            arg = newArg;
        } else if(arg instanceof PerunBean) {
            arg = ((PerunBean)arg).serializeToString();
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
     * Imidiately flushes stored message for specified transaction into the log
     *
     * @param transaction
     */
    public void flush() {
        List<AuditerMessage> messages = (List<AuditerMessage>) TransactionSynchronizationManager.unbindResourceIfPossible(this);
        if(messages == null) {
            log.trace("No message to flush");
            return;
        }

        log.trace("Audit messages was flushed for current transaction.");
        synchronized (LOCK_DB_TABLE_AUDITER_LOG) {
            for(AuditerMessage auditerMessage : messages) {
                log.info("AUDIT: {}", auditerMessage.getMessage());
                storeMessageToDb(auditerMessage.getOriginaterPerunSession(), auditerMessage.getMessage());
            }
        }

        //TODO: Co kdyz se zpravy prohazi a prvne se vyresi zprava ktera prisla az jako druha?
        for(AuditerMessage message: messages) {
            for(VirtualAttributesModuleImplApi virtAttrModuleImplApi : registeredAttributesModules) {
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

                if(!resolvingMessages.isEmpty()) {
                    for(String msg : resolvingMessages) {
                        log.info("AUDIT: {}", msg);
                        storeMessageToDb(message.getOriginaterPerunSession(), msg);
                    }
                }
            }
        }
    }

    public void clean() {
        List<AuditerMessage> messages = (List<AuditerMessage>) TransactionSynchronizationManager.unbindResourceIfPossible(this);
        log.trace("Audit messages erased for current transaction. {}", messages);
    }

    /**
     * Get stored (not flushed) messages for current transaction. Messages remains stored.
     *
     * @return list of messages
     */
    public List<AuditerMessage> getMessages() {
        List<AuditerMessage> messages = (List<AuditerMessage>) TransactionSynchronizationManager.getResource(this);
        if(messages == null) return new ArrayList<AuditerMessage>();
        return messages;
    }

    public List<AuditMessage> getMessages(int count) throws InternalErrorException {
        try {
            return jdbc.query("select " + auditMessageMappingSelectQuery + " from (select " + auditMessageMappingSelectQuery + ",row_number() over (ORDER BY id DESC) as rownumber from auditer_log) "+Compatibility.getAsAlias("temp")+" where rownumber <= ?",
                    AUDITMESSAGE_MAPPER, count);
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
          jdbc.update("update auditer_consumers set last_processed_id=? where name=?", lastProcessedId, consumerName);
      } catch (Exception ex) {
          throw new InternalErrorException(ex);
      }
    }
    
    public List<AuditMessage> getMessageForParser(int count) throws InternalErrorException {
        try {
            return jdbc.query("select " + auditMessageMappingSelectQuery + " from (select " + auditMessageMappingSelectQuery + ",row_number() over (ORDER BY id DESC) as rownumber from auditer_log) "+Compatibility.getAsAlias("temp")+" where rownumber <= ?",
                    AUDITMESSAGE_MAPPER_FOR_PARSER, count);
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<AuditMessage>();
        } catch (RuntimeException err) {
            throw new InternalErrorException(err);
        }
    }

    /**
     * Store the message to the DB.
     *
     * @param message
     * @throws InternalErrorException
     */
    public void storeMessageToDb(final PerunSession sess, final String message) {
        synchronized (LOCK_DB_TABLE_AUDITER_LOG) {
            try {
                final int msgId = Utils.getNewId(jdbc, "auditer_log_id_seq");
                jdbc.execute("insert into auditer_log (id, msg, actor, created_at, created_by_uid) values (?,?,?," + Compatibility.getSysdate() + ",?)",
                        new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
                            public void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
                                ps.setInt(1, msgId);
                                lobCreator.setClobAsString(ps, 2, message);
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

    /**
     * Register the listener.
     *
     * @param listener
     * @return false if the listener is already registered
     */
    @Deprecated
    public boolean registerListener(AuditerListener listener, String name) throws InternalErrorException {
        if(listenersMap.containsKey(listener)) return false;

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
        if(!listenersMap.containsKey(listener)) return false;

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
            for(String message : messages) {
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
                while(!this.isInterrupted()) {
                    listener.notifyWith(queue.take());
                }
            } catch(InterruptedException ex) {
                //mark this thread as interrupted.
                this.interrupt();
            } finally {
                log.debug("ListenerThread stopped. Queue with unprocessed mesages: {}", queue);
            }
        }
    }
}
