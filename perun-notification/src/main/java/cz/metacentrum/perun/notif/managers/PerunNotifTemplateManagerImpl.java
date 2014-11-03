package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.notif.StringTemplateLoader;
import cz.metacentrum.perun.notif.dao.PerunNotifTemplateDao;
import cz.metacentrum.perun.notif.dto.PerunNotifMessageDto;
import cz.metacentrum.perun.notif.dto.PoolMessage;
import cz.metacentrum.perun.notif.entities.*;
import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;
import cz.metacentrum.perun.notif.exceptions.NotExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifReceiverAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifTemplateMessageAlreadyExistsException;
import cz.metacentrum.perun.notif.senders.PerunNotifSender;
import freemarker.cache.MruCacheStorage;
import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("perunNotifTemplateManager")
public class PerunNotifTemplateManagerImpl implements PerunNotifTemplateManager {

	@Autowired
	private PerunNotifTemplateDao perunNotifTemplateDao;

	@Autowired
	private PerunNotifPoolMessageManager perunNotifPoolMessageManager;

	@Autowired
	private PerunNotifEmailManager perunNotifEmailManager;

	@Autowired
	private PerunNotifRegexManager perunNotifRegexManager;

	@Autowired
	private PerunBl perun;

	private List<PerunNotifSender> notifSenders;

	private PerunSession session;

	// cache for quick search of templates sorted by regex id
	private Map<Integer, List<PerunNotifTemplate>> allTemplatesByRegexId = new ConcurrentHashMap<Integer, List<PerunNotifTemplate>>();
	// cache for quick search of templates sorted by id
	private Map<Integer, PerunNotifTemplate> allTemplatesById = new ConcurrentHashMap<Integer, PerunNotifTemplate>();

	private static final Logger logger = LoggerFactory.getLogger(PerunNotifTemplateManager.class);

	private Configuration configuration;

	@PostConstruct
	public void init() throws Exception {

		// Loads all templates to cache
		List<PerunNotifTemplate> templates = perunNotifTemplateDao.getAllPerunNotifTemplates();
		for (PerunNotifTemplate template : templates) {
			// Cache template by id
			allTemplatesById.put(template.getId(), template);

			// Cache template by regexId
			for (PerunNotifRegex regexId : template.getMatchingRegexs()) {

				List<PerunNotifTemplate> templateList = null;
				if (!allTemplatesByRegexId.containsKey(regexId.getId())) {
					templateList = new ArrayList<PerunNotifTemplate>();
					templateList.add(template);
					allTemplatesByRegexId.put(regexId.getId(), templateList);
				} else {
					templateList = allTemplatesByRegexId.get(regexId.getId());
					templateList.add(template);
				}
			}
		}

		//Initialization of freemarker
		StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
		for (PerunNotifTemplate template : templates) {
			for (PerunNotifTemplateMessage pattern : template.getPerunNotifTemplateMessages()) {
				insertPerunNotifTemplateMessageToLoader(stringTemplateLoader, pattern);
			}
		}
		//All templates loaded to freemarker configuration

		configuration = createFreemarkerConfiguration(stringTemplateLoader);

		this.session = perun.getPerunSession(new PerunPrincipal("perunNotifications", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));
	}

	private Configuration createFreemarkerConfiguration(StringTemplateLoader stringTemplateLoader) {

		Configuration newConfiguration = new Configuration();
		newConfiguration.setTagSyntax(Configuration.ANGLE_BRACKET_TAG_SYNTAX);
		newConfiguration.setDefaultEncoding("utf-8");
		newConfiguration.setLocalizedLookup(true);
		newConfiguration.setCacheStorage(new MruCacheStorage(10, 100));
		newConfiguration.setTemplateLoader(stringTemplateLoader);

		return newConfiguration;
	}

	/**
	 * Inserts subject and content of PerunNotifMessage into FreeMaker loader.
	 *
	 * @param templateLoader
	 * @param templateMessage
	 */
	private void insertPerunNotifTemplateMessageToLoader(StringTemplateLoader templateLoader, PerunNotifTemplateMessage templateMessage) {

		String templateName = createTemplateName(templateMessage);
		String subjectTemplateName = createSubjectTemplateName(templateMessage);

		if (templateLoader.findTemplateSource(templateName) == null) {
 			templateLoader.removeTemplate(templateName);
 		}

		templateLoader.putTemplate(templateName, templateMessage.getMessage());
		templateLoader.putTemplate(subjectTemplateName, templateMessage.getSubject());
	}

