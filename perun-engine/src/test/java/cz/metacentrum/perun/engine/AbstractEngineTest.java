package cz.metacentrum.perun.engine;

import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.engine.jms.JMSQueueManager;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.dao.TaskDao;
import cz.metacentrum.perun.taskslib.model.SendTask;
import cz.metacentrum.perun.taskslib.model.Task;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@Rollback
@Transactional(transactionManager = "springTransactionManager")
// !! order of app context files matter in order to correctly recognize both data sources !!
@ContextConfiguration(locations = { "classpath:perun-core.xml", "classpath:perun-tasks-lib.xml", "classpath:perun-engine.xml" })
public abstract class AbstractEngineTest {

	@Autowired Properties propertiesBean;
	@Autowired GeneralServiceManager controller;
	@Autowired PerunBl perun;

	/*
	 * Connected to the perun-core DB - needed since we must have Task present (as if actions were initiated by
	 * the perun-dispatcher component)
	 */
	@Autowired TaskDao taskDaoCore;
	@Autowired TaskDao taskDao;

	PerunSession sess;

	public int engineId = 0;

	// base objects needed as test environment
	public Facility facility;
	public Service service;
	public Service service2;
	public Destination destination1;
	public Destination destination2;
	public Destination destination3;
	public Destination destination4;
	public Task task1;
	public Task task2;
	public SendTask sendTask1;
	public SendTask sendTask2;
	public SendTask sendTask3;
	public SendTask sendTask4;
	public SendTask sendTaskFalse;

	public JMSQueueManager jmsQueueManagerMock;
	public SchedulingPool schedulingPoolMock;

	@Before
	public void setup() throws Exception {

		// determine engine ID

		engineId = Integer.parseInt(propertiesBean.getProperty("engine.unique.id"));

		// create session
		sess = perun.getPerunSession(
				new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient());

		// create expected core objects

		facility = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(0, "EngineTestFacility"));
		Service srv = new Service(0, "test_service", null);
		srv.setEnabled(true);
		srv.setDelay(1);
		srv.setRecurrence(2);
		srv.setScript("/bin/true"); // this command always return true
		service = perun.getServicesManagerBl().createService(sess, srv);

		Service srv2 = new Service(0, "test_service2", null);
		srv2.setEnabled(true);
		srv2.setDelay(1);
		srv2.setRecurrence(2);
		srv2.setScript("/bin/false"); // this command always return false
		service2 = perun.getServicesManagerBl().createService(sess, srv2);

		destination1 = perun.getServicesManagerBl().addDestination(
				sess, service, facility, new Destination(0, "par_dest1", "host", "PARALLEL"));
		destination2 = perun.getServicesManagerBl().addDestination(
				sess, service, facility, new Destination(0, "par_dest2", "host", "PARALLEL"));
		destination3 = perun.getServicesManagerBl().addDestination(
				sess, service, facility, new Destination(0, "one_dest1", "host", "ONE"));
		destination4 = perun.getServicesManagerBl().addDestination(
				sess, service, facility, new Destination(0, "one_dest2", "host", "ONE"));

		List<Destination> destinations = new ArrayList<Destination>() {{
			add(destination1);
			add(destination2);
			add(destination3);
			add(destination4);
		}};

		// create Tasks in shared perun-core DB (as if action was initiated by dispatcher).

		task1 = new Task();
		task1.setDestinations(destinations);
		task1.setFacility(facility);
		task1.setService(service);
		task1.setSchedule(new Date());
		task1.setStatus(Task.TaskStatus.PLANNED);
		task1.setId(taskDaoCore.scheduleNewTask(task1, engineId));

		task2 = new Task();
		task2.setDestinations(destinations);
		task2.setFacility(facility);
		task2.setService(service2);
		task2.setSchedule(new Date());
		task2.setStatus(Task.TaskStatus.PLANNED);
		task2.setId(taskDaoCore.scheduleNewTask(task2, engineId));

		sendTask1 = new SendTask(task1, destination1);
		sendTask1.setStartTime(new Date(System.currentTimeMillis()));
		sendTask1.setStatus(SendTask.SendTaskStatus.SENDING);
		sendTask1.setReturnCode(0);

		sendTask2 = new SendTask(task1, destination2);
		sendTask2.setStartTime(new Date(System.currentTimeMillis()));
		sendTask2.setStatus(SendTask.SendTaskStatus.SENDING);
		sendTask2.setReturnCode(0);

		sendTask3 = new SendTask(task1, destination3);
		sendTask3.setStartTime(new Date(System.currentTimeMillis()));
		sendTask3.setStatus(SendTask.SendTaskStatus.SENDING);
		sendTask3.setReturnCode(0);

		sendTask4 = new SendTask(task1, destination4);
		sendTask4.setStartTime(new Date(System.currentTimeMillis()));
		sendTask4.setStatus(SendTask.SendTaskStatus.SENDING);
		sendTask4.setReturnCode(0);

		sendTaskFalse = new SendTask(task2, destination1);
		sendTaskFalse.setStartTime(new Date(System.currentTimeMillis()));
		sendTaskFalse.setStatus(SendTask.SendTaskStatus.SENDING);
		sendTaskFalse.setReturnCode(1);
	}

	public void mockSetUp() throws Exception {
		schedulingPoolMock = mock(SchedulingPool.class);
		jmsQueueManagerMock = mock(JMSQueueManager.class);
	}
}
