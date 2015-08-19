package cz.metacentrum.perun.taskslib.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michal Karm Babacek
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-tasks-lib.xml", "classpath:perun-core-jdbc.xml", "classpath:perun-core.xml", "classpath:perun-core-transaction-manager.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "springTransactionManager")
@Transactional
public class ExecServiceDependencyDaoTest {

	private final static Logger log = LoggerFactory.getLogger(ExecServiceDependencyDaoTest.class);

	@Autowired
	private ExecServiceDependencyDao execServiceDependencyDao;
	@Autowired
	private ExecServiceDao execServiceDao;
	@Autowired
	private ServicesManager servicesManager;
	@Autowired
	private DataSource dataSource;
	@Autowired
	private Perun perun;
	private JdbcPerunTemplate jdbcTemplate;
	private PerunSession perunSession;
	private Owner testOwner;
	private Service testService1;
	private Service testService2;
	private ExecService testExecService1;
	private ExecService testExecService2;
	private ExecService testExecService3;

	@Before
	public void beforeClass() {
		try {
			perunSession = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));
		} catch (InternalErrorException e) {
			log.error(e.toString());
		}
		jdbcTemplate = new JdbcPerunTemplate(dataSource);

		// Test Owner
		int newOwnerId = 0;
		try {
			newOwnerId = Utils.getNewId(jdbcTemplate, "owners_id_seq");
		} catch (InternalErrorException e) {
			log.error(e.toString(), e);
		}
		testOwner = new Owner();
		testOwner.setContact("Call me babe");
		testOwner.setType(OwnerType.technical);
		testOwner.setName("Tester-" + Long.toHexString(System.currentTimeMillis()));
		testOwner.setId(newOwnerId);
		jdbcTemplate.update("insert into owners(id, name, contact, type) values (?,?,?,?)", newOwnerId, testOwner.getName(), testOwner.getContact(), testOwner.getType().toString());

		// Test Service #1
		testService1 = new Service();
		testService1.setName("Test service 1-" + Long.toHexString(System.currentTimeMillis()));

		// Test Service #2
		testService2 = new Service();
		testService2.setName("Test service 2-" + Long.toHexString(System.currentTimeMillis()));

		try {
			testService1.setId(servicesManager.createService(perunSession, testService1, testOwner).getId());
			testService2.setId(servicesManager.createService(perunSession, testService2, testOwner).getId());
		} catch (InternalErrorException e) {
			log.error(e.toString());
		} catch (PrivilegeException e) {
			log.error(e.toString());
		} catch (OwnerNotExistsException e) {
			log.error(e.toString());
		} catch (ServiceExistsException e) {
			log.error(e.toString());
		}

		// Test ExecService #1 (Parent:testService1)
		testExecService1 = new ExecService();
		testExecService1.setDefaultDelay(1);
		testExecService1.setDefaultRecurrence(1);
		testExecService1.setEnabled(true);
		testExecService1.setService(testService1);
		testExecService1.setScript("/hellish/test/script");
		testExecService1.setExecServiceType(ExecServiceType.GENERATE);
		try {
			testExecService1.setId(execServiceDao.insertExecService(testExecService1));
		} catch (InternalErrorException e) {
			log.error(e.toString(), e);
		}

		// Test ExecService #2 (Parent:testService1)
		testExecService2 = new ExecService();
		testExecService2.setDefaultDelay(2);
		testExecService2.setDefaultRecurrence(2);
		testExecService2.setEnabled(true);
		testExecService2.setService(testService1);
		testExecService2.setScript("/hellish/test/script2");
		testExecService2.setExecServiceType(ExecServiceType.SEND);
		try {
			testExecService2.setId(execServiceDao.insertExecService(testExecService2));
		} catch (InternalErrorException e) {
			log.error(e.toString(), e);
		}

		// Test ExecService #3 (Parent:testService2)
		testExecService3 = new ExecService();
		testExecService3.setDefaultDelay(3);
		testExecService3.setDefaultRecurrence(3);
		testExecService3.setEnabled(true);
		testExecService3.setService(testService2);
		testExecService3.setScript("/hellish/test/script3");
		testExecService3.setExecServiceType(ExecServiceType.SEND);
		try {
			testExecService3.setId(execServiceDao.insertExecService(testExecService3));
		} catch (InternalErrorException e) {
			log.error(e.toString(), e);
		}
	}

	@Test
	public void testIsThereDependency() {
		System.out.println("ExecServiceDependencyDao.isThereDependency");

		try {

			log.debug("testIsThereDependency: Testing...");
			assertFalse(execServiceDependencyDao.isThereDependency(testExecService1.getId(), testExecService2.getId()));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testCreateDependency() {
		System.out.println("ExecServiceDependencyDao.createDependency");

		try {

			log.debug("testCreateDependency: Testing...");
			execServiceDependencyDao.createDependency(testExecService1.getId(), testExecService2.getId());
			assertTrue(execServiceDependencyDao.isThereDependency(testExecService1.getId(), testExecService2.getId()));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testRemoveDependency() {
		System.out.println("ExecServiceDependencyDao.removeDependency");

		try {

			log.debug("testRemoveDependency: Testing...");
			execServiceDependencyDao.createDependency(testExecService1.getId(), testExecService2.getId());
			execServiceDependencyDao.createDependency(testExecService2.getId(), testExecService3.getId());
			execServiceDependencyDao.removeDependency(testExecService1.getId(), testExecService2.getId());
			assertFalse(execServiceDependencyDao.isThereDependency(testExecService1.getId(), testExecService2.getId()));
			assertTrue(execServiceDependencyDao.isThereDependency(testExecService2.getId(), testExecService3.getId()));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testListExecServicesDependingOn() {
		System.out.println("ExecServiceDependencyDao.listExecServicesDependingOn");

		try {

			log.debug("testListExecServicesDependingOn: Testing...");
			execServiceDependencyDao.createDependency(testExecService1.getId(), testExecService2.getId());
			execServiceDependencyDao.createDependency(testExecService3.getId(), testExecService2.getId());

			List<ExecService> execServices = execServiceDependencyDao.listExecServicesDependingOn(testExecService2.getId());
			assertNotNull(execServices);
			for (ExecService execService : execServices) {
				log.debug("\tID:" + execService.getId());
				log.debug("\tDefDELAY:" + execService.getDefaultDelay());
				log.debug("\tDefRecurrence:" + execService.getDefaultRecurrence());
				log.debug("\tENABLED:" + execService.isEnabled());
				log.debug("\tService:" + execService.getService().getName());
				log.debug("\tSCRIPT:" + execService.getScript());
				log.debug("\tTYPE:" + execService.getExecServiceType().toString());
			}
			assertEquals(execServices.size(), 2);
			assertEquals(execServiceDependencyDao.listExecServicesDependingOn(testExecService1.getId()).size(), 0);
			assertEquals(execServiceDependencyDao.listExecServicesDependingOn(testExecService3.getId()).size(), 0);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testListExecServicesThisExecServiceDependsOn() {
		System.out.println("ExecServiceDependencyDao.listExecServicesThisExecServiceDependsOn");

		try {

			log.debug("testListExecServicesThisExecServiceDependsOn: Testing...");
			execServiceDependencyDao.createDependency(testExecService2.getId(), testExecService1.getId());
			execServiceDependencyDao.createDependency(testExecService2.getId(), testExecService3.getId());

			List<ExecService> execServices = execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(testExecService2.getId());
			assertNotNull(execServices);
			for (ExecService execService : execServices) {
				log.debug("\tID:" + execService.getId());
				log.debug("\tDefDELAY:" + execService.getDefaultDelay());
				log.debug("\tDefRecurrence:" + execService.getDefaultRecurrence());
				log.debug("\tENABLED:" + execService.isEnabled());
				log.debug("\tService:" + execService.getService().getName());
				log.debug("\tSCRIPT:" + execService.getScript());
				log.debug("\tTYPE:" + execService.getExecServiceType().toString());
			}
			assertEquals(execServices.size(), 2);
			assertEquals(execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(testExecService1.getId()).size(), 0);
			assertEquals(execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(testExecService3.getId()).size(), 0);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testListExecServicesThisExecServiceDependsOnWithType() {
		System.out.println("ExecServiceDependencyDao.listExecServicesThisExecServiceDependsOnWithType");

		try {

			log.debug("testListExecServicesThisExecServiceDependsOn: Testing...");
			//Depends on GENERATE
			execServiceDependencyDao.createDependency(testExecService2.getId(), testExecService1.getId());
			//Depends on SEND
			execServiceDependencyDao.createDependency(testExecService2.getId(), testExecService3.getId());

			List<ExecService> execServices = execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(testExecService2.getId(), ExecServiceType.GENERATE);
			assertNotNull(execServices);
			for (ExecService execService : execServices) {
				log.debug("\tID:" + execService.getId());
				log.debug("\tDefDELAY:" + execService.getDefaultDelay());
				log.debug("\tDefRecurrence:" + execService.getDefaultRecurrence());
				log.debug("\tENABLED:" + execService.isEnabled());
				log.debug("\tService:" + execService.getService().getName());
				log.debug("\tSCRIPT:" + execService.getScript());
				log.debug("\tTYPE:" + execService.getExecServiceType().toString());
			}
			//There are 2 dependencies, but only one of them is of type GENERATE...
			assertEquals(execServices.size(), 1);

			execServices = execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(testExecService2.getId(), ExecServiceType.SEND);
			assertNotNull(execServices);
			for (ExecService execService : execServices) {
				log.debug("\tID:" + execService.getId());
				log.debug("\tDefDELAY:" + execService.getDefaultDelay());
				log.debug("\tDefRecurrence:" + execService.getDefaultRecurrence());
				log.debug("\tENABLED:" + execService.isEnabled());
				log.debug("\tService:" + execService.getService().getName());
				log.debug("\tSCRIPT:" + execService.getScript());
				log.debug("\tTYPE:" + execService.getExecServiceType().toString());
			}
			//There are 2 dependencies, but only one of them is of type SEND...
			assertEquals(execServices.size(), 1);

			//These should be 0
			assertEquals(execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(testExecService1.getId(), ExecServiceType.SEND).size(), 0);
			assertEquals(execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(testExecService1.getId(), ExecServiceType.GENERATE).size(), 0);
			assertEquals(execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(testExecService3.getId(), ExecServiceType.SEND).size(), 0);
			assertEquals(execServiceDependencyDao.listExecServicesThisExecServiceDependsOn(testExecService3.getId(), ExecServiceType.GENERATE).size(), 0);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	public void setExecServiceDependencyDao(ExecServiceDependencyDao execServiceDependencyDao) {
		this.execServiceDependencyDao = execServiceDependencyDao;
	}

	public ExecServiceDependencyDao getExecServiceDependencyDao() {
		return execServiceDependencyDao;
	}

	public ExecServiceDao getExecServiceDao() {
		return execServiceDao;
	}

	public void setExecServiceDao(ExecServiceDao execServiceDao) {
		this.execServiceDao = execServiceDao;
	}
}
