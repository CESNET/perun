package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;

import java.util.List;

/**
 * Consents BL logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public interface ConsentsManagerBl {

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
	 * Finds existing Consent Hub by facility id.
	 *
	 * @param sess perun session
	 * @param facilityId facility for which consent hub is searched
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 */
	ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException;

	/**
	 * Creates new consent hub.
	 * @param perunSession session
	 * @param consentHub consent hub
	 * @return new consent hub
	 * @throws ConsentHubExistsException if consent hub with similar name exists
	 */
	ConsentHub createConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubExistsException;

	/**
	 * Deletes consent hub.
	 * @param perunSession session
	 * @param consentHub consent hub
	 * @throws ConsentHubAlreadyRemovedException if no such consent hub stored in db
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

	/** Updates the ConsentHub. Ignores related facilities.
	 *
	 * @param perunSession session
	 * @param consentHub consentHub
	 * @return updated consent hub
	 * @throws ConsentHubExistsException if consent hub with the same name exists
	 */
	ConsentHub updateConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubExistsException;
}
