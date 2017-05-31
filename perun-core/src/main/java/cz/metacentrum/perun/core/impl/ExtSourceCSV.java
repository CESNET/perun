package cz.metacentrum.perun.core.impl;

import com.opencsv.CSVReader;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sona Mastrakova, Jan Zverina
 */
public class ExtSourceCSV extends ExtSource implements ExtSourceApi {

    private final static Logger log = LoggerFactory.getLogger(ExtSourceCSV.class);

    private String file = null;
    private String query = null;
    private String[] header = null;
    private HashMap<String, String> csvMapping = null;

    private static PerunBlImpl perunBl;

    // Filled by Spring (perun-core.xml)
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
            prepareFile();

			List<Map<String, String>> subjects = new ArrayList<>();
            csvParsing(query, maxResults, subjects);
            return subjects;

        } catch (IOException ex) {
            log.error("IOException in findSubjects() method while parsing csv file", ex);
            throw new InternalErrorException(ex);
        }
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
            prepareFile();

            List<Map<String, String>> subjects = new ArrayList<>();
            this.csvParsing(query, 0, subjects);

            if (subjects.isEmpty()) {
                throw new SubjectNotExistsException("Login: " + login);
            }
            if (subjects.size() > 1) {
                throw new InternalErrorException("External source must return exactly one result, search string: " + login);
            }

            return subjects.get(0);

        } catch (IOException ex) {
            log.error("IOException in getSubjectByLogin() method while parsing csv file", ex);
            throw new InternalErrorException(ex);
        }
    }

    /**
     * Get subjects of group. These subjects will be stored in maps obtained in parameters.
     *
     *
     * @param sess
     * @param group Map of attributes used for quering the external source
     * @param status Status about synchronization (lightweight, full). Not used in CSV external source.
     * @param subjects list of maps, which contains attr_name-&gt;attr_value, e.g. firstName-&gt;Michal
     * @return String containing status about data received from external source (e.g. data are sorted,
     * @throws InternalErrorException
     * @throws ExtSourceUnsupportedOperationException
     */
    @Override
    public String getGroupSubjects(PerunSession sess, Group group, String status, List<Map<String, String>> subjects) throws InternalErrorException, ExtSourceUnsupportedOperationException {
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

            // If there is no query for group, throw exception
            if (queryForGroup == null) {
                throw new InternalErrorException("Value of " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " can't be null.");
            }

            // Get CSV file
            prepareFile();

            csvParsing(queryForGroup, 0, subjects);
            return GroupsManager.GROUP_SYNC_STATUS_FULL;

        } catch (IOException ex) {
            log.error("IOException in getGroupSubjects() method while parsing csv file", ex);
            throw new InternalErrorException(ex);
        }
    }

    @Override
    public void close() throws InternalErrorException, ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("For CSV, using this method is not optimized, use findSubjects instead.");
    }

    private void prepareFile() throws InternalErrorException {
        //Get csv file
        file = getAttributes().get("file");
        if (file == null || file.isEmpty()) {
            throw new InternalErrorException("File cannot be empty!");
        }
    }

    private void csvParsing(String query, int maxResults, List<Map<String,String>> subjects) throws InternalErrorException, IOException {
        CSVReader reader = initializeCSVReader(file);
        header = reader.readNext();
        if (header == null) {
            throw new RuntimeException("No header in csv file");
        }
        createSubjectsFromRows(query, reader, maxResults, subjects);
    }

    /**
     * Initialize CSVReader object to read CSV file from path defined in parameter.
     *
     * @param fileName Path to file
     * @return CSVReader object
     * @throws FileNotFoundException
     */
    private CSVReader initializeCSVReader(String fileName) throws FileNotFoundException {
        FileReader fileReader = new FileReader(fileName);
        if (fileReader == null) {
            throw new FileNotFoundException("File was not found!");
        }
        return new CSVReader(fileReader);
    }

    /**
     * Converts reader of rows from file to list of subjects obtained in parameter.
     * This method CONTROLS maximum of results.
     *
     * @param query Query to select correct rows with users from file.
     * @param reader Reader of file without header
     * @param maxResults Maximum of results, which will be stored to subjects. If 0, its unlimited.
     * @param subjects List to store subjects.
     * @throws InternalErrorException
     * @throws IOException
     */
    private void createSubjectsFromRows(String query, CSVReader reader, int maxResults, List<Map<String,String>> subjects) throws InternalErrorException, IOException {
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
    }

    /**
     * Comparison of one row in CSV file with the query.
     * - If the row would be result of the query, then this method returns true
     * - If not, then this method returns false
     *
     * @param row one row from CSV file
     * @param query query we want to 'execute' on the row, e.g. nameOfColumn=valueInRow
     * @return boolean
     * @throws InternalErrorException
     */
    private boolean compareRowToQuery(String[] row, String query) throws InternalErrorException {
        // Symbol '=' indicates getSubjectByLogin()
        int index = query.indexOf("=");

        if (index != -1) {
            String queryType = query.substring(0, index);
            String value = query.substring(index + 1);

            for (int i = 0; i < row.length; i++) {
                if ((header[i].compareTo(queryType) == 0 && row[i].compareTo(value) == 0)) {
                    return true;
                }
            }
        } else {
            // Word 'contains' indicates findSubjects() method or getGroupSubjects() method
            int indexContains = query.indexOf("contains");

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
                // If there's no symbol '=' or word 'contains' in the query
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

    /**
     * Get attributes of external source (defined in perun-extSources.xml).
     *
     * @return map with attributes about external source
     * @throws InternalErrorException
     */
	protected Map<String,String> getAttributes() throws InternalErrorException {
		return perunBl.getExtSourcesManagerBl().getAttributes(this);
	}
}
