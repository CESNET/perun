package cz.metacentrum.perun.notif.managers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.notif.dao.PerunNotifObjectDao;
import cz.metacentrum.perun.notif.dao.PerunNotifRegexDao;
import cz.metacentrum.perun.notif.entities.PerunNotifAuditMessage;
import cz.metacentrum.perun.notif.entities.PerunNotifObject;
import cz.metacentrum.perun.notif.entities.PerunNotifRegex;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import cz.metacentrum.perun.notif.exceptions.NotExistsException;
import cz.metacentrum.perun.notif.exceptions.NotifRegexAlreadyExistsException;
import cz.metacentrum.perun.notif.exceptions.PerunNotifRegexUsedException;
import java.util.ArrayList;

@Service
public class PerunNotifRegexManagerImpl implements PerunNotifRegexManager {

	@Autowired
	private PerunNotifRegexDao perunNotifRegexDao;
	@Autowired
	private PerunNotifTemplateManager perunNotifTemplateManager;
	@Autowired
	private PerunNotifObjectDao perunNotifObjectDao;

	private static final Logger logger = LoggerFactory.getLogger(PerunNotifRegexManager.class);

	private Set<PerunNotifRegex> allRegex = null;
	private Set<PerunNotifObject> allObjects = null;

	@PostConstruct
	private void init() {

		allRegex = Collections.synchronizedSet(new HashSet<PerunNotifRegex>());
		allRegex.addAll(perunNotifRegexDao.getAll());

		allObjects = Collections.synchronizedSet(new HashSet<PerunNotifObject>());
		allObjects.addAll(perunNotifObjectDao.getAll());
	}

	@Override
	public Set<Integer> getIdsOfRegexesMatchingMessage(PerunNotifAuditMessage auditMessage) throws InternalErrorException {

		Set<PerunNotifObject> setOfObjects = new HashSet<PerunNotifObject>();
		for (PerunBean bean : auditMessage.getPerunBeanList()) {
			for (PerunNotifObject object : allObjects) {
				if (object.getObjectClass() != null) {
					if (bean.getClass().isAssignableFrom(object.getObjectClass())) {
						setOfObjects.add(object);
					}
				}
			}
		}

		Set<Integer> result = new HashSet<Integer>();

		for (PerunNotifRegex regex : allRegex) {
			if (auditMessage.getMessage().matches(regex.getRegex())) {
				//We test whether message has all objects
				boolean matches = true;
				for (PerunNotifObject object : regex.getObjects()) {
					if (!setOfObjects.contains(object)) {
						matches = false;
					}
				}
				if (matches) {
					result.add(regex.getId());
				}
			}
		}

		return result;
	}

	@Override
	public PerunNotifRegex getPerunNotifRegexById(int id) throws InternalErrorException {

		return perunNotifRegexDao.getPerunNotifRegexById(id);
	}

	@Override
	public List<PerunNotifRegex> getAllPerunNotifRegexes() {
		return perunNotifRegexDao.getAll();
	}

	@Override
	public PerunNotifRegex createPerunNotifRegex(PerunNotifRegex regex) throws InternalErrorException, NotifRegexAlreadyExistsException {

		// check if there is no other Notif regex with the same regular expression
		for (PerunNotifRegex item: getAllPerunNotifRegexes()) {
			if (item.getRegex().equals(regex.getRegex())) {
				throw new NotifRegexAlreadyExistsException(regex);
			}
		}

		PerunNotifRegex perunNotifRegex = perunNotifRegexDao.saveInternals(regex);

		for (PerunNotifObject object : regex.getObjects()) {
			if (object.getId() == null) {
				throw new NotExistsException("Object does not exists.");
			}

			perunNotifObjectDao.saveObjectRelation(regex.getId(), object.getId());
		}

		allRegex.add(regex);

		return perunNotifRegex;
	}

