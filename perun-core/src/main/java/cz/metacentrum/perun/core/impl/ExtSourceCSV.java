package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Ext source for CSV files. It expects them to have 1st row as a header.
 *
 * Docs: https://wiki.metacentrum.cz/wiki/Perun_external_sources#CSV
 *
 * @author Sona Mastrakova
 * @author Pavel Zlámal
 */
public class ExtSourceCSV extends ExtSource implements ExtSourceApi {

    private final static Logger log = LoggerFactory.getLogger(ExtSourceCSV.class);

    private String file = null;
    private String query = null;

    private static PerunBlImpl perunBl;

    // Filled by Spring (perun-core.xml)
    public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
        perunBl = perun;
        return perun;
    }

    @Override
    public List<Map<String, String>> findSubjectsLogins(String searchString) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("For CSV using this method is not optimized, use findSubjects instead.");
    }

    @Override
    public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("For CSV using this method is not optimized, use findSubjects instead.");
    }

    @Override
    public List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException {
        return findSubjects(searchString, 0);
    }

    @Override
    public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException {
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

            //Get CSV file
            prepareFile();

            return csvParsing(query, maxResults);

        } catch (IOException ex) {
            log.error("IOException in findSubjects() method while parsing csv file", ex);
        }

        return null;
    }

    @Override
    public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException {
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

            //Get CSV file
            prepareFile();

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
    public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException {
        try {
            // Get the query for the group subjects
            String queryForGroup = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

            // If there is no query for group, throw exception
            if (queryForGroup == null) {
                throw new InternalErrorException("Attribute " + GroupsManager.GROUPMEMBERSQUERY_ATTRNAME + " can't be null.");
            }

            // Get CSV file
            prepareFile();

            return csvParsing(queryForGroup, 0);

        } catch (IOException ex) {
            log.error("IOException in getGroupSubjects() method while parsing csv file", ex);
        }
        return null;
    }

	@Override
	public List<Map<String, String>> getUsersSubjects() throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

    @Override
    public void close() throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("Using this method is not supported for CSV.");
    }

    @Override
    public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes) throws ExtSourceUnsupportedOperationException {
        throw new ExtSourceUnsupportedOperationException("Using this method is not supported for CSV.");
    }

    /**
     * Initialize CSV file.
     *
     * @throws InternalErrorException When fail not exists or is empty
     */
    private void prepareFile() throws InternalErrorException {
        //Get CSV file
        file = getAttributes().get("file");
        if (file == null || file.isEmpty()) {
            throw new InternalErrorException("File cannot be empty!");
        }
    }

    /**
     * Parse CSV file into list of our standard "subject" (aka candidates)
     *
     * @param query query to check CSV file content against
     * @param maxResults limit results to X row or 0 for unlimited
     * @return List of Maps representing subjects for synchronization (perun_attr/constant = value).
     * @throws InternalErrorException When implementation fails
     * @throws IOException When reading CSV file fails
     */
    private List<Map<String, String>> csvParsing(String query, int maxResults) throws InternalErrorException, IOException {

        List<Map<String, String>> subjects = new ArrayList<>();

        Map<String,String> attributeMapping = getCsvMapping();

        File csvFile = new File(file);
        CsvMapper mapper = new CsvMapper();
        // use first row as header; otherwise defaults are fine
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        MappingIterator<Map<String,String>> it = mapper.readerFor(Map.class).with(schema).readValues(csvFile);
        while (it.hasNext()) {

            Map<String,String> rowAsMap = it.next();

            if (compareRowToQuery(rowAsMap, query)) {

                Map<String,String> singleSubject = new HashMap<>();

                // translate CSV column names to perun attribute URNs
                for (String key : rowAsMap.keySet()) {
                    singleSubject.put(attributeMapping.get(key), rowAsMap.get(key));
                }

                subjects.add(singleSubject);

                // break if we required limited response
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
     * Comparison of one row in CSV file with the query.
     * - if the row would be result of the query, then this method returns true
     * - if not, then this method returns false
     *
     * Basically it has two modes, exact match when query contains mapping "column=value"
     * and contains (substring) when query contains mapping "column contains ?".
     *
     * @param rowAsMap one row from CSV file
     * @param query query we want to 'execute' on the row, e.g. nameOfColumn=valueInRow
     * @return TRUE if row matches query and should be processed / FALSE when row should be skipped
     * @throws InternalErrorException When implementation fails
     */
    private boolean compareRowToQuery(Map<String,String> rowAsMap, String query) throws InternalErrorException {

        // symbol '=' indicates getSubjectByLogin() or getGroupSubjects() method
        int index = query.indexOf("=");
        // word 'contains' indicates findSubjects() method
        int indexContains = query.indexOf("contains");

        if (index != -1) {

            String queryType = query.substring(0, index);
            String value = query.substring(index + 1);

            // whether value in requested "column" in CSV equals expected value
            return Objects.equals(value, rowAsMap.get(queryType));

        } else if (indexContains != -1) {

            String queryType = query.substring(0, indexContains);
            String value = query.substring(indexContains + "contains".trim().length());

            value = value.trim();
            queryType = queryType.trim();

            // whether value in requested "column" in CSV contains (substring) expected value
            return (rowAsMap.get(queryType) != null && rowAsMap.get(queryType).contains(value));

        } else {

            // If there's no symbol '=' or word 'contains' in the query
            throw new InternalErrorException("Wrong query!");

        }

    }

    /**
     * Returns Map<String,String> with mapping of "CSV column name" to "Perun attribute URN".
     *
     * @return Map<String, String>, like <CSV column name,Perun attribute URN>
     * @throws InternalErrorException When implementation fails
     */
    private Map<String, String> getCsvMapping() throws InternalErrorException {

        Map<String, String> attributeMapping = new HashMap<>();

        String mapping = getAttributes().get("csvMapping");

        String[] mappingArray = mapping.split(",\n");

        for (String s : mappingArray) {

            String attr = s.trim();

            int index = attr.indexOf("=");

            if (index <= 0) {
                throw new InternalErrorException("There is no text in csvMapping attribute or there is no '=' character.");
            }

            String name = attr.substring(0, index);
            String value = attr.substring(index + 1);

            if (value.startsWith("{")) {
                // exclude curly brackets from value
                value = value.substring(1, value.length() - 1);
            }

            attributeMapping.put(name.trim(), value.trim());

        }

        return attributeMapping;

    }

    /**
     * Get attributes of the external source (defined in perun-extSources.xml).
     *
     * @return map with attributes about the external source
     * @throws InternalErrorException When implementation fails
     */
    protected Map<String,String> getAttributes() throws InternalErrorException {
        return perunBl.getExtSourcesManagerBl().getAttributes(this);
    }

}
