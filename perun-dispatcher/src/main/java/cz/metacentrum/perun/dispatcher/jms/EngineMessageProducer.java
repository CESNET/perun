package cz.metacentrum.perun.dispatcher.jms;

import java.util.concurrent.BlockingDeque;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.hornetq.api.jms.HornetQJMSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instance of Engine message queue producer for sending messages to Engine. For each Engine own producer (message
 * queue) is created, and stored in EngineMessageProducerPool.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see cz.metacentrum.perun.dispatcher.jms.EngineMessageProducerFactory
 */
public class EngineMessageProducer {

  private static final Logger LOG = LoggerFactory.getLogger(EngineMessageProducer.class);

  private Queue queue;
  private Session session;
  private MessageProducer producer;
  private String queueName;
  private BlockingDeque<TextMessage> outputMessages;

  // this one is to allow for mock objects which extend this class
  public EngineMessageProducer(String queueName) {
    this.queueName = queueName;
  }

  public EngineMessageProducer(String queueName, Session session, BlockingDeque<TextMessage> outputQueue) {
    this.queueName = queueName;
    this.session = session;
    this.outputMessages = outputQueue;
    try {
      // Step 1. Directly instantiate the JMS Queue object.
      this.queue = HornetQJMSClient.createQueue(this.queueName);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Created queue named as: " + this.queueName);
      }
      // Step 6. Create a JMS Message Producer
      this.producer = session.createProducer(this.queue);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Producer created: " + this.producer);
      }
    } catch (JMSException e) {
      LOG.error(e.toString(), e);
    } catch (Exception e) {
      LOG.error(e.toString(), e);
      // TODO: Restart connection...?
    }
  }

  /**
   * Try to deliver all pending messages.
   *
   * @throws JMSException
   */
  public void deliverOutputMessages() throws JMSException {
    while (!outputMessages.isEmpty()) {
      TextMessage message = outputMessages.poll();
      producer.send(message);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Sent message (queue name:" + queueName + "): " + message.getText());
      }
    }
  }

  /**
   * Get name of the queue for engine.
   *
   * @return Name of queue
   */
  public String getQueueName() {
    return queueName;
  }

  /**
   * Queue  JMS message to the Engine associated with this queue for delivery.
   *
   * @param text Message content
   */
  public void sendMessage(String text) {

    try {
      // Step 7. Create a Text Message
      TextMessage message = session.createTextMessage("task|" + text);
      // Step 8. Send...
      outputMessages.put(message);
    } catch (JMSException e) {
      LOG.error(e.toString(), e);
    } catch (Exception e) {
      LOG.error(e.toString(), e);
      // TODO: Restart connection...?
    }
  }

  /**
   * Shutdown before destroying the producer.
   */
  public void shutdown() {
    try {
      producer.close();
      // session is not ours to close
      // session.close();
    } catch (JMSException e) {
      LOG.error(e.toString(), e);
    }
  }
}
