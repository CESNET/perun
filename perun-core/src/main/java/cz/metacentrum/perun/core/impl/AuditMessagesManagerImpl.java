package cz.metacentrum.perun.core.impl;

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
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.implApi.AuditMessagesManagerImplApi;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemWithPrefilledValue;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import cz.metacentrum.perun.rpclib.impl.JsonDeserializer;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AuditMessagesManagerImplApi with methods used to read stored auditer messages.
 *
 * @author Pavel Zlámal
 */
public class AuditMessagesManagerImpl implements AuditMessagesManagerImplApi {

	private final static Logger log = LoggerFactory.getLogger(AuditMessagesManagerImpl.class);
	private final static ObjectMapper mapper = new ObjectMapper();
	private final static String auditMessageMappingSelectQuery = "id, msg, actor, created_at, created_by_uid";

	private final JdbcPerunTemplate jdbc;

	static {

		// configure JSON deserializer for auditer log
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enableDefaultTyping();

		mapper.getDeserializationConfig().addMixInAnnotations(Attribute.class, JsonDeserializer.AttributeMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(AttributeDefinition.class, JsonDeserializer.AttributeDefinitionMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(User.class, JsonDeserializer.UserMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Member.class, JsonDeserializer.MemberMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PerunBean.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Candidate.class, JsonDeserializer.CandidateMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PerunException.class, JsonDeserializer.PerunExceptionMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Destination.class, JsonDeserializer.DestinationMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Group.class, JsonDeserializer.GroupMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Group.class, JsonDeserializer.UserExtSourceMixIn.class);

		// we probably do not log these objects to auditer log, but to be sure we could read them later they are included

		mapper.getDeserializationConfig().addMixInAnnotations(Application.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationForm.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationFormItem.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationFormItemWithPrefilledValue.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ApplicationMail.class, JsonDeserializer.PerunBeanMixIn.class);

		mapper.getDeserializationConfig().addMixInAnnotations(Author.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Category.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Publication.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PublicationForGUI.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(PublicationSystem.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(Thanks.class, JsonDeserializer.PerunBeanMixIn.class);
		mapper.getDeserializationConfig().addMixInAnnotations(ThanksForGUI.class, JsonDeserializer.PerunBeanMixIn.class);

	}

	private static final RowMapper<AuditEvent> AUDIT_EVENT_MAPPER = new RowMapper<AuditEvent>() {
		@Override
		public AuditEvent mapRow(ResultSet resultSet, int i) throws SQLException {
			try {
				return mapper.readValue(resultSet.getString("msg"), AuditEvent.class);
			} catch (JsonParseException | JsonMappingException ex) {
				log.error("Can't parse JSON auditer log!", ex);
				throw new SQLException(ex);
			} catch (IOException ex) {
				throw new SQLException(ex);
			}

		}
	};

	private static final RowMapper<AuditMessage> AUDIT_MESSAGE_MAPPER = new RowMapper<AuditMessage>() {
		@Override
		public AuditMessage mapRow(ResultSet resultSet, int i) throws SQLException {

			AuditEvent event = AUDIT_EVENT_MAPPER.mapRow(resultSet, i);

			Integer principalUserId = null;
			if (resultSet.getInt("created_by_uid") != 0) principalUserId = resultSet.getInt("created_by_uid");
			return new AuditMessage(resultSet.getInt("id"), event, resultSet.getString("actor"),
					resultSet.getString("created_at"), principalUserId);

		}
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

	public AuditMessagesManagerImpl(DataSource perunPool) {
		this.jdbc = new JdbcPerunTemplate(perunPool);
	}

	@Override
	public List<AuditMessage> getMessages(PerunSession perunSession, int count) throws InternalErrorException {
		try {
			return jdbc.query("select " + auditMessageMappingSelectQuery + " from (select " + auditMessageMappingSelectQuery + Compatibility.getRowNumberOver() + " from auditer_log ORDER BY id Desc limit ?) "+Compatibility.getAsAlias("temp"),
					AUDIT_MESSAGE_MAPPER, count);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<AuditMessage> getMessagesByCount(PerunSession perunSession, int count) throws InternalErrorException {
		try {
			return jdbc.query("select " + auditMessageMappingSelectQuery + " from auditer_log where id > ((select max(id) from auditer_log)-?) order by id desc", AUDIT_MESSAGE_MAPPER, count);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public int getLastMessageId(PerunSession perunSession) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select max(id) from auditer_log");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void setLastProcessedId(PerunSession perunSession, String consumerName, int lastProcessedId) throws InternalErrorException {
		try {
			jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId ,consumerName);
		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public int getAuditerMessagesCount(PerunSession perunSession) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(id) from auditer_log");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void createAuditerConsumer(PerunSession perunSession, String consumerName) throws InternalErrorException {
		try {
			int lastProcessedId = jdbc.queryForInt("select max(id) from auditer_log");

			int consumerId = Utils.getNewId(jdbc, "auditer_consumers_id_seq");
			jdbc.update("insert into auditer_consumers (id, name, last_processed_id) values (?,?,?)", consumerId, consumerName, lastProcessedId);
			log.debug("New consumer [name: '{}', lastProcessedId: '{}'] created.", consumerName, lastProcessedId);
		} catch(Exception e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName) throws InternalErrorException {
		if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");

		try {
			if(jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
				throw new InternalErrorException("Auditer consumer doesn't exist.");
			}

			int lastProcessedId = getLastProcessedId(consumerName);

			int maxId = jdbc.queryForInt("select max(id) from auditer_log");
			if(maxId > lastProcessedId) {
				List<AuditMessage> messages = jdbc.query("select " + auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDIT_MESSAGE_MAPPER, lastProcessedId, maxId);
				lastProcessedId = maxId;
				jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
				return messages;
			}
			return new ArrayList<>();
		} catch(Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName) throws InternalErrorException {
		if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");

		try {

			if(jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) != 1) {
				throw new InternalErrorException("Auditer consumer doesn't exist.");
			}

			List<AuditEvent> eventList = new ArrayList<>();

			int lastProcessedId = getLastProcessedId(consumerName);
			int maxId = jdbc.queryForInt("select max(id) from auditer_log");
			if (maxId > lastProcessedId) {

				eventList = jdbc.query("select " + auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDIT_EVENT_MAPPER, lastProcessedId, maxId);

				// we successfully de-serialized all messages, bump counter
				jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", maxId, consumerName);

			}

			return eventList;

		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public Map<String, Integer> getAllAuditerConsumers(PerunSession sess) throws InternalErrorException {
		Map<String, Integer> auditerConsumers;

		try {
			auditerConsumers = jdbc.query("select name, last_processed_id from auditer_consumers", AUDITER_CONSUMER_EXTRACTOR);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		return auditerConsumers;
	}

	/**
	 * Return last processed ID of audit message for specified consumer.
	 *
	 * @param consumerName Name of consumer
	 * @return ID of last processed message
	 * @throws InternalErrorException When implementation failse
	 */
	private int getLastProcessedId(String consumerName) throws InternalErrorException {
		int lastProcessedId;

		try {
			lastProcessedId = jdbc.queryForInt("select last_processed_id from auditer_consumers where name=? for update", consumerName);
		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}

		return lastProcessedId;

	}

}
