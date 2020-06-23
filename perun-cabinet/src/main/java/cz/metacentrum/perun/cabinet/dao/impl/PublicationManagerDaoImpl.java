package cz.metacentrum.perun.cabinet.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.dao.PublicationManagerDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.core.impl.Utils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

import static cz.metacentrum.perun.cabinet.dao.impl.AuthorshipManagerDaoImpl.AUTHOR_ROW_MAPPER;
import static cz.metacentrum.perun.cabinet.dao.impl.AuthorshipManagerDaoImpl.AUTHOR_SELECT_QUERY;
import static cz.metacentrum.perun.cabinet.dao.impl.ThanksManagerDaoImpl.THANKS_FOR_GUI_ROW_MAPPER;
import static cz.metacentrum.perun.cabinet.dao.impl.ThanksManagerDaoImpl.THANKS_FOR_GUI_SELECT_QUERY;

/**
 * Class of DAO layer for handling Publication entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PublicationManagerDaoImpl implements PublicationManagerDao {

	private JdbcPerunTemplate jdbc;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public PublicationManagerDaoImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
		this.namedParameterJdbcTemplate.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	private final static String PUBLICATION_SELECT_QUERY = "cabinet_publications.id as publication_id, " +
			"cabinet_publications.externalId as publication_externalId, cabinet_publications.publicationSystemId as publication_pubSystemId," +
			"cabinet_publications.title as publication_title, cabinet_publications.year as publication_year, cabinet_publications.main as publication_main, " +
			"cabinet_publications.isbn as publication_isbn, cabinet_publications.categoryId as publication_categoryId, " +
			"cabinet_publications.createdBy as publication_createdBy, cabinet_publications.created_by_uid as publication_created_by_uid, " +
			"cabinet_publications.modified_by_uid as publication_modified_by_uid, cabinet_publications.rank as publication_rank, " +
			"cabinet_publications.doi as publication_doi, cabinet_publications.locked as publication_locked, " +
			"cabinet_publications.createdDate as publication_createdDate";

	private final static String RICH_PUBLICATION_SELECT_QUERY = PUBLICATION_SELECT_QUERY + ", cabinet_publication_systems.friendlyName as ps_friendlyName, " +
			"cabinet_categories.name as category_name, " + AUTHOR_SELECT_QUERY + ", " + THANKS_FOR_GUI_SELECT_QUERY;

	private final static RowMapper<Publication> PUBLICATION_ROW_MAPPER = new RowMapper<Publication>() {
		@Override
		public Publication mapRow(ResultSet resultSet, int i) throws SQLException {

			Publication publication = new Publication();
			publication.setId(resultSet.getInt("publication_id"));
			publication.setExternalId(resultSet.getInt("publication_externalId"));
			publication.setPublicationSystemId(resultSet.getInt("publication_pubSystemId"));

			publication.setTitle(resultSet.getString("publication_title"));
			publication.setYear(resultSet.getInt("publication_year"));
			publication.setMain(resultSet.getString("publication_main"));
			publication.setIsbn(resultSet.getString("publication_isbn"));
			publication.setCategoryId(resultSet.getInt("publication_categoryId"));

			publication.setDoi(resultSet.getString("publication_doi"));
			publication.setRank(resultSet.getDouble("publication_rank"));
			publication.setLocked(resultSet.getBoolean("publication_locked"));

			publication.setCreatedBy(resultSet.getString("publication_createdBy"));
			publication.setCreatedByUid(resultSet.getInt("publication_created_by_uid"));
			publication.setCreatedDate(resultSet.getDate("publication_createdDate"));

			// TODO - modified by ??

			return publication;

		}
	};

	private final static ResultSetExtractor<List<PublicationForGUI>> PUBLICATION_ROW_EXTRACTOR = new ResultSetExtractor<List<PublicationForGUI>>() {
		@Override
		public List<PublicationForGUI> extractData(ResultSet resultSet) throws SQLException {

			Map<Integer, PublicationForGUI> publications = new HashMap<>();

			while (resultSet.next()) {

				PublicationForGUI publication = new PublicationForGUI(PUBLICATION_ROW_MAPPER.mapRow(resultSet, resultSet.getRow()));
				if (publications.get(publication.getId()) == null) {
					publications.put(publication.getId(), publication);
				}
				PublicationForGUI resultPub = publications.get(publication.getId());
				resultPub.setCategoryName(resultSet.getString("category_name"));
				resultPub.setPubSystemName(resultSet.getString("ps_friendlyName"));

				if (resultSet.getInt("users_id") != 0) {
					// if author is present in a row
					HashSet<Author> authors = new HashSet<Author>(resultPub.getAuthors());
					authors.add(AUTHOR_ROW_MAPPER.mapRow(resultSet, resultSet.getRow()));
					resultPub.setAuthors(new ArrayList<>(authors));
				}

				if (resultSet.getInt("thanks_id") != 0) {
					// if thanks is present in a row
					HashSet<ThanksForGUI> thanks = new HashSet<ThanksForGUI>(resultPub.getThanks());
					thanks.add(THANKS_FOR_GUI_ROW_MAPPER.mapRow(resultSet, resultSet.getRow()));
					resultPub.setThanks(new ArrayList<>(thanks));
				}

			}

			return new ArrayList<>(publications.values());

		}
	};

	// methods ----------------------


	@Override
	public Publication createPublication(PerunSession sess, Publication publication) {
		try {
			// Set the new Category id
			int newId = Utils.getNewId(jdbc, "cabinet_publications_id_seq");
			jdbc.update("insert into cabinet_publications (id, externalId, publicationSystemId, title, year, main," +
							" isbn, categoryId, createdBy, createdDate, rank, doi, locked, created_by_uid, modified_by_uid)" +
							" values (?,?,?,?,?,?,?,?,?,"+ Compatibility.getSysdate()+",?,?,?,?,?)",
					newId, (publication.getExternalId() == 0) ? newId : publication.getExternalId(), publication.getPublicationSystemId(),
					publication.getTitle(), publication.getYear(), publication.getMain(), publication.getIsbn(), publication.getCategoryId(),
					sess.getPerunPrincipal().getActor(), publication.getRank(), publication.getDoi(), (publication.getLocked()) ? 1 : 0,
					sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			publication.setId(newId);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
		return publication;
	}

	@Override
	public Publication updatePublication(PerunSession sess, Publication publication) throws CabinetException {
		try {
			int rows = jdbc.update("update cabinet_publications set title=?, year=?, main=?, isbn=?, categoryId=?, rank=?, doi=?"+
					" where id=?", publication.getTitle(), publication.getYear(), publication.getMain(), publication.getIsbn(),
					publication.getCategoryId(), publication.getRank(), publication.getDoi(), publication.getId());
			if(rows == 0) throw new CabinetException(ErrorCodes.PUBLICATION_NOT_EXISTS);
			if (rows > 1) throw new ConsistencyErrorException("There are multiple Publications with same id: " + publication.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
		return publication;
	}

	@Override
	public void deletePublication(Publication publication) throws CabinetException {
		try {
			int rows = jdbc.update("delete from cabinet_publications where id=?", publication.getId());
			if(rows == 0) throw new CabinetException(ErrorCodes.PUBLICATION_NOT_EXISTS);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Publication getPublicationById(int id) throws CabinetException {
		try {
			return jdbc.queryForObject("select " + PUBLICATION_SELECT_QUERY +
					" from cabinet_publications where id=?", PUBLICATION_ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.PUBLICATION_NOT_EXISTS, ex);
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Publication getPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException {
		try {
			return jdbc.queryForObject("select " + PUBLICATION_SELECT_QUERY +
					" from cabinet_publications where externalId=? and publicationSystemId=?",
					PUBLICATION_ROW_MAPPER, externalId, publicationSystem);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.PUBLICATION_NOT_EXISTS, ex);
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Publication> getPublicationsByCategoryId(int categoryId) {
		try {
			return jdbc.query("select " + PUBLICATION_SELECT_QUERY +
					" from cabinet_publications where categoryId=? order by cabinet_publications.year DESC", PUBLICATION_ROW_MAPPER, categoryId);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public PublicationForGUI getRichPublicationById(int id) throws CabinetException {
		try {
			return (PublicationForGUI) jdbc.queryForObject("select " + RICH_PUBLICATION_SELECT_QUERY +
					" from cabinet_publications " +
					" left outer join cabinet_publication_systems on cabinet_publications.publicationSystemId = cabinet_publication_systems.id" +
					" left outer join cabinet_categories on cabinet_publications.categoryId = cabinet_categories.id" +
					" left outer join cabinet_thanks on cabinet_publications.id = cabinet_thanks.publicationId" +
					" left outer join owners on cabinet_thanks.ownerId = owners.id" +
					" left outer join cabinet_authorships on cabinet_publications.id = cabinet_authorships.publicationId" +
					" left outer join users on cabinet_authorships.userId = users.id" +
					" where cabinet_publications.id=? order by cabinet_publications.year DESC", PUBLICATION_ROW_EXTRACTOR, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.PUBLICATION_NOT_EXISTS, ex);
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public PublicationForGUI getRichPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException {
		try {
			return (PublicationForGUI) jdbc.queryForObject("select " + RICH_PUBLICATION_SELECT_QUERY +
					" from cabinet_publications " +
					" left outer join cabinet_publication_systems on cabinet_publications.publicationSystemId = cabinet_publication_systems.id" +
					" left outer join cabinet_categories on cabinet_publications.categoryId = cabinet_categories.id" +
					" left outer join cabinet_thanks on cabinet_publications.id = cabinet_thanks.publicationId" +
					" left outer join owners on cabinet_thanks.ownerId = owners.id" +
					" left outer join cabinet_authorships on cabinet_publications.id = cabinet_authorships.publicationId" +
					" left outer join users on cabinet_authorships.userId = users.id" +
					" where cabinet_publications.externalId=? and cabinet_publications.publicationSystemId=?" +
					" order by cabinet_publications.year DESC",
					PUBLICATION_ROW_EXTRACTOR, externalId, publicationSystem);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.PUBLICATION_NOT_EXISTS, ex);
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<PublicationForGUI> getRichPublicationsByFilter(Publication p, int userId, int yearSince, int yearTill) {

		String select = "select " + RICH_PUBLICATION_SELECT_QUERY + " from cabinet_publications " +
				" left outer join cabinet_publication_systems on cabinet_publications.publicationSystemId = cabinet_publication_systems.id" +
				" left outer join cabinet_categories on cabinet_publications.categoryId = cabinet_categories.id" +
				" left outer join cabinet_thanks on cabinet_publications.id = cabinet_thanks.publicationId" +
				" left outer join owners on cabinet_thanks.ownerId = owners.id" +
				" left outer join cabinet_authorships on cabinet_publications.id = cabinet_authorships.publicationId" +
				" left outer join users on cabinet_authorships.userId = users.id" +
				" where ";

		List<Object> params = new ArrayList<Object>() {};
		boolean first = true;

		if (p != null) {
			if (p.getId() != 0) {
				select = select + "cabinet_publications.id = ?";
				params.add(p.getId());
				first = false;
			}
			if (p.getYear() != 0) {
				if (!first) select = select + " and ";
				select = select + "cabinet_publications.year = ?";
				params.add(p.getYear());
				first = false;
			}
			if (p.getCategoryId() != 0) {
				if (!first) select = select + " and ";
				select = select + "cabinet_publications.categoryId = ?";
				params.add(p.getCategoryId());
				first = false;
			}
			if (p.getTitle() != null && !p.getTitle().isEmpty()) {
				if (!first) select = select + " and ";
				select = select + "LOWER(cabinet_publications.title) like ?";
				params.add("%"+p.getTitle().toLowerCase()+"%");
				first = false;
			}
			if (p.getIsbn() != null && !p.getIsbn().isEmpty()) {
				if (!first) select = select + " and ";
				select = select + "LOWER(cabinet_publications.isbn) like ?";
				params.add("%"+p.getIsbn().toLowerCase()+"%");
				first = false;
			}
			if (p.getRank() != 0.0) {
				if (!first) select = select + " and ";
				select = select + "cabinet_publications.rank = ?";
				params.add(p.getRank());
				first = false;
			}
			if (p.getDoi() != null && !p.getDoi().isEmpty()) {
				if (!first) select = select + " and ";
				select = select + "LOWER(cabinet_publications.doi) like ?";
				params.add("%"+p.getDoi().toLowerCase()+"%");
				first = false;
			}
		}

		// process rest of params
		if (yearSince != 0 && yearTill == 0) {
			if (!first) select = select + " and ";
			select = select + "cabinet_publications.year >= ?";
			params.add(yearSince);
			first = false;
		}
		if (yearTill != 0 && yearSince == 0) {
			if (!first) select = select + " and ";
			select = select + "cabinet_publications.year <= ?";
			params.add(yearTill);
			first = false;
		}

		if (yearSince != 0 && yearTill != 0) {
			if (!first) select = select + " and ";
			select = select + "(cabinet_publications.year between ? and ?)";
			params.add(yearSince);
			params.add(yearTill);
			first = false;
		}

		if (userId != 0) {
			if (!first) select = select + " and ";
			select = select + "users.id = ?";
			params.add(userId);
			first = false;
		}

		if (first) select = select + " 1=1 ";

		try {
			return jdbc.query(select + " order by cabinet_publications.year DESC", PUBLICATION_ROW_EXTRACTOR, params.toArray());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public List<Publication> getPublicationsByFilter(int userId, int yearSince, int yearTill) {

		String select = "select " + PUBLICATION_SELECT_QUERY + " from cabinet_publications " +
				" left outer join cabinet_authorships on cabinet_publications.id = cabinet_authorships.publicationId" +
				" left outer join users on cabinet_authorships.userId = users.id" +
				" where ";

		List<Object> params = new ArrayList<Object>() {};
		boolean first = true;

		// only year since
		if (yearSince > 0 && yearTill < 1) {
			if (!first) select = select + " and ";
			select = select + "cabinet_publications.year >= ?";
			params.add(yearSince);
			first = false;
		}
		// only year till
		if (yearTill > 0 && yearSince < 1) {
			if (!first) select = select + " and ";
			select = select + "cabinet_publications.year <= ?";
			params.add(yearTill);
			first = false;
		}
		// both dates
		if (yearSince > 0 && yearTill > 0) {
			if (!first) select = select + " and ";
			select = select + "(cabinet_publications.year between ? and ?)";
			params.add(yearSince);
			params.add(yearTill);
			first = false;
		}

		if (userId > 0) {
			if (!first) select = select + " and ";
			select = select + "users.id = ?";
			params.add(userId);
			first = false;
		}

		if (first) select = select + " 1=1 ";

		try {
			return jdbc.query(select + " order by cabinet_publications.year DESC", PUBLICATION_ROW_MAPPER, params.toArray());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public void lockPublications(boolean lockState, List<Publication> pubs) {

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		Set<Integer> pubIds = new HashSet<Integer>();
		for (Publication pub : pubs) {
			pubIds.add(pub.getId());
		}
		parameters.addValue("ids", pubIds);
		parameters.addValue("lock", lockState ? 1 : 0);

		try {
			namedParameterJdbcTemplate.update("update cabinet_publications set locked=:lock where id in (:ids)", parameters);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

}
