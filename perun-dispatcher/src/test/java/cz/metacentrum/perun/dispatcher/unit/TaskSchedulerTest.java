package cz.metacentrum.perun.dispatcher.unit;

import static org.junit.Assert.assertFalse;

import cz.metacentrum.perun.core.bl.TasksManagerBl;
import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler;
import cz.metacentrum.perun.dispatcher.scheduling.impl.SchedulingPoolImpl;
import cz.metacentrum.perun.dispatcher.scheduling.impl.TaskScheduled;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskSchedule;
import java.util.Properties;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.FutureTask;
import javax.annotation.Resource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@Ignore("All those tests must be reworked to use Mocks!")
public class TaskSchedulerTest extends AbstractDispatcherTest {

  private final static Logger log = LoggerFactory.getLogger(TaskSchedulerTest.class);
  @Autowired
  SchedulingPool schedulingPool;
  @Resource(name = "dispatcherPropertiesBean")
  Properties dispatcherProperties;
  @Autowired
  TasksManagerBl tasksManagerBl;
  private SimpleTaskSchedulerSpy simpleSpy;
  private SimpleTaskSchedulerSpy recurrenceSpy;
  private FutureTask<SimpleTaskSchedulerSpy> simpleFutureTask;
  private FutureTask<SimpleTaskSchedulerSpy> recurrenceFutureTask;

  @Before
  public void setupTests() throws Exception {
    super.setupTests();
    simpleSpy = new SimpleTaskSchedulerSpy(2);
    recurrenceSpy = new SimpleTaskSchedulerSpy(0);
    simpleFutureTask = new FutureTask<SimpleTaskSchedulerSpy>(simpleSpy, null);
    recurrenceFutureTask = new FutureTask<SimpleTaskSchedulerSpy>(recurrenceSpy, null);
    DelayQueue<TaskSchedule> waitingQueue = new DelayQueue<>();
    DelayQueue<TaskSchedule> forcedWaitingQueue = new DelayQueue<>();
    simpleSpy.setWaitingTasksQueue(waitingQueue);
    simpleSpy.setWaitingForcedTasksQueue(forcedWaitingQueue);
    simpleSpy.setTasksManagerBl(tasksManagerBl);
    recurrenceSpy.setWaitingTasksQueue(waitingQueue);
    recurrenceSpy.setWaitingForcedTasksQueue(forcedWaitingQueue);
    recurrenceSpy.setTasksManagerBl(tasksManagerBl);
    ((SchedulingPoolImpl) schedulingPool).setWaitingTasksQueue(waitingQueue);
    ((SchedulingPoolImpl) schedulingPool).setWaitingTasksQueue(forcedWaitingQueue);
  }

  @Test
  public void simpleRunTest() throws InterruptedException {
    simpleSpy.setTask(simpleFutureTask);
    simpleSpy.setSchedulingPool(schedulingPool);
    Long timeLimit = 100L;
    Task[] tasks = simpleSetup(timeLimit, schedulingPool);
    Task testTask1 = tasks[0], testTask2 = tasks[1];
    schedulingPool.scheduleTask(testTask1, 4);
    schedulingPool.scheduleTask(testTask1, 4);
    Thread.sleep(timeLimit / 100);
    schedulingPool.scheduleTask(testTask1, 4);
    Thread.sleep((timeLimit / 100) * 8);
    schedulingPool.scheduleTask(testTask2, 4);

    simpleFutureTask.run();
    assertFalse(simpleSpy.testFailed);
    try {
      // FIXME - make every second test not to fail on interrupted exception
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void sourceUpdatedRunTest() {
    simpleSpy.setTask(simpleFutureTask);
    simpleSpy.setSchedulingPool(schedulingPool);
    Long timeLimit = 100L;
    Task[] tasks = simpleSetup(timeLimit, schedulingPool);
    Task testTask1 = tasks[0], testTask2 = tasks[1];
    testTask2.setSourceUpdated(true);
    schedulingPool.scheduleTask(testTask2, 4);
    schedulingPool.scheduleTask(testTask1, 4);
    simpleFutureTask.run();
    assertFalse(simpleSpy.testFailed);
    try {
      // FIXME - make every second test not to fail on interrupted exception
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void sourceUpdatedRecurrenceRunTest() {
    recurrenceSpy.setTask(recurrenceFutureTask);
    recurrenceSpy.setSchedulingPool(schedulingPool);
    Long timeLimit = 100L;
    Task[] tasks = simpleSetup(timeLimit, schedulingPool);
    Task testTask1 = tasks[0];
    schedulingPool.scheduleTask(testTask1, 2);

    recurrenceFutureTask.run();
    assertFalse(recurrenceSpy.testFailed);
    try {
      // FIXME - make every second test not to fail on interrupted exception
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private Task[] simpleSetup(Long timeLimit, SchedulingPool schedulingPool) {
    Properties properties = ((SchedulingPoolImpl) schedulingPool).getDispatcherProperties();
    if (properties == null) {
      properties = dispatcherProperties;
    }
    properties.setProperty("dispatcher.task.delay.time", timeLimit.toString());
    properties.setProperty("dispatcher.task.delay.count", "1");
    ((SchedulingPoolImpl) schedulingPool).setDispatcherProperties(properties);
    Task testTask1 = new Task();
    testTask1.setService(service1);
    testTask1.setFacility(facility1);
    testTask1.setId(0);
    testTask1.setStatus(TaskStatus.WAITING);
    Task testTask2 = new Task();
    testTask2.setId(2);
    testTask2.setService(service2);
    testTask2.setFacility(facility1);
    testTask2.setStatus(TaskStatus.WAITING);

    return new Task[] {testTask1, testTask2};
  }

  private abstract class AbstractTaskSchedulerSpy extends TaskScheduler {
    @Override
    protected void initPerunSession() {
    }
  }

  private class SimpleTaskSchedulerSpy extends AbstractTaskSchedulerSpy {
    int scheduledCounter = 0;
    int scheduleLimit;
    boolean testFailed = true;
    FutureTask<SimpleTaskSchedulerSpy> task;

    public SimpleTaskSchedulerSpy(int scheduleLimit) {
      this.scheduleLimit = scheduleLimit;
    }

    public void setTask(FutureTask<SimpleTaskSchedulerSpy> task) {
      this.task = task;
    }

    @Override
    protected TaskScheduled sendToEngine(Task task) {
      if (task.isSourceUpdated() && !task.isPropagationForced()) {
        testFailed = true;
        this.task.cancel(true);
        return TaskScheduled.ERROR;
      }
      scheduledCounter += 1;
      if (scheduledCounter >= scheduleLimit) {
        testFailed = false;
        this.task.cancel(true);
        stop();
      }
      return TaskScheduled.SUCCESS;
    }
  }

}
