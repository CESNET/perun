package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import static cz.metacentrum.perun.core.impl.Utils.parseCommonName;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ext source implementation for VOOT protocol.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 12.07.2016
 */
public class ExtSourceVOOT extends ExtSource implements ExtSourceApi {

    private final static Logger log = LoggerFactory.getLogger(ExtSourceVOOT.class);

    private String uriMembership = null;
    private String uriMembers = null;
    private String query = null;
    private String username = null;
    private String password = null;
    private static PerunBlImpl perunBl;

    // filled by spring (perun-core.xml)
    public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
        perunBl = perun;
        return perun;
    }

    @Override
    public List<Map<String, String>> findSubjectsLogins(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException(
                "For VOOT using this method is not optimized, use findSubjects instead.");
    }

    @Override
    public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException(
                "For VOOT using this method is not optimized, use findSubjects instead.");
    }

    @Override
    public List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException {
        return findSubjects(searchString, 0);
    }

    @Override
    public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException {
        query = getAttributes().get("query");

        if (query == null || query.isEmpty()) {
            throw new InternalErrorException("query attribute is required");
        }

        //Replace '?' by searchString
        query = query.replaceAll("\\?", searchString);

        prepareEnvironment();

        try {
            return getUsersFromRemote(getGroupsFromRemote(), 0);
        } catch (IOException ex) {
            log.error("IOException in findSubjects() method while obtaining users"
                    + "from VOOT external source", ex);
        }

        return null;
    }

    @Override
    public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException {
        query = getAttributes().get("loginQuery");

        if (query == null || query.isEmpty()) {
            throw new InternalErrorException("loginQuery attribute is required");
        }

        if (login == null || login.isEmpty()) {
            throw new InternalErrorException("login string can't be null or empty");
        }

        //Replace '?' by searchString
        query = query.replaceAll("\\?", login);

        prepareEnvironment();

        try {
            List<Map<String, String>> subjects
                    = getUsersFromRemote(getGroupsFromRemote(), 0);

            if (subjects.isEmpty()) {
                throw new SubjectNotExistsException("Login: " + login);
            }
            if (subjects.size() > 1) {
                throw new InternalErrorException("External source must return"
                        + " exactly one result, search string: " + login);
            }

            return subjects.get(0);
        } catch (IOException ex) {
            log.error("IOException in getSubjectByLogin() method while obtaining"
                    + "user from VOOT external source", ex);
        }

        return null;
    }

    @Override
    public String getGroupSubjects(PerunSession sess, Group group, String status, List<Map<String, String>> subjects) throws InternalErrorException {
        try {
            Attribute queryForGroupAttribute = null;
            try {
                queryForGroupAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
            } catch (WrongAttributeAssignmentException e) {
                // Should not happen
                throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " is not from group namespace.");
            } catch (AttributeNotExistsException e) {
                throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " must exists.");
            }

            // Get the query for the group subjects
            String queryForGroup = BeansUtils.attributeValueToString(queryForGroupAttribute);

            if (queryForGroup == null) {
                throw new InternalErrorException("Attribute " + 
                        GroupsManager.GROUPMEMBERSQUERY_ATTRNAME +
                        " can't be null.");
            }

            prepareEnvironment();

            List<Map<String, String>> parsedResult = getUsersFromRemoteGroup(queryForGroup);

            for (Map map : parsedResult) {
                if (map != null) {
                    subjects.add(map);
                }
            }

        } catch (IOException ex) {
            log.error("IOException in getGroupSubjects() method while obtaining"
                    + "users from VOOT external source by group name", ex);
        }
        return GroupsManager.GROUP_SYNC_STATUS_FULL;
    }

    @Override
    public void close() throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException(
                "Using this method is not supported for VOOT");
    }

    private HttpURLConnection createConnection(String uri) throws IOException, InternalErrorException {
        HttpURLConnection connection;

        username = getAttributes().get("user");
        password = getAttributes().get("password");

        String userPassword = username + ":" + password;

        connection = (HttpURLConnection) new URL(uri).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", getBasicAuthenticationEncoding(userPassword));
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            return connection;
        } else {
            log.warn("Cannot establish connection to " + uri + ". Response code is " 
                    + responseCode + ". Error message is \'" + new BufferedReader(
                            new InputStreamReader(connection.getErrorStream())).readLine() + "\'.");
        }

        return null;
    }

    private String getBasicAuthenticationEncoding(String userCredentials) {
        return "Basic " + new String(Base64Coder.encode(userCredentials.getBytes()));
    }

    // use uriMembership attribute to obtain list of available groups
    private List<String> getGroupsFromRemote() throws IOException, InternalErrorException {
        List<String> groups = new ArrayList();

        HttpURLConnection connection = createConnection(uriMembership);
        InputStream is = null;
        if (connection != null) {
            is = connection.getInputStream();
        }
        if (is != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;

            try {
                while ((line = bufferedReader.readLine()) != null) {
                    JSONObject obj = new JSONObject(line);
                    JSONArray groupsArray = obj.getJSONArray("entry");

                    for (int i = 0; i < groupsArray.length(); i++) {
                        groups.add(groupsArray.getJSONObject(i).getString("id"));
                    }

                    return groups;
                }
            } finally {
                bufferedReader.close();
                connection.disconnect();
            }
        }
        return null;
    }

    private List<Map<String, String>> getUsersFromRemote(List<String> groups, int maxResults) throws InternalErrorException, IOException {
        List<Map<String, String>> subjects = new ArrayList<>();

        for (String group : groups) {
            List<Map<String, String>> parsedResult = getUsersFromRemoteGroup(group);
            for (Map map : parsedResult) {
                if (map != null && !subjects.contains(map)) {
                    subjects.add(map);
                }
            }
            if (maxResults > 0) {
                if (subjects.size() >= maxResults) {
                    break;
                }
            }
        }

        return subjects;
    }

    // use uriMembers attribute to obtain members from available groups
    private List<Map<String, String>> getUsersFromRemoteGroup(String group) throws IOException, InternalErrorException {
        List<Map<String, String>> users = new ArrayList();

        HttpURLConnection connection = createConnection(uriMembers + "/" + group);
        InputStream is = null;
        if (connection != null) {
            is = connection.getInputStream();
        }
        if (is != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line;

            try {
                while ((line = bufferedReader.readLine()) != null) {
                    JSONObject obj = new JSONObject(line);
                    JSONArray usersArray = obj.getJSONArray("entry");

                    for (int i = 0; i < usersArray.length(); i++) {
                        JSONObject user = usersArray.getJSONObject(i);
                        if (query == null) {
                            users.add(jsonParsing(user));
                        } else {
                            int index = query.indexOf("=");
                            String queryType = query.substring(0, index);
                            String value = query.substring(index + 1);

                            if (queryType.equals("email")
                                    && value.equals(user.getJSONArray("emails").getJSONObject(0).getString("value"))) {
                                Map parsedUser = jsonParsing(usersArray.getJSONObject(i));
                                users.add(parsedUser);
                            }

                            if (queryType.equals("id")
                                    && value.equals(user.getString("id"))) {
                                users.add(jsonParsing(usersArray.getJSONObject(i)));
                            }
                        }
                    }
                    return users;
                }
            } finally {
                bufferedReader.close();
                connection.disconnect();
            }
        }
        return null;
    }

    // method is used by getUsersFromRemoteGroup() method, creates MAP from required user
    private Map<String, String> jsonParsing(JSONObject user) throws InternalErrorException {
        Map<String, String> resultMap = new HashMap<>();

        String mapping = getAttributes().get("vootMapping");
        String[] mappingArray = mapping.split(",\n");

        for (int i = 0; i < mappingArray.length; i++) {
            String attr = mappingArray[i].trim();
            int mappingIndex = attr.indexOf("=");

            if (mappingIndex <= 0) {
                throw new InternalErrorException("There is no text in vootMapping"
                        + " attribute or there is no '=' character.");
            }

            String attrName = attr.substring(0, mappingIndex);
            String attrValue = attr.substring(mappingIndex + 1);

            if (attrValue.startsWith("{")) {

                // exclude curly brackets from value
                attrValue = attrValue.substring(1, attrValue.length() - 1);

                switch (attrValue) {
                    case "login":
                        String id = user.getString("id");
                        attrValue = id.substring(0, id.indexOf("@"));
                        resultMap.put(attrName.trim(), attrValue.trim());
                        break;
                    case "displayName":
                        attrValue = user.getString("displayName");
                        resultMap.put(attrName.trim(), attrValue.trim());
                        break;
                    case "firstName":
                    case "lastName":
                        resultMap.putAll(parseCommonName(user.getString("displayName")));
                        break;
                    case "eppn":
                        attrValue = user.getString("eppn");
                        resultMap.put(attrName.trim(), attrValue.trim());
                        break;
                    case "email":
                        attrValue = user.getJSONArray("emails").getJSONObject(0).getString("value");
                        resultMap.put(attrName.trim(), attrValue.trim());
                        break;
                }
            } else {
                resultMap.put(attrName.trim(), attrValue.trim());
                break;
            }
        }
        return resultMap;
    }

    protected void prepareEnvironment() throws InternalErrorException {
        uriMembership = (String) getAttributes().get("uriMembership");
        uriMembers = (String) getAttributes().get("uriMembers");

        if (uriMembership == null || uriMembership.isEmpty()) {
            throw new InternalErrorException("uriMembership attribute is required");
        }

        if (uriMembers == null || uriMembers.isEmpty()) {
            throw new InternalErrorException("uriMembers attribute is required");
        }
    }

    protected Map<String, String> getAttributes() throws InternalErrorException {
        return perunBl.getExtSourcesManagerBl().getAttributes(this);
    }
}