	/**
	 * The FreeMaker template name is created with id of the notifTemplate and locale.
	 *
	 * @param templateMessage
	 * @return
	 */
	private String createTemplateName(PerunNotifTemplateMessage templateMessage) {
		return templateMessage.getTemplateId() + "_" + templateMessage.getLocale().getLanguage();
	}

	private String createSubjectTemplateName(PerunNotifTemplateMessage templateMessage) {
		return templateMessage.getTemplateId() + "-subject_" + templateMessage.getLocale().getLanguage();
	}

	@Override
	public List<PerunNotifPoolMessage> getPerunNotifPoolMessagesForRegexIds(Set<Integer> regexIds, PerunNotifAuditMessage perunAuditMessage, PerunSession session)
		throws InternalErrorException {

		Map<Integer, List<PerunNotifTemplate>> templates = new HashMap<Integer, List<PerunNotifTemplate>>();

		for (Integer regexId : regexIds) {
			templates.put(regexId, allTemplatesByRegexId.get(regexId));
		}

		return perunNotifPoolMessageManager.createPerunNotifPoolMessagesForTemplates(templates, perunAuditMessage);
	}

	@Override
	public Set<Integer> processPoolMessages(Integer templateId, List<PoolMessage> notifMessages) {

		if (notifMessages == null || notifMessages.isEmpty() || templateId == null) {
			return null;
		}

		List<PerunNotifMessageDto> messageDtoList = new ArrayList<PerunNotifMessageDto>();
		logger.info("Starting to process messages for template with id:" + templateId);

		PerunNotifTemplate template = allTemplatesById.get(templateId);
		switch (template.getNotifyTrigger()) {
			case ALL_REGEX_IDS:

				for (PoolMessage dto : notifMessages) {

					// Test for all regexIds present
					logger.info("Starting to process dto for templateId: " + templateId + " and keyAttributes: " + dto.getKeyAttributes());
					Set<Integer> foundRegexIds = new HashSet<Integer>();
					for (PerunNotifPoolMessage poolMessage : dto.getList()) {
						foundRegexIds.add(poolMessage.getRegexId());
					}

					boolean allRegexes = true;
					for (PerunNotifRegex regex : template.getMatchingRegexs()) {
						if (!foundRegexIds.contains(regex.getId())) {
							logger.info("Not all regexes found for templateId: " + templateId + ", and keyAttributes: " + dto.getKeyAttributes() + " missing:"
								+ regex.getId());
							allRegexes = false;
						}
					}

					if (allRegexes) {
						logger.info("All regexes found for templateId: " + templateId + " and keyAttribute: " + dto.getKeyAttributes()
							+ " starting to create message.");
						try {
							messageDtoList.addAll(createMessageToSend(template, dto));
						} catch (Exception ex) {
							logger.error("Error during creating message to send.", ex);
						}
					}
				}
				break;
			// Not fully supported yet !
			// TODO - create reliable workflow with regexes in the STREAM mode.
			//      - need to work with more than one msg of the same regex
			//      - need to ensure delivery of all the msgs required by the template message
			case STREAM:

				DateTime now = new DateTime();
				DateTime oldestTime = new DateTime(now.getMillis() - template.getOldestMessageTime());
				DateTime youngestTime = new DateTime(now.getMillis() - template.getYoungestMessageTime());

				for (PoolMessage parentDto : notifMessages) {
					List<PerunNotifPoolMessage> poolMessages = parentDto.getList();
					if (poolMessages != null) {
						// Test for oldest message first message in list is oldest,
						// messages
						// are sorted from sql query
						PerunNotifPoolMessage oldestPoolMessage = poolMessages.get(0);
						if (oldestPoolMessage.getCreated().compareTo(oldestTime) < 0) {
							// We have reached longest wait time, we take everything
							// we have and send it
							try {
								logger.debug("Oldest message is older than oldest time for template id " + template.getId() + " message will be sent.");
								messageDtoList.addAll(createMessageToSend(template, parentDto));
							} catch (Exception ex) {
								logger.error("Error during creating of messages to send.", ex);
							}
						} else {
							// We test youngest message so we now nothing new will
							// propably come in close future
							PerunNotifPoolMessage youngestPoolMessage = poolMessages.get(poolMessages.size() - 1);
							if (youngestPoolMessage.getCreated().compareTo(youngestTime) < 0) {
								// Youngest message is older
								try {
									logger.debug("Youngest message is older than youngest time for template id " + template.getId() + " message will be sent.");
									messageDtoList.addAll(createMessageToSend(template, parentDto));
								} catch (Exception ex) {
									logger.error("Error during creating of messages to send.", ex);
								}
							} else {
								Period oldestPeriod = new Period(oldestPoolMessage.getCreated().getMillis() - oldestTime.getMillis());
								Period youngestPeriod = new Period(youngestPoolMessage.getCreated().getMillis() - youngestTime.getMillis());
								Period period = oldestPeriod.getMillis() < youngestPeriod.getMillis() ? oldestPeriod : youngestPeriod;
								String remainingTime = "";
								if (period.getDays() > 0) {
									remainingTime += period.getDays() + " days ";
								}
								if (period.getHours() > 0) {
									remainingTime += period.getHours() + " hours ";
								}
								if (period.getMinutes() > 0) {
									remainingTime += period.getMinutes()+ " minutes ";
								}
								if (period.getSeconds()> 0) {
									remainingTime += period.getSeconds()+ " sec.";
								}
								logger.debug("The time limits for messages are greater that messages creation time for template id " + template.getId() + ", the message will not be sent yet. "
								+ "Provided no messages is created, the notification will be sent in " + remainingTime);

							}
						}
					}
				}
				break;
		}

		Map<PerunNotifTypeOfReceiver, List<PerunNotifMessageDto>> messagesToSend = new HashMap<PerunNotifTypeOfReceiver, List<PerunNotifMessageDto>>();
		Set<Integer> processedIds = new HashSet<Integer>();

		for (PerunNotifMessageDto messageToSend : messageDtoList) {
			List<PerunNotifMessageDto> list = messagesToSend.get(messageToSend.getReceiver().getTypeOfReceiver());
			if (list == null) {
				list = new ArrayList<PerunNotifMessageDto>();
				list.add(messageToSend);
				messagesToSend.put(messageToSend.getReceiver().getTypeOfReceiver(), list);
			} else {
				list.add(messageToSend);
			}
		}

		for (PerunNotifTypeOfReceiver typeOfReceiver : messagesToSend.keySet()) {

			PerunNotifSender handlingSender = null;
			for (PerunNotifSender sender : notifSenders) {

				if (sender.canHandle(typeOfReceiver)) {
					handlingSender = sender;
				}
			}

			if (handlingSender != null) {
				logger.debug("Found handling sender: {}", handlingSender.toString());
				processedIds.addAll(handlingSender.send(messagesToSend.get(typeOfReceiver)));
				logger.debug("Messages send by sender: {}", handlingSender.toString());
			} else {
				logger.error("No handling sender found for: {}", typeOfReceiver);
			}
		}

		return processedIds;
	}

	/**
	 * Creates message which can be send using different ways defined in
	 * typeOfReceiver Every typeOfReceiver creates own Instance of
	 * PerunNotifMessageToSendDto, so the messages can be later send in
	 * batch
	 * <p/>
	 * Allowed syntax of variables in text -
	 * #{cz.metacentrum.perun.core.api.Destination.getId#} - #{for(regexId)
	 * "repeatable message
	 * ${cz.metacentrum.perun.core.api.Destination.getId$}" #}
	 *
	 * @param template
	 * @param dto
	 * @return
	 */
	private List<PerunNotifMessageDto> createMessageToSend(PerunNotifTemplate template, PoolMessage dto) throws IOException, TemplateException, InternalErrorException {

		Map<String, Object> container = new HashMap<String, Object>();

		Map<String, Map<String, PerunBean>> resultMapOfBeans = new HashMap<String, Map<String, PerunBean>>();
		for (PerunNotifPoolMessage message : dto.getList()) {
			List<PerunBean> retrievedObjects = AuditParser.parseLog(message.getNotifMessage());
			Map<String, PerunBean> normalizedBeans = new HashMap<String, PerunBean>();
			for (PerunBean retrievedObject : retrievedObjects) {
				normalizedBeans.put(normalizeName(retrievedObject.getClass().toString()), retrievedObject);
			}
			resultMapOfBeans.put(message.getRegexId().toString(), normalizedBeans);
		}

		container.put("retrievedObjects", resultMapOfBeans);
		container.put("perunSession", session);
		container.put("perun", perun);

		// We get all used pool ids so we can erase them later
		Set<Integer> usedPoolIds = new HashSet<Integer>();
		for (PerunNotifPoolMessage message : dto.getList()) {
			usedPoolIds.add(message.getId());
		}

		// Create of message based on receiver
		List<PerunNotifMessageDto> result = new ArrayList<PerunNotifMessageDto>();
		for (PerunNotifReceiver receiver : template.getReceivers()) {
			PerunNotifMessageDto messageDto = new PerunNotifMessageDto();
			String messageContent = compileTemplate(Integer.toString(template.getId()), receiver.getLocale(), container);
			String subjectContent = compileTemplate(Integer.toString(template.getId()) + "-subject", receiver.getLocale(), container);
			messageDto.setMessageToSend(messageContent);
			messageDto.setPoolMessage(dto);
			messageDto.setUsedPoolIds(usedPoolIds);
			messageDto.setReceiver(receiver);
			messageDto.setTemplate(template);
			messageDto.setSubject(subjectContent);

			result.add(messageDto);
		}

		return result;
	}

	private String compileTemplate(String templateName, Locale locale, Map<String, Object> container) throws IOException, TemplateException {

		class NotificationTemplateExceptionHandler implements TemplateExceptionHandler {

			@Override
			public void handleTemplateException(TemplateException te, Environment env, java.io.Writer out) throws TemplateException {
				if (te instanceof InvalidReferenceException) {
					// skip undefined values
				} else {
					throw te;
				}
			}
		}

		this.configuration.setTemplateExceptionHandler(new NotificationTemplateExceptionHandler());

		StringWriter stringWriter = new StringWriter(4096);

		Template freeMarkerTemplate = this.configuration.getTemplate(templateName + "_" + locale.getLanguage(), locale);

		freeMarkerTemplate.process(container, stringWriter);

		return stringWriter.toString();
	}

	private String normalizeName(String className) {

		String name = className.substring(className.lastIndexOf(".") + 1);

		return name;
	}

	@Override
	public PerunNotifTemplate getPerunNotifTemplateById(int id) throws InternalErrorException {

		return perunNotifTemplateDao.getPerunNotifTemplateById(id);
	}

	@Override
	public PerunNotifTemplate getPerunNotifTemplateByIdFromDb(int id) throws InternalErrorException {

		return perunNotifTemplateDao.getPerunNotifTemplateById(id);
	}

	@Override
	public List<PerunNotifTemplate> getAllPerunNotifTemplates() throws InternalErrorException {

		return perunNotifTemplateDao.getAllPerunNotifTemplates();
	}

	private String resolveName(String command) {

		String result = null;
		// Remove of ${ and $}
		String totalObject = command.substring(2);
		totalObject = totalObject.substring(0, totalObject.length() - 2);

		if (!totalObject.startsWith("for(")) {
			// First part of command, we recognize method or object
			String firstPart = totalObject.substring(0, totalObject.indexOf("."));
			if (firstPart.matches("get.*Manager()")) {

				result = PerunNotifPoolMessageManagerImpl.METHOD_CLASSNAME;
			} else {
				result = firstPart;
			}
		} else {
			return null;
		}

		return result;
	}

	private String resolveProperty(String command) {

		String result = null;
		// Remove of ${ and $}
		String totalObject = command.substring(2);
		totalObject = totalObject.substring(0, totalObject.length() - 2);

		if (!totalObject.startsWith("for(")) {
			// First part of command, we recognize method or object
			String firstPart = totalObject.substring(0, totalObject.indexOf("."));
			if (firstPart.matches("get.*Manager()")) {

				result = firstPart;
			} else {
				result = totalObject.substring(totalObject.lastIndexOf(".") + 1, totalObject.length());
			}
		} else {
			return null;
		}

		return result;
	}

	@Override
	public PerunNotifTemplate updatePerunNotifTemplateData(PerunNotifTemplate template) throws InternalErrorException {

		return perunNotifTemplateDao.updatePerunNotifTemplateData(template);
	}

	public PerunBl getPerun() {
		return perun;
	}

	public void setPerun(PerunBl perul) {
		this.perun = perun;
	}

