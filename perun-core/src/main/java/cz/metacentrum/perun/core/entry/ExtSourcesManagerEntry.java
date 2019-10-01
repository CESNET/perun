package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.ExtSourcesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ExtSourcesManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * ExtSourcesManager entry logic.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class ExtSourcesManagerEntry implements ExtSourcesManager {

	final static Logger log = LoggerFactory.getLogger(ExtSourcesManagerEntry.class);

	private ExtSourcesManagerBl extSourcesManagerBl;
	private PerunBl perunBl;


	public ExtSourcesManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.extSourcesManagerBl = perunBl.getExtSourcesManagerBl();
	}

	public ExtSourcesManagerEntry() {}

	//FIXME delete this method
	public ExtSourcesManagerImplApi getExtSourcesManagerImpl() {
		throw new InternalErrorException("Unsupported method!");
	}

	@Override
	public ExtSource createExtSource(PerunSession sess, ExtSource extSource, Map<String, String> attributes) throws InternalErrorException, ExtSourceExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "createExtSource");
		}

		Utils.notNull(extSource, "extSource");

		return getExtSourcesManagerBl().createExtSource(sess, extSource, attributes);
	}

	@Override
	public void deleteExtSource(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceNotExistsException, PrivilegeException, ExtSourceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "deleteExtSource");
		}

		getExtSourcesManagerBl().checkExtSourceExists(sess, extSource);

		getExtSourcesManagerBl().deleteExtSource(sess, extSource);
	}

	@Override
	public ExtSource getExtSourceById(PerunSession sess, int id) throws InternalErrorException, ExtSourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.RPC) && !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getExtSourceById");
		}

		return getExtSourcesManagerBl().getExtSourceById(sess, id);
	}

	@Override
	public ExtSource getExtSourceByName(PerunSession sess, String name) throws InternalErrorException, ExtSourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.RPC) && !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getExtSourceByName");
		}

		Utils.notNull(name, "name");

		return getExtSourcesManagerBl().getExtSourceByName(sess, name);
	}

	@Override
	public List<ExtSource> getVoExtSources(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getVoExtSources");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getExtSourcesManagerBl().getVoExtSources(sess, vo);
	}

	@Override
	public List<ExtSource> getGroupExtSources(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getGroupExtSources");
		}

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		return getExtSourcesManagerBl().getGroupExtSources(sess, group);
	}

	@Override
	public List<ExtSource> getExtSources(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getExtSources");
		}

		return getExtSourcesManagerBl().getExtSources(sess);
	}
	@Override
	public void addExtSource(PerunSession sess, Vo vo, ExtSource source) throws InternalErrorException, PrivilegeException, VoNotExistsException, ExtSourceNotExistsException, ExtSourceAlreadyAssignedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "addExtSource");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		getExtSourcesManagerBl().addExtSource(sess, vo, source);
	}

	@Override
	public void addExtSource(PerunSession sess, Group group, ExtSource source) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ExtSourceNotExistsException, ExtSourceAlreadyAssignedException, ExtSourceNotAssignedException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)) {
			throw new PrivilegeException(sess, "addExtSource");
		}

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getExtSourcesManagerBl().checkExtSourceExists(sess, source);
		getExtSourcesManagerBl().checkExtSourceAssignedToVo(sess, source, group.getVoId());

		getExtSourcesManagerBl().addExtSource(sess, group, source);
	}

	@Override
	public ExtSource checkOrCreateExtSource(PerunSession sess, String extSourceName, String extSourceType) throws InternalErrorException {
		Utils.checkPerunSession(sess);

		//TODO Authorization

		Utils.notNull(extSourceName, "extSourceName");
		Utils.notNull(extSourceType, "extSourceType");

		return getExtSourcesManagerBl().checkOrCreateExtSource(sess, extSourceName, extSourceType);
	}

	@Override
	public void removeExtSource(PerunSession sess, Vo vo, ExtSource source) throws InternalErrorException, PrivilegeException, VoNotExistsException, ExtSourceNotExistsException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "removeExtSource");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		getExtSourcesManagerBl().removeExtSource(sess, vo, source);
	}

	@Override
	public void removeExtSource(PerunSession sess, Group group, ExtSource source) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ExtSourceNotExistsException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)) {
			throw new PrivilegeException(sess, "removeExtSource");
		}

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		getExtSourcesManagerBl().removeExtSource(sess, group, source);
	}

	@Override
	public List<User> getInvalidUsers(PerunSession sess, ExtSource source) throws InternalErrorException, PrivilegeException, ExtSourceNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "removeExtSource");
		}

		getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		return getExtSourcesManagerBl().getInvalidUsers(sess, source);
	}

	@Override
	public void loadExtSourcesDefinitions(PerunSession sess) throws PrivilegeException, InternalErrorException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "loadExtSourcesDefinitions");
		}

		getExtSourcesManagerBl().loadExtSourcesDefinitions(sess);
	}

	/**
	 * Gets the extSourcesManagerBl for this instance.
	 *
	 * @return extSourceManagerImpl
	 */
	public ExtSourcesManagerBl getExtSourcesManagerBl() {
		return this.extSourcesManagerBl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl)
	{
		this.perunBl = perunBl;
	}

	/**
	 * Sets the extSourcesManagerBl for this instance.
	 *
	 * @param extSourcesManagerBl The extSourcesManagerBl.
	 */
	public void setExtSourcesManagerBl(ExtSourcesManagerBl extSourcesManagerBl)
	{
		this.extSourcesManagerBl = extSourcesManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	@Override
	public Candidate getCandidate(PerunSession sess, ExtSource source, String login) throws InternalErrorException, PrivilegeException, ExtSourceNotExistsException, CandidateNotExistsException, ExtSourceUnsupportedOperationException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getCandidate");
		}

		getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		return getExtSourcesManagerBl().getCandidate(sess, source, login);
	}

	@Override
	public Candidate getCandidate(PerunSession perunSession, Map<String,String> subjectData, ExtSource source) throws InternalErrorException, PrivilegeException, ExtSourceNotExistsException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(subjectData, "subjectData");
		Utils.notNull(subjectData.get("login"), "subjectLogin");

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN) &&
			!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getCandidate");
		}

		getExtSourcesManagerBl().checkExtSourceExists(perunSession, source);

		return getExtSourcesManagerBl().getCandidate(perunSession, subjectData, source, subjectData.get("login"));
	}

	@Override
	public Map<String, String> getAttributes(PerunSession sess, ExtSource extSource) throws InternalErrorException, PrivilegeException, ExtSourceNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAttributes");
		}
		getExtSourcesManagerBl().checkExtSourceExists(sess, extSource);
		return getExtSourcesManagerBl().getAttributes(extSource);
	}
}
