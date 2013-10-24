package cz.metacentrum.perun.notif.listener;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.AuditerConsumer;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifPoolMessage;
import cz.metacentrum.perun.notif.managers.PerunNotifAuditMessageManager;
import cz.metacentrum.perun.notif.managers.PerunNotifPoolMessageManager;
import cz.metacentrum.perun.notif.managers.PerunNotifRegexManager;
import cz.metacentrum.perun.notif.managers.PerunNotifTemplateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Listener for auditor messages
 * Handles receive of auditer message, storing to db to ensure processing
 * Then starts processing ending in creation of poolMessage
 * If process is finished than auditer message is removed from db
 *
 * @author tomas.tunkl
 */
@SuppressWarnings("deprecation")
@Service
public class NotificationListener implements DisposableBean {

    private PerunSession session;

    @Autowired
    private PerunNotifAuditMessageManager perunNotifAuditMessagesManager;

    @Autowired
    private PerunNotifRegexManager perunNotifRegexManager;

    @Autowired
    private PerunNotifTemplateManager perunNotifTemplateManager;

    @Autowired
    private PerunNotifPoolMessageManager perunNotifPoolMessageManager;

    @Autowired
    private Properties propertiesBean;

    @Autowired
    private DataSource dataSource;

    //Lock used for reading new messages from auditer
    private static final ReentrantLock readLock = new ReentrantLock();
    //Lock used when process of old messages is triggered by time
    private static final ReentrantLock oldProcessLock = new ReentrantLock();

    private static final int MAX_PERMITS_PROCESS_SEMAPHORE = 100;
    //Semaphore is used for process of perun messages, we can stop processing in case of shuting down application
    private static final Semaphore processSemaphore = new Semaphore(MAX_PERMITS_PROCESS_SEMAPHORE, true);

    private volatile boolean running = true;

    private AuditerConsumer auditerConsumer;

    private final static Logger logger = LoggerFactory.getLogger(NotificationListener.class);

