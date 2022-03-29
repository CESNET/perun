package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.ConsentsManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests of ConsentsManager.
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class ConsentsManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {
	private final String CLASS_NAME = "ConsentsManager.";

	private ConsentsManager consentsManagerEntry;

	@Before
	public void setUp() throws Exception {
		consentsManagerEntry = perun.getConsentsManager();
	}

	@Test
	public void getConsentHubByFacility() throws Exception {
		System.out.println(CLASS_NAME + "getConsentHubByFacility");
		Facility facility = setUpFacility();

		assertEquals(consentsManagerEntry.getConsentHubByFacility(sess, facility.getId()).getFacilities().get(0), facility);
	}

	@Test
	public void getAllConsentHubs() throws Exception {
		System.out.println(CLASS_NAME + "getAllConsentHubs");

		Facility facility1 = setUpFacility();
		Facility facility2 = new Facility();
		facility2.setName("ConsentsTestFacility2");

		// createFacility method creates also new Consent Hub
		perun.getFacilitiesManager().createFacility(sess, facility2);

		assertEquals(2, consentsManagerEntry.getAllConsentHubs(sess).size());
	}

	@Test
	public void getConsentHubById() throws Exception {
		System.out.println(CLASS_NAME + "getConsentHubById");

		Facility facility = setUpFacility();
		// createFacility method creates also new Consent Hub
		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		ConsentHub returnedConsentHub = consentsManagerEntry.getConsentHubById(sess, consentHub.getId());

		assertEquals(consentHub, returnedConsentHub);
		assertThatExceptionOfType(ConsentHubNotExistsException.class).isThrownBy(
			() -> consentsManagerEntry.getConsentHubById(sess, returnedConsentHub.getId()+1));
	}

	@Test
	public void getConsentHubByName() throws Exception {
		System.out.println(CLASS_NAME + "getConsentHubByName");

		Facility facility = setUpFacility();
		ConsentHub consentHub = consentsManagerEntry.getConsentHubByName(sess, facility.getName());

		assertEquals(1, consentsManagerEntry.getAllConsentHubs(sess).size());
		assertTrue(consentHub.getFacilities().contains(facility));
		assertThatExceptionOfType(ConsentHubNotExistsException.class).isThrownBy(
			() -> consentsManagerEntry.getConsentHubByName(sess, "wrongName"));
	}

	@Test
	public void createConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "createConsentHub");
		Facility facility = setUpFacility();

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		assertTrue("id must be greater than zero", hub.getId() > 0);
	}

	@Test
	public void createExistingHub() throws Exception {
		System.out.println(CLASS_NAME + "createExistingHub");
		Facility facility = setUpFacility();

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		assertThatExceptionOfType(ConsentHubExistsException.class).isThrownBy(() -> perun.getConsentsManagerBl().createConsentHub(sess, hub));
	}

	@Test
	public void deleteConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "deleteConsentHub");
		Facility facility = setUpFacility();

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		perun.getConsentsManagerBl().deleteConsentHub(sess, hub);
		assertThatExceptionOfType(ConsentHubNotExistsException.class).isThrownBy(() -> consentsManagerEntry.getConsentHubById(sess, hub.getId()));
	}

	@Test
	public void deleteRemovedConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "deleteRemovedConsentHub");
		Facility facility = setUpFacility();

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		perun.getConsentsManagerBl().deleteConsentHub(sess, hub);
		assertThatExceptionOfType(ConsentHubAlreadyRemovedException.class).isThrownBy(() -> perun.getConsentsManagerBl().deleteConsentHub(sess, hub));
	}

	private Facility setUpFacility() throws Exception {
		Facility facility = new Facility();
		facility.setName("ConsentsTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);
		return facility;
	}

}
