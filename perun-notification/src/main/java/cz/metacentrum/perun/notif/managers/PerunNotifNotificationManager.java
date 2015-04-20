package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.notif.entities.*;
import cz.metacentrum.perun.notif.exceptions.NotifReceiverAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifRegexAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifTemplateMessageAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.PerunNotifRegexUsedException;
import cz.metacentrum.perun.notif.exceptions.TemplateMessageSyntaxErrorException;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Manager defines methods for work with objects used in notification system
 * User: tomastunkl Date: 14.10.12 Time: 21:20
 */
public interface PerunNotifNotificationManager {

	/*
	 * Methods for perunNotifObject
	 */
	/**
	 * Method returns PerunNotifObject from db with given id.
	 *
	 * @param id
	 * @return
	 * @throws InternalErrorException
	 */
	public PerunNotifObject getPerunNotifObjectById(int id) throws InternalErrorException;

	/**
	 * Returns all PerunNotifObjects.
	 *
	 * @return list of all objects
	 */
	public List<PerunNotifObject> getAllPerunNotifObjects();

	/**
	 * Saves PerunNotifObject to db and creates id
	 *
	 * @param object
	 * @return perunNotifObject with new id set
	 * @throws InternalErrorException
	 */
	public PerunNotifObject createPerunNotifObject(PerunNotifObject object) throws InternalErrorException;

	/**
	 * Updates perunNotifObject in db
	 *
	 * @param object
	 * @return
	 * @throws InternalErrorException
	 */
	public PerunNotifObject updatePerunNotifObject(PerunNotifObject object) throws InternalErrorException;

	/**
	 * Removes object and relations to object with regex from db
	 *
	 * @param id
	 * @throws InternalErrorException
	 */
	public void removePerunNotifObjectById(int id) throws InternalErrorException;

	/**
	 * Saves relation between object and regex if not exists
	 *
	 * @param regexId
	 * @param objectId
	 * @throws InternalErrorException
	 */
	public void saveObjectRegexRelation(int regexId, int objectId) throws InternalErrorException;

	/**
	 * Removes relation between object and regex
	 *
	 * @param regexId
	 * @param objectId
	 * @throws InternalErrorException
	 */
	public void removePerunNotifRegexObjectRelation(int regexId, int objectId) throws InternalErrorException;

