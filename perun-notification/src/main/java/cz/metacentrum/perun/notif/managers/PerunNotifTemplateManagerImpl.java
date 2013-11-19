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
import cz.metacentrum.perun.notif.senders.PerunNotifSender;
import freemarker.cache.MruCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private Map<Integer, List<PerunNotifTemplate>> allTemplatesByRegexId = new ConcurrentHashMap<Integer, List<PerunNotifTemplate>>();
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

        this.session = perun.getPerunSession(new PerunPrincipal("perunNotifications", ExtSourcesManager.EXTSOURCE_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL));
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

    private void insertPerunNotifTemplateMessageToLoader(StringTemplateLoader templateLoader, PerunNotifTemplateMessage templateMessage) {

        String templateName = createTemplateName(templateMessage);
        String subjectTemplateName = createSubjectTemplateName(templateMessage);

        templateLoader.putTemplate(templateName, templateMessage.getMessage());
        templateLoader.putTemplate(subjectTemplateName, templateMessage.getSubject());
    }

    private String createTemplateName(PerunNotifTemplateMessage templateMessage) {
        return templateMessage.getTemplateId() + "_" + templateMessage.getLocale().getKey();
    }

    private String createSubjectTemplateName(PerunNotifTemplateMessage templateMessage) {
        return templateMessage.getTemplateId() + "-subject_" + templateMessage.getLocale().getKey();
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
                                    messageDtoList.addAll(createMessageToSend(template, parentDto));
                                } catch (Exception ex) {
                                    logger.error("Error during creating of messages to send.", ex);
                                }
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
     * PerunNotifMessageToSendDto, so the messages can be later send in batch
     * <p/>
     * Allowed syntax of variables in text -
     * #{cz.metacentrum.perun.core.api.Destination.getId#} - #{for(regexId)
     * "repeatable message ${cz.metacentrum.perun.core.api.Destination.getId$}"
     * #}
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

        String messageContent = compileTemplate(Integer.valueOf(template.getId()).toString(), dto.getLocale(), container);
        String subjectContent = compileTemplate(Integer.valueOf(template.getId()).toString() + "-subject", dto.getLocale(), container);

        // We get all used pool ids so we can erase them later
        Set<Integer> usedPoolIds = new HashSet<Integer>();
        for (PerunNotifPoolMessage message : dto.getList()) {
            usedPoolIds.add(message.getId());
        }

        // Create of message based on receiver
        List<PerunNotifMessageDto> result = new ArrayList<PerunNotifMessageDto>();
        for (PerunNotifReceiver receiver : template.getReceivers()) {
            PerunNotifMessageDto messageDto = new PerunNotifMessageDto();
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

        StringWriter stringWriter = new StringWriter(4096);

        Template freeMarkerTemplate = this.configuration.getTemplate(templateName, locale);

        freeMarkerTemplate.process(container, stringWriter);

        return stringWriter.toString();
    }

    private String normalizeName(String className) {

        String name = className.substring(className.lastIndexOf(".") + 1);

        return name;
    }

    @Override
    public PerunNotifTemplate getPerunNotifTemplateById(int id) {

        return allTemplatesById.get(id);
    }

    @Override
    public PerunNotifTemplate getPerunNotifTemplateByIdFromDb(int id) throws InternalErrorException {

        return perunNotifTemplateDao.getPerunNotifTemplateById(id);
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
    public PerunNotifReceiver savePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException {

        PerunNotifReceiver perunNotifReceiver = perunNotifTemplateDao.savePerunNotifReceiver(receiver);

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
            for (Iterator<PerunNotifReceiver> iter = oldTemplate.getReceivers().iterator(); iter.hasNext(); ) {
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
    public PerunNotifTemplate savePerunNotifTemplate(PerunNotifTemplate template) throws InternalErrorException {

        PerunNotifTemplate perunNotifTemplate = perunNotifTemplateDao.savePerunNotifTemplateInternals(template);

        if (template.getMatchingRegexs() != null) {
            for (PerunNotifRegex regex : template.getMatchingRegexs()) {
                if (regex.getId() != null) {
                    //We update relation between template and regex
                    perunNotifRegexManager.saveRegexRelation(template.getId(), regex.getId());
                }
            }
        }

        if (template.getReceivers() != null) {
            for (PerunNotifReceiver receiver : template.getReceivers()) {
                savePerunNotifReceiver(receiver);
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

        Set<PerunNotifRegex> oldRegexes = new HashSet<PerunNotifRegex>(oldTemplate.getMatchingRegexs());
        for (PerunNotifRegex regex : template.getMatchingRegexs()) {
            if (!oldRegexes.remove(regex)) {
                //Regex was not in old regexes
                //We update relation between template and regex
                perunNotifRegexManager.saveRegexRelation(template.getId(), regex.getId());
            }
        }

        for (PerunNotifRegex regexToRemove : oldRegexes) {
            perunNotifRegexManager.removePerunNotifTemplateRegexRelation(template.getId(), regexToRemove.getId());
            List<PerunNotifTemplate> listOfTemplates = allTemplatesByRegexId.get(regexToRemove.getId());
            for (Iterator<PerunNotifTemplate> iter = listOfTemplates.iterator(); iter.hasNext(); ) {
                PerunNotifTemplate myTemplate = iter.next();
                if (myTemplate.getId() == template.getId()) {
                    iter.remove();
                }
            }
        }

        Set<PerunNotifReceiver> oldReceivers = new HashSet<PerunNotifReceiver>(oldTemplate.getReceivers());
        for (PerunNotifReceiver receiver : template.getReceivers()) {
            oldReceivers.remove(receiver);
            //Receiver is not in db
            if (receiver.getId() == null) {
                savePerunNotifReceiver(receiver);
            } else {
                updatePerunNotifReceiver(receiver);
            }
        }

        for (PerunNotifReceiver receiversToRemove : oldReceivers) {
            removePerunNotifReceiverById(receiversToRemove.getId());
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
        for (Iterator<PerunNotifReceiver> iter = template.getReceivers().iterator(); iter.hasNext(); ) {
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
    public PerunNotifTemplateMessage savePerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException {

        PerunNotifTemplateMessage perunNotifTemplateMessage = perunNotifTemplateDao.savePerunNotifTemplateMessage(message);

        PerunNotifTemplate template = allTemplatesById.get(message.getTemplateId());
        template.addPerunNotifTemplateMessage(message);

        StringTemplateLoader stringTemplateLoader = (StringTemplateLoader) configuration.getTemplateLoader();
        insertPerunNotifTemplateMessageToLoader(stringTemplateLoader, message);
        
        return perunNotifTemplateMessage;
    }

    @Override
    public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException {

        PerunNotifTemplateMessage oldMessage = perunNotifTemplateDao.getPerunNotifTemplateMessageById(message.getId());
        PerunNotifTemplateMessage newMessage = perunNotifTemplateDao.updatePerunNotifTemplateMessage(message);

        if (oldMessage.getId() != newMessage.getId()) {
            //Template message has changed template
            PerunNotifTemplate oldTemplate = allTemplatesById.get(oldMessage.getTemplateId());
            oldTemplate.getPerunNotifTemplateMessages().remove(oldMessage);

            PerunNotifTemplate newTemplate = allTemplatesById.get(newMessage.getTemplateId());
            newTemplate.addPerunNotifTemplateMessage(newMessage);
        } else {
            //We only update templateMessage
            PerunNotifTemplate template = allTemplatesById.get(newMessage.getTemplateId());
            for (PerunNotifTemplateMessage myMessage : template.getPerunNotifTemplateMessages()) {

                if (myMessage.getId() == newMessage.getId()) {

                    myMessage.update(newMessage);
                    return newMessage;
                }
            }
        }

        StringTemplateLoader stringTemplateLoader = (StringTemplateLoader) configuration.getTemplateLoader();
        insertPerunNotifTemplateMessageToLoader(stringTemplateLoader, message);

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
            for (Iterator<PerunNotifTemplate> iter = templatestInRegex.iterator(); iter.hasNext(); ) {
                PerunNotifTemplate myTemplate = iter.next();
                if (myTemplate.getId() == id) {
                    iter.remove();
                }
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
}
