package cz.metacentrum.perun.engine.jms;

import cz.metacentrum.perun.engine.exceptions.UnknownMessageTypeException;
import cz.metacentrum.perun.engine.processing.CommandProcessor;
import cz.metacentrum.perun.engine.processing.EventProcessor;
import jakarta.jms.DeliveryMode;
import jakarta.jms.InvalidDestinationException;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

/**
 * @author Michal Karm Babacek JavaDoc coming soon...
 */
@org.springframework.stereotype.Service(value = "eventReceiver")
public class MessageReceiver implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(MessageReceiver.class);

  private static final int TOO_LONG = 15000;

  private MessageConsumer messageConsumer = null;
  private MessageProducer messageProducer = null;

  private Queue queue = null;
  private boolean running = true;
  // time out for message consumer
  private int timeout = 5000; // ms
  // messageConsumer.receive(timeout) is a blocking operation!
  private int waitTime = 0; // ms
  private String queueName = null;
  private Session session = null;
  private boolean queueAcquired = false;
  @Autowired
  private CommandProcessor commandProcessor;
  @Autowired
  private EventProcessor eventProcessor;
  @Autowired
  private TaskExecutor taskExecutorMessageProcess;
  private BlockingDeque<TextMessage> inputMessages;

  public MessageReceiver() {
  }

  public CommandProcessor getCommandProcessor() {
    return commandProcessor;
  }

  public EventProcessor getEventProcessor() {
    return eventProcessor;
  }

  public TaskExecutor getTaskExecutorMessageProcess() {
    return taskExecutorMessageProcess;
  }

  public boolean isRunning() {
    return running;
  }

  @Override
  public void run() {
    while (running) {
      if (!queueAcquired) {
        try {
          LOG.debug("Creating new JMS queue " + queueName);
          // Step 1. Directly instantiate the JMS Queue object.
          queue = session.createQueue(queueName);
          // Step 9. Create a JMS Message Consumer
          LOG.debug("Creating JMS consumer");
          messageConsumer = session.createConsumer(queue);
          queueAcquired = true;
          LOG.debug("Ready to receive messages.");
          // messageConsumer.receive(timeout) is a blocking operation!
          waitTime = 0;
        } catch (InvalidDestinationException e) {
          queueAcquired = false;
          waitTime = setWaitTime(waitTime);
          LOG.error("Queue doesn't exist yet. We gonna wait a bit ({} s) and try it again.", (waitTime / 1000), e);
          // wait for a time mentioned in the error message before try it again
          try {
            Thread.sleep(waitTime);
          } catch (InterruptedException interrupted) {
            LOG.error(interrupted.toString(), interrupted);
          }
        } catch (JMSException e) {
          queueAcquired = false;
          waitTime = setWaitTime(waitTime);
          LOG.error("Something went wrong with JMS. We are gonna wait a bit ({} s) and try it again...",
              (waitTime / 1000), e);
        } catch (Exception e) {
          queueAcquired = false;
          waitTime = setWaitTime(waitTime);
          LOG.error("Can not continue. We gonna wait a bit ({} s) and try it again...", (waitTime / 1000), e);
        }
      } else {

        // Try to send queued messages first
        while (!inputMessages.isEmpty()) {
          TextMessage message = inputMessages.remove();
          try {
            messageProducer.send(message, DeliveryMode.PERSISTENT, message.getIntProperty("priority"), 0);
            LOG.trace("Message {} for dispatcher sent.\n", message.getText());
          } catch (JMSException e) {
            queueAcquired = false;
            LOG.error("Something went wrong with JMS. We are gonna restart and try it again...", e);
            // goes back to reinitialize the connection
            return;
          }
        }

        // Step 11. Receive the message
        TextMessage messageReceived = null;
        try {
          messageReceived = (TextMessage) messageConsumer.receive(timeout);
          if (messageReceived != null) {
            final String message = messageReceived.getText();

            String messageType = message.split("\\|", 2)[0].trim();
            LOG.debug("RECEIVED MESSAGE:{}, Type:{}", message, messageType);

            if (messageType.equalsIgnoreCase("task")) {
              try {
                taskExecutorMessageProcess.execute(new Runnable() {
                  @Override
                  public void run() {
                    // TODO: Remove in future
                    LOG.trace("I am going to call eventProcessor.receiveEvent(\"{}\") " + "in thread: {}", message,
                        Thread.currentThread().getName());
                    eventProcessor.receiveEvent(message);
                  }
                });
              } catch (TaskRejectedException ex) {
                LOG.error("Task was rejected. Message {}", message);
                throw ex;
              }
            } else if (messageType.equalsIgnoreCase("command")) {
              // TODO: There is no need to put commandProcessor to
              // a separate thread at the moment, however it is
              // very likely to be so in a future.
              commandProcessor.receiveCommand(message);
            } else {
              throw new UnknownMessageTypeException("UNKNOWN TYPE[" + messageType + "]");
            }

          }
          waitTime = 0;
        } catch (InvalidDestinationException e) {
          queueAcquired = false;
          waitTime = setWaitTime(waitTime);
          LOG.error("Queue doesn't exist or the connection is broken. We gonna wait a bit (" + (waitTime / 1000) +
                    "s) and try it again...", e);
        } catch (JMSException e) {
          queueAcquired = false;
          waitTime = setWaitTime(waitTime);
          LOG.error(
              "Something went wrong with JMS. We gonna wait a bit (" + (waitTime / 1000) + "s) and try it again...", e);
        } catch (Exception e) {
          queueAcquired = false;
          waitTime = setWaitTime(waitTime);
          LOG.error("Can not continue. We gonna wait a bit (" + (waitTime / 1000) + "s) and try it again...", e);
        }
      }
      if (waitTime > 0) {
        if (waitTime > TOO_LONG) {
          // gonna be back after trying to reinitialize the connection
          return;
        }
        try {
          Thread.sleep(waitTime);
        } catch (InterruptedException e) {
          LOG.error(e.toString(), e);
        }
      }
    }
  }

  public void sendMessage(TextMessage msg) throws InterruptedException {
    inputMessages.put(msg);
  }

  public void setCommandProcessor(CommandProcessor commandProcessor) {
    this.commandProcessor = commandProcessor;
  }

  public void setEventProcessor(EventProcessor eventProcessor) {
    this.eventProcessor = eventProcessor;
  }

  public void setTaskExecutorMessageProcess(TaskExecutor taskExecutorMessageProcess) {
    this.taskExecutorMessageProcess = taskExecutorMessageProcess;
  }

  public void setUp(String queueName, Session session, MessageProducer producer) {
    this.queueName = queueName;
    this.session = session;
    this.messageProducer = producer;
    inputMessages = new LinkedBlockingDeque<TextMessage>();
  }

  /**
   * Returns incremented wait time, but max limit is 10 minutes
   *
   * @param waitTime previous wait time
   * @return new wait time
   */
  private int setWaitTime(int waitTime) {
    waitTime = Math.min((waitTime + 5000), 600000);
    return waitTime;
  }

  public void stop() {
    running = false;
  }

}
