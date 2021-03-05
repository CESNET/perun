package cz.metacentrum.perun.dispatcher.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import cz.metacentrum.perun.dispatcher.exceptions.MessageFormatException;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import org.hornetq.api.jms.HornetQJMSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Thread used to actually read JMS messages from Engine.
 * Received messages are then parsed by SystemQueueProcessor.
 * If parsing fails, it tries to restart whole JMS processing.
 *
 * @see EngineMessageProcessor
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "engineMessageConsumer")
public class EngineMessageConsumer extends AbstractRunner {

	private final static Logger log = LoggerFactory.getLogger(EngineMessageConsumer.class);

	private final static int timeout = 5000; // ms

	private EngineMessageProcessor engineMessageProcessor;
	private MessageConsumer messageConsumer = null;
	private Session session = null;
	private String queueName = null;
	@Autowired
	private EngineMessageProducerFactory producerFactory;
	private EngineMessageProducer producer = null;

	public EngineMessageConsumer() {
	}

	// ----- setters -------------------------------------


	public EngineMessageProcessor getEngineMessageProcessor() {
		return engineMessageProcessor;
	}

	@Autowired
	public void setEngineMessageProcessor(EngineMessageProcessor engineMessageProcessor) {
		this.engineMessageProcessor = engineMessageProcessor;
	}

	public EngineMessageProducerFactory getProducerFactory() {
		return producerFactory;
	}

	public void setProducerFactory(EngineMessageProducerFactory producerFactory) {
		this.producerFactory = producerFactory;
	}


	// ----- methods -------------------------------------


	/**
	 * Set QueueName and HornetQ session in order to create correct message consumer.
	 *
	 * @param queueName Name of the JMS queue
	 * @param session HornetQ session
	 */
	public void setUp(String queueName, Session session) {
		this.queueName = queueName;
		this.session = session;
	}

	/**
	 * Create JMS message consumer for a queue and pass message content to EngineMessageProcessor.
	 *
	 * @see EngineMessageProcessor
	 */
	@Override
	public void run() {

		log.debug("SystemQueueReceiver has started...");
		try {

			// Step 1. Directly instantiate the JMS Queue object.
			log.debug("Creating queue...");
			Queue queue = HornetQJMSClient.createQueue(queueName);

			// Step 9. Create a JMS Message Consumer
			log.debug("Creating consumer...");
			messageConsumer = session.createConsumer(queue);

		} catch (JMSException e) {
			log.error(e.toString(), e);
		} catch (Exception e) {
			log.error(e.toString(), e);
		}

		while (!shouldStop()) {

			producer = producerFactory.getProducer();
			
			// Step 11. Deliver output and try to receive the message
			TextMessage messageReceived = null;
			try {
				if(producer != null) {
					producer.deliverOutputMessages();
				}

				log.debug("Gonna call messageConsumer.receive(timeout)...");
				messageReceived = (TextMessage) messageConsumer.receive(timeout);
				if (messageReceived != null) {
					if (log.isDebugEnabled()) {
						log.debug("System message received [" + messageReceived.getText() + "]");
					}
					try {
						engineMessageProcessor.processEngineMessage(messageReceived.getText());
					} catch (MessageFormatException ex) {
						// engine sent wrongly formatted messages
						// shouldn't kill whole messaging process
						log.error(ex.toString(), ex);
					}
					messageReceived.acknowledge();
				} else {
					if (log.isDebugEnabled()) {
						log.debug("No message available...");
					}
				}
			} catch (JMSException e) {
				// try to restart JMS messaging
				log.error(e.toString(), e);
				// NOTE: this will call stop() on us
				engineMessageProcessor.stopProcessingSystemMessages();
				engineMessageProcessor.startProcessingSystemMessages();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ex) {
					log.error(ex.toString(), ex);
					stop();
				}
			} catch (Exception e) {
				log.error(e.toString(), e);
				stop();
			}
		}
		try {
			messageConsumer.close();
		} catch (JMSException e) {
			log.error(e.toString(), e);
		}
		messageConsumer = null;
	}

}
