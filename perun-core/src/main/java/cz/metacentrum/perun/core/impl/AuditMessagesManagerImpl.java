package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private static final Map<Class<?>,Class<?>> mixinMap = new HashMap<>();
	private final static String auditMessageMappingSelectQuery = "id, msg, actor, created_at, created_by_uid";

	private final JdbcPerunTemplate jdbc;

	static {

		// configure JSON deserializer for auditer log
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.enableDefaultTyping();

		mixinMap.put(Attribute.class, JsonDeserializer.AttributeMixIn.class);
		mixinMap.put(AttributeDefinition.class, JsonDeserializer.AttributeDefinitionMixIn.class);
		mixinMap.put(User.class, JsonDeserializer.UserMixIn.class);
		mixinMap.put(Member.class, JsonDeserializer.MemberMixIn.class);
		mixinMap.put(PerunBean.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(Candidate.class, JsonDeserializer.CandidateMixIn.class);
		mixinMap.put(PerunException.class, JsonDeserializer.PerunExceptionMixIn.class);
		mixinMap.put(Destination.class, JsonDeserializer.DestinationMixIn.class);
		mixinMap.put(Group.class, JsonDeserializer.GroupMixIn.class);
		mixinMap.put(UserExtSource.class, JsonDeserializer.UserExtSourceMixIn.class);

		// we probably do not log these objects to auditer log, but to be sure we could read them later they are included

		mixinMap.put(Application.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(ApplicationForm.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(ApplicationFormItem.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(ApplicationFormItemWithPrefilledValue.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(ApplicationMail.class, JsonDeserializer.PerunBeanMixIn.class);

		mixinMap.put(Author.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(Category.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(Publication.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(PublicationForGUI.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(PublicationSystem.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(Thanks.class, JsonDeserializer.PerunBeanMixIn.class);
		mixinMap.put(ThanksForGUI.class, JsonDeserializer.PerunBeanMixIn.class);

		mapper.setMixIns(mixinMap);

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
			return jdbc.query("select " + auditMessageMappingSelectQuery + " from (select " + auditMessageMappingSelectQuery + Compatibility.getRowNumberOver() + " from auditer_log ORDER BY id desc) "+Compatibility.getAsAlias("temp")+" where rownumber <= ?",
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
			jdbc.update("update auditer_consumers set last_processed_id=?, modified_at=" + Compatibility.getSysdate() + " where name=?", lastProcessedId, consumerName);
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
			int lastProcessedId = getLastMessageId(perunSession);
			int consumerId = Utils.getNewId(jdbc, "auditer_consumers_id_seq");
			jdbc.update("insert into auditer_consumers (id, name, last_processed_id) values (?,?,?)", consumerId, consumerName, lastProcessedId);
			log.debug("New consumer [name: '{}', lastProcessedId: '{}'] created.", consumerName, lastProcessedId);
		} catch(Exception e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<AuditMessage> pollConsumerMessages(PerunSession perunSession, String consumerName) throws InternalErrorException {

		checkAuditerConsumerExists(perunSession, consumerName);

		try {

			List<AuditMessage> messages = new ArrayList<>();

			int lastProcessedId = getLastProcessedId(consumerName);
			int maxId = getLastMessageId(perunSession);
			if(maxId > lastProcessedId) {
				// get messages
				messages = jdbc.query("select " + auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDIT_MESSAGE_MAPPER, lastProcessedId, maxId);
				// update counter
				setLastProcessedId(perunSession, consumerName, maxId);
			}
			return messages;
		} catch(Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<AuditEvent> pollConsumerEvents(PerunSession perunSession, String consumerName) throws InternalErrorException {

		checkAuditerConsumerExists(perunSession, consumerName);

		try {

			List<AuditEvent> eventList = new ArrayList<>();

			int lastProcessedId = getLastProcessedId(consumerName);
			int maxId = getLastMessageId(perunSession);
			if (maxId > lastProcessedId) {
				// get events
				eventList = jdbc.query("select " + auditMessageMappingSelectQuery + " from auditer_log where id > ? and id <= ? order by id", AUDIT_EVENT_MAPPER, lastProcessedId, maxId);
				// update counter
				setLastProcessedId(perunSession, consumerName, maxId);
			}

			return eventList;

		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public Map<String, Integer> getAllAuditerConsumers(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select name, last_processed_id from auditer_consumers", AUDITER_CONSUMER_EXTRACTOR);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean checkAuditerConsumerExists(PerunSession session, String consumerName) throws InternalErrorException {
		if (consumerName == null) throw new InternalErrorException("Auditer consumer doesn't exist.");
		try {
			return jdbc.queryForInt("select count(*) from auditer_consumers where name=?", consumerName) == 1;
		} catch (Exception ex) {
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
	private int getLastProcessedId(String consumerName) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select last_processed_id from auditer_consumers where name=? for update", consumerName);
		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}
	}

}
