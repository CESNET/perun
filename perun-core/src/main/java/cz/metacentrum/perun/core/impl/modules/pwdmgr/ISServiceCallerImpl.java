package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISResponseData;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.ISServiceCaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ISServiceCallerImpl implements ISServiceCaller {

  private final static Logger log = LoggerFactory.getLogger(ISServiceCallerImpl.class);
  private final static String CRLF = "\r\n"; // Line separator required by multipart/form-data.

  private static volatile ISServiceCallerImpl instance;

  public static ISServiceCallerImpl getInstance() {
    if (instance == null) {
      instance = new ISServiceCallerImpl();
    }
    return instance;
  }

  /**
   * Based on tests from: http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
   * Most quicker and native InputStream reading method.
   *
   * @param inputStream Input stream to convert
   * @param encoding    encoding used to parse input stream
   * @return Content of inputStream as a String
   * @throws IOException
   */
  static String convertStreamToString(InputStream inputStream, Charset encoding) throws IOException {

    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    return result.toString(encoding);

  }

  @Override
  public ISResponseData call(String requestBody, int requestId) throws IOException {
    InputStream responseInputStream = makeCall(requestBody, requestId);
    return parseResponse(responseInputStream, requestId);
  }

  /**
   * Makes secure SSL connection to IS MU and perform required password manager action
   *
   * @param dataToPass XML request body
   * @param requestId  unique ID of a request
   * @return InputStream response to be parsed
   * @throws InternalErrorException
   * @throws IOException
   */
  public InputStream makeCall(String dataToPass, int requestId) throws IOException {

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
    String boundary =
        Long.toHexString(System.currentTimeMillis()); //random number for purpose of creating boundaries in multipart

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
      logBuilder.append("Content-Disposition: form-data; name=\"xml\"; filename=\"perun-pwd-manager.xml\"")
          .append(CRLF);
      writer.append("Content-Type: text/xml; charset=" + StandardCharsets.UTF_8)
          .append(CRLF); // Text file itself must be saved in this charset!
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
        response = convertStreamToString(con.getErrorStream(), StandardCharsets.UTF_8);
      } catch (IOException ex) {
        log.error("Unable to convert InputStream to String.", ex);
      }

      log.trace("[IS Request {}] Response: {}", requestId, response);

    }

    throw new InternalErrorException(
        "Wrong response code while opening connection on uri '" + uri + "'. Response code: " + responseCode +
            ". Request ID: " + requestId);
  }

  /**
   * Parse XML response from IS MU to XML document.
   *
   * @param inputStream Stream to be parsed to Document
   * @param requestID   ID of request made to IS MU.
   * @return XML document for further processing
   * @throws InternalErrorException
   */
  public ISResponseData parseResponse(InputStream inputStream, int requestID) {

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
      response = convertStreamToString(inputStream, StandardCharsets.UTF_8);
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
      throw new InternalErrorException(
          "Problem with parsing is more complex, not only invalid characters. Request ID: " + requestID, ex);
    } catch (IOException ex) {
      throw new InternalErrorException(
          "Error when parsing uri by document builder. Problem with input or output. Request ID: " + requestID, ex);
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
      throw new InternalErrorException(
          "Error when evaluate xpath query on document to resolve response status. Request ID: " + requestID, ex);
    }

    log.trace("[IS Request {}] Response of request from IS MU has status: {}", requestID, responseStatus);
    log.debug("[IS Request {}] Response of request from IS MU has status: {}", requestID, responseStatus);

    ISResponseData responseData = new ISResponseData();
    responseData.setStatus(responseStatus);
    responseData.setResponse(doc);

    if (!IS_OK_STATUS.equals(responseStatus)) {
      try {
        String error = (String) getErrorTextExpr.evaluate(doc, XPathConstants.STRING);
        if (error == null || error.isEmpty()) {
          error = (String) getDbErrorTextExpr.evaluate(doc, XPathConstants.STRING);
        }
        responseData.setError(error.trim());
      } catch (XPathExpressionException ex) {
        throw new InternalErrorException(
            "Error when evaluate xpath query on document to resolve error status. Request ID: " + requestID, ex);
      }
    }

    return responseData;
  }
}
