package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
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
import cz.metacentrum.perun.core.api.exceptions.PolicyNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthzResolverImpl implements AuthzResolverImplApi {

	final static Logger log = LoggerFactory.getLogger(FacilitiesManagerImpl.class);

	private PerunRolesLoader perunRolesLoader;
	private static PerunPoliciesContainer perunPoliciesContainer = new PerunPoliciesContainer();

	//http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
	private static JdbcPerunTemplate jdbc;

	private final static Pattern patternForExtractingPerunBean = Pattern.compile("^pb_([a-z_]+)_id$");

	private final static String authzRoleMappingSelectQuery = " authz.user_id as authz_user_id, authz.role_id as authz_role_id," +
		"authz.authorized_group_id as authz_authorized_group_id, authz.vo_id as pb_vo_id, authz.group_id as pb_group_id, " +
		"authz.facility_id as pb_facility_id, authz.member_id as pb_member_id, authz.resource_id as pb_resource_id, " +
		"authz.service_id as pb_service_id, authz.service_principal_id as pb_user_id, authz.security_team_id as pb_security_team_id, " +
		"authz.sponsored_user_id as pb_sponsored_user_id";


	private static final RowMapper<String> AUTHZROLE_MAPPER_FOR_ATTRIBUTES = (rs, i) -> rs.getString("name").toUpperCase();

	private static final RowMapper<Pair<String, Map<String, Set<Integer>>>> AUTHZROLE_MAPPER = (rs, i) -> {
		try {
			Map<String, Set<Integer>> perunBeans = null;
			String role = rs.getString("role_name").toUpperCase();

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
							perunBeans = new HashMap<>();
						}
						perunBeans.computeIfAbsent(className, k -> new HashSet<>());
						perunBeans.get(className).add(id);
					}
				}
			}

			return new Pair<>(role, perunBeans);

		} catch (Exception e) {
			throw new InternalErrorException(e);
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
		jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	@Override
	public AuthzRoles getRoles(User user) throws InternalErrorException {
		AuthzRoles authzRoles = new AuthzRoles();

		if (user != null) {
			try {
				// Get roles from Authz table
				List<Pair<String, Map<String, Set<Integer>>>> authzRolesPairs = jdbc.query("select " + authzRoleMappingSelectQuery
						+ ", roles.name as role_name from authz left join roles on authz.role_id=roles.id where authz.user_id=? or authorized_group_id in "
						+ "(select groups.id from groups join groups_members on groups.id=groups_members.group_id join members on "
						+ "members.id=groups_members.member_id join users on users.id=members.user_id where users.id=?)", AUTHZROLE_MAPPER, user.getId(), user.getId());

				for (Pair<String, Map<String, Set<Integer>>> pair : authzRolesPairs) {
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

	@Override
	public AuthzRoles getRoles(Group group) throws InternalErrorException {
		AuthzRoles authzRoles = new AuthzRoles();

		if (group != null) {
			try {
				// Get roles from Authz table
				List<Pair<String, Map<String, Set<Integer>>>> authzRolesPairs = jdbc.query("select " + authzRoleMappingSelectQuery
				+ ", roles.name as role_name from authz left join roles on authz.role_id=roles.id where authz.authorized_group_id=?",
				AUTHZROLE_MAPPER, group.getId());

				for (Pair<String, Map<String, Set<Integer>>> pair : authzRolesPairs) {
					authzRoles.putAuthzRoles(pair.getLeft(), pair.getRight());
				}

			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}

		return authzRoles;
	}

	public void initialize() throws InternalErrorException {
		this.perunRolesLoader.loadPerunRoles(jdbc);
		perunPoliciesContainer.setPerunPolicies(this.perunRolesLoader.loadPerunPolicies());
	}

	public static Map<String, Set<ActionType>> getRolesWhichCanWorkWithAttribute(ActionType actionType, AttributeDefinition attrDef) throws InternalErrorException {
		String actType = actionType.getActionType().toLowerCase() + "%";
		try {
			List<Pair<String, ActionType>> pairs = jdbc.query("select distinct roles.name, action_types.action_type from attributes_authz " +
							"join roles on attributes_authz.role_id=roles.id " +
							"join action_types on attributes_authz.action_type_id=action_types.id " +
							"where attributes_authz.attr_id=? and action_types.action_type like ?",
					(rs, arg1) -> new Pair<>(rs.getString("name").toUpperCase(), ActionType.valueOf(rs.getString("action_type").toUpperCase())),
					attrDef.getId(), actType);

			Map<String, Set<ActionType>> result = new HashMap<>();
			for (Pair<String, ActionType> pair : pairs) {
				if (result.containsKey(pair.getLeft())) {
					result.get(pair.getLeft()).add(pair.getRight());
				} else {
					Set<ActionType> rights = new HashSet<>();
					rights.add(pair.getRight());
					result.put(pair.getLeft(), rights);
				}
			}
			return result;

		} catch (EmptyResultDataAccessException e) {
			return new HashMap<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAllUserAuthz(PerunSession sess, User user) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where user_id=?", user.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAllSponsoredUserAuthz(PerunSession sess, User sponsoredUser) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where sponsored_user_id=?", sponsoredUser.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAllAuthzForVo(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where vo_id=?", vo.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public void removeAllAuthzForGroup(PerunSession sess, Group group) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where group_id=?", group.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public void removeAllAuthzForFacility(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where facility_id=?", facility.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public void removeAllAuthzForResource(PerunSession sess, Resource resource) throws InternalErrorException {
		try {
			jdbc.update("delete from authz where resource_id=?", resource.getId());
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
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

	@Override
	public void addAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (user_id, role_id, facility_id) values (?, (select id from roles where name=?), ?)", user.getId(), Role.FACILITYADMIN.toLowerCase(), facility.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already admin of the facility " + facility, e, user, facility);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (authorized_group_id, role_id, facility_id) values (?, (select id from roles where name=?), ?)", group.getId(), Role.FACILITYADMIN.toLowerCase(), facility.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already admin of the facility " + facility, e, group, facility);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and facility_id=? and role_id=(select id from roles where name=?)", user.getId(), facility.getId(), Role.FACILITYADMIN.toLowerCase())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the facility " + facility);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and facility_id=? and role_id=(select id from roles where name=?)", group.getId(), facility.getId(), Role.FACILITYADMIN.toLowerCase())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not admin of the facility " + facility);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addAdmin(PerunSession sess, Resource resource, User user) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (user_id, role_id, resource_id) values (?, (select id from roles where name=?), ?)", user.getId(), Role.RESOURCEADMIN.toLowerCase(), resource.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already admin of the resource " + resource, e, user, resource);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addAdmin(PerunSession sess, Resource resource, Group group) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (authorized_group_id, role_id, resource_id) values (?, (select id from roles where name=?), ?)", group.getId(), Role.RESOURCEADMIN.toLowerCase(), resource.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already admin of the resource " + resource, e, group, resource);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, Resource resource, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and resource_id=? and role_id=(select id from roles where name=?)", user.getId(), resource.getId(), Role.RESOURCEADMIN.toLowerCase())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the resource " + resource);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, Resource resource, Group group) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and resource_id=? and role_id=(select id from roles where name=?)", group.getId(), resource.getId(), Role.RESOURCEADMIN.toLowerCase())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not admin of the resource " + resource);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addAdmin(PerunSession sess, User sponsoredUser, User user) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (user_id, role_id, sponsored_user_id) values (?, (select id from roles where name=?), ?)", user.getId(), Role.SPONSOR.toLowerCase(), sponsoredUser.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already sponsor of the sponsoredUser " + sponsoredUser, e, user, sponsoredUser);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addAdmin(PerunSession sess, User sponsoredUser, Group group) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (authorized_group_id, role_id, sponsored_user_id) values (?, (select id from roles where name=?), ?)", group.getId(), Role.SPONSOR.toLowerCase(), sponsoredUser.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already sponsor of the sponsoredUser " + sponsoredUser, e, group, sponsoredUser);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, User sponsoredUser, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and sponsored_user_id=? and role_id=(select id from roles where name=?)", user.getId(), sponsoredUser.getId(), Role.SPONSOR.toLowerCase())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not sponsor of the sponsored user " + sponsoredUser);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, User sponsoredUser, Group group) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and sponsored_user_id=? and role_id=(select id from roles where name=?)", group.getId(), sponsoredUser.getId(), Role.SPONSOR.toLowerCase())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not sponsor of the sponsored user " + sponsoredUser);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, AlreadyAdminException {
		try {
			// Add GROUPADMIN role + groupId and voId
			jdbc.update("insert into authz (user_id, role_id, group_id, vo_id) values (?, (select id from roles where name=?), ?, ?)",
					user.getId(), Role.GROUPADMIN.toLowerCase(), group.getId(), group.getVoId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already admin in group " + group, e, user, group);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, AlreadyAdminException {
		try {
			jdbc.update("insert into authz (authorized_group_id, role_id, group_id, vo_id) values (?, (select id from roles where name=?), ?, ?)",
					authorizedGroup.getId(), Role.GROUPADMIN.toLowerCase(), group.getId(), group.getVoId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + authorizedGroup.getId() + " is already group admin in group " + group, e, authorizedGroup, group);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and group_id=? and role_id=(select id from roles where name=?)",
						user.getId(), group.getId(), Role.GROUPADMIN.toLowerCase())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the group " + group);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and group_id=? and role_id=(select id from roles where name=?)",
						authorizedGroup.getId(), group.getId(), Role.GROUPADMIN.toLowerCase())) {
				throw new GroupNotAdminException("Group id=" + authorizedGroup.getId() + " is not admin of the group " + group);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws AlreadyAdminException, InternalErrorException {
		try {
			jdbc.update("insert into authz (user_id, role_id, security_team_id) values (?, (select id from roles where name=?), ?)", user.getId(),
					Role.SECURITYADMIN.toLowerCase(), securityTeam.getId());
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
					Role.SECURITYADMIN.toLowerCase(), securityTeam.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already admin in securityTeam " + securityTeam, e, group, securityTeam);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, SecurityTeam securityTeam, User user) throws UserNotAdminException, InternalErrorException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and security_team_id=? and role_id=(select id from roles where name=?)", user.getId(), securityTeam.getId(), Role.SECURITYADMIN.toLowerCase())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the security team " + securityTeam);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeAdmin(PerunSession sess, SecurityTeam securityTeam, Group group) throws GroupNotAdminException, InternalErrorException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and security_team_id=? and role_id=(select id from roles where name=?)", group.getId(), securityTeam.getId(), Role.SECURITYADMIN.toLowerCase())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not admin of the security team " + securityTeam);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void makeUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException {
		try {
			jdbc.update("insert into authz (user_id, role_id) values (?, (select id from roles where name=?))", user.getId(), Role.PERUNADMIN.toLowerCase());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void makeUserPerunObserver(PerunSession sess, User user) throws InternalErrorException {
		try {
			jdbc.update("insert into authz (user_id, role_id) values (?, (select id from roles where name=?))", user.getId(), Role.PERUNOBSERVER.toLowerCase());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void makeAuthorizedGroupPerunObserver(PerunSession sess, Group authorizedGroup) throws InternalErrorException {
		try {
			jdbc.update("insert into authz (authorized_group_id, role_id) values (?, (select id from roles where name=?))", authorizedGroup.getId(), Role.PERUNOBSERVER.toLowerCase());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removePerunObserverFromAuthorizedGroup(PerunSession sess, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and role_id=(select id from roles where name=?)", authorizedGroup.getId(), Role.PERUNOBSERVER.toLowerCase())) {
				throw new GroupNotAdminException("Group id=" + authorizedGroup.getId() + " is not perun observer.");
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removePerunAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and role_id=(select id from roles where name=?)", user.getId(), Role.PERUNADMIN.toLowerCase())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not perun admin.");
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removePerunObserver(PerunSession sess, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and role_id=(select id from roles where name=?)", user.getId(), Role.PERUNOBSERVER.toLowerCase())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not perun observer.");
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void makeUserCabinetAdmin(PerunSession sess, User user) throws InternalErrorException {
		try {
			jdbc.update("insert into authz (user_id, role_id) values (?, (select id from roles where name=?))", user.getId(), Role.CABINETADMIN.toLowerCase());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeCabinetAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and role_id=(select id from roles where name=?)", user.getId(), Role.CABINETADMIN.toLowerCase())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not cabinet admin.");
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addVoRole(PerunSession sess, String role, Vo vo, User user) throws InternalErrorException, AlreadyAdminException {
		if(!Arrays.asList(Role.SPONSOR,Role.TOPGROUPCREATOR,Role.VOADMIN,Role.VOOBSERVER).contains(role)) {
			throw new IllegalArgumentException("Role "+role+" cannot be set on VO");
		}
		try {
			jdbc.update("insert into authz (user_id, role_id, vo_id) values (?, (select id from roles where name=?), ?)", user.getId(),
					role.toLowerCase(), vo.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already "+role+" in vo " + vo, e, user, vo, role);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addVoRole(PerunSession sess, String role, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException {
		if(!Arrays.asList(Role.SPONSOR,Role.TOPGROUPCREATOR,Role.VOADMIN,Role.VOOBSERVER).contains(role)) {
			throw new IllegalArgumentException("Role "+role+" cannot be set on VO");
		}
		try {
			jdbc.update("insert into authz (role_id, vo_id, authorized_group_id) values ((select id from roles where name=?), ?, ?)",
					role.toLowerCase(), vo.getId(), group.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already "+role+" in vo " + vo, e, group, vo, role);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeVoRole(PerunSession sess, String role, Vo vo, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and vo_id=? and role_id=(select id from roles where name=?)", user.getId(), vo.getId(), role.toLowerCase())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not "+role+" in the vo " + vo);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeVoRole(PerunSession sess, String role, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and vo_id=? and role_id=(select id from roles where name=?)", group.getId(), vo.getId(), role.toLowerCase())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not "+role+" in the vo " + vo);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean isUserInRoleForVo(PerunSession session, User user, String role, Vo vo) {
		// COUNT(*) should never return NULL
		return jdbc.queryForObject(
				"SELECT COUNT(*) FROM authz JOIN roles ON (authz.role_id=roles.id) " +
						"WHERE authz.user_id=? AND roles.name=? AND authz.vo_id=?",	Integer.class,
				user.getId(), role.toLowerCase(), vo.getId()) > 0;
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean isGroupInRoleForVo(PerunSession session, Group group, String role, Vo vo) {
		// COUNT(*) should never return NULL
		return jdbc.queryForObject(
				"SELECT COUNT(*) FROM authz JOIN roles ON (authz.role_id=roles.id) " +
						"WHERE authz.authorized_group_id=? AND roles.name=? AND authz.vo_id=?",	Integer.class,
				group.getId(), role.toLowerCase(), vo.getId()) > 0;
	}

	@Override
	public List<Integer> getVoIdsForGroupInRole(PerunSession sess, Group group, String role) throws InternalErrorException {
		try {
			return jdbc.query("SELECT vo_id FROM authz WHERE role_id=(select id from roles where name=?) and authorized_group_id=? and vo_id is not NULL",
					new SingleColumnRowMapper<>(Integer.class), role.toLowerCase(), group.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Integer> getVoIdsForUserInRole(PerunSession sess, User user, String role) throws InternalErrorException {
		try {
			return jdbc.query("SELECT vo_id FROM authz WHERE role_id=(select id from roles where name=?) and user_id=? and vo_id is not NULL",
					new SingleColumnRowMapper<>(Integer.class), role.toLowerCase(), user.getId() );
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addResourceRole(PerunSession sess, User user, String role, Resource resource) throws InternalErrorException, AlreadyAdminException {
		if (!role.equals(Role.RESOURCESELFSERVICE)) {
			throw new InternalErrorException("Role " + role + " cannot be set on resource.");
		}
		try {
			jdbc.update("insert into authz (user_id, role_id, resource_id) values (?, (select id from roles where name=?), ?)", user.getId(),
				role.toLowerCase(), resource.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("User id=" + user.getId() + " is already "+role+" in resource " + resource, e, user, resource, role);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addResourceRole(PerunSession sess, Group group, String role, Resource resource) throws InternalErrorException, AlreadyAdminException {
		if (!role.equals(Role.RESOURCESELFSERVICE)) {
			throw new IllegalArgumentException("Role "+role+" cannot be set on resource.");
		}
		try {
			jdbc.update("insert into authz (role_id, resource_id, authorized_group_id) values ((select id from roles where name=?), ?, ?)",
				role.toLowerCase(), resource.getId(), group.getId());
		} catch (DataIntegrityViolationException e) {
			throw new AlreadyAdminException("Group id=" + group.getId() + " is already "+role+" in resource " + resource, e, group, resource, role);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}


	@Override
	public void removeResourceRole(PerunSession sess, String role, Resource resource, User user) throws InternalErrorException, UserNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where user_id=? and resource_id=? and role_id=(select id from roles where name=?)", user.getId(), resource.getId(), role.toLowerCase())) {
				throw new UserNotAdminException("User id=" + user.getId() + " is not "+role+" in the resource " + resource);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeResourceRole(PerunSession sess, String role, Resource resource, Group group) throws InternalErrorException, GroupNotAdminException {
		try {
			if (0 == jdbc.update("delete from authz where authorized_group_id=? and resource_id=? and role_id=(select id from roles where name=?)", group.getId(), resource.getId(), role.toLowerCase())) {
				throw new GroupNotAdminException("Group id=" + group.getId() + " is not "+role+" in the resource " + resource);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}


	public void setPerunRolesLoader(PerunRolesLoader perunRolesLoader) {
		this.perunRolesLoader = perunRolesLoader;
	}

	@Override
	public boolean roleExists(String role) {
		if (role == null) return false;
		try {
			return 1 == jdbc.queryForInt("select count(*) from roles where name=?", role.toLowerCase());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public static JsonNode getPerunPolicy(String policyName) throws PolicyNotExistsException {
		return perunPoliciesContainer.getPerunPolicy(policyName);
	}
}
