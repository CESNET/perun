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
	public void getAllConsentHubs() throws Exception {
		//TODO: test
	}

	@Test
	public void getConsentHubById() throws Exception {
		//TODO: test
	}

	@Test
	public void getConsentHubByName() throws Exception {
		//TODO: test
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