	@Override
	public PerunNotifReceiver getPerunNotifReceiverById(int id) throws InternalErrorException {
		return perunNotifTemplateDao.getPerunNotifReceiverById(id);
	}

	@Override
	public List<PerunNotifReceiver> getAllPerunNotifReceivers() {
		return perunNotifTemplateDao.getAllPerunNotifReceivers();
	}

	@Override
	public PerunNotifReceiver createPerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException, NotifReceiverAlreadyExistsException {

		// check if there is no other Notif receiver with the same target and locale
		for (PerunNotifReceiver item: getAllPerunNotifReceivers()) {
			if ((item.getTarget().equals(receiver.getTarget())) && (item.getLocale().equals(receiver.getLocale()))) {
				throw new NotifReceiverAlreadyExistsException(receiver);
			}
		}

		PerunNotifReceiver perunNotifReceiver = perunNotifTemplateDao.createPerunNotifReceiver(receiver);

		//Propagating new receiver to template
		PerunNotifTemplate template = allTemplatesById.get(receiver.getTemplateId());
		template.addReceiver(receiver);

		return perunNotifReceiver;
	}

	@Override
	public PerunNotifReceiver updatePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException {
		PerunNotifReceiver oldReceiver = perunNotifTemplateDao.getPerunNotifReceiverById(receiver.getId());
		PerunNotifReceiver newReceiver = perunNotifTemplateDao.updatePerunNotifReceiver(receiver);

		if (!oldReceiver.getTemplateId().equals(newReceiver.getTemplateId())) {
			//WE remove old relation between receiver and template
			PerunNotifTemplate oldTemplate = allTemplatesById.get(oldReceiver.getTemplateId());
			for (Iterator<PerunNotifReceiver> iter = oldTemplate.getReceivers().iterator(); iter.hasNext();) {
				PerunNotifReceiver templateOldReceiver = iter.next();
				if (templateOldReceiver.getId().equals(oldReceiver.getId())) {
					iter.remove();
				}
			}

			//We add new receiver to template
			PerunNotifTemplate newTemplate = allTemplatesById.get(newReceiver.getTemplateId());
			newTemplate.addReceiver(newReceiver);
			return newReceiver;
		} else {
			//We update existing data in template
			PerunNotifTemplate template = allTemplatesById.get(newReceiver.getTemplateId());
			for (PerunNotifReceiver myReceiver : template.getReceivers()) {
				if (myReceiver.getId().equals(newReceiver.getId())) {
					myReceiver.update(newReceiver);
					return newReceiver;
				}
			}

			logger.warn("Trying to update receiver in template failed. Receiver not recognized in template.");
			return newReceiver;
		}
	}

	@Override
	public PerunNotifTemplate createPerunNotifTemplate(PerunNotifTemplate template) throws InternalErrorException {

		PerunNotifTemplate perunNotifTemplate = perunNotifTemplateDao.savePerunNotifTemplateInternals(template);

		if (template.getMatchingRegexs() != null) {
			for (PerunNotifRegex regex : template.getMatchingRegexs()) {
				if (regex.getId() != null) {
					//We update relation between template and regex
					perunNotifRegexManager.saveTemplateRegexRelation(template.getId(), regex.getId());
				}
			}
		}

		allTemplatesById.put(template.getId(), template);
		if (template.getMatchingRegexs() != null) {
			for (PerunNotifRegex regex : template.getMatchingRegexs()) {
				List<PerunNotifTemplate> list = allTemplatesByRegexId.get(regex.getId());
				list.add(template);
			}
		}

		return perunNotifTemplate;
	}

	@Override
	public PerunNotifTemplate updatePerunNotifTemplate(PerunNotifTemplate template) throws InternalErrorException {

		PerunNotifTemplate oldTemplate = getPerunNotifTemplateById(template.getId());
		perunNotifTemplateDao.updatePerunNotifTemplateData(template);

		PerunNotifTemplate updatedTemplate = getPerunNotifTemplateById(template.getId());

		allTemplatesById.put(updatedTemplate.getId(), updatedTemplate);
		for (PerunNotifRegex regex : updatedTemplate.getMatchingRegexs()) {
			List<PerunNotifTemplate> list = allTemplatesByRegexId.get(regex.getId());
			if (list == null) {
				list = new ArrayList<PerunNotifTemplate>();
				allTemplatesByRegexId.put(regex.getId(), list);
			}
			boolean updated = false;
			for (PerunNotifTemplate templateToUpdate : list) {
				if (templateToUpdate.getId() == updatedTemplate.getId()) {
					templateToUpdate.update(updatedTemplate);
					updated = true;
				}
			}
			if (!updated) {
				list.add(updatedTemplate);
			}
		}

		return updatedTemplate;
	}

