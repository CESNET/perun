package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.ConsentExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyAssigned;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidConsentStatusException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import java.util.List;

/**
 * Consents BL logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public interface ConsentsManagerBl {


  /**
   * Gel all consents
   *
   * @param sess perun session
   * @return all existing consents in the database
   */
  List<Consent> getAllConsents(PerunSession sess);

  /**
   * Get all consents for chosen ConsentHub with the specified status
   *
   * @param sess   perun session
   * @param id     id of the ConsentHub
   * @param status status of the consent
   * @return consents for chosen ConsentHub with the specified status
   */
  List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status);

  /**
   * Get all consents for chosen ConsentHub
   *
   * @param sess perun session
   * @param id   id of the ConsentHub
   * @return consents for chosen ConsentHub
   */
  List<Consent> getConsentsForConsentHub(PerunSession sess, int id);

  /**
   * Get all consents for chosen User with the specified status
   *
   * @param sess   perun session
   * @param id     id of the User
   * @param status status of the consent
   * @return consents for chosen User with the specified status
   */
  List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status);

  /**
   * Get all consents for chosen User
   *
   * @param sess perun session
   * @param id   id of the user
   * @return consents for chosen User
   */
  List<Consent> getConsentsForUser(PerunSession sess, int id);

  /**
   * Get all consents for chosen user in specified consent hub
   *
   * @param sess         perun session
   * @param userId       id of the user
   * @param consentHubId id of the consent hub
   * @return consents
   */
  List<Consent> getConsentsForUserAndConsentHub(PerunSession sess, int userId, int consentHubId);

  /**
   * Get consent for chosen user in specified consent hub with specified status
   *
   * @param sess         perun session
   * @param userId       if of the user
   * @param consentHubId id of the consent hub
   * @param status       specified status
   * @return consent
   * @throws ConsentNotExistsException thrown if consent with the id doesn't exist
   */
  Consent getConsentForUserAndConsentHub(PerunSession sess, int userId, int consentHubId, ConsentStatus status)
      throws ConsentNotExistsException;

  /**
   * Get consent object with specified id
   *
   * @param sess perun session
   * @param id   id of desired consent object
   * @return consent object with specified id
   * @throws ConsentNotExistsException thrown if consent with the id doesn't exist
   */
  Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException;

  /**
   * Check if consent exists in underlying data source.
   *
   * @param sess    PerunSession
   * @param consent Consent to check
   * @throws ConsentNotExistsException if consent doesn't exist
   */
  void checkConsentExists(PerunSession sess, Consent consent) throws ConsentNotExistsException;

  /**
   * Creates a new consent with status 'UNSIGNED'.
   * Consent's attributes are computed based on
   * the attributes of the user and the consent hub.
   * Attributes in the consent are ignored!
   * <p>
   * If the user has already 'UNSIGNED' consent for the same consent hub,
   * that consent is deleted and replaced with the new one.
   *
   * @param perunSession perun session
   * @param consent      Consent to create
   * @return created consent
   * @throws ConsentExistsException       if consent already exists
   * @throws ConsentHubNotExistsException if consent hub doesn't exist
   * @throws UserNotExistsException       if a user with consent's userId doesn't exist
   */
  Consent createConsent(PerunSession perunSession, Consent consent)
      throws ConsentExistsException, ConsentHubNotExistsException, UserNotExistsException;

  /**
   * Deletes consent
   *
   * @param sess    perun session
   * @param consent consent to delete
   * @throws ConsentNotExistsException if consent doesn't exist
   */
  void deleteConsent(PerunSession sess, Consent consent) throws ConsentNotExistsException;

  /**
   * Get list of all Consent Hubs
   *
   * @param sess perun session
   * @return list of Consent Hubs
   * @throws InternalErrorException if an exception occurred
   */
  List<ConsentHub> getAllConsentHubs(PerunSession sess);

  /**
   * Finds existing Consent Hub by id.
   *
   * @param sess perun session
   * @param id   id of the Consent Hub you are looking for
   * @return found Consent Hub
   * @throws ConsentHubNotExistsException if Consent Hub doesn't exist
   * @throws InternalErrorException       if an exception occurred
   */
  ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException;

  /**
   * Finds existing Consent Hub by name.
   *
   * @param sess perun session
   * @param name name of the Consent Hub you are looking for
   * @return found Consent Hub
   * @throws ConsentHubNotExistsException if Consent Hub doesn't exist
   * @throws InternalErrorException       if an exception occurred
   */
  ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException;

  /**
   * Finds existing Consent Hub by facility id.
   *
   * @param sess       perun session
   * @param facilityId facility for which consent hub is searched
   * @return found Consent Hub
   * @throws ConsentHubNotExistsException if Consent Hub doesn't exist
   */
  ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException;

  /**
   * Finds all existing Consent Hubs by service id (consent hubs that have given service assigned through facilities).
   *
   * @param sess      perun session
   * @param serviceId service for which consent hubs is searched
   * @return found Consent Hubs
   * @throws ConsentHubNotExistsException if Consent Hub doesn't exist
   */
  List<ConsentHub> getConsentHubsByService(PerunSession sess, int serviceId) throws ConsentHubNotExistsException;

  /**
   * Creates new consent hub.
   *
   * @param perunSession session
   * @param consentHub   consent hub
   * @return new consent hub
   * @throws ConsentHubExistsException if consent hub with similar name exists
   */
  ConsentHub createConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubExistsException;

  /**
   * Deletes consent hub.
   *
   * @param perunSession session
   * @param consentHub   consent hub
   * @throws ConsentHubAlreadyRemovedException if no such consent hub stored in db
   */
  void deleteConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubAlreadyRemovedException;

  /**
   * Returns true, if consent hub exists, false otherwise.
   *
   * @param sess       session
   * @param consentHub consent hub
   * @return whether consent hub exists
   */
  boolean consentHubExists(PerunSession sess, ConsentHub consentHub);

  /**
   * Throws exception if consent hub does not exist.
   *
   * @param sess       session
   * @param consentHub consent hub
   * @throws ConsentHubNotExistsException if consent hub does not exist
   */
  void checkConsentHubExists(PerunSession sess, ConsentHub consentHub) throws ConsentHubNotExistsException;

  /**
   * Updates the ConsentHub. Ignores related facilities.
   *
   * @param perunSession session
   * @param consentHub   consentHub
   * @return updated consent hub
   * @throws ConsentHubExistsException if consent hub with the same name exists
   */
  ConsentHub updateConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubExistsException;

  /**
   * Adds facility to consent hub.
   *
   * @param sess       session
   * @param consentHub consent hub
   * @param facility   facility to be added
   * @throws FacilityAlreadyAssigned if facility is already assigned to consent hub
   */
  void addFacility(PerunSession sess, ConsentHub consentHub, Facility facility) throws FacilityAlreadyAssigned;

  /**
   * Removes facility from consent hub.
   * If it was the last facility on consent hub, deletes consent hub too.
   *
   * @param sess       session
   * @param consentHub consent hub
   * @param facility   facility to be removed
   * @throws RelationNotExistsException        if facility is not assigned to consent hub
   * @throws ConsentHubAlreadyRemovedException if the last facility was removed and deletion of consent hub failed
   */
  void removeFacility(PerunSession sess, ConsentHub consentHub, Facility facility)
      throws RelationNotExistsException, ConsentHubAlreadyRemovedException;

  /**
   * Set consent status
   *
   * @param sess    perun session
   * @param consent consent
   * @return consent
   * @throws InvalidConsentStatusException if passed status value can not be set
   */
  Consent changeConsentStatus(PerunSession sess, Consent consent, ConsentStatus status)
      throws InvalidConsentStatusException;

  /**
   * This method runs in a new transaction!! Because it is used by getData methods
   * which run in read-only serializable transactions.
   * <p>
   * Returns members from the given members list that have a valid consent for the propagation
   * of the service required attributes to the facility.
   * <p>
   * Users must have a consent for the facility's consent hub with status GRANTED
   * and it contains all the required attributes.
   * <p>
   * If the user has no consent in any status that contains all the required attributes,
   * a new UNSIGNED consent is created for the user.
   * <p>
   * If the consent logic is turned off on the instance (property forceConsents)
   * or the facility's consent hub doesn't enforce consents, all members are returned
   * and no unsigned consents for users are created.
   *
   * @param sess     perun session
   * @param service  the service
   * @param facility the facility
   * @param members  the given members
   */
  List<Member> evaluateConsents(PerunSession sess, Service service, Facility facility, List<Member> members);

  /**
   * This method runs in a new transaction!! Because it is used by getData methods
   * which run in read-only serializable transactions.
   * <p>
   * Returns members from the given members list that have a valid consent for the propagation
   * of the service required attributes to the facility.
   * <p>
   * Users must have a consent for the facility's consent hub with status GRANTED
   * and it contains all the required attributes.
   * <p>
   * If the user has no consent in any status that contains all the required attributes,
   * a new UNSIGNED consent is created for the user.
   * <p>
   * If the consent logic is turned off on the instance (property forceConsents)
   * or the facility's consent hub doesn't enforce consents, all members are returned
   * and no unsigned consents for users are created.
   * <p>
   * If the consent evaluation is turned off (in CLI without the -c option),
   * the members with UNSIGNED consent creation is skipped.
   *
   * @param sess        perun session
   * @param service     the service
   * @param facility    the facility
   * @param consentEval if the consent evaluation should be performed
   * @param members     the given members
   */
  List<Member> evaluateConsents(PerunSession sess, Service service, Facility facility, List<Member> members,
                                boolean consentEval);


  /**
   * Evaluates consents for given consent hub with enforced consents enabled.
   * <p>
   * For given ConsentHub, collects all attributes from services assigned to it
   * and compares it against attributes from users' Consent objects that are
   * assigned to resources of given ConsentHub object.
   * <p>
   * Service defines whether only active users will be evaluated or expired ones as well.
   *
   * @param sess       session
   * @param consentHub consent hub
   */
  void evaluateConsents(PerunSession sess, ConsentHub consentHub);

  /**
   * Consolidate consents using given service on consent hubs the service is assigned to.
   * <p>
   * Method finds all consent hubs that contain given service in any of its facilities and start
   * consent consolidation for each consent hub (only selected service in consent hubs will be checked).
   * Service defines whether only active users will be evaluated or expired ones as well.
   * If new consent is created, attributes from ALL services under given consent hub are gathered for it.
   *
   * @param sess    session
   * @param service service
   */
  void evaluateConsents(PerunSession sess, Service service);
}
