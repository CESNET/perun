package cz.metacentrum.perun.core.bl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.JapaneseDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.model.TaskResult.TaskResultStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
@ContextConfiguration(locations = { "classpath:perun-base.xml", "classpath:perun-core.xml" })
})
@Transactional(transactionManager = "springTransactionManager")
public class TasksManagerBlImplTest {

	@Autowired
	private ServicesManager servicesManager;
	@Autowired
	private DataSource dataSource;
	@Autowired
	private Perun perun;
	private JdbcPerunTemplate jdbcTemplate;
	private PerunSession perunSession;
	private Service testService1;
	private int testDestinationId1, testDestinationId2;
	private int testFacilityId1, testFacilityId2;
	private Facility facility1, facility2;
	private Destination destination1, destination2;
	private Task task1, task2;
	private int task1Id, task2Id;
	private TaskResult result1, result2;
	private int result1Id, result2Id;
	
	@Before
	public void setUp() throws Exception
	{
		perunSession = perun.getPerunSession(
				new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient());

		jdbcTemplate = new JdbcPerunTemplate(dataSource);

		facility1 = new Facility();
		facility2 = new Facility();


		// Test Service #1
		testService1 = new Service();
		testService1.setName("Test_service_1_" + Long.toHexString(System.currentTimeMillis()));
		testService1.setDelay(1);
		testService1.setRecurrence(1);
		testService1.setEnabled(true);
		testService1.setScript("/hellish/test/script");

		testService1.setId(servicesManager.createService(perunSession, testService1).getId());

		// 
		// Testing Destination #1
		testDestinationId1 = Utils.getNewId(jdbcTemplate, "destinations_id_seq");
		jdbcTemplate.update("insert into destinations(id, destination, type) values (?,?,'host')", testDestinationId1, "test.destination." + testDestinationId1);

		// Testing Destination #2
		testDestinationId2 = Utils.getNewId(jdbcTemplate, "destinations_id_seq");
		jdbcTemplate.update("insert into destinations(id, destination, type) values (?,?,'host')", testDestinationId2, "test.destination." + testDestinationId2);

		// Testing Facility #1
		testFacilityId1 = Utils.getNewId(jdbcTemplate, "facilities_id_seq");
		jdbcTemplate.update("insert into facilities(id, name) values (?,?)", testFacilityId1, "Cluster_" + testFacilityId1);
		facility1.setId(testFacilityId1);

		// Testing Facility #2
		testFacilityId2 = Utils.getNewId(jdbcTemplate, "facilities_id_seq");
		jdbcTemplate.update("insert into facilities(id, name) values (?,?)", testFacilityId2, "Cluster_" + testFacilityId2);
		facility2.setId(testFacilityId2);

		// facility_service_destinations
		destination1 = ((PerunBl)perun).getServicesManagerBl().getDestinationById(perunSession, testDestinationId1);
		((PerunBl)perun).getServicesManagerBl().addDestination(perunSession, testService1, facility1, destination1);
		// facility_service_destinations
		destination2 = ((PerunBl)perun).getServicesManagerBl().getDestinationById(perunSession, testDestinationId2);
		((PerunBl)perun).getServicesManagerBl().addDestination(perunSession, testService1, facility2, destination2);

		// tasks
		task1 = new Task();
		task1.setFacility(facility1);
		task1.setService(testService1);
		task1.setSchedule(0L);
		task1.setStatus(TaskStatus.DONE);
		List<Destination> destinationsList = new ArrayList<>();
		destinationsList.add(destination1);
		task1.setDestinations(destinationsList);
		task1Id = ((PerunBl)perun).getTasksManagerBl().insertTask(perunSession, task1);
		task1.setId(task1Id);
		
		// tasks
		task2 = new Task();
		task2.setFacility(facility2);
		task2.setService(testService1);
		task2.setSchedule(0L);
		task2.setStatus(TaskStatus.DONE);
		destinationsList = new ArrayList<>();
		destinationsList.add(destination2);
		task2.setDestinations(destinationsList);
		task2Id = ((PerunBl)perun).getTasksManagerBl().insertTask(perunSession, task2);
		task2.setId(task2Id);

		// task results
		result1 = new TaskResult();
		result1.setDestination(destination1);
		result1.setDestinationId(testDestinationId1);
		result1.setService(testService1);
		result1.setTaskId(task1Id);
		result1.setStatus(TaskResultStatus.DONE);
		result1.setTimestamp(new Date());
		result1Id = ((PerunBl)perun).getTasksManagerBl().insertNewTaskResult(perunSession, result1);
		result1.setId(result1Id);
		
		// task results
		result2 = new TaskResult();
		result2.setDestination(destination1);
		result2.setDestinationId(testDestinationId2);
		result2.setService(testService1);
		result2.setTaskId(task1Id);
		result2.setStatus(TaskResultStatus.DONE);
		result2.setTimestamp(Date.from(
				LocalDate
				.now()
				.minusDays(7)
				.atStartOfDay(ZoneId.systemDefault())
				.toInstant()));
		result2Id = ((PerunBl)perun).getTasksManagerBl().insertNewTaskResult(perunSession, result2);
		result2.setId(result2Id);
	}
	
