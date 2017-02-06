package cz.metacentrum.perun.cabinet.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.dao.CategoryManagerDao;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Utils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

/**
 * Class of DAO layer for handling Category entity.
 * Provides connection to proper mapper.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class CategoryManagerDaoImpl implements CategoryManagerDao {

	private JdbcPerunTemplate jdbc;

	public CategoryManagerDaoImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
	}

	private final static String CATEGORY_SELECT_QUERY = "cabinet_categories.id as category_id, " +
			"cabinet_categories.name as category_name, cabinet_categories.rank as category_rank";

	private final static RowMapper<Category> CATEGORY_ROW_MAPPER = new RowMapper<Category>() {
		@Override
		public Category mapRow(ResultSet resultSet, int i) throws SQLException {
			Category category = new Category();
			category.setId(resultSet.getInt("category_id"));
			category.setName(resultSet.getString("category_name"));
			category.setRank(resultSet.getDouble("category_rank"));
			return category;
		}
	};

	// methods ----------------------

	@Override
	public Category createCategory(PerunSession sess, Category category) throws InternalErrorException, CabinetException {
		try {
			// Set the new Category id
			int newId = Utils.getNewId(jdbc, "cabinet_categories_id_seq");
			jdbc.update("insert into cabinet_categories (id, name, rank, created_by_uid, modified_by_uid)" +
							" values (?,?,?,?,?)", newId, category.getName(), category.getRank(),
					sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			category.setId(newId);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
		return category;
	}

	@Override
	public Category updateCategory(PerunSession sess, Category category) throws InternalErrorException, CabinetException {
		try {
			int numAffected = jdbc.update("update cabinet_categories set name=?,rank=?,modified_by_uid=?" +
					" where id=?", category.getName(), category.getRank(), sess.getPerunPrincipal().getUserId(), category.getId());
			if (numAffected == 0) throw new CabinetException(ErrorCodes.CATEGORY_NOT_EXISTS);
			if (numAffected > 1)
				throw new ConsistencyErrorException("There are multiple Categories with same id: " + category.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
		return category;
	}

	@Override
	public void deleteCategory(PerunSession sess, Category category) throws InternalErrorException, CabinetException {
		try {
			int numAffected = jdbc.update("delete from cabinet_categories where id=?", category.getId());
			if (numAffected == 0) throw new CabinetException(ErrorCodes.CATEGORY_NOT_EXISTS);
		} catch (DataIntegrityViolationException ex) {
			throw new CabinetException(ErrorCodes.CATEGORY_HAS_PUBLICATIONS, ex);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Category> getCategories() throws InternalErrorException {
		try {
			return jdbc.query("select " + CATEGORY_SELECT_QUERY +
					" from cabinet_categories", CATEGORY_ROW_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<Category>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Category getCategoryById(int id) throws CabinetException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + CATEGORY_SELECT_QUERY +
					" from cabinet_categories where id=?", CATEGORY_ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.CATEGORY_NOT_EXISTS, ex);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

}
