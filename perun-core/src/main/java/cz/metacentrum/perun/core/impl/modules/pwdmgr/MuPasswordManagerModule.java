package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.SponsoredUserData;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISResponseData;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISServiceCaller;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISServiceCaller.IS_ERROR_STATUS;
import static cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISServiceCaller.IS_OK_STATUS;

/**
 * Password manager implementation for MU login-namespace.
 * !! It doesn't reuse generic password manager !!
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class MuPasswordManagerModule implements PasswordManagerModule {

	private final static Logger log = LoggerFactory.getLogger(MuPasswordManagerModule.class);

	private static ISServiceCaller isServiceCaller = ISServiceCallerImpl.getInstance();

	protected int randomPasswordLength = 12;

	// omit chars that can be mistaken by users: iI, oO, l, yY, zZ, 0 (zero), most of spec.chars
	protected char[] randomPasswordCharacters = "ABCDEFGHJKLMNPQRSTUVWXabcdefghjkmnpqrstuvwx23456789,.-_".toCharArray();

	@Override
	public String handleSponsorship(PerunSession sess, SponsoredUserData userData) throws PasswordStrengthException {
		Map<String, String> parameters = new HashMap<>();

		// We need to support both - whole guestName and separate first/lastName
		if (StringUtils.isNotBlank(userData.getGuestName())) {
			User fakeUser = Utils.parseUserFromCommonName(userData.getGuestName(), true);
			parameters.put(PasswordManagerModule.TITLE_BEFORE_KEY, fakeUser.getTitleBefore());
			parameters.put(PasswordManagerModule.FIRST_NAME_KEY, fakeUser.getFirstName());
			parameters.put(PasswordManagerModule.LAST_NAME_KEY, fakeUser.getLastName());
			parameters.put(PasswordManagerModule.TITLE_AFTER_KEY, fakeUser.getTitleAfter());
		} else {
			parameters.put(PasswordManagerModule.TITLE_BEFORE_KEY, userData.getTitleBefore());
			parameters.put(PasswordManagerModule.FIRST_NAME_KEY, userData.getFirstName());
			parameters.put(PasswordManagerModule.LAST_NAME_KEY, userData.getLastName());
			parameters.put(PasswordManagerModule.TITLE_AFTER_KEY, userData.getTitleAfter());
		}

		if (userData.getPassword() != null) {
			parameters.put(PasswordManagerModule.PASSWORD_KEY, userData.getPassword());
		}

		return generateAccount(sess, parameters).get(PasswordManagerModule.LOGIN_PREFIX + "mu");
	}

	@Override
	public Map<String, String> generateAccount(PerunSession session, Map<String, String> parameters) throws PasswordStrengthException {

		String password = null;
		if (parameters.get(PASSWORD_KEY) != null && !parameters.get(PASSWORD_KEY).isEmpty()) {
			password = parameters.get(PASSWORD_KEY);
		}
		checkPasswordStrength(session, "--not yet known--", password);

		try {
			int requestID = (new Random()).nextInt(1000000) + 1;
			String requestBody = getGenerateAccountRequest(session, parameters, requestID);
			ISResponseData responseData = isServiceCaller.call(requestBody, requestID);
			if (!IS_OK_STATUS.equals(responseData.getStatus())) {
				throw new InternalErrorException("IS MU (password manager backend) responded with error to a Request ID: " + requestID + " Error: "+ responseData.getError());
			}
			return parseUCO(responseData.getResponse(), requestID);
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}

	}

	@Override
	public void reservePassword(PerunSession session, String userLogin, String password) {
		throw new InternalErrorException("Reserving password in login namespace 'mu' is not supported.");
	}

	@Override
	public void reserveRandomPassword(PerunSession session, String userLogin) {
		throw new InternalErrorException("Reserving random password in login namespace 'mu' is not supported.");
	}

	@Override
	public void checkPassword(PerunSession sess, String userLogin, String password) {
		// silently skip, since MU doesn't check old before change.
	}

	@Override
	public void changePassword(PerunSession sess, String userLogin, String newPassword) throws PasswordStrengthException {
		checkPasswordStrength(sess, userLogin, newPassword);

		changePasswordWithoutCheck(sess, userLogin, newPassword);
	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin, User user) throws InvalidLoginException {
		if (user == null) {
			user = ((PerunBl) sess.getPerun()).getModulesUtilsBl().getUserByLoginInNamespace(sess, userLogin, "mu");
		}

		if (user == null) {
			log.warn("No user was found by login '{}' in {} namespace.", userLogin, "mu");
		} else {
			// set extSources and extSource related attributes
			try {
				ExtSource extSource = ((PerunBl) sess.getPerun()).getExtSourcesManagerBl().getExtSourceByName(sess, "https://idp2.ics.muni.cz/idp/shibboleth");
				UserExtSource ues = new UserExtSource(extSource, userLogin + "@muni.cz");
				ues.setLoa(2);

				try {
					((PerunBl) sess.getPerun()).getUsersManagerBl().addUserExtSource(sess, user, ues);
				} catch (UserExtSourceExistsException ex) {
					//this is OK
				}
			} catch (ExtSourceNotExistsException ex) {
				throw new InternalErrorException(ex);
			}
		}

		// MU doesn't validate password
	}

	@Override
	public void deletePassword(PerunSession sess, String userLogin) {
		throw new InternalErrorException("Deleting user/password in login namespace 'mu' is not supported.");
	}

	@Override
	public void createAlternativePassword(PerunSession sess, User user, String passwordId, String password) {
		throw new InternalErrorException("Creating alternative password in login namespace 'mu' is not supported.");
	}

	@Override
	public void deleteAlternativePassword(PerunSession sess, User user, String passwordId) {
		throw new InternalErrorException("Deleting alternative password in login namespace 'mu' is not supported.");
	}

	@Override
	public void checkLoginFormat(PerunSession sess, String login) throws InvalidLoginException {

		// check login syntax/format
		((PerunBl)sess.getPerun()).getModulesUtilsBl().checkLoginNamespaceRegex("mu", login, GenericPasswordManagerModule.defaultLoginPattern);

		// check if login is permitted
		if (!((PerunBl)sess.getPerun()).getModulesUtilsBl().isUserLoginPermitted("mu", login)) {
			log.warn("Login '{}' is not allowed in {} namespace by configuration.", login, "mu");
			throw new InvalidLoginException("Login '"+login+"' is not allowed in 'mu' namespace by configuration.");
		}

	}

	@Override
	public void checkPasswordStrength(PerunSession sess, String login, String password) throws PasswordStrengthException {
		if (StringUtils.isBlank(password)) {
			log.warn("Password for {}:{} cannot be empty.", "mu", login);
			throw new PasswordStrengthException("Password for mu:" + login + " cannot be empty.");
		}

		// The IS password check is performed by trying to change a password to a user, which has been specifically
		// created for this purpose.
		String passwordTestUco = getPasswordTestUco();
		changePasswordWithoutCheck(sess, passwordTestUco, password);
	}

	@Override
	public String generateRandomPassword(PerunSession sess, String login) {

		String randomPassword = null;
		boolean strengthOk = false;
		while (!strengthOk) {

			randomPassword = RandomStringUtils.random(randomPasswordLength, 0, randomPasswordCharacters.length - 1,
					false, false, randomPasswordCharacters, new SecureRandom());

			try {
				checkPasswordStrength(sess, login, randomPassword);
				strengthOk = true;
			} catch (PasswordStrengthException ex) {
				strengthOk = false;
			}
		}

		return randomPassword;

	}

	public String getPasswordTestUco() {
		return BeansUtils.getPropertyFromCustomConfiguration("pwchange.mu.is", "muPasswordStrengthTestLogin");
	}

	private void changePasswordWithoutCheck(PerunSession sess, String login, String password) throws PasswordStrengthException {
		try {
			int requestID = (new Random()).nextInt(1000000) + 1;
			String requestBody = getPwdChangeRequest(sess, login, password, requestID);
			// if error, throws exception, otherwise it's ok
			ISResponseData responseData = isServiceCaller.call(requestBody, requestID);
			if (IS_ERROR_STATUS.equals(responseData.getStatus())) {
				throw new PasswordStrengthException(responseData.getError());
			}
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Generate XML request body from passed parameters in order to generate account.
	 *
	 * @param session
	 * @param parameters request parameters to pass
	 * @param requestID unique ID of a request
	 * @return XML request body
	 */
	private String getGenerateAccountRequest(PerunSession session, Map<String, String> parameters, int requestID) {

		log.debug("[IS Request {}] Making 'Generate account' request to IS MU.", requestID);

		String params = "";
		String loggedParams = "";

		if (parameters != null && !parameters.isEmpty()) {

			if (parameters.get(FIRST_NAME_KEY) != null && !parameters.get(FIRST_NAME_KEY).isEmpty()) {
				params += "<jmeno>" + escapeXMLChars(parameters.get(FIRST_NAME_KEY)) + "</jmeno>\n";
				loggedParams += "<jmeno>" + escapeXMLChars(parameters.get(FIRST_NAME_KEY)) + "</jmeno>\n";
			} else {
				// IS requires first and last name
				// in case of a single word name value, it's stored in a lastName, so send "guest" as a firstName if it's empty.
				params += "<jmeno>guest</jmeno>\n";
				loggedParams += "<jmeno>guest</jmeno>\n";
			}

			if (parameters.get(LAST_NAME_KEY) != null && !parameters.get(LAST_NAME_KEY).isEmpty()) {
				params += "<prijmeni>" + escapeXMLChars(parameters.get(LAST_NAME_KEY)) + "</prijmeni>\n";
				loggedParams += "<prijmeni>" + escapeXMLChars(parameters.get(LAST_NAME_KEY)) + "</prijmeni>\n";
			}
			if (parameters.get(TITLE_BEFORE_KEY) != null && !parameters.get(TITLE_BEFORE_KEY).isEmpty()) {
				params += "<titul_pred>" + escapeXMLChars(parameters.get(TITLE_BEFORE_KEY)) + "</titul_pred>\n";
				loggedParams += "<titul_pred>" + escapeXMLChars(parameters.get(TITLE_BEFORE_KEY)) + "</titul_pred>\n";
			}
			if (parameters.get(TITLE_AFTER_KEY) != null && !parameters.get(TITLE_AFTER_KEY).isEmpty()) {
				params += "<titul_za>" + escapeXMLChars(parameters.get(TITLE_AFTER_KEY)) + "</titul_za>\n";
				loggedParams += "<titul_za>" + escapeXMLChars(parameters.get(TITLE_AFTER_KEY)) + "</titul_za>\n";
			}
			if (parameters.get(BIRTH_DAY_KEY) != null && !parameters.get(BIRTH_DAY_KEY).isEmpty()) {
				params += "<datum_narozeni>" + escapeXMLChars(parameters.get(BIRTH_DAY_KEY)) + "</datum_narozeni>\n";
				loggedParams += "<datum_narozeni>" + escapeXMLChars(parameters.get(BIRTH_DAY_KEY)) + "</datum_narozeni>\n";
			}
			if (parameters.get(BIRTH_NUMBER_KEY) != null && !parameters.get(BIRTH_NUMBER_KEY).isEmpty()) {
				params += "<rodne_cislo>" + escapeXMLChars(parameters.get(BIRTH_NUMBER_KEY)) + "</rodne_cislo>\n";
				loggedParams += "<rodne_cislo>" + escapeXMLChars(parameters.get(BIRTH_NUMBER_KEY)) + "</rodne_cislo>\n";
			}
			if (parameters.get(MAIL_KEY) != null && !parameters.get(MAIL_KEY).isEmpty()) {
				params += "<email>" + escapeXMLChars(parameters.get(MAIL_KEY)) + "</email>\n";
				loggedParams += "<email>" + escapeXMLChars(parameters.get(MAIL_KEY)) + "</email>\n";
			}
			if (parameters.get(PASSWORD_KEY) != null && !parameters.get(PASSWORD_KEY).isEmpty()) {
				params += "<heslo>" + escapeXMLChars(parameters.get(PASSWORD_KEY)) + "</heslo>\n"; // password is not logged
				loggedParams += "<heslo>realPasswordIsNotLogged</heslo>\n";
			}
		}

		String ucoChanged = getUcoFromSessionUser(session);
		params += ucoChanged;
		loggedParams += ucoChanged;

		log.trace("[IS Request {}] File content:\n"+
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<request>\n" +
				"<osoba reqid=\"" + requestID + "\">\n" +
				"<uco></uco>\n" +
				loggedParams +
				"<operace>INS</operace>\n" +
				"</osoba>\n" +
				"</request>", requestID);

		return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<request>\n" +
				"<osoba reqid=\"" + requestID + "\">\n" +
				"<uco></uco>\n" +
				params +
				"<operace>INS</operace>\n" +
				"</osoba>\n" +
				"</request>";

	}

	/**
	 * Generate XML request body from passed parameters in order to change/reset password.
	 *
	 * @param session
	 * @param login
	 * @param newPassword
	 * @param requestID unique ID of a request
	 * @return XML request body
	 */
	private String getPwdChangeRequest(PerunSession session, String login, String newPassword, int requestID) {

		log.debug("[IS Request {}] Making 'Change password' request to IS MU.", requestID);

		String params = "";
		String loggedParams = "";
		if (newPassword != null && !newPassword.isEmpty()) {
			params += "<heslo>" + escapeXMLChars(newPassword) + "</heslo>\n";
			loggedParams += "<heslo>realPasswordIsNotLogged</heslo>\n";
		}
		String ucoChanged = getUcoFromSessionUser(session);
		params += ucoChanged;
		loggedParams += ucoChanged;

		log.trace("[IS Request {}] File content:\n" +
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<request>\n" +
				"<osoba reqid=\"" + requestID + "\">\n" +
				"<jazyk>en</jazyk>\n" +
				"<uco>" + login + "</uco>\n" +
				loggedParams +
				"<operace>UPD</operace>\n" +
				"</osoba>\n" +
				"</request>", requestID);

		return	"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<request>\n" +
				"<osoba reqid=\"" + requestID + "\">\n" +
				"<jazyk>en</jazyk>\n" +
				"<uco>" + login + "</uco>\n" +
				params +
				"<operace>UPD</operace>\n" +
				"</osoba>\n" +
				"</request>";

	}

	/**
	 * Parse UCO from XML body response and convert it to map of parameters.
	 *
	 * @param document XML document to be parsed
	 * @param requestID unique ID of a request
	 * @return Map of response params
	 * @throws InternalErrorException
	 */
	private Map<String, String> parseUCO(Document document, int requestID) {

		Map<String, String> result = new HashMap<>();

		//Prepare xpath expression
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression ucoExpr;
		try {
			ucoExpr = xpath.compile("//resp/uco/text()");
		} catch (XPathExpressionException ex) {
			throw new InternalErrorException("Error when compiling xpath query. Request ID: " + requestID, ex);
		}

		try {

			String uco = (String) ucoExpr.evaluate(document, XPathConstants.STRING);
			result.put(LOGIN_PREFIX+"mu", uco);

		} catch (XPathExpressionException ex) {
			throw new InternalErrorException("Error when evaluate xpath query on resulting document for request ID: " + requestID, ex);
		}

		return result;

	}

	/**
	 * Return MU UCO of a pwdmanager method caller from his UserExtSource in MU IdP.
	 *
	 * @param session Session to get user and identity from
	 * @return Part of API call params like: "<zmenil>UČO</zmenil>" or empty string.
	 */
	private String getUcoFromSessionUser(PerunSession session) {

		PerunBl perunBl = (PerunBl)session.getPerun();

		List<UserExtSource> ueses;
		try {
			if (session.getPerunPrincipal().getUser() != null) {
				ueses = perunBl.getUsersManagerBl().getUserExtSources(session, session.getPerunPrincipal().getUser());
			} else {
				return "";
			}
		} catch (Exception ex) {
			return "";
		}
		for (UserExtSource extSource : ueses) {
			if (extSource.getExtSource().getName().equals("https://idp2.ics.muni.cz/idp/shibboleth") &&
					extSource.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
				String login = extSource.getLogin();
				if (login != null) {
					log.debug(" - Action triggered by {}", login.split("@")[0]);
					return "<zmenil>" + login.split("@")[0] + "</zmenil>\n";
				}
			}
		}

		return "";

	}

	/**
	 * Escape restricted XML chars for element content (& < >).
	 *
	 * @param input Input to safely escape
	 * @return Output with escaped chars
	 */
	private String escapeXMLChars(String input) {
		return StringEscapeUtils.escapeXml10(input);
	}

	public ISServiceCaller getIsServiceCaller() {
		return isServiceCaller;
	}

	public void setIsServiceCaller(ISServiceCaller isServiceCaller) {
		MuPasswordManagerModule.isServiceCaller = isServiceCaller;
	}
}
