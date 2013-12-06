package cz.metacentrum.perun.webgui.client.localization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * Translations for buttons
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: c7243aabccd0a29f77580b056d6c4684ba108e5e $
 */
public interface ButtonTranslation extends Messages {

    public static final ButtonTranslation INSTANCE = GWT.create(ButtonTranslation.class);

    /* ========== BUTTON TEXT ==================*/

    /* PRE-DEFINED BUTTONS */

    @DefaultMessage("Add")
    String addButton();

    @DefaultMessage("Add{0}")
    String addButtonWhat(String what);

    @DefaultMessage("Create")
    String createButton();

    @DefaultMessage("Create{0}")
    String createButtonWhat(String what);

    @DefaultMessage("Remove")
    String removeButton();

    @DefaultMessage("Remove{0}")
    String removeButtonWhat(String what);

    @DefaultMessage("Delete")
    String deleteButton();

    @DefaultMessage("Delete{0}")
    String deleteButtonWhat(String what);

    @DefaultMessage("Save")
    String saveButton();

    @DefaultMessage("Save{0}")
    String saveButtonWhat(String what);

    @DefaultMessage("Search")
    String searchButton();

    @DefaultMessage("Search{0}")
    String searchButtonWhat(String what);

    @DefaultMessage("Filter")
    String filterButton();

    @DefaultMessage("Filter{0}")
    String filterButtonWhat(String what);

    @DefaultMessage("Ok")
    String okButton();

    @DefaultMessage("Cancel")
    String cancelButton();

    @DefaultMessage("Close")
    String closeButton();

    @DefaultMessage("Continue")
    String continueButton();

    @DefaultMessage("Back")
    String backButton();

    @DefaultMessage("Finish")
    String finishButton();

    @DefaultMessage("Fill")
    String fillButton();

    @DefaultMessage("List all")
    String listAllMembersButton();

    @DefaultMessage("List all")
    String listAllUsersButton();

    @DefaultMessage("Verify")
    String verifyButton();

    @DefaultMessage("Approve")
    String approveButton();

    @DefaultMessage("Reject")
    String rejectButton();

    @DefaultMessage("Refresh")
    String refreshButton();

    @DefaultMessage("Preview")
    String previewButton();

    @DefaultMessage("Settings")
    String settingsButton();

    @DefaultMessage("Settings{0}")
    String settingsButtonWhat(String what);

    @DefaultMessage("Enable")
    String enableButton();

    @DefaultMessage("Disable")
    String disableButton();

    /* CUSTOM BUTTONS */

    @DefaultMessage("Copy from VO")
    String copyFromVoButton();

    @DefaultMessage("Copy from group")
    String copyFromGroupButton();

    @DefaultMessage("Notifications")
    String emailNotificationsButton();

    @DefaultMessage("E-mail footer")
    String mailFooterButton();

    @DefaultMessage("Create service member")
    String createServiceMemberButton();

    @DefaultMessage("Search in external sources")
    String searchForMembersInExtSourcesButton();

    @DefaultMessage("Search among existing users")
    String searchForMembersInPerunUsersButton();

    @DefaultMessage("Edit")
    String editFormItemButton();

    @DefaultMessage("Restore")
    String undeleteFormItemButton();

    @DefaultMessage("Switch to EXTENSION")
    String switchToExtensionButton();

    @DefaultMessage("Switch to INITIAL")
    String switchToInitialButton();

    @DefaultMessage("Switch to Czech")
    String switchToCzechButton();

    @DefaultMessage("Switch to English")
    String switchToEnglishButton();

    @DefaultMessage("Add member")
    String addMemberButton();

    @DefaultMessage("Add manager")
    String addManagerButton();

    @DefaultMessage("Create group")
    String createGroupButton();

    @DefaultMessage("Add member to resource")
    String addMemberToResourceButton();

    @DefaultMessage("Edit")
    String editButton();

    @DefaultMessage("Force propagation")
    String forcePropagationButton();

    @DefaultMessage("Block")
    String blockPropagationButton();

