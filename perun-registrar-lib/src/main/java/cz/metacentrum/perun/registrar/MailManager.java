package cz.metacentrum.perun.registrar;

import java.util.List;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import cz.metacentrum.perun.registrar.model.ApplicationMail.MailType;
import org.springframework.dao.DuplicateKeyException;

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
	 * @throws DuplicateKeyException When mail definition already exists.
	 */
	public Integer addMail(PerunSession sess, ApplicationForm form, ApplicationMail mail) throws PerunException, DuplicateKeyException;

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
	 * Copy all mail definitions from one VO to Group or reverse.
	 *
	 * @param sess PerunSession for authz
	 * @param fromVo VO to get application mails from (or opposite if reverse=TRUE)
	 * @param toGroup Group to set application mails to (or opposite if reverse=TRUE)
	 * @param reverse FALSE = copy from VO to Group (default) / TRUE = copy from Group to VO
	 * @throws PerunException
	 */
	public void copyMailsFromVoToGroup(PerunSession sess, Vo fromVo, Group toGroup, boolean reverse) throws PerunException;

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
	 * Re-send mail notification for specific application and MailType.
	 * This method throw exceptions, if sending is not possible (template not defined,
	 * sending disabled, notification is not related to current AppState,...).
	 *
	 * PerunAdmin can send all notifications not limited by AppState.
	 *
	 * Contextual data, like exceptions related to processing Application itself can't be sent
	 * this way, since this method doesn't perform any action with Application. It only send emails.
	 *
	 * @param sess PerunSession for authz
	 * @param app application to send notification for
	 * @param mailType MailType action which caused sending
	 * @param reason custom text passed to mail by admin (e.g. reason of application reject)
	 */
	public void sendMessage(PerunSession sess, Application app, MailType mailType, String reason) throws PerunException;

	/**
	 * Sends invitation with link to VO / Group application form.
	 *
	 * If VO or Group have non-empty attribute urn:perun:[vo/group]:attribute-def:def:applicationURL
	 * content is used as link to application form. Otherwise link is automatically generated based on
	 * required AUTHZ in template and registrar url set in /etc/perun/perun-registrar.properties.
	 *
	 * @param sess PerunSession for authz
	 * @param vo VO to link form to
	 * @param group Group to link form to
	 * @param name Name of invited User
	 * @param email Email to send invitation to.
	 * @param language Language used in notification (if not specified, VO settings is used, if not set, "en" is used).
	 *
	 * @throws PerunException
	 */
	public void sendInvitation(PerunSession sess, Vo vo, Group group, String name, String email, String language) throws PerunException;

	/**
	 * Sends invitation with link to VO / Group application form.
	 *
	 * If VO or Group have non-empty attribute urn:perun:[vo/group]:attribute-def:def:applicationURL
	 * content is used as link to application form. Otherwise link is automatically generated based on
	 * required AUTHZ in template and registrar url set in /etc/perun/perun-registrar.properties.
	 *
	 * @param sess PerunSession for authz
	 * @param vo VO to link form to
	 * @param group Group to link form to
	 * @param user User to send invitation to
	 *
	 * @throws PerunException
	 */
	public void sendInvitation(PerunSession sess, Vo vo, Group group, User user) throws PerunException;

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
