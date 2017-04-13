package org.springframework.jdbc.core;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;

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
	 * @param rse result set extractor that returns List of objects
	 * @param args arguments for the query
	 *
	 * @return the single object obtained from the query
	 *
	 * @throws IncorrectResultSizeDataAccessException if more than one element has been found in the given Collection
	 * @throws EmptyResultDataAccessException if no element at all has been found in the given Collection
	 */
	public <T> T queryForObject(String sql, ResultSetExtractor<? extends List<T>> rse, Object... args) throws DataAccessException {
		return DataAccessUtils.requiredSingleResult(this.query(sql, rse, args));
	}

	/**
	 * Implements a method removed from spring-jdbc
	 * @param sql SQL query
	 * @param args arguments for the SQL query
	 * @return result of the query or zero if null
	 */
	public int queryForInt(String sql,Object... args) {
		Integer i = this.queryForObject(sql, Integer.class, args);
		return i==null?0:i;
	}
}
