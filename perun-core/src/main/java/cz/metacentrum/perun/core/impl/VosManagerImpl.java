package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Group;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import java.util.HashSet;
import java.util.Set;

/**
 * VosManager implementation.
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class VosManagerImpl implements VosManagerImplApi {

	private final static Logger log = LoggerFactory.getLogger(VosManagerImpl.class);

	public static final String STEM = "virtual_organizations" ;
	public static final String VO_GROUPS_STEM = "groups";
	public static final String VO_SUBGROUPS_STEM = "subgroups";
	public static final String VO_RESOURCES_STEM = "resources";
	public static final String PERSON_TYPE = "person";

	// http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
	private JdbcTemplate jdbc;

	protected final static String voMappingSelectQuery = "vos.id as vos_id,vos.name as vos_name, vos.short_name as vos_short_name, " +
		"vos.created_at as vos_created_at, vos.created_by as vos_created_by, vos.modified_by as vos_modified_by, vos.modified_at as vos_modified_at, " +
		"vos.created_by_uid as vos_created_by_uid, vos.modified_by_uid as vos_modified_by_uid";


	/**
	 * Converts s ResultSet's row to a Vo instance.
	 */
	protected static final RowMapper<Vo> VO_MAPPER = new RowMapper<Vo>() {
		public Vo mapRow(ResultSet rs, int i) throws SQLException {
			return new Vo(rs.getInt("vos_id"), rs.getString("vos_name"), rs.getString("vos_short_name"), rs.getString("vos_created_at"),
					rs.getString("vos_created_by"), rs.getString("vos_modified_at"), rs.getString("vos_modified_by"),
					rs.getInt("vos_created_by_uid") == 0 ? null : rs.getInt("vos_created_by_uid"),
					rs.getInt("vos_modified_by_uid") == 0 ? null : rs.getInt("vos_modified_by_uid"));
		}
	};

	/**
	 * Constructor.
	 *
	 * @param perunPool connection pool instance
	 */
	public VosManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcTemplate(perunPool);
	}

	public List<Vo> getVos(PerunSession sess) throws InternalErrorException {
		try {
			List<Vo> list = jdbc.query("select " + voMappingSelectQuery + " from vos", VO_MAPPER);
			if (list == null) return new ArrayList<Vo>();

			return list;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Vo getVoByShortName(PerunSession sess, String shortName) throws VoNotExistsException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + voMappingSelectQuery + " from vos where short_name=?", VO_MAPPER, shortName);
		} catch (EmptyResultDataAccessException ex) {
			throw new VoNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Vo getVoById(PerunSession sess, int id) throws VoNotExistsException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + voMappingSelectQuery + " from vos where id=?", VO_MAPPER, id);
		} catch(EmptyResultDataAccessException ex) {
			throw new VoNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException, InternalErrorException {
		Utils.notNull(vo.getName(), "vo.getName()");
		Utils.notNull(vo.getShortName(), "vo.getShortName()");


		// Check if the Vo already exists
		if (this.voExists(sess, vo)) throw new VoExistsException(vo.toString());

		// Get VO ID
		int voId;
		try {
			voId = Utils.getNewId(jdbc, "vos_id_seq");
			jdbc.update("insert into vos(id, name, short_name, created_by,modified_by, created_by_uid, modified_by_uid) values (?,?,?,?,?,?,?)",
					voId, vo.getName(), vo.getShortName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		// set assigned id
		vo.setId(voId);

		return vo;
	}

	public void deleteVo(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			// Delete authz entries for this VO
			AuthzResolverBlImpl.removeAllAuthzForVo(sess, vo);

			if (jdbc.update("delete from vos where id=?", vo.getId()) == 0) {
				throw new ConsistencyErrorException("no record was deleted from the DB.");
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		log.debug("Vo {} deleted", vo);
	}

	public Vo updateVo(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			Map<String, Object> map = jdbc.queryForMap("select name, short_name from vos where id=?", vo.getId());

			if (!vo.getName().equals(map.get("name"))) {
				jdbc.update("update vos set name=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", vo.getName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), vo.getId());
			}

			return vo;
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsistencyErrorException("Updating non existing VO", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<User> getAdmins(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			Set<User> setOfAdmins = new HashSet<User>();
			// direct admins
			setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id " +
						"where authz.vo_id=? and authz.role_id=(select id from roles where name='voadmin')", UsersManagerImpl.USER_MAPPER, vo.getId()));

			// admins through a group
			List<Group> listOfGroupAdmins = getAdminGroups(sess, vo);
			for(Group group : listOfGroupAdmins) {
				setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from users join members on users.id=members.user_id " +
							"join groups_members on groups_members.member_id=members.id where groups_members.group_id=?", UsersManagerImpl.USER_MAPPER, group.getId()));
			}

			return new ArrayList(setOfAdmins);

		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<User>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<User> getDirectAdmins(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id " +
					"where authz.vo_id=? and authz.role_id=(select id from roles where name='voadmin')", UsersManagerImpl.USER_MAPPER, vo.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<User>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.query("select " + GroupsManagerImpl.groupMappingSelectQuery_compat + " from authz join groups on "
					+ "authz.authorized_group_id=groups.id " + GroupsManagerImpl.groupQNameJoinQuery + " where authz.vo_id=? and authz.role_id=(select id from roles where name='voadmin')",
					GroupsManagerImpl.GROUP_MAPPER, vo.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<Group>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean voExists(PerunSession sess, Vo vo) throws InternalErrorException {
		Utils.notNull(vo, "vo");
		try {
			return 1 == jdbc.queryForInt("select 1 from vos where id=?", vo.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void checkVoExists(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException {
		if(!voExists(sess, vo)) throw new VoNotExistsException("Vo: " + vo);
	}

	public List<Integer> getVoApplicationIds(PerunSession sess, Vo vo) {
		// get app ids for all applications
		return jdbc.query("select id from application where vo_id=?", new RowMapper<Integer>() {
			@Override
			public Integer mapRow(ResultSet rs, int arg1)
			throws SQLException {
			return rs.getInt("id");
			}
		},vo.getId());
	}

	public List<Pair<String, String>> getApplicationReservedLogins(Integer appId) {
		return jdbc.query("select namespace,login from application_reserved_logins where app_id=?", new RowMapper<Pair<String, String>>() {
			@Override
			public Pair<String, String> mapRow(ResultSet rs, int arg1) throws SQLException {
				return new Pair<String, String>(rs.getString("namespace"), rs.getString("login"));
			}
		}, appId);
	}

	public void deleteVoReservedLogins(PerunSession sess, Vo vo) {
		// remove all reserved logins first
		for (Integer appId : getVoApplicationIds(sess, vo)) {
			jdbc.update("delete from application_reserved_logins where app_id=?", appId);
		}
	}

	public void deleteVoApplicationForm(PerunSession sess, Vo vo) {
		// form items + texts are deleted on cascade with form itself
		jdbc.update("delete from application_form where vo_id=?", vo.getId());
	}

	public void createApplicationForm(PerunSession sess, Vo vo) throws InternalErrorException {
		int id = Utils.getNewId(jdbc, "APPLICATION_FORM_ID_SEQ");
		jdbc.update("insert into application_form(id, vo_id) values (?,?)", id, vo.getId());
	}

}
