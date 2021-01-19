package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.rpc.deserializer.JsonDeserializer;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ExtSource for synchronization from another Perun instance
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class ExtSourcePerun extends ExtSource implements ExtSourceApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourcePerun.class);

	private static final String format = "json";
	private String perunUrl;
	private String username;
	private String password;
	// this will allow us to keep session to other Perun instances on all subsequent synchronization calls
	private static CookieStore cookieStore = new BasicCookieStore();

	private String extSourceNameForLogin = null;
	public static final Pattern attributePattern = Pattern.compile("[{](.+)[}]");

	private static PerunBlImpl perunBl;

	// filled by spring (perun-core.xml)
	public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
		perunBl = perun;
		return perun;
	}


	@Override
	public List<Map<String,String>> findSubjectsLogins(String searchString) throws ExtSourceUnsupportedOperationException {
		return findSubjectsLogins(searchString, 0);
	}

	@Override
	public List<Map<String,String>> findSubjectsLogins(String searchString, int maxResulsts) throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException("For Perun ExtSource is not supported to use this method. Use findSubjects instead.");
	}

	@Override
	public List<Map<String,String>> findSubjects(String searchString) {
		return findSubjects(searchString, 0);
	}

	@Override
	public List<Map<String,String>> findSubjects(String searchString, int maxResults) {
		setEnviroment();
		List<RichUser> richUsers = findRichUsers(searchString);
		if(maxResults != 0) {
			if(richUsers.size() > maxResults) {
				richUsers = richUsers.subList(0, maxResults);
			}
		}

		List<Map<String,String>> subjects = convertRichUsersToListOfSubjects(richUsers);
		return subjects;
	}

	@Override
	public Map<String, String> getSubjectByLogin(String login) throws SubjectNotExistsException {
		setEnviroment();
		Map<String,String> subject = covertRichUserToSubject(findRichUser(login));
		return subject;
	}

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) {
		setEnviroment();
		// Get the query for the group subjects
		String queryForGroup = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

		//If there is no query for group, throw exception
		if(queryForGroup == null) throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " can't be null.");

		Integer groupId = Integer.valueOf(queryForGroup);

		List<Map<String,String>> subjectsFromGroup = convertRichUsersToListOfSubjects(findRichUsers(groupId));

		return subjectsFromGroup;
	}

	@Override
	public List<Map<String, String>> getUsersSubjects() {
		String query = getAttributes().get(UsersManager.USERS_QUERY);

		setEnviroment();

		return convertRichUsersToListOfSubjects(findRichUsers(query));
	}

	private List<Map<String, String>> convertRichUsersToListOfSubjects(List<RichUser> richUsers) {
		List<Map<String, String>> listOfSubjects = new ArrayList<>();
		for(RichUser ru: richUsers) {
			listOfSubjects.add(covertRichUserToSubject(ru));
		}
		return listOfSubjects;
	}

	private Map<String, String> covertRichUserToSubject(RichUser richUser) {
		Map<String,String> richUserInMap = new HashMap<>();
		String mapping = getAttributes().get("xmlMapping");
		String[] mappingArray = mapping.split(",\n");

		//Get user login
		String login = "";
		for(UserExtSource ues: richUser.getUserExtSources()) {
			if(ues.getExtSource() != null && ues.getExtSource().getName().equals(extSourceNameForLogin)) {
				login = ues.getLogin();
				break;
			}
		}

		//Null login is not allowed there
		if(login == null) throw new InternalErrorException("There is missing login for user " + richUser + " and extSource " + extSourceNameForLogin);

		for (String s : mappingArray) {
			String attr = s.trim();
			int index = attr.indexOf("=");

			if (index <= 0)
				throw new InternalErrorException("There is no text in xmlMapping attribute or there is no '=' character.");
			String name = attr.substring(0, index);
			String value = attr.substring(index + 1);

			Matcher attributeMatcher = attributePattern.matcher(value);
			//Try to find perun attributes in value part
			if (attributeMatcher.find()) {
				if (attributeMatcher.group(1).equals("login")) {
					value = attributeMatcher.replaceFirst(login);
				} else {
					String replacement = lookingForValueInRichUserAttributes(attributeMatcher.group(1), richUser);
					if (replacement == null) replacement = "";
					value = attributeMatcher.replaceFirst(replacement);
					//If whole value is empty because of replacement, it means null for us
					if (value.isEmpty()) value = null;
				}
			} else if (value.startsWith("urn:perun:")) {
				//DEPRECATED, but need to be first removed from all settings of PerunExtSource in perun-extSource.xml file
				//It is probably old way how to use attribute (without {}) so try to find value for it
				value = lookingForValueInRichUserAttributes(value, richUser);
			}
			//If nothing found, let the value be the same, it is probably static value (without any attribute)

			richUserInMap.put(name.trim(), value);
		}

		return richUserInMap;
	}

	private String lookingForValueInRichUserAttributes(String value, RichUser richUser) {
		String returnedValue = null;
		List<Attribute> attributes = richUser.getUserAttributes();
		for(Attribute attr: attributes) {
			if(attr.getName().equals(value)) {
				returnedValue = BeansUtils.attributeValueToString(attr);
				break;
			}
		}
		return returnedValue;
	}

	private RichUser findRichUser(String login) throws SubjectNotExistsException {
		Map<String, Object> params = new HashMap<>();

		List<RichUser> richUsers = this.findRichUsers(login);

		List<RichUser> matchesRichUsers = new ArrayList<>();
		for(RichUser richUser: richUsers) {
			List<UserExtSource> userExtSources = richUser.getUserExtSources();
			for(UserExtSource userExtSource: userExtSources) {
				if(extSourceNameForLogin.equals(userExtSource.getExtSource().getName())) {
					if(login.equals(userExtSource.getLogin())) matchesRichUsers.add(richUser);
				}
			}
		}

		if(matchesRichUsers.isEmpty()) throw new SubjectNotExistsException("There is no subject with login " + login + " in extSource " + extSourceNameForLogin + " in System perun with RPC url: " + perunUrl);
		if(matchesRichUsers.size() > 1) throw new InternalErrorException("There are more then one subject with login " + login + " in extSource " + extSourceNameForLogin + " in System perun with RPC url: " + perunUrl);

		return richUsers.get(0);
	}

	private List<RichUser> findRichUsers(String substring) {

		String query;
		// encode query params
		query = "searchString=" + URLEncoder.encode(substring, StandardCharsets.UTF_8);

		List<RichUser> richUsers;
		try {
			richUsers = this.call("usersManager", "findRichUsers", query).readList(RichUser.class);
		} catch (PerunException ex) {
			throw new InternalErrorException(ex);
		}

		Iterator<RichUser> iterator = richUsers.iterator();
		while(iterator.hasNext()) {
			RichUser richUser = iterator.next();
			boolean hasLogin = false;
			for(UserExtSource ues: richUser.getUserExtSources()) {
				if(ues.getExtSource() != null && ues.getExtSource().getName().equals(extSourceNameForLogin)) {
					hasLogin = true;
					continue;
				}
			}
			if(!hasLogin) iterator.remove();
		}

		return richUsers;
	}

	private List<RichUser> findRichUsers(Integer groupId) {
		// we don't need to encode query params here, no unsafe char in fixed string
		String query = "group=" + groupId + "&" + "allowedStatuses[]=" + "VALID";

		List<RichMember> richMembers;
		try {
			richMembers = this.call("membersManager", "getRichMembersWithAttributes", query).readList(RichMember.class);
		} catch (PerunException ex) {
			throw new InternalErrorException(ex);
		}

		return convertListOfRichMembersToListOfRichUsers(richMembers);
	}

	private void setEnviroment() {
		perunUrl = getAttributes().get("perunUrl");
		username = getAttributes().get("username");
		password = getAttributes().get("password");
		extSourceNameForLogin = getAttributes().get("extSourceNameForLogin");
		BeansUtils.notNull(perunUrl, "perunUrl");
		BeansUtils.notNull(username, "username");
		BeansUtils.notNull(password, "password");
		BeansUtils.notNull(extSourceNameForLogin, "extSourceNameForLogin");
	}

	private List<RichUser> convertListOfRichMembersToListOfRichUsers(List<RichMember> richMembers) {
		List<RichUser> richUsers = new ArrayList<>();
		if(richMembers == null || richMembers.isEmpty()) return richUsers;

		for(RichMember rm: richMembers) {
			RichUser ru = new RichUser(rm.getUser(), rm.getUserExtSources(), rm.getUserAttributes());
			richUsers.add(ru);
		}

		return richUsers;
	}

	private Deserializer call(String managerName, String methodName) throws PerunException {
		return this.call(managerName, methodName, null);
	}

	protected Deserializer call(String managerName, String methodName, String query) throws PerunException {
		//Prepare sending message
		HttpResponse response;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		httpClientBuilder.setDefaultCookieStore(cookieStore);
		HttpClient httpClient = httpClientBuilder.build();

		String commandUrl = perunUrl + format + "/" + managerName + "/" + methodName;
		if(query != null) commandUrl+= "?" + query;

		HttpGet get = new HttpGet(commandUrl);
		get.setHeader("Content-Type", "application/json");
		get.setHeader("charset", StandardCharsets.UTF_8.toString());
		get.setHeader("Connection", "Close");
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

		get.addHeader(BasicScheme.authenticate(credentials, StandardCharsets.UTF_8.toString(), false));
		//post.setParams(params);

		InputStream rpcServerAnswer = null;

		try {
			 response = httpClient.execute(get);
			 rpcServerAnswer = response.getEntity().getContent();
		} catch(IOException ex) {
			this.processIOException(ex);
		}

		JsonDeserializer des = null;
		try {
			des = new JsonDeserializer(rpcServerAnswer);
		} catch (IOException ex) {
			this.processIOException(ex);
		}

		return des;
	}

	private void processIOException(Throwable e) {
		this.processIOException(null, e);
	}

	private void processIOException(HttpURLConnection con, Throwable e) {
		// Process known IOExceptions
		if (e instanceof ProtocolException) {
			throw new RpcException(RpcException.Type.COMMUNICATION_ERROR_WITH_PERUN_RPC_SERVER, "Communication problem with Perun server on URL: " + perunUrl, e);
		} else if (e instanceof UnknownHostException) {
			throw new RpcException(RpcException.Type.UNKNOWN_PERUN_RPC_SERVER, "Perun server cannot be contacted on URL: " + perunUrl, e);
		}

		// If the connection has been provided, check the responseCode
		if (con != null) {
			// Check return code
			int responseCode;
			try {
				responseCode = con.getResponseCode();

				if (responseCode != HttpURLConnection.HTTP_OK) {
					throw new RpcException(RpcException.Type.PERUN_RPC_SERVER_ERROR_HTTP_CODE, "Perun server on URL: " + perunUrl + " returned HTTP code: " + responseCode, e);
				}
			} catch (IOException e1) {
				throw new RpcException(RpcException.Type.UNKNOWN_EXCEPTION, "Failed to contact Perun server on URL: " + perunUrl, e1);
			}
		}

		throw new RpcException(RpcException.Type.UNKNOWN_EXCEPTION, "Failed to contact Perun server on URL: " + perunUrl, e);
	}

	@Override
	public void close() {
		//not needed there
	}

	@Override
	public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes) throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	protected Map<String,String> getAttributes() {
		return perunBl.getExtSourcesManagerBl().getAttributes(this);
	}
}
