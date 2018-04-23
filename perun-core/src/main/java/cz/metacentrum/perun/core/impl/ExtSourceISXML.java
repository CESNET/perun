package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XML extSource for groups in IS MU
 *
 * @author Michal Stava
 */
public class ExtSourceISXML extends ExtSourceXML {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceISXML.class);
	//add 1 to prevent value "0"
	private final int requestID = (new Random()).nextInt(1000000) + 1;
	private final String boundary = Long.toHexString(System.currentTimeMillis()); //random number for purpose of creating bounderies in multipart
	private final String CRLF = "\r\n"; // Line separator required by multipart/form-data.
	private String ismuquery = null;
	private String workplace = null;
	private String groupName = null;

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		// Get the query for the group
		String queryForGroup = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
		//If there is no query for group, throw exception
		if(queryForGroup == null || queryForGroup.isEmpty()) throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " can't be null.");
		//Expected value like "workplaceId:groupName" with ':' as separator
		if(!queryForGroup.contains(":")) throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " has to contain separator ':' between workplaceId and groupName.");

		//Parse workplace and groupName from queryForGroup
		String parsedQuery[] = queryForGroup.split(":");
		if(parsedQuery.length != 2) throw new InternalErrorException("Expected 2 values workplace and groupName but get " + parsedQuery.length);
		workplace = parsedQuery[0];
		groupName = parsedQuery[1];

		// Get the generic query for group subjects
		String query = getAttributes().get("query");
		//If there is no generic query for group subjects, throw exception
		if(query == null) throw new InternalErrorException("General query for ExtSourceISXML can't be null.");

		//Get file or uri of xml
		prepareEnvironment();

		return xpathParsing(query, 0);
	}

	@Override
	protected InputStream createTwoWaySSLConnection(String uri) throws IOException, InternalErrorException {
		//prepare sslFactory
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		HttpsURLConnection.setDefaultSSLSocketFactory(factory);

		URL myurl = new URL(uri);
		this.setCon((HttpURLConnection) myurl.openConnection());

		// Prepare the basic auth, if the username and password was specified
		if (getAttributes().get("user") != null && getAttributes().get("password") != null) {
			String val = (new StringBuffer(getAttributes().get("user")).append(":").append(getAttributes().get("password"))).toString();

			Base64 encoder = new Base64();
			String base64Encoded = new String(encoder.encode(val.getBytes()));
			base64Encoded = base64Encoded.trim();
			String authorizationString = "Basic " + base64Encoded;
			getCon().setRequestProperty("Authorization", authorizationString);
		}
		getCon().setAllowUserInteraction(false);

		//prepare query for IS MU page
		ismuquery = getQueryForGroup();

		//set request header if is required (set in extSource xml)
		this.getCon().setDoOutput(true);
		this.getCon().setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		try (
			OutputStream output = getCon().getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
		) {
			// Send param about return
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"out\"").append(CRLF);
			writer.append(CRLF).append("xml").append(CRLF).flush();

			// Send xml file.
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"xml\"; filename=\"perun.xml\"").append(CRLF);
			writer.append("Content-Type: text/xml; charset=" + StandardCharsets.UTF_8).append(CRLF); // Text file itself must be saved in this charset!
			writer.append(CRLF).flush();
			writer.append(ismuquery);
			output.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

			// End of multipart/form-data.
			writer.append("--" + boundary + "--").append(CRLF).flush();
		}

		//String text = "";
		int responseCode = this.getCon().getResponseCode();
		if(responseCode == 200) {
			InputStream is = this.getCon().getInputStream();
			return is;
		}

		throw new InternalErrorException("Wrong response code while opening connection on uri '" + uri + "'. Response code: " + responseCode);
	}

	/**
	 * Prepare request for getting group information.
	 *
	 * @return request with specific settings for group (by XML setting in perun-extSource.xml file)
	 */
	private String getQueryForGroup() throws InternalErrorException {
		log.debug("RequestID for ISXML ExtSource group " + workplace + ":" + groupName + " = " + requestID);

		return	"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<request>\n" +
				"<skupina_clenove reqid=\"" + requestID + "\">\n" +
				"<skupina_id></skupina_id>\n" +
				"<pracoviste_id>" + workplace + "</pracoviste_id>\n" +
				"<zkratka>" + groupName  + "</zkratka>\n" +
				"<operace>INF</operace>\n" +
				"</skupina_clenove>\n" +
				"</request>";
	}
}
