package cz.metacentrum.perun.notif.dao;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.PerunNotifObject;
import java.util.List;

/**
 * Dao layer for work with PerunNotifObject
 * <p>
 * User: tomastunkl Date: 01.11.12 Time: 22:54
 */
public interface PerunNotifObjectDao {

  /**
   * Saves object to db and creates unique id which sets to object
   *
   * @param object
   * @return perunNotifObject with new id set
   * @throws InternalErrorException
   */
  public PerunNotifObject createPerunNotifObject(PerunNotifObject object);

  /**
   * Update object in db and returns newly loaded Object from db
   *
   * @param object
   * @return Newly loaded Object from db
   * @throws InternalErrorException
   */
  public PerunNotifObject updatePerunNotifObject(PerunNotifObject object);

  /**
   * Removes object from db, also removes relation between object and
   * regex
   *
   * @param id
   * @throws InternalErrorException
   */
  public void removePerunNotifObjectById(int id);

  /**
   * Returns PerunNotifObject with given id from db
   *
   * @param id
   * @return null or PerunNotifObject with given id
   * @throws InternalErrorException
   */
  public PerunNotifObject getPerunNotifObjectById(int id);

  /**
   * Returns whether relation between object and regex exists
   *
   * @param regexId
   * @param objectId
   * @return true if relation exists
   * @throws InternalErrorException
   */
  public boolean isObjectRelation(int regexId, Integer objectId);

  /**
   * Saves relation between regex and object
   *
   * @param regexId
   * @param objectId
   * @throws InternalErrorException
   */
  public void saveObjectRelation(int regexId, Integer objectId);

  /**
   * Removes relation between object and regex
   *
   * @param regexId
   * @param objectId
   * @throws InternalErrorException
   */
  public void removePerunNotifObjectRegexRelation(int regexId, int objectId);

  /**
   * Gets all PerunNotifObjects from db
   *
   * @return
   */
  public List<PerunNotifObject> getAll();
}
