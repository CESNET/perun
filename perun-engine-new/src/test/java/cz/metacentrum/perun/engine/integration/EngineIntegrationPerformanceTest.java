package cz.metacentrum.perun.engine.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.annotation.IfProfileValue;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.BaseTest;
import cz.metacentrum.perun.engine.TestBase;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.processing.EventProcessor;
import cz.metacentrum.perun.engine.scheduling.PropagationMaintainer;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskExecutorEngine;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;
import cz.metacentrum.perun.engine.scheduling.TaskStatusManager;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskManager;

/**
 * @author Michal Karm Babacek
 * 
 *         Unfortunately, this test can not be transactional due to multi-threaded environment, hence it can not be rolled back. We have to clean up after ourselves...
 */
//@Transactional(propagation = Propagation.NEVER)
public class EngineIntegrationPerformanceTest extends TestBase {

    private final static Logger log = LoggerFactory.getLogger(EngineIntegrationPerformanceTest.class);
    // Time out for threads to complete (milliseconds)
    private final static int TIME_OUT = 20000;

    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private TaskExecutorEngine taskExecutorEngine;
    @Autowired
    private TaskExecutor taskExecutorMessageProcess;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private TaskStatusManager taskStatusManager;
    @Autowired
    private Properties propertiesBean;
    @Autowired
    private SchedulingPool schedulingPool;
    @Autowired 
    private PropagationMaintainer propagationMaintainer;
    @Autowired
    private TaskResultDao taskResultDao;
    @Autowired
    private EventProcessor eventProcessor;
    @Autowired
    private Task task1;
    @Autowired
    private Destination destination1;
    @Autowired
    private Destination destination2;
    @Autowired
    private Destination destination3;
    @Autowired
    private Destination destination4;
    
    private long started;
    private int ended = 0;
    private final int NUM_TASKS = 500;
    
    private class JMSQueueManagerMock extends JMSQueueManager {

		@Override
		public void reportFinishedTask(Task task, String destinations)
				throws JMSException {
			ended += 1;
		}
    	
    }
    
    @IfProfileValue(name="perun.test.groups", values=("performance"))
    @Test(timeout=10000)
    public void testExecutingRealMessage() throws InterruptedException, InternalErrorException {
    	long sum = 0;
    	int num;
    	for(num = 0; num < NUM_TASKS; num++) {
    	String testEvent = "task|1|[" + num + "][" + task1.getExecServiceId() 
				+ "][" + task1.getFacility().serializeToString()
				+ "]|[Destinations [";
		for(Destination destination: task1.getDestinations()) {
			testEvent = testEvent.concat(destination.serializeToString() + ", ");
		}
		testEvent = testEvent.concat("]]|[]");

		//String message = "event|1|[Tue Aug 30 12:29:23 CEST 2011][clockworkorange][Member:[id='36712'] added to Group:[id='16326', name='falcon', description='null'].]";

        started = System.currentTimeMillis();

		// creates task, puts it into pool
		eventProcessor.receiveEvent(testEvent);
    	}
        
        /*
        EventProcessorWorker eventProcessorWorker = new EventProcessorWorker(testEvent);
        taskExecutorMessageProcess.execute(eventProcessorWorker);
        boolean itWentOk = false;
        long started = System.currentTimeMillis();
        while (System.currentTimeMillis() - started < TIME_OUT) {
            if (taskScheduler.getPoolSize() >= 1) {
                itWentOk = true;
                log.debug("     #marDk323 OK, we have " + taskScheduler.getPoolSize() + " ExecService-Facility pairs in the pool.");
                break;
            } else {
                log.debug("     #marDk323 There are only " + taskScheduler.getPoolSize() + " ExecService-Facility pairs in the pool.");
            }
            Thread.sleep(500);
        }
        if (!itWentOk) {
            fail("#marDk323 Waiting for SchedilingPool has timed out.");
        }
		*/
        
		// process pool, set task status to planned
		taskScheduler.processPool();
        
        /*
        TaskSchedulerWorker taskSchedulerWorker = new TaskSchedulerWorker();
        taskExecutorMessageProcess.execute(taskSchedulerWorker);
        started = System.currentTimeMillis();
        itWentOk = false;
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itWentOk = true;
            //if (taskManager.getTask(getExecServicePasswdGenerate(), getFacility1195(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))) == null) {
            //    itWentOk = false;
            //}
            log.debug("###TASK TASKS were:" + taskManager.listAllTasks(Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))).toString());
            Thread.sleep(500);
            if (itWentOk) {
                break;
            }
        }
        if (!itWentOk) {
            fail("#marDk323 Waiting for TaskScheduler has timed out.");
        }
        */


		// actually run tasks
		taskExecutorEngine.beginExecuting();
		
		/*
        TaskExecutorWorker taskExecutorWorker = new TaskExecutorWorker();
        taskExecutorMessageProcess.execute(taskExecutorWorker);

        ///
        taskExecutorMessageProcess.execute(taskSchedulerWorker);
        started = System.currentTimeMillis();
        itWentOk = false;
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itWentOk = true;

            //if (taskManager.getTask(getExecServicePasswdSend(), getFacility1195(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))) == null) {
            //    itWentOk = false;
            //}
            log.debug("###TASK TASKS were:" + taskManager.listAllTasks(Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))).toString());
            Thread.sleep(500);
            if (itWentOk) {
                break;
            }
        }
		*/
        ////

		/*
		taskExecutorMessageProcess.execute(taskSchedulerWorker);
        started = System.currentTimeMillis();
        itWentOk = false;
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itWentOk = true;

            //if (taskManager.getTask(getExecServicePasswdSend(), getFacility1195(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))) == null) {
            //    itWentOk = false;
            //}
            log.debug("###TASK TASKS were:" + taskManager.listAllTasks(Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))).toString());
            Thread.sleep(500);
            if (itWentOk) {
                break;
            }
        }
		
        taskExecutorMessageProcess.execute(taskExecutorWorker);
		*/

		propagationMaintainer.setJmsQueueManager(new JMSQueueManagerMock());
		
		/////
		/*
        int expectedTaskResults = 3;
        List<TaskResult> taskResults = null;
        itWentOk = false;
        if (taskManager == null) {
            fail("taskManager really shouldn't be null :-)");
        }

        Task taskExecService1 = task1; //taskManager.getTask(getExecServicePasswdSend(), getFacility1195(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        if (taskExecService1 == null) {
            fail("taskExecService1 really shouldn't be null :-)");
        }
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itWentOk = true;
            if (taskResultDao == null) {
                fail("taskResultDao really shouldn't be null :-)");
            }
            taskResults = taskResultDao.getTaskResultsByTask(taskExecService1.getId());
            log.debug("TASKRESULTS:" + taskResults.size());
            // Are there three of them?
            if (taskResults.size() != expectedTaskResults) {
                itWentOk = false;
            } else {
                boolean hasDestinationA = false;
                boolean hasDestinationB = false;
                boolean hasDestinationC = false;
                for (TaskResult taskResult : taskResults) {
                    // DestinationA ?
                    if (taskResult.getDestinationId() == destination1.getId()) {
                        hasDestinationA = true;
                        log.debug("Result " + taskResult.getId() + " hasDestinationA: TRUE");
                    }
                    // DestinationB ?
                    if (taskResult.getDestinationId() == destination2.getId()) {
                        hasDestinationB = true;
                        log.debug("Result " + taskResult.getId() + " hasDestinationB: TRUE");
                    }
                    // DestinationC ?
                    if (taskResult.getDestinationId() == destination3.getId()) {
                        hasDestinationC = true;
                        log.debug("Result " + taskResult.getId() + " hasDestinationC: TRUE");
                    }
                }
                log.debug("hasDestinationA: " + hasDestinationA + ", hasDestinationB: " + hasDestinationB + ", hasDestinationC: " + hasDestinationC);
                if (!(hasDestinationA && hasDestinationB && hasDestinationC)) {
                    itWentOk = false;
                }
            }
            if (itWentOk) {
                break;
            }
            Thread.sleep(100);
        }
        log.debug("\tTASKRESULTS total:" + taskResults.size());
        // TODO: Put some While here...
        Thread.sleep(10000);

        // GENERATE
        log.debug("\tTASKRESULTS total:" + taskResults.size());
        Task task = task1; //taskManager.getTask(getExecServicePasswdGenerate(), getFacility1195(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        assertNotNull("Task with getExecServicePasswdGenerate should not be null.", task);
        if (!(task.getStatus().equals(TaskStatus.DONE))) {
            fail("Task with getExecServicePasswdGenerate should be DONE.");
        }
        assertTrue("Task with getExecServicePasswdSend should have all its Task results.", itWentOk);
        // TODO: Put some While here...
        Thread.sleep(10000);
        */
		
		while(ended < NUM_TASKS) {
			propagationMaintainer.checkResults();
			Thread.sleep(100);
		}
		for(Task task : schedulingPool.getDoneTasks()) {
			sum += task.getEndTime().getTime() - task.getSchedule().getTime();
		}
		log.debug("Average TASK propagation time: " + sum/NUM_TASKS);
    }

    private class EventProcessorWorker implements Runnable {
        private String message;

        public EventProcessorWorker(String message) {
            super();
            this.message = message;
        }

        @Override
        public void run() {
            eventProcessor.receiveEvent(message);
        }

    }

    private class TaskSchedulerWorker implements Runnable {
        @Override
        public void run() {
            try {
                taskScheduler.processPool();
            } catch (InternalErrorException e) {
                e.printStackTrace();
            }
        }

    }

    private class TaskExecutorWorker implements Runnable {
        @Override
        public void run() {
            taskExecutorEngine.beginExecuting();
        }

    }

    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public TaskExecutorEngine getTaskExecutorEngine() {
        return taskExecutorEngine;
    }

    public void setTaskExecutorEngine(TaskExecutorEngine taskExecutorEngine) {
        this.taskExecutorEngine = taskExecutorEngine;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutorMessageProcess;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutorMessageProcess = taskExecutor;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public Properties getPropertiesBean() {
        return propertiesBean;
    }

    public void setPropertiesBean(Properties propertiesBean) {
        this.propertiesBean = propertiesBean;
    }

    public TaskResultDao getTaskResultDao() {
        return taskResultDao;
    }

    public void setTaskResultDao(TaskResultDao taskResultDao) {
        this.taskResultDao = taskResultDao;
    }

    public EventProcessor getEventProcessor() {
        return eventProcessor;
    }

    public void setEventProcessor(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

}
