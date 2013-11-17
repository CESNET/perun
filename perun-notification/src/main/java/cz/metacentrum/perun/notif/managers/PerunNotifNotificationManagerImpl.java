package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.*;
import cz.metacentrum.perun.notif.exceptions.PerunNotifRegexUsedException;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Manager for rpc
 * User: tomastunkl
 * Date: 14.10.12
 * Time: 21:29
 * To change this template use File | Settings | File Templates.
 */
@Service("perunNotifNotificationManager")
public class PerunNotifNotificationManagerImpl implements PerunNotifNotificationManager {

    @Autowired
    private PerunNotifTemplateManager perunNotifTemplateManager;

    @Autowired
    private PerunNotifRegexManager perunNotifRegexManager;

    @Autowired
    private PerunNotifObjectManager perunNotifObjectManager;

    @Override
    public PerunNotifObject getPerunNotifObjectById(int id) throws InternalErrorException{

        return perunNotifObjectManager.getPerunNotifObjectById(id);
    }

    @Override
    public PerunNotifObject savePerunNotifObject(PerunNotifObject object) throws InternalErrorException {

        return perunNotifObjectManager.savePerunNotifObject(object);
    }

    @Override
    public PerunNotifObject updatePerunNotifObject(PerunNotifObject object) throws InternalErrorException {

        return perunNotifObjectManager.updatePerunNotifObject(object);
    }

    public void removePerunNotifObjectById(int id) throws InternalErrorException {

        perunNotifObjectManager.removePerunNotifObjectById(id);
    }

    @Override
    public PerunNotifReceiver getPerunNotifReceiverById(int id) throws InternalErrorException {

        return perunNotifTemplateManager.getPerunNotifReceiverById(id);
    }

    @Override
    public PerunNotifReceiver savePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException {

        return perunNotifTemplateManager.savePerunNotifReceiver(receiver);
    }

    @Override
    public PerunNotifReceiver updatePerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException {
        return perunNotifTemplateManager.updatePerunNotifReceiver(receiver);
    }

    @Override
    public void removePerunNotifReceiverById(int id) throws InternalErrorException {

        perunNotifTemplateManager.removePerunNotifReceiverById(id);
    }

    @Override
    public PerunNotifRegex getPerunNotifRegexById(int id) throws InternalErrorException {

        return perunNotifRegexManager.getPerunNotifRegexById(id);
    }

    @Override
    public PerunNotifRegex savePerunNotifRegex(PerunNotifRegex regex) throws InternalErrorException {
        return perunNotifRegexManager.savePerunNotifRegex(regex);
    }

    @Override
    public PerunNotifRegex updatePerunNotifRegex(PerunNotifRegex regex) throws InternalErrorException {
        return perunNotifRegexManager.updatePerunNotifRegex(regex);
    }

    @Override
    public void removePerunNotifRegexById(int id) throws InternalErrorException, PerunNotifRegexUsedException {

        perunNotifRegexManager.removePerunNotifRegexById(id);
    }

    @Override
    public PerunNotifTemplate getPerunNotifTemplateById(int id) throws InternalErrorException {

        return perunNotifTemplateManager.getPerunNotifTemplateByIdFromDb(id);
    }

    @Override
    public PerunNotifTemplate savePerunNotifTemplate(PerunNotifTemplate template) throws InternalErrorException {

        return perunNotifTemplateManager.savePerunNotifTemplate(template);
    }

    @Override
    public PerunNotifTemplate updatePerunNotifTemplate(PerunNotifTemplate template) throws InternalErrorException {

        return perunNotifTemplateManager.updatePerunNotifTemplate(template);
    }

    @Override
    public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(int id) throws InternalErrorException {

        return perunNotifTemplateManager.getPerunNotifTemplateMessageById(id);
    }

    @Override
    public PerunNotifTemplateMessage savePerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException {
        return perunNotifTemplateManager.savePerunNotifTemplateMessage(message);
    }

    @Override
    public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException {
        return perunNotifTemplateManager.updatePerunNotifTemplateMessage(message);
    }

    @Override
    public void removePerunNotifTemplateMessage(int id) throws InternalErrorException {
        perunNotifTemplateManager.removePerunNotifTemplateMessage(id);
    }

    @Override
    public void removePerunNotifTemplateById(int id) throws InternalErrorException {
        perunNotifTemplateManager.removePerunNotifTemplateById(id);
    }

    @Override
    public String testPerunNotifMessageText(String template, Map<Integer, List<PerunBean>> regexIdsPerunBeans) throws IOException, TemplateException {

        return perunNotifTemplateManager.testPerunNotifMessageText(template, regexIdsPerunBeans);
    }

    @Override
    public void removePerunNotifTemplateRegexRelation(int templateId, int regexId) throws InternalErrorException {
        perunNotifRegexManager.removePerunNotifTemplateRegexRelation(templateId, regexId);
    }

    @Override
    public void removePerunNotifRegexObjectRelation(int regexId, int objectId) throws InternalErrorException {
        perunNotifObjectManager.removePerunNotifRegexObjectRelation(regexId, objectId);
    }
}
