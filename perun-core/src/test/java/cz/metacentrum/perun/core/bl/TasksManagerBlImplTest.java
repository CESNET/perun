package cz.metacentrum.perun.core.bl;


import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.FacilityState.FacilityPropagationState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.Vo;
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
	private TasksManagerBl tasksManager;
	private JdbcPerunTemplate jdbcTemplate;
	private PerunSession perunSession;
	private Service testService1, testService2;
	private int testDestinationId1, testDestinationId2;
	private int testFacilityId1, testFacilityId2;
	private Facility facility1, facility2;
	private Destination destination1, destination2;
	private Vo vo;
	private Resource resource;
	private Task task1, task2;
	private int task1Id, task2Id;
	private TaskResult result1, result2, result3;
	private int result1Id, result2Id, result3Id;
	
	@Before
	public void setUp() throws Exception
	{
		perunSession = perun.getPerunSession(
				new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient());

		jdbcTemplate = new JdbcPerunTemplate(dataSource);

		tasksManager = ((PerunBl)perun).getTasksManagerBl();
		
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

		// Test Service #2
		testService2 = new Service();
		testService2.setName("Test_service_2_" + Long.toHexString(System.currentTimeMillis()));
		testService2.setDelay(1);
		testService2.setRecurrence(1);
		testService2.setEnabled(true);
		testService2.setScript("/hellish/test/script");

		testService2.setId(servicesManager.createService(perunSession, testService2).getId());

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
		destination2 = ((PerunBl)perun).getServicesManagerBl().getDestinationById(perunSession, testDestinationId2);
		((PerunBl)perun).getServicesManagerBl().addDestination(perunSession, testService1, facility1, destination1);
		((PerunBl)perun).getServicesManagerBl().addDestination(perunSession, testService1, facility1, destination2);
		((PerunBl)perun).getServicesManagerBl().addDestination(perunSession, testService1, facility2, destination2);

		// vo
		vo = new Vo(0, "TasksManagerTestVo", "TMTestVo");
		vo = ((PerunBl)perun).getVosManagerBl().createVo(perunSession, vo);
		
		// resource
		resource = new Resource();
		resource.setName("TasksManagerTestResource");
		resource.setDescription("Testovaci");
		resource = ((PerunBl)perun).getResourcesManagerBl().createResource(perunSession, resource, vo, facility1);
		
		// tasks
		task1 = new Task();
		task1.setFacility(facility1);
		task1.setService(testService1);
		task1.setSchedule(0L);
		task1.setStatus(TaskStatus.DONE);
		List<Destination> destinationsList = new ArrayList<>();
		destinationsList.add(destination1);
		destinationsList.add(destination2);
		task1.setDestinations(destinationsList);
		task1Id = tasksManager.insertTask(perunSession, task1);
		task1.setId(task1Id);
		
		// tasks
		task2 = new Task();
		task2.setFacility(facility2);
		task2.setService(testService1);
		task2.setSchedule(0L);
		task2.setStatus(TaskStatus.WARNING);
		destinationsList = new ArrayList<>();
		destinationsList.add(destination2);
		task2.setDestinations(destinationsList);
		task2Id = tasksManager.insertTask(perunSession, task2);
		task2.setId(task2Id);

		// task results
		result1 = new TaskResult();
		result1.setDestination(destination1);
		result1.setDestinationId(testDestinationId1);
		result1.setService(testService1);
		result1.setTaskId(task1Id);
		result1.setStatus(TaskResultStatus.DONE);
		result1.setTimestamp(new Date());
		result1Id = tasksManager.insertNewTaskResult(perunSession, result1);
		result1.setId(result1Id);
		
		// task results
		result2 = new TaskResult();
		result2.setDestination(destination1);
		result2.setDestinationId(testDestinationId1);
		result2.setService(testService1);
		result2.setTaskId(task1Id);
		result2.setStatus(TaskResultStatus.DONE);
		result2.setTimestamp(Date.from(
				LocalDate
				.now()
				.minusDays(7)
				.atStartOfDay(ZoneId.systemDefault())
				.toInstant()));
		result2Id = tasksManager.insertNewTaskResult(perunSession, result2);
		result2.setId(result2Id);

		// task results
		result3 = new TaskResult();
		result3.setDestination(destination2);
		result3.setDestinationId(testDestinationId2);
		result3.setService(testService1);
		result3.setTaskId(task1Id);
		result3.setStatus(TaskResultStatus.DONE);
		result3.setTimestamp(Date.from(
				LocalDate
				.now()
				.minusDays(7)
				.atStartOfDay(ZoneId.systemDefault())
				.toInstant()));
		result3Id = tasksManager.insertNewTaskResult(perunSession, result3);
		result3.setId(result3Id);
		
		jdbcTemplate.query("select id from tasks_results where task_id = ?", 
				row -> { System.out.println("ID: " + row.getInt("id")); },
				task2Id
			);
			
	}
	
	@Test
	public void testCountTasks() {
		System.out.println("TasksManagerBlImplTest.testCountTasks");
		assertEquals(2, tasksManager.countTasks());
	}

	@Test
	public void testDeleteAllTaskResults() {
		System.out.println("TasksManagerBlImplTest.testDeleteAllTaskResults");
		tasksManager.deleteAllTaskResults(perunSession);
		assertEquals(0, tasksManager.getTaskResults(perunSession).size());
	}
	
	@Test
	public void testDeleteOldTaskResults() {
		System.out.println("TasksManagerBlImplTest.testDeleteOldTasksResults");
		assertEquals(1, tasksManager.deleteOldTaskResults(perunSession, 1));
		assertEquals(2, tasksManager.getTaskResults(perunSession).size());
	}

	@Test 
	public void testDeleteTask() {
		System.out.println("TasksManagerBlImplTest.testDeleteTask");
		tasksManager.deleteTask(perunSession, task1);
		assertEquals(0, tasksManager.getTaskResults(perunSession).size());
		assertEquals(1, tasksManager.countTasks());
		assertNull(tasksManager.getTaskById(perunSession, task1Id));
	}
	
	@Test 
	public void testDeleteTaskResultsById() {
		System.out.println("TasksManagerBlImplTest.testDeleteTaskResultsById");
		tasksManager.deleteTaskResultById(perunSession, result1Id);
		assertThatExceptionOfType(EmptyResultDataAccessException.class)
			.isThrownBy( () -> tasksManager.getTaskResultById(perunSession, result1Id) );
	}

	@Test
	public void testDeleteTaskResultsByIds() throws Exception {
		System.out.println("TasksManagerBlImplTest.testDeleteTaskResultsByIds");

		List<Integer> taskResultIds = tasksManager.getTaskResults(perunSession).stream().map(TaskResult::getId).toList();


		assertTrue(taskResultIds.containsAll(List.of(result1Id, result2Id)));

		perun.getTasksManager().deleteTaskResultsByIds(perunSession, List.of(result1Id, result2Id));

		taskResultIds = tasksManager.getTaskResults(perunSession).stream().map(TaskResult::getId).toList();

		assertThat(taskResultIds).doesNotContain(result1Id, result2Id);

	}
	
	@Test 
	public void testDeleteTaskResults_Task() {
		System.out.println("TasksManagerBlImplTest.testDeleteTaskResults_Task");
		tasksManager.deleteTaskResults(perunSession, task1Id);
		assertEquals(0, tasksManager.getTaskResultsByTask(perunSession, task1Id).size());
	}
	
	@Test 
	public void testDeleteTaskResults_TaskDestination() {
		System.out.println("TasksManagerBlImplTest.testDeleteTaskResults_TaskDestination");
		tasksManager.deleteTaskResults(perunSession, task1Id, testDestinationId1);
		assertEquals(0, tasksManager.getTaskResultsByTaskAndDestination(perunSession, task1Id, testDestinationId1).size());
		assertEquals(1, tasksManager.getTaskResults(perunSession).size());
	}
	
	@Test 
	public void testGetAllFacilitiesStates() {
		System.out.println("TasksManagerBlImplTest.testGetAllFacilitiesStates");
		// TODO - add more thorough test
		assertThatNoException().isThrownBy( () -> {
			List<FacilityState> states = tasksManager.getAllFacilitiesStates(perunSession);
			assertEquals(2, states.size());
		} );
	}
	
	@Test 
	public void testGetAllFacilitiesStatesForVo() {
		System.out.println("TasksManagerBlImplTest.testGetAllFacilitiesStatesForVo");
		// TODO - add more thorough test
		assertThatNoException().isThrownBy( () -> {
			List<FacilityState> states = tasksManager.getAllFacilitiesStatesForVo(perunSession, vo);
			assertEquals(1, states.size());
		} );
	}
	
	@Test 
	public void testGetFacilityServicesState() {
		System.out.println("TasksManagerBlImplTest.testGetFacilityServicesState");
		// TODO - add more thorough test
		List<ServiceState> states = tasksManager.getFacilityServicesState(perunSession, facility1);
		assertEquals(1, states.size());
	}
	
	@Test 
	public void testGetFacilityState() {
		System.out.println("TasksManagerBlImplTest.testGetFacilityState");
		// TODO - add more thorough test
		assertThatNoException().isThrownBy( () -> {
			FacilityState state = tasksManager.getFacilityState(perunSession, facility1);
			assertEquals(facility1, state.getFacility());
			assertEquals(FacilityPropagationState.OK, state.getState());
		} );
	}
	
	@Test 
	public void testGetResourcesState() {
		System.out.println("TasksManagerBlImplTest.testGetResourcesState");
		// TODO - add more thorough test
		assertThatNoException().isThrownBy( () -> {
			List<ResourceState> states = tasksManager.getResourcesState(perunSession, vo);
			assertEquals(1, states.size());
		} );
	}
	
	@Test 
	public void testGetTask() {
		System.out.println("TasksManagerBlImplTest.testGetTask");
		Task task =  tasksManager.getTask(perunSession, testService1, facility1);
		assertEquals(testService1.getId(), task.getServiceId());
		assertEquals(facility1.getId(), task.getFacilityId());
	}
	
	@Test 
	public void testGetTaskById() {
		System.out.println("TasksManagerBlImplTest.testGetTaskById");
		assertEquals(task1Id, tasksManager.getTaskById(perunSession, task1Id).getId());
	}
	
	@Test 
	public void testGetTaskResultById() {
		System.out.println("TasksManagerBlImplTest.testGetTaskResultById");
		assertTrue(result1.equals(tasksManager.getTaskResultById(perunSession, result1Id)));
	}
	
	@Test 
	public void testGetTaskResults() {
		System.out.println("TasksManagerBlImplTest.testGetTaskResults");
		assertEquals(3,  tasksManager.getTaskResults(perunSession).size());
	}
	
	@Test 
	public void testGetTaskResultsByTask() {
		System.out.println("TasksManagerBlImplTest.testGetTaskResultsByTask");
		assertEquals(3, tasksManager.getTaskResultsByTask(perunSession, task1Id).size());
	}
	
	@Test 
	public void testGetTaskResultsByTaskAndDestination() {
		System.out.println("TasksManagerBlImplTest.testGetTaskResultsByTaskAndDestination");
		assertEquals(2, tasksManager.getTaskResultsByTaskAndDestination(perunSession, task1Id, testDestinationId1).size());
		assertEquals(1, tasksManager.getTaskResultsByTaskAndDestination(perunSession, task1Id, testDestinationId2).size());
	}
	
	@Test 
	public void testGetTaskResultsByTaskOnlyNewest() {
		System.out.println("TasksManagerBlImplTest.testGetTaskResultsByTaskOnlyNewest");
		assertEquals(2,  tasksManager.getTaskResultsByTaskOnlyNewest(perunSession, task1Id).size());
	}
	
	@Test 
	public void testGetTaskResultsByDestinations() {
		System.out.println("TasksManagerBlImplTest.testGetTaskResultsByDestinations");
		assertEquals(2, tasksManager.getTaskResultsByDestinations(perunSession, List.of("test.destination." + testDestinationId1)).size());
		assertEquals(1, tasksManager.getTaskResultsByDestinations(perunSession, List.of("test.destination." + testDestinationId2)).size());
		assertEquals(3, tasksManager.getTaskResultsByDestinations(perunSession, List.of("test.destination." + testDestinationId1, "test.destination." + testDestinationId2)).size());
	}
	
	@Test 
	public void testInsertNewTaskResult() {
		System.out.println("TasksManagerBlImplTest.testInsertNewTaskResults");
		assertTrue(result1.equals(tasksManager.getTaskResultById(perunSession, result1Id)));
	}
	
	@Test 
	public void testInsertTask() {
		System.out.println("TasksManagerBlImplTest.testInsertTask");
		assertEquals(task1Id, tasksManager.getTaskById(perunSession, task1Id).getId());
	}
	
	@Test 
	public void testIsThereSuchTask() {
		System.out.println("TasksManagerBlImplTest.testIsThereSuchTask");
		assertTrue(tasksManager.isThereSuchTask(perunSession, testService1, facility1));
		assertFalse(tasksManager.isThereSuchTask(perunSession, testService2, facility1));
	}
	
	@Test 
	public void testListAllTasks() {
		System.out.println("TasksManagerBlImplTest.testListAllTasks");
		assertEquals(2, tasksManager.listAllTasks(perunSession).size());
	}
	
	@Test 
	public void testListAllTasksForFacility() {
		System.out.println("TasksManagerBlImplTest.testListAllTasksForFacility");
		assertEquals(1, tasksManager.listAllTasksForFacility(perunSession, testFacilityId1).size());
		assertEquals(1, tasksManager.listAllTasksForFacility(perunSession, testFacilityId2).size());
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
		System.out.println("TasksManagerBlImplTest.testListAllTasksInState");
		assertEquals(1, tasksManager.listAllTasksInState(perunSession, TaskStatus.DONE).size());
		assertEquals(1, tasksManager.listAllTasksInState(perunSession, TaskStatus.WARNING).size());
		assertEquals(0, tasksManager.listAllTasksInState(perunSession, TaskStatus.ERROR).size());
	}
	
	@Test 
	public void testListAllTasksNotInState() {
		System.out.println("TasksManagerBlImplTest.testListAllTasksNotInState");
		assertEquals(1, tasksManager.listAllTasksNotInState(perunSession, TaskStatus.DONE).size());
		assertEquals(1, tasksManager.listAllTasksNotInState(perunSession, TaskStatus.WARNING).size());
		assertEquals(2, tasksManager.listAllTasksNotInState(perunSession, TaskStatus.ERROR).size());
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
		System.out.println("TasksManagerBlImplTest.testRemoveTask");
		assertThatNoException().isThrownBy(() -> tasksManager.removeTask(perunSession, task2Id) );
		assertNull(tasksManager.getTaskById(perunSession, task2Id));
	}

	@Test 
	public void testRemoveTask_fails() {
		System.out.println("TasksManagerBlImplTest.testRemoveTask_fails");
		assertThatExceptionOfType(DataIntegrityViolationException.class)
			.isThrownBy( () -> tasksManager.removeTask(perunSession, task1Id) );
		// the transaction above fails, so add no db operations here, as they would fail too
	}

	@Test 
	public void testRemoveTask_ServiceFacility() {
		System.out.println("TasksManagerBlImplTest.testRemoveTask_ServiceFacility");
		assertThatNoException().isThrownBy( () -> tasksManager.removeTask(perunSession, testService1, facility2) );
		assertNull(tasksManager.getTaskById(perunSession, task2Id));
		assertNotNull(tasksManager.getTaskById(perunSession, task1Id));
	}
	
	@Test 
	public void testRemoveTask_ServiceFacility_fails() {
		System.out.println("TasksManagerBlImplTest.testRemoveTask_ServiceFacility");
		assertThatExceptionOfType(DataIntegrityViolationException.class)
			.isThrownBy( () -> tasksManager.removeTask(perunSession, testService1, facility1) );
		// the transaction above fails, so add no db operations here, as they would fail too
	}

	@Test 
	public void testUpdateTask() {
		System.out.println("TasksManagerBlImplTest.testUpdateTask");
		task1.setStatus(TaskStatus.ERROR);
		tasksManager.updateTask(perunSession, task1);
		Task task = tasksManager.getTaskById(perunSession, task1Id);
		assertEquals(TaskStatus.ERROR, task.getStatus());
	}
	
}
