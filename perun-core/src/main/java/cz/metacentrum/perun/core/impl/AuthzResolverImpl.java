package cz.metacentrum.perun.core.impl;

import static cz.metacentrum.perun.core.impl.FacilitiesManagerImpl.FACILITY_MAPPER;
import static cz.metacentrum.perun.core.impl.FacilitiesManagerImpl.FACILITY_MAPPING_SELECT_QUERY;
import static cz.metacentrum.perun.core.impl.GroupsManagerImpl.GROUP_MAPPER;
import static cz.metacentrum.perun.core.impl.GroupsManagerImpl.GROUP_MAPPING_SELECT_QUERY;
import static cz.metacentrum.perun.core.impl.MembersManagerImpl.MEMBER_MAPPER;
import static cz.metacentrum.perun.core.impl.MembersManagerImpl.MEMBER_MAPPING_SELECT_QUERY;
import static cz.metacentrum.perun.core.impl.ResourcesManagerImpl.RESOURCE_MAPPER;
import static cz.metacentrum.perun.core.impl.ResourcesManagerImpl.RESOURCE_MAPPING_SELECT_QUERY;
import static cz.metacentrum.perun.core.impl.UsersManagerImpl.USER_MAPPER;
import static cz.metacentrum.perun.core.impl.UsersManagerImpl.USER_MAPPING_SELECT_QUERY;
import static cz.metacentrum.perun.core.impl.VosManagerImpl.VO_MAPPER;
import static cz.metacentrum.perun.core.impl.VosManagerImpl.VO_MAPPING_SELECT_QUERY;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunPolicy;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.RoleAssignmentType;
import cz.metacentrum.perun.core.api.RoleManagementRules;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PolicyNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleAlreadySetException;
import cz.metacentrum.perun.core.api.exceptions.RoleManagementRulesNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSetException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class AuthzResolverImpl implements AuthzResolverImplApi {

  static final Logger LOG = LoggerFactory.getLogger(FacilitiesManagerImpl.class);
  private static final Pattern PATTERN_FOR_EXTRACTING_PERUN_BEAN = Pattern.compile("^pb_([a-z_]+)_id$");
  private static final String AUTHZ_COMPLEMENTARY_OBJECTS_WITH_AUTHORIZED_GROUPS =
      " authz.authorized_group_id as authz_authorized_group_id, " +
          "authz.vo_id as pb_vo_id, authz.group_id as pb_group_id, authz.facility_id as pb_facility_id, authz" +
          ".member_id as pb_member_id, " +
          "authz.resource_id as pb_resource_id, authz.service_id as pb_service_id, " +
          "authz.sponsored_user_id as pb_sponsored_user_id, roles.name as role_name, groups.name as groups_name, " +
          "groups.dsc as groups_description, groups.vo_id as groups_vo_id";
  private static final String AUTHZ_ROLE_MAPPING_SELECT_QUERY =
      " authz.user_id as authz_user_id, authz.role_id as authz_role_id," +
          "authz.authorized_group_id as authz_authorized_group_id, authz.vo_id as pb_vo_id, authz.group_id as " +
          "pb_group_id, " +
          "authz.facility_id as pb_facility_id, authz.member_id as pb_member_id, authz.resource_id as pb_resource_id," +
          " " + "authz.service_id as pb_service_id, " +
          "authz.sponsored_user_id as pb_sponsored_user_id";
  private static final Pattern COLUMN_NAMES_PATTERN = Pattern.compile("^[_0-9a-zA-Z]+$");
  private static final RowMapper<Pair<String, Map<String, Map<Integer, List<Group>>>>>
      AUTHZ_COMPLEMENTARY_OBJECTS_AUTH_GROUPS_MAPPER = (rs, i) -> {
        try {
          Map<String, Map<Integer, List<Group>>> perunBeanIdsWithAuthorizedGroups = new HashMap<>();

          String role = rs.getString("role_name").toUpperCase();

          // Iterate through all returned columns and try to extract PerunBean name from the labels
          for (int j = rs.getMetaData().getColumnCount(); j > 0; j--) {
            Matcher matcher = PATTERN_FOR_EXTRACTING_PERUN_BEAN
                                  .matcher(rs.getMetaData().getColumnLabel(j).toLowerCase());
            if (matcher.find()) {
              String perunBeanName = matcher.group(1);
              int complementaryObjectId = rs.getInt(j);

              if (!rs.wasNull()) {
                perunBeanIdsWithAuthorizedGroups.computeIfAbsent(perunBeanName, k -> new HashMap<>());
                perunBeanIdsWithAuthorizedGroups.get(perunBeanName)
                    .computeIfAbsent(complementaryObjectId, k -> new ArrayList<>());

                perunBeanIdsWithAuthorizedGroups.get(perunBeanName).get(complementaryObjectId).add(
                    new Group(rs.getInt("authz_authorized_group_id"), rs.getString("groups_name"),
                        rs.getString("groups_description"), rs.getInt("groups_vo_id")));
              }
            }
          }

          return new Pair<>(role, perunBeanIdsWithAuthorizedGroups);

        } catch (Exception e) {
          throw new InternalErrorException(e);
        }
      };
  private static final RowMapper<String> AUTHZROLE_MAPPER_FOR_ATTRIBUTES =
      (rs, i) -> rs.getString("name").toUpperCase();
  private static final RowMapper<Pair<String, Map<String, Set<Integer>>>> AUTHZROLE_MAPPER = (rs, i) -> {
    try {
      Map<String, Set<Integer>> perunBeans = null;
      String role = rs.getString("role_name").toUpperCase();

      // Iterate through all returned columns and try to extract PerunBean name from the labels
      for (int j = rs.getMetaData().getColumnCount(); j > 0; j--) {
        Matcher matcher = PATTERN_FOR_EXTRACTING_PERUN_BEAN.matcher(rs.getMetaData().getColumnLabel(j).toLowerCase());
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
  private static PerunPoliciesContainer perunPoliciesContainer = new PerunPoliciesContainer();
  //http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
  private static JdbcPerunTemplate jdbc;
  private static NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private PerunRolesLoader perunRolesLoader;

  public AuthzResolverImpl(DataSource perunPool) {
    jdbc = new JdbcPerunTemplate(perunPool);
    jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
    namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
    namedParameterJdbcTemplate.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

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

  /**
   * Get PerunPolicy for the policy name from the PerunPoliciesContainer
   *
   * @param policyName for which will be the policy fetched
   * @return PerunPolicy for the role name
   * @throws PolicyNotExistsException of there is no policy for the policy name
   */
  public static PerunPolicy getPerunPolicy(String policyName) throws PolicyNotExistsException {
    return perunPoliciesContainer.getPerunPolicy(policyName);
  }

  /**
   * Get the policy according the policy name and all its inlcuded policies (without cycle).
   *
   * @param policyName from which will be the policies fetched
   * @return list of policies
   * @throws PolicyNotExistsException if policy or some included policies does not exists in PerunPoliciesContainer
   */
  public static List<PerunPolicy> fetchPolicyWithAllIncludedPolicies(String policyName)
      throws PolicyNotExistsException {
    return perunPoliciesContainer.fetchPolicyWithAllIncludedPolicies(policyName);
  }

  /**
   * Return all loaded perun policies.
   *
   * @return all loaded policies
   */
  public static List<PerunPolicy> getAllPolicies() {
    return new ArrayList<>(perunPoliciesContainer.getAllPolicies());
  }

  /**
   * Return all loaded roles management rules.
   *
   * @return all roles management rules
   */
  public static List<RoleManagementRules> getAllRolesManagementRules() {
    return perunPoliciesContainer.getAllRolesManagementRules();
  }

  /**
   * Get RoleManagementRules for the role name from the PerunPoliciesContainer
   *
   * @param roleName for which will be the rules fetched
   * @return RoleManagementRules for the role name
   * @throws PolicyNotExistsException of there are no rules for the role name
   */
  public static RoleManagementRules getRoleManagementRules(String roleName)
      throws RoleManagementRulesNotExistsException {
    return perunPoliciesContainer.getRoleManagementRules(roleName);
  }

  @Override
  public void addAdmin(PerunSession sess, Facility facility, User user) throws AlreadyAdminException {
    try {
      jdbc.update("insert into authz (user_id, role_id, facility_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?, ?)", user.getId(),
          Role.FACILITYADMIN.toLowerCase(), facility.getId(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("User id=" + user.getId() + " is already admin of the facility " + facility, e,
          user, facility);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addAdmin(PerunSession sess, Facility facility, Group group) throws AlreadyAdminException {
    try {
      jdbc.update("insert into authz (authorized_group_id, role_id, facility_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?, ?)", group.getId(),
          Role.FACILITYADMIN.toLowerCase(), facility.getId(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("Group id=" + group.getId() + " is already admin of the facility " + facility, e,
          group, facility);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addAdmin(PerunSession sess, Resource resource, User user) throws AlreadyAdminException {
    try {
      jdbc.update("insert into authz (user_id, role_id, resource_id, vo_id, facility_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?, ?, ?, ?)", user.getId(),
          Role.RESOURCEADMIN.toLowerCase(), resource.getId(), resource.getVoId(), resource.getFacilityId(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("User id=" + user.getId() + " is already admin of the resource " + resource, e,
          user, resource);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addAdmin(PerunSession sess, Resource resource, Group group) throws AlreadyAdminException {
    try {
      jdbc.update("insert into authz (authorized_group_id, role_id, resource_id, vo_id, facility_id, created_by, " +
                      "created_by_uid)" + " values (?, (select id from roles where name=?), ?, ?, ?, ?, ?)",
          group.getId(), Role.RESOURCEADMIN.toLowerCase(), resource.getId(), resource.getVoId(),
          resource.getFacilityId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("Group id=" + group.getId() + " is already admin of the resource " + resource, e,
          group, resource);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addAdmin(PerunSession sess, User sponsoredUser, User user) throws AlreadyAdminException {
    try {
      jdbc.update("insert into authz (user_id, role_id, sponsored_user_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?, ?)", user.getId(),
          Role.SPONSOR.toLowerCase(), sponsoredUser.getId(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException(
          "User id=" + user.getId() + " is already sponsor of the sponsoredUser " + sponsoredUser, e, user,
          sponsoredUser);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addAdmin(PerunSession sess, User sponsoredUser, Group group) throws AlreadyAdminException {
    try {
      jdbc.update("insert into authz (authorized_group_id, role_id, sponsored_user_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?, ?)", group.getId(),
          Role.SPONSOR.toLowerCase(), sponsoredUser.getId(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException(
          "Group id=" + group.getId() + " is already sponsor of the sponsoredUser " + sponsoredUser, e, group,
          sponsoredUser);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addAdmin(PerunSession sess, Group group, User user) throws AlreadyAdminException {
    try {
      // Add GROUPADMIN role + groupId and voId
      jdbc.update("insert into authz (user_id, role_id, group_id, vo_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?, ?, ?)", user.getId(),
          Role.GROUPADMIN.toLowerCase(), group.getId(), group.getVoId(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("User id=" + user.getId() + " is already admin in group " + group, e, user,
          group);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws AlreadyAdminException {
    try {
      jdbc.update("insert into authz (authorized_group_id, role_id, group_id, vo_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?, ?, ?)", authorizedGroup.getId(),
          Role.GROUPADMIN.toLowerCase(), group.getId(), group.getVoId(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException(
          "Group id=" + authorizedGroup.getId() + " is already group admin in group " + group, e, authorizedGroup,
          group);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addResourceRole(PerunSession sess, User user, String role, Resource resource)
      throws AlreadyAdminException {
    if (!role.equals(Role.RESOURCESELFSERVICE)) {
      throw new InternalErrorException("Role " + role + " cannot be set on resource.");
    }
    try {
      jdbc.update(
          "insert into authz (user_id, role_id, resource_id, vo_id, facility_id, created_by, created_by_uid) values " +
              "(?, (select id from roles where name=?), ?, ?, ?, ?, ?)", user.getId(), role.toLowerCase(),
          resource.getId(), resource.getVoId(), resource.getFacilityId(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("User id=" + user.getId() + " is already " + role + " in resource " + resource, e,
          user, resource, role);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addResourceRole(PerunSession sess, Group group, String role, Resource resource)
      throws AlreadyAdminException {
    if (!role.equals(Role.RESOURCESELFSERVICE)) {
      throw new IllegalArgumentException("Role " + role + " cannot be set on resource.");
    }
    try {
      jdbc.update("insert into authz (role_id, resource_id, authorized_group_id, vo_id, facility_id, created_by, " +
                      "created_by_uid)" + " values ((select id from roles where name=?), ?, ?, ?, ?, ?, ?)",
          role.toLowerCase(), resource.getId(), group.getId(), resource.getVoId(), resource.getFacilityId(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("Group id=" + group.getId() + " is already " + role + " in resource " + resource,
          e, group, resource, role);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addVoRole(PerunSession sess, String role, Vo vo, User user) throws AlreadyAdminException {
    if (!Arrays.asList(Role.SPONSOR, Role.TOPGROUPCREATOR, Role.VOADMIN, Role.VOOBSERVER).contains(role)) {
      throw new IllegalArgumentException("Role " + role + " cannot be set on VO");
    }
    try {
      jdbc.update(
          "insert into authz (user_id, role_id, vo_id, created_by, created_by_uid)  values (?, (select id from roles " +
              "where name=?), ?, ?, ?)", user.getId(), role.toLowerCase(), vo.getId(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("User id=" + user.getId() + " is already " + role + " in vo " + vo, e, user, vo,
          role);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addVoRole(PerunSession sess, String role, Vo vo, Group group) throws AlreadyAdminException {
    if (!Arrays.asList(Role.SPONSOR, Role.TOPGROUPCREATOR, Role.VOADMIN, Role.VOOBSERVER).contains(role)) {
      throw new IllegalArgumentException("Role " + role + " cannot be set on VO");
    }
    try {
      jdbc.update("insert into authz (role_id, vo_id, authorized_group_id, created_by, created_by_uid)" +
                      " values ((select id from roles where name=?), ?, ?, ?, ?)", role.toLowerCase(), vo.getId(),
          group.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("Group id=" + group.getId() + " is already " + role + " in vo " + vo, e, group,
          vo, role);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Group> getAdminGroups(Map<String, Integer> mappingOfValues) {
    String query = prepareQueryToGetAdminGroups(mappingOfValues);

    try {
      return jdbc.query(query, GROUP_MAPPER);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> getAdmins(Map<String, Integer> mappingOfValues, boolean onlyDirectAdmins) {
    String query = prepareQueryToGetRichAdmins(mappingOfValues);

    try {
      Set<User> admins = new HashSet<>(jdbc.query(query, USER_MAPPER));

      if (!onlyDirectAdmins) {
        // Admins through a group
        List<Group> listOfAdminGroups = getAdminGroups(mappingOfValues);
        for (Group authorizedGroup : listOfAdminGroups) {
          admins.addAll(jdbc.query("select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY +
                                   " from users join members on users.id=members.user_id " +
                                   "join groups_members on groups_members.member_id=members.id where groups_members" +
                                   ".group_id=? and " + "members.status=? and groups_members.source_group_status=?",
              UsersManagerImpl.USER_MAPPER, authorizedGroup.getId(), Status.VALID.getCode(),
              MemberGroupStatus.VALID.getCode()));
        }
      }

      return new ArrayList<>(admins);

    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public boolean someAdminExists(Map<String, Integer> mappingOfValues, boolean onlyDirectAdmins) {
    try {
      String mappingAsString = prepareSelectQueryString(mappingOfValues);
      boolean adminExists = !jdbc.query(
          "select " + USER_MAPPING_SELECT_QUERY + " from authz join users on authz.user_id=users.id" + " where  " +
              mappingAsString + " limit 1", USER_MAPPER).isEmpty();

      if (!adminExists && !onlyDirectAdmins) {
        // Admins through a group
        List<Group> listOfAdminGroups = getAdminGroups(mappingOfValues);
        for (Group authorizedGroup : listOfAdminGroups) {
          adminExists = adminExists || !jdbc.query("select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY +
                                                       " from users join members on users.id=members.user_id " +
                                                       "join groups_members on groups_members.member_id=members.id " +
                                                       "where groups_members.group_id=? and members.status=? and " +
                                                       "groups_members.source_group_status=? limit 1",
              UsersManagerImpl.USER_MAPPER, authorizedGroup.getId(), Status.VALID.getCode(),
              MemberGroupStatus.VALID.getCode()).isEmpty();
        }
      }

      return adminExists;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public Set<Facility> getFacilitiesWhereUserIsInRoles(User user, List<String> roles) {
    MapSqlParameterSource parameters = prepareParametersToGetObjectsByUserRoles(user, roles);

    try {
      return new HashSet<>(namedParameterJdbcTemplate.query(
          "select " + FACILITY_MAPPING_SELECT_QUERY + " from authz" +
              " join facilities on authz.facility_id=facilities.id " +
              " left outer join groups_members on groups_members.group_id=authz.authorized_group_id " +
              " left outer join members on members.id=groups_members.member_id " +
              " where (authz.user_id=:uid or members.user_id=:uid) and authz.role_id in " +
              "(select id from roles where name in (:roles))", parameters, FACILITY_MAPPER));
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Set<Group> getGroupsWhereUserIsInRoles(User user, List<String> roles) {
    MapSqlParameterSource parameters = prepareParametersToGetObjectsByUserRoles(user, roles);

    try {
      return new HashSet<>(namedParameterJdbcTemplate.query(
          "select " + GROUP_MAPPING_SELECT_QUERY + " from authz join groups on authz.group_id=groups.id " +
              " left outer join groups_members on groups_members.group_id=authz.authorized_group_id " +
              " left outer join members on members.id=groups_members.member_id " +
              " where (authz.user_id=:uid or members.user_id=:uid) and authz.role_id in " +
              "(select id from roles where name in (:roles))", parameters, GROUP_MAPPER));
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Set<Member> getMembersWhereUserIsInRoles(User user, List<String> roles) {
    MapSqlParameterSource parameters = prepareParametersToGetObjectsByUserRoles(user, roles);

    try {
      return new HashSet<>(namedParameterJdbcTemplate.query(
          "select " + MEMBER_MAPPING_SELECT_QUERY + " from authz join members on authz.member_id=members.id " +
              " left outer join groups_members on groups_members.group_id=authz.authorized_group_id " +
              " left outer join members m on m.id=groups_members.member_id " +
              " where (authz.user_id=:uid or m.user_id=:uid) and authz.role_id in " +
              "(select id from roles where name in (:roles))", parameters, MEMBER_MAPPER));
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Set<Resource> getResourcesWhereUserIsInRoles(User user, List<String> roles) {
    MapSqlParameterSource parameters = prepareParametersToGetObjectsByUserRoles(user, roles);

    try {
      return new HashSet<>(namedParameterJdbcTemplate.query(
          "select " + RESOURCE_MAPPING_SELECT_QUERY + " from authz join resources on authz.resource_id=resources.id " +
              " left outer join groups_members on groups_members.group_id=authz.authorized_group_id " +
              " left outer join members on members.id=groups_members.member_id " +
              " where (authz.user_id=:uid or members.user_id=:uid) and authz.role_id in " +
              "(select id from roles where name in (:roles))", parameters, RESOURCE_MAPPER));
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  public Map<String, Map<String, Map<Integer, List<Group>>>> getRoleComplementaryObjectsWithAuthorizedGroups(
      User user) {
    Map<String, Map<String, Map<Integer, List<Group>>>> roleWithComplementaryObjectsWithAuthorizedGroups =
        new HashMap<>();

    if (user == null) {
      return roleWithComplementaryObjectsWithAuthorizedGroups;
    }

    try {
      // Get user roles based on membership in authorized group(s)
      List<Pair<String, Map<String, Map<Integer, List<Group>>>>> rolePairs = jdbc.query(
          "select " + AUTHZ_COMPLEMENTARY_OBJECTS_WITH_AUTHORIZED_GROUPS + ", roles.name as role_name " +
              "from authz " + "left join roles on authz.role_id=roles.id " +
              "join groups on authz.authorized_group_id=groups.id " + "where authorized_group_id in " +
              "(select groups_members.group_id " + "from groups_members " +
              "join members on members.id=groups_members.member_id " + "join users on users.id=members.user_id" +
              " where groups_members.source_group_status=? and users.id=? and members.status=?)",
          AUTHZ_COMPLEMENTARY_OBJECTS_AUTH_GROUPS_MAPPER, MemberGroupStatus.VALID.getCode(), user.getId(),
          Status.VALID.getCode());

      for (Pair<String, Map<String, Map<Integer, List<Group>>>> rolePair : rolePairs) {
        roleWithComplementaryObjectsWithAuthorizedGroups.computeIfAbsent(rolePair.getLeft(), k -> new HashMap<>());

        for (Map.Entry<String, Map<Integer, List<Group>>> perunBeanNames : rolePair.getRight().entrySet()) {
          // if specific bean name (e.g. 'Vo') is not present, add it
          roleWithComplementaryObjectsWithAuthorizedGroups.get(rolePair.getLeft())
              .computeIfAbsent(perunBeanNames.getKey(), k -> new HashMap<>());

          for (Map.Entry<Integer, List<Group>> perunBeanIdsWithGroups : perunBeanNames.getValue().entrySet()) {
            // if given complementary object id (e.g. VO id for VO ADMIN) is not present, add new key-value pair
            roleWithComplementaryObjectsWithAuthorizedGroups.get(rolePair.getLeft()).get(perunBeanNames.getKey())
                .computeIfAbsent(perunBeanIdsWithGroups.getKey(), k -> new ArrayList<>());

            // add associated authzgroups
            roleWithComplementaryObjectsWithAuthorizedGroups.get(rolePair.getLeft()).get(perunBeanNames.getKey())
                .get(perunBeanIdsWithGroups.getKey()).addAll(perunBeanIdsWithGroups.getValue());
          }
        }
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

    return roleWithComplementaryObjectsWithAuthorizedGroups;
  }

  @Override
  public Integer getRoleId(String role) {
    try {
      return jdbc.queryForInt("SELECT id FROM roles WHERE name=?", role.toLowerCase());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  /**
   * Returns role id based on its name
   *
   * @param name - name of the role
   * @return role id
   */
  @Override
  public int getRoleIdByName(String name) {
    try {
      return jdbc.queryForInt("SELECT id FROM roles WHERE name=?", name.toLowerCase());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public AuthzRoles getRoles(User user, boolean getAuthorizedGroupBasedRoles) {
    AuthzRoles authzRoles = new AuthzRoles();

    if (user != null) {
      try {
        // Get directly assigned user roles from Authz table
        List<Pair<String, Map<String, Set<Integer>>>> authzRolesPairs = jdbc.query(
            "select " + AUTHZ_ROLE_MAPPING_SELECT_QUERY + ", roles.name as role_name " +
                "from authz left join roles on authz.role_id=roles.id " + "where authz.user_id=?", AUTHZROLE_MAPPER,
            user.getId());

        for (Pair<String, Map<String, Set<Integer>>> pair : authzRolesPairs) {
          authzRoles.putAuthzRoles(pair.getLeft(), pair.getRight());
        }

        if (getAuthorizedGroupBasedRoles) {
          // add user roles based on membership in authorized groups
          getRolesObtainedFromAuthorizedGroupMemberships(user).forEach(authzRoles::putAuthzRoles);
        }

        // Get service users for user
        List<Integer> authzServiceUsers = jdbc.query("select specific_user_users.specific_user_id as id" +
                                                         " from users, specific_user_users where" +
                                                         " users.id=specific_user_users.user_id and" +
                                                         " specific_user_users.status=0 and " + "users.id=? " +
                                                         "and specific_user_users.type=?", Utils.ID_MAPPER,
            user.getId(), SpecificUserType.SERVICE.getSpecificUserType());
        for (Integer serviceUserId : authzServiceUsers) {
          authzRoles.putAuthzRole(Role.SELF, User.class, serviceUserId);
        }

        // Get members for user
        List<Integer> authzMember =
            jdbc.query("select members.id as id from members where members.user_id=? and members.status=?",
                Utils.ID_MAPPER, user.getId(), Status.VALID.getCode());
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
  public AuthzRoles getRoles(Group group) {
    AuthzRoles authzRoles = new AuthzRoles();

    if (group != null) {
      try {
        // Get roles from Authz table
        List<Pair<String, Map<String, Set<Integer>>>> authzRolesPairs = jdbc.query(
            "select " + AUTHZ_ROLE_MAPPING_SELECT_QUERY +
                ", roles.name as role_name from authz left join roles on authz.role_id=roles.id where authz" +
                ".authorized_group_id=?", AUTHZROLE_MAPPER, group.getId());

        for (Pair<String, Map<String, Set<Integer>>> pair : authzRolesPairs) {
          authzRoles.putAuthzRoles(pair.getLeft(), pair.getRight());
        }

      } catch (RuntimeException e) {
        throw new InternalErrorException(e);
      }
    }

    return authzRoles;
  }

  @Override
  public AuthzRoles getRolesObtainedFromAuthorizedGroupMemberships(User user) {
    AuthzRoles authzRoles = new AuthzRoles();

    if (user == null) {
      return authzRoles;
    }

    try {
      // Get user roles based on membership in authorized group(s)
      jdbc.query("select " + AUTHZ_ROLE_MAPPING_SELECT_QUERY + ", roles.name as role_name " +
                     "from authz left join roles on authz.role_id=roles.id " + "where authorized_group_id in " +
                     "(select groups.id " + "from groups " +
                     "join groups_members on groups.id=groups_members.group_id" +
                     " and groups_members.source_group_status=? " +
                     "join members on members.id=groups_members.member_id " + "join users on users.id=members.user_id" +
                     " where users.id=? and members.status=?)", AUTHZROLE_MAPPER, MemberGroupStatus.VALID.getCode(),
              user.getId(), Status.VALID.getCode())
          .forEach((pair) -> authzRoles.putAuthzRoles(pair.getLeft(), pair.getRight()));
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

    return authzRoles;
  }

  @Override
  public List<Integer> getVoIdsForGroupInRole(PerunSession sess, Group group, String role) {
    try {
      return jdbc.query(
          "SELECT vo_id FROM authz WHERE role_id=(select id from roles where name=?) and authorized_group_id=? and " +
              "vo_id is not NULL", new SingleColumnRowMapper<>(Integer.class), role.toLowerCase(), group.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Integer> getVoIdsForUserInRole(PerunSession sess, User user, String role) {
    try {
      return jdbc.query(
          "SELECT vo_id FROM authz WHERE role_id=(select id from roles where name=?) and user_id=? and vo_id is not " +
              "NULL", new SingleColumnRowMapper<>(Integer.class), role.toLowerCase(), user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public Set<Vo> getVosWhereUserIsInRoles(User user, List<String> roles) {
    MapSqlParameterSource parameters = prepareParametersToGetObjectsByUserRoles(user, roles);

    try {
      return new HashSet<>(namedParameterJdbcTemplate.query(
          "select " + VO_MAPPING_SELECT_QUERY + " from authz join vos on authz.vo_id=vos.id " +
              " left outer join groups_members on groups_members.group_id=authz.authorized_group_id " +
              " left outer join members on members.id=groups_members.member_id " +
              " where (authz.user_id=:uid or members.user_id=:uid) and authz.role_id in " +
              "(select id from roles where name in (:roles))", parameters, VO_MAPPER));
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean groupMatchesUserRolesFilter(PerunSession sess, User user, Group group, List<String> roles,
                                             List<RoleAssignmentType> types) {
    MapSqlParameterSource parameters = prepareParametersToGetObjectsByUserRoles(user, roles);
    parameters.addValue("groupId", group.getId());

    boolean includeIndirectRoles = types.contains(RoleAssignmentType.INDIRECT) || types.isEmpty();
    boolean includeDirectRoles = types.contains(RoleAssignmentType.DIRECT) || types.isEmpty();

    try {
      List<Group> directRelations = namedParameterJdbcTemplate.query(
          "select " + GROUP_MAPPING_SELECT_QUERY + " from authz join groups on authz.group_id=groups.id " +
              " where groups.id=:groupId and authz.user_id=:uid" +
              (!roles.isEmpty() ? " and (authz.role_id in (select id from roles where name in (:roles)))" : ""),
          parameters, GROUP_MAPPER);

      List<Group> indirectRelations = namedParameterJdbcTemplate.query(
          "select " + GROUP_MAPPING_SELECT_QUERY + " from authz join groups on authz.group_id=groups.id " +
              " left outer join groups_members on groups_members.group_id=authz.authorized_group_id left outer join " +
              "members on members.id=groups_members.member_id " + " where groups.id=:groupId and members.user_id=:uid" +
              (!roles.isEmpty() ? " and (authz.role_id in (select id from roles where name in (:roles)))" : ""),
          parameters, GROUP_MAPPER);

      return (includeDirectRoles && !directRelations.isEmpty()) ||
                 (includeIndirectRoles && !indirectRelations.isEmpty());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  /**
   * Load all authorization components to the database and to the PerunPoliciesContainer
   *
   * @throws InternalErrorException
   */
  public void initialize() {
    if (BeansUtils.isPerunReadOnly()) {
      LOG.debug("Loading authzresolver manager init in readOnly version.");
    }

    List<String> roles = this.perunRolesLoader.loadPerunRoles(jdbc);
    perunPoliciesContainer.setPerunPolicies(this.perunRolesLoader.loadPerunPolicies(roles));
    perunPoliciesContainer.setRolesManagementRules(this.perunRolesLoader.loadPerunRolesManagement(roles));
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public boolean isGroupInRoleForVo(PerunSession session, Group group, String role, Vo vo) {
    // COUNT(*) should never return NULL
    return jdbc.queryForObject("SELECT COUNT(*) FROM authz JOIN roles ON (authz.role_id=roles.id) " +
                                   "WHERE authz.authorized_group_id=? AND roles.name=? AND authz.vo_id=?",
        Integer.class, group.getId(), role.toLowerCase(), vo.getId()) > 0;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public boolean isUserInRoleForVo(PerunSession session, User user, String role, Vo vo) {
    // COUNT(*) should never return NULL
    return jdbc.queryForObject("SELECT COUNT(*) FROM authz JOIN roles ON (authz.role_id=roles.id) " +
                                   "WHERE authz.user_id=? AND roles.name=? AND authz.vo_id=?", Integer.class,
        user.getId(), role.toLowerCase(), vo.getId()) > 0;
  }

  /**
   * Returns true if the user in session is vo admin or vo observer of specific vo
   *
   * @param sess - session
   * @param vo   - vo
   * @return
   */
  @Override
  public boolean isVoAdminOrObserver(PerunSession sess, Vo vo) {
    try {
      var query =
          jdbc.query("SELECT 1 FROM authz WHERE user_id=? AND vo_id=? AND (role_id=? OR role_id=?)", (rs, i) -> true,
              sess.getPerunPrincipal().getUserId(), vo.getId(), AuthzResolverBlImpl.getRoleIdByName(Role.VOADMIN),
              AuthzResolverBlImpl.getRoleIdByName(Role.VOOBSERVER));
      return !query.isEmpty();
    } catch (InternalErrorException e) {
      LOG.error("Error during checking if user is vo admin of vo {}", vo, e);
    }
    return false;
  }

  @Override
  public void loadAuthorizationComponents() {
    List<String> roles = this.perunRolesLoader.loadPerunRoles(jdbc);
    perunPoliciesContainer.setPerunPolicies(this.perunRolesLoader.loadPerunPolicies(roles));
    perunPoliciesContainer.setRolesManagementRules(this.perunRolesLoader.loadPerunRolesManagement(roles));
  }

  @Override
  public void makeAuthorizedGroupPerunObserver(PerunSession sess, Group authorizedGroup) throws AlreadyAdminException {
    try {
      jdbc.update("insert into authz (authorized_group_id, role_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?)", authorizedGroup.getId(),
          Role.PERUNOBSERVER.toLowerCase(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("Group id=" + authorizedGroup.getId() + " is already perun observer", e,
          authorizedGroup, Role.PERUNOBSERVER);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void makeUserCabinetAdmin(PerunSession sess, User user) {
    try {
      jdbc.update("insert into authz (user_id, role_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?)", user.getId(),
          Role.CABINETADMIN.toLowerCase(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void makeUserPerunAdmin(PerunSession sess, User user) throws AlreadyAdminException {
    try {
      jdbc.update("insert into authz (user_id, role_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?)", user.getId(),
          Role.PERUNADMIN.toLowerCase(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("User id=" + user.getId() + " is already perun admin", e, user, Role.PERUNADMIN);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void makeUserPerunObserver(PerunSession sess, User user) throws AlreadyAdminException {
    try {
      jdbc.update("insert into authz (user_id, role_id, created_by, created_by_uid)" +
                      " values (?, (select id from roles where name=?), ?, ?)", user.getId(),
          Role.PERUNOBSERVER.toLowerCase(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
    } catch (DataIntegrityViolationException e) {
      throw new AlreadyAdminException("User id=" + user.getId() + " is already perun observer", e, user,
          Role.PERUNOBSERVER);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  /**
   * Create query to set role according to the mapping of values
   *
   * @param mappingOfValues from which will be the query created
   * @return sql query
   */
  private Map<String, Object> prepareMappingToSetRole(Map<String, Integer> mappingOfValues) {
    Map<String, Object> genericMappingOfValues = new HashMap<>();
    for (String columnName : mappingOfValues.keySet()) {

      if (columnName == null || mappingOfValues.get(columnName) == null) {
        throw new InternalErrorException(
            "Column name and its value cannot be null in the mapping of values, while trying to set role.");
      }

      Matcher matcher = COLUMN_NAMES_PATTERN.matcher(columnName);
      if (!matcher.matches()) {
        throw new InternalErrorException("Cannot create a query to set role, because column name: " + columnName +
                                             " contains forbidden characters. Allowed are only [1-9a-zA-Z_].");
      }
      genericMappingOfValues.put(columnName, mappingOfValues.get(columnName));
    }

    return genericMappingOfValues;
  }

  /**
   * Create parameters for obtaining objects according to user and list of roles.
   *
   * @param user  for who will be fetched id
   * @param roles which will be lower cased
   * @return user and roles parameters
   */
  private MapSqlParameterSource prepareParametersToGetObjectsByUserRoles(User user, List<String> roles) {
    //Not converted in place because there my be used immutable list.
    List<String> rolesLoweCase = new ArrayList<>();
    roles.forEach(role -> rolesLoweCase.add(role.toLowerCase()));
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("uid", user.getId());
    parameters.addValue("roles", rolesLoweCase);

    return parameters;
  }

  /**
   * Create query to select admin groups according to the mapping of values
   *
   * @param mappingOfValues from which will be the query created
   * @return sql query
   */
  private String prepareQueryToGetAdminGroups(Map<String, Integer> mappingOfValues) {
    String mappingAsString = prepareSelectQueryString(mappingOfValues);

    return "select " + GROUP_MAPPING_SELECT_QUERY + " from authz join groups on authz.authorized_group_id=groups.id" +
               " where  " + mappingAsString;
  }

  /**
   * Create query to select rich admins according to the mapping of values
   *
   * @param mappingOfValues from which will be the query created
   * @return sql query
   */
  private String prepareQueryToGetRichAdmins(Map<String, Integer> mappingOfValues) {
    String mappingAsString = prepareSelectQueryString(mappingOfValues);

    return "select " + USER_MAPPING_SELECT_QUERY + " from authz join users on authz.user_id=users.id" + " where  " +
               mappingAsString;
  }

  /**
   * Create query to unset role according to the mapping of values
   *
   * @param mappingOfValues from which will be the query created
   * @return sql query
   */
  private String prepareQueryToUnsetRole(Map<String, Integer> mappingOfValues) {
    List<String> listOfConditions = new ArrayList<>();

    for (String columnName : mappingOfValues.keySet()) {

      if (columnName == null || mappingOfValues.get(columnName) == null) {
        throw new InternalErrorException(
            "Column name and its value cannot be null in the mapping of values, while trying to unset role.");
      }

      Matcher matcher = COLUMN_NAMES_PATTERN.matcher(columnName);
      if (!matcher.matches()) {
        throw new InternalErrorException("Cannot create a query to unset role, because column name: " + columnName +
                                             " contains forbidden characters. Allowed are only [1-9a-zA-Z_].");
      }
      String condition = columnName + "=" + mappingOfValues.get(columnName).toString();
      listOfConditions.add(condition);
    }

    String mappingAsString = StringUtils.join(listOfConditions, " and ");

    return "delete from authz where " + mappingAsString;
  }

  /**
   * Create part of the query which will be used in the final query as a where clause.
   *
   * @param mappingOfValues from which will be the query created
   * @return sql conditions as string
   */
  private String prepareSelectQueryString(Map<String, Integer> mappingOfValues) {
    List<String> listOfConditions = new ArrayList<>();

    for (String columnName : mappingOfValues.keySet()) {

      if (columnName == null || mappingOfValues.get(columnName) == null) {
        throw new InternalErrorException(
            "Column name and its value cannot be null in the mapping of values, while trying to read role.");
      }

      Matcher matcher = COLUMN_NAMES_PATTERN.matcher(columnName);
      if (!matcher.matches()) {
        throw new InternalErrorException("Cannot create a query to read role, because column name: " + columnName +
                                             " contains forbidden characters. Allowed are only [1-9a-zA-Z_].");
      }
      String condition = "authz." + columnName + "=" + mappingOfValues.get(columnName).toString();
      listOfConditions.add(condition);
    }

    return StringUtils.join(listOfConditions, " and ");
  }

  @Override
  public void removeAdmin(PerunSession sess, Facility facility, User user) throws UserNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where user_id=? and facility_id=? and role_id=(select id from roles where name=?)",
          user.getId(), facility.getId(), Role.FACILITYADMIN.toLowerCase())) {
        throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the facility " + facility);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAdmin(PerunSession sess, Facility facility, Group group) throws GroupNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where authorized_group_id=? and facility_id=? and role_id=(select id from roles where " +
              "name=?)", group.getId(), facility.getId(), Role.FACILITYADMIN.toLowerCase())) {
        throw new GroupNotAdminException("Group id=" + group.getId() + " is not admin of the facility " + facility);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAdmin(PerunSession sess, Resource resource, User user) throws UserNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where user_id=? and resource_id=? and role_id=(select id from roles where name=?)",
          user.getId(), resource.getId(), Role.RESOURCEADMIN.toLowerCase())) {
        throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the resource " + resource);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAdmin(PerunSession sess, Resource resource, Group group) throws GroupNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where authorized_group_id=? and resource_id=? and role_id=(select id from roles where " +
              "name=?)", group.getId(), resource.getId(), Role.RESOURCEADMIN.toLowerCase())) {
        throw new GroupNotAdminException("Group id=" + group.getId() + " is not admin of the resource " + resource);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAdmin(PerunSession sess, User sponsoredUser, User user) throws UserNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where user_id=? and sponsored_user_id=? and role_id=(select id from roles where name=?)",
          user.getId(), sponsoredUser.getId(), Role.SPONSOR.toLowerCase())) {
        throw new UserNotAdminException(
            "User id=" + user.getId() + " is not sponsor of the sponsored user " + sponsoredUser);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAdmin(PerunSession sess, User sponsoredUser, Group group) throws GroupNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where authorized_group_id=? and sponsored_user_id=? and role_id=(select id from roles " +
              "where name=?)", group.getId(), sponsoredUser.getId(), Role.SPONSOR.toLowerCase())) {
        throw new GroupNotAdminException(
            "Group id=" + group.getId() + " is not sponsor of the sponsored user " + sponsoredUser);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAdmin(PerunSession sess, Group group, User user) throws UserNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where user_id=? and group_id=? and role_id=(select id from roles where name=?)",
          user.getId(), group.getId(), Role.GROUPADMIN.toLowerCase())) {
        throw new UserNotAdminException("User id=" + user.getId() + " is not admin of the group " + group);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws GroupNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where authorized_group_id=? and group_id=? and role_id=(select id from roles where " +
              "name=?)", authorizedGroup.getId(), group.getId(), Role.GROUPADMIN.toLowerCase())) {
        throw new GroupNotAdminException("Group id=" + authorizedGroup.getId() + " is not admin of the group " + group);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAllAuthzForFacility(PerunSession sess, Facility facility) {
    try {
      jdbc.update("delete from authz where facility_id=?", facility.getId());
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public void removeAllAuthzForGroup(PerunSession sess, Group group) {
    try {
      jdbc.update("delete from authz where group_id=?", group.getId());
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public void removeAllAuthzForResource(PerunSession sess, Resource resource) {
    try {
      jdbc.update("delete from authz where resource_id=?", resource.getId());
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public void removeAllAuthzForService(PerunSession sess, Service service) {
    try {
      jdbc.update("delete from authz where service_id=?", service.getId());
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public void removeAllAuthzForVo(PerunSession sess, Vo vo) {
    try {
      jdbc.update("delete from authz where vo_id=?", vo.getId());
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public void removeAllSponsoredUserAuthz(PerunSession sess, User sponsoredUser) {
    try {
      jdbc.update("delete from authz where sponsored_user_id=?", sponsoredUser.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAllUserAuthz(PerunSession sess, User user) {
    try {
      jdbc.update("delete from authz where user_id=?", user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeCabinetAdmin(PerunSession sess, User user) throws UserNotAdminException {
    try {
      if (0 == jdbc.update("delete from authz where user_id=? and role_id=(select id from roles where name=?)",
          user.getId(), Role.CABINETADMIN.toLowerCase())) {
        throw new UserNotAdminException("User id=" + user.getId() + " is not cabinet admin.");
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removePerunAdmin(PerunSession sess, User user) throws UserNotAdminException {
    try {
      if (0 == jdbc.update("delete from authz where user_id=? and role_id=(select id from roles where name=?)",
          user.getId(), Role.PERUNADMIN.toLowerCase())) {
        throw new UserNotAdminException("User id=" + user.getId() + " is not perun admin.");
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removePerunObserver(PerunSession sess, User user) throws UserNotAdminException {
    try {
      if (0 == jdbc.update("delete from authz where user_id=? and role_id=(select id from roles where name=?)",
          user.getId(), Role.PERUNOBSERVER.toLowerCase())) {
        throw new UserNotAdminException("User id=" + user.getId() + " is not perun observer.");
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removePerunObserverFromAuthorizedGroup(PerunSession sess, Group authorizedGroup)
      throws GroupNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where authorized_group_id=? and role_id=(select id from roles where name=?)",
          authorizedGroup.getId(), Role.PERUNOBSERVER.toLowerCase())) {
        throw new GroupNotAdminException("Group id=" + authorizedGroup.getId() + " is not perun observer.");
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeResourceRole(PerunSession sess, String role, Resource resource, User user)
      throws UserNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where user_id=? and resource_id=? and role_id=(select id from roles where name=?)",
          user.getId(), resource.getId(), role.toLowerCase())) {
        throw new UserNotAdminException("User id=" + user.getId() + " is not " + role + " in the resource " + resource);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeResourceRole(PerunSession sess, String role, Resource resource, Group group)
      throws GroupNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where authorized_group_id=? and resource_id=? and role_id=(select id from roles where " +
              "name=?)", group.getId(), resource.getId(), role.toLowerCase())) {
        throw new GroupNotAdminException(
            "Group id=" + group.getId() + " is not " + role + " in the resource " + resource);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeVoRole(PerunSession sess, String role, Vo vo, User user) throws UserNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where user_id=? and vo_id=? and role_id=(select id from roles where name=?)", user.getId(),
          vo.getId(), role.toLowerCase())) {
        throw new UserNotAdminException("User id=" + user.getId() + " is not " + role + " in the vo " + vo);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeVoRole(PerunSession sess, String role, Vo vo, Group group) throws GroupNotAdminException {
    try {
      if (0 == jdbc.update(
          "delete from authz where authorized_group_id=? and vo_id=? and role_id=(select id from roles where name=?)",
          group.getId(), vo.getId(), role.toLowerCase())) {
        throw new GroupNotAdminException("Group id=" + group.getId() + " is not " + role + " in the vo " + vo);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public boolean roleExists(String role) {
    if (role == null) {
      return false;
    }
    try {
      return 1 == jdbc.queryForInt("select count(*) from roles where name=?", role.toLowerCase());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  public void setPerunRolesLoader(PerunRolesLoader perunRolesLoader) {
    this.perunRolesLoader = perunRolesLoader;
  }

  @Override
  public void setRole(PerunSession sess, Map<String, Integer> mappingOfValues, String role)
      throws RoleAlreadySetException {
    Map<String, Object> genericMappingOfValues = prepareMappingToSetRole(mappingOfValues);
    genericMappingOfValues.put("created_by", sess.getPerunPrincipal().getActor());
    genericMappingOfValues.put("created_by_uid", sess.getPerunPrincipal().getUserId());

    SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc);
    insert.withTableName("authz");
    insert.usingColumns(genericMappingOfValues.keySet().toArray(new String[0]));
    try {
      insert.execute(genericMappingOfValues);
    } catch (DataIntegrityViolationException e) {
      throw new RoleAlreadySetException(role);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void unsetRole(PerunSession sess, Map<String, Integer> mappingOfValues, String role)
      throws RoleNotSetException {
    String query = prepareQueryToUnsetRole(mappingOfValues);

    try {
      if (0 == jdbc.update(query)) {
        throw new RoleNotSetException(role);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }
}
