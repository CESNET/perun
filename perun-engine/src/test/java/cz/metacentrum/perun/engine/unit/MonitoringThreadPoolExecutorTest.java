package cz.metacentrum.perun.engine.unit;

import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.scheduling.TaskResultListener;
import cz.metacentrum.perun.engine.scheduling.impl.ExecutorEngineWorkerImpl;
import cz.metacentrum.perun.engine.scheduling.impl.MonitoringThreadPoolExecutor;
import cz.metacentrum.perun.taskslib.dao.TaskDao;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-controller.xml", "classpath:perun-engine.xml", "classpath:perun-engine-jdbc-local-test.xml" })
public class MonitoringThreadPoolExecutorTest{
	ExecutorEngineWorkerImpl test;
	ExecutorEngineWorkerImpl test2;
	@Autowired
	TaskResultDao taskResultDao;
	@Autowired Properties propertiesBean;
	MonitoringThreadPoolExecutor monitoringThreadPoolExecutor;

	@Autowired
	GeneralServiceManager controller;
	@Autowired
	PerunBl perun;

	/*
	 * Connected to the perun-core DB - needed since we must have Task present (as if actions were initiated by
	 * the perun-dispatcher component)
	 */
	@Autowired
	TaskDao taskDaoCore;
	@Autowired TaskDao taskDao;

	PerunSession sess;

	public int engineId = 0;

	// base objects needed as test environment
	public Facility facility;
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

	@After
	public void cleanup() throws Exception {
		taskResultDao.clearAll();
		taskDaoCore.removeTask(task1.getId());
		taskDaoCore.removeTask(task2.getId());
		taskDaoCore.removeTask(task_gen.getId());
		controller.deleteExecService(execService1);
		controller.deleteExecService(execService2);
		controller.deleteExecService(execService_gen);
		perun.getServicesManagerBl().removeAllDestinations(sess, service, facility);
		perun.getServicesManagerBl().deleteService(sess, service);
		perun.getFacilitiesManagerBl().deleteFacility(sess, facility);
	}

    @Before
    public void setup() throws Exception {
		// determine engine ID

		engineId = Integer.parseInt(propertiesBean.getProperty("engine.unique.id"));

		// create session

		sess = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));

		// create expected core objects

		facility = perun.getFacilitiesManagerBl().createFacility(sess, new Facility(0, "EngineTestFacility"));
		service = perun.getServicesManagerBl().createService(sess, new Service(0, "test_service"));

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
		execService1.setId(controller.insertExecService(sess, execService1));

		execService2 = new ExecService();
		execService2.setService(service);
		execService2.setExecServiceType(ExecService.ExecServiceType.SEND);
		execService2.setEnabled(true);
		execService2.setDefaultDelay(1);
		execService2.setScript("/bin/true"); // this command always return true
		execService2.setId(controller.insertExecService(sess, execService2));

		execService_gen = new ExecService();
		execService_gen.setService(service);
		execService_gen.setExecServiceType(ExecService.ExecServiceType.GENERATE);
		execService_gen.setEnabled(true);
		execService_gen.setDefaultDelay(1);
		execService_gen.setScript("/bin/true"); // this command always return true
		execService_gen.setId(controller.insertExecService(sess, execService_gen));

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


		BlockingQueue workQueue = new ArrayBlockingQueue(10);

		TaskResultListener taskResultListener = new TaskResultListener() {
			@Override
			public void onTaskDestinationDone(Task task, Destination destination, TaskResult result) {
			}

			@Override
			public void onTaskDestinationError(Task task, Destination destination, TaskResult result) {
				fail("Task did not execute successfully. Task: " + task + " ; TaskResult: " + result);

			}
		};

		propertiesBean.setProperty("engine.unique.id", "1");
		propertiesBean.setProperty("engine.sendscript.path", "/");
		propertiesBean.setProperty("engine.genscript.path", "/");

		monitoringThreadPoolExecutor = new MonitoringThreadPoolExecutor(12,12,12,TimeUnit.SECONDS,workQueue);
		test = new ExecutorEngineWorkerImpl();
        test.setDestination(destination3);
        test.setExecService(execService2);
        test.setFacility(facility);
        test.setID(1);
		test.setTaskResultDao(taskResultDao);
		test.setResultListener(taskResultListener);
        test.setTask(task1);
		test.setPropertiesBean(propertiesBean);
		task1.setStartTime(new Date());

		test2 = new ExecutorEngineWorkerImpl();
		test2.setDestination(destination2);
		test2.setExecService(execService_gen);
		test2.setFacility(facility);
		test2.setID(2);
		test2.setTaskResultDao(taskResultDao);
		test2.setResultListener(taskResultListener);
		test2.setTask(task_gen);
		test2.setPropertiesBean(propertiesBean);
		task_gen.setStartTime(new Date());
    }

    @Test
    public void executeTest() throws InterruptedException {
		monitoringThreadPoolExecutor.execute(test2);
		monitoringThreadPoolExecutor.execute(test);
		int i = 0;
		while(monitoringThreadPoolExecutor.getActiveCount() > 0 && i < 100) {
			Thread.sleep(200);
			i += 1;
		}
		assertEquals(0, monitoringThreadPoolExecutor.getActiveCount());
    }
}