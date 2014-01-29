package cz.metacentrum.perun.taskslib.dao;

import org.junit.Test;

/*import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.FacilitiesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.taskslib.dao.TaskDao;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult.TaskResultStatus;
import cz.metacentrum.perun.taskslib.service.GeneralServiceManager;
*/
/**
 * @author Michal Karm Babacek
 **/
/*
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-tasks-lib-applicationcontext.xml", "classpath:perun-datasources.xml", "classpath:perun-beans.xml", "classpath:perun-transaction-manager.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "springTransactionManager")
@Transactional
*/
public class TaskResultDaoTest {
    
    @Test
    public void testDummy() {
        
    }
    
/*
    private final static Logger log = LoggerFactory.getLogger(TaskResultDaoTest.class);
    private int virtualEngineID = 1;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private TaskResultDao taskResultDao;
    private Task testTask1;
    private Task testTask2;
    private Destination destination1 = new Destination();
    private Destination destination2 = new Destination();
    private Destination destination3 = new Destination();
    private ExecService testExecService1;
    private ExecService testExecService2;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private Perun perun;
    @Autowired
    private ServicesManager servicesManager;
    @Autowired
    private FacilitiesManager facilitiesManager;
    @Autowired
    private GeneralServiceManager generalServiceManager;
    private JdbcTemplate jdbcTemplate;
    private PerunSession perunSession;
    private Owner testOwner;
    private Service testService1;
    private Service testService2;
    private Facility facility1;
    private Facility facility2;

    @Before
    public void setUp() throws OwnerNotExistsException, ServiceExistsException, InternalErrorException, PrivilegeException, FacilityExistsException, ServiceNotExistsException,
            FacilityNotExistsException, DestinationAlreadyAssignedException {
        try {
            perunSession = perun.getPerunSession(new PerunPrincipal("michalp@META"));
        } catch (InternalErrorException e) {
            log.error(e.toString());
        }

        jdbcTemplate = new JdbcTemplate(dataSource);

        // Test Owner
        int newOwnerId = Utils.getNewId(jdbcTemplate, "owners_id_seq");
        testOwner = new Owner();
        testOwner.setContact("Call me babe");
        testOwner.setName("Tester-" + Long.toHexString(System.currentTimeMillis()));
        testOwner.setId(newOwnerId);
        jdbcTemplate.update("insert into owners(id, name, contact) values (?,?,?)", newOwnerId, testOwner.getName(), testOwner.getContact());

        // Test Service #1
        testService1 = new Service();
        testService1.setName("Test service 1-" + Long.toHexString(System.currentTimeMillis()));

        // Test Service #2
        testService2 = new Service();
        testService2.setName("Test service 2-" + Long.toHexString(System.currentTimeMillis()));

        testService1.setId(servicesManager.createService(perunSession, testService1, testOwner).getId());
        testService2.setId(servicesManager.createService(perunSession, testService2, testOwner).getId());

        // Test Facility #1
        facility1 = new Facility();
        facility1.setName("Facility 1-" + Long.toHexString(System.currentTimeMillis()));
        facility1.setType(FacilitiesManager.CLUSTERTYPE);
        facility1 = facilitiesManager.createFacility(perunSession, facility1);

        // Test Facility #2
        facility2 = new Facility();
        facility2.setName("Facility 2-" + Long.toHexString(System.currentTimeMillis()));
        facility2.setType(FacilitiesManager.CLUSTERTYPE);
        facility2 = facilitiesManager.createFacility(perunSession, facility2);

        // Setup
        // Test ExecService #1 (Parent:testService1)
        testExecService1 = new ExecService();
        testExecService1.setDefaultDelay(1);
        testExecService1.setDefaultRecurrence(1);
        testExecService1.setEnabled(true);
        testExecService1.setService(testService1);
        testExecService1.setScript("serviceGenerate.bash");
        testExecService1.setExecServiceType(ExecServiceType.GENERATE);
        testExecService1.setId(generalServiceManager.insertExecService(perunSession, testExecService1, testOwner));

        // Test ExecService #2 (Parent:testService1)
        testExecService2 = new ExecService();
        testExecService2.setDefaultDelay(1);
        testExecService2.setDefaultRecurrence(1);
        testExecService2.setEnabled(true);
        testExecService2.setService(testService2);
        testExecService2.setScript("serviceSend.bash");
        testExecService2.setExecServiceType(ExecServiceType.SEND);
        testExecService2.setId(generalServiceManager.insertExecService(perunSession, testExecService2, testOwner));

        testTask1 = new Task();
        testTask1.setDelay(10);
        testTask1.setExecServiceId(testExecService1.getId());
        testTask1.setFacilityId(facility1.getId());
        testTask1.setRecurrence(10);
        testTask1.setSchedule(new Date(System.currentTimeMillis() + 120000));
        testTask1.setStatus(TaskStatus.PROCESSING);
        testTask1.setId(taskDao.scheduleNewTask(testTask1, virtualEngineID));

        testTask2 = new Task();
        testTask2.setDelay(10);
        testTask2.setExecServiceId(testExecService1.getId());
        testTask2.setFacilityId(facility1.getId());
        testTask2.setRecurrence(10);
        testTask2.setSchedule(new Date(System.currentTimeMillis() + 120000));
        testTask2.setStatus(TaskStatus.PROCESSING);
        testTask2.setId(taskDao.scheduleNewTask(testTask2, virtualEngineID));

        destination1.setDestination("Destination-1-" + Long.toHexString(System.currentTimeMillis()));
        destination2.setDestination("Destination-2-" + Long.toHexString(System.currentTimeMillis()));
        destination3.setDestination("Destination-3-" + Long.toHexString(System.currentTimeMillis()));
        destination1.setType("CLUSTER");
        destination2.setType("CLUSTER");
        destination3.setType("CLUSTER");
        destination1 = servicesManager.addDestination(perunSession, testService1, facility1, destination1);
        destination2 = servicesManager.addDestination(perunSession, testService2, facility1, destination2);
        destination3 = servicesManager.addDestination(perunSession, testService2, facility1, destination3);
        jdbcTemplate.update("delete from tasks_results");
    }

    @Test
    public void testInsertNewTaskResult() {
        try {
            TaskResult taskResult = new TaskResult();
            taskResult.setDestinationId(destination1.getId());
            taskResult.setErrorMessage("error message");
            taskResult.setReturnCode(0);
            taskResult.setStandardMessage("std message");
            taskResult.setStatus(TaskResultStatus.DONE);
            taskResult.setTaskId(testTask1.getId());
            taskResult.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult.setId(taskResultDao.insertNewTaskResult(taskResult, virtualEngineID));

            TaskResult returnedTaskResult1 = taskResultDao.getTaskResultById(taskResult.getId());
            TaskResult returnedTaskResult2 = taskResultDao.getTaskResultsByTask(testTask1.getId()).get(0);

            assertEquals(returnedTaskResult1.getDestinationId(), taskResult.getDestinationId());
            assertEquals(returnedTaskResult1.getErrorMessage(), taskResult.getErrorMessage());
            assertEquals(returnedTaskResult1.getId(), taskResult.getId());
            assertEquals(returnedTaskResult1.getReturnCode(), taskResult.getReturnCode());
            assertEquals(returnedTaskResult1.getStandardMessage(), taskResult.getStandardMessage());
            assertEquals(returnedTaskResult1.getStatus(), taskResult.getStatus());
            assertEquals(returnedTaskResult1.getTaskId(), taskResult.getTaskId());
            // assertEquals(returnedTaskResult1.getTimestamp(), taskResult.getTimestamp());
            assertEquals(returnedTaskResult2.getDestinationId(), taskResult.getDestinationId());
            assertEquals(returnedTaskResult2.getErrorMessage(), taskResult.getErrorMessage());
            assertEquals(returnedTaskResult2.getId(), taskResult.getId());
            assertEquals(returnedTaskResult2.getReturnCode(), taskResult.getReturnCode());
            assertEquals(returnedTaskResult2.getStandardMessage(), taskResult.getStandardMessage());
            assertEquals(returnedTaskResult2.getStatus(), taskResult.getStatus());
            assertEquals(returnedTaskResult2.getTaskId(), taskResult.getTaskId());
            // assertEquals(returnedTaskResult2.getTimestamp(), taskResult.getTimestamp());
        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    @Test
    public void testGetTaskResults() {
        try {

            TaskResult taskResult = new TaskResult();
            taskResult.setDestinationId(destination1.getId());
            taskResult.setErrorMessage("error message");
            taskResult.setReturnCode(0);
            taskResult.setStandardMessage("std message");
            taskResult.setStatus(TaskResultStatus.DONE);
            taskResult.setTaskId(testTask1.getId());
            taskResult.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult.setId(taskResultDao.insertNewTaskResult(taskResult, virtualEngineID));

            TaskResult taskResult1 = new TaskResult();
            taskResult1.setDestinationId(destination1.getId());
            taskResult1.setErrorMessage("error message");
            taskResult1.setReturnCode(0);
            taskResult1.setStandardMessage("std message");
            taskResult1.setStatus(TaskResultStatus.DONE);
            taskResult1.setTaskId(testTask1.getId());
            taskResult1.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult1.setId(taskResultDao.insertNewTaskResult(taskResult1, virtualEngineID));

            assertEquals(2, taskResultDao.getTaskResults().size());

        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    @Test
    public void testGetTaskResultsByTask() {
        try {

            TaskResult taskResult = new TaskResult();
            taskResult.setDestinationId(destination1.getId());
            taskResult.setErrorMessage("error message");
            taskResult.setReturnCode(0);
            taskResult.setStandardMessage("std message");
            taskResult.setStatus(TaskResultStatus.DONE);
            taskResult.setTaskId(testTask1.getId());
            taskResult.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult.setId(taskResultDao.insertNewTaskResult(taskResult, virtualEngineID));

            TaskResult taskResult1 = new TaskResult();
            taskResult1.setDestinationId(destination1.getId());
            taskResult1.setErrorMessage("error message");
            taskResult1.setReturnCode(0);
            taskResult1.setStandardMessage("std message");
            taskResult1.setStatus(TaskResultStatus.DONE);
            taskResult1.setTaskId(testTask1.getId());
            taskResult1.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult1.setId(taskResultDao.insertNewTaskResult(taskResult1, virtualEngineID));

            // Different one
            TaskResult taskResult2 = new TaskResult();
            taskResult2.setDestinationId(destination1.getId());
            taskResult2.setErrorMessage("error message");
            taskResult2.setReturnCode(0);
            taskResult2.setStandardMessage("std message");
            taskResult2.setStatus(TaskResultStatus.DONE);
            // Different TASK
            taskResult2.setTaskId(testTask2.getId());
            taskResult2.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult2.setId(taskResultDao.insertNewTaskResult(taskResult2, virtualEngineID));

            assertEquals(2, taskResultDao.getTaskResultsByTask(testTask1.getId()).size());
        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    @Test
    public void testClearByTask() {
        try {

            TaskResult taskResult = new TaskResult();
            taskResult.setDestinationId(destination1.getId());
            taskResult.setErrorMessage("error message");
            taskResult.setReturnCode(0);
            taskResult.setStandardMessage("std message");
            taskResult.setStatus(TaskResultStatus.DONE);
            taskResult.setTaskId(testTask1.getId());
            taskResult.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult.setId(taskResultDao.insertNewTaskResult(taskResult, virtualEngineID));

            TaskResult taskResult1 = new TaskResult();
            taskResult1.setDestinationId(destination1.getId());
            taskResult1.setErrorMessage("error message");
            taskResult1.setReturnCode(0);
            taskResult1.setStandardMessage("std message");
            taskResult1.setStatus(TaskResultStatus.DONE);
            taskResult1.setTaskId(testTask1.getId());
            taskResult1.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult1.setId(taskResultDao.insertNewTaskResult(taskResult1, virtualEngineID));

            // Different one
            TaskResult taskResult2 = new TaskResult();
            taskResult2.setDestinationId(destination1.getId());
            taskResult2.setErrorMessage("error message");
            taskResult2.setReturnCode(0);
            taskResult2.setStandardMessage("std message");
            taskResult2.setStatus(TaskResultStatus.DONE);
            // Different TASK
            taskResult2.setTaskId(testTask2.getId());
            taskResult2.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult2.setId(taskResultDao.insertNewTaskResult(taskResult2, virtualEngineID));

            assertEquals(3, taskResultDao.getTaskResults().size());

            taskResultDao.clearByTask(testTask2.getId());

            assertEquals(2, taskResultDao.getTaskResults().size());

        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    @Test
    public void testClearAll() {
        try {
            TaskResult taskResult = new TaskResult();
            taskResult.setDestinationId(destination1.getId());
            taskResult.setErrorMessage("error message");
            taskResult.setReturnCode(0);
            taskResult.setStandardMessage("std message");
            taskResult.setStatus(TaskResultStatus.DONE);
            taskResult.setTaskId(testTask1.getId());
            taskResult.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult.setId(taskResultDao.insertNewTaskResult(taskResult, virtualEngineID));

            TaskResult taskResult1 = new TaskResult();
            taskResult1.setDestinationId(destination1.getId());
            taskResult1.setErrorMessage("error message");
            taskResult1.setReturnCode(0);
            taskResult1.setStandardMessage("std message");
            taskResult1.setStatus(TaskResultStatus.DONE);
            taskResult1.setTaskId(testTask1.getId());
            taskResult1.setTimestamp(new Date(System.currentTimeMillis()));
            taskResult1.setId(taskResultDao.insertNewTaskResult(taskResult1, virtualEngineID));

            assertEquals(2, taskResultDao.getTaskResults().size());

            taskResultDao.clearAll();

            assertEquals(0, taskResultDao.getTaskResults().size());

        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    public TaskDao getTaskDao() {
        return taskDao;
    }

    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public TaskResultDao getTaskResultDao() {
        return taskResultDao;
    }

    public void setTaskResultDao(TaskResultDao taskResultDao) {
        this.taskResultDao = taskResultDao;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Perun getPerun() {
        return perun;
    }

    public void setPerun(Perun perun) {
        this.perun = perun;
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public FacilitiesManager getFacilitiesManager() {
        return facilitiesManager;
    }

    public void setFacilitiesManager(FacilitiesManager facilitiesManager) {
        this.facilitiesManager = facilitiesManager;
    }

    public GeneralServiceManager getGeneralServiceManager() {
        return generalServiceManager;
    }

    public void setGeneralServiceManager(GeneralServiceManager generalServiceManager) {
        this.generalServiceManager = generalServiceManager;
    }

    public PerunSession getPerunSession() {
        return perunSession;
    }

    public void setPerunSession(PerunSession perunSession) {
        this.perunSession = perunSession;
    }*/
}
