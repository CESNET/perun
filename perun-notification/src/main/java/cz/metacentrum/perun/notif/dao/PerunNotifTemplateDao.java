package cz.metacentrum.perun.notif.dao;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplateMessage;

import java.util.List;

/**
 * Interface for work with perunNotifTemplate in db
 * @author tomas.tunkl
 *
 */
public interface PerunNotifTemplateDao {

	/**
	 * Gets all templates from db.
	 * @return
	 */
	public List<PerunNotifTemplate> getAllPerunNotifTemplates() throws InternalErrorException;

	/**
	 * Updates only data of template
	 * @param template
	 * @return 
	 */
	public PerunNotifTemplate updatePerunNotifTemplateData(PerunNotifTemplate template) throws InternalErrorException;

    /**
     * Gets receiver by id
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifReceiver getPerunNotifReceiverById(int id) throws InternalErrorException;

    /**
     * Saves perunNotifReceiver
     * @return perunNotifReceiver with new id set
     * @param receiver
     */
    public PerunNotifReceiver savePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException;

    /**
     * Updates perunNotifReceiver
     * @param receiver
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifReceiver updatePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException;

    /**
     * Gets perunNotifTemplate by id from db
     * @param id
     * @return
     * @throws InternalErrorException
     */
    public PerunNotifTemplate getPerunNotifTemplateById(int id) throws InternalErrorException;

    /**
     * Saves data about transaction to db, not dependencies
     * @param template
     * @return perunNotifTemplate with new id set
     * @throws InternalErrorException
     */
    public PerunNotifTemplate savePerunNotifTemplateInternals(PerunNotifTemplate template) throws InternalErrorException;

    /**
     * Removes perunNotifReceiver by id
     * @param id
     * @throws InternalErrorException
     */
    public void removePerunNotifReceiverById(int id) throws InternalErrorException;

    /**
     * get perunNotifTemplateMessage from db by id
     * @param id
     * @return
     */
    public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(int id) throws InternalErrorException;

    /**
     * Save perunNotifTemplateMessage to db
     * @return perunNotifTemplateMessage with new id set
     * @param message
     */
    public PerunNotifTemplateMessage savePerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException;

    /**
     * Update perunNotifTemplateMessage in db
     * @param message
     * @return
     */
    public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException;

    /**
     * Remove perunNotifTemplateMessage from db
     * @param id
     */
    public void removePerunNotifTemplateMessage(int id) throws InternalErrorException;

    /**
     * Removes template by id
     * @param id
     */
    public void removePerunNotifTemplateById(int id);
}