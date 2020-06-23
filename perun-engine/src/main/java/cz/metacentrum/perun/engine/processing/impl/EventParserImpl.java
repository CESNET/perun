package cz.metacentrum.perun.engine.processing.impl;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.exceptions.InvalidEventMessageException;
import cz.metacentrum.perun.engine.processing.EventParser;
import cz.metacentrum.perun.taskslib.model.Task;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@org.springframework.stereotype.Service(value = "eventParser")
public class EventParserImpl implements EventParser {
	private static final Logger log = LoggerFactory
			.getLogger(EventParserImpl.class);

	@Autowired
	private Properties propertiesBean;

	@Override
	public Task parseEvent(String event) throws InvalidEventMessageException,
			ServiceNotExistsException,
			PrivilegeException {

		log.info("Going to process event: {}", event);

		/*
		 * Expected string format:
		 * "task|[task_id][is_forced]|[service]|[facility]|[destination_list]|[dependency_list]"
		 *
		 *  String eventParsingPattern =
		 * "^event\\|([0-9]{1,6})\\|\\[([a-zA-Z0-9: ]+)\\]\\[([^\\]]+)\\]\\[(.*)\\]$";
		 */
		String eventParsingPattern = "^task\\|\\[([0-9]+)\\]\\[([^\\]]+)\\]\\|\\[([^\\|]+)\\]\\|\\[([^\\|]+)\\]\\|\\[([^\\|]+)\\]$";
		Pattern pattern = Pattern.compile(eventParsingPattern);
		Matcher matcher = pattern.matcher(event);
		boolean matchFound = matcher.find();

		if (matchFound) {
			log.debug("Message format matched ok...");
			// Data should provide information regarding the target Service (Processing rule).
			String eventTaskId = matcher.group(1);
			String eventIsForced = matcher.group(2);
			String eventService = matcher.group(3);
			String eventFacility = matcher.group(4);
			String eventDestinationList = matcher.group(5);

			// check possible enconding
			if (!eventService.startsWith("Service")) {
				eventService = new String(Base64.decodeBase64(eventService));
			}
			if (!eventService.startsWith("Service")) {
				throw new InvalidEventMessageException("Wrong exec service: parse exception");
			}
			if (!eventFacility.startsWith("Facility")) {
				eventFacility = new String(Base64.decodeBase64(eventFacility));
			}
			if (!eventFacility.startsWith("Facility")) {
				throw new InvalidEventMessageException("Wrong facility: parse exception");
			}
			if (!eventDestinationList.startsWith("Destinations")) {
				eventDestinationList = new String(Base64.decodeBase64(eventDestinationList));
			}
			if (!eventDestinationList.startsWith("Destinations")) {
				throw new InvalidEventMessageException("Wrong destination list: parse exception");
			}

			log.debug("Event data to be parsed: task id {}, forced {}, facility {}, service {}, destination list {}",
					eventTaskId, eventIsForced, eventFacility, eventService, eventDestinationList);

			// Prepare variables
			Facility facility;
			Service service;
			List<Destination> destinationList = new ArrayList<Destination>();

			// resolve facility and deserialize event data
			List<PerunBean> listOfBeans = AuditParser.parseLog(eventFacility);
			try {
				facility = (Facility) listOfBeans.get(0);
			} catch (Exception e) {
				throw new InvalidEventMessageException(
						"Could not resolve facility from event ["
								+ eventFacility + "]", e);
			}

			// resolve exec service and deserialize event data
			listOfBeans = AuditParser.parseLog(eventService);
			try {
				service = (Service) listOfBeans.get(0);
			} catch (Exception e) {
				throw new InvalidEventMessageException("Could not resolve service from event [" + eventService + "]", e);
			}

			// resolve list of destinations
			listOfBeans = AuditParser.parseLog(eventDestinationList);
			log.debug("Found list of destination beans: {}", listOfBeans);
			try {
				for (PerunBean bean : listOfBeans) {
					destinationList.add((Destination) bean);
				}
			} catch (Exception e) {
				throw new InvalidEventMessageException(
						"Could not resolve list of destinations from event.", e);
			}

			Task task = new Task();
			task.setId(Integer.parseInt(eventTaskId));
			task.setFacility(facility);
			task.setService(service);
			task.setDestinations(destinationList);
			task.setDelay(service.getDelay());
			task.setRecurrence(service.getRecurrence());
			task.setPropagationForced(Boolean.parseBoolean(eventIsForced));

			return task;

		} else {
			throw new InvalidEventMessageException(
					"Invalid message format: Message[" + event + "]");
		}
	}

	public Properties getPropertiesBean() {
		return propertiesBean;
	}

	public void setPropertiesBean(Properties propertiesBean) {
		this.propertiesBean = propertiesBean;
	}

}
