package cz.metacentrum.perun.taskslib.dao;

import org.junit.Test;
/*
	 import static org.junit.Assert.assertEquals;
	 import static org.junit.Assert.assertNotNull;
	 import static org.junit.Assert.assertTrue;

	 import java.util.Date;
	 import java.util.List;

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

	 import cz.metacentrum.perun.core.api.FacilitiesManager;
	 import cz.metacentrum.perun.core.api.Facility;
	 import cz.metacentrum.perun.core.api.Owner;
	 import cz.metacentrum.perun.core.api.Perun;
	 import cz.metacentrum.perun.core.api.PerunPrincipal;
	 import cz.metacentrum.perun.core.api.PerunSession;
	 import cz.metacentrum.perun.core.api.Service;
	 import cz.metacentrum.perun.core.api.ServicesManager;
	 import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
	 import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
	 import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
	 import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
	 import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
	 import cz.metacentrum.perun.core.impl.Utils;
	 import cz.metacentrum.perun.taskslib.dao.TaskDao;
	 import cz.metacentrum.perun.taskslib.model.ExecService;
	 import cz.metacentrum.perun.taskslib.model.Task;
	 import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
	 import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
	 import cz.metacentrum.perun.taskslib.service.GeneralServiceManager;*/

/**
 * @author Michal Karm Babacek
 **/
/*
	 @RunWith(SpringJUnit4ClassRunner.class)
	 @ContextConfiguration(locations = { "classpath:perun-tasks-lib.xml", "classpath:perun-core-jdbc.xml", "classpath:perun-core.xml", "classpath:perun-core-transaction-manager.xml" })
	 @TransactionConfiguration(defaultRollback = true, transactionManager = "springTransactionManager")
	 @Transactional
	 */
public class TaskDaoTest {


