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
public class ExecServiceDenialDaoTest {

	private final static Logger log = LoggerFactory.getLogger(ExecServiceDenialDaoTest.class);

	@Autowired
	private ExecServiceDenialDao execServiceDenialDao;
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
	private int testDestinationId1;
	private int testDestinationId2;
	private int testFacilityId1;
	private int testFacilityId2;
	private ExecService testExecService1;
	private ExecService testExecService2;

	@Before
	public void setUp() throws InternalErrorException, OwnerNotExistsException, ServiceExistsException, PrivilegeException {
		perunSession = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));

		jdbcTemplate = new JdbcPerunTemplate(dataSource);

		// Test Owner
		int newOwnerId = Utils.getNewId(jdbcTemplate, "owners_id_seq");
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

		testService1.setId(servicesManager.createService(perunSession, testService1, testOwner).getId());
		testService2.setId(servicesManager.createService(perunSession, testService2, testOwner).getId());

		// Testing Destination #1
		testDestinationId1 = Utils.getNewId(jdbcTemplate, "destinations_id_seq");
		jdbcTemplate.update("insert into destinations(id, destination, type) values (?,?,'host')", testDestinationId1, "test.destination." + testDestinationId1);
		// Testing Destination #2
		testDestinationId2 = Utils.getNewId(jdbcTemplate, "destinations_id_seq");
		jdbcTemplate.update("insert into destinations(id, destination, type) values (?,?,'host')", testDestinationId2, "test.destination." + testDestinationId2);
		// Testing Facility #1
		testFacilityId1 = Utils.getNewId(jdbcTemplate, "facilities_id_seq");
		jdbcTemplate.update("insert into facilities(id, name) values (?,?)", testFacilityId1, "Cluster_" + testFacilityId1);
		// Testing Facility #2
		testFacilityId2 = Utils.getNewId(jdbcTemplate, "facilities_id_seq");
		jdbcTemplate.update("insert into facilities(id, name) values (?,?)", testFacilityId2, "Cluster_" + testFacilityId2);

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
		testExecService2.setService(testService2);
		testExecService2.setScript("/hellish/test/script2");
		testExecService2.setExecServiceType(ExecServiceType.SEND);
		try {
			testExecService2.setId(execServiceDao.insertExecService(testExecService2));
		} catch (InternalErrorException e) {
			log.error(e.toString(), e);
		}
	}

	@Test
	public void testIsExecServiceDeniedOnFacility() {
		System.out.println("ExecServiceDenialDao.isExecServiceDeniedOnFacility");

		try {

			log.debug("testIsExecServiceDeniedOnFacility: Testing...");
			assertFalse(execServiceDenialDao.isExecServiceDeniedOnFacility(testExecService1.getId(), testFacilityId1));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testIsExecServiceDeniedOnDestination() {
		System.out.println("ExecServiceDenialDao.isExecServiceDeniedOnDestination");

		try {

			log.debug("testIsExecServiceDeniedOnDestination: Testing...");
			assertFalse(execServiceDenialDao.isExecServiceDeniedOnDestination(testExecService1.getId(), testDestinationId1));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testBanExecServiceOnFacility() {
		System.out.println("ExecServiceDenialDao.banExecServiceOnFacility");

		try {

			log.debug("testBanExecServiceOnFacility: Inserting a new denial...");
			execServiceDenialDao.banExecServiceOnFacility(testExecService1.getId(), testFacilityId1);
			assertTrue(execServiceDenialDao.isExecServiceDeniedOnFacility(testExecService1.getId(), testFacilityId1));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testBanExecServiceOnDestination() {
		System.out.println("ExecServiceDenialDao.banExecServiceOnDestination");

		try {

			log.debug("testBanExecServiceOnDestination: Inserting a new denial...");
			execServiceDenialDao.banExecServiceOnDestination(testExecService1.getId(), testDestinationId1);
			assertTrue(execServiceDenialDao.isExecServiceDeniedOnDestination(testExecService1.getId(), testDestinationId1));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testListDenialsForFacility() {
		System.out.println("ExecServiceDenialDao.listDenialsForFacility");

		try {

			log.debug("testListDenialsForFacility: Inserting a new denial...");

			execServiceDenialDao.banExecServiceOnFacility(testExecService1.getId(), testFacilityId1);
			execServiceDenialDao.banExecServiceOnFacility(testExecService2.getId(), testFacilityId1);

			log.debug("testListDenialsForFacility: Listing all denials (denied services) for the facility...");
			List<ExecService> deniedExecServices = execServiceDenialDao.listDenialsForFacility(testFacilityId1);

			assertNotNull(deniedExecServices);

			for (ExecService deniedExecService : deniedExecServices) {
				log.debug("\tCLUSTER:" + testFacilityId1);
				log.debug("\tID:" + deniedExecService.getId());
				log.debug("\tDefDELAY:" + deniedExecService.getDefaultDelay());
				log.debug("\tDefRecurrence:" + deniedExecService.getDefaultRecurrence());
				log.debug("\tENABLED:" + deniedExecService.isEnabled());
				log.debug("\tService:" + deniedExecService.getService().getName());
				log.debug("\tSCRIPT:" + deniedExecService.getScript());
				log.debug("\tTYPE:" + deniedExecService.getExecServiceType().toString());
			}

			assertEquals(deniedExecServices.size(), 2);

			assertEquals(execServiceDenialDao.listDenialsForFacility(testFacilityId2).size(), 0);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testListDenialsForDestination() {
		System.out.println("ExecServiceDenialDao.listDenialsForDestination");

		try {

			log.debug("testListDenialsForDestination: Inserting a new denial...");

			execServiceDenialDao.banExecServiceOnDestination(testExecService1.getId(), testDestinationId1);
			execServiceDenialDao.banExecServiceOnDestination(testExecService2.getId(), testDestinationId1);

			log.debug("testListDenialsForDestination: Listing all denials (denied services) for the destination...");
			List<ExecService> deniedExecServices = execServiceDenialDao.listDenialsForDestination(testDestinationId1);

			assertNotNull(deniedExecServices);

			for (ExecService deniedExecService : deniedExecServices) {
				log.debug("\tDESTINATION:" + testDestinationId1);
				log.debug("\tID:" + deniedExecService.getId());
				log.debug("\tDefDELAY:" + deniedExecService.getDefaultDelay());
				log.debug("\tDefRecurrence:" + deniedExecService.getDefaultRecurrence());
				log.debug("\tENABLED:" + deniedExecService.isEnabled());
				log.debug("\tService:" + deniedExecService.getService().getName());
				log.debug("\tSCRIPT:" + deniedExecService.getScript());
				log.debug("\tTYPE:" + deniedExecService.getExecServiceType().toString());
			}

			assertEquals(deniedExecServices.size(), 2);

			assertEquals(execServiceDenialDao.listDenialsForDestination(testDestinationId2).size(), 0);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testFreeAllDenialsOnFacility() {
		System.out.println("ExecServiceDenialDao.freeAllDenialsOnFacility");

		try {

			log.debug("testFreeAllDenialsOnFacility: Inserting a new denial...");
			execServiceDenialDao.banExecServiceOnFacility(testExecService1.getId(), testFacilityId1);
			execServiceDenialDao.banExecServiceOnFacility(testExecService2.getId(), testFacilityId1);
			log.debug("testFreeAllDenialsOnFacility: freeing all the denials on the facility...");
			execServiceDenialDao.freeAllDenialsOnFacility(testFacilityId1);
			assertFalse(execServiceDenialDao.isExecServiceDeniedOnFacility(testExecService1.getId(), testFacilityId1));
			assertFalse(execServiceDenialDao.isExecServiceDeniedOnFacility(testExecService2.getId(), testFacilityId1));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testFreeAllDenialsOnDestination() {
		System.out.println("ExecServiceDenialDao.freeAllDenialsOnDestination");

		try {

			log.debug("testFreeAllDenialsOnDestination: Inserting a new denial...");
			execServiceDenialDao.banExecServiceOnDestination(testExecService1.getId(), testDestinationId1);
			execServiceDenialDao.banExecServiceOnDestination(testExecService2.getId(), testDestinationId1);
			log.debug("testFreeAllDenialsOnDestination: freeing all the denials on the destination...");
			execServiceDenialDao.freeAllDenialsOnDestination(testDestinationId1);
			assertFalse(execServiceDenialDao.isExecServiceDeniedOnDestination(testExecService1.getId(), testDestinationId1));
			assertFalse(execServiceDenialDao.isExecServiceDeniedOnDestination(testExecService2.getId(), testDestinationId1));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testFreeDenialOfExecServiceOnFacility() {
		System.out.println("ExecServiceDenialDao.freeDenialOfExecServiceOnFacility");

		try {

			log.debug("testFreeDenialOfExecServiceOnFacility: Inserting a new denial...");
			execServiceDenialDao.banExecServiceOnFacility(testExecService1.getId(), testFacilityId1);
			execServiceDenialDao.banExecServiceOnFacility(testExecService2.getId(), testFacilityId1);
			log.debug("testFreeDenialOfExecServiceOnFacility: freeing the denial of the service on the destination...");
			execServiceDenialDao.freeDenialOfExecServiceOnFacility(testExecService2.getId(), testFacilityId1);
			assertTrue(execServiceDenialDao.isExecServiceDeniedOnFacility(testExecService1.getId(), testFacilityId1));
			assertFalse(execServiceDenialDao.isExecServiceDeniedOnFacility(testExecService2.getId(), testFacilityId1));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void testFreeDenialOfExecServiceOnDestination() {
		System.out.println("ExecServiceDenialDao.freeDenialOfExecServiceOnDestination");

		try {

			log.debug("testFreeDenialOfExecServiceOnDestination: Inserting a new denial...");
			execServiceDenialDao.banExecServiceOnDestination(testExecService1.getId(), testDestinationId1);
			execServiceDenialDao.banExecServiceOnDestination(testExecService2.getId(), testDestinationId1);
			log.debug("testFreeDenialOfExecServiceOnDestination: freeing the denial of the service on the destination...");
			execServiceDenialDao.freeDenialOfExecServiceOnDestination(testExecService2.getId(), testDestinationId1);
			assertTrue(execServiceDenialDao.isExecServiceDeniedOnDestination(testExecService1.getId(), testDestinationId1));
			assertFalse(execServiceDenialDao.isExecServiceDeniedOnDestination(testExecService2.getId(), testDestinationId1));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	public ExecServiceDenialDao getExecServiceDenialDao() {
		return execServiceDenialDao;
	}

	public void setExecServiceDenialDao(ExecServiceDenialDao execServiceDenialDao) {
		this.execServiceDenialDao = execServiceDenialDao;
	}

	public ExecServiceDao getExecServiceDao() {
		return execServiceDao;
	}

	public void setExecServiceDao(ExecServiceDao execServiceDao) {
		this.execServiceDao = execServiceDao;
	}

	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
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

	public JdbcPerunTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcPerunTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public PerunSession getPerunSession() {
		return perunSession;
	}

	public void setPerunSession(PerunSession perunSession) {
		this.perunSession = perunSession;
	}

	public Owner getTestOwner() {
		return testOwner;
	}

	public void setTestOwner(Owner testOwner) {
		this.testOwner = testOwner;
	}

	public Service getTestService1() {
		return testService1;
	}

	public void setTestService1(Service testService1) {
		this.testService1 = testService1;
	}

	public Service getTestService2() {
		return testService2;
	}

	public void setTestService2(Service testService2) {
		this.testService2 = testService2;
	}

	public int getTestDestinationId1() {
		return testDestinationId1;
	}

	public void setTestDestinationId1(int testDestinationId1) {
		this.testDestinationId1 = testDestinationId1;
	}

	public int getTestDestinationId2() {
		return testDestinationId2;
	}

	public void setTestDestinationId2(int testDestinationId2) {
		this.testDestinationId2 = testDestinationId2;
	}

	public int getTestFacilityId1() {
		return testFacilityId1;
	}

	public void setTestFacilityId1(int testFacilityId1) {
		this.testFacilityId1 = testFacilityId1;
	}

	public int getTestFacilityId2() {
		return testFacilityId2;
	}

	public void setTestFacilityId2(int testFacilityId2) {
		this.testFacilityId2 = testFacilityId2;
	}

	public ExecService getTestExecService1() {
		return testExecService1;
	}

	public void setTestExecService1(ExecService testExecService1) {
		this.testExecService1 = testExecService1;
	}

	public ExecService getTestExecService2() {
		return testExecService2;
	}

	public void setTestExecService2(ExecService testExecService2) {
		this.testExecService2 = testExecService2;
	}
}
