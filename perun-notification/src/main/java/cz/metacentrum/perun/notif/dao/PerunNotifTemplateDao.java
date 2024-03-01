package cz.metacentrum.perun.notif.dao;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplateMessage;
import java.util.List;

/**
 * Interface for work with perunNotifTemplate in db
 *
 * @author tomas.tunkl
 */
public interface PerunNotifTemplateDao {

  /**
   * Gets all templates from db.
   *
   * @return
   */
  public List<PerunNotifTemplate> getAllPerunNotifTemplates();

  /**
   * Updates only data of template
   *
   * @param template
   * @return
   */
  public PerunNotifTemplate updatePerunNotifTemplateData(PerunNotifTemplate template);

  /**
   * Gets receiver by id
   *
   * @param id
   * @return
   * @throws InternalErrorException
   */
  public PerunNotifReceiver getPerunNotifReceiverById(int id);

  /**
   * Returns all PerunNotifReceivers from db.
   *
   * @return list of all PerunNotifReceivers or null (when no receiver with id was found)
   */
  public List<PerunNotifReceiver> getAllPerunNotifReceivers();

  ;

  /**
   * Saves perunNotifReceiver
   *
   * @param receiver
   * @return perunNotifReceiver with new id set
   */
  public PerunNotifReceiver createPerunNotifReceiver(PerunNotifReceiver receiver);

  /**
   * Updates perunNotifReceiver
   *
   * @param receiver
   * @return
   * @throws InternalErrorException
   */
  public PerunNotifReceiver updatePerunNotifReceiver(PerunNotifReceiver receiver);

  /**
   * Gets perunNotifTemplate by id from db
   *
   * @param id
   * @return
   * @throws InternalErrorException
   */
  public PerunNotifTemplate getPerunNotifTemplateById(int id);

  /**
   * Saves data about transaction to db, not dependencies
   *
   * @param template
   * @return perunNotifTemplate with new id set
   * @throws InternalErrorException
   */
  public PerunNotifTemplate savePerunNotifTemplateInternals(PerunNotifTemplate template);

  /**
   * Removes perunNotifReceiver by id
   *
   * @param id
   * @throws InternalErrorException
   */
  public void removePerunNotifReceiverById(int id);

  /**
   * get perunNotifTemplateMessage from db by id
   *
   * @param id
   * @return template message of null (when message with id was not found)
   */
  public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(int id);

  /**
   * Returns all PerunNotifTemplateMessages.
   *
   * @return list of all PerunNotifTemplateMessages
   */
  public List<PerunNotifTemplateMessage> getAllPerunNotifTemplateMessages();

  /**
   * Save perunNotifTemplateMessage to db
   *
   * @param message
   * @return perunNotifTemplateMessage with new id set
   */
  public PerunNotifTemplateMessage createPerunNotifTemplateMessage(PerunNotifTemplateMessage message);

  /**
   * Update perunNotifTemplateMessage in db
   *
   * @param message
   * @return
   */
  public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage message);

  /**
   * Remove perunNotifTemplateMessage from db
   *
   * @param id
   */
  public void removePerunNotifTemplateMessage(int id);

  /**
   * Removes template by id
   *
   * @param id
   */
  public void removePerunNotifTemplateById(int id);

  /**
   * Stores relation between template and regex to db
   *
   * @param templateId
   * @param regexId
   */
  public void saveTemplateRegexRelation(int templateId, Integer regexId);
}
