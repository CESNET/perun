package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.ActionType;

import java.util.*;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeRights;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;

/**
 * AttributesManager entry logic.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class AttributesManagerEntry implements AttributesManager {

	private PerunBl perunBl;
	private AttributesManagerBl attributesManagerBl;

	/**
	 * Constructor.
	 */
	public AttributesManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.attributesManagerBl = perunBl.getAttributesManagerBl();
	}

	public AttributesManagerEntry() {
	}

	public List<Attribute> getAttributes(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);

		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, facility);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), facility, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility, null));
		}

		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, vo);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), vo, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, vo, null));
		}

		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), group, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, null));
		}

		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource) throws PrivilegeException, ResourceNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), resource, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, null));
		}

		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException {
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, resource, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), resource, member)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
		}

		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member, boolean workWithUserAttributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, resource, member, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, facility, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility, user));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
			} else {
				throw new ConsistencyErrorException("One of getting attributes is not correct type : " + attrNext);
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, group, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		// Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames, boolean workWithUserAttributes) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, group, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		// Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else {
				throw new ConsistencyErrorException("One of getting attributes is not correct type : " + attrNext);
			}
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member) throws PrivilegeException, WrongAttributeAssignmentException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, facility, resource, user, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, facility, user)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility, user));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
			} else {
				throw new ConsistencyErrorException("One of getting attributes is not correct type_: " + attrNext);
			}
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Member member) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Vo vo, List<String> attrNames) throws PrivilegeException, InternalErrorException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, vo, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), vo, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, vo, null));
		}
		return attributes;
	}

	public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Group group, String startPartOfName) throws PrivilegeException, GroupNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		Utils.notNull(startPartOfName, "startPartOfName");
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, group, startPartOfName);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), group, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, null));
		}
		return attributes;
	}

	public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Resource resource, String startPartOfName) throws PrivilegeException, ResourceNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		Utils.notNull(startPartOfName, "startPartOfName");
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		List<Attribute> attributes = getAttributesManagerBl().getAllAttributesStartWithNameWithoutNullValue(sess, resource, startPartOfName);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), resource, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, null));
		}
		return attributes;

	}

	public List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Group group, List<String> attrNames) throws PrivilegeException, InternalErrorException, GroupNotExistsException{
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, group, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), group, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, null));
		}
		return attributes;
	}
	
	public List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, attrNames, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), member, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, User user, List<String> attrNames) throws PrivilegeException, InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, user, attrNames);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), user, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Facility facility, User user) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, facility, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), facility, user)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility, user));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, User user) throws PrivilegeException, InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), user, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Host host) throws PrivilegeException, InternalErrorException, HostNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, host);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), host, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, host, null));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, resource, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, new AttributeDefinition(attrNext), resource, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, group));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, resource, group, workWithGroupAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, null));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, resource)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, resource));
			} else {
				throw new ConsistencyErrorException("One of getting attribute is not type of group or group_resource : " + attrNext);
			}
		}

		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getAttributes(sess, member, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
			} else {
				throw new ConsistencyErrorException("One of getting attribute is not type of member or user : " + attrNext);
			}
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, String key) throws PrivilegeException, InternalErrorException{
		Utils.checkPerunSession(sess);
		Utils.notNull(key, "key for entityless attribute");
		if(key.isEmpty()) throw new InternalErrorException("key for entityless attribute can't be empty string");

		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("For getting entityless attributes principal need to be PerunAdmin.");

		return getAttributesManagerBl().setWritableTrue(sess, getAttributesManagerBl().getAttributes(sess, key));
	}

	public List<Attribute> getEntitylessAttributes(PerunSession sess, String attrName) throws PrivilegeException, InternalErrorException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attrName, "name of entityless attributes");
		if(attrName.isEmpty()) throw new InternalErrorException("name for entityless attribute can't be empty string");
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("For getting entityless attributes principal need to be PerunAdmin.");

		return getAttributesManagerBl().setWritableTrue(sess, getAttributesManagerBl().getEntitylessAttributes(sess, attrName));
	}

	public List<String> getEntitylessKeys(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, PrivilegeException {
		Utils.checkPerunSession(sess);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("For getting entityless attributes principal need to be PerunAdmin.");
		return getAttributesManagerBl().getEntitylessKeys(sess, attributeDefinition);
	}

	public List<Attribute> getAttributesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attributeDefinition);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("For getting entityless attributes principal need to be PerunAdmin.");
		return getAttributesManagerBl().setWritableTrue(sess, getAttributesManagerBl().getAttributesByAttributeDefinition(sess, attributeDefinition));
	}

	public void setAttributes(PerunSession sess, Facility facility, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);

		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), facility , null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, facility, attributes);
	}

	public void setAttributes(PerunSession sess, Vo vo, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), vo , null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, vo, attributes);
	}

	public void setAttributes(PerunSession sess, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group , null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, group, attributes);
	}

	public void setAttributes(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, resource, attributes);
	}

	public void setAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , member)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, resource, member, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		for (Attribute attribute : attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		// Choose to which attributes has the principal access
		for(Attribute attribute : attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member, group)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		}
		getAttributesManagerBl().setAttributes(sess, member, group, attributes);
	}

	@Override
	public void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_GROUP_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member, group))
					throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR_DEF)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, member, group, attributes, workWithUserAttributes);
	}

	public void setAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR_DEF)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , member)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u , f)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, resource, member, attributes, workWithUserAttributes);
	}

	public void setAttributes(PerunSession sess, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, MemberNotExistsException, UserNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException{
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		if(workWithUserAttributes) getPerunBl().getUsersManagerBl().checkUserExists(sess,getPerunBl().getUsersManagerBl().getUserByMember(sess, member));
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, member, attributes, workWithUserAttributes);
	}


	public void setAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR_DEF)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , member)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , facility)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, facility, resource, user, member, attributes);
	}

	public void setAttributes(PerunSession sess, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, member, attributes);
	}

	public void setAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , facility)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, facility, user, attributes);
	}

	public void setAttributes(PerunSession sess, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, user, attributes);
	}

	public void setAttributes(PerunSession sess, Host host, List<Attribute> attributes) throws PrivilegeException, HostNotExistsException, InternalErrorException,AttributeNotExistsException,WrongAttributeAssignmentException, WrongAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), host, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		try {
			getAttributesManagerBl().setAttributes(sess, host, attributes);
		} catch (WrongReferenceAttributeValueException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, AttributeNotExistsException, WrongReferenceAttributeValueException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , group)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().setAttributes(sess, resource, group, attributes);
	}

	public void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttributes) throws InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, AttributeNotExistsException, WrongReferenceAttributeValueException, PrivilegeException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		for(Attribute attribute: attributes) {
			attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
			attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		}
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}

		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group , resource)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("One of setting attribute has not correct type : " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().setAttributes(sess, resource, group, attributes, workWithGroupAttributes);
	}

	public Attribute getAttribute(PerunSession sess, Facility facility, String attributeName) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, facility, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, facility, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility, null));
		return attr;
	}

	public Attribute getAttribute(PerunSession sess, Vo vo, String attributeName) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, vo, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, vo, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, vo, null));
		return attr;
	}

	public Attribute getAttribute(PerunSession sess, Group group, String attributeName) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, group, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, group, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, group, null));
		return attr;
	}

	public Attribute getAttribute(PerunSession sess, Resource resource, String attributeName) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, resource, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, resource, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource, null));
		return attr;
	}

	public Attribute getAttribute(PerunSession sess, Resource resource, Member member, String attributeName) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, resource, member, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, resource, member)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource, member));
		return attr;
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Member member, Group group, String attributeName) throws PrivilegeException, GroupNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		Attribute attribute = getAttributesManagerBl().getAttribute(sess, member, group, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attribute, member, group)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attribute));
		else attribute.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member, group));

		return attribute;
	}

	public Attribute getAttribute(PerunSession sess, Member member, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, member, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, member, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member, null));
		return attr;
	}

	public Attribute getAttribute(PerunSession sess, Facility facility, User user, String attributeName) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, facility, user, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, facility, user)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility, user));
		return attr;
	}

	public Attribute getAttribute(PerunSession sess, User user, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, user, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, user, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, user, null));
		return attr;
	}

	public Attribute getAttribute(PerunSession sess, Host host, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, HostNotExistsException,WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		Attribute attr = getAttributesManagerBl().getAttribute(sess, host, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, host, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, host, null));
		return attr;
	}

	public Attribute getAttribute(PerunSession sess, Resource resource, Group group, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		Attribute attr = getAttributesManagerBl().getAttribute(sess, resource, group, attributeName);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, resource, group)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource, group));
		return attr;
	}

	public Attribute getAttribute(PerunSession sess, String key, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		Utils.notNull(key, "key");
		if(key.isEmpty()) throw new InternalErrorException("key for entityless attribute can't be empty string");

		return getAttributesManagerBl().getAttribute(sess, key, attributeName);
	}

	public AttributeDefinition getAttributeDefinition(PerunSession sess, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attributeName, "attributeName");
		//Everyone can access to attrDefs
		return getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
	}

	public List<AttributeDefinition> getAttributesDefinition(PerunSession sess) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		//Everyone can access to attrDefs
		return getAttributesManagerBl().getAttributesDefinition(sess);
	}

	public List<AttributeDefinition> getAttributesDefinitionWithRights(PerunSession sess, List<PerunBean> entities) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		//For this method are rights resolved in the Bl(BlImpl)
		return getAttributesManagerBl().getAttributesDefinitionWithRights(sess, entities);
	}

	public List<AttributeDefinition> getAttributesDefinition(PerunSession sess, List<String> listOfAttributesNames) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(listOfAttributesNames, "List of attrNames");
		//Everyone can access to attrDefs
		return getAttributesManagerBl().getAttributesDefinition(sess, listOfAttributesNames);
	}

	public AttributeDefinition getAttributeDefinitionById(PerunSession sess, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		//Everyone can access to attrDefs
		return getAttributesManagerBl().getAttributeDefinitionById(sess, id);
	}

	public List<AttributeDefinition> getAttributesDefinitionByNamespace(PerunSession sess, String namespace) throws PrivilegeException, InternalErrorException {
		Utils.checkPerunSession(sess);
		Utils.notNull(namespace, "namespace");
		//Everyone can access to attrDefs
		return getAttributesManagerBl().getAttributesDefinitionByNamespace(sess, namespace);
	}

	public Attribute getAttributeById(PerunSession sess, Facility facility, int id) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, facility, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, facility, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility, null));
		return attr;
	}

	public Attribute getAttributeById(PerunSession sess, Vo vo, int id) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, vo, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, vo, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, vo, null));
		return attr;
	}

	public Attribute getAttributeById(PerunSession sess, Resource resource, int id) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, resource, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, resource, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource, null));
		return attr;
	}

	public Attribute getAttributeById(PerunSession sess, Resource resource, Member member, int id) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, resource, member, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, resource, member)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource, member));
		return attr;
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Member member, Group group, int id) throws PrivilegeException, GroupNotExistsException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		Attribute attribute = getAttributesManagerBl().getAttributeById(sess, member, group, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attribute, member, group)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attribute));
		else attribute.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member, group));

		return attribute;
	}

	public Attribute getAttributeById(PerunSession sess, Member member, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, member, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, member, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member, null));
		return attr;
	}

	public Attribute getAttributeById(PerunSession sess, Facility facility, User user, int id) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, facility, user, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, facility, user)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility, user));
		return attr;
	}

	public Attribute getAttributeById(PerunSession sess, User user, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, user, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, user, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, user, null));
		return attr;
	}

	public Attribute getAttributeById(PerunSession sess, Host host, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, HostNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, host, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, host, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, host, null));
		return attr;
	}

	public Attribute getAttributeById(PerunSession sess, Resource resource, Group group, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, resource, group, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, resource, group)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource, group));
		return attr;
	}

	public Attribute getAttributeById(PerunSession sess, Group group, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		Attribute attr = getAttributesManagerBl().getAttributeById(sess, group, id);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attr, group, null)) throw new PrivilegeException("Principal has no access to get attribute = " + new AttributeDefinition(attr));
		else attr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, group, null));
		return attr;
	}

	public void setAttribute(PerunSession sess, Facility facility, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, facility, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, facility, attribute);
	}

	public void setAttribute(PerunSession sess, Vo vo, Attribute attribute) throws PrivilegeException, InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, vo, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, vo, attribute);
	}

	public void setAttribute(PerunSession sess, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, group, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, group, attribute);
	}

	public void setAttribute(PerunSession sess, Resource resource, Attribute attribute) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, resource, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, resource, attribute);
	}

	public void setAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, resource, member)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, resource, member, attribute);
	}

	@Override
	public void setAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws PrivilegeException, GroupNotExistsException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member, group)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, member, group, attribute);
	}

	public void setAttribute(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, member, attribute);
	}

	public void setAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws PrivilegeException, FacilityNotExistsException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, facility, user)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, facility, user, attribute);
	}

	public void setAttribute(PerunSession sess, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, user, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, user, attribute);
	}

	public void setAttribute(PerunSession sess, Host host, Attribute attribute) throws PrivilegeException, InternalErrorException, HostNotExistsException,AttributeNotExistsException,WrongAttributeValueException,WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, host, null)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		try {
			getAttributesManagerBl().setAttribute(sess, host, attribute);
		} catch (WrongReferenceAttributeValueException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void setAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, resource, group)) throw new PrivilegeException("Principal has no access to set attribute = " + new AttributeDefinition(attribute));
		getAttributesManagerBl().setAttribute(sess, resource, group, attribute);

	}

	public void setAttribute(PerunSession sess, String key, Attribute attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		attribute = this.perunBl.getAttributesManagerBl().convertEmptyStringIntoNullInAttrValue(attribute);
		attribute = this.perunBl.getAttributesManagerBl().convertBooleanFalseIntoNullInAttrValue(attribute);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		Utils.notNull(key, "key");
		if(key.isEmpty()) throw new InternalErrorException("key for entityless attribute can't be empty string");
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("Only perunAdmin can set entityless attributes.");
		getAttributesManagerBl().setAttribute(sess, key, attribute);

	}

	public AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, AttributeExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(attribute, "attributeDefinition");
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("Only perunAdmin can create new Attribute.");
		if (!attribute.getFriendlyName().matches(AttributesManager.ATTRIBUTES_REGEXP)) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong attribute name " + attribute.getFriendlyName() + ", attribute name must match " + AttributesManager.ATTRIBUTES_REGEXP));
		}
		return getAttributesManagerBl().createAttribute(sess, attribute);
	}

	public void deleteAttribute(PerunSession sess, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, RelationExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("Only perunAdmin can delete existing Attribute.");
		getAttributesManagerBl().deleteAttribute(sess, attribute);
	}

	public void deleteAttribute(PerunSession sess, AttributeDefinition attributeDefinition, boolean force) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, RelationExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attributeDefinition);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("Only perunAdmin can delete existing Attribute.");
		getAttributesManagerBl().deleteAttribute(sess, attributeDefinition, force);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, null));
		}
		return attributes;
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, resource, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, member)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
		}
		return attributes;
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, resource, member, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)){
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u, f)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u, f));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group) throws PrivilegeException, InternalErrorException, WrongAttributeAssignmentException, ResourceNotExistsException, MemberNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, WrongAttributeAssignmentException, ResourceNotExistsException, MemberNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member, group, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, Resource resource, User user, Member member) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, facility, resource, user, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, User user) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, FacilityNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, facility, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
		}
		return attributes;
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member) throws PrivilegeException, InternalErrorException, MemberNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
		}
		return attributes;
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, User user) throws PrivilegeException, InternalErrorException, UserNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
		}
		return attributes;
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Host host) throws PrivilegeException, InternalErrorException, HostNotExistsException, ResourceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, host);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, host, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, host, null));
		}
		return attributes;
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(group.getVoId() != resource.getVoId()) {
			throw new GroupResourceMismatchException("Group and resource are not in the same VO.");
		}

		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, resource, group, workWithGroupAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, group));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, null));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not of expected type (group or group_resource).");
			}
		}
		return attributes;
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, resource, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, group));
		}
		return attributes;
	}


	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Group group) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resourceToGetServicesFrom);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getResourceRequiredAttributes(sess, resourceToGetServicesFrom, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, null));
		}
		return attributes;
	}


	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Member member) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, resource, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, resource)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, resource));
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, resource, member, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u, f)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u, f));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, FacilityNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, facility, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, facility, user)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility, user));
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, member, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u, null));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, User user) throws PrivilegeException, InternalErrorException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, user);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
		}
		return attributes;
	}

	public List<AttributeDefinition> getRequiredAttributesDefinition(PerunSession sess, Service service) throws PrivilegeException, InternalErrorException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		//Everyone can access to attrNexts
		return getAttributesManagerBl().getRequiredAttributesDefinition(sess, service);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, facility);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, facility, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility, null));
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, facility);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, facility, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility, null));
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Vo vo) throws PrivilegeException, InternalErrorException, VoNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, vo);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, vo, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, vo, null));
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Facility facility) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		Set<Attribute> attributes = new HashSet<Attribute>();
		// TODO & FIXME: there should be a proper select in BL & Impl
		for (Service s : services) {
			getPerunBl().getServicesManagerBl().checkServiceExists(sess, s);
			attributes.addAll(getAttributesManagerBl().getRequiredAttributes(sess, s, facility));
		}
		List<Attribute> result = new ArrayList<Attribute>();
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, facility, null)) {
				// if allowed to read, add it to result
				attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, facility, null));
				result.add(attrNext);
			}
		}
		return result;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, null));
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Resource resource) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		for (Service s : services) getPerunBl().getServicesManagerBl().checkServiceExists(sess, s);
		List<Attribute> attributes = attributesManagerBl.getRequiredAttributes(sess, services, resource);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, null));
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Host host) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, HostNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, host);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, host, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, host, null));
		}
		return attributes;
	}
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, resource, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, member)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, resource, member, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, member)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)){
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, u, f)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, u, f));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	@Override
	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, ServiceNotExistsException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		for (Member member : members) {
			getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		}
		HashMap<Member, List<Attribute>> result = getAttributesManagerBl().getRequiredAttributes(sess, service, facility, resource, members, workWithUserAttributes);
		for (Member member : result.keySet()) {
			Iterator<Attribute> attrIter = result.get(member).iterator();
			//Choose to which attributes has the principal access
					User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			while (attrIter.hasNext()) {
				Attribute attrNext = attrIter.next();
				if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, member))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
					} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
					} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
					} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
					} else {
					throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
					}
				}
			}
		return result;
		}

	@Override
	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members) throws InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		for (Member member : members) {
			getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		}
		HashMap<Member, List<Attribute>> result = getAttributesManagerBl().getRequiredAttributes(sess, service, resource, members);
		for (Member member : result.keySet()) {
			Iterator<Attribute> attrIter = result.get(member).iterator();
			//Choose to which attributes has the principal access
					while (attrIter.hasNext()) {
				Attribute attrNext = attrIter.next();
				if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, member))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, member));
					} else {
					throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
					}
				}
			}
		return result;
	}

	@Override
	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Resource resource, Service service, List<Member> members) throws InternalErrorException, ServiceNotExistsException, ResourceNotExistsException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		for (Member member : members) {
			getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		}
		HashMap<Member, List<Attribute>> result = getAttributesManagerBl().getRequiredAttributes(sess, resource, service, members);
		for (Member member : result.keySet()) {
			Iterator<Attribute> attrIter = result.get(member).iterator();
			//Choose to which attributes has the principal access
					while (attrIter.hasNext()) {
				Attribute attrNext = attrIter.next();
				if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
					} else {
					throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
					}
				}
			}
		return result;
	}

	@Override
	public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility, List<User> users) throws InternalErrorException, ServiceNotExistsException, FacilityNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		for (User user : users) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		}
		HashMap<User, List<Attribute>> result = getAttributesManagerBl().getRequiredAttributes(sess, service, facility, users);
		for (User user : result.keySet()) {
			Iterator<Attribute> attrIter = result.get(user).iterator();
			//Choose to which attributes has the principal access
					while (attrIter.hasNext()) {
				Attribute attrNext = attrIter.next();
				if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, facility))
						attrIter.remove();
					else
					attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, facility));
					} else {
					throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
					}
				}
			}
		return result;
	}

	@Override
	public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<User> users) throws InternalErrorException, ServiceNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		for (User user : users) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		}
		HashMap<User, List<Attribute>> result = getAttributesManagerBl().getRequiredAttributes(sess, service, users);
		for (User user : result.keySet()) {
			Iterator<Attribute> attrIter = result.get(user).iterator();
			//Choose to which attributes has the principal access
			while (attrIter.hasNext()) {
				Attribute attrNext = attrIter.next();
				if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
					if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null))
						attrIter.remove();
					else
						attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
				} else {
					throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
				}
			}
		}
		return result;
	}
		
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, MemberNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, member, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
		}

		return attributes;
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group, boolean workWithUserAttributes) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException, ResourceNotExistsException, ServiceNotExistsException, MemberNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, member, group, workWithUserAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, group));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, member);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, member, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, member, null));
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, resource, group, workWithGroupAttributes);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, group)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, group));
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_ATTR)){
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, null)) attrIter.remove();
				else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, null));
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, resource, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, resource, group)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, resource, group));
		}
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Group group) throws PrivilegeException, InternalErrorException, ServiceNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getServicesManagerBl().checkServiceExists(sess, service);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		List<Attribute> attributes = getAttributesManagerBl().getRequiredAttributes(sess, service, group);
		Iterator<Attribute> attrIter = attributes.iterator();
		//Choose to which attributes has the principal access
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, group, null)) attrIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, group, null));
		}
		return attributes;
	}


	public Attribute fillAttribute(PerunSession sess, Resource resource, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), resource , null)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, resource, attribute);
		attr.setWritable(true);
		return attr;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access

		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, resource, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), resource , null)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), resource , member)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, resource, member, attribute);
		attr.setWritable(true);
		return attr;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, resource, member, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), resource , member)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, resource, member, attributes, workWithUserAttributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();

		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member, null)) attrIter.remove();
				else attrNext.setWritable(true);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), resource , member)) attrIter.remove();
				else attrNext.setWritable(true);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), u, null)) attrIter.remove();
				else attrNext.setWritable(true);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), u, f)) attrIter.remove();
				else attrNext.setWritable(true);
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}

		return listOfAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), group, member)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, member, group, attribute);
		attr.setWritable(true);
		return attr;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, member, group, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member, group)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		// Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, member, group, attributes, workWithUserAttributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();

		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member, group)) attrIter.remove();
				else attrNext.setWritable(true);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), u, null)) attrIter.remove();
				else attrNext.setWritable(true);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member, null)) attrIter.remove();
				else attrNext.setWritable(true);
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}

		return listOfAttributes;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, facility, resource, user, member, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();

		while (attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), member, null)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_MEMBER_RESOURCE_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), resource, member)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), user, null)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_USER_FACILITY_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), user, facility)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}

		return listOfAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member , null)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, member, attribute);
		attr.setWritable(true);
		return attr;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, member, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext),member , null)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), user , facility)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, facility, user, attribute);
		attr.setWritable(true);
		return attr;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, facility, user, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), facility, user)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), user , null)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, user, attribute);
		attr.setWritable(true);
		return attr;
	}

	public List<Attribute> fillAttributes(PerunSession sess, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, user, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext),user , null)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), resource , group)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, resource, group, attribute);
		attr.setWritable(true);
		return attr;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, resource, group, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext),resource ,group)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access

		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, resource, group, attributes,workWithGroupAttributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();

		while (attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), group, null)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else if (getAttributesManagerBl().isFromNamespace(sess, attrNext, NS_GROUP_RESOURCE_ATTR)) {
				if (!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext), group, resource)) {
					attrIter.remove();
				} else {
					attrNext.setWritable(true);
				}
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}

		return listOfAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, Host host, Attribute attribute) throws PrivilegeException, InternalErrorException, HostNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), host , null)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, host, attribute);
		attr.setWritable(true);
		return attr;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Host host, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, HostNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, host, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext),host ,null)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), group , null)) throw new PrivilegeException("Principal has no access to fill attribute = " + new AttributeDefinition(attribute));

		Attribute attr = getAttributesManagerBl().fillAttribute(sess, group, attribute);
		attr.setWritable(true);
		return attr;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, GroupNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		List<Attribute> listOfAttributes = getAttributesManagerBl().fillAttributes(sess, group, attributes);
		Iterator<Attribute> attrIter = listOfAttributes.iterator();
		while(attrIter.hasNext()) {
			Attribute attrNext = attrIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attrNext),group ,null)) attrIter.remove();
			else attrNext.setWritable(true);
		}

		return listOfAttributes;
	}

	public void checkAttributeValue(PerunSession sess, Facility facility, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), facility , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, facility, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Facility facility, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), facility , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, facility, attributes);
	}

	public void checkAttributeValue(PerunSession sess, Vo vo, Attribute attribute) throws PrivilegeException, InternalErrorException, VoNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), vo , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, vo, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Vo vo, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, VoNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), vo , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, vo, attributes);
	}

	public void checkAttributeValue(PerunSession sess, Resource resource, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), resource , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, resource, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, resource, attributes);
	}

	public void checkAttributeValue(PerunSession sess, Resource resource, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), resource , member)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, resource, member, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , member)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, resource, member, attributes);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Member member, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, GroupNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member, group)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, member, group, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, GroupNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		for(Attribute attribute: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member, group)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));
		}
		getAttributesManagerBl().checkAttributesValue(sess, member, group, attributes);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , group)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().checkAttributesValue(sess, member, group, attributes, workWithUserAttributes);
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , member)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility f = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), u , f)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().checkAttributesValue(sess, resource, member, attributes, workWithUserAttributes);
	}

	public void checkAttributesValue(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , member)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , facility)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().checkAttributesValue(sess, facility, resource, user, member, attributes);
	}

	public void checkAttributeValue(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, InternalErrorException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), member , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, member, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Member member, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, MemberNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), member , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, member, attributes);
	}

	public void checkAttributeValue(PerunSession sess, Facility facility, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), facility , user)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, facility, user, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, FacilityNotExistsException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), facility , user)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, facility, user, attributes);
	}

	public void checkAttributeValue(PerunSession sess, User user, Attribute attribute) throws PrivilegeException, InternalErrorException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), user , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, user, attribute);
	}

	public void checkAttributeValue(PerunSession sess, Host host, Attribute attribute) throws PrivilegeException, InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException,AttributeNotExistsException, HostNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), host , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, host, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Host host, List<Attribute> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, HostNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException{
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), host , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, host, attributes);
	}

	public void checkAttributeValue(PerunSession sess, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), group , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, group, attribute);
	}

	public void checkAttributesValue(PerunSession sess, User user, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, UserNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), user , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, user, attributes);
	}

	public void checkAttributeValue(PerunSession sess, Resource resource, Group group, Attribute attribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, GroupNotExistsException, ResourceNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attribute), resource , group)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().checkAttributeValue(sess, resource, group, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, GroupResourceMismatchException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , group)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().checkAttributesValue(sess, resource, group, attributes);
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttribute) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, GroupResourceMismatchException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		for(Attribute attr: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), resource , group)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			} else if(getAttributesManagerBl().isFromNamespace(sess, attr, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, new AttributeDefinition(attr), group , null)) throw new PrivilegeException("Principal has no access to check attribute = " + new AttributeDefinition(attr));
			}
		}
		getAttributesManagerBl().checkAttributesValue(sess, resource, group, attributes,workWithGroupAttribute);

	}

	public void removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, facility , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, facility, attribute);
	}

	public void removeAttribute(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, AttributeNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, key , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, key, attribute);
	}

	public void removeAttributes(PerunSession sess, Resource resource, Group group, List<? extends AttributeDefinition> attributes, boolean workWithGroupAttributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, GroupNotExistsException, ResourceNotExistsException, GroupResourceMismatchException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().removeAttributes(sess, resource, group, attributes, workWithGroupAttributes);
	}

	public void removeAttributes(PerunSession sess, Facility facility, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, facility , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, facility, attributes);
	}

	public void removeAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<? extends AttributeDefinition> attributes) throws PrivilegeException, ResourceNotExistsException, InternalErrorException, MemberNotExistsException, FacilityNotExistsException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_MEMBER_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, resource , member)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_USER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_USER_FACILITY_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user , facility)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().removeAttributes(sess, facility, resource, user, member, attributes);
	}


	public void removeAttributes(PerunSession sess, Member member, boolean workWithUserAttributes, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_MEMBER_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_USER_ATTR)) {
				User u = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, u , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else {
				throw new WrongAttributeAssignmentException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().removeAttributes(sess, member, workWithUserAttributes, attributes);
	}

	public void removeAllAttributes(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, facility);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, facility);
	}

	public void removeAllAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ResourceNotExistsException, GroupResourceMismatchException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, group, workWithGroupAttributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: allAttributes) {
			if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_GROUP_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else if(getAttributesManagerBl().isFromNamespace(sess, attrDef, NS_GROUP_RESOURCE_ATTR)) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group , resource)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
			} else {
				throw new ConsistencyErrorException("There is some attribute which is not type of any possible choice.");
			}
		}
		getAttributesManagerBl().removeAllAttributes(sess, resource, group, workWithGroupAttributes);
	}

	public void removeAllAttributes(PerunSession sess, Facility facility, boolean removeAlsoUserFacilityAttributes) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getUserFacilityAttributesForAnyUser(sess, facility);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, facility, removeAlsoUserFacilityAttributes);
	}

	public void removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, VoNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, vo , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, vo, attribute);
	}

	public void removeAttributes(PerunSession sess, Vo vo, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, VoNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, vo , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, vo, attributes);
	}

	public void removeAllAttributes(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, vo);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, vo , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, vo);
	}

	public void removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, group , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, group, attribute);
	}

	public void removeAttributes(PerunSession sess, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, group, attributes);
	}

	public void removeAllAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, group);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, group , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, group);
	}

	public void removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, resource , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, resource, attribute);
	}

	public void removeAttributes(PerunSession sess, Resource resource, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, resource , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, resource, attributes);
	}

	public void removeAllAttributes(PerunSession sess, Resource resource) throws InternalErrorException, PrivilegeException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, resource);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, resource);
	}

	public void removeAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, resource , member)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, resource, member, attribute);
	}

	public void removeAttributes(PerunSession sess, Resource resource, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, ResourceNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, resource , member)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, resource, member, attributes);
	}

	public void removeAllAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, MemberNotExistsException, ResourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, member);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource , member)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, resource, member);
	}

	@Override
	public void removeAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member, group)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, member, group, attribute);
	}

	@Override
	public void removeAttributes(PerunSession sess, Member member, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member, group)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, member, group, attributes);
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, MemberNotExistsException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, member, group);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member, group)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, member, group);
	}

	public void removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, member , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, member, attribute);
	}

	public void removeAttributes(PerunSession sess, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, MemberNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, member, attributes);
	}

	public void removeAllAttributes(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, member);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, member , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, member);
	}

	public void removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, facility , user)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, facility, user, attribute);
	}

	public void removeAttributes(PerunSession sess, Facility facility, User user, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user , facility)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, facility, user, attributes);
	}

	public void removeAllAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException, FacilityNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, facility, user);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, facility , user)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, facility, user);
	}

	public void removeAttribute(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, user , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, user, attribute);
	}

	public void removeAttributes(PerunSession sess, User user, List<? extends AttributeDefinition> attributes) throws InternalErrorException, PrivilegeException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, user, attributes);
	}

	public void removeAllAttributes(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, user);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, user , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, user);
	}

	public void removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, HostNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, host , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, host, attribute);

	}

	public void removeAttributes(PerunSession sess, Host host, List<? extends AttributeDefinition> attributes) throws PrivilegeException, InternalErrorException, HostNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, host , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		getAttributesManagerBl().removeAttributes(sess, host, attributes);
	}

	public void removeAllAttributes(PerunSession sess, Host host) throws PrivilegeException, InternalErrorException, HostNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getFacilitiesManagerBl().checkHostExists(sess, host);
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, host);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, host , null)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, host);
	}

	public void removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException, AttributeNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributeExists(sess, attribute);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose to which attributes has the principal access
		if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attribute, resource , group)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attribute));

		getAttributesManagerBl().removeAttribute(sess, resource, group, attribute);
	}

	public void removeAttributes(PerunSession sess, Resource resource, Group group, List<? extends AttributeDefinition> attributes) throws PrivilegeException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, GroupResourceMismatchException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getAttributesManagerBl().checkAttributesExists(sess, attributes);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		//Choose to which attributes has the principal access
		for(AttributeDefinition attrDef: attributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, resource , group)) throw new PrivilegeException("Principal has no access to remove attribute = " + attrDef);
		}
		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		getAttributesManagerBl().removeAttributes(sess, resource, group, attributes);
	}

	public void removeAllAttributes(PerunSession sess, Resource resource, Group group) throws PrivilegeException, WrongAttributeAssignmentException, InternalErrorException, ResourceNotExistsException, GroupNotExistsException, GroupResourceMismatchException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		if(!getPerunBl().getGroupsManagerBl().getVo(sess, group).equals(getPerunBl().getResourcesManagerBl().getVo(sess, resource))) {
			throw new GroupResourceMismatchException("group and resource are not in the same VO");
		}
		//Choose if principal has access to remove all attributes
		List<Attribute> allAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, group);
		for(Attribute attr: allAttributes) {
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attr, resource , group)) throw new PrivilegeException("Principal has no access to remove attribute = " + new AttributeDefinition(attr));
		}
		getAttributesManagerBl().removeAllAttributes(sess, resource, group);
	}

	public boolean isOptAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerBl().isOptAttribute(sess, attribute);
	}

	public boolean isCoreAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerBl().isCoreAttribute(sess, attribute);
	}

	public boolean isCoreManagedAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerBl().isCoreManagedAttribute(sess, attribute);
	}

	public boolean isFromNamespace(PerunSession sess, AttributeDefinition attribute, String namespace) {
		return getAttributesManagerBl().isFromNamespace(sess, attribute, namespace);
	}

	public void checkNamespace(PerunSession sess, AttributeDefinition attribute, String namespace) throws WrongAttributeAssignmentException {
		getAttributesManagerBl().checkNamespace(sess, attribute, namespace);
	}

	public void checkNamespace(PerunSession sess, List<? extends AttributeDefinition> attributes, String namespace) throws WrongAttributeAssignmentException {
		getAttributesManagerBl().checkNamespace(sess, attributes, namespace);
	}

	public String getNamespaceFromAttributeName(String attributeName) throws InternalErrorException {
		Utils.notNull(attributeName, "attributeName");

		return getAttributesManagerBl().getNamespaceFromAttributeName(attributeName);
	}

	public String getFriendlyNameFromAttributeName(String attributeName) throws InternalErrorException {
		Utils.notNull(attributeName, "attributeName");

		return getAttributesManagerBl().getFriendlyNameFromAttributeName(attributeName);
	}

	public List<Attribute> getLogins(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		//Choose if principal has access to remove all attributes
		List<Attribute> logins = getAttributesManagerBl().getLogins(sess, user);

		Iterator<Attribute> loginIter = logins.iterator();
		while(loginIter.hasNext()) {
			Attribute attrNext = loginIter.next();
			if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrNext, user , null)) loginIter.remove();
			else attrNext.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrNext, user, null));
		}

		return getAttributesManagerBl().getLogins(sess, user);
	}

	public AttributeDefinition updateAttributeDefinition(PerunSession perunSession, AttributeDefinition attributeDefinition) throws AttributeNotExistsException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(attributeDefinition, "attributeDefinition");
		getAttributesManagerBl().checkAttributeExists(perunSession, attributeDefinition);

		//Choose if principal has access to update attribute
		if(!AuthzResolver.isAuthorized(perunSession, Role.PERUNADMIN)) throw new PrivilegeException("Only PerunAdmin can update AttributeDefinition");

		return getAttributesManagerBl().updateAttributeDefinition(perunSession, attributeDefinition);
	}

	public void doTheMagic(PerunSession sess, Member member) throws InternalErrorException, WrongAttributeAssignmentException, PrivilegeException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("This operation can do only PerunAdmin.");
		getAttributesManagerBl().doTheMagic(sess, member);
	}

	public void doTheMagic(PerunSession sess, Member member, boolean trueMagic) throws InternalErrorException, PrivilegeException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("This operation can do only PerunAdmin.");
		getAttributesManagerBl().doTheMagic(sess, member, trueMagic);
	}

	public List<AttributeRights> getAttributeRights(PerunSession sess, int attributeId) throws InternalErrorException, PrivilegeException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		// so as we can check, if the attribute exists
		getAttributeDefinitionById(sess, attributeId);

		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("This operation can do only PerunAdmin.");
		return getAttributesManagerBl().getAttributeRights(sess, attributeId);
	}

	public void setAttributeRights(PerunSession sess, List<AttributeRights> rights) throws InternalErrorException, PrivilegeException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		// so as we can check, if the attributes exist
		for (AttributeRights attributeright : rights) {
			getAttributeDefinitionById(sess, attributeright.getAttributeId());
		}
		if(!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) throw new PrivilegeException("This operation can do only PerunAdmin.");
		getAttributesManagerBl().setAttributeRights(sess, rights);
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
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
	 * Sets the attributesManagerBl for this instance.
	 *
	 * @param attributesManagerBl The attributesManagerBl.
	 */
	public void setAttributesManagerBl(AttributesManagerBl attributesManagerBl)
	{
		this.attributesManagerBl = attributesManagerBl;
	}

	/**
	 * Gets the attributesManagerBl for this instance.
	 *
	 * @return The getAttributesManagerBl().
	 */
	public AttributesManagerBl  getAttributesManagerBl() {
		return this.attributesManagerBl;
	}

}
