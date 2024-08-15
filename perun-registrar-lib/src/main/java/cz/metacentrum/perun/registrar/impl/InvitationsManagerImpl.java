package cz.metacentrum.perun.registrar.impl;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.implApi.InvitationsManagerImplApi;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import cz.metacentrum.perun.registrar.model.InvitationWithSender;
import cz.metacentrum.perun.registrar.model.InvitationsPageQuery;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class InvitationsManagerImpl implements InvitationsManagerImplApi {
  private JdbcPerunTemplate jdbc;
  static final Logger LOG = LoggerFactory.getLogger(InvitationsManagerImpl.class);
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  protected static final String INVITATION_SELECT_QUERY = "invitations.id as invitations_id, " +
                                                              "invitations.token as invitations_token, " +
                                                              "invitations.vo_id as invitations_vo_id, " +
                                                              "invitations.group_id as invitations_group_id, " +
                                                              "invitations.application_id " +
                                                              "as invitations_application_id, " +
                                                              "invitations.sender_id as invitations_sender_id, " +
                                                              "invitations.receiver_name " +
                                                              "as invitations_receiver_name, " +
                                                              "invitations.receiver_email " +
                                                              "as invitations_receiver_email, " +
                                                              "invitations.redirect_url as invitations_redirect_url, " +
                                                              "invitations.language as invitations_language, " +
                                                              "invitations.expiration as invitations_expiration, " +
                                                              "invitations.status as invitations_status, " +
                                                              "invitations.created_at as invitations_created_at, " +
                                                              "invitations.created_by as invitations_created_by, " +
                                                              "invitations.modified_by as " +
          "invitations_modified_by, invitations.modified_at as invitations_modified_at, " +
                                                              "invitations.created_by_uid " +
          "as invitations_created_by_uid, invitations.modified_by_uid as invitations_modified_by_uid";

  protected static final String INVITATION_WITH_SENDER_SELECT_QUERY =
      "invitations.id as invitations_id, " +
      "invitations.token as invitations_token, " +
      "invitations.vo_id as invitations_vo_id, " +
      "invitations.group_id as invitations_group_id, " +
      "invitations.application_id as invitations_application_id, " +
      "invitations.sender_id as invitations_sender_id, " +
      "invitations.receiver_name as invitations_receiver_name, " +
      "invitations.receiver_email as invitations_receiver_email, " +
      "invitations.redirect_url as invitations_redirect_url, " +
      "invitations.language as invitations_language, " +
      "invitations.expiration as invitations_expiration, " +
      "invitations.status as invitations_status, " +
      "invitations.created_at as invitations_created_at, " +
      "invitations.created_by as invitations_created_by, " +
      "invitations.modified_by as " +
      "invitations_modified_by, invitations.modified_at as invitations_modified_at, " +
      "invitations.created_by_uid as invitations_created_by_uid, " +
      "invitations.modified_by_uid as invitations_modified_by_uid, " +
      "users.first_name as invitations_sender_first_name, " +
      "users.last_name as invitations_sender_last_name, " +
      "mails.attr_value as invitations_sender_email";

  protected static final RowMapper<Invitation> INVITATION_ROW_MAPPER =
      (resultSet, i) -> new Invitation(
          resultSet.getInt("invitations_id"),
          resultSet.getInt("invitations_vo_id"),
          resultSet.getInt("invitations_group_id"),
          resultSet.getObject("invitations_application_id", Integer.class),
          resultSet.getInt("invitations_sender_id"),
          resultSet.getString("invitations_receiver_name"),
          resultSet.getString("invitations_receiver_email"),
          resultSet.getString("invitations_redirect_url"),
          resultSet.getObject("invitations_token", UUID.class),
          new Locale(resultSet.getString("invitations_language")),
          resultSet.getTimestamp("invitations_expiration").toLocalDateTime().toLocalDate(),
          InvitationStatus.valueOf(resultSet.getString("invitations_status")),
          resultSet.getString("invitations_created_at"),
          resultSet.getString("invitations_created_by"),
          resultSet.getString("invitations_modified_at"),
          resultSet.getString("invitations_modified_by"),
          resultSet.getInt("invitations_created_by_uid") == 0 ? null
              : resultSet.getInt("invitations_created_by_uid"),
          resultSet.getInt("invitations_modified_by_uid") == 0 ? null :
              resultSet.getInt("invitations_modified_by_uid"));

  protected static final RowMapper<InvitationWithSender> INVITATION_WITH_SENDER_ROW_MAPPER =
      (resultSet, i) -> new InvitationWithSender(
          resultSet.getInt("invitations_id"),
          resultSet.getInt("invitations_vo_id"),
          resultSet.getInt("invitations_group_id"),
          resultSet.getObject("invitations_application_id", Integer.class),
          resultSet.getInt("invitations_sender_id"),
          resultSet.getString("invitations_receiver_name"),
          resultSet.getString("invitations_receiver_email"),
          resultSet.getString("invitations_redirect_url"),
          resultSet.getObject("invitations_token", UUID.class),
          new Locale(resultSet.getString("invitations_language")),
          resultSet.getTimestamp("invitations_expiration").toLocalDateTime().toLocalDate(),
          InvitationStatus.valueOf(resultSet.getString("invitations_status")),
          resultSet.getString("invitations_created_at"),
          resultSet.getString("invitations_created_by"),
          resultSet.getString("invitations_modified_at"),
          resultSet.getString("invitations_modified_by"),
          resultSet.getInt("invitations_created_by_uid") == 0 ? null
              : resultSet.getInt("invitations_created_by_uid"),
          resultSet.getInt("invitations_modified_by_uid") == 0 ? null :
              resultSet.getInt("invitations_modified_by_uid"),
          resultSet.getString("invitations_sender_first_name") + " " +
              resultSet.getString("invitations_sender_last_name"),
          resultSet.getString("invitations_sender_email"));

  public void setDataSource(DataSource dataSource) {
    this.jdbc = new JdbcPerunTemplate(dataSource);
    this.jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    this.namedParameterJdbcTemplate.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

  @Override
  public Invitation getInvitationById(PerunSession sess, int id) throws InvitationNotExistsException {
    try {
      return jdbc.queryForObject("select " + INVITATION_SELECT_QUERY + " from invitations where invitations.id = ?",
          INVITATION_ROW_MAPPER, id);
    } catch (EmptyResultDataAccessException ex) {
      throw new InvitationNotExistsException(ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Invitation getInvitationByToken(PerunSession sess, UUID token) throws InvitationNotExistsException {
    try {
      return jdbc.queryForObject("select " + INVITATION_SELECT_QUERY + " from invitations where invitations.token=?",
          INVITATION_ROW_MAPPER, token);
    } catch (EmptyResultDataAccessException ex) {
      throw new InvitationNotExistsException(ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Invitation getInvitationByApplication(PerunSession sess, Application application)
      throws InvitationNotExistsException {
    try {
      return jdbc.queryForObject("select " + INVITATION_SELECT_QUERY + " from invitations" +
                                     " where invitations.application_id=?", INVITATION_ROW_MAPPER, application.getId());
    } catch (EmptyResultDataAccessException ex) {
      throw new InvitationNotExistsException(ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Invitation> getInvitationsForSender(PerunSession sess, Group group, User user) {
    try {
      return jdbc.query("select " + INVITATION_SELECT_QUERY + " from invitations where invitations.group_id=? and " +
                            "invitations.sender_id=?",
          INVITATION_ROW_MAPPER, group.getId(), user.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Invitation> getInvitationsForGroup(PerunSession sess, Group group) {
    try {
      return jdbc.query("select " + INVITATION_SELECT_QUERY + " from invitations where " +
                            "invitations.group_id=?", INVITATION_ROW_MAPPER, group.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Invitation> getInvitationsForVo(PerunSession sess, Vo vo) {
    try {
      return jdbc.query("select " + INVITATION_SELECT_QUERY + " from invitations where invitations.vo_id=?",
          INVITATION_ROW_MAPPER, vo.getId());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Invitation> getAllInvitations(PerunSession sess, InvitationStatus status) {
    try {
      return jdbc.query("select " + INVITATION_SELECT_QUERY + " from invitations where status = ?::invitations_status",
              INVITATION_ROW_MAPPER, status.toString());
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Invitation createInvitation(PerunSession sess, Invitation invitation) {
    // TODO check for existing?

    try {
      int newId = Utils.getNewId(jdbc, "invitations_id_seq");
      // set app id as null for now, update later when invitation filled out and application created
      jdbc.update("insert into invitations(id, vo_id, group_id, application_id, sender_id, receiver_name, " +
                      "receiver_email, redirect_url, language, expiration, status, created_by, modified_by," +
                      " created_by_uid, modified_by_uid) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::invitations_status," +
                      " ?, ?, ?, ?)", newId, invitation.getVoId(), invitation.getGroupId(), null,
                      invitation.getSenderId(), invitation.getReceiverName(), invitation.getReceiverEmail(),
          invitation.getRedirectUrl(), invitation.getLanguage().toString(),
          Timestamp.valueOf(invitation.getExpiration().atStartOfDay()), invitation.getStatus().toString(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

      // get invitation to retrieve created uuid
      invitation = getInvitationById(sess, newId);
      LOG.info("Invitation {} created.", invitation);
      return invitation;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    } catch (InvitationNotExistsException ex) {
      throw new InternalErrorException("Error trying to create invitation " + invitation);
    }
  }

  @Override
  public void setInvitationStatus(PerunSession sess, Invitation invitation, InvitationStatus status) {
    try {
      jdbc.update(
          "update invitations set status=?::invitations_status, modified_by=?, modified_at= " +
              Compatibility.getSysdate() + ", modified_by_uid=? where id=?", status.toString(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), invitation.getId()
      );
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void setInvitationApplicationId(PerunSession sess, Invitation invitation, Integer applicationId) {
    try {
      jdbc.update(
          "update invitations set application_id=?, modified_by=?, modified_at= " +
              Compatibility.getSysdate() + ", modified_by_uid=? where id=?", applicationId,
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), invitation.getId()
      );
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Invitation setInvitationExpiration(PerunSession sess, Invitation invitation, LocalDate newExpirationDate) {
    try {
      jdbc.update(
          "update invitations set expiration=?, modified_by=?, modified_at= " +
              Compatibility.getSysdate() + ", modified_by_uid=? where id=?", newExpirationDate.atStartOfDay(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), invitation.getId()
      );

      return getInvitationById(sess, invitation.getId());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    } catch (InvitationNotExistsException e) {
      LOG.warn("Invitation was probably deleted while being updated.");
      throw new RuntimeException(e);
    }
  }

  @Override
  public Paginated<InvitationWithSender> getInvitationsPage(PerunSession sess, Group group,
                                                            InvitationsPageQuery query) {
    MapSqlParameterSource namedParams = new MapSqlParameterSource();

    namedParams.addValue("groupId", group.getId());
    namedParams.addValue("offset", query.getOffset());
    namedParams.addValue("limit", query.getPageSize());

    String selectQuery = getSQLSelectForInvitationsPage();
    String searchQuery = getSQLWhereForInvitationsPage(query, namedParams);

    String statusesQueryString = getInvitationStatusSQLConditionForInvitationsPage(query);
    String expirationQueryString = getExpirationSQLConditionForInvitationsPage(query, namedParams);

    return namedParameterJdbcTemplate.query(
        selectQuery + " WHERE invitations.group_id = (:groupId) " + expirationQueryString + statusesQueryString +
            searchQuery +
            " ORDER BY " + query.getSortColumn().getSqlOrderBy(query) + " OFFSET (:offset)" + " LIMIT (:limit)",
        namedParams, getPaginatedInvitationsExtractor(query));
  }

  private String getSQLSelectForInvitationsPage() {
    return "SELECT " + INVITATION_WITH_SENDER_SELECT_QUERY + ", count(*) OVER() AS total_count" + " FROM invitations " +
               "LEFT JOIN users ON invitations.sender_id = users.id " +
               "LEFT JOIN (user_attr_values JOIN (SELECT * FROM attr_names " +
               "WHERE attr_names.attr_name = '" + AttributesManager.NS_USER_ATTR_DEF + ":preferredMail') AS an " +
               "ON user_attr_values.attr_id = an.id) AS mails ON users.id = mails.user_id ";
  }

  private String getSQLWhereForInvitationsPage(InvitationsPageQuery query, MapSqlParameterSource namedParams) {
    if (isEmpty(query.getSearchString())) {
      return "";
    }
    return " AND " + Utils.prepareSqlWhereForInvitationSearch(query.getSearchString(), namedParams);
  }

  private String getInvitationStatusSQLConditionForInvitationsPage(InvitationsPageQuery query) {
    String statusesQueryString = "";
    if (query.getStatuses() != null && !query.getStatuses().isEmpty()) {
      String statusListString = query.getStatuses().stream()
                                    .map(status -> "'" + status.name() + "'")
                                    .collect(Collectors.joining(", "));

      statusesQueryString = " AND invitations.status IN (" + statusListString + ") ";
    }
    return statusesQueryString;
  }

  private String getExpirationSQLConditionForInvitationsPage(InvitationsPageQuery query,
                                                             MapSqlParameterSource namedParams) {
    if (query.getExpirationFrom() == null || query.getExpirationTo() == null) {
      return "";
    }

    namedParams.addValue("dateFrom", query.getExpirationFrom());
    namedParams.addValue("dateTo", query.getExpirationTo());

    return  " AND (:dateFrom) <= invitations.expiration::date AND invitations.expiration::date <= (:dateTo) ";
  }

  /**
   * Extractor for paginated invitations with sender's information
   *
   * @param query for invitations
   * @return extractor, that can be used to extract returned paginated invitations with sender's information from db
   */
  private static ResultSetExtractor<Paginated<InvitationWithSender>> getPaginatedInvitationsExtractor(
      InvitationsPageQuery query) {
    return resultSet -> {
      List<InvitationWithSender> invitationWithSenders = new ArrayList<>();
      int totalCount = 0;
      int row = 0;
      while (resultSet.next()) {
        totalCount = resultSet.getInt("total_count");
        invitationWithSenders.add(INVITATION_WITH_SENDER_ROW_MAPPER.mapRow(resultSet, row));
        row++;
      }
      return new Paginated<>(invitationWithSenders, query.getOffset(), query.getPageSize(), totalCount);
    };
  }

}
