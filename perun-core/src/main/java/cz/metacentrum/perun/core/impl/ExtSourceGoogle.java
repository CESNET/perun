package cz.metacentrum.perun.core.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ext source implementation for Google Groups.
 *
 * @author Sona Mastrakova
 * @date 05.08.2015
 */
public class ExtSourceGoogle extends ExtSource implements ExtSourceApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceGoogle.class);

	private String query = null;
	private Directory service = null;
	private String domainName = null;
	private String groupName = null;

	private static PerunBlImpl perunBl;

	/**
	 * Application name.
	 */
	private static final String APPLICATION_NAME = "External Source for Perun";

	/**
	 * Global instance of the JSON factory.
	 */
	private static JsonFactory JSON_FACTORY;

	/**
	 * Global instance of the HTTP transport.
	 */
	private static HttpTransport HTTP_TRANSPORT;

	/**
	 * Global instance of the scopes required by this application.
	 */
	private static List<String> SCOPES;

	/**
	 * Email of the Service Account.
	 */
	private static String SERVICE_ACCOUNT_EMAIL;

	/**
	 * Email of the User that Application will work behalf on.
	 */
	private static String USER_EMAIL;

	/**
	 * Path to the Service Account's Private Key file.
	 */
	private static String SERVICE_ACCOUNT_PKCS12_FILE_PATH;

	@Override
	public List<Map<String, String>> findSubjectsLogins(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException("For Google Groups using this method is not optimized, use findSubjects instead.");
	}

	@Override
	public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException("For Google Groups using this method is not optimized, use findSubjects instead.");
	}

	@Override
	public List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		return findSubjects(searchString, 0);
	}

	@Override
	public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		try {
			query = getAttributes().get("query");
			domainName = getAttributes().get("domain");
			groupName = getAttributes().get("group");

			if (query == null || query.isEmpty()) {
				throw new InternalErrorException("query attribute is required");
			}

			if (domainName == null || domainName.isEmpty()) {
				throw new InternalErrorException("domainName attribute is required");
			}

			if (groupName == null || groupName.isEmpty()) {
				throw new InternalErrorException("groupName attribute is required");
			}

			if (searchString == null) {
				throw new InternalErrorException("search string can't be null");
			}

			//Replace '?' by searchString
			query = query.replaceAll("\\?", searchString);

			//Get connection to Google Groups
			prepareEnvironment();
			List<Map<String, String>> subjects = new ArrayList<>();
			querySource(query, maxResults, subjects);
			return subjects;

		} catch (IOException ex) {
			log.error("IOException in findSubjects() method while parsing data from Google Groups", ex);
		} catch (GeneralSecurityException ex) {
			log.error("GeneralSecurityException while trying to connect to Google Apps account", ex);
		}

		return null;
	}

	@Override
	public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException, ExtSourceUnsupportedOperationException {
		try {
			query = getAttributes().get("loginQuery");
			domainName = getAttributes().get("domain");
			groupName = getAttributes().get("group");

			if (query == null || query.isEmpty()) {
				throw new InternalErrorException("loginQuery attribute is required");
			}

			if (domainName == null || domainName.isEmpty()) {
				throw new InternalErrorException("domainName attribute is required");
			}

			if (groupName == null || groupName.isEmpty()) {
				throw new InternalErrorException("groupName attribute is required");
			}

			if (login == null || login.isEmpty()) {
				throw new InternalErrorException("login string can't be null or empty");
			}

			//Replace '?' by searchString
			query = query.replaceAll("\\?", login);

			//Get connection to Google Groups
			prepareEnvironment();
			List<Map<String, String>> subjects = new ArrayList<>();
			querySource(query, 0, subjects);

			if (subjects.isEmpty()) {
				throw new SubjectNotExistsException("Login: " + login);
			}
			if (subjects.size() > 1) {
				throw new InternalErrorException("External source must return exactly one result, search string: " + login);
			}

			return subjects.get(0);

		} catch (IOException ex) {
			log.error("IOException in getSubjectByLogin() method while parsing data from Google Groups", ex);
		} catch (GeneralSecurityException ex) {
			log.error("GeneralSecurityException while trying to connect to Google Apps account", ex);
		}

		return null;
	}

	@Override
	public String getGroupSubjects(PerunSession sess, Group group, String status, List<Map<String, String>> subjects) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		try {
			// Get the query for the group subjects
			Attribute queryForGroupAttribute = null;
			try {
				queryForGroupAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
			} catch (WrongAttributeAssignmentException e) {
				// Should not happen
				throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSEXTSOURCE_ATTRNAME + " is not from group namespace.");
			} catch (AttributeNotExistsException e) {
				throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " must exists.");
			}

			// Get the query for the group subjects
			String queryForGroup = BeansUtils.attributeValueToString(queryForGroupAttribute);

			//If there is no query for group, throw exception
			if (queryForGroup == null) {
				throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " can't be null.");
			}

			domainName = getAttributes().get("domain");
			groupName = getAttributes().get("group");

			if (domainName == null || domainName.isEmpty()) {
				throw new InternalErrorException("domainName attribute is required");
			}

			if (groupName == null || groupName.isEmpty()) {
				throw new InternalErrorException("groupName attribute is required");
			}

			//Get connection to Google Groups
			prepareEnvironment();

			querySource(queryForGroup, 0, subjects);

		} catch (IOException ex) {
			log.error("IOException in getGroupSubjects() method while parsing data from Google Groups", ex);
		} catch (GeneralSecurityException ex) {
			log.error("GeneralSecurityException while trying to connect to Google Apps account", ex);
		}
		return GroupsManager.GROUP_SYNC_STATUS_FULL;
	}

	@Override
	public void close() throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException("For Google Groups, using this method is not optimized, use findSubjects instead.");
	}

	/**
	 * Prepares class variables to be able to connect to Google Groups via API.
	 *
	 * @throws InternalErrorException
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private void prepareEnvironment() throws InternalErrorException, IOException, GeneralSecurityException {
		ExtSourceGoogle.SERVICE_ACCOUNT_EMAIL = getAttributes().get("serviceAccountEmail");
		ExtSourceGoogle.USER_EMAIL = getAttributes().get("userEmail");
		ExtSourceGoogle.SERVICE_ACCOUNT_PKCS12_FILE_PATH = getAttributes().get("p12File");
		ExtSourceGoogle.JSON_FACTORY = JacksonFactory.getDefaultInstance();
		ExtSourceGoogle.HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		ExtSourceGoogle.SCOPES = Arrays.asList(DirectoryScopes.ADMIN_DIRECTORY_USER,
				DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY,
				DirectoryScopes.ADMIN_DIRECTORY_GROUP,
				DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER);

		this.service = this.getDirectoryService();

		if (!domainName.equals(this.getDomainName())) {
			throw new IllegalArgumentException("Domain name in extSources.xml can't be different from user's email domain in google_groups.properties.");
		}
	}

	/**
	 * Setting values for attributes in 'googleMapping' attribute from
	 * /etc/perun/perun-extSources.xml file.
	 *
	 * It's possible to fill these attributes for Google Groups ExtSource: -
	 * urn:perun:user:attribute-def:virt:logins-namespace:google={userID}, -
	 * urn:perun:facility:attribute-def:def:googleGroupNameNamespace={domainName},
	 * -
	 * urn:perun:group:attribute-def:def:googleGroupName-namespace:einfra.cesnet.cz={groupName}
	 *
	 * All other attributes have to be filled in directly in XML file (without
	 * curly brackets).
	 *
	 * @param id member's ID
	 * @return Map<String, String>, like <name,value>
	 * @throws InternalErrorException
	 */
	private Map<String, String> processGoogleMappingAttribute(String id) throws InternalErrorException {
		Map<String, String> map = new HashMap<>();

		String mapping = getAttributes().get("googleMapping");
		String[] mappingArray = mapping.split(",\n");
		for (String mappingElement : mappingArray) {

			String attr = mappingElement.trim();
			int index = attr.indexOf("=");

			if (index <= 0) {
				throw new InternalErrorException("There is no text in googleMapping attribute or there is no '=' character.");
			}

			String name = attr.substring(0, index);
			String value = attr.substring(index + 1);

			if (value.startsWith("{")) {

				// exclude curly brackets from value
				value = value.substring(1, value.length() - 1);
				switch (value) {
					case "userID":
						value = id;
						map.put(name.trim(), value.trim());
						break;
					case "domainName":
						value = this.domainName;
						map.put(name.trim(), value.trim());
						break;
					case "groupName":
						value = this.groupName;
						map.put(name.trim(), value.trim());
						break;
					default:
						throw new IllegalArgumentException("Value in curly brackets in googleMapping attribute can be word 'userID' or 'domainName' or 'groupName', nothing else.");
				}
			} else {
				map.put(name.trim(), value.trim());
				break;
			}
		}

		return map;
	}

	/**
	 * Searching for member with ID specified in 'value' variable in group
	 * specified by groupName class variable.
	 *
	 * When we reach that concrete member, we send his ID to
	 * processGoogleMappingAttribute() method to create map from suitable
	 * attributes. All these maps for each user (until maxResults number is
	 * reached) are stored together in list, that is returned by method.
	 *
	 * @param value member's ID
	 * @param maxResults max number of results for this query that should be
	 * returned by this method
	 * @return List<Map<String, String>>, list of maps returned by
	 * processGoogleMappingAttribute() method
	 * @throws InternalErrorException
	 */
	private void executeQueryTypeID(String value, int maxResults, List<Map<String, String>> subjects) throws InternalErrorException {
		try {
			Members result = service.members().list(groupName).execute();
			List<Member> membersInGroup = result.getMembers();

			for (Member member : membersInGroup) {
				Map<String, String> map = new HashMap<>();

				if (member.getId().equals(value)) {
					map = processGoogleMappingAttribute(member.getId());
				}

				if (!map.isEmpty()) {
					subjects.add(map);
				}

				if (maxResults > 0) {
					if (subjects.size() >= maxResults) {
						break;
					}
				}
			}
		} catch (IOException ex) {
			log.error("Problem with I/O operation while accesing Google Group API in ExtSourceGoogle class.", ex);
		}
	}

	/**
	 * Searching for member with email address specified in 'value' variable in
	 * group specified by groupName class variable.
	 *
	 * When we find suitable member, we send his ID to
	 * processGoogleMappingAttribute() method to create map from suitable
	 * attributes. All these maps for each user (until maxResults number is
	 * reached) are stored together in list, that is returned by method.
	 *
	 * @param value member's ID
	 * @param maxResults max number of results for this query that should be
	 * returned by this method
	 * @return List<Map<String, String>>, list of maps returned by
	 * processGoogleMappingAttribute() method
	 * @throws InternalErrorException
	 */
	private void executeQueryTypeEmail(String value, int maxResults, List<Map<String, String>> subjects) throws InternalErrorException {
		try {
			Members result = service.members().list(groupName).execute();
			List<Member> membersInGroup = result.getMembers();

			for (Member member : membersInGroup) {
				Map<String, String> map = new HashMap<>();

				if (member.getEmail().equals(value)) {
					map = processGoogleMappingAttribute(member.getId());
				}

				if (!map.isEmpty()) {
					subjects.add(map);
				}

				if (maxResults > 0) {
					if (subjects.size() >= maxResults) {
						break;
					}
				}
			}
		} catch (IOException ex) {
			log.error("Problem with I/O operation while accesing Google Group API in ExtSourceGoogle class.", ex);
		}
	}

	/**
	 * Searching for members in group specified by 'value' variable.
	 *
	 * When we find these members, we their ID's one by one to
	 * processGoogleMappingAttribute() method to create map from suitable
	 * attributes. All these maps for each user (until maxResults number is
	 * reached) are stored together in list, that is returned by method.
	 *
	 * @param value group email address, like groupname@domain.com, NOT only
	 * groupname
	 * @param maxResults max number of results for this query that should be
	 * returned by this method
	 * @param subjects
	 * @throws InternalErrorException
	 */
	private void executeQueryTypeGroupSubjects(String value, int maxResults, List<Map<String, String>> subjects) throws InternalErrorException {
		if (value.contains("@" + this.domainName)) {
			try {
				Members result = service.members().list(value).execute();
				List<Member> membersInGroup = result.getMembers();

				for (Member member : membersInGroup) {
					Map<String, String> map;
					map = processGoogleMappingAttribute(member.getId());

					if (!map.isEmpty()) {
						subjects.add(map);
					}

					if (maxResults > 0) {
						if (subjects.size() >= maxResults) {
							break;
						}
					}
				}
			} catch (IOException ex) {
				log.error("Problem with I/O operation while accesing Google Group API in ExtSourceGoogle class.", ex);
			}
		} else {
			throw new IllegalArgumentException("You are trying to get users from nonexistin group, please check name if your group name is something like 'groupname@domainname.com'.");
		}
	}

	/**
	 * Searching for members in group specified by groupName class variable by
	 * an email address. Email address doesn't have to be full, we are searching
	 * for substrings, not exact match.
	 *
	 * When we find suitable members, we send their ID's one by one to
	 * processGoogleMappingAttribute() method to create map from suitable
	 * attributes. All these maps for each user (until maxResults number is
	 * reached) are stored together in list, that is returned by method.
	 *
	 * @param value part of email address
	 * @param subjects list of maps of strings which contains pontentional members
	 * processGoogleMappingAttribute() method
	 * @throws InternalErrorException
	 */
	private void executeQueryTypeContains(String value, List<Map<String, String>> subjects) throws InternalErrorException {
		try {
			Members result = service.members().list(groupName).execute();
			List<Member> membersInGroup = result.getMembers();

			for (Member member : membersInGroup) {
				Map<String, String> map = new HashMap<>();

				if (member.getEmail().contains(value)) {
					map = processGoogleMappingAttribute(member.getId());
				}

				if (!map.isEmpty()) {
					subjects.add(map);
				}
			}
		} catch (IOException ex) {
			log.error("Problem with I/O operation while accesing Google Group API in ExtSourceGoogle class.", ex);
		}
	}

	/**
	 * Method differs 2 types of queries: - "exact queries" with '=' symbol -
	 * "containing queries" with keyword 'contains'
	 *
	 * It's possible to use "exact queries" for 'id' or 'email' and "containing
	 * queries" for 'email'.
	 *
	 * @param query query which differs other queries from each other, it has to
	 * contain either '=' symbol or 'contains' keyword
	 * @param maxResults max number of results for this query that should be
	 * returned by this method
	 * @return List<Map<String, String>>, list of maps returned by suitable
	 * executing method
	 * @throws InternalErrorException
	 */
	private void querySource(String query, int maxResults, List<Map<String, String>> subjects) throws InternalErrorException, FileNotFoundException, IOException {
		// Symbol '=' indicates getSubjectByLogin() or getGroupSubjects method
		int index = query.indexOf("=");
		// Word 'contains' indicates findSubjects() method
		int indexContains = query.indexOf("contains");

		if (index != -1) {
			String queryType = query.substring(0, index);
			String value = query.substring(index + 1);
			// find user according to name of query (queryType) and value in it
			// (after '=' symbol) and add him to map
			switch (queryType) {
				case "id":
					executeQueryTypeID(value, maxResults, subjects);
					break;

				case "email":
					executeQueryTypeEmail(value, maxResults, subjects);
					break;

				case "group":
					executeQueryTypeGroupSubjects(value, maxResults, subjects);
					break;

				default:
					throw new IllegalArgumentException("Word before '=' symbol can be 'id' or 'email' or 'group', nothing else.");
			}
		} else {
			if (indexContains != -1) {

				String queryType = query.substring(0, indexContains).trim();
				String value = query.substring(indexContains + "contains".trim().length());

				if (queryType.equals("email")) {
					executeQueryTypeContains(value.trim(), subjects);
				} else {
					throw new IllegalArgumentException("Search for substrings is possible only for 'email' attribute.");
				}
			} else {
				// if there's no symbol '=' or word 'contains' in the query
				throw new InternalErrorException("Wrong query!");
			}
		}
	}

	public String getDomainName() {
		return USER_EMAIL.substring(USER_EMAIL.indexOf("@") + 1);
	}

	/**
	 * Creates Credential object.
	 *
	 * @return an authorized Credential object.
	 */
	private static Credential authorize() {
		try {
			GoogleCredential credential = new GoogleCredential.Builder()
					.setTransport(HTTP_TRANSPORT)
					.setJsonFactory(JSON_FACTORY)
					.setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
					.setServiceAccountScopes(SCOPES)
					.setServiceAccountUser(USER_EMAIL)
					.setServiceAccountPrivateKeyFromP12File(
							new java.io.File(SERVICE_ACCOUNT_PKCS12_FILE_PATH))
					.build();

			return credential;
		} catch (IOException ex) {
			log.error("Problem with I/O operation while building GoogleCredential object in authorize() method.", ex);
		} catch (GeneralSecurityException ex) {
			log.error("Problem with security while building GoogleCredential object in authorize() method.", ex);
		}

		return null;
	}

	/**
	 * Build and returns a Directory service object authorized with the service
	 * accounts that act on behalf of the given user.
	 *
	 * @return Directory service object that is ready to make requests.
	 */
	public Directory getDirectoryService() {

		Directory serviceLocal = new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize())
				.setApplicationName(APPLICATION_NAME).build();

		return serviceLocal;
	}

	protected Map<String,String> getAttributes() throws InternalErrorException {
		return perunBl.getExtSourcesManagerBl().getAttributes(this);
	}

	// filled by spring (perun-core.xml)
	public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
		perunBl = perun;
		return perun;
	}
}