	@Test
	public void testCountTasks() {
		 assertEquals(2, ((PerunBl)perun).getTasksManagerBl().countTasks());
	}

	@Test
	public void testDeleteAllTaskResults() {
		((PerunBl)perun).getTasksManagerBl().deleteAllTaskResults(perunSession);
		assertEquals(0,((PerunBl)perun).getTasksManagerBl().getTaskResults(perunSession).size());
	}
	
	@Test
	public void testDeleteOldTaskResults() {
		assertEquals(1, ((PerunBl)perun).getTasksManagerBl().deleteOldTaskResults(perunSession, 1));
		assertEquals(1,((PerunBl)perun).getTasksManagerBl().getTaskResults(perunSession).size());
	}

	@Test 
	public void testDeleteTask() {
		// TODO
	}
	
	@Test 
	public void testDeleteTaskResultsById() {
		// TODO
	}
	
	@Test 
	public void testDeleteTaskResults_Task() {
		// TODO
	}
	
	@Test 
	public void testDeleteTaskResults_TaskDestination() {
		// TODO
	}
	
	@Test 
	public void testGetAllFacilitiesStates() {
		// TODO
	}
	
	@Test 
	public void testGetAllFacilitiesStatesForVo() {
		// TODO
	}
	
	@Test 
	public void testGetFacilityServicesState() {
		// TODO
	}
	
	@Test 
	public void testGetFacilityState() {
		// TODO
	}
	
	@Test 
	public void testGetResourcesState() {
		// TODO
	}
	
	@Test 
	public void testGetTask() {
		// TODO
	}
	
	@Test 
	public void testGetTaskById() {
		// TODO
	}
	
	@Test 
	public void testGetTaskResultById() {
		// TODO
	}
	
	@Test 
	public void testGetTaskResults() {
		// TODO
	}
	
	@Test 
	public void testGetTaskResultsByTask() {
		// TODO
	}
	
	@Test 
	public void testGetTaskResultsByTaskAndDestination() {
		// TODO
	}
	
	@Test 
	public void testGetTaskResultsByTaskOnlyNewest() {
		// TODO
	}
	
	@Test 
	public void testGetTaskResultsByDestinations() {
		// TODO
	}
	
	@Test 
	public void testInsertNewTaskResult() {
		// TODO
	}
	
	@Test 
	public void testInsertTask() {
		// TODO
	}
	
	@Test 
	public void testIsThereSuchTask() {
		// TODO
	}
	
	@Test 
	public void testListAllTasks() {
		// TODO
	}
	
	@Test 
	public void testListAllTasksForFacility() {
		// TODO
	}
	
	@Test
	public void testListAllTasksForService()
	{
		System.out.println("TasksManagerBlImplTest.testListAllTasksForService");
		
		List<Task> tasks = ((PerunBl)perun).getTasksManagerBl().listAllTasksForService(perunSession, testService1.getId());
		
		assertNotNull(tasks);
		assertEquals(tasks.size(), 2);
	}
	
	@Test 
	public void testListAllTasksInState() {
		// TODO
	}
	
	@Test 
	public void testListAllTasksNotInState() {
		// TODO
	}
	
	@Test
	public void testRemoveAllTasksForService()
	{
		System.out.println("TasksManagerBlImplTest.testRemoveAllTasksForService");
		
		((PerunBl)perun).getTasksManagerBl().removeAllTasksForService(perunSession, testService1);
		List<Task> tasks = ((PerunBl)perun).getTasksManagerBl().listAllTasksForService(perunSession, testService1.getId());
		
		assertNotNull(tasks);
		assertEquals(tasks.size(), 0);
		
	}

	@Test 
	public void testRemoveTask() {
		// TODO
	}

	@Test 
	public void testRemoveTask_ServiceFacility() {
		// TODO
	}
	
	@Test 
	public void testUpdateTask() {
		// TODO
	}
	
}
