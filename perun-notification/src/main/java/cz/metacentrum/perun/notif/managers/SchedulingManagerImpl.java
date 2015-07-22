package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.AuditerConsumer;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifPoolMessage;
import cz.metacentrum.perun.notif.utils.NotifUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Scheduling manager for firing sending emails from db to users. Main method is
 * doNotification.
 *
 * @author tomas.tunkl
 *
 */
@Service("schedulingManager")
public class SchedulingManagerImpl {

	private static final Logger logger = LoggerFactory.getLogger(SchedulingManagerImpl.class);
	private PerunSession session;

	private static volatile AtomicBoolean running = new AtomicBoolean(false);
	private static volatile AtomicBoolean runningAllowed = new AtomicBoolean(true);

	@Autowired
	private Properties propertiesBean;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private PerunNotifPoolMessageManager perunNotifPoolMessageManager;

	@Autowired
	private PerunNotifAuditMessageManager perunNotifAuditMessagesManager;

	@Autowired
	private PerunNotifRegexManager perunNotifRegexManager;

	@Autowired
	private PerunNotifTemplateManager perunNotifTemplateManager;

	@Autowired
	private PerunBl perun;

	private final String consumerName = "notifications";

	@PostConstruct
	public void init() throws InternalErrorException {
		session = NotifUtils.getPerunSession(perun);
	}

	/**
	 * Method starts processing poolMessages from db and starts sending
	 * notifications to users.
	 */
	public void doNotification() {

		if (!(runningAllowed.get())) {
			return;
		}

		if (!(running.compareAndSet(false, true))) {
			logger.warn("DoNotification is still running.");
			return;
		}

		logger.info("Starting doNotification");

		try {
			logger.debug("1: Processing perun AuditMessages");
			processPerunAuditMessages();
			logger.debug("2: Processing perunNotifAuditMessages");
			processPerunNotifAuditMessages();
			logger.info("3: Getting poolMessages from db.");
			perunNotifPoolMessageManager.processPerunNotifPoolMessagesFromDb();
		} catch (Exception ex) {
			stopNotifications(ex);
		} finally {
			running.set(false);

		}
	}

	/**
	 * Loads notif audit messages from db restart their processing.
	 * Call processing of one perunAuditMessage for each gotten msg.
	 *
	 * @throws InternalErrorException
	 */
	private void processPerunNotifAuditMessages() throws Exception {

		List<PerunNotifAuditMessage> oldAuditMessages = null;
		try {
			oldAuditMessages = perunNotifAuditMessagesManager.getAll();
		} catch (Exception ex) {
			logger.error("Error during getting all old messages.");
			throw ex;
		}
		if (oldAuditMessages != null && !oldAuditMessages.isEmpty()) {
			for (PerunNotifAuditMessage perunAuditMessage : oldAuditMessages) {
				processPerunNotifAuditMessage(perunAuditMessage, session);
			}
		}
	}

	/**
	 * The method loads perun audit messages from the database and saves them as PerunNotifAudiMessages.
	 */
	public void processPerunAuditMessages() throws Exception {
		List<PerunNotifAuditMessage> perunNotifAuditMessages = new ArrayList<PerunNotifAuditMessage>();
		try {
			List<AuditMessage> messages = perun.getAuditMessagesManagerBl().pollConsumerMessagesForParser(consumerName);

			for (AuditMessage message : messages) {
				try {
					PerunNotifAuditMessage perunNotifAuditMessage = perunNotifAuditMessagesManager.saveMessageToPerunAuditerMessage(message.getMsg(), session);
					perunNotifAuditMessages.add(perunNotifAuditMessage);
				} catch (InternalErrorException ex) {
					logger.error("Error during saving message to db. Message: " + message.getMsg());
					throw ex;
				}
			}
		} catch (Exception ex) {
			logger.error("Error during perunNotification process.");
			throw ex;
		}
	}

	/**
	 * Handles processing of auditer message and in case of success removes
	 * auditer message from db. To accomplish a success, the message have to be well-formed, so that the
	 * object can be parsed, there have to be matching notifRegex in the db. If the message is recognized and
	 * the matching regex is assigned to the template,  PerunNotifPoolMessage is created.
	 *
	 * @param perunAuditMessage
	 * @param session
	 * @throws InternalErrorException
	 */
	private void processPerunNotifAuditMessage(PerunNotifAuditMessage perunAuditMessage, PerunSession session) throws Exception {

		try {
			logger.debug("Getting regexIds, matching received message with id: " + perunAuditMessage.getId());
			Set<Integer> regexIds = perunNotifRegexManager.getIdsOfRegexesMatchingMessage(perunAuditMessage);
			logger.debug("Received regexIds for message with id: " + perunAuditMessage.getId() + "; regexIds = " + regexIds + "; now getting templateIds.");
			if (regexIds == null || regexIds.isEmpty()) {
				logger.info("Message is not recognized, will be deleted: " + perunAuditMessage.getMessage());
				perunNotifAuditMessagesManager.removePerunAuditerMessageById(perunAuditMessage.getId());
				return;
			}
			List<PerunNotifPoolMessage> perunNotifPoolMessages = null;
			try {
				perunNotifPoolMessages = perunNotifTemplateManager.getPerunNotifPoolMessagesForRegexIds(regexIds, perunAuditMessage, session);
			} catch (InternalErrorException ex) {
				logger.error("Error during processPerunNotifAuditMessage.");
				throw ex;
			}

			if (perunNotifPoolMessages != null && !perunNotifPoolMessages.isEmpty()) {
				try {
					perunNotifPoolMessageManager.savePerunNotifPoolMessages(perunNotifPoolMessages);
				} catch (InternalErrorException ex) {
					logger.error("Error during saving pool message.");
					throw ex;
				}
			} else {
				logger.warn("No pool messages recognized for message: " + perunAuditMessage.getMessage());
			}

			logger.info("Removing saved perunMessage with id=" + perunAuditMessage.getId());
			perunNotifAuditMessagesManager.removePerunAuditerMessageById(perunAuditMessage.getId());
		} catch (Exception ex) {
			logger.error("Error during process of perun notif audit message: " + perunAuditMessage.getId());
			throw ex;
		}
	}

	public void processOneAuditerMessage(String message) throws Exception {

		PerunNotifAuditMessage perunNotifAuditMessage = null;
		try {
			perunNotifAuditMessage = perunNotifAuditMessagesManager.saveMessageToPerunAuditerMessage(message, session);
		} catch (InternalErrorException ex) {
			logger.error("Error during saving one time auditer message: " + message);
		}

		processPerunNotifAuditMessage(perunNotifAuditMessage, session);
	}

	public void stopNotifications() {
		stopNotifications(null);
	}

	public void stopNotifications(Exception ex) {
		runningAllowed.set(false);
		if (ex == null) {
			logger.info("Notifications was stopped.");
		} else {
			logger.error("Notifications was stopped due to exception: ", ex);
		}
	}

	public void startNotifications() {
		runningAllowed.set(true);
		logger.info("Notifications was started.");
	}

	public boolean isNotificationsRunning() {
		return runningAllowed.get();
	}
}
