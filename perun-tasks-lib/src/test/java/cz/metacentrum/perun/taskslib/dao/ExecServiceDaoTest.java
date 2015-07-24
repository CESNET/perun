package cz.metacentrum.perun.taskslib.dao;

import static org.junit.Assert.assertEquals;
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
public class ExecServiceDaoTest {

	private final static Logger log = LoggerFactory.getLogger(ExecServiceDaoTest.class);

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

	@Before
	public void setUp() throws InternalErrorException, OwnerNotExistsException, ServiceExistsException, PrivilegeException {
		try {
			perunSession = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));
		} catch (InternalErrorException e) {
			log.error(e.toString());
		}

		jdbcTemplate = new JdbcPerunTemplate(dataSource);

		// Test Owner
		int newOwnerId = Utils.getNewId(jdbcTemplate, "owners_id_seq");
		testOwner = new Owner();
		testOwner.setContact("Call me babe");
		testOwner.setName("Tester-" + Long.toHexString(System.currentTimeMillis()));
		testOwner.setType(OwnerType.technical);
		testOwner.setId(newOwnerId);
		jdbcTemplate.update("insert into owners(id, name, contact, type) values (?,?,?,?)", newOwnerId, testOwner.getName(), testOwner.getContact(), testOwner.getType().toString());

		// Test Service #1
		testService1 = new Service();
		testService1.setName("Test service 1-" + Long.toHexString(System.currentTimeMillis()));

		// Test Service #2
		testService2 = new Service();
		testService2.setName("Test service 2-" + Long.toHexString(System.currentTimeMillis()));

		testService1.setId(servicesManager.createService(perunSession, testService1, testOwner).getId());
		testService2.setId(servicesManager.createService(perunSession, testService2, testOwner).getId());
	}

	@Test
	public void testInsertExecService() {
		try {
			log.debug("testInsertExecService: Inserting a new ExecService...");

			// Test ExecService #1 (Parent:testService1)

			ExecService execService = new ExecService();
			execService.setDefaultDelay(1);
			execService.setDefaultRecurrence(1);
			execService.setEnabled(true);
			execService.setService(testService1);
			execService.setScript("/hellish/test/script");
			execService.setExecServiceType(ExecServiceType.GENERATE);

			int newExecServiceId = execServiceDao.insertExecService(execService);

			// Test ExecService #2 (Parent:testService1)
			ExecService execService2 = new ExecService();
			execService2.setDefaultDelay(2);
			execService2.setDefaultRecurrence(2);
			execService2.setEnabled(true);
			execService2.setService(testService1);
			execService2.setScript("/hellish/test/script2");
			execService2.setExecServiceType(ExecServiceType.SEND);
			int newExecServiceId2 = execServiceDao.insertExecService(execService2);

			// Test ExecService #3 (Parent:testService2)
			ExecService execService3 = new ExecService();
			execService3.setDefaultDelay(3);
			execService3.setDefaultRecurrence(3);
			execService3.setEnabled(true);
			execService3.setService(testService2);
			execService3.setScript("/hellish/test/script3");
			execService3.setExecServiceType(ExecServiceType.SEND);
			int newExecServiceId3 = execServiceDao.insertExecService(execService3);

			assertTrue(newExecServiceId > 0);
			assertTrue(newExecServiceId2 > newExecServiceId);
			assertTrue(newExecServiceId3 > newExecServiceId2);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testCountExecServices() {
		try {
			log.debug("testCountExecServices: Counting execExecServices...");

			int countBeforeTest = execServiceDao.countExecServices();

			log.debug("testCountExecServices: Inserting execServices...");

			// Test ExecService #1 (Parent:testService1)
			ExecService execService = new ExecService();
			execService.setDefaultDelay(1);
			execService.setDefaultRecurrence(1);
			execService.setEnabled(true);
			execService.setService(testService1);
			execService.setScript("/hellish/test/script");
			execService.setExecServiceType(ExecServiceType.GENERATE);
			execServiceDao.insertExecService(execService);

			// Test ExecService #2 (Parent:testService1)
			ExecService execService2 = new ExecService();
			execService2.setDefaultDelay(2);
			execService2.setDefaultRecurrence(2);
			execService2.setEnabled(true);
			execService2.setService(testService1);
			execService2.setScript("/hellish/test/script2");
			execService2.setExecServiceType(ExecServiceType.SEND);
			execServiceDao.insertExecService(execService2);

			// Test ExecService #3 (Parent:testService2)
			ExecService execService3 = new ExecService();
			execService3.setDefaultDelay(3);
			execService3.setDefaultRecurrence(3);
			execService3.setEnabled(true);
			execService3.setService(testService2);
			execService3.setScript("/hellish/test/script3");
			execService3.setExecServiceType(ExecServiceType.SEND);
			execServiceDao.insertExecService(execService3);

			log.debug("testCountExecServices: Counting execExecServices...");

			int countAfterTest = execServiceDao.countExecServices();

			assertTrue(countAfterTest == countBeforeTest + 3);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testListExecServices() {
		try {

			log.debug("testListExecServices: Inserting execServices...");

			int originalNumberOfExecServices = execServiceDao.countExecServices();

			// Test ExecService #1 (Parent:testService1)
			ExecService execService = new ExecService();
			execService.setDefaultDelay(1);
			execService.setDefaultRecurrence(1);
			execService.setEnabled(true);
			execService.setService(testService1);
			execService.setScript("/hellish/test/script");
			execService.setExecServiceType(ExecServiceType.GENERATE);
			execServiceDao.insertExecService(execService);

			// Test ExecService #2 (Parent:testService1)
			ExecService execService2 = new ExecService();
			execService2.setDefaultDelay(2);
			execService2.setDefaultRecurrence(2);
			execService2.setEnabled(true);
			execService2.setService(testService1);
			execService2.setScript("/hellish/test/script2");
			execService2.setExecServiceType(ExecServiceType.SEND);
			execServiceDao.insertExecService(execService2);

			// Test ExecService #3 (Parent:testService2)
			ExecService execService3 = new ExecService();
			execService3.setDefaultDelay(3);
			execService3.setDefaultRecurrence(3);
			execService3.setEnabled(true);
			execService3.setService(testService2);
			execService3.setScript("/hellish/test/script3");
			execService3.setExecServiceType(ExecServiceType.SEND);
			execServiceDao.insertExecService(execService3);

			log.debug("testListExecServices: Retrieving all execExecServices...");
			List<ExecService> execServices = execServiceDao.listExecServices();

			assertNotNull(execServices);

			for (ExecService s : execServices) {
				log.debug("\tID:" + s.getId());
				log.debug("\tDefDELAY:" + s.getDefaultDelay());
				log.debug("\tDefRecurrence:" + s.getDefaultRecurrence());
				log.debug("\tENABLED:" + s.isEnabled());
				log.debug("\tSERVICE:" + s.getService().getName());
				log.debug("\tSCRIPT:" + s.getScript());
				log.debug("\tTYPE:" + s.getExecServiceType().toString());
			}

			assertEquals(execServices.size(), originalNumberOfExecServices + 3);

		} catch (Exception e) {
			log.error("testListExecServices: " + e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testGetExecService() {
		try {

			log.debug("testGetExecService: Inserting execServices...");

			// Test ExecService #1 (Parent:testService1)
			ExecService execService = new ExecService();
			execService.setDefaultDelay(1);
			execService.setDefaultRecurrence(1);
			execService.setEnabled(true);
			execService.setService(testService1);
			execService.setScript("/hellish/test/script");
			execService.setExecServiceType(ExecServiceType.GENERATE);
			execService.setId(execServiceDao.insertExecService(execService));

			// Test ExecService #2 (Parent:testService1)
			ExecService execService2 = new ExecService();
			execService2.setDefaultDelay(2);
			execService2.setDefaultRecurrence(2);
			execService2.setEnabled(true);
			execService2.setService(testService1);
			execService2.setScript("/hellish/test/script2");
			execService2.setExecServiceType(ExecServiceType.SEND);
			execService2.setId(execServiceDao.insertExecService(execService2));

			// Test ExecService #3 (Parent:testService2)
			ExecService execService3 = new ExecService();
			execService3.setDefaultDelay(3);
			execService3.setDefaultRecurrence(3);
			execService3.setEnabled(true);
			execService3.setService(testService2);
			execService3.setScript("/hellish/test/script3");
			execService3.setExecServiceType(ExecServiceType.SEND);
			execService3.setId(execServiceDao.insertExecService(execService3));

			assertEquals(execServiceDao.getExecService(execService2.getId()).getScript(), execService2.getScript());
			assertEquals(execServiceDao.getExecService(execService3.getId()).getScript(), execService3.getScript());
			assertEquals(execServiceDao.getExecService(execService.getId()).getScript(), execService.getScript());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testUpdateExecService() {
		try {

			log.debug("testUpdateExecService: Inserting execExecService...");

			// Test ExecService #1 (Parent:testService1)
			ExecService execService = new ExecService();
			execService.setDefaultDelay(1);
			execService.setDefaultRecurrence(1);
			execService.setEnabled(true);
			execService.setService(testService1);
			execService.setScript("/hellish/test/script");
			execService.setExecServiceType(ExecServiceType.GENERATE);
			execService.setId(execServiceDao.insertExecService(execService));

			log.debug("testUpdateExecService: Updating execService...");

			execService.setDefaultDelay(2);
			execService.setDefaultRecurrence(2);
			execService.setEnabled(true);
			execService.setScript("/hellish/test/script2");
			execService.setExecServiceType(ExecServiceType.GENERATE);

			execServiceDao.updateExecService(execService);

			ExecService retrievedExecService = execServiceDao.getExecService(execService.getId());

			assertEquals(retrievedExecService.getDefaultDelay(), execService.getDefaultDelay());
			assertEquals(retrievedExecService.getDefaultRecurrence(), execService.getDefaultRecurrence());
			assertEquals(retrievedExecService.getScript(), execService.getScript());
			assertEquals(retrievedExecService.getExecServiceType(), execService.getExecServiceType());

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testRemoveExecService() {
		try {

			log.debug("testRemoveExecService: Inserting execServices...");

			// Test ExecService #1 (Parent:testService1)
			ExecService execService = new ExecService();
			execService.setDefaultDelay(1);
			execService.setDefaultRecurrence(1);
			execService.setEnabled(true);
			execService.setService(testService1);
			execService.setScript("/hellish/test/script");
			execService.setExecServiceType(ExecServiceType.GENERATE);
			execService.setId(execServiceDao.insertExecService(execService));

			// Test ExecService #2 (Parent:testService1)
			ExecService execService2 = new ExecService();
			execService2.setDefaultDelay(2);
			execService2.setDefaultRecurrence(2);
			execService2.setEnabled(true);
			execService2.setService(testService1);
			execService2.setScript("/hellish/test/script2");
			execService2.setExecServiceType(ExecServiceType.SEND);
			execService2.setId(execServiceDao.insertExecService(execService2));

			// Test ExecService #3 (Parent:testService2)
			ExecService execService3 = new ExecService();
			execService3.setDefaultDelay(3);
			execService3.setDefaultRecurrence(3);
			execService3.setEnabled(true);
			execService3.setService(testService2);
			execService3.setScript("/hellish/test/script3");
			execService3.setExecServiceType(ExecServiceType.SEND);
			execService3.setId(execServiceDao.insertExecService(execService3));

			log.debug("testRemoveExecService: Deleting one of the execExecServices...");

			ExecService execServiceToBeDeleted = execServiceDao.getExecService(execService2.getId());

			execServiceDao.deleteExecService(execServiceToBeDeleted.getId());

			assertEquals(execServiceDao.getExecService(execService.getId()), execService);
			assertEquals(execServiceDao.getExecService(execService2.getId()), null);
			assertEquals(execServiceDao.getExecService(execService3.getId()), execService3);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	public void setExecServiceDao(ExecServiceDao execServiceDao) {
		this.execServiceDao = execServiceDao;
	}

	public ExecServiceDao getExecServiceDao() {
		return execServiceDao;
	}

	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setPerun(Perun perun) {
		this.perun = perun;
	}

	public Perun getPerun() {
		return perun;
	}

	public void setPerunSession(PerunSession perunSession) {
		this.perunSession = perunSession;
	}

	public PerunSession getPerunSession() {
		return perunSession;
	}
}
