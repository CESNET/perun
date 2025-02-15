package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MessagesPageQuery;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.implApi.AuditMessagesManagerImplApi;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import cz.metacentrum.perun.rpclib.impl.JsonDeserializer;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Implementation of AuditMessagesManagerImplApi with methods used to read stored auditer messages.
 *
 * @author Pavel Zlámal
 */
public class AuditMessagesManagerImpl implements AuditMessagesManagerImplApi {

  private static final Logger LOG = LoggerFactory.getLogger(AuditMessagesManagerImpl.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Map<Class<?>, Class<?>> MIXIN_MAP = new HashMap<>();
  private static final String AUDIT_MESSAGE_MAPPING_SELECT_QUERY = "id, msg, actor, created_at, created_by_uid";
  private static final String WHERE_EXISTING_EVENT;

  private static final int AUDITLOG_READ_LIMIT = BeansUtils.getCoreConfig().getAuditlogReadLimit();
  private static final int PAGE_COUNT_PRECISION = 1000;
  private static final RowMapper<AuditEvent> AUDIT_EVENT_MAPPER = (resultSet, i) -> {
    try {
      return MAPPER.readValue(resultSet.getString("msg"), AuditEvent.class);
    } catch (JsonParseException | JsonMappingException ex) {
      LOG.error("Can't parse JSON auditer log!", ex);
      throw new SQLException(ex);
    } catch (IOException ex) {
      throw new SQLException(ex);
    }

  };
  private static final RowMapper<AuditMessage> AUDIT_MESSAGE_MAPPER = (resultSet, i) -> {

    AuditEvent event = AUDIT_EVENT_MAPPER.mapRow(resultSet, i);

    Integer principalUserId = null;
    if (resultSet.getInt("created_by_uid") != 0) {
      principalUserId = resultSet.getInt("created_by_uid");
    }
    return new AuditMessage(resultSet.getInt("id"), event, resultSet.getString("actor"),
        resultSet.getString("created_at"), principalUserId);

  };
  private static final ResultSetExtractor<Map<String, Integer>> AUDITER_CONSUMER_EXTRACTOR = resultSet -> {
    Map<String, Integer> auditerConsumers = new HashMap<>();
    while (resultSet.next()) {
      // fetch from map by ID
      String name = resultSet.getString("name");
      Integer lastProcessedId = resultSet.getInt("last_processed_id");
      auditerConsumers.put(name, lastProcessedId);
    }
    return auditerConsumers;
  };

  static {

    JavaTimeModule module = new JavaTimeModule();
    MAPPER.registerModule(module);
    // make mapper to serialize dates and timestamps like "YYYY-MM-DD" or "YYYY-MM-DDTHH:mm:ss.SSSSSS"
    MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // configure JSON deserializer for auditer log
    MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    MAPPER.enableDefaultTyping();

    MIXIN_MAP.put(Attribute.class, JsonDeserializer.AttributeMixIn.class);
    MIXIN_MAP.put(AttributeDefinition.class, JsonDeserializer.AttributeDefinitionMixIn.class);
    MIXIN_MAP.put(User.class, JsonDeserializer.UserMixIn.class);
    MIXIN_MAP.put(Member.class, JsonDeserializer.MemberMixIn.class);
    MIXIN_MAP.put(PerunBean.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(Candidate.class, JsonDeserializer.CandidateMixIn.class);
    MIXIN_MAP.put(PerunException.class, JsonDeserializer.PerunExceptionMixIn.class);
    MIXIN_MAP.put(Destination.class, JsonDeserializer.DestinationMixIn.class);
    MIXIN_MAP.put(Group.class, JsonDeserializer.GroupMixIn.class);
    MIXIN_MAP.put(UserExtSource.class, JsonDeserializer.UserExtSourceMixIn.class);

    // we probably do not log these objects to auditer log, but to be sure we could read them later they are included

    MIXIN_MAP.put(Application.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(ApplicationForm.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(ApplicationFormItem.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(ApplicationFormItemWithPrefilledValue.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(ApplicationMail.class, JsonDeserializer.PerunBeanMixIn.class);

    MIXIN_MAP.put(Author.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(Category.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(Publication.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(PublicationForGUI.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(PublicationSystem.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(Thanks.class, JsonDeserializer.PerunBeanMixIn.class);
    MIXIN_MAP.put(ThanksForGUI.class, JsonDeserializer.PerunBeanMixIn.class);

    MAPPER.setMixIns(MIXIN_MAP);

  }

  public static final List<String> EXISTING_EVENT_CLASS_NAMES = new ArrayList<>();

  static {
    Reflections reflections = new Reflections("cz.metacentrum.perun.audit.events");
    List<Class<? extends AuditEvent>> classes = new ArrayList<>(reflections.getSubTypesOf(AuditEvent.class));
    StringBuilder builder = new StringBuilder();
    builder.append(" split_part(msg::json ->>'name', '.', 7) IN (");
    for (Class<? extends AuditEvent> clazz : classes) {
      EXISTING_EVENT_CLASS_NAMES.add(clazz.getSimpleName());
      builder.append("'").append(clazz.getSimpleName()).append("'").append(", ");
    }
    builder.deleteCharAt(builder.length() - 1).deleteCharAt(builder.length() - 1);
    builder.append(") ");
    WHERE_EXISTING_EVENT = builder.toString();
  }

  private final JdbcPerunTemplate jdbc;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public AuditMessagesManagerImpl(DataSource perunPool) {
    this.jdbc = new JdbcPerunTemplate(perunPool);
    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(perunPool);
  }

  /**
   * Returns ResultSetExtractor that can be used to extract returned paginated audit messages from db.
   *
   * @param query query data
   * @return extractor, that can be used to extract returned paginated audit messages from db
   */
  private static ResultSetExtractor<Paginated<AuditMessage>> getPaginatedMessagesExtractor(MessagesPageQuery query) {
    return resultSet -> {
      List<AuditMessage> messages = new ArrayList<>();
      int totalCount = 0;
      int row = 0;
      while (resultSet.next()) {
        totalCount = resultSet.getInt("total_count");
        messages.add(AUDIT_MESSAGE_MAPPER.mapRow(resultSet, row));
        row++;
      }

      if (messages.size() > 0 && totalCount == 0) {
        totalCount = PAGE_COUNT_PRECISION + 1;
      }

      return new Paginated<>(messages, query.getOffset(), query.getPageSize(), totalCount);
    };
  }

  @Override
  public boolean checkAuditerConsumerExists(PerunSession session, String consumerName) {
    if (consumerName == null) {
      throw new InternalErrorException("Auditer consumer doesn't exist.");
    }
    try {
      return jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) == 1;
    } catch (Exception ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void createAuditerConsumer(PerunSession perunSession, String consumerName) {
    try {
      int lastProcessedId = getLastMessageId(perunSession);
      int consumerId = Utils.getNewId(jdbc, "auditer_consumers_id_seq");
      jdbc.update("insert into auditer_consumers (id, name, last_processed_id) values (?,?,?)", consumerId,
          consumerName, lastProcessedId);
      LOG.debug("New consumer [name: '{}', lastProcessedId: '{}'] created.", consumerName, lastProcessedId);
    } catch (Exception e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public Map<String, Integer> getAllAuditerConsumers(PerunSession sess) {
    try {
      return jdbc.query("select name, last_processed_id from auditer_consumers", AUDITER_CONSUMER_EXTRACTOR);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public int getAuditerMessagesCount(PerunSession perunSession) {
    try {
      return jdbc.queryForInt("select count(id) from auditer_log where " + WHERE_EXISTING_EVENT);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public int getLastMessageId(PerunSession perunSession) {
    try {
      return jdbc.queryForInt("select max(id) from auditer_log where " + WHERE_EXISTING_EVENT);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  /**
   * Return last processed ID of audit message for specified consumer.
   *
   * @param consumerName Name of consumer
   * @return ID of last processed message
   * @throws InternalErrorException When implementation failse
   */
  private int getLastProcessedId(String consumerName) {
    try {
      return jdbc.queryForInt("select last_processed_id from auditer_consumers where name=? for update", consumerName);
    } catch (Exception ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<AuditMessage> getMessages(PerunSession perunSession, int count) {
    try {
      return jdbc.query("select " + AUDIT_MESSAGE_MAPPING_SELECT_QUERY + " from (select " +
                            AUDIT_MESSAGE_MAPPING_SELECT_QUERY +
                        Compatibility.getRowNumberOver() + " from auditer_log where " + WHERE_EXISTING_EVENT +
                            " ORDER BY id desc) " + Compatibility.getAsAlias("temp") + " where rownumber <= ?",
          AUDIT_MESSAGE_MAPPER, count);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) {
    try {
      return jdbc.query("select " + AUDIT_MESSAGE_MAPPING_SELECT_QUERY +
                        " from auditer_log where id > ((select max(id) from auditer_log)-?) and " +
                            WHERE_EXISTING_EVENT + " order by id desc",
          AUDIT_MESSAGE_MAPPER, count);
    } catch (EmptyResultDataAccessException ex) {
      return new ArrayList<>();
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public List<AuditMessage> getMessagesByIdAndCount(PerunSession perunSession, int id, int count) {
    try {
      return jdbc.query(
          "select " + AUDIT_MESSAGE_MAPPING_SELECT_QUERY + " from auditer_log where id >=? and " +
              WHERE_EXISTING_EVENT + " order by id asc limit ?",
          AUDIT_MESSAGE_MAPPER, id, count);
    } catch (RuntimeException err) {
      throw new InternalErrorException(err);
    }
  }

  @Override
  public Paginated<AuditMessage> getMessagesPage(PerunSession perunSession, MessagesPageQuery query) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("selectedEvents", query.getSelectedEvents());
    String filter =
        !query.getSelectedEvents().isEmpty() ? "where split_part(msg::json ->>'name', '.', 7) IN (:selectedEvents)" :
            "where " + WHERE_EXISTING_EVENT;

    // take exact total count up to PAGE_COUNT_PRECISION entries, estimate it otherwise
    return namedParameterJdbcTemplate.query(
        "select " + AUDIT_MESSAGE_MAPPING_SELECT_QUERY +
            ", case when (select count(*) from (select 1 from auditer_log " +
        filter + " limit " + PAGE_COUNT_PRECISION + ") as sample) < " + PAGE_COUNT_PRECISION +
        " then (select count(*) from auditer_log " + filter + ")" +
        " else (select 100 * count(*) FROM auditer_log TABLESAMPLE SYSTEM (1) " + filter + ") end as total_count " +
        "from auditer_log " + filter + " order by id " +
            query.getOrder().getSqlValue() + " offset " +
        query.getOffset() + " limit " + query.getPageSize(), parameters, getPaginatedMessagesExtractor(query));
  }

  @Override
  public List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName, int lastProcessedId) {

    checkAuditerConsumerExists(perunSession, consumerName);

    try {

      List<AuditEvent> eventList = new ArrayList<>();

      int maxId = getLastMessageId(perunSession);
      if (maxId > lastProcessedId) {
        // get events
        eventList = jdbc.query(
            "select " + AUDIT_MESSAGE_MAPPING_SELECT_QUERY + " from auditer_log where id > ? and " +
                WHERE_EXISTING_EVENT + " order by id limit ?",
            AUDIT_EVENT_MAPPER, lastProcessedId, AUDITLOG_READ_LIMIT);
      }

      return eventList;

    } catch (Exception ex) {
      throw new InternalErrorException(ex);
    }

  }

  @Override
  public List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName) {

    checkAuditerConsumerExists(perunSession, consumerName);

    try {

      List<AuditMessage> messages = new ArrayList<>();

      int lastProcessedId = getLastProcessedId(consumerName);
      int maxId = getLastMessageId(perunSession);
      if (maxId > lastProcessedId) {
        // get messages
        messages = jdbc.query(
            "select " + AUDIT_MESSAGE_MAPPING_SELECT_QUERY + " from auditer_log where id > ? and " +
                WHERE_EXISTING_EVENT + " order by id limit ?",
            AUDIT_MESSAGE_MAPPER, lastProcessedId, AUDITLOG_READ_LIMIT);
        // update counter
        int currentLastId = messages.stream().max(Comparator.comparingInt(AuditMessage::getId)).orElseThrow().getId();
        setLastProcessedId(perunSession, consumerName, currentLastId);
      }
      return messages;
    } catch (Exception ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName, int lastProcessedId) {

    checkAuditerConsumerExists(perunSession, consumerName);

    try {

      List<AuditMessage> messages = new ArrayList<>();

      int maxId = getLastMessageId(perunSession);
      if (maxId > lastProcessedId) {
        // get messages
        messages = jdbc.query(
            "select " + AUDIT_MESSAGE_MAPPING_SELECT_QUERY + " from auditer_log where id > ? and " +
                WHERE_EXISTING_EVENT + " order by id limit ?",
            AUDIT_MESSAGE_MAPPER, lastProcessedId, AUDITLOG_READ_LIMIT);
      }
      return messages;
    } catch (Exception ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void setLastProcessedId(PerunSession perunSession, String consumerName, int lastProcessedId) {
    try {
      jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() +
                  " where name=?", lastProcessedId, consumerName);
    } catch (Exception ex) {
      throw new InternalErrorException(ex);
    }
  }

}
