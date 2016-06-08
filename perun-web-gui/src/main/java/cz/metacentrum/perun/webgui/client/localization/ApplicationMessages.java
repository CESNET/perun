package cz.metacentrum.perun.webgui.client.localization;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * Set of messages for application form GUI
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public interface ApplicationMessages extends Messages {

	public static final ApplicationMessages INSTANCE = GWT.create(ApplicationMessages.class);

	@DefaultMessage("Applications")
	String applicationForm();

	@DefaultMessage("Missing value")
	String missingValue();

	@DefaultMessage("Incorrect format")
	String incorrectFormat();

	@DefaultMessage("Incorrect e-mail format")
	String incorrectEmailFormat();

	@DefaultMessage("Validating")
	String validating();

	@DefaultMessage("Username available")
	String usernameAvailable();

	@DefaultMessage("Username not available")
	String usernameNotAvailable();

	@DefaultMessage("Passwords do not match")
	String passwordsDontMatch();

	@DefaultMessage("Input text is too long")
	String inputTextIsTooLong();

	@DefaultMessage("Application for {0}")
	String applicationFormForVo(String voName);

	@DefaultMessage("Application for {0}")
	String applicationFormForGroup(String groupName);

	@DefaultMessage("Membership extension for {0}")
	String membershipExtensionForVo(String voName);

	@DefaultMessage("Membership extension for {0}")
	String membershipExtensionForGroup(String groupName);

	@DefaultMessage("Your application should by automatically approved within a few minutes. Check your e-mail for further information.")
	String autoApprovalApplicationText();

	@DefaultMessage("Please wait until your application is either approved or rejected by VO administrator. Check your e-mail for further information.")
	String nonAutoApprovalApplicationText();

	@DefaultMessage("Application for {0} has been successfully created.")
	String applicationSuccessfullySent(String voName);

	@DefaultMessage("You have provided unverified e-mail, please check your mailbox for validation link.")
	String ifEmailProvidedCheckInbox();

	@DefaultMessage("Extension application for VO {0} has been successfully sent.")
	String membershipExtensionSuccessfullySent(String voName);

	@DefaultMessage("Error occurred when processing your application.")
	String errorWhileCreatingApplication();

	@DefaultMessage("Your application was not saved! <p> Login/password reservation failed in external system. <br /> Please contact support to resolve this issue before new application submission.")
	String errorWhileCreatingApplicationMessage();

	@DefaultMessage("VO administrator has been notified about all errors.")
	String voAdministratorWasNotified();

	@DefaultMessage("{0}: my applications")
	String applicationsForUser(String user);

	@DefaultMessage("Filter by virtual organization:")
	String filterByVo();

	@DefaultMessage("{0} application for {1}")
	String applicationDetail(String type, String vo);

	@DefaultMessage("{0} application for group {1} in VO {2}")
	String applicationDetailGroup(String type, String group, String vo);

	@DefaultMessage("Application submitted at <strong>{0}</strong> is in state <strong>{1}</strong>")
	String applicationDetailMessage(String date, String state);

	@DefaultMessage("Back")
	String backButton();

	@DefaultMessage("Back to My applications")
	String backButtonMessage();

	@DefaultMessage("My applications")
	String applications();

	@DefaultMessage("Similar user(s) found")
	String similarUsersFound();

	@DefaultMessage("Similar user(s) are already registered in system. If it`s you, prove your identity by logging-in using one of the registered identities.")
	String similarUsersFoundIsItYou();

	@DefaultMessage("Person")
	String name();

	@DefaultMessage("Email")
	String email();

	@DefaultMessage("Organization")
	String organization();

	@DefaultMessage("Prove identity by")
	String identities();

	@DefaultMessage("No suitable identity found.")
	String noIdentsFound();

	@DefaultMessage("Yes")
	String yes();

	@DefaultMessage("No, continue")
	String noContinue();

	@DefaultMessage("Please, contact user support {2}, which will help you connect your new identity ({0} / {1}) to the existing one.")
	String forAddingNewIdentityContactSupport(String identity, String extSourceName, String support);

	@DefaultMessage("E-mail verification")
	String emailValidationMenuItem();

	@DefaultMessage("Your e-mail address has been verified !")
	String emailValidationSuccess();

	@DefaultMessage("Your e-mail address has NOT been verified !<p>Wrong verification code !")
	String emailValidationFail();

	@DefaultMessage("Change language to {0}")
	String changeLanguageToCzech(String lang);

	@DefaultMessage("Change language to English")
	String changeLanguageToEnglish();

	@DefaultMessage("<p>Changing language will reload whole application.</p><p><strong>All unsaved changes will be lost.</strong></p><p>Do you want to proceed ?</p>")
	String changeLanguageText();

	@DefaultMessage("<h2>You have already sent your initial application to this VO / group.</h2><h2>Please wait until your application is either approved or rejected by VO / group administrator.</h2>")
	String duplicateRegistrationAttemptExceptionText();

	@DefaultMessage("<h2>Your IDP doesn`t provide data required by application form.</h2><h2>Please contact your IDP to resolve this issue or try to log-in with different IDP.</h2>")
	String missingDataFromIDP();

	@DefaultMessage("<strong>Can`t reconstruct {0}. Missing IDP attributes: displayName, cn, givenName, sn.</strong>")
	String cantResolveIDPNameAttribute(String attrName);

	@DefaultMessage("<strong>Missing IDP attribute: </strong>")
	String missingIDPAttribute();

	@DefaultMessage("<h2>VO does not have application form defined.</h2>")
	String noFormDefined();

	@DefaultMessage("<h2>You are already member of VO.</h2>")
	String alreadyVoMember();

	@DefaultMessage("You are already member of {0}.")
	String groupMembershipCantBeExtended(String groupName);

	@DefaultMessage("Processing...")
	String processing();

	@DefaultMessage("Contact: ")
	String supportContact();

	@DefaultMessage("<h2>In order to continue to the registration page, please use CAPTCHA below.</h2>")
	String captchaDescription();

	@DefaultMessage("Answer is incorrect. Please try again.")
	String captchaErrorMessage();

	@DefaultMessage("Wrong CAPTCHA answer")
	String captchaErrorHeader();

	@DefaultMessage("Continue")
	String captchaSendButton();

	@DefaultMessage("<h2>Your IDP does not provide Level of Assurance but VO requires it.</h2><h2>Try to log-in with different IDP.</h2>")
	String noUserLoa();

	@DefaultMessage("<h2>Your Level of Assurance (provided by IDP) is not sufficient for applying for VO membership.</h2><h2>Try to log-in with different IDP.</h2>")
	String insufficientLoa();

	@DefaultMessage("<h2>Your Level of Assurance (provided by IDP) is not sufficient for membership extension in VO.</h2><h2>Try to log-in with different IDP.</h2>")
	String insufficientLoaForExtension();

	@DefaultMessage("<h2>Your membership cannot be extended right now.</h2><h2>There is fixed period before membership expiration, when membership can be extended.</h2>")
	String outsideExtensionPeriod();

	@DefaultMessage("Join identity")
	String joinIdentity();

	@DefaultMessage("No, thanks")
	String notJoinIdentity();

	@DefaultMessage("Created date")
	String createdDate();

	@DefaultMessage("Type")
	String type();

	@DefaultMessage("State")
	String state();

	@DefaultMessage("Virtual organization")
	String virtualOrganization();

	@DefaultMessage("Group")
	String group();

	@DefaultMessage("Click on application to see more details.")
	String clickOnApplicationToSee();

	@DefaultMessage("In order to apply for group membership, you must be VO member first.")
	String mustBeVoMemberFirst();

	@DefaultMessage("Logout")
	String logout();

}
