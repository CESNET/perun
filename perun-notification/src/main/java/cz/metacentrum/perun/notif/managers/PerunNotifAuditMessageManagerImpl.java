package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.notif.dao.PerunNotifAuditMessageDao;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manager for work with message received from auditer
 *
 * @author tomas.tunkl
 */
@Service("perunNotifAuditMessageManager")
public class PerunNotifAuditMessageManagerImpl implements PerunNotifAuditMessageManager {

  @Autowired
  private PerunNotifAuditMessageDao perunNotifAuditMessageDao;

  @Override
  public List<PerunNotifAuditMessage> getAll() {

    return perunNotifAuditMessageDao.getAll();
  }

  public void removePerunAuditerMessageById(long id) {

    perunNotifAuditMessageDao.remove(id);
  }

  public PerunNotifAuditMessage saveMessageToPerunAuditerMessage(String message, PerunSession session) {

    return perunNotifAuditMessageDao.save(message);
  }
}
