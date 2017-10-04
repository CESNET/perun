package cz.metacentrum.perun.registrar;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.registrar.exceptions.CantBeApprovedException;
import cz.metacentrum.perun.registrar.exceptions.DuplicateRegistrationAttemptException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static cz.metacentrum.perun.registrar.model.Application.AppType;

/**
 * Perun Registrar API.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@SuppressWarnings("UnusedDeclaration")
public interface RegistrarManager {

	/**
	 * Retrieves all necessary data about VO/group under registrar session
	 *
	 * @param voShortName VOs shortname to get info about
	 * @param groupName Groups name to get info about
	 * @return List of VO attributes
	 * @throws PerunException
	 */
	public List<Attribute> initialize(String voShortName, String groupName) throws PerunException;

	/**
	 * Retrieves all necessary data for new registrar webapp in one big call.
	 * Everything is resolved internally instead of making multiple calls from GUI.
	 *
	 * @param sess PerunSession of user to resolve app form for.
	 * @param voShortName VOs shortname to get info about
	 * @param groupName Groups name to get info about
	 * @return Map of expected data
	 * @throws PerunException
	 */
	public Map<String, Object> initRegistrar(PerunSession sess, String voShortName, String groupName) throws PerunException;

	/**
	 * Create application form for vo
	 *
	 * @param sess for authz
	 * @param vo VO to create application form for
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void createApplicationFormInVo(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException;

	/**
	 * Create application form for Group
	 *
	 * @param sess for authz
	 * @param group Group to create application form for
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void createApplicationFormInGroup(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException;

	/**
	 * Gets an application form for a given VO. There is exactly one form for membership per VO, one form is used for both initial registration and annual account expansion,
	 * just the form items are marked whether the should be present in one, the other, or both types of application.
	 *
	 * @param vo VO
	 * @return registration form description
	 * @throws PerunException
	 */
	ApplicationForm getFormForVo(Vo vo) throws PerunException;

	/**
	 * Gets an application form for a given Group. There is exactly one form for membership per Group, one form is used for both initial registration and annual account expansion,
	 * just the form items are marked whether the should be present in one, the other, or both types of application.
	 *
	 * @param group GROUP
	 * @return registration form description
	 * @throws PerunException
	 */
	ApplicationForm getFormForGroup(Group group) throws PerunException;

	/**
	 * Gets an application form for a given Id.
	 *
	 * @param sess PerunSession for authz
	 * @param id ID of application form to get
	 * @return registration form
	 * @throws PerunException
	 */
	ApplicationForm getFormById(PerunSession sess, int id) throws PerunException;

	/**
	 * Gets an application form for a given form item ID.
	 *
	 * @param sess PerunSession for authz
	 * @param id ID of application form item to get form for
	 * @return registration form
	 * @throws PerunException
	 */
	ApplicationForm getFormByItemId(PerunSession sess, int id) throws PerunException;

	/**
	 * Adds a new item to a form.
	 *
	 * @param user            the user that adds the items
	 * @param applicationForm the form to change
	 * @param formItem        the new item
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @return created ApplicationFormItem with id and ordnum property set
	 */
	ApplicationFormItem addFormItem(PerunSession user, ApplicationForm applicationForm, ApplicationFormItem formItem) throws PrivilegeException, InternalErrorException;

	/**
	 * Updates whole FormItem object including ItemTexts and associated AppTypes
	 *
	 * @param sess Session for authorization
	 * @param form ApplicationForm to update items for
	 * @param items items to update (by their IDs)
	 * @return number of updated items
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	int updateFormItems(PerunSession sess, ApplicationForm form, List<ApplicationFormItem> items) throws PrivilegeException, InternalErrorException;

	/**
	 * Updates the form attributes, not the form items.
	 *  - update automatic approval style
	 *  - update module_name
	 *
	 * @param user            the user
	 * @param applicationForm the form
	 * @return number of updated rows (should be 1)
	 */
	int updateForm(PerunSession user, ApplicationForm applicationForm) throws InternalErrorException, PrivilegeException;

	/**
	 * Removes a form item permanently. The user data associated with it remain in the database, just they lose the foreign key
	 * reference which becomes null.
	 *
	 * @param user   the user
	 * @param form   the form
	 * @param ordnum index of the item, starting with zero
	 */
	void deleteFormItem(PerunSession user, ApplicationForm form, int ordnum) throws InternalErrorException, PrivilegeException;

	/**
	 * Changes position of an item in form.
	 *
	 * @param user   the user changing the form
	 * @param form   the form
	 * @param ordnum index of the item, starting with zero
	 * @param up     true for moving up (to lower ord number) or false for moving down (to higher ord number)
	 */
	void moveFormItem(PerunSession user, ApplicationForm form, int ordnum, boolean up) throws InternalErrorException, PrivilegeException;

	/**
	 * Updates internationalized texts for a form item.
	 * @param user user changing the form item texts
	 * @param item the form item to be changed
	 * @param locale the locale for which texts should be changed
	 */
	void updateFormItemTexts(PerunSession user, ApplicationFormItem item, Locale locale) throws PrivilegeException, PerunException;

	/**
	 * Updates internationalized texts for a form item (all locales are replaced by current value)
	 *
	 * @param user user changing the form item texts
	 * @param item the form item to be changed
	 */
	void updateFormItemTexts(PerunSession user, ApplicationFormItem item) throws PrivilegeException, PerunException;

	/**
	 * Gets all form items.
	 * @param sess PerunSession
	 * @param applicationForm the form
	 * @return all form items regardless of type
	 * @throws PerunException
	 */
	List<ApplicationFormItem> getFormItems(PerunSession sess, ApplicationForm applicationForm) throws PerunException;

	/**
	 * Gets form items of specified type, for initial registration or extension of account.
	 *
	 * @param form the form
	 * @param appType the type or null for all items
	 * @return items of specified type
	 * @throws PerunException
	 */
	List<ApplicationFormItem> getFormItems(PerunSession sess, ApplicationForm form, AppType appType) throws PerunException;

	/**
	 * Gets the content for an application form for a given type of application,vo, group and user.
	 * The values are pre-filled from database for extension applications, and always from federation values
	 * taken from the user argument.
	 *
	 * @param sess PerunSession including PerunPrincipal containing info from authentication system
	 * @param appType application type INITIAL or EXTENSION
	 * @param form ApplicationForm to get items for (specify vo and group)
	 * @return list of form items for a given application type with pre-filled values
	 * @throws PerunException
	 * @throws DuplicateRegistrationAttemptException when registration already exists
	 */
	List<ApplicationFormItemWithPrefilledValue> getFormItemsWithPrefilledValues(PerunSession sess, AppType appType, ApplicationForm form) throws PerunException;

	/**
	 * Creates a new application and if succeeds, trigger validation and approval.
	 *
	 * <p>The method triggers approval for VOs with auto-approved applications.
	 *
	 * @param user user present in session
	 * @param application application
	 * @param data data
	 * @return stored app data
	 * @throws PerunException
	 */
	List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException;

	/**
	 * Creates a new application in one transaction
	 *
	 * @param user user present in session
	 * @param application application
	 * @param data data
	 * @return stored app data
	 * @throws PerunException
	 */
	Application createApplicationInternal(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException;

	/**
	 * Delete application by Id. Application must be in state: NEW or REJECTED.
	 *
	 * @param session PerunSession
	 * @param application application
	 * @throws PerunException
	 */
	void deleteApplication(PerunSession session, Application application) throws PerunException;


	/**
	 * Gets all applications in a given state for a given VO.
	 * If state is null, returns all applications for a given VO.
	 *
	 * @param sess who is asking
	 * @param vo VO to get applications for
	 * @param state application state to filter by
	 * @return list of applications
	 * @throws PerunException
	 */
	List<Application> getApplicationsForVo(PerunSession sess, Vo vo, List<String> state) throws PerunException;

	/**
	 * Gets all applications in a given state for a given Group
	 * If state is null, returns all applications for a given Group.
	 *
	 * @param sess who is asking
	 * @param group Group to get applications for
	 * @param state application state to filter by
	 * @return list of applications
	 * @throws PerunException
	 */
	List<Application> getApplicationsForGroup(PerunSession sess, Group group, List<String> state) throws PerunException;


	/**
	 * Validates an email. THis method should receive all URL parameters from a URL sent by an email to validate
	 * the email address that was provided by a user. The parameters describe the user, application, email and contain
	 * a message authentication code to prevent spoofing.
	 *
	 * <p>The method triggers approval for VOs with auto-approved applications.
	 * @param urlParameters all of them
	 * @return true for validated, false for non-valid
	 * @throws PerunException
	 */
	boolean validateEmailFromLink(Map<String, String> urlParameters) throws PerunException;

	/**
	 * Forcefully marks application as verified
	 * (only when application was in NEW state)
	 *
	 * FIXME: for testing purpose now
	 *
	 * @param sess Session to verify user
	 * @param appId app to verify
	 * @throws PerunException
	 */
	Application verifyApplication(PerunSession sess, int appId) throws PerunException;

	/**
	 * Manually approves an application. Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param session who approves the application
	 * @param appId application id
	 * @throws PerunException
	 */
	Application approveApplication(PerunSession session, int appId) throws PerunException;

	/**
	 * Approves an application in one transaction.
	 *
	 * @param session who approves the application
	 * @param appId application id
	 * @throws PerunException
	 */
	Application approveApplicationInternal(PerunSession session, int appId) throws PerunException;

	/**
	 * Throws exception if application can't be approved based on form module rules.
	 * Is meant to be used from GUI before actual approval happens so VO/Group admin can override
	 * this default behavior.
	 *
	 * @param session Who wants to approve application
	 * @param application Application to check approval for
	 * @throws CantBeApprovedException
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws PerunException
	 */
	void canBeApproved(PerunSession session, Application application) throws PerunException;

	/**
	 * Manually rejects an application. Expected to be called as a result of direct VO administrator action in the web UI.
	 *
	 * @param session who rejects the application
	 * @param appId application id
	 * @param reason optional reason of rejection displayed to user
	 * @throws PerunException
	 */
	Application rejectApplication(PerunSession session, int appId, String reason) throws PerunException;

	/**
	 * Returns data submitted by user in given application (by id)
	 *
	 * @param sess PerunSession
	 * @param appId application to get user's data for
	 * @return data submitted by user in given application
	 * @throws PerunException
	 */
	List<ApplicationFormItemData> getApplicationDataById(PerunSession sess, int appId) throws PerunException;

	/**
	 * Returns applications submitted by user
	 *
	 * @param user user to get applications for
	 * @return applications submitted by user
	 */
	List<Application> getApplicationsForUser(User user);

	/**
	 * Returns applications submitted by user (in session)
	 *
	 * @param sess PerunSession
	 * @return applications submitted by user (in session)
	 */
	List<Application> getApplicationsForUser(PerunSession sess);

	/**
	 * Returns applications submitted by member of VO or Group
	 *
	 * @param sess PerunSession
	 * @param group group to filter by or NULL if want apps for whole VO including all groups
	 * @param member member
	 * @return applications submitted by member
	 * @throws PerunException
	 */
	List<Application> getApplicationsForMember(PerunSession sess, Group group, Member member) throws PerunException;

	/**
	 * Returns full form item including texts and appTypes
	 *
	 * @param session PerunSession for authz
	 * @param id ID of form item to return
	 * @return form item
	 * @throws PrivilegeException
	 */
	ApplicationFormItem getFormItemById(PerunSession session, int id) throws PrivilegeException;

	/**
	 * Returns full form item including texts and appTypes
	 * For Internal use only !!
	 *
	 * @param id ID of form item to return
	 * @return form item
	 */
	ApplicationFormItem getFormItemById(int id);

	/**
	 * Update form item by it's ID.
	 *
	 * @param session PerunSession for authz
	 * @param item Application form item to update
	 * @throws PrivilegeException
	 * @throws PerunException
	 */
	void updateFormItem(PerunSession session, ApplicationFormItem item) throws PrivilegeException, PerunException;

	/**
	 * Returns application object by it's ID
	 *
	 * @param sess session to authorize VO admin
	 * @param id ID of application to return
	 * @return application
	 * @throws PerunException
	 */
	Application getApplicationById(PerunSession sess, int id) throws PerunException;

	/**
	 * Copy all form items from selected VO into another.
	 *
	 * @param sess PerunSession for authz
	 * @param fromVo VO to get form items from
	 * @param toVo VO to set new form items
	 * @throws PerunException
	 */
	void copyFormFromVoToVo(PerunSession sess, Vo fromVo, Vo toVo) throws PerunException;

	/**
	 * Copy all form items from selected VO into Group and also in opposite direction.
	 *
	 * @param sess PerunSession for authz
	 * @param fromVo VO to copy form items from (or opposite if reverse=TRUE)
	 * @param toGroup Group to copy form items into (or opposite if reverse=TRUE)
	 * @param reverse FALSE = copy from VO to Group (default) / TRUE = copy from Group to VO
	 * @throws PerunException
	 */
	public void copyFormFromVoToGroup(PerunSession sess, Vo fromVo, Group toGroup, boolean reverse) throws PerunException;

	/**
	 * Copy all form items from selected Group into another.
	 *
	 * @param sess PerunSession for authz
	 * @param fromGroup Group to get form items from
	 * @param toGroup Group to set new form items
	 * @throws PerunException
	 */
	void copyFormFromGroupToGroup(PerunSession sess, Group fromGroup, Group toGroup) throws PerunException;

	/**
	 * Getter for Mail manager used for notifications
	 *
	 * @return mail manager
	 */
	MailManager getMailManager();

	/**
	 * Getter for Consolidator manager used for notifications
	 *
	 * @return consolidator manager
	 */
	ConsolidatorManager getConsolidatorManager();

}
