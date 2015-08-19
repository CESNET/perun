package cz.metacentrum.perun.controller.service;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test is not yet implemented for there are extensive test of the DAOs.
 *
 * @author Michal Karm Babacek
 *
 @RunWith(SpringJUnit4ClassRunner.class)
 * @ContextConfiguration(locations = { "classpath:perun-controller.xml", "classpath:perun-core.xml", "classpath:perun-core-jdbc.xml" })
 * @TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManagerPerunController")
 *                                           //@Transactional(readOnly = true)
 */
public class GeneralServiceManagerTest {
	private final static Logger log = LoggerFactory.getLogger(GeneralServiceManagerTest.class);

	@Autowired
	private GeneralServiceManager generalServiceManager;

	@Test
	public void banExecServiceOnFacility() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void banExecServiceOnDestination() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void isExecServiceDeniedOnFacility() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void isExecServiceDeniedOnDestination() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void freeAllDenialsOnFacility() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void freeAllDenialsOnDestination() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void freeDenialOfExecServiceOnFacility() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void freeDenialOfExecServiceOnDestination() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void listDenialsForFacility() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void listDenialsForDestination() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void insertExecService() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void countExecServices() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void getExecService() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void getService() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void deleteExecService() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void listExecServices() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void listServices() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void updateExecService() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void createDependency() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void removeDependency() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void isThereDependency() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void listExecServicesDependingOn() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	@Test
	public void listExecServicesThisExecServiceDependsOn() {
		try {

			// TODO:Just a dummy...
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail();
		}
	}

	public void setServiceManager(GeneralServiceManager generalServiceManager) {
		this.generalServiceManager = generalServiceManager;
	}

	public GeneralServiceManager getServiceManager() {
		return generalServiceManager;
	}

}
