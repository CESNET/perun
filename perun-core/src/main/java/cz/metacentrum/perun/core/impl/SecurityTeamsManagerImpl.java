package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.SecurityTeamsManagerImplApi;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamsManagerImpl implements SecurityTeamsManagerImplApi {

  protected static final String SECURITY_TEAM_MAPPING_SELECT_QUERY =
      "security_teams.id as security_teams_id,security_teams.name as security_teams_name, " +
      "security_teams.description as security_teams_description, " +
      "security_teams.created_at as security_teams_created_at, security_teams.created_by as " +
      "security_teams_created_by, " +
      "security_teams.modified_by as security_teams_modified_by, security_teams.modified_at as " +
      "security_teams_modified_at, " +
      "security_teams.created_by_uid as security_teams_created_by_uid, security_teams.modified_by_uid as " +
      "security_teams_modified_by_uid";
  /**
   * Converts s ResultSet's row to a SecurityTeam instance.
   */
  protected static final RowMapper<SecurityTeam> SECURITY_TEAM_MAPPER =
      (resultSet, i) -> new SecurityTeam(resultSet.getInt("security_teams_id"),
          resultSet.getString("security_teams_name"), resultSet.getString("security_teams_description"),
          resultSet.getString("security_teams_created_at"), resultSet.getString("security_teams_created_by"),
          resultSet.getString("security_teams_modified_at"), resultSet.getString("security_teams_modified_by"),
          resultSet.getInt("security_teams_created_by_uid") == 0 ? null :
              resultSet.getInt("security_teams_created_by_uid"),
          resultSet.getInt("security_teams_modified_by_uid") == 0 ? null :
              resultSet.getInt("security_teams_modified_by_uid"));
  private static final Logger LOG = LoggerFactory.getLogger(SecurityTeamsManagerImpl.class);
  private final JdbcPerunTemplate jdbc;

  /**
   * Create new instance of this class.
   */
  public SecurityTeamsManagerImpl(DataSource perunPool) {
    this.jdbc = new JdbcPerunTemplate(perunPool);
    this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

  @Override
  public void addUserToBlacklist(PerunSession sess, SecurityTeam securityTeam, User user, String description) {
    if (description != null && description.trim().isEmpty()) {
      description = null;
    }
    try {
      jdbc.update(
          "insert into blacklists(security_team_id, user_id, description, created_by, created_at, modified_by, " +
          "modified_at, created_by_uid, modified_by_uid) " + "values (?,?,?,?," + Compatibility.getSysdate() + ",?," +
          Compatibility.getSysdate() + ",?,?)", securityTeam.getId(), user.getId(), description,
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void checkGroupIsNotSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, Group group)
      throws AlreadyAdminException {
    if (isGroupSecurityAdmin(group, securityTeam)) {
      throw new AlreadyAdminException("Group " + group + " is already admin of " + securityTeam);
    }
  }

  @Override
  public void checkGroupIsSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, Group group)
      throws GroupNotAdminException {
    if (!isGroupSecurityAdmin(group, securityTeam)) {
      throw new GroupNotAdminException("Group " + group + " is not admin of " + securityTeam);
    }
  }

  @Override
  public void checkSecurityTeamExists(PerunSession sess, SecurityTeam securityTeam)
      throws SecurityTeamNotExistsException {
    if (!securityTeamExists(securityTeam)) {
      throw new SecurityTeamNotExistsException("Security Team " + securityTeam + " doesn't exist in DB");
    }
  }

  @Override
  public void checkSecurityTeamNotExists(PerunSession sess, SecurityTeam securityTeam)
      throws SecurityTeamExistsException {
    if (securityTeamExists(securityTeam)) {
      throw new SecurityTeamExistsException("Security Team " + securityTeam + " already exists in DB");
    }
  }

  @Override
  public void checkSecurityTeamUniqueName(PerunSession sess, SecurityTeam securityTeam)
      throws SecurityTeamExistsException {
    try {
      int number = jdbc.queryForInt("select count(1) from security_teams where name=?", securityTeam.getName());
      if (number == 1) {
        throw new SecurityTeamExistsException(
            "Name of security team " + securityTeam + " is not unique. It already exists.");
      } else if (number > 1) {
        throw new ConsistencyErrorException(
            "Security teams with the same name as security team" + securityTeam + " exist multiple times.");
      }
    } catch (EmptyResultDataAccessException ex) {
      // is ok. No row with same name was founded
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void checkUserIsNotSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, User user)
      throws AlreadyAdminException {
    if (isUserSecurityAdmin(user, securityTeam)) {
      throw new AlreadyAdminException("User " + user + " is already admin of " + securityTeam);
    }
  }

  @Override
  public void checkUserIsSecurityAdmin(PerunSession sess, SecurityTeam securityTeam, User user)
      throws UserNotAdminException {
    if (!isUserSecurityAdmin(user, securityTeam)) {
      throw new UserNotAdminException("User " + user + " is not admin of " + securityTeam);
    }
  }

  @Override
  public SecurityTeam createSecurityTeam(PerunSession sess, SecurityTeam securityTeam) {

    Utils.notNull(securityTeam, "securityTeam");
    // we do not store empty string in description
    if (securityTeam.getDescription() != null && securityTeam.getDescription().trim().isEmpty()) {
      securityTeam.setDescription(null);
    }

    // Get SecurityTeam ID
    int securityTeamId;
    try {
      securityTeamId = Utils.getNewId(jdbc, "security_teams_id_seq");
      jdbc.update("insert into security_teams(id, name, description, created_by, modified_by, created_by_uid, " +
                  "modified_by_uid) values (?,?,?,?,?,?,?)", securityTeamId, securityTeam.getName(),
          securityTeam.getDescription(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

    // set assigned id
    securityTeam.setId(securityTeamId);

    return securityTeam;
  }

  @Override
  public void deleteSecurityTeam(PerunSession sess, SecurityTeam securityTeam) throws SecurityTeamNotExistsException {
    try {
      // Delete authz entries for this Security team
      AuthzResolverBlImpl.removeAllAuthzForSecurityTeam(sess, securityTeam);

      if (jdbc.update("delete from security_teams where id=?", securityTeam.getId()) == 0) {
        throw new ConsistencyErrorException("no record was deleted from the DB.");
      }
    } catch (ConsistencyErrorException e) {
      throw new SecurityTeamNotExistsException(e);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

    LOG.debug("SecurityTeam {} deleted", securityTeam);
  }

  @Override
  public List<Group> getAdminGroups(PerunSession sess, SecurityTeam securityTeam) {
    try {
      return jdbc.query("select " + GroupsManagerImpl.GROUP_MAPPING_SELECT_QUERY +
                        " from authz join groups on authz.authorized_group_id=groups.id" +
                        " where authz.security_team_id=?", GroupsManagerImpl.GROUP_MAPPER, securityTeam.getId());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> getAdmins(PerunSession sess, SecurityTeam securityTeam) {
    try {
      List<User> list = jdbc.query("select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY + " from users inner join (" +
                                   "select user_id from authz where security_team_id=? " + "UNION (" +
                                   "select user_id from members inner join (" +
                                   "select gm.member_id, gm.group_id from groups_members " +
                                   Compatibility.getAsAlias("gm") + " inner join " +
                                   "authz on authz.authorized_group_id=gm.group_id where authz.security_team_id=? " +
                                   ") " + Compatibility.getAsAlias("member_group_ids") +
                                   " on members.id=member_group_ids.member_id)" + ") " +
                                   Compatibility.getAsAlias("user_ids") + " on users.id=user_ids.user_id",
          UsersManagerImpl.USER_MAPPER, securityTeam.getId(), securityTeam.getId());

      return list;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<SecurityTeam> getAllSecurityTeams(PerunSession sess) {
    try {
      List<SecurityTeam> list =
          jdbc.query("select " + SECURITY_TEAM_MAPPING_SELECT_QUERY + " from security_teams", SECURITY_TEAM_MAPPER);

      return list;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<User> getBlacklist(PerunSession sess, List<SecurityTeam> securityTeams) {
    try {
      Set<User> blacklisted = new HashSet<>();
      List<User> list;
      for (SecurityTeam st : securityTeams) {

        list = jdbc.query("select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY + " from users inner join (" +
                          "select blacklists.user_id from blacklists where security_team_id=?" + ") " +
                          Compatibility.getAsAlias("blacklisted_ids") + " ON users.id=blacklisted_ids.user_id",
            UsersManagerImpl.USER_MAPPER, st.getId());

        blacklisted.addAll(list);

      }
      return new ArrayList<>(blacklisted);

    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Pair<User, String>> getBlacklistWithDescription(PerunSession sess, List<SecurityTeam> securityTeams) {
    try {
      List<Pair<User, String>> result = new ArrayList<>();
      for (SecurityTeam st : securityTeams) {

        result = jdbc.query("select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY +
                            ",  blacklisted_ids.description as description from users inner join (" +
                            "select blacklists.user_id, blacklists.description from blacklists where " +
                            "security_team_id=?" +
                            ") " + Compatibility.getAsAlias("blacklisted_ids") + " ON users.id=blacklisted_ids.user_id",
            UsersManagerImpl.USERBLACKLIST_EXTRACTOR, st.getId());
      }
      return result;

    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<User> getDirectAdmins(PerunSession sess, SecurityTeam securityTeam) {
    try {
      return jdbc.query(
          "select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY + " from authz join users on authz.user_id=users.id" +
          "  where authz.security_team_id=? ", UsersManagerImpl.USER_MAPPER, securityTeam.getId());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public SecurityTeam getSecurityTeamById(PerunSession sess, int id) throws SecurityTeamNotExistsException {
    try {
      return jdbc.queryForObject("select " + SECURITY_TEAM_MAPPING_SELECT_QUERY + " from security_teams where id=?",
          SECURITY_TEAM_MAPPER, id);
    } catch (EmptyResultDataAccessException ex) {
      throw new SecurityTeamNotExistsException(ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public SecurityTeam getSecurityTeamByName(PerunSession sess, String name) throws SecurityTeamNotExistsException {
    try {
      return jdbc.queryForObject("select " + SECURITY_TEAM_MAPPING_SELECT_QUERY + " from security_teams where name=?",
          SECURITY_TEAM_MAPPER, name);
    } catch (EmptyResultDataAccessException ex) {
      throw new SecurityTeamNotExistsException(ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  private boolean isGroupSecurityAdmin(Group group, SecurityTeam securityTeam) {
    try {
      int number = jdbc.queryForInt("select count(1) from authz where authorized_group_id=? and security_team_id=?",
          group.getId(), securityTeam.getId());
      if (number == 1) {
        return true;
      } else if (number > 1) {
        throw new ConsistencyErrorException(
            "Group " + group + " is security admin more times of one security team " + securityTeam);
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public boolean isUserBlacklisted(PerunSession sess, SecurityTeam securityTeam, User user) {
    try {
      int number = jdbc.queryForInt("select count(1) from blacklists where security_team_id=? and user_id=?",
          securityTeam.getId(), user.getId());
      if (number == 1) {
        return true;
      } else if (number > 1) {
        throw new ConsistencyErrorException(
            "User " + user + " is blacklisted multiple in security team " + securityTeam);
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public boolean isUserBlacklisted(PerunSession sess, User user) {
    try {
      int number = jdbc.queryForInt("select count(1) from blacklists where user_id=?", user.getId());
      if (number >= 1) {
        return true;
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  private boolean isUserSecurityAdmin(User user, SecurityTeam securityTeam) {
    try {
      int number = jdbc.queryForInt("select count(1) from authz " +
                                    " left outer join groups_members on groups_members.group_id=authz" +
                                    ".authorized_group_id " +
                                    " left outer join members on members.id=groups_members.member_id " +
                                    " where (authz.user_id=? or members.user_id=?) and security_team_id=? ",
          user.getId(), user.getId(), securityTeam.getId());
      if (number > 0) {
        return true;
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeUserFromAllBlacklists(PerunSession sess, User user) {
    try {
      jdbc.update("delete from blacklists where and user_id=?", user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeUserFromBlacklist(PerunSession sess, SecurityTeam securityTeam, User user) {
    try {
      jdbc.update("delete from blacklists where security_team_id=? and user_id=?", securityTeam.getId(), user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  private boolean securityTeamExists(SecurityTeam securityTeam) {
    try {
      int number = jdbc.queryForInt("select count(1) from security_teams where id=?", securityTeam.getId());
      if (number == 1) {
        return true;
      } else if (number > 1) {
        throw new ConsistencyErrorException("Security Team " + securityTeam + " exists multiple in DB");
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public SecurityTeam updateSecurityTeam(PerunSession sess, SecurityTeam securityTeam)
      throws SecurityTeamNotExistsException {

    Utils.notNull(securityTeam, "securityTeam");
    // we do not store empty string in description
    if (securityTeam.getDescription() != null && securityTeam.getDescription().trim().isEmpty()) {
      securityTeam.setDescription(null);
    }

    try {
      Map<String, Object> map =
          jdbc.queryForMap("select name, description from security_teams where id=?", securityTeam.getId());

      if (!Objects.equals(securityTeam.getName(), map.get("name"))) {
        jdbc.update("update security_teams set name=?, modified_by=?, modified_by_uid=?, modified_at=" +
                    Compatibility.getSysdate() + "  where id=?", securityTeam.getName(),
            sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), securityTeam.getId());
      }

      if (!Objects.equals(securityTeam.getDescription(), map.get("description"))) {
        jdbc.update("update security_teams set description=?, modified_by=?, modified_by_uid=?, modified_at=" +
                    Compatibility.getSysdate() + "  where id=?", securityTeam.getDescription(),
            sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), securityTeam.getId());
      }

      return securityTeam;
    } catch (EmptyResultDataAccessException ex) {
      throw new SecurityTeamNotExistsException("Updating non existing SecurityTeam", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }
}
