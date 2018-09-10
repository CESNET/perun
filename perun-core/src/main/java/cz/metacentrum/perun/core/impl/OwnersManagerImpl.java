package cz.metacentrum.perun.core.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.implApi.OwnersManagerImplApi;

/**
 * OwnersManager implementation.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class OwnersManagerImpl implements OwnersManagerImplApi {

	final static Logger log = LoggerFactory.getLogger(OwnersManagerImpl.class);

	private JdbcPerunTemplate jdbc;

	protected final static String ownerMappingSelectQuery = "owners.id as owners_id, owners.name as owners_name, owners.contact as owners_contact, owners.type as owners_type, " +
		"owners.created_at as owners_created_at, owners.created_by as owners_created_by, owners.modified_by as owners_modified_by, owners.modified_at as owners_modified_at, " +
		"owners.created_by_uid as owners_created_by_uid, owners.modified_by_uid as owners_modified_by_uid";


	/**
	 * Constructor.
	 *
	 * @param perunPool connection pool
	 */
	public OwnersManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
	}

	protected static final RowMapper<Owner> OWNER_MAPPER = new RowMapper<Owner>() {
		@Override
		public Owner mapRow(ResultSet rs, int i) throws SQLException {
			Owner owner = new Owner();
			owner.setId(rs.getInt("owners_id"));
			owner.setName(rs.getString("owners_name"));
			owner.setContact(rs.getString("owners_contact"));
			owner.setTypeByString(rs.getString("owners_type"));
			owner.setCreatedAt(rs.getString("owners_created_at"));
			owner.setCreatedBy(rs.getString("owners_created_by"));
			owner.setModifiedAt(rs.getString("owners_modified_at"));
			owner.setModifiedBy(rs.getString("owners_modified_by"));
			if(rs.getInt("owners_modified_by_uid") == 0) owner.setModifiedByUid(null);
			else owner.setModifiedByUid(rs.getInt("owners_modified_by_uid"));
			if(rs.getInt("owners_created_by_uid") == 0) owner.setCreatedByUid(null);
			else owner.setCreatedByUid(rs.getInt("owners_created_by_uid"));
			return owner;
		}
	};


	@Override
	public boolean ownerExists(PerunSession sess, Owner owner) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from owners where id=?", owner.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkOwnerExists(PerunSession sess, Owner owner) throws InternalErrorException, OwnerNotExistsException {
		if(!ownerExists(sess, owner)) throw new OwnerNotExistsException("Owner: " + owner);
	}

	@Override
	public Owner createOwner(PerunSession sess, Owner owner) throws InternalErrorException {
		Utils.notNull(owner.getName(), "owner.getName()");
		Utils.notNull(owner.getContact(), "owner.getContact()");
		Utils.notNull(owner.getType(), "owner.getType()");

		int newId = Utils.getNewId(jdbc, "owners_id_seq");

		try {
			jdbc.update("insert into owners(id, name, contact, type, created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
					"values (?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", newId, owner.getName(),
					owner.getContact(), owner.getType().toString(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
		owner.setId(newId);

		return owner;
	}

	@Override
	public void deleteOwner(PerunSession sess, Owner owner) throws InternalErrorException, OwnerAlreadyRemovedException {
		try {
			int numAffected = jdbc.update("delete from owners where id=?", owner.getId());
			if(numAffected == 0) throw new OwnerAlreadyRemovedException("Owner: " + owner);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Owner getOwnerById(PerunSession sess, int id) throws OwnerNotExistsException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + ownerMappingSelectQuery + " from owners where id=?", OWNER_MAPPER, id);
		} catch(EmptyResultDataAccessException ex) {
			throw new OwnerNotExistsException("Owner id=" + id, ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Owner> getOwners(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + ownerMappingSelectQuery + " from owners", OWNER_MAPPER);
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Owner>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}
}
