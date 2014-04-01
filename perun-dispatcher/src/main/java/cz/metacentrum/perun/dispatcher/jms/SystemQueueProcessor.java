package cz.metacentrum.perun.dispatcher.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.hornetq.core.remoting.impl.netty.TransportConstants;

import cz.metacentrum.perun.dispatcher.exceptions.MessageFormatException;
import cz.metacentrum.perun.dispatcher.exceptions.PerunHornetQServerException;
import cz.metacentrum.perun.dispatcher.hornetq.PerunHornetQServer;
import cz.metacentrum.perun.dispatcher.processing.SmartMatcher;

/**
 *
 * @author Michal Karm Babacek JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "systemQueueProcessor")
public class SystemQueueProcessor {

	private final static Logger log = LoggerFactory.getLogger(SystemQueueProcessor.class);

	@Autowired
	private Properties propertiesBean;
	@Autowired
	private DispatcherQueuePool dispatcherQueuePool;
	@Autowired
	private PerunHornetQServer perunHornetQServer;
	@Autowired
	private SmartMatcher smartMatcher;
	@Autowired
	private TaskExecutor taskExecutor;
	private Session session = null;
	@Autowired
	private SystemQueueReceiver systemQueueReceiver;
	private boolean processingMessages = false;
	private boolean systemQueueInitiated = false;

	public void startProcessingSystemMessages() {
		Connection connection = null;
		try {
			// Step 2. Instantiate the TransportConfiguration object which
			// contains the knowledge of what transport to use,
			// The server port etc.
			log.debug("Creating transport configuration...");
			Map<String, Object> connectionParams = new HashMap<String, Object>();
			if (log.isDebugEnabled()) {
				log.debug("Gonna connect to the host[" + propertiesBean.getProperty("dispatcher.ip.address") + "] on port[" + propertiesBean.getProperty("dispatcher.port") + "]...");
			}
			connectionParams.put(TransportConstants.PORT_PROP_NAME, Integer.parseInt(propertiesBean.getProperty("dispatcher.port")));
			connectionParams.put(TransportConstants.HOST_PROP_NAME, propertiesBean.getProperty("dispatcher.ip.address"));
			TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams);

			// Step 3 Directly instantiate the JMS ConnectionFactory object
			// using that TransportConfiguration
			log.debug("Creating connection factory...");
			ConnectionFactory cf = (ConnectionFactory) HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);

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
				stopProcessingSystemMessages();
			}
			systemQueueReceiver.setUp("systemQueue", session);
			log.debug("Executor: taskExecutor.execute(systemQueueReceiver)...");
			taskExecutor.execute(systemQueueReceiver);
			log.debug("Initialization done.");
			processingMessages = true;
		} catch (JMSException e) {
			// If unable to connect to the server...
			log.error("Connection failed. \nThis is weird...are you sure that the Perun-Dispatcher is running on host[" + propertiesBean.getProperty("dispatcher.ip.address") + "] on port["
					+ propertiesBean.getProperty("dispatcher.port") + "] ? \nSee: dispatcher-config.properties. We gonna wait 5 sec and try again...", e);

			throw new RuntimeException(e);
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}

	public void stopProcessingSystemMessages() {
		if (processingMessages && systemQueueReceiver != null) {
			systemQueueReceiver.stop();
		}
	}

	public void closeSystemQueue() {
		throw new UnsupportedOperationException("Sorry...");
		/*
		 * // TODO: Send "we gonna close the bar" message to all clients... if
		 * (systemQueueInitiated) { stopProcessingSystemMessages(); try {
		 * connection.close(); } catch (JMSException e) {
		 * log.error(e.toString(),e.getCause()); } }
		 */
	}

	public boolean isProcessingMessages() {
		return processingMessages;
	}

	public boolean isSystemQueueInitiated() {
		return systemQueueInitiated;
	}

	protected void processDispatcherQueueAndMatchingRule(String systemMessagetext) throws PerunHornetQServerException, MessageFormatException {
		if (perunHornetQServer.isServerRunning() && perunHornetQServer.getJMSServerManager() != null) {
			if (log.isDebugEnabled()) {
				log.debug("Processing system message:" + systemMessagetext);
			}

			// Expected messages:

			// Register message
			// register:x
			// where x is an Integer that represents Engine's ID in the Perun
			// DB.

			// Good bye message
			// goodbye:x
			// where x is an Integer that represents Engine's ID in the Perun
			// DB.

			String[] clientIDsplitter = systemMessagetext.split(":");
			if (!clientIDsplitter[0].equalsIgnoreCase("register")) {
				throw new MessageFormatException("Client (Perun-Engine) sent a malformed message [" + systemMessagetext + "]");
			}
			int clientID = 0;
			try {
				clientID = Integer.parseInt(clientIDsplitter[1]);
			} catch (NumberFormatException e) {
				throw new MessageFormatException("Client (Perun-Engine) sent a malformed message [" + systemMessagetext + "]", e);
			}

			// Do we have this queue already?
			DispatcherQueue dispatcherQueue = null;
			dispatcherQueue = dispatcherQueuePool.getDispatcherQueueByClient(clientID);
			// Yes, so we just reload matching rules...
			if (dispatcherQueue != null) {

				smartMatcher.reloadRulesFromDBForEngine(clientID);

				// No, we have to create the whole JMS queue and load matching
				// rules...
			} else {
				createDispatcherQueueForClient(clientID);
			}

		} else {
			throw new PerunHornetQServerException("It looks like the HornetQ server is not running or JMSServerManager is fucked up...");
		}
	}

	public void createDispatcherQueuesForClients(Set<Integer> clientIDs) throws PerunHornetQServerException {
		if (perunHornetQServer.isServerRunning() && perunHornetQServer.getJMSServerManager() != null) {
			for (Integer clientID : clientIDs) {
				createDispatcherQueueForClient(clientID);
			}
		} else {
			throw new PerunHornetQServerException("It looks like the HornetQ server is not running or JMSServerManager is fucked up...");
		}
	}

	private void createDispatcherQueueForClient(Integer clientID) {
		// Create a new queue
		String queueName = "queue" + clientID;
		try {
			perunHornetQServer.getJMSServerManager().createQueue(false, queueName, null, false, new String[0]);
		} catch (Exception e) {
			log.error(e.toString(), e);
		}

		DispatcherQueue dispatcherQueue = new DispatcherQueue(clientID, queueName, session);
		// Rules
		smartMatcher.reloadRulesFromDBForEngine(clientID);
		// Add to the queue
		dispatcherQueuePool.addDispatcherQueue(dispatcherQueue);
	}

	public void setDispatcherQueuePool(DispatcherQueuePool dispatcherQueuePool) {
		this.dispatcherQueuePool = dispatcherQueuePool;
	}

	public void setPropertiesBean(Properties propertiesBean) {
		this.propertiesBean = propertiesBean;
	}

	public void setPerunHornetQServer(PerunHornetQServer perunHornetQServer) {
		this.perunHornetQServer = perunHornetQServer;
	}

	public void setSmartMatcher(SmartMatcher smartMatcher) {
		this.smartMatcher = smartMatcher;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

}