	@Override
	public void removePerunNotifReceiverById(int id) throws InternalErrorException {

		PerunNotifReceiver receiverToRemove = getPerunNotifReceiverById(id);
		if (receiverToRemove == null) {
			throw new NotExistsException("Receiver does not exists in db.");
		}

		perunNotifTemplateDao.removePerunNotifReceiverById(id);

		PerunNotifTemplate template = allTemplatesById.get(receiverToRemove.getTemplateId());
		for (Iterator<PerunNotifReceiver> iter = template.getReceivers().iterator(); iter.hasNext();) {
			PerunNotifReceiver myReceiver = iter.next();
			if (myReceiver.getId().equals(receiverToRemove.getId())) {
				iter.remove();
				return;
			}
		}

		logger.warn("Removing of receiver from template in cache failed. Receiver not found.");
	}

	@Override
	public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(int id) throws InternalErrorException {

		return perunNotifTemplateDao.getPerunNotifTemplateMessageById(id);
	}

	@Override
	public List<PerunNotifTemplateMessage> getAllPerunNotifTemplateMessages() {
		return perunNotifTemplateDao.getAllPerunNotifTemplateMessages();
	}

	@Override
	public PerunNotifTemplateMessage createPerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException, NotifTemplateMessageAlreadyExistsException {

		// if there is already template message with the same template id and locale -> throw exception
		PerunNotifTemplate template = allTemplatesById.get(message.getTemplateId());
		for (PerunNotifTemplateMessage item: template.getPerunNotifTemplateMessages()) {
			if (item.getLocale().equals(message.getLocale())) {
				throw new NotifTemplateMessageAlreadyExistsException(message);
			}
		}

		PerunNotifTemplateMessage perunNotifTemplateMessage = perunNotifTemplateDao.createPerunNotifTemplateMessage(message);

		template.addPerunNotifTemplateMessage(message);

		StringTemplateLoader stringTemplateLoader = (StringTemplateLoader) configuration.getTemplateLoader();
		insertPerunNotifTemplateMessageToLoader(stringTemplateLoader, message);

		return perunNotifTemplateMessage;
	}

	@Override
	public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException {

		PerunNotifTemplateMessage oldMessage = perunNotifTemplateDao.getPerunNotifTemplateMessageById(message.getId());
		PerunNotifTemplateMessage newMessage = perunNotifTemplateDao.updatePerunNotifTemplateMessage(message);

		if (oldMessage.getTemplateId() != newMessage.getTemplateId()) {
			//Template message has changed template
			PerunNotifTemplate oldTemplate = allTemplatesById.get(oldMessage.getTemplateId());
			oldTemplate.getPerunNotifTemplateMessages().remove(oldMessage);

			PerunNotifTemplate newTemplate = allTemplatesById.get(newMessage.getTemplateId());
			newTemplate.addPerunNotifTemplateMessage(newMessage);

			for (List<PerunNotifTemplate> templateList : allTemplatesByRegexId.values()) {
 				for (PerunNotifTemplate template : templateList) {
 					if (template.getId() == oldTemplate.getId()) {
 						template.getPerunNotifTemplateMessages().remove(oldMessage);
 					}
 					if (template.getId() == newTemplate.getId()) {
 						template.addPerunNotifTemplateMessage(newMessage);
 					}
 				}
 			}

		} else {
			//We only update templateMessage
			PerunNotifTemplate template = allTemplatesById.get(newMessage.getTemplateId());
			for (PerunNotifTemplateMessage myMessage : template.getPerunNotifTemplateMessages()) {

				if (myMessage.getId() == newMessage.getId()) {

					myMessage.update(newMessage);
				}
			}
		}

		StringTemplateLoader stringTemplateLoader = (StringTemplateLoader) configuration.getTemplateLoader();
		insertPerunNotifTemplateMessageToLoader(stringTemplateLoader, newMessage);

		configuration.clearTemplateCache();

		return newMessage;
	}

