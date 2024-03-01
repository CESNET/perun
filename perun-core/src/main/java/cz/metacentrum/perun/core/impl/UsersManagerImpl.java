package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.BlockedLogin;
import cz.metacentrum.perun.core.api.BlockedLoginsPageQuery;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupResourceStatus;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Sponsorship;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersPageQuery;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetLinkExpiredException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetLinkNotValidException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SpecificUserOwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsAlreadyBlockedException;
import cz.metacentrum.perun.core.api.exceptions.LoginIsNotBlockedException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;
import cz.metacentrum.perun.core.implApi.UsersManagerImplApi;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static cz.metacentrum.perun.core.impl.AttributesManagerImpl.KEY_VALUE_DELIMITER;
import static cz.metacentrum.perun.core.impl.AttributesManagerImpl.LIST_DELIMITER;
import static cz.metacentrum.perun.core.impl.AttributesManagerImpl.MAX_SIZE_FOR_IN_CLAUSE;
import static cz.metacentrum.perun.core.impl.MembersManagerImpl.MEMBER_SPONSORSHIP_MAPPER;
import static cz.metacentrum.perun.core.impl.MembersManagerImpl.memberSponsorshipSelectQuery;
import static cz.metacentrum.perun.core.impl.ResourcesManagerImpl.RESOURCE_MAPPER;
import static cz.metacentrum.perun.core.impl.ResourcesManagerImpl.RICH_RESOURCE_WITH_TAGS_EXTRACTOR;
import static cz.metacentrum.perun.core.impl.ResourcesManagerImpl.resourceMappingSelectQuery;
import static cz.metacentrum.perun.core.impl.ResourcesManagerImpl.resourceTagMappingSelectQuery;
import static cz.metacentrum.perun.registrar.model.ApplicationFormItem.Type.USERNAME;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

/**
 * UsersManager implementation.
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 * @author Sona Mastrakova
 */
public class UsersManagerImpl implements UsersManagerImplApi {

  public static final RowMapper<BlockedLogin> BLOCKED_LOGINS_MAPPER = new RowMapper<BlockedLogin>() {
    @Override
    public BlockedLogin mapRow(ResultSet rs, int rowNum) throws SQLException {
      int id = rs.getInt("id");
      String login = rs.getString("login");
      String namespace = rs.getString("namespace");
      return new BlockedLogin(id, login, namespace);
    }
  };
  // Part of the SQL script used for getting the User object
  protected final static String userMappingSelectQuery =
      "users.id as users_id, users.uu_id as users_uu_id, users.first_name as users_first_name, users.last_name as users_last_name, " +
          "users.middle_name as users_middle_name, users.title_before as users_title_before, users.title_after as users_title_after, " +
          "users.created_at as users_created_at, users.created_by as users_created_by, users.modified_by as users_modified_by, users.modified_at as users_modified_at, " +
          "users.sponsored_acc as users_sponsored_acc, users.service_acc as users_service_acc, users.created_by_uid as users_created_by_uid, users.modified_by_uid as users_modified_by_uid";
  protected final static String userExtSourceMappingSelectQuery =
      "user_ext_sources.id as user_ext_sources_id, user_ext_sources.login_ext as user_ext_sources_login_ext, " +
          "user_ext_sources.user_id as user_ext_sources_user_id, user_ext_sources.loa as user_ext_sources_loa, user_ext_sources.created_at as user_ext_sources_created_at, user_ext_sources.created_by as user_ext_sources_created_by, " +
          "user_ext_sources.modified_by as user_ext_sources_modified_by, user_ext_sources.modified_at as user_ext_sources_modified_at, " +
          "user_ext_sources.created_by_uid as ues_created_by_uid, user_ext_sources.modified_by_uid as ues_modified_by_uid, user_ext_sources.last_access as ues_last_access";
  protected static final RowMapper<User> USER_MAPPER = (resultSet, i) ->
      new User(resultSet.getInt("users_id"), resultSet.getObject("users_uu_id", UUID.class),
          resultSet.getString("users_first_name"), resultSet.getString("users_last_name"),
          resultSet.getString("users_middle_name"), resultSet.getString("users_title_before"),
          resultSet.getString("users_title_after"),
          resultSet.getString("users_created_at"), resultSet.getString("users_created_by"),
          resultSet.getString("users_modified_at"), resultSet.getString("users_modified_by"),
          resultSet.getBoolean("users_service_acc"),
          resultSet.getBoolean("users_sponsored_acc"),
          resultSet.getInt("users_created_by_uid") == 0 ? null : resultSet.getInt("users_created_by_uid"),
          resultSet.getInt("users_modified_by_uid") == 0 ? null : resultSet.getInt("users_modified_by_uid"));
  protected static final ResultSetExtractor<List<Pair<User, String>>> USERBLACKLIST_EXTRACTOR = resultSet -> {
    List<Pair<User, String>> result = new ArrayList<>();

    int row = 0;
    while (resultSet.next()) {
      result.add(new Pair<>(USER_MAPPER.mapRow(resultSet, row), resultSet.getString("description")));
      row++;
    }

    return result;
  };
  private final static Logger log = LoggerFactory.getLogger(UsersManagerImpl.class);
  // time window size for mail validation if not taken from peruns configuration file
  private final static int VALIDATION_ALLOWED_HOURS = 6;
  // If user extSource is older than 'number' months, it is not defined as ACTIVE in methods
  // INACTIVE userExtSources are skipped in counting max loa for user
  private static final int MAX_OLD_OF_ACTIVE_USER_EXTSOURCE = 13;
  private static final Map<String, Pattern> userExtSourcePersistentPatterns;
  protected static final RowMapper<UserExtSource> USEREXTSOURCE_MAPPER = new RowMapper<UserExtSource>() {
    @Override
    public UserExtSource mapRow(ResultSet rs, int i) throws SQLException {
      ExtSource extSource = new ExtSource();
      extSource.setId(rs.getInt("ext_sources_id"));
      extSource.setName(rs.getString("ext_sources_name"));
      extSource.setType(rs.getString("ext_sources_type"));
      extSource.setCreatedAt(rs.getString("ext_sources_created_at"));
      extSource.setCreatedBy(rs.getString("ext_sources_created_by"));
      extSource.setModifiedAt(rs.getString("ext_sources_modified_at"));
      extSource.setModifiedBy(rs.getString("ext_sources_modified_by"));
      if (rs.getInt("ext_sources_modified_by_uid") == 0) {
        extSource.setModifiedByUid(null);
      } else {
        extSource.setModifiedByUid(rs.getInt("ext_sources_modified_by_uid"));
      }
      if (rs.getInt("ext_sources_created_by_uid") == 0) {
        extSource.setCreatedByUid(null);
      } else {
        extSource.setCreatedByUid(rs.getInt("ext_sources_created_by_uid"));
      }

      boolean persistent = false;
      Pattern p = userExtSourcePersistentPatterns.get(rs.getString("ext_sources_name"));
      if (p != null) {
        if (p.matcher(rs.getString("user_ext_sources_login_ext")).matches()) {
          persistent = true;
        }
      }

      return new UserExtSource(rs.getInt("user_ext_sources_id"), extSource, rs.getString("user_ext_sources_login_ext"),
          rs.getInt("user_ext_sources_user_id"), rs.getInt("user_ext_sources_loa"), persistent,
          rs.getString("user_ext_sources_created_at"), rs.getString("user_ext_sources_created_by"),
          rs.getString("user_ext_sources_modified_at"), rs.getString("user_ext_sources_modified_by"),
          rs.getInt("ues_created_by_uid") == 0 ? null : rs.getInt("ues_created_by_uid"),
          rs.getInt("ues_modified_by_uid") == 0 ? null : rs.getInt("ues_modified_by_uid"),
          rs.getString("ues_last_access"));
    }
  };

  static {
    // Prepare userExtSourcePersistentPatterns for matching regex from perun property file.
    // It is done in advance because of performance.
    userExtSourcePersistentPatterns = new HashMap<>();
    String persistentConfig = BeansUtils.getCoreConfig().getUserExtSourcesPersistent();
    for (String extSource : persistentConfig.split(";")) {
      String[] extSourceTuple = extSource.split(",", 2);
      if (extSourceTuple.length > 1) {
        userExtSourcePersistentPatterns.put(extSourceTuple[0], Pattern.compile(extSourceTuple[1]));
      } else {
        userExtSourcePersistentPatterns.put(extSource, Pattern.compile(".*"));
      }
    }
  }

