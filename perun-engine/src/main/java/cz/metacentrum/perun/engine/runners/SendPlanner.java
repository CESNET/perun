package cz.metacentrum.perun.engine.runners;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.SendWorker;
import cz.metacentrum.perun.engine.scheduling.impl.BlockingSendExecutorCompletionService;
import cz.metacentrum.perun.engine.scheduling.impl.SendWorkerImpl;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import cz.metacentrum.perun.taskslib.model.SendTask;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.runners.impl.AbstractRunner;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class represents permanently running thread, which should run in a single instance.
 * <p>
 * It takes all GENERATED Tasks from generatedTasks blocking queue provided by GenCollector and creates SendTask and
 * SendWorker for each Destination and put them to BlockingSendExecutorCompletionService. Processing waits on call of
 * blockingSubmit() for each SendWorker.
 * <p>
 * Expected Task status change GENERATED -> SENDING is reported to Dispatcher. For Tasks without any Destination, status
 * changes GENERATED -> ERROR and Task is removed from SchedulingPool (Engine).
 *
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 * @see SchedulingPool#getGeneratedTasksQueue()
 * @see SendTask
 * @see SendWorkerImpl
 * @see BlockingSendExecutorCompletionService
 */
public class SendPlanner extends AbstractRunner {

  private static final Logger LOG = LoggerFactory.getLogger(SendPlanner.class);

  @Autowired
  private BlockingSendExecutorCompletionService sendCompletionService;
  @Autowired
  private SchedulingPool schedulingPool;
  @Autowired
  private JMSQueueManager jmsQueueManager;
  private File directory;

  public SendPlanner() {
  }

  public SendPlanner(BlockingSendExecutorCompletionService sendCompletionService, SchedulingPool schedulingPool,
                     JMSQueueManager jmsQueueManager) {
    this.sendCompletionService = sendCompletionService;
    this.schedulingPool = schedulingPool;
    this.jmsQueueManager = jmsQueueManager;
  }

  private void jmsLogError(Task task) {
    LOG.warn("[{}] Could not send SEND status update to {} to Dispatcher.", task.getId(), task.getStatus());
  }

  @Override
  public void run() {
    BlockingQueue<Task> generatedTasks = schedulingPool.getGeneratedTasksQueue();
    while (!shouldStop()) {
      try {
        Task task = generatedTasks.take();
        // set Task status immediately
        // no destination -> ERROR
        // has destinations -> SENDING
        if (task.getDestinations().isEmpty()) {
          task.setStatus(Task.TaskStatus.ERROR);
          try {
            jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(), System.currentTimeMillis());
          } catch (JMSException e) {
            jmsLogError(task);
          }
          try {
            schedulingPool.removeTask(task);
          } catch (TaskStoreException e) {
            LOG.error("[{}] Generated Task without destinations could not be removed from SchedulingPool: {}",
                task.getId(), e);
          }
          // skip to next generated Task
          continue;
        }
        // Task has destinations
        task.setStatus(Task.TaskStatus.SENDING);
        // TODO - would be probably better to have this as one time call after first SendWorker is submitted
        // TODO   but then processing stuck tasks must reflect, that SENDING task might have sendStartTime=NULL
        task.setSendStartTime(LocalDateTime.now());

        schedulingPool.addSendTaskCount(task, task.getDestinations().size());
        try {
          jmsQueueManager.reportTaskStatus(task.getId(), task.getStatus(),
              task.getSendStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } catch (JMSException e) {
          jmsLogError(task);
        }

        // create SendTask and SendWorker for each Destination
        for (Destination destination : task.getDestinations()) {
          // submit for execution
          SendTask sendTask = new SendTask(task, destination);
          SendWorker worker = new SendWorkerImpl(sendTask, directory);
          sendCompletionService.blockingSubmit(worker);
        }

      } catch (InterruptedException e) {

        String errorStr = "Thread planning SendTasks was interrupted.";
        LOG.error(errorStr);
        throw new RuntimeException(errorStr, e);

      } catch (Throwable ex) {
        LOG.error(
            "Unexpected exception in SendPlanner thread. Stuck Tasks will be cleaned by " +
            "PropagationMaintainer#endStuckTasks() later.",
            ex);
      }
    }
  }

  @Autowired
  public void setPropertiesBean(Properties propertiesBean) {
    if (propertiesBean != null) {
      directory = new File(propertiesBean.getProperty("engine.sendscript.path"));
    }
  }

}
