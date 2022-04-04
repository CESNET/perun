package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.ConsentsManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyAssigned;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;


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
	public void createConsent() throws Exception {
		System.out.println(CLASS_NAME + "createConsent");

		User user = setUpUser("John", "Doe");

		Facility facility = setUpFacility();

		Consent consent = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		consent = perun.getConsentsManagerBl().createConsent(sess, consent);

		assertEquals(consent, consentsManagerEntry.getConsentById(sess, consent.getId()));
	}

	@Test (expected = ConsentNotExistsException.class)
	public void createConsentDeleteExistingUnsigned() throws Exception {
		System.out.println(CLASS_NAME + "createConsentDeleteExistingUnsigned");

		User user = setUpUser("John", "Doe");

		Facility facility = setUpFacility();

		Consent consentOld = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		consentOld = perun.getConsentsManagerBl().createConsent(sess, consentOld);

		Consent consentNew = new Consent(-11, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		consentNew = perun.getConsentsManagerBl().createConsent(sess, consentNew);

		perun.getConsentsManagerBl().checkConsentExists(sess, consentOld);

	}

	@Test (expected = ConsentNotExistsException.class)
	public void deleteConsent() throws Exception {
		System.out.println(CLASS_NAME + "deleteConsent");

		User user = setUpUser("John", "Doe");

		Facility facility = setUpFacility();

		Consent consent = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		perun.getConsentsManagerBl().deleteConsent(sess, consent);
		perun.getConsentsManagerBl().getConsentById(sess, consent.getId());

	}

	@Test
	public void getAllConsents() throws Exception {
		System.out.println(CLASS_NAME + "getAllConsents");

		User user = setUpUser("John", "Doe");

		Facility facility1 = setUpFacility();
		Facility facility2 = new Facility();
		facility2.setName("TestFacility2");

		// createFacility method creates also new Consent Hub
		perun.getFacilitiesManager().createFacility(sess, facility2);

		Consent consent1 = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility1.getName()), new ArrayList<>());
		Consent consent2 = new Consent(-11, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility2.getName()), new ArrayList<>());

		perun.getConsentsManagerBl().createConsent(sess, consent1);
		perun.getConsentsManagerBl().createConsent(sess, consent2);

		assertEquals(2, consentsManagerEntry.getAllConsents(sess).size());
	}

	@Test
	public void getConsentsForUser() throws Exception {
		System.out.println(CLASS_NAME + "getConsentsForUser");

		User user = setUpUser("John", "Doe");

		Facility facility1 = setUpFacility();
		Facility facility2 = new Facility();
		facility2.setName("TestFacility2");

		// createFacility method creates also new Consent Hub
		perun.getFacilitiesManager().createFacility(sess, facility2);

		Consent consent1 = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility1.getName()), new ArrayList<>());
		Consent consent2 = new Consent(-11, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility2.getName()), new ArrayList<>());

		perun.getConsentsManagerBl().createConsent(sess, consent1);
		perun.getConsentsManagerBl().createConsent(sess, consent2);


		assertEquals(2, consentsManagerEntry.getConsentsForUser(sess, user.getId()).size());
		assertEquals(2, consentsManagerEntry.getConsentsForUser(sess, user.getId(), ConsentStatus.UNSIGNED).size());
		assertEquals(0, consentsManagerEntry.getConsentsForUser(sess, user.getId(), ConsentStatus.GRANTED).size());
	}

	@Test
	public void getConsentsForConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "getConsentsForConsentHub");

		Facility facility = setUpFacility();

		User user1 = setUpUser("John", "Doe");
		User user2 = setUpUser("Donald", "Trump");

		Consent consent1 = new Consent(-1, user1.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		Consent consent2 = new Consent(-11, user2.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());

		perun.getConsentsManagerBl().createConsent(sess, consent1);
		perun.getConsentsManagerBl().createConsent(sess, consent2);

		assertEquals(2, consentsManagerEntry.getConsentsForConsentHub(sess, perun.getConsentsManager().getConsentHubByName(sess, facility.getName()).getId()).size());
		assertEquals(2, consentsManagerEntry.getConsentsForConsentHub(sess, perun.getConsentsManager().getConsentHubByName(sess, facility.getName()).getId(), ConsentStatus.UNSIGNED).size());
		assertEquals(0, consentsManagerEntry.getConsentsForConsentHub(sess, perun.getConsentsManager().getConsentHubByName(sess, facility.getName()).getId(), ConsentStatus.GRANTED).size());
	}

	@Test
	public void getConsentById() throws Exception {
		System.out.println(CLASS_NAME + "getConsentById");

		Facility facility = setUpFacility();
		User user = setUpUser("John", "Doe");

		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrDef.setType(Integer.class.getName());
		attrDef.setFriendlyName("testUserAttr");
		attrDef.setDisplayName("test user attr");

		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);

		Consent consent = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), List.of(attrDef));

		perun.getConsentsManagerBl().createConsent(sess, consent);

		Consent result = consentsManagerEntry.getConsentById(sess, consent.getId());

		assertEquals(1, result.getAttributes().size());
		assertEquals(consent, result);
		assertThat(result.getConsentHub().getFacilities()).contains(facility);
	}

	@Test
	public void getConsentsForUserAndConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "getConsentsForUserAndConsentHub");

		Facility facility = setUpFacility();
		User user = setUpUser("John", "Doe");
		Facility facility2 = new Facility();
		facility2.setName("TestFacility2");
		// createFacility method creates also new Consent Hub
		perun.getFacilitiesManager().createFacility(sess, facility2);

		Consent consent = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		perun.getConsentsManagerBl().createConsent(sess, consent);
		Consent consent2 = new Consent(-2, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility2.getName()), new ArrayList<>());
		perun.getConsentsManagerBl().createConsent(sess, consent2);

		List<Consent> result = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user.getId(), consentsManagerEntry.getConsentHubByFacility(sess, facility.getId()).getId());

		assertThat(consentsManagerEntry.getAllConsents(sess)).contains(consent, consent2);
		assertThat(result).containsOnly(consent);
		assertEquals(consent2, consentsManagerEntry.getConsentForUserAndConsentHub(sess, user.getId(), consentsManagerEntry.getConsentHubByFacility(sess, facility2.getId()).getId(), ConsentStatus.UNSIGNED));
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

	private User setUpUser(String firstName, String lastName) throws Exception {
		User user = new User();
		user.setFirstName(firstName);
		user.setMiddleName("");
		user.setLastName(lastName);
		user.setTitleBefore("");
		user.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user));

		return user;
	}
}
