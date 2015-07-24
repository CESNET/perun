package cz.metacentrum.perun.engine;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import org.springframework.jdbc.core.JdbcPerunTemplate;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.service.EngineManager;
import cz.metacentrum.perun.rpclib.Rpc;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:perun-engine.xml",
		"classpath:perun-datasources.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManagerPerunEngine")
// @Transactional
// @Transactional(propagation = Propagation.NEVER)
public abstract class BaseTest {
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(BaseTest.class);

	@Autowired
	private EngineManager engineManager;

	private JdbcPerunTemplate jdbcTemplate;
	private Owner testOwner;
	private Service testService1;
	private Service testService2;
	private Service testService3;
	private Service servicePasswd;
	private Facility facility1;
	private Facility facility2;
	private Facility facility3;
	private ExecService execService1;
	private ExecService execService2;
	private ExecService execServicePasswdSend;
	private ExecService execServicePasswdGenerate;
	private Facility facility1195;
	private Destination destinationA;
	private Destination destinationB;
	private Destination destinationC;

	private Destination destination1 = new Destination();
	private Destination destination2 = new Destination();
	private Destination destination3 = new Destination();
	private Destination destination4 = new Destination();
	private Destination destination5 = new Destination();
	private Destination destination6 = new Destination();
	private Destination destination7 = new Destination();
	private Destination destination8 = new Destination();
	private Destination destination9 = new Destination();

	@Autowired
	private DataSource dataSource;

	public void initJdbcTemplate() {
		jdbcTemplate = new JdbcPerunTemplate(dataSource);
	}

	public void cleanAll() {
		// Clean up
		/*
		 * FIXME too danger code
		 * jdbcTemplate.update("delete from service_processing_rule");
		 * jdbcTemplate.update("delete from service_denials");
		 * jdbcTemplate.update("delete from service_dependencies");
		 * jdbcTemplate.update("delete from service_service_packages");
		 * jdbcTemplate.update("delete from service_packages");
		 * jdbcTemplate.update("delete from service_required_attrs");
		 * jdbcTemplate.update("delete from resource_services");
		 * jdbcTemplate.update("delete from facility_service_destinations");
		 * jdbcTemplate.update("delete from tasks_results");
		 * jdbcTemplate.update("delete from tasks");
		 * jdbcTemplate.update("delete from exec_services");
		 * jdbcTemplate.update("delete from processing_rules");
		 * jdbcTemplate.update("delete from destinations");
		 * jdbcTemplate.update("delete from services");
		 */
		throw new UnsupportedOperationException(
				"No hellish Clean-up for U bro...");
	}