	@Override
	public void removePerunNotifTemplateMessage(int id) throws InternalErrorException {

		PerunNotifTemplateMessage templateMessage = getPerunNotifTemplateMessageById(id);
		if (templateMessage == null) {
			throw new NotExistsException("Template message with id: " + id + " not exists.");
		}
		perunNotifTemplateDao.removePerunNotifTemplateMessage(id);

		PerunNotifTemplate template = allTemplatesById.get(templateMessage.getTemplateId());
		template.getPerunNotifTemplateMessages().remove(templateMessage);

		StringTemplateLoader stringTemplateLoader = (StringTemplateLoader) configuration.getTemplateLoader();
		String templateName = createTemplateName(templateMessage);

		stringTemplateLoader.removeTemplate(templateName);
		configuration.clearTemplateCache();
	}

	@Override
	public void removePerunNotifTemplateById(int id) {

		perunNotifTemplateDao.removePerunNotifTemplateById(id);

		PerunNotifTemplate template = allTemplatesById.get(id);
		allTemplatesById.remove(id);
		for (PerunNotifRegex regex : template.getMatchingRegexs()) {
			List<PerunNotifTemplate> templatestInRegex = allTemplatesByRegexId.get(regex.getId());
			for (Iterator<PerunNotifTemplate> iter = templatestInRegex.iterator(); iter.hasNext();) {
				PerunNotifTemplate myTemplate = iter.next();
				if (myTemplate.getId() == id) {
					iter.remove();
				}
			}
		}
	}

	@Override
	public void assignTemplateToRegex(int regexId, PerunNotifTemplate template) {
		List<PerunNotifTemplate> templates = allTemplatesByRegexId.get(regexId);
		if (templates == null) {
			templates = new ArrayList<>();
			templates.add(template);
			allTemplatesByRegexId.put(regexId, templates);
		} else {
			templates.add(template);
		}
	}

	@Override
	public void removeTemplateFromRegex(int regexId, int templateId) throws InternalErrorException {
		List<PerunNotifTemplate> templates = allTemplatesByRegexId.get(regexId);
		if (templates == null) {
			throw new InternalErrorException("The regex id " + regexId + " has no templates in the cache, therefore template id " + templateId + " cannot be removed.");
		}

		for (Iterator<PerunNotifTemplate> iter = templates.iterator(); iter.hasNext();) {
			PerunNotifTemplate myTemplate = iter.next();
			if (myTemplate.getId() == templateId) {
				iter.remove();
				if (templates.isEmpty()) {
					allTemplatesByRegexId.remove(regexId);
				}
				return;
			}
		}
		throw new InternalErrorException("The regex id " + regexId + " doesn't relate to template id " + templateId + " in the cache, removing failed.");
	}

	@Override
	public String testPerunNotifMessageText(String template, Map<Integer, List<PerunBean>> regexIdsBeans) throws IOException, TemplateException {

		//Initialization of freemarker
		StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
		Configuration myConfiguration = createFreemarkerConfiguration(stringTemplateLoader);

		stringTemplateLoader.putTemplate("test", template);

		Template freeMarkerTemplate = myConfiguration.getTemplate(template, new Locale("en"));

		StringWriter stringWriter = new StringWriter(4096);
		Map<String, Object> container = new HashMap<String, Object>();

		Map<String, Map<String, PerunBean>> resultMapOfBeans = new HashMap<String, Map<String, PerunBean>>();
		for (Integer regexId : regexIdsBeans.keySet()) {
			Map<String, PerunBean> normalizedBeans = new HashMap<String, PerunBean>();
			List<PerunBean> perunBeans = regexIdsBeans.get(regexId);
			for (PerunBean retrievedObject : perunBeans) {
				normalizedBeans.put(normalizeName(retrievedObject.getClass().toString()), retrievedObject);
			}

			resultMapOfBeans.put(regexId.toString(), normalizedBeans);
		}

		container.put("retrievedObjects", resultMapOfBeans);
		container.put("perunSession", session);
		container.put("perun", perun);

		freeMarkerTemplate.process(container, stringWriter);

		return stringWriter.toString();
	}

	public List<PerunNotifSender> getNotifSenders() {
		return notifSenders;
	}

	public void setNotifSenders(List<PerunNotifSender> notifSenders) {
		this.notifSenders = notifSenders;
	}
}
