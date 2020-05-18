package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.notif.StringTemplateLoader;
import cz.metacentrum.perun.notif.dao.PerunNotifRegexDao;
import cz.metacentrum.perun.notif.dao.PerunNotifTemplateDao;
import cz.metacentrum.perun.notif.dto.PerunNotifMessageDto;
import cz.metacentrum.perun.notif.dto.PoolMessage;
import cz.metacentrum.perun.notif.entities.*;
import cz.metacentrum.perun.notif.enums.PerunNotifTypeOfReceiver;
import cz.metacentrum.perun.notif.exceptions.NotExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifReceiverAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifTemplateMessageAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.TemplateMessageSyntaxErrorException;
import cz.metacentrum.perun.notif.senders.PerunNotifSender;
import cz.metacentrum.perun.notif.utils.NotifUtils;
import freemarker.cache.MruCacheStorage;
import freemarker.core.Environment;
import freemarker.core.InvalidReferenceException;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;

@Service("perunNotifTemplateManager")
public class PerunNotifTemplateManagerImpl implements PerunNotifTemplateManager {

	@Autowired
	private PerunNotifTemplateDao perunNotifTemplateDao;

	@Autowired
	private PerunNotifRegexDao perunNotifRegexDao;

	@Autowired
	private PerunNotifPoolMessageManager perunNotifPoolMessageManager;

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

	private Locale DEFAULT_LOCALE = Locale.ENGLISH;

	private String EVALUATION_TEMPLATE = "evaluation";

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
		// create evaluation freemarker template
		stringTemplateLoader.putTemplate(EVALUATION_TEMPLATE, "");

		//All templates loaded to freemarker configuration

		configuration = createFreemarkerConfiguration(stringTemplateLoader);