	public void intiIt() throws PrivilegeException, InternalErrorException,
			OwnerNotExistsException, ServiceExistsException,
			FacilityExistsException, ServiceNotExistsException,
			FacilityNotExistsException, DestinationAlreadyAssignedException {
		// Test Owner
		testOwner = new Owner();
		testOwner.setContact("Call me babe");
		testOwner.setName("Tester-"
				+ Long.toHexString(System.currentTimeMillis()));
		testOwner = (Rpc.OwnersManager.createOwner(
				engineManager.getRpcCaller(), testOwner));

		// Test Service #1
		testService1 = new Service();
		testService1.setName("Test service 1-"
				+ Long.toHexString(System.currentTimeMillis()));
		testService1 = Rpc.ServicesManager.createService(
				engineManager.getRpcCaller(), testService1, testOwner);

		// Test Service #2
		testService2 = new Service();
		testService2.setName("Test service 2-"
				+ Long.toHexString(System.currentTimeMillis()));
		testService2 = Rpc.ServicesManager.createService(
				engineManager.getRpcCaller(), testService2, testOwner);

		// Test Service #3
		testService3 = new Service();
		testService3.setName("Test service 3-"
				+ Long.toHexString(System.currentTimeMillis()));
		testService3 = Rpc.ServicesManager.createService(
				engineManager.getRpcCaller(), testService3, testOwner);

		// Test Facility #1
		facility1 = new Facility();
		facility1.setName("Facility 1-"
				+ Long.toHexString(System.currentTimeMillis()));
		facility1 = Rpc.FacilitiesManager.createFacility(
				engineManager.getRpcCaller(), facility1);

		// Test Facility #2
		facility2 = new Facility();
		facility2.setName("Facility 2-"
				+ Long.toHexString(System.currentTimeMillis()));
		facility2 = Rpc.FacilitiesManager.createFacility(
				engineManager.getRpcCaller(), facility2);

		// Test Facility #3
		facility3 = new Facility();
		facility3.setName("Facility 3-"
				+ Long.toHexString(System.currentTimeMillis()));
		facility3 = Rpc.FacilitiesManager.createFacility(
				engineManager.getRpcCaller(), facility3);

		/*
		 * servicePasswd = new Service(); servicePasswd.setName("ENGINE-passwd"
		 * + Long.toHexString(System.currentTimeMillis())); servicePasswd =
		 * Rpc.ServicesManager.createService(engineManager.getRpcCaller(),
		 * servicePasswd, testOwner);
		 */
		servicePasswd = Rpc.ServicesManager.getServiceById(
				engineManager.getRpcCaller(), 1);

		execServicePasswdGenerate = Rpc.GeneralServiceManager.getExecService(
				engineManager.getRpcCaller(), 12951);
		execServicePasswdSend = Rpc.GeneralServiceManager.getExecService(
				engineManager.getRpcCaller(), 12950);

		assertEquals(servicePasswd, execServicePasswdGenerate.getService());
		assertEquals(servicePasswd, execServicePasswdSend.getService());

		execService1 = new ExecService();
		execService1.setDefaultDelay(10);
		execService1.setEnabled(true);
		execService1.setDefaultRecurrence(5);
		execService1.setScript(ClassLoader
				.getSystemResource("serviceSend.bash").getPath());
		execService1.setService(testService1);
		execService1.setExecServiceType(ExecServiceType.SEND);
		execService1.setId(Rpc.GeneralServiceManager.insertExecService(
				engineManager.getRpcCaller(), execService1, testOwner));

		execService2 = new ExecService();
		execService2.setDefaultDelay(10);
		execService2.setEnabled(true);
		execService2.setDefaultRecurrence(5);
		execService2.setScript(ClassLoader.getSystemResource(
				"serviceGenerate.bash").getPath());
		execService2.setService(testService1);
		execService2.setExecServiceType(ExecServiceType.GENERATE);
		execService2.setId(Rpc.GeneralServiceManager.insertExecService(
				engineManager.getRpcCaller(), execService2, testOwner));

		facility1195 = new Facility();
		facility1195.setId(1195);
		facility1195.setName("falcon");

		destinationA = new Destination();
		destinationA.setId(8307);
		destinationA.setType("host");
		destinationA.setDestination("A");

		destinationB = new Destination();
		destinationB.setId(8308);
		destinationB.setType("host");
		destinationB.setDestination("B");

		destinationC = new Destination();
		destinationC.setId(8309);
		destinationC.setType("host");
		destinationC.setDestination("C");

		// destinationA =
		// Rpc.ServicesManager.addDestination(engineManager.getRpcCaller(),
		// getServicePasswd(), getFacility1195(), destinationA);
		// destinationB =
		// Rpc.ServicesManager.addDestination(engineManager.getRpcCaller(),
		// getServicePasswd(), getFacility1195(), destinationB);
		// destinationC =
		// Rpc.ServicesManager.addDestination(engineManager.getRpcCaller(),
		// getServicePasswd(), getFacility1195(), destinationC);
	}

