package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;

import java.util.List;

/**
 * Consents entry logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public interface ConsentsManager {

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
	 * @param id id of the Consent Hub you are looking for
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
	 * @param sess perun session
	 * @param facilityId id of facility for which consent hub is searched
	 * @return found Consent Hub
	 * @throws ConsentHubNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws FacilityNotExistsException
	 */
	ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException, PrivilegeException, FacilityNotExistsException;

	/**
	 * Updates the consent hub. Ignores related facilities.
	 *
	 * @param perunSession session
	 * @param consentHub consent hub
	 * @return updated consent hub
	 * @throws InternalErrorException
	 * @throws ConsentHubNotExistsException if consent hub does not exist
	 * @throws ConsentHubExistsException if consent hub with the same name already exists
	 * @throws PrivilegeException insufficient rights
	 */
	ConsentHub updateConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubNotExistsException, PrivilegeException, ConsentHubExistsException;
}
