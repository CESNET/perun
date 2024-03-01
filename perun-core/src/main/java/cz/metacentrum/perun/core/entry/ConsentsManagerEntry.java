package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.ConsentsManager;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidConsentStatusException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.ConsentsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;

import java.util.List;
import java.util.stream.Stream;

/**
 * Consents entry logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentsManagerEntry implements ConsentsManager {

  private ConsentsManagerBl consentsManagerBl;
  private PerunBl perunBl;

  public ConsentsManagerEntry(PerunBl perunBl) {
    this.perunBl = perunBl;
    this.consentsManagerBl = perunBl.getConsentsManagerBl();
  }

  public ConsentsManagerEntry() {
  }

  public ConsentsManagerBl getConsentsManagerBl() {
    return this.consentsManagerBl;
  }

  public void setConsentsManagerBl(ConsentsManagerBl consentsManagerBl) {
    this.consentsManagerBl = consentsManagerBl;
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }


  @Override
  public List<Consent> getAllConsents(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    // auth
    if (!AuthzResolver.authorizedInternal(sess, "getAllConsents_policy")) {
      throw new PrivilegeException("getAllConsents");
    }

    return consentsManagerBl.getAllConsents(sess);
  }

  @Override
  public List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status)
      throws PrivilegeException, ConsentHubNotExistsException {
    Utils.checkPerunSession(sess);

    ConsentHub consentHub = getPerunBl().getConsentsManagerBl().getConsentHubById(sess, id);

    // remove facilities user doesn't have access to
    filterConsentHubFacilities(sess, consentHub);
    if (consentHub.getFacilities().isEmpty()) {
      // if user has access to no facilities, throw privilege exception
      throw new PrivilegeException("getConsentsForConsentHub");
    }
    List<Consent> consents = consentsManagerBl.getConsentsForConsentHub(sess, id, status);
    // set ConsentHub of Consent objects to the ConsentHub with filtered facilities
    for (Consent consent : consents) {
      consent.setConsentHub(consentHub);
    }

    return consents;
  }

  @Override
  public List<Consent> getConsentsForConsentHub(PerunSession sess, int id)
      throws PrivilegeException, ConsentHubNotExistsException {
    Utils.checkPerunSession(sess);

    ConsentHub consentHub = getPerunBl().getConsentsManagerBl().getConsentHubById(sess, id);

    // remove facilities user doesn't have access to
    filterConsentHubFacilities(sess, consentHub);
    if (consentHub.getFacilities().isEmpty()) {
      // if user has access to no facilities, throw privilege exception
      throw new PrivilegeException("getConsentsForConsentHub");
    }
    List<Consent> consents = consentsManagerBl.getConsentsForConsentHub(sess, id);
    // set ConsentHub of Consent objects to the ConsentHub with filtered facilities
    for (Consent consent : consents) {
      consent.setConsentHub(consentHub);
    }

    return consents;
  }

  @Override
  public List<Consent> getConsentsForConsentHubByResource(PerunSession sess, int resourceId)
      throws PrivilegeException, ConsentHubNotExistsException, FacilityNotExistsException, ResourceNotExistsException {
    Utils.checkPerunSession(sess);

    ConsentHub consentHub = getConsentHubByResource(sess, resourceId);
    Resource resource = getPerunBl().getResourcesManagerBl().getResourceById(sess, resourceId);
    List<Member> members = getPerunBl().getResourcesManagerBl().getAssignedMembers(sess, resource);

    List<Consent> consents = consentsManagerBl.getConsentsForConsentHub(sess, consentHub.getId());
    consents.removeIf(consent -> members.stream().noneMatch(member -> member.getUserId() == consent.getUserId()));
    for (Consent consent : consents) {
      consent.setConsentHub(consentHub);
    }

    return consents;
  }

  @Override
  public List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status)
      throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    User user = getPerunBl().getUsersManager().getUserById(sess, id);

    // auth
    if (!AuthzResolver.authorizedInternal(sess, "getConsentsForUser_int_ConsentStatus_policy", user)) {
      throw new PrivilegeException("getConsentsForUser");
    }

    List<Consent> consents = consentsManagerBl.getConsentsForUser(sess, id, status);
    consents.removeIf(consent -> consent.getConsentHub().getFacilities().stream().noneMatch(
        facility -> AuthzResolver.authorizedInternal(sess, "filter-getConsentsForUser_policy", facility, user)));
    for (Consent consent : consents) {
      filterConsentHubFacilities(sess, consent.getConsentHub());
    }


    return consents;
  }

  @Override
  public List<Consent> getConsentsForUser(PerunSession sess, int id) throws UserNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    User user = getPerunBl().getUsersManager().getUserById(sess, id);

    // auth
    if (!AuthzResolver.authorizedInternal(sess, "getConsentsForUser_int_policy", user)) {
      throw new PrivilegeException("getConsentsForUser");
    }

    List<Consent> consents = consentsManagerBl.getConsentsForUser(sess, id);
    consents.removeIf(consent -> consent.getConsentHub().getFacilities().stream().noneMatch(
        facility -> AuthzResolver.authorizedInternal(sess, "filter-getConsentsForUser_policy", facility, user)));
    for (Consent consent : consents) {
      filterConsentHubFacilities(sess, consent.getConsentHub());
    }

    return consents;
  }

  @Override
  public List<Consent> getConsentsForUserAndConsentHub(PerunSession sess, int userId, int consentHubId)
      throws PrivilegeException, UserNotExistsException, ConsentHubNotExistsException {
    Utils.checkPerunSession(sess);

    User user = getPerunBl().getUsersManager().getUserById(sess, userId);
    ConsentHub consentHub = getPerunBl().getConsentsManagerBl().getConsentHubById(sess, consentHubId);

    // auth
    if (!AuthzResolver.authorizedInternal(sess, "getConsentsForUserAndConsentHub_int_int_policy", user)) {
      throw new PrivilegeException("getConsentsForUserAndConsentHub");
    }
    List<Consent> consents = consentsManagerBl.getConsentsForUserAndConsentHub(sess, userId, consentHubId);
    consents.removeIf(consent -> consent.getConsentHub().getFacilities().stream().noneMatch(
        facility -> AuthzResolver.authorizedInternal(sess, "filter-getConsentsForUserAndConsentHub_int_int_policy",
            facility, user)));
    for (Consent consent : consents) {
      filterConsentHubFacilities(sess, consent.getConsentHub());
    }
    return consents;
  }

  @Override
  public Consent getConsentForUserAndConsentHub(PerunSession sess, int userId, int consentHubId, ConsentStatus status)
      throws PrivilegeException, UserNotExistsException, ConsentHubNotExistsException, ConsentNotExistsException {
    Utils.checkPerunSession(sess);

    User user = getPerunBl().getUsersManager().getUserById(sess, userId);
    ConsentHub consentHub = getPerunBl().getConsentsManagerBl().getConsentHubById(sess, consentHubId);

    // auth
    if (consentHub.getFacilities().stream().noneMatch(facility -> AuthzResolver.authorizedInternal(sess,
        "getConsentForUserAndConsentHub_int_int_ConsentStatus_policy", facility, user))) {
      throw new PrivilegeException("getConsentForUserAndConsentHub");
    }
    Consent consent = consentsManagerBl.getConsentForUserAndConsentHub(sess, userId, consentHubId, status);
    filterConsentHubFacilities(sess, consent.getConsentHub());

    return consent;
  }

  @Override
  public Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    Consent consent = consentsManagerBl.getConsentById(sess, id);
    User user;
    try {
      user = getPerunBl().getUsersManager().getUserById(sess, consent.getUserId());
    } catch (UserNotExistsException ex) {
      throw new InternalErrorException(ex);
    }

    // auth

    if (consent.getConsentHub().getFacilities().stream()
        .noneMatch(facility -> AuthzResolver.authorizedInternal(sess, "getConsentById_int_policy", facility, user))) {
      throw new PrivilegeException("getConsentById");
    }
    filterConsentHubFacilities(sess, consent.getConsentHub());


    return consent;
  }


  @Override
  public List<ConsentHub> getAllConsentHubs(PerunSession sess) throws PrivilegeException {
    Utils.notNull(sess, "sess");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getAllConsentHubs_policy")) {
      throw new PrivilegeException(sess, "getAllConsentHubs");
    }
    return consentsManagerBl.getAllConsentHubs(sess);
  }

  @Override
  public ConsentHub getConsentHubById(PerunSession sess, int id)
      throws ConsentHubNotExistsException, PrivilegeException {
    Utils.notNull(sess, "sess");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getConsentHubById_int_policy")) {
      throw new PrivilegeException(sess, "getConsentHubById");
    }

    // Block of code prepared for manage FACILITY ADMIN/OBSERVER roles
    // Don't forget to check roles in perun-roles.yml
		/*ConsentHub consentHub = consentsManagerBl.getConsentHubById(sess, id);
		List<Facility> facilities = consentHub.getFacilities();
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getConsentHub_policy", facility));

		// Authorization
		if (facilities.isEmpty()) {
			throw new PrivilegeException(sess, "getConsentHubById");
		}*/

    return consentsManagerBl.getConsentHubById(sess, id);
  }

  @Override
  public ConsentHub getConsentHubByName(PerunSession sess, String name)
      throws ConsentHubNotExistsException, PrivilegeException {
    Utils.notNull(sess, "sess");
    Utils.notNull(name, "name");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getConsentHubByName_String_policy")) {
      throw new PrivilegeException(sess, "getConsentHubByName");
    }

    // Block of code prepared for manage FACILITY ADMIN/OBSERVER roles
    // Don't forget to check roles in perun-roles.yml
		/*ConsentHub consentHub = consentsManagerBl.getConsentHubByName(sess, name);
		List<Facility> facilities = consentHub.getFacilities();
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getConsentHub_policy", facility));

		// Authorization
		if (facilities.isEmpty()) {
			throw new PrivilegeException(sess, "getConsentHubByName");
		}*/

    return consentsManagerBl.getConsentHubByName(sess, name);
  }

  @Override
  public ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId)
      throws ConsentHubNotExistsException, PrivilegeException, FacilityNotExistsException {
    Utils.notNull(sess, "sess");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getConsentHubByFacility_Facility_policy",
        perunBl.getFacilitiesManagerBl().getFacilityById(sess, facilityId))) {
      throw new PrivilegeException(sess, "getConsentHubByFacility");
    }
    ConsentHub consentHub = consentsManagerBl.getConsentHubByFacility(sess, facilityId);
    filterConsentHubFacilities(sess, consentHub);

    return consentHub;
  }

  @Override
  public ConsentHub getConsentHubByResource(PerunSession sess, int resourceId)
      throws ConsentHubNotExistsException, PrivilegeException, FacilityNotExistsException, ResourceNotExistsException {
    Utils.notNull(sess, "sess");

    Resource resource = perunBl.getResourcesManagerBl().getResourceById(sess, resourceId);
    Facility facility = perunBl.getFacilitiesManagerBl().getFacilityById(sess, resource.getFacilityId());

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getConsentHubByResource_Resource_policy", resource)) {
      throw new PrivilegeException(sess, "getConsentHubByResource");
    }
    ConsentHub consentHub = consentsManagerBl.getConsentHubByFacility(sess, facility.getId());
    consentHub.getFacilities().removeIf(f -> !f.equals(facility));

    return consentHub;
  }

  @Override
  public ConsentHub updateConsentHub(PerunSession sess, ConsentHub consentHub)
      throws ConsentHubNotExistsException, PrivilegeException, ConsentHubExistsException {
    Utils.checkPerunSession(sess);
    Utils.notNull(consentHub.getName(), "consentHub.name");
    if (consentHub.getName().isEmpty()) {
      throw new InternalErrorException("ConsentHub name to be updated cannot be empty.");
    }

    getConsentsManagerBl().checkConsentHubExists(sess, consentHub);

    //Authorization
    List<Facility> facilities = consentHub.getFacilities();
    for (Facility facility : facilities) {
      if (!AuthzResolver.authorizedInternal(sess, "updateConsentHub_ConsentHub_policy", facility)) {
        throw new PrivilegeException(sess, "updateConsentHub");
      }
    }
    ConsentHub returnedConsentHub = getConsentsManagerBl().updateConsentHub(sess, consentHub);
    filterConsentHubFacilities(sess, returnedConsentHub);
    return returnedConsentHub;
  }

  // Make sure that the ConsentHub object includes only facilities that the user has access to
  private void filterConsentHubFacilities(PerunSession sess, ConsentHub consentHub) {
    List<Facility> facilities = consentHub.getFacilities();
    facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getConsentHub_policy", facility));
  }

  @Override
  public Consent changeConsentStatus(PerunSession sess, Consent consent, ConsentStatus status)
      throws ConsentNotExistsException, PrivilegeException, InvalidConsentStatusException, UserNotExistsException {
    Utils.notNull(sess, "sess");
    consentsManagerBl.checkConsentExists(sess, consent);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "changeConsentStatus_Consent_ConsentStatus_policy",
        getPerunBl().getUsersManagerBl().getUserById(sess, consent.getUserId()))) {
      throw new PrivilegeException(sess, "changeConsentStatus");
    }

    return consentsManagerBl.changeConsentStatus(sess, consent, status);
  }

  @Override
  public void evaluateConsents(PerunSession sess, ConsentHub consentHub) throws PrivilegeException {
    Utils.notNull(sess, "sess");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "evaluateConsents_ConsentHub_policy", consentHub)) {
      throw new PrivilegeException(sess, "evaluateConsents");
    }
    consentsManagerBl.evaluateConsents(sess, consentHub);
  }

  @Override
  public void evaluateConsents(PerunSession sess, List<ConsentHub> consentHubs) throws PrivilegeException {
    for (ConsentHub consentHub : consentHubs) {
      evaluateConsents(sess, consentHub);
    }
  }

  @Override
  public void evaluateConsents(PerunSession sess, Service service) throws PrivilegeException {
    Utils.notNull(sess, "sess");

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "evaluateConsents_Service_policy", service)) {
      throw new PrivilegeException(sess, "evaluateConsents");
    }
    consentsManagerBl.evaluateConsents(sess, service);
  }

}