	/*
	 * Methods for PerunNotifReceiver
	 */
	/**
	 * Return PerunNotifReceiver with given id from db
	 *
	 * @param sess perun session
	 * @param id
	 * @return
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifReceiver getPerunNotifReceiverById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns all PerunNotifReceivers from db.
	 *
	 * @param sess perun session
	 * @return list of all PerunNotifReceivers
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public List<PerunNotifReceiver> getAllPerunNotifReceivers(PerunSession sess) throws InternalErrorException, PrivilegeException;

	/**
	 * Saves PerunNotifReceiver to db and crates id
	 *
	 * @param sess perun session
	 * @param receiver
	 * @return perunNotifReceiver with new id set
	 * @throws InternalErrorException
	 * @throws NotifReceiverAlreadyExistsException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifReceiver createPerunNotifReceiver(PerunSession sess, PerunNotifReceiver receiver) throws InternalErrorException, NotifReceiverAlreadyExistsException, PrivilegeException;

	/**
	 * Updates receiver in db
	 *
	 * @param sess perun session
	 * @param receiver
	 * @return
	 * @throws InternalErrorException
	 * @throws NotifReceiverAlreadyExistsException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifReceiver updatePerunNotifReceiver(PerunSession sess, PerunNotifReceiver receiver) throws InternalErrorException, NotifReceiverAlreadyExistsException, PrivilegeException;

	/**
	 * Removes PerunNotifReceiver from db
	 *
	 * @param sess perun session
	 * @param id
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public void removePerunNotifReceiverById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException;

	/*
	 * Methods for perunNotifRegex
	 */
	/**
	 * Returns PerunNotifRegex by id, returns also object related to regex
	 *
	 * @param sess perun session
	 * @param id
	 * @return
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifRegex getPerunNotifRegexById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns all PerunNotifRegexes.
	 *
	 * @param sess perun session
	 * @return list of all PerunNotifRegexes
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	public List<PerunNotifRegex> getAllPerunNotifRegexes(PerunSession sess) throws PrivilegeException, InternalErrorException;

	/**
	 * Saves perunNotifRegex to db and creates id, also saves relation
	 * between regex and object
	 *
	 * @param sess perun session
	 * @param regex
	 * @return perunNotifRegex with new id set
	 * @throws InternalErrorException
	 * @throws NotifRegexAlreadyExistsException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifRegex createPerunNotifRegex(PerunSession sess, PerunNotifRegex regex) throws InternalErrorException, NotifRegexAlreadyExistsException, PrivilegeException;

	/**
	 * Updates PerunNotifRegex in db, also updates relation between regex
	 * and objects
	 *
	 * @param sess perun session
	 * @param regex
	 * @return
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifRegex updatePerunNotifRegex(PerunSession sess, PerunNotifRegex regex) throws InternalErrorException, PrivilegeException;

	/**
	 * Removes PerunNotifRegex from db, if regex is referenced from template
	 * exception is thrown Also removes relation between regex and objects
	 *
	 * @param sess perun session
	 * @param id
	 * @throws InternalErrorException
	 * @throws PerunNotifRegexUsedException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public void removePerunNotifRegexById(PerunSession sess, int id) throws InternalErrorException, PerunNotifRegexUsedException, PrivilegeException;

	/**
	 * Save relation between template and regex if not exists yet.
	 *
	 * @param templateId
	 * @param regexId
	 * @param sess perun session
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public void saveTemplateRegexRelation(PerunSession sess, int templateId, Integer regexId) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns all regexes related to given template.
	 *
	 * @param sess perun session
	 * @param templateId
	 * @return list of regexes
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public List<PerunNotifRegex> getRelatedRegexesForTemplate(PerunSession sess, int templateId) throws InternalErrorException, PrivilegeException;

	/**
	 * Removes relation between PerunNotifRegex and PerunNotifTemplate
	 *
	 * @param templateId
	 * @param regexId
	 * @param sess perun session
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public void removePerunNotifTemplateRegexRelation(PerunSession sess, int templateId, int regexId) throws InternalErrorException, PrivilegeException;

	/*
	 * Methods for perunNotifTemplateMessage
	 */
	/**
	 * Gets PerunNotifTemplateMessage from db
	 *
	 * @param id
	 * @param sess perun session
	 * @return
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns all PerunNotifTemplateMessages.
	 *
	 * @param sess perun session
	 * @return list of all PerunNotifTemplateMessages
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	public List<PerunNotifTemplateMessage> getAllPerunNotifTemplateMessages(PerunSession sess) throws PrivilegeException, InternalErrorException;

	/**
	 * Saves perunNotifTemplateMessage to db and creates id
	 *
	 * @param message
	 * @param sess perun session
	 * @return perunNotifTemplateMessage with new id set
	 * @throws InternalErrorException
	 * @throws NotifTemplateMessageAlreadyExistsException
	 * @throws TemplateMessageSyntaxErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifTemplateMessage createPerunNotifTemplateMessage(PerunSession sess, PerunNotifTemplateMessage message) throws InternalErrorException, NotifTemplateMessageAlreadyExistsException, TemplateMessageSyntaxErrorException, PrivilegeException;

	/**
	 * Update perunNotifTemplateMessage in db.
	 *
	 * @param message
	 * @param sess perun session
	 * @return
	 * @throws InternalErrorException
	 * @throws TemplateMessageSyntaxErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunSession sess, PerunNotifTemplateMessage message) throws InternalErrorException, TemplateMessageSyntaxErrorException, PrivilegeException;

	/**
	 * Removes PerunNotifTemplateMessage from db.
	 *
	 * @param id
	 * @param sess perun session
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public void removePerunNotifTemplateMessage(PerunSession sess, int id) throws InternalErrorException, PrivilegeException;

	/*
	 * Methods for perunNotifTemplate
	 */
	/**
	 * Return perunNotifTemplate from db, return also filled all collections
	 *
	 * @param id
	 * @param sess perun session
	 * @return
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifTemplate getPerunNotifTemplateById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns all PerunNotifTemplates.
	 *
	 * @param sess perun session
	 * @throws InternalErrorException
	 * @return list of all PerunNotifTemplates
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public List<PerunNotifTemplate> getAllPerunNotifTemplates(PerunSession sess) throws InternalErrorException, PrivilegeException;

	/**
	 * Saves PerunNotifTemplate to db and saves all relations to db
	 *
	 * @param template
	 * @param sess perun session
	 * @return perunNotifTemplate with new id set
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifTemplate createPerunNotifTemplate(PerunSession sess, PerunNotifTemplate template) throws InternalErrorException, PrivilegeException;

	/**
	 * Method will update perunNotifTemplate, also update relations, but not
	 * deletes them
	 *
	 * @param template
	 * @param sess perun session
	 * @return
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public PerunNotifTemplate updatePerunNotifTemplate(PerunSession sess, PerunNotifTemplate template) throws InternalErrorException, PrivilegeException;

	/**
	 * Removes perunNotifTemplate from db
	 *
	 * @param id
	 * @param sess perun session
	 * @throws InternalErrorException
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 */
	public void removePerunNotifTemplateById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException;

	/**
	 * Method allows to test freeMarkerTemplate, list of beans is provided
	 * to method
	 *
	 * @param template
	 * @param regexIdsPerunBeans
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public String testPerunNotifMessageText(String template, Map<Integer, List<PerunBean>> regexIdsPerunBeans) throws IOException, TemplateException;

	/**
	 * Stops notifications processing.
	 *
	 * @param sess perun session
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	public void stopNotifications(PerunSession sess) throws PrivilegeException, InternalErrorException;

	/**
	 * Starts notifications processing, if it was stopped before.
	 *
	 * @param sess perun session
	 * @throws cz.metacentrum.perun.core.api.exceptions.PrivilegeException
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	public void startNotifications(PerunSession sess) throws PrivilegeException, InternalErrorException;

	/**
	 * Method checks if the notifications module is running at the time.
	 *
	 * @return true if running
	 */
	public boolean isNotificationsRunning();
}
