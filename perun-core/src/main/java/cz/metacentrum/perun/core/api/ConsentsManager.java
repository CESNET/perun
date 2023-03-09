package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidConsentStatusException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import java.util.List;

/**
 * Consents entry logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public interface ConsentsManager {

	/**
	 * Gel all consents
	 *
	 * @param sess
	 * @return all existing consents in the database
	 * @throws PrivilegeException
	 */
	List<Consent> getAllConsents(PerunSession sess) throws PrivilegeException;

	/**
	 * Get all consents for chosen ConsentHub with the specified status
	 *
	 * @param sess
	 * @param id     id of the ConsentHub
	 * @param status status of the consent
	 * @return consents for chosen ConsentHub with the specified status
	 * @throws PrivilegeException
	 * @throws ConsentHubNotExistsException
	 */
	List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status) throws PrivilegeException, ConsentHubNotExistsException;

	/**
	 * Get all consents for chosen ConsentHub
	 *
	 * @param sess
	 * @param id   id of the ConsentHub
	 * @return consents for chosen ConsentHub
	 * @throws PrivilegeException
	 * @throws ConsentHubNotExistsException
	 */
	List<Consent> getConsentsForConsentHub(PerunSession sess, int id) throws PrivilegeException, ConsentHubNotExistsException;

	/**
	 * Get consents for members assigned to the chosen resource.
	 *
	 * @param sess session
	 * @param resourceId id of resource
	 * @return consents for corresponding ConsentHub
	 * @throws PrivilegeException
	 * @throws ConsentHubNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws ResourceNotExistsException
	 */
	List<Consent> getConsentsForConsentHubByResource(PerunSession sess, int resourceId) throws PrivilegeException, ConsentHubNotExistsException, FacilityNotExistsException, ResourceNotExistsException;

	/**
	 * Get all consents for chosen User with the specified status
	 *
	 * @param sess
	 * @param id     id of the User
	 * @param status status of the consent
	 * @return consents for chosen User with the specified status
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 */
	List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status) throws PrivilegeException, UserNotExistsException;

	/**
	 * Get all consents for chosen User
	 *
	 * @param sess
	 * @param id   id of the user
	 * @return consents for chosen User
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 */
	List<Consent> getConsentsForUser(PerunSession sess, int id) throws PrivilegeException, UserNotExistsException;

	/**
	 * Get all consents for chosen User in specified consent hub
	 *
	 * @param sess
	 * @param userId       id of the user
	 * @param consentHubId id of the consent hub
	 * @return
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws ConsentHubNotExistsException
	 */
	List<Consent> getConsentsForUserAndConsentHub(PerunSession sess, int userId, int consentHubId) throws PrivilegeException, UserNotExistsException, ConsentHubNotExistsException;

	/**
	 * Get consent for chosen user in specified consent hub with specified status
	 *
	 * @param sess
	 * @param userId       id of the user
	 * @param consentHubId id of the consent hub
	 * @param status       status of the consent
	 * @return
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws ConsentHubNotExistsException
	 */
	Consent getConsentForUserAndConsentHub(PerunSession sess, int userId, int consentHubId, ConsentStatus status) throws PrivilegeException, UserNotExistsException, ConsentHubNotExistsException, ConsentNotExistsException;

	/**
	 * Get consent object with specified id
	 *
	 * @param sess
	 * @param id   id of desired consent object
	 * @return consent object with specified id
	 * @throws ConsentNotExistsException thrown if consent with the id doesn't exist
	 * @throws PrivilegeException
	 */
	Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException, PrivilegeException;

	/**
	 * Get list of all Consent Hubs
	 *
	 * @param sess perun session
	 * @return list of Consent Hubs
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	List<ConsentHub> getAllConsentHubs(PerunSession sess) throws PrivilegeException;

	/**
	 * Finds existing Consent Hub by id.
	 *
	 * @param sess perun session
	 * @param id   id of the Consent Hub you are looking for
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 */
	ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException, PrivilegeException;

	/**
	 * Finds existing Consent Hub by name.
	 *
	 * @param sess perun session
	 * @param name name of the Consent Hub you are looking for
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 */
	ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException, PrivilegeException;

	/**
	 * Finds existing Consent Hub by facility.
	 *
	 * @param sess       perun session
	 * @param facilityId id of facility for which consent hub is searched
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws FacilityNotExistsException
	 */
	ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException, PrivilegeException, FacilityNotExistsException;

	/**
	 * Finds existing Consent Hub for facility to which resource belongs.
	 *
	 * @param sess perun session
	 * @param resourceId id of resource
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws FacilityNotExistsException
	 * @throws ResourceNotExistsException
	 */
	ConsentHub getConsentHubByResource(PerunSession sess, int resourceId) throws ConsentHubNotExistsException, PrivilegeException, FacilityNotExistsException, ResourceNotExistsException;

	/**
	 * Updates the consent hub. Ignores related facilities.
	 *
	 * @param perunSession session
	 * @param consentHub   consent hub
	 * @return updated consent hub
	 * @throws InternalErrorException
	 * @throws ConsentHubNotExistsException if consent hub does not exist
	 * @throws ConsentHubExistsException    if consent hub with the same name already exists
	 * @throws PrivilegeException           insufficient rights
	 */
	ConsentHub updateConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubNotExistsException, PrivilegeException, ConsentHubExistsException;

	/**
	 * Set consent status
	 *
	 * @param sess perun session
	 * @param consent consent
	 * @param status status that should be set
	 * @return consent
	 * @throws ConsentNotExistsException if consent hub does not exist
	 * @throws PrivilegeException if insufficient rights
	 * @throws InvalidConsentStatusException if passed status value can not be set
	 * @throws UserNotExistsException if user does not exist
	 */
	Consent changeConsentStatus(PerunSession sess, Consent consent, ConsentStatus status) throws ConsentNotExistsException, PrivilegeException, InvalidConsentStatusException, UserNotExistsException;

	/**
	 * Evaluates consents for given consent hub with enforced consents enabled.
	 *
	 * Service defines whether only active users will be evaluated or expired ones as well.
	 *  @param sess session
	 * @param consentHub consent hub
	 */
	void evaluateConsents(PerunSession sess, ConsentHub consentHub) throws PrivilegeException;

	/**
	 * Evaluates consents for given list of consent hubs with enforced consents enabled.
	 *
	 * Service defines whether only active users will be evaluated or expired ones as well.
	 *  @param sess session
	 * @param consentHubs consent hubs
	 */
	void evaluateConsents(PerunSession sess, List<ConsentHub> consentHubs) throws PrivilegeException;

	/**
	 * Evaluates consents for given service for all consent hubs with given service with enforced consents enabled.
	 *
	 * Corresponding consent hubs (containing the service) will have consents evaluated ONLY for selected service.
	 * Service defines whether only active users will be evaluated or expired ones as well.
	 * If new consent is created, attributes from ALL services under given consent hub are gathered for it.
	 *
	 * @param sess session
	 * @param service service
	 */
	void evaluateConsents(PerunSession sess, Service service) throws PrivilegeException;
}