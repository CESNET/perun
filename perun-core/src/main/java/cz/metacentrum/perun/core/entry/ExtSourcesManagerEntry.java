package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
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

import java.util.Arrays;
import java.util.Collections;
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
	public ExtSource createExtSource(PerunSession sess, ExtSource extSource, Map<String, String> attributes) throws ExtSourceExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createExtSource_ExtSource_Map<String_String>_policy"))
			throw new PrivilegeException(sess, "createExtSource");

		Utils.notNull(extSource, "extSource");

		return getExtSourcesManagerBl().createExtSource(sess, extSource, attributes);
	}

	@Override
	public void deleteExtSource(PerunSession sess, ExtSource extSource) throws ExtSourceNotExistsException, PrivilegeException, ExtSourceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		getExtSourcesManagerBl().checkExtSourceExists(sess, extSource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteExtSource_ExtSource_policy", extSource))
			throw new PrivilegeException(sess, "deleteExtSource");

		getExtSourcesManagerBl().deleteExtSource(sess, extSource);
	}

	@Override
	public ExtSource getExtSourceById(PerunSession sess, int id) throws ExtSourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getExtSourceById_int_policy"))
		throw new PrivilegeException(sess, "getExtSourceById");

		return getExtSourcesManagerBl().getExtSourceById(sess, id);
	}

	@Override
	public ExtSource getExtSourceByName(PerunSession sess, String name) throws ExtSourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getExtSourceByName_String_policy"))
			throw new PrivilegeException(sess, "getExtSourceByName");

		Utils.notNull(name, "name");

		return getExtSourcesManagerBl().getExtSourceByName(sess, name);
	}

	@Override
	public List<ExtSource> getVoExtSources(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getVoExtSources_Vo_policy", vo))
			throw new PrivilegeException(sess, "getVoExtSources");

		return getExtSourcesManagerBl().getVoExtSources(sess, vo);
	}

	@Override
	public List<ExtSource> getGroupExtSources(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupExtSources_Group_policy", group))
			throw new PrivilegeException(sess, "getGroupExtSources");

		return getExtSourcesManagerBl().getGroupExtSources(sess, group);
	}

	@Override
	public List<ExtSource> getExtSources(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getExtSources_policy"))
			throw new PrivilegeException(sess, "getExtSources");

		return getExtSourcesManagerBl().getExtSources(sess);
	}
	@Override
	public void addExtSource(PerunSession sess, Vo vo, ExtSource source) throws PrivilegeException, VoNotExistsException, ExtSourceNotExistsException, ExtSourceAlreadyAssignedException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addExtSource_Vo_ExtSource_policy", Arrays.asList(vo, source)))
			throw new PrivilegeException(sess, "addExtSource");

		getExtSourcesManagerBl().addExtSource(sess, vo, source);
	}

	@Override
	public void addExtSource(PerunSession sess, Group group, ExtSource source) throws PrivilegeException, GroupNotExistsException, ExtSourceNotExistsException, ExtSourceAlreadyAssignedException, ExtSourceNotAssignedException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getExtSourcesManagerBl().checkExtSourceExists(sess, source);
		getExtSourcesManagerBl().checkExtSourceAssignedToVo(sess, source, group.getVoId());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addExtSource_Group_ExtSource_policy", Arrays.asList(group, source)))
			throw new PrivilegeException(sess, "addExtSource");

		getExtSourcesManagerBl().addExtSource(sess, group, source);
	}

	@Override
	public ExtSource checkOrCreateExtSource(PerunSession sess, String extSourceName, String extSourceType) {
		Utils.checkPerunSession(sess);

		//TODO Authorization

		Utils.notNull(extSourceName, "extSourceName");
		Utils.notNull(extSourceType, "extSourceType");

		return getExtSourcesManagerBl().checkOrCreateExtSource(sess, extSourceName, extSourceType);
	}

	@Override
	public void removeExtSource(PerunSession sess, Vo vo, ExtSource source) throws PrivilegeException, VoNotExistsException, ExtSourceNotExistsException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeExtSource_Vo_ExtSource_policy", Arrays.asList(vo, source)))
			throw new PrivilegeException(sess, "removeExtSource");

		getExtSourcesManagerBl().removeExtSource(sess, vo, source);
	}

	@Override
	public void removeExtSource(PerunSession sess, Group group, ExtSource source) throws PrivilegeException, GroupNotExistsException, ExtSourceNotExistsException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeExtSource_Group_ExtSource_policy", Arrays.asList(group, source)))
			throw new PrivilegeException(sess, "removeExtSource");

		getExtSourcesManagerBl().removeExtSource(sess, group, source);
	}

	@Override
	public List<User> getInvalidUsers(PerunSession sess, ExtSource source) throws PrivilegeException, ExtSourceNotExistsException {
		Utils.checkPerunSession(sess);

		getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getInvalidUsers_ExtSource_policy", source))
			throw new PrivilegeException(sess, "removeExtSource");

		return getExtSourcesManagerBl().getInvalidUsers(sess, source);
	}

	@Override
	public void loadExtSourcesDefinitions(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "loadExtSourcesDefinitions_policy"))
			throw new PrivilegeException(sess, "loadExtSourcesDefinitions");

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
	public Candidate getCandidate(PerunSession sess, ExtSource source, String login) throws PrivilegeException, ExtSourceNotExistsException, CandidateNotExistsException, ExtSourceUnsupportedOperationException {
		Utils.checkPerunSession(sess);

		getExtSourcesManagerBl().checkExtSourceExists(sess, source);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getCandidate_ExtSource_String_policy", source))
			throw new PrivilegeException(sess, "getCandidate");

		return new Candidate(getExtSourcesManagerBl().getCandidate(sess, source, login));
	}

	@Override
	public Candidate getCandidate(PerunSession perunSession, Map<String,String> subjectData, ExtSource source) throws PrivilegeException, ExtSourceNotExistsException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(subjectData, "subjectData");
		Utils.notNull(subjectData.get("login"), "subjectLogin");

		getExtSourcesManagerBl().checkExtSourceExists(perunSession, source);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getCandidate_Map<String_String>_ExtSource_policy", source))
			throw new PrivilegeException(perunSession, "getCandidate");

		return new Candidate(getExtSourcesManagerBl().getCandidate(perunSession, subjectData, source, subjectData.get("login")));
	}

	@Override
	public Map<String, String> getAttributes(PerunSession sess, ExtSource extSource) throws PrivilegeException, ExtSourceNotExistsException {
		Utils.checkPerunSession(sess);

		getExtSourcesManagerBl().checkExtSourceExists(sess, extSource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAttributes_ExtSource_policy", extSource))
			throw new PrivilegeException(sess, "getAttributes");

		return getExtSourcesManagerBl().getAttributes(extSource);
	}
}
