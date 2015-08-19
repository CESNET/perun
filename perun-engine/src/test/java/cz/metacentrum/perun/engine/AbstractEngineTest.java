package cz.metacentrum.perun.engine;

import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.taskslib.dao.TaskDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@TransactionConfiguration(defaultRollback = true, transactionManager = "springTransactionManager")
// !! order of app context files matter in order to correctly recognize both data sources !!
@ContextConfiguration(locations = { "classpath:perun-controller.xml", "classpath:perun-engine.xml", "classpath:perun-engine-jdbc-local-test.xml" })
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
	public Owner owner;
	public Service service;
	public Destination destination1;
	public Destination destination2;
	public Destination destination3;
	public Destination destination4;
	public ExecService execService1;
	public ExecService execService2;
	public ExecService execService_gen;
	public Task task1;
	public Task task2;
	public Task task_gen;

	@Before
	public void setup() throws Exception {

		// determine engine ID

		engineId = Integer.parseInt(propertiesBean.getProperty("engine.unique.id"));

		// create session

		sess = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));

		// create expected core objects

		facility = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(0, "EngineTestFacility"));
		owner = perun.getOwnersManagerBl().createOwner(sess, new Owner(0, "test_owner", "test", OwnerType.technical));
		service = perun.getServicesManagerBl().createService(sess, new Service(0, "test_service"), owner);

		destination1 = perun.getServicesManagerBl().addDestination(sess, service, facility, new Destination(0, "par_dest1", "host", "PARALLEL"));
		destination2 = perun.getServicesManagerBl().addDestination(sess, service, facility, new Destination(0, "par_dest2", "host", "PARALLEL"));
		destination3 = perun.getServicesManagerBl().addDestination(sess, service, facility, new Destination(0, "one_dest1", "host", "ONE"));
		destination4 = perun.getServicesManagerBl().addDestination(sess, service, facility, new Destination(0, "one_dest2", "host", "ONE"));

		execService1 = new ExecService();
		execService1.setService(service);
		execService1.setExecServiceType(ExecService.ExecServiceType.SEND);
		execService1.setEnabled(true);
		execService1.setDefaultDelay(1);
		execService1.setScript("/bin/true"); // this command always return true
		execService1.setId(controller.insertExecService(sess, execService1, owner));

		execService2 = new ExecService();
		execService2.setService(service);
		execService2.setExecServiceType(ExecService.ExecServiceType.SEND);
		execService2.setEnabled(true);
		execService2.setDefaultDelay(1);
		execService2.setScript("/bin/true"); // this command always return true
		execService2.setId(controller.insertExecService(sess, execService2, owner));

		execService_gen = new ExecService();
		execService_gen.setService(service);
		execService_gen.setExecServiceType(ExecService.ExecServiceType.GENERATE);
		execService_gen.setEnabled(true);
		execService_gen.setDefaultDelay(1);
		execService_gen.setScript("/bin/true"); // this command always return true
		execService_gen.setId(controller.insertExecService(sess, execService_gen, owner));

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
		task1.setExecService(execService1);
		task1.setSchedule(new Date());
		task1.setStatus(Task.TaskStatus.NONE);
		task1.setId(taskDaoCore.scheduleNewTask(task1, engineId));

		task2 = new Task();
		task2.setDestinations(destinations);
		task2.setFacility(facility);
		task2.setExecService(execService2);
		task2.setSchedule(new Date());
		task2.setStatus(Task.TaskStatus.NONE);
		task2.setId(taskDaoCore.scheduleNewTask(task2, engineId));

		task_gen = new Task();
		task_gen.setDestinations(destinations);
		task_gen.setFacility(facility);
		task_gen.setExecService(execService_gen);
		task_gen.setSchedule(new Date());
		task_gen.setStatus(Task.TaskStatus.NONE);
		task_gen.setId(taskDaoCore.scheduleNewTask(task_gen, engineId));

	}

}