    /**
     * Method starts processing of unresolved auditer messages
     */
    @PostConstruct
    public void init() throws InternalErrorException {
        String dispatcherName = (String) propertiesBean.get("notif.dispatcherName");

        this.auditerConsumer = new AuditerConsumer(dispatcherName, dataSource);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new ProcessOldPerunNotifAuditMessagesRunnable(), 0, 300, TimeUnit.SECONDS);
    }

    private class ProcessOldPerunNotifAuditMessagesRunnable implements Runnable {

        @Override
        public void run() {

            try {
                processOldPerunNotifAuditMessages();
            } catch (Exception ex) {
                logger.error("Processing of old perunNotifAuditMessages has failed.", ex);
                return;
            }
        }
    }

    public void start() {

        while (running) {
            oldProcessLock.lock();
            readLock.lock();
            List<PerunNotifAuditMessage> perunNotifAuditMessages = new ArrayList<PerunNotifAuditMessage>();
            try {
                List<String> messages = this.auditerConsumer.getMessagesForParser();
                for (String message : messages) {
                    try {
                        PerunNotifAuditMessage perunNotifAuditMessage = perunNotifAuditMessagesManager.saveMessageToPerunAuditerMessage(message, session);
                        perunNotifAuditMessages.add(perunNotifAuditMessage);
                    } catch (InternalErrorException ex) {
                        logger.error("Error during saving message to db. Message: " + message);
                    }
                }
            } catch (Exception ex) {
                logger.error("Error during perunNotification process.", ex);
            }
            readLock.unlock();

            for (PerunNotifAuditMessage perunAuditMessage : perunNotifAuditMessages) {
                processPerunNotifAuditMessage(perunAuditMessage, session);
            }
            oldProcessLock.unlock();
        }
    }

    /**
     * Handles processing of auditer message and in case of success removes auditer message from db
     *
     * @param perunAuditMessage
     * @param session
     * @throws InternalErrorException
     */
    private void processPerunNotifAuditMessage(PerunNotifAuditMessage perunAuditMessage, PerunSession session) {

        boolean acguired = false;
        while (!acguired) {
            try {
                processSemaphore.acquire();
                acguired = true;
            } catch (InterruptedException ex) {
                logger.error("Acquire of permit from semaphore for processPerunNotifAuditMessage interrupted.");
            }
        }
        try {
            logger.debug("Getting regexIds, matching received message with id: " + perunAuditMessage.getId());
            Set<Integer> regexIds = perunNotifRegexManager.getIdsOfRegexesMatchingMessage(perunAuditMessage);
            logger.debug("Received regexIds for message with id: " + perunAuditMessage.getId() + "; regexIds = " + regexIds + "; now getting templateIds.");
            if (regexIds == null || regexIds.isEmpty()) {
                logger.info("Message is not recognized: " + perunAuditMessage.getMessage());
                return;
            }
            List<PerunNotifPoolMessage> perunNotifPoolMessages = null;
            try {
                perunNotifPoolMessages = perunNotifTemplateManager.getPerunNotifPoolMessagesForRegexIds(regexIds, perunAuditMessage, session);
            } catch (InternalErrorException ex) {
                logger.error("Error during processPerunNotifAuditMessage.", ex);
                return;
            }

            if (perunNotifPoolMessages != null && !perunNotifPoolMessages.isEmpty()) {
                try {
                    perunNotifPoolMessageManager.savePerunNotifPoolMessages(perunNotifPoolMessages);
                } catch (InternalErrorException ex) {
                    logger.error("Error during saving pool message.", ex);
                    return;
                }
            } else {
                logger.warn("No pool messages recognized for message: " + perunAuditMessage.getMessage());
            }

            logger.info("Removing saved perunMessage with id=" + perunAuditMessage.getId());
            perunNotifAuditMessagesManager.removePerunAuditerMessageById(perunAuditMessage.getId());
        } catch (Exception ex) {
            logger.error("Error during process of perun notif audit message: {}", perunAuditMessage.getId(), ex);
        } finally {
            processSemaphore.release();
        }
    }

    /**
     * Loads old messages from db restart their processing
     *
     * @throws InternalErrorException
     */
    private void processOldPerunNotifAuditMessages() throws InternalErrorException {

        oldProcessLock.lock();
        logger.debug("Processing old perunNotifAuditMessages");
        List<PerunNotifAuditMessage> oldAuditMessages = null;
        try {
            oldAuditMessages = perunNotifAuditMessagesManager.getAll();
        } catch (Exception ex) {
            logger.error("Error during getting all old messages.", ex);
        }
        if (oldAuditMessages != null && !oldAuditMessages.isEmpty()) {
            for (PerunNotifAuditMessage perunAuditMessage : oldAuditMessages) {
                processPerunNotifAuditMessage(perunAuditMessage, session);
            }
        }
        oldProcessLock.unlock();
    }

    public void processOneAuditerMessage(String message) {

        PerunNotifAuditMessage perunNotifAuditMessage = null;
        try {
            perunNotifAuditMessage = perunNotifAuditMessagesManager.saveMessageToPerunAuditerMessage(message, session);
        } catch (InternalErrorException ex) {
            logger.error("Error during saving one time auditer message: " + message);
        }

        processPerunNotifAuditMessage(perunNotifAuditMessage, session);
    }

    @Override
    public void destroy() throws Exception {

        //We acquire read lock, then we know that reading is not running
        readLock.lock();
        //We acquire all permits from processSemaphore and stops processing of messages
        processSemaphore.acquireUninterruptibly(MAX_PERMITS_PROCESS_SEMAPHORE);
    }

    public PerunSession getSession() {
        return session;
    }

    public void setSession(PerunSession session) {
        this.session = session;
    }

    public PerunNotifAuditMessageManager getPerunNotifAuditMessagesManager() {
        return perunNotifAuditMessagesManager;
    }

    public void setPerunNotifAuditMessagesManager(PerunNotifAuditMessageManager perunNotifAuditMessagesManager) {
        this.perunNotifAuditMessagesManager = perunNotifAuditMessagesManager;
    }

    public PerunNotifRegexManager getPerunNotifRegexManager() {
        return perunNotifRegexManager;
    }

    public void setPerunNotifRegexManager(PerunNotifRegexManager perunNotifRegexManager) {
        this.perunNotifRegexManager = perunNotifRegexManager;
    }

    public PerunNotifTemplateManager getPerunNotifTemplateManager() {
        return perunNotifTemplateManager;
    }

    public void setPerunNotifTemplateManager(PerunNotifTemplateManager perunNotifTemplateManager) {
        this.perunNotifTemplateManager = perunNotifTemplateManager;
    }

    public PerunNotifPoolMessageManager getPerunNotifPoolMessageManager() {
        return perunNotifPoolMessageManager;
    }

    public void setPerunNotifPoolMessageManager(PerunNotifPoolMessageManager perunNotifPoolMessageManager) {
        this.perunNotifPoolMessageManager = perunNotifPoolMessageManager;
    }
}
