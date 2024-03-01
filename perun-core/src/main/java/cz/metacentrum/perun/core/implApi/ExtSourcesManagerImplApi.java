package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 */
public interface ExtSourcesManagerImplApi {

  /**
   * Initialize manager
   */
  void initialize(PerunSession sess, PerunBl perunBl);

  /**
   * Clean up allocated resources.
   */
  void destroy();

  /**
   * Creates an external source.
   *
   * @param perunSession
   * @param extSource
   * @param attributes
   * @return ExtSource object with newly associated ID.
   * @throws InternalErrorException
   * @throws ExtSourceExistsException
   */
  ExtSource createExtSource(PerunSession perunSession, ExtSource extSource, Map<String, String> attributes)
      throws ExtSourceExistsException;

  /**
   * Deletes an external source.
   *
   * @param perunSession
   * @param extSource
   * @throws ExtSourceAlreadyRemovedException if there are 0 rows affected by delete in DB
   * @throws InternalErrorException
   */
  void deleteExtSource(PerunSession perunSession, ExtSource extSource) throws ExtSourceAlreadyRemovedException;

  /**
   * Updates extSource definition. It should be called only internally, because extSources are defined in the external XML file.
   * It shouldn't be called from upper layers !!!
   *
   * @param sess
   * @param extSource
   * @throws InternalErrorException
   */
  void updateExtSource(PerunSession sess, ExtSource extSource, Map<String, String> attributes)
      throws ExtSourceNotExistsException;

  /**
   * Searches for the external source with specified id.
   *
   * @param perunSession
   * @param id
   * @return External source with specified id
   * @throws InternalErrorException
   * @throws ExtSourceNotExistsException
   */
  ExtSource getExtSourceById(PerunSession perunSession, int id) throws ExtSourceNotExistsException;

  /**
   * Searches for the external source using specified name.
   *
   * @param perunSession
   * @param name
   * @return External source with specified name
   * @throws InternalErrorException
   * @throws ExtSourceNotExistsException
   */
  ExtSource getExtSourceByName(PerunSession perunSession, String name) throws ExtSourceNotExistsException;

  /**
   * Get list of external sources ids associated to the VO.
   *
   * @param perunSession
   * @param vo
   * @return list of external sources ids associated with the VO
   * @throws InternalErrorException
   */
  List<Integer> getVoExtSourcesIds(PerunSession perunSession, Vo vo) throws InternalErrorException;

  /**
   * Get list of external sources ids associated with the GROUP.
   *
   * @param perunSession
   * @param group
   * @return list of external sources ids associated with the group
   * @throws InternalErrorException
   */
  List<Integer> getGroupExtSourcesIds(PerunSession perunSession, Group group) throws InternalErrorException;

  /**
   * Get list of all external sources.
   *
   * @param perunSession
   * @return list of VO
   * @throws InternalErrorException
   */
  List<ExtSource> getExtSources(PerunSession perunSession);

  /**
   * Associate external source definition with the VO.
   *
   * @param perunSession
   * @param vo
   * @param source
   * @throws InternalErrorException
   * @throws ExtSourceAlreadyAssignedException
   */
  void addExtSource(PerunSession perunSession, Vo vo, ExtSource source) throws ExtSourceAlreadyAssignedException;

  /**
   * Associate external source definition with the GROUP.
   *
   * @param perunSession
   * @param group
   * @param source
   * @throws InternalErrorException
   * @throws ExtSourceAlreadyAssignedException
   */
  void addExtSource(PerunSession perunSession, Group group, ExtSource source) throws ExtSourceAlreadyAssignedException;

  /**
   * Remove association of the external source from the VO.
   *
   * @param perunSession
   * @param vo
   * @param source
   * @throws InternalErrorException
   * @throws ExtSourceNotAssignedException
   * @throws ExtSourceAlreadyRemovedException if there are 0 rows affected by remove in DB
   */
  void removeExtSource(PerunSession perunSession, Vo vo, ExtSource source)
      throws ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

  /**
   * Remove association of the external source from the GROUP.
   *
   * @param perunSession
   * @param group
   * @param source
   * @throws InternalErrorException
   * @throws ExtSourceAlreadyRemovedException when 0 rows affected by removing from DB
   * @throws ExtSourceNotAssignedException
   */
  void removeExtSource(PerunSession perunSession, Group group, ExtSource source)
      throws ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException;

  /**
   * Get all users' id associate with the provided ExtSource
   *
   * @param perunSession
   * @param source
   * @return list of users' id associated with the provided ExtSource
   * @throws InternalErrorException
   */
  List<Integer> getAssociatedUsersIdsWithExtSource(PerunSession perunSession, ExtSource source);

  /**
   * Check if extSource exists in underlaying data source.
   *
   * @param perunSession
   * @param extSource
   * @return true if extSource exists in underlaying data source, false otherwise
   * @throws InternalErrorException
   */
  boolean extSourceExists(PerunSession perunSession, ExtSource extSource);

  /**
   * Check if extSource exists in underlaying data source.
   *
   * @param perunSession
   * @param extSource
   * @throws InternalErrorException
   * @throws ExtSourceNotExistsException
   */
  void checkExtSourceExists(PerunSession perunSession, ExtSource extSource) throws ExtSourceNotExistsException;

  /**
   * Returns a database connection pool.
   *
   * @param poolName named defined in perun-extSources.xml
   * @return database connection pool
   */
  DataSource getDataSource(String poolName);

  /**
   * Loads ext source definitions from the configuration file and updates entries stored in the DB.
   *
   * @param sess
   */
  void loadExtSourcesDefinitions(PerunSession sess);

  /**
   * Gets attributes for external source.
   *
   * @param extSource External Source
   * @throws InternalErrorException
   * @return Map of attributes for external source
   */
  Map<String, String> getAttributes(ExtSource extSource);

  /**
   * Returns all ExtSources with enabled synchronization
   *
   * @param sess PerunSession
   * @throws InternalErrorException
   * @return List of External Sources with enabled synchronization
   */
  List<ExtSource> getExtSourcesToSynchronize(PerunSession sess);
}
