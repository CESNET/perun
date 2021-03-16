package cz.metacentrum.perun.auditlogger.logger.impl;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.auditlogger.logger.EventLogger;
import cz.metacentrum.perun.auditlogger.service.AuditLoggerManager;
import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service(value = "eventLogger")
public class EventLoggerImpl implements EventLogger, Runnable {

	private final static Logger log = LoggerFactory.getLogger(EventLoggerImpl.class);

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static final String SYSLOG_LOGGER_NAME = "syslog-logger";
	
	private static final Logger syslog = LoggerFactory.getLogger(SYSLOG_LOGGER_NAME);
	
	private static final Map<Class<?>,Class<?>> mixinMap = new HashMap<>();
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.enableDefaultTyping();
		// TODO - skip any problematic properties using interfaces for mixins
		mapper.setMixIns(mixinMap);
	}

	@Autowired
	private AuditLoggerManager auditLoggerManager;

	private PerunSession perunSession;
	private Perun perun;
	private int lastProcessedIdNumber;

	private boolean running = false;

	@Override
	public void run() {


		running = true;
		AuditMessage message = null;
		List<AuditMessage> messages;

		try {
			perunSession = auditLoggerManager.getPerunSession();
			perun = auditLoggerManager.getPerunBl();
			
			if (lastProcessedIdNumber == 0) {
				loadLastProcessedId();
			}

			messages = null;

			//If running is true, then this process will be continuously
			while (running) {

				int sleepTime = 1000;
				//Waiting for new messages. If consumer failed in some internal case, waiting until it will be repaired (waiting time is increases by each attempt)
				while(messages == null || messages.isEmpty()) {
					try {
						//IMPORTANT STEP1: Get new bulk of messages
						log.debug("Waiting for audit messages.");
						messages = ((PerunBl)perun).getAuditMessagesManagerBl().pollConsumerMessages(perunSession, auditLoggerManager.getConsumerName(), lastProcessedIdNumber);
						if (messages.size() > 0) log.debug("Read {} new audit messages starting from {}", messages.size(), lastProcessedIdNumber);
					} catch (InternalErrorException ex) {
						log.error("Consumer failed due to {}. Sleeping for {} ms.", ex, sleepTime);
						Thread.sleep(sleepTime);
						sleepTime += sleepTime;
					}

					//If there are no messages, sleep for 5 sec and then try it again
					if (messages == null || messages.isEmpty()) Thread.sleep(5000);
				} 
				//If new messages exist, resolve them all
				Iterator<AuditMessage> messagesIterator = messages.iterator();
				log.debug("Trying to send {} messages", messages.size());
				while (messagesIterator.hasNext()) {
					message = messagesIterator.next();
					//Warning when two consecutive messages are separated by more than 15 ids
					if (lastProcessedIdNumber > 0 && lastProcessedIdNumber < message.getId()) {
						if ((message.getId() - lastProcessedIdNumber) > 15)
							log.debug("SKIP FLAG WARNING: lastProcessedIdNumber: {} - newMessageNumber: {} = {}",
									lastProcessedIdNumber, message.getId(), (lastProcessedIdNumber - message.getId()));
					}
					//IMPORTANT STEP2: send all messages to syslog
					if(this.logEvent(message.getEvent()) == 0) {
						messagesIterator.remove();
						lastProcessedIdNumber = message.getId();
					} else {
						break;
					}
				}
				if(messages.isEmpty()) {
					log.debug("All messages sent.");
					messages = null;
				}
				//After all messages has been resolved, test interrupting of thread and if its ok, wait and go for another bulk of messages
				if (Thread.interrupted()) {
					running = false;
				} else {
					saveLastProcessedId();
					Thread.sleep(5000);
				}
			}
			//If auditlogger is interrupted
		} catch (InterruptedException e) {
			Date date = new Date();
			log.error("Last message has ID='{}' and was INTERRUPTED at {} due to interrupting.", 
					((message != null) ? message.getId() : 0), DATE_FORMAT.format(date));
			running = false;
			Thread.currentThread().interrupt();
			//If some other exception is thrown
		} catch (Exception e) {
			Date date = new Date();
			log.error("Last message has ID='{}' and was bad PARSED or EXECUTE at {} due to exception {}",
					((message != null) ? message.getId() : 0), DATE_FORMAT.format(date), e.toString());
			throw new RuntimeException(e);
		} finally {
			saveLastProcessedId();
		}
	}

	@Override
	public int logEvent(AuditEvent event) {
		try {
			syslog.info(mapper.writeValueAsString(event));
		} catch (JsonProcessingException e) {
			return -1;
		}
		return 0;
	}


	public int getLastProcessedIdNumber() {
		return lastProcessedIdNumber;
	}

	public void setLastProcessedIdNumber(int lastProcessedIdNumber) {
		this.lastProcessedIdNumber = lastProcessedIdNumber;
	}

	protected void loadLastProcessedId() {
		Path file = FileSystems.getDefault().getPath(auditLoggerManager.getStateFile());
		if(!file.toFile().canRead()) {
			log.warn("Could not read state from " + file.toString() + ", defaulting to end of current audit log.");
			this.lastProcessedIdNumber = perun.getAuditMessagesManager().getLastMessageId(perunSession);
		} else {
			try {
				List<String> id_s = Files.readAllLines(file, Charset.defaultCharset());
				int lastId = id_s.isEmpty() ? 0 : Integer.parseInt(id_s.get(0));
				if (lastId > 0) {
					this.lastProcessedIdNumber = lastId;
				} else {
					log.error("Wrong number for last processed message id {}, exiting.", id_s);
					System.exit(-1);

				}
			} catch (IOException exception) {
				log.error("Error reading last processed message id from {}", auditLoggerManager.getStateFile(), exception);
				System.exit(-1);
			}
		}
	}

	protected void saveLastProcessedId() {
		Path file = FileSystems.getDefault().getPath(auditLoggerManager.getStateFile());
		try {
			Files.write(file, String.valueOf(lastProcessedIdNumber).getBytes());
		} catch (IOException e) {
			log.error("Error writing last processed message id to file {}", auditLoggerManager.getStateFile(), e);
		}
	}
}
