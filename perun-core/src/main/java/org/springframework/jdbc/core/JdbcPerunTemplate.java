package org.springframework.jdbc.core;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
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

	public JdbcPerunTemplate(DataSource perunPool) {
		super(perunPool);
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
	 * @throws IncorrectResultSizeDataAccessException if more than one
	 * element has been found in the given Collection
	 * @throws EmptyResultDataAccessException if no element at all
	 * has been found in the given Collection
	 */
	public <T> T queryForObject(String sql, ResultSetExtractor<T> rse, Object... args) throws DataAccessException {
		List<T> results = (List<T>) this.query(sql, rse, args);
		return DataAccessUtils.requiredSingleResult(results);
	}

}