    @DefaultMessage("Allow")
    String allowPropagationButton();

    @DefaultMessage("List users without VO")
    String listUsersWithoutVoButton();

    @DefaultMessage("Logout")
    String logoutButton();

    @DefaultMessage("Select")
    String selectIdentityButton();

    /* ========== BUTTON TITLE - ON HOVER TEXT TRANSLATION ==================*/

    /* ATTRIBUTES */

    @DefaultMessage("Create new attribute definition")
    String createAttributeDefinition();

    @DefaultMessage("Delete selected attribute definitions")
    String deleteAttributeDefinition();

    @DefaultMessage("Filter attribute definitions by name")
    String filterAttributeDefinition();

    @DefaultMessage("Save new attributes")
    String saveNewAttributes();

    @DefaultMessage("Save changes in selected attributes")
    String saveChangesInAttributes();

    @DefaultMessage("Set new attributes")
    String setNewAttributes();

    @DefaultMessage("Remove values from selected attributes")
    String removeAttributes();

    /* VOS */

    @DefaultMessage("Create new virtual organization")
    String createVo();

    @DefaultMessage("Delete selected virtual organizations")
    String deleteVo();

    @DefaultMessage("Filter virtual organizations by name")
    String filterVo();

    /* SLDS */

    @DefaultMessage("Create new Service Level Description")
    String createSLD();

    @DefaultMessage("Delete selected SLDs")
    String deleteSLD();

    /* MEMBERS */

    @DefaultMessage("Add new member to VO")
    String addMemberToVo();

    @DefaultMessage("Add selected candidates to VO")
    String addSelectedCandidateToVo();

    @DefaultMessage("Remove selected members from VO")
    String removeMemberFromVo();

    @DefaultMessage("Search for members in VO by name, email or login")
    String searchMemberInVo();

    @DefaultMessage("Add new member to group")
    String addMemberToGroup();

    @DefaultMessage("Add selected members to group")
    String addSelectedMemberToGroup();

    @DefaultMessage("Remove selected members from group")
    String removeMemberFromGroup();

    @DefaultMessage("Search for members in group by name, email or login")
    String searchMemberInGroup();

    @DefaultMessage("Search for members in parent group by name, email or login")
    String searchMemberInParentGroup();

    @DefaultMessage("List all members in VO")
    String listAllMembersInVo();

    @DefaultMessage("List all members in group")
    String listAllMembersInGroup();

    @DefaultMessage("Create service member in VO (can have multiple users assigned)")
    String createServiceMember();

    @DefaultMessage("Search in external sources like LDAP, SQL DB,...")
    String searchForMembersInExtSources();

    @DefaultMessage("Search among existing Perun users")
    String searchForMembersInPerunUsers();

    @DefaultMessage("Add member to specific resource")
    String addMemberToResource();

    @DefaultMessage("Filter members by name")
    String filterMembers();

    @DefaultMessage("Change membership status for {0}")
    String changeStatus(String name);

    /* MANAGERS */

    @DefaultMessage("Add new VO manager")
    String addManagerToVo();

    @DefaultMessage("Add selected users as VO`s managers")
    String addSelectedManagersToVo();

    @DefaultMessage("Remove selected VO managers")
    String removeManagerFromVo();

   @DefaultMessage("Add new group manager")
    String addManagerToGroup();

    @DefaultMessage("Add selected users as VO`s managers")
    String addSelectedManagersToGroup();

    @DefaultMessage("Remove selected group managers")
    String removeManagerFromGroup();

    @DefaultMessage("Add new facility manager")
    String addManagerToFacility();

    @DefaultMessage("Add selected users as facility managers")
    String addSelectedManagersToFacility();

    @DefaultMessage("Remove selected facility managers")
    String removeManagerFromFacility();

    /* USERS */

    @DefaultMessage("Search for users by name, email or login")
    String searchUsers();

    @DefaultMessage("List all users in Perun without VO")
    String listUsersWithoutVo();

    /* RESOURCES */

    @DefaultMessage("Create new resource for VO")
    String createResource();

