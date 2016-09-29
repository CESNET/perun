package cz.metacentrum.perun.dispatcher.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import cz.metacentrum.perun.dispatcher.exceptions.MessageFormatException;
import org.hornetq.api.jms.HornetQJMSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "systemQueueReceiver")
public class SystemQueueReceiver implements Runnable {

	private final static Logger log = LoggerFactory
			.getLogger(SystemQueueReceiver.class);

	@Autowired
	private SystemQueueProcessor systemQueueProcessor;
	private MessageConsumer messageConsumer = null;
	private Queue queue = null;
	private boolean running = true;
	private int timeout = 5000; // ms
	private int periodicity = 1000; // ms
	private Session session = null;
	private String queueName = null;

	public SystemQueueReceiver() {
	}

	public void setUp(String queueName, Session session) {
		this.queueName = queueName;
		this.session = session;
	}

	@Override
	public void run() {
		log.debug("SystemQueueReceiver has started...");
		try {

			// Step 1. Directly instantiate the JMS Queue object.
			log.debug("Creating queue...");
			queue = HornetQJMSClient.createQueue(queueName);

			// Step 9. Create a JMS Message Consumer
			log.debug("Creating consumer...");
			messageConsumer = session.createConsumer(queue);

		} catch (JMSException e) {
			log.error(e.toString(), e);
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		TextMessage messageReceived = null;
		while (running) {

			// Step 11. Receive the message
			messageReceived = null;
			try {
				log.debug("Gonna call messageConsumer.receive(timeout)...");
				messageReceived = (TextMessage) messageConsumer
						.receive(timeout);
				if (messageReceived != null) {
					if (log.isDebugEnabled()) {
						log.debug("System message received["
								+ messageReceived.getText() + "]");
					}
					try {
						systemQueueProcessor
								.processDispatcherQueueAndMatchingRule(messageReceived
										.getText());
					} catch (MessageFormatException ex) {
						// engine sent wrongly formatted messages
						// shouldn't kill whole messaging process
						log.error(ex.toString(), ex);
					}
					messageReceived.acknowledge();
				}
				if (log.isDebugEnabled()) {
					if (messageReceived == null) {
						log.debug("No message available...");
					}
				}
				Thread.sleep(periodicity);
			} catch (JMSException e) {
				log.error(e.toString(), e);
				systemQueueProcessor.stopProcessingSystemMessages();
				systemQueueProcessor.startProcessingSystemMessages();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ex) {
					log.error(ex.toString(), ex);
					stop();
				}
			} catch (InterruptedException e) {
				log.error(e.toString(), e);
				stop();
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

	public void stop() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}
}
