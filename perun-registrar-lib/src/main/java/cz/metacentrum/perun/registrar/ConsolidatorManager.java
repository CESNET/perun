package cz.metacentrum.perun.registrar;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.Identity;

import java.util.List;

/**
 * Utility manager with methods used for identity consolidation.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface ConsolidatorManager {

	/**
	 * Check for similar users by name and email.
	 *
	 * @param sess PerunSession for authz with data to search by.
	 * @return List of found similar Identities
	 *
	 * @throws PerunException
	 */
	List<Identity> checkForSimilarUsers(PerunSession sess) throws PerunException;

	/**
	 * Check if new application may belong to another user in Perun
	 * (but same person in real life).
	 *
	 * Return list of similar users (by identity, name or email).
	 *
	 * Returned users contain also organization and preferredMail attribute.
	 *
	 * @param sess PerunSession for authz
	 * @param appId ID of application to check for
	 * @return List of found similar Identities
	 * @throws PerunException
	 */
	List<Identity> checkForSimilarUsers(PerunSession sess, int appId) throws PerunException;

	/**
	 * Check if new application may belong to another user in Perun
	 * (but same person in real life).
	 *
	 * IMPORTANT: This check is performed only on latest application of specified vo/group and type which belongs
	 * to logged in user/identity.
	 *
	 * Return list of similar users (by identity, name or email).
	 *
	 * Returned users contain also organization and preferredMail attribute.
	 *
	 * @param sess PerunSession for authz
	 * @param vo to get application for
	 * @param group group to get application for
	 * @param type application type
	 *
	 * @return List of found similar Identities
	 * @throws PerunException
	 */
	public List<Identity> checkForSimilarUsers(PerunSession sess, Vo vo, Group group, Application.AppType type) throws PerunException;

}
