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
   * Saves perunNotifReceiver
   *
   * @param receiver
   * @return perunNotifReceiver with new id set
   */
  public PerunNotifReceiver createPerunNotifReceiver(PerunNotifReceiver receiver);

  /**
   * Save perunNotifTemplateMessage to db
   *
   * @param message
   * @return perunNotifTemplateMessage with new id set
   */
  public PerunNotifTemplateMessage createPerunNotifTemplateMessage(PerunNotifTemplateMessage message);

  /**
   * Returns all PerunNotifReceivers from db.
   *
   * @return list of all PerunNotifReceivers or null (when no receiver with id was found)
   */
  public List<PerunNotifReceiver> getAllPerunNotifReceivers();

  /**
   * Returns all PerunNotifTemplateMessages.
   *
   * @return list of all PerunNotifTemplateMessages
   */
  public List<PerunNotifTemplateMessage> getAllPerunNotifTemplateMessages();


  /**
   * Gets all templates from db.
   *
   * @return
   */
  public List<PerunNotifTemplate> getAllPerunNotifTemplates();

  /**
   * Gets receiver by id
   *
   * @param id
   * @return
   * @throws InternalErrorException
   */
  public PerunNotifReceiver getPerunNotifReceiverById(int id);

  /**
   * Gets perunNotifTemplate by id from db
   *
   * @param id
   * @return
   * @throws InternalErrorException
   */
  public PerunNotifTemplate getPerunNotifTemplateById(int id);

  /**
   * get perunNotifTemplateMessage from db by id
   *
   * @param id
   * @return template message of null (when message with id was not found)
   */
  public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(int id);

  /**
   * Removes perunNotifReceiver by id
   *
   * @param id
   * @throws InternalErrorException
   */
  public void removePerunNotifReceiverById(int id);

  /**
   * Removes template by id
   *
   * @param id
   */
  public void removePerunNotifTemplateById(int id);

  /**
   * Remove perunNotifTemplateMessage from db
   *
   * @param id
   */
  public void removePerunNotifTemplateMessage(int id);

  /**
   * Saves data about transaction to db, not dependencies
   *
   * @param template
   * @return perunNotifTemplate with new id set
   * @throws InternalErrorException
   */
  public PerunNotifTemplate savePerunNotifTemplateInternals(PerunNotifTemplate template);

  /**
   * Stores relation between template and regex to db
   *
   * @param templateId
   * @param regexId
   */
  public void saveTemplateRegexRelation(int templateId, Integer regexId);

  /**
   * Updates perunNotifReceiver
   *
   * @param receiver
   * @return
   * @throws InternalErrorException
   */
  public PerunNotifReceiver updatePerunNotifReceiver(PerunNotifReceiver receiver);

  /**
   * Updates only data of template
   *
   * @param template
   * @return
   */
  public PerunNotifTemplate updatePerunNotifTemplateData(PerunNotifTemplate template);

  /**
   * Update perunNotifTemplateMessage in db
   *
   * @param message
   * @return
   */
  public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage message);
}
