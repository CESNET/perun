package cz.metacentrum.perun.cabinet.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.dao.AuthorshipManagerDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.core.impl.Utils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

/**
 * Class of DAO layer for handling Authorship entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class AuthorshipManagerDaoImpl implements AuthorshipManagerDao {

	private JdbcPerunTemplate jdbc;

	private final static String AUTHORSHIP_SELECT_QUERY = "cabinet_authorships.id as authorship_id, " +
			"cabinet_authorships.userId as authorship_user_id, cabinet_authorships.publicationId as authorship_publication_id," +
			"cabinet_authorships.createdBy as authorship_created_by, cabinet_authorships.createdDate as authorship_created_date," +
			"cabinet_authorships.created_by_uid as authorship_created_by_uid, cabinet_authorships.modified_by_uid as authorship_modified_by_uid";

	private final static RowMapper<Authorship> AUTHORSHIP_ROW_MAPPER = new RowMapper<Authorship>() {
		@Override
		public Authorship mapRow(ResultSet resultSet, int i) throws SQLException {
			Authorship authorship = new Authorship();
			authorship.setId(resultSet.getInt("authorship_id"));
			authorship.setUserId(resultSet.getInt("authorship_user_id"));
			authorship.setPublicationId(resultSet.getInt("authorship_publication_id"));
			authorship.setCreatedBy(resultSet.getString("authorship_created_by"));
			authorship.setCreatedDate(resultSet.getDate("authorship_created_date"));
			authorship.setCreatedByUid(resultSet.getInt("authorship_created_by_uid"));
			// TODO - modified_by_uid ??
			return authorship;
		}
	};

	protected final static String AUTHOR_SELECT_QUERY = "users.id as users_id, users.first_name as users_first_name, users.last_name as users_last_name, " +
			"users.middle_name as users_middle_name, users.title_before as users_title_before, users.title_after as users_title_after, " +
			AUTHORSHIP_SELECT_QUERY;

	protected final static RowMapper<Author> AUTHOR_ROW_MAPPER = new RowMapper<Author>() {
		@Override
		public Author mapRow(ResultSet resultSet, int i) throws SQLException {
			Author author = new Author();
			author.setId(resultSet.getInt("users_id"));
			author.setFirstName(resultSet.getString("users_first_name"));
			author.setLastName(resultSet.getString("users_last_name"));
			author.setTitleBefore(resultSet.getString("users_title_before"));
			author.setTitleAfter(resultSet.getString("users_title_after"));
			return author;
		}
	};

	private final static ResultSetExtractor<List<Author>> AUTHOR_RESULT_SET_EXTRACTOR = new ResultSetExtractor<List<Author>>() {
		@Override
		public List<Author> extractData(ResultSet resultSet) throws SQLException {
			HashMap<Integer, Author> result = new HashMap<>();
			while (resultSet.next()) {
				Author author = AUTHOR_ROW_MAPPER.mapRow(resultSet, resultSet.getRow());
				if (!result.containsKey(author.getId())) {
					// new author
					result.put(author.getId(), author);
				}
				// add authorships
				result.get(author.getId()).getAuthorships().add(AUTHORSHIP_ROW_MAPPER.mapRow(resultSet, resultSet.getRow()));

				if (resultSet.getInt("authorship_id") != 0) {
					HashSet<Authorship> authorships = new HashSet<Authorship>(result.get(author.getId()).getAuthorships());
					authorships.add(AUTHORSHIP_ROW_MAPPER.mapRow(resultSet, resultSet.getRow()));
					result.get(author.getId()).setAuthorships(new ArrayList<>(authorships));
				}

			}
			return new ArrayList<Author>(result.values());
		}
	};


	public AuthorshipManagerDaoImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	// methods ----------------------

	@Override
	public Authorship createAuthorship(PerunSession sess, Authorship authorship) {
		try {
			// Set the new Authorship id
			int newId = Utils.getNewId(jdbc, "cabinet_authorships_id_seq");
			jdbc.update("insert into cabinet_authorships (id, userId, publicationId, createdBy, createdDate, created_by_uid, modified_by_uid)" +
							" values (?,?,?,?," + Compatibility.getSysdate() + ",?,?)", newId, authorship.getUserId(), authorship.getPublicationId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			authorship.setId(newId);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
		return authorship;
	}

	@Override
	public void deleteAuthorship(PerunSession sess, Authorship authorship) throws CabinetException {
		try {
			int numAffected = jdbc.update("delete from cabinet_authorships where id=?", authorship.getId());
			if (numAffected == 0) throw new CabinetException(ErrorCodes.AUTHORSHIP_NOT_EXISTS);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Authorship getAuthorshipById(int id) throws CabinetException {
		try {
			return jdbc.queryForObject("select " + AUTHORSHIP_SELECT_QUERY +
					" from cabinet_authorships where id=?", AUTHORSHIP_ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.AUTHORSHIP_NOT_EXISTS, ex);
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Authorship> getAuthorshipsByUserId(int id) {
		try {
			return jdbc.query("select " + AUTHORSHIP_SELECT_QUERY +
					" from cabinet_authorships where userId=?", AUTHORSHIP_ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<Authorship>();
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Authorship> getAuthorshipsByPublicationId(int id) {
		try {
			return jdbc.query("select " + AUTHORSHIP_SELECT_QUERY +
					" from cabinet_authorships where publicationId=?", AUTHORSHIP_ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<Authorship>();
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Authorship getAuthorshipByUserAndPublicationId(int userId, int publicationId) throws CabinetException {
		try {
			return jdbc.queryForObject("select " + AUTHORSHIP_SELECT_QUERY +
					" from cabinet_authorships where userId=? and publicationId=?", AUTHORSHIP_ROW_MAPPER, userId, publicationId);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.AUTHORSHIP_NOT_EXISTS, ex);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Author getAuthorById(int id) throws CabinetException {
		try {
			return (Author) jdbc.queryForObject("select " + AUTHOR_SELECT_QUERY +
					" from users" +
					" join cabinet_authorships on users.id=cabinet_authorships.userId" +
					" and users.id=?", AUTHOR_RESULT_SET_EXTRACTOR, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.AUTHOR_NOT_EXISTS, ex);
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Author> getAllAuthors() {
		try {
			return jdbc.query("select " + AUTHOR_SELECT_QUERY +
					" from users" +
					" join cabinet_authorships on users.id=cabinet_authorships.userId" +
					" order by users_last_name, users_first_name", AUTHOR_RESULT_SET_EXTRACTOR);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Author> getAuthorsByPublicationId(int id) {
		try {
			return jdbc.query("select " + AUTHOR_SELECT_QUERY +
					" from users" +
					" join cabinet_authorships on users.id=cabinet_authorships.userId" +
					" and cabinet_authorships.publicationId=?" +
					" order by users_last_name, users_first_name", AUTHOR_RESULT_SET_EXTRACTOR, id);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

}