	public PerunNotifRegex updatePerunNotifRegex(PerunNotifRegex regex) throws InternalErrorException {

		PerunNotifRegex oldRegex = perunNotifRegexDao.getPerunNotifRegexById(regex.getId());
		PerunNotifRegex updatedRegex = perunNotifRegexDao.updatePerunNotifRegexInternals(regex);

		Set<PerunNotifObject> oldObjects = new HashSet<PerunNotifObject>(oldRegex.getObjects());
		Set<PerunNotifObject> newObjects = updatedRegex.getObjects();

		for (PerunNotifObject object : newObjects) {
			if (oldObjects.contains(object)) {
				//Object relation has not changed
				oldObjects.remove(object);
			} else {
				//Object is new in relation
				perunNotifObjectDao.saveObjectRelation(updatedRegex.getId(), object.getId());
			}
		}

		//In set oldObjects remains now only objects which relation has been removed.
		for (PerunNotifObject objectsToRemove : oldObjects) {
			perunNotifObjectDao.removePerunNotifObjectRegexRelation(updatedRegex.getId(), objectsToRemove.getId());
		}

		for (PerunNotifRegex regexToUpdate : allRegex) {
			if (regexToUpdate.getId().equals(updatedRegex.getId())) {
				regexToUpdate.update(updatedRegex);
				return updatedRegex;
			}
		}

		logger.warn("Updating regex in cache failed. Regex not found in cache id: {}", updatedRegex.getId());
		return updatedRegex;
	}

	@Override
	public void removePerunNotifRegexById(int id) throws InternalErrorException, PerunNotifRegexUsedException {

		PerunNotifRegex regex = getPerunNotifRegexById(id);
		if (regex == null) {
			throw new NotExistsException("Regex does not exists in db");
		}

		List<Integer> referencedTemplates = perunNotifRegexDao.getTemplateIdsForRegexId(id);
		if (referencedTemplates != null && !referencedTemplates.isEmpty()) {
			throw new PerunNotifRegexUsedException("Regex is still used.", referencedTemplates);
		}

		perunNotifRegexDao.removePerunNotifRegexById(id);

		allRegex.remove(regex);
	}

	@Override
	public void saveTemplateRegexRelation(int templateId, Integer regexId) throws InternalErrorException {

		if (perunNotifRegexDao.isRegexRelation(templateId, regexId)) {
			//Relation exists
			return;
		} else {
			perunNotifRegexDao.saveTemplateRegexRelation(templateId, regexId);
			PerunNotifTemplate template = perunNotifTemplateManager.getPerunNotifTemplateById(templateId);
			template.addPerunNotifRegex(getPerunNotifRegexById(regexId));
			perunNotifTemplateManager.assignTemplateToRegex(regexId, template);
		}
	}

	@Override
	public List<PerunNotifRegex> getRelatedRegexesForTemplate(int templateId) throws InternalErrorException {
		return new ArrayList<>(perunNotifRegexDao.getPerunNotifRegexForTemplateId(templateId));
	}

	@Override
	public void removePerunNotifTemplateRegexRelation(int templateId, int regexId) throws InternalErrorException {

		if (perunNotifRegexDao.isRegexRelation(templateId, regexId)) {
			perunNotifRegexDao.removePerunNotifTemplateRegexRelation(templateId, regexId);
			PerunNotifTemplate template = perunNotifTemplateManager.getPerunNotifTemplateById(templateId);
			template.getMatchingRegexs().remove(getPerunNotifRegexById(regexId));
			perunNotifTemplateManager.removeTemplateFromRegex(regexId, templateId);
		} else {
			throw new InternalErrorException("Template - Regex relation cannot be removed, because does not exist.");
		}
	}

	@Override
	public void addObjectToCache(PerunNotifObject object) {
		if (!allObjects.add(object)) {
			//Object is already in set
			for (PerunNotifObject cacheObject : allObjects) {
				if (cacheObject.getId().equals(object.getId())) {
					cacheObject.update(object);
					return;
				}
			}
		}
	}

	@Override
	public void updateObjectInCache(PerunNotifObject object) {

		for (PerunNotifObject oldObject : allObjects) {
			if (oldObject.getId().equals(object.getId())) {
				oldObject.update(object);
			}
		}
	}

	@Override
	public void removePerunNotifObjectFromCache(PerunNotifObject objectToRemove) {

		boolean removed = false;
		for (Iterator<PerunNotifObject> iter = allObjects.iterator(); iter.hasNext();) {
			PerunNotifObject objectFromCache = iter.next();
			if (objectFromCache.getId().equals(objectToRemove.getId())) {
				iter.remove();
				removed = true;
			}
		}
		if (!removed) {
			logger.warn("Remove of object from cache failed. Object is not in cache. ID: {}", objectToRemove.getId());
		}

		for (PerunNotifRegex regex : allRegex) {
			Set<PerunNotifObject> regexObjects = regex.getObjects();
			if (regexObjects.contains(objectToRemove)) {
				regexObjects.remove(objectToRemove);
			}
		}
	}
}
