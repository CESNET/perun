package cz.metacentrum.perun.webgui.client.localization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * Translations for common widgets in GUI
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface WidgetTranslation extends Messages {

	public static final WidgetTranslation INSTANCE = GWT.create(WidgetTranslation.class);

	/* AJAX LOADER WIDGET */

	@DefaultMessage("No items found.")
	String noItemsFound();

	@DefaultMessage("Request timeout exceeded, please try again later.")
	String requestTimeout();

	@DefaultMessage("Server responded with an error.")
	String serverRespondedWithError();

	@DefaultMessage("Type the text to search form and click on the Search button.")
	String emptySearch();

	/* CUSTOM TEXTS FOR AJAX LOADER WIDGET */

	@DefaultMessage("Type in user`s First name, Last name (or both) or Login or Email and press Search button.")
	String emptySearchForUsers();

	@DefaultMessage("Search member by name, login, email or click 'List all' to see all members.")
	String emptySearchForMembers();

	@DefaultMessage("Type in external identification (login) of people you want to add as members and press Search button.")
	String emptySearchForCandidates();

	/* REST - CHECKBOX WIDGETS*/

	@DefaultMessage("Show also expired / disabled members")
	String showDisabledMembers();

	@DefaultMessage("Show also members with expired / disabled membership")
	String showDisabledMembersTitle();

	@DefaultMessage("Offer available only")
	String offerAvailableServicesOnly();

	@DefaultMessage("Offer only currently available services")
	String offerAvailableServicesOnlyTitle();

	@DefaultMessage("Configure group(s) before assign")
	String configureGroupBeforeAssign();

	@DefaultMessage("Configure group(s) and members settings before assigning to resource")
	String configureGroupBeforeAssignTitle();

	@DefaultMessage("Display all services")
	String displayAllServices();

	@DefaultMessage("Display all possible services to create destination for")
	String displayAllServicesTitle();

	@DefaultMessage("Use names of all facility`s hosts")
	String useFacilityHostnames();

	@DefaultMessage("Use names of all hosts on facility as destinations for selected service(s)")
	String useFacilityHostnamesTitle();

	/* CANT SAVE EMPTY LIST CONFIRM */

	@DefaultMessage("No items selected")
	String cantSaveEmptyListConfirmHeader();

	@DefaultMessage("To make changes please select some items first.")
	String cantSaveEmptyListConfirmMessage();

	/* SEARCH STRING CANT BE EMPTY */

	@DefaultMessage("Search string is empty")
	String searchStringCantBeEmptyHeader();

	@DefaultMessage("Can`t search for items by empty string.")
	String searchStringCantBeEmptyMessage();

	/* CANT SAVE EMPTY ATTRIBUTE */

	@DefaultMessage("Wrong attribute value")
	String cantSaveAttributeValueDialogBoxHeader();

	@DefaultMessage("Can`t save null or empty string as value for attribute <strong>{0}</strong>.<p>To safely remove value please use Remove button.")
	String cantSaveAttributeValueDialogBoxWrongString(String attrName);

	@DefaultMessage("Can`t save \"Not a number\" as value for attribute <strong>{0}</strong>.<p>To safely remove value please use Remove button.")
	String cantSaveAttributeValueDialogBoxWrongInteger(String attrName);

	@DefaultMessage("Can`t save empty list as value for attribute <strong>{0}</strong>.<p>To safely remove value please use Remove button.")
	String cantSaveAttributeValueDialogBoxWrongList(String attrName);

	@DefaultMessage("Can`t parse value for attribute <strong>{0}</strong>.<p>Please try again or to safely remove value use Remove button.")
	String cantSaveAttributeValueDialogBoxGeneral(String attrName);

	/* JSONCLIENT / JSONPOSTCLIENT ALERT BOX */

	@DefaultMessage("Server responded with an error")
	String jsonClientAlertBoxHeader();

	@DefaultMessage("Report error")
	String jsonClientReportErrorButton();

	@DefaultMessage("You are not authorized")
	String jsonClientNotAuthorizedHeader();

	@DefaultMessage("You are not authorized to perform this action. If you think you should have this right, send us report.")
	String jsonClientNotAuthorizedMessage();

	@DefaultMessage("Send report to RT")
	String jsonClientSendErrorButton();

	@DefaultMessage("Error type:")
	String jsonClientAlertBoxErrorType();

	@DefaultMessage("Error text:")
	String jsonClientAlertBoxErrorText();

	@DefaultMessage("Request:")
	String jsonClientAlertBoxErrorRequest();

	@DefaultMessage("POST&nbsp;data:")
	String jsonClientAlertBoxErrorPostData();

	@DefaultMessage("App&nbsp;state:")
	String jsonClientAlertBoxErrorAppState();

	@DefaultMessage("Authorization:")
	String jsonClientAlertBoxErrorAuthz();

	@DefaultMessage("Subject:")
	String jsonClientAlertBoxErrorSubject();

	@DefaultMessage("Message:")
	String jsonClientAlertBoxErrorMessage();

	@DefaultMessage("Error while sending request")
	String jsonClientAlertBoxErrorCrossSiteType();

	@DefaultMessage("Response was null or blocked by browser (cross-site request).")
	String jsonClientAlertBoxErrorCrossSiteText();

	/* FOOTER SETTINGS BUTTONS */

	@DefaultMessage("Change language to {0}")
	String changeLanguageToCzech(String lang);

	@DefaultMessage("Change language to English")
	String changeLanguageToEnglish();

	@DefaultMessage("<p>Changing language will reload whole application.</p><p><strong>All unsaved changes will be lost.</strong></p><p class=\"inputFormInlineComment\">Function is experimental - only few strings are translated to Czech at the moment.</p><p>Do you want to proceed ?</p>")
	String changeLanguageConfirmText();

	@DefaultMessage("Show / hide extended information")
	String showHideExtendedInfo();

	/* LISTBOX STANDARD PARAMS */

	@DefaultMessage("All")
	String listboxAll();

	@DefaultMessage("Not selected")
	String listboxNotSelected();

	/* LISTBOX STANDARD PARAMS */

	@DefaultMessage("By selecting a member you switch to members settings")
	String selectingMember();

	/* PERUN ATTRIBUTE CELLS */

	@DefaultMessage("Add new value")
	String addValue();

	@DefaultMessage("Remove this value")
	String removeValue();

	@DefaultMessage("You are not allowed to change this value")
	String notWritable();

	/* TAB PANEL BUTTONS */

	@DefaultMessage("Refresh tab")
	String refreshTabButton();

	@DefaultMessage("Move to left")
	String moveLeftButton();

	@DefaultMessage("Move to right")
	String moveRightButton();

	@DefaultMessage("Close other tabs")
	String closeOthersButton();

	@DefaultMessage("Close this tab")
	String closeThisTab();

	/* CUSTOM BUTTON WIDGET */

	@DefaultMessage("Processing...")
	String customButtonProcessing();

	/* DELETE CONFIRM WIDGET */

	@DefaultMessage("Confirm delete action")
	String deleteConfirmTitle();

	@DefaultMessage("Following items will be removed:")
	String deleteConfirmText();

	/* APPLICATION FORM */

	@DefaultMessage("Form doesn`t exists, do you wish to create new one ?")
	String formDoesntExists();

	/* SAVE CONFIRM WIDGET */

	@DefaultMessage("Confirm save action")
	String saveConfirmTitle();

}