    @DefaultMessage("Delete selected resources")
    String deleteResource();

    @DefaultMessage("Filter resources by name")
    String filterResources();

    @DefaultMessage("Edit resource details")
    String editResourceDetails();

    @DefaultMessage("Save changes in resource details")
    String saveResourceDetails();

    @DefaultMessage("Assign new group to this resource")
    String assignGroupToResource();

    @DefaultMessage("Assign group to selected resources")
    String assignGroupToSelectedResources();

    @DefaultMessage("Assign this group to new resources")
    String assignGroupToResources();

    @DefaultMessage("Remove selected groups from this resource")
    String removeGroupFromResource();

    @DefaultMessage("Remove selected groups from this resource")
    String removeGroupFromSelectedResources();

    @DefaultMessage("Assign new service to this resource")
    String assignServiceToResource();

    @DefaultMessage("Remove selected services from this resource")
    String removeServiceFromResource();

    @DefaultMessage("Assign selected groups to resource")
    String assignSelectedGroupsToResource();

    @DefaultMessage("Assign selected services to resource")
    String assignSelectedServicesToResource();

    @DefaultMessage("Fill values for all empty displayed attributes based on current configuration. Changes are not saved until you click on \"Save\" button.")
    String fillResourceAttributes();

    @DefaultMessage("Finish assigning of selected group")
    String finishGroupAssigning();

    /* GROUPS */

    @DefaultMessage("Create new group")
    String createGroup();

    @DefaultMessage("Create new sub-group")
    String createSubGroup();

    @DefaultMessage("Delete selected groups")
    String deleteGroup();

    @DefaultMessage("Delete selected sub-groups")
    String deleteSubGroup();

    @DefaultMessage("Filter groups by name")
    String filterGroup();

    @DefaultMessage("Edit group details")
    String editGroupDetails();

    @DefaultMessage("Save changes in group`s details")
    String saveGroupDetails();

    /* EXT SOURCES */

    @DefaultMessage("Add new external source of users")
    String addExtSource();

    @DefaultMessage("Add selected external sources of users")
    String addSelectedExtSource();

    @DefaultMessage("Remove selected external sources of users")
    String removeExtSource();

    /* APPLICATIONS + NOTIFICATIONS */

    @DefaultMessage("Verify selected applications")
    String verifyApplication();

    @DefaultMessage("Approve selected applications (must be in state VERIFIED)")
    String approveApplication();

    @DefaultMessage("Reject selected applications")
    String rejectApplication();

    @DefaultMessage("Delete selected applications (must be in state NEW or REJECTED)")
    String deleteApplication();

    @DefaultMessage("Filter applications by user")
    String filterApplications();

    @DefaultMessage("Save changes made in application form")
    String saveApplicationFormSettings();

    @DefaultMessage("Add new application form item")
    String addNewAppFormItem();

    @DefaultMessage("Preview application form (with unsaved changes)")
    String previewAppForm();

    @DefaultMessage("Change application form settings")
    String changeAppFormSettings();

    @DefaultMessage("Copy all form items from another VO into this")
    String copyFromVo();

    @DefaultMessage("Copy all form items from another group into this")
    String copyFromGroup();

    @DefaultMessage("Manage e-mail notifications")
    String emailNotifications();

    @DefaultMessage("Copy notifications from another VO into yours")
    String copyMailsFromVo();

    @DefaultMessage("Copy notifications from another group into yours")
    String copyMailsFromGroup();

    @DefaultMessage("Add new mail notification")
    String addMail();

    @DefaultMessage("Remove selected mail notifications")
    String removeMail();

    @DefaultMessage("Enable sending for selected mail notifications")
    String enableMail();

    @DefaultMessage("Disable sending for selected mail notifications")
    String disableMail();

    @DefaultMessage("Edit common footer, which can be added to each mail by mailFooter")
    String editMailFooter();

    @DefaultMessage("Edit form item properties")
    String editFormItem();

    @DefaultMessage("Delete form item")
    String deleteFormItem();

    @DefaultMessage("Move form item up")
    String moveFormItemUp();

