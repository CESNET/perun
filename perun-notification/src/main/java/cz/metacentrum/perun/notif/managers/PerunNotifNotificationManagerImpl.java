package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.entities.*;
import cz.metacentrum.perun.notif.exceptions.NotifReceiverAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifRegexAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifTemplateMessageAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.PerunNotifRegexUsedException;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Manager for rpc User: tomastunkl Date: 14.10.12 Time: 21:29 To change this
 * template use File | Settings | File Templates.
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
	public PerunNotifObject getPerunNotifObjectById(int id) throws InternalErrorException {

		return perunNotifObjectManager.getPerunNotifObjectById(id);
	}

	@Override
	public List<PerunNotifObject> getAllPerunNotifObjects() {

		return perunNotifObjectManager.getAllPerunNotifObjects();
	}

	@Override
	public PerunNotifObject createPerunNotifObject(PerunNotifObject object) throws InternalErrorException {

		return perunNotifObjectManager.createPerunNotifObject(object);
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
	public List<PerunNotifReceiver> getAllPerunNotifReceivers() {
		return perunNotifTemplateManager.getAllPerunNotifReceivers();
	}

	@Override
	public PerunNotifReceiver createPerunNotifReceiver(PerunNotifReceiver receiver) throws InternalErrorException, NotifReceiverAlreadyExistsException {

		return perunNotifTemplateManager.createPerunNotifReceiver(receiver);
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
	public List<PerunNotifRegex> getAllPerunNotifRegexes() {
		return perunNotifRegexManager.getAllPerunNotifRegexes();
	}

	@Override
	public PerunNotifRegex createPerunNotifRegex(PerunNotifRegex regex) throws InternalErrorException, NotifRegexAlreadyExistsException {
		return perunNotifRegexManager.createPerunNotifRegex(regex);
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
	public List<PerunNotifTemplateMessage> getAllPerunNotifTemplateMessages() {
		return perunNotifTemplateManager.getAllPerunNotifTemplateMessages();
	}

	@Override
	public PerunNotifTemplate createPerunNotifTemplate(PerunNotifTemplate template) throws InternalErrorException {

		return perunNotifTemplateManager.createPerunNotifTemplate(template);
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
	public List<PerunNotifTemplate> getAllPerunNotifTemplates() throws InternalErrorException {
		return perunNotifTemplateManager.getAllPerunNotifTemplates();
	}

	@Override
	public PerunNotifTemplateMessage createPerunNotifTemplateMessage(PerunNotifTemplateMessage message) throws InternalErrorException, NotifTemplateMessageAlreadyExistsException {
		return perunNotifTemplateManager.createPerunNotifTemplateMessage(message);
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
	public void saveTemplateRegexRelation(int templateId, Integer regexId) throws InternalErrorException {
		perunNotifRegexManager.saveTemplateRegexRelation(templateId, regexId);
	}

	@Override
	public List<PerunNotifRegex> getRelatedRegexesForTemplate(int templateId) throws InternalErrorException {
		return perunNotifRegexManager.getRelatedRegexesForTemplate(templateId);
	}

	@Override
	public void removePerunNotifTemplateRegexRelation(int templateId, int regexId) throws InternalErrorException {
		perunNotifRegexManager.removePerunNotifTemplateRegexRelation(templateId, regexId);
	}

	@Override
	public void saveObjectRegexRelation(int regexId, int objectId) throws InternalErrorException {
		perunNotifObjectManager.saveObjectRegexRelation(regexId, objectId);
	}

	@Override
	public void removePerunNotifRegexObjectRelation(int regexId, int objectId) throws InternalErrorException {
		perunNotifObjectManager.removePerunNotifRegexObjectRelation(regexId, objectId);
	}
}
