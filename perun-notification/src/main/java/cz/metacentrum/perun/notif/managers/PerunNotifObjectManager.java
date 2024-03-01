package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.PerunNotifObject;
import java.util.List;

/**
 * Manager to manipulate with PerunNotifObject in db
 * <p>
 * User: tomastunkl Date: 03.11.12 Time: 16:15
 */
public interface PerunNotifObjectManager {

  /**
   * Updates perunNotifObject
   *
   * @param object
   * @return
   */
  public PerunNotifObject updatePerunNotifObject(PerunNotifObject object);

  /**
   * Saves paerunNotifObject
   *
   * @param object
   * @return perunNotifObject with new id set
   */
  public PerunNotifObject createPerunNotifObject(PerunNotifObject object);

  /**
   * Method returns perunNotifObject by id
   *
   * @param id
   * @return
   * @throws InternalErrorException
   */
  public PerunNotifObject getPerunNotifObjectById(int id);

  /**
   * Removes PerunNotifObject based on id, first object is removed from db
   * with relations to regex, than cache in PerunNotifRegexManager is
   * updated.
   *
   * @param id
   * @throws InternalErrorException
   */
  public void removePerunNotifObjectById(int id);

  /**
   * Saves relation between object and regex if not exists
   *
   * @param regexId
   * @param objectId
   */
  public void saveObjectRegexRelation(int regexId, int objectId);

  /**
   * Removes relation between regex and object
   *
   * @param objectId
   * @param regexId
   */
  public void removePerunNotifRegexObjectRelation(int regexId, int objectId);

  /**
   * Returns all PerunNotifObjects.
   *
   * @return list of all objects
   */
  public List<PerunNotifObject> getAllPerunNotifObjects();
}