		session = NotifUtils.getPerunSession(perun);
	}

	private Configuration createFreemarkerConfiguration(StringTemplateLoader stringTemplateLoader) {

		Configuration newConfiguration = new Configuration(DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		newConfiguration.setTagSyntax(Configuration.ANGLE_BRACKET_TAG_SYNTAX);
		newConfiguration.setDefaultEncoding("utf-8");
		newConfiguration.setLocalizedLookup(true);
		newConfiguration.setCacheStorage(new MruCacheStorage(10, 100));
		newConfiguration.setTemplateLoader(stringTemplateLoader);

		return newConfiguration;
	}

	/**
	 * Inserts subject and content of PerunNotifMessage into FreeMarker loader.
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
	 * The FreeMarker template name is created with id of the notifTemplate and locale.
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

				Instant oldestTime = Instant.now().minusMillis(template.getOldestMessageTime());
				Instant youngestTime = Instant.now().minusMillis(template.getYoungestMessageTime());

				for (PoolMessage parentDto : notifMessages) {
					List<PerunNotifPoolMessage> poolMessages = parentDto.getList();
					if (poolMessages != null) {
						// Test for oldest message first message in list is oldest,
						// messages are sorted from sql query
						PerunNotifPoolMessage oldestPoolMessage = poolMessages.get(0);
						if (oldestPoolMessage.getCreated().isBefore(oldestTime)) {
							// We have reached longest wait time, we take everything we have and send it
							try {
								logger.debug("Oldest message is older than oldest time for template id " + template.getId() + " message will be sent.");
								messageDtoList.addAll(createMessageToSend(template, parentDto));
							} catch (Exception ex) {
								logger.error("Error during creating of messages to send.", ex);
							}
						} else {
							// We test youngest message so we now nothing new will probably come in close future
							PerunNotifPoolMessage youngestPoolMessage = poolMessages.get(poolMessages.size() - 1);
							if (youngestPoolMessage.getCreated().isBefore(youngestTime)) {
								// Youngest message is older
								try {
									logger.debug("Youngest message is older than youngest time for template id " + template.getId() + " message will be sent.");
									messageDtoList.addAll(createMessageToSend(template, parentDto));
								} catch (Exception ex) {
									logger.error("Error during creating of messages to send.", ex);
								}
							} else {
								Duration oldestPeriod = Duration.between(oldestPoolMessage.getCreated(), oldestTime);
								Duration youngestPeriod = Duration.between(youngestPoolMessage.getCreated(), youngestTime);
								Duration period = oldestPeriod.getSeconds() < youngestPeriod.getSeconds() ? oldestPeriod : youngestPeriod;
								String remainingTime = DurationFormatUtils.formatDurationWords(period.toMillis(), true, true);
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

		// resolve sender
		String sender = resolveSender(template.getSender(), container);

		// Create of message based on receiver
		List<PerunNotifMessageDto> result = new ArrayList<PerunNotifMessageDto>();
		for (PerunNotifReceiver receiver : template.getReceivers()) {
			PerunNotifMessageDto messageDto = new PerunNotifMessageDto();
			Locale locale = interpretLocale(receiver.getLocale(), receiver.getTarget(), dto.getKeyAttributes());
			String messageContent = compileTemplate(Integer.toString(template.getId()), locale, container);
			String subjectContent = compileTemplate(Integer.toString(template.getId()) + "-subject", locale, container);
			messageDto.setMessageToSend(messageContent);
			messageDto.setPoolMessage(dto);
			messageDto.setUsedPoolIds(usedPoolIds);
			messageDto.setReceiver(receiver);
			messageDto.setTemplate(template);
			messageDto.setSubject(subjectContent);
			messageDto.setSender(sender);

			result.add(messageDto);
		}

		return result;
	}

	private String compileTemplate(final String templateName, Locale locale, Map<String, Object> container) throws IOException, TemplateException {

		class NotificationTemplateExceptionHandler implements TemplateExceptionHandler {

			@Override
			public void handleTemplateException(TemplateException te, Environment env, java.io.Writer out) throws TemplateException {
				if (te instanceof InvalidReferenceException) {
					// skip undefined values
					logger.info("Undefined value found in the TemplateMessage " + templateName + ".", te);
				} else {
					throw te;
				}
			}
		}

		this.configuration.setTemplateExceptionHandler(new NotificationTemplateExceptionHandler());

		StringWriter stringWriter = new StringWriter(4096);

		Template freeMarkerTemplate = null;
		try {
			freeMarkerTemplate = this.configuration.getTemplate(templateName + "_" + locale.getLanguage(), locale);
		} catch (FileNotFoundException ex) {
			if (!(locale.equals(DEFAULT_LOCALE))) {
				// if we do not know the language, try to send it at least in default locale
				freeMarkerTemplate = this.configuration.getTemplate(templateName + "_" + DEFAULT_LOCALE.getLanguage(), DEFAULT_LOCALE);
				logger.info("There is no message with template " + templateName + " in locale " + locale.getLanguage() +
					", therefore the message will be sent in " + DEFAULT_LOCALE.getLanguage() + " locale.");
			} else {
				throw ex;
			}
		}

		freeMarkerTemplate.process(container, stringWriter);

		return stringWriter.toString();
	}

	private void validateTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException, TemplateMessageSyntaxErrorException {
		String templateName = Integer.toString(message.getTemplateId());
		Locale locale = message.getLocale();

		try {
			Template freeMarkerTemplate = this.configuration.getTemplate(templateName + "_" + locale.getLanguage(), locale);
		} catch (ParseException ex) {
			throw new TemplateMessageSyntaxErrorException(message, ex);
		} catch (IOException ex) {
			// template not found
			throw new InternalErrorException("FreeMarker Template internal error.", ex);
		}
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
		validateReceiver(receiver);

		if (!(allTemplatesById.containsKey(receiver.getTemplateId()))) {
			throw new NotExistsException("CreatePerunNotifReceiver: template id: " + receiver.getTemplateId()
				+ " does not exist.");
		}

		PerunNotifReceiver perunNotifReceiver = perunNotifTemplateDao.createPerunNotifReceiver(receiver);

		//Propagating new receiver to template
		PerunNotifTemplate template = allTemplatesById.get(receiver.getTemplateId());
		template.addReceiver(receiver);

		return perunNotifReceiver;
	}

	@Override
	public PerunNotifReceiver updatePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException, NotifReceiverAlreadyExistsException {
		validateReceiver(receiver);

		if (!(allTemplatesById.containsKey(receiver.getTemplateId()))) {
			throw new NotExistsException("CreatePerunNotifReceiver: template id: " + receiver.getTemplateId()
				+ " does not exist.");
		}

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

		// save template internals
		PerunNotifTemplate newTemplate = perunNotifTemplateDao.savePerunNotifTemplateInternals(template);
		template.setId(newTemplate.getId());

		// create rexeges if not exist
		if (template.getMatchingRegexs() != null) {
			for (PerunNotifRegex regex: template.getMatchingRegexs()) {
				if ((regex.getId() == null) || (perunNotifRegexDao.getPerunNotifRegexById(regex.getId()) == null)) {
					PerunNotifRegex newRegex = perunNotifRegexDao.saveInternals(regex);
					regex.setId(newRegex.getId());
				}
				if (!(perunNotifRegexDao.isRegexRelation(template.getId(), regex.getId()))) {
					perunNotifRegexDao.saveTemplateRegexRelation(template.getId(), regex.getId());
				}
			}
		}

		// create receivers if not exist
		if (template.getReceivers() != null) {
			for (PerunNotifReceiver receiver : template.getReceivers()) {
				receiver.setTemplateId(template.getId());
				if ((receiver.getId() == null) || (perunNotifTemplateDao.getPerunNotifReceiverById(receiver.getId()) == null)) {
					PerunNotifReceiver newReceiver = perunNotifTemplateDao.createPerunNotifReceiver(receiver);
					receiver.setId(newReceiver.getId());
				}
			}
		}

		// create template messages if not exist
		if (template.getPerunNotifTemplateMessages() != null) {
			for (PerunNotifTemplateMessage message : template.getPerunNotifTemplateMessages()) {
				message.setTemplateId(template.getId());
				PerunNotifTemplateMessage newMessage;
				try {
					newMessage = createPerunNotifTemplateMessage(message);
					message.setId(newMessage.getId());
				} catch (NotifTemplateMessageAlreadyExistsException ex) {
					// template message already exists
				} catch (TemplateMessageSyntaxErrorException ex) {
					throw new InternalErrorException(ex);
				}
			}
		}

		// update cache allTemplatesById
		allTemplatesById.put(template.getId(), template);

		// update cache allTemplatesByRegexId
		if (template.getMatchingRegexs() != null) {
			for (PerunNotifRegex regex : template.getMatchingRegexs()) {
				List<PerunNotifTemplate> list = allTemplatesByRegexId.get(regex.getId());
				if (list == null) {
					list = new ArrayList<>();
					list.add(template);
					allTemplatesByRegexId.put(regex.getId(), list);
				} else {
					list.add(template);
				}

			}
		}

		return template;
	}

	@Override
	public PerunNotifTemplate updatePerunNotifTemplate(PerunNotifTemplate template) throws InternalErrorException {

		PerunNotifTemplate oldTemplate = getPerunNotifTemplateById(template.getId());
		perunNotifTemplateDao.updatePerunNotifTemplateData(template);

		// create rexeges if not exist
		if (template.getMatchingRegexs() != null) {
			for (PerunNotifRegex regex: template.getMatchingRegexs()) {
				if ((regex.getId() == null) || (perunNotifRegexDao.getPerunNotifRegexById(regex.getId()) == null)) {
					perunNotifRegexDao.saveInternals(regex);
				}
				if (!(perunNotifRegexDao.isRegexRelation(template.getId(), regex.getId()))) {
					perunNotifRegexDao.saveTemplateRegexRelation(template.getId(), regex.getId());
				}
			}
		}

		// create receivers if not exist
		if (template.getReceivers() != null) {
			for (PerunNotifReceiver receiver : template.getReceivers()) {
				receiver.setTemplateId(template.getId());
				if ((receiver.getId() == null) || (perunNotifTemplateDao.getPerunNotifReceiverById(receiver.getId()) == null)) {
					perunNotifTemplateDao.createPerunNotifReceiver(receiver);
				}
			}
		}

		// create template messages if not exist
		if (template.getPerunNotifTemplateMessages() != null) {
			for (PerunNotifTemplateMessage message : template.getPerunNotifTemplateMessages()) {
				message.setTemplateId(template.getId());
				PerunNotifTemplateMessage newMessage;
				try {
					newMessage = createPerunNotifTemplateMessage(message);
					message.setId(newMessage.getId());
				} catch (NotifTemplateMessageAlreadyExistsException ex) {
					// template message already exists
				} catch (TemplateMessageSyntaxErrorException ex) {
					throw new InternalErrorException(ex);
				}
			}
		}

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

		for (List<PerunNotifTemplate> listOftemplate: allTemplatesByRegexId.values()) {
			for (PerunNotifTemplate t : listOftemplate) {
				if (t.equals(template)) {
					t.getReceivers().remove(receiverToRemove);
				}
			}
		}

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
	public PerunNotifTemplateMessage createPerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException, NotifTemplateMessageAlreadyExistsException, TemplateMessageSyntaxErrorException {

		// if there is already template message with the same template id and locale -> throw exception
		PerunNotifTemplate template = allTemplatesById.get(message.getTemplateId());
		if (template != null) {
			for (PerunNotifTemplateMessage item: template.getPerunNotifTemplateMessages()) {
				if (item.getLocale().equals(message.getLocale())) {
					throw new NotifTemplateMessageAlreadyExistsException(message);
				}
			}
		}

		StringTemplateLoader stringTemplateLoader = (StringTemplateLoader) configuration.getTemplateLoader();
		insertPerunNotifTemplateMessageToLoader(stringTemplateLoader, message);
		validateTemplateMessage(message);

		PerunNotifTemplateMessage perunNotifTemplateMessage = perunNotifTemplateDao.createPerunNotifTemplateMessage(message);

		if (template != null) {
			template.addPerunNotifTemplateMessage(perunNotifTemplateMessage);
		}

		return perunNotifTemplateMessage;
	}

	@Override
	public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException, TemplateMessageSyntaxErrorException {

		StringTemplateLoader stringTemplateLoader = (StringTemplateLoader) configuration.getTemplateLoader();
		insertPerunNotifTemplateMessageToLoader(stringTemplateLoader, message);
		configuration.clearTemplateCache();
		validateTemplateMessage(message);

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
				break;
			}
		}
		//throw new InternalErrorException("The regex id " + regexId + " doesn't relate to template id " + templateId + " in the cache, removing failed.");

		for (Iterator<PerunNotifRegex> iter = allTemplatesById.get(templateId).getMatchingRegexs().iterator(); iter.hasNext();) {
			PerunNotifRegex regex = iter.next();
			if (regex.getId() == regexId) {
				iter.remove();
				return;
			}
		}
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

	private void validateReceiver(PerunNotifReceiver receiver) throws NotifReceiverAlreadyExistsException {
		// check if there is no other Notif receiver with the same target, templateID and locale
		for (PerunNotifReceiver item: getAllPerunNotifReceivers()) {
			if (item.getId().equals(receiver.getId())) {
				continue;
			}

			if ((item.getTarget().equals(receiver.getTarget())) &&
				(item.getLocale().equals(receiver.getLocale())) &&
				(item.getTemplateId().equals(receiver.getTemplateId()))) {
				throw new NotifReceiverAlreadyExistsException(receiver);
			}
		}
	}

	private Locale interpretLocale(String stringLocale, String target, Map<String, String> keyAttributes) {
		Locale loc = null;
		String myReceiverId = keyAttributes.get(target);
		if (myReceiverId == null || myReceiverId.isEmpty()) {
			// can be set one static account
			loc = new Locale(stringLocale);
		} else {
			// dynamic user account - check if locale is dynamic too
			Integer id = null;
			try {
				id = Integer.valueOf(myReceiverId);
			} catch (NumberFormatException ex) {
				// wrong user id format -> make classic locale
				logger.error("Cannot resolve user id in receiver target: {}, error: {}", id, ex.getMessage());
				loc = new Locale(stringLocale);
				return loc;
			}
			switch (stringLocale) {
				// you can add more locale interpretations in the future
				case "$user.preferredLanguage":
					try {
						String userLocale = (String) perun.getAttributesManagerBl().getAttribute(session, perun.getUsersManagerBl()
								.getUserById(session, id), "urn:perun:user:attribute-def:def:preferredLanguage").getValue();
						if (userLocale == null) {
							// user's preferred language is not defined -> use default
							logger.info("User's preferred language is not defined, therefore the message will be sent in default language: + " + DEFAULT_LOCALE);
							loc = DEFAULT_LOCALE;
						} else {
							loc = new Locale(userLocale);
						}
					} catch (UserNotExistsException ex) {
						logger.error("Cannot found user with id: {}, ex: {}", id, ex.getMessage());
					} catch (AttributeNotExistsException ex) {
						logger.warn("Cannot find language for user with id: {}, ex: {}", id, ex.getMessage());
					} catch (PerunException ex) {
						logger.error("Error during user language recognition, ex: {}", ex.getMessage());
					}
					break;
				default:
					loc = new Locale(stringLocale);
			}
		}
		return loc;
	}

	private String resolveSender(String input, Map<String,Object> container) throws IOException {
		Matcher emailMatcher = Utils.emailPattern.matcher(input);
		String method = null;
		String email = null;
		if (input.contains(";")) {
			String[] parts = input.split(";", 2);
			method = parts[0];
			email = parts[1];
		} else if (!emailMatcher.find()) {
			method = input;
		} else {
			email = input;
		}

		if (method != null) {
			StringTemplateLoader stringTemplateLoader = (StringTemplateLoader) configuration.getTemplateLoader();
			stringTemplateLoader.putTemplate(EVALUATION_TEMPLATE, "${" + method + "}");
			configuration.clearTemplateCache();
			Template freeMarkerTemplate = this.configuration.getTemplate(EVALUATION_TEMPLATE);
			StringWriter stringWriter = new StringWriter(4096);
			try {
				// because for template messages the nulls are ignored, now we want to fail when null
				this.configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
				freeMarkerTemplate.process(container, stringWriter);
			} catch (TemplateException ex) {
				stringWriter = null;
				logger.info("Resolving sender for method " + method + " failed because of exception: ", ex);
			}
			if (stringWriter != null) {
				if (stringWriter.toString().trim().isEmpty()) {
					stringWriter = null;
				}
			}
			if (stringWriter == null) {
				return email;
			} else {
				return stringWriter.toString();
			}
		} else {
			return email;
		}
	}
}
