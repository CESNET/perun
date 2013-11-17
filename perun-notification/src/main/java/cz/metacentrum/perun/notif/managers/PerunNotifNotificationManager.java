package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.*;
import cz.metacentrum.perun.notif.exceptions.PerunNotifRegexUsedException;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Manager defines methods for work with objects used in notification system
 * User: tomastunkl
 * Date: 14.10.12
 * Time: 21:20
 */
public interface PerunNotifNotificationManager {

    /*
     * Methods for perunNotifObject
     */

    /**
     * Method returns PerunNotifObject from db with given id.
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifObject getPerunNotifObjectById(int id) throws InternalErrorException;

    /**
     * Saves PerunNotifObject to db and creates id
     * @param object
     * @throws InternalErrorException
     */
    public void savePerunNotifObject(PerunNotifObject object) throws InternalErrorException;

    /**
     * Updates perunNotifObject in db
     * @param object
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifObject updatePerunNotifObject(PerunNotifObject object) throws InternalErrorException;

    /**
     * Removes object and relations to object with regex from db
     * @param id
     * @throws InternalErrorException
     */
    public void removePerunNotifObjectById(int id) throws InternalErrorException;

    /**
     * Removes relation between object and regex
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
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifReceiver getPerunNotifReceiverById(int id) throws InternalErrorException;

    /**
     * Saves PerunNotifReceiver to db and crates id
     *
     * @param receiver
     * @throws InternalErrorException
     */
    public void savePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException;

    /**
     * Updates receiver in db
     * @param receiver
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifReceiver updatePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException;

    /**
     * Removes PerunNotifReceiver from db
     * @param id
     * @throws InternalErrorException
     */
    public void removePerunNotifReceiverById(int id) throws InternalErrorException;

    /*
     * Methods for perunNotifRegex
     */

    /**
     * Returns PerunNotifRegex by id, returns also object related to regex
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifRegex getPerunNotifRegexById(int id) throws InternalErrorException;

    /**
     * Saves perunNotifRegex to db and creates id, also saves relation between regex and object
     * @param regex
     * @throws InternalErrorException
     */
    public void savePerunNotifRegex(PerunNotifRegex regex) throws InternalErrorException;

    /**
     * Updates PerunNotifRegex in db, also updates relation between regex and objects
     * @param regex
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifRegex updatePerunNotifRegex(PerunNotifRegex regex) throws InternalErrorException;

    /**
     * Removes PerunNotifRegex from db, if regex is referenced from template exception is thrown
     * Also removes relation between regex and objects
     * @param id
     * @throws InternalErrorException
     * @throws PerunNotifRegexUsedException
     */
    public void removePerunNotifRegexById(int id) throws InternalErrorException, PerunNotifRegexUsedException;

    /**
     * Removes relation between PerunNotifRegex and PerunNotifTemplate
     * @param templateId
     * @param regexId
     * @throws InternalErrorException
     */
    public void removePerunNotifTemplateRegexRelation(int templateId, int regexId) throws InternalErrorException;

    /*
     * Methods for perunNotifTemplateMessage
     */

    /**
     * Gets PerunNotifTemplateMessage from db
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(int id) throws InternalErrorException;

    /**
     * Saves perunNotifTemplateMessage to db and creates id
     * @param message
     * @throws InternalErrorException
     */
    public void savePerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException;

    /**
     * Update perunNotifTemplateMessage in db.
     * @param message
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException;

    /**
     * Removes PerunNotifTemplateMessage from db.
     * @param id
     * @throws InternalErrorException
     */
    public void removePerunNotifTemplateMessage(int id) throws InternalErrorException;

    /*
     * Methods for perunNotifTemplate
     */

    /**
     * Return perunNotifTemplate from db, return also filled all collections
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifTemplate getPerunNotifTemplateById(int id) throws InternalErrorException;

    /**
     * Saves PerunNotifTemplate to db and saves all relations to db
     * @param template
     * @throws InternalErrorException
     */
    public void savePerunNotifTemplate(PerunNotifTemplate template) throws InternalErrorException;

    /**
     * Method will update perunNotifTemplate, also update relations, but not deletes them
     * @param template
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifTemplate updatePerunNotifTemplate(PerunNotifTemplate template) throws InternalErrorException;

    /**
     * Removes perunNotifTemplate from db
     * @param id
     * @throws InternalErrorException
     */
    public void removePerunNotifTemplateById(int id) throws InternalErrorException;

    /**
     * Method allows to test freeMarkerTemplate, list of beans is provided to method
     * @param template
     * @param regexIdsPerunBeans
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public String testPerunNotifMessageText(String template, Map<Integer, List<PerunBean>> regexIdsPerunBeans) throws IOException, TemplateException;
}
