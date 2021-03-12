package cz.metacentrum.perun.dispatcher.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import cz.metacentrum.perun.dispatcher.exceptions.MessageFormatException;
import cz.metacentrum.perun.dispatcher.exceptions.PerunHornetQServerException;
import cz.metacentrum.perun.dispatcher.hornetq.PerunHornetQServer;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;

/**
 * Main class ensuring processing of JMS communication between Dispatcher and Engines.
 * It start/stop processing of messages, create queues and load processing rules for Engines.
 * Also provide method for message parsing.
 *
 * Queues to Engines are represented by EngineMessageProducer objects. Queue is used by TaskScheduler.
 * Queue from Engine is represented by EngineMessageConsumer. Received messages result in calls to SchedulingPool.
 *
 * @see cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer
 * @see cz.metacentrum.perun.dispatcher.jms.EngineMessageConsumer
 * @see cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler
 * @see cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "engineMessageProcessor")
public class EngineMessageProcessor {

	private final static Logger log = LoggerFactory.getLogger(EngineMessageProcessor.class);

	private Properties dispatcherProperties;
	private PerunHornetQServer perunHornetQServer;
	private TaskExecutor taskExecutor;
	private EngineMessageConsumer engineMessageConsumer;
	private SchedulingPool schedulingPool;
	private EngineMessageProducerFactory engineMessageProducerFactory;

	private Session session = null;
	private boolean processingMessages = false;
	private boolean systemQueueInitiated = false;
	private ConnectionFactory cf;
	private Connection connection;
	private BlockingDeque<TextMessage> outputMessages = null;
	private boolean restartHornetQServer = false;


	// ----- setters -------------------------------------


	public Properties getDispatcherProperties() {
		return dispatcherProperties;
	}

	@Resource(name="dispatcherPropertiesBean")
	public void setDispatcherProperties(Properties dispatcherProperties) {
		this.dispatcherProperties = dispatcherProperties;
	}

	public PerunHornetQServer getPerunHornetQServer() {
		return perunHornetQServer;
	}

	@Autowired
	public void setPerunHornetQServer(PerunHornetQServer perunHornetQServer) {
		this.perunHornetQServer = perunHornetQServer;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	@Autowired
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public EngineMessageConsumer getEngineMessageConsumer() {
		return engineMessageConsumer;
	}

	@Autowired
	public void setEngineMessageConsumer(EngineMessageConsumer engineMessageConsumer) {
		this.engineMessageConsumer = engineMessageConsumer;
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	@Autowired
	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	public EngineMessageProducerFactory getEngineMessageProducerFactory() {
		return engineMessageProducerFactory;
	}

	@Autowired
	public void setEngineMessageProducer(EngineMessageProducerFactory engineMessageProducerFactory) {
		this.engineMessageProducerFactory = engineMessageProducerFactory;
	}


	// ----- methods -------------------------------------


	/**
	 * Setup JMS queues between dispatcher and engines and start processing available messages.
	 * HornetQ server must be running already.
	 *
	 * @see cz.metacentrum.perun.dispatcher.hornetq.PerunHornetQServer
	 */
	public void startProcessingSystemMessages() {

		if(outputMessages == null) {
			outputMessages = new LinkedBlockingDeque<TextMessage>();
		}
		
		connection = null;
		try {
			if(restartHornetQServer) {
				engineMessageProducerFactory.removeProducer();
				perunHornetQServer.stopServer();
				perunHornetQServer.startServer();
			}
			
			// Step 2. Instantiate the TransportConfiguration object which
			// contains the knowledge of what transport to use,
			// The server port etc.
			if (log.isDebugEnabled()) {
				log.debug("Creating transport configuration...");
				log.debug("Gonna connect to the host["
						+ dispatcherProperties.getProperty("dispatcher.ip.address")
						+ "] on port["
						+ dispatcherProperties.getProperty("dispatcher.port")
						+ "]...");
			}
			Map<String, Object> connectionParams = new HashMap<String, Object>();
			try {
				connectionParams.put(TransportConstants.PORT_PROP_NAME, Integer.parseInt(dispatcherProperties.getProperty("dispatcher.port")));
			} catch (NumberFormatException e) {
				log.error("Could not parse value of dispatcher.port property. Trying without...");
			}
			connectionParams.put(TransportConstants.HOST_PROP_NAME, dispatcherProperties.getProperty("dispatcher.ip.address"));

			TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams);

			// Step 3 Directly instantiate the JMS ConnectionFactory object
			// using that TransportConfiguration
			log.debug("Creating connection factory...");
			cf = (ConnectionFactory) HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
			((HornetQConnectionFactory)cf).setUseGlobalPools(false);

			// Step 4.Create a JMS Connection
			log.debug("Creating connection...");
			connection = cf.createConnection();

			// Step 5. Create a JMS Session
			log.debug("Creating session...");
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Step 10. Start the Connection
			log.debug("Starting connection...");
			connection.start();
			if (processingMessages) {
				// make sure processing is stopped before new start when called as "restart"
				engineMessageConsumer.stop();
			}
			engineMessageConsumer.setUp("systemQueue", session);
			log.debug("Receiving messages started...");
			taskExecutor.execute(engineMessageConsumer);
			log.debug("JMS Initialization done.");
			processingMessages = true;

		} catch (JMSException e) {
			// If unable to connect to the server...
			log.error("Connection failed. \nThis is weird...are you sure that the Perun-Dispatcher is running on host ["
							+ dispatcherProperties.getProperty("dispatcher.ip.address")
							+ "] on port [" + dispatcherProperties.getProperty("dispatcher.port")
							+ "] ? \nSee: perun-dispatcher.properties. We gonna wait 5 sec and try again...", e);
			restartHornetQServer = true;
			throw new RuntimeException(e);
		} catch (Exception e) {
			log.error("Can't start processing of JMS: {}", e);
		}
	}

	/**
	 * Stop processing of JMS messages between dispatcher and engines and close the connection.
	 */
	public void stopProcessingSystemMessages() {
		if (processingMessages && engineMessageConsumer != null) {
			engineMessageConsumer.stop();
			engineMessageProducerFactory.removeProducer();
			try {
				connection.stop();
				session.close();
				connection.close();
				((HornetQConnectionFactory)cf).close();
				log.debug("JMS processing stopped.");
			} catch (JMSException e) {
				log.error("Error closing JMS client connection: ", e.toString());
			}
		}
	}

	/**
	 * TRUE if processing of JMS messages running
	 *
	 * @return TRUE processing of JMS is running / FALSE otherwise
	 */
	public boolean isProcessingMessages() {
		return processingMessages;
	}

	/**
	 * TRUE if JMS queue between dispatcher and engine is initialized.
	 *
	 * @return TRUE if JMS queue is initialized / FALSE otherwise
	 */
	public boolean isSystemQueueInitiated() {
		return systemQueueInitiated;
	}

	/**
	 * Process content of JMS message received from Engine. This is called by SystemQueueReceiver
	 * for each message.
	 *
	 * Expected message format is:
	 *
	 * Register engine message
	 * register
	 *
	 * Good bye engine message
	 * goodbye
	 *
	 * Task status change message
	 * task:y:status:timestamp
	 * y is an Integer that represents task ID
	 * status is string representation of task status
	 * timestamp is a string representation of timestamp (long)
	 *
	 * Task result message
	 * taskresult:object
	 * object is serialized TaskResult object sent from Engine
	 *
	 * @see EngineMessageConsumer
	 *
	 * @param message Message to be parsed a processed
	 * @throws PerunHornetQServerException When HornetQ server is not running
	 * @throws MessageFormatException When Engine sent malformed JMS message
	 */
	protected void processEngineMessage(String message) throws PerunHornetQServerException, MessageFormatException {

		if (perunHornetQServer.isServerRunning() && perunHornetQServer.getJMSServerManager() != null) {

			log.debug("Processing JMS message: " + message);

			if (null == message || message.isEmpty()) {
				throw new MessageFormatException("Engine sent empty message");
			}

			String[] clientMessageSplitter = message.split(":", 2);

			// process expected messages
			EngineMessageProducer engineMessageProducer;
			
			if (clientMessageSplitter[0].equalsIgnoreCase("register")) {

				// Do we have this queue already?
				engineMessageProducer = engineMessageProducerFactory.getProducer();
				
				if (engineMessageProducer != null) {
					// ...and close all tasks that could have been running there
					schedulingPool.closeTasksForEngine();
				} else {
					// No, we have to create the whole JMS queue and load matching rules...
					createDispatcherQueueForClient();
				}

			} else if (clientMessageSplitter[0].equalsIgnoreCase("goodbye")) {

				// engine is going down, should mark all tasks as failed
				schedulingPool.closeTasksForEngine();
				engineMessageProducerFactory.removeProducer();

			} else if (clientMessageSplitter[0].equalsIgnoreCase("task")) {

				clientMessageSplitter = message.split(":", 4);

				if(clientMessageSplitter.length < 4) {
					throw new MessageFormatException("Engine sent a malformed message, not enough params [" + message + "]");
				}

				try {
					schedulingPool.onTaskStatusChange(
							Integer.parseInt(clientMessageSplitter[1]),
							clientMessageSplitter[2],
							clientMessageSplitter[3]);
				} catch(NumberFormatException e) {
					throw new MessageFormatException("Engine sent a malformed message, could not parse client ID", e);
				}

			} else if (clientMessageSplitter[0].equalsIgnoreCase("taskresult")) {

				if(clientMessageSplitter.length < 2) {
					throw new MessageFormatException("Engine sent a malformed message, not enough params [" + message + "]");
				}

				schedulingPool.onTaskDestinationComplete(clientMessageSplitter[1]);

			} else {
				throw new MessageFormatException("Engine sent a malformed message, unknown type of message [" + message + "]");
			}

		} else {
			throw new PerunHornetQServerException("HornetQ server is not running or JMSServerManager is fucked up...");
		}
	}

	/**
	 * Create JMS queue for Engine.
	 *
	 */
	private void createDispatcherQueueForClient() {

		String queueName = "queue";

		try {
			perunHornetQServer.getJMSServerManager().createQueue(false, queueName, null, false);
		} catch (Exception e) {
			log.error("Can't create JMS {}: {}", queueName, e);
		}

		engineMessageProducerFactory.createProducer(queueName, session, outputMessages);
	}

}
