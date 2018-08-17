package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Searcher;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SearcherBl;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class SearcherEntry implements Searcher {

	final static Logger log = LoggerFactory.getLogger(ResourcesManagerEntry.class);

	private SearcherBl searcherBl;
	private PerunBl perunBl;

	public SearcherEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.searcherBl = perunBl.getSearcherBl();
	}

	public SearcherEntry() {
	}

	public List<User> getUsers(PerunSession sess, Map<String, String> attributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, PrivilegeException, WrongAttributeAssignmentException {
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getUsers");
		}

		return searcherBl.getUsers(sess, attributesWithSearchingValues);
	}

	public List<Member> getMembersByUserAttributes(PerunSession sess, Vo vo,  Map<String, String> userAttributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, PrivilegeException, WrongAttributeAssignmentException, VoNotExistsException {		// Authorization
		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getMembersByUserAttributes");
		}

		//If map is null or empty, return all members from vo
		if(userAttributesWithSearchingValues == null || userAttributesWithSearchingValues.isEmpty()) {
			return perunBl.getMembersManagerBl().getMembers(sess, vo);
		}

		Set<String> attrNames = userAttributesWithSearchingValues.keySet();
		List<AttributeDefinition> attrDefs = new ArrayList<>();
		for(String attrName: attrNames) {
			if(attrName == null || attrName.isEmpty()) throw new InternalErrorException("One of attributes has empty name.");

			//throw AttributeNotExistsException if this attr_name not exists in DB
			AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, attrName);
			attrDefs.add(attrDef);

			//test namespace of attribute
			if(!getPerunBl().getAttributesManagerBl().isFromNamespace(sess, attrDef, AttributesManager.NS_USER_ATTR)) {
				throw new WrongAttributeAssignmentException("Attribute can be only in user namespace " + attrDef);
			}
		}

		//get all found users
		List<User> users = searcherBl.getUsers(sess, userAttributesWithSearchingValues);
		List<Member> members = new ArrayList<>();

		for(User user: users) {

			//get member for user
			Member member;
			try {
				member = perunBl.getMembersManagerBl().getMemberByUser(sess, vo, user);
			} catch (MemberNotExistsException ex) {
				continue;
			}

			boolean isAuthorized = true;
			for(AttributeDefinition attrDef: attrDefs) {
				//Test if user has righ to read such attribute for specific user, if not, remove it from returning list
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, user, null)) {
					isAuthorized = false;
					break;
				}
			}
			if(isAuthorized) members.add(member);
		}

		return members;
	}

	public List<User> getUsersForCoreAttributes(PerunSession sess, Map<String, String> coreAttributesWithSearchingValues) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, PrivilegeException {
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getUsersForCoreAttributes");
		}

		return searcherBl.getUsersForCoreAttributes(sess, coreAttributesWithSearchingValues);
	}

	@Override
	public List<Member> getMembersByExpiration(PerunSession sess, String operator, int days) throws PrivilegeException, InternalErrorException {

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getMembersByExpiration");
		}

		return getPerunBl().getSearcherBl().getMembersByExpiration(sess, operator, days);

	}

	@Override
	public List<Member> getMembersByExpiration(PerunSession sess, String operator, Calendar date) throws PrivilegeException, InternalErrorException {

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getMembersByExpiration");
		}

		return getPerunBl().getSearcherBl().getMembersByExpiration(sess, operator, date);

	}

	@Override
	public List<Facility> getFacilities(PerunSession sess, Map<String, String> attributesWithSearchingValues) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "getFacilities");
		}

		return searcherBl.getFacilities(sess, attributesWithSearchingValues);
	}

	public SearcherBl getSearcherBl() {
		return this.searcherBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	public void setSearcherBl(SearcherBl searcherBl) {
		this.searcherBl = searcherBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}


}