    @DefaultMessage("Move form item down")
    String moveFormItemDown();

    @DefaultMessage("Restore deleted form item")
    String undeleteFormItem();

    @DefaultMessage("Create and configure new form item")
    String createFormItem();

    @DefaultMessage("Save local changes of form item")
    String saveFormItem();

    @DefaultMessage("Switch between INITIAL and EXTENSION")
    String switchBetweenInitialAndExtension();

    @DefaultMessage("Switch between Czech and English language")
    String switchBetweenCzechAndEnglish();

    @DefaultMessage("Create new e-mail notification for application")
    String createEmailNotificationForApplication();

    @DefaultMessage("Save changes in e-mail notification for application")
    String saveEmailNotificationForApplication();

    @DefaultMessage("Create empty application form")
    String createEmptyApplicationForm();

    @DefaultMessage("Add new item into selectionbox / combobox widget")
    String addNewSelectionBoxItem();

    @DefaultMessage("Save changes in mail footer definition")
    String saveMailFooter();

    @DefaultMessage("Filter applications by VO or group")
    String filterByVoOrGroup();

    /* OWNERS */

    @DefaultMessage("Create new owner")
    String createOwner();

    @DefaultMessage("Delete selected owners")
    String deleteOwner();

    @DefaultMessage("Filter owners by name, contact or type")
    String filterOwners();

    @DefaultMessage("Add selected owners")
    String addOwners();

    @DefaultMessage("Add new owners")
    String addNewOwners();

    @DefaultMessage("Remove selected owners")
    String removeSelectedOwners();

    /* FACILITIES */

    @DefaultMessage("Create new facility")
    String createFacility();

    @DefaultMessage("Delete selected facilities")
    String deleteFacilities();

    @DefaultMessage("Filter facilities by name or owner")
    String filterFacilities();

    @DefaultMessage("Edit facility details")
    String editFacilityDetails();

    @DefaultMessage("Save changes in facility details")
    String saveFacilityDetails();

    @DefaultMessage("Finish configuration of facility")
    String finishFacilityConfiguration();

    @DefaultMessage("Refresh list of propagation results")
    String refreshPropagationResults();

    @DefaultMessage("Add new destination")
    String addDestination();

    @DefaultMessage("Remove selected destinations")
    String removeSelectedDestinations();

    @DefaultMessage("Filter destinations by name or service")
    String filterDestination();

    @DefaultMessage("Filter destinations by name or facility")
    String filterDestinationByFacility();

    @DefaultMessage("Force propagation of selected services")
    String forcePropagation();

    @DefaultMessage("Block propagation of selected services")
    String blockServicesOnFacility();

    @DefaultMessage("Allow propagation of selected services")
    String allowServicesOnFacility();

    @DefaultMessage("Add new hosts to facility")
    String addHost();

    @DefaultMessage("Remove selected hosts from facility")
    String removeHosts();

    /* PERUN ADMIN */

    @DefaultMessage("Refresh list of audit messages")
    String refreshAuditMessages();

    /* SERVICES */

    @DefaultMessage("Create new service in Perun")
    String createService();

    @DefaultMessage("Delete selected services from Perun")
    String deleteSelectedServices();

    @DefaultMessage("Add required attribute")
    String addRequiredAttribute();

    @DefaultMessage("Add selected attribute definitions between required by service")
    String addSelectedRequiredAttribute();

    @DefaultMessage("Remove selected attribute definitions from required by service")
    String removeSelectedRequiredAttributes();

    @DefaultMessage("Add dependent exec service")
    String addDependantExecService();

    @DefaultMessage("Remove selected dependent services")
    String removeSelectedDependantExecServices();

    @DefaultMessage("Add dependent exec service")
    String createExecService();

    @DefaultMessage("Remove selected dependent services")
    String deleteSelectedExecServices();

    /* ======= TITLES FOR DISABLED BUTTONS =============== */

    @DefaultMessage("Click to logout from Perun GUI")
    String logout();

    @DefaultMessage("Click to select identity")
    String select();

}
