package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.PerunNotifObject;

/**
 * Manager to manipulate with PerunNotifObject in db
 *
 * User: tomastunkl
 * Date: 03.11.12
 * Time: 16:15
 */
public interface PerunNotifObjectManager {

    /**
     * Updates perunNotifObject
     * @param object
     * @return
     */
    public PerunNotifObject updatePerunNotifObject(PerunNotifObject object) throws InternalErrorException;

    /**
     * Saves paerunNotifObject
     * @param object
     * @return perunNotifObject with new id set
     */
    public PerunNotifObject savePerunNotifObject(PerunNotifObject object) throws InternalErrorException;

    /**
     * Method returns perunNotifObject by id
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifObject getPerunNotifObjectById(int id) throws InternalErrorException;

    /**
     * Removes PerunNotifObject based on id, first object is removed from db with relations to regex,
     * than cache in PerunNotifRegexManager is updated.
     *
     * @param id
     * @throws InternalErrorException
     */
    public void removePerunNotifObjectById(int id) throws InternalErrorException;

    /**
     * Saves relation between object and regex if not exists
     * @param regexId
     * @param objectId
     */
    public void saveObjectRegexRelation(int regexId, int objectId) throws InternalErrorException;

    /**
     * Removes relation between regex and object
     * @param objectId
     * @param regexId
     */
    public void removePerunNotifRegexObjectRelation(int regexId, int objectId) throws InternalErrorException;
}
