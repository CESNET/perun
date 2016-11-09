package cz.metacentrum.perun.registrar;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
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
	List<Identity> checkForSimilarUsers(PerunSession sess, Vo vo, Group group, Application.AppType type) throws PerunException;

	/**
	 * Check if new application may belong to another user in Perun
	 * (but same person in real life).
	 *
	 * Return list of similar users (by identity, name or email).
	 *
	 * Returned users contain also organization and preferredMail attribute.
	 *
	 * @param sess PerunSession for authz
	 * @param formItems List of application form items with data
	 *
	 * @return List of found similar Identities
	 * @throws PerunException
	 */
	List<Identity> checkForSimilarUsers(PerunSession sess, List<ApplicationFormItemData> formItems) throws PerunException;

	/**
	 * Return unique token with information about current authz. It can be used to join this identity
	 * with another, when user calls opposite method with different credentials.
	 *
	 * @param sess PerunSession for authz (identity)
	 * @return Unique token for identity consolidation
	 * @throws PerunException When error occurs.
	 * @see #consolidateIdentityUsingToken(PerunSession, String)
	 */
	String getConsolidatorToken(PerunSession sess) throws PerunException;

	/**
	 * Join current user identity with another referenced by the passed token.
	 * To get token for your authz see opposite method.
	 *
	 * @param sess PerunSession for authz (identity)
	 * @param token Reference to identity to join with.
	 * @return List of User identities (UserExtSources) known to Perun.
	 * @throws PerunException When error occurs.
	 * @see #getConsolidatorToken(PerunSession)
	 */
	List<UserExtSource> consolidateIdentityUsingToken(PerunSession sess, String token) throws PerunException;

}
