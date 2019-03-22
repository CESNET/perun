/**
 *
 */
package cz.metacentrum.perun.core.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;

/**
 * @author Michal Prochazka michalp@ics.muni.cz
 */
public class ExtSourceISMU extends ExtSource implements ExtSourceSimpleApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceISMU.class);

	private static PerunBlImpl perunBl;

	// filled by spring (perun-core.xml)
	public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
		perunBl = perun;
		return perun;
	}

	@Override
	public List<Map<String,String>> findSubjectsLogins(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException {
		// Get the url query for the group subjects
		String queryForGroup = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

		return this.querySource(queryForGroup, null, 0);
	}

	protected List<Map<String,String>> querySource(String query, String searchString, int maxResults) throws InternalErrorException {

		// Get the URL, if query was provided it has precedence over url attribute defined in extSource
		String url = null;
		if (query != null && !query.isEmpty()) {
			url = query;
		} else if (getAttributes().get("url") != null) {
			url = getAttributes().get("url");
		} else {
			throw new InternalErrorException("url attribute or query is required");
		}

		log.debug("Searching in external source url:'{}'", url);

		// If there is a search string, replace all occurences of the * with the searchstring
		if (searchString != null && searchString != "") {
			url.replaceAll("\\*", searchString);
		}

		try {
			URL u = new URL(url);

			// Check supported protocols
			HttpURLConnection http = null;
			if (u.getProtocol().equals("https")) {
				http = (HttpsURLConnection)u.openConnection();
			} else if (u.getProtocol().equals("http")) {
				http = (HttpURLConnection)u.openConnection();
			} else {
				throw new InternalErrorException("Protocol " + u.getProtocol() + " is not supported by this extSource.");
			}

			// Prepare the basic auth, if the username and password was specified
			if (getAttributes().get("user") != null && getAttributes().get("password") != null) {
				String val = getAttributes().get("user") + ":" + getAttributes().get("password");

				Base64 encoder = new Base64();
				String base64Encoded = new String(encoder.encode(val.getBytes()));
				// Java bug : http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6459815
				base64Encoded = base64Encoded.trim();
				String authorizationString = "Basic " + base64Encoded;
				http.setRequestProperty("Authorization", authorizationString);
			}

			http.setAllowUserInteraction(false);
			http.setRequestMethod("GET");
			http.connect();

			InputStream is = http.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;

			List<Map<String, String>> subjects = new ArrayList<>();

			while ((line = reader.readLine()) != null) {
				Map<String, String> map = new HashMap<>();

				// Each line looks like:
				// UCO  ;;          ;"title before. title before. firstName lastName, title after
				// 39700;;“RNDr. Michal Procházka";Procházka;Michal;

				// Parse the line
				String[] entries = line.split(";");
				// Get the UCO
				if (entries[0].equals("")) {
					// skip this subject, because it doesn't have UCO defined
					continue;
				}
				String login = entries[0];
				if (login.isEmpty()) login = null;
				map.put("login", login);

				String name = entries[2];
				// Remove "" from name
				name.replaceAll("^\"|\"$", "");
				// entries[3] contains name of the user, so parse it to get titleBefore, firstName, lastName and titleAfter in separate fields
				map.putAll(Utils.parseCommonName(name));

				// Add additional userExtSource for MU IdP with loa 2
				map.put(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING + "1",
						"https://idp2.ics.muni.cz/idp/shibboleth|cz.metacentrum.perun.core.impl.ExtSourceIdp|" + login + "@muni.cz|2");

				subjects.add(map);
			}

			return subjects;
		} catch (Exception e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void close() throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException("Using this method is not supported for ISMU");
	}

	@Override
	public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	protected Map<String,String> getAttributes() throws InternalErrorException {
		return perunBl.getExtSourcesManagerBl().getAttributes(this);
	}
}
