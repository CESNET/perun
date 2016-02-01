package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;
import java.util.HashSet;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;

public class AuthzResolverImpl implements AuthzResolverImplApi {

	final static Logger log = LoggerFactory.getLogger(FacilitiesManagerImpl.class);

	//http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
	private static JdbcPerunTemplate jdbc;

	private Perun perun;
	private final static Pattern patternForExtractingPerunBean = Pattern.compile("^pb_([a-z_]+)_id$");

	public final static String authzRoleMappingSelectQuery = " authz.user_id as authz_user_id, authz.role_id as authz_role_id," +
		"authz.authorized_group_id as authz_authorized_group_id, authz.vo_id as pb_vo_id, authz.group_id as pb_group_id, " +
		"authz.facility_id as pb_facility_id, authz.member_id as pb_member_id, authz.resource_id as pb_resource_id, " +
		"authz.service_id as pb_service_id, authz.service_principal_id as pb_user_id, authz.security_team_id as pb_security_team_id, " +
		"authz.sponsored_user_id as pb_sponsored_user_id";


	protected static final RowMapper<Role> AUTHZROLE_MAPPER_FOR_ATTRIBUTES = new RowMapper<Role>() {
		public Role mapRow(ResultSet rs, int i) throws SQLException {
			Role role = Role.valueOf(rs.getString("name").toUpperCase());
			return role;
		}
	};

	public static final RowMapper<Pair<Role, Map<String, Set<Integer>>>> AUTHZROLE_MAPPER = new RowMapper<Pair<Role, Map<String, Set<Integer>>>>() {
		public Pair<Role, Map<String, Set<Integer>>> mapRow(ResultSet rs, int i) throws SQLException {
			try {
				Map<String, Set<Integer>> perunBeans = null;
				Role role = Role.valueOf(rs.getString("role_name").toUpperCase());

				// Iterate through all returned columns and try to extract PerunBean name from the labels
				for (int j = rs.getMetaData().getColumnCount(); j > 0; j--) {
					Matcher matcher = patternForExtractingPerunBean.matcher(rs.getMetaData().getColumnLabel(j).toLowerCase());
					if (matcher.find()) {
						String perunBeanName = matcher.group(1);
						int id = rs.getInt(j);
						if (!rs.wasNull()) {
							// We have to make first letters o words uppercase
							String className = convertUnderScoreCaseToCamelCase(perunBeanName);

							if (perunBeans == null) {
								perunBeans = new HashMap<String, Set<Integer>>();
							}
							if (perunBeans.get(className) == null) {
								perunBeans.put(className, new HashSet<Integer>());

							}
							perunBeans.get(className).add(id);
						}
					}
				}

				return new Pair<Role, Map<String, Set<Integer>>>(role, perunBeans);

			} catch (Exception e) {
				throw new InternalErrorRuntimeException(e);
			}
		}
	};

	private static String convertUnderScoreCaseToCamelCase(String name) {
		boolean nextIsCapital = true;
		StringBuilder nameBuilder = new StringBuilder();
		for (char c : name.toCharArray()) {
			if (c == '_') {
				nextIsCapital = true;
			} else {
				if (nextIsCapital) {
					c = Character.toUpperCase(c);
					nextIsCapital = false;
				}
				nameBuilder.append(c);
			}
		}
		return nameBuilder.toString();
	}

	public AuthzResolverImpl(DataSource perunPool) {
		jdbc = new JdbcPerunTemplate(perunPool);
	}

