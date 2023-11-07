package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.exceptions.ConsentExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyAssigned;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.List;

/**
 * Consents database logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public interface ConsentsManagerImplApi {

	/**
	 * Save consent to database.
	 *
	 * @param perunSession PerunSession
	 * @param consent Consent
	 * @return created consent
	 * @throws ConsentExistsException if consent already exists
	 */
	Consent createConsent(PerunSession perunSession, Consent consent) throws ConsentExistsException;

	/**
	 * Delete consent from the database.
	 *
	 * @param perunSession PerunSession
	 * @param consent Consent
	 * @throws ConsentNotExistsException if consent doesn't exist
	 */
	void deleteConsent(PerunSession perunSession, Consent consent) throws ConsentNotExistsException;
	/**
	 * Gel all consents
	 *
	 * @param sess PerunSession
	 * @return all existing consents in the database
	 */
	List<Consent> getAllConsents(PerunSession sess);

	/**
	 * Get all consents for chosen ConsentHub with the specified status
	 *
	 * @param sess PerunSession
	 * @param id id of the ConsentHub
	 * @param status status of the consent
	 * @return consents for chosen ConsentHub with the specified status
	 */
	List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status);

	/**
	 * Get all consents for chosen ConsentHub
	 *
	 * @param sess PerunSession
	 * @param id id of the ConsentHub
	 * @return consents for chosen ConsentHub
	 */
	List<Consent> getConsentsForConsentHub(PerunSession sess, int id);

	/**
	 * Get all consents for chosen User with the specified status
	 *
	 * @param sess PerunSession
	 * @param id id of the User
	 * @param status status of the consent
	 * @return consents for chosen User with the specified status
	 */
	List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status);

	/**
	 * Get all consents for chosen User
	 *
	 * @param sess PerunSession
	 * @param id id of the user
	 * @return consents for chosen User
	 */
	List<Consent> getConsentsForUser(PerunSession sess, int id);

	/**
	 * Get consent object with specified id
	 *
	 * @param sess 	perun session
	 * @param id id of desired consent object
	 * @return consent object with specified id
	 * @throws ConsentNotExistsException thrown if consent with the id doesn't exist
	 */
	Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException;

	/**
	 * Check if consent exists in underlying data source.
	 *
	 * @param sess PerunSession
	 * @param consent Consent to check
	 * @throws ConsentNotExistsException if consent doesn't exist
	 */
	void checkConsentExists(PerunSession sess, Consent consent) throws ConsentNotExistsException;

	/**
	 * Check if consent exists in underlying data source.
	 *
	 * @param sess 	perun session
	 * @param consent Consent to check
	 * @return true if consent exists in data source, false otherwise
	 */
	boolean consentExists(PerunSession sess, Consent consent);

	/**
	 * Get list of consents for user and consent hub
	 *
	 * @param sess PerunSession
	 * @param userId id of the user
	 * @param consentHubId id of the consent hub
	 *
	 * @return list of consents for the user and consent hub
	 */
	List<Consent> getConsentsForUserAndConsentHub(PerunSession sess, int userId, int consentHubId);

	/**
	 * Get consent for chosen user in specified consent hub with specified status
	 *
	 * @param sess PerunSession
	 * @param userId id of the user
	 * @param consentHubId id of the consent hub
	 * @param status chosen status
	 * @return consent for chosen user in specified consent hub with specified status
	 * @throws ConsentNotExistsException if consent doesn't exist
	 */
	Consent getConsentForUserAndConsentHub(PerunSession sess, int userId, int consentHubId, ConsentStatus status) throws ConsentNotExistsException;

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
	 * @param id id of the Consent Hub you are looking for
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException if Consent Hub with the specified id doesn't exist
	 * @throws InternalErrorException if an exception occurred
	 */
	ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException;

	/**
	 * Finds existing Consent Hub by name.
	 *
	 * @param sess perun session
	 * @param name name of the Consent Hub you are looking for
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException if Consent Hub with the specified name doesn't exist
	 * @throws InternalErrorException if an exception occurred
	 */
	ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException;

	/**
	 * Get list of all facilities associated to the given Consent Hub
	 *
	 * @param consentHub Consent Hub
	 * @return list of facilities
	 */
	List<Facility> getFacilitiesForConsentHub(ConsentHub consentHub);

	/**
	 * Finds existing Consent Hub by facility.
	 *
	 * @param sess perun session
	 * @param facilityId facility for which consent hub is searched
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException if Consent Hub with the specified name doesn't exist
	 * @throws InternalErrorException if an exception occurred
	 */
	ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException;

	/**
	 * Finds all existing Consent Hubs by service (service is assigned to them)
	 *
	 * @param session perun session
	 * @param serviceId service for which consent hubs are searched
	 * @return list of consent hubs that have given service assigned to them
	 */
	List<ConsentHub> getConsentHubsByService(PerunSession session, int serviceId);

	/**
	 * Creates new consent hub.
	 * @param perunSession session
	 * @param consentHub consent hub
	 * @return new consent hub
	 */
	ConsentHub createConsentHub(PerunSession perunSession, ConsentHub consentHub);

	/**
	 * Deletes the consent hub.
	 * @param perunSession session
	 * @param consentHub consent hub
	 */
	void deleteConsentHub(PerunSession perunSession, ConsentHub consentHub);

	/**
	 * Returns true, if consent hub exists, false otherwise.
	 * @param sess session
	 * @param consentHub consent hub
	 * @return whether consent hub exists
	 */
	boolean consentHubExists(PerunSession sess, ConsentHub consentHub);

	/**
	 * Throws exception if consent hub does not exist.
	 * @param sess session
	 * @param consentHub consent hub
	 * @throws ConsentHubNotExistsException if consent hub does not exist
	 */
	void checkConsentHubExists(PerunSession sess, ConsentHub consentHub) throws ConsentHubNotExistsException;

	/** Updates the consent hub. Ignores related facilities.
	 *
	 * @param perunSession session
	 * @param consentHub consent hub
	 * @throws ConsentHubExistsException if consent hub with the same name exists
	 */
	void updateConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubExistsException;

	/**
	 * Adds facility to consent hub.
	 *
	 * @param sess session
	 * @param consentHub consent hub
	 * @param facility facility to be added
	 * @throws FacilityAlreadyAssigned if facility is already assigned to consent hub
	 */
	void addFacility(PerunSession sess, ConsentHub consentHub, Facility facility) throws FacilityAlreadyAssigned;

	/**
	 * Removes facility from consent hub.
	 *
	 * @param sess session
	 * @param consentHub consent hub
	 * @param facility facility to be removed
	 * @throws RelationNotExistsException if facility is not assigned to consent hub
	 */
	void removeFacility(PerunSession sess, ConsentHub consentHub, Facility facility) throws RelationNotExistsException;

	/**
	 * Set consent status
	 *
	 * @param sess perun session
	 * @param consent consent
	 */
	void changeConsentStatus(PerunSession sess, Consent consent);

}
