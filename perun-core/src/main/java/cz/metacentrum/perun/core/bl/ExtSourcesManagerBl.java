/**
 *
 */
package cz.metacentrum.perun.core.bl;

import java.util.List;

import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
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
   *
   * @return ExtSource object with newly associated ID.
   *
   * @throws InternalErrorException
   * @throws ExtSourceExistsException
   */
  ExtSource createExtSource(PerunSession perunSession, ExtSource extSource) throws InternalErrorException, ExtSourceExistsException;

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
  Candidate getCandidate(PerunSession perunSession, ExtSource source, String login) throws InternalErrorException, ExtSourceNotExistsException, CandidateNotExistsException,ExtSourceUnsupportedOperationException;

  void checkExtSourceExists(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceNotExistsException;

  /**
   * Loads ext source definitions from the configuration file and updates entries stored in the DB.
   *
   * @param sess
   */
  void loadExtSourcesDefinitions(PerunSession sess);
}
