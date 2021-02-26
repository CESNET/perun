package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.ResultSet;
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

	protected final static String banOnVoMappingSelectQuery = "vos_bans.id as vos_bans_id, vos_bans.description as vos_bans_description, " +
			"vos_bans.member_id as vos_bans_member_id, vos_bans.vo_id as vos_bans_vo_id, vos_bans.banned_to as vos_bans_validity_to, " +
			"vos_bans.created_at as vos_bans_created_at, vos_bans.created_by as vos_bans_created_by, vos_bans.modified_at as vos_bans_modified_at, " +
			"vos_bans.modified_by as vos_bans_modified_by, vos_bans.created_by_uid as vos_bans_created_by_uid, vos_bans.modified_by_uid as vos_bans_modified_by_uid";


	/**
	 * Converts s ResultSet's row to a Vo instance.
	 */
	protected static final RowMapper<Vo> VO_MAPPER = (resultSet, i) ->
		new Vo(resultSet.getInt("vos_id"), resultSet.getString("vos_name"), resultSet.getString("vos_short_name"), resultSet.getString("vos_created_at"),
			resultSet.getString("vos_created_by"), resultSet.getString("vos_modified_at"), resultSet.getString("vos_modified_by"),
			resultSet.getInt("vos_created_by_uid") == 0 ? null : resultSet.getInt("vos_created_by_uid"),
			resultSet.getInt("vos_modified_by_uid") == 0 ? null : resultSet.getInt("vos_modified_by_uid"));

	protected static final RowMapper<BanOnVo> BAN_ON_VO_MAPPER = (resultSet, i) -> {
		BanOnVo banOnVo = new BanOnVo();
		banOnVo.setId(resultSet.getInt("vos_bans_id"));
		banOnVo.setMemberId(resultSet.getInt("vos_bans_member_id"));
		banOnVo.setVoId(resultSet.getInt("vos_bans_vo_id"));
		banOnVo.setDescription(resultSet.getString("vos_bans_description"));
		banOnVo.setValidityTo(resultSet.getTimestamp("vos_bans_validity_to"));
		banOnVo.setCreatedAt(resultSet.getString("vos_bans_created_at"));
		banOnVo.setCreatedBy(resultSet.getString("vos_bans_created_by"));
		banOnVo.setModifiedAt(resultSet.getString("vos_bans_modified_at"));
		banOnVo.setModifiedBy(resultSet.getString("vos_bans_modified_by"));
		if(resultSet.getInt("vos_bans_modified_by_uid") == 0) banOnVo.setModifiedByUid(null);
		else banOnVo.setModifiedByUid(resultSet.getInt("vos_bans_modified_by_uid"));
		if(resultSet.getInt("vos_bans_created_by_uid") == 0) banOnVo.setCreatedByUid(null);
		else banOnVo.setCreatedByUid(resultSet.getInt("vos_bans_created_by_uid"));
		return banOnVo;
	};

	/**
	 * Constructor.
	 *
	 * @param perunPool connection pool instance
	 */
	public VosManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
		this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	@Override
	public List<Vo> getVos(PerunSession sess) {
		try {
			return jdbc.query("select " + voMappingSelectQuery + " from vos", VO_MAPPER);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Vo getVoByShortName(PerunSession sess, String shortName) throws VoNotExistsException {
		try {
			return jdbc.queryForObject("select " + voMappingSelectQuery + " from vos where short_name=?", VO_MAPPER, shortName);
		} catch (EmptyResultDataAccessException ex) {
			throw new VoNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Vo getVoById(PerunSession sess, int id) throws VoNotExistsException {
		try {
			return jdbc.queryForObject("select " + voMappingSelectQuery + " from vos where id=?", VO_MAPPER, id);
		} catch(EmptyResultDataAccessException ex) {
			throw new VoNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Vo> getVosByIds(PerunSession perunSession, List<Integer> ids) {
		try {
			return jdbc.execute("select " + voMappingSelectQuery + " from vos where id " + Compatibility.getStructureForInClause(),
				(PreparedStatementCallback<List<Vo>>) preparedStatement -> {
					Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbersFromIntegers(ids, preparedStatement);
					preparedStatement.setArray(1, sqlArray);
					ResultSet rs = preparedStatement.executeQuery();
					List<Vo> vos = new ArrayList<>();
					while (rs.next()) {
						vos.add(VO_MAPPER.mapRow(rs, rs.getRow()));
					}
					return vos;
				});
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException {
		Utils.notNull(vo.getName(), "vo.getName()");
		Utils.notNull(vo.getShortName(), "vo.getShortName()");


		// Check if the Vo already exists (first check by ID, second by shortName attribute
		if (this.voExists(sess, vo)) throw new VoExistsException(vo.toString());
		if (this.shortNameForVoExists(sess, vo)) throw new VoExistsException(vo.toString());

		try {
			// Get VO ID
			return jdbc.queryForObject("insert into vos(id, name, short_name, created_by,modified_by, created_by_uid, modified_by_uid) values (nextval('vos_id_seq'),?,?,?,?,?,?) returning " + voMappingSelectQuery,
					VO_MAPPER, vo.getName(), vo.getShortName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteVo(PerunSession sess, Vo vo) {
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
	public Vo updateVo(PerunSession sess, Vo vo) {
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
	public List<User> getAdmins(PerunSession sess, Vo vo, String role) {
		try {
			// direct admins
			Set<User> setOfAdmins = new HashSet<>(jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id " +
				"where authz.vo_id=? and authz.role_id=(select id from roles where name=?)", UsersManagerImpl.USER_MAPPER, vo.getId(), role.toLowerCase()));

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
	public List<User> getDirectAdmins(PerunSession sess, Vo vo, String role) {
		try {
			return jdbc.query("select " + UsersManagerImpl.userMappingSelectQuery + " from authz join users on authz.user_id=users.id " +
					"where authz.vo_id=? and authz.role_id=(select id from roles where name=?)", UsersManagerImpl.USER_MAPPER, vo.getId(), role.toLowerCase());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Vo vo, String role) {
		try {
			return jdbc.query("select " + GroupsManagerImpl.groupMappingSelectQuery + " from authz join groups on "
					+ "authz.authorized_group_id=groups.id where authz.vo_id=? and authz.role_id=(select id from roles where name=?)",
					GroupsManagerImpl.GROUP_MAPPER, vo.getId(), role.toLowerCase());
		} catch(EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		}   catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Deprecated
	@Override
	public List<User> getAdmins(PerunSession sess, Vo vo) {
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
	public List<User> getDirectAdmins(PerunSession sess, Vo vo) {
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
	public List<Group> getAdminGroups(PerunSession sess, Vo vo) {
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
	public boolean voExists(PerunSession sess, Vo vo) {
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

	public boolean shortNameForVoExists(PerunSession sess, Vo vo) {
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
	public void checkVoExists(PerunSession sess, Vo vo) throws VoNotExistsException {
		if(!voExists(sess, vo)) throw new VoNotExistsException("Vo: " + vo);
	}

	@Override
	public List<Integer> getVoApplicationIds(PerunSession sess, Vo vo) {
		// get app ids for all applications
		try {
			return jdbc.query("select id from application where vo_id=?",
				(resultSet, arg1) -> resultSet.getInt("id"),vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Pair<String, String>> getApplicationReservedLogins(Integer appId) {
		try {
			return jdbc.query("select namespace,login from application_reserved_logins where app_id=?",
				(resultSet, arg1) -> new Pair<>(resultSet.getString("namespace"), resultSet.getString("login")), appId);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteVoReservedLogins(PerunSession sess, Vo vo) {
		// remove all reserved logins first
		try {
			for (Integer appId : getVoApplicationIds(sess, vo)) {
				jdbc.update("delete from application_reserved_logins where app_id=?", appId);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void deleteVoApplicationForm(PerunSession sess, Vo vo) {
		// form items + texts are deleted on cascade with form itself
		try {
			jdbc.update("delete from application_form where vo_id=?", vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void createApplicationForm(PerunSession sess, Vo vo) {
		int id = Utils.getNewId(jdbc, "APPLICATION_FORM_ID_SEQ");
		try {
			jdbc.update("insert into application_form(id, vo_id) values (?,?)", id, vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public int getVosCount(PerunSession sess) {
		try {
			return jdbc.queryForInt("select count(*) from vos");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnVo setBan(PerunSession sess, BanOnVo banOnVo) {
		Utils.notNull(banOnVo.getValidityTo(), "banOnVo.getValidityTo");
		try {
			int newId = Utils.getNewId(jdbc, "vos_bans_id_seq");

			jdbc.update("insert into vos_bans(" +
							"id, " +
							"description, " +
							"banned_to, " +
							"member_id, " +
							"vo_id, " +
							"modified_by, " +
							"created_by, " +
							"created_by_uid, " +
							"modified_by_uid) values (?,?,?,?,?,?,?,?,?)",
					newId,
					banOnVo.getDescription(),
					Compatibility.getDate(banOnVo.getValidityTo().getTime()),
					banOnVo.getMemberId(),
					banOnVo.getVoId(),
					sess.getPerunPrincipal().getActor(),
					sess.getPerunPrincipal().getActor(),
					sess.getPerunPrincipal().getUserId(),
					sess.getPerunPrincipal().getUserId()
			);

			banOnVo.setId(newId);

			return banOnVo;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnVo getBanById(PerunSession sess, int banId) throws BanNotExistsException {
		try {
			return jdbc.queryForObject("select " + banOnVoMappingSelectQuery + " from vos_bans where id=? ",
					BAN_ON_VO_MAPPER, banId);
		} catch (EmptyResultDataAccessException ex) {
			throw new BanNotExistsException("Ban with id " + banId + " not exists for any vo.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<BanOnVo> getBansForVo(PerunSession sess, int voId) {
		try {
			return jdbc.query("select " + banOnVoMappingSelectQuery + " from vos_bans where vo_id=?",
					BAN_ON_VO_MAPPER, voId);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnVo getBanForMember(PerunSession sess, int memberId) throws BanNotExistsException {
		try {
			return jdbc.queryForObject("select " + banOnVoMappingSelectQuery + " from vos_bans where member_id=? ",
					BAN_ON_VO_MAPPER, memberId);
		} catch (EmptyResultDataAccessException ex) {
			throw new BanNotExistsException("Ban for member with id " + memberId + " does not exist.");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public BanOnVo updateBan(PerunSession sess, BanOnVo banOnVo) {
		try {
			jdbc.update("UPDATE vos_bans SET " +
							"description=?, " +
							"banned_to=?, " +
							"modified_by=?, " +
							"modified_by_uid=?, " +
							"modified_at= "+ Compatibility.getSysdate() +
							" WHERE id=?",
					banOnVo.getDescription(),
					Compatibility.getDate(banOnVo.getValidityTo().getTime()),
					sess.getPerunPrincipal().getActor(),
					sess.getPerunPrincipal().getUserId(),
					banOnVo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}

		return banOnVo;
	}

	@Override
	public void removeBan(PerunSession sess, int banId) throws BanNotExistsException {
		try {
			int numAffected = jdbc.update("delete from vos_bans where id=?", banId);
			if (numAffected != 1) {
				throw new BanNotExistsException("Ban with id " + banId + " can't be remove, because not exists yet.");
			}
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean isMemberBanned(PerunSession sess, int memberId) {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from vos_bans where member_id=?", memberId);
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Ban on member with ID=" + memberId + " exists more than once.");
			}
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}
}
