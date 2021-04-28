package cz.metacentrum.perun.engine.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;

/**
 * Class used to send messages through JMS to Dispatcher and also to initiate/close the needed connection.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "jmsQueueManager")
public class JMSQueueManager {
	private final static Logger log = LoggerFactory.getLogger(JMSQueueManager.class);
	private static final String systemQueueName = "systemQueue";

	@Autowired
	private Properties propertiesBean;
	@Autowired
	private TaskExecutor taskExecutorMessageProcess;
	private boolean systemInitiated = false;
	private MessageProducer producer = null;
	@Autowired
	private MessageReceiver messageReceiver;
	private boolean receivingMessages = false;
	private Session session = null;
	private Connection connection = null;
	private boolean needToConnect = true;
	private int waitTime = 0;

	/**
	 *
	 *
	 */
	public void initiateConnection() {
		receivingMessages = false;
		while (needToConnect) {

			try {
				// Step 2. Instantiate the TransportConfiguration object which
				// contains the knowledge of what transport to use,
				// The server port etc.
				Map<String, Object> connectionParams = new HashMap<String, Object>();
				log.debug("Gonna connect to the host[{}] on port[{}].",
						propertiesBean.getProperty("dispatcher.ip.address"),
						propertiesBean.getProperty("dispatcher.port"));
				connectionParams.put(TransportConstants.PORT_PROP_NAME,
						Integer.parseInt(propertiesBean
								.getProperty("dispatcher.port")));
				connectionParams.put(TransportConstants.HOST_PROP_NAME,
						propertiesBean.getProperty("dispatcher.ip.address"));

				TransportConfiguration transportConfiguration = new TransportConfiguration(
						NettyConnectorFactory.class.getName(), connectionParams);

				// Step 3 Directly instantiate the JMS ConnectionFactory object
				// using that TransportConfiguration
				ConnectionFactory cf = (ConnectionFactory) HornetQJMSClient
						.createConnectionFactoryWithoutHA(JMSFactoryType.CF,
								transportConfiguration);

				// Step 4.Create a JMS Connection
				connection = cf.createConnection();

				// Step 5. Create a JMS Session
				session = connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);

				// Step 10. Start the Connection
				connection.start();
				systemInitiated = true;
				needToConnect = false;
				waitTime = 0;
			} catch (JMSException e) {
				// If unable to connect to the server...
				needToConnect = true;
				waitTime = waitTime + 10000;
				log.error("Connection failed. We gonna wait {} s and try again.", (waitTime / 1000), e);
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e1) {
					log.error(e1.toString(), e1);
				}
			}
		}
	}

	public void registerForReceivingMessages() {
		try {
			// Step 1. Directly instantiate the JMS Queue object.
			Queue queue = HornetQJMSClient.createQueue(systemQueueName);

			// Step 6. Create a JMS Message Producer
			producer = session.createProducer(queue);

			if(!receivingMessages) {
				TextMessage message = session.createTextMessage("register");

				// Step 8. Send the Message
				producer.send(message);
				log.debug("Registration message[{}] has been sent.", message.getText());
				Thread.sleep(1000);
			}

			// Execute receiver
			messageReceiver.setUp("queue", session, producer);
			// taskExecutorMessageProcess.execute(messageReceiver);
			messageReceiver.run();
			receivingMessages = true;
			// TODO: Put a while loop here? As same as in the
			// "public void initiateConnection() {...}" ?
		} catch (Exception e) {
			log.error(e.toString(), e);
		}

	}

	public void start() {

		taskExecutorMessageProcess.execute(new Runnable() {
			public void run() {
				while (!systemInitiated || receivingMessages) {
					initiateConnection();
					registerForReceivingMessages();
					// tear down the session, connection etc.
					try {
						session.close();
						connection.stop();
						connection.close();
					} catch (Exception e) {
						log.error(e.toString(), e);
					}
					needToConnect = true;
					receivingMessages = messageReceiver.isRunning();
				}
			}
		});

	}

	public void reportTaskResult(TaskResult taskResult) throws JMSException, InterruptedException {
		TextMessage message = session.createTextMessage("taskresult:" + taskResult.serializeToString());
		message.setIntProperty("priority",  2);
		messageReceiver.sendMessage(message);
		log.info("[{}] TaskResult for destination {} sent to dispatcher.", taskResult.getTaskId(),
				taskResult.getDestinationId());
	}

	public void reportTaskStatus(int id, Task.TaskStatus status, long miliseconds) throws JMSException, InterruptedException {
		TextMessage message = session.createTextMessage("task:"
				+ id + ":" + status + ":" + miliseconds);
		message.setIntProperty("priority", 6);
		messageReceiver.sendMessage(message);
		log.info("[{}] Task state {} sent to dispatcher.", id, status);
	}

	public void sendGoodByeAndClose() {
		try {
			TextMessage message = session.createTextMessage("goodbye");
			// Step 8. Send the Message
			synchronized(producer) {
				producer.send(message);
			}
			// TODO: Put a while loop here? As same as in the
			// "public void initiateConnection() {...}" ?
		} catch (Exception e) {
			log.error(e.toString(), e);
		}
	}

	public boolean isSystemInitiated() {
		return systemInitiated;
	}

	public boolean isReceivingMessages() {
		return receivingMessages;
	}

	public Properties getPropertiesBean() {
		return propertiesBean;
	}

	public void setPropertiesBean(Properties propertiesBean) {
		this.propertiesBean = propertiesBean;
	}

	public TaskExecutor getTaskExecutorMessageProcess() {
		return taskExecutorMessageProcess;
	}

	public void setTaskExecutorMessageProcess(
			TaskExecutor taskExecutorMessageProcess) {
		this.taskExecutorMessageProcess = taskExecutorMessageProcess;
	}


}
