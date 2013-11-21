package cz.metacentrum.perun.notif.dao;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.PerunNotifRegex;

import java.util.List;
import java.util.Set;

/**
 * Dao layer for work with PerunNotifRegexDao in db
 */
public interface PerunNotifRegexDao {

	/**
	 * Gets all perunNotifRegexs from db
	 * @return
	 */
	public List<PerunNotifRegex> getAll();
	
	/**
	 * Saves regex to db and creates new id using sequence, saves only basic attributes, not collections
	 * @param regex
         * @return perunNotifRegex with new id set
	 * @throws InternalErrorException
	 */
	public PerunNotifRegex saveInternals(PerunNotifRegex regex) throws InternalErrorException;

    /**
     * Gets perunNotifRegex by id, entity also contains related PerunNotifObject
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifRegex getPerunNotifRegexById(int id) throws InternalErrorException;

    /**
     * Gets PerunNotifRegexs for templateId, regexes contains also related perunNotifObjects
     *
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public Set<PerunNotifRegex> getPerunNotifRegexForTemplateId(int id) throws InternalErrorException;

    /**
     * Updates perunNotifRegex internals not collections
     * @param regex
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifRegex updatePerunNotifRegexInternals(PerunNotifRegex regex) throws InternalErrorException;

    /**
     * Removes regex by id, but only regexInternals from pn_regex, not collections
     * @param id
     * @throws InternalErrorException
     */
    public void removePerunNotifRegexById(int id) throws InternalErrorException;

    /**
     * Returns all templatesIds which uses this regex id
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public List<Integer> getTemplateIdsForRegexId(int id) throws InternalErrorException;

    /**
     * Return whether relation between regex and template is in db
     * @param templateId
     * @param regexId
     * @return
     */
    public boolean isRegexRelation(int templateId, Integer regexId);

    /**
     * Stores relation between template and regex to db
     * @param templateId
     * @param regexId
     */
    public void saveRegexRelation(int templateId, Integer regexId) throws InternalErrorException;

    /**
     * Removes relation between template and regex
     * @param templateId
     * @param regexId
     * @throws InternalErrorException
     */
    public void removePerunNotifTemplateRegexRelation(int templateId, int regexId) throws InternalErrorException;
}
