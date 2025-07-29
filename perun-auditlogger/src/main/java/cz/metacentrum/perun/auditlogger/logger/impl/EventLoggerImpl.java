package cz.metacentrum.perun.auditlogger.logger.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.metacentrum.perun.auditlogger.logger.EventLogger;
import cz.metacentrum.perun.auditlogger.service.AuditLoggerManager;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class containing the core logic of AuditLogger. It retrieves the last processed event ID from the configured file,
 * periodically polls Perun core for AuditMessages which it logs using logback into the configured journal file and
 * handles errors/interruptions by writing the id of the last processed message into the configured state file.
 *
 * As the name suggests, AuditLogger output is mainly used for auditing and potential third party handling.
 * Similar logic is used in the LDAP connector to poll for events and propagate updates to LDAP.
 */
@org.springframework.stereotype.Service(value = "eventLogger")
public class EventLoggerImpl implements EventLogger, Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(EventLoggerImpl.class);

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  private static final String AUDIT_LOGGER_NAME = "journal-audit";

  private static final Logger JOURNAL = LoggerFactory.getLogger(AUDIT_LOGGER_NAME);

  private static final Map<Class<?>, Class<?>> MIXIN_MAP = new HashMap<>();
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    JavaTimeModule module = new JavaTimeModule();
    MAPPER.registerModule(module);
    // make mapper to serialize dates and timestamps like "YYYY-MM-DD" or "YYYY-MM-DDTHH:mm:ss.SSSSSS"
    MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    MAPPER.enableDefaultTyping();
    // TODO - skip any problematic properties using interfaces for mixins
    MAPPER.setMixIns(MIXIN_MAP);
  }

  @Autowired
  private AuditLoggerManager auditLoggerManager;

  private PerunSession perunSession;
  private Perun perun;
  private int lastProcessedIdNumber;

  private boolean running = false;

  public int getLastProcessedIdNumber() {
    return lastProcessedIdNumber;
  }

  protected void loadLastProcessedId() {
    Path file = FileSystems.getDefault().getPath(auditLoggerManager.getStateFile());
    if (!file.toFile().canRead()) {
      LOG.warn("Could not read state from " + file.toString() + ", defaulting to end of current auditLOG.");
      this.lastProcessedIdNumber = perun.getAuditMessagesManager().getLastMessageId(perunSession);
    } else {
      try {
        List<String> idString = Files.readAllLines(file, Charset.defaultCharset());
        int lastId = idString.isEmpty() ? 0 : Integer.parseInt(idString.get(0));
        if (lastId >= 0) {
          this.lastProcessedIdNumber = lastId;
          LOG.info("Loaded last processed message id={}", lastId);
        } else {
          LOG.error("Wrong number for last processed message id {}, exiting.", idString);
          System.exit(-1);
        }
      } catch (IOException exception) {
        LOG.error("Error reading last processed message id from {}", auditLoggerManager.getStateFile(), exception);
        System.exit(-1);
      }
    }
  }

  @Override
  public int logMessage(AuditMessage message) {
    try {
      JOURNAL.info(MAPPER.writeValueAsString(message));
    } catch (JsonProcessingException e) {
      LOG.error("Unable to write audit message: {}", message, e);
      return -1;
    }
    return 0;
  }

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

        // Waiting for new messages. If consumer failed in some internal case, waiting until it will be repaired
        // (waiting time is increases by each attempt)
        while (messages == null || messages.isEmpty()) {
          try {
            //IMPORTANT STEP1: Get new bulk of messages
            LOG.debug("Waiting for audit messages.");
            messages = ((PerunBl) perun).getAuditMessagesManagerBl()
                .pollConsumerMessages(perunSession, auditLoggerManager.getConsumerName(), lastProcessedIdNumber);
            if (messages.size() > 0) {
              LOG.debug("Read {} new audit messages starting from {}", messages.size(), lastProcessedIdNumber);
            }
          } catch (InternalErrorException ex) {
            LOG.error("Consumer failed due to {}. Sleeping for {} ms.", ex, sleepTime);
            Thread.sleep(sleepTime);
            sleepTime += sleepTime;
          }

          //If there are no messages, sleep for 5 sec and then try it again
          if (messages == null || messages.isEmpty()) {
            Thread.sleep(5000);
          }
        }
        //If new messages exist, resolve them all
        Iterator<AuditMessage> messagesIterator = messages.iterator();
        LOG.debug("Trying to send {} messages", messages.size());
        while (messagesIterator.hasNext()) {
          message = messagesIterator.next();
          //Warning when two consecutive messages are separated by more than 15 ids
          if (lastProcessedIdNumber >= 0 && lastProcessedIdNumber < message.getId()) {
            if ((message.getId() - lastProcessedIdNumber) > 15) {
              LOG.debug("SKIP FLAG WARNING: lastProcessedIdNumber: {} - newMessageNumber: {} = {}",
                  lastProcessedIdNumber, message.getId(), (lastProcessedIdNumber - message.getId()));
            }
          }
          //IMPORTANT STEP2: send all messages to journal
          if (this.logMessage(message) == 0) {
            messagesIterator.remove();
            lastProcessedIdNumber = message.getId();
          } else {
            break;
          }
        }
        if (messages.isEmpty()) {
          LOG.debug("All messages sent.");
          messages = null;
        }
        // After all messages has been resolved, test interrupting of thread and if its ok, wait and go for another
        // bulk of messages
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
      LOG.error("Last message has ID='{}' and was INTERRUPTED at {} due to interrupting.",
          ((message != null) ? message.getId() : 0), DATE_FORMAT.format(date));
      running = false;
      Thread.currentThread().interrupt();
      //If some other exception is thrown
    } catch (Exception e) {
      Date date = new Date();
      LOG.error("Last message has ID='{}' and was bad PARSED or EXECUTE at {} due to exception {}",
          ((message != null) ? message.getId() : 0), DATE_FORMAT.format(date), e.toString());
      throw new RuntimeException(e);
    } finally {
      saveLastProcessedId();
    }
  }

  protected void saveLastProcessedId() {
    Path file = FileSystems.getDefault().getPath(auditLoggerManager.getStateFile());
    try {
      Files.write(file, String.valueOf(lastProcessedIdNumber).getBytes());
    } catch (IOException e) {
      LOG.error("Error writing last processed message id={} to file {}", lastProcessedIdNumber,
              auditLoggerManager.getStateFile(), e);
    }
  }

  public void setLastProcessedIdNumber(int lastProcessedIdNumber) {
    this.lastProcessedIdNumber = lastProcessedIdNumber;
  }
}
