package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.ConsentsManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyAssigned;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidConsentStatusException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAttributesCannotExtend;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Integration tests of ConsentsManager.
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class ConsentsManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {
	private final String CLASS_NAME = "ConsentsManager.";

	private ConsentsManager consentsManagerEntry;
	private User user;
	private Facility facility;
	private Resource resource;
	private Service service;
	private Vo vo;
	private Group group;
	private Member member;
	private AttributeDefinition attrDef;
	private AttributeDefinition facAttrDef;

	@Before
	public void setUp() throws Exception {
		consentsManagerEntry = perun.getConsentsManager();

		user = setUpUser("John", "Doe");
		facility = setUpFacility("ConsentsTestFacility");
		service = setUpService("testService");
		vo = setUpVo("TestVo", "TestVo");
		member = perun.getMembersManager().createMember(sess, vo, user);
		resource = setUpResource("testResource", "testResource", facility, vo);
		perun.getResourcesManagerBl().assignService(sess, resource, service);
		attrDef = setUpUserAttributeDefinition("testUserAttribute");
		facAttrDef = setUpFacilityAttributeDefinition();
		perun.getServicesManagerBl().addRequiredAttributes(sess, service, List.of(attrDef, facAttrDef));

		// add member to a group assigned to the resource
		Group testGroup = new Group("test", "test group");
		group = perun.getGroupsManagerBl().createGroup(sess, vo, testGroup);
		perun.getResourcesManagerBl().assignGroupToResource(sess, group, resource, false, false, false);
		perun.getGroupsManagerBl().addMember(sess, group, member);
	}


	@Test
	public void createConsent() throws Exception {
		System.out.println(CLASS_NAME + "createConsent");

		Consent consent = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		consent = perun.getConsentsManagerBl().createConsent(sess, consent);

		assertEquals(consent, consentsManagerEntry.getConsentById(sess, consent.getId()));
	}

	@Test
	public void createConsentDeleteExistingUnsigned() throws Exception {
		System.out.println(CLASS_NAME + "createConsentDeleteExistingUnsigned");

		Consent consentOld = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		consentOld = perun.getConsentsManagerBl().createConsent(sess, consentOld);

		Consent consentNew = new Consent(-11, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		consentNew = perun.getConsentsManagerBl().createConsent(sess, consentNew);

		Consent finalConsentOld = consentOld;
		assertThatExceptionOfType(ConsentNotExistsException.class).isThrownBy(
			() -> perun.getConsentsManagerBl().checkConsentExists(sess, finalConsentOld));
	}

	@Test
	public void createConsentOnlyPossibleAttributes() throws Exception {
		System.out.println(CLASS_NAME + "createConsentOnlyPossibleAttributes");

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());
		Service service2 = new Service(0, "TestService2");
		service2 = perun.getServicesManager().createService(sess, service2);

		Resource resource2 = new Resource(0, "TestResource2", "TestResource2", facility.getId());
		resource2 = perun.getResourcesManagerBl().createResource(sess, resource2, vo, facility);

		perun.getResourcesManagerBl().assignService(sess, resource2, service2);

		AttributeDefinition anotherDef = setUpUserAttributeDefinition("secondTestUserAttr");
		perun.getServicesManagerBl().addRequiredAttribute(sess, service2, anotherDef);

		// Consent should only have one attribute because attribute from service2
		// is on resource2 which is not assigned to the user through group
		Consent consent = new Consent(-1, user.getId(), consentHub, null);
		perun.getConsentsManagerBl().createConsent(sess, consent);

		Consent result = consentsManagerEntry.getConsentById(sess, consent.getId());

		assertEquals(1, result.getAttributes().size());
		assertThat(result.getAttributes()).contains(attrDef);
	}

	@Test
	public void deleteNonExistingConsent() throws Exception {
		System.out.println(CLASS_NAME + "deleteNonExistingConsent");

		Consent consent = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		assertThatExceptionOfType(ConsentNotExistsException.class).isThrownBy(
			() -> perun.getConsentsManagerBl().deleteConsent(sess, consent));
	}

	@Test
	public void deleteExistingConsent() throws Exception {
		System.out.println(CLASS_NAME + "deleteExistingConsent");

		Consent consent = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		consent = perun.getConsentsManagerBl().createConsent(sess, consent);

		perun.getConsentsManagerBl().deleteConsent(sess, consent);
		Consent finalConsent = consent;
		assertThatExceptionOfType(ConsentNotExistsException.class).isThrownBy(
			() -> perun.getConsentsManagerBl().getConsentById(sess, finalConsent.getId()));
	}

	@Test
	public void getAllConsents() throws Exception {
		System.out.println(CLASS_NAME + "getAllConsents");

		Facility facility2 = new Facility();
		facility2.setName("TestFacility2");

		// createFacility method creates also new Consent Hub
		perun.getFacilitiesManager().createFacility(sess, facility2);

		Consent consent1 = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		Consent consent2 = new Consent(-11, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility2.getName()), new ArrayList<>());

		perun.getConsentsManagerBl().createConsent(sess, consent1);
		perun.getConsentsManagerBl().createConsent(sess, consent2);

		assertEquals(2, consentsManagerEntry.getAllConsents(sess).size());
	}

	@Test
	public void getConsentsForUser() throws Exception {
		System.out.println(CLASS_NAME + "getConsentsForUser");

		Facility facility2 = new Facility();
		facility2.setName("TestFacility2");

		// createFacility method creates also new Consent Hub
		perun.getFacilitiesManager().createFacility(sess, facility2);

		Consent consent1 = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
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

		User user2 = setUpUser("Donald", "Trump");

		Consent consent1 = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
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

		// setupFacility method creates also new Consent Hub
		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		// Consent should only be able to have one attribute
		Consent consent = new Consent(-1, user.getId(), consentHub, List.of(attrDef, facAttrDef));
		perun.getConsentsManagerBl().createConsent(sess, consent);

		Consent result = consentsManagerEntry.getConsentById(sess, consent.getId());

		assertThat(result.getConsentHub().getFacilities()).contains(facility);
		assertEquals(1, result.getAttributes().size());
		assertThat(result.getAttributes()).contains(attrDef);
	}

	@Test
	public void getConsentsForUserAndConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "getConsentsForUserAndConsentHub");

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

		assertEquals(consentsManagerEntry.getConsentHubByFacility(sess, facility.getId()).getFacilities().get(0), facility);
	}

	@Test
	public void getAllConsentHubs() throws Exception {
		System.out.println(CLASS_NAME + "getAllConsentHubs");

		Facility facility2 = new Facility();
		facility2.setName("ConsentsTestFacility2");

		// createFacility method creates also new Consent Hub
		perun.getFacilitiesManager().createFacility(sess, facility2);

		assertEquals(2, consentsManagerEntry.getAllConsentHubs(sess).size());
	}

	@Test
	public void getConsentHubById() throws Exception {
		System.out.println(CLASS_NAME + "getConsentHubById");

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

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByName(sess, facility.getName());

		assertEquals(1, consentsManagerEntry.getAllConsentHubs(sess).size());
		assertTrue(consentHub.getFacilities().contains(facility));
		assertThatExceptionOfType(ConsentHubNotExistsException.class).isThrownBy(
			() -> consentsManagerEntry.getConsentHubByName(sess, "wrongName"));
	}

	@Test
	public void getConsentHubsByService() throws Exception {
		System.out.println(CLASS_NAME + "getConsentHubsByService");

		List<ConsentHub> consentHubs = perun.getConsentsManagerBl().getConsentHubsByService(sess, service.getId());

		assertEquals(1, consentsManagerEntry.getAllConsentHubs(sess).size());

		assertEquals(1, consentHubs.size());
		assertEquals(1, consentHubs.get(0).getFacilities().size());

		List<Service> assignedServicesToFacility = perun.getServicesManagerBl().getAssignedServices(sess, consentHubs.get(0).getFacilities().get(0));

		assertEquals(1, assignedServicesToFacility.size());
		assertEquals(service, assignedServicesToFacility.get(0));
	}

	@Test
	public void createConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "createConsentHub");

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		assertTrue("id must be greater than zero", hub.getId() > 0);
	}

	@Test
	public void createExistingHub() throws Exception {
		System.out.println(CLASS_NAME + "createExistingHub");

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		assertThatExceptionOfType(ConsentHubExistsException.class).isThrownBy(() -> perun.getConsentsManagerBl().createConsentHub(sess, hub));
	}

	@Test
	public void deleteConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "deleteConsentHub");

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		perun.getConsentsManagerBl().deleteConsentHub(sess, hub);
		assertThatExceptionOfType(ConsentHubNotExistsException.class).isThrownBy(() -> consentsManagerEntry.getConsentHubById(sess, hub.getId()));
	}

	@Test
	public void deleteRemovedConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "deleteRemovedConsentHub");

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		perun.getConsentsManagerBl().deleteConsentHub(sess, hub);
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> perun.getConsentsManagerBl().deleteConsentHub(sess, hub));
	}

	@Test
	public void deleteLastFacilityRemovesConsentHub() throws Exception {
		System.out.println(CLASS_NAME + "deleteLastFacilityRemovesConsentHub");

		// createFacility method creates also new Consent Hub
		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		perun.getFacilitiesManagerBl().deleteFacility(sess, facility, true);
		assertThatExceptionOfType(InternalErrorException.class).isThrownBy(() -> perun.getConsentsManagerBl().deleteConsentHub(sess, hub));
	}

	@Test
	public void updateFacilityHub() throws Exception {
		System.out.println(CLASS_NAME + "updateFacilityHub");

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

		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		perun.getConsentsManagerBl().removeFacility(sess, hub, facility);
		assertThatExceptionOfType(ConsentHubNotExistsException.class).isThrownBy(() -> perun.getConsentsManagerBl().checkConsentHubExists(sess, hub));
	}

	@Test
	public void addFacilityToConsentHubAgain() throws Exception {
		System.out.println(CLASS_NAME + "addFacilityToConsentHubAgain");

		ConsentHub hub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		assertThatExceptionOfType(FacilityAlreadyAssigned.class).isThrownBy(() -> perun.getConsentsManagerBl().addFacility(sess, hub, facility));
	}

	@Test
	public void changeConsentStatus() throws Exception {
		System.out.println(CLASS_NAME + "changeConsentStatus");

		ConsentStatus status = ConsentStatus.GRANTED;

		Consent consent = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		perun.getConsentsManagerBl().createConsent(sess, consent);

		Consent updatedConsent = consentsManagerEntry.changeConsentStatus(sess, consent, status);
		assertEquals("Updated consent has different status than sent consent.", status, updatedConsent.getStatus());
	}

	@Test
	public void changeConsentStatusAndRemoveConsent() throws Exception {
		System.out.println(CLASS_NAME + "changeConsentStatusAndRemoveConsent");

		ConsentStatus status = ConsentStatus.GRANTED;

		Consent consent1 = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		perun.getConsentsManagerBl().createConsent(sess, consent1);
		consentsManagerEntry.changeConsentStatus(sess, consent1, status);

		Consent consent2 = new Consent(-2, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		perun.getConsentsManagerBl().createConsent(sess, consent2);
		consentsManagerEntry.changeConsentStatus(sess, consent2, status);

		assertThatExceptionOfType(ConsentNotExistsException.class).isThrownBy(() -> perun.getConsentsManagerBl().getConsentById(sess, consent1.getId()));
	}

	@Test
	public void changeConsentStatusToUnsigned() throws Exception {
		System.out.println(CLASS_NAME + "changeConsentStatusToUnsigned");

		Consent consent = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		perun.getConsentsManagerBl().createConsent(sess, consent);
		consentsManagerEntry.changeConsentStatus(sess, consent, ConsentStatus.GRANTED);

		assertThatExceptionOfType(InvalidConsentStatusException.class).isThrownBy(() -> perun.getConsentsManagerBl().changeConsentStatus(sess, consent, ConsentStatus.UNSIGNED));
	}

	@Test
	public void changeConsentStatusToSameValue() throws Exception {
		System.out.println(CLASS_NAME + "changeConsentStatusToSameValue");

		ConsentStatus status = ConsentStatus.GRANTED;

		Consent consent = new Consent(-1, user.getId(), perun.getConsentsManager().getConsentHubByName(sess, facility.getName()), new ArrayList<>());
		perun.getConsentsManagerBl().createConsent(sess, consent);
		consentsManagerEntry.changeConsentStatus(sess, consent, status);

		assertThatExceptionOfType(InvalidConsentStatusException.class).isThrownBy(() -> perun.getConsentsManagerBl().changeConsentStatus(sess, consent, status));
	}

	@Test
	public void evaluateConsentsIsTurnedOffOnInstance() {
		System.out.println(CLASS_NAME + "evaluateConsentsIsTurnedOffOnInstance");

		List<Member> allowedMembers = perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
		// didn't sign any consent but should be still returned because consents are turned off in the instance config
		assertThat(allowedMembers).containsExactly(member);
	}

	@Test
	public void evaluateConsentsIsTurnedOffOnHub() throws Exception {
		System.out.println(CLASS_NAME + "evaluateConsentsIsTurnedOffOnHub");

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());
		consentHub.setEnforceConsents(false);
		consentsManagerEntry.updateConsentHub(sess, consentHub);

		List<Member> allowedMembers;
		boolean originalForce = BeansUtils.getCoreConfig().getForceConsents();
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			allowedMembers = perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
		}

		// didn't sign any consent but should be still returned because consents are turned off in the consent hub
		assertThat(allowedMembers).containsExactly(member);
	}

	@Test
	public void evaluateConsentsCreatesConsents() throws Exception {
		System.out.println(CLASS_NAME + "evaluateConsentsCreatesConsents");

		boolean originalForce = BeansUtils.getCoreConfig().getForceConsents();
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
		}

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());
		List<Consent> consents = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user.getId(), consentHub.getId());
		Consent expectedConsent = new Consent(consents.get(0).getId(), user.getId(), consentHub, List.of(attrDef));
		assertThat(consents).containsExactly(expectedConsent);

		// evaluate again, still should have only the one UNSIGNED consent
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
		}
		assertThat(consents).containsExactly(expectedConsent);
	}

	@Test
	public void evaluateConsentsWithSufficientGrantedConsent() throws Exception {
		System.out.println(CLASS_NAME + "evaluateConsentsWithSufficientGrantedConsent");

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		List<Member> allowedMembers;
		boolean originalForce = BeansUtils.getCoreConfig().getForceConsents();
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			// evaluate to create UNSIGNED consent
			perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
			Consent unsignedConsent = consentsManagerEntry.getConsentForUserAndConsentHub(sess, user.getId(), consentHub.getId(), ConsentStatus.UNSIGNED);
			consentsManagerEntry.changeConsentStatus(sess, unsignedConsent, ConsentStatus.GRANTED);

			allowedMembers = perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
		}

		assertThat(allowedMembers).containsExactly(member);
	}

	@Test
	public void evaluateConsentsWithSufficientRevokedConsent() throws Exception {
		System.out.println(CLASS_NAME + "evaluateConsentsWithSufficientRevokedConsent");

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		List<Member> allowedMembers;
		boolean originalForce = BeansUtils.getCoreConfig().getForceConsents();
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			// evaluate to create UNSIGNED consent
			perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
			Consent unsignedConsent = consentsManagerEntry.getConsentForUserAndConsentHub(sess, user.getId(), consentHub.getId(), ConsentStatus.UNSIGNED);
			consentsManagerEntry.changeConsentStatus(sess, unsignedConsent, ConsentStatus.REVOKED);

			allowedMembers = perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
		}

		assertThat(allowedMembers).isEmpty();
	}

	@Test
	public void evaluateConsentsWithInsufficientGrantedConsent() throws Exception {
		System.out.println(CLASS_NAME + "evaluateConsentsWithInsufficientGrantedConsent");

		AttributeDefinition secondUserAttrDef = setUpUserAttributeDefinition("testUserAttribute2");

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		List<Member> allowedMembers;
		boolean originalForce = BeansUtils.getCoreConfig().getForceConsents();
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			// evaluate to create UNSIGNED consent and then grant consent
			perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
			Consent unsignedConsent = consentsManagerEntry.getConsentForUserAndConsentHub(sess, user.getId(), consentHub.getId(), ConsentStatus.UNSIGNED);
			consentsManagerEntry.changeConsentStatus(sess, unsignedConsent, ConsentStatus.GRANTED);

			// add a new required attribute and evaluate, it should create a new unsigned consent
			addAttributeToService(service, secondUserAttrDef);
			allowedMembers = perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
		}

		assertThat(allowedMembers).isEmpty();
		Consent unsignedConsent = consentsManagerEntry.getConsentForUserAndConsentHub(sess, user.getId(), consentHub.getId(), ConsentStatus.UNSIGNED);
		assertThat(unsignedConsent.getAttributes()).containsExactlyInAnyOrder(attrDef, secondUserAttrDef);
		Consent grantedConsent = consentsManagerEntry.getConsentForUserAndConsentHub(sess, user.getId(), consentHub.getId(), ConsentStatus.GRANTED);
		assertThat(grantedConsent.getAttributes()).containsExactlyInAnyOrder(attrDef);
		// make sure there is no revoked consent
		assertThatExceptionOfType(ConsentNotExistsException.class)
			.isThrownBy(() -> consentsManagerEntry.getConsentForUserAndConsentHub(sess, user.getId(), consentHub.getId(), ConsentStatus.REVOKED));
	}

	@Test
	public void evaluateConsentsWithInsufficientUnsignedConsent() throws Exception {
		System.out.println(CLASS_NAME + "evaluateConsentsWithInsufficientUnsignedConsent");

		AttributeDefinition secondUserAttrDef = setUpUserAttributeDefinition("testUserAttribute2");

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		List<Member> allowedMembers;
		boolean originalForce = BeansUtils.getCoreConfig().getForceConsents();
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			// evaluate to create UNSIGNED consent
			perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));

			// add a new required attribute and evaluate, it should create a new unsigned consent
			addAttributeToService(service, secondUserAttrDef);
			allowedMembers = perun.getConsentsManagerBl().evaluateConsents(sess, service, facility, List.of(member));
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
		}

		assertThat(allowedMembers).isEmpty();
		List<Consent> userConsents = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user.getId(), consentHub.getId());
		assertThat(userConsents).hasSize(1);
		assertThat(userConsents.get(0).getAttributes()).containsExactlyInAnyOrder(attrDef, secondUserAttrDef);
		assertThat(userConsents.get(0).getStatus()).isEqualTo(ConsentStatus.UNSIGNED);
	}

	@Test
	public void evaluateConsentsForConsentHubCreatesConsents() throws Exception {
		System.out.println(CLASS_NAME + "evaluateConsentsForConsentHubCreatesConsents");

		User user1 = setUpUser("Harry", "Doe");
		User user2 = setUpUser("James", "Doe");
		Member member1 = perun.getMembersManager().createMember(sess, vo, user1);
		Member member2 = perun.getMembersManager().createMember(sess, vo, user2);
		perun.getGroupsManagerBl().addMember(sess, group, member1);
		perun.getGroupsManagerBl().addMember(sess, group, member2);
		perun.getGroupsManager().setMemberGroupStatus(sess, member1, group, MemberGroupStatus.EXPIRED);
		perun.getGroupsManager().setMemberGroupStatus(sess, member2, group, MemberGroupStatus.VALID);

		// validate both members in Vo, otherwise they will be skipped
		perun.getMembersManagerBl().validateMember(sess, member1);
		perun.getMembersManagerBl().validateMember(sess, member2);

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		boolean originalForce = BeansUtils.getCoreConfig().getForceConsents();
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			perun.getConsentsManagerBl().evaluateConsents(sess, consentHub);
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
		}

		List<Consent> consentsUser1 = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user1.getId(), consentHub.getId());
		List<Consent> consentsUser2 = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user2.getId(), consentHub.getId());

		Consent expectedConsent1 = new Consent(consentsUser1.get(0).getId(), user1.getId(), consentHub, List.of(attrDef));
		assertThat(consentsUser1).containsExactly(expectedConsent1);
		Consent expectedConsent2 = new Consent(consentsUser2.get(0).getId(), user2.getId(), consentHub, List.of(attrDef));
		assertThat(consentsUser2).containsExactly(expectedConsent2);

		// evaluate again, still should have only the one UNSIGNED consent
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			perun.getConsentsManagerBl().evaluateConsents(sess, consentHub);
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
		}
		consentsUser1 = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user1.getId(), consentHub.getId());
		assertThat(consentsUser1).containsExactly(expectedConsent1);
		consentsUser2 = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user2.getId(), consentHub.getId());
		assertThat(consentsUser2).containsExactly(expectedConsent2);
	}

	@Test
	public void evaluateConsentsForConsentHubUseExpiredMembersCreatesConsents() throws Exception {
		System.out.println(CLASS_NAME + "evaluateConsentsForConsentHubUseExpiredMembersCreatesConsents");

		boolean originalUseExpiredMembers = service.isUseExpiredMembers();

		User user1 = setUpUser("Harry", "Doe");
		User user2 = setUpUser("James", "Doe");
		Member member1 = perun.getMembersManager().createMember(sess, vo, user1);
		Member member2 = perun.getMembersManager().createMember(sess, vo, user2);
		perun.getGroupsManagerBl().addMember(sess, group, member1);
		perun.getGroupsManagerBl().addMember(sess, group, member2);
		perun.getGroupsManager().setMemberGroupStatus(sess, member1, group, MemberGroupStatus.VALID);
		perun.getGroupsManager().setMemberGroupStatus(sess, member2, group, MemberGroupStatus.EXPIRED);

		// validate both members in VOs, otherwise they will be skipped
		perun.getMembersManagerBl().validateMember(sess, member1);
		perun.getMembersManagerBl().validateMember(sess, member2);

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());

		boolean originalForce = BeansUtils.getCoreConfig().getForceConsents();
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			service.setUseExpiredMembers(true);
			perun.getServicesManagerBl().updateService(sess, service);

			perun.getConsentsManagerBl().evaluateConsents(sess, consentHub);
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
			service.setUseExpiredMembers(originalUseExpiredMembers);
			perun.getServicesManagerBl().updateService(sess, service);
		}

		List<Consent> consents1 = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user1.getId(), consentHub.getId());
		List<Consent> consents2 = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user2.getId(), consentHub.getId());

		Consent expectedConsent1 = new Consent(consents1.get(0).getId(), user1.getId(), consentHub, List.of(attrDef));
		Consent expectedConsent2 = new Consent(consents2.get(0).getId(), user2.getId(), consentHub, List.of(attrDef));

		assertThat(consents1).containsExactly(expectedConsent1);
		assertThat(consents2).containsExactly(expectedConsent2);

		// evaluate again, each user still should have only the one UNSIGNED consent
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			service.setUseExpiredMembers(true);
			perun.getServicesManagerBl().updateService(sess, service);

			perun.getConsentsManagerBl().evaluateConsents(sess, consentHub);
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
			service.setUseExpiredMembers(originalUseExpiredMembers);
			perun.getServicesManagerBl().updateService(sess, service);

		}

		consents1 = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user1.getId(), consentHub.getId());
		consents2 = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user2.getId(), consentHub.getId());
		assertThat(consents1).containsExactly(expectedConsent1);
		assertThat(consents2).containsExactly(expectedConsent2);
	}

	@Test
	public void createConsentNotIncludeAttrFromExpiredServices() throws Exception {
		System.out.println(CLASS_NAME + "createConsentNotIncludeAttrFromExpiredServices");

		boolean originalUseExpiredMembers1 = service.isUseExpiredMembers();

		User user1 = setUpUser("Harry", "Doe");
		Member member1 = perun.getMembersManager().createMember(sess, vo, user1);
		perun.getGroupsManagerBl().addMember(sess, group, member1);
		perun.getGroupsManager().setMemberGroupStatus(sess, member1, group, MemberGroupStatus.EXPIRED);
		perun.getMembersManagerBl().validateMember(sess, member1);

		AttributeDefinition secondUserAttrDef = setUpUserAttributeDefinition("testUserAttribute2");
		Service service2 = setUpService("service2");
		boolean originalUseExpiredMembers2 = service2.isUseExpiredMembers();
		perun.getResourcesManagerBl().assignService(sess, resource, service2);
		addAttributeToService(service2, secondUserAttrDef);

		ConsentHub consentHub = consentsManagerEntry.getConsentHubByFacility(sess, facility.getId());
		Consent consent;

		boolean originalForce = BeansUtils.getCoreConfig().getForceConsents();
		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			service.setUseExpiredMembers(true);
			service2.setUseExpiredMembers(true);
			perun.getServicesManagerBl().updateService(sess, service);
			perun.getServicesManagerBl().updateService(sess, service2);

			// create consent, it should include both attributes, since both services use expired members
			consent = perun.getConsentsManagerBl().createConsent(sess, new Consent(-1, member1.getUserId(), consentHub, null));
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
			service.setUseExpiredMembers(originalUseExpiredMembers1);
			service2.setUseExpiredMembers(originalUseExpiredMembers2);
			perun.getServicesManagerBl().updateService(sess, service);
			perun.getServicesManagerBl().updateService(sess, service2);
		}

		List<Consent> userConsents = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user1.getId(), consentHub.getId());
		assertThat(userConsents).hasSize(1);
		assertThat(userConsents.get(0).getAttributes()).containsExactlyInAnyOrder(attrDef, secondUserAttrDef);
		assertThat(userConsents.get(0).getStatus()).isEqualTo(ConsentStatus.UNSIGNED);

		try {
			BeansUtils.getCoreConfig().setForceConsents(true);
			service.setUseExpiredMembers(true);
			service2.setUseExpiredMembers(false);
			perun.getServicesManagerBl().updateService(sess, service);
			perun.getServicesManagerBl().updateService(sess, service2);


			// create consent, it should include only first attribute, since service2 doesn't use expired members
			consent = perun.getConsentsManagerBl().createConsent(sess, new Consent(-1, member1.getUserId(), consentHub, null));
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(originalForce);
			service.setUseExpiredMembers(originalUseExpiredMembers1);
			service2.setUseExpiredMembers(originalUseExpiredMembers2);
			perun.getServicesManagerBl().updateService(sess, service);
		}

		userConsents = consentsManagerEntry.getConsentsForUserAndConsentHub(sess, user1.getId(), consentHub.getId());
		assertThat(userConsents).hasSize(1);
		assertThat(userConsents.get(0).getAttributes()).containsExactlyInAnyOrder(attrDef);
		assertThat(userConsents.get(0).getStatus()).isEqualTo(ConsentStatus.UNSIGNED);
	}

		/* ------- PRIVATE METHODS -------- */

	private void addAttributeToService(Service service, AttributeDefinition secondUserAttrDef) throws AttributeAlreadyAssignedException, ServiceAttributesCannotExtend {
		service.setEnabled(false);
		perun.getServicesManagerBl().updateService(sess, service);
		perun.getServicesManagerBl().addRequiredAttribute(sess, service, secondUserAttrDef);
		service.setEnabled(true);
		perun.getServicesManagerBl().updateService(sess, service);
	}

	private AttributeDefinition setUpUserAttributeDefinition(String name) throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrDef.setType(Integer.class.getName());
		attrDef.setFriendlyName(name);
		attrDef.setDisplayName(name);
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		return attrDef;
	}

	private AttributeDefinition setUpFacilityAttributeDefinition() throws Exception {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attrDef.setType(Integer.class.getName());
		attrDef.setFriendlyName("testFacAttr");
		attrDef.setDisplayName("test facility attr");
		attrDef = perun.getAttributesManagerBl().createAttribute(sess, attrDef);
		return attrDef;
	}

	private Resource setUpResource(String name, String description, Facility facility, Vo vo) throws Exception {
		Resource resource = new Resource(0, name, description, facility.getId());
		resource = perun.getResourcesManagerBl().createResource(sess, resource, vo, facility);
		return resource;
	}

	private Facility setUpFacility(String name) throws Exception {
		Facility facility = new Facility();
		facility.setName(name);
		facility = perun.getFacilitiesManager().createFacility(sess, facility);
		return facility;
	}

	private User setUpUser(String firstName, String lastName) {
		User user = new User();
		user.setFirstName(firstName);
		user.setMiddleName("");
		user.setLastName(lastName);
		user.setTitleBefore("");
		user.setTitleAfter("");
		assertNotNull(perun.getUsersManagerBl().createUser(sess, user));

		return user;
	}

	private Service setUpService(String name) throws Exception {
		Service service = new Service(0, name);
		service = perun.getServicesManager().createService(sess, service);
		return service;
	}

	private Vo setUpVo(String name, String shortName) throws VoExistsException, PrivilegeException {
		Vo vo = new Vo(0, name, shortName);
		vo = perun.getVosManager().createVo(sess, vo);
		return vo;
	}
}
