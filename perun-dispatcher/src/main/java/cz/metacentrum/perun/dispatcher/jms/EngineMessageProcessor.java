package cz.metacentrum.perun.dispatcher.jms;

import cz.metacentrum.perun.dispatcher.activemq.PerunActiveMQServer;
import cz.metacentrum.perun.dispatcher.exceptions.MessageFormatException;
import cz.metacentrum.perun.dispatcher.exceptions.PerunActiveMQServerException;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import jakarta.annotation.Resource;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

/**
 * Main class ensuring processing of JMS communication between Dispatcher and Engines. It start/stop processing of
 * messages, create queues and load processing rules for Engines. Also provide method for message parsing.
 * <p>
 * Queues to Engines are represented by EngineMessageProducer objects. Queue is used by TaskScheduler. Queue from Engine
 * is represented by EngineMessageConsumer. Received messages result in calls to SchedulingPool.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer
 * @see cz.metacentrum.perun.dispatcher.jms.EngineMessageConsumer
 * @see cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler
 * @see cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool
 */
@org.springframework.stereotype.Service(value = "engineMessageProcessor")
public class EngineMessageProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(EngineMessageProcessor.class);

  private Properties dispatcherProperties;
  private PerunActiveMQServer perunActiveMQServer;
  private TaskExecutor taskExecutor;
  private EngineMessageConsumer engineMessageConsumer;
  private SchedulingPool schedulingPool;
  private EngineMessageProducerFactory engineMessageProducerFactory;

  private Session session = null;
  private boolean processingMessages = false;
  private boolean systemQueueInitiated = false;
  private ActiveMQConnectionFactory connectionFactory = null;
  private Connection connection;
  private BlockingDeque<TextMessage> outputMessages = null;
  private boolean restartActiveMQServer = false;


  // ----- setters -------------------------------------

  /**
   * Create JMS queue for Engine.
   */
  private void createDispatcherQueueForClient() {
    engineMessageProducerFactory.createProducer("queue", session, outputMessages);
  }

  public Properties getDispatcherProperties() {
    return dispatcherProperties;
  }

  public EngineMessageConsumer getEngineMessageConsumer() {
    return engineMessageConsumer;
  }

  public EngineMessageProducerFactory getEngineMessageProducerFactory() {
    return engineMessageProducerFactory;
  }

  public PerunActiveMQServer getPerunActiveMQServer() {
    return perunActiveMQServer;
  }

  public SchedulingPool getSchedulingPool() {
    return schedulingPool;
  }

  public TaskExecutor getTaskExecutor() {
    return taskExecutor;
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
   * Process content of JMS message received from Engine. This is called by SystemQueueReceiver for each message.
   * <p>
   * Expected message format is:
   * <p>
   * Register engine message register
   * <p>
   * Good bye engine message goodbye
   * <p>
   * Task status change message task:y:status:timestamp y is an Integer that represents task ID status is string
   * representation of task status timestamp is a string representation of timestamp (long)
   * <p>
   * Task result message taskresult:object object is serialized TaskResult object sent from Engine
   *
   * @param message Message to be parsed a processed
   * @throws PerunActiveMQServerException When ActiveMQ server is not running
   * @throws MessageFormatException      When Engine sent malformed JMS message
   * @see EngineMessageConsumer
   */
  protected void processEngineMessage(String message) throws PerunActiveMQServerException, MessageFormatException {

    if (perunActiveMQServer.isServerRunning()) {

      LOG.debug("Processing JMS message: " + message);

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

        if (clientMessageSplitter.length < 4) {
          throw new MessageFormatException("Engine sent a malformed message, not enough params [" + message + "]");
        }

        try {
          schedulingPool.onTaskStatusChange(Integer.parseInt(clientMessageSplitter[1]), clientMessageSplitter[2],
                  clientMessageSplitter[3]);
        } catch (NumberFormatException e) {
          throw new MessageFormatException("Engine sent a malformed message, could not parse client ID", e);
        }

      } else if (clientMessageSplitter[0].equalsIgnoreCase("taskresult")) {

        if (clientMessageSplitter.length < 2) {
          throw new MessageFormatException("Engine sent a malformed message, not enough params [" + message + "]");
        }

        schedulingPool.onTaskDestinationComplete(clientMessageSplitter[1]);

      } else {
        throw new MessageFormatException("Engine sent a malformed message, unknown type of message [" + message + "]");
      }

    } else {
      throw new PerunActiveMQServerException("ActiveMQ server is not running or JMSServerManager is messed up...");
    }
  }

  @Resource(name = "dispatcherPropertiesBean")
  public void setDispatcherProperties(Properties dispatcherProperties) {
    this.dispatcherProperties = dispatcherProperties;
  }

  @Autowired
  public void setEngineMessageConsumer(EngineMessageConsumer engineMessageConsumer) {
    this.engineMessageConsumer = engineMessageConsumer;
  }


  // ----- methods -------------------------------------

  @Autowired
  public void setEngineMessageProducer(EngineMessageProducerFactory engineMessageProducerFactory) {
    this.engineMessageProducerFactory = engineMessageProducerFactory;
  }

  @Autowired
  public void setPerunActiveMQServer(PerunActiveMQServer perunActiveMQServer) {
    this.perunActiveMQServer = perunActiveMQServer;
  }

  @Autowired
  public void setSchedulingPool(SchedulingPool schedulingPool) {
    this.schedulingPool = schedulingPool;
  }

  @Autowired
  public void setTaskExecutor(TaskExecutor taskExecutor) {
    this.taskExecutor = taskExecutor;
  }

  /**
   * Setup JMS queues between dispatcher and engines and start processing available messages. ActiveMQ server must be
   * running already.
   *
   * @see PerunActiveMQServer
   */
  public void startProcessingSystemMessages() {

    if (outputMessages == null) {
      outputMessages = new LinkedBlockingDeque<TextMessage>();
    }

    connection = null;
    try {
      if (restartActiveMQServer) {
        engineMessageProducerFactory.removeProducer();
        perunActiveMQServer.stopServer();
        perunActiveMQServer.startServer();
      }

      // load dispatcher config, fallback to fixed values
      String host = dispatcherProperties.getProperty("dispatcher.ip.address", "127.0.0.1");
      String port = dispatcherProperties.getProperty("dispatcher.port", "6071");
      connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);

      //Next we create a JMS connection using the connection factory:
      connection = connectionFactory.createConnection();

      //And we create a non transacted JMS Session, with AUTO\_ACKNOWLe.g. //acknowledge mode:
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      //We make sure we start the connection, or delivery won't occur on it:
      connection.start();

      if (processingMessages) {
        // make sure processing is stopped before new start when called as "restart"
        engineMessageConsumer.stop();
      }
      engineMessageConsumer.setUp("systemQueue", session);
      LOG.debug("Receiving messages started...");
      taskExecutor.execute(engineMessageConsumer);
      LOG.debug("JMS Initialization done.");
      processingMessages = true;

    } catch (JMSException e) {
      // If unable to connect to the server...
      LOG.error("Connection failed. \nThis is weird...are you sure that the Perun-Dispatcher is running on host [" +
              dispatcherProperties.getProperty("dispatcher.ip.address") + "] on port [" +
              dispatcherProperties.getProperty("dispatcher.port") +
              "] ? \nSee: perun-dispatcher.properties. We gonna wait 5 sec and try again...", e);
      restartActiveMQServer = true;
      throw new RuntimeException(e);
    } catch (Exception e) {
      LOG.error("Can't start processing of JMS: {}", e);
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
        LOG.debug("JMS processing stopped.");
      } catch (JMSException e) {
        LOG.error("Error closing JMS client connection: ", e.toString());
      }
    }
  }

}