  private JdbcPerunTemplate jdbc;
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  /**
   * Constructor.
   *
   * @param perunPool connection pool
   */
  public UsersManagerImpl(DataSource perunPool) {
    this.jdbc = new JdbcPerunTemplate(perunPool);
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
    this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
    this.namedParameterJdbcTemplate.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

  /**
   * Returns ResultSetExtractor that can be used to extract returned paginated users
   * from db.
   *
   * @param query query data
   * @return extractor, that can be used to extract returned paginated users from db
   */
  private static ResultSetExtractor<Paginated<User>> getPaginatedUsersExtractor(UsersPageQuery query) {
    return resultSet -> {
      List<User> users = new ArrayList<>();
      int total_count = 0;
      int row = 0;
      while (resultSet.next()) {
        total_count = resultSet.getInt("total_count");
        users.add(USER_MAPPER.mapRow(resultSet, row));
        row++;
      }
      return new Paginated<>(users, query.getOffset(), query.getPageSize(), total_count);
    };
  }

  /**
   * Returns ResultSetExtractor that can be used to extract Sponsors and Sponsorships for the
   * given members.
   *
   * @return extractor, that can be used to extract Sponsors and Sponsorships for the memberIds
   */
  private static ResultSetExtractor<Map<Integer, List<Pair<User, Sponsorship>>>> getSponsorsExtractor() {
    return resultSet -> {
      Map<Integer, List<Pair<User, Sponsorship>>> memberIdSponsorsMap = new HashMap<>();
      while (resultSet.next()) {
        int memberId = resultSet.getInt("member_id");
        User sponsor = USER_MAPPER.mapRow(resultSet, resultSet.getRow());
        Sponsorship sponsorship = MEMBER_SPONSORSHIP_MAPPER.mapRow(resultSet, resultSet.getRow());
        if (!memberIdSponsorsMap.containsKey(memberId)) {
          memberIdSponsorsMap.put(memberId, new ArrayList<>());
        }
        memberIdSponsorsMap.get(memberId).add(new Pair<>(sponsor, sponsorship));
      }
      return memberIdSponsorsMap;
    };
  }

  /**
   * Extractor for paginated blocked logins
   *
   * @param query for blocked logins
   * @return extractor to extract login, namespace pair
   */
  private static ResultSetExtractor<Paginated<BlockedLogin>> getPaginatedBlockedLoginsExtractor(
      BlockedLoginsPageQuery query) {
    return resultSet -> {
      List<BlockedLogin> blockedLogins = new ArrayList<>();
      int total_count = 0;
      int row = 0;
      while (resultSet.next()) {
        total_count = resultSet.getInt("total_count");
        blockedLogins.add(BLOCKED_LOGINS_MAPPER.mapRow(resultSet, row));
        row++;
      }
      return new Paginated<>(blockedLogins, query.getOffset(), query.getPageSize(), total_count);
    };
  }

  @Override
  public User getUserById(PerunSession sess, int id) throws UserNotExistsException {
    try {
      return jdbc.queryForObject("select " + userMappingSelectQuery + " from users where users.id=? ", USER_MAPPER, id);
    } catch (EmptyResultDataAccessException ex) {
      throw new UserNotExistsException("user id=" + id);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public User getUserByUserExtSource(PerunSession sess, UserExtSource userExtSource) throws UserNotExistsException {
    try {
      return jdbc.queryForObject("select " + userMappingSelectQuery +
              " from users, user_ext_sources " +
              "where users.id=user_ext_sources.user_id and user_ext_sources.login_ext=? and user_ext_sources.ext_sources_id=? ",
          USER_MAPPER, userExtSource.getLogin(), userExtSource.getExtSource().getId());
    } catch (EmptyResultDataAccessException ex) {
      throw new UserNotExistsException("userExtSource=" + userExtSource.toString());
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login) {
    try {
      return jdbc.query("select " + userMappingSelectQuery +
          " from users join user_ext_sources on users.id=user_ext_sources.user_id join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id"
          + " where ext_sources.type=? and user_ext_sources.login_ext=?", USER_MAPPER, extSourceType, login);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public User getUserByMember(PerunSession sess, Member member) {
    try {
      return jdbc.queryForObject("select " + userMappingSelectQuery + " from users, members " +
          "where members.id=? and members.user_id=users.id", USER_MAPPER, member.getId());
    } catch (EmptyResultDataAccessException ex) {
      throw new ConsistencyErrorException("Member has to have a corresponding User", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<User> getUsersByVo(PerunSession sess, Vo vo) {
    try {
      return jdbc.query("select " + userMappingSelectQuery + " from users, members " +
          "where members.user_id=users.id and members.vo_id=?", USER_MAPPER, vo.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> getUsers(PerunSession sess) {
    try {
      return jdbc.query("select " + userMappingSelectQuery +
          "  from users", USER_MAPPER);
    } catch (EmptyResultDataAccessException ex) {
      // Return empty list
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> getSpecificUsersByUser(PerunSession sess, User user) {
    try {
      return jdbc.query("select " + userMappingSelectQuery +
              " from users, specific_user_users where users.id=specific_user_users.specific_user_id and specific_user_users.status=0 and specific_user_users.user_id=?",
          USER_MAPPER, user.getId());
    } catch (EmptyResultDataAccessException ex) {
      // Return empty list
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> getUsersBySpecificUser(PerunSession sess, User specificUser) {
    try {
      return jdbc.query("select " + userMappingSelectQuery +
              " from users, specific_user_users where users.id=specific_user_users.user_id and specific_user_users.status=0 and specific_user_users.specific_user_id=? " +
              " and specific_user_users.type=?", USER_MAPPER, specificUser.getId(),
          specificUser.getMajorSpecificType().getSpecificUserType());
    } catch (EmptyResultDataAccessException ex) {
      // Return empty list
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeSpecificUserOwner(PerunSession sess, User user, User specificUser)
      throws SpecificUserOwnerAlreadyRemovedException {
    try {
      int numAffected = jdbc.update(
          "delete from specific_user_users where user_id=? and specific_user_id=? and specific_user_users.type=?",
          user.getId(), specificUser.getId(), specificUser.getMajorSpecificType().getSpecificUserType());
      if (numAffected == 0) {
        throw new SpecificUserOwnerAlreadyRemovedException(
            "SpecificUser-Owner: " + user + " , SpecificUser: " + specificUser);
      }

    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public void addSpecificUserOwner(PerunSession sess, User user, User specificUser) {
    try {
      jdbc.update(
          "insert into specific_user_users(user_id,specific_user_id,status,created_by_uid,modified_at,type) values (?,?,0,?," +
              Compatibility.getSysdate() + ",?)",
          user.getId(), specificUser.getId(), sess.getPerunPrincipal().getUserId(),
          specificUser.getMajorSpecificType().getSpecificUserType());

    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public void enableOwnership(PerunSession sess, User user, User specificUser) {
    try {
      jdbc.update("update specific_user_users set status=0, modified_at=" + Compatibility.getSysdate() +
              ", modified_by_uid=? where user_id=? and specific_user_id=? and type=?",
          sess.getPerunPrincipal().getUserId(), user.getId(), specificUser.getId(),
          specificUser.getMajorSpecificType().getSpecificUserType());
    } catch (RuntimeException er) {
      throw new InternalErrorException(er);
    }
  }

  @Override
  public void disableOwnership(PerunSession sess, User user, User specificUser) {
    try {
      jdbc.update("update specific_user_users set status=1, modified_at=" + Compatibility.getSysdate() +
              ", modified_by_uid=? where user_id=? and specific_user_id=? and type=?",
          sess.getPerunPrincipal().getUserId(), user.getId(), specificUser.getId(),
          specificUser.getMajorSpecificType().getSpecificUserType());
    } catch (RuntimeException er) {
      throw new InternalErrorException(er);
    }
  }

  @Override
  public boolean specificUserOwnershipExists(PerunSession sess, User user, User specificUser) {
    try {
      int numberOfExistences =
          jdbc.queryForInt("select count(1) from specific_user_users where user_id=? and specific_user_id=? and type=?",
              user.getId(), specificUser.getId(), specificUser.getMajorSpecificType().getSpecificUserType());
      if (numberOfExistences == 1) {
        return true;
      } else if (numberOfExistences > 1) {
        throw new ConsistencyErrorException(
            "Ownership between user " + user + " and specificUser " + specificUser + " exists more than once.");
      }
      return false;
    } catch (EmptyResultDataAccessException e) {
      return false;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> getSpecificUsers(PerunSession sess) {
    try {
      return jdbc.query("select " + userMappingSelectQuery +
          "  from users where users.service_acc=true or users.sponsored_acc=true", USER_MAPPER);
    } catch (EmptyResultDataAccessException ex) {
      // Return empty list
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void deleteUser(PerunSession sess, User user)
      throws UserAlreadyRemovedException, SpecificUserAlreadyRemovedException {
    try {
      // delete all relations like  user -> sponsor -> service
      jdbc.update("delete from specific_user_users where specific_user_id=? or user_id=?", user.getId(), user.getId());
      int numAffected = jdbc.update("delete from users where id=?", user.getId());
      if (numAffected == 0) {
        if (user.isSpecificUser()) {
          throw new SpecificUserAlreadyRemovedException("SpecificUser: " + user);
        }
        throw new UserAlreadyRemovedException("User: " + user);
      }
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public User createUser(PerunSession sess, User user) {
    int newId;
    try {
      newId = Utils.getNewId(jdbc, "users_id_seq");
      jdbc.update(
          "insert into users(id,first_name,last_name,middle_name,title_before,title_after,created_by,modified_by,service_acc,sponsored_acc,created_by_uid,modified_by_uid)" +
              " values (?,?,?,?,?,?,?,?,?,?,?,?)", newId, user.getFirstName(), user.getLastName(), user.getMiddleName(),
          user.getTitleBefore(), user.getTitleAfter(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getActor(), user.isServiceUser(), user.isSponsoredUser(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
    User newUser;
    try {
      newUser = getUserById(sess, newId);
    } catch (UserNotExistsException e) {
      throw new InternalErrorException("Failed to read newly created user with id: " + newId, e);
    }
    user.setId(newId);
    user.setUuid(newUser.getUuid());
    return newUser;
  }

  @Override
  public User setSpecificUserType(PerunSession sess, User user, SpecificUserType specificUserType) {
    try {
      if (specificUserType.equals(SpecificUserType.SERVICE)) {
        jdbc.update("update users set service_acc=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            true, sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
        user.setServiceUser(true);
      } else if (specificUserType.equals(SpecificUserType.SPONSORED)) {
        jdbc.update("update users set sponsored_acc=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            true, sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
        user.setSponsoredUser(true);
      } else {
        throw new InternalErrorException("Unsupported specific user type " + specificUserType.getSpecificUserType());
      }
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }

    return user;
  }

  @Override
  public User unsetSpecificUserType(PerunSession sess, User user, SpecificUserType specificUserType) {
    try {
      if (specificUserType.equals(SpecificUserType.SERVICE)) {
        jdbc.update("update users set service_acc=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            false, sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
        user.setServiceUser(false);
      } else if (specificUserType.equals(SpecificUserType.SPONSORED)) {
        jdbc.update("update users set sponsored_acc=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            false, sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
        user.setSponsoredUser(false);
      } else {
        throw new InternalErrorException("Unsupported specific user type " + specificUserType.getSpecificUserType());
      }
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }

    return user;
  }

  @Override
  public User updateUser(PerunSession sess, User user) {
    try {
      User userDb = jdbc.queryForObject("select " + userMappingSelectQuery + " from users where id=? ", USER_MAPPER,
          user.getId());

      if (userDb == null) {
        throw new ConsistencyErrorException("Updating non existing user");
      }

      if ((user.getFirstName() != null && !user.getFirstName().equals(userDb.getFirstName())) ||
          (user.getFirstName() == null && userDb.getFirstName() != null)) {
        jdbc.update("update users set first_name=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            user.getFirstName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
            user.getId());
        userDb.setFirstName(user.getFirstName());
      }
      if (user.getLastName() != null && !user.getLastName().equals(userDb.getLastName())) {
        jdbc.update("update users set last_name=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            user.getLastName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
            user.getId());
        userDb.setLastName(user.getLastName());
      }
      if ((user.getMiddleName() != null && !user.getMiddleName().equals(userDb.getMiddleName())) ||
          (user.getMiddleName() == null && userDb.getMiddleName() != null)) {
        jdbc.update("update users set middle_name=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            user.getMiddleName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
            user.getId());
        userDb.setMiddleName(user.getMiddleName());
      }
      if ((user.getTitleBefore() != null && !user.getTitleBefore().equals(userDb.getTitleBefore())) ||
          (user.getTitleBefore() == null && userDb.getTitleBefore() != null)) {
        jdbc.update("update users set title_before=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            user.getTitleBefore(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
            user.getId());
        userDb.setTitleBefore(user.getTitleBefore());
      }
      if ((user.getTitleAfter() != null && !user.getTitleAfter().equals(userDb.getTitleAfter())) ||
          (user.getTitleAfter() == null && userDb.getTitleAfter() != null)) {
        jdbc.update("update users set title_after=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            user.getTitleAfter(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
            user.getId());
        userDb.setTitleAfter(user.getTitleAfter());
      }

      return userDb;
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public User updateNameTitles(PerunSession sess, User user) {
    try {
      User userDb = jdbc.queryForObject("select " + userMappingSelectQuery + " from users where id=? ", USER_MAPPER,
          user.getId());

      if (userDb == null) {
        throw new ConsistencyErrorException("Updating titles for non existing user");
      }

      // changed condition to updateUser case to handle: fill, change and remove

      if ((user.getTitleBefore() != null && !user.getTitleBefore().equals(userDb.getTitleBefore())) ||
          (user.getTitleBefore() == null && userDb.getTitleBefore() != null)) {
        jdbc.update("update users set title_before=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            user.getTitleBefore(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
            user.getId());
        userDb.setTitleBefore(user.getTitleBefore());
      }
      if ((user.getTitleAfter() != null && !user.getTitleAfter().equals(userDb.getTitleAfter())) ||
          ((user.getTitleAfter() == null && userDb.getTitleAfter() != null))) {
        jdbc.update("update users set title_after=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            user.getTitleAfter(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
            user.getId());
        userDb.setTitleAfter(user.getTitleAfter());
      }

      return userDb;
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public User anonymizeUser(PerunSession sess, User user) {
    try {
      user.setFirstName("");
      user.setMiddleName("");
      user.setLastName("");
      user.setTitleBefore("");
      user.setTitleAfter("");
      jdbc.update(
          "update users set first_name=NULL, last_name=NULL, middle_name=NULL, title_before=NULL, title_after=NULL, " +
              "anonymized=true, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() +
              " where id=?",
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), user.getId());
      return user;
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public boolean isUserAnonymized(PerunSession sess, User user) {
    try {
      int numberOfExistences =
          jdbc.queryForInt("select count(1) from users where id=? and anonymized=true", user.getId());
      return numberOfExistences == 1;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void updateUserExtSourceLastAccess(PerunSession sess, UserExtSource userExtSource) {
    try {
      jdbc.update("update user_ext_sources set last_access=" + Compatibility.getSysdate() + " where id=?",
          userExtSource.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public UserExtSource updateUserExtSource(PerunSession sess, UserExtSource userExtSource)
      throws UserExtSourceExistsException {
    try {
      UserExtSource userExtSourceDb = jdbc.queryForObject(
          "select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
              " from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where" +
              " user_ext_sources.id=?", USEREXTSOURCE_MAPPER, userExtSource.getId());

      if (userExtSourceDb == null) {
        throw new ConsistencyErrorException("Updating non existing userExtSource");
      }

      if (userExtSource.getLoa() != userExtSourceDb.getLoa()) {
        jdbc.update("update user_ext_sources set loa=?, modified_by=?, modified_by_uid=?, modified_at=" +
                Compatibility.getSysdate() + " where id=?",
            userExtSource.getLoa(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
            userExtSource.getId());
      }
      if (userExtSource.getLogin() != null && !userExtSourceDb.getLogin().equals(userExtSource.getLogin())) {
        try {
          jdbc.update("update user_ext_sources set login_ext=?, modified_by=?, modified_by_uid=?, modified_at=" +
                  Compatibility.getSysdate() + " where id=?",
              userExtSource.getLogin(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
              userExtSource.getId());
        } catch (DuplicateKeyException ex) {
          throw new UserExtSourceExistsException("UES with same login already exists: " + userExtSource);
        }
      }

      return userExtSource;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public UserExtSource addUserExtSource(PerunSession sess, User user, UserExtSource userExtSource) {
    try {
      Utils.notNull(userExtSource.getLogin(), "userExtSource.getLogin");

      int ueaId = Utils.getNewId(jdbc, "user_ext_sources_id_seq");

      log.trace(
          "Adding new user ext source: ueaId {}, user.getId() {}, userExtSource.getLogin() {}, userExtSource.getLoa() {}, userExtSource.getExtSource().getId() {}, " +
              "sess.getPerunPrincipal().getActor() {}, sess.getPerunPrincipal().getActor() {}, " +
              "sess.getPerunPrincipal().getUser().getId() {}, sess.getPerunPrincipal().getUser().getId() {}", ueaId,
          user.getId(), userExtSource.getLogin(),
          userExtSource.getLoa(), userExtSource.getExtSource().getId(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

      if (userExtSource.getLastAccess() != null) {
        // user ext source has last access info
        jdbc.update(
            "insert into user_ext_sources (id, user_id, login_ext, loa, ext_sources_id, last_access, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
                "values (?,?,?,?,?," + Compatibility.toDate("?", "'YYYY-MM-DD HH24:MI:SS.US'") + ",?," +
                Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
            ueaId, user.getId(), userExtSource.getLogin(), userExtSource.getLoa(), userExtSource.getExtSource().getId(),
            userExtSource.getLastAccess(),
            sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
            sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
      } else {
        // adding new user ext source with default current timestamp
        jdbc.update(
            "insert into user_ext_sources (id, user_id, login_ext, loa, ext_sources_id, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
                "values (?,?,?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)",
            ueaId, user.getId(), userExtSource.getLogin(), userExtSource.getLoa(), userExtSource.getExtSource().getId(),
            sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
            sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
      }

      userExtSource.setId(ueaId);
      userExtSource.setUserId(user.getId());

      return userExtSource;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public UserExtSource getUserExtSourceByExtLogin(PerunSession sess, ExtSource source, String extLogin)
      throws UserExtSourceNotExistsException {
    try {
      return jdbc.queryForObject(
          "select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
              " from user_ext_sources left join ext_sources " +
              " on user_ext_sources.ext_sources_id=ext_sources.id " +
              " where ext_sources.id=? and user_ext_sources.login_ext=?", USEREXTSOURCE_MAPPER, source.getId(),
          extLogin);
    } catch (EmptyResultDataAccessException e) {
      throw new UserExtSourceNotExistsException("ExtSource: " + source + " for extLogin " + extLogin, e);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<UserExtSource> getActiveUserExtSources(PerunSession sess, User user) {
    //get now date
    LocalDate date = LocalDate.now();
    date = date.minusMonths(MAX_OLD_OF_ACTIVE_USER_EXTSOURCE);

    try {
      String query =
          "select " + userExtSourceMappingSelectQuery + ", " + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
              " from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where " +
              " user_ext_sources.user_id=? and " +
              " user_ext_sources.last_access > " + Compatibility.toDate("'" + date + "'", "'YYYY-MM-DD'");

      return jdbc.query(query, USEREXTSOURCE_MAPPER, user.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<UserExtSource> getAllUserExtSourcesByTypeAndLogin(PerunSession sess, String extType, String extLogin) {
    try {
      return jdbc.query(
          "select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
              " from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where" +
              " ext_sources.type=? and user_ext_sources.login_ext=?", USEREXTSOURCE_MAPPER, extType, extLogin);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public UserExtSource getUserExtSourceById(PerunSession sess, int id) throws UserExtSourceNotExistsException {
    try {
      return jdbc.queryForObject(
          "select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
              " from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where" +
              " user_ext_sources.id=?", USEREXTSOURCE_MAPPER, id);
    } catch (EmptyResultDataAccessException e) {
      throw new UserExtSourceNotExistsException(e);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public UserExtSource getUserExtSourceByUniqueAttributeValue(PerunSession sess, int attrId, String uniqueValue)
      throws UserExtSourceNotExistsException {
    try {
      return jdbc.queryForObject(
          "select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
              " from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id " +
              " left join user_ext_source_attr_u_values on user_ext_source_attr_u_values.user_ext_source_id=user_ext_sources.id" +
              " where user_ext_source_attr_u_values.attr_id=? and user_ext_source_attr_u_values.attr_value=?",
          USEREXTSOURCE_MAPPER, attrId, uniqueValue);
    } catch (EmptyResultDataAccessException e) {
      throw new UserExtSourceNotExistsException(e);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<UserExtSource> getUserExtSourcesByIds(PerunSession sess, List<Integer> ids) {
    try {
      return jdbc.execute(
          "select " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
              "  from user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id where user_ext_sources.id " +
              Compatibility.getStructureForInClause(),
          (PreparedStatementCallback<List<UserExtSource>>) preparedStatement -> {
            Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbersFromIntegers(ids, preparedStatement);
            preparedStatement.setArray(1, sqlArray);
            ResultSet rs = preparedStatement.executeQuery();
            List<UserExtSource> userExtSources = new ArrayList<>();
            while (rs.next()) {
              userExtSources.add(USEREXTSOURCE_MAPPER.mapRow(rs, rs.getRow()));
            }
            return userExtSources;
          });
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<UserExtSource> getUserExtSources(PerunSession sess, User user) {
    try {
      return jdbc.query(
          "SELECT " + userExtSourceMappingSelectQuery + "," + ExtSourcesManagerImpl.extSourceMappingSelectQuery +
              " FROM user_ext_sources left join ext_sources on user_ext_sources.ext_sources_id=ext_sources.id" +
              " WHERE user_ext_sources.user_id=?", USEREXTSOURCE_MAPPER, user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

  }

  @Override
  public void removeUserExtSource(PerunSession sess, User user, UserExtSource userExtSource)
      throws UserExtSourceAlreadyRemovedException {
    try {
      int numAffected = jdbc.update("delete from user_ext_sources where id=?", userExtSource.getId());
      if (numAffected == 0) {
        throw new UserExtSourceAlreadyRemovedException("User: " + user + " , UserExtSource: " + userExtSource);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAllUserExtSources(PerunSession sess, User user) {
    try {
      jdbc.update("delete from user_ext_sources where user_id=?", user.getId());
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, User user) {
    try {
      return jdbc.query("select distinct " + GroupsManagerImpl.groupMappingSelectQuery + " from groups" +
              " where groups.id in (select group_id from authz where ( authz.user_id=? or  authz.authorized_group_id in " +
              " (select distinct groups.id from groups join groups_members on groups_members.group_id=groups.id and groups_members.source_group_status=? " +
              " join members on groups_members.member_id=members.id where members.user_id=? and members.status=?) " +
              " and authz.role_id=(select id from roles where roles.name=?))) ",
          GroupsManagerImpl.GROUP_MAPPER, user.getId(), MemberGroupStatus.VALID.getCode(), user.getId(),
          Status.VALID.getCode(), Role.GROUPADMIN.toLowerCase());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Group> getGroupsWhereUserIsAdmin(PerunSession sess, Vo vo, User user) {
    try {
      return jdbc.query("select distinct " + GroupsManagerImpl.groupMappingSelectQuery + " from groups" +
              " where groups.id in (select group_id from authz where ( authz.user_id=? or  authz.authorized_group_id in " +
              " (select distinct groups.id from groups join groups_members on groups_members.group_id=groups.id and groups_members.source_group_status=?" +
              " join members on groups_members.member_id=members.id where members.user_id=? and members.status=?) " +
              " and authz.role_id=(select id from roles where roles.name=?))) and groups.vo_id=?",
          GroupsManagerImpl.GROUP_MAPPER, user.getId(), MemberGroupStatus.VALID.getCode(), user.getId(),
          Status.VALID.getCode(), Role.GROUPADMIN.toLowerCase(), vo.getId());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Vo> getVosWhereUserIsAdmin(PerunSession sess, User user) {
    try {
      return jdbc.query(
          "select " + VosManagerImpl.voMappingSelectQuery + " from authz join vos on authz.vo_id=vos.id " +
              " left outer join groups_members on groups_members.group_id=authz.authorized_group_id and groups_members.source_group_status=?" +
              " left outer join members on members.id=groups_members.member_id " +
              " where (authz.user_id=? or members.user_id=?) and authz.role_id=(select id from roles where name=?) and (members.status=? or members.status is null)",
          VosManagerImpl.VO_MAPPER, MemberGroupStatus.VALID.getCode(), user.getId(), user.getId(),
          Role.VOADMIN.toLowerCase(), Status.VALID.getCode());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Vo> getVosWhereUserIsMember(PerunSession sess, User user) {
    try {
      return jdbc.query("select " + VosManagerImpl.voMappingSelectQuery +
          " from users join members on users.id=members.user_id, vos where " +
          "users.id=? and members.vo_id=vos.id", VosManagerImpl.VO_MAPPER, user.getId());
    } catch (EmptyResultDataAccessException e) {
      // If user is not member of any vo, just return empty list
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) {
    return getUsersByAttribute(sess, attribute, false);
  }

  @Override
  public List<User> getUsersByAttribute(PerunSession sess, Attribute attribute, boolean ignoreCase) {
    try {
      if (ignoreCase) {
        return jdbc.query("select " + userMappingSelectQuery +
                " from users, user_attr_values where " +
                " lower(user_attr_values.attr_value)=lower(?) and users.id=user_attr_values.user_id and user_attr_values.attr_id=?",
            USER_MAPPER, BeansUtils.attributeValueToString(attribute), attribute.getId());
      } else {
        return jdbc.query("select " + userMappingSelectQuery +
                " from users, user_attr_values where " +
                " user_attr_values.attr_value=? and users.id=user_attr_values.user_id and user_attr_values.attr_id=?",
            USER_MAPPER, BeansUtils.attributeValueToString(attribute), attribute.getId());
      }
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> getUsersByAttributeValue(PerunSession sess, AttributeDefinition attributeDefinition,
                                             String attributeValue) {
    MapSqlParameterSource namedParams = new MapSqlParameterSource();
    namedParams.addValue("attr_id", attributeDefinition.getId());
    String matchingPart = "=:value";

    String attributeType = attributeDefinition.getType();
    var simpleTypes = List.of(String.class.getName(), Integer.class.getName(), Boolean.class.getName());
    if (simpleTypes.contains(attributeType)) {
      namedParams.addValue("value", attributeValue.trim());
    } else if (attributeType.equals(ArrayList.class.getName())) {
      matchingPart = "~ any(array[(:firstElementRegex),(:anyOtherElementRegex)])";
      // escape commas in value, double escape backslashes
      String escapedValue =
          AttributesManagerBlImpl.escapeListAttributeValue(attributeValue.trim()).replace("\\", "\\\\");
      namedParams.addValue("firstElementRegex", "^" + escapedValue + LIST_DELIMITER + ".*");
      // avoid matching escaped commas, f.e. 'corgi' in 'horse,dog\,corgi,'
      namedParams.addValue("anyOtherElementRegex", ".*[^\\\\]" + LIST_DELIMITER + escapedValue + LIST_DELIMITER + ".*");
    } else if (attributeType.equals(LinkedHashMap.class.getName())) {
      // escape commas and colons in value, double escape backslashes
      String escapedValue =
          AttributesManagerBlImpl.escapeMapAttributeValue(attributeValue.trim()).replace("\\", "\\\\");
      matchingPart = "~ any(array[(:firstKeyRegex),(:anyValueRegex),(:anyOtherKeyRegex)])";
      namedParams.addValue("firstKeyRegex", "^" + escapedValue + KEY_VALUE_DELIMITER + ".*");
      // avoid matching escaped delimiters, f.e. 'dalmatian' in 'animals:dog\,dalmatian,'
      namedParams.addValue("anyValueRegex", ".*[^\\\\]" + KEY_VALUE_DELIMITER + escapedValue + LIST_DELIMITER + ".*");
      namedParams.addValue("anyOtherKeyRegex",
          ".*[^\\\\]" + LIST_DELIMITER + escapedValue + KEY_VALUE_DELIMITER + ".*");
    } else {
      throw new InternalErrorException("Unknown attribute type: " + attributeType);
    }

    String query = "select " + userMappingSelectQuery + " from users, user_attr_values where " +
        " user_attr_values.attr_value " + matchingPart +
        " and users.id=user_attr_values.user_id and user_attr_values.attr_id=:attr_id";


    try {
      return namedParameterJdbcTemplate.query(query, namedParams, USER_MAPPER);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> findUsers(PerunSession sess, String searchString) {
    return findUsers(searchString, false);
  }

  @Override
  public List<User> findUsersByExactMatch(PerunSession sess, String searchString) {
    return findUsers(searchString, true);
  }

  /**
   * Returns list of users who matches the searchString, searching name, id, uuid, member attributes, user attributes
   * and userExtSource attributes (listed in CoreConfig).
   *
   * @param searchString string used to search by
   * @param exactMatch   if true, searches name only by exact match
   * @return list of users
   */
  private List<User> findUsers(String searchString, boolean exactMatch) {
    Map<String, List<String>> attributesToSearchBy = Utils.getDividedAttributes();
    MapSqlParameterSource namedParams =
        Utils.getMapSqlParameterSourceToSearchUsersOrMembers(searchString, attributesToSearchBy);

    String searchQuery = Utils.prepareSqlWhereForUserMemberSearch(searchString, namedParams, exactMatch);

    // Search by member attributes
    // Search by user attributes
    // Search by login in userExtSources
    // Search by userExtSource attributes
    // Search by user id
    // Search by user uuid
    // Search by name for user
    Set<User> users = new HashSet<>(namedParameterJdbcTemplate.query("select distinct " + userMappingSelectQuery +
            " from users " +
            " left join members on users.id=members.user_id " +
            " where " +
            searchQuery,
        namedParams, USER_MAPPER));

    log.debug("Searching for users using searchString '{}'", searchString);

    return new ArrayList<>(users);
  }

  @Override
  public List<User> findUsersByName(PerunSession sess, String searchString) {
    if (searchString == null || searchString.isEmpty()) {
      return new ArrayList<>();
    }

    // Convert to lowercase
    searchString = searchString.toLowerCase();
    log.trace("Search string '{}' converted into lower-cased", searchString);

    // remove spaces from the search string
    searchString = searchString.replaceAll(" ", "");

    log.debug("Searching users by name using searchString '{}'", searchString);

    // the searchString is already lower cased
    try {
      return jdbc.query("select " + userMappingSelectQuery + "  from users " +
              "where strpos(lower(" + Compatibility.convertToAscii(
              "COALESCE(users.first_name,'') || COALESCE(users.middle_name,'') || COALESCE(users.last_name,'')") + ")," +
              Compatibility.convertToAscii("?") + ") > 0",
          USER_MAPPER, searchString);
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName,
                                    String lastName, String titleAfter) {

    if (titleBefore.isEmpty()) {
      titleBefore = "%";
    }
    if (firstName.isEmpty()) {
      firstName = "%";
    }
    if (middleName.isEmpty()) {
      middleName = "%";
    }
    if (lastName.isEmpty()) {
      lastName = "%";
    }
    if (titleAfter.isEmpty()) {
      titleAfter = "%";
    }

    // the searchString is already lower cased
    try {
      return jdbc.query("select " + userMappingSelectQuery + " from users " +
              " where coalesce(lower(" + Compatibility.convertToAscii("users.title_before") + "), '%') like " +
              Compatibility.convertToAscii("?") + " and lower(" + Compatibility.convertToAscii("users.first_name") +
              ") like " + Compatibility.convertToAscii("?") +
              " and coalesce(lower(" + Compatibility.convertToAscii("users.middle_name") + "),'%') like " +
              Compatibility.convertToAscii("?") + " and lower(" + Compatibility.convertToAscii("users.last_name") +
              ") like " + Compatibility.convertToAscii("?") +
              " and coalesce(lower(" + Compatibility.convertToAscii("users.title_after") + "), '%') like " +
              Compatibility.convertToAscii("?"),
          USER_MAPPER, titleBefore, firstName, middleName, lastName, titleAfter);
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> findUsersByExactName(PerunSession sess, String searchString) {
    if (searchString == null || searchString.isEmpty()) {
      return new ArrayList<>();
    }

    // Convert to lower case
    searchString = searchString.toLowerCase();
    log.debug("Search string '{}' converted into the lowercase", searchString);

    // remove spaces from the search string
    searchString = searchString.replaceAll(" ", "");

    log.debug("Searching users by name using searchString '{}'", searchString);

    // the searchString is already lower cased
    try {
      return jdbc.query("select " + userMappingSelectQuery + " from users "
              + "where lower(" + Compatibility.convertToAscii(
              "COALESCE(users.first_name,'') || COALESCE(users.middle_name,'') || COALESCE(users.last_name,'')") + ")=" +
              Compatibility.convertToAscii("?"),
          USER_MAPPER, searchString);
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public Paginated<User> getUsersPage(PerunSession sess, UsersPageQuery query) {
    Map<String, List<String>> attributesToSearchBy = Utils.getDividedAttributes();
    MapSqlParameterSource namedParams =
        Utils.getMapSqlParameterSourceToSearchUsersOrMembers(query.getSearchString(), attributesToSearchBy);

    String select = getSQLSelectForUsersPage(query);
    String searchQuery = getSQLWhereForUsersPage(query, namedParams);
    String joinFacility = getSQLJoinFacility(query, namedParams);
    String whereForFacility = getSQLWhereForFacility(query, namedParams);
    String filterOnlyAllowed = getOnlyAllowed(query, namedParams);

    namedParams.addValue("offset", query.getOffset());
    namedParams.addValue("limit", query.getPageSize());

    String withoutVoString = getWithoutVoSQLConditionForUsersPage(query);

    return namedParameterJdbcTemplate.query(
        select +
            joinFacility +
            withoutVoString +
            searchQuery +
            whereForFacility +
            filterOnlyAllowed +
            " GROUP BY users_id" +
            " ORDER BY " + query.getSortColumn().getSqlOrderBy(query) +
            " OFFSET (:offset)" +
            " LIMIT (:limit)"
        , namedParams, getPaginatedUsersExtractor(query));
  }

  @Override
  @Deprecated
  public boolean isUserPerunAdmin(PerunSession sess, User user) {
    try {
      int numberOfExistences =
          jdbc.queryForInt("select count(1) from authz where user_id=? and role_id=(select id from roles where name=?)",
              user.getId(), Role.PERUNADMIN.toLowerCase());
      if (numberOfExistences == 1) {
        return true;
      } else if (numberOfExistences > 1) {
        throw new ConsistencyErrorException("User " + user + " is PERUNADMIN more than once.");
      }
      return false;
    } catch (EmptyResultDataAccessException e) {
      return false;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public boolean userExists(PerunSession sess, User user) {
    Utils.notNull(user, "user");
    try {
      int numberOfExistences =
          jdbc.queryForInt("select count(1) from users where id=? and service_acc=? and sponsored_acc=?", user.getId(),
              user.isServiceUser(), user.isSponsoredUser());
      if (numberOfExistences == 1) {
        return true;
      } else if (numberOfExistences > 1) {
        throw new ConsistencyErrorException("User " + user + " exists more than once.");
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkUserExtSourceExists(PerunSession sess, UserExtSource userExtSource)
      throws UserExtSourceNotExistsException {
    if (!userExtSourceExists(sess, userExtSource)) {
      throw new UserExtSourceNotExistsException("UserExtSource: " + userExtSource);
    }
  }

  @Override
  public void checkUserExtSourceExistsById(PerunSession sess, int id) throws UserExtSourceNotExistsException {

    try {
      int numberOfExistences = jdbc.queryForInt("select count(1) from user_ext_sources where id=?", id);
      if (numberOfExistences == 0) {
        throw new UserExtSourceNotExistsException("UserExtSource with ID=" + id + " doesn't exists.");
      }
      if (numberOfExistences > 1) {
        throw new ConsistencyErrorException("UserExtSource wit ID=" + id + " exists more than once.");
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }

  }

  @Override
  public void checkReservedLogins(PerunSession sess, String namespace, String login, boolean ignoreCase)
      throws AlreadyReservedLoginException {
    if (isLoginReserved(sess, namespace, login, ignoreCase)) {
      throw new AlreadyReservedLoginException(namespace, login);
    }
  }

  @Override
  public boolean isLoginReserved(PerunSession sess, String namespace, String login, boolean ignoreCase) {
    Utils.notNull(login, "userLogin");

    try {
      int numberOfExistences = 0;
      String namespaceValue = (namespace == null) ? "%" : namespace;
      if (ignoreCase) {
        numberOfExistences = jdbc.queryForInt(
            "select count(1) from application_reserved_logins where namespace like ? and lower(login)=lower(?)",
            namespaceValue, login);
      } else {
        numberOfExistences =
            jdbc.queryForInt("select count(1) from application_reserved_logins where namespace like ? and login=?",
                namespaceValue, login);
      }
      if (numberOfExistences > 0) {
        if (namespace != null && numberOfExistences > 1) {
          throw new ConsistencyErrorException(
              "Login " + login + " in namespace " + namespace + " is reserved more than once.");
        }
        return true;
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<BlockedLogin> getAllBlockedLoginsInNamespaces(PerunSession sess) {
    try {
      return jdbc.query("select id, login, namespace from blocked_logins", BLOCKED_LOGINS_MAPPER);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public boolean isLoginBlocked(PerunSession sess, String login, boolean ignoreCase) {
    Utils.notNull(login, "userLogin");

    try {
      if (ignoreCase) {
        return jdbc.queryForInt("select count(1) from blocked_logins where UPPER(login)=?", login.toUpperCase()) == 1;
      } else {
        return jdbc.queryForInt(
            "select count(1) from blocked_logins where login=? OR (UPPER(login)=? AND namespace IS NULL)", login,
            login.toUpperCase()) == 1;
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public boolean isLoginBlockedGlobally(PerunSession sess, String login) {
    Utils.notNull(login, "userLogin");

    try {
      return jdbc.queryForInt("select count(1) from blocked_logins where UPPER(login)=? and namespace is null",
          login.toUpperCase()) == 1;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public boolean isLoginBlockedForNamespace(PerunSession sess, String login, String namespace, boolean ignoreCase) {
    Utils.notNull(login, "userLogin");

    try {
      if (namespace == null) {
        return this.isLoginBlockedGlobally(sess, login);
      } else {
        if (ignoreCase) {
          return jdbc.queryForInt("select count(1) from blocked_logins where UPPER(login)=? and namespace=?",
              login.toUpperCase(), namespace) == 1;
        } else {
          return
              jdbc.queryForInt("select count(1) from blocked_logins where login=? and namespace=?", login, namespace) ==
                  1;
        }
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void blockLogin(PerunSession sess, String login, String namespace, Integer relatedUserId)
      throws LoginIsAlreadyBlockedException {
    Utils.notNull(login, "userLogin");

    if (namespace == null && isLoginBlockedGlobally(sess, login)) {
      throw new LoginIsAlreadyBlockedException("Login: " + login + " is already blocked globally");
    }

    try {
      int newId = Utils.getNewId(jdbc, "blocked_logins_id_seq");
      jdbc.update("insert into blocked_logins(id, login, namespace, related_user_id) values (?,?,?,?)",
          newId, login, namespace, relatedUserId);
      if (namespace == null) {
        log.info("Login {} globally blocked", login);
      } else {
        log.info("Login {} blocked in namespace: {}", login, namespace);
      }
    } catch (DuplicateKeyException ex) {
      throw new LoginIsAlreadyBlockedException("Login: " + login + " is already blocked in namespace: " + namespace);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void unblockLogin(PerunSession sess, String login, String namespace) throws LoginIsNotBlockedException {
    Utils.notNull(login, "userLogin");

    try {
      if (namespace == null) {
        int numAffected = jdbc.update("delete from blocked_logins where login=? and namespace is null", login);
        if (numAffected == 0) {
          throw new LoginIsNotBlockedException("Login: " + login + " is not blocked globally");
        }
        log.info("Login {} globally unblocked", login);
      } else {
        int numAffected = jdbc.update("delete from blocked_logins where login=? and namespace=?", login, namespace);
        if (numAffected == 0) {
          throw new LoginIsNotBlockedException("Login: " + login + " is not blocked in namespace: " + namespace);
        }
        log.info("Login {} unblocked in namespace: {}", login, namespace);
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Paginated<BlockedLogin> getBlockedLoginsPage(PerunSession sess, BlockedLoginsPageQuery query) {
    try {
      MapSqlParameterSource namedParams = new MapSqlParameterSource();
      namedParams.addValue("login", query.getSearchString().toLowerCase());
      namedParams.addValue("namespaces", query.getNamespaces());
      namedParams.addValue("offset", query.getOffset());
      namedParams.addValue("limit", query.getPageSize());

      String sqlQuery = "SELECT id, login, namespace, count(*) OVER() AS total_count FROM blocked_logins";

      boolean hasSearchString = query.getSearchString() != null && !query.getSearchString().isEmpty();
      boolean hasNamespaces = query.getNamespaces() != null && !query.getNamespaces().isEmpty();
      if (hasSearchString || hasNamespaces) {
        sqlQuery += " WHERE";
        if (hasSearchString) {
          sqlQuery += " LOWER(login) like ('%%' || :login || '%%')";
        }

        if (hasSearchString && hasNamespaces) {
          sqlQuery += " AND";
        }

        if (hasNamespaces) {
          sqlQuery += " (";
          if (query.getNamespaces().contains(null)) {
            sqlQuery += "namespace IS NULL OR";
          }
          sqlQuery += " namespace IN (:namespaces)";
          sqlQuery += ")";
        }
      }

      sqlQuery += " ORDER BY " + query.getSortColumn().getSqlOrderBy(query) + " OFFSET (:offset) LIMIT (:limit);";

      return namedParameterJdbcTemplate.query(
          sqlQuery,
          namedParams,
          getPaginatedBlockedLoginsExtractor(query));
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void unblockLoginsById(PerunSession sess, List<Integer> loginIds) {
    try {
      MapSqlParameterSource parameters = new MapSqlParameterSource();
      parameters.addValue("ids", loginIds);
      namedParameterJdbcTemplate.update("DELETE FROM blocked_logins WHERE id in (:ids)", parameters);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void unblockLoginsForNamespace(PerunSession sess, String namespace) {
    try {
      if (namespace == null) {
        jdbc.update("delete from blocked_logins where namespace is null");
        log.info("All globally blocked logins were unblocked");
      } else {
        jdbc.update("delete from blocked_logins where namespace=?", namespace);
        log.info("All logins for namespace {} were unblocked", namespace);
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public BlockedLogin getBlockedLoginById(PerunSession sess, int id) throws LoginIsNotBlockedException {
    try {
      return jdbc.queryForObject("SELECT id, login, namespace FROM blocked_logins WHERE id=?", BLOCKED_LOGINS_MAPPER,
          id);
    } catch (EmptyResultDataAccessException ex) {
      throw new LoginIsNotBlockedException(ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public int getIdOfBlockedLogin(PerunSession sess, String login, String namespace) {
    try {
      if (namespace == null) {
        return jdbc.queryForInt("select id from blocked_logins where login=? and namespace is null", login);
      } else {
        return jdbc.queryForInt("SELECT id FROM blocked_logins WHERE login=? and namespace=?", login, namespace);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public Integer getRelatedUserIdByBlockedLoginInNamespace(PerunSession sess, String login, String namespace)
      throws LoginIsNotBlockedException {
    try {
      if (namespace == null) {
        return jdbc.queryForObject("SELECT related_user_id FROM blocked_logins WHERE login=? AND namespace IS NULL",
            Integer.class, login);
      } else {
        return jdbc.queryForObject("SELECT related_user_id FROM blocked_logins WHERE login=? AND namespace=?",
            Integer.class, login, namespace);
      }
    } catch (EmptyResultDataAccessException ex) {
      throw new LoginIsNotBlockedException(ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean userExtSourceExists(PerunSession sess, UserExtSource userExtSource) {
    Utils.notNull(userExtSource, "userExtSource");
    Utils.notNull(userExtSource.getLogin(), "userExtSource.getLogin");
    Utils.notNull(userExtSource.getExtSource(), "userExtSource.getExtSource");

    try {

      // check by ext identity (login/ext source ID)
      if (userExtSource.getUserId() >= 0) {
        int numberOfExistences = jdbc.queryForInt(
            "select count(1) from user_ext_sources where login_ext=? and ext_sources_id=? and user_id=?",
            userExtSource.getLogin(), userExtSource.getExtSource().getId(), userExtSource.getUserId());
        if (numberOfExistences == 1) {
          return true;
        } else if (numberOfExistences > 1) {
          throw new ConsistencyErrorException("UserExtSource " + userExtSource + " exists more than once.");
        }
        return false;
      } else {
        int numberOfExistences =
            jdbc.queryForInt("select count(1) from user_ext_sources where login_ext=? and ext_sources_id=?",
                userExtSource.getLogin(), userExtSource.getExtSource().getId());
        if (numberOfExistences == 1) {
          return true;
        } else if (numberOfExistences > 1) {
          throw new ConsistencyErrorException("UserExtSource " + userExtSource + " exists more than once.");
        }
        return false;
      }

    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }

  }

  @Override
  public List<User> getUsersByIds(PerunSession sess, List<Integer> usersIds) {
    // If usersIds is empty, we can immediately return empty results
    if (usersIds.size() == 0) {
      return new ArrayList<>();
    }
    return jdbc.execute(
        "select " + userMappingSelectQuery + "  from users where users.id " + Compatibility.getStructureForInClause(),
        (PreparedStatementCallback<List<User>>) preparedStatement -> {
          Array sqlArray = DatabaseManagerBl.prepareSQLArrayOfNumbersFromIntegers(usersIds, preparedStatement);
          preparedStatement.setArray(1, sqlArray);
          ResultSet rs = preparedStatement.executeQuery();
          List<User> users = new ArrayList<>();
          while (rs.next()) {
            users.add(USER_MAPPER.mapRow(rs, rs.getRow()));
          }
          return users;
        });
  }

  @Override
  public List<User> getUsersWithoutVoAssigned(PerunSession sess) {
    try {
      return jdbc.query("select " + userMappingSelectQuery + " from users where " +
          "users.id not in (select user_id from members) order by last_name, first_name", USER_MAPPER);
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAllAuthorships(PerunSession sess, User user) {

    try {
      jdbc.update("delete from cabinet_authorships where userid=?", user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

  }

  @Override
  public List<Pair<String, String>> getUsersReservedLogins(User user) {
    try {
      return jdbc.query("select namespace,login from application_reserved_logins where user_id=? for update",
          (resultSet, arg1) -> new Pair<>(resultSet.getString("namespace"), resultSet.getString("login")),
          user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void deleteUsersReservedLogins(User user) {
    try {
      jdbc.update("delete from application_reserved_logins where user_id=?", user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

  }

  @Override
  public void deleteReservedLoginsForNamespace(PerunSession sess, String namespace) {
    try {
      jdbc.update("delete from application_reserved_logins where namespace=?", namespace);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void deleteUsersApplications(User user) {
    try {
      jdbc.update("delete from application where user_id=?", user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public UUID requestPreferredEmailChange(PerunSession sess, User user, String email) {
    try {
      return jdbc.queryForObject("insert into mailchange(id, value, user_id, created_by, created_by_uid) " +
              "values (nextval('mailchange_id_seq'),?,?,?,?) returning uu_id", UUID.class,
          email, user.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public String getPreferredEmailChangeRequest(PerunSession sess, User user, UUID uuid) {
    int validWindow = BeansUtils.getCoreConfig().getMailchangeValidationWindow();

    // get new email if possible
    String newEmail;
    try {
      newEmail = jdbc.queryForObject(
          "select value from mailchange where uu_id=? and user_id=? and (created_at > (now() - interval '" +
              validWindow + " hours'))",
          String.class, uuid, user.getId());
    } catch (EmptyResultDataAccessException ex) {
      throw new InternalErrorException(
          "Preferred mail change request with UUID=" + uuid + " doesn't exists or isn't valid anymore.");
    }

    return newEmail;
  }

  @Override
  public void removeAllPreferredEmailChangeRequests(PerunSession sess, User user) {

    try {
      jdbc.update("delete from mailchange where user_id=?", user.getId());
    } catch (Exception ex) {
      throw new InternalErrorException("Unable to remove preferred mail change requests for user: " + user, ex);
    }

  }

  @Override
  public List<String> getPendingPreferredEmailChanges(PerunSession sess, User user) {

    int validWindow = BeansUtils.getCoreConfig().getMailchangeValidationWindow();

    try {
      return jdbc.query(
          "select value from mailchange where user_id=? and (created_at > (now() - interval '" + validWindow +
              " hours'))",
          (resultSet, i) -> resultSet.getString("value"), user.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    }

  }

  @Override
  public void checkPasswordResetRequestIsValid(PerunSession sess, UUID uuid)
      throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {
    this.checkAndGetPasswordResetRequest(uuid);
  }

  private Map<String, Object> checkAndGetPasswordResetRequest(UUID uuid)
      throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {
    try {
      int numberOfRequests = jdbc.queryForInt("select count(1) from pwdreset where uu_id=?", uuid);
      if (numberOfRequests == 0) {
        throw new PasswordResetLinkNotValidException("Password request " + uuid + " doesn't exist.");
      } else if (numberOfRequests > 1) {
        throw new ConsistencyErrorException("Password reset request " + uuid + " exists more than once.");
      }

      return jdbc.queryForMap("select namespace, mail, user_id from pwdreset where uu_id=? and validity_to >= now()",
          uuid);
    } catch (EmptyResultDataAccessException ex) {
      throw new PasswordResetLinkExpiredException("Password reset request " + uuid + " has already expired.");
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Map<String, Object> loadPasswordResetRequest(PerunSession sess, UUID uuid)
      throws PasswordResetLinkExpiredException, PasswordResetLinkNotValidException {
    Map<String, Object> requestInfo = this.checkAndGetPasswordResetRequest(uuid);

    try {
      jdbc.update("delete from pwdreset where uu_id=?", uuid);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }

    return requestInfo;
  }

  @Override
  public void removeAllPasswordResetRequests(PerunSession sess, User user) {

    try {
      jdbc.update("delete from pwdreset where user_id=?", user.getId());
    } catch (Exception ex) {
      throw new InternalErrorException("Unable to remove password reset requests for user: " + user, ex);
    }

  }

  @Override
  public int getUsersCount(PerunSession sess) {
    try {
      return jdbc.queryForInt("select count(*) from users");
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkUserExists(PerunSession sess, User user) throws UserNotExistsException {
    if (!userExists(sess, user)) {
      throw new UserNotExistsException("User: " + user);
    }
  }

  @Override
  public PasswordManagerModule getPasswordManagerModule(PerunSession session, String namespace) {

    if (namespace == null || namespace.isEmpty()) {
      throw new InternalErrorException("Login-namespace to get password manager module must be specified.");
    }

    namespace = namespace.replaceAll("[^A-Za-z0-9]", "");
    namespace = Character.toUpperCase(namespace.charAt(0)) + namespace.substring(1);

    try {
      return (PasswordManagerModule) Class.forName(
          "cz.metacentrum.perun.core.impl.modules.pwdmgr." + namespace + "PasswordManagerModule").newInstance();
    } catch (ClassNotFoundException ex) {
      return null;
    } catch (InstantiationException | IllegalAccessException ex) {
      throw new InternalErrorException("Unable to instantiate password manager module.", ex);
    }

  }

  @Override
  public List<User> getSponsors(PerunSession sess, Member sponsoredMember) {
    try {
      return jdbc.query(
          "SELECT " + userMappingSelectQuery + " FROM users JOIN members_sponsored ms ON (users.id=ms.sponsor_id)" +
              "WHERE ms.active=? AND ms.sponsored_id=? ", USER_MAPPER, true, sponsoredMember.getId());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Map<Integer, List<Pair<User, Sponsorship>>> getSponsorsForSponsoredMembersInVo(PerunSession sess, int voId) {
    try {
      return jdbc.query("SELECT members.id as member_id, "
          + memberSponsorshipSelectQuery + ", " + userMappingSelectQuery +
          " FROM members JOIN members_sponsored ON (members.id=members_sponsored.sponsored_id)" +
          " JOIN users ON (users.id=members_sponsored.sponsor_id)" +
          " WHERE members.vo_id=? AND members_sponsored.active=?", getSponsorsExtractor(), voId, true);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }


  @Override
  public void deleteSponsorLinks(PerunSession sess, User sponsor) {
    try {
      jdbc.update("DELETE FROM members_sponsored WHERE sponsor_id=?", sponsor.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> findUsersWithExtSourceAttributeValueEnding(PerunSessionImpl sess, String attributeName,
                                                               String valueEnd, List<String> excludeValueEnds) {
    try {
      StringBuilder sb = new StringBuilder("SELECT DISTINCT " + userMappingSelectQuery + " FROM users " +
          "  JOIN user_ext_sources ues ON users.id = ues.user_id " +
          "  JOIN user_ext_source_attr_values v ON ues.id = v.user_ext_source_id" +
          "  JOIN attr_names a ON (v.attr_id = a.id AND a.attr_name=?)" +
          "  WHERE v.attr_value LIKE ? ");
      List<String> args = new ArrayList<>();
      args.add(attributeName);
      args.add("%" + valueEnd);
      for (String excl : excludeValueEnds) {
        sb.append(" AND v.attr_value NOT LIKE ?");
        args.add("%" + excl);
      }
      return jdbc.query(sb.toString(), USER_MAPPER, args.toArray());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }

  }

  @Override
  public List<Resource> getAssignedResources(PerunSession sess, User user) {
    try {
      return jdbc.query("select distinct " + resourceMappingSelectQuery + " from resources" +
          " join groups_resources_state on resources.id=groups_resources_state.resource_id and groups_resources_state.status=?::group_resource_status " +
          " join groups on groups_resources_state.group_id=groups.id" +
          " join groups_members on groups.id=groups_members.group_id " +
          " join members on groups_members.member_id=members.id " +
          " where members.user_id=?", RESOURCE_MAPPER, GroupResourceStatus.ACTIVE.toString(), user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Resource> getAssociatedResources(PerunSession sess, User user) {
    try {
      return jdbc.query("select distinct " + resourceMappingSelectQuery + " from resources" +
          " join groups_resources_state on resources.id=groups_resources_state.resource_id" +
          " join groups on groups_resources_state.group_id=groups.id" +
          " join groups_members on groups.id=groups_members.group_id" +
          " join members on groups_members.member_id=members.id" +
          " where members.user_id=?", RESOURCE_MAPPER, user.getId());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Resource> getAllowedResources(PerunSession sess, User user) {
    try {
      return jdbc.query("select distinct " + resourceMappingSelectQuery + " from resources" +
              " join groups_resources_state on resources.id=groups_resources_state.resource_id and groups_resources_state.status=?::group_resource_status " +
              " join groups on groups_resources_state.group_id=groups.id" +
              " join groups_members on groups.id=groups_members.group_id " +
              " join members on groups_members.member_id=members.id " +
              " where members.user_id=? and members.status!=? and members.status!=?",
          RESOURCE_MAPPER, GroupResourceStatus.ACTIVE.toString(), user.getId(), Status.INVALID.getCode(),
          Status.DISABLED.getCode());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Resource> getAssignedResources(PerunSession sess, Facility facility, User user) {
    try {
      return jdbc.query("select distinct " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resources " +
              " join groups_resources_state on groups_resources_state.resource_id=resources.id and groups_resources_state.status=?::group_resource_status " +
              " join groups_members on groups_members.group_id=groups_resources_state.group_id" +
              " join members on members.id=groups_members.member_id" +
              " where resources.facility_id=? and members.user_id=?",
          RESOURCE_MAPPER, GroupResourceStatus.ACTIVE.toString(), facility.getId(), user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Resource> getAssociatedResources(PerunSession sess, Facility facility, User user) {
    try {
      return jdbc.query("select distinct " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resources " +
              " join groups_resources_state on groups_resources_state.resource_id=resources.id" +
              " join groups_members on groups_members.group_id=groups_resources_state.group_id" +
              " join members on members.id=groups_members.member_id" +
              " where resources.facility_id=? and members.user_id=?",
          RESOURCE_MAPPER, facility.getId(), user.getId());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<RichResource> getAssignedRichResources(PerunSession sess, User user) {
    try {
      return jdbc.query(
          "select distinct " + resourceMappingSelectQuery + ", " + VosManagerImpl.voMappingSelectQuery + ", " +
              FacilitiesManagerImpl.facilityMappingSelectQuery + ", " + resourceTagMappingSelectQuery +
              " from resources" +
              " join vos on resources.vo_id=vos.id " +
              " join facilities on resources.facility_id=facilities.id " +
              " join groups_resources_state on resources.id=groups_resources_state.resource_id and groups_resources_state.status=?::group_resource_status " +
              " join groups on groups_resources_state.group_id=groups.id " +
              " join groups_members on groups.id=groups_members.group_id " +
              " join members on groups_members.member_id=members.id " +
              " left outer join tags_resources on resources.id=tags_resources.resource_id" +
              " left outer join res_tags on tags_resources.tag_id=res_tags.id" +
              " where members.user_id=?", RICH_RESOURCE_WITH_TAGS_EXTRACTOR, GroupResourceStatus.ACTIVE.toString(),
          user.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Pair<String, String>> getReservedLoginsByApp(PerunSession sess, int appId) {
    List<Pair<String, String>> logins = jdbc.query("SELECT dst_attr, value FROM application_data" +
            " JOIN application_form_items ON application_form_items.id = application_data.item_id  " +
            " AND type=? AND app_id=?" +
            " ORDER BY dst_attr, value", // order to prevent deadlock when locking later in this method
        (resultSet, arg1) -> new Pair<>(resultSet.getString("dst_attr"), resultSet.getString("value")),
        USERNAME.toString(), appId);

    List<Pair<String, String>> reservedLogins = new ArrayList<>();
    for (Pair<String, String> login : logins) {
      String dstAttr = login.getLeft();
      try {
        AttributeDefinition loginAttribute =
            ((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, dstAttr);
        String namespace = loginAttribute.getFriendlyNameParameter();
        // check if it is still reserved
        try {
          reservedLogins.add(jdbc.queryForObject("select namespace, login " +
                  "from application_reserved_logins where namespace=? and login=? for update",
              (resultSet, arg1) -> new Pair<>(resultSet.getString("namespace"), resultSet.getString("login")),
              namespace, login.getRight()));
        } catch (IncorrectResultSizeDataAccessException ex) {
          continue; //login isn't reserved anymore
        }
      } catch (AttributeNotExistsException e) {
        continue;
      }
    }

    return reservedLogins;
  }

  @Override
  public List<Pair<String, String>> getReservedLoginsOnlyByGivenApp(PerunSession sess, int appId) {
    List<Pair<String, String>> appLogins = getReservedLoginsByApp(sess, appId);

    // filter only reserved logins which can be deleted - they are reserved only for this application
    List<Pair<String, String>> loginsToDelete = new ArrayList<>();
    for (Pair<String, String> login : appLogins) {
      String loginAttr =
          AttributesManager.NS_USER_ATTR_DEF + ":" + AttributesManager.LOGIN_NAMESPACE + ":" + login.getLeft();
      int numberOfApps = jdbc.queryForInt("SELECT count(*) FROM application_data " +
              "JOIN application ON application_data.app_id = application.id " +
              "JOIN application_form_items ON application_form_items.id = application_data.item_id " +
              "WHERE state!=? and state!=? and type=? and value=? and dst_attr=? and application.id!=?",
          Application.AppState.APPROVED.toString(), Application.AppState.REJECTED.toString(), USERNAME.toString(),
          login.getRight(), loginAttr, appId);
      if (numberOfApps == 0) {
        loginsToDelete.add(login);
      }
    }

    return loginsToDelete;
  }

  @Override
  public void deleteReservedLogin(PerunSession sess, Pair<String, String> login) {
    try {
      jdbc.update("delete from application_reserved_logins where namespace=? and login=?", login.getLeft(),
          login.getRight());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  private String getSQLSelectForUsersPage(UsersPageQuery query) {
    String select =
        "SELECT " + userMappingSelectQuery +
            " ,count(*) OVER() AS total_count" +
            " FROM users";

    String selectWithMembers =
        "SELECT " + userMappingSelectQuery +
            " ,count(*) OVER() AS total_count" +
            " FROM users LEFT JOIN members on members.user_id = users.id";


    return !query.isWithoutVo() && isEmpty(query.getSearchString()) ? select : selectWithMembers;
  }

  private String getSQLWhereForUsersPage(UsersPageQuery query, MapSqlParameterSource namedParams) {
    if (isEmpty(query.getSearchString())) {
      return "";
    }
    if (query.isWithoutVo()) {
      return " AND " + Utils.prepareSqlWhereForUserMemberSearch(query.getSearchString(), namedParams, false);
    }
    return " WHERE " + Utils.prepareSqlWhereForUserMemberSearch(query.getSearchString(), namedParams, false);
  }

  private String getWithoutVoSQLConditionForUsersPage(UsersPageQuery query) {
    String withoutVoQueryString = "";
    if (query.isWithoutVo()) {
      withoutVoQueryString = " WHERE users.id not in (select user_id from members) ";
    }
    return withoutVoQueryString;
  }

  private String getSQLJoinFacility(UsersPageQuery query, MapSqlParameterSource namedParams) {
    String joinString = "";

    if (query.getFacilityId() == null && query.getResourceId() == null) {
      return joinString;
    }

    if (isEmpty(query.getSearchString())) {
      joinString += " LEFT JOIN members on members.user_id = users.id";
    }

    namedParams.addValue("groupResourceStatus", GroupResourceStatus.ACTIVE.toString());
    joinString += " join groups_members on members.id = groups_members.member_id" +
        " join groups_resources_state on groups_members.group_id = groups_resources_state.group_id and groups_resources_state.status=(:groupResourceStatus)::group_resource_status";

    if (query.getFacilityId() != null) {
      namedParams.addValue("facilityId", query.getFacilityId());

      joinString +=
          " join resources on resources.id = groups_resources_state.resource_id and resources.facility_id=(:facilityId)";

      if (query.getServiceId() != null) {
        namedParams.addValue("serviceId", query.getServiceId());
        joinString +=
            " join resource_services on resources.id=resource_services.resource_id and resource_services.service_id=(:serviceId)";
      }

      if (query.getConsentStatuses() != null && !query.getConsentStatuses().isEmpty()) {
        List<String> statusStrings = query.getConsentStatuses().stream()
            .map(ConsentStatus::toString).toList();
        namedParams.addValue("consentStatuses", statusStrings);
        joinString += " join consents on users.id = consents.user_id" +
            " and" +
            " (select consents.status" +
            " from consents" +
            " where consents.user_id=users.id" +
            " and consents.consent_hub_id in" +
            " (select consent_hub_id" +
            " from consent_hubs_facilities" +
            " where consent_hubs_facilities.facility_id=(:facilityId))" +
            " order by consents.modified_at desc" +
            " limit 1)::text in (:consentStatuses)" +
            " and consents.consent_hub_id in" +
            " (select consent_hub_id" +
            " from consent_hubs_facilities" +
            " where consent_hubs_facilities.facility_id=(:facilityId))";
      }
    }

    return joinString;
  }

  private String getSQLWhereForFacility(UsersPageQuery query, MapSqlParameterSource namedParams) {
    String sqlWhereForResource = "";

    if (query.getFacilityId() == null && query.getResourceId() == null) {
      return sqlWhereForResource;
    }

    if (isEmpty(query.getSearchString())) {
      sqlWhereForResource = " where";
    } else {
      sqlWhereForResource = " and";
    }

    if (query.getResourceId() == null) {
      if (query.getVoId() != null && query.getServiceId() != null) {
        namedParams.addValue("voId", query.getVoId());
        namedParams.addValue("serviceId", query.getServiceId());

        return sqlWhereForResource + " groups_resources_state.resource_id in (" +
            "select resources.id from resource_services " +
            "join resources on resource_services.resource_id=resources.id " +
            "where facility_id=(:facilityId) and vo_id=(:voId) and service_id=(:serviceId))";
      }

      if (query.getVoId() != null) {
        namedParams.addValue("voId", query.getVoId());

        return sqlWhereForResource + " groups_resources_state.resource_id in (" +
            "select resources.id from resources " +
            "where facility_id=(:facilityId) and vo_id=(:voId))";
      }

      if (query.getServiceId() != null) {
        namedParams.addValue("serviceId", query.getServiceId());

        return sqlWhereForResource + " groups_resources_state.resource_id in (" +
            "select resources.id from resource_services " +
            "join resources on resource_services.resource_id=resources.id " +
            "where facility_id=(:facilityId) and service_id=(:serviceId))";
      }
    } else {
      namedParams.addValue("resourceId", query.getResourceId());
      return sqlWhereForResource + " groups_resources_state.resource_id=(:resourceId)";
    }

    return "";
  }

  private String getOnlyAllowed(UsersPageQuery query, MapSqlParameterSource namedParams) {
    if (query.isOnlyAllowed()) {
      namedParams.addValue("invalidStatus", Status.INVALID.getCode());
      namedParams.addValue("disabledStatus", Status.DISABLED.getCode());

      return " and members.status!=(:invalidStatus) and members.status!=(:disabledStatus)";
    }

    return "";
  }

}