	@Test
	public void testDummy() {

	}
	/*
		 private final static Logger log = LoggerFactory.getLogger(TaskDaoTest.class);
		 private int virtualEngineID = 1;
		 @Autowired
		 private TaskDao taskDao;
		 private ExecService testExecService1;
		 private ExecService testExecService2;
		 @Autowired
		 private DataSource dataSource;
		 @Autowired
		 private Perun perun;
		 private JdbcTemplate jdbcTemplate;
		 private PerunSession perunSession = null;
		 private Owner testOwner;
		 private Service testService1;
		 private Service testService2;
		 private Facility facility1;
		 private Facility facility2;
		 @Autowired
		 private FacilitiesManager facilitiesManager;
		 @Autowired
		 private ServicesManager servicesManager;
		 @Autowired
		 private GeneralServiceManager generalServiceManager;

		 @Before
		 public void setUp() throws InternalErrorException, OwnerNotExistsException, ServiceExistsException, PrivilegeException, FacilityExistsException {
		 try {
		 if(perunSession == null) {
		 perunSession = perun.getPerunSession(new PerunPrincipal("michalp@META"));
		 }
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


jdbcTemplate.update("delete from tasks_results");
jdbcTemplate.update("delete from tasks");
		 }

		 @Test
		 public void testScheduleNewTask() throws Exception {
			 try {
				 log.debug("testScheduleNewTask: Scheduling a new Task...");
				 int records1 = Utils.getNewId(jdbcTemplate, "tasks_id_seq") - 1;

				 Task task = new Task();
				 task.setDelay(5);
				 task.setExecServiceId(testExecService1.getId());
				 task.setFacilityId(facility1.getId());
				 task.setRecurrence(5);
				 task.setSchedule(new Date(System.currentTimeMillis() + 120000));
				 task.setStatus(TaskStatus.PLANNED);
				 task.setId(taskDao.scheduleNewTask(task, virtualEngineID));

				 int records2 = Utils.getNewId(jdbcTemplate, "tasks_id_seq") - 1;
				 log.debug("records2:" + records2 + ", records1:" + records1);
				 assertTrue(records2 > records1);

			 } catch (Exception e) {
				 log.error(e.toString(), e);
				 throw e;
			 }
		 }

		 @Test
		 public void testGetTask() throws Exception {
			 try {
				 log.debug("testGetTask: Scheduling a new Task...");
				 Date date1 = new Date(System.currentTimeMillis() + 120000);

				 int delay = 5;
				 int recurrence = 5;
				 TaskStatus taskStatus = TaskStatus.PLANNED;

				 Task task = new Task();
				 task.setDelay(delay);
				 task.setExecServiceId(testExecService1.getId());
				 task.setFacilityId(facility1.getId());
				 task.setRecurrence(recurrence);
				 task.setSchedule(date1);
				 task.setStatus(taskStatus);
				 task.setId(taskDao.scheduleNewTask(task, virtualEngineID));

				 Task taskReceived = taskDao.getTaskById(task.getId());
				 assertNotNull(taskReceived);
				 assertEquals(delay, taskReceived.getDelay());
				 assertEquals(recurrence, taskReceived.getRecurrence());
				 // Note: These dates are not the same to milliseconds, let's don't
				 // bother with it.
				 log.debug("Comparing dates: " + taskReceived.getSchedule().toString() + ", expected:" + date1.toString());
				 assertEquals(date1.toString(), taskReceived.getSchedule().toString());
				 assertEquals(taskStatus, taskReceived.getStatus());
				 assertEquals(testExecService1.getId(), taskReceived.getExecServiceId());
				 assertEquals(facility1.getId(), taskReceived.getFacilityId());

				 Task taskReceivedBy = taskDao.getTask(testExecService1, facility1);
				 assertNotNull(taskReceivedBy);
				 assertEquals(delay, taskReceivedBy.getDelay());
				 assertEquals(recurrence, taskReceivedBy.getRecurrence());
				 // Note: These dates are not the same to milliseconds, let's don't
				 // bother with it.
				 log.debug("Comparing dates: " + taskReceivedBy.getSchedule().toString() + ", expected:" + date1.toString());
				 assertEquals(date1.toString(), taskReceivedBy.getSchedule().toString());
				 assertEquals(taskStatus, taskReceivedBy.getStatus());
				 assertEquals(testExecService1.getId(), taskReceived.getExecServiceId());
				 assertEquals(facility1.getId(), taskReceived.getFacilityId());

			 } catch (Exception e) {
				 log.error(e.toString(), e);
				 throw e;
			 }
		 }

		 @Test
		 public void testListAllTasksInState() throws Exception {
			 try {
				 log.debug("testListAllTasksInState: Scheduling a new Tasks...");
				 Date date1 = new Date(System.currentTimeMillis() + 120000);
				 Date date2 = new Date(System.currentTimeMillis() + 125000);

				 int delay1 = 5;
				 int recurrence1 = 5;

				 int delay2 = 6;
				 int recurrence2 = 6;

				 TaskStatus taskStatus1 = TaskStatus.DONE;
				 TaskStatus taskStatus2 = TaskStatus.ERROR;
				 TaskStatus taskStatus3 = TaskStatus.NONE;
				 TaskStatus taskStatus5 = TaskStatus.PLANNED;
				 TaskStatus taskStatus6 = TaskStatus.PROCESSING;
				 TaskStatus taskStatus7 = TaskStatus.PROCESSING;
				 TaskStatus taskStatus8 = TaskStatus.PROCESSING;

				 Task task1 = new Task();
				 task1.setDelay(delay1);
				 task1.setExecServiceId(testExecService1.getId());
				 task1.setFacilityId(facility1.getId());
				 task1.setRecurrence(recurrence1);
				 task1.setSchedule(date1);
				 task1.setStatus(taskStatus1);
				 task1.setId(taskDao.scheduleNewTask(task1, virtualEngineID));

				 Task task2 = new Task();
				 task2.setDelay(delay2);
				 task2.setExecServiceId(testExecService2.getId());
				 task2.setFacilityId(facility2.getId());
				 task2.setRecurrence(recurrence2);
				 task2.setSchedule(date2);
				 task2.setStatus(taskStatus2);
				 task2.setId(taskDao.scheduleNewTask(task2, virtualEngineID));

				 Task task3 = new Task();
				 task3.setDelay(delay2);
				 task3.setExecServiceId(testExecService2.getId());
				 task3.setFacilityId(facility2.getId());
				 task3.setRecurrence(recurrence2);
				 task3.setSchedule(date2);
				 task3.setStatus(taskStatus3);
				 task3.setId(taskDao.scheduleNewTask(task3, virtualEngineID));

				 Task task5 = new Task();
				 task5.setDelay(delay2);
				 task5.setExecServiceId(testExecService2.getId());
				 task5.setFacilityId(facility2.getId());
				 task5.setRecurrence(recurrence2);
				 task5.setSchedule(date2);
				 task5.setStatus(taskStatus5);
				 task5.setId(taskDao.scheduleNewTask(task5, virtualEngineID));

				 Task task6 = new Task();
				 task6.setDelay(delay2);
				 task6.setExecServiceId(testExecService2.getId());
				 task6.setFacilityId(facility2.getId());
				 task6.setRecurrence(recurrence2);
				 task6.setSchedule(date2);
				 task6.setStatus(taskStatus6);
				 task6.setId(taskDao.scheduleNewTask(task6, virtualEngineID));

				 Task task7 = new Task();
				 task7.setDelay(delay2);
				 task7.setExecServiceId(testExecService2.getId());
				 task7.setFacilityId(facility2.getId());
				 task7.setRecurrence(recurrence2);
				 task7.setSchedule(date2);
				 task7.setStatus(taskStatus7);
				 task7.setId(taskDao.scheduleNewTask(task7, virtualEngineID));

				 Task task8 = new Task();
				 task8.setDelay(delay2);
				 task8.setExecServiceId(testExecService2.getId());
				 task8.setFacilityId(facility2.getId());
				 task8.setRecurrence(recurrence2);
				 task8.setSchedule(date2);
				 task8.setStatus(taskStatus8);
				 task8.setId(taskDao.scheduleNewTask(task8, virtualEngineID));

				 List<Task> tasks = taskDao.listAllTasksInState(TaskStatus.DONE);
				 assertEquals(1, tasks.size());
				 tasks = taskDao.listAllTasksInState(TaskStatus.ERROR);
				 assertEquals(1, tasks.size());
				 tasks = taskDao.listAllTasksInState(TaskStatus.NONE);
				 assertEquals(1, tasks.size());
				 tasks = taskDao.listAllTasksInState(TaskStatus.PLANNED);
				 assertEquals(1, tasks.size());
				 tasks = taskDao.listAllTasksInState(TaskStatus.PROCESSING);
				 assertEquals(3, tasks.size());

			 } catch (Exception e) {
				 log.error(e.toString(), e);
				 throw e;
			 }
		 }

		 @Test
		 public void testListAllTasks() throws Exception {
			 try {
				 log.debug("testListAllTasks: Scheduling a new Task... #1");
				 Date date1 = new Date(System.currentTimeMillis() + 120000);
				 Date date2 = new Date(System.currentTimeMillis() + 125000);

				 int delay1 = 5;
				 int recurrence1 = 5;

				 int delay2 = 6;
				 int recurrence2 = 6;

				 TaskStatus taskStatus1 = TaskStatus.PLANNED;
				 TaskStatus taskStatus2 = TaskStatus.DONE;

				 Task task1 = new Task();
				 task1.setDelay(delay1);
				 task1.setExecServiceId(testExecService1.getId());
				 task1.setFacilityId(facility1.getId());
				 task1.setRecurrence(recurrence1);
				 task1.setSchedule(date1);
				 task1.setStatus(taskStatus1);
				 task1.setId(taskDao.scheduleNewTask(task1, virtualEngineID));

				 Task task2 = new Task();
				 task2.setDelay(delay2);
				 task2.setExecServiceId(testExecService2.getId());
				 task2.setFacilityId(facility2.getId());
				 task2.setRecurrence(recurrence2);
				 task2.setSchedule(date2);
				 task2.setStatus(taskStatus2);
				 task2.setId(taskDao.scheduleNewTask(task2, virtualEngineID));

				 List<Task> tasks = taskDao.listAllTasks();

				 assertEquals(2, tasks.size());

			 } catch (Exception e) {
				 log.error(e.toString(), e);
				 throw e;
			 }
		 }

		 @Test
		 public void testListTasksScheduledBetweenDates() throws Exception {
			 try {
				 log.debug("testListTasksScheduledBetweenDates: Scheduling a new Task...");
				 Date date1 = new Date(System.currentTimeMillis() + 100000);
				 Date date2 = new Date(System.currentTimeMillis() + 300000);
				 Date date3 = new Date(System.currentTimeMillis() + 600000);
				 Date date4 = new Date(System.currentTimeMillis() + 900000);

				 int delay1 = 5;
				 int recurrence1 = 5;
				 TaskStatus taskStatus1 = TaskStatus.PLANNED;

				 Task task1 = new Task();
				 task1.setDelay(delay1);
				 task1.setExecServiceId(testExecService1.getId());
				 task1.setFacilityId(facility1.getId());
				 task1.setRecurrence(recurrence1);
				 task1.setSchedule(date2);
				 task1.setStatus(taskStatus1);
				 task1.setId(taskDao.scheduleNewTask(task1, virtualEngineID));

				 assertEquals(task1, taskDao.listTasksScheduledBetweenDates(date1, date3).get(0));
				 assertEquals(task1, taskDao.listTasksScheduledBetweenDates(date1, date4).get(0));
				 assertEquals(task1, taskDao.listTasksScheduledBetweenDates(date2, date3).get(0));
				 assertEquals(task1, taskDao.listTasksScheduledBetweenDates(date2, date4).get(0));
				 assertEquals(0, taskDao.listTasksScheduledBetweenDates(date3, date4).size());
				 assertEquals(0, taskDao.listTasksScheduledBetweenDates(date4, date3).size());
				 assertEquals(0, taskDao.listTasksScheduledBetweenDates(date3, date1).size());
				 assertEquals(0, taskDao.listTasksScheduledBetweenDates(date4, date1).size());
				 assertEquals(0, taskDao.listTasksScheduledBetweenDates(date3, date2).size());
				 assertEquals(0, taskDao.listTasksScheduledBetweenDates(date4, date2).size());

			 } catch (Exception e) {
				 log.error(e.toString(), e);
				 throw e;
			 }
		 }

		 @Test
		 public void testUpdateTask() throws Exception {
			 try {
				 log.debug("testUpdateTask: Scheduling a new Task...");
				 Date date1 = new Date(System.currentTimeMillis() + 100000);
				 Date date2 = new Date(System.currentTimeMillis() + 300000);
				 Date date3 = new Date(System.currentTimeMillis() + 600000);

				 int delay1 = 5;
				 int recurrence1 = 5;
				 TaskStatus taskStatus1 = TaskStatus.PLANNED;
				 TaskStatus taskStatus2 = TaskStatus.DONE;

				 Task task1 = new Task();
				 task1.setDelay(delay1);
				 task1.setExecServiceId(testExecService1.getId());
				 task1.setFacilityId(facility1.getId());
				 task1.setRecurrence(recurrence1);
				 task1.setSchedule(date1);
				 task1.setStatus(taskStatus1);
				 task1.setId(taskDao.scheduleNewTask(task1, virtualEngineID));

				 task1.setStartTime(date2);
				 task1.setEndTime(date3);
				 task1.setStatus(taskStatus2);

				 taskDao.updateTask(task1);

				 assertEquals(date1.toString(), taskDao.getTaskById(task1.getId()).getSchedule().toString());
				 assertEquals(date2.toString(), taskDao.getTaskById(task1.getId()).getStartTime().toString());
				 assertEquals(date3.toString(), taskDao.getTaskById(task1.getId()).getEndTime().toString());
				 assertEquals(taskDao.getTaskById(task1.getId()).getStatus().toString(), taskStatus2.toString());

			 } catch (Exception e) {
				 log.error(e.toString(), e);
				 throw e;
			 }
		 }

		 @Test
		 public void testListTasksStartedBetweenDates() throws Exception {
			 try {
				 log.debug("testListTasksStartedBetweenDates: Scheduling a new Task...");
				 Date date1 = new Date(System.currentTimeMillis() + 100000);
				 Date date2 = new Date(System.currentTimeMillis() + 300000);
				 Date date3 = new Date(System.currentTimeMillis() + 600000);
				 Date date4 = new Date(System.currentTimeMillis() + 900000);

				 int delay1 = 5;
				 int recurrence1 = 5;
				 TaskStatus taskStatus1 = TaskStatus.PLANNED;

				 Task task1 = new Task();
				 task1.setDelay(delay1);
				 task1.setExecServiceId(testExecService1.getId());
				 task1.setFacilityId(facility1.getId());
				 task1.setRecurrence(recurrence1);
				 task1.setSchedule(date2);
				 task1.setStatus(taskStatus1);
				 task1.setId(taskDao.scheduleNewTask(task1, virtualEngineID));

				 task1.setStartTime(date2);

				 taskDao.updateTask(task1);

				 assertEquals(task1, taskDao.listTasksStartedBetweenDates(date1, date3).get(0));
				 assertEquals(task1, taskDao.listTasksStartedBetweenDates(date1, date4).get(0));
				 assertEquals(task1, taskDao.listTasksStartedBetweenDates(date2, date3).get(0));
				 assertEquals(task1, taskDao.listTasksStartedBetweenDates(date2, date4).get(0));
				 assertEquals(0, taskDao.listTasksStartedBetweenDates(date3, date4).size());
				 assertEquals(0, taskDao.listTasksStartedBetweenDates(date4, date3).size());
				 assertEquals(0, taskDao.listTasksStartedBetweenDates(date3, date1).size());
				 assertEquals(0, taskDao.listTasksStartedBetweenDates(date4, date1).size());
				 assertEquals(0, taskDao.listTasksStartedBetweenDates(date3, date2).size());
				 assertEquals(0, taskDao.listTasksStartedBetweenDates(date4, date2).size());

			 } catch (Exception e) {
				 log.error(e.toString(), e);
				 throw e;
			 }
		 }

		 @Test
		 public void testListTasksEndedBetweenDates() throws Exception {
			 try {
				 log.debug("testListTasksEndedBetweenDates: Scheduling a new Task...");
				 Date date1 = new Date(System.currentTimeMillis() + 100000);
				 Date date2 = new Date(System.currentTimeMillis() + 300000);
				 Date date3 = new Date(System.currentTimeMillis() + 600000);
				 Date date4 = new Date(System.currentTimeMillis() + 900000);

				 int delay1 = 5;
				 int recurrence1 = 5;
				 TaskStatus taskStatus1 = TaskStatus.PLANNED;

				 Task task1 = new Task();
				 task1.setDelay(delay1);
				 task1.setExecServiceId(testExecService1.getId());
				 task1.setFacilityId(facility1.getId());
				 task1.setRecurrence(recurrence1);
				 task1.setSchedule(date2);
				 task1.setStatus(taskStatus1);
				 task1.setId(taskDao.scheduleNewTask(task1, virtualEngineID));

				 task1.setEndTime(date2);

				 taskDao.updateTask(task1);

				 assertEquals(task1, taskDao.listTasksEndedBetweenDates(date1, date3).get(0));
				 assertEquals(task1, taskDao.listTasksEndedBetweenDates(date1, date4).get(0));
				 assertEquals(task1, taskDao.listTasksEndedBetweenDates(date2, date3).get(0));
				 assertEquals(task1, taskDao.listTasksEndedBetweenDates(date2, date4).get(0));
				 assertEquals(0, taskDao.listTasksEndedBetweenDates(date3, date4).size());
				 assertEquals(0, taskDao.listTasksEndedBetweenDates(date4, date3).size());
				 assertEquals(0, taskDao.listTasksEndedBetweenDates(date3, date1).size());
				 assertEquals(0, taskDao.listTasksEndedBetweenDates(date4, date1).size());
				 assertEquals(0, taskDao.listTasksEndedBetweenDates(date3, date2).size());
				 assertEquals(0, taskDao.listTasksEndedBetweenDates(date4, date2).size());

			 } catch (Exception e) {
				 log.error(e.toString(), e);
				 throw e;
			 }
		 }

		 @Test
		 public void testIsThereSuchTask() throws Exception {
			 try {
				 log.debug("testListTasksEndedBetweenDates: Scheduling a new Task...");
				 Date date1 = new Date(System.currentTimeMillis() + 100000);

				 int delay1 = 5;
				 int recurrence1 = 5;
				 TaskStatus taskStatus1 = TaskStatus.PLANNED;

				 Task task1 = new Task();
				 task1.setDelay(delay1);
				 task1.setExecServiceId(testExecService1.getId());
				 task1.setFacilityId(facility1.getId());
				 task1.setRecurrence(recurrence1);
				 task1.setSchedule(date1);
				 task1.setStatus(taskStatus1);
				 task1.setId(taskDao.scheduleNewTask(task1, virtualEngineID));

				 ExecService execService = new ExecService();
				 execService.setId(testExecService1.getId());
				 Facility facility = new Facility();
				 facility.setId(facility1.getId());

				 assertTrue(taskDao.isThereSuchTask(execService, facility));

				 execService.setId(testExecService1.getId() + 1);
				 facility.setId(facility1.getId() + 1);

				 assertTrue(!taskDao.isThereSuchTask(execService, facility));

			 } catch (Exception e) {
				 log.error("HELL:" + e.toString(), e);
				 throw e;
			 }
		 }

		 @Test
		 public void testRemoveTask() throws Exception {
			 try {
				 log.debug("testRemoveTask: Scheduling a new Task...");
				 Date date1 = new Date(System.currentTimeMillis() + 100000);

				 int delay1 = 5;
				 int recurrence1 = 5;
				 TaskStatus taskStatus1 = TaskStatus.PLANNED;

				 Task task1 = new Task();
				 task1.setDelay(delay1);
				 task1.setExecServiceId(testExecService1.getId());
				 task1.setFacilityId(facility1.getId());
				 task1.setRecurrence(recurrence1);
				 task1.setSchedule(date1);
				 task1.setStatus(taskStatus1);
				 task1.setId(taskDao.scheduleNewTask(task1, virtualEngineID));

				 ExecService execService = new ExecService();
				 execService.setId(testExecService1.getId());
				 Facility facility = new Facility();
				 facility.setId(facility1.getId());

				 assertTrue(taskDao.isThereSuchTask(execService, facility));

				 execService.setId(testExecService1.getId() + 1);
				 facility.setId(facility1.getId() + 1);

				 assertTrue(!taskDao.isThereSuchTask(execService, facility));

				 execService.setId(testExecService1.getId());
				 facility.setId(facility1.getId());

				 taskDao.removeTask(execService, facility);

				 assertTrue(!taskDao.isThereSuchTask(execService, facility));

				 task1.setId(taskDao.scheduleNewTask(task1, virtualEngineID));

				 assertTrue(taskDao.isThereSuchTask(execService, facility));

				 taskDao.removeTask(task1.getId());

				 assertTrue(!taskDao.isThereSuchTask(execService, facility));

			 } catch (Exception e) {
				 log.error(e.toString(), e);
				 throw e;
			 }
		 }

		 public TaskDao getTaskDao() {
			 return taskDao;
		 }

		 public void setTaskDao(TaskDao taskDao) {
			 this.taskDao = taskDao;
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

		 public FacilitiesManager getFacilitiesManager() {
			 return facilitiesManager;
		 }

		 public void setFacilitiesManager(FacilitiesManager facilitiesManager) {
			 this.facilitiesManager = facilitiesManager;
		 }

		 public ServicesManager getServicesManager() {
			 return servicesManager;
		 }

		 public void setServicesManager(ServicesManager servicesManager) {
			 this.servicesManager = servicesManager;
		 }

		 public GeneralServiceManager getGeneralServiceManager() {
			 return generalServiceManager;
		 }

		 public void setGeneralServiceManager(GeneralServiceManager generalServiceManager) {
			 this.generalServiceManager = generalServiceManager;
		 }
		 */
}
