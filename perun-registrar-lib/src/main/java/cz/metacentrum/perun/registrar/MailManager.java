package cz.metacentrum.perun.registrar;

import java.util.List;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import cz.metacentrum.perun.registrar.model.ApplicationMail.MailType;

public interface MailManager {

	/**
	 * Add new mail notification.
	 * Relation to application form (vo) is inside ApplicationMail object
	 *
	 * @param sess PerunSession for authz
	 * @param form for authz
	 * @param mail ApplicationMail to be stored
	 * @return ApplicationMail with ID property set returned from DB (null on error)
	 * @throws PerunException
	 */
	public Integer addMail(PerunSession sess, ApplicationForm form, ApplicationMail mail) throws PerunException;

	/**
	 * Delete mail notification from DB based on ID property.
	 *
	 * @param sess PerunSession for authz
	 * @param form for authz
	 * @param id ID of ApplicationMail to delete from DB
	 * @throws PerunException
	 */
	public void deleteMailById(PerunSession sess, ApplicationForm form, Integer id) throws PerunException;

	/**
	 * Update notification parameters (including message texts)
	 * based on provided ID param in ApplicationMail object.
	 *
	 * @param sess PerunSession for authz
	 * @param mail ApplicationMail to update to
	 * @throws PerunException
	 */
	public void updateMailById(PerunSession sess, ApplicationMail mail) throws PerunException;

	/**
	 * Enable or disable sending for list of mail definitions
	 *
	 * @param sess for Authz
	 * @param mails mail definitions to update
	 * @param enabled true = enable sending / false = disable sending
	 * @throws PerunException
	 */
	public void setSendingEnabled(PerunSession sess, List<ApplicationMail> mails, boolean enabled) throws PerunException;

	/**
	 * Return mail definition including texts by ID.
	 *
	 * @param id of mail definition to get
	 * @param sess for authz
	 * @return mail
	 * @throws InternalErrorException when mail definition doesn't exists
	 * @throws PrivilegeException if not VO admin
	 */
	public ApplicationMail getMailById(PerunSession sess, Integer id) throws InternalErrorException, PrivilegeException;

	/**
	 * Return all mail notifications related to specific app form (vo/group)
	 *
	 * @param sess PerunSession for authz
	 * @param form for which VO / group we want mails for
	 * @return list of mail notifications related to app form (vo/group)
	 * @throws PerunException
	 */
	public List<ApplicationMail> getApplicationMails(PerunSession sess, ApplicationForm form) throws PerunException;

    /**
     * Copy all mail definitions from one VO's form into another VO's form.
     *
     * @param sess PerunSession
     * @param fromVo VO to get application mails from
     * @param toVo VO to add application mails to
     * @throws PerunException
     */
    public void copyMailsFromVoToVo(PerunSession sess, Vo fromVo, Vo toVo) throws PerunException;

    /**
     * Copy all mail definitions from one group into another group.
     *
     * @param sess PerunSession
     * @param fromGroup Group to get application mails from
     * @param toGroup Group to add application mails to
     * @throws PerunException
     */
    public void copyMailsFromGroupToGroup(PerunSession sess, Group fromGroup, Group toGroup) throws PerunException;

	/**
	 * Send mail notification for specific application and mail type.
	 * VO (form) and AppType is taken form Application object.
	 *
	 * Consumes all exceptions since sending mail is not mandatory,
	 * exceptions are loged into perun-registrar.log
	 *
	 * @param app application to send notification for
	 * @param mailType MailType action which caused sending
	 * @param reason custom text passed to mail by admin (e.g. reason of application reject)
	 * @param exceptions list of exceptions which occured when processing parent request
	 */
	public void sendMessage(Application app, MailType mailType, String reason, List<Exception> exceptions);


	/**
	 * Creates a MAC with a hard-compiled secret key encoded to printable characters.
	 *
	 * @param input any string
	 * @return message authentication code suitable to be passed in URLs
	 */
	public String getMessageAuthenticationCode(String input);

    /**
     * Get property from configuration
     *
     * @param input property to get
     * @return property value or empty string on any error or when not found
     */
    public String getPropertyFromConfiguration(String input);

}