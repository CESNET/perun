package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
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

}
