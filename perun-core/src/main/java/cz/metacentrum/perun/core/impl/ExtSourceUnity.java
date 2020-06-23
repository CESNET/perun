package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static cz.metacentrum.perun.core.impl.Utils.parseCommonName;

/**
 * ExtSource implementation for Unity system.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 12.09.2016
 */
public class ExtSourceUnity extends ExtSource implements ExtSourceApi {

    private final static Logger log = LoggerFactory.getLogger(ExtSourceUnity.class);
    private String username;
    private String password;
    private String uriAll;
    private String uriEntity;
    private static PerunBlImpl perunBl;

    // filled by spring (perun-core.xml)
    public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
        perunBl = perun;
        return perun;
    }

    @Override
    public List<Map<String, String>> findSubjectsLogins(String searchString) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("For Unity using this method is not optimized, use findSubjects instead.");
    }

    @Override
    public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("For Unity using this method is not optimized, use findSubjects instead.");
    }

    @Override
    public List<Map<String, String>> findSubjects(String searchString) {
        return findSubjects(searchString, 0);
    }

    @Override
    public List<Map<String, String>> findSubjects(String searchString, int maxResults) {
        String query = getAttributes().get("query");

        if (query == null || query.isEmpty()) {
            throw new InternalErrorException("query attribute is required");
        }

        if (searchString == null) {
            throw new InternalErrorException("search string can't be null");
        }

        //Replace '?' by searchString
        query = query.replaceAll("\\?", searchString);

        prepareEnvironment();

        return jsonParsing(query, maxResults);

    }

    @Override
    public Map<String, String> getSubjectByLogin(String login) throws SubjectNotExistsException {
        String query = getAttributes().get("loginQuery");

        if (query == null || query.isEmpty()) {
            throw new InternalErrorException("loginQuery attribute is required");
        }

        if (login == null || login.isEmpty()) {
            throw new InternalErrorException("login string can't be null or empty");
        }

        //Replace '?' by searchString
        query = query.replaceAll("\\?", login);

        prepareEnvironment();

        List<Map<String, String>> subjects = jsonParsing(query, 0);

        if (subjects == null || subjects.isEmpty()) {
            throw new SubjectNotExistsException("Login: " + login);
        }
        if (subjects.size() > 1) {
            throw new InternalErrorException("External source must return exactly one result, search string: " + login);
        }

        return subjects.get(0);
    }

    @Override
    public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) {
        // Get the query for the group subjects
        String queryForGroup = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

        //If there is no query for group, throw exception
        if (queryForGroup == null) {
            throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " can't be null.");
        }

        prepareEnvironment();

        return jsonParsingGroups(queryForGroup);
    }

	@Override
	public List<Map<String, String>> getUsersSubjects() throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

    @Override
    public void close() {
        throw new UnsupportedOperationException("Using this method is not supported for Unity");
    }

    @Override
    public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException();
    }

    protected Map<String, String> getAttributes() {
        return perunBl.getExtSourcesManagerBl().getAttributes(this);
    }

    private void prepareEnvironment() {
        uriAll = getAttributes().get("uriAll");
        if (uriAll == null || uriAll.isEmpty()) {
            throw new InternalErrorException("UriAll cannot be empty.");
        }
        uriEntity = getAttributes().get("uriEntity");
        if (uriEntity == null || uriEntity.isEmpty()) {
            throw new InternalErrorException("UriEntity cannot be empty.");
        }
    }

    private List<Integer> connectAndGetEntitiesId() throws IOException {
        HttpURLConnection connection = createConnection(uriAll);
        InputStream is = null;
        if (connection != null) {
            is = connection.getInputStream();
        }
        if (is != null) {

	        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {
		        String line;
		        while ((line = bufferedReader.readLine()) != null) {
			        return parseEntitiesIdFromRemote(line);
		        }
	        } finally {
		        connection.disconnect();
	        }
        }
        return null;
    }

    private List<UnityEntity> connectAndGetValidEntitiesById(List<Integer> ids) throws IOException {
        List<UnityEntity> results = new ArrayList();

        for (int id : ids) {
            JSONObject entity = getEntityFromRemote(id);
            if (entity != null) {
	            UnityEntity unityEntity = parseValidUnityEntity(entity);
	            if (unityEntity != null) {
		            results.add(unityEntity);
	            }
            }
        }

        return results;
    }

    private UnityEntity connectAndGetValidEntityByGroup(int entityId, String groupName) throws IOException {
        HttpURLConnection connection = createConnection(checkUri(uriEntity) + entityId + "/groups/");
        JSONArray groups = getJSONArrayFromRemote(connection);

        // if entity belongs to groupName
        if (groups != null && compareGroupName(groups, groupName)) {
            JSONObject entity = getEntityFromRemote(entityId);
            if (entity == null) throw new InternalErrorException("Entity returned for entityId " + entityId + " was null.");
            return parseValidUnityEntity(entity);
        }
        return null;
    }

    /**
     * Method creates connection to the uri.
     *
     * @param uri
     * @returns if responseCode to the uri is 200, then returns connection
     */
    private HttpURLConnection createConnection(String uri) throws IOException {
        HttpURLConnection connection;

        username = getAttributes().get("user");
        password = getAttributes().get("password");

        String userPassword = username + ":" + password;

        connection = (HttpURLConnection) new URL(uri).openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", getBasicAuthenticationEncoding(userPassword));
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            log.warn("Cannot establish connection to " + uri + ". Response code is " + responseCode + ". Error message is \'"
                    + new BufferedReader(new InputStreamReader(connection.getErrorStream())).readLine() + "\'.");
        }
        return connection;
    }

    private String getBasicAuthenticationEncoding(String userCredentials) {
        return "Basic " + new String(Base64Coder.encode(userCredentials.getBytes()));
    }

    /**
     * Method obtains JSONObject, that represents entity with specific id.
     *
     * @param id entity id
     * @return entity obtained by API call
     */
    private JSONObject getEntityFromRemote(int id) throws IOException {
        HttpURLConnection connection = createConnection(checkUri(uriEntity) + id + "/");
        InputStream is = null;
        if (connection != null) {
            is = connection.getInputStream();
        }
        if (is != null) {

	        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {
		        String line;
		        while ((line = bufferedReader.readLine()) != null) {
			        return new JSONObject(line);
		        }
	        } finally {
		        connection.disconnect();
	        }
        }
        return null;
    }

    private List<Integer> parseEntitiesIdFromRemote(String line) {
        List<Integer> results = new ArrayList();

        JSONObject obj = new JSONObject(line);
        JSONArray members = obj.getJSONArray("members");

        // get each member from json array
        for (int i = 0; i < members.length(); i++) {
            results.add(members.getInt(i));
        }

        return results;
    }

    /**
     * Method obtains JSON object, that represents entity in Unity API and
     * checks if it's state is VALID. If yes, then it transforms object to
     * UnityEntity class. If not, then it does nothing.
     *
     * @param obj JSON object obtained from API
     * @return parsed unity entity
     */
    private UnityEntity parseValidUnityEntity(JSONObject obj) throws IOException {
        if ("valid".equals(obj.get("state"))) {
            UnityEntity result = new UnityEntity();
            result.setEntityId(obj.get("id").toString());
            result.setHasAdditionalExtSource(false);

            // calls API to obtain entity attributes
            JSONArray attributes = getEntityAttributesFromRemote(result.getEntityId());
            if (attributes == null) {
                return null;
            }

            // parse name and email
            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attribute = attributes.getJSONObject(i);
                if ("name".equals(attribute.get("name"))) {
                    JSONArray attrName = attribute.getJSONArray("values");
                    String[] names = attrName.getString(0).split(" ");
                    if (names.length > 1) {
                        result.setFullName(attrName.getString(0));
                    }
                } else if ("email".equals(attribute.get("name"))) {
                    JSONArray attrEmail = attribute.getJSONArray("values");
                    result.setEmail(attrEmail.getString(0));
                }
            }

            // parse remoteIdp as additional extSource
            JSONArray identities = obj.getJSONArray("identities");
            for (int i = 0; i < identities.length(); i++) {
                JSONObject identity = identities.getJSONObject(i);
                if ("identifier".equals(identity.get("typeId"))) {
                    if ("googleProfile".equals(identity.get("translationProfile"))) {
                        result.setExtSourceLogin(identity.get("value").toString());
                        result.hasAdditionalExtSource = true;
                    } else {
                        result.setLogin(identity.get("value").toString());
                    }
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Method obtains JSONArray, that represents attributes of the entity with
     * specific entity id.
     *
     * @param id entity id
     * @return entity attributes obtained by API call
     */
    private JSONArray getEntityAttributesFromRemote(String id) throws IOException {
        HttpURLConnection connection = createConnection(checkUri(uriEntity) + id + "/attributes");

        return getJSONArrayFromRemote(connection);
    }

    /**
     * Method transforms input stream from connection to JSONArray.
     *
     * @param connection connection, from which we obtain JSONArray
     * @return JSONArray containing some values
     */
    private JSONArray getJSONArrayFromRemote(HttpURLConnection connection) throws IOException {
        InputStream is;
        if (connection != null) {
            is = connection.getInputStream();
        } else {
            return null;
        }
        if (is != null) {

	        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {
		        String line;
		        while ((line = bufferedReader.readLine()) != null) {
			        return new JSONArray(line);
		        }
	        } finally {
		        connection.disconnect();
	        }
        }

        return null;
    }

    /**
     * Method looks for groupName in the groups array.
     *
     * @param groups JSONArray with all group names
     * @param groupName one group name
     * @return true if array contains groupName, otherwise false.
     */
    private boolean compareGroupName(JSONArray groups, String groupName) {
        for (int i = 0; i < groups.length(); i++) {
            if (groupName.equals(groups.get(i))) {
                return true;
            }
        }
        return false;
    }

    private List<Map<String, String>> jsonParsing(String query, int maxResults) {
        try {
            List<Integer> entitiesIds = connectAndGetEntitiesId();
            List<UnityEntity> validEntities = connectAndGetValidEntitiesById(entitiesIds);

            List<Map<String, String>> subjects = new ArrayList<>();

            int index = query.indexOf("=");
            if (index != -1) {
                String attrName = query.substring(0, index);
                String attrValue = query.substring(index + 1);

                for (UnityEntity entity : validEntities) {
                    Map<String, String> map = new HashMap<>();
                    if (maxResults > 0) {
                        if (subjects.size() >= maxResults) {
                            break;
                        }
                    }

                    // if query contains login or email
                    switch (attrName) {
                        case "login":
                            String login = entity.getLogin();
                            // if login in query equals entity's login
                            if (login.contains("@") && attrValue.equals(login.substring(0, login.indexOf("@")))) {
                                map = processUnityMapping(entity);
                            } else if (attrValue.equals(login)) {
                                map = processUnityMapping(entity);
                            }
                            break;
                        case "email":
                            // if email in query equals entity's email
                            if (attrValue.equals(entity.getEmail())) {
                                map = processUnityMapping(entity);
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Query can use only 'login' or 'email' values. "
                                    + "Value \'" + attrName + "\' is not allowed.");
                    }

                    if (!map.isEmpty()) {
                        subjects.add(map);
                    }
                }
            }
            return subjects;
        } catch (IOException ex) {
            log.error("IOException while trying to connect to Unity REST API to obtain user(s) containing value " + query, ex);
        }
        return null;
    }

    private List<Map<String, String>> jsonParsingGroups(String queryForGroup) {
        try {
            List<Integer> entitiesIds = connectAndGetEntitiesId();
            List<UnityEntity> validEntities = new ArrayList();

            for (int id : entitiesIds) {
                UnityEntity validEntity = connectAndGetValidEntityByGroup(id, queryForGroup);
                if (validEntity != null) {
                    validEntities.add(validEntity);
                }
            }

            List<Map<String, String>> subjects = new ArrayList<>();

            for (UnityEntity entity : validEntities) {
                Map<String, String> map = processUnityMapping(entity);

                if (!map.isEmpty()) {
                    subjects.add(map);
                }
            }

            return subjects;
        } catch (IOException ex) {
            log.error("IOException while trying to connect to Unity REST API to obtain users of the group " + queryForGroup, ex);
        }
        
        return null;
    }

    /**
     * Method transforms unityEntity to Map<String,String> where first String
     * represents mapName and second String represents mapValue according to
     * mapAttribute in the extSources.xml file.
     *
     * @param unityEntity entity, that is transformed to Map
     * @return map containing name and value
     */
    private Map<String, String> processUnityMapping(UnityEntity unityEntity) {
        Map<String, String> map = new HashMap<>();
        if (unityEntity.getFullName() == null) {
            return map;
        }

        String mapping = getAttributes().get("unityMapping");
        String[] mappingArray = mapping.split(",\n");

        for (String mappingElement : mappingArray) {

            String attr = mappingElement.trim();
            int index = attr.indexOf("=");

            if (index <= 0) {
                throw new InternalErrorException("There is no text in unityMapping attribute or "
                        + "there is no '=' character.");
            }

            String mapName = attr.substring(0, index);
            String mapValue = attr.substring(index + 1);

            if (mapValue.startsWith("{")) {

                // exclude curly brackets from value
                mapValue = mapValue.substring(1, mapValue.length() - 1);
                switch (mapValue) {
                    case "login":
                        map.put(mapName.trim(), unityEntity.getLogin().trim());
                        break;
                    case "firstName":
                    case "lastName":
                        map.putAll(parseCommonName(unityEntity.getFullName()));
                        break;
                    case "email":
                        if (unityEntity.getEmail() != null) {
                            map.put(mapName.trim(), unityEntity.getEmail().trim());
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Obtained value in curly brackets is not supported.");
                }
            } else {
                map.put(mapName.trim(), mapValue.trim());
            }
            break;
        }

        if (unityEntity.getExtSourceLogin()!= null) {
            map.put(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING + "1", "https://extidp.cesnet.cz/idp/shibboleth|cz.metacentrum.perun.core.impl.ExtSourceIdp|" + unityEntity.getExtSourceLogin()+ "@google.extidp.cesnet.cz");
        }

        return map;
    }

    /**
     * Method checks if uri ends with slash character.
     *
     * @param uri
     * @return uri ending with slash character
     */
    private String checkUri(String uri) {
        return (uri.endsWith("/")) ? uri : (uri + "/");
    }

    private static class UnityEntity {

        String entityId;
        String login;
        String extSourceLogin;
        String fullName;
        String email;
        Boolean hasAdditionalExtSource;

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String unityValue) {
            this.login = unityValue;
        }

        public String getExtSourceLogin() {
            return extSourceLogin;
        }

        public void setExtSourceLogin(String extSourceLogin) {
            this.extSourceLogin = extSourceLogin;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Boolean getHasAdditionalExtSource() {
            return hasAdditionalExtSource;
        }

        public void setHasAdditionalExtSource(Boolean hasAdditionalExtSource) {
            this.hasAdditionalExtSource = hasAdditionalExtSource;
        }

        @Override
        public String toString() {
            return "UnityEntity{"
                    + "entityId='" + entityId + '\''
                    + ", value='" + login + '\''
                    + ", fullName='" + fullName + '\''
                    + ", email='" + email + '\''
                    + '}';
        }
    }
}
