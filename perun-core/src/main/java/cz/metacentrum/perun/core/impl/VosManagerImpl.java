package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	private final JdbcPerunTemplate jdbc;

	protected final static String voMappingSelectQuery = "vos.id as vos_id,vos.name as vos_name, vos.short_name as vos_short_name, " +
		"vos.created_at as vos_created_at, vos.created_by as vos_created_by, vos.modified_by as vos_modified_by, vos.modified_at as vos_modified_at, " +
		"vos.created_by_uid as vos_created_by_uid, vos.modified_by_uid as vos_modified_by_uid";


	/**
	 * Converts s ResultSet's row to a Vo instance.
	 */
	protected static final RowMapper<Vo> VO_MAPPER = (resultSet, i) ->
		new Vo(resultSet.getInt("vos_id"), resultSet.getString("vos_name"), resultSet.getString("vos_short_name"), resultSet.getString("vos_created_at"),
			resultSet.getString("vos_created_by"), resultSet.getString("vos_modified_at"), resultSet.getString("vos_modified_by"),
			resultSet.getInt("vos_created_by_uid") == 0 ? null : resultSet.getInt("vos_created_by_uid"),
			resultSet.getInt("vos_modified_by_uid") == 0 ? null : resultSet.getInt("vos_modified_by_uid"));

	/**
	 * Constructor.
	 *
	 * @param perunPool connection pool instance
	 */
	public VosManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
	}

	@Override
	public List<Vo> getVos(PerunSession sess) throws InternalErrorException {
		try {
			List<Vo> list = jdbc.query("select " + voMappingSelectQuery + " from vos", VO_MAPPER);
			if (list == null) return new ArrayList<>();

			return list;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Vo getVoByShortName(PerunSession sess, String shortName) throws VoNotExistsException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + voMappingSelectQuery + " from vos where short_name=?", VO_MAPPER, shortName);
		} catch (EmptyResultDataAccessException ex) {
			throw new VoNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Vo getVoById(PerunSession sess, int id) throws VoNotExistsException, InternalErrorException {
		try {
			return jdbc.queryForObject("select " + voMappingSelectQuery + " from vos where id=?", VO_MAPPER, id);
		} catch(EmptyResultDataAccessException ex) {
			throw new VoNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException, InternalErrorException {
		Utils.notNull(vo.getName(), "vo.getName()");
		Utils.notNull(vo.getShortName(), "vo.getShortName()");


		// Check if the Vo already exists (first check by ID, second by shortName attribute
		if (this.voExists(sess, vo)) throw new VoExistsException(vo.toString());
		if (this.shortNameForVoExists(sess, vo)) throw new VoExistsException(vo.toString());

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

	@Override
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

	@Override
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
	public List<User> getAdmins(PerunSession sess, Vo vo, Role role) throws InternalErrorException {
		try {
			// direct admins
			Set<User> setOfAdmins = new HashSet<>(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id " +
				"where authz.vo_id=? and authz.role_id=(select id from roles where name=?)", UsersManagerImpl.USER_MAPPER, vo.getId(), role.getRoleName()));

			// admins through a group
			List<Group> listOfGroupAdmins = getAdminGroups(sess, vo, role);
			for(Group group : listOfGroupAdmins) {
				setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from users join members on users.id=members.user_id " +
							"join groups_members on groups_members.member_id=members.id where groups_members.group_id=?", UsersManagerImpl.USER_MAPPER, group.getId()));
			}

			return new ArrayList(setOfAdmins);

		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<User> getDirectAdmins(PerunSession sess, Vo vo, Role role) throws InternalErrorException {
		try {
			return jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id " +
					"where authz.vo_id=? and authz.role_id=(select id from roles where name=?)", UsersManagerImpl.USER_MAPPER, vo.getId(), role.getRoleName());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Vo vo, Role role) throws InternalErrorException {
		try {
			return jdbc.query("select " + GroupsManagerImpl.groupMappingSelectQuery + " from authz join groups on "
					+ "authz.authorized_group_id=groups.id where authz.vo_id=? and authz.role_id=(select id from roles where name=?)",
					GroupsManagerImpl.GROUP_MAPPER, vo.getId(), role.getRoleName());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Deprecated
	@Override
	public List<User> getAdmins(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			// direct admins
			Set<User> setOfAdmins = new HashSet<>(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id " +
				"where authz.vo_id=? and authz.role_id=(select id from roles where name='voadmin')", UsersManagerImpl.USER_MAPPER, vo.getId()));

			// admins through a group
			List<Group> listOfGroupAdmins = getAdminGroups(sess, vo);
			for(Group group : listOfGroupAdmins) {
				setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from users join members on users.id=members.user_id " +
							"join groups_members on groups_members.member_id=members.id where groups_members.group_id=?", UsersManagerImpl.USER_MAPPER, group.getId()));
			}

			return new ArrayList(setOfAdmins);

		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Deprecated
	@Override
	public List<User> getDirectAdmins(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id " +
					"where authz.vo_id=? and authz.role_id=(select id from roles where name='voadmin')", UsersManagerImpl.USER_MAPPER, vo.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Deprecated
	@Override
	public List<Group> getAdminGroups(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.query("select " + GroupsManagerImpl.groupMappingSelectQuery + " from authz join groups on "
					+ "authz.authorized_group_id=groups.id where authz.vo_id=? and authz.role_id=(select id from roles where name='voadmin')",
					GroupsManagerImpl.GROUP_MAPPER, vo.getId());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean voExists(PerunSession sess, Vo vo) throws InternalErrorException {
		Utils.notNull(vo, "vo");
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from vos where id=?", vo.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Vo " + vo + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean shortNameForVoExists(PerunSession sess, Vo vo) throws InternalErrorException {
		Utils.notNull(vo, "vo");
		try{
			int numberOfExistences = jdbc.queryForInt("select count(1) from vos where short_name=?", vo.getShortName());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Short name " + vo.getShortName() + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkVoExists(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException {
		if(!voExists(sess, vo)) throw new VoNotExistsException("Vo: " + vo);
	}

	@Override
	public List<Integer> getVoApplicationIds(PerunSession sess, Vo vo) throws InternalErrorException {
		// get app ids for all applications
		try {
			return jdbc.query("select id from application where vo_id=?",
				(resultSet, arg1) -> resultSet.getInt("id"),vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Pair<String, String>> getApplicationReservedLogins(Integer appId) throws InternalErrorException {
		try {
			return jdbc.query("select namespace,login from application_reserved_logins where app_id=?",
				(resultSet, arg1) -> new Pair<>(resultSet.getString("namespace"), resultSet.getString("login")), appId);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteVoReservedLogins(PerunSession sess, Vo vo) throws InternalErrorException {
		// remove all reserved logins first
		try {
			for (Integer appId : getVoApplicationIds(sess, vo)) {
				jdbc.update("delete from application_reserved_logins where app_id=?", appId);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void deleteVoApplicationForm(PerunSession sess, Vo vo) throws InternalErrorException {
		// form items + texts are deleted on cascade with form itself
		try {
			jdbc.update("delete from application_form where vo_id=?", vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void createApplicationForm(PerunSession sess, Vo vo) throws InternalErrorException {
		int id = Utils.getNewId(jdbc, "APPLICATION_FORM_ID_SEQ");
		try {
			jdbc.update("insert into application_form(id, vo_id) values (?,?)", id, vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public int getVosCount(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from vos");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}


}
