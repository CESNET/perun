package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.dto.PoolMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifPoolMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplateMessage;
import cz.metacentrum.perun.notif.exceptions.NotifReceiverAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifTemplateMessageAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.TemplateMessageSyntaxErrorException;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for work with templates. Which represents one email
 *
 * @author tomas.tunkl
 */
public interface PerunNotifTemplateManager {

  /**
   * Creates new perunNotifPoolMessage for regex id and perunMessage
   *
   * @param regexIds
   * @param perunAuditMessage
   * @return
   * @throws InternalErrorException
   */
  public List<PerunNotifPoolMessage> getPerunNotifPoolMessagesForRegexIds(
      Set<Integer> regexIds, PerunNotifAuditMessage perunAuditMessage, PerunSession perunSession);

  /**
   * Processes perunNotifPoolMessagesProcessDto which holds one message
   * for user with more auditerMessages
   *
   * @param templateId
   * @param notifMessages
   * @return returns set of ids which were processed and should be deleted
   */
  public Set<Integer> processPoolMessages(Integer templateId, List<PoolMessage> notifMessages);

  /**
   * Gets perunNotifTemplate by id
   *
   * @param id
   * @return
   */
  public PerunNotifTemplate getPerunNotifTemplateById(int id);

  /**
   * Returns all PerunNotifTemplates.
   *
   * @return list of all PerunNotifTemplates
   * @throws InternalErrorException
   */
  public List<PerunNotifTemplate> getAllPerunNotifTemplates();

  /**
   * Gets perunNotifTemplate from db not from cache
   *
   * @param id
   * @return
   */
  public PerunNotifTemplate getPerunNotifTemplateByIdFromDb(int id);

  /**
   * Updates only data of template no collections.
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
   * @return list of all PerunNotifReceivers
   */
  public List<PerunNotifReceiver> getAllPerunNotifReceivers();

  /**
   * Save perunNotifReceiver
   *
   * @param receiver
   * @return perunNotifReceiver with new id set
   * @throws InternalErrorException
   * @throws NotifReceiverAlreadyExistsException
   */
  public PerunNotifReceiver createPerunNotifReceiver(PerunNotifReceiver receiver)
      throws NotifReceiverAlreadyExistsException;

  /**
   * Updates perunNotifReceiver
   *
   * @param receiver
   * @return
   * @throws InternalErrorException
   * @throws NotifReceiverAlreadyExistsException
   */
  public PerunNotifReceiver updatePerunNotifReceiver(PerunNotifReceiver receiver)
      throws NotifReceiverAlreadyExistsException;

  /**
   * Saves perunNotifTemplate to db. Receivers in template will be saved
   *
   * @param template
   * @return perunNotifTemplate with new id set
   * @throws InternalErrorException
   */
  public PerunNotifTemplate createPerunNotifTemplate(PerunNotifTemplate template);

  /**
   * update template
   *
   * @param template
   * @return
   * @throws InternalErrorException
   */
  public PerunNotifTemplate updatePerunNotifTemplate(PerunNotifTemplate template);

  /**
   * Removes PerunNotifReceiver by id
   *
   * @param id
   * @throws InternalErrorException
   */
  public void removePerunNotifReceiverById(int id);

  /**
   * get perunNotifTemplateMessage from db by id
   *
   * @param id
   * @return
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
   * @throws InternalErrorException
   * @throws NotifTemplateMessageAlreadyExistsException
   * @throws TemplateMessageSyntaxErrorException
   */
  public PerunNotifTemplateMessage createPerunNotifTemplateMessage(PerunNotifTemplateMessage message)
      throws NotifTemplateMessageAlreadyExistsException, TemplateMessageSyntaxErrorException;

  /**
   * Update perunNotifTemplateMessage in db
   *
   * @param message
   * @return
   * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
   * @throws TemplateMessageSyntaxErrorException
   */
  public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage message)
      throws TemplateMessageSyntaxErrorException;

  /**
   * Remove perunNotifTemplateMessage from db
   *
   * @param id
   */
  public void removePerunNotifTemplateMessage(int id);

  /**
   * Removes perun template
   *
   * @param id
   */
  public void removePerunNotifTemplateById(int id);

  /**
   * Assigns template to the regex in the cache - map
   * allTemplatesByRegexIds.
   *
   * @param regexId
   * @param template
   */
  public void assignTemplateToRegex(int regexId, PerunNotifTemplate template);

  /**
   * Removes template-regex relation from the cache - map
   * allTemplatesByRegexIds.
   *
   * @param regexId
   * @param templateId
   * @throws InternalErrorException when regex is not in relation with
   *                                template
   */
  public void removeTemplateFromRegex(int regexId, int templateId);

  /**
   * Test method for creating template message
   *
   * @param template
   * @param regexIdsBeans
   * @return
   */
  public String testPerunNotifMessageText(String template, Map<Integer, List<PerunBean>> regexIdsBeans)
      throws IOException, TemplateException;
}
