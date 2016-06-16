package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
	public Map<String, String> generateAccount(PerunSession session, String namespace, Map<String, String> parameters) throws InternalErrorException {

		try {
			int requestID = (new Random()).nextInt(1000000) + 1;
			InputStream response = makeCall(getGenerateAccountRequest(parameters, requestID), requestID);
			return parseResponse(response, requestID);
		} catch (IOException e) {
			throw new InternalErrorException(e);
		}

	}

	@Override
	public void reservePassword(PerunSession session, String userLogin, String loginNamespace, String password) throws InternalErrorException{
		throw new InternalErrorException("Reserving password in login namespace 'mu' is not supported.");
	}

	@Override
	public void reserveRandomPassword(PerunSession session, String userLogin, String loginNamespace) throws InternalErrorException {
		throw new InternalErrorException("Reserving random password in login namespace 'mu' is not supported.");
	}

	@Override
	public void changePassword(PerunSession sess, String loginNamespace, String oldPassword, String newPassword, boolean checkOldPassword) {



	}

	@Override
	public void validatePassword(PerunSession sess, String userLogin, String loginNamespace) throws InternalErrorException {
		throw new InternalErrorException("Validating password in login namespace 'mu' is not supported.");
	}

	@Override
	public void deletePassword(PerunSession sess, String userLogin, String loginNamespace) throws InternalErrorException {

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

		String uri = BeansUtils.getPropertyFromCustomConfiguration("pwchange.mu.is", "uri");
		String login = BeansUtils.getPropertyFromCustomConfiguration("pwchange.mu.is", "login");
		String password = BeansUtils.getPropertyFromCustomConfiguration("pwchange.mu.is", "password");

		URL myurl = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
		String boundary = Long.toHexString(System.currentTimeMillis()); //random number for purpose of creating boundaries in multipart

		// Prepare the basic auth, if the username and password was specified
		if (login != null && password != null) {
			String val = (new StringBuffer(login).append(":").append(password)).toString();
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

		try (
				OutputStream output = con.getOutputStream();
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
		) {
			// Send param about return
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"out\"").append(CRLF);
			writer.append(CRLF).append("xml").append(CRLF).flush();

			// Send xml file.
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"xml\"; filename=\"perun-pwd-manager.xml\"").append(CRLF);
			writer.append("Content-Type: text/xml; charset=" + StandardCharsets.UTF_8).append(CRLF); // Text file itself must be saved in this charset!
			writer.append(CRLF).flush();
			writer.append(dataToPass);
			output.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

			// End of multipart/form-data.
			writer.append("--" + boundary + "--").append(CRLF).flush();
		}

		int responseCode = con.getResponseCode();
		if (responseCode == 200) {
			return con.getInputStream();
		}

		throw new InternalErrorException("Wrong response code while opening connection on uri '" + uri + "'. Response code: " + responseCode + ". Request ID: " + requestId);

	}

	/**
	 * Generate XML request body from passed parameters in order to generate account.
	 *
	 * @param parameters request parameters to pass
	 * @param requestID unique ID of a request
	 * @return XML request body
	 */
	private String getGenerateAccountRequest(Map<String, String> parameters, int requestID) {

		log.debug("Making request with ID: " + requestID + " to IS MU.");

		String params = "";
		if (parameters != null && !parameters.isEmpty()) {

			if (parameters.get("urn:perun:user:attribute-def:core:firstName") != null && !parameters.get("urn:perun:user:attribute-def:core:firstName").isEmpty())
				params = "<jmeno>" + parameters.get("urn:perun:user:attribute-def:core:firstName") + "</jmeno>\n";

			if (parameters.get("urn:perun:user:attribute-def:core:lastName") != null && !parameters.get("urn:perun:user:attribute-def:core:lastName").isEmpty())
				params = "<prijmeni>" + parameters.get("urn:perun:user:attribute-def:core:lastName") + "</prijmeni>\n";

			if (parameters.get("urn:perun:user:attribute-def:core:titleBefore") != null && !parameters.get("urn:perun:user:attribute-def:core:titleBefore").isEmpty())
				params = "<titul_pred>" + parameters.get("urn:perun:user:attribute-def:core:titleBefore") + "</titul_pred>\n";

			if (parameters.get("urn:perun:user:attribute-def:core:titleAfter") != null && !parameters.get("urn:perun:user:attribute-def:core:titleAfter").isEmpty())
				params = "<titul_za>" + parameters.get("urn:perun:user:attribute-def:core:titleAfter") + "</titul_za>\n";

			if (parameters.get("urn:perun:user:attribute-def:def:birthDay") != null && !parameters.get("urn:perun:user:attribute-def:def:birthDay").isEmpty())
				params = "<datum_narozeni>" + parameters.get("urn:perun:user:attribute-def:def:birthDay") + "</datum_narozeni>\n";

			if (parameters.get("urn:perun:user:attribute-def:def:rc") != null && !parameters.get("urn:perun:user:attribute-def:def:rc").isEmpty())
				params = "<rodne_cislo>" + parameters.get("urn:perun:user:attribute-def:def:rc") + "</rodne_cislo>\n";

			if (parameters.get("urn:perun:member:attribute-def:def:mail") != null && !parameters.get("urn:perun:member:attribute-def:def:mail").isEmpty())
				params = "<email>" + parameters.get("urn:perun:member:attribute-def:def:mail") + "</email>\n";

			if (parameters.get("password") != null && !parameters.get("password").isEmpty())
				params = "<heslo>" + parameters.get("password") + "</heslo>\n";

		}

		return	"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<request>\n" +
				"<osoba reqid=\"" + requestID + "\">\n" +
				"<uco></uco>\n" +
				params +
				"<operace>INS</operace>\n" +
				"</osoba>\n" +
				"</request>";

	}

	/**
	 * Parse XML body response from InputStream and convert it to map of parameters.
	 *
	 * @param inputStream XML response to be parsed
	 * @param requestID unique ID of a request
	 * @return Map of response params
	 * @throws InternalErrorException
	 */
	private Map<String, String> parseResponse(InputStream inputStream, int requestID) throws InternalErrorException {

		Map<String, String> result = new HashMap<>();

		//Create new document factory builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new InternalErrorException("Error when creating newDocumentBuilder. Request ID: " + requestID, ex);
		}

		Document doc;
		try {
			doc = builder.parse(inputStream);
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
		XPathExpression ucoExpr;
		try {
			isErrorExpr = xpath.compile("/resp/stav/text()");
			getErrorTextExpr = xpath.compile("/resp/error/text()");
			ucoExpr = xpath.compile("/resp/uco/text()");
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

		if ("OK".equals(responseStatus)) {

			try {

				String uco = (String) ucoExpr.evaluate(doc, XPathConstants.STRING);
				result.put("urn:perun:user:attribute-def:def:login-namespace:mu", uco);

			} catch (XPathExpressionException ex) {
				throw new InternalErrorException("Error when evaluate xpath query on resulting document for request ID: " + requestID, ex);
			}

		} else {

			try {
				String error = (String) getErrorTextExpr.evaluate(doc, XPathConstants.STRING);
				throw new InternalErrorException("IS MU (password manager backend) responded with error to a Request ID: " + requestID + " Error: "+ error);
			} catch (XPathExpressionException ex) {
				throw new InternalErrorException("Error when evaluate xpath query on document to resolve error status. Request ID: " + requestID, ex);
			}

		}

		return result;

	}


}
