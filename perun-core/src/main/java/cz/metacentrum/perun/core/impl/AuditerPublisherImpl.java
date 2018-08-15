package cz.metacentrum.perun.core.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.AuditerPublisher;
import cz.metacentrum.perun.core.implApi.PubSubMechanism;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents wrapper for AuditerConsumer and publishes messages received
 * from AuditerConsumer to PubSub mechanism
 *
 * @author Richard Husár 445238@mail.muni.cz
 */
public class AuditerPublisherImpl implements AuditerPublisher {
	private final static Logger log = LoggerFactory.getLogger(AuditerPublisherImpl.class);

	private PubSubMechanism pubSubMechanism;
	private AuditerConsumer consumer;

	@Autowired
	public AuditerPublisherImpl(PubSubMechanism pubSubMechanism, DataSource dataSource) throws InternalErrorException {
		this.pubSubMechanism = pubSubMechanism;
		this.consumer = new AuditerConsumer("AuditerPublisherConsumer", dataSource);
	}

	@Override
	@Scheduled(fixedRate = 5000)
	public void checkNewMessages() {
		List<String> messages = getMessages();

		if (!messages.isEmpty()) {
			publishMessages(messages);
		}
	}

	/**
	 * Get json messages from auditer through auditerConsumer
	 *
	 * @return audit messages in JSON
	 */
	private List<String> getMessages() {
		List<String> messages = new ArrayList<>();

		try {
			messages = consumer.getMessagesInJson();
		} catch (InternalErrorException e) {
			log.error("Could not get messages to publisher: " + e.getMessage());
		}

		return messages;
	}

	/**
	 * Deserialize messages and pulishes them into pubSubMechanism channel
	 *
	 * @param messages messages to be parsed
	 */
	private void publishMessages(List<String> messages) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		AuditEvent event;

		for (String m : messages) {
			try {
				Class clazz = Class.forName(getNameOfClassAttribute(m));
				event = (AuditEvent) mapper.readValue(m, clazz);
			} catch (JsonParseException | InternalErrorException e) {
				log.error("Failed to parse audit message. ", e);
				continue;
			} catch (JsonMappingException e) {
				log.error("Could not map message for class.", e);
				continue;
			} catch (IOException e) {
				log.error("Failed to read value from audit message.", e);
				continue;
			} catch (ClassNotFoundException e) {
				log.error("Class not found for audit message: " + m, e);
				continue;
			}
			pubSubMechanism.publishAsync(event);
		}
	}

	/**
	 * Get name of class from json string
	 *
	 * @param jsonString audit message in json format
	 * @return name of class included in json string
	 */
	private String getNameOfClassAttribute(String jsonString) throws InternalErrorException {
		try {
			//get everything from in between of next quotes
			String message;
			int index = jsonString.indexOf("\"name\":\"cz");
			if (index != -1) {
				message = jsonString.substring(index + 8);
				message = message.substring(0, message.indexOf("\""));
				return message;
			}
		} catch (Exception e) {
			log.error("Could not get name from json string: \"{}\".", jsonString);
		}

		throw new InternalErrorException("Failed to read attribute 'name' from string: '" + jsonString + "'");
	}
}
