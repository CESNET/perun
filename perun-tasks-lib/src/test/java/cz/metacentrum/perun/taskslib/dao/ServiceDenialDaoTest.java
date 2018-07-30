package cz.metacentrum.perun.taskslib.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.sql.DataSource;

import cz.metacentrum.perun.core.api.PerunClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michal Karm Babacek
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:perun-core.xml", "classpath:perun-tasks-lib.xml" })
@Transactional(transactionManager = "springTransactionManager")
public class ServiceDenialDaoTest {

	@Autowired
	private ServiceDenialDao serviceDenialDao;
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

	@Before
	public void setUp() throws InternalErrorException, OwnerNotExistsException, ServiceExistsException, PrivilegeException {
		perunSession = perun.getPerunSession(
				new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
				new PerunClient());

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
		testService1.setName("Test_service_1_" + Long.toHexString(System.currentTimeMillis()));
		testService1.setDelay(1);
		testService1.setRecurrence(1);
		testService1.setEnabled(true);
		testService1.setScript("/hellish/test/script");

		// Test Service #2
		testService2 = new Service();
		testService2.setName("Test_service_2_" + Long.toHexString(System.currentTimeMillis()));
		testService2.setDelay(2);
		testService2.setRecurrence(2);
		testService2.setEnabled(true);
		testService2.setScript("/hellish/test/script2");

		testService1.setId(servicesManager.createService(perunSession, testService1).getId());
		testService2.setId(servicesManager.createService(perunSession, testService2).getId());

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


	}

	@Test
	public void testIsServiceDeniedOnFacility() throws Exception {
		System.out.println("ServiceDenialDaoTest.isServiceBlockedOnFacility");

		assertFalse(serviceDenialDao.isServiceBlockedOnFacility(testService1.getId(), testFacilityId1));

	}

	@Test
	public void testIsServiceDeniedOnDestination() throws Exception {
		System.out.println("ServiceDenialDaoTest.isServiceBlockedOnDestination");

		assertFalse(serviceDenialDao.isServiceBlockedOnDestination(testService1.getId(), testDestinationId1));

	}

	@Test
	public void testBanServiceOnFacility() throws Exception {
		System.out.println("ServiceDenialDaoTest.blockServiceOnFacility");

		serviceDenialDao.blockServiceOnFacility(testService1.getId(), testFacilityId1);
		assertTrue(serviceDenialDao.isServiceBlockedOnFacility(testService1.getId(), testFacilityId1));

	}

	@Test
	public void testBanServiceOnDestination() throws Exception {
		System.out.println("ServiceDenialDaoTest.blockServiceOnDestination");

		serviceDenialDao.blockServiceOnDestination(testService1.getId(), testDestinationId1);
		assertTrue(serviceDenialDao.isServiceBlockedOnDestination(testService1.getId(), testDestinationId1));

	}

	@Test
	public void testListDenialsForFacility() throws Exception {
		System.out.println("ServiceDenialDaoTest.getServicesBlockedOnFacility");

		serviceDenialDao.blockServiceOnFacility(testService1.getId(), testFacilityId1);
		serviceDenialDao.blockServiceOnFacility(testService2.getId(), testFacilityId1);

		List<Service> deniedServices = serviceDenialDao.getServicesBlockedOnFacility(testFacilityId1);
		assertNotNull(deniedServices);
		assertEquals(deniedServices.size(), 2);

		assertEquals(serviceDenialDao.getServicesBlockedOnFacility(testFacilityId2).size(), 0);

	}

	@Test
	public void testListDenialsForDestination() throws Exception {
		System.out.println("ServiceDenialDaoTest.getServicesBlockedOnDestination");

		serviceDenialDao.blockServiceOnDestination(testService1.getId(), testDestinationId1);
		serviceDenialDao.blockServiceOnDestination(testService2.getId(), testDestinationId1);

		List<Service> deniedServices = serviceDenialDao.getServicesBlockedOnDestination(testDestinationId1);
		assertNotNull(deniedServices);
		assertEquals(deniedServices.size(), 2);

		assertEquals(serviceDenialDao.getServicesBlockedOnDestination(testDestinationId2).size(), 0);

	}

	@Test
	public void testFreeAllDenialsOnFacility() throws Exception {
		System.out.println("ServiceDenialDaoTest.unblockAllServicesOnFacility");

		serviceDenialDao.blockServiceOnFacility(testService1.getId(), testFacilityId1);
		serviceDenialDao.blockServiceOnFacility(testService2.getId(), testFacilityId1);
		serviceDenialDao.unblockAllServicesOnFacility(testFacilityId1);
		assertFalse(serviceDenialDao.isServiceBlockedOnFacility(testService1.getId(), testFacilityId1));
		assertFalse(serviceDenialDao.isServiceBlockedOnFacility(testService2.getId(), testFacilityId1));

	}

	@Test
	public void testFreeAllDenialsOnDestination() throws Exception {
		System.out.println("ServiceDenialDaoTest.unblockAllServicesOnDestination");

		serviceDenialDao.blockServiceOnDestination(testService1.getId(), testDestinationId1);
		serviceDenialDao.blockServiceOnDestination(testService2.getId(), testDestinationId1);

		serviceDenialDao.unblockAllServicesOnDestination(testDestinationId1);
		assertFalse(serviceDenialDao.isServiceBlockedOnDestination(testService1.getId(), testDestinationId1));
		assertFalse(serviceDenialDao.isServiceBlockedOnDestination(testService2.getId(), testDestinationId1));

	}

	@Test
	public void testFreeDenialOfServiceOnFacility() throws Exception {
		System.out.println("ServiceDenialDaoTest.unblockServiceOnFacility");

		serviceDenialDao.blockServiceOnFacility(testService1.getId(), testFacilityId1);
		serviceDenialDao.blockServiceOnFacility(testService2.getId(), testFacilityId1);

		serviceDenialDao.unblockServiceOnFacility(testService2.getId(), testFacilityId1);
		assertTrue(serviceDenialDao.isServiceBlockedOnFacility(testService1.getId(), testFacilityId1));
		assertFalse(serviceDenialDao.isServiceBlockedOnFacility(testService2.getId(), testFacilityId1));

	}

	@Test
	public void testFreeDenialOfServiceOnDestination() throws Exception {
		System.out.println("ServiceDenialDaoTest.unblockServiceOnDestination");

		serviceDenialDao.blockServiceOnDestination(testService1.getId(), testDestinationId1);
		serviceDenialDao.blockServiceOnDestination(testService2.getId(), testDestinationId1);

		serviceDenialDao.unblockServiceOnDestination(testService2.getId(), testDestinationId1);
		assertTrue(serviceDenialDao.isServiceBlockedOnDestination(testService1.getId(), testDestinationId1));
		assertFalse(serviceDenialDao.isServiceBlockedOnDestination(testService2.getId(), testDestinationId1));

	}

	public ServiceDenialDao getServiceDenialDao() {
		return serviceDenialDao;
	}

	public void setServiceDenialDao(ServiceDenialDao serviceDenialDao) {
		this.serviceDenialDao = serviceDenialDao;
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

}
