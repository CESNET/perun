/**
 *
 */
package cz.metacentrum.perun.core.bl;

import java.util.List;

import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.CandidateGroup;
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
	ExtSource createExtSource(PerunSession perunSession, ExtSource extSource, Map<String, String> attributes) throws InternalErrorException, ExtSourceExistsException;

	/**
	 * Deletes an external source.
	 *
	 * @param perunSession
	 * @param extSource
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceAlreadyRemovedException if there are 0 rows affected by delete in DB
	 */
	void deleteExtSource(PerunSession perunSession, ExtSource extSource) throws InternalErrorException, ExtSourceAlreadyRemovedException;

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
	ExtSource getExtSourceById(PerunSession perunSession, int id) throws InternalErrorException, ExtSourceNotExistsException;

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
	ExtSource getExtSourceByName(PerunSession perunSession, String name) throws InternalErrorException, ExtSourceNotExistsException;

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
	List<ExtSource> getVoExtSources(PerunSession perunSession, Vo vo) throws InternalErrorException;

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
	List<ExtSource> getGroupExtSources(PerunSession perunSession, Group group) throws InternalErrorException;

	/**
	 * Get list of all external sources.
	 *
	 * @param perunSession
	 *
	 * @return list of external source
	 *
	 * @throws InternalErrorException
	 */
	List<ExtSource> getExtSources(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Associate external source definition with the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param source
	 *
	 * @throws InternalErrorException
	 */
	void addExtSource(PerunSession perunSession, Vo vo, ExtSource source) throws InternalErrorException, ExtSourceNotExistsException, ExtSourceAlreadyAssignedException;

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
	void addExtSource(PerunSession perunSession, Group group, ExtSource source) throws InternalErrorException, ExtSourceAlreadyAssignedException;

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
	void removeExtSource(PerunSession perunSession, Vo vo, ExtSource source) throws InternalErrorException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

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
	void removeExtSource(PerunSession perunSession, Group group, ExtSource source) throws InternalErrorException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

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
	 */
	List<User> getInvalidUsers(PerunSession perunSession, ExtSource source) throws InternalErrorException;

	/**
	 * Get the candidate from the ExtSource defined by the extsource login.
	 *
	 * @param perunSession
	 * @param source
	 * @param login
	 * @return a Candidate object
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 * @throws CandidateNotExistsException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	Candidate getCandidate(PerunSession perunSession, ExtSource source, String login) throws InternalErrorException, ExtSourceNotExistsException, CandidateNotExistsException, ExtSourceUnsupportedOperationException;

	/**
	 * Get the candidate from subjectData where at least login must exists.
	 *
	 * IMPORTANT: expected, that these subjectData was get from the ExtSource before using.
	 *
	 * @param perunSession
	 * @param subjectData
	 * @param source
	 * @param login
	 *
	 * @return a Candidate object
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 * @throws CandidateNotExistsException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	Candidate getCandidate(PerunSession perunSession, Map<String,String> subjectData ,ExtSource source, String login) throws InternalErrorException, ExtSourceNotExistsException, CandidateNotExistsException, ExtSourceUnsupportedOperationException;

	void checkExtSourceExists(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceNotExistsException;

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
	void checkExtSourceAssignedToVo(PerunSession sess, ExtSource extSource, int voId) throws InternalErrorException, ExtSourceNotAssignedException, VoNotExistsException;

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
	Map<String, String> getAttributes(ExtSource extSource) throws InternalErrorException;

	/**
	 * Generate a candidate group from a group subject data.
	 *
	 * IMPORTANT: expected, that these subjectData was get from the ExtSource before using.
	 *
	 * @param perunSession
	 * @param groupSubjectData
	 * @param source
	 *
	 * @return Candidate group object
	 * @throws InternalErrorException
	 */
	CandidateGroup generateCandidateGroup(PerunSession perunSession, Map<String,String> groupSubjectData, ExtSource source) throws InternalErrorException;

	/**
	 * Generate candidate groups from a group subject data.
	 *
	 * IMPORTANT: expected, that these subjectData was get from the ExtSource before using.
	 *
	 * @param perunSession
	 * @param groupSubjectsData
	 * @param source
	 *
	 * @return Candidate group objects
	 * @throws InternalErrorException
	 */
	List<CandidateGroup> generateCandidateGroups(PerunSession perunSession, List<Map<String,String>> groupSubjectsData, ExtSource source) throws InternalErrorException;

}
