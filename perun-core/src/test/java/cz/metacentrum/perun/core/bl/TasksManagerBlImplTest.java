package cz.metacentrum.perun.core.bl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
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
	private Destination destination;
	private Task task1, task2;

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
		destination = ((PerunBl)perun).getServicesManagerBl().getDestinationById(perunSession, testDestinationId1);
		((PerunBl)perun).getServicesManagerBl().addDestination(perunSession, testService1, facility1, destination);
		// facility_service_destinations
		destination = ((PerunBl)perun).getServicesManagerBl().getDestinationById(perunSession, testDestinationId2);
		((PerunBl)perun).getServicesManagerBl().addDestination(perunSession, testService1, facility2, destination);

		// tasks
		task1 = new Task();
		task1.setFacility(facility1);
		task1.setService(testService1);
		task1.setSchedule(0L);
		task1.setStatus(TaskStatus.DONE);
		List<Destination> destinationsList = new ArrayList<>();
		destinationsList.add(destination);
		task1.setDestinations(destinationsList);
		((PerunBl)perun).getTasksManagerBl().insertTask(task1);
		
		// tasks
		task2 = new Task();
		task2.setFacility(facility2);
		task2.setService(testService1);
		task2.setSchedule(0L);
		task2.setStatus(TaskStatus.DONE);
		destinationsList = new ArrayList<>();
		destinationsList.add(destination);
		task2.setDestinations(destinationsList);
		((PerunBl)perun).getTasksManagerBl().insertTask(task2);

	}
	
	@Test
	public void testListAllTasksForService()
	{
		System.out.println("TasksManagerBlImplTest.testListAllTasksForService");
		
		List<Task> tasks = ((PerunBl)perun).getTasksManagerBl().listAllTasksForService(testService1.getId());
		
		assertNotNull(tasks);
		assertEquals(tasks.size(), 2);
	}
	
	@Test
	public void testRemoveAllTasksForService()
	{
		System.out.println("TasksManagerBlImplTest.testRemoveAllTasksForService");
		
		((PerunBl)perun).getTasksManagerBl().removeAllTasksForService(testService1);
		List<Task> tasks = ((PerunBl)perun).getTasksManagerBl().listAllTasksForService(testService1.getId());
		
		assertNotNull(tasks);
		assertEquals(tasks.size(), 0);
		
	}
}
