/**
 *
 */
package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.CandidateGroup;
import cz.metacentrum.perun.core.api.CandidateSync;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 *
 */
public interface ExtSourcesManagerBl {

	/**
	 * Initialize manager.
	 */
	void initialize(PerunSession sess);

	/**
	 * Destroy manager. Clean resources.
	 */
	void destroy();

	/**
	 * Creates an external source.
	 *
	 * @param perunSession
	 * @param extSource
	 * @param attributes
	 *
	 * @return ExtSource object with newly associated ID.
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceExistsException
	 */
	ExtSource createExtSource(PerunSession perunSession, ExtSource extSource, Map<String, String> attributes) throws ExtSourceExistsException;

	/**
	 * Deletes an external source.
	 *
	 * @param perunSession
	 * @param extSource
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceAlreadyRemovedException if there are 0 rows affected by delete in DB
	 */
	void deleteExtSource(PerunSession perunSession, ExtSource extSource) throws ExtSourceAlreadyRemovedException;

	/**
	 * Searches for the external source with specified id.
	 *
	 * @param perunSession
	 * @param id
	 *
	 * @return External source with specified id
	 *
	 * @throws InternalErrorException
	 */
	ExtSource getExtSourceById(PerunSession perunSession, int id) throws ExtSourceNotExistsException;

	/**
	 * Searches for the external source using specified name.
	 *
	 * @param perunSession
	 * @param name
	 *
	 * @return External source with specified name
	 *
	 * @throws InternalErrorException
	 */
	ExtSource getExtSourceByName(PerunSession perunSession, String name) throws ExtSourceNotExistsException;

	/**
	 * Get list of external sources associated with the VO.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @return list of external sources associated with the VO
	 *
	 * @throws InternalErrorException
	 */
	List<ExtSource> getVoExtSources(PerunSession perunSession, Vo vo);

	/**
	 * Get list of external sources associated with the GROUP.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @return list of external sources associated with the VO
	 *
	 * @throws InternalErrorException
	 */
	List<ExtSource> getGroupExtSources(PerunSession perunSession, Group group);

	/**
	 * Get list of all external sources.
	 *
	 * @param perunSession
	 *
	 * @return list of external source
	 *
	 * @throws InternalErrorException
	 */
	List<ExtSource> getExtSources(PerunSession perunSession);

	/**
	 * Associate external source definition with the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param source
	 *
	 * @throws InternalErrorException
	 */
	void addExtSource(PerunSession perunSession, Vo vo, ExtSource source) throws ExtSourceAlreadyAssignedException;

	/**
	 * Associate external source definitions with the VO.
	 *
	 * @param perunSession sess
	 * @param vo vo
	 * @param sources list of sources to associate
	 *
	 * @throws ExtSourceAlreadyAssignedException
	 */
	void addExtSources(PerunSession perunSession, Vo vo, List<ExtSource> sources) throws ExtSourceAlreadyAssignedException;

	/**
	 * Associate external source definition with the GROUP.
	 *
	 * @param perunSession
	 * @param group
	 * @param source
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceAlreadyAssignedException
	 */
	void addExtSource(PerunSession perunSession, Group group, ExtSource source) throws ExtSourceAlreadyAssignedException;

	/**
	 * Associate external source definitions with the GROUP.
	 *
	 * @param perunSession sess
	 * @param group group
	 * @param sources list of sources to associate
	 *
	 * @throws ExtSourceAlreadyAssignedException
	 */
	void addExtSources(PerunSession perunSession, Group group, List<ExtSource> sources) throws ExtSourceAlreadyAssignedException;

	/**
	 * Remove association of the external source from the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param source
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceNotAssignedException
	 * @throws ExtSourceAlreadyRemovedException if there are 0 rows affected by delete from DB
	 */
	void removeExtSource(PerunSession perunSession, Vo vo, ExtSource source) throws ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

	/**
	 * Remove associations of external sources from the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param sources
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceAlreadyRemovedException when 0 rows affected by removing from DB
	 * @throws ExtSourceNotAssignedException
	 */
	void removeExtSources(PerunSession perunSession, Vo vo, List<ExtSource> sources) throws ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

