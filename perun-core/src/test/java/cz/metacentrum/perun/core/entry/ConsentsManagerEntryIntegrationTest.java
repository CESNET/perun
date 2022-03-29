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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

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
	public void getAllConsentHubs() throws Exception {
		System.out.println(CLASS_NAME + "getAllConsentHubs");

		Facility facility1 = setUpFacility();
		setUpConsentHub(List.of(facility1));
		Facility facility2 = new Facility();
		facility2.setName("ConsentsTestFacility2");

		// createFacility method creates also new Consent Hub
		perun.getFacilitiesManager().createFacility(sess, facility2);

		assertEquals(3, consentsManagerEntry.getAllConsentHubs(sess).size());
	}

	@Test
	public void getConsentHubById() throws Exception {
		System.out.println(CLASS_NAME + "getConsentHubById");

		Facility facility = setUpFacility();
		ConsentHub consentHub = setUpConsentHub(List.of(facility));
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
		final ConsentHub newHub = perun.getConsentsManagerBl().createConsentHub(sess, new ConsentHub(0, "testHub", true, List.of(facility)));

		assertTrue("id must be greater than zero", newHub.getId() > 0);
	}

	@Test (expected = ConsentHubExistsException.class)
	public void createExistingHub() throws Exception {
		System.out.println(CLASS_NAME + "createExistingHub");
		Facility facility = setUpFacility();
		ConsentHub newHub = setUpConsentHub(List.of(facility));

		perun.getConsentsManagerBl().createConsentHub(sess, newHub);
	}

	@Test (expected = ConsentHubNotExistsException.class)
	public void deleteConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "deleteConsentHub");
		Facility facility = setUpFacility();
		ConsentHub newHub = setUpConsentHub(List.of(facility));

		perun.getConsentsManagerBl().deleteConsentHub(sess, newHub);
		consentsManagerEntry.getConsentHubById(sess, newHub.getId());
	}

	@Test (expected = ConsentHubAlreadyRemovedException.class)
	public void deleteRemovedConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "deleteRemovedConsentHub");
		Facility facility = setUpFacility();
		ConsentHub newHub = setUpConsentHub(List.of(facility));

		perun.getConsentsManagerBl().deleteConsentHub(sess, newHub);
		perun.getConsentsManagerBl().deleteConsentHub(sess, newHub);
	}


	private Facility setUpFacility() throws Exception {
		Facility facility = new Facility();
		facility.setName("ConsentsTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);
		return facility;
	}

	private ConsentHub setUpConsentHub(List<Facility> facilities) throws Exception {
		ConsentHub hub = new ConsentHub(0, "testHub", true, facilities);
		return perun.getConsentsManagerBl().createConsentHub(sess, hub);
	}

}
