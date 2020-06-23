package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.impl.Utils;

import java.util.List;
import java.util.Map;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 */
public interface ExtSourcesManager {

	String EXTSOURCE_IDP = "cz.metacentrum.perun.core.impl.ExtSourceIdp";
	String EXTSOURCE_SQL = "cz.metacentrum.perun.core.impl.ExtSourceSql";
	String EXTSOURCE_LDAP = "cz.metacentrum.perun.core.impl.ExtSourceLdap";
	String EXTSOURCE_KERBEROS = "cz.metacentrum.perun.core.impl.ExtSourceKerberos";
	String EXTSOURCE_INTERNAL = "cz.metacentrum.perun.core.impl.ExtSourceInternal";
	//	String EXTSOURCE_ISMU = "cz.metacentrum.perun.core.impl.ExtSourceISMU";
	String EXTSOURCE_X509 = "cz.metacentrum.perun.core.impl.ExtSourceX509";
	String EXTSOURCE_REMS = "cz.metacentrum.perun.core.impl.ExtSourceREMS";

	/**
	 * Name of the LOCAL extSource, which is used for users without any external authentication.
	 * extLogin is generated on the fly, usually it is time of the first access.
	 */
	String EXTSOURCE_NAME_LOCAL = "LOCAL";

	/**
	 * Name of the INTERNAL extSource, which is used for internal Perun components like Registrar etc.
	 */
	String EXTSOURCE_NAME_INTERNAL = "INTERNAL";

	/**
	 * Name of the default extSource which have every user in Perun.
	 */
	String EXTSOURCE_NAME_PERUN = "PERUN";

	String CONFIGURATIONFILE = Utils.configurationsLocations + "perun-extSources.xml";

	String EXTSOURCE_SYNCHRONIZATION_ENABLED_ATTRNAME = "extSourceSynchronizationEnabled";

	/**
	 * Creates an external source.
	 *
	 * @return ExtSource object with newly associated ID.
	 */
	ExtSource createExtSource(PerunSession perunSession, ExtSource extSource, Map<String, String> attributes) throws ExtSourceExistsException, PrivilegeException;

	/**
	 * Deletes an external source.
	 *
	 * @throws ExtSourceAlreadyRemovedException when 0 rows are affected by deleting from DB
	 */
	void deleteExtSource(PerunSession perunSession, ExtSource extSource) throws ExtSourceNotExistsException, PrivilegeException, ExtSourceAlreadyRemovedException;

	/**
	 * Searches for the external source with specified id.
	 *
	 * @return External source with specified id
	 */
	ExtSource getExtSourceById(PerunSession perunSession, int id) throws ExtSourceNotExistsException, PrivilegeException;

	/**
	 * Searches for the external source using specified name.
	 *
	 * @return External source with specified name
	 */
	ExtSource getExtSourceByName(PerunSession perunSession, String name) throws ExtSourceNotExistsException, PrivilegeException;

	/**
	 * Get list of external sources associated with the VO.
	 *
	 * @return list of external sources associated with the VO
	 */
	List<ExtSource> getVoExtSources(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException;

	/**
	 * Get list of external sources associated with the GROUP.
	 *
	 * @return list of external sources associated with the VO
	 */
	List<ExtSource> getGroupExtSources(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Get list of all external sources.
	 *
	 * @return list of external source
	 */
	List<ExtSource> getExtSources(PerunSession perunSession) throws PrivilegeException;

	/**
	 * Associate external source definition with the VO.
	 */
	void addExtSource(PerunSession perunSession, Vo vo, ExtSource source) throws PrivilegeException, VoNotExistsException, ExtSourceNotExistsException, ExtSourceAlreadyAssignedException;

	/**
	 * Associate external source definition with the GROUP.
	 */
	void addExtSource(PerunSession perunSession, Group group, ExtSource source) throws PrivilegeException, VoNotExistsException, GroupNotExistsException, ExtSourceNotAssignedException, ExtSourceNotExistsException, ExtSourceAlreadyAssignedException;

	/**
	 * Remove association of the external source from the VO.
	 *
	 * @throws ExtSourceAlreadyRemovedException when 0 rows affected by removing from DB
	 */
	void removeExtSource(PerunSession perunSession, Vo vo, ExtSource source) throws PrivilegeException, VoNotExistsException, ExtSourceNotExistsException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

	/**
	 * Remove association of the external source from the GROUP.
	 *
	 * @throws ExtSourceAlreadyRemovedException when 0 rows affected by removing from DB
	 */
	void removeExtSource(PerunSession perunSession, Group group, ExtSource source) throws PrivilegeException, GroupNotExistsException, ExtSourceNotExistsException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

	/**
	 * Checks whether the ExtSource exists, if not, then the ExtSource is created.
	 *
	 * @return existing or newly created extSource is returned
	 */
	ExtSource checkOrCreateExtSource(PerunSession perunSession, String extSourceName, String extSourceType);

	/**
	 * Returns list of users stored by this ExtSource, which are not valid.
	 *
	 * @return list of users, who is not in the extSource anymore
	 */
	@SuppressWarnings("unused")
	List<User> getInvalidUsers(PerunSession perunSession, ExtSource source) throws PrivilegeException, ExtSourceNotExistsException;

	/**
	 * Get the candidate from the ExtSource defined by the extsource login.
	 *
	 * @return a Candidate object
	 */
	@SuppressWarnings("unused")
	Candidate getCandidate(PerunSession perunSession, ExtSource source, String login) throws PrivilegeException, ExtSourceNotExistsException, CandidateNotExistsException, ExtSourceUnsupportedOperationException;

	/**
	 * Get the candidate from subjectData where at least login must exists.
	 * <p>
	 * IMPORTANT: expected, that these subjectData was get from the ExtSource before using.
	 *
	 * @return a Candidate object
	 */
	@SuppressWarnings("unused")
	Candidate getCandidate(PerunSession perunSession, Map<String, String> subjectData, ExtSource source) throws PrivilegeException, ExtSourceNotExistsException;

	/**
	 * Loads ext source definitions from the configuration file and updates entries stored in the DB.
	 */
	void loadExtSourcesDefinitions(PerunSession sess) throws PrivilegeException;

	/**
	 * Gets attributes for external source. Must be Perun Admin.
	 *
	 * @param sess      Current Session
	 * @param extSource External Source
	 * @return Map of attributes for external source
	 */
	Map<String, String> getAttributes(PerunSession sess, ExtSource extSource) throws PrivilegeException, ExtSourceNotExistsException;
}
