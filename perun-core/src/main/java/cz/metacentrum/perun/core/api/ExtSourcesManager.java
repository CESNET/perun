package cz.metacentrum.perun.core.api;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.impl.Utils;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 *
 */
public interface ExtSourcesManager {

	public static final String EXTSOURCE_IDP = "cz.metacentrum.perun.core.impl.ExtSourceIdp";
	public static final String EXTSOURCE_SQL = "cz.metacentrum.perun.core.impl.ExtSourceSql";
	public static final String EXTSOURCE_LDAP = "cz.metacentrum.perun.core.impl.ExtSourceLdap";
	public static final String EXTSOURCE_KERBEROS = "cz.metacentrum.perun.core.impl.ExtSourceKerberos";
	public static final String EXTSOURCE_INTERNAL = "cz.metacentrum.perun.core.impl.ExtSourceInternal";
	public static final String EXTSOURCE_ISMU = "cz.metacentrum.perun.core.impl.ExtSourceISMU";
	public static final String EXTSOURCE_X509 = "cz.metacentrum.perun.core.impl.ExtSourceX509";

	/**
	 * Name of the LOCAL extSource, which is used for users without any external authentication.
	 * extLogin is generated on the fly, usually it is time of the first access.
	 */
	public static final String EXTSOURCE_NAME_LOCAL = "LOCAL";

	/**
	 * Name of the INTERNAL extSource, which is used for internal Perun components like Registrar etc.
	 */
	public static final String EXTSOURCE_NAME_INTERNAL = "INTERNAL";

	/**
	 * Name of the default extSource which have every user in Perun.
	 */
	public static final String EXTSOURCE_NAME_PERUN = "PERUN";

	public final static String CONFIGURATIONFILE = Utils.configurationsLocations + "perun-extSources.xml";

	/**
	 * Creates an external source.
	 *
	 * @param perunSession
	 * @param extSource
	 *
	 * @return ExtSource object with newly associated ID.
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceExistsException
	 * @throws PrivilegeException
	 */
	ExtSource createExtSource(PerunSession perunSession, ExtSource extSource) throws InternalErrorException, ExtSourceExistsException, PrivilegeException;

	/**
	 * Deletes an external source.
	 *
	 * @param perunSession
	 * @param extSource
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceExistsException
	 * @throws PrivilegeException
	 * @throws ExtSourceAlreadyRemovedException when 0 rows are affected by deleting from DB
	 */
	void deleteExtSource(PerunSession perunSession, ExtSource extSource) throws InternalErrorException, ExtSourceNotExistsException, PrivilegeException, ExtSourceAlreadyRemovedException;

	/**
	 * Searches for the external source with specified id.
	 *
	 * @param perunSession
	 * @param id
	 *
	 * @return External source with specified id
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 * @throws PrivilegeException
	 */
	ExtSource getExtSourceById(PerunSession perunSession, int id) throws InternalErrorException, ExtSourceNotExistsException, PrivilegeException;

	/**
	 * Searches for the external source using specified name.
	 *
	 * @param perunSession
	 * @param name
	 *
	 * @return External source with specified name
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 * @throws PrivilegeException
	 */
	ExtSource getExtSourceByName(PerunSession perunSession, String name) throws InternalErrorException, ExtSourceNotExistsException, PrivilegeException;

	/**
	 * Get list of external sources associated with the VO.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @return list of external sources associated with the VO
	 *
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<ExtSource> getVoExtSources(PerunSession perunSession, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get list of all external sources.
	 *
	 * @param perunSession
	 *
	 * @return list of external source
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<ExtSource> getExtSources(PerunSession perunSession) throws InternalErrorException, PrivilegeException;

	/**
	 * Associate external source definition with the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param source
	 *
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ExtSourceNotExistsException
	 */
	void addExtSource(PerunSession perunSession, Vo vo, ExtSource source) throws InternalErrorException, PrivilegeException, VoNotExistsException, ExtSourceNotExistsException, ExtSourceAlreadyAssignedException;

	/**
	 * Remove association of the external source from the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param source
	 *
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 * @throws PrivilegeException
	 * @throws ExtSourceAlreadyRemovedException when 0 rows affected by removing from DB
	 */
	void removeExtSource(PerunSession perunSession, Vo vo, ExtSource source) throws InternalErrorException, PrivilegeException, VoNotExistsException, ExtSourceNotExistsException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

	/**
	 * Checks whether the ExtSource exists, if not, then the ExtSource is created.
	 *
	 * @param perunSession
	 * @param extSourceName
	 * @param extSourceType
	 *
	 * @return existing or newly created extSource is returned
	 *
	 * @throws InternalErrorException
	 */
	ExtSource checkOrCreateExtSource(PerunSession perunSession, String extSourceName, String extSourceType) throws InternalErrorException;

	/**
	 * Returns list of users stored by this ExtSource, which are not valid.
	 *
	 * @param perunSession
	 * @param source
	 *
	 * @return list of users, who is not in the extSource anymore
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 * @throws PrivilegeException
	 */
	List<User> getInvalidUsers(PerunSession perunSession, ExtSource source) throws InternalErrorException, PrivilegeException, ExtSourceNotExistsException;

	/**
	 * Get the candidate from the ExtSource defined by the extsource login.
	 *
	 * @param perunSession
	 * @param source
	 * @param login
	 * @return a Candidate object
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws ExtSourceNotExistsException
	 * @throws CandidateNotExistsException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	Candidate getCandidate(PerunSession perunSession, ExtSource source, String login) throws InternalErrorException, PrivilegeException, ExtSourceNotExistsException, CandidateNotExistsException,ExtSourceUnsupportedOperationException;

	/**
	 * Loads ext source definitions from the configuration file and updates entries stored in the DB.
	 *
	 * @param sess
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void loadExtSourcesDefinitions(PerunSession sess) throws InternalErrorException, PrivilegeException;
}
