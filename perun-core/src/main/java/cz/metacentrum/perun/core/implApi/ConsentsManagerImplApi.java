package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.List;

/**
 * Consents database logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public interface ConsentsManagerImplApi {

	/**
	 * Get list of all Consent Hubs
	 *
	 * @param sess perun session
	 * @return list of Consent Hubs
	 * @throws InternalErrorException
	 */
	List<ConsentHub> getAllConsentHubs(PerunSession sess);

	/**
	 * Finds existing Consent Hub by id.
	 *
	 * @param sess perun session
	 * @param id id of the Consent Hub you are looking for
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 */
	ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException;

	/**
	 * Finds existing Consent Hub by name.
	 *
	 * @param sess perun session
	 * @param name name of the Consent Hub you are looking for
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
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
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 */
	ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException;

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
	void deleteConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubAlreadyRemovedException;

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

}
