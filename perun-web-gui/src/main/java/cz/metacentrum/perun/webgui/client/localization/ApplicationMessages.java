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

	@DefaultMessage("Error occurred when processing your application.")
	String errorWhileCreatingApplication();

	@DefaultMessage("Your application was not saved! <p> Login/password reservation failed in external system. <br /> Please contact support to resolve this issue before new application submission.")
	String errorWhileCreatingApplicationMessage();

	@DefaultMessage("My applications")
	String applications();

	@DefaultMessage("Person")
	String name();

	@DefaultMessage("Email")
	String email();

	@DefaultMessage("Organization")
	String organization();

	@DefaultMessage("Prove identity by")
	String identities();

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

	@DefaultMessage("Processing...")
	String processing();

	@DefaultMessage("<h2>Your IDP does not provide Level of Assurance but VO requires it.</h2><h2>Try to log-in with different IDP.</h2>")
	String noUserLoa();

	@DefaultMessage("<h2>Your Level of Assurance (provided by IDP) is not sufficient for applying for VO membership.</h2><h2>Try to log-in with different IDP.</h2>")
	String insufficientLoa();

	@DefaultMessage("<h2>Your Level of Assurance (provided by IDP) is not sufficient for membership extension in VO.</h2><h2>Try to log-in with different IDP.</h2>")
	String insufficientLoaForExtension();

	@DefaultMessage("<h2>Your membership cannot be extended right now.</h2><h2>There is fixed period before membership expiration, when membership can be extended.</h2>")
	String outsideExtensionPeriod();

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

}
