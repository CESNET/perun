package cz.metacentrum.perun.cabinet.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.cabinet.dao.PublicationSystemManagerDao;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Utils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

/**
 * DAO layer for handling PublicationSystem entity.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PublicationSystemManagerDaoImpl implements PublicationSystemManagerDao {

	private JdbcPerunTemplate jdbc;

	public PublicationSystemManagerDaoImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
	}

	private final static String PUBLICATION_SYSTEM_SELECT_QUERY = "cabinet_publication_systems.id as ps_id, " +
			"cabinet_publication_systems.friendlyName as ps_friendlyName, cabinet_publication_systems.type as ps_type, " +
			" cabinet_publication_systems.url as ps_url, cabinet_publication_systems.username as ps_username," +
			" cabinet_publication_systems.password as ps_password, cabinet_publication_systems.loginNamespace as ps_loginNamespace";

	private final static RowMapper<PublicationSystem> PUBLICATION_SYSTEM_ROW_MAPPER = new RowMapper<PublicationSystem>() {
		@Override
		public PublicationSystem mapRow(ResultSet resultSet, int i) throws SQLException {
			PublicationSystem ps = new PublicationSystem();
			ps.setId(resultSet.getInt("ps_id"));
			ps.setFriendlyName(resultSet.getString("ps_friendlyName"));
			ps.setType(resultSet.getString("ps_type"));
			ps.setUrl(resultSet.getString("ps_url"));
			ps.setUsername(resultSet.getString("ps_username"));
			ps.setPassword(resultSet.getString("ps_password"));
			ps.setLoginNamespace(resultSet.getString("ps_loginNamespace"));
			return ps;
		}
	};

	// methods ------------------------------

	@Override
	public PublicationSystem createPublicationSystem(PerunSession session, PublicationSystem ps) throws InternalErrorException {
		try {
			// Set the new PS id
			int newId = Utils.getNewId(jdbc, "cabinet_pub_sys_id_seq");
			jdbc.update("insert into cabinet_publication_systems (id, friendlyName, type, url, username, password, loginNamespace, created_by_uid, modified_by_uid)" +
					" values (?,?,?,?,?,?,?,?,?)", newId, ps.getFriendlyName(), ps.getType(), ps.getUrl(), ps.getUsername(), ps.getPassword(),
					ps.getLoginNamespace(), session.getPerunPrincipal().getUserId(), session.getPerunPrincipal().getUserId());
			ps.setId(newId);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
		return ps;
	}

	@Override
	public PublicationSystem updatePublicationSystem(PerunSession session, PublicationSystem ps) throws CabinetException, InternalErrorException {
		try {
			int numAffected = jdbc.update("update cabinet_publication_systems set friendlyName=?,type=?,url=?,loginNamespace=?,modified_by_uid=?" +
					" where id=?", ps.getFriendlyName(), ps.getType(), ps.getUrl(), ps.getLoginNamespace(), session.getPerunPrincipal().getUserId(), ps.getId());
			if(numAffected == 0) throw new CabinetException(ErrorCodes.PUBLICATION_SYSTEM_NOT_EXISTS);
			if (numAffected > 1) throw new ConsistencyErrorException("There are multiple PS with same id: " + ps.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
		return ps;
	}

	@Override
	public void deletePublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException {
		try {
			int numAffected = jdbc.update("delete from cabinet_publication_systems where id=?", ps.getId());
			if(numAffected == 0) throw new CabinetException(ErrorCodes.PUBLICATION_SYSTEM_NOT_EXISTS);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<PublicationSystem> getPublicationSystems() throws InternalErrorException {
		try {
			return jdbc.query("select " + PUBLICATION_SYSTEM_SELECT_QUERY +
					" from cabinet_publication_systems", PUBLICATION_SYSTEM_ROW_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<PublicationSystem>();
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public PublicationSystem getPublicationSystemById(int id) throws CabinetException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + PUBLICATION_SYSTEM_SELECT_QUERY +
							" from cabinet_publication_systems where id=?", PUBLICATION_SYSTEM_ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.PUBLICATION_SYSTEM_NOT_EXISTS, ex);
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public PublicationSystem getPublicationSystemByName(String name) throws CabinetException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + PUBLICATION_SYSTEM_SELECT_QUERY +
							" from cabinet_publication_systems where friendlyName=?", PUBLICATION_SYSTEM_ROW_MAPPER, name);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.PUBLICATION_SYSTEM_NOT_EXISTS, ex);
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public PublicationSystem getPublicationSystemByNamespace(String namespace) throws CabinetException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + PUBLICATION_SYSTEM_SELECT_QUERY +
					" from cabinet_publication_systems where loginNamespace=?", PUBLICATION_SYSTEM_ROW_MAPPER, namespace);
		} catch (EmptyResultDataAccessException ex) {
			throw new CabinetException(ErrorCodes.PUBLICATION_SYSTEM_NOT_EXISTS, ex);
		}   catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

}