	/**
	 * Remove association of the external source from the GROUP.
	 *
	 * @param perunSession
	 * @param group
	 * @param source
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceAlreadyRemovedException when 0 rows affected by removing from DB
	 * @throws ExtSourceNotAssignedException
	 */
	void removeExtSource(PerunSession perunSession, Group group, ExtSource source) throws ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

	/**
	 * Remove associations of external sources from the GROUP.
	 *
	 * @param perunSession
	 * @param group
	 * @param sources
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceAlreadyRemovedException when 0 rows affected by removing from DB
	 * @throws ExtSourceNotAssignedException
	 */
	void removeExtSources(PerunSession perunSession, Group group, List<ExtSource> sources) throws ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

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
	ExtSource checkOrCreateExtSource(PerunSession perunSession, String extSourceName, String extSourceType);

	/**
	 * Returns list of users stored by this ExtSource, which are not valid.
	 *
	 * @param perunSession
	 * @param source
	 *
	 * @return list of users, who is not in the extSource anymore
	 *
	 * @throws InternalErrorException
	 */
	List<User> getInvalidUsers(PerunSession perunSession, ExtSource source);

	/**
	 * Get the candidate from the ExtSource.
	 * Login of the candidate will be used to gain data from the ExtSource.
	 *
	 * @param perunSession Perun session
	 * @param source External source which will be used to get data about the candidate
	 * @param login Login of the candidate
	 * @return a Candidate object
	 * @throws InternalErrorException
	 * @throws CandidateNotExistsException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	CandidateSync getCandidate(PerunSession perunSession, ExtSource source, String login) throws CandidateNotExistsException, ExtSourceUnsupportedOperationException;

	/**
	 * Get the candidate from subjectData where at least login must exists.
	 *
	 * IMPORTANT: expected, that these subjectData was get from the ExtSource before using.
	 *
	 * @param perunSession Perun session
	 * @param subjectData
	 * @param source External source which will be used to get data about the candidate
	 * @param login Login of the candidate
	 *
	 * @return a Candidate object
	 * @throws InternalErrorException
	 */
	CandidateSync getCandidate(PerunSession perunSession, Map<String,String> subjectData , ExtSource source, String login);

	void checkExtSourceExists(PerunSession sess, ExtSource extSource) throws ExtSourceNotExistsException;

	/**
	 * Check if extSource is assigned to vo or not. Throw exception if not.
	 *
	 * @param sess
	 * @param extSource
	 * @param voId
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceNotAssignedException
	 * @throws VoNotExistsException
	 */
	void checkExtSourceAssignedToVo(PerunSession sess, ExtSource extSource, int voId) throws ExtSourceNotAssignedException, VoNotExistsException;

	/**
	 * Loads ext source definitions from the configuration file and updates entries stored in the DB.
	 *
	 * @param sess
	 */
	void loadExtSourcesDefinitions(PerunSession sess);

	/**
	 * Gets attributes for external source.
	 *
	 * @param extSource	External Source
	 * @return			Map of attributes for external source
	 * @throws InternalErrorException
	 */
	Map<String, String> getAttributes(ExtSource extSource);

	/**
	 * Generate a candidate group from a group subject data.
	 *
	 * IMPORTANT: expected, that these subjectData was get from the ExtSource before using.
	 *
	 * @param perunSession
	 * @param groupSubjectData
	 * @param source
	 * @param loginPrefix login prefix to change group login and parent group login by it
	 *
	 * @return Candidate group object
	 * @throws InternalErrorException
	 */
	CandidateGroup generateCandidateGroup(PerunSession perunSession, Map<String,String> groupSubjectData, ExtSource source, String loginPrefix);

	/**
	 * Generate candidate groups from a group subject data.
	 *
	 * IMPORTANT: expected, that these subjectData was get from the ExtSource before using.
	 *
	 * @param perunSession
	 * @param groupSubjectsData
	 * @param source
	 * @param loginPrefix login prefix to change group login and parent group login by it
	 *
	 * @return Candidate group objects
	 * @throws InternalErrorException
	 */
	List<CandidateGroup> generateCandidateGroups(PerunSession perunSession, List<Map<String,String>> groupSubjectsData, ExtSource source, String loginPrefix);

	/**
	 * Returns a database connection pool.
	 *
	 * @param poolName named defined in perun-extSources.xml
	 * @return database connection pool
	 */
	DataSource getDataSource(String poolName);
}
