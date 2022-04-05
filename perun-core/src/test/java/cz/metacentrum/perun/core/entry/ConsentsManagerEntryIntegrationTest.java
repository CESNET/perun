package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.ConsentsManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyAssigned;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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

	@Test
	public void deleteLastFacilityRemovesConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "deleteLastFacilityRemovesConsentHub");
		Facility facility = setUpFacility();

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		perun.getFacilitiesManagerBl().deleteFacility(sess, facility, true);
		assertThatExceptionOfType(ConsentHubAlreadyRemovedException.class).isThrownBy(() -> perun.getConsentsManagerBl().deleteConsentHub(sess, hub));
	}

	@Test
	public void updateFacilityHub() throws Exception {
		System.out.println(CLASS_NAME + "updateFacilityHub");
		Facility facility = setUpFacility();

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());
		hub.setName("perunHub");
		hub.setEnforceConsents(false);
		// facilities should be ignored for update, otherwise would throw exception
		hub.setFacilities(List.of(facility, facility));

		ConsentHub updatedHub = consentsManagerEntry.updateConsentHub(sess, hub);
		assertEquals("Updated hub has different name than sent hub", updatedHub.getName(), hub.getName());
		assertEquals("Updated hub has different enforce rules set than sent hub", updatedHub.isEnforceConsents(), hub.isEnforceConsents());
	}

	@Test
	public void updateFacilityHubDuplicateName() throws Exception {
		System.out.println(CLASS_NAME + "updateFacilityHubDuplicateName");
		Facility facility = setUpFacility();

		Facility facility2 = new Facility();
		facility2.setName("ConsentsTestFacility2");
		// createFacility method creates also new Consent Hub with facility's name
		perun.getFacilitiesManager().createFacility(sess, facility2);


		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());
		hub.setName(facility2.getName());

		assertThatExceptionOfType(ConsentHubExistsException.class).isThrownBy(() -> consentsManagerEntry.updateConsentHub(sess, hub));
	}

	@Test
	public void removeLastFacilityRemovesConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "removeLastFacilityRemovesConsentHub");
		Facility facility = setUpFacility();

		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		perun.getConsentsManagerBl().removeFacility(sess, hub, facility);
		assertThatExceptionOfType(ConsentHubNotExistsException.class).isThrownBy(() -> perun.getConsentsManagerBl().checkConsentHubExists(sess, hub));
	}

	@Test
	public void addFacilityToConsentHubAgain() throws Exception {
		System.out.println(CLASS_NAME + "addFacilityToConsentHubAgain");
		Facility facility = setUpFacility();

		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		assertThatExceptionOfType(FacilityAlreadyAssigned.class).isThrownBy(() -> perun.getConsentsManagerBl().addFacility(sess, hub, facility));
	}

	private Facility setUpFacility() throws Exception {
		Facility facility = new Facility();
		facility.setName("ConsentsTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);
		return facility;
	}

}
