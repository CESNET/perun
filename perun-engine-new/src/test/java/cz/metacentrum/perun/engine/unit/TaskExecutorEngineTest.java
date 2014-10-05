package cz.metacentrum.perun.engine.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.BaseTest;
import cz.metacentrum.perun.engine.scheduling.TaskExecutorEngine;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;
import cz.metacentrum.perun.engine.service.EngineManager;
import cz.metacentrum.perun.rpclib.Rpc;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskManager;

/**
 * @author Michal Karm Babacek
 *
 *         Unfortunately, this test can not be transactional due to multi-threaded environment, hence it can not be rolled back. We have to clean up after ourselves...
 */
	@TransactionConfiguration(defaultRollback = false, transactionManager = "transactionManagerPerunEngine")
@Transactional(propagation = Propagation.NEVER)
public class TaskExecutorEngineTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(TaskExecutorEngineTest.class);
    // Time out for threads to complete (milliseconds)
    private final static int TIME_OUT = 60000;

    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private TaskExecutorEngine taskExecutorEngine;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private EngineManager engineManager;
    @Autowired
    private Properties propertiesBean;
    @Autowired
    private TaskResultDao taskResultDao;
    private Set<Integer> execServices = null;
    private Set<Integer> tasks = null;

    @PostConstruct
    public void setUp() throws ServiceNotExistsException, FacilityNotExistsException, PrivilegeException, InternalErrorException, DestinationAlreadyAssignedException, OwnerNotExistsException,
            ServiceExistsException, FacilityExistsException {
        log.debug("Gonna do @PostConstruct SETUP...");
        initJdbcTemplate();
        intiIt();

        // Prepare all destinations
        initDestinations();
    }

    @PreDestroy
    public void cleanIt() throws ServiceNotExistsException, FacilityNotExistsException, PrivilegeException, InternalErrorException {
        log.debug("Gonna do @PreDestroy cleansing...");

        // Cleanup all created destinations
        cleanUpDestinations();

        getJdbcTemplate().update("delete from owners where owners.id = ?", getTestOwner().getId());
        getJdbcTemplate().update("delete from services where services.id = ?", getTestService1().getId());
        getJdbcTemplate().update("delete from services where services.id = ?", getTestService2().getId());
        getJdbcTemplate().update("delete from services where services.id = ?", getTestService3().getId());
        getJdbcTemplate().update("delete from facilities where facilities.id = ?", getFacility1().getId());
        getJdbcTemplate().update("delete from facilities where facilities.id = ?", getFacility2().getId());
        getJdbcTemplate().update("delete from facilities where facilities.id = ?", getFacility3().getId());

    }

    @Before
    public void rememberState() {
        execServices = new HashSet<Integer>(getJdbcTemplate().queryForList("select id from exec_services", Integer.class));
        tasks = new HashSet<Integer>(getJdbcTemplate().queryForList("select id from tasks", Integer.class));
    }

    @After
    public void returnToRememberedState() throws InterruptedException {
        // TODO: Remove.
        //Let Threads complete:
        Thread.sleep(5000);

        Set<Integer> currentExecServices = new HashSet<Integer>(getJdbcTemplate().queryForList("select id from exec_services", Integer.class));
        Set<Integer> currentTasks = new HashSet<Integer>(getJdbcTemplate().queryForList("select id from tasks", Integer.class));
        // Difference
        currentExecServices.removeAll(execServices);
        currentTasks.removeAll(tasks);
        // Log
        log.debug("We are gonna delete TasksResults for Tasks:" + currentTasks.toString());
        for (Integer id : currentTasks) {
            getJdbcTemplate().update("delete from tasks_results where tasks_results.task_id = ?", id);
        }

        log.debug("We are gonna delete ExecServices dependencies for ExecServices:" + currentExecServices.toString());
        for (Integer id : currentExecServices) {
            getJdbcTemplate().update("delete from service_dependencies where service_dependencies.exec_service_id = ?", id);
        }

        log.debug("We are gonna delete Tasks:" + currentTasks.toString());
        for (Integer id : currentTasks) {
            getJdbcTemplate().update("delete from tasks where tasks.id = ?", id);
        }

        log.debug("We are gonna delete ExecServices:" + currentExecServices.toString());
        for (Integer id : currentExecServices) {
            getJdbcTemplate().update("delete from exec_services where exec_services.id = ?", id);
        }
    }

    //@Test
    public void testSimpleExecution() throws OwnerNotExistsException, ServiceExistsException, InternalErrorException, PrivilegeException, ServiceNotExistsException, InterruptedException {
        log.debug("testSimpleExecution: ...");

        // Setup
        // Test ExecService #1 (Parent:testService1)
        ExecService testExecService1 = new ExecService();
        testExecService1.setDefaultDelay(1);
        testExecService1.setDefaultRecurrence(1);
        testExecService1.setEnabled(true);
        testExecService1.setService(getTestService1());
        testExecService1.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
        testExecService1.setExecServiceType(ExecServiceType.SEND);
        testExecService1.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService1, getTestOwner()));

        // Test ExecService #2 (Parent:testService1)
        ExecService testExecService2 = new ExecService();
        testExecService2.setDefaultDelay(1);
        testExecService2.setDefaultRecurrence(1);
        testExecService2.setEnabled(true);
        testExecService2.setService(getTestService1());
        testExecService2.setScript(ClassLoader.getSystemResource("serviceGenerate.bash").getPath());
        testExecService2.setExecServiceType(ExecServiceType.GENERATE);
        testExecService2.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService2, getTestOwner()));

        // Setup
        // Test ExecService #3 (Parent:testService2)
        ExecService testExecService3 = new ExecService();
        testExecService3.setDefaultDelay(1);
        testExecService3.setDefaultRecurrence(1);
        testExecService3.setEnabled(true);
        testExecService3.setService(getTestService2());
        testExecService3.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
        testExecService3.setExecServiceType(ExecServiceType.SEND);
        testExecService3.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService3, getTestOwner()));

        // Setup
        // Test ExecService #4 (Parent:testService2)
        ExecService testExecService4 = new ExecService();
        testExecService4.setDefaultDelay(1);
        testExecService4.setDefaultRecurrence(1);
        testExecService4.setEnabled(true);
        testExecService4.setService(getTestService2());
        testExecService4.setScript(ClassLoader.getSystemResource("serviceGenerate.bash").getPath());
        testExecService4.setExecServiceType(ExecServiceType.GENERATE);
        testExecService4.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService4, getTestOwner()));

        // Setup
        // Test ExecService #5 (Parent:testService3)
        ExecService testExecService5 = new ExecService();
        testExecService5.setDefaultDelay(1);
        testExecService5.setDefaultRecurrence(1);
        testExecService5.setEnabled(true);
        testExecService5.setService(getTestService3());
        testExecService5.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
        testExecService5.setExecServiceType(ExecServiceType.SEND);
        testExecService5.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService5, getTestOwner()));

        // Setup
        // Test ExecService #6 (Parent:testService3)
        ExecService testExecService6 = new ExecService();
        testExecService6.setDefaultDelay(1);
        testExecService6.setDefaultRecurrence(1);
        testExecService6.setEnabled(true);
        testExecService6.setService(getTestService3());
        testExecService6.setScript(ClassLoader.getSystemResource("serviceGenerate.bash").getPath());
        testExecService6.setExecServiceType(ExecServiceType.GENERATE);
        testExecService6.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService6, getTestOwner()));

        // Test
        Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService2);
        Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService3, testExecService4);
        Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService5, testExecService6);
        Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService3);
        Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService5);

        assertEquals(3, Rpc.GeneralServiceManager.listExecServicesThisExecServiceDependsOn(engineManager.getRpcCaller(), testExecService1).size());

        taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

        taskExecutorEngine.beginExecuting();

        Task task = taskManager.getTask(testExecService1, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        // Yes NULL.
        assertNull("TestExecService1 should be sleeping in the schedulingPool till its dependencies are DONE, it should not be scheduled!", task);

        long started = System.currentTimeMillis();
        boolean itIsOK = true;
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itIsOK = true;
            task = taskManager.getTask(testExecService2, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("Task with ExecService2 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            // ExecService2 is of type GENERATE -> it goes from PROCESSING to DONE almost immediately in this test.
            if (!(TaskStatus.DONE.equals(task.getStatus()))) {
                itIsOK = false;
            }
            task = taskManager.getTask(testExecService4, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("Task with ExecService4 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            // ExecService4 is of type GENERATE -> it goes from PROCESSING to DONE almost immediately in this test.
            if (!(TaskStatus.DONE.equals(task.getStatus()))) {
                itIsOK = false;
            }
            task = taskManager.getTask(testExecService6, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("Task with ExecService6 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            // ExecService6 is of type GENERATE -> it goes from PROCESSING to DONE almost immediately in this test.
            if (!(TaskStatus.DONE.equals(task.getStatus()))) {
                itIsOK = false;
            }
            if (itIsOK) {
                log.debug("All GENERATE tasks in  DONE in " + (System.currentTimeMillis() - started) + " miliseconds.");
                break;
            }
        }
        if (!itIsOK) {
            fail("GENERATE Tasks did not make it from PLANNED to DONE in time :-( We waited:" + (System.currentTimeMillis() - started) + " miliseconds.");
        }

        taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

        task = taskManager.getTask(testExecService3, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        log.debug("Task with testExecService3:" + task);
        // Has to be scheduled.
        assertNotNull(task);
        assertEquals(task.getStatus(), TaskStatus.PLANNED);

        task = taskManager.getTask(testExecService5, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        log.debug("Task with testExecService5:" + task);
        // Has to be scheduled.
        assertNotNull(task);
        assertEquals(task.getStatus(), TaskStatus.PLANNED);

        taskExecutorEngine.beginExecuting();

        started = System.currentTimeMillis();
        itIsOK = true;
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itIsOK = true;
            task = taskManager.getTask(testExecService3, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("Task with ExecService3 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            if (!(TaskStatus.PROCESSING.equals(task.getStatus()))) {
                itIsOK = false;
            }
            task = taskManager.getTask(testExecService5, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("Task with ExecService5 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            if (!(TaskStatus.PROCESSING.equals(task.getStatus()))) {
                itIsOK = false;
            }
            if (itIsOK) {
                log.debug("Both ExecService3 and ExecService5 tasks in PROCESSING in " + (System.currentTimeMillis() - started) + " miliseconds.");
                break;
            }
        }
        if (!itIsOK) {
            fail("SNED Tasks ExecService3 and ExecService5 did not make it from PLANNED to PROCESSING in time :-( We waited:" + (System.currentTimeMillis() - started) + " miliseconds.");
        }

        // Let's switch SNED dependencies states to DONE
        task = taskManager.getTask(testExecService3, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        task.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

        task = taskManager.getTask(testExecService5, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        task.setStatus(TaskStatus.DONE);
        taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

        taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

        // Now, it should be propagated all right..
        task = taskManager.getTask(testExecService1, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        assertNotNull(task);
        assertEquals(TaskStatus.PLANNED, task.getStatus());

        taskExecutorEngine.beginExecuting();

        started = System.currentTimeMillis();
        itIsOK = true;
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itIsOK = true;
            task = taskManager.getTask(testExecService1, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("Task with ExecService1 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            if (!(TaskStatus.PROCESSING.equals(task.getStatus()))) {
                itIsOK = false;
            }
            if (itIsOK) {
                log.debug("ExecService1 task in PROCESSING in " + (System.currentTimeMillis() - started) + " miliseconds.");
                break;
            }
        }
        if (!itIsOK) {
            fail("SNED ExecService1 task did not make it from PLANNED to PROCESSING in time :-( We waited:" + (System.currentTimeMillis() - started) + " miliseconds.");
        }

        log.debug(taskManager.listAllTasks(Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))).toArray().toString());

        // Did we get all the TasksResults? (There are as many TasksResulsts as there are Destinations)
        List<TaskResult> taskResults = null;
        boolean itWentOk = false;
        started = System.currentTimeMillis();
        Task taskExecService1 = taskManager.getTask(testExecService1, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itWentOk = true;
            taskResults = taskResultDao.getTaskResultsByTask(taskExecService1.getId());
            log.debug("TASKRESULTS:" + taskResults.size());
            // Are there three of them?
            if (taskResults.size() != 3) {
                itWentOk = false;
            }
            if (itWentOk) {
                break;
            }
            Thread.sleep(100);
        }

        if (!itWentOk) {
            fail("ExecService1 does not have all its 3 TasksResults :-(");
        }
    }

    //@Test
    public void testTaskResults() throws OwnerNotExistsException, ServiceExistsException, InternalErrorException, PrivilegeException, InterruptedException {
        // Setup // Test ExecService #1 (Parent:testService1)
        ExecService testExecService1 = new ExecService();
        testExecService1.setDefaultDelay(1);
        testExecService1.setDefaultRecurrence(1);
        testExecService1.setEnabled(true);
        testExecService1.setService(getTestService1());
        testExecService1.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
        testExecService1.setExecServiceType(ExecServiceType.SEND);
        testExecService1.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService1, getTestOwner()));

        // Test ExecService #2 (Parent:testService1)
        ExecService testExecService2 = new ExecService();
        testExecService2.setDefaultDelay(1);
        testExecService2.setDefaultRecurrence(1);
        testExecService2.setEnabled(true);
        testExecService2.setService(getTestService1());
        testExecService2.setScript(ClassLoader.getSystemResource("serviceGenerate.bash").getPath());
        testExecService2.setExecServiceType(ExecServiceType.GENERATE);
        testExecService2.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService2, getTestOwner()));

        // Setup // Test ExecService #3 (Parent:testService2)
        ExecService testExecService3 = new ExecService();
        testExecService3.setDefaultDelay(1);
        testExecService3.setDefaultRecurrence(1);
        testExecService3.setEnabled(true);
        testExecService3.setService(getTestService2());
        testExecService3.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
        testExecService3.setExecServiceType(ExecServiceType.SEND);
        testExecService3.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService3, getTestOwner()));

        // Setup // Test ExecService #4 (Parent:testService2)
        ExecService testExecService4 = new ExecService();
        testExecService4.setDefaultDelay(1);
        testExecService4.setDefaultRecurrence(1);
        testExecService4.setEnabled(true);
        testExecService4.setService(getTestService2());
        testExecService4.setScript(ClassLoader.getSystemResource("serviceGenerate.bash").getPath());
        testExecService4.setExecServiceType(ExecServiceType.GENERATE);
        testExecService4.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService4, getTestOwner()));

        Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService2);
        Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService3, testExecService4);
        Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService3);

        taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());
        taskExecutorEngine.beginExecuting();

        Task task = null;
        long started = System.currentTimeMillis();
        boolean itIsOK = true;
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itIsOK = true;
            task = taskManager.getTask(testExecService2, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("Task with ExecService2 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            // ExecService2 is of type GENERATE -> it goes from PROCESSING to DONE almost immediately in this test.
            if (!(TaskStatus.DONE.equals(task.getStatus()))) {
                itIsOK = false;
            }
            task = taskManager.getTask(testExecService4, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("Task with ExecService4 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            // ExecService4 is of type GENERATE -> it goes from PROCESSING to DONE almost immediately in this test.
            if (!(TaskStatus.DONE.equals(task.getStatus()))) {
                itIsOK = false;
            }
            if (itIsOK) {
                log.debug("All GENERATE tasks in  DONE in " + (System.currentTimeMillis() - started) + " miliseconds.");
                break;
            }
        }
        if (!itIsOK) {
            fail("GENERATE Tasks did not make it from PLANNED to DONE in time :-( We waited:" + (System.currentTimeMillis() - started) + " miliseconds.");
        }

        taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());
        taskExecutorEngine.beginExecuting();

        /**
         * Gonna check whether there are 3 task results containing 3 destinations of ExecService3(SEND) and we have to verify that task with ExecService4(GENERATE) is DONE. If OK, we can switch ExecService3 from PROCESSING to DONE.
         */
        List<TaskResult> taskResults = null;
        boolean itWentOk = false;
        started = System.currentTimeMillis();
        Task taskExecService3 = taskManager.getTask(testExecService3, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itWentOk = true;
            if (taskResultDao == null) {
                fail("taskResultDao is NULL!!!");
            }
            taskResults = taskResultDao.getTaskResultsByTask(taskExecService3.getId());
            log.debug("TASKRESULTS:" + taskResults.size()); // Are there three of them?
            if (taskResults.size() != 3) {
                itWentOk = false;
            } else {
                boolean hasDestination4 = false;
                boolean hasDestination5 = false;
                boolean hasDestination6 = false;
                for (TaskResult taskResult : taskResults) { // Destination4 ?
                    if (taskResult.getDestinationId() == getDestination4().getId()) {
                        hasDestination4 = true;
                        log.debug("Result " + taskResult.getId() + " hasDestination4: TRUE");
                    } // Destination5 ?
                    if (taskResult.getDestinationId() == getDestination5().getId()) {
                        hasDestination5 = true;
                        log.debug("Result " + taskResult.getId() + " hasDestination5: TRUE");
                    } // Destination6 ?
                    if (taskResult.getDestinationId() == getDestination6().getId()) {
                        hasDestination6 = true;
                        log.debug("Result " + taskResult.getId() + " hasDestination6: TRUE");
                    }
                }
                log.debug("hasDestination6: " + hasDestination6 + ", hasDestination5: " + hasDestination5 + ", hasDestination4: " + hasDestination4);
                if (!(hasDestination4 && hasDestination5 && hasDestination6)) {
                    itWentOk = false;
                }
            }
            if (itWentOk) {
                break;
            }
            Thread.sleep(100);
        }
        log.debug("\tTASKRESULTS total:" + taskResults.size()); // How about ExecService 4 ?
        Thread.sleep(5000);
        task = taskManager.getTask(testExecService4, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        assertNotNull(task);
        TaskStatus stat4 = task.getStatus();
        // Test ExecService 4 (gen)
        // Well, it might look strange, but the status can be ERROR. e.g. if there are no scripts prepared for the test.
        if ((TaskStatus.DONE.equals(stat4) || TaskStatus.ERROR.equals(stat4)) && itWentOk) {
            // Switch testExecService3
            task = taskManager.getTask(testExecService3, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        } else {
            fail("ExecService4 does not have all its dependencies ready and it is not done itself (it was:" + stat4.toString() + ").");
        }

        taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());
        taskExecutorEngine.beginExecuting();

        /*
         * Gonna check whether there are 6 task results containing 6 destinations of ExecService3(SEND) + ExecService1(SEND) and we have to verify that tasks with ExecService4(GENERATE) and ExecService2(GENERATE) are DONE. If OK, we can switch ExecService1 from PROCESSING to DONE.
         */
        taskResults = null;
        itWentOk = false;
        started = System.currentTimeMillis();
        Task taskExecService1 = taskManager.getTask(testExecService1, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        while (System.currentTimeMillis() - started < TIME_OUT) {
            itWentOk = true;
            taskResults = taskResultDao.getTaskResultsByTask(taskExecService3.getId());
            taskResults.addAll(taskResultDao.getTaskResultsByTask(taskExecService1.getId()));
            log.debug("TASKRESULTS:" + taskResults.size()); // Are there three of them?
            if (taskResults.size() != 6) {
                itWentOk = false;
            } else {
                boolean hasDestination1 = false;
                boolean hasDestination2 = false;
                boolean hasDestination3 = false;
                boolean hasDestination4 = false;
                boolean hasDestination5 = false;
                boolean hasDestination6 = false;
                for (TaskResult taskResult : taskResults) { // Destination1 ?
                    if (taskResult.getDestinationId() == getDestination1().getId()) {
                        hasDestination1 = true;
                    } // Destination2?
                    if (taskResult.getDestinationId() == getDestination2().getId()) {
                        hasDestination2 = true;
                    } // Destination3 ?
                    if (taskResult.getDestinationId() == getDestination3().getId()) {
                        hasDestination3 = true;
                    } // Destination4 ?
                    if (taskResult.getDestinationId() == getDestination4().getId()) {
                        hasDestination4 = true;
                    } // Destination5 ?
                    if (taskResult.getDestinationId() == getDestination5().getId()) {
                        hasDestination5 = true;
                    } // Destination6 ?
                    if (taskResult.getDestinationId() == getDestination6().getId()) {
                        hasDestination6 = true;
                    }
                }
                if (!(hasDestination1 && hasDestination2 && hasDestination3 && hasDestination4 && hasDestination5 && hasDestination6)) {
                    itWentOk = false;
                }
            }
            if (itWentOk) {
                break;
            }
            Thread.sleep(100);
        }
        Thread.sleep(1000);
        log.debug("\tTASKRESULTS total:" + taskResults.size()); // How about ExecService 2 ?
        task = taskManager.getTask(testExecService2, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        assertNotNull(task);
        TaskStatus stat2 = task.getStatus();
        // Yes, ERROR is correct as well. It means that ExecutorEngineWorker could not find the defined scripts. Doesn't matter for testing.
        if ((TaskStatus.DONE.equals(stat2) || TaskStatus.ERROR.equals(stat2)) && itWentOk) {
            task = taskManager.getTask(testExecService1, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        } else {
            fail("ExecService1 does not have all its dependencies ready and it is not done itself (it was:" + stat2.toString() + ").");
        }
    }

    //@Test
    public void testGenerateSendSequence() throws OwnerNotExistsException, ServiceExistsException, InternalErrorException, PrivilegeException, InterruptedException {
        // Setup
        // Test ExecService #1 (Parent:testService1)
        ExecService testExecService1 = new ExecService();
        testExecService1.setDefaultDelay(1);
        testExecService1.setDefaultRecurrence(1);
        testExecService1.setEnabled(true);
        testExecService1.setService(getTestService1());
        testExecService1.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
        testExecService1.setExecServiceType(ExecServiceType.SEND);
        testExecService1.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService1, getTestOwner()));

        // Test ExecService #2 (Parent:testService1)
        ExecService testExecService2 = new ExecService();
        testExecService2.setDefaultDelay(1);
        testExecService2.setDefaultRecurrence(1);
        testExecService2.setEnabled(true);
        testExecService2.setService(getTestService1());
        testExecService2.setScript(ClassLoader.getSystemResource("serviceGenerateLong10s.bash").getPath());
        testExecService2.setExecServiceType(ExecServiceType.GENERATE);
        testExecService2.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService2, getTestOwner()));

        Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService2);

        log.debug("SlavX: Gonna call propagateService on execService ID "+testExecService1.getId()+" and facility "+getFacility1().getId()+".");
        taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());
        log.debug("SlavX: Gonna call taskExecutorEngine.beginExecuting (execService ID "+testExecService1.getId()+" and facility "+getFacility1().getId()+") should be PLANNED.");
        taskExecutorEngine.beginExecuting();

        Task task;// = taskManager.getTask(testExecService1, getFacility1());

        // Just in case so as taskExecutorEngine has a time to follow up...
        Thread.sleep(1000);

        long started = System.currentTimeMillis();
        // For at least 4 seconds, testExecService1 should NOT PLANNED and testExecService2 should be PROCESSING.
        while (System.currentTimeMillis() - started < 4000) {
            task = taskManager.getTask(testExecService1, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNull(task);

            task = taskManager.getTask(testExecService2, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("#bud62 Task with ExecService2 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            assertTrue("testExecService2 task should be PROCESSING by this time, bur was:" + task.getStatus().toString(), TaskStatus.PROCESSING.equals(task.getStatus()));
            Thread.sleep(300);
        }

        // Within the next 10 seconds, it shall be all OK...
        boolean itIsOK = true;
        while (System.currentTimeMillis() - started < 15000) {
            itIsOK = true;
            task = taskManager.getTask(testExecService1, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            if (task == null) {
                taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());
            } else {
                log.debug("#bud62 Task with ExecService1 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
                if (!TaskStatus.PROCESSING.equals(task.getStatus())) {
                    itIsOK = false;
                }
            }

            task = taskManager.getTask(testExecService2, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("#bud62 Task with ExecService2 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            if (!TaskStatus.DONE.equals(task.getStatus())) {
                itIsOK = false;
            }
            taskExecutorEngine.beginExecuting();

            if (itIsOK) {
                break;
            }
            Thread.sleep(500);
        }
        if (!itIsOK) {
            fail("Tasks did not make it from PLANNED to PROCESSING (DONE) in time :-( We waited:" + (System.currentTimeMillis() - started) + " miliseconds.");
        }
        Thread.sleep(5000);
    }
    
    @IfProfileValue(name="test-groups", values=("unit-tests-old"))
    @Test
    public void testGenerateError() throws OwnerNotExistsException, ServiceExistsException, InternalErrorException, PrivilegeException, InterruptedException {
        // Setup
        // Test ExecService #1 (Parent:testService1)
        ExecService testExecService1 = new ExecService();
        testExecService1.setDefaultDelay(1);
        testExecService1.setDefaultRecurrence(1);
        testExecService1.setEnabled(true);
        testExecService1.setService(getTestService1());
        testExecService1.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
        testExecService1.setExecServiceType(ExecServiceType.SEND);
        testExecService1.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService1, getTestOwner()));

        // Test ExecService #2 (Parent:testService1)
        ExecService testExecService2 = new ExecService();
        testExecService2.setDefaultDelay(1);
        testExecService2.setDefaultRecurrence(1);
        testExecService2.setEnabled(true);
        testExecService2.setService(getTestService1());
        testExecService2.setScript(ClassLoader.getSystemResource("serviceGenerateError.bash").getPath());
        testExecService2.setExecServiceType(ExecServiceType.GENERATE);
        testExecService2.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService2, getTestOwner()));

        Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService2);

        taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());
        taskExecutorEngine.beginExecuting();

        Task task;// = taskManager.getTask(testExecService1, getFacility1());

        // Just in case so as taskExecutorEngine has a time to follow up...
        Thread.sleep(1000);

        long started = System.currentTimeMillis();
        // For at least 4 seconds, testExecService1 should NOT PLANNED and testExecService2 should be PROCESSING.
        while (System.currentTimeMillis() - started < 4000) {
            task = taskManager.getTask(testExecService1, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNull(task);

            task = taskManager.getTask(testExecService2, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("#bud62 Task with ExecService2 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            assertTrue("testExecService2 task should be PROCESSING by this time, bur was:" + task.getStatus().toString(), TaskStatus.PROCESSING.equals(task.getStatus()));
            Thread.sleep(300);
        }

        // Within the next 10 seconds, it shall be all OK...
        boolean itIsOK = true;
        while (System.currentTimeMillis() - started < 15000) {
            itIsOK = true;
            task = taskManager.getTask(testExecService1, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            if (task == null) {
                log.debug("#bud62 Task with ExecService1 status: not in DB");
                taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());
            } else {
                itIsOK = false;
            }

            task = taskManager.getTask(testExecService2, getFacility1(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);
            log.debug("#bud62 Task with ExecService2 status:" + task.getStatus().toString() + ", ID:" + task.getExecServiceId());
            if (!TaskStatus.PLANNED.equals(task.getStatus())) {
                itIsOK = false;
            }

            if (itIsOK) {
                break;
            }
            Thread.sleep(500);
        }
        if (!itIsOK) {
            // TODO: Which of the aforementioned errors has failed?
            fail("ExecService1 should never appear in DB. ExecService2 should remain in PLANNED (switched from ERROR). We waited:" + (System.currentTimeMillis() - started) + " miliseconds.");
        }
        Thread.sleep(5000);
    }

}