	public void initDestinations() throws ServiceNotExistsException,
			FacilityNotExistsException, PrivilegeException,
			InternalErrorException, DestinationAlreadyAssignedException {
		// Wooot? Thanks for code completion...
		destination1.setDestination("Destination-1-"
				+ Long.toHexString(System.currentTimeMillis()));
		destination2.setDestination("Destination-2-"
				+ Long.toHexString(System.currentTimeMillis()));
		destination3.setDestination("Destination-3-"
				+ Long.toHexString(System.currentTimeMillis()));

		destination4.setDestination("Destination-4-"
				+ Long.toHexString(System.currentTimeMillis()));
		destination5.setDestination("Destination-5-"
				+ Long.toHexString(System.currentTimeMillis()));
		destination6.setDestination("Destination-6-"
				+ Long.toHexString(System.currentTimeMillis()));

		destination7.setDestination("Destination-7-"
				+ Long.toHexString(System.currentTimeMillis()));
		destination8.setDestination("Destination-8-"
				+ Long.toHexString(System.currentTimeMillis()));
		destination9.setDestination("Destination-9-"
				+ Long.toHexString(System.currentTimeMillis()));

		destination1.setType("CLUSTER");
		destination2.setType("CLUSTER");
		destination3.setType("CLUSTER");

		destination4.setType("CLUSTER");
		destination5.setType("CLUSTER");
		destination6.setType("CLUSTER");

		destination7.setType("CLUSTER");
		destination8.setType("CLUSTER");
		destination9.setType("CLUSTER");

		destination1 = Rpc.ServicesManager.addDestination(
				engineManager.getRpcCaller(), getTestService1(),
				getFacility1(), destination1);
		destination2 = Rpc.ServicesManager.addDestination(
				engineManager.getRpcCaller(), getTestService1(),
				getFacility1(), destination2);
		destination3 = Rpc.ServicesManager.addDestination(
				engineManager.getRpcCaller(), getTestService1(),
				getFacility1(), destination3);

		destination4 = Rpc.ServicesManager.addDestination(
				engineManager.getRpcCaller(), getTestService2(),
				getFacility1(), destination1);
		destination5 = Rpc.ServicesManager.addDestination(
				engineManager.getRpcCaller(), getTestService2(),
				getFacility1(), destination2);
		destination6 = Rpc.ServicesManager.addDestination(
				engineManager.getRpcCaller(), getTestService2(),
				getFacility1(), destination3);

		destination7 = Rpc.ServicesManager.addDestination(
				engineManager.getRpcCaller(), getTestService3(),
				getFacility1(), destination1);
		destination8 = Rpc.ServicesManager.addDestination(
				engineManager.getRpcCaller(), getTestService3(),
				getFacility1(), destination2);
		destination9 = Rpc.ServicesManager.addDestination(
				engineManager.getRpcCaller(), getTestService3(),
				getFacility1(), destination3);
	}

	public void cleanUpDestinations() throws ServiceNotExistsException,
			FacilityNotExistsException, PrivilegeException,
			InternalErrorException {
		Rpc.ServicesManager.removeAllDestinations(engineManager.getRpcCaller(),
				getTestService1(), getFacility1());
		Rpc.ServicesManager.removeAllDestinations(engineManager.getRpcCaller(),
				getTestService2(), getFacility1());
		Rpc.ServicesManager.removeAllDestinations(engineManager.getRpcCaller(),
				getTestService3(), getFacility1());
	}

	public void cleanUp() {
		// Clean up
		// jdbcTemplate.update("delete from service_denials");
		// jdbcTemplate.update("delete from service_dependencies");
		// jdbcTemplate.update("delete from tasks_results");
		// jdbcTemplate.update("delete from exec_services");
		// jdbcTemplate.update("delete from tasks");
		throw new UnsupportedOperationException("No Clean-up for U bro...");
	}

	public JdbcPerunTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public Owner getTestOwner() {
		return testOwner;
	}

	public Service getTestService1() {
		return testService1;
	}

	public Service getTestService2() {
		return testService2;
	}

	public Service getTestService3() {
		return testService3;
	}

	public Facility getFacility1() {
		return facility1;
	}

	public Facility getFacility2() {
		return facility2;
	}

	public Facility getFacility3() {
		return facility3;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public ExecService getExecService1() {
		return execService1;
	}

	public ExecService getExecService2() {
		return execService2;
	}

	public Facility getFacility1195() {
		return facility1195;
	}

	public Destination getDestinationA() {
		return destinationA;
	}

	public Destination getDestinationB() {
		return destinationB;
	}

	public Destination getDestinationC() {
		return destinationC;
	}

	public Destination getDestination1() {
		return destination1;
	}

	public Destination getDestination2() {
		return destination2;
	}

	public Destination getDestination3() {
		return destination3;
	}

	public Destination getDestination4() {
		return destination4;
	}

	public Destination getDestination5() {
		return destination5;
	}

	public Destination getDestination6() {
		return destination6;
	}

	public Destination getDestination7() {
		return destination7;
	}

	public Destination getDestination8() {
		return destination8;
	}

	public Destination getDestination9() {
		return destination9;
	}

	public EngineManager getEngineManager() {
		return engineManager;
	}

	public void setEngineManager(EngineManager engineManager) {
		this.engineManager = engineManager;
	}

	public Service getServicePasswd() {
		return servicePasswd;
	}

	public void setExecServicePasswdSend(ExecService execServicePasswdSend) {
		this.execServicePasswdSend = execServicePasswdSend;
	}

	public ExecService getExecServicePasswdSend() {
		return execServicePasswdSend;
	}

	public void setExecServicePasswdGenerate(
			ExecService execServicePasswdGenerate) {
		this.execServicePasswdGenerate = execServicePasswdGenerate;
	}

	public ExecService getExecServicePasswdGenerate() {
		return execServicePasswdGenerate;
	}

}
