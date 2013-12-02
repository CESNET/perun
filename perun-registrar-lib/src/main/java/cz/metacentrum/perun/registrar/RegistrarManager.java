package cz.metacentrum.perun.registrar;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
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
 * @version $Id: dee17e9f6ff7127b81c945a25a123e6a1ca8e82b $
 */
@SuppressWarnings("UnusedDeclaration")
public interface RegistrarManager {

    /**
     * Retrieves all necessary data about VO under registrar session
     * 
     * @param voShortName VO's shortname to get info about
     * @param groupName Groups name to get info about
     * @return List of VO attributes
     * @throws PerunException
     */
    public List<Attribute> initialize(String voShortName, String groupName) throws PerunException;
	
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
     * @param locale the locale for which texts shoudl be changed
     */
    void updateFormItemTexts(PerunSession user, ApplicationFormItem item, Locale locale);

    /**
     * Gets all form items.
     * @param applicationForm the form
     * @return all form items regardless of type
     */
    List<ApplicationFormItem> getFormItems(ApplicationForm applicationForm);

    /**
     * Gets form items of specified type, for initital registration or extension of account.
     * @param form the form
     * @param appType the type or null for all items
     * @return items of specified type
     */
    List<ApplicationFormItem> getFormItems(ApplicationForm form, AppType appType);

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
     */
    List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException;

    /**
     * Creates a new application in one transaction
     *
     * @param user user present in session
     * @param application application
     * @param data data
     * @return stored app data
     */
    Application createApplicationInternal(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException;

    /**
     * Delete application by Id. Application must be in state: NEW or REJECTED.
     *
     * @param session PerunSession
     * @param application application
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
     * @throws PrivilegeException if not authorized 
     * @throws InternalErrorException other reasons
     */
    Application verifyApplication(PerunSession sess, int appId) throws PrivilegeException, InternalErrorException;
    
    /**
     * Manually approves an application. Expected to be called as a result of direct VO administrator action in the web UI.
     * 
     * @param session who approves the application
     * @param appId application id
     */
    Application approveApplication(PerunSession session, int appId) throws PerunException;

    /**
     * Approves an application in one transaction.
     *
     * @param session who approves the application
     * @param appId application id
     */
    Application approveApplicationInternal(PerunSession session, int appId) throws PerunException;

    /**
     * Manually rejects an application. Expected to be called as a result of direct VO administrator action in the web UI.
     * 
     * @param session who rejects the application
     * @param appId application id
     * @param reason optional reason of rejection displayed to user
     */
    Application rejectApplication(PerunSession session, int appId, String reason) throws PerunException;
    
    /**
     * Returns data submitted by user in given application (by id)
     * 
     * @param sess PerunSession
     * @param appId application to get user's data for
     * @return data submitted by user in given application
     */
    List<ApplicationFormItemData> getApplicationDataById(PerunSession sess, int appId);

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
     */
    List<Application> getApplicationsForMember(PerunSession sess, Group group, Member member) throws PerunException;

    /**
     * Returns full form item including texts and appTypes
     * 
     * @param id ID of form item to return
     * @return form item
     */
    ApplicationFormItem getFormItemById(int id);
    
    /**
     * Returns application object by it's ID
     * 
     * @param sess session to authorize VO admin
     * @param id ID of application to return
     * @return application
     */
    Application getApplicationById(PerunSession sess, int id) throws PerunException;

    /**
     * Copy all form items from selected VO into another.
     * 
     * @param sess
     * @param fromVo VO to get form items from
     * @param toVo VO to set new form items
     * @throws PerunException
     */
    void copyFormFromVoToVo(PerunSession sess, Vo fromVo, Vo toVo) throws PerunException;

    /**
     * Copy all form items from selected Group into another.
     *
     * @param sess
     * @param fromGroup Group to get form items from
     * @param toGroup Group to set new form items
     * @throws PerunException
     */
    void copyFormFromGroupToGroup(PerunSession sess, Group fromGroup, Group toGroup) throws PerunException;
    
    /**
     * Getter for mail manager used for notifications
     * 
     * @return mail manager
     */
    MailManager getMailManager();

    /**
     * Check if new application may belong to another user in perun
     * (but same person in real life)
     *
     * Return list of similar users (by name).
     *
     * @param sess PerunSession for authz
     * @param appId ID of application to check for
     * @return List of similar users found
     * @throws PerunException
     */
    List<User> checkForSimilarUsers(PerunSession sess, int appId) throws PerunException;
    
}