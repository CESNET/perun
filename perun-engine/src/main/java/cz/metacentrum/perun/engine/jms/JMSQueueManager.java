package cz.metacentrum.perun.engine.jms;

import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

/**
 * Class used to send messages through JMS to Dispatcher and also to initiate/close the needed connection.
 *
 * @author Michal Karm Babacek
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@org.springframework.stereotype.Service(value = "jmsQueueManager")
public class JMSQueueManager {
  private static final Logger LOG = LoggerFactory.getLogger(JMSQueueManager.class);
  private static final String SYSTEM_QUEUE_NAME = "systemQueue";

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

  public Properties getPropertiesBean() {
    return propertiesBean;
  }

  public TaskExecutor getTaskExecutorMessageProcess() {
    return taskExecutorMessageProcess;
  }

  /**
   *
   */
  public void initiateConnection() {
    receivingMessages = false;
    while (needToConnect) {

      try {
        LOG.debug("Gonna connect to the host[{}] on port[{}].", propertiesBean.getProperty("dispatcher.ip.address"),
                    propertiesBean.getProperty("dispatcher.port"));

        // load dispatcher config, fallback to fixed values
        String host = propertiesBean.getProperty("dispatcher.ip.address", "127.0.0.1");
        String port = propertiesBean.getProperty("dispatcher.port", "6071");
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);

        // Step 4.Create a JMS Connection
        connection = connectionFactory.createConnection();

        // Step 5. Create a JMS Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Step 10. Start the Connection
        connection.start();
        systemInitiated = true;
        needToConnect = false;
        waitTime = 0;
      } catch (JMSException e) {
        // If unable to connect to the server...
        needToConnect = true;
        waitTime = waitTime + 10000;
        LOG.error("Connection failed. We gonna wait {} s and try again.", (waitTime / 1000), e);
        try {
          Thread.sleep(waitTime);
        } catch (InterruptedException e1) {
          LOG.error(e1.toString(), e1);
        }
      }
    }
  }

  public boolean isReceivingMessages() {
    return receivingMessages;
  }

  public boolean isSystemInitiated() {
    return systemInitiated;
  }

  public void registerForReceivingMessages() {
    try {
      // Step 1. Directly instantiate the JMS Queue object.
      Queue queue = session.createQueue(SYSTEM_QUEUE_NAME);

      // Step 6. Create a JMS Message Producer
      producer = session.createProducer(queue);

      if (!receivingMessages) {
        TextMessage message = session.createTextMessage("register");

        // Step 8. Send the Message
        producer.send(message);
        LOG.debug("Registration message[{}] has been sent.", message.getText());
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
      LOG.error(e.toString(), e);
    }

  }

  public void reportTaskResult(TaskResult taskResult) throws JMSException, InterruptedException {
    TextMessage message = session.createTextMessage("taskresult:" + taskResult.serializeToString());
    message.setIntProperty("priority", 2);
    messageReceiver.sendMessage(message);
    LOG.info("[{}, {}] TaskResult for destination {} sent to dispatcher.", taskResult.getTaskId(),
        taskResult.getTaskRunId(),
        taskResult.getDestinationId());
  }

  public void reportTaskStatus(Task task, Task.TaskStatus status, long miliseconds)
      throws JMSException, InterruptedException {
    TextMessage message = session.createTextMessage("task:" + task.getId() + ":" + status + ":" + miliseconds);
    message.setIntProperty("priority", 6);
    messageReceiver.sendMessage(message);
    LOG.info("[{}, {}] Task state {} sent to dispatcher.", task.getId(), task.getRunId(), status);
  }

  public void sendGoodByeAndClose() {
    try {
      TextMessage message = session.createTextMessage("goodbye");
      // Step 8. Send the Message
      synchronized (producer) {
        producer.send(message);
      }
      // TODO: Put a while loop here? As same as in the
      // "public void initiateConnection() {...}" ?
    } catch (Exception e) {
      LOG.error(e.toString(), e);
    }
  }

  public void setPropertiesBean(Properties propertiesBean) {
    this.propertiesBean = propertiesBean;
  }

  public void setTaskExecutorMessageProcess(TaskExecutor taskExecutorMessageProcess) {
    this.taskExecutorMessageProcess = taskExecutorMessageProcess;
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
            LOG.error(e.toString(), e);
          }
          needToConnect = true;
          receivingMessages = messageReceiver.isRunning();
        }
      }
    });

  }


}
