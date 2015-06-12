package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * Class JdbcPerunTemplate extends JdbcTemplate from spring. - it has 1
 * additional method queryForObject(String sql, ResultSetExtractor<T> rse,
 * Object... args)
 *
 * @author Sona Mastrakova
 */
public class JdbcPerunTemplate extends JdbcTemplate {

    private final JdbcTemplate jdbc;

    public JdbcPerunTemplate(DataSource perunPool) {
        jdbc = new JdbcPerunTemplate(perunPool);
    }

    /**
     * Returns one object from the query, else throws an exception.
     *
     * @param sql sql query
     * @param rse result set extractor
     * @param args arguments for the query
     *
     * @return 1 object obtained from the query
     * 
     * @throws DataAccessException 
     * @throws ConsistencyErrorException if the query returns more objects than 1
     * @throws InternalErrorException if the query returns empty object
     * @throws NullPointerException if the query returns object with type NULL
     * 
     */
    public <T extends Object> T queryForObject(String sql, ResultSetExtractor<T> rse, Object... args) throws DataAccessException, NullPointerException, ConsistencyErrorException, InternalErrorException {

        List<T> result = (List) jdbc.query(sql, rse, args);

        if (result.size() > 1) {
            throw new ConsistencyErrorException("Query \"" + sql + "\" with parameters " + Arrays.toString(args) + " returned more than 1 object.");
        } else if (result.isEmpty()) {
            throw new InternalErrorException("Query \"" + sql + "\" with parameters " + Arrays.toString(args) + " returned no object.");
        }

        if (result.get(0) != null) {
            // return correct data
            return result.get(0);
        } else {
            throw new NullPointerException("Object returned by query \"" + sql + "\" with parameters " + Arrays.toString(args) + " is null.");
        }
    }

}
