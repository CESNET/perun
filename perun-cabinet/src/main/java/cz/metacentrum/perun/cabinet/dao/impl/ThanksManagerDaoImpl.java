package cz.metacentrum.perun.cabinet.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.dao.ThanksManagerDao;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.core.impl.Utils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

/**
 * Class of DAO layer for handling Thanks entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ThanksManagerDaoImpl implements ThanksManagerDao {

	private JdbcPerunTemplate jdbc;

	public ThanksManagerDaoImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	private final static String THANKS_SELECT_QUERY = "cabinet_thanks.id as thanks_id, " +
			"cabinet_thanks.ownerId as thanks_own_id, cabinet_thanks.publicationId as thanks_pub_id, " +
			" cabinet_thanks.createdBy as thanks_created_by, cabinet_thanks.created_by_uid as thanks_created_by_uid," +
			" cabinet_thanks.createdDate as thanks_created_at";

	protected final static String THANKS_FOR_GUI_SELECT_QUERY = THANKS_SELECT_QUERY + ",owners.name as thanks_own_name";

	private final static RowMapper<Thanks> THANKS_ROW_MAPPER = new RowMapper<Thanks>() {
		@Override
		public Thanks mapRow(ResultSet resultSet, int i) throws SQLException {
			Thanks thanks = new Thanks();
			thanks.setId(resultSet.getInt("thanks_id"));
			thanks.setOwnerId(resultSet.getInt("thanks_own_id"));
			thanks.setPublicationId(resultSet.getInt("thanks_pub_id"));
			thanks.setCreatedBy(resultSet.getString("thanks_created_by"));
			thanks.setCreatedByUid(resultSet.getInt("thanks_created_by_uid"));
			thanks.setCreatedDate(resultSet.getDate("thanks_created_at"));
			return thanks;
		}
	};

	protected final static RowMapper<ThanksForGUI> THANKS_FOR_GUI_ROW_MAPPER = new RowMapper<ThanksForGUI>() {
		@Override
		public ThanksForGUI mapRow(ResultSet resultSet, int i) throws SQLException {
			ThanksForGUI thanks = new ThanksForGUI(THANKS_ROW_MAPPER.mapRow(resultSet, i));
			thanks.setOwnerName(resultSet.getString("thanks_own_name"));
			return thanks;
		}
	};

	// methods ----------------------

	@Override
	public Thanks createThanks(PerunSession sess, Thanks thanks) throws InternalErrorException, CabinetException {
		try {
			// Set the new Thanks id
			int newId = Utils.getNewId(jdbc, "cabinet_thanks_id_seq");
			jdbc.update("insert into cabinet_thanks (id, ownerId, publicationId, createdBy, createdDate, created_by_uid, modified_by_uid)" +
							" values (?,?,?,?,"+ Compatibility.getSysdate()+",?,?)", newId, thanks.getOwnerId(), thanks.getPublicationId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			thanks.setId(newId);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
		return thanks;
	}

	@Override
	public void deleteThanks(PerunSession sess, Thanks thanks) throws InternalErrorException, CabinetException {
		try {
			int numAffected = jdbc.update("delete from cabinet_thanks where id=?", thanks.getId());
			if(numAffected == 0) throw new CabinetException(ErrorCodes.THANKS_NOT_EXISTS);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public boolean thanksExist(Thanks thanks) throws InternalErrorException {
		try {
			jdbc.queryForObject("select " + THANKS_SELECT_QUERY +
					" from cabinet_thanks where id=? or (ownerId=? and publicationId=?)",
					THANKS_ROW_MAPPER, thanks.getId(), thanks.getOwnerId(), thanks.getPublicationId());
			return true;
		} catch (EmptyResultDataAccessException ex) {
			return false;
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Thanks getThanksById(int id) throws CabinetException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + THANKS_SELECT_QUERY +
					" from cabinet_thanks where id=?", THANKS_ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.THANKS_NOT_EXISTS, ex);
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Thanks> getThanksByPublicationId(int publicationId) throws InternalErrorException {
		try {
			return jdbc.query("select " + THANKS_SELECT_QUERY +
					" from cabinet_thanks where publicationId=?", THANKS_ROW_MAPPER, publicationId);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<Thanks>();
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<ThanksForGUI> getRichThanksByPublicationId(int publicationId) throws InternalErrorException {
		try {
			return jdbc.query("select " + THANKS_FOR_GUI_SELECT_QUERY +
					" from cabinet_thanks" +
					" left outer join owners on cabinet_thanks.ownerId=owners.id" +
					" where publicationId=?", THANKS_FOR_GUI_ROW_MAPPER, publicationId);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<ThanksForGUI>();
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<ThanksForGUI> getRichThanksByUserId(int userId) throws InternalErrorException {
		try {
			return jdbc.query("select " + THANKS_FOR_GUI_SELECT_QUERY +
					" from cabinet_thanks" +
					" join owners on cabinet_thanks.ownerId=owners.id" +
					" where cabinet_thanks.publicationId in (select publicationId from cabinet_authorships where userId = ?)",
					THANKS_FOR_GUI_ROW_MAPPER, userId);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<ThanksForGUI>();
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

}
