package cz.metacentrum.perun.engine.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Properties;

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

import cz.metacentrum.perun.engine.BaseTest;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;
import cz.metacentrum.perun.engine.service.EngineManager;
import cz.metacentrum.perun.rpclib.Rpc;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.service.TaskManager;

/**
 * @author Michal Karm Babacek
 */
@TransactionConfiguration(defaultRollback = false, transactionManager = "transactionManagerPerunEngine")
@Transactional(propagation = Propagation.NEVER)
public class TaskSchedulerTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(TaskSchedulerTest.class);

    @Autowired
    private EngineManager engineManager;
    @Autowired
    private SchedulingPool schedulingPool;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private Properties propertiesBean;

    // Time out for threads to complete (milliseconds)
    private boolean inited = false;

    @Before
    public void setUp() throws Exception {
        if (!inited) {
            initJdbcTemplate();
            //cleanAll();
            intiIt();
            inited = true;
        }
        //cleanUp();
    }

    @After
    public void cleanPool() {
        schedulingPool.emptyPool();
    }

    /**
     * @see https://projekty.ics.muni.cz/perunv3/trac/wiki/PerunEngineDispatcherController#Singleservice
     */
    @IfProfileValue(name="test-groups", values=("unit-tests-old"))
    @Test
    public void testSingleService() {
        try {
            log.debug("testPropagateService: ...");

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

            // Test
            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            Task task = taskManager.getTask(testExecService1, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);

        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    /**
     * @see https://projekty.ics.muni.cz/perunv3/trac/wiki/PerunEngineDispatcherController#Basicdependency
     */
    @IfProfileValue(name="test-groups", values=("unit-tests-old"))
    @Test
    public void testBasicDependencyService() {
        try {
            log.debug("testBasicDependencyService: ...");

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

            // Test
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService2);

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            //GENERATE should be there planned
            Task task = taskManager.getTask(testExecService2, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("testBasicDependencyService: Task with testExecService2:" + task);
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.PLANNED);

            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            task = taskManager.getTask(testExecService1, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("testBasicDependencyService: Task with testExecService1:" + task);
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.PLANNED);
        } catch (Exception e) {
            log.error(e.toString(), e);
            fail(e.toString());
        }
    }

    /**
     * @see https://projekty.ics.muni.cz/perunv3/trac/wiki/PerunEngineDispatcherController#Onesendonlydependency
     */
    //evil
    @IfProfileValue(name="test-groups", values=("unit-tests-old"))
    @Test
    public void testOneSendOnlyDependency() {
        try {
            log.debug("testOneSendOnlyDependency: ...");

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
            testExecService3.setService(getTestService1());
            testExecService3.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
            testExecService3.setExecServiceType(ExecServiceType.SEND);
            testExecService3.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService3, getTestOwner()));

            // Test
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService2);
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService3);

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            Task task = taskManager.getTask(testExecService1, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("testBasicDependencyService: Task with testExecService1:" + task);
            // Yes NULL. TestExecService1 should be sleeping in the schedulingPool till its dependencies are DONE...
            assertNull(task);
            task = taskManager.getTask(testExecService2, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("testBasicDependencyService: Task with testExecService2:" + task);
            assertNotNull(task);
            task = taskManager.getTask(testExecService3, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("testBasicDependencyService: Task with testExecService3:" + task);
            assertNotNull(task);

            // Let's switch dependencies states to DONE
            task = taskManager.getTask(testExecService2, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task = taskManager.getTask(testExecService3, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            // Now, it should be propagated all right..
            task = taskManager.getTask(testExecService1, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);

            log.debug(taskManager.listAllTasks(Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))).toArray().toString());
        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    /**
     * @see https://projekty.ics.muni.cz/perunv3/trac/wiki/PerunEngineDispatcherController#Onefulldependency
     */
    //evil
    @IfProfileValue(name="test-groups", values=("unit-tests-old"))
    @Test
    public void testOneFullDependency() {
        try {
            log.debug("...");

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
            testExecService3.setService(getTestService1());
            testExecService3.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
            testExecService3.setExecServiceType(ExecServiceType.SEND);
            testExecService3.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService3, getTestOwner()));

            // Setup
            // Test ExecService #4 (Parent:testService2)
            ExecService testExecService4 = new ExecService();
            testExecService4.setDefaultDelay(1);
            testExecService4.setDefaultRecurrence(1);
            testExecService4.setEnabled(true);
            testExecService4.setService(getTestService1());
            testExecService4.setScript(ClassLoader.getSystemResource("serviceGenerate.bash").getPath());
            testExecService4.setExecServiceType(ExecServiceType.GENERATE);
            testExecService4.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService4, getTestOwner()));

            // Test
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService2);
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService3, testExecService4);
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService3);

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            Task task = taskManager.getTask(testExecService1, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService1:" + task);
            // Yes NULL. TestExecService1 should be sleeping in the schedulingPool till its dependencies are DONE...
            assertNull(task);

            task = taskManager.getTask(testExecService2, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService2:" + task);
            // Has to be scheduled.
            assertNotNull(task);

            task = taskManager.getTask(testExecService3, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService1:" + task);
            // Yes NULL. TestExecService3 should be sleeping in the schedulingPool till its dependencies are DONE...
            assertNull(task);

            task = taskManager.getTask(testExecService4, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService4:" + task);
            // Has to be scheduled.
            assertNotNull(task);

            //Switch GENERATE services to DONE
            task = taskManager.getTask(testExecService2, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

            task = taskManager.getTask(testExecService4, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            task = taskManager.getTask(testExecService3, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService3:" + task);
            // Has to be scheduled.
            assertNotNull(task);

            // Let's switch SEND dependency state to DONE
            task = taskManager.getTask(testExecService3, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            // Now, it should be propagated all right..
            task = taskManager.getTask(testExecService1, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            assertNotNull(task);

            log.debug(taskManager.listAllTasks(Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))).toArray().toString());

        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    /**
     * @see https://projekty.ics.muni.cz/perunv3/trac/wiki/PerunEngineDispatcherController#Multipledependencies
     */
    @IfProfileValue(name="test-groups", values=("unit-tests-old"))
    @Test
    public void testMultipleDependencies() {
        try {
            log.debug("testMultipleDependencies: ...");

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
            testExecService3.setService(getTestService1());
            testExecService3.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
            testExecService3.setExecServiceType(ExecServiceType.SEND);
            testExecService3.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService3, getTestOwner()));

            // Setup
            // Test ExecService #4 (Parent:testService2)
            ExecService testExecService4 = new ExecService();
            testExecService4.setDefaultDelay(1);
            testExecService4.setDefaultRecurrence(1);
            testExecService4.setEnabled(true);
            testExecService4.setService(getTestService1());
            testExecService4.setScript(ClassLoader.getSystemResource("serviceGenerate.bash").getPath());
            testExecService4.setExecServiceType(ExecServiceType.GENERATE);
            testExecService4.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService4, getTestOwner()));

            // Setup
            // Test ExecService #5 (Parent:testService3)
            ExecService testExecService5 = new ExecService();
            testExecService5.setDefaultDelay(1);
            testExecService5.setDefaultRecurrence(1);
            testExecService5.setEnabled(true);
            testExecService5.setService(getTestService1());
            testExecService5.setScript(ClassLoader.getSystemResource("serviceSend.bash").getPath());
            testExecService5.setExecServiceType(ExecServiceType.SEND);
            testExecService5.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService5, getTestOwner()));

            // Setup
            // Test ExecService #6 (Parent:testService6)
            ExecService testExecService6 = new ExecService();
            testExecService6.setDefaultDelay(1);
            testExecService6.setDefaultRecurrence(1);
            testExecService6.setEnabled(true);
            testExecService6.setService(getTestService1());
            testExecService6.setScript(ClassLoader.getSystemResource("serviceGenerate.bash").getPath());
            testExecService6.setExecServiceType(ExecServiceType.GENERATE);
            testExecService6.setId(Rpc.GeneralServiceManager.insertExecService(engineManager.getRpcCaller(), testExecService6, getTestOwner()));

            // Test
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService2);
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService3, testExecService4);
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService5, testExecService6);
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService3);
            Rpc.GeneralServiceManager.createDependency(engineManager.getRpcCaller(), testExecService1, testExecService5);

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            Task task = taskManager.getTask(testExecService1, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService1:" + task);
            // Yes NULL. TestExecService1 should be sleeping in the schedulingPool till its dependencies are DONE...
            assertNull(task);

            task = taskManager.getTask(testExecService3, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService3:" + task);
            // Yes NULL. TestExecService3 should be sleeping in the schedulingPool till its dependencies are DONE...
            assertNull(task);

            task = taskManager.getTask(testExecService5, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService5:" + task);
            // Yes NULL. TestExecService5 should be sleeping in the schedulingPool till its dependencies are DONE...
            assertNull(task);

            //GENERATE services should be scheduled
            task = taskManager.getTask(testExecService2, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService2:" + task);
            // Has to be scheduled.
            assertNotNull(task);

            task = taskManager.getTask(testExecService4, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService4:" + task);
            // Has to be scheduled.
            assertNotNull(task);

            task = taskManager.getTask(testExecService6, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService6:" + task);
            // Has to be scheduled.
            assertNotNull(task);

            // Let's switch testExecService6 GENERATE state to DONE
            task = taskManager.getTask(testExecService6, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            task = taskManager.getTask(testExecService1, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService1:" + task);
            // Yes NULL. TestExecService1 should be sleeping in the schedulingPool till its dependencies are DONE...
            assertNull(task);

            task = taskManager.getTask(testExecService3, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService3:" + task);
            // Yes NULL. TestExecService3 should be sleeping in the schedulingPool till its dependencies are DONE...
            assertNull(task);

            task = taskManager.getTask(testExecService5, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService5:" + task);
            // Has to be scheduled.
            assertNotNull(task);

            //GENERATE services should be scheduled
            task = taskManager.getTask(testExecService2, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService2:" + task);
            // Has to be scheduled.
            assertNotNull(task);

            task = taskManager.getTask(testExecService4, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService4:" + task);
            // Has to be scheduled.
            assertNotNull(task);

            // Let's switch the rest of GENERATE services to DONE
            task = taskManager.getTask(testExecService2, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

            task = taskManager.getTask(testExecService4, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            task = taskManager.getTask(testExecService1, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService1:" + task);
            // Yes NULL. TestExecService1 should be sleeping in the schedulingPool till its dependencies are DONE...
            assertNull(task);

            task = taskManager.getTask(testExecService2, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService2:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.DONE);

            task = taskManager.getTask(testExecService3, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService3:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.PLANNED);

            task = taskManager.getTask(testExecService4, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService4:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.DONE);

            task = taskManager.getTask(testExecService5, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService5:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.PLANNED);

            task = taskManager.getTask(testExecService6, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService6:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.DONE);

            // Let's switch the rest of services to DONE
            task = taskManager.getTask(testExecService3, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

            task = taskManager.getTask(testExecService5, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            task.setStatus(TaskStatus.DONE);
            taskManager.updateTask(task,Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));

            taskScheduler.propagateService(testExecService1, new Date(System.currentTimeMillis()), getFacility1());

            task = taskManager.getTask(testExecService1, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService1:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.PLANNED);

            task = taskManager.getTask(testExecService2, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService2:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.DONE);

            task = taskManager.getTask(testExecService3, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService3:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.DONE);

            task = taskManager.getTask(testExecService4, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService4:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.DONE);

            task = taskManager.getTask(testExecService5, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService5:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.DONE);

            task = taskManager.getTask(testExecService6, getFacility1(),Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
            log.debug("Task with testExecService6:" + task);
            // Has to be scheduled.
            assertNotNull(task);
            assertEquals(task.getStatus(), TaskStatus.DONE);

            log.debug(taskManager.listAllTasks(Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))).toArray().toString());

        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    public EngineManager getEngineManager() {
        return engineManager;
    }

    public void setEngineManager(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    public SchedulingPool getSchedulingPool() {
        return schedulingPool;
    }

    public void setSchedulingPool(SchedulingPool schedulingPool) {
        this.schedulingPool = schedulingPool;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public Properties getPropertiesBean() {
        return propertiesBean;
    }

    public void setPropertiesBean(Properties propertiesBean) {
        this.propertiesBean = propertiesBean;
    }

    public boolean isInited() {
        return inited;
    }

    public void setInited(boolean inited) {
        this.inited = inited;
    }

}
