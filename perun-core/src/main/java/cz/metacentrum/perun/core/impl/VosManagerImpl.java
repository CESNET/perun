package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * VosManager implementation.
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class VosManagerImpl implements VosManagerImplApi {

  public static final String STEM = "virtual_organizations";
  public static final String VO_GROUPS_STEM = "groups";
  public static final String VO_SUBGROUPS_STEM = "subgroups";
  public static final String VO_RESOURCES_STEM = "resources";
  public static final String PERSON_TYPE = "person";
  protected static final String VO_MAPPING_SELECT_QUERY =
      "vos.id as vos_id, vos.uu_id as vos_uu_id, vos.name as vos_name, vos.short_name as vos_short_name, " +
          "vos.created_at as vos_created_at, vos.created_by as vos_created_by," +
          " vos.modified_by as vos_modified_by, vos.modified_at as vos_modified_at, " +
          "vos.created_by_uid as vos_created_by_uid, vos.modified_by_uid as vos_modified_by_uid";
  protected static final String BAN_ON_VO_MAPPING_SELECT_QUERY =
      "vos_bans.id as vos_bans_id, vos_bans.description as vos_bans_description, " +
          "vos_bans.member_id as vos_bans_member_id, vos_bans.vo_id as vos_bans_vo_id," +
          " vos_bans.banned_to as vos_bans_validity_to, " +
          "vos_bans.created_at as vos_bans_created_at, vos_bans.created_by as vos_bans_created_by," +
          " vos_bans.modified_at as vos_bans_modified_at, " +
          "vos_bans.modified_by as vos_bans_modified_by, vos_bans.created_by_uid as vos_bans_created_by_uid," +
          " vos_bans.modified_by_uid as vos_bans_modified_by_uid";
  /**
   * Converts s ResultSet's row to a Vo instance.
   */
  protected static final RowMapper<Vo> VO_MAPPER = (resultSet, i) ->
                                                       new Vo(resultSet.getInt("vos_id"),
                                                           resultSet.getObject("vos_uu_id", UUID.class),
                                                           resultSet.getString("vos_name"),
                                                           resultSet.getString("vos_short_name"),
                                                           resultSet.getString("vos_created_at"),
                                                           resultSet.getString("vos_created_by"),
                                                           resultSet.getString("vos_modified_at"),
                                                           resultSet.getString("vos_modified_by"),
                                                           resultSet.getInt("vos_created_by_uid") == 0 ? null :
                                                               resultSet.getInt("vos_created_by_uid"),
                                                           resultSet.getInt("vos_modified_by_uid") == 0 ? null :
                                                               resultSet.getInt("vos_modified_by_uid"));
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
    if (resultSet.getInt("vos_bans_modified_by_uid") == 0) {
      banOnVo.setModifiedByUid(null);
    } else {
      banOnVo.setModifiedByUid(resultSet.getInt("vos_bans_modified_by_uid"));
    }
    if (resultSet.getInt("vos_bans_created_by_uid") == 0) {
      banOnVo.setCreatedByUid(null);
    } else {
      banOnVo.setCreatedByUid(resultSet.getInt("vos_bans_created_by_uid"));
    }
    return banOnVo;
  };
  private static final Logger LOG = LoggerFactory.getLogger(VosManagerImpl.class);
  // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
  private final JdbcPerunTemplate jdbc;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  /**
   * Constructor.
   *
   * @param perunPool connection pool instance
   */
  public VosManagerImpl(DataSource perunPool) {
    this.jdbc = new JdbcPerunTemplate(perunPool);
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
    this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
    this.namedParameterJdbcTemplate.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

  @Override
  public void addMemberVo(PerunSession sess, Vo vo, Vo memberVo) throws RelationExistsException {
    try {
      jdbc.update("INSERT INTO vos_vos(vo_id, member_vo_id, created_at, created_by, " +
                      "modified_at, modified_by) VALUES(?,?," + Compatibility.getSysdate() + ",?," +
                      Compatibility.getSysdate() + ",?)",
          vo.getId(), memberVo.getId(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor());
    } catch (DataIntegrityViolationException ex) {
      throw new RelationExistsException("Relation between " + vo + " and its member " + memberVo + " already exists.",
          ex);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void checkVoExists(PerunSession sess, Vo vo) throws VoNotExistsException {
    if (!voExists(sess, vo)) {
      throw new VoNotExistsException("Vo: " + vo);
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
  public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException {
    Utils.notNull(vo.getName(), "vo.getName()");
    Utils.notNull(vo.getShortName(), "vo.getShortName()");


    // Check if the Vo already exists (first check by ID, second by shortName attribute
    if (this.voExists(sess, vo)) {
      throw new VoExistsException(vo.toString());
    }
    if (this.shortNameForVoExists(sess, vo)) {
      throw new VoExistsException(vo.toString());
    }

    try {
      // Get VO ID
      return jdbc.queryForObject(
          "insert into vos(id, name, short_name, created_by,modified_by, created_by_uid, modified_by_uid)" +
              " values (nextval('vos_id_seq'),?,?,?,?,?,?) returning " +
              VO_MAPPING_SELECT_QUERY,
          VO_MAPPER, vo.getName(), vo.getShortName(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
          sess.getPerunPrincipal().getUserId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public Vo deleteVo(PerunSession sess, Vo vo) {
    try {
      // Delete authz entries for this VO
      AuthzResolverBlImpl.removeAllAuthzForVo(sess, vo);

      vo =
          jdbc.queryForObject("delete from vos where id=? returning " + VO_MAPPING_SELECT_QUERY, VO_MAPPER, vo.getId());
    } catch (EmptyResultDataAccessException e) {
      throw new ConsistencyErrorException("no record was deleted from the DB.");
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

    LOG.debug("Vo {} deleted", vo);

    return vo;
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
  public List<Group> getAdminGroups(PerunSession sess, Vo vo, String role) {
    try {
      return jdbc.query("select " + GroupsManagerImpl.GROUP_MAPPING_SELECT_QUERY + " from authz join groups on " +
                            "authz.authorized_group_id=groups.id where authz.vo_id=?" +
                            " and authz.role_id=(select id from roles where name=?)",
          GroupsManagerImpl.GROUP_MAPPER, vo.getId(), role.toLowerCase());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Deprecated
  @Override
  public List<Group> getAdminGroups(PerunSession sess, Vo vo) {
    try {
      return jdbc.query("select " + GroupsManagerImpl.GROUP_MAPPING_SELECT_QUERY + " from authz join groups on " +
                            "authz.authorized_group_id=groups.id where authz.vo_id=?" +
                            " and authz.role_id=(select id from roles where name='voadmin')",
          GroupsManagerImpl.GROUP_MAPPER, vo.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<User> getAdmins(PerunSession sess, Vo vo, String role) {
    try {
      // direct admins
      Set<User> setOfAdmins = new HashSet<>(jdbc.query(
          "select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY + " from authz join users on authz.user_id=users.id " +
              "where authz.vo_id=? and authz.role_id=(select id from roles where name=?)", UsersManagerImpl.USER_MAPPER,
          vo.getId(), role.toLowerCase()));

      // admins through a group
      List<Group> listOfGroupAdmins = getAdminGroups(sess, vo, role);
      for (Group group : listOfGroupAdmins) {
        setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY +
                                          " from users join members on users.id=members.user_id " +
                                          "join groups_members on groups_members.member_id=members.id " +
                                          "and groups_members.source_group_status=? where groups_members.group_id=? " +
                                          "and members.status=?",
            UsersManagerImpl.USER_MAPPER, MemberGroupStatus.VALID.getCode(), group.getId(), Status.VALID.getCode()));
      }

      return new ArrayList(setOfAdmins);

    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Deprecated
  @Override
  public List<User> getAdmins(PerunSession sess, Vo vo) {
    try {
      // direct admins
      Set<User> setOfAdmins = new HashSet<>(jdbc.query(
          "select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY + " from authz join users on authz.user_id=users.id " +
              "where authz.vo_id=? and authz.role_id=(select id from roles where name='voadmin')",
          UsersManagerImpl.USER_MAPPER, vo.getId()));

      // admins through a group
      List<Group> listOfGroupAdmins = getAdminGroups(sess, vo);
      for (Group group : listOfGroupAdmins) {
        setOfAdmins.addAll(jdbc.query("select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY +
                                          " from users join members on users.id=members.user_id " +
                                          "join groups_members on groups_members.member_id=members.id " +
                                          "where groups_members.group_id=?",
            UsersManagerImpl.USER_MAPPER, group.getId()));
      }

      return new ArrayList(setOfAdmins);

    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<BanOnVo> getAllExpiredBansOnVos(PerunSession sess) {
    try {
      return jdbc.query("select " + BAN_ON_VO_MAPPING_SELECT_QUERY + " from vos_bans where banned_to < " +
                           Compatibility.getSysdate(), BAN_ON_VO_MAPPER);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public BanOnVo getBanById(PerunSession sess, int banId) throws BanNotExistsException {
    try {
      return jdbc.queryForObject("select " + BAN_ON_VO_MAPPING_SELECT_QUERY + " from vos_bans where id=? ",
          BAN_ON_VO_MAPPER, banId);
    } catch (EmptyResultDataAccessException ex) {
      throw new BanNotExistsException("Ban with id " + banId + " not exists for any vo.");
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public BanOnVo getBanForMember(PerunSession sess, int memberId) {
    try {
      return jdbc.queryForObject("select " + BAN_ON_VO_MAPPING_SELECT_QUERY + " from vos_bans where member_id=? ",
          BAN_ON_VO_MAPPER, memberId);
    } catch (EmptyResultDataAccessException ex) {
      return null;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<BanOnVo> getBansForUser(PerunSession sess, int userId) {
    try {
      return jdbc.query("select " + BAN_ON_VO_MAPPING_SELECT_QUERY +
                            " from vos_bans join members on vos_bans.member_id=members.id where members.user_id=?",
          BAN_ON_VO_MAPPER, userId);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<BanOnVo> getBansForVo(PerunSession sess, int voId) {
    try {
      return jdbc.query("select " + BAN_ON_VO_MAPPING_SELECT_QUERY + " from vos_bans where vo_id=?",
          BAN_ON_VO_MAPPER, voId);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<User> getDirectAdmins(PerunSession sess, Vo vo, String role) {
    try {
      return jdbc.query(
          "select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY + " from authz join users on authz.user_id=users.id " +
              "where authz.vo_id=? and authz.role_id=(select id from roles where name=?)", UsersManagerImpl.USER_MAPPER,
          vo.getId(), role.toLowerCase());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Deprecated
  @Override
  public List<User> getDirectAdmins(PerunSession sess, Vo vo) {
    try {
      return jdbc.query(
          "select " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY + " from authz join users on authz.user_id=users.id " +
              "where authz.vo_id=? and authz.role_id=(select id from roles where name='voadmin')",
          UsersManagerImpl.USER_MAPPER, vo.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Vo> getMemberVos(PerunSession sess, int voId) {
    try {
      return jdbc.query("SELECT " + VO_MAPPING_SELECT_QUERY + " FROM vos_vos JOIN vos " +
                            "ON vos.id = vos_vos.member_vo_id WHERE vo_id=?", VO_MAPPER, voId);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Vo> getParentVos(PerunSession sess, int memberVoId) {
    try {
      return jdbc.query("SELECT " + VO_MAPPING_SELECT_QUERY + " FROM vos_vos JOIN vos " +
                            "ON vos.id = vos_vos.vo_id WHERE member_vo_id=?", VO_MAPPER, memberVoId);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Integer> getVoApplicationIds(PerunSession sess, Vo vo) {
    // get app ids for all applications
    try {
      return jdbc.query("select id from application where vo_id=?",
          (resultSet, arg1) -> resultSet.getInt("id"), vo.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public Vo getVoById(PerunSession sess, int id) throws VoNotExistsException {
    try {
      return jdbc.queryForObject("select " + VO_MAPPING_SELECT_QUERY + " from vos where id=?", VO_MAPPER, id);
    } catch (EmptyResultDataAccessException ex) {
      throw new VoNotExistsException(ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Vo getVoByShortName(PerunSession sess, String shortName) throws VoNotExistsException {
    try {
      return jdbc.queryForObject("select " + VO_MAPPING_SELECT_QUERY + " from vos where short_name=?", VO_MAPPER,
          shortName);
    } catch (EmptyResultDataAccessException ex) {
      throw new VoNotExistsException(ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Vo> getVos(PerunSession sess) {
    try {
      return jdbc.query("select " + VO_MAPPING_SELECT_QUERY + " from vos", VO_MAPPER);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Vo> getVosByIds(PerunSession perunSession, List<Integer> ids) {
    try {
      return jdbc.execute(
          "select " + VO_MAPPING_SELECT_QUERY + " from vos where id " + Compatibility.getStructureForInClause(),
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
  public int getVosCount(PerunSession sess) {
    try {
      return jdbc.queryForInt("select count(*) from vos");
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean hasEmbeddedGroupsItemInForm(PerunSession sess, int voId) {
    int count = jdbc.queryForInt("SELECT count(*) FROM application_form_items WHERE form_id = " +
                                     "(SELECT id FROM application_form WHERE vo_id = ? AND group_id IS NULL)" +
                                     " AND type = 'EMBEDDED_GROUP_APPLICATION';",
        voId);
    return count > 0;
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
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
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
  public void removeMemberVo(PerunSession sess, Vo vo, Vo memberVo) throws RelationNotExistsException {
    try {
      if (0 == jdbc.update("DELETE FROM vos_vos WHERE vo_id = ? AND member_vo_id = ?",
          vo.getId(), memberVo.getId())) {
        throw new RelationNotExistsException(
            "Relation between " + vo + " and member vo " + memberVo + " does not exist.");
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Vo> searchForVos(PerunSession sess, String searchString, boolean includeIDs) {
    MapSqlParameterSource namedParams = new MapSqlParameterSource();
    namedParams.addValue("searchString", searchString);
    // String idSearch = includeIDs ? "OR id::varchar(255) ILIKE '%' || (:searchString) || '%'" : "";
    String idSearch = "";
    if (includeIDs) {
      try {
        namedParams.addValue("searchId", Integer.parseInt(searchString));
        idSearch = " OR id = (:searchId) ";
      } catch (NumberFormatException e) {
        // ignore
      }
    }

    try {
      return namedParameterJdbcTemplate.query(
          "SELECT " + VO_MAPPING_SELECT_QUERY + " FROM vos " +
              "WHERE unaccent(name) ILIKE '%' || unaccent((:searchString)) || '%'" +
              " OR unaccent(short_name) ILIKE '%' || unaccent((:searchString)) || '%' " + idSearch +
              " LIMIT " + Utils.GLOBAL_SEARCH_LIMIT,
          namedParams, VO_MAPPER);
    } catch (EmptyResultDataAccessException ex) {
      // Return empty list
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Vo> searchForVos(PerunSession sess, String searchString, Set<Integer> voIds, boolean includeIDs) {
    MapSqlParameterSource namedParams = new MapSqlParameterSource();
    namedParams.addValue("searchString", searchString);
    namedParams.addValue("voIds", voIds);
    // String idSearch = includeIDs ? " OR id::varchar(255) ILIKE '%' || (:searchString) || '%' " : "";
    String idSearch = "";
    if (includeIDs) {
      try {
        namedParams.addValue("searchId", Integer.parseInt(searchString));
        idSearch = " OR id = (:searchId) ";
      } catch (NumberFormatException e) {
        // ignore
      }
    }

    try {
      return namedParameterJdbcTemplate.query(
          "SELECT " + VO_MAPPING_SELECT_QUERY + " FROM vos " +
              "WHERE id IN (:voIds) AND " +
              " (unaccent(name) ILIKE '%' || unaccent((:searchString)) || '%'" +
              " OR unaccent(short_name) ILIKE '%' || unaccent((:searchString)) || '%' " + idSearch +
              ") LIMIT " + Utils.GLOBAL_SEARCH_LIMIT,
          namedParams, VO_MAPPER);
    } catch (EmptyResultDataAccessException ex) {
      // Return empty list
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public BanOnVo setBan(PerunSession sess, BanOnVo banOnVo) {
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
                      "modified_by_uid) values (?,?," + (banOnVo.getValidityTo() != null ? "'" + Compatibility.getDate(
              banOnVo.getValidityTo().getTime()) + "'" : "DEFAULT") + ",?,?,?,?,?,?)",
          newId,
          banOnVo.getDescription(),
          banOnVo.getMemberId(),
          banOnVo.getVoId(),
          sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(),
          sess.getPerunPrincipal().getUserId()
      );

      banOnVo.setId(newId);
      // need to adjust date in object if original date was null and default date was assigned in db
      if (banOnVo.getValidityTo() == null) {
        banOnVo.setValidityTo(
            jdbc.queryForObject("select banned_to from vos_bans where id =" + newId, Timestamp.class));
      }

      return banOnVo;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  public boolean shortNameForVoExists(PerunSession sess, Vo vo) {
    Utils.notNull(vo, "vo");
    try {
      int numberOfExistences = jdbc.queryForInt("select count(1) from vos where short_name=?", vo.getShortName());
      if (numberOfExistences == 1) {
        return true;
      } else if (numberOfExistences > 1) {
        throw new ConsistencyErrorException("Short name " + vo.getShortName() + " exists more than once.");
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public BanOnVo updateBan(PerunSession sess, BanOnVo banOnVo) {
    try {
      jdbc.update("UPDATE vos_bans SET " +
                      "description=?, " +
                      "banned_to=" + (banOnVo.getValidityTo() != null ?
                                          "'" + Compatibility.getDate(banOnVo.getValidityTo().getTime()) + "' " :
                                          "DEFAULT") +
                      ", modified_by=?, " +
                      "modified_by_uid=?, " +
                      "modified_at= " + Compatibility.getSysdate() +
                      " WHERE id=?",
          banOnVo.getDescription(),
          sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(),
          banOnVo.getId());

      // need to adjust date in object if original date was null and default date was assigned in db
      if (banOnVo.getValidityTo() == null) {
        banOnVo.setValidityTo(
            jdbc.queryForObject("select banned_to from vos_bans where id =" + banOnVo.getId(), Timestamp.class));
      }

    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }

    return banOnVo;
  }

  @Override
  public Vo updateVo(PerunSession sess, Vo vo) {
    try {
      Map<String, Object> map = jdbc.queryForMap("select name, short_name from vos where id=?", vo.getId());

      if (!vo.getName().equals(map.get("name"))) {
        jdbc.update(
            "update vos set name=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() +
                "  where id=?", vo.getName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
            vo.getId());
      }

      return vo;
    } catch (EmptyResultDataAccessException ex) {
      throw new ConsistencyErrorException("Updating non existing VO", ex);
    } catch (RuntimeException ex) {
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
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }
}
