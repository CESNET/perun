package cz.metacentrum.perun.core.impl;

import com.jayway.jsonpath.JsonPath;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Ext source for JSON files.
 *
 * @author Johana Supikova supikova@ics.muni.cz
 */
public class ExtSourceJSON extends ExtSourceImpl implements ExtSourceApi {

    private final static Logger log = LoggerFactory.getLogger(ExtSourceJSON.class);

    private String filepath = null;
    private String query = null;

    @Override
    public List<Map<String, String>> findSubjectsLogins(String searchString) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<Map<String, String>> findSubjects(String searchString) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("Not implemented.");
    }

    @Override
    public Map<String, String> getSubjectByLogin(String login) throws SubjectNotExistsException, ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("Not implemented.");
    }


    @Override
    public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) {
        // Using JsonPath lib does not allow filtering by child properties! Adjust the json structure accordingly.
        try {
            // Get the query, expects json path format, f.e. "$.members.*"
            String membersQuery = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

            // If there is no query for group, throw exception
            if (membersQuery == null) {
                throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " can't be null.");
            }

            filepath = prepareFilepath(attributes);

            return jsonParsing(membersQuery, 0);

        } catch (IOException ex) {
            log.error("IOException in getGroupSubjects() method while parsing json file", ex);
        }
        return null;
    }

	@Override
	public List<Map<String, String>> getUsersSubjects() throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("Not implemented.");
	}

    @Override
    public void close() throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("Not implemented.");
    }

    /**
     * Initialize JSON file. The file is either specified in the extSource configuration 'file' property,
     * or a combination of configuration 'directory' property and 'file' attribute of the group
     *
     * @throws InternalErrorException When filepath cannot be constructed
     */
    private String prepareFilepath(Map<String, String> groupAttributes) {
        Map<String, String> extsourceAttributes = getAttributes();
        String file = extsourceAttributes.get("file");
        String directory = extsourceAttributes.get("directory");

        if (directory == null || directory.isEmpty()) {
            if (file == null || file.isEmpty()) {
                throw new InternalErrorException("File or directory config property is missing!");
            }
            filepath = file;
        } else {
            String fileAttr = groupAttributes.get(GroupsManager.GROUP_SYNCHRONIZATION_FILE_ATTRNAME);
            if (fileAttr == null || fileAttr.isEmpty()) {
                throw new InternalErrorException("Filename attribute is missing!");
            }
            filepath = directory + "/" + fileAttr;
        }
        return filepath;
    }

    /**
     * Parse JSON file into list of our standard "subject" (aka candidates)
     *
     * @param query query to check JSON file content against
     * @param maxResults limit results to X row or 0 for unlimited
     * @return List of Maps representing subjects for synchronization (perun_attr/constant = value).
     * @throws InternalErrorException When implementation fails
     * @throws IOException When reading JSON file fails
     */
    private List<Map<String, String>> jsonParsing(String query, int maxResults) throws IOException {

        List<Map<String, String>> subjects = new ArrayList<>();

        Map<String,String> attributeMapping = getJsonMapping();
        String json;
        try {
            json = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (NoSuchFileException e) {
            String skipMissingFiles = getAttributes().get("skipMissingFiles");

            // Return empty structure if skipping missing files
            if (skipMissingFiles != null && skipMissingFiles.equalsIgnoreCase("true")) {
                return subjects;
            }
            throw new InternalErrorException("File " + filepath + " not found!", e);
        }

        List<Map<String, String>> rawMembers = JsonPath.parse(json).read(query);

        for (Map<String,String> rawMember : rawMembers) {
            Map<String,String> singleSubject = new HashMap<>();

            // Translate JSON keys to perun attribute URNs
            for (String key : attributeMapping.keySet()) {
                String mappingValue = attributeMapping.get(key);
                if (mappingValue.contains("{")) {
                    String fieldName = StringUtils.substringBetween(mappingValue, "{", "}");
                    String fieldValue = rawMember.get(fieldName);
                    String newValue = mappingValue.replace("{"+fieldName+"}", fieldValue);
                    singleSubject.put(key, newValue);
                } else {
                    singleSubject.put(key, mappingValue);
                }
            }

            subjects.add(singleSubject);

            // Break if we require limited response
            if (maxResults > 0 && subjects.size() >= maxResults) {
                break;
            }
        }

        return subjects;

    }


    /**
     * Returns Map<String,String> with mapping of "JSON property name" to "Perun attribute URN".
     *
     * @return Map<String, String>, like <JSON property name,Perun attribute URN>
     * @throws InternalErrorException When implementation fails
     */
    private Map<String, String> getJsonMapping() {
        Map<String, String> attributeMapping = new HashMap<>();

        String mapping = getAttributes().get("jsonMapping");

        String[] mappingArray = mapping.split(",\n");

        for (String s : mappingArray) {

            String attr = s.trim();

            int index = attr.indexOf("=");

            if (index <= 0) {
                throw new InternalErrorException("There is no text in jsonMapping attribute or there is no '=' character.");
            }

            String name = attr.substring(0, index);
            String value = attr.substring(index + 1);

            attributeMapping.put(name.trim(), value.trim());

        }

        return attributeMapping;

    }
}
