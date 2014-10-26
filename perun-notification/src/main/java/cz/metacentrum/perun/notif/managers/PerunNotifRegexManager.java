package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifObject;
import cz.metacentrum.perun.notif.entities.PerunNotifRegex;
import cz.metacentrum.perun.notif.exceptions.PerunNotifRegexUsedException;
import cz.metacentrum.perun.notif.exceptions.NotifRegexAlreadyExistsException;
import java.util.List;

import java.util.Set;

/**
 * Manager for work with perun regexes used for recognizing type of message
 *
 * @author tomas.tunkl
 *
 */
public interface PerunNotifRegexManager {

	public Set<Integer> getIdsOfRegexesMatchingMessage(PerunNotifAuditMessage auditMessage) throws InternalErrorException;

	/**
	 * Get perunNotifRegex by id
	 *
	 * @param id
	 * @return
	 */
	public PerunNotifRegex getPerunNotifRegexById(int id) throws InternalErrorException;

	/**
	 * Returns all PerunNotifRegexes.
	 *
	 * @return list of all PerunNotifRegexes
	 */
	public List<PerunNotifRegex> getAllPerunNotifRegexes();

	/**
	 * Save perunNotifRegex to db, saves only regex and relations to object,
	 * not objects if they dont exists
	 *
	 * @param regex
	 * @return perunNotifRegex with new id set
	 * @throws InternalErrorException
	 * @throws NotifRegexAlreadyExistsException
	 */
	public PerunNotifRegex createPerunNotifRegex(PerunNotifRegex regex) throws InternalErrorException, NotifRegexAlreadyExistsException;

	/**
	 * Updates perunNotifRegex and relations between objects and regex, not
	 * objects themselves
	 *
	 * @param reqex
	 * @return
	 * @throws InternalErrorException
	 */
	public PerunNotifRegex updatePerunNotifRegex(PerunNotifRegex reqex) throws InternalErrorException;

	/**
	 * Remove regex based on id, removes also relation with object
	 *
	 * @param id
	 * @throws InternalErrorException
	 */
	public void removePerunNotifRegexById(int id) throws InternalErrorException, PerunNotifRegexUsedException;

	/**
	 * Save relation between template and regex if not exists yet.
	 *
	 * @param templateId
	 * @param regexId
	 */
	public void saveTemplateRegexRelation(int templateId, Integer regexId) throws InternalErrorException;

	/**
	 * Returns all regexes related to given template.
	 *
	 * @param templateId
	 * @return list of regexes
	 * @throws InternalErrorException
	 */
	public List<PerunNotifRegex> getRelatedRegexesForTemplate(int templateId) throws InternalErrorException;

	/**
	 * Removes relation between regex and template
	 *
	 * @param templateId
	 * @param regexId
	 * @throws InternalErrorException
	 */
	public void removePerunNotifTemplateRegexRelation(int templateId, int regexId) throws InternalErrorException;

	/**
	 * Method adds object to cache
	 *
	 * @param object
	 */
	public void addObjectToCache(PerunNotifObject object);

	/**
	 * Method updates object in cache
	 *
	 * @param object
	 */
	public void updateObjectInCache(PerunNotifObject object);

	/**
	 * Remove object from objects cache
	 *
	 * @param objectToRemove
	 */
	public void removePerunNotifObjectFromCache(PerunNotifObject objectToRemove);
}
