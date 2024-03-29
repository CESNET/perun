package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.notif.dao.PerunNotifObjectDao;
import cz.metacentrum.perun.notif.entities.PerunNotifObject;
import cz.metacentrum.perun.notif.exceptions.NotExistsException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: tomastunkl Date: 03.11.12 Time: 16:18
 */
@Service("perunNotifObjectManager")
public class PerunNotifObjectManagerImpl implements PerunNotifObjectManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(PerunNotifObjectManager.class);

  @Autowired
  private PerunNotifObjectDao perunNotifObjectDao;

  @Autowired
  private PerunNotifRegexManager perunNotifRegexManager;

  @Override
  public PerunNotifObject createPerunNotifObject(PerunNotifObject object) {

    PerunNotifObject perunNotifObject = perunNotifObjectDao.createPerunNotifObject(object);

    perunNotifRegexManager.addObjectToCache(object);

    return perunNotifObject;
  }

  @Override
  public List<PerunNotifObject> getAllPerunNotifObjects() {
    return perunNotifObjectDao.getAll();
  }

  @Override
  public PerunNotifObject getPerunNotifObjectById(int id) {

    return perunNotifObjectDao.getPerunNotifObjectById(id);
  }

  @Override
  public void removePerunNotifObjectById(int id) {

    PerunNotifObject objectToRemove = getPerunNotifObjectById(id);
    if (objectToRemove == null) {
      throw new NotExistsException("Object does not exists in db.");
    }

    perunNotifObjectDao.removePerunNotifObjectById(id);
    perunNotifRegexManager.removePerunNotifObjectFromCache(objectToRemove);
  }

  @Override
  public void removePerunNotifRegexObjectRelation(int regexId, int objectId) {

    perunNotifObjectDao.removePerunNotifObjectRegexRelation(regexId, objectId);
  }

  @Override
  public void saveObjectRegexRelation(int regexId, int objectId) {

    if (perunNotifObjectDao.isObjectRelation(regexId, objectId)) {
      LOGGER.debug("Relation between object: {}, template: {} exists", objectId, regexId);
      //Relation exists
      return;
    } else {
      LOGGER.debug("Saving relation between object: {}, template: {}", objectId, regexId);
      perunNotifObjectDao.saveObjectRelation(regexId, objectId);
    }
  }

  @Override
  public PerunNotifObject updatePerunNotifObject(PerunNotifObject object) {

    PerunNotifObject newObject = perunNotifObjectDao.updatePerunNotifObject(object);

    perunNotifRegexManager.addObjectToCache(newObject);

    return newObject;
  }

}
