package cz.metacentrum.perun.engine.jms;

import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.hornetq.api.jms.HornetQJMSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

import cz.metacentrum.perun.engine.exceptions.UnknownMessageTypeException;
import cz.metacentrum.perun.engine.processing.CommandProcessor;
import cz.metacentrum.perun.engine.processing.EventProcessor;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "eventReceiver")
public class MessageReceiver implements Runnable {
	private final static Logger log = LoggerFactory
			.getLogger(MessageReceiver.class);

	private final static int TOO_LONG = 15000;

	private MessageConsumer messageConsumer = null;
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

	public MessageReceiver() {
	}

	public void setUp(String queueName, Session session) {
		this.queueName = queueName;
		this.session = session;
	}

	@Override
	public void run() {
		while (running) {
			if (!queueAcquired) {
				try {
					// Step 1. Directly instantiate the JMS Queue object.
					queue = HornetQJMSClient.createQueue(queueName);
					// Step 9. Create a JMS Message Consumer
					messageConsumer = session.createConsumer(queue);
					queueAcquired = true;
					// messageConsumer.receive(timeout) is a blocking operation!
					waitTime = 0;
				} catch (InvalidDestinationException e) {
					queueAcquired = false;
					waitTime = waitTime + 5000;
					log.error("Queue doesn't exist yet. We gonna wait a bit ("
							+ (waitTime / 1000) + "s) and try it again...", e);
				} catch (JMSException e) {
					queueAcquired = false;
					waitTime = waitTime + 5000;
					log.error(
							"Something went wrong with JMS. We gonna wait a bit ("
									+ (waitTime / 1000)
									+ "s) and try it again...", e);
				} catch (Exception e) {
					queueAcquired = false;
					waitTime = waitTime + 5000;
					log.error("Can not continue. We gonna wait a bit ("
							+ (waitTime / 1000) + "s) and try it again...", e);
				}
			} else {
				// Step 11. Receive the message
				TextMessage messageReceived = null;
				try {
					messageReceived = (TextMessage) messageConsumer
							.receive(timeout);
					if (messageReceived != null) {
						final String message = messageReceived.getText();

						String messageType = message.split("\\|", 2)[0].trim();
						log.debug("RECEIVED MESSAGE:" + message + ", Type:"
								+ messageType);

						if (messageType.equalsIgnoreCase("task")) {
							try {
								taskExecutorMessageProcess
										.execute(new Runnable() {
											@Override
											public void run() {
												// TODO: Remove in future
												log.info("I am going to call eventProcessor.receiveEvent(\""
														+ message
														+ "\") in thread:"
														+ Thread.currentThread()
																.getName());
												eventProcessor
														.receiveEvent(message);
											}
										});
							} catch (TaskRejectedException ex) {
								log.error("Task was rejected. Message {}",
										message);
								throw ex;
							}
						} else if (messageType.equalsIgnoreCase("command")) {
							// TODO: There is no need to put commandProcessor to
							// a separate thread at the moment, however it is
							// very likely to be so in a future.
							commandProcessor.receiveCommand(message);
						} else {
							throw new UnknownMessageTypeException(
									"UNKNOWN TYPE[" + messageType + "]");
						}

					}
				} catch (InvalidDestinationException e) {
					queueAcquired = false;
					waitTime = waitTime + 5000;
					log.error(
							"Queue doesn't exist or the connection is broken. We gonna wait a bit ("
									+ (waitTime / 1000)
									+ "s) and try it again...", e);
				} catch (JMSException e) {
					queueAcquired = false;
					waitTime = waitTime + 5000;
					log.error(
							"Something went wrong with JMS. We gonna wait a bit ("
									+ (waitTime / 1000)
									+ "s) and try it again...", e);
				} catch (Exception e) {
					queueAcquired = false;
					waitTime = waitTime + 5000;
					log.error("Can not continue. We gonna wait a bit ("
							+ (waitTime / 1000) + "s) and try it again...", e);
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
					log.error(e.toString(), e);
				}
			}
		}
	}

	public void stop() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public void setCommandProcessor(CommandProcessor commandProcessor) {
		this.commandProcessor = commandProcessor;
	}

	public CommandProcessor getCommandProcessor() {
		return commandProcessor;
	}

	public void setEventProcessor(EventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}

	public EventProcessor getEventProcessor() {
		return eventProcessor;
	}

	public TaskExecutor getTaskExecutorMessageProcess() {
		return taskExecutorMessageProcess;
	}

	public void setTaskExecutorMessageProcess(
			TaskExecutor taskExecutorMessageProcess) {
		this.taskExecutorMessageProcess = taskExecutorMessageProcess;
	}

}