	public AuthzRoles getRoles(User user) throws InternalErrorException {
		AuthzRoles authzRoles = new AuthzRoles();

		if (user != null) {
			try {
				// Get roles from Authz table
				List<Pair<Role, Map<String, Set<Integer>>>> authzRolesPairs = jdbc.query("select " + authzRoleMappingSelectQuery
						+ ", roles.name as role_name from authz left join roles on authz.role_id=roles.id where authz.user_id=? or authorized_group_id in "
						+ "(select groups.id from groups join groups_members on groups.id=groups_members.group_id join members on "
						+ "members.id=groups_members.member_id join users on users.id=members.user_id where users.id=?)", AUTHZROLE_MAPPER, user.getId(), user.getId());

				for (Pair<Role, Map<String, Set<Integer>>> pair : authzRolesPairs) {
					authzRoles.putAuthzRoles(pair.getLeft(), pair.getRight());
				}

				// Get service users for user
				List<Integer> authzServiceUsers = jdbc.query("select specific_user_users.specific_user_id as id from users, " +
						"specific_user_users where users.id=specific_user_users.user_id and specific_user_users.status='0' and users.id=? " +
				        "and specific_user_users.type=?", Utils.ID_MAPPER ,user.getId(), SpecificUserType.SERVICE.getSpecificUserType());
				for (Integer serviceUserId : authzServiceUsers) {
					authzRoles.putAuthzRole(Role.SELF, User.class, serviceUserId);
				}

				// Get members for user
				List<Integer> authzMember = jdbc.query("select members.id as id from members where members.user_id=?",
						Utils.ID_MAPPER ,user.getId());
				for (Integer memberId : authzMember) {
					authzRoles.putAuthzRole(Role.SELF, Member.class, memberId);
				}

			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}

		return authzRoles;
	}

	public void initialize() throws InternalErrorException {

		if(perun.isPerunReadOnly()) log.debug("Loading authzresolver manager init in readOnly version.");

		// Check if all roles defined in class Role exists in the DB
		for (Role role: Role.values()) {
			try {
				if (0 == jdbc.queryForInt("select count(*) from roles where name=?", role.getRoleName())) {
					//Skip creating not existing roles for read only Perun
					if(perun.isPerunReadOnly()) {
						throw new InternalErrorException("One of deafult roles not exists in DB - " + role);
					} else {
						int newId = Utils.getNewId(jdbc, "roles_id_seq");
						jdbc.update("insert into roles (id, name) values (?,?)", newId, role.getRoleName());
					}
				}
			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	public static List<Role> getRolesWhichCanWorkWithAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) throws InternalErrorException {
		String actType = actionType.getActionType().toLowerCase();
		try {
			return jdbc.query("select distinct roles.name from attributes_authz " +
					"join roles on attributes_authz.role_id=roles.id " +
					"join action_types on attributes_authz.action_type_id=action_types.id " +
					"where attributes_authz.attr_id=? and action_types.action_type=?", AUTHZROLE_MAPPER_FOR_ATTRIBUTES, attrDef.getId(), actType);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<Role>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAllUserAuthz(PerunSession sess, User user) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where user_id=?", user.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAllSponsoredUserAuthz(PerunSession sess, User sponsoredUser) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where sponsored_user_id=?", sponsoredUser.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAllGroupAuthz(PerunSession sess, Group group) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where authorized_group_id=?", group.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAllAuthzForVo(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where vo_id=?", vo.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public void removeAllAuthzForGroup(PerunSession sess, Group group) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where group_id=?", group.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public void removeAllAuthzForFacility(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where facility_id=?", facility.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public void removeAllAuthzForResource(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where resource_id=?", resource.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public void removeAllAuthzForService(PerunSession sess, Service service) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where service_id=?", service.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public void removeAllAuthzForSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where security_team_id=?", securityTeam.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	public void addAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (user_id, role_id, facility_id) values (?, (select id from roles where name=?), ?)", user.getId(), Role.FACILITYADMIN.getRoleName(), facility.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already admin of the facility " + facility, e, user, facility);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (authorized_group_id, role_id, facility_id) values (?, (select id from roles where name=?), ?)", group.getId(), Role.FACILITYADMIN.getRoleName(), facility.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already admin of the facility " + facility, e, group, facility);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and facility_id=? and role_id=(select id from roles where name=?)", user.getId(), facility.getId(), Role.FACILITYADMIN.getRoleName())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the facility " + facility);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and facility_id=? and role_id=(select id from roles where name=?)", group.getId(), facility.getId(), Role.FACILITYADMIN.getRoleName())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not admin of the facility " + facility);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addAdmin(PerunSession sess, User sponsoredUser, User user) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (user_id, role_id, sponsored_user_id) values (?, (select id from roles where name=?), ?)", user.getId(), Role.SPONSOR.getRoleName(), sponsoredUser.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already sponsor of the sponsoredUser " + sponsoredUser, e, user, sponsoredUser);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addAdmin(PerunSession sess, User sponsoredUser, Group group) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (authorized_group_id, role_id, sponsored_user_id) values (?, (select id from roles where name=?), ?)", group.getId(), Role.SPONSOR.getRoleName(), sponsoredUser.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already sponsor of the sponsoredUser " + sponsoredUser, e, group, sponsoredUser);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAdmin(PerunSession sess, User sponsoredUser, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and sponsored_user_id=? and role_id=(select id from roles where name=?)", user.getId(), sponsoredUser.getId(), Role.SPONSOR.getRoleName())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not sponsor of the sponsored user " + sponsoredUser);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAdmin(PerunSession sess, User sponsoredUser, Group group) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and sponsored_user_id=? and role_id=(select id from roles where name=?)", group.getId(), sponsoredUser.getId(), Role.SPONSOR.getRoleName())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not sponsor of the sponsored user " + sponsoredUser);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, AlreadyAdminException {
		try {
			// Add GROUPADMIN role + groupId and voId
			jdbc.update("insert into authz (user_id, role_id, group_id, vo_id) values (?, (select id from roles where name=?), ?, ?)",
					user.getId(), Role.GROUPADMIN.getRoleName(), group.getId(), group.getVoId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already admin in group " + group, e, user, group);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (authorized_group_id, role_id, group_id, vo_id) values (?, (select id from roles where name=?), ?, ?)",
					authorizedGroup.getId(), Role.GROUPADMIN.getRoleName(), group.getId(), group.getVoId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + authorizedGroup.getId() + " is already group admin in group " + group, e, authorizedGroup, group);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and group_id=? and role_id=(select id from roles where name=?)",
						user.getId(), group.getId(), Role.GROUPADMIN.getRoleName())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the group " + group);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and group_id=? and role_id=(select id from roles where name=?)",
						authorizedGroup.getId(), group.getId(), Role.GROUPADMIN.getRoleName())) {
				throw new GroupNotAdminException("Group id=" + authorizedGroup.getId() + " is not admin of the group " + group);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (user_id, role_id, vo_id) values (?, (select id from roles where name=?), ?)", user.getId(),
					Role.VOADMIN.getRoleName(), vo.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already admin in vo " + vo, e, user, vo);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (role_id, vo_id, authorized_group_id) values ((select id from roles where name=?), ?, ?)",
					Role.VOADMIN.getRoleName(), vo.getId(), group.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already admin in vo " + vo, e, group, vo);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws AlreadyAdminException, InternalErrorException {
		try {
			jdbc.update("insert into authz (user_id, role_id, security_team_id) values (?, (select id from roles where name=?), ?)", user.getId(),
					Role.SECURITYADMIN.getRoleName(), securityTeam.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already admin in securityTeam " + securityTeam, e, user, securityTeam);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws AlreadyAdminException, InternalErrorException {
		try {
			jdbc.update("insert into authz (authorized_group_id, role_id, security_team_id) values (?, (select id from roles where name=?), ?)", group.getId(),
					Role.SECURITYADMIN.getRoleName(), securityTeam.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already admin in securityTeam " + securityTeam, e, group, securityTeam);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addObserver(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (user_id, role_id, vo_id) values (?, (select id from roles where name=?), ?)", user.getId(),
					Role.VOOBSERVER.getRoleName(), vo.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already observer in vo " + vo, e, user, vo);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addObserver(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (role_id, vo_id, authorized_group_id) values ((select id from roles where name=?), ?, ?)",
					Role.VOOBSERVER.getRoleName(), vo.getId(), group.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already observer in vo " + vo, e, group, vo);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addTopGroupCreator(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (user_id, role_id, vo_id) values (?, (select id from roles where name=?), ?)", user.getId(),
					Role.TOPGROUPCREATOR.getRoleName(), vo.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already observer in vo " + vo, e, user, vo);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addTopGroupCreator(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (role_id, vo_id, authorized_group_id) values ((select id from roles where name=?), ?, ?)",
					Role.TOPGROUPCREATOR.getRoleName(), vo.getId(), group.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already observer in vo " + vo, e, group, vo);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeObserver(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and vo_id=? and role_id=(select id from roles where name=?)", user.getId(), vo.getId(), Role.VOOBSERVER.getRoleName())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not observer of the vo " + vo);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeObserver(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and vo_id=? and role_id=(select id from roles where name=?)", group.getId(), vo.getId(), Role.VOOBSERVER.getRoleName())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not observer of the vo " + vo);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeTopGroupCreator(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and vo_id=? and role_id=(select id from roles where name=?)", user.getId(), vo.getId(), Role.TOPGROUPCREATOR.getRoleName())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not observer of the vo " + vo);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeTopGroupCreator(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and vo_id=? and role_id=(select id from roles where name=?)", group.getId(), vo.getId(), Role.TOPGROUPCREATOR.getRoleName())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not observer of the vo " + vo);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and vo_id=? and role_id=(select id from roles where name=?)", user.getId(), vo.getId(), Role.VOADMIN.getRoleName())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the vo " + vo);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and vo_id=? and role_id=(select id from roles where name=?)", group.getId(), vo.getId(), Role.VOADMIN.getRoleName())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not admin of the vo " + vo);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws UserNotAdminException, InternalErrorException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and security_team_id=? and role_id=(select id from roles where name=?)", user.getId(), securityTeam.getId(), Role.SECURITYADMIN.getRoleName())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the security team " + securityTeam);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws GroupNotAdminException, InternalErrorException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and security_team_id=? and role_id=(select id from roles where name=?)", group.getId(), securityTeam.getId(), Role.SECURITYADMIN.getRoleName())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not admin of the security team " + securityTeam);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void makeUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException {
		try {
			jdbc.update("insert into authz (user_id, role_id) values (?, (select id from roles where name=?))", user.getId(), Role.PERUNADMIN.getRoleName());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removePerunAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and role_id=(select id from roles where name=?)", user.getId(), Role.PERUNADMIN.getRoleName())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not perun admin.");
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void setPerun(Perun perun) {
		this.perun = perun;
	}
}
