package cz.metacentrum.perun.notif.managers;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.notif.entities.*;
import cz.metacentrum.perun.notif.exceptions.NotifReceiverAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifRegexAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifTemplateMessageAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.PerunNotifRegexUsedException;
import cz.metacentrum.perun.notif.exceptions.TemplateMessageSyntaxErrorException;
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

	@Autowired
	private SchedulingManagerImpl schedulingManager;

	@Override
	public PerunNotifObject getPerunNotifObjectById(int id) {

		return perunNotifObjectManager.getPerunNotifObjectById(id);
	}

	@Override
	public List<PerunNotifObject> getAllPerunNotifObjects() {

		return perunNotifObjectManager.getAllPerunNotifObjects();
	}

	@Override
	public PerunNotifObject createPerunNotifObject(PerunNotifObject object) {

		return perunNotifObjectManager.createPerunNotifObject(object);
	}

	@Override
	public PerunNotifObject updatePerunNotifObject(PerunNotifObject object) {

		return perunNotifObjectManager.updatePerunNotifObject(object);
	}

	@Override
	public void removePerunNotifObjectById(int id) {

		perunNotifObjectManager.removePerunNotifObjectById(id);
	}

	@Override
	public PerunNotifReceiver getPerunNotifReceiverById(PerunSession sess, int id) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "getPerunNotifReceiverById");
		}

		return perunNotifTemplateManager.getPerunNotifReceiverById(id);
	}

	@Override
	public List<PerunNotifReceiver> getAllPerunNotifReceivers(PerunSession sess) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "getAllPerunNotifReceivers");
		}
		return perunNotifTemplateManager.getAllPerunNotifReceivers();
	}

	@Override
	public PerunNotifReceiver createPerunNotifReceiver(PerunSession sess, PerunNotifReceiver receiver) throws NotifReceiverAlreadyExistsException, PrivilegeException {

		return perunNotifTemplateManager.createPerunNotifReceiver(receiver);
	}

	@Override
	public PerunNotifReceiver updatePerunNotifReceiver(PerunSession sess, PerunNotifReceiver receiver) throws NotifReceiverAlreadyExistsException, PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "updatePerunNotifReceiver");
		}
		return perunNotifTemplateManager.updatePerunNotifReceiver(receiver);
	}

	@Override
	public void removePerunNotifReceiverById(PerunSession sess, int id) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "removePerunNotifReceiverById");
		}
		perunNotifTemplateManager.removePerunNotifReceiverById(id);
	}

	@Override
	public PerunNotifRegex getPerunNotifRegexById(PerunSession sess, int id) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "getPerunNotifRegexById");
		}
		return perunNotifRegexManager.getPerunNotifRegexById(id);
	}

	@Override
	public List<PerunNotifRegex> getAllPerunNotifRegexes(PerunSession sess) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "getAllPerunNotifRegexes");
		}
		return perunNotifRegexManager.getAllPerunNotifRegexes();
	}

	@Override
	public PerunNotifRegex createPerunNotifRegex(PerunSession sess, PerunNotifRegex regex) throws NotifRegexAlreadyExistsException, PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "createPerunNotifRegex");
		}
		return perunNotifRegexManager.createPerunNotifRegex(regex);
	}

	@Override
	public PerunNotifRegex updatePerunNotifRegex(PerunSession sess, PerunNotifRegex regex) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "updatePerunNotifRegex");
		}
		return perunNotifRegexManager.updatePerunNotifRegex(regex);
	}

	@Override
	public void removePerunNotifRegexById(PerunSession sess, int id) throws PerunNotifRegexUsedException, PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "removePerunNotifRegexById");
		}

		perunNotifRegexManager.removePerunNotifRegexById(id);
	}

	@Override
	public PerunNotifTemplate getPerunNotifTemplateById(PerunSession sess, int id) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "getPerunNotifTemplateById");
		}

		return perunNotifTemplateManager.getPerunNotifTemplateByIdFromDb(id);
	}

	@Override
	public List<PerunNotifTemplateMessage> getAllPerunNotifTemplateMessages(PerunSession sess) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "getAllPerunNotifTemplateMessages");
		}
		return perunNotifTemplateManager.getAllPerunNotifTemplateMessages();
	}

	@Override
	public PerunNotifTemplate createPerunNotifTemplate(PerunSession sess, PerunNotifTemplate template) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "createPerunNotifTemplate");
		}

		return perunNotifTemplateManager.createPerunNotifTemplate(template);
	}

	@Override
	public PerunNotifTemplate updatePerunNotifTemplate(PerunSession sess, PerunNotifTemplate template) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "updatePerunNotifTemplate");
		}

		return perunNotifTemplateManager.updatePerunNotifTemplate(template);
	}

	@Override
	public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(PerunSession sess, int id) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "removePerunNotifReceiverById");
		}

		return perunNotifTemplateManager.getPerunNotifTemplateMessageById(id);
	}

	@Override
	public List<PerunNotifTemplate> getAllPerunNotifTemplates(PerunSession sess) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "getAllPerunNotifTemplates");
		}
		return perunNotifTemplateManager.getAllPerunNotifTemplates();
	}

	@Override
	public PerunNotifTemplateMessage createPerunNotifTemplateMessage(PerunSession sess, PerunNotifTemplateMessage message) throws NotifTemplateMessageAlreadyExistsException, TemplateMessageSyntaxErrorException, PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "createPerunNotifTemplateMessage");
		}
		return perunNotifTemplateManager.createPerunNotifTemplateMessage(message);
	}

	@Override
	public PerunNotifTemplateMessage updatePerunNotifTemplateMessage(PerunSession sess, PerunNotifTemplateMessage message) throws TemplateMessageSyntaxErrorException, PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "updatePerunNotifTemplateMessage");
		}
		return perunNotifTemplateManager.updatePerunNotifTemplateMessage(message);
	}

	@Override
	public void removePerunNotifTemplateMessage(PerunSession sess, int id) {
		perunNotifTemplateManager.removePerunNotifTemplateMessage(id);
	}

	@Override
	public void removePerunNotifTemplateById(PerunSession sess, int id) {
		perunNotifTemplateManager.removePerunNotifTemplateById(id);
	}

	@Override
	public String testPerunNotifMessageText(String template, Map<Integer, List<PerunBean>> regexIdsPerunBeans) throws IOException, TemplateException {

		return perunNotifTemplateManager.testPerunNotifMessageText(template, regexIdsPerunBeans);
	}

	@Override
	public void saveTemplateRegexRelation(PerunSession sess, int templateId, Integer regexId) {
		perunNotifRegexManager.saveTemplateRegexRelation(templateId, regexId);
	}

	@Override
	public List<PerunNotifRegex> getRelatedRegexesForTemplate(PerunSession sess, int templateId) {
		return perunNotifRegexManager.getRelatedRegexesForTemplate(templateId);
	}

	@Override
	public void removePerunNotifTemplateRegexRelation(PerunSession sess, int templateId, int regexId) {
		perunNotifRegexManager.removePerunNotifTemplateRegexRelation(templateId, regexId);
	}

	@Override
	public void saveObjectRegexRelation(int regexId, int objectId) {
		perunNotifObjectManager.saveObjectRegexRelation(regexId, objectId);
	}

	@Override
	public void removePerunNotifRegexObjectRelation(int regexId, int objectId) {
		perunNotifObjectManager.removePerunNotifRegexObjectRelation(regexId, objectId);
	}

	@Override
	public void stopNotifications(PerunSession sess) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "stopNotifications");
		}
		schedulingManager.stopNotifications();
	}

	@Override
	public void startNotifications(PerunSession sess) throws PrivilegeException {
		if (!(AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))) {
			throw new PrivilegeException(sess, "stopNotifications");
		}
		schedulingManager.startNotifications();
	}

	@Override
	public boolean isNotificationsRunning() {
		return schedulingManager.isNotificationsRunning();
	}
}
