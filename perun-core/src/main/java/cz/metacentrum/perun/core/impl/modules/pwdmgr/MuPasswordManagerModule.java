package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Password manager implementation for MU login-namespace.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class MuPasswordManagerModule implements PasswordManagerModule {

	private final static Logger log = LoggerFactory.getLogger(MuPasswordManagerModule.class);
	private final static String CRLF = "\r\n"; // Line separator required by multipart/form-data.

	@Override
	public Map<String, String> generateAccount(PerunSession session, Map<String, String> parameters) throws InternalErrorException {

		try {
			int requestID = (new Random()).nextInt(1000000) + 1;
			InputStream response = makeCall(getGenerateAccountRequest(session, parameters, requestID), requestID);
			Document document = parseResponse(response, requestID);
			return parseUCO(document, requestID);
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}

	}

	@Override
	public void reservePassword(PerunSession session, String userLogin, String password) throws InternalErrorException{
		throw new InternalErrorException("Reserving password in login namespace 'mu' is not supported.");
	}

	@Override
	public void reserveRandomPassword(PerunSession session, String userLogin) throws InternalErrorException {
		throw new InternalErrorException("Reserving random password in login namespace 'mu' is not supported.");
	}

	@Override
	public void checkPassword(PerunSession sess, String userLogin, String password) {
		// silently skip, since MU doesn't check old before change.
	}

	@Override
	public void changePassword(PerunSession sess, String userLogin, String newPassword) throws InternalErrorException {

		try {
			int requestID = (new Random()).nextInt(1000000) + 1;
			InputStream response = makeCall(getPwdChangeRequest(sess, userLogin, newPassword, requestID), requestID);
			// if error, throws exception, otherwise it's ok
			parseResponse(response, requestID);
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}

	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin) {
		// silently skip, since generic code calls this but MU doesn't validate it.
	}

	@Override
	public void deletePassword(PerunSession sess, String userLogin) throws InternalErrorException {
		throw new InternalErrorException("Deleting user/password in login namespace 'mu' is not supported.");
	}

	/**
	 * Makes secure SSL connection to IS MU and perform required password manager action
	 *
	 * @param dataToPass XML request body
	 * @param requestId unique ID of a request
	 * @return InputStream response to be parsed
	 * @throws InternalErrorException
	 * @throws IOException
	 */
	private InputStream makeCall(String dataToPass, int requestId) throws InternalErrorException, IOException {

		//prepare sslFactory
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		HttpsURLConnection.setDefaultSSLSocketFactory(factory);

		// we want to log what we send
		StringBuilder logBuilder = new StringBuilder();

		String uri = BeansUtils.getPropertyFromCustomConfiguration("pwchange.mu.is", "uri");
		String login = BeansUtils.getPropertyFromCustomConfiguration("pwchange.mu.is", "login");
		String password = BeansUtils.getPropertyFromCustomConfiguration("pwchange.mu.is", "password");

		URL myurl = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
		String boundary = Long.toHexString(System.currentTimeMillis()); //random number for purpose of creating boundaries in multipart

		// Prepare the basic auth, if the username and password was specified
		if (login != null && password != null) {
			String val = login + ":" + password;
			Base64 encoder = new Base64();
			String base64Encoded = new String(encoder.encode(val.getBytes()));
			base64Encoded = base64Encoded.trim();
			String authorizationString = "Basic " + base64Encoded;
			con.setRequestProperty("Authorization", authorizationString);
		}
		con.setAllowUserInteraction(false);

		//set request header if is required (set in extSource xml)
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		log.trace("[IS Request {}] Content-Type: multipart/form-data; boundary={}", requestId, boundary);

		try (
				OutputStream output = con.getOutputStream();
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)
		) {
			// Send param about return
			writer.append("--" + boundary).append(CRLF);
			logBuilder.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"out\"").append(CRLF);
			logBuilder.append("Content-Disposition: form-data; name=\"out\"").append(CRLF);
			writer.append(CRLF).append("xml").append(CRLF).flush();
			logBuilder.append(CRLF).append("xml").append(CRLF);

			// Send xml file.
			writer.append("--" + boundary).append(CRLF);
			logBuilder.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"xml\"; filename=\"perun-pwd-manager.xml\"").append(CRLF);
			logBuilder.append("Content-Disposition: form-data; name=\"xml\"; filename=\"perun-pwd-manager.xml\"").append(CRLF);
			writer.append("Content-Type: text/xml; charset=" + StandardCharsets.UTF_8).append(CRLF); // Text file itself must be saved in this charset!
			logBuilder.append("Content-Type: text/xml; charset=" + StandardCharsets.UTF_8).append(CRLF);
			writer.append(CRLF).flush();
			logBuilder.append(CRLF);
			writer.append(dataToPass);
			logBuilder.append("\n--File content is logged separately--\n");
			output.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
			logBuilder.append(CRLF);

			// End of multipart/form-data.
			writer.append("--" + boundary + "--").append(CRLF).flush();
			logBuilder.append("--" + boundary + "--").append(CRLF);

			log.trace("[IS Request {}] {}", requestId, logBuilder.toString());

		}

		int responseCode = con.getResponseCode();
		if (responseCode == 200) {
			return con.getInputStream();
		} else {

			String response = null;
			try {
				response = convertStreamToString(con.getErrorStream(), "UTF-8");
			} catch (IOException ex) {
				log.error("Unable to convert InputStream to String: {}", ex);
			}

			log.trace("[IS Request {}] Response: {}", requestId, response);

		}

		throw new InternalErrorException("Wrong response code while opening connection on uri '" + uri + "'. Response code: " + responseCode + ". Request ID: " + requestId);

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
				"<uco>" + login + "</uco>\n" +
				loggedParams +
				"<operace>UPD</operace>\n" +
				"</osoba>\n" +
				"</request>", requestID);

		return	"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<request>\n" +
				"<osoba reqid=\"" + requestID + "\">\n" +
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
	private Map<String, String> parseUCO(Document document, int requestID) throws InternalErrorException {

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
	 * Parse XML response from IS MU to XML document.
	 *
	 * @param inputStream Stream to be parsed to Document
	 * @param requestID ID of request made to IS MU.
	 * @return XML document for further processing
	 * @throws InternalErrorException
	 */
	private Document parseResponse(InputStream inputStream, int requestID) throws InternalErrorException {

		//Create new document factory builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new InternalErrorException("Error when creating newDocumentBuilder. Request ID: " + requestID, ex);
		}

		String response;
		try {
			response = convertStreamToString(inputStream, "UTF-8");
		} catch (IOException ex) {
			throw new IllegalArgumentException("Unable to convert InputStream to String.", ex);
		}

		log.trace("[IS Request {}] Response: {}", requestID, response);
		log.debug("[IS Request {}] Processing response from IS MU.", requestID);

		Document doc;
		try {
			doc = builder.parse(new InputSource(new StringReader(response)));
		} catch (SAXParseException ex) {
			throw new InternalErrorException("Error when parsing uri by document builder. Request ID: " + requestID, ex);
		} catch (SAXException ex) {
			throw new InternalErrorException("Problem with parsing is more complex, not only invalid characters. Request ID: " + requestID, ex);
		} catch (IOException ex) {
			throw new InternalErrorException("Error when parsing uri by document builder. Problem with input or output. Request ID: " + requestID, ex);
		}

		//Prepare xpath expression
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression isErrorExpr;
		XPathExpression getErrorTextExpr;
		XPathExpression getDbErrorTextExpr;
		try {
			isErrorExpr = xpath.compile("//resp/stav/text()");
			getErrorTextExpr = xpath.compile("//resp/error/text()");
			getDbErrorTextExpr = xpath.compile("//resp/dberror/text()");
		} catch (XPathExpressionException ex) {
			throw new InternalErrorException("Error when compiling xpath query. Request ID: " + requestID, ex);
		}

		// OK or ERROR
		String responseStatus;
		try {
			responseStatus = (String) isErrorExpr.evaluate(doc, XPathConstants.STRING);
		} catch (XPathExpressionException ex) {
			throw new InternalErrorException("Error when evaluate xpath query on document to resolve response status. Request ID: " + requestID, ex);
		}

		log.trace("[IS Request {}] Response of request from IS MU has status: {}", requestID, responseStatus);
		log.debug("[IS Request {}] Response of request from IS MU has status: {}", requestID, responseStatus);

		if ("OK".equals(responseStatus)) {

			return doc;

		} else {

			try {
				String error = (String) getErrorTextExpr.evaluate(doc, XPathConstants.STRING);
				if (error == null || error.isEmpty()) {
					error = (String) getDbErrorTextExpr.evaluate(doc, XPathConstants.STRING);
				}
				throw new InternalErrorException("IS MU (password manager backend) responded with error to a Request ID: " + requestID + " Error: "+ error);
			} catch (XPathExpressionException ex) {
				throw new InternalErrorException("Error when evaluate xpath query on document to resolve error status. Request ID: " + requestID, ex);
			}

		}

	}

	/**
	 * Based on tests from: http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
	 * Most quicker and native InputStream reading method.
	 *
	 * @param inputStream Input stream to convert
	 * @param encoding encoding used to parse input stream
	 * @return Content of inputStream as a String
	 * @throws IOException
	 */
	static String convertStreamToString(InputStream inputStream, String encoding) throws IOException {

		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString(encoding);

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

}
