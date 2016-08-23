package cz.metacentrum.perun.core.impl;

import com.opencsv.CSVReader;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sona Mastrakova
 */
public class ExtSourceCSV extends ExtSource implements ExtSourceApi {

    private final static Logger log = LoggerFactory.getLogger(ExtSourceCSV.class);

    private String file = null;
    private String query = null;
    private String[] header = null;

    private static PerunBlImpl perunBl;

    // filled by spring (perun-core.xml)
    public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
        perunBl = perun;
        return perun;
    }

    @Override
    public List<Map<String, String>> findSubjectsLogins(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("For CSV using this method is not optimized, use findSubjects instead.");
    }

    @Override
    public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("For CSV using this method is not optimized, use findSubjects instead.");
    }

    @Override
    public List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
        return findSubjects(searchString, 0);
    }

    @Override
    public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
        try {
            query = getAttributes().get("query");

            if (query == null || query.isEmpty()) {
                throw new InternalErrorException("query attribute is required");
            }

            if (searchString == null) {
                throw new InternalErrorException("search string can't be null");
            }

            //Replace '?' by searchString
            query = query.replaceAll("\\?", searchString);

            //Get csv file 
            prepareEnvironment();

            return csvParsing(query, maxResults);

        } catch (IOException ex) {
            log.error("IOException in findSubjects() method while parsing csv file", ex);
        }

        return null;
    }

    @Override
    public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException, ExtSourceUnsupportedOperationException {
        try {
            query = getAttributes().get("loginQuery");

            if (query == null || query.isEmpty()) {
                throw new InternalErrorException("loginQuery attribute is required");
            }

            if (login == null || login.isEmpty()) {
                throw new InternalErrorException("login string can't be null or empty");
            }

            //Replace '?' by searchString
            query = query.replaceAll("\\?", login);

            //Get csv file 
            prepareEnvironment();

            List<Map<String, String>> subjects = this.csvParsing(query, 0);

            if (subjects.isEmpty()) {
                throw new SubjectNotExistsException("Login: " + login);
            }
            if (subjects.size() > 1) {
                throw new InternalErrorException("External source must return exactly one result, search string: " + login);
            }

            return subjects.get(0);

        } catch (IOException ex) {
            log.error("IOException in getSubjectByLogin() method while parsing csv file", ex);
        }

        return null;
    }

    @Override
    public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException {
        try {
            // Get the query for the group subjects
            String queryForGroup = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

            //If there is no query for group, throw exception
            if (queryForGroup == null) {
                throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSEXTSOURCE_ATTRNAME + " can't be null.");
            }

            //Get csv file
            prepareEnvironment();

            return csvParsing(queryForGroup, 0);

        } catch (IOException ex) {
            log.error("IOException in getGroupSubjects() method while parsing csv file", ex);
        }
        return null;
    }

    @Override
    public void close() throws InternalErrorException, ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("For CSV, using this method is not optimized, use findSubjects instead.");
    }

    private void prepareEnvironment() throws InternalErrorException {
        //Get csv files
        file = (String) getAttributes().get("file");
        if (file == null || file.isEmpty()) {
            throw new InternalErrorException("File cannot be empty!");
        }
    }

    private List<Map<String, String>> csvParsing(String query, int maxResults) throws InternalErrorException, FileNotFoundException, IOException {
        List<Map<String, String>> subjects = new ArrayList<Map<String, String>>();

        FileReader fileReader = new FileReader(file);
        if (fileReader == null) {
            throw new FileNotFoundException("File was not found!");
        }

        CSVReader reader = new CSVReader(fileReader);

        header = reader.readNext();
        if (header == null) {
            throw new RuntimeException("No header in csv file");
        }

        String[] row;

        while ((row = reader.readNext()) != null) {

            if (header.length != row.length) {
                throw new RuntimeException("Csv file is not valid - some rows have different number of columns from the header row.");
            }

            if (compareRowToQuery(row, query)) {

                Map<String, String> map = convertLineToMap(row);

                if (map != null) {
                    subjects.add(map);
                }

                if (maxResults > 0) {
                    if (subjects.size() >= maxResults) {
                        break;
                    }
                }
            }
        }

        return subjects;
    }

    /**
     * Comparison of 1 row in csv file with the query.
     * - if the row would be result of the query, then this method returns true
     * - if not, then this method returns false
     *
     * @param row 1 row from csv file
     * @param query query we want to 'execute' on the row, e.g. nameOfColumn=valueInRow
     * @return boolean
     * @throws InternalErrorException
     */
    private boolean compareRowToQuery(String[] row, String query) throws InternalErrorException {

        // symbol '=' indicates getSubjectByLogin() or getGroupSubjects method
        int index = query.indexOf("=");
        // word 'contains' indicates findSubjects() method
        int indexContains = query.indexOf("contains");

        if (index != -1) {
            String queryType = query.substring(0, index);
            String value = query.substring(index + 1);

            for (int i = 0; i < row.length; i++) {
                if ((header[i].compareTo(queryType) == 0 && row[i].compareTo(value) == 0)) {
                    return true;
                }
            }
        } else {
            if (indexContains != -1) {
                String queryType = query.substring(0, indexContains);
                String value = query.substring(indexContains + "contains".trim().length());

                for (int i = 0; i < row.length; i++) {
                    value = value.trim();
                    queryType = queryType.trim();

                    if (header[i].compareTo(queryType) == 0 && row[i].contains(value)) {
                        return true;
                    }
                }
            } else {
                // if there's no symbol '=' or word 'contains' in the query
                throw new InternalErrorException("Wrong query!");
            }
        }

        return false;
    }

    /**
     * Creates Map<String,String> from 1 row in csv file
     * 
     * @param line 1 row from csv file
     * @return Map<String, String>, like <name,value>
     * @throws InternalErrorException
     */
    private Map<String, String> convertLineToMap(String[] line) throws InternalErrorException {

        Map<String, String> lineAsMap = new HashMap<String, String>();

        String mapping = getAttributes().get("csvMapping");

        String[] mappingArray = mapping.split(",\n");

        for (int i = 0; i < mappingArray.length; i++) {

            for (int j = 0; j < line.length; j++) {

                String attr = mappingArray[i].trim();

                int index = attr.indexOf("=");

                if (index <= 0) {
                    throw new InternalErrorException("There is no text in csvMapping attribute or there is no '=' character.");
                }

                String name = attr.substring(0, index);
                String value = attr.substring(index + 1);

                if (value.startsWith("{")) {
                    
                    // exclude curly brackets from value
                    value = value.substring(1, value.length() - 1);

                    if (value.compareTo(header[j]) == 0) {
                        value = line[j];
                        lineAsMap.put(name.trim(), value.trim());
                        break;
                    }
                } else {
                    lineAsMap.put(name.trim(), value.trim());
                    break;
                }
            }
        }
        return lineAsMap;
    }

	protected Map<String,String> getAttributes() throws InternalErrorException {
		return perunBl.getExtSourcesManagerBl().getAttributes(this);
	}
}
