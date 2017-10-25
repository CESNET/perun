package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.ActionType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Objects;

import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.implApi.modules.attributes.VirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeRights;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.ModuleNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongModuleTypeException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.AttributesManagerImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.AttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.core.api.AttributesManager.*;

/**
 * AttributesManager buisness logic.
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class AttributesManagerBlImpl implements AttributesManagerBl {

	private final static Logger log = LoggerFactory.getLogger(AttributesManagerBlImpl.class);

	//Attributes modules.  name => module
	private Map<String, AttributesModuleImplApi> attributesModulesMap = new ConcurrentHashMap<String, AttributesModuleImplApi>();

	private final AttributesManagerImplApi attributesManagerImpl;
	private PerunBl perunBl;

	//Attributes dependencies. Attr => dependent attributes (and inverse version)
	private Map<AttributeDefinition, Set<AttributeDefinition>> dependencies = new ConcurrentHashMap<AttributeDefinition, Set<AttributeDefinition>>();
	private Map<AttributeDefinition, Set<AttributeDefinition>> strongDependencies = new ConcurrentHashMap<AttributeDefinition, Set<AttributeDefinition>>();
	private Map<AttributeDefinition, Set<AttributeDefinition>> inverseDependencies = new ConcurrentHashMap<AttributeDefinition, Set<AttributeDefinition>>();
	private Map<AttributeDefinition, Set<AttributeDefinition>> inverseStrongDependencies = new ConcurrentHashMap<AttributeDefinition, Set<AttributeDefinition>>();
	private Map<AttributeDefinition, Set<AttributeDefinition>> allDependencies = new ConcurrentHashMap<AttributeDefinition, Set<AttributeDefinition>>();

	private final static int MAX_SIZE_OF_BULK_IN_SQL = 10000;

	/**
	 * Constructor.
	 */
	public AttributesManagerBlImpl(AttributesManagerImplApi attributesManagerImpl) {
		this.attributesManagerImpl = attributesManagerImpl;
	}

	public List<Attribute> getAttributes(PerunSession sess, Facility facility) throws InternalErrorException {
		//get virtual attributes
		List<Attribute> attributes = getAttributesManagerImpl().getVirtualAttributes(sess, facility);
		//filter out virtual attributes with null value
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while(attributeIterator.hasNext()) if(attributeIterator.next().getValue() == null) attributeIterator.remove();

		//adds non-empty non-virtual attributes
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, facility));
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Vo vo) throws InternalErrorException {
		//get virtual attributes
		List<Attribute> attributes = getAttributesManagerImpl().getVirtualAttributes(sess, vo);
		//filter out virtual attributes with null value
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while(attributeIterator.hasNext()) if(attributeIterator.next().getValue() == null) attributeIterator.remove();

		//adds non-empty non-virtual attributes
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, vo));
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Group group) throws InternalErrorException {
		//get virtual attributes
		List<Attribute> attributes = getAttributesManagerImpl().getVirtualAttributes(sess, group);
		//filter out virtual attributes with null value
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while(attributeIterator.hasNext()) if(attributeIterator.next().getValue() == null) attributeIterator.remove();

		//adds non-empty non-virtual attributes
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, group));
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		//get virtual attributes
		List<Attribute> attributes = getVirtualAttributes(sess, resource);
		//filter out virtual attributes with null value
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while(attributeIterator.hasNext()) if(attributeIterator.next().getValue() == null) attributeIterator.remove();

		//adds non-empty non-virtual attributes
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, resource));
		return attributes;
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		return getAttributesManagerImpl().getVirtualAttributes(sess, resource);
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException {
		return getAttributesManagerImpl().getVirtualAttributes(sess, resource, member);
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException, MemberResourceMismatchException {
		return getAttributes(sess, resource, member, false);
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException {
		// get virtual attributes
		List<Attribute> attributes = getAttributesManagerImpl().getVirtualAttributes(sess, member, group);
		// filter out virtual attributes with null value
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while(attributeIterator.hasNext()) if(attributeIterator.next().getValue() == null) attributeIterator.remove();
		// adds non-empty non-virtual attributes
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, member, group));
		return attributes;
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames) throws InternalErrorException {
		if (attrNames.isEmpty()) return new ArrayList<>();
		// adds all attributes which names are in attrNames list (virtual and empty too)
		return  getAttributesManagerImpl().getAttributes(sess, member, group, attrNames);
	}

	@Override
	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group, boolean workWithUserAttributes) throws InternalErrorException {
		// get virtual attributes
		List<Attribute> attributes = getAttributesManagerImpl().getVirtualAttributes(sess, member, group);
		// filter out virtual attributes with null value
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while(attributeIterator.hasNext()) if(attributeIterator.next().getValue() == null) attributeIterator.remove();
		// adds non-empty non-virtual attributes
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, member, group));
		if(workWithUserAttributes) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			// adds virtual attributes too
			attributes.addAll(this.getAttributes(sess, user));
			attributes.addAll(this.getAttributes(sess, member));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames, boolean workWithUserAttributes) throws InternalErrorException {
		if(attrNames.isEmpty()) return this.getAttributes(sess, member, group, workWithUserAttributes);

		//differentiate between user+member and member-group namespace
		List<String> userAndMemberAttributeNames = new ArrayList<>();
		List<String> memberGroupAttributeNames = new ArrayList<>();
		for(String attrName : attrNames) {
			if(attrName.startsWith(AttributesManager.NS_USER_ATTR) || attrName.startsWith(AttributesManager.NS_MEMBER_ATTR)) {
				userAndMemberAttributeNames.add(attrName);
			} else if(attrName.startsWith(AttributesManager.NS_MEMBER_GROUP_ATTR)) {
				memberGroupAttributeNames.add(attrName);
			} else {
				log.error("Attribute defined by " + attrName + " is not in supported namespace. Skip it there!");
			}
		}

		List<Attribute> attributes = new ArrayList<>();
		if(!userAndMemberAttributeNames.isEmpty()) attributes.addAll(this.getAttributes(sess, member, userAndMemberAttributeNames, workWithUserAttributes));
		if(!memberGroupAttributeNames.isEmpty()) attributes.addAll(getAttributesManagerImpl().getAttributes(sess, member, group, memberGroupAttributeNames));

		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member, boolean workWithUserAttributes) throws InternalErrorException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		// get virtual attributes
		List<Attribute> attributes = getAttributesManagerImpl().getAttributes(sess, resource, member);
		List<Attribute> virtualAttributes = getVirtualAttributes(sess, resource, member);
		//remove virtual attributes with null value
		Iterator<Attribute> virtualAttributesIterator = virtualAttributes.iterator();
		while (virtualAttributesIterator.hasNext()) if(virtualAttributesIterator.next().getValue() == null) virtualAttributesIterator.remove();
		// adds non-empty non-virtual attributes
		attributes.addAll(virtualAttributes);

		if(workWithUserAttributes) {
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

			attributes.addAll(getAttributesManagerImpl().getAttributes(sess, facility, user));
			attributes.addAll(getAttributesManagerImpl().getAttributes(sess, user));
			attributes.addAll(getAttributesManagerImpl().getAttributes(sess, member));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Member member, List<String> attrNames, boolean workWithUserAttributes) throws InternalErrorException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		if(attrNames.isEmpty()) return this.getAttributes(sess, resource, member, workWithUserAttributes);

		List<String> userAndMemberAttributeNames = new ArrayList<>();
		List<String> memberResourceAttributeNames = new ArrayList<>();
		List<String> userFacilityAttirbuteNames = new ArrayList<>();
		for(String attributeName: attrNames) {
			if(attributeName.startsWith(AttributesManager.NS_USER_ATTR) || attributeName.startsWith(AttributesManager.NS_MEMBER_ATTR)) {
				userAndMemberAttributeNames.add(attributeName);
			} else if(attributeName.startsWith(AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
				memberResourceAttributeNames.add(attributeName);
			} else if(attributeName.startsWith(AttributesManager.NS_USER_FACILITY_ATTR)) {
				userFacilityAttirbuteNames.add(attributeName);
			} else {
				log.error("Attribute defined by " + attributeName + " is not in supported namespace. Skip it there!");
			}
		}

		List<Attribute> attributes = new ArrayList<>();
		//Call only if list of attributes is not empty
		if(!userAndMemberAttributeNames.isEmpty()) attributes.addAll(this.getAttributes(sess, member, userAndMemberAttributeNames, workWithUserAttributes));
		if(!memberResourceAttributeNames.isEmpty()) attributes.addAll(getAttributesManagerImpl().getAttributes(sess, member, resource, memberResourceAttributeNames));
		if(workWithUserAttributes && !userFacilityAttirbuteNames.isEmpty()) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
			attributes.addAll(getAttributesManagerImpl().getAttributes(sess, user, facility, userFacilityAttirbuteNames));
		}

		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Group group, Resource resource, Member member, List<String> attrNames, boolean workWithUserAttributes) throws InternalErrorException, MemberResourceMismatchException, GroupResourceMismatchException {
		checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		List<Attribute> attributes = getAttributes(sess, resource, member, attrNames, workWithUserAttributes);

		if(attrNames.isEmpty()) {
			attributes.addAll(getAttributes(sess, member, group));
			return attributes;
		}

		List<String> memberGroupAttributeNames = new ArrayList<>();

		for(String attributeName: attrNames) {
			if(attributeName.startsWith(AttributesManager.NS_MEMBER_GROUP_ATTR)) {
				memberGroupAttributeNames.add(attributeName);
			}
		}
		if(!memberGroupAttributeNames.isEmpty()){
			attributes.addAll(getAttributesManagerImpl().getAttributes(sess, member, group, memberGroupAttributeNames));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Member member, boolean workWithUserAttributes) throws InternalErrorException {
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(this.getAttributes(sess, member));
		if(workWithUserAttributes) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

			attributes.addAll(this.getAttributes(sess, user));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Member member) throws InternalErrorException {
		//get virtual attributes
		List<Attribute> attributes = getAttributesManagerImpl().getVirtualAttributes(sess, member);
		//filter out virtual attributes with null value
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while(attributeIterator.hasNext()) if(attributeIterator.next().getValue() == null) attributeIterator.remove();

		//adds non-empty non-virtual attributes
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, member));
		return attributes;
	}

	public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Group group, String startPartOfName) throws InternalErrorException {
		List<Attribute> attrs = getAttributesManagerImpl().getAllAttributesStartWithNameWithoutNullValue(sess, group, startPartOfName);
		Iterator<Attribute> i = attrs.iterator();
		while(i.hasNext()) {
			Attribute attr = i.next();
			if(attr.getValue() == null) i.remove();
		}
		return attrs;
	}

	public List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Resource resource, String startPartOfName) throws InternalErrorException {
		List<Attribute> attrs = getAttributesManagerImpl().getAllAttributesStartWithNameWithoutNullValue(sess, resource, startPartOfName);
		Iterator<Attribute> i = attrs.iterator();
		while(i.hasNext()) {
			Attribute attr = i.next();
			if(attr.getValue() == null) i.remove();
		}
		return attrs;
	}

	public List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames) throws InternalErrorException {
		if(attrNames.isEmpty()) return new ArrayList<Attribute>();

		return getAttributesManagerImpl().getAttributes(sess, member, attrNames);
	}
	public List<Attribute> getAttributes(PerunSession sess, Group group, List<String> attrNames) throws InternalErrorException {
		if(attrNames.isEmpty()) return new ArrayList<Attribute>();
		List<Attribute> attributes = getAttributesManagerImpl().getAttributes(sess, group, attrNames);

		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames, boolean workWithUserAttributes) throws InternalErrorException {
		List<Attribute> attributes = this.getAttributes(sess, member, attrNames);

		if(!workWithUserAttributes) return attributes;
		else {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			attributes.addAll(this.getAttributes(sess, user, attrNames));

			return attributes;
		}
	}

	public List<Attribute> getAttributes(PerunSession sess, Vo vo, List<String> attrNames) throws InternalErrorException {
		if(attrNames.isEmpty()) return new ArrayList<Attribute>();

		return getAttributesManagerImpl().getAttributes(sess, vo, attrNames);
	}

	public List<Attribute> getAttributes(PerunSession sess, UserExtSource ues, List<String> attrNames) throws InternalErrorException {
		if(attrNames.isEmpty()) return new ArrayList<Attribute>();

		return getAttributesManagerImpl().getAttributes(sess, ues, attrNames);
	}

	public List<Attribute> getUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility) throws InternalErrorException {
		List<Attribute> attributes =  getAttributesManagerImpl().getUserFacilityAttributesForAnyUser(sess, facility);
		List<User> facilityUsers = perunBl.getFacilitiesManagerBl().getAllowedUsers(sess, facility);
		List<Attribute> virtualAttributes = new ArrayList<Attribute>();
		for(User user: facilityUsers) {
			virtualAttributes.addAll(getVirtualAttributes(sess, facility, user));
		}

		//remove virtual attributes with null value
		Iterator<Attribute> virtualAttributesIterator = virtualAttributes.iterator();
		while(virtualAttributesIterator.hasNext()) {
			if(virtualAttributesIterator.next().getValue() == null) virtualAttributesIterator.remove();
		}

		attributes.addAll(virtualAttributes);
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		List<Attribute> attributes =  getAttributesManagerImpl().getAttributes(sess, facility, user);
		List<Attribute> virtualAttributes = getVirtualAttributes(sess, facility, user);

		//remove virtual attributes with null value
		Iterator<Attribute> virtualAttributesIterator = virtualAttributes.iterator();
		while(virtualAttributesIterator.hasNext()) {
			if(virtualAttributesIterator.next().getValue() == null) virtualAttributesIterator.remove();
		}

		attributes.addAll(virtualAttributes);
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, String key) throws InternalErrorException {
		return getAttributesManagerImpl().getAttributes(sess, key);
	}

	public List<Attribute> getEntitylessAttributes(PerunSession sess, String attrName) throws  InternalErrorException {
		return getAttributesManagerImpl().getEntitylessAttributes(sess, attrName);
	}

	public List<String> getEntitylessKeys(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException{
		return getAttributesManagerImpl().getEntitylessKeys(sess, attributeDefinition);
	}

	public Attribute getEntitylessAttributeForUpdate(PerunSession sess, String key, String attrName) throws InternalErrorException, AttributeNotExistsException {
		AttributeDefinition attrDef = this.getAttributeDefinition(sess, attrName);
		Attribute attr = new Attribute(attrDef);

		String value = getAttributesManagerImpl().getEntitylessAttrValueForUpdate(sess, attrDef.getId(), key);

		if(value != null) {
			attr.setValue(BeansUtils.stringToAttributeValue(value, attr.getType()));
		}

		return attr;
	}

	public List<Attribute> getAttributesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		if(isCoreAttribute(sess, attributeDefinition) || isVirtAttribute(sess, attributeDefinition) || isCoreManagedAttribute(sess, attributeDefinition)) throw new WrongAttributeAssignmentException(attributeDefinition);

		return getAttributesManagerImpl().getAttributesByAttributeDefinition(sess, attributeDefinition);
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		return getAttributesManagerImpl().getVirtualAttributes(sess, facility, user);
	}

	public List<Attribute> getVirtualAttributes(PerunSession sess, User user) throws InternalErrorException {
		return getAttributesManagerImpl().getVirtualAttributes(sess, user);
	}

	public List<Attribute> getAttributes(PerunSession sess, User user) throws InternalErrorException {
		//get virtual attributes
		List<Attribute> attributes = getAttributesManagerImpl().getVirtualAttributes(sess, user);
		//filter out virtual attributes with null value
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while(attributeIterator.hasNext()) if(attributeIterator.next().getValue() == null) attributeIterator.remove();

		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, user));
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, User user, List<String> attrNames) throws InternalErrorException {
		if(attrNames.isEmpty()) return new ArrayList<Attribute>();

		return getAttributesManagerImpl().getAttributes(sess, user, attrNames);
	}

	public List<Attribute> getAttributes(PerunSession sess, Host host) throws InternalErrorException {
		//get virtual attributes
		List<Attribute> attributes = getAttributesManagerImpl().getVirtualAttributes(sess, host);
		//filter out virtual attributes with null value
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while(attributeIterator.hasNext()) if(attributeIterator.next().getValue() == null) attributeIterator.remove();

		//adds non-empty non-virtual attributes
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, host));
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException, GroupResourceMismatchException {
		return getAttributesManagerImpl().getAttributes(sess, resource, group);
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes) throws InternalErrorException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, resource, group));
		if(workWithGroupAttributes) {
			attributes.addAll(getAttributesManagerImpl().getAttributes(sess, group));
		}
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, List<String> attrNames, boolean workWithGroupAttributes) throws InternalErrorException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		if(attrNames.isEmpty()) return this.getAttributes(sess, resource, group, workWithGroupAttributes);

		List<String> groupAttributeNames = new ArrayList<>();
		List<String> groupResourceAttributeNames = new ArrayList<>();

		for(String attributeName: attrNames) {
			if(attributeName.startsWith(AttributesManager.NS_GROUP_ATTR)) {
				groupAttributeNames.add(attributeName);
			} else if(attributeName.startsWith(AttributesManager.NS_GROUP_RESOURCE_ATTR)) {
				groupResourceAttributeNames.add(attributeName);
			} else {
				log.error("Attribute defined by " + attributeName + " is not in supported namespace. Skip it there!");
			}
		}

		List<Attribute> attributes = new ArrayList<>();
		//Call only if list of attributes is not empty
		if(workWithGroupAttributes && !groupAttributeNames.isEmpty()) attributes.addAll(this.getAttributes(sess, group, attrNames));
		if(!groupResourceAttributeNames.isEmpty()) attributes.addAll(getAttributesManagerImpl().getAttributes(sess, resource, group, attrNames));

		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member) throws InternalErrorException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, resource, member));
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, facility, user));
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, user));
		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, member));
		return attributes;
	}

	public List<Attribute> getAttributes(PerunSession sess, UserExtSource ues) throws InternalErrorException {
		//get virtual attributes
		List<Attribute> attributes = getAttributesManagerImpl().getVirtualAttributes(sess, ues);
		//filter out virtual attributes with null value
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while(attributeIterator.hasNext()) if(attributeIterator.next().getValue() == null) attributeIterator.remove();

		attributes.addAll(getAttributesManagerImpl().getAttributes(sess, ues));
		return attributes;
	}

	public void setAttributes(PerunSession sess, Facility facility, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, facility, attributesToRemove);
		//fist we have to store attributes into DB because checkAttributesValue can be preformed only on stored attributes.
		for(Attribute attribute : attributesToSet) {
			//skip core attributes
			if(!isCoreAttribute(sess, attribute)) {
				if(isVirtAttribute(sess, attribute)) {
					//TODO
					throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute.");
					//getAttributesManagerImpl().setVirtualAttribute(sess, facility, attribute);
				} else {
					setAttributeWithoutCheck(sess, facility, attribute);
				}
			}
		}
		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, facility, attributesToSet);
		log.debug("IMPORTANT: ENTERING CHECK ATTRIBUTES DEPENDENCIES");
		checkAttributesDependencies(sess, facility, null, attributesToSet);
		log.debug("IMPORTANT: EXITING CHECK ATTRIBUTES DEPENDENCIES");
	}

	public void setAttributes(PerunSession sess, Vo vo, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, vo, attributesToRemove);
		//fist we have to store attributes into DB because checkAttributesValue can be preformed only on stored attributes.
		for(Attribute attribute : attributesToSet) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if(isVirtAttribute(sess, attribute)) {
					//TODO
					throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute.");
					//getAttributesManagerImpl().setVirtualAttribute(sess, vo, attribute);
				} else {
					setAttributeWithoutCheck(sess, vo, attribute);
				}
			}
		}
		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, vo, attributesToSet);
		this.checkAttributesDependencies(sess, vo, null, attributesToSet);
	}

	public void setAttributes(PerunSession sess, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, group, attributesToRemove);
		//fist we have to store attributes into DB because checkAttributesValue can be preformed only on stored attributes.
		for(Attribute attribute : attributesToSet) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if(isVirtAttribute(sess, attribute)) {
					//TODO
					throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute.");
				} else {
					setAttributeWithoutCheck(sess, group, attribute);
				}
			}
		}
		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, group, attributesToSet);
		this.checkAttributesDependencies(sess, group, null, attributesToSet);
	}

	public void setAttributes(PerunSession sess, Resource resource, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, resource, attributesToRemove);
		//fist we have to store attributes into DB because checkAttributesValue can be preformed only on stored attributes.
		for(Attribute attribute: attributesToSet) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				setAttributeWithoutCheck(sess, resource, attribute);
			}
		}
		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, resource, attributesToSet);
		this.checkAttributesDependencies(sess, resource, null, attributesToSet);
	}

	public void setAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		setAttributes(sess, resource, member, attributes, false);
	}

	@Override
	public void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserNotExistsException {
		setAttributes(sess, member, group, attributes, false);
	}

	@Override
	public void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, UserNotExistsException {
		// classification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, member, group, attributesToRemove, workWithUserAttributes);
		// fist we have to store attributes into DB because checkAttributesValue can be preformed only on stored attributes.
		if (!workWithUserAttributes) {
			for (Attribute attribute : attributesToSet) {
				//skip core attributes
				if (!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
					setAttributeWithoutCheck(sess, member, group, attribute, false);
				}
			}
		} else {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

			for(Attribute attribute : attributesToSet) {
				// skip core attributes
				if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
					// this can handle member-group, member and user attributes too
					setAttributeWithoutCheck(sess, member, group, attribute, true);
				}
			}
		}

		// if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, member, group, attributesToSet, workWithUserAttributes);
		this.checkAttributesDependencies(sess, member, group, attributesToSet, workWithUserAttributes);
	}

	public void setAttributes(PerunSession sess, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, member, attributesToRemove);
		User user;
		if(!workWithUserAttributes) {
			long timer = Utils.startTimer();
			for(Attribute attribute : attributesToSet) {
				//skip core attributes
				if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
					setAttributeWithoutCheck(sess, member, attribute);
				}
			}
			log.debug("addMember timer: setAttributes (for(Attribute attribute : attributes)) [{}].", Utils.getRunningTime(timer));
		}else{
			long timer = Utils.startTimer();
			user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			log.debug("addMember timer: getMember and User [{}].", Utils.getRunningTime(timer));
			for(Attribute attribute : attributesToSet) {
				boolean changed = false;
				//skip core attributes
				if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
					if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
						timer = Utils.startTimer();
						changed = setAttributeWithoutCheck(sess, user, attribute);
						if(changed) {
							log.debug("addMember timer: setAttribute u [{}] [{}].", attribute, Utils.getRunningTime(timer));
						}
					} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
						timer = Utils.startTimer();
						changed = setAttributeWithoutCheck(sess, member, attribute);
						if(changed) {
							log.debug("addMember timer: setAttribute m [{}] [{}].", attribute, Utils.getRunningTime(timer));
						}
					} else {
						throw new WrongAttributeAssignmentException(attribute);
					}
				}
			}
		}
		checkAttributesValue(sess, member, attributesToSet, workWithUserAttributes);
		this.checkAttributesDependencies(sess, member, attributesToSet, workWithUserAttributes);
	}


	public void setAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, resource, member, attributesToRemove, workWithUserAttributes);
		//fist we have to store attributes into DB because checkAttributesValue can be preformed only on stored attributes.
		if (!workWithUserAttributes) {
			long timer = Utils.startTimer();
			for (Attribute attribute : attributesToSet) {
				//skip core attributes
				if (!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
					setAttributeWithoutCheck(sess, resource, member, attribute, false);
				}
			}
			log.debug("addMember timer: setAttributes (for(Attribute attribute : attributes)) [{}].", Utils.getRunningTime(timer));
		} else {
			long timer = Utils.startTimer();
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			log.debug("addMember timer: getFacility and User [{}].", Utils.getRunningTime(timer));

			for(Attribute attribute : attributesToSet) {
				boolean changed = false;
				//skip core attributes
				if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {

					if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
						timer = Utils.startTimer();
						changed = setAttributeWithoutCheck(sess, resource, member, attribute, false);
						if(changed) {
							log.debug("addMember timer: setAttribute rm [{}] [{}].", attribute, Utils.getRunningTime(timer));
						}
					} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
						timer = Utils.startTimer();
						changed = setAttributeWithoutCheck(sess, facility, user, attribute);
						if (changed) {
							log.debug("addMember timer: setAttribute uf [{}] [{}].", attribute, Utils.getRunningTime(timer));
						}
					} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
						timer = Utils.startTimer();
						changed = setAttributeWithoutCheck(sess, user, attribute);
						if (changed) {
							log.debug("addMember timer: setAttribute u [{}] [{}].", attribute, Utils.getRunningTime(timer));
						}
					} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
						timer = Utils.startTimer();
						changed = setAttributeWithoutCheck(sess, member, attribute);
						if (changed) {
							log.debug("addMember timer: setAttribute m [{}] [{}].", attribute, Utils.getRunningTime(timer));
						}
					} else {
						throw new WrongAttributeAssignmentException(attribute);
					}
				}
			}
		}

		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, resource, member, attributesToSet, workWithUserAttributes);
		this.checkAttributesDependencies(sess, resource, member, attributesToSet, workWithUserAttributes);
	}

	public void setAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, facility, resource, user, member, attributesToRemove);
		for(Attribute attribute : attributesToSet) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {

				if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
					setAttributeWithoutCheck(sess, resource, member, attribute, false);
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
					setAttributeWithoutCheck(sess, facility, user, attribute);
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					setAttributeWithoutCheck(sess, user, attribute);
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					setAttributeWithoutCheck(sess, member, attribute);
				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			}
		}

		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, facility, resource, user, member, attributesToSet);
		this.checkAttributesDependencies(sess, resource, member, user, facility, attributesToSet);
	}

	public void setAttributes(PerunSession sess, Facility facility, Resource resource, Group group, User user, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException, MemberResourceMismatchException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, facility, resource, group, user, member, attributesToRemove);
		for(Attribute attribute : attributesToSet) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {

				if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
					setAttributeWithoutCheck(sess, resource, member, attribute, false);
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
					setAttributeWithoutCheck(sess, facility, user, attribute);
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					setAttributeWithoutCheck(sess, user, attribute);
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					setAttributeWithoutCheck(sess, member, attribute);
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_GROUP_ATTR)) {
					setAttributeWithoutCheck(sess, member, group, attribute, false);
				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			}
		}

		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, facility, resource, group, user, member, attributesToSet);
		this.checkAttributesDependencies(sess, resource, group, member, user, facility, attributesToSet);
	}

	public void setAttributes(PerunSession sess, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, member, attributesToRemove);
		//fist we have to store attributes into DB because checkAttributesValue can be preformed only on stored attributes.
		for(Attribute attribute : attributesToSet) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if(isVirtAttribute(sess, attribute)) {
					//TODO
					throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute.");
				} else {
					setAttributeWithoutCheck(sess, member, attribute);
				}
			}
		}

		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, member, attributesToSet);
		this.checkAttributesDependencies(sess, member, null, attributesToSet);
	}

	public void setAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, facility, user, attributesToRemove);
		//fist we have to store attributes into DB because checkAttributesValue can be preformed only on stored attributes.
		for(Attribute attribute : attributesToSet) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				setAttributeWithoutCheck(sess, facility, user, attribute);
			}
		}

		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, facility, user, attributesToSet);
		this.checkAttributesDependencies(sess, facility, user, attributesToSet);
	}

	public void setAttributes(PerunSession sess, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, user, attributesToRemove);
		//fist we have to store attributes into DB because checkAttributesValue can be preformed only on stored attributes.
		for(Attribute attribute : attributesToSet) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if(isVirtAttribute(sess, attribute)) {
					//TODO
					throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute.");
				} else {
					setAttributeWithoutCheck(sess, user, attribute);
				}
			}
		}

		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, user, attributesToSet);
		this.checkAttributesDependencies(sess, user, null, attributesToSet);
	}

	public void setAttributes(PerunSession sess, Host host, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, host, attributesToRemove);
		for(Attribute attribute : attributesToSet) {
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if(isVirtAttribute(sess, attribute)) {
					//TODO
					throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute.");
				} else {
					setAttributeWithoutCheck(sess, host, attribute);
				}
			}
		}

		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, host, attributesToSet);
		this.checkAttributesDependencies(sess, host, null, attributesToSet);
	}

	public void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, resource, group, attributesToRemove);
		for(Attribute attribute : attributesToSet) {
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				setAttributeWithoutCheck(sess, resource, group, attribute);
			}
		}
		checkAttributesValue(sess, resource, group, attributesToSet);
		this.checkAttributesDependencies(sess, resource, group, attributesToSet);
	}

	public void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for (Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, resource, group, attributesToRemove, workWithGroupAttributes);
		if (!workWithGroupAttributes) {
			setAttributes(sess, resource, group, attributes);
		} else {
			for (Attribute attribute : attributesToSet) {
				//skip core attributes
				if (!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {

					if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR)) {
						setAttributeWithoutCheck(sess, resource, group, attribute);
					} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_GROUP_ATTR)) {
						setAttributeWithoutCheck(sess, group, attribute);
					} else {
						throw new WrongAttributeAssignmentException(attribute);
					}
				}
			}
			checkAttributesValue(sess, resource, group, attributesToSet, workWithGroupAttributes);
			this.checkAttributesDependencies(sess, resource, group, attributesToSet, workWithGroupAttributes);
		}
	}

	public void setAttributes(PerunSession sess, UserExtSource ues, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		// clasification of attributes to attributes to remove and attributes to set
		List<Attribute> attributesToRemove = new ArrayList<Attribute>();
		List<Attribute> attributesToSet = new ArrayList<Attribute>();
		convertEmptyAttrValueToNull(attributes);
		for(Attribute attribute : attributes) {
			if (attribute.getValue() == null) {
				attributesToRemove.add(attribute);
			} else {
				attributesToSet.add(attribute);
			}
		}
		removeAttributes(sess, ues, attributesToRemove);
		//fist we have to store attributes into DB because checkAttributesValue can be preformed only on stored attributes.
		for(Attribute attribute : attributesToSet) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if(isVirtAttribute(sess, attribute)) {
					//TODO
					throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute.");
				} else {
					setAttributeWithoutCheck(sess, ues, attribute);
				}
			}
		}

		//if checkAttributesValue fails it causes rollback so no attribute will be stored
		checkAttributesValue(sess, ues, attributesToSet);
		this.checkAttributesDependencies(sess, ues, null, attributesToSet);
	}

	public void setCoreAttributeWithoutCheck(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {

		if(!attribute.getName().equals("urn:perun:member:attribute-def:core:status")) {
			throw new InternalErrorException("We can set only urn:perun:member:attribute-def:core:status from member's core attributes. Others are not permitted.");
		}

		//defensive construction
		Member storedMember;
		try {
			storedMember = getPerunBl().getMembersManagerBl().getMemberById(sess, member.getId());
		} catch(MemberNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
		if(!member.equals(storedMember)) throw new InternalErrorException("You wan't to store core attribute for member which is not equals to member from DB (with same Id)");

		String methodName = "set" + Character.toUpperCase(attribute.getFriendlyName().charAt(0)) + attribute.getFriendlyName().substring(1);
		Method method;
		try {
			method = member.getClass().getMethod(methodName, Class.forName(attribute.getType()));
		} catch(NoSuchMethodException ex) {
			throw new InternalErrorException("Bad core attribute definition. " + attribute , ex);
		} catch(ClassNotFoundException ex) {
			throw new InternalErrorException("Bad core attribute type. " + attribute , ex);
		}

		try {
			method.invoke(member, attribute.getValue());
		} catch(IllegalAccessException ex) {
			throw new InternalErrorException(ex);
		} catch(InvocationTargetException ex) {
			throw new WrongAttributeValueException(ex);
		} catch(IllegalArgumentException ex) {
			throw new WrongAttributeValueException(attribute, "Probably bad type of value", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		member = getPerunBl().getMembersManagerBl().updateMember(sess, member);
	}

	public List<String> getAllSimilarAttributeNames(PerunSession sess, String startingPartOfAttributeName) throws InternalErrorException {
		return getAttributesManagerImpl().getAllSimilarAttributeNames(sess, startingPartOfAttributeName);
	}

	public Attribute getAttribute(PerunSession sess, Facility facility, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		//check namespace
		if(!attributeName.startsWith(AttributesManager.NS_FACILITY_ATTR)) throw new WrongAttributeAssignmentException("Attribute name=" + attributeName);

		return getAttributesManagerImpl().getAttribute(sess, facility, attributeName);
	}

	public Attribute getAttribute(PerunSession sess, Vo vo, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		//check namespace
		if(!attributeName.startsWith(AttributesManager.NS_VO_ATTR)) throw new WrongAttributeAssignmentException("Attribute name=" + attributeName);

		return getAttributesManagerImpl().getAttribute(sess, vo, attributeName);
	}

	public Attribute getAttribute(PerunSession sess, Group group, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		//check namespace
		String namespace = getNamespaceFromAttributeName(attributeName);
		if(!namespace.startsWith(AttributesManager.NS_GROUP_ATTR)) throw new WrongAttributeAssignmentException("Attribute name=" + attributeName);

		return getAttributesManagerImpl().getAttribute(sess, group, attributeName);
	}

	public Attribute getAttribute(PerunSession sess, Resource resource, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		//check namespace
		if(!attributeName.startsWith(AttributesManager.NS_RESOURCE_ATTR)) throw new WrongAttributeAssignmentException("Attribute name=" + attributeName);

		return getAttributesManagerImpl().getAttribute(sess, resource, attributeName);
	}

	public Attribute getAttribute(PerunSession sess, Resource resource, Member member, String attributeName) throws InternalErrorException, MemberResourceMismatchException, WrongAttributeAssignmentException, AttributeNotExistsException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		//check namespace
		if(!attributeName.startsWith(AttributesManager.NS_MEMBER_RESOURCE_ATTR)) throw new WrongAttributeAssignmentException("Attribute name=" + attributeName);

		return getAttributesManagerImpl().getAttribute(sess, resource, member, attributeName);
	}

	@Override
	public Attribute getAttribute(PerunSession sess, Member member, Group group, String attributeName) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		// check namespace
		if(!attributeName.startsWith(AttributesManager.NS_MEMBER_GROUP_ATTR)) throw new WrongAttributeAssignmentException("Attribute name=" + attributeName);

		return getAttributesManagerImpl().getAttribute(sess, member, group, attributeName);
	}

	public Attribute getAttribute(PerunSession sess, Member member, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeAssignmentException, AttributeNotExistsException {
		//check namespace
		if(!attributeName.startsWith(AttributesManager.NS_MEMBER_ATTR)) throw new WrongAttributeAssignmentException("Attribute name=" + attributeName);

		return getAttributesManagerImpl().getAttribute(sess, member, attributeName);
	}

	public Attribute getAttribute(PerunSession sess, Facility facility, User user, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		//check namespace
		if(!attributeName.startsWith(AttributesManager.NS_USER_FACILITY_ATTR)) throw new WrongAttributeAssignmentException("Attribute name=" + attributeName);

		return getAttributesManagerImpl().getAttribute(sess, facility, user, attributeName);

	}

	public Attribute getAttribute(PerunSession sess, User user, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		//check namespace
		if(!attributeName.startsWith(AttributesManager.NS_USER_ATTR)) throw new WrongAttributeAssignmentException("Attribute name=" + attributeName);

		return getAttributesManagerImpl().getAttribute(sess, user, attributeName);
	}


	public Attribute getAttribute(PerunSession sess, Host host, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		if(!attributeName.startsWith(AttributesManager.NS_HOST_ATTR)) throw new WrongAttributeAssignmentException("Attribute name= " + attributeName);
		return getAttributesManagerImpl().getAttribute(sess, host, attributeName);

	}

	public Attribute getAttribute(PerunSession sess, Resource resource, Group group, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		if(!attributeName.startsWith(AttributesManager.NS_GROUP_RESOURCE_ATTR)) throw new WrongAttributeAssignmentException("Attribute name= " + attributeName);
		return getAttributesManagerImpl().getAttribute(sess, resource, group, attributeName);
	}

	public Attribute getAttribute(PerunSession sess, String key, String attributeName) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		if(!attributeName.startsWith(AttributesManager.NS_ENTITYLESS_ATTR)) throw new WrongAttributeAssignmentException("Attribute name= " + attributeName);
		return getAttributesManagerImpl().getAttribute(sess, key, attributeName);
	}

	public Attribute getAttribute(PerunSession sess, UserExtSource ues, String attributeName) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		//check namespace
		if(!attributeName.startsWith(AttributesManager.NS_UES_ATTR)) throw new WrongAttributeAssignmentException("Attribute name=" + attributeName);

		return getAttributesManagerImpl().getAttribute(sess, ues, attributeName);
	}

	public AttributeDefinition getAttributeDefinition(PerunSession sess, String attributeName) throws InternalErrorException, AttributeNotExistsException {
		return getAttributesManagerImpl().getAttributeDefinition(sess, attributeName);
	}

	public List<AttributeDefinition> getAttributesDefinitionWithRights(PerunSession sess, List<PerunBean> entities) throws InternalErrorException, AttributeNotExistsException {
		List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

		//if there is no entities, so no attribute definition will be returned => empty array list of ADs
		if(entities == null || entities.isEmpty()) return attributeDefinitions;
			//or fill list by all attributeDefinitions
		else attributeDefinitions = this.getAttributesDefinition(sess);

		//Prepare possible objects
		User user = null;
		Member member = null;
		Vo vo = null;
		Resource resource = null;
		Group group = null;
		Facility facility = null;
		Host host = null;
		UserExtSource ues = null;

		//Iterate through all entities and fill those which are in list of entities
		for(PerunBean entity: entities) {
			if(entity instanceof User) user = (User) entity;
			else if(entity instanceof Member) member = (Member) entity;
			else if(entity instanceof Vo) vo = (Vo) entity;
			else if(entity instanceof Resource) resource = (Resource) entity;
			else if(entity instanceof Group) group = (Group) entity;
			else if(entity instanceof Facility) facility = (Facility) entity;
			else if(entity instanceof Host) host = (Host) entity;
			else if(entity instanceof UserExtSource) ues = (UserExtSource) entity;
				//Else skip not identified entity (log it)
			else log.debug("In method GetAttributesDefinitionWithRights there are entity which is not identified correctly and will be skipped: " + entity);
		}

		//Iterate through all attributesDefinitions and remove those which are not in the possible namespace or user in session has no rights to read them
		Iterator<AttributeDefinition> iterator = attributeDefinitions.iterator();
		while(iterator.hasNext()) {
			AttributeDefinition attrDef = iterator.next();

			if(this.isFromNamespace(sess, attrDef, NS_USER_FACILITY_ATTR) && user != null && facility != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, user, facility)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user, facility));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_MEMBER_RESOURCE_ATTR) && member != null && resource != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, member, resource)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member, resource));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_MEMBER_GROUP_ATTR) && member != null && group != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, member, group)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member, group));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_GROUP_RESOURCE_ATTR) && group != null && resource != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, group, resource)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group, resource));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_USER_ATTR) && user != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, user, null)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, user, null));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_MEMBER_ATTR) && member != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, member, null)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, member, null));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_VO_ATTR) && vo != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, vo, null)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, vo, null));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_RESOURCE_ATTR) && resource != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, resource, null)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, resource, null));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_GROUP_ATTR) && group != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, group, null)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, group, null));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_FACILITY_ATTR) && facility != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, facility, null)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, facility, null));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_HOST_ATTR) && host != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, host, null)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, host, null));
				}
			} else if(this.isFromNamespace(sess, attrDef, NS_UES_ATTR) && ues != null) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attrDef, ues, null)) {
					iterator.remove();
				} else {
					attrDef.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, attrDef, ues, null));
				}
			} else {
				//if there is another namespace or if there are no entities (which are needed for the namespace) remove this attributeDefinition
				iterator.remove();
			}
		}

		return attributeDefinitions;
	}

	public List<AttributeDefinition> getAttributesDefinition(PerunSession sess) throws InternalErrorException {
		return getAttributesManagerImpl().getAttributesDefinition(sess);
	}

	public List<AttributeDefinition> getAttributesDefinition(PerunSession sess, List<String> listOfAttributesNames) throws AttributeNotExistsException, InternalErrorException {
		List<AttributeDefinition> listOfAttributeDefinitions = new ArrayList<AttributeDefinition>();
		for(String name: listOfAttributesNames) {
			listOfAttributeDefinitions.add(this.getAttributeDefinition(sess, name));
		}
		return listOfAttributeDefinitions;
	}

	public AttributeDefinition getAttributeDefinitionById(PerunSession sess, int id) throws InternalErrorException, AttributeNotExistsException {
		return getAttributesManagerImpl().getAttributeDefinitionById(sess, id);
	}

	public List<AttributeDefinition> getAttributesDefinitionByNamespace(PerunSession sess, String namespace) throws InternalErrorException {
		return getAttributesManagerImpl().getAttributesDefinitionByNamespace(sess, namespace);
	}

	public Attribute getAttributeById(PerunSession sess, Facility facility, int id) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, facility, id);
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_FACILITY_ATTR);
		return attribute;
	}

	public Attribute getAttributeById(PerunSession sess, Vo vo, int id) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, vo, id);
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_VO_ATTR);
		return attribute;
	}

	public Attribute getAttributeById(PerunSession sess, Resource resource, int id) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, resource, id);
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_RESOURCE_ATTR);
		return attribute;
	}

	public Attribute getAttributeById(PerunSession sess, Resource resource, Member member, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		AttributeDefinition attributeDefinition = getAttributeDefinitionById(sess, id);

		if(getAttributesManagerImpl().isFromNamespace(sess, attributeDefinition, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
			Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, resource, member, id);
			getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_RESOURCE_ATTR);
			return attribute;
		} else if(getAttributesManagerImpl().isFromNamespace(sess, attributeDefinition, AttributesManager.NS_USER_FACILITY_ATTR)) {
			//user-facility attribues
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);

			return getAttributesManagerImpl().getAttributeById(sess, facility, user, id);

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attributeDefinition, AttributesManager.NS_USER_ATTR)) {
			//user and user core attributes
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

			return getAttributesManagerImpl().getAttributeById(sess, user, id);
		} else {
			throw new WrongAttributeAssignmentException(attributeDefinition);
		}
	}

	@Override
	public Attribute getAttributeById(PerunSession sess, Member member, Group group, int id) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		AttributeDefinition attributeDefinition = getAttributeDefinitionById(sess, id);

		if(getAttributesManagerImpl().isFromNamespace(sess, attributeDefinition, AttributesManager.NS_MEMBER_GROUP_ATTR)) {
			Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, member, group, id);
			getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_GROUP_ATTR);
			return attribute;
		} else if(getAttributesManagerImpl().isFromNamespace(sess, attributeDefinition, AttributesManager.NS_MEMBER_ATTR)) {
			return getAttributesManagerImpl().getAttributeById(sess, member, id);
		} else if(getAttributesManagerImpl().isFromNamespace(sess, attributeDefinition, AttributesManager.NS_USER_ATTR)) {
			//user and user core attributes
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			return getAttributesManagerImpl().getAttributeById(sess, user, id);
		} else {
			throw new WrongAttributeAssignmentException(attributeDefinition);
		}
	}

	public Attribute getAttributeById(PerunSession sess, Member member, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, member, id);
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_ATTR);
		return attribute;
	}

	public Attribute getAttributeById(PerunSession sess, Facility facility, User user, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, facility, user, id);
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_USER_FACILITY_ATTR);
		return attribute;
	}

	public Attribute getAttributeById(PerunSession sess, User user, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, user, id);
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_USER_ATTR);
		return attribute;
	}

	public Attribute getAttributeById(PerunSession sess, Host host, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, host, id);
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_HOST_ATTR);
		return attribute;
	}

	public Attribute getAttributeById(PerunSession sess, Resource resource, Group group, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, resource, group, id);
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR);
		return attribute;
	}

	public Attribute getAttributeById(PerunSession sess, Group group, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, group, id);
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_ATTR);
		return attribute;
	}

	public Attribute getAttributeById(PerunSession sess, UserExtSource ues, int id) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Attribute attribute = getAttributesManagerImpl().getAttributeById(sess, ues, id);
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_UES_ATTR);
		return attribute;
	}

	public void setRequiredAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeValueException, MemberResourceMismatchException {
		//fill attributes and get back only those which were really filled with new value
		List<Attribute> filledAttributes = this.fillAttributes(sess, facility, resource, user, member, attributes, true);

		//Remove all filledAttributes from all attributes list
		Iterator<Attribute> iterAttr = attributes.iterator();
		while(iterAttr.hasNext()) {
			Attribute attributeFromAllAttrs = iterAttr.next();
			for(Attribute attributeFromFillAttrs: filledAttributes) {
				if(attributeFromAllAttrs.getName().equals(attributeFromFillAttrs.getName())) {
					iterAttr.remove();
					break;
				}
			}
		}

		//Set all filledAttributes withoutCheck
		for(Attribute attribute : filledAttributes) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
					this.setAttributeWithoutCheck(sess, resource, member, attribute, false);
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
					this.setAttributeWithoutCheck(sess, facility, user, attribute);
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					this.setAttributeWithoutCheck(sess, user, attribute);
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					this.setAttributeWithoutCheck(sess, member, attribute);
				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			}
		}

		//Join all attributes and filled attributes together
		attributes.addAll(filledAttributes);

		//refresh all virtual attributes with new value
		for(Attribute attr: attributes) {
			if(this.isVirtAttribute(sess, attr)) {
				if (getAttributesManagerImpl().isFromNamespace(sess, attr, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
					attr.setValue(this.getAttribute(sess, resource, member, attr.getName()).getValue());
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attr, AttributesManager.NS_USER_FACILITY_ATTR)) {
					attr.setValue(this.getAttribute(sess, facility, user, attr.getName()).getValue());
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attr, AttributesManager.NS_USER_ATTR)) {
					attr.setValue(this.getAttribute(sess, user, attr.getName()).getValue());
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attr, AttributesManager.NS_MEMBER_ATTR)) {
					attr.setValue(this.getAttribute(sess, member, attr.getName()).getValue());
				} else {
					throw new WrongAttributeAssignmentException(attr);
				}
			}
		}

		//Check all attributes
		checkAttributesValue(sess, facility, resource, user, member, attributes);

		//Check all attributes dependencies
		this.checkAttributesDependencies(sess, resource, member, user, facility, attributes);
	}

	public void setRequiredAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, WrongAttributeValueException, AttributeNotExistsException, MemberResourceMismatchException {
		//get all attributes (for member, resource, facility and user) with values
		List<Attribute> attributes = this.getResourceRequiredAttributes(sess, resource, facility, resource, user, member);

		this.setRequiredAttributes(sess, facility, resource, user, member, attributes);
	}

	public void setRequiredAttributes(PerunSession sess, Service service, Facility facility, Resource resource, User user, Member member) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException, WrongAttributeValueException, MemberResourceMismatchException {
		//get all attributes (for member, resource, facility, user and service) with values
		List<Attribute> attributes = this.getRequiredAttributes(sess, service, facility, resource, user, member);

		this.setRequiredAttributes(sess, facility, resource, user, member, attributes);
	}

	public void setAttribute(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, facility, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, facility, attribute)) {
			checkAttributeValue(sess, facility, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(facility, null, attribute));
		}
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_FACILITY_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(isVirtAttribute(sess, attribute)) {
			//TODO
			throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute.");
			//getAttributesManagerImpl().setVirtualAttribute(sess, facility, attribute);
		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, facility, attribute);
		}
		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {}.", attribute, facility);
			getAttributesManagerImpl().changedAttributeHook(sess, facility, attribute);
		}
		return changed;
	}

	public void setAttribute(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, vo, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, vo, attribute)) {
			checkAttributeValue(sess, vo, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(vo, null, attribute));
		}
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_VO_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(isVirtAttribute(sess, attribute)) {
			//TODO
			throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute.");
		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, vo, attribute);
		}
		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {}.", attribute, vo);
			getAttributesManagerImpl().changedAttributeHook(sess, vo, attribute);
		}
		return changed;
	}

	public void setAttribute(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, group, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, group, attribute)) {
			checkAttributeValue(sess, group, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(group, null, attribute));
		}
	}

	public void setAttribute(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, resource, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, resource, attribute)) {
			checkAttributeValue(sess, resource, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(resource, null, attribute));
		}
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_RESOURCE_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(isVirtAttribute(sess, attribute)) {
			try {
				changed = getAttributesManagerImpl().setVirtualAttribute(sess, resource, attribute);
			} catch (WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException(ex);
			}
		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, resource, attribute);
		}

		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {}.", attribute, resource);
			getAttributesManagerImpl().changedAttributeHook(sess, resource, attribute);
		}

		return changed;
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(isVirtAttribute(sess, attribute)) {
			//TODO
			throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute instead.");
		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, group, attribute);
		}
		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {}.", attribute, group);
			getAttributesManagerImpl().changedAttributeHook(sess, group, attribute);
		}

		return changed;
	}

	public void setAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, resource, member, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, resource, member, attribute, false)) {
			checkAttributeValue(sess, resource, member, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(resource, member, attribute));
		}
	}

	@Override
	public void setAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, member, group, attribute);
			return;
		}
		if (setAttributeWithoutCheck(sess, member, group, attribute, false)) {
			checkAttributeValue(sess, member, group, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(member, group, attribute));
		}
	}

	public void setAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, resource, member, attribute);
			return;
		}
		if(!workWithUserAttributes) {
			if(setAttributeWithoutCheck(sess, resource, member, attribute, false)) {
				this.checkAttributeDependencies(sess, new RichAttribute<>(resource, member, attribute));
				checkAttributeValue(sess, resource, member, attribute);
			}
		} else {
			if(setAttributeWithoutCheck(sess, resource, member, attribute, true)) {
				List<Attribute> listOfAttributes = new ArrayList<Attribute>();
				listOfAttributes.add(attribute);
				checkAttributesValue(sess, resource, member, listOfAttributes, workWithUserAttributes);
				this.checkAttributesDependencies(sess, resource, member, listOfAttributes, workWithUserAttributes);
			}
		}
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(getAttributesManagerImpl().isVirtAttribute(sess, attribute)) {
			//TODO better exception here
			try {
				changed = getAttributesManagerImpl().setVirtualAttribute(sess, facility, user, attribute);
			} catch (WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException(ex);
			}

			//FIXME update changed variable
		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, facility, user, attribute);
		}

		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {} and {}.", attribute, facility, user);
			getAttributesManagerImpl().changedAttributeHook(sess, facility, user, attribute);
		}

		return changed;
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, Resource resource, Member member, Attribute attribute, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
			//NS_MEMBER_RESOURCE_ATTR
			if(getAttributesManagerImpl().isVirtAttribute(sess, attribute)) {
				//TODO
				throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute instead.");
			} else {
				changed = getAttributesManagerImpl().setAttribute(sess, resource, member, attribute);
				if(changed) {
					getPerunBl().getAuditer().log(sess, "{} set for {} and {}.", attribute, resource, member);
					getAttributesManagerImpl().changedAttributeHook(sess, resource, member, attribute);
				}
			}
		} else if(workWithUserAttributes) {
			if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
				//NS_USER_FACILITY_ATTR
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
				if(getAttributesManagerImpl().isVirtAttribute(sess, attribute)) {
					changed = getAttributesManagerImpl().setVirtualAttribute(sess, facility, user, attribute);
				} else {
					changed = setAttributeWithoutCheck(sess, facility, user, attribute);
				}
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
				//NS_USER_ATTR
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(getAttributesManagerImpl().isVirtAttribute(sess, attribute)) {
					//TODO
					throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute instead.");
				} else {
					changed = setAttributeWithoutCheck(sess, user, attribute);
				}
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
				if(getAttributesManagerImpl().isVirtAttribute(sess, attribute)) {
					changed = getAttributesManagerImpl().setVirtualAttribute(sess, member, attribute);
				} else {
					changed = setAttributeWithoutCheck(sess, member, attribute);
				}
			} else {
				throw new WrongAttributeAssignmentException(attribute);
			}
		} else {
			throw new WrongAttributeAssignmentException(attribute);
		}
		return changed;
	}

	@Override
	public boolean setAttributeWithoutCheck(PerunSession sess, Member member, Group group, Attribute attribute, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_GROUP_ATTR)) {
			if(getAttributesManagerImpl().isVirtAttribute(sess, attribute)) {
				changed = getAttributesManagerImpl().setVirtualAttribute(sess, member, group, attribute);
			} else {
				changed = getAttributesManagerImpl().setAttribute(sess, member, group, attribute);
				if(changed) {
					getPerunBl().getAuditer().log(sess, "{} set for {} and {}.", attribute, member, group);
					getAttributesManagerImpl().changedAttributeHook(sess, member, group, attribute);
				}
			}
		} else if(workWithUserAttributes) {
			if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				if(getAttributesManagerImpl().isVirtAttribute(sess, attribute)) {
					changed = getAttributesManagerImpl().setVirtualAttribute(sess, user, attribute);
				} else {
					changed = setAttributeWithoutCheck(sess, user, attribute);
				}
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
				if(getAttributesManagerImpl().isVirtAttribute(sess, attribute)) {
					changed = getAttributesManagerImpl().setVirtualAttribute(sess, member, attribute);
				} else {
					changed = setAttributeWithoutCheck(sess, member, attribute);
				}
			} else {
				throw new WrongAttributeAssignmentException(attribute);
			}
		} else {
			throw new WrongAttributeAssignmentException(attribute);
		}
		return changed;
	}

	public void setAttribute(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, member, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, member, attribute)) {
			checkAttributeValue(sess, member, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(member, null, attribute));
		}
	}

	public void setAttributeInNestedTransaction(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		setAttribute(sess, member, attribute);
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR);

		boolean changed = true;
		if(isVirtAttribute(sess, attribute)) {
			//TODO better exception here
			try {
				return getAttributesManagerImpl().setVirtualAttribute(sess, member, attribute);
			} catch (WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException(ex);
			}

			//FIXME update "changed" variable

		} else if(isCoreAttribute(sess, attribute)) {
			//TODO better exception
			try {
				setCoreAttributeWithoutCheck(sess, member, attribute);
			} catch (WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException(ex);
			} catch (WrongAttributeValueException ex) {
				throw new InternalErrorException(ex);
			}
			changed = true; //FIXME check if attribute is acctualy changed
		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, member, attribute);
		}
		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {}.", attribute, member);
			getAttributesManagerImpl().changedAttributeHook(sess, member, attribute);
		}
		return changed;
	}

	public void setAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, facility, user, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, facility, user, attribute)) {
			checkAttributeValue(sess, facility, user, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(facility, user, attribute));
		}
	}

	public void setAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, user, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, user, attribute)) {
			checkAttributeValue(sess, user, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(user, null, attribute));
		}
	}

	public void setAttributeInNestedTransaction(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		setAttribute(sess, user, attribute);
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_USER_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(isVirtAttribute(sess, attribute)) {
			return getAttributesManagerImpl().setVirtualAttribute(sess, user, attribute);
		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, user, attribute);
		}

		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {}.", attribute, user);
			getAttributesManagerImpl().changedAttributeHook(sess, user, attribute);
		}

		return changed;
	}

	public void setAttribute(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, host, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, host, attribute)) {
			checkAttributeValue(sess, host, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(host, null, attribute));
		}
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_HOST_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(isVirtAttribute(sess, attribute)) {
			//TODO
			throw new InternalErrorException("Virtual attribute " + attribute + " can't be set this way yet. Please set physical attribute.");
		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, host, attribute);
		}
		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {}.", attribute, host);
			//TODO this method not existed yet!
			//getAttributesManagerImpl().changedAttributeHook(sess, host, attribute);
		}

		return changed;
	}

	public void setAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, resource, group, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, resource, group, attribute)) {
			this.checkAttributeDependencies(sess, new RichAttribute<>(resource, group, attribute));
			checkAttributeValue(sess, resource, group, attribute);
		}
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(isVirtAttribute(sess, attribute)) {
			//FIXME Zatim je zakazane nastavovani virtualnich atributu group_resource

			Attribute storedAttribute;
			try {
				storedAttribute = getAttribute(sess, resource, group, attribute.getName());
			} catch(AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
			if(!(storedAttribute.getValue() == null ? attribute.getValue() == null : storedAttribute.getValue().equals(attribute.getValue()))) { //unless attribute and storedAttribute have equals value
				//FIXME
				if(attribute.getName().equals(AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT  + ":unixGID") ||
						attribute.getName().equals(AttributesManager.NS_GROUP_RESOURCE_ATTR_VIRT  + ":unixGroupName")) {
					return getAttributesManagerImpl().setVirtualAttribute(sess, resource, group, attribute);
				} else {
					throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute. " + attribute);
				}
			} else {
				return false;
			}

		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, resource, group, attribute);
		}
		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {} and {}.", attribute, group, resource);
			getAttributesManagerImpl().changedAttributeHook(sess, resource, group, attribute);
		}

		return changed;
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_UES_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(isVirtAttribute(sess, attribute)) {
			return getAttributesManagerImpl().setVirtualAttribute(sess, ues, attribute);
		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, ues, attribute);
		}

		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {}.", attribute, ues);
			getAttributesManagerImpl().changedAttributeHook(sess, ues, attribute);
		}

		return changed;
	}

	public boolean setAttributeWithoutCheck(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_ENTITYLESS_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;
		if(isVirtAttribute(sess, attribute)) {
			//TODO
			throw new InternalErrorException("Virtual attribute can't be set this way yet. Please set physical attribute.");
		} else {
			changed = getAttributesManagerImpl().setAttribute(sess, key, attribute);
		}
		if(changed) {
			getPerunBl().getAuditer().log(sess, "{} set for {}.", attribute, key);
			//TODO this method not existed yet
			//getAttributesManagerImpl().changedAttributeHook(sess, key, attribute);
		}

		return changed;
	}

	public void setAttribute(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, key, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, key, attribute)) {
			checkAttributeValue(sess, key, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(key, null, attribute));
		}
	}

	public void setAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		convertEmptyAttrValueToNull(attribute);
		if (attribute.getValue() == null) {
			removeAttribute(sess, ues, attribute);
			return;
		}
		if(setAttributeWithoutCheck(sess, ues, attribute)) {
			checkAttributeValue(sess, ues, attribute);
			this.checkAttributeDependencies(sess, new RichAttribute<>(ues, null, attribute));
		}
	}

	public AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException, AttributeDefinitionExistsException {
		Utils.notNull(attribute.getName(), "attribute.getName");
		Utils.notNull(attribute.getNamespace(), "attribute.getNamespace");
		Utils.notNull(attribute.getFriendlyName(), "attribute.getFriendlyName");
		Utils.notNull(attribute.getType(), "attribute.getType");

		//check if attribute.nameSpace is valid nameSpace
		if(!isCorrectNameSpace(attribute.getNamespace())) {
			throw new InternalErrorException("Incorrect namespace " + attribute.getNamespace());
		}

		//check if attribute.type is valid class name
		try {
			if (!attribute.getType().equals(BeansUtils.largeStringClassName) &&
						!attribute.getType().equals(BeansUtils.largeArrayListClassName)) {
				Class.forName(attribute.getType());
			}
		} catch(ClassNotFoundException ex) {
			//TODO dat nejakou jinou vyjimku
			throw new InternalErrorException("Wrong attribute type", ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

		getPerunBl().getAuditer().log(sess, "{} created.", attribute);
		return getAttributesManagerImpl().createAttribute(sess, attribute);
	}

	private boolean isCorrectNameSpace(String value) {
		for(String entityType : AttributesManager.ENTITY_TYPES) {
			if(value.matches("urn:perun:" + entityType + ":attribute-def:(def|opt|virt|core)")) {
				return true;
			}
		}
		return false;
	}

	public void deleteAttribute(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		//Remove services' required attributes
		//TODO

		//Remove attribute and all it's values
		getPerunBl().getAuditer().log(sess, "{} deleted.", attribute);
		this.deleteAllAttributeAuthz(sess, attribute);
		getAttributesManagerImpl().deleteAttribute(sess, attribute);
	}

	public void deleteAllAttributeAuthz(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException {
		getPerunBl().getAuditer().log(sess, "All authorization information were deleted for {}.", attribute);
		getAttributesManagerImpl().deleteAllAttributeAuthz(sess, attribute);
	}

	public void deleteAttribute(PerunSession sess, AttributeDefinition attributeDefinition, boolean force) throws InternalErrorException {
		throw new InternalErrorException("Not implemented yet!");
	}

	public List<AttributeDefinition> getResourceRequiredAttributesDefinition(PerunSession sess, Resource resource) throws InternalErrorException {
		return getAttributesManagerImpl().getResourceRequiredAttributesDefinition(sess, resource);
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, facility);
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, resource);
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member) throws InternalErrorException, MemberResourceMismatchException {
		return getResourceRequiredAttributes(sess, resourceToGetServicesFrom, resource, member, false);
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Member member, boolean workWithUserAttributes) throws InternalErrorException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		List<Attribute> attributes = new ArrayList<Attribute>();

		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, resource, member));

		if(workWithUserAttributes) {
			User user;
			Facility facility;
			try {
				user = getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
				facility = getPerunBl().getFacilitiesManagerBl().getFacilityById(sess, resource.getFacilityId());
			} catch (UserNotExistsException e) {
				throw new ConsistencyErrorException("Member has non-existent user.", e);
			} catch (FacilityNotExistsException e) {
				throw new ConsistencyErrorException("Resource has non-existent facility.", e);
			}

			attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, facility, user));
			attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, user));
			attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, member));
		}
		return attributes;
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group) throws InternalErrorException, WrongAttributeAssignmentException {
		return getResourceRequiredAttributes(sess, resourceToGetServicesFrom, member, group, false);
	}

	@Override
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member, Group group, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException {
		List<Attribute> attributes = new ArrayList<>();

		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, member, group));

		if(workWithUserAttributes) {
			User user;
			try {
				user = getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
			} catch (UserNotExistsException e) {
				throw new ConsistencyErrorException("Member has non-existent user.", e);
			}
			attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, user));
			attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, member));
		}
		return attributes;
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, User user) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, facility, user);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Resource resource) throws InternalErrorException {
		List<Integer> serviceIds = new ArrayList<>();
		for(Service service: services) {
			serviceIds.add(service.getId());
		}
		return this.attributesManagerImpl.getRequiredAttributes(sess, resource, serviceIds);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource) throws InternalErrorException {
		return this.getResourceRequiredAttributes(sess, resource, resource);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException, MemberResourceMismatchException {
		return this.getResourceRequiredAttributes(sess, resource, resource, member);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource, Member member, boolean workWithUserAttributes) throws InternalErrorException, MemberResourceMismatchException {
		return this.getResourceRequiredAttributes(sess, resource, resource, member, workWithUserAttributes);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException {
		List<Resource> resources = getPerunBl().getUsersManagerBl().getAssignedResources(sess, facility, user);
		Set<Attribute> attributes = new HashSet<Attribute>();
		for(Resource resource : resources) {
			attributes.addAll(this.getResourceRequiredAttributes(sess, resource, facility, user));
		}
		return new ArrayList<Attribute>(attributes);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, User user) throws InternalErrorException {
		List<Resource> resources = getPerunBl().getUsersManagerBl().getAssignedResources(sess, user);
		Set<Attribute> attributes = new HashSet<Attribute>();
		for(Resource resource : resources) {
			attributes.addAll(this.getResourceRequiredAttributes(sess, resource, user));
		}
		return new ArrayList<Attribute>(attributes);

	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Member member, boolean workWithUserAttributes) throws InternalErrorException {
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, member);
		Set<Attribute> attributes = new HashSet<Attribute>();
		for(Resource resource : resources) {
			attributes.addAll(this.getResourceRequiredAttributes(sess, resource, member));
		}

		if(workWithUserAttributes){
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			attributes.addAll(this.getRequiredAttributes(sess, user));
		}
		return new ArrayList<Attribute>(attributes);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Member member, Group group, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException {

		List<Resource> memberResources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, member);
		List<Resource> groupResources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		// get intersection of resources to determine correct set of services
		memberResources.retainAll(groupResources);

		Set<Attribute> attributes = new HashSet<Attribute>();

		for(Resource resource : memberResources) {
			attributes.addAll(this.getResourceRequiredAttributes(sess, resource, member, group));
		}

		attributes.addAll(this.getRequiredAttributes(sess, member, workWithUserAttributes));

		return new ArrayList<Attribute>(attributes);
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Facility facility, Resource resource, User user, Member member) throws InternalErrorException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, resource, member));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, facility, user));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, user));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, member));
		return attributes;
	}

	public List<Attribute>getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, member);
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, resource, group);
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Group group) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, group);
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Resource resource, Group group, boolean workWithGroupAttributes) throws InternalErrorException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		List<Attribute> attributes = new ArrayList<Attribute>();

		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, resource, group));

		if(workWithGroupAttributes) {
			attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, group));
		}
		return attributes;
	}
	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Host host) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, host);
	}
	public List<AttributeDefinition> getRequiredAttributesDefinition(PerunSession sess, Service service) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributesDefinition(sess, service);
	}

	public List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, User user) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, resourceToGetServicesFrom, user);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, facility);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, service, facility);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Vo vo) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, service, vo);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, service, resource);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member) throws InternalErrorException, MemberResourceMismatchException {
		return getRequiredAttributes(sess, service, resource, member, false);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Member member, boolean workWithUserAttributes) throws InternalErrorException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		if(!workWithUserAttributes) return getAttributesManagerImpl().getRequiredAttributes(sess, service, resource, member);

		User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);

		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, resource, member));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, member));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, facility, user));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, user));
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group, Member member, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, MemberResourceMismatchException, GroupResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);

		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, member));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, member, group));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, resource, member));

		if (workWithUserAttributes) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
			attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, facility, user));
			attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, user));
		}

		return attributes;
	}

	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility, Resource resource, List<Member> members, boolean workWithUserAttributes) throws InternalErrorException, MemberResourceMismatchException {
		// check if members are from the same VO as resource
		if (members.isEmpty()) {
			return new HashMap<>();
		}

		for (Member m : members) {
			this.checkMemberIsFromTheSameVoLikeResource(sess, m, resource);
		}

		if(!workWithUserAttributes) {
			HashMap<Member, List<Attribute>> resourceMemberAttributes = getRequiredAttributes(sess, service, resource, members);
			HashMap<Member, List<Attribute>> memberAttributes = getRequiredAttributes(sess, resource, service, members);

			for (Member mem : memberAttributes.keySet()) {
				if (!resourceMemberAttributes.containsKey(mem)) {
					resourceMemberAttributes.put(mem, memberAttributes.get(mem));
				} else {
					resourceMemberAttributes.get(mem).addAll(memberAttributes.get(mem));
				}
			}
			return resourceMemberAttributes;
		}

		// get list of users, save user id as a key and list of member objects as a value
		List<User> users = new ArrayList<>();
		HashMap<User, List<Member>> userMemberIdMap = new HashMap<>();

		// Maps user ids to member objects and fills list of users
		for (Member m : members) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, m);
			users.add(user);
			if (userMemberIdMap.containsKey(user)) {
				userMemberIdMap.get(user).add(m);
			} else {
				userMemberIdMap.put(user, Arrays.asList(m));
			}
		}

		// get facility if null
		if (facility == null) {
			facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		}

		// get 4 maps from Impl getRequiredAttributes
		HashMap<Member, List<Attribute>> resourceMemberAttributes = getRequiredAttributes(sess, service, resource, members);
		HashMap<Member, List<Attribute>> memberAttributes = getRequiredAttributes(sess, resource, service, members);
		HashMap<User, List<Attribute>> userFacilityAttributes = getRequiredAttributes(sess, service, facility, users);
		HashMap<User, List<Attribute>> userAttributes = getRequiredAttributes(sess, service, users);

		for (Member mem : memberAttributes.keySet()) {
			if (!resourceMemberAttributes.containsKey(mem)) {
				resourceMemberAttributes.put(mem, memberAttributes.get(mem));
			} else {
				resourceMemberAttributes.get(mem).addAll(memberAttributes.get(mem));
			}
		}

		for (User user : userFacilityAttributes.keySet()) {
			// List of members for given user
			List<Member> mems = userMemberIdMap.get(user);
			for (Member mem : mems) {
				if (!resourceMemberAttributes.containsKey(mem)) {
					resourceMemberAttributes.put(mem, userFacilityAttributes.get(user));
				} else {
					resourceMemberAttributes.get(mem).addAll(userFacilityAttributes.get(user));
				}
			}
		}

		for (User user : userAttributes.keySet()) {
			// List of members for given user
			List<Member> mems = userMemberIdMap.get(user);
			for (Member mem : mems) {
				if (!resourceMemberAttributes.containsKey(mem)) {
					resourceMemberAttributes.put(mem, userAttributes.get(user));
				} else {
					resourceMemberAttributes.get(mem).addAll(userAttributes.get(user));
				}
			}
		}

		return resourceMemberAttributes;

	}

	@Override
	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource, List<Member> members) throws InternalErrorException {
		if (!members.isEmpty()) {
			return getRequiredAttributesForBulk(sess, service, resource, members);
		}
		return new HashMap<>();
	}

	@Override
	public HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Resource resource, Service service, List<Member> members) throws InternalErrorException {
		if (!members.isEmpty()) {
			return getRequiredAttributesForBulk(sess, resource, service, members);
		}
		return new HashMap<>();
	}

	@Override
	public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility, List<User> users) throws InternalErrorException {
		if (!users.isEmpty()) {
			return getRequiredAttributesForBulk(sess, service, facility, users);
		}
		return new HashMap<>();
	}

	@Override
	public HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<User> users) throws InternalErrorException {
		if (!users.isEmpty()) {
			return getRequiredAttributesForBulk(sess, service, users);
		}
		return new HashMap<>();
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, service, member, group);
	}

	@Override
	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group, boolean workWithUserAttributes) throws InternalErrorException {
		if(!workWithUserAttributes) return getAttributesManagerImpl().getRequiredAttributes(sess, service, member, group);

		User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, member, group));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, member));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, user));
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility, Resource resource, User user, Member member) throws InternalErrorException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, resource, member));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, member));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, facility, user));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, user));
		return attributes;
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group, boolean withGroupAttributes) throws InternalErrorException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		if(!withGroupAttributes) return getAttributesManagerImpl().getRequiredAttributes(sess, service, resource, group);

		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, resource, group));
		attributes.addAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, group));
		return attributes;

	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, service, member);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group) throws InternalErrorException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		return getAttributesManagerImpl().getRequiredAttributes(sess, service, resource, group);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Host host) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, service, host);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Group group) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, service, group);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, User user) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, service, user);
	}

	public List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility, User user) throws InternalErrorException {
		return getAttributesManagerImpl().getRequiredAttributes(sess, service, facility, user);
	}

	public Attribute fillAttribute(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_RESOURCE_ATTR);

		return getAttributesManagerImpl().fillAttribute(sess, resource, attribute);
	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_RESOURCE_ATTR);

		List<Attribute> filledAttributes = new ArrayList<Attribute>();
		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null) {
				filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, resource, attribute));
			} else {
				//skip non-empty attribute
				filledAttributes.add(attribute);
			}
		}
		return filledAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_RESOURCE_ATTR);

		return getAttributesManagerImpl().fillAttribute(sess, resource, member, attribute);
	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		return fillAttributes(sess, resource, member, attributes, false);
	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		if(!workWithUserAttributes) {
			List<Attribute> filledAttributes = new ArrayList<Attribute>();
			for(Attribute attribute : attributes) {
				getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_RESOURCE_ATTR);
				if(attribute.getValue() == null) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, resource, member, attribute));
				} else {
					//skip non-empty attribute
					filledAttributes.add(attribute);
				}
			}
			return filledAttributes;
		}

		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

		List<Attribute> filledAttributes = new ArrayList<Attribute>();
		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, resource, member, attribute));
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, facility, user, attribute));
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, user, attribute));
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, member, attribute));
				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			} else {
				//skip non-empty attribute
				filledAttributes.add(attribute);

				//TODO and check it's namespace
			}
		}
		return filledAttributes;
	}

	@Override
	public Attribute fillAttribute(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_GROUP_ATTR);
		return getAttributesManagerImpl().fillAttribute(sess, member, group, attribute);
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException {
		return fillAttributes(sess, member, group, attributes, false);
	}

	@Override
	public List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException {
		if(!workWithUserAttributes) {
			List<Attribute> filledAttributes = new ArrayList<>();
			for(Attribute attribute : attributes) {
				getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_GROUP_ATTR);
				if(attribute.getValue() == null) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, member, group, attribute));
				} else {
					//skip non-empty attribute
					filledAttributes.add(attribute);
				}
			}
			return filledAttributes;
		}

		User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

		List<Attribute> filledAttributes = new ArrayList<>();
		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_GROUP_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, member, group, attribute));
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, user, attribute));
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, member, attribute));
				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			} else {
				//skip non-empty attribute
				filledAttributes.add(attribute);
			}
		}
		return filledAttributes;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		List<Attribute> filledAttributes = new ArrayList<Attribute>();
		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, resource, member, attribute));
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, facility, user, attribute));
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, user, attribute));
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, member, attribute));
				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			} else {
				//skip non-empty attribute
				filledAttributes.add(attribute);

				//TODO and check it's namespace
			}
		}
		return filledAttributes;
	}

	public List<Attribute> fillAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes, boolean returnOnlyAttributesWithChangedValue) throws InternalErrorException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		if(!returnOnlyAttributesWithChangedValue) {
			return this.fillAttributes(sess, facility, resource, user, member, attributes);
		} else {
			List<Attribute> attributesWithChangedValue = new ArrayList<Attribute>();
			for(Attribute attribute : attributes) {
				if(attribute.getValue() == null) {
					Attribute a;
					if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
						a = getAttributesManagerImpl().fillAttribute(sess, resource, member, attribute);
					} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
						a = getAttributesManagerImpl().fillAttribute(sess, facility, user, attribute);
					} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
						a = getAttributesManagerImpl().fillAttribute(sess, user, attribute);
					} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
						a = getAttributesManagerImpl().fillAttribute(sess, member, attribute);
					} else {
						throw new WrongAttributeAssignmentException(attribute);
					}
					if(a.getValue() != null) attributesWithChangedValue.add(a);
				}
			}
			return attributesWithChangedValue;
		}
	}

	public Attribute fillAttribute(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		throw new InternalErrorException("Not implemented yet!");
	}

	public List<Attribute> fillAttributes(PerunSession sess, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException {
		throw new InternalErrorException("Not implemented yet!");
	}

	public Attribute fillAttribute(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_USER_FACILITY_ATTR);

		return getAttributesManagerImpl().fillAttribute(sess, facility, user, attribute);
	}

	public List<Attribute> fillAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_USER_FACILITY_ATTR);

		List<Attribute> filledAttributes = new ArrayList<Attribute>();
		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null) {
				filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, facility, user, attribute));
			} else {
				//skip non-empty attribute
				filledAttributes.add(attribute);
			}
		}
		return filledAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_USER_ATTR);
		return getAttributesManagerImpl().fillAttribute(sess, user, attribute);
	}

	public List<Attribute> fillAttributes(PerunSession sess, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_USER_ATTR);

		List<Attribute> filledAttributes = new ArrayList<Attribute>();
		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null) {
				filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, user, attribute));
			} else {
				//skip non-empty attribute
				filledAttributes.add(attribute);
			}
		}
		return filledAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_HOST_ATTR);
		return getAttributesManagerImpl().fillAttribute(sess, host, attribute);
	}

	public List<Attribute> fillAttributes(PerunSession sess, Host host, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, AttributesManager.NS_HOST_ATTR);
		List<Attribute> filledAttributes = new ArrayList<Attribute>();

		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null) {
				filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, host, attribute));
			} else {
				//skip non-empty attribute
				filledAttributes.add(attribute);
			}
		}
		return filledAttributes;
	}


	public Attribute fillAttribute(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_ATTR);
		return getAttributesManagerImpl().fillAttribute(sess, group, attribute);
	}
	public List<Attribute> fillAttributes(PerunSession sess, Group group, List<Attribute> groupReqAttributes) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, groupReqAttributes, AttributesManager.NS_GROUP_ATTR);
		List<Attribute> filledAttributes = new ArrayList<Attribute>();

		for(Attribute attribute : groupReqAttributes) {
			if(attribute.getValue() == null) {
				filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, group, attribute));
			} else {
				//skip non-empty attribute
				filledAttributes.add(attribute);
			}
		}
		return filledAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR);
		return getAttributesManagerImpl().fillAttribute(sess, resource, group, attribute);
	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		getAttributesManagerImpl().checkNamespace(sess, attributes, AttributesManager.NS_GROUP_RESOURCE_ATTR);
		List<Attribute> filledAttributes = new ArrayList<Attribute>();

		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null) {
				filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, resource,group,  attribute));
			} else {
				//skip non-empty attribute
				filledAttributes.add(attribute);
			}
		}
		return filledAttributes;

	}

	public List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttributes) throws InternalErrorException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		if(!workWithGroupAttributes) {
			return this.fillAttributes(sess, resource, group, attributes);
		}
		List<Attribute> filledAttributes = new ArrayList<Attribute>();
		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attribute, NS_GROUP_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, group, attribute));
				} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR)) {
					filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, resource, group, attribute));
				}
			} else {

				filledAttributes.add(attribute);
			}
		}
		return filledAttributes;
	}

	public Attribute fillAttribute(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_UES_ATTR);
		return getAttributesManagerImpl().fillAttribute(sess, ues, attribute);
	}

	public List<Attribute> fillAttributes(PerunSession sess, UserExtSource ues, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, AttributesManager.NS_UES_ATTR);
		List<Attribute> filledAttributes = new ArrayList<Attribute>();

		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null) {
				filledAttributes.add(getAttributesManagerImpl().fillAttribute(sess, ues, attribute));
			} else {
				//skip non-empty attribute
				filledAttributes.add(attribute);
			}
		}
		return filledAttributes;
	}

	public void checkAttributeValue(PerunSession sess, Facility facility, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_FACILITY_ATTR);

		if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, facility, attribute)) return;
		getAttributesManagerImpl().checkAttributeValue(sess, facility, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Facility facility, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_FACILITY_ATTR);
		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, facility, attribute)) continue;
			checkAttributeValue(sess, facility, attribute);
		}
	}

	public void checkAttributeValue(PerunSession sess, Vo vo, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_VO_ATTR);

		if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, vo, attribute)) return;
		getAttributesManagerImpl().checkAttributeValue(sess, vo, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Vo vo, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_VO_ATTR);

		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, vo, attribute)) continue;
			getAttributesManagerImpl().checkAttributeValue(sess, vo, attribute);
		}
	}

	public void checkAttributeValue(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_GROUP_ATTR);

		if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, group, attribute)) return;
		getAttributesManagerImpl().checkAttributeValue(sess, group, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_GROUP_ATTR);

		for(Attribute attribute : attributes) {
			if(attribute.getValue() != null || isTrulyRequiredAttribute(sess, group, attribute)) {
				getAttributesManagerImpl().checkAttributeValue(sess, group, attribute);
			}
		}
	}

	public void checkAttributeValue(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_RESOURCE_ATTR);

		if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, resource, attribute)) return;
		getAttributesManagerImpl().checkAttributeValue(sess, resource, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_RESOURCE_ATTR);

		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, resource, attribute)) continue;
			getAttributesManagerImpl().checkAttributeValue(sess, resource, attribute);
		}
	}

	public void checkAttributeValue(PerunSession sess, Resource resource, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_RESOURCE_ATTR);

		if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, resource, member, attribute)) return;
		getAttributesManagerImpl().checkAttributeValue(sess, resource, member, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		checkAttributesValue(sess, resource, member, attributes, false);
	}

	@Override
	public void checkAttributeValue(PerunSession sess, Member member, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_GROUP_ATTR);

		if (attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, group, attribute)) return;
		getAttributesManagerImpl().checkAttributeValue(sess, member, group, attribute);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Member member, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		checkAttributesValue(sess, member, group, attributes, false);
	}

	@Override
	public void checkAttributesValue(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if(!workWithUserAttributes) {
			getAttributesManagerImpl().checkNamespace(sess, attributes, NS_MEMBER_GROUP_ATTR);

			for (Attribute attribute : attributes) {
				if (attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, group, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, member, group, attribute);
			}
		} else {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

			for(Attribute attribute : attributes) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_GROUP_ATTR)) {
					if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, group, attribute)) continue;
					getAttributesManagerImpl().checkAttributeValue(sess, member, group, attribute);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, user, attribute)) continue;
					getAttributesManagerImpl().checkAttributeValue(sess, user, attribute);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, attribute)) continue;
					getAttributesManagerImpl().checkAttributeValue(sess, member, attribute);
				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			}
		}
	}

	public void checkAttributesValue(PerunSession sess, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if(!workWithUserAttributes) checkAttributesValue(sess, member, attributes);
		else {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			for(Attribute attribute : attributes) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, user, attribute)) continue;
					getAttributesManagerImpl().checkAttributeValue(sess, user, attribute);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, attribute)) continue;
					getAttributesManagerImpl().checkAttributeValue(sess, member, attribute);
				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			}
		}
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		if(!workWithUserAttributes) {
			for(Attribute attribute : attributes) {
				getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_RESOURCE_ATTR);
				if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, resource, member, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, resource, member, attribute);
			}
		} else {
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);

			for(Attribute attribute : attributes) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
					if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, resource, member, attribute)) continue;
					getAttributesManagerImpl().checkAttributeValue(sess, resource, member, attribute);

				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
					if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, facility, user, attribute)) continue;
					getAttributesManagerImpl().checkAttributeValue(sess, facility, user, attribute);

				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, user, attribute)) continue;
					getAttributesManagerImpl().checkAttributeValue(sess, user, attribute);

				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, attribute)) continue;
					getAttributesManagerImpl().checkAttributeValue(sess, member, attribute);

				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			}
		}
	}


	public void checkAttributesValue(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		for(Attribute attribute : attributes) {
			if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
				if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, resource, member, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, resource, member, attribute);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
				if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, facility, user, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, facility, user, attribute);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
				if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, user, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, user, attribute);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
				if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, member, attribute);
			} else {
				throw new WrongAttributeAssignmentException(attribute);
			}
		}
	}

	public void checkAttributesValue(PerunSession sess, Facility facility, Resource resource, Group group, User user, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		for(Attribute attribute : attributes) {
			if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
				if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, resource, member, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, resource, member, attribute);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
				if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, facility, user, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, facility, user, attribute);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
				if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, user, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, user, attribute);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
				if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, member, attribute);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_GROUP_ATTR)) {
				if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, group, attribute)) continue;
				getAttributesManagerImpl().checkAttributeValue(sess, member, group, attribute);
			} else {
				throw new WrongAttributeAssignmentException(attribute);
			}
		}
	}

	public void checkAttributeValue(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_ATTR);

		if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, attribute)) return;
		getAttributesManagerImpl().checkAttributeValue(sess, member, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Member member, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_MEMBER_ATTR);

		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, member, attribute)) continue;
			getAttributesManagerImpl().checkAttributeValue(sess, member, attribute);
		}
	}

	public void checkAttributeValue(PerunSession sess, Facility facility, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_USER_FACILITY_ATTR);

		if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, facility, user, attribute)) return;
		getAttributesManagerImpl().checkAttributeValue(sess, facility, user, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Facility facility, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_USER_FACILITY_ATTR);

		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, facility, user, attribute)) continue;
			getAttributesManagerImpl().checkAttributeValue(sess, facility, user, attribute);
		}
	}

	public void checkAttributeValue(PerunSession sess, Resource resource, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR);

		if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, resource, group, attribute)) return;
		getAttributesManagerImpl().checkAttributeValue(sess, resource, group, attribute);
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, Group group, List<Attribute> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, AttributesManager.NS_GROUP_RESOURCE_ATTR);

		for(Attribute attribute : attributes) {
			checkAttributeValue(sess, resource, group, attribute);
		}
	}

	public void checkAttributesValue(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		if(!workWithGroupAttribute) {
			this.checkAttributesValue(sess, resource, group, attributes);
		}
		for(Attribute attribute : attributes) {
			if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR)) {
				getAttributesManagerImpl().checkAttributeValue(sess, resource, group, attribute);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_GROUP_ATTR)) {
				getAttributesManagerImpl().checkAttributeValue(sess, group, attribute);
			} else {
				throw new WrongAttributeAssignmentException(attribute);
			}
		}
	}


	public void checkAttributeValue(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_USER_ATTR);

		if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, user, attribute)) return;
		getAttributesManagerImpl().checkAttributeValue(sess, user, attribute);
	}

	public void checkAttributesValue(PerunSession sess, User user, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException{
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_USER_ATTR);

		for(Attribute attribute : attributes) {
			if(attribute.getValue() == null && !isTrulyRequiredAttribute(sess, user, attribute)) continue;
			getAttributesManagerImpl().checkAttributeValue(sess, user, attribute);
		}
	}

	public void checkAttributesValue(PerunSession sess, Host host, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException,WrongAttributeAssignmentException{
		getAttributesManagerImpl().checkNamespace(sess, attributes, AttributesManager.NS_HOST_ATTR);

		for(Attribute attribute : attributes) {
			getAttributesManagerImpl().checkAttributeValue(sess, host, attribute);
		}
	}
	public void checkAttributeValue(PerunSession sess, Host host, Attribute attribute) throws InternalErrorException, WrongAttributeValueException,WrongAttributeAssignmentException{
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_HOST_ATTR);

		getAttributesManagerImpl().checkAttributeValue(sess, host, attribute);
	}

	public void checkAttributesValue(PerunSession sess, UserExtSource ues, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException{
		getAttributesManagerImpl().checkNamespace(sess, attributes, AttributesManager.NS_UES_ATTR);

		for(Attribute attribute : attributes) {
			getAttributesManagerImpl().checkAttributeValue(sess, ues, attribute);
		}
	}

	public void checkAttributeValue(PerunSession sess, UserExtSource ues, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_UES_ATTR);

		getAttributesManagerImpl().checkAttributeValue(sess, ues, attribute);
	}

	public void checkAttributeValue(PerunSession sess, String key, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_ENTITYLESS_ATTR);

		getAttributesManagerImpl().checkAttributeValue(sess, key, attribute);
	}

	public void forceCheckAttributeValue(PerunSession sess, Group group, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_ATTR);

		getAttributesManagerImpl().checkAttributeValue(sess, group, attribute);
	}

	public void forceCheckAttributeValue(PerunSession sess, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_RESOURCE_ATTR);

		getAttributesManagerImpl().checkAttributeValue(sess, resource, attribute);
	}
	public boolean removeAttributeWithoutCheck(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_ENTITYLESS_ATTR);
		boolean changed = getAttributesManagerImpl().removeAttribute(sess, key, attribute);
		if (changed) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, key, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, "{} removed for {}", attribute, key);
		}
		return changed;
	}

	public void removeAllMemberResourceAttributes(PerunSession sess, Resource resource) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		this.attributesManagerImpl.removeAllMemberResourceAttributes(sess, resource);
		this.getPerunBl().getAuditer().log(sess, "All non-virtual member-resource attributes removed for all members and {}", resource);
	}

	public void removeAllGroupResourceAttributes(PerunSession sess, Resource resource) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		List<Group> groups = this.getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
		for (Group group : groups) {
			this.getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, resource, group);
		}
		this.attributesManagerImpl.removeAllGroupResourceAttributes(sess, resource);
		this.getPerunBl().getAuditer().log(sess, "All non-virtual group-resource attributes removed for all groups and {}", resource);
	}

	public void removeAttribute(PerunSession sess, String key, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if (removeAttributeWithoutCheck(sess, key, attribute)) {
			this.checkAttributeValue(sess, key, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(key, null, new Attribute(attribute)));
		}
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_FACILITY_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);
		boolean changed = getAttributesManagerImpl().removeAttribute(sess, facility, attribute);
		if (changed) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, facility, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, "{} removed for {}", attribute, facility);
		}
		return changed;
	}

	public void removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if (removeAttributeWithoutCheck(sess, facility, attribute)) {
			this.checkAttributeValue(sess, facility, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(facility, null, new Attribute(attribute)));
		}
	}

	public void removeAttributes(PerunSession sess, Member member, boolean workWithUserAttributes, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if(!workWithUserAttributes){
			getAttributesManagerImpl().checkNamespace(sess, attributes, NS_MEMBER_ATTR);
			List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
			for(AttributeDefinition attribute : attributes) {
				//skip core attributes
				if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
					if (removeAttributeWithoutCheck(sess, member, attribute)) attributesToCheck.add(attribute);
				}
			}
			this.checkAttributesValue(sess, member, attributesFromDefinitions(attributesToCheck));
			this.checkAttributesDependencies(sess, member, null, attributesFromDefinitions(attributesToCheck));
		}else{
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
			for(AttributeDefinition attribute : attributes) {
				//skip core attributes
				if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
					if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
						if (removeAttributeWithoutCheck(sess, member, attribute)) attributesToCheck.add(attribute);
					} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
						if (removeAttributeWithoutCheck(sess, user, attribute)) attributesToCheck.add(attribute);
					} else {
						throw new WrongAttributeAssignmentException(attribute);
					}
				}
			}
			this.checkAttributesValue(sess, member, attributesFromDefinitions(attributesToCheck), true);
			this.checkAttributesDependencies(sess, member, attributesFromDefinitions(attributesToCheck), workWithUserAttributes);
		}
	}

	public void removeAttributes(PerunSession sess, Facility facility, List<? extends AttributeDefinition> attributesDefinition) throws InternalErrorException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, WrongAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributesDefinition, NS_FACILITY_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributesDefinition) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if (removeAttributeWithoutCheck(sess, facility, attribute)) attributesToCheck.add(attribute);
			}
		}
		this.checkAttributesValue(sess, facility, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, facility, null, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {

				if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					if (removeAttributeWithoutCheck(sess, member, attribute)) attributesToCheck.add(attribute);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					if (removeAttributeWithoutCheck(sess, user, attribute)) attributesToCheck.add(attribute);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)){
					if (removeAttributeWithoutCheck(sess, resource, member, attribute)) attributesToCheck.add(attribute);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)){
					if (removeAttributeWithoutCheck(sess, facility, user, attribute)) attributesToCheck.add(attribute);
				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			}
		}
		this.checkAttributesValue(sess, facility, resource, user, member, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, resource, member, user, facility, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAttributes(PerunSession sess, Facility facility, Resource resource, Group group, User user, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {

				if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
					if (removeAttributeWithoutCheck(sess, member, attribute)) attributesToCheck.add(attribute);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
					if (removeAttributeWithoutCheck(sess, user, attribute)) attributesToCheck.add(attribute);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)){
					if (removeAttributeWithoutCheck(sess, resource, member, attribute)) attributesToCheck.add(attribute);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)){
					if (removeAttributeWithoutCheck(sess, facility, user, attribute)) attributesToCheck.add(attribute);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_GROUP_ATTR)){
					if (removeAttributeWithoutCheck(sess, member, group, attribute)) attributesToCheck.add(attribute);
				} else {
					throw new WrongAttributeAssignmentException(attribute);
				}
			}
		}
		this.checkAttributesValue(sess, facility, resource, group, user, member, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, resource, group, member, user, facility, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAllAttributes(PerunSession sess, Facility facility) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> attributes = getAttributes(sess, facility);
		getAttributesManagerImpl().removeAllAttributes(sess, facility);
		getPerunBl().getAuditer().log(sess, "All attributes removed for {}", facility);

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, facility, attributes);
			this.checkAttributesDependencies(sess, facility, null, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}
		for(Attribute attribute : attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, facility, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public void removeAllAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		removeAllAttributes(sess, resource, group);
		if (workWithGroupAttributes) {
			removeAllAttributes(sess, group);
		}
	}

	public void removeAllAttributes(PerunSession sess, Facility facility, boolean removeAlsoUserFacilityAttributes) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		removeAllAttributes(sess, facility);
		if(removeAlsoUserFacilityAttributes) {
			List<Attribute> userFacilityAttributes = getUserFacilityAttributesForAnyUser(sess, facility);
			getAttributesManagerImpl().removeAllUserFacilityAttributesForAnyUser(sess, facility);
			getPerunBl().getAuditer().log(sess, "All user-facility attributes removed for {} for any user.", facility);

			for(Attribute attribute : userFacilityAttributes) attribute.setValue(null);
			List<User> facilityUsers = perunBl.getFacilitiesManagerBl().getAllowedUsers(sess, facility);
			for(User user: facilityUsers) {
				try {
					checkAttributesValue(sess, facility, user, userFacilityAttributes);
					this.checkAttributesDependencies(sess, facility, user, userFacilityAttributes);
				} catch(WrongAttributeAssignmentException ex) {
					throw new ConsistencyErrorException(ex);
				}
				for(Attribute attribute : userFacilityAttributes) {
					try {
						getAttributesManagerImpl().changedAttributeHook(sess, facility, user, new Attribute(attribute));
					} catch (WrongAttributeValueException ex) {
						//TODO better exception here
						throw new InternalErrorException(ex);
					} catch (WrongReferenceAttributeValueException ex) {
						//TODO better exception here
						throw new InternalErrorException(ex);
					}
				}
			}
		}
	}

	public void removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException {
		if (removeAttributeWithoutCheck(sess, host, attribute)) {
			checkAttributeValue(sess, host, new Attribute(attribute));
			try {
				this.checkAttributeDependencies(sess, new RichAttribute<>(host, null, new Attribute(attribute)));
			} catch (WrongReferenceAttributeValueException ex) {
				throw new WrongAttributeValueException(ex);
			}
		}
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, Host host, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_HOST_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = getAttributesManagerImpl().removeAttribute(sess, host, attribute);
		if (changed) {
			//TODO HOOK FOR HOSTS!
			/*try {
				getAttributesManagerImpl().changedAttributeHook(sess, host, new Attribute(attribute));
				} catch (WrongAttributeValueException ex) {
			//TODO better exception here
			throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
			//TODO better exception here
			throw new InternalErrorException(ex);
			}*/
			getPerunBl().getAuditer().log(sess, "{} removed for {}", attribute, host);
		}
		return changed;
	}

	public void removeAttributes(PerunSession sess, Host host, List<? extends AttributeDefinition> attributesDefinition) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributesDefinition, AttributesManager.NS_HOST_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributesDefinition) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if (removeAttributeWithoutCheck(sess, host, attribute)) attributesToCheck.add(attribute);
			}
		}
		this.checkAttributesValue(sess, host, attributesFromDefinitions(attributesToCheck));
		try {
			this.checkAttributesDependencies(sess, host, null, attributesFromDefinitions(attributesToCheck));
		} catch (WrongReferenceAttributeValueException ex) {
			throw new WrongAttributeValueException(ex);
		}
	}

	public void removeAllAttributes(PerunSession sess, Host host) throws InternalErrorException, WrongAttributeValueException {
		List<Attribute> attributes = getAttributes(sess, host);
		getAttributesManagerImpl().removeAllAttributes(sess, host);
		getPerunBl().getAuditer().log(sess, "All attributes removed for {}", host);

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, host, attributes);
			this.checkAttributesDependencies(sess, host, null, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		} catch(WrongReferenceAttributeValueException ex) {
			throw new WrongAttributeValueException(ex);
		}

		//TODO HOOK FOR HOSTS
		/*
			 for(Attribute attribute: attributes) {
			 try {
			 getAttributesManagerImpl().changedAttributeHook(sess, host, new Attribute(attribute));
			 } catch (WrongAttributeValueException ex) {
		//TODO better exception here
		throw new InternalErrorException(ex);
		} catch (WrongReferenceAttributeValueException ex) {
		//TODO better exception here
		throw new InternalErrorException(ex);
		}
		}*/
	}

	public void removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if (removeAttributeWithoutCheck(sess, vo, attribute)) {
			checkAttributeValue(sess, vo, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(vo, null, new Attribute(attribute)));
		}
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, Vo vo, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_VO_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = getAttributesManagerImpl().removeAttribute(sess, vo, attribute);
		if (changed) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, vo, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, "{} removed for {}", attribute, vo);
		}
		return changed;
	}

	public void removeAttributes(PerunSession sess, Vo vo, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_VO_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if (removeAttributeWithoutCheck(sess, vo, attribute)) attributesToCheck.add(attribute);
			}
		}
		checkAttributesValue(sess, vo, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, vo, null, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAllAttributes(PerunSession sess, Vo vo) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> attributes = getAttributes(sess, vo);
		getAttributesManagerImpl().removeAllAttributes(sess, vo);
		getPerunBl().getAuditer().log(sess, "All attributes removed for {}", vo);

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, vo, attributes);
			this.checkAttributesDependencies(sess, vo, null, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}

		for(Attribute attribute: attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, vo, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public void removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if (removeAttributeWithoutCheck(sess, group, attribute)) {
			checkAttributeValue(sess, group, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(group, null, new Attribute(attribute)));
		}
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_GROUP_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = getAttributesManagerImpl().removeAttribute(sess, group, attribute);
		if (changed) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, group, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, "{} removed for {}", attribute, group);
		}
		return changed;
	}

	public void removeAttributes(PerunSession sess, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_GROUP_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if (removeAttributeWithoutCheck(sess, group, attribute)) attributesToCheck.add(attribute);
			}
		}
		checkAttributesValue(sess, group, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, group, null, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAllAttributes(PerunSession sess, Group group) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> attributes = getAttributes(sess, group);
		getAttributesManagerImpl().removeAllAttributes(sess, group);
		getPerunBl().getAuditer().log(sess, "All attributes removed for {}", group);
		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, group, attributes);
			this.checkAttributesDependencies(sess, group, null, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}

		for(Attribute attribute: attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, group, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public boolean removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		boolean changed = removeAttributeWithoutCheck(sess, resource, attribute);
		if (changed) {
			checkAttributeValue(sess, resource, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(resource, null, new Attribute(attribute)));
		}
		return changed;
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, Resource resource, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_RESOURCE_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = true;

		try {
			if (this.isVirtAttribute(sess, attribute)) {
				changed = getAttributesManagerImpl().removeVirtualAttribute(sess, resource, attribute);
			} else {
				changed = getAttributesManagerImpl().removeAttribute(sess, resource, attribute);
			}
			if (changed) getAttributesManagerImpl().changedAttributeHook(sess, resource, new Attribute(attribute));
		} catch (WrongAttributeValueException ex) {
			//TODO better exception here
			throw new InternalErrorException(ex);
		} catch (WrongReferenceAttributeValueException ex) {
			//TODO better exception here
			throw new InternalErrorException(ex);
		}
		if (changed) getPerunBl().getAuditer().log(sess, "{} removed for {}", attribute, resource);
		return changed;
	}

	public void removeAttributes(PerunSession sess, Resource resource, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_RESOURCE_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			//skip core attributes
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if (removeAttributeWithoutCheck(sess, resource, attribute)) attributesToCheck.add(attribute);
			}
		}
		checkAttributesValue(sess, resource, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, resource, null, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAllAttributes(PerunSession sess, Resource resource) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> attributes = getAttributes(sess, resource);
		getAttributesManagerImpl().removeAllAttributes(sess, resource);

		//remove all virtual attributes
		/*for(Attribute attribute : getVirtualAttributes(sess, resource)) {
			getAttributesManagerImpl().removeVirtualAttribute(sess, resource, attribute);
			}*/

		getPerunBl().getAuditer().log(sess, "All attributes removed for {}", resource);

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, resource, attributes);
			this.checkAttributesDependencies(sess, resource, null, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}

		for(Attribute attribute: attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, resource, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public void removeAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		if (removeAttributeWithoutCheck(sess, resource, member, attribute)) {
			checkAttributeValue(sess, resource, member, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(resource, member, new Attribute(attribute)));
		}
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, Resource resource, Member member, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_RESOURCE_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = getAttributesManagerImpl().removeAttribute(sess, resource, member, attribute);
		if (changed) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, resource, member, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, "{} removed for {} and {}", attribute, resource, member);
		}

		return changed;
	}

	public void removeAttributes(PerunSession sess, Resource resource, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_MEMBER_RESOURCE_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if (removeAttributeWithoutCheck(sess, resource, member, attribute)) attributesToCheck.add(attribute);
			}
		}
		checkAttributesValue(sess, resource, member, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, resource, member, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAttributes(PerunSession sess, Resource resource, Member member, List<? extends AttributeDefinition> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		if (!(workWithUserAttributes)) {
			removeAttributes(sess, resource, member, attributes);
		} else {
			List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
			for (AttributeDefinition attribute : attributes) {
				if (!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
					Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
					User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
					if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
						if (removeAttributeWithoutCheck(sess, resource, member, attribute)) attributesToCheck.add(attribute);
					} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_FACILITY_ATTR)) {
						if (removeAttributeWithoutCheck(sess, facility, user, attribute)) attributesToCheck.add(attribute);
					} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
						if (removeAttributeWithoutCheck(sess, user, attribute)) attributesToCheck.add(attribute);
					} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
						if (removeAttributeWithoutCheck(sess, member, attribute)) attributesToCheck.add(attribute);
					} else {
						throw new WrongAttributeAssignmentException(attribute);
					}
				}
			}
			checkAttributesValue(sess, resource, member, attributesFromDefinitions(attributesToCheck), workWithUserAttributes);
			this.checkAttributesDependencies(sess, resource, member, attributesFromDefinitions(attributesToCheck), workWithUserAttributes);
		}
	}

	public void removeAllAttributes(PerunSession sess, Resource resource, Member member) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		List<Attribute> attributes = getAttributes(sess, resource, member);
		getAttributesManagerImpl().removeAllAttributes(sess, resource, member);
		getPerunBl().getAuditer().log(sess, "All attributes removed for {} and {}", resource, member);

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, resource, member, attributes);
			checkAttributesValue(sess, resource, member, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}

		for(Attribute attribute: attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, resource, member, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	@Override
	public void removeAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if (removeAttributeWithoutCheck(sess, member, group, attribute)) {
			checkAttributeValue(sess, member, group, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(member, group, new Attribute(attribute)));
		}
	}
	// s workWithUserAttr.
	public boolean removeAttributeWithoutCheck(PerunSession sess, Member member, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_GROUP_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = getAttributesManagerImpl().removeAttribute(sess, member, group, attribute);
		if (changed) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, member, group, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, "{} removed for {} and {}", attribute, member, group);
		}

		return changed;
	}

	@Override
	public void removeAttributes(PerunSession sess, Member member, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		removeAttributes(sess, member, group, attributes, false);
	}

	@Override
	public void removeAttributes(PerunSession sess, Member member, Group group, List<? extends AttributeDefinition> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if (!workWithUserAttributes) {
			getAttributesManagerImpl().checkNamespace(sess, attributes, NS_MEMBER_GROUP_ATTR);
			List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
			for(AttributeDefinition attribute : attributes) {
				if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
					if (removeAttributeWithoutCheck(sess, member, group, attribute)) attributesToCheck.add(attribute);
				}
			}
			checkAttributesValue(sess, member, group, attributesFromDefinitions(attributesToCheck));
			this.checkAttributesDependencies(sess, member, group, attributesFromDefinitions(attributesToCheck));
		} else {
			List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
			for (AttributeDefinition attribute : attributes) {
				if (!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
					User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
					if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_GROUP_ATTR)) {
						if (removeAttributeWithoutCheck(sess, member, group, attribute)) attributesToCheck.add(attribute);
					} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR)) {
						if (removeAttributeWithoutCheck(sess, user, attribute)) attributesToCheck.add(attribute);
					} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR)) {
						if (removeAttributeWithoutCheck(sess, member, attribute)) attributesToCheck.add(attribute);
					} else {
						throw new WrongAttributeAssignmentException(attribute);
					}
				}
			}
			checkAttributesValue(sess, member, group, attributesFromDefinitions(attributesToCheck), workWithUserAttributes);
			this.checkAttributesDependencies(sess, member, group, attributesFromDefinitions(attributesToCheck), workWithUserAttributes);
		}
	}

	@Override
	public void removeAllAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> attributes = getAttributes(sess, member, group);
		getAttributesManagerImpl().removeAllAttributes(sess, member, group);
		getPerunBl().getAuditer().log(sess, "All attributes removed for {} and {}", member, group);

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, member, group, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}

		for(Attribute attribute: attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, member, group, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public void removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if (removeAttributeWithoutCheck(sess, member, attribute)) {
			checkAttributeValue(sess, member, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(member, null, new Attribute(attribute)));
		}
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, Member member, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_MEMBER_ATTR);

		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = getAttributesManagerImpl().removeAttribute(sess, member, attribute);

		if (changed) {
			try {
				if (changed) getAttributesManagerImpl().changedAttributeHook(sess, member, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, "{} removed for {}", attribute, member);
		}

		return changed;

	}

	public void removeAttributes(PerunSession sess, Member member, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_MEMBER_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if (removeAttributeWithoutCheck(sess, member, attribute)) attributesToCheck.add(attribute);
			}
		}
		checkAttributesValue(sess, member, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, member, null, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAllAttributes(PerunSession sess, Member member) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> attributes = getAttributes(sess, member);
		getAttributesManagerImpl().removeAllAttributes(sess, member);
		getPerunBl().getAuditer().log(sess, "All attributes removed for {}", member);

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, member, attributes);
			this.checkAttributesDependencies(sess, member, null, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}

		for(Attribute attribute: attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, member, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public void removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if (removeAttributeWithoutCheck(sess, facility, user, attribute)) {
			checkAttributeValue(sess, facility, user, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(facility, user, new Attribute(attribute)));
		}
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, Facility facility, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_USER_FACILITY_ATTR);
		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = false;

		if(getAttributesManagerImpl().isVirtAttribute(sess, attribute)) {
			changed = getAttributesManagerImpl().removeVirtualAttribute(sess, facility, user, attribute);
		} else {
			changed = getAttributesManagerImpl().removeAttribute(sess, facility, user, attribute);
		}

		if (changed) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, facility, user, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, "{} removed for {} and {}", attribute, facility, user);
		}
		return changed;
	}

	public void removeAttributes(PerunSession sess, Facility facility, User user, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_USER_FACILITY_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if(getAttributesManagerImpl().isVirtAttribute(sess, attribute)) {
					if (getAttributesManagerImpl().removeVirtualAttribute(sess, facility, user, attribute)) attributesToCheck.add(attribute);
				} else {
					if (removeAttributeWithoutCheck(sess, facility, user, attribute)) attributesToCheck.add(attribute);
				}
			}
		}
		checkAttributesValue(sess, facility, user, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, facility, user, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAllAttributes(PerunSession sess, Facility facility, User user) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> attributes = getAttributes(sess, facility, user);
		//remove all non-virtual attributes
		getAttributesManagerImpl().removeAllAttributes(sess, facility, user);

		//remove all virtual attributes
		List<Attribute> virtualAttributes = getVirtualAttributes(sess, facility, user);
		for(Attribute attribute : virtualAttributes) {
			getAttributesManagerImpl().removeVirtualAttribute(sess, facility, user, attribute);
		}
		attributes.addAll(virtualAttributes);

		getPerunBl().getAuditer().log(sess, "All attributes removed for {} and {}", facility, user);

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, facility, user, attributes);
			this.checkAttributesDependencies(sess, facility, user, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}

		for(Attribute attribute: attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, facility, user, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public void removeAllUserFacilityAttributes(PerunSession sess, User user) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {

		List<RichAttribute<User, Facility>> userFacilitiesAttributes = getAttributesManagerImpl().getAllUserFacilityRichAttributes(sess, user);

		//remove all non-virtual attributes
		getAttributesManagerImpl().removeAllUserFacilityAttributes(sess, user);
		getPerunBl().getAuditer().log(sess, "All non-virtual user-facility attributes removed for all facilities and {}", user);

		for(RichAttribute<User, Facility> richAttribute : userFacilitiesAttributes) {
			try {
				checkAttributeValue(sess, richAttribute.getSecondaryHolder(), richAttribute.getPrimaryHolder(), new Attribute(richAttribute.getAttribute()));
				this.checkAttributeDependencies(sess, richAttribute);
			} catch(WrongAttributeAssignmentException ex) {
				throw new ConsistencyErrorException(ex);
			}
		}
		for(RichAttribute<User, Facility> attribute: userFacilitiesAttributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, attribute.getSecondaryHolder(), attribute.getPrimaryHolder(),  new Attribute(attribute.getAttribute()));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public void removeAttribute(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if (removeAttributeWithoutCheck(sess, user, attribute)) {
			checkAttributeValue(sess, user, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(user, null, new Attribute(attribute)));
		}
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, User user, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, NS_USER_ATTR);

		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = getAttributesManagerImpl().removeAttribute(sess, user, attribute);
		if (changed) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, user, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, "{} removed for {}", attribute, user);
		}
		return changed;
	}

	public void removeAttributes(PerunSession sess, User user, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, NS_USER_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if (removeAttributeWithoutCheck(sess, user, attribute)) attributesToCheck.add(attribute);
			}
		}
		checkAttributesValue(sess, user, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, user, null, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAllAttributes(PerunSession sess, User user) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> attributes = getAttributes(sess, user);
		getAttributesManagerImpl().removeAllAttributes(sess, user);
		getPerunBl().getAuditer().log(sess, "All attributes removed for {}", user);

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, user, attributes);
			this.checkAttributesDependencies(sess, user, null, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}

		for(Attribute attribute: attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, user, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public void removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		if (removeAttributeWithoutCheck(sess, resource, group, attribute)) {
			checkAttributeValue(sess, resource, group, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(resource, group, new Attribute(attribute)));
		}
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR);
		boolean changed = false;
		try {
			if (this.isVirtAttribute(sess, attribute)) {
				changed = getAttributesManagerImpl().removeVirtualAttribute(sess, resource, group, attribute);
			} else {
				changed = getAttributesManagerImpl().removeAttribute(sess, resource, group, attribute);
			}

			if (changed) getAttributesManagerImpl().changedAttributeHook(sess, resource, group, new Attribute(attribute));
		} catch (WrongAttributeValueException ex) {
			//TODO better exception here
			throw new InternalErrorException(ex);
		} catch (WrongReferenceAttributeValueException ex) {
			//TODO better exception here
			throw new InternalErrorException(ex);
		}
		if (changed) getPerunBl().getAuditer().log(sess, "{} removed for {} and {}", attribute, group, resource);
		return changed;
	}

	public void removeAttributes(PerunSession sess, Resource resource, Group group, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		//getAttributesManagerImpl().checkNamespace(sess, attributes, AttributesManager.NS_GROUP_RESOURCE_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			if (removeAttributeWithoutCheck(sess, resource, group, attribute)) attributesToCheck.add(attribute);
		}
		checkAttributesValue(sess, resource, group, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, resource, group, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAttributes(PerunSession sess, Resource resource, Group group, List<? extends AttributeDefinition> attributes, boolean workWithGroupAttributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException {
		if (!workWithGroupAttributes) {
			removeAttributes(sess, resource, group, attributes);
		} else {

			List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
			for (AttributeDefinition attribute : attributes) {
				//skip core attributes
				if (!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {

					if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR)) {
						if (removeAttributeWithoutCheck(sess, resource, group, attribute)) attributesToCheck.add(attribute);
					} else if (getAttributesManagerImpl().isFromNamespace(sess, attribute, AttributesManager.NS_GROUP_ATTR)) {
						if (removeAttributeWithoutCheck(sess, group, attribute)) attributesToCheck.add(attribute);
					} else {
						throw new WrongAttributeAssignmentException(attribute);
					}
				}

			}
			checkAttributesValue(sess, resource, group, attributesFromDefinitions(attributesToCheck), workWithGroupAttributes);
			this.checkAttributesDependencies(sess, resource, group, attributesFromDefinitions(attributesToCheck), workWithGroupAttributes);
		}
	}

	public void removeAllAttributes(PerunSession sess, Resource resource, Group group) throws InternalErrorException, WrongAttributeValueException, GroupResourceMismatchException, WrongReferenceAttributeValueException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		List<Attribute> attributes = getAttributes(sess, resource, group);
		getAttributesManagerImpl().removeAllAttributes(sess, resource, group);
		getPerunBl().getAuditer().log(sess, "All attributes removed for {} and {}", group, resource);

		//remove all virtual attributes
		/*for(Attribute attribute : getVirtualAttributes(sess, resource)) {
			getAttributesManagerImpl().removeVirtualAttribute(sess, resource, attribute);
			}*/

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, resource, group, attributes);
			this.checkAttributesDependencies(sess, resource, group, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}

		for(Attribute attribute: attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, resource, group, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public void removeAttribute(PerunSession sess, UserExtSource ues, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if (removeAttributeWithoutCheck(sess, ues, attribute)) {
			checkAttributeValue(sess, ues, new Attribute(attribute));
			this.checkAttributeDependencies(sess, new RichAttribute<>(ues, null, new Attribute(attribute)));
		}
	}

	public boolean removeAttributeWithoutCheck(PerunSession sess, UserExtSource ues, AttributeDefinition attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, AttributesManager.NS_UES_ATTR);

		if(getAttributesManagerImpl().isCoreAttribute(sess, attribute)) throw new WrongAttributeAssignmentException(attribute);

		boolean changed = getAttributesManagerImpl().removeAttribute(sess, ues, attribute);
		if (changed) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, ues, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, "{} removed for {}", attribute, ues);
		}
		return changed;
	}

	public void removeAttributes(PerunSession sess, UserExtSource ues, List<? extends AttributeDefinition> attributes) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, AttributesManager.NS_UES_ATTR);
		List<AttributeDefinition> attributesToCheck = new ArrayList<AttributeDefinition>();
		for(AttributeDefinition attribute : attributes) {
			if(!getAttributesManagerImpl().isCoreAttribute(sess, attribute)) {
				if (removeAttributeWithoutCheck(sess, ues, attribute)) attributesToCheck.add(attribute);
			}
		}
		checkAttributesValue(sess, ues, attributesFromDefinitions(attributesToCheck));
		this.checkAttributesDependencies(sess, ues, null, attributesFromDefinitions(attributesToCheck));
	}

	public void removeAllAttributes(PerunSession sess, UserExtSource ues) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<Attribute> attributes = getAttributes(sess, ues);
		getAttributesManagerImpl().removeAllAttributes(sess, ues);
		getPerunBl().getAuditer().log(sess, "All attributes removed for {}", ues);

		for(Attribute attribute : attributes) attribute.setValue(null);
		try {
			checkAttributesValue(sess, ues, attributes);
			this.checkAttributesDependencies(sess, ues, null, attributes);
		} catch(WrongAttributeAssignmentException ex) {
			throw new ConsistencyErrorException(ex);
		}

		for(Attribute attribute: attributes) {
			try {
				getAttributesManagerImpl().changedAttributeHook(sess, ues, new Attribute(attribute));
			} catch (WrongAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			} catch (WrongReferenceAttributeValueException ex) {
				//TODO better exception here
				throw new InternalErrorException(ex);
			}
		}
	}

	public void checkActionTypeExists(PerunSession sess, ActionType actionType) throws InternalErrorException, ActionTypeNotExistsException {
		getAttributesManagerImpl().checkActionTypeExists(sess, actionType);
	}

	public void checkAttributeExists(PerunSession sess, AttributeDefinition attribute) throws InternalErrorException, AttributeNotExistsException {
		getAttributesManagerImpl().checkAttributeExists(sess, attribute);
	}

	public void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeNotExistsException {
		getAttributesManagerImpl().checkAttributesExists(sess, attributes);
	}

	public boolean isDefAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerImpl().isDefAttribute(sess, attribute);
	}

	public boolean isOptAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerImpl().isOptAttribute(sess, attribute);
	}

	public boolean isCoreAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerImpl().isCoreAttribute(sess, attribute);
	}

	public boolean isCoreManagedAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerImpl().isCoreManagedAttribute(sess, attribute);
	}

	public boolean isVirtAttribute(PerunSession sess, AttributeDefinition attribute) {
		return getAttributesManagerImpl().isVirtAttribute(sess, attribute);
	}

	public boolean isFromNamespace(PerunSession sess, AttributeDefinition attribute, String namespace) {
		return getAttributesManagerImpl().isFromNamespace(sess, attribute, namespace);
	}

	public void checkNamespace(PerunSession sess, AttributeDefinition attribute, String namespace) throws WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attribute, namespace);
	}

	public void checkNamespace(PerunSession sess, List<? extends AttributeDefinition> attributes, String namespace) throws WrongAttributeAssignmentException {
		getAttributesManagerImpl().checkNamespace(sess, attributes, namespace);
	}

	public String getNamespaceFromAttributeName(String attributeName) {
		return attributeName.replaceFirst("(urn:perun:[^:]+:attribute-def:[^:]+):.*", "$1");
	}

	public String getFriendlyNameFromAttributeName(String attributeName) {
		return attributeName.replaceFirst("urn:perun:[^:]+:attribute-def:[^:]+:", "");
	}

	public List<Attribute> getLogins(PerunSession sess, User user) throws InternalErrorException {
		List<Attribute> usersAttributes = this.getAttributes(sess, user);  //Get all non-empty user's attributes
		Iterator<Attribute> it = usersAttributes.iterator();
		while (it.hasNext()) {
			if(!it.next().getFriendlyName().startsWith("login-namespace:")) it.remove();
		}
		return usersAttributes;
	}

	public List<Object> getAllValues(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		if(isCoreAttribute(sess, attributeDefinition) || isCoreManagedAttribute(sess, attributeDefinition) || isVirtAttribute(sess, attributeDefinition)) throw new WrongAttributeAssignmentException(attributeDefinition);

		if(isFromNamespace(sess, attributeDefinition, AttributesManager.NS_RESOURCE_ATTR)) {
			return getAttributesManagerImpl().getAllResourceValues(sess, attributeDefinition);
		} else if(isFromNamespace(sess, attributeDefinition, AttributesManager.NS_GROUP_RESOURCE_ATTR)) {
			return getAttributesManagerImpl().getAllGroupResourceValues(sess, attributeDefinition);
		} else if(isFromNamespace(sess, attributeDefinition, AttributesManager.NS_GROUP_ATTR)) {
			return getAttributesManagerImpl().getAllGroupValues(sess, attributeDefinition);
		} else {
			//TODO
			throw new InternalErrorException("Not implemented yet!");
		}
	}

	public boolean isTrulyRequiredAttribute(PerunSession sess, Facility facility, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		this.checkNamespace(sess, attributeDefinition, NS_FACILITY_ATTR);
		return getAttributesManagerImpl().isAttributeRequiredByFacility(sess, facility, attributeDefinition);
	}

	public boolean isTrulyRequiredAttribute(PerunSession sess, Vo vo, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		this.checkNamespace(sess, attributeDefinition, NS_VO_ATTR);
		return getAttributesManagerImpl().isAttributeRequiredByVo(sess, vo, attributeDefinition);
	}

	public boolean isTrulyRequiredAttribute(PerunSession sess, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		this.checkNamespace(sess, attributeDefinition, NS_GROUP_ATTR);
		return getAttributesManagerImpl().isAttributeRequiredByGroup(sess, group, attributeDefinition);
	}

	public boolean isTrulyRequiredAttribute(PerunSession sess, Resource resource, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		this.checkNamespace(sess, attributeDefinition, NS_RESOURCE_ATTR);
		return getAttributesManagerImpl().isAttributeRequiredByResource(sess, resource, attributeDefinition);
	}

	public boolean isTrulyRequiredAttribute(PerunSession sess, Member member, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		this.checkNamespace(sess, attributeDefinition, NS_MEMBER_ATTR);
		List<Resource> allowedResources = getPerunBl().getResourcesManagerBl().getAllowedResources(sess, member);
		for(Resource resource : allowedResources) {
			if(getAttributesManagerImpl().isAttributeRequiredByResource(sess, resource, attributeDefinition)) return true;
		}
		return false;
	}

	public boolean isTrulyRequiredAttribute(PerunSession sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		this.checkNamespace(sess, attributeDefinition, NS_USER_ATTR);
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for(Member member : members) {
			List<Resource> allowedResources = getPerunBl().getResourcesManagerBl().getAllowedResources(sess, member);
			for(Resource resource : allowedResources) {
				if(getAttributesManagerImpl().isAttributeRequiredByResource(sess, resource, attributeDefinition)) return true;
			}
		}
		return false;
	}

	public boolean isTrulyRequiredAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		this.checkNamespace(sess, attributeDefinition, NS_USER_FACILITY_ATTR);
		List<Facility> allowedFacilities = getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, user);
		if(!allowedFacilities.contains(facility)) {
			return false;
		} else {
			if(!getAttributesManagerImpl().isAttributeRequiredByFacility(sess, facility, attributeDefinition)) return false;
			List<Resource> resources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
			resources.retainAll(getPerunBl().getUsersManagerBl().getAllowedResources(sess, user));
			for(Resource resource : resources) {
				if(getAttributesManagerImpl().isAttributeRequiredByResource(sess, resource, attributeDefinition)) return true;
			}
			return false;
		}

	}

	public boolean isTrulyRequiredAttribute(PerunSession sess, Resource resource, Member member, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		this.checkMemberIsFromTheSameVoLikeResource(sess, member, resource);
		this.checkNamespace(sess, attributeDefinition, NS_MEMBER_RESOURCE_ATTR);
		List<Member> allowedMembers = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
		if(!allowedMembers.contains(member)) {
			return false;
		} else {
			return getAttributesManagerImpl().isAttributeRequiredByResource(sess, resource, attributeDefinition);
		}
	}

	public boolean isTrulyRequiredAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException {
		this.checkNamespace(sess, attributeDefinition, NS_MEMBER_GROUP_ATTR);
		List<Resource> assignedResources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		for (Resource resource : assignedResources) {
			if (getAttributesManagerImpl().isAttributeRequiredByResource(sess, resource, attributeDefinition)) return true;
		}
		return false;
	}

	public boolean isTrulyRequiredAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attributeDefinition) throws InternalErrorException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		this.checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		this.checkNamespace(sess, attributeDefinition, NS_GROUP_RESOURCE_ATTR);
		List<Group> assignedGroups = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
		if(!assignedGroups.contains(group)) {
			return false;
		} else {
			return getAttributesManagerImpl().isAttributeRequiredByResource(sess, resource, attributeDefinition);
		}
	}

	public Object stringToAttributeValue(String value, String type) throws InternalErrorException {
		if (type.equals(ArrayList.class.getName()) || type.equals(LinkedHashMap.class.getName()) ||
					type.equals(BeansUtils.largeArrayListClassName)) {
			if (value != null && !value.isEmpty() && !value.endsWith(String.valueOf(AttributesManagerImpl.LIST_DELIMITER))) {
				value = value.concat(String.valueOf(AttributesManagerImpl.LIST_DELIMITER));
			}
		}
		return BeansUtils.stringToAttributeValue(value, type);
	}

	public static String escapeListAttributeValue(String value) {
		return AttributesManagerImpl.escapeListAttributeValue(value);
	}

	public static String escapeMapAttributeValue(String value) {
		return AttributesManagerImpl.escapeMapAttributeValue(value);
	}

	public void doTheMagic(PerunSession sess, Member member) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		doTheMagic(sess, member, false);
	}

	public void doTheMagic(PerunSession sess, Member member, boolean trueMagic) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getAllowedResources(sess, member);
		for(Resource resource : resources) {
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
			List<Attribute> requiredAttributes = null;
			try {
				requiredAttributes = getResourceRequiredAttributes(sess, resource, facility, resource, user, member);
			} catch (MemberResourceMismatchException ex) {
				throw new ConsistencyErrorException(ex);
			}
			boolean allOk = false;
			AttributeDefinition lastWrongAttribute = null;
			int safetyCounter = 0;
			do {
				try {
					setRequiredAttributes(sess, facility, resource, user, member, requiredAttributes);
					allOk = true;
				} catch(AttributeNotExistsException ex) {
					throw new ConsistencyErrorException(ex);
				} catch(WrongAttributeAssignmentException ex) {
					throw new ConsistencyErrorException(ex);
				} catch(WrongAttributeValueException ex) {
					if(!trueMagic) throw ex;
					AttributeDefinition wrongAttributeDefinition = ex.getAttribute();
					if(wrongAttributeDefinition == null) throw new ConsistencyErrorException("WrongAttributeValueException doesn't have set the wrong attribute.", ex);
					if(wrongAttributeDefinition.equals(lastWrongAttribute)) throw new WrongAttributeValueException("Method doTheMagic can't fix this attribute value", ex);
					lastWrongAttribute = wrongAttributeDefinition;
					findAndSetValueInList(requiredAttributes, wrongAttributeDefinition, null);
				} catch(WrongReferenceAttributeValueException ex) {
					if(!trueMagic) throw ex;
					AttributeDefinition wrongAttributeDefinition = ex.getReferenceAttribute();
					if(wrongAttributeDefinition == null) throw new ConsistencyErrorException("WrongReferenceAttributeValueException doesn't have set reference attribute.", ex);
					if(wrongAttributeDefinition.equals(lastWrongAttribute)) throw new WrongReferenceAttributeValueException("Method doTheMagic can't fix this attribute value", ex);
					lastWrongAttribute = wrongAttributeDefinition;
					if(!findAndSetValueInList(requiredAttributes, wrongAttributeDefinition, null)) {
						//this attribute can't be fixed here
						throw ex;
					}
				} catch (MemberResourceMismatchException ex) {
					throw new ConsistencyErrorException(ex);
				}
				safetyCounter++;
				if(safetyCounter == 50) throw new InternalErrorException("Method doTheMagic possibly stays in infinite loop.");
			} while(trueMagic && !allOk);
		}
	}

	public void mergeAttributesValues(PerunSession sess, User user, List<Attribute> attributes)  throws InternalErrorException, WrongAttributeValueException,
			WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		for (Attribute attribute: attributes) {
			this.mergeAttributeValue(sess, user, attribute);
		}
	}

	public void mergeAttributesValues(PerunSession sess, Member member, List<Attribute> attributes)  throws InternalErrorException, WrongAttributeValueException,
			WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		for (Attribute attribute: attributes) {
			this.mergeAttributeValue(sess, member, attribute);
		}
	}

	/**
	 * Merges attribute value for supported attribute's namespaces if the attribute type is list or map. In other cases it only stores new value.
	 *
	 * If attribute has null value or it's value is same as value of attribute already stored in Perun, return stored attribute instead.
	 * If the type is list, new values are added to the current stored list.
	 * It the type is map, new values are added and existing are overwritten with new values, but only if there is any change.
	 *
	 * Supported namespaces
	 *  - user attributes
	 *  - member attributes
	 *
	 * @param sess
	 * @param attribute attribute to merge it's value if possible
	 * @param primaryHolder holder defines object for which is attribute stored in Perun
	 *
	 * @return attribute after merging his value
	 *
	 * @throws InternalErrorException if one of mandatory objects is null or some internal problem has occured
	 * @throws WrongAttributeValueException attribute value of set attribute is not correct
	 * @throws WrongReferenceAttributeValueException any reference attribute value is not correct
	 * @throws WrongAttributeAssignmentException if attribute is not from the same namespace defined by primaryHolder
	 */
	private Attribute mergeAttributeValue(PerunSession sess, Attribute attribute, PerunBean primaryHolder) throws InternalErrorException, WrongAttributeValueException,
			WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		//If attribute is null, throw an exception
		if(attribute == null) throw new InternalErrorException("Can't merge null attribute with anything!");
		if(primaryHolder == null) throw new InternalErrorException("Can't merge attribute value without notNull primaryHolder!");

		//Get stored attribute in Perun
		Attribute storedAttribute = null;
		try {
			if(primaryHolder instanceof User) {
				storedAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, (User) primaryHolder, attribute.getName());
			} else if(primaryHolder instanceof Member) {
				storedAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, (Member) primaryHolder, attribute.getName());
			} else {
				throw new InternalErrorException("Primary holder for attribute is not supported: " + primaryHolder);
			}
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException(e);
		}

		//if attribute to merge has null value or it's value is same as stored attribute's value, return the stored attribute
		convertEmptyAttrValueToNull(attribute);
		if(attribute.getValue() == null || Objects.equals(attribute.getValue(), storedAttribute.getValue())) return storedAttribute;

		// Check type ArrayList
		if (attribute.getType().equals(ArrayList.class.getName()) || attribute.getType().equals(BeansUtils.largeArrayListClassName)) {
			ArrayList<String> updatedList = (ArrayList<String>) storedAttribute.getValue();
			// If there were someting then find values which haven't been already stored
			if (updatedList != null) {
				for (String value : ((ArrayList<String>) attribute.getValue())) {
					if (!updatedList.contains(value)) {
						updatedList.add(value);
					}
				}
				attribute.setValue(updatedList);
			}
		// Check type LinkedHashMap
		} else if (attribute.getType().equals(LinkedHashMap.class.getName())) {
			//Find values which haven't been already stored
			LinkedHashMap<String, String> updatedMap = (LinkedHashMap<String, String>) storedAttribute.getValue();
			if (updatedMap != null) {
				LinkedHashMap<String, String> receivedMap = (LinkedHashMap<String, String>) attribute.getValue();
				updatedMap.putAll(receivedMap);
				attribute.setValue(updatedMap);
			}
		}

		//Other types as String, Integer, Boolean etc. will be replaced by new value (no way how to merge them properly)
		if(primaryHolder instanceof User) {
			getPerunBl().getAttributesManagerBl().setAttribute(sess, (User) primaryHolder, attribute);
		} else if(primaryHolder instanceof Member) {
			getPerunBl().getAttributesManagerBl().setAttribute(sess, (Member) primaryHolder, attribute);
		}  else {
			throw new InternalErrorException("Primary holder for attribute is not supported: " + primaryHolder);
		}

		return attribute;
	}

	public Attribute mergeAttributeValue(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException,
				 WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		return this.mergeAttributeValue(sess, attribute, user);
	}

	public Attribute mergeAttributeValue(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException,
			WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		return this.mergeAttributeValue(sess, attribute, member);
	}

	public Attribute mergeAttributeValueInNestedTransaction(PerunSession sess, User user, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		return mergeAttributeValue(sess, user, attribute);
	}

	public Attribute mergeAttributeValueInNestedTransaction(PerunSession sess, Member member, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
		return mergeAttributeValue(sess, member, attribute);
	}

	private boolean findAndSetValueInList(List<Attribute> attributes, AttributeDefinition attributeDefinition, Object value) {
		for(Attribute attribute : attributes) {
			if(attribute.getId() == attributeDefinition.getId()) {
				attribute.setValue(value);
				return true;
			}
		}
		return false;
	}

	public AttributeDefinition updateAttributeDefinition(PerunSession perunSession, AttributeDefinition attributeDefinition) throws InternalErrorException {
		getPerunBl().getAuditer().log(perunSession, "{} updated.", attributeDefinition);
		return getAttributesManagerImpl().updateAttributeDefinition(perunSession, attributeDefinition);
	}

	private void checkAttributesDependencies(PerunSession sess, Resource resource, Group group, List<Attribute> attributes, boolean workWithGroupAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if(workWithGroupAttributes) {
			List<Attribute> groupAttributes = new ArrayList<Attribute>();
			List<Attribute> groupResourceAttributes = new ArrayList<Attribute>();
			for(Attribute attr: attributes) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_GROUP_RESOURCE_ATTR)) {
					groupResourceAttributes.add(attr);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_GROUP_ATTR)) {
					groupAttributes.add(attr);
				} else {
					throw new WrongAttributeAssignmentException(attr);
				}
			}
			checkAttributesDependencies(sess, resource, group, groupResourceAttributes);
			checkAttributesDependencies(sess, group, null, groupAttributes);
		} else {
			checkAttributesDependencies(sess, resource, group, attributes);
		}
	}

	private void checkAttributesDependencies(PerunSession sess, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if(workWithUserAttributes) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			List<Attribute> userAttributes = new ArrayList<Attribute>();
			List<Attribute> memberAttributes = new ArrayList<Attribute>();
			for(Attribute attr: attributes) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
					userAttributes.add(attr);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
					memberAttributes.add(attr);
				} else {
					throw new WrongAttributeAssignmentException(attr);
				}
			}
			checkAttributesDependencies(sess, member, null, memberAttributes);
			checkAttributesDependencies(sess, user, null, userAttributes);
		} else {
			checkAttributesDependencies(sess, member, null, attributes);
		}
	}

	private void checkAttributesDependencies(PerunSession sess, Resource resource, Member member, User user, Facility facility, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		List<Attribute> userAttributes = new ArrayList<Attribute>();
		List<Attribute> memberAttributes = new ArrayList<Attribute>();
		List<Attribute> memberResourceAttributes = new ArrayList<Attribute>();
		List<Attribute> userFacilityAttributes = new ArrayList<Attribute>();
		for(Attribute attr: attributes) {
			if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				userAttributes.add(attr);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
				memberAttributes.add(attr);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				memberResourceAttributes.add(attr);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				userFacilityAttributes.add(attr);
			} else {
				throw new WrongAttributeAssignmentException(attr);
			}
		}
		checkAttributesDependencies(sess, member, null, memberAttributes);
		checkAttributesDependencies(sess, user, null, userAttributes);
		checkAttributesDependencies(sess, facility, user, userFacilityAttributes);
		checkAttributesDependencies(sess, resource, member, memberResourceAttributes);
	}

	private void checkAttributesDependencies(PerunSession sess, Resource resource, Group group, Member member, User user, Facility facility, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		List<Attribute> userAttributes = new ArrayList<Attribute>();
		List<Attribute> memberAttributes = new ArrayList<Attribute>();
		List<Attribute> memberResourceAttributes = new ArrayList<Attribute>();
		List<Attribute> userFacilityAttributes = new ArrayList<Attribute>();
		List<Attribute> memberGroupAttributes = new ArrayList<Attribute>();
		for(Attribute attr: attributes) {
			if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
				userAttributes.add(attr);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
				memberAttributes.add(attr);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
				memberResourceAttributes.add(attr);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
				userFacilityAttributes.add(attr);
			} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_MEMBER_GROUP_ATTR)) {
				memberGroupAttributes.add(attr);
			} else {
				throw new WrongAttributeAssignmentException(attr);
			}
		}
		checkAttributesDependencies(sess, member, null, memberAttributes);
		checkAttributesDependencies(sess, user, null, userAttributes);
		checkAttributesDependencies(sess, facility, user, userFacilityAttributes);
		checkAttributesDependencies(sess, resource, member, memberResourceAttributes);
		checkAttributesDependencies(sess, member, group, memberGroupAttributes);
	}

	private void checkAttributesDependencies(PerunSession sess, Resource resource, Member member, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if(workWithUserAttributes) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
			List<Attribute> userAttributes = new ArrayList<Attribute>();
			List<Attribute> memberAttributes = new ArrayList<Attribute>();
			List<Attribute> memberResourceAttributes = new ArrayList<Attribute>();
			List<Attribute> userFacilityAttributes = new ArrayList<Attribute>();
			for(Attribute attr: attributes) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
					userAttributes.add(attr);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
					memberAttributes.add(attr);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_MEMBER_RESOURCE_ATTR)) {
					memberResourceAttributes.add(attr);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_USER_FACILITY_ATTR)) {
					userFacilityAttributes.add(attr);
				} else {
					throw new WrongAttributeAssignmentException(attr);
				}
			}
			checkAttributesDependencies(sess, member, null, memberAttributes);
			checkAttributesDependencies(sess, user, null, userAttributes);
			checkAttributesDependencies(sess, facility, user, userFacilityAttributes);
			checkAttributesDependencies(sess, resource, member, memberResourceAttributes);
		} else {
			checkAttributesDependencies(sess, resource, member, attributes);
		}
	}

	private void checkAttributesDependencies(PerunSession sess, Member member, Group group, List<Attribute> attributes, boolean workWithUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if(workWithUserAttributes) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			List<Attribute> userAttributes = new ArrayList<Attribute>();
			List<Attribute> memberAttributes = new ArrayList<Attribute>();
			List<Attribute> memberGroupAttributes = new ArrayList<Attribute>();
			for(Attribute attr: attributes) {
				if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_USER_ATTR)) {
					userAttributes.add(attr);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_MEMBER_ATTR)) {
					memberAttributes.add(attr);
				} else if(getAttributesManagerImpl().isFromNamespace(sess, attr, NS_MEMBER_GROUP_ATTR)) {
					memberGroupAttributes.add(attr);
				} else {
					throw new WrongAttributeAssignmentException(attr);
				}
			}
			checkAttributesDependencies(sess, member, null, memberAttributes);
			checkAttributesDependencies(sess, user, null, userAttributes);
			checkAttributesDependencies(sess, member, group, memberGroupAttributes);
		} else {
			checkAttributesDependencies(sess, member, group, attributes);
		}
	}

	private void checkAttributesDependencies(PerunSession sess, Object primaryHolder, Object secondaryHolder, List<Attribute> attributes) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if(attributes != null && !attributes.isEmpty()) {
			for(Attribute attr: attributes) {
				checkAttributeDependencies(sess, new RichAttribute<>(primaryHolder, secondaryHolder, attr));
			}
		}
	}

	public void checkAttributeDependencies(PerunSession sess, RichAttribute richAttr) throws InternalErrorException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
		if(getAllDependencies() == null || getAllDependencies().isEmpty()) log.error("Map of all dependencies is empty. If this is not test, its an error probably.");
		if(richAttr == null || richAttr.getAttribute() == null) throw new InternalErrorException("RichAttribute or Attribute in it can't be null!");
		else {
			//Get All attributeDef which are dependencies
			Set<AttributeDefinition> dependencies = getAllDependencies().get(new AttributeDefinition(richAttr.getAttribute()));
			if(dependencies != null && !dependencies.isEmpty() ) {
				for(AttributeDefinition dependency: dependencies) {
					List<RichAttribute> richAttributesToCheck;
					try {
						richAttributesToCheck = getRichAttributesWithHoldersForAttributeDefinition(sess, dependency, richAttr);
					} catch (AttributeNotExistsException | VoNotExistsException | UserNotExistsException ex) {
						//TODO better exception here (need to implement to all setMethods)
						throw new InternalErrorException(ex);
					} catch (MemberResourceMismatchException ex) {
						throw new InternalErrorException(ex);
					} catch (GroupResourceMismatchException ex) {
						throw new InternalErrorException(ex);
					}
					for(RichAttribute richAttribute: richAttributesToCheck) {
						if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), NS_VO_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Vo) {
								if(richAttribute.getSecondaryHolder() != null) {
									throw new InternalErrorException("Secondary Holder for VO Attribute must be null!");
								} else {
									this.checkAttributeValue(sess, (Vo) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
								}
							} else {
								throw new InternalErrorException("For VO Attribute there must be VO in primaryHolder");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), NS_GROUP_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Group) {
								if(richAttribute.getSecondaryHolder() != null) {
									throw new InternalErrorException("Secondary Holder for Group Attribute must be null!");
								} else {
									this.checkAttributeValue(sess, (Group) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
								}
							} else {
								throw new InternalErrorException("For Group Attribute there must be Group in primaryHolder");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), NS_MEMBER_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Member) {
								if(richAttribute.getSecondaryHolder() != null) {
									throw new InternalErrorException("Secondary Holder for Member Attribute must be null!");
								} else {
									this.checkAttributeValue(sess, (Member) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
								}
							} else {
								throw new InternalErrorException("For Member Attribute there must be Member in primaryHolder");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), NS_USER_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof User) {
								if(richAttribute.getSecondaryHolder() != null) {
									throw new InternalErrorException("Secondary Holder for User Attribute must be null!");
								} else {
									this.checkAttributeValue(sess, (User) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
								}
							} else {
								throw new InternalErrorException("For User Attribute there must be User in primaryHolder");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), NS_RESOURCE_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Resource) {
								if(richAttribute.getSecondaryHolder() != null) {
									throw new InternalErrorException("Secondary Holder for Resource Attribute must be null!");
								} else {
									this.checkAttributeValue(sess, (Resource) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
								}
							} else {
								throw new InternalErrorException("For Resource Attribute there must be Resource in primaryHolder");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), NS_FACILITY_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Facility) {
								if(richAttribute.getSecondaryHolder() != null) {
									throw new InternalErrorException("Secondary Holder for Facility Attribute must be null!");
								} else {
									this.checkAttributeValue(sess, (Facility) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
								}
							} else {
								throw new InternalErrorException("For Facility Attribute there must be Facility in primaryHolder");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), AttributesManager.NS_ENTITYLESS_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof String) {
								if(richAttribute.getSecondaryHolder() != null) {
									throw new InternalErrorException("Secondary Holder for Entityless Attribute must be null!");
								} else {
									this.checkAttributeValue(sess, (String) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
								}
							} else {
								throw new InternalErrorException("For Entityless Attribute there must be String in primaryHolder");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), AttributesManager.NS_HOST_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Host) {
								if(richAttribute.getSecondaryHolder() != null) {
									throw new InternalErrorException("Secondary Holder for Host Attribute must be null!");
								} else {
									this.checkAttributeValue(sess, (Host) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
								}
							} else {
								throw new InternalErrorException("For Host Attribute there must be Host in primaryHolder");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), NS_GROUP_RESOURCE_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Resource) {
								if(richAttribute.getSecondaryHolder() != null && richAttribute.getSecondaryHolder() instanceof Group) {
									try {
										this.checkAttributeValue(sess, (Resource) richAttribute.getPrimaryHolder(), (Group) richAttribute.getSecondaryHolder(), richAttribute.getAttribute());
									} catch (GroupResourceMismatchException ex) {
										throw new ConsistencyErrorException(ex);
									}
								} else {
									throw new InternalErrorException("Secondary Holder for Group_Resource Attribute is null or its not group or resource");
								}
							} else if(richAttribute.getSecondaryHolder() != null && richAttribute.getSecondaryHolder() instanceof Resource) {
								if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Group) {
									try {
										this.checkAttributeValue(sess, (Resource) richAttribute.getSecondaryHolder(), (Group) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
									} catch (GroupResourceMismatchException ex) {
										throw new ConsistencyErrorException(ex);
									}
								} else {
									throw new InternalErrorException("Secondary Holder for Group_Resource Attribute is null or its not group or resource");
								}
							} else {
								throw new InternalErrorException("For Group_Resource Attribute there must be Group or Resource in primaryHolder.");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), NS_MEMBER_RESOURCE_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Resource) {
								if(richAttribute.getSecondaryHolder() != null && richAttribute.getSecondaryHolder() instanceof Member) {
									try {
										this.checkAttributeValue(sess, (Resource) richAttribute.getPrimaryHolder(), (Member) richAttribute.getSecondaryHolder(), richAttribute.getAttribute());
									} catch (MemberResourceMismatchException ex) {
										throw new ConsistencyErrorException(ex);
									}
								} else {
									throw new InternalErrorException("Secondary Holder for Member_Resource Attribute is null or its not member or resource");
								}
							} else if(richAttribute.getSecondaryHolder() != null && richAttribute.getSecondaryHolder() instanceof Resource) {
								if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Member) {
									try {
										this.checkAttributeValue(sess, (Resource) richAttribute.getSecondaryHolder(), (Member) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
									} catch (MemberResourceMismatchException ex) {
										throw new ConsistencyErrorException(ex);
									}
								} else {
									throw new InternalErrorException("Secondary Holder for Member_Resource Attribute is null or its not member or resource");
								}
							} else {
								throw new InternalErrorException("For Member_Resource Attribute there must be Member or Resource in primaryHolder.");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), NS_MEMBER_GROUP_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Group) {
								if(richAttribute.getSecondaryHolder() != null && richAttribute.getSecondaryHolder() instanceof Member) {
									this.checkAttributeValue(sess, (Member) richAttribute.getSecondaryHolder(), (Group) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
								} else {
									throw new InternalErrorException("Secondary Holder for Member_Group Attribute is null or its not member or group");
								}
							} else if(richAttribute.getSecondaryHolder() != null && richAttribute.getSecondaryHolder() instanceof Group) {
								if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Member) {
									this.checkAttributeValue(sess, (Member) richAttribute.getPrimaryHolder(), (Group) richAttribute.getSecondaryHolder(), richAttribute.getAttribute());
								} else {
									throw new InternalErrorException("Secondary Holder for Member_Group Attribute is null or its not member or group");
								}
							} else {
								throw new InternalErrorException("For Member_Group Attribute there must be Member or Group in primaryHolder.");
							}
						} else if(getAttributesManagerImpl().isFromNamespace(sess, richAttribute.getAttribute(), NS_USER_FACILITY_ATTR)) {
							if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof Facility) {
								if(richAttribute.getSecondaryHolder() != null && richAttribute.getSecondaryHolder() instanceof User) {
									this.checkAttributeValue(sess, (Facility) richAttribute.getPrimaryHolder(), (User) richAttribute.getSecondaryHolder(), richAttribute.getAttribute());
								} else {
									throw new InternalErrorException("Secondary Holder for Facility_User Attribute is null or its not facility or user");
								}
							} else if(richAttribute.getSecondaryHolder() != null && richAttribute.getSecondaryHolder() instanceof Facility) {
								if(richAttribute.getPrimaryHolder() != null && richAttribute.getPrimaryHolder() instanceof User) {
									this.checkAttributeValue(sess, (Facility) richAttribute.getSecondaryHolder(), (User) richAttribute.getPrimaryHolder(), richAttribute.getAttribute());
								} else {
									throw new InternalErrorException("Secondary Holder for Facility_User Attribute is null or its not facility or user");
								}
							} else {
								throw new InternalErrorException("For Facility_User Attribute there must be Facility or User in primaryHolder.");
							}
						}
					}
				}
			}
		}
	}

	public List<RichAttribute> getRichAttributesWithHoldersForAttributeDefinition(PerunSession sess, AttributeDefinition attrDef, RichAttribute aidingAttr) throws InternalErrorException, AttributeNotExistsException, UserNotExistsException, VoNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException, MemberResourceMismatchException {
		//Filling objects from aidingAttr
		if(aidingAttr == null) throw new InternalErrorException("Aiding attribute cant be null.");
		if(attrDef == null) throw new InternalErrorException("attrDef cant be null.");

		List<RichAttribute> listOfRichAttributes = new ArrayList<>();

		//All possible useful objects
		Vo vo = null;
		Facility facility = null;
		Group group = null;
		Member member = null;
		User user = null;
		Host host = null;
		Resource resource = null;
		String key = null;
		UserExtSource userExtSource = null;

		//Get object for primaryHolder of aidingAttr
		if(aidingAttr.getPrimaryHolder() != null) {
			if(aidingAttr.getPrimaryHolder() instanceof Vo) vo = (Vo) aidingAttr.getPrimaryHolder();
			else if(aidingAttr.getPrimaryHolder() instanceof Facility) facility = (Facility) aidingAttr.getPrimaryHolder();
			else if(aidingAttr.getPrimaryHolder() instanceof Group) group = (Group) aidingAttr.getPrimaryHolder();
			else if(aidingAttr.getPrimaryHolder() instanceof Member) member = (Member) aidingAttr.getPrimaryHolder();
			else if(aidingAttr.getPrimaryHolder() instanceof User) user = (User) aidingAttr.getPrimaryHolder();
			else if(aidingAttr.getPrimaryHolder() instanceof Host) host = (Host) aidingAttr.getPrimaryHolder();
			else if(aidingAttr.getPrimaryHolder() instanceof Resource) resource = (Resource) aidingAttr.getPrimaryHolder();
			else if(aidingAttr.getPrimaryHolder() instanceof UserExtSource) userExtSource = (UserExtSource) aidingAttr.getPrimaryHolder();
			else if(aidingAttr.getPrimaryHolder() instanceof String) key = (String) aidingAttr.getPrimaryHolder();
			else {
				throw new InternalErrorException("There is unrecognized object in primaryHolder of aidingAttr.");
			}
		} else {
			throw new InternalErrorException("Aiding attribute must have primaryHolder which is not null.");
		}

		//Get object for secondaryHolder of aidingAttr
		if(aidingAttr.getSecondaryHolder() != null) {
			if(aidingAttr.getSecondaryHolder() instanceof Vo) vo = (Vo) aidingAttr.getSecondaryHolder();
			else if(aidingAttr.getSecondaryHolder() instanceof Facility) facility = (Facility) aidingAttr.getSecondaryHolder();
			else if(aidingAttr.getSecondaryHolder() instanceof Group) group = (Group) aidingAttr.getSecondaryHolder();
			else if(aidingAttr.getSecondaryHolder() instanceof Member) member = (Member) aidingAttr.getSecondaryHolder();
			else if(aidingAttr.getSecondaryHolder() instanceof User) user = (User) aidingAttr.getSecondaryHolder();
			else if(aidingAttr.getSecondaryHolder() instanceof Host) host = (Host) aidingAttr.getSecondaryHolder();
			else if(aidingAttr.getSecondaryHolder() instanceof Resource) resource = (Resource) aidingAttr.getSecondaryHolder();
			else if(aidingAttr.getSecondaryHolder() instanceof UserExtSource) userExtSource = (UserExtSource) aidingAttr.getSecondaryHolder();
			else if(aidingAttr.getSecondaryHolder() instanceof String) key = (String) aidingAttr.getSecondaryHolder();
			else {
				throw new InternalErrorException("There is unrecognized object in secondaryHolder of aidingAttr");
			}
		} // If not, its ok, secondary holder can be null

		//First i choose what i am looking for by descriptionAttr
		//Second on the fact what i really have in aidingAttr i try to find what i am looking for
		//IMPORTANT: If member is not allowed on connected objects (INVALID or DISABLED status), we skip these objects

		if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_VO_ATTR)) {

			if(resource != null && member != null) {
				//we do not need object resource to resolve this case
				listOfRichAttributes.addAll(this.getVoAttributes(sess, member, attrDef));
			} else if(group != null && resource != null) {
				//we do not need object resource to resolve this case
				listOfRichAttributes.addAll(this.getVoAttributes(sess, group, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getVoAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				//we do not need object group to resolve this case
				listOfRichAttributes.addAll(this.getVoAttributes(sess, member, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getVoAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getVoAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getVoAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getVoAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getVoAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getVoAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getVoAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getVoAttributes(sess, userExtSource, attrDef));
			} else if(key != null){
				listOfRichAttributes.addAll(this.getVoAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_GROUP_ATTR)) {
			if(resource != null && member != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, member, resource, attrDef));
			} else if(group != null && resource != null) {
				//we do not need to use the resource object here
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, group, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, member, group, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getGroupAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_FACILITY_ATTR)) {
			if(resource != null && member != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, member, resource, attrDef));
			} else if(group != null && resource != null) {
				//we do not need to use the group object here
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, resource, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, member, group, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getFacilityAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_MEMBER_ATTR)) {
			if(resource != null && member != null) {
				//we do not need to use the resource object here
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, member, attrDef));
			} else if(group != null && resource != null) {
				//we do not need to use the resource object here
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, group, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				//we do not need to use the group object here
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, member, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getMemberAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_RESOURCE_ATTR)) {
			if(resource != null && member != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, member, resource, attrDef));
			} else if(group != null && resource != null) {
				//we do not need to use the group object here
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, resource, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, member, group, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getResourceAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_USER_ATTR)) {
			if(resource != null && member != null) {
				//we do not need to use the resource object here
				listOfRichAttributes.addAll(this.getUserAttributes(sess, member, attrDef));
			} else if(group != null && resource != null) {
				//we do not need to use the resource object here
				listOfRichAttributes.addAll(this.getUserAttributes(sess, group, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getUserAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				//we do not need to use the group object here
				listOfRichAttributes.addAll(this.getUserAttributes(sess, member, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getUserAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getUserAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getUserAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getUserAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getUserAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getUserAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getUserAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getUserAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getUserAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, AttributesManager.NS_HOST_ATTR)) {
			if(resource != null && member != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, member, resource, attrDef));
			} else if(group != null && resource != null) {
				//we do not need to user the group object here
				listOfRichAttributes.addAll(this.getHostAttributes(sess, resource, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, member, group, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getHostAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_GROUP_RESOURCE_ATTR)) {
			if(resource != null && member != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, member, resource, attrDef));
			} else if(group != null && resource != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, group, resource, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, member, group, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getGroupResourceAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if (getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_MEMBER_GROUP_ATTR)) {
			if(resource != null && member != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, member, resource, attrDef));
			} else if(group != null && resource != null) {
				//we do not need to use the resource object
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, group, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, member, group, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getMemberGroupAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_MEMBER_RESOURCE_ATTR)) {
			if(resource != null && member != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, member, resource, attrDef));
			} else if(group != null && resource != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, group, resource, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, member, group, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getMemberResourceAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_USER_FACILITY_ATTR)) {
			if(resource != null && member != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, member, resource, attrDef));
			} else if(group != null && resource != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, group, resource, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, member, group, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getUserFacilityAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, NS_UES_ATTR)) {
			if(resource != null && member != null) {
				//we do not need to use the resource object here
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, member, attrDef));
			} else if(group != null && resource != null) {
				//we do not need to use the resource object here
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, group, attrDef));
			} else if(user != null && facility != null) {
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, user, facility, attrDef));
			} else if (member != null && group != null) {
				//we do not need to use the group object here
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, member, attrDef));
			} else if(group != null) {
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, group, attrDef));
			} else if(member != null) {
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, member, attrDef));
			} else if(resource != null) {
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, resource, attrDef));
			} else if(user != null) {
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, user, attrDef));
			} else if(host != null) {
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, host, attrDef));
			} else if(facility != null) {
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, facility, attrDef));
			} else if(vo != null) {
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, vo, attrDef));
			} else if(userExtSource != null) {
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, userExtSource, attrDef));
			} else if(key != null) {
				listOfRichAttributes.addAll(this.getUserExtSourceAttributes(sess, key, attrDef));
			} else {
				throw new InternalErrorException("Unknown combination of PerunBeans: " + aidingAttr );
			}

		} else if(getAttributesManagerImpl().isFromNamespace(sess, attrDef, AttributesManager.NS_ENTITYLESS_ATTR)) {
			if(key != null) {
				listOfRichAttributes.addAll(getEntitylessAttributes(sess, key, attrDef));
			} else {
				listOfRichAttributes.addAll(getEntitylessAttributes(sess, attrDef));
			}

		} else {
			throw new InternalErrorException("There is unrecognized namespace in attribute " + attrDef);
		}

		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
    return listOfRichAttributes;
	}

	/**
	 * Checks if the attributes represent empty values. If so, converts them into null.
	 *
	 * @param attributes attributes to be checked if are null
	 */
	public void convertEmptyAttrValueToNull(List<Attribute> attributes) throws ConsistencyErrorException {
		for (Attribute attribute : attributes) {
			convertEmptyAttrValueToNull(attribute);
		}
	}

	/**
	 * Checks if the attribute represents empty value. If so, converts it into null.
	 *
	 * @param attribute attribute to be checked if is null
	 */
	public void convertEmptyAttrValueToNull(Attribute attribute) throws ConsistencyErrorException {
		if(attribute.getValue() == null) {
			return;
		}
		if (attribute.getValue() instanceof Integer) {
			return;
		}
		if (attribute.getValue() instanceof String) {
			if(((String)attribute.getValue()).matches("\\s*")) {
				attribute.setValue(null);
			}
		} else if (attribute.getValue() instanceof Boolean) {
			if(attribute.getValue().equals(Boolean.FALSE)) {
				attribute.setValue(null);
			}
		} else if (attribute.getValue() instanceof ArrayList) {
			if(((ArrayList)attribute.getValue()).isEmpty()) {
				attribute.setValue(null);
			}
		} else if (attribute.getValue() instanceof LinkedHashMap) {
			if(((LinkedHashMap)attribute.getValue()).isEmpty()) {
				attribute.setValue(null);
			}
		} else {
			throw new ConsistencyErrorException("Invalid attribute value type: " + attribute.getType() +
			                                    " class: " + attribute.getValue().getClass().getName());
		}
	}

	/**
	 * Gets the getAttributesManagerImpl().
	 *
	 * @return The getAttributesManagerImpl().
	 */
	public AttributesManagerImplApi getAttributesManagerImpl() {
		return this.attributesManagerImpl;
	}

	/**
	 * Gets the perunBl.
	 *
	 * @return The perunBl.
	 */
	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	private List<Attribute> attributesFromDefinitions(List<? extends AttributeDefinition> attributesDefinition) {
		List<Attribute> attributes = new ArrayList<Attribute>(attributesDefinition.size());
		for(AttributeDefinition attributeDefinition : attributesDefinition) {
			attributes.add(new Attribute(attributeDefinition));
		}
		return attributes;
	}

	protected void initialize() throws InternalErrorException {
		log.debug("AttributesManagerBlImpl initialize started.");

		//Get PerunSession
		String attributesManagerInitializator = "attributesManagerBlImplInitializator";
		PerunPrincipal pp = new PerunPrincipal(attributesManagerInitializator, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		PerunSession sess = perunBl.getPerunSession(pp, new PerunClient());

		//Load all attributes modules
		ServiceLoader<AttributesModuleImplApi> attributeModulesLoader = ServiceLoader.load(AttributesModuleImplApi.class);
		for(AttributesModuleImplApi module : attributeModulesLoader) {
			attributesModulesMap.put(module.getClass().getName(), module);
			if(module instanceof VirtualAttributesModuleImplApi) {
				Auditer.registerAttributeModule((VirtualAttributesModuleImplApi) module);
			}
			log.debug("Module " + module.getClass().getSimpleName() + " loaded.");
		}

		//Check if all core atributes exists, create if doesn't
		Map<AttributeDefinition, List<AttributeRights>> attributes = new HashMap<>();
		//Facility.id
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Facility id");
		//set attribute rights (with dummy id of attribute - not known yet)
		List<AttributeRights> rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Facility.name
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("name");
		attr.setDisplayName("Facility name");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Resource.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Resource id");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Resource.name
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("name");
		attr.setDisplayName("Resource name");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Resource.description
		attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("description");
		attr.setDisplayName("Resource description");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Member.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Member id");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//User.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("User id");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//User.firstName
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("firstName");
		attr.setDisplayName("User first name");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//User.lastName
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("lastName");
		attr.setDisplayName("User last name");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//User.middleName
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("middleName");
		attr.setDisplayName("User middle name");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//User.titleBefore
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("titleBefore");
		attr.setDisplayName("User title before");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//User.titleAfter
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("titleAfter");
		attr.setDisplayName("User title after");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//User.serviceUser
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(Boolean.class.getName());
		attr.setFriendlyName("serviceUser");
		attr.setDisplayName("If user is service user or not.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		attributes.put(attr, rights);

		//User.displayName
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("displayName");
		attr.setDisplayName("Display name");
		attr.setDescription("Displayed user's name.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Group.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Group id");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Group.name
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("name");
		attr.setDisplayName("Group full name");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Group.description
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("description");
		attr.setDisplayName("Group description");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Group.parentGroupId
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("parentGroupId");
		attr.setDisplayName("Id of group's parent group.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		attributes.put(attr, rights);

		//Vo.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Vo id");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		attributes.put(attr, rights);

		//Vo.name
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("name");
		attr.setDisplayName("Vo full name");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Vo.createdAt
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("createdAt");
		attr.setDisplayName("Vo created date");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		attributes.put(attr, rights);

		//Vo.shortName
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_VO_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("shortName");
		attr.setDisplayName("Vo short name");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Host.id
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_HOST_ATTR_CORE);
		attr.setType(Integer.class.getName());
		attr.setFriendlyName("id");
		attr.setDisplayName("Host id");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//Host.hostname
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_HOST_ATTR_CORE);
		attr.setType(String.class.getName());
		attr.setFriendlyName("hostname");
		attr.setDisplayName("Host hostname");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		// *** Def attributes

		//urn:perun:user:attribute-def:def:organization
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("organization");
		attr.setDisplayName("Organization");
		attr.setDescription("Organization, from which user comes from. Provided by IDP.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:user:attribute-def:def:preferredMail
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("preferredMail");
		attr.setDisplayName("Preferred mail");
		attr.setDescription("E-mail address preferred for communication.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:user:attribute-def:def:phone
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("phone");
		attr.setDisplayName("Phone");
		attr.setDescription("Phone number in organization. Provided by IDP.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:member:attribute-def:def:mail
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_MEMBER_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("mail");
		attr.setDisplayName("Mail");
		attr.setDescription("E-mail address in organization (VO wide).");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:vo:attribute-def:def:membershipExpirationRules
		attr = new AttributeDefinition();
		attr.setDisplayName("Membership expiration rules");
		attr.setFriendlyName("membershipExpirationRules");
		attr.setNamespace("urn:perun:vo:attribute-def:def");
		attr.setDescription("Set of rules to determine date of membership expiration. If not set, membership is not limited.");
		attr.setType(String.class.getName());
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		attributes.put(attr, rights);


		//urn:perun:group:attribute-def:def:groupExtSource
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("groupExtSource");
		attr.setDisplayName("Group extSource");
		attr.setDescription("External source from which group comes from. Used for groups synchronization.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:group:attribute-def:def:groupMembersExtSource
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("groupMembersExtSource");
		attr.setDisplayName("Group members extSource");
		attr.setDescription("External source from which group members comes from. Used for group synchronization. If not set, members are loaded from the same external source as group itself.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:group:attribute-def:def:groupMembersQuery
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("groupMembersQuery");
		attr.setDisplayName("Group members query");
		attr.setDescription("Query (SQL) on external source which retrieves list of it's members.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:group:attribute-def:def:synchronizationEnabled
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("synchronizationEnabled");
		attr.setDisplayName("Group synchronization enabled");
		attr.setDescription("Enables group synchronization from external source.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:group:attribute-def:def:synchronizationInterval
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("synchronizationInterval");
		attr.setDisplayName("Synchronization interval");
		attr.setDescription("Time between two successful synchronizations.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:group:attribute-def:def:lastSynchronizationState
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("lastSynchronizationState");
		attr.setDisplayName("Last synchronization state");
		attr.setDescription("If group is synchronized, there will be information about state of last synchronization.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:group:attribute-def:def:lastSynchronizationTimestamp
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("lastSynchronizationTimestamp");
		attr.setDisplayName("Last Synchronization timestamp");
		attr.setDescription("If group is synchronized, there will be the last timestamp of group synchronization.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:group:attribute-def:def:lightweightSynchronization
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("lightweightSynchronization");
		attr.setDisplayName("Lightweight Synchronization");
		attr.setDescription("If true, then do not update actual members.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:group:attribute-def:def:lastSuccessSynchronizationTimestamp
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("lastSuccessSynchronizationTimestamp");
		attr.setDisplayName("Last successful synchronization timestamp");
		attr.setDescription("If group is synchronized, there will be timestamp of last successful synchronization.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:group:attribute-def:def:authoritativeGroup
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("authoritativeGroup");
		attr.setDisplayName("Authoritative Group");
		attr.setDescription("If group is authoritative for member. (for synchronization)");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		//urn:perun:facility:attribute-def:def:login-namespace
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_FACILITY_ATTR_DEF);
		attr.setType(String.class.getName());
		attr.setFriendlyName("login-namespace");
		attr.setDisplayName("Login namespace");
		attr.setDescription("Define namespace for all user's logins on Facility.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ, ActionType.WRITE)));
		attributes.put(attr, rights);

		//urn:perun:user_facility:attribute-def:virt:login
		attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_FACILITY_ATTR_VIRT);
		attr.setType(String.class.getName());
		attr.setFriendlyName("login");
		attr.setDisplayName("Login");
		attr.setDescription("User's logname at facility. Value is determined automatically from all user's logins by Facility's namespace.");
		//set attribute rights (with dummy id of attribute - not known yet)
		rights = new ArrayList<>();
		rights.add(new AttributeRights(-1, Role.SELF, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.VOADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.GROUPADMIN, Arrays.asList(ActionType.READ)));
		rights.add(new AttributeRights(-1, Role.FACILITYADMIN, Arrays.asList(ActionType.READ)));
		attributes.put(attr, rights);

		if(perunBl.isPerunReadOnly()) log.debug("Loading attributes manager init in readOnly version.");

		for(Map.Entry<AttributeDefinition, List<AttributeRights>> entry : attributes.entrySet()) {
			AttributeDefinition attribute = entry.getKey();
			List<AttributeRights> listOfRights = entry.getValue();
			try {
				// If attribute definition is not found, catch exception and create this attribute definition
				getAttributeDefinition(sess,attribute.getName());
			} catch (AttributeNotExistsException e) {
				if(perunBl.isPerunReadOnly()) {
					throw new InternalErrorException("There is missing required attribute " + attribute + " and can't be created because this instance is read only.");
				} else {
					try {
						attribute = createAttribute(sess, attribute);
					} catch (AttributeDefinitionExistsException ex) {
						//should not happen
						throw new InternalErrorException("Attribute " + attribute + " already exists in Perun when attributeInitializer tried to create it.");
					}
					//set correct id of attribute to rights
					for (int i = 0; i < listOfRights.size(); i++) {
						listOfRights.get(i).setAttributeId(attribute.getId());
					}
					setAttributeRights(sess, listOfRights);
				}
			}
		}

		//Prepare all attribute definition from system perun
		Set<AttributeDefinition> allAttributesDef = new HashSet<AttributeDefinition>();
		allAttributesDef.addAll(this.getAttributesDefinition(sess));

		//Basic state of all maps (record for every existing attributeDefinitions)
		for(AttributeDefinition ad: allAttributesDef) {
			dependencies.put(ad, new HashSet<AttributeDefinition>());
			strongDependencies.put(ad, new HashSet<AttributeDefinition>());
			inverseDependencies.put(ad, new HashSet<AttributeDefinition>());
			inverseStrongDependencies.put(ad, new HashSet<AttributeDefinition>());
			allDependencies.put(ad, new HashSet<AttributeDefinition>());
		}

		log.debug("Dependencies and StrongDependencies filling started.");

		//Fill dep and strongDep maps
		for(AttributeDefinition ad: allAttributesDef) {
			AttributesModuleImplApi module = null;
			List<String> depList = new ArrayList<String>();
			List<String> strongDepList = new ArrayList<String>();
			Set<AttributeDefinition> depSet = new HashSet<AttributeDefinition>();
			Set<AttributeDefinition> strongDepSet = new HashSet<AttributeDefinition>();

			//Return null to object if module not exist
			Object attributeModule = getAttributesManagerImpl().getAttributesModule(sess, ad);

			//If there is any existing module
			if(attributeModule != null) {
				module = (AttributesModuleImplApi) attributeModule;
				depList = module.getDependencies();
				strongDepList = module.getStrongDependencies();
				//Fill Set of dependencies
				for(String s: depList) {
					if(!s.endsWith("*")) {
						try {
							AttributeDefinition attrDef = getAttributeDefinition(sess, s);
							depSet.add(attrDef);
						} catch (AttributeNotExistsException ex) {
							log.error("For attribute name " + s + "can't be found attributeDefinition at Inicialization in AttributesManagerBlImpl.");
						}
						//If there is something like AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace" + ":*" we need to replace * by all possibilities
					} else {
						List<String> allVariantOfDependence = getAllSimilarAttributeNames(sess, s.substring(0, s.length()-2));
						for(String variant: allVariantOfDependence) {
							try {
								AttributeDefinition attrDef = getAttributeDefinition(sess, variant);
								depSet.add(attrDef);
							} catch (AttributeNotExistsException ex) {
								log.error("For attribute name " + variant + "can't be found attributeDefinition at Inicialization in AttributesManagerBlImpl.");
							}
						}
					}
				}
				//Fil Set of strongDependencies
				for(String s: strongDepList) {
					if(!s.endsWith("*")) {
						try {
							AttributeDefinition attrDef = getAttributeDefinition(sess, s);
							strongDepSet.add(attrDef);
						} catch (AttributeNotExistsException ex) {
							log.error("For attribute name " + s + "can't be found attributeDefinition at Inicialization in AttributesManagerBlImpl.");
						}
						//If there is something like AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace" + ":*" we need to replace * by all possibilities
					} else {
						List<String> allVariantOfDependence = getAllSimilarAttributeNames(sess, s.substring(0, s.length()-2));
						for(String variant: allVariantOfDependence) {
							try {
								AttributeDefinition attrDef = getAttributeDefinition(sess, variant);
								strongDepSet.add(attrDef);
							} catch (AttributeNotExistsException ex) {
								log.error("For attribute name " + variant + "can't be found attributeDefinition at Inicialization in AttributesManagerBlImpl.");
							}
						}
					}
				}
			}
			dependencies.put(ad, depSet);
			strongDependencies.put(ad, strongDepSet);
		}

		log.debug("Dependencies and StrongDependencies was filled successfully.");

		log.debug("InverseDependencies and InverseStrongDependencies filling started.");

		//First create inversion map for simple dependencies
		Set<AttributeDefinition> depSet = dependencies.keySet();
		for(AttributeDefinition key: depSet) {
			Set<AttributeDefinition> keySet = new HashSet<AttributeDefinition>();
			keySet = dependencies.get(key);
			for(AttributeDefinition keySetItem: keySet) {
				Set<AttributeDefinition> changeSet = new HashSet<AttributeDefinition>();
				changeSet = inverseDependencies.get(keySetItem);
				changeSet.add(key);
				//inverseDependencies.put(keySetItem, changeSet);
			}
		}

		//Second create inversion map for strong dependencies
		depSet = strongDependencies.keySet();
		for(AttributeDefinition key: depSet) {
			Set<AttributeDefinition> keySet = new HashSet<AttributeDefinition>();
			keySet = strongDependencies.get(key);
			for(AttributeDefinition keySetItem: keySet) {
				Set<AttributeDefinition> changeSet = new HashSet<AttributeDefinition>();
				changeSet = inverseStrongDependencies.get(keySetItem);
				changeSet.add(key);
				//inverseDependencies.put(keySetItem, changeSet);
			}
		}

		log.debug("InverseDependencies and InverseStrongDependencies was filled successfully.");

		log.debug("Cycle test of InverseStrongDependencies started.");
		//Test StrDepInveMap on cycles

		if(isMapOfAttributesDefCyclic(inverseStrongDependencies)) {
			log.error("There is cycle in inverseStrongDependencies so map of All attribute will be not created!");
		} else {
			log.debug("Cycle test of InverseStrongDependencies was successfull.");
			log.debug("Filling map of allDependencies started.");

			for(AttributeDefinition key: allDependencies.keySet()) {
				List<AttributeDefinition> stackingAttributes = new ArrayList<AttributeDefinition>();
				Set<AttributeDefinition> dependenciesOfAttribute = new HashSet<AttributeDefinition>();
				dependenciesOfAttribute.addAll(inverseStrongDependencies.get(key));
				dependenciesOfAttribute.addAll(inverseDependencies.get(key));
				stackingAttributes.addAll(inverseStrongDependencies.get(key));
				while(!stackingAttributes.isEmpty()) {
					AttributeDefinition firstAttr = stackingAttributes.get(0);
					stackingAttributes.remove(firstAttr);
					dependenciesOfAttribute.addAll(inverseStrongDependencies.get(firstAttr));
					dependenciesOfAttribute.addAll(inverseDependencies.get(firstAttr));
					stackingAttributes.addAll(inverseStrongDependencies.get(firstAttr));
				}
				allDependencies.put(key, dependenciesOfAttribute);
			}

			log.debug("Map of allDependencies was filled successfully.");
		}

		//DEBUG creating file with all dependencies of all attributes (180+- on devel)
		/*String pathToFile = "./AllDependencies.log";
			File f = new File(pathToFile);
			try {
			f.createNewFile();
			PrintWriter writer;
			writer = new PrintWriter(new FileWriter(f, true));
			int i=1;
			for(AttributeDefinition ad: allDependencies.keySet()) {
			writer.println(i + ") " + ad.toString());
			for(AttributeDefinition a: allDependencies.get(ad)) {
			writer.println(" ---> " + a);
			}
			i++;
			}
			writer.close();
			} catch (IOException ex) {
			log.error("Error at saving AllDependencies file.");
			}*/
		//DEBUG end

		log.debug("AttributesManagerBlImpl initialize ended.");
	}

	/**
	 * This method try to find cycle between strongDependencies of Attributes modules.
	 * If exist at least 1 cycle, return true.
	 * If there is no cycle, return false.
	 *
	 * @param map
	 * @return true if cycle exist, false if cycle not exist
	 */
	private static boolean isMapOfAttributesDefCyclic(Map<AttributeDefinition, Set<AttributeDefinition>> map) {
		Set<AttributeDefinition> processed = new HashSet<AttributeDefinition>();
		Set<AttributeDefinition> unprocessed = new HashSet<AttributeDefinition>();
		List<AttributeDefinition> stack = new ArrayList<AttributeDefinition>();

		for(AttributeDefinition attributeDef: map.keySet()) {
			stack.add(attributeDef);
			while(!stack.isEmpty()) {
				AttributeDefinition firstInStack = stack.get(0);
				if(map.get(firstInStack).contains(attributeDef)) {
					log.error("Cycle exist for " + attributeDef);
					return true;
				}
				processed.add(firstInStack);
				unprocessed.addAll(map.get(firstInStack));
				unprocessed.removeAll(processed);
				stack.remove(firstInStack);
				for(AttributeDefinition unprocessedAttr: unprocessed) {
					if(!stack.contains(unprocessedAttr)) stack.add(unprocessedAttr);
				}
				unprocessed.clear();
			}
		}
		return false;
	}

	public List<Attribute> setWritableTrue(PerunSession sess, List<Attribute> attributes) throws InternalErrorException {
		List<Attribute> emptyList = new ArrayList<Attribute>();
		if(attributes == null) return emptyList;

		for(Attribute a: attributes) {
			if(a != null) a.setWritable(true);
		}

		return attributes;
	}

	@Override
	public List<AttributeRights> getAttributeRights(PerunSession sess, int attributeId) throws InternalErrorException {
		List<AttributeRights> listOfAr = getAttributesManagerImpl().getAttributeRights(sess, attributeId);

		//Do not return VoObsever rights by this method
		if(listOfAr != null) {
			Iterator<AttributeRights> iterator = listOfAr.iterator();
			while(iterator.hasNext()) {
				AttributeRights ar = iterator.next();
				if(ar.getRole().equals(Role.VOOBSERVER)) iterator.remove();
			}
		}

		return listOfAr;
	}

	@Override
	public void setAttributeRights(PerunSession sess, List<AttributeRights> rights) throws InternalErrorException {
		for (AttributeRights right : rights) {
			getAttributesManagerImpl().setAttributeRight(sess, right);
			getPerunBl().getAuditer().log(sess, "Attribute right set : {}", right);

			//If these rights are for VoAdmin, do the same for VoObserver but only for READ privilegies
			if(right.getRole().equals(Role.VOADMIN)) {
				List<ActionType> onlyReadActionType = new ArrayList<ActionType>();
				if(right.getRights().contains(ActionType.READ)) onlyReadActionType.add(ActionType.READ);
				right.setRights(onlyReadActionType);
				right.setRole(Role.VOOBSERVER);
				//Rights are now set for VoObserver with read privilegies on the same attribute like VoAdmin
				getAttributesManagerImpl().setAttributeRight(sess, right);
				getPerunBl().getAuditer().log(sess, "Attribute right set : {}", right);
			}
		}
	}

	public UserVirtualAttributesModuleImplApi getUserVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute) throws ModuleNotExistsException, WrongModuleTypeException, InternalErrorException {
		return getAttributesManagerImpl().getUserVirtualAttributeModule(sess, attribute);
	}

	/**
	 * Check if member is assigned on resource. If not, throw MemberResourceMismatchException Exception
	 *
	 * @param sess
	 * @param member
	 * @param resource
	 * @throws InternalErrorException
	 * @throws MemberResourceMismatchException
	 */
	private void checkMemberIsFromTheSameVoLikeResource(PerunSession sess, Member member, Resource resource) throws MemberResourceMismatchException, InternalErrorException {
		Utils.notNull(sess, "sess");
		Utils.notNull(member, "member");
		Utils.notNull(resource, "resource");

		if(member.getVoId() != resource.getVoId()) throw new MemberResourceMismatchException("Member is not from the same vo like Resource: " + member + " " + resource);
	}

	/**
	 * Check if group is assigned on resource. If not, throw WrongAttributeAssignment Exception
	 *
	 * @param sess
	 * @param group
	 * @param resource
	 * @throws WrongAttributeAssignmentException
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @throws ResourceNotExistsException
	 */
	public void checkGroupIsFromTheSameVoLikeResource(PerunSession sess, Group group, Resource resource) throws GroupResourceMismatchException, InternalErrorException {
		Utils.notNull(sess, "sess");
		Utils.notNull(group, "group");
		Utils.notNull(resource, "resource");

		if(group.getVoId() != resource.getVoId()) throw new GroupResourceMismatchException("Group is not from the same vo like Resource: " + group + " " + resource);
	}

	public Map<AttributeDefinition, Set<AttributeDefinition>> getAllDependencies() {
		return allDependencies;
	}

	/**
	 * Get Map of members with list of member-resource attributes in values.
	 *
	 * This method calls 'getRequiredAttributes(session, service, resource, List members)' for
	 * every MAX_SIZE_OF_BULL_IN_SQL records (default 10000 records) in list of members.
	 *
	 * Example: if there are 25000 records in list of members, this method call it by 3 separate
	 * queries instead of 1, if less or equal to 10000 records are in list of members, than this method
	 * calls getRequiredAttributes just once without any changes
	 *
	 * Reason: SQL error in Oracle for too much records in one SQL query
	 *
	 * @param sess perunSession
	 * @param service service to get required attributes for
	 * @param resource resource to get required attributes for
	 * @param members members to get required attributes for
	 * @return map of members in keys with list of their member-resource attributes in value
	 * @throws InternalErrorException
	 */
	private HashMap<Member, List<Attribute>> getRequiredAttributesForBulk(PerunSession sess, Service service, Resource resource, List<Member> members) throws InternalErrorException {
		if(members.size() <= MAX_SIZE_OF_BULK_IN_SQL) return getAttributesManagerImpl().getRequiredAttributes(sess, service, resource, members);

		HashMap<Member, List<Attribute>> memberResourceAttrs = new HashMap<>();

		int from = 0;
		int to = MAX_SIZE_OF_BULK_IN_SQL;
		do {
			memberResourceAttrs.putAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, resource, members.subList(from, to)));
			from+=MAX_SIZE_OF_BULK_IN_SQL;
			to+=MAX_SIZE_OF_BULK_IN_SQL;
		} while (members.size()>to);
		memberResourceAttrs.putAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, resource, members.subList(from, members.size())));

		return memberResourceAttrs;
	}

	/**
	 * Get Map of members with list of member attributes in values.
	 *
	 * This method calls 'getRequiredAttributes(session, resource, service, List members)' for
	 * every MAX_SIZE_OF_BULL_IN_SQL records (default 10000 records) in list of members.
	 *
	 * Example: if there are 25000 records in list of members, this method call it by 3 separate
	 * queries instead of 1, if less or equal to 10000 records are in list of members, than this method
	 * calls getRequiredAttributes just once without any changes
	 *
	 * Reason: SQL error in Oracle for too much records in one SQL query
	 *
	 * @param sess perunSession
	 * @param resource resource to get required attributes for
	 * @param service service to get required attributes for
	 * @param members members to get required attributes for
	 * @return map of members in keys with list of their member attributes in value
	 * @throws InternalErrorException
	 */
	private HashMap<Member, List<Attribute>> getRequiredAttributesForBulk(PerunSession sess, Resource resource, Service service, List<Member> members) throws InternalErrorException {
		if(members.size() <= MAX_SIZE_OF_BULK_IN_SQL) return getAttributesManagerImpl().getRequiredAttributes(sess, resource, service, members);

		HashMap<Member, List<Attribute>> memberAttrs = new HashMap<>();

		int from = 0;
		int to = MAX_SIZE_OF_BULK_IN_SQL;
		do {
			memberAttrs.putAll(getAttributesManagerImpl().getRequiredAttributes(sess, resource, service, members.subList(from, to)));
			from+=MAX_SIZE_OF_BULK_IN_SQL;
			to+=MAX_SIZE_OF_BULK_IN_SQL;
		} while (members.size()>to);
		memberAttrs.putAll(getAttributesManagerImpl().getRequiredAttributes(sess, resource, service, members.subList(from, members.size())));

		return memberAttrs;
	}

	/**
	 * Get Map of users with list of user-facility attributes in values.
	 *
	 * This method calls 'getRequiredAttributes(session, service, facility, List users)' for
	 * every MAX_SIZE_OF_BULL_IN_SQL records (default 10000 records) in list of users.
	 *
	 * Example: if there are 25000 records in list of users, this method call it by 3 separate
	 * queries instead of 1, if less or equal to 10000 records are in list of users, than this method
	 * calls getRequiredAttributes just once without any changes
	 *
	 * Reason: SQL error in Oracle for too much records in one SQL query
	 *
	 * @param sess perunSession
	 * @param service service to get required attributes for
	 * @param facility facility to get required attributes for
	 * @param users users to get required attributes for
	 * @return map of users in keys with list of their user-facility attributes in value
	 * @throws InternalErrorException
	 */
	private HashMap<User, List<Attribute>> getRequiredAttributesForBulk(PerunSession sess, Service service, Facility facility, List<User> users) throws InternalErrorException {
		if(users.size() <= MAX_SIZE_OF_BULK_IN_SQL) return getAttributesManagerImpl().getRequiredAttributes(sess, service, facility, users);

		HashMap<User, List<Attribute>> userFacAttrs = new HashMap<>();

		int from = 0;
		int to = MAX_SIZE_OF_BULK_IN_SQL;
		do {
			userFacAttrs.putAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, facility, users.subList(from, to)));
			from+=MAX_SIZE_OF_BULK_IN_SQL;
			to+=MAX_SIZE_OF_BULK_IN_SQL;
		} while (users.size()>to);
		userFacAttrs.putAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, facility, users.subList(from, users.size())));

		return userFacAttrs;
	}

	/**
	 * Get Map of users with list of user attributes in values.
	 *
	 * This method calls 'getRequiredAttributes(session, service, List users)' for
	 * every MAX_SIZE_OF_BULL_IN_SQL records (default 10000 records) in list of users.
	 *
	 * Example: if there are 25000 records in list of users, this method call it by 3 separate
	 * queries instead of 1, if less or equal to 10000 records are in list of users, than this method
	 * calls getRequiredAttributes just once without any changes
	 *
	 * Reason: SQL error in Oracle for too much records in one SQL query
	 *
	 * @param sess perunSession
	 * @param service service to get required attributes for
	 * @param users users to get required attributes for
	 * @return map of users in keys with list of their user attributes in value
	 * @throws InternalErrorException
	 */
	private HashMap<User, List<Attribute>> getRequiredAttributesForBulk(PerunSession sess, Service service, List<User> users) throws InternalErrorException {
		if(users.size() <= MAX_SIZE_OF_BULK_IN_SQL) return getAttributesManagerImpl().getRequiredAttributes(sess, service, users);

		HashMap<User, List<Attribute>> userAttrs = new HashMap<>();

		int from = 0;
		int to = MAX_SIZE_OF_BULK_IN_SQL;
		do {
			userAttrs.putAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, users.subList(from, to)));
			from+=MAX_SIZE_OF_BULK_IN_SQL;
			to+=MAX_SIZE_OF_BULK_IN_SQL;
		} while (users.size()>to);
		userAttrs.putAll(getAttributesManagerImpl().getRequiredAttributes(sess, service, users.subList(from, users.size())));

		return userAttrs;
	}

	// ------------ PRIVATE METHODS FOR ATTRIBUTE DEPENDENCIES LOGIC --------------
	// These methods get one or two Perun Beans and return list of richAttributes
	// of specific type defined by name of method which actually exists in Perun
	// and they are connected to the Perun Beans in parameters. If there is any
	// possibility to filter them by members, use only allowed members connections.
	// If member is not allowed (is in state Disabled or Invalid), remove
	// all objects connected to him from structure for getting attributes.
	//
	// Example: We have USER and we want all GROUP attributes connected to this
	// User. So we find all members connected to this user and only for those who
	// are allowed (NOT have status DISABLED or INVALID we find all connected groups
	// and then we find all group attributes for these groups and return them
	// as RichAttributes.
	// ----------------------------------------------------------------------------

	// ---------------------------------VO-----------------------------------------

	/**
	 * Returns all relevant Vo RichAttributes for given User.
	 * That means find all Vos where given user has allowed member.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of Rich attributes for given user
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getVoAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for (Member memberElement : membersFromUser) {
			listOfRichAttributes.addAll(getVoAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Vo RichAttributes for given member.
	 * If member is allowed returns its Vo's Rich attribute.
	 * Otherwise no attributes are returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getVoAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);
			listOfRichAttributes.addAll(getVoAttributes(sess, vo, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Vo RichAttributes for given group.
	 * Finds directly the Vo where the group belongs.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getVoAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Vo vo = getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId());
		listOfRichAttributes.addAll(getVoAttributes(sess, vo, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Vo RichAttributes for given resource.
	 * Finds the Vo that the resource is assigned to.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return list of RichAttributes
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getVoAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, VoNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Vo vo = getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId());
		listOfRichAttributes.addAll(getVoAttributes(sess, vo, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Vo RichAttributes for given Vo.
	 * Find attributes for given Vo directly.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo virtual organization
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getVoAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, vo, attrDef.getName());
		listOfRichAttributes.add(new RichAttribute<>(vo, null, attribute));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Vo RichAttributes for given Facility.
	 * Finds allowed Vos for given facility.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getVoAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Vo> vos = getPerunBl().getFacilitiesManagerBl().getAllowedVos(sess, facility);
		for (Vo voElement : vos) {
			listOfRichAttributes.addAll(getVoAttributes(sess, voElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Vo RichAttributes for given host.
	 * Finds facility for given host and gets allowed Vos for the facility.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAtrributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getVoAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getVoAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Vo RichAttributes for given userExtSource. That means find user for given userExtSource and
	 * find all Vos where given user has allowed member.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource user external source
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws VoNotExistsException
	 */
	private List<RichAttribute> getVoAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, VoNotExistsException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		listOfRichAttributes.addAll(getVoAttributes(sess, user, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Vo RichAttributes for given user and facility.
	 * That means:
	 * 1. Finds all members for given user. For each member, if is allowed, find all its groups.
	 * 2. For given facility find all its resource. For each resource finds assigned groups.
	 * 3. Then compare these groups and keeps only those that were in both lists.
	 * 4. For those groups finds theirs Vos and returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getVoAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Group> groupsFromUser = new ArrayList<>();
		List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for (Member memberElement : membersFromUser) {
			if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
				groupsFromUser.addAll(getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, memberElement));
			}
		}
		List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		List<Group> groupsFromFacility = new ArrayList<>();
		for (Resource resourceElement : resourcesFromFacility) {
			groupsFromFacility.addAll(perunBl.getResourcesManagerBl().getAssignedGroups(sess, resourceElement));
		}
		groupsFromUser.retainAll(groupsFromFacility);
		groupsFromUser = new ArrayList<>(new HashSet<>(groupsFromUser));

		List<Vo> vos = new ArrayList<>();
		for (Group groupElement : groupsFromUser) {
			vos.add(getPerunBl().getGroupsManagerBl().getVo(sess, groupElement));
		}

		vos = new ArrayList<>(new HashSet<>(vos));

		for (Vo voElement : vos) {
			listOfRichAttributes.addAll(getVoAttributes(sess, voElement, attrDef));
		}

		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all Vo RichAttributes.
	 * Finds attributes for all Vos. The given parameter key is not used.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key not used
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getVoAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Vo> vos = getPerunBl().getVosManagerBl().getVos(sess);
		for (Vo voElement : vos) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, voElement, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(voElement, null, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------------------------USER----------------------------------------

	/**
	 * Returns all relevant User RichAttributes for given user.
	 * Finds attributes for given user directly.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, attrDef.getName());
		listOfRichAttributes.add(new RichAttribute<>(user, null, attribute));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant User RichAttributes for given member.
	 * Checks if given member is allowed. If so, gets its user and return its attribute.
	 * If member is not allowed, an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			listOfRichAttributes.addAll(getUserAttributes(sess, user, attrDef));
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant User RichAttributes for given member.
	 * Finds all members for given group. For each allowed member gets its user. Returns attributes for those users.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> members = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		for (Member memberElement : members) {
			listOfRichAttributes.addAll(getUserAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant User RichAttributes for given resource.
	 * Gets allowed users for given resource. Returns attributes for those users.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<User> usersFromResource = getPerunBl().getResourcesManagerBl().getAllowedUsers(sess, resource);
		for (User userElement : usersFromResource) {
			listOfRichAttributes.addAll(getUserAttributes(sess, userElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant User RichAttributes for given vo.
	 * Find all members of given vo. For each allowed member gets its user and returns rich attributes for those users.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembers(sess, vo);
		for (Member memberElement : members) {
			listOfRichAttributes.addAll(getUserAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant User RichAttributes for given facility.
	 * Finds allowed users for given facility. For each of them returns rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<User> usersFromFacility = getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
		for (User userElement : usersFromFacility) {
			listOfRichAttributes.addAll(getUserAttributes(sess, userElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant User RichAttributes for given host.
	 * For given host finds its facility. Then for this facility finds allowed users and returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getUserAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant User RichAttributes for given userExtSource.
	 * For given userExtSource finds its user. Then for this user gets its attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource user external source
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		listOfRichAttributes.addAll(getUserAttributes(sess, user, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant User RichAttributes for given user and facility.
	 * That means:
	 * 1. Finds all allowed facilities for given user.
	 * 2. Checks if those facilities contains given facility.
	 * 3. If so, returns the user's rich attribute, an empty list otherwise.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Facility> facilitiesFromUser = getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, user);
		if (facilitiesFromUser.contains(facility)) {
			listOfRichAttributes.addAll(getUserAttributes(sess, user, attrDef));
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns RichAttributes for all users.
	 * Finds all users. Returns theirs rich attributes. The key param is not used.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key not used
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<User> allUsers = getPerunBl().getUsersManagerBl().getUsers(sess);
		for (User userElement : allUsers) {
			listOfRichAttributes.addAll(getUserAttributes(sess, userElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------------------------MEMBER--------------------------------------

	/**
	 * Returns all relevant Member RichAttributes for given user.
	 * Finds all members for given user. For those members that are allowed returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for (Member memberElement : membersFromUser) {
			listOfRichAttributes.addAll(getMemberAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Member RichAttributes for given member.
	 * Checks if given member is allowed. If so, returns its attribute.
	 * Otherwise an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, member, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(member, null, attribute));
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Member RichAttributes for given group.
	 * Finds all members for given group. For each member that is allowed returns its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromGroup = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		for (Member memberElement : membersFromGroup) {
			listOfRichAttributes.addAll(getMemberAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Member RichAttributes for given resource.
	 * Finds allowed members who can access the resource.
	 * For those members that are allowed returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromResource = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
		for (Member memberElement : membersFromResource) {
			listOfRichAttributes.addAll(getMemberAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Member RichAttributes for given Vo.
	 * Gets all members of given Vo. For each allowed member returns its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo virtual organization
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromVo = getPerunBl().getMembersManagerBl().getMembers(sess, vo);
		for (Member memberElement : membersFromVo) {
			listOfRichAttributes.addAll(getMemberAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Member RichAttributes for given facility.
	 * Finds members that can access the given facility. For each allowed member returns its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromFacility = getPerunBl().getFacilitiesManagerBl().getAllowedMembers(sess, facility);
		for (Member memberElement : membersFromFacility) {
			listOfRichAttributes.addAll(getMemberAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Member RichAttributes for given host.
	 * Finds facility for given host. Finds all members who can access the facility.
	 * For each allowed member returns its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getMemberAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Member RichAttributes for given userExtSource.
	 * For given userExtSource finds user. Finds all members for the user.
	 * For each allowed member returns its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource user external source
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		listOfRichAttributes.addAll(getMemberAttributes(sess, user, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Member RichAttributes for given user and facility.
	 * That means:
	 * 1. Finds members for given user.
	 * 2. Finds members for given facility.
	 * 3. Keeps those that are in both lists.
	 * 4. For each of them return its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		List<Member> membersFromFacility = getPerunBl().getFacilitiesManagerBl().getAllowedMembers(sess, facility);
		membersFromUser.retainAll(membersFromFacility);
		membersFromUser = new ArrayList<>(new HashSet<>(membersFromUser));
		for (Member memberElement : membersFromUser) {
			listOfRichAttributes.addAll(getMemberAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns RichAttributes for all allowed members.
	 * Key argument is not used.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session.
	 * @param key is not used.
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Vo> vos = getPerunBl().getVosManagerBl().getVos(sess);
		for (Vo voElement : vos) {
			listOfRichAttributes.addAll(getMemberAttributes(sess, voElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------------------------GROUP---------------------------------------

	/**
	 * Returns all relevant Group RichAttributes for given user.
	 * Finds members for given user. For each allowed member returns its group's rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for (Member memberElement : members) {
			listOfRichAttributes.addAll(getGroupAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Group RichAttributes for given member.
	 * Checks if given member is allowed. If so, finds all groups for it. For each group returns its rich attribute.
	 * If member is not allowed return an empty List.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Group> groupsFromMember = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
			for (Group groupElement : groupsFromMember) {
				listOfRichAttributes.addAll(getGroupAttributes(sess, groupElement, attrDef));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Group RichAttributes for given group.
	 * Finds rich attribute for given group directly.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, attrDef.getName());
		listOfRichAttributes.add(new RichAttribute<>(group, null, attribute));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Group RichAttributes for given resource.
	 * Finds groups assigned for given resource. Returns rich attributes for those groups.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Group> groupsFromResource = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
		for (Group groupElement : groupsFromResource) {
			listOfRichAttributes.addAll(getGroupAttributes(sess, groupElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Group RichAttributes for given Vo.
	 * Finds groups that belongs in given Vo. For each group return its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo virtual organization
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Group> groups = getPerunBl().getGroupsManagerBl().getAllGroups(sess, vo);
		for (Group groupElement : groups) {
			listOfRichAttributes.addAll(getGroupAttributes(sess, groupElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Group RichAttributes for given facility.
	 * Finds groups that are assigned to given facility. For each group returns its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Group> groupsFromFacility = getPerunBl().getGroupsManagerBl().getAssignedGroupsToFacility(sess, facility);
		for (Group groupElement : groupsFromFacility) {
			listOfRichAttributes.addAll(getGroupAttributes(sess, groupElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Group RichAttributes for given host.
	 * Finds facility for given host. Finds groups assigned to this host. For each group returns its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getGroupAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Group RichAttributes for given userExtSource.
	 * Finds user for given userExtSource. Finds members for this user.
	 * For each allowed member returns its group's rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource user external source
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		listOfRichAttributes.addAll(getGroupAttributes(sess, user, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Group RichAttributes for given member and group.
	 * Checks if the member is allowed. If so, returns rich attribute for given group.
	 * Otherwise an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, Member member, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			listOfRichAttributes = getGroupAttributes(sess, group, attrDef);
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Group RichAttributes for given member and resource.
	 * That means:
	 * 1. Check if member is allowed, if is not, returns an empty list.
	 * 2. Finds assigned groups for given resource.
	 * 3. Finds all groups for given member.
	 * 4. Keeps those groups that are in both lists.
	 * 5. Returns rich attributes for those groups.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, Member member, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Group> groupsFromResource = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
			List<Group> groupsFromMember = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
			groupsFromResource.retainAll(groupsFromMember);
			groupsFromResource = new ArrayList<>(new HashSet<>(groupsFromResource));
			for (Group groupElement : groupsFromResource) {
				listOfRichAttributes.addAll(getGroupAttributes(sess, groupElement, attrDef));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Group RichAttributes for given user and facility.
	 * That means:
	 * 1. Finds all members for given user.
	 * 2. For each member that is allowed, find its group.
	 * 3. Finds group assigned to given facility.
	 * 4. Keeps groups that are in both list.
	 * 5. For those groups returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		Set<Group> groupsFromMembers = new HashSet<>();
		for (Member memberElement : members) {
			if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
				groupsFromMembers.addAll(getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, memberElement));
			}
		}
		groupsFromMembers.retainAll(getPerunBl().getGroupsManagerBl().getAssignedGroupsToFacility(sess, facility));
		for (Group groupElement : groupsFromMembers) {
			listOfRichAttributes.addAll(getGroupAttributes(sess, groupElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns rich attributes for all groups.
	 * The key param is not used.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key key
	 * @param attrDef type of attribute that will be returned
	 * @return list of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getGroupAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Vo> vos = getPerunBl().getVosManagerBl().getVos(sess);
		for (Vo voElement : vos) {
			listOfRichAttributes.addAll(getGroupAttributes(sess, voElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------------------------RESOURCE------------------------------------
	/**
	 * Returns all relevant Resource RichAttributes for given user.
	 * Finds members for given user. For each allowed member finds resources the member can access.
	 * For each of those resources returns theirs rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return list of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for (Member memberElement : members) {
			listOfRichAttributes.addAll(getResourceAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Resource RichAttributes for given member.
	 * If member is not allowed returns an empty list.
	 * Otherwise finds resources the member can access.
	 * Then returns rich attributes for those resources.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return list of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Resource> resourcesFromMember = getPerunBl().getResourcesManagerBl().getAllowedResources(sess, member);
			for (Resource resourceElement : resourcesFromMember) {
				listOfRichAttributes.addAll(getResourceAttributes(sess, resourceElement, attrDef));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Resource RichAttributes for given group.
	 * Finds assigned resources for given group. For those resources returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return list of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resourcesFromGroup = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		for (Resource resourceElement : resourcesFromGroup) {
			listOfRichAttributes.addAll(getResourceAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Resource RichAttributes for given resource.
	 * Finds rich attribute for the given resource.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess sesion
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, attrDef.getName());
		listOfRichAttributes.add(new RichAttribute<>(resource, null, attribute));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Resource RichAttributes for given vo.
	 * Finds resources assigned to given Vo. For each returns its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo virtual organization
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resourcesFromVo = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
		for (Resource resourceElement : resourcesFromVo) {
			listOfRichAttributes.addAll(getResourceAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Resource RichAttributes for given facility.
	 * Finds assigned resources to the given facility. Returns rich attributes of these resources.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		for (Resource resourceElement : resourcesFromFacility) {
			listOfRichAttributes.addAll(getResourceAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Resource RichAttributes for given host.
	 * Finds the given host's facility. For this facility finds assigned resources.
	 * For each of those resources returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getResourceAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Resource RichAttributes for given userExtSource.
	 * Finds user for given userExtSource. Finds members for the user.
	 * For each allowed member finds resources the member can access.
	 * For each of those resources returns theirs rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource user external source
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		listOfRichAttributes.addAll(getResourceAttributes(sess, user, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Resource RichAttributes for given member and group.
	 * 1. Checks if member is allowed, if is not returns an empty list.
	 * 2. Otherwise finds assigned resources to given group.
	 * 3. For those resources returns theirs rich attributes
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, Member member, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Resource> resourcesFromGroup = new ArrayList<>(new HashSet<>(getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group)));
			for (Resource resourceElement : resourcesFromGroup) {
				listOfRichAttributes.addAll(getResourceAttributes(sess, resourceElement, attrDef));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Resource RichAttributes for given member and resource.
	 * 1. Checks if member is allowed, if not so, an empty list is returned
	 * 2. Otherwise returns rich attribute for given resource
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, Member member, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(resource, null, attribute));
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Resource RichAttributes for given user and facility.
	 * 1. Finds members for given user
	 * 2. For each allowed member finds its allowed resources
	 * 3. Finds assigned resources to given facility
	 * 4. Keeps only resources that are in both lists
	 * 5. For those resources returns theirs rich attributes
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		List<Resource> resourcesFromUser = new ArrayList<>();
		for (Member memberElement : members) {
			if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
				resourcesFromUser.addAll(getPerunBl().getResourcesManagerBl().getAllowedResources(sess, memberElement));
			}
		}
		List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		resourcesFromUser.retainAll(resourcesFromFacility);
		resourcesFromUser = new ArrayList<>(new HashSet<>(resourcesFromUser));
		for (Resource resourceElement : resourcesFromUser) {
			listOfRichAttributes.addAll(getResourceAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Finds rich attributes for all resources.
	 * The key param is not used.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key not used
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getResourceAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Vo> vos = getPerunBl().getVosManagerBl().getVos(sess);
		for (Vo voElement : vos) {
			listOfRichAttributes.addAll(getResourceAttributes(sess, voElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------------------------FACILITY------------------------------------

	/**
	 * Returns all relevant Facility RichAttributes for given user.
	 * Finds facilities that the given user can access.
	 * For those facilities are returned rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, user);
		for (Facility facilityElement : facilities) {
			listOfRichAttributes.addAll(getFacilityAttributes(sess, facilityElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Facility RichAttributes for given member.
	 * 1. Checks if given member is allowed, if not, an empty list is returned.
	 * 2. Otherwise finds groups for given member.
	 * 3. For those groups finds assigned resources.
	 * 4. Finds facility for each resource.
	 * 5. Returns rich attributes for those facilities.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Group> groupsForMember = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
			for (Group groupElement : groupsForMember) {
				listOfRichAttributes.addAll(getFacilityAttributes(sess, groupElement, attrDef));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Facility RichAttributes for given group.
	 * 1. For the given group finds assigned resources.
	 * 2. Finds facility for each resource.
	 * 3. Returns rich attributes for those facilities.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		for (Resource resourceElement : resources) {
			listOfRichAttributes.addAll(getFacilityAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Facility RichAttributes for given resource.
	 * Finds facility that the resource belongs to and return its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		listOfRichAttributes.addAll(getFacilityAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Facility RichAttributes for given vo.
	 * Finds resources assigned to given vo.
	 * For each resource finds facility it belongs to.
	 * Returns rich attributes for these resources.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo virtual organization
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
		for (Resource resourceElement : resources) {
			listOfRichAttributes.addAll(getFacilityAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Facility RichAttributes for given Facility.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, attrDef.getName());
		listOfRichAttributes.add(new RichAttribute<>(facility, null, attribute));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Facility RichAttributes for given host.
	 * Finds the facility the given host is assigned to.
	 * For this facility returns its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getFacilityAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Facility RichAttributes for given user.
	 * Finds user for given userExtSource.
	 * Finds facilities that the user can access.
	 * For those facilities are returned rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource user external source
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws UserNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		listOfRichAttributes.addAll(getFacilityAttributes(sess, user, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Facility RichAttributes for given member and group.
	 * 1. Checks if the member is allowed, if not, empty list is returned
	 * 2. Otherwise for the given group finds assigned resources.
	 * 3. Finds facility for each resource.
	 * 4. Returns rich attributes for those facilities.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, Member member, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			listOfRichAttributes = getFacilityAttributes(sess, group, attrDef);
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Facility RichAttributes for given member and resource.
	 * 1. Checks if the given member is allowed. If not, an empty list is returned.
	 * 2. Otherwise finds facility that the resource belongs to and returns its rich attribute.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, Member member, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			listOfRichAttributes.addAll(getFacilityAttributes(sess, resource, attrDef));
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Facility RichAttributes for given user and facility
	 * 1. Finds facilities the given user can access.
	 * 2. Checks if those facilities contains the given one. If they do, return this facility's rich attribute.
	 * 3. Otherwise an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Facility> facilitiesFromUser = getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, user);
		if (facilitiesFromUser.contains(facility)) {
			listOfRichAttributes.addAll(getFacilityAttributes(sess, facility, attrDef));
		}
		return listOfRichAttributes;
	}

	/**
	 * Finds all facilities rich attributes.
	 * The key param is not used.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key not used
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getFacilityAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getFacilities(sess);
		for (Facility facilityElement : facilities) {
			listOfRichAttributes.addAll(getFacilityAttributes(sess, facilityElement, attrDef));
		}
		return listOfRichAttributes;
	}

	// --------------------------------HOST----------------------------------------

	/**
	 * Returns all relevant Host RichAttributes for given user.
	 * Finds facilities where the given user is allowed.
	 * For each of those facilities finds theirs hosts.
	 * For each of those host returns its rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, user);
		for (Facility facilityElement : facilities) {
			listOfRichAttributes.addAll(getHostAttributes(sess, facilityElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Host RichAttributes for given member.
	 * If member is not allowed returns an empty list.
	 * Otherwise finds groups for the given member.
	 * For those groups finds assigned resources.
	 * For each of those resources finds theirs facilities.
	 * For each of those facilities finds theirs hosts.
	 * For each of those host returns its rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(perunBl.getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Group> groupsForMember = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
			for (Group groupElement : groupsForMember) {
				listOfRichAttributes.addAll(getHostAttributes(sess, groupElement, attrDef));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Host RichAttributes for given group.
	 * For the given group finds assigned resources.
	 * For each of those resources finds theirs facilities.
	 * For each of those facilities finds theirs hosts.
	 * For each of those host returns its rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		for (Resource resourceElement : resources) {
			listOfRichAttributes.addAll(getHostAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Host RichAttributes for given resource.
	 * For the given resource finds its facility.
	 * For the facility finds its hosts.
	 * For each of those host returns its rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resources
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		listOfRichAttributes.addAll(getHostAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Host RichAttributes for given vo.
	 * Finds assigned resources to the given vo.
	 * For each of those resources finds theirs facilities.
	 * For each of those facilities finds theirs hosts.
	 * For each of those host returns its rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo virtual organization
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
		for (Resource resourceElement : resources) {
			listOfRichAttributes.addAll(getHostAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Host RichAttributes for given facility.
	 * For the given facility finds its hosts.
	 * For each of those host returns its rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Host> hostsFromFacility = getPerunBl().getFacilitiesManagerBl().getHosts(sess, facility);
		for (Host hostElement : hostsFromFacility) {
			listOfRichAttributes.addAll(getHostAttributes(sess, hostElement, attrDef));
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Host RichAttributes for given host.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, host, attrDef.getName());
		listOfRichAttributes.add(new RichAttribute<>(host, null, attribute));
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Host RichAttributes for given userExtSource.
	 * Finds user for the given userExtSource.
	 * Finds facilities where the user is allowed.
	 * For each of those facilities finds theirs hosts.
	 * For each of those host returns its rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource userExtSource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws UserNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		listOfRichAttributes.addAll(getHostAttributes(sess, user, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Host RichAttributes for given member and group.
	 * If the member is not allowed returns an empty list.
	 * Otherwise, for the given group finds assigned resources.
	 * For each of those resources finds theirs facilities.
	 * For each of those facilities finds theirs hosts.
	 * For each of those host returns its rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, Member member, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			listOfRichAttributes.addAll(getHostAttributes(sess, group, attrDef));
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Host RichAttributes for given member and resource.
	 * For the given resource finds its facility.
	 * For the facility finds its hosts.
	 * For each of those host returns its rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resources
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, Member member, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			listOfRichAttributes = getHostAttributes(sess, resource, attrDef);
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant Host RichAttributes for given user and facility.
	 * Checks if user can access the facility. If not, an empty list is returned.
	 * Otherwise, gets hosts from the given facility and returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, user).contains(facility)) {
			List<Host> hostsFromFacility = getPerunBl().getFacilitiesManagerBl().getHosts(sess, facility);
			for (Host hostElement : hostsFromFacility) {
				listOfRichAttributes.addAll(getHostAttributes(sess, hostElement, attrDef));
			}
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns rich attributes for all hosts.
	 * The key param is not used.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key not used
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getHostAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getFacilities(sess);
		for (Facility facilityElement : facilities) {
			listOfRichAttributes.addAll(getHostAttributes(sess, facilityElement, attrDef));
		}
		return listOfRichAttributes;
	}

	// --------------------------------USER-EXT-SOURCE-------------------------------

	/**
	 * Returns all relevant UserExtSource RichAttributes for given user.
	 * Finds all user's userExtSources and returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserExtSourceAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<UserExtSource> userExtSources = getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
		for(UserExtSource userExtSourceElement : userExtSources) {
			listOfRichAttributes.addAll(getUserExtSourceAttributes(sess, userExtSourceElement, attrDef));
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserExtSource RichAttributes for given member.
	 * Checks if the given member is allowed. If not, an empty list is returned.
	 * Otherwise, finds all its user's userExtSources and returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserExtSourceAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			listOfRichAttributes.addAll(getUserExtSourceAttributes(sess, user, attrDef));
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserExtSource RichAttributes for given group.
	 * Finds all members for given group.
	 * For all allowed members finds theirs user's userExtSources and returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserExtSourceAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> groupMembers = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		for(Member memberElement: groupMembers) {
			listOfRichAttributes.addAll(getUserExtSourceAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserExtSource RichAttributes for given resource.
	 * Finds allowed members for the given resource.
	 * Finds theirs user's userExtSources and returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserExtSourceAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> resourceMembers = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
		for(Member memberElement: resourceMembers) {
			listOfRichAttributes.addAll(getUserExtSourceAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserExtSource RichAttributes for given vo.
	 * Finds allowed members for the given vo.
	 * Finds theirs user's userExtSources and returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserExtSourceAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> voMembers = getPerunBl().getMembersManagerBl().getMembers(sess, vo);
		for(Member memberElement: voMembers) {
			listOfRichAttributes.addAll(getUserExtSourceAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserExtSource RichAttributes for given facility.
	 * Finds allowed members for the given facility.
	 * Finds theirs user's userExtSources and returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserExtSourceAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> facilityMembers = getPerunBl().getFacilitiesManagerBl().getAllowedMembers(sess, facility);
		for(Member memberElement: facilityMembers) {
			listOfRichAttributes.addAll(getUserExtSourceAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserExtSource RichAttributes for given host.
	 * Finds allowed members for the given host's facility.
	 * Finds theirs user's userExtSources and returns theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserExtSourceAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getUserExtSourceAttributes(sess, facility, attrDef));
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserExtSource RichAttributes for given userExtSource.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource userExtSource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserExtSourceAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, userExtSource, attrDef.getName());
		listOfRichAttributes.add(new RichAttribute<>(userExtSource, null, attribute));
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserExtSource RichAttributes for given user and facility.
	 * Checks if the given user can access the given facility. If not, an empty list is returned.
	 * Otherwise, finds all given user's userExtSources and return theirs rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserExtSourceAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<User> usersFromFacility = getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
		if(usersFromFacility.contains(user)) {
			List<UserExtSource> userExtSources = getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
			for(UserExtSource userExtSourceElement: userExtSources) {
				listOfRichAttributes.addAll(getUserExtSourceAttributes(sess, userExtSourceElement, attrDef));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns RichAttributes for all userExtSource.
	 * The key param is not used.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key not used
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserExtSourceAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getFacilities(sess);
		for(Facility facilityElement: facilities) {
			listOfRichAttributes.addAll(getUserExtSourceAttributes(sess, facilityElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------------------------GROUP-RESOURCE-------------------------------

	/**
	 * Returns all relevant GroupResource RichAttributes for given user.
	 * That means, returns all GroupResource rich attributes for those groups where the given user has at least one
	 * allowed member and for those resources that the given user can access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for(Member memberElement: members) {
			listOfRichAttributes.addAll(getGroupResourceAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given member.
	 * That means, returns all GroupResource rich attributes for those groups that the given member is assigned to
	 * and for those resources that the given member can access.
	 * If the given member is not allowed, an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Group> groupsFromMember = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
			List<Resource> resourcesFromMember = getPerunBl().getResourcesManagerBl().getAllowedResources(sess, member);
			for(Resource resourceElement: resourcesFromMember) {
				List<Group> groupsFromResourceElement = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resourceElement);
				groupsFromResourceElement.retainAll(groupsFromMember);
				groupsFromResourceElement = new ArrayList<>(new HashSet<>(groupsFromResourceElement));
				for(Group groupElement: groupsFromResourceElement) {
					Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceElement, groupElement, attrDef.getName());
					listOfRichAttributes.add(new RichAttribute<>(resourceElement, groupElement, attribute));
				}
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given group.
	 * That means, returns all GroupResource rich attributes for the given group and resources that the given group
	 * can access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resourcesFromGroup = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		for(Resource resourceElement: resourcesFromGroup) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceElement, group, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(resourceElement, group, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given resource.
	 * That means, returns all GroupResource rich attributes for the given resource and those groups that can access
	 * this resource.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Group> groupsFromResource = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
		for(Group groupElement: groupsFromResource) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, groupElement, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(resource, groupElement, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given vo.
	 * That means, returns all GroupResource rich attributes for those resources, that can be accessed by the given vo.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
		for(Resource resourceElement: resources) {
			listOfRichAttributes.addAll(getGroupResourceAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given facility.
	 * That means, returns all GroupResource rich attributes for those resources, that are located on the given facility.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		for(Resource resourceElement: resourcesFromFacility) {
			listOfRichAttributes.addAll(getGroupResourceAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given facility.
	 * That means, returns all GroupResource rich attributes for those resources, that are located on the facility
	 * with the given host.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getGroupResourceAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given userExtSource.
	 * That means, returns all GroupResource rich attributes for groups, where user with given userExtSource has
	 * at least one allowed member and for those resources, that can the user access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource userExtSource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		listOfRichAttributes.addAll(getGroupResourceAttributes(sess, user, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given group and resource.
	 * That means, returns all GroupResource rich attributes for the given group and resource.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, Group group, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, group, attrDef.getName());
		listOfRichAttributes.add(new RichAttribute<>(resource, group, attribute));
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given group and member.
	 * That means, returns all GroupResource rich attributes for the given group and those resources, that can the
	 * given member access. If the given member is not allowed, an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, Member member, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		// there is no need to get Resources from Member because Members are only in those groups from which we already took Resources
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			listOfRichAttributes = getGroupResourceAttributes(sess, group, attrDef);
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given resource and member.
	 * That means, returns all GroupResource rich attributes for the given resource and those groups, that can the
	 * given member access and that can access the given resource.
	 * If the given member is not allowed, an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, Member member, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Group> groupsFromResource = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
			List<Group> groupsFromMember = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
			groupsFromResource.retainAll(groupsFromMember);
			groupsFromResource = new ArrayList<>(new HashSet<>(groupsFromResource));
			for (Group groupElement : groupsFromResource) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, groupElement, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(resource, groupElement, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant GroupResource RichAttributes for given user and facility.
	 * That means, returns all GroupResource rich attributes for those groups, where has the given user at least
	 * one allowed member, and for those resources, that can be accessed via this groups and are located on the
	 * given facility.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		//Groups from User
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		List<Group> groupsFromUser = new ArrayList<>();
		for (Member memberElement : members) {
			if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
				groupsFromUser.addAll(getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, memberElement));
			}
		}
		//Retain of Groups from facility
		List<Group> groupsFromFacility = getPerunBl().getGroupsManagerBl().getAssignedGroupsToFacility(sess, facility);
		groupsFromFacility.retainAll(groupsFromUser);
		//Resources from user
		List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		List<Resource> resourcesFromUser = new ArrayList<>();
		for (Member memberElement : membersFromUser) {
			if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
				resourcesFromUser.addAll(getPerunBl().getResourcesManagerBl().getAllowedResources(sess, memberElement));
			}
		}
		//Resource from facility
		List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		//Retain of Resources
		resourcesFromFacility.retainAll(resourcesFromUser);
		//All possibilities
		resourcesFromFacility = new ArrayList<>(new HashSet<>(resourcesFromFacility));
		for (Resource resourceElement : resourcesFromFacility) {
			List<Group> groupsForResourceElement = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resourceElement);
			groupsForResourceElement.retainAll(groupsFromFacility);
			groupsForResourceElement = new ArrayList<>(new HashSet<>(groupsForResourceElement));
			for (Group groupElement : groupsForResourceElement) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceElement, groupElement, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(resourceElement, groupElement, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all GroupResource RichAttributes.
	 * That means, returns all GroupResource rich attributes with all groups and those resources, that can be accessed
	 * via these groups.
	 * The key param is not used.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key key
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws GroupResourceMismatchException
	 */
	private List<RichAttribute> getGroupResourceAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, GroupResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getFacilities(sess);
		List<Resource> resources = new ArrayList<>();
		for(Facility facilityElement: facilities) {
			resources.addAll(getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facilityElement));
		}
		resources = new ArrayList<>(new HashSet<>(resources));
		for(Resource resourceElement: resources) {
			List<Group> groupsFromResourceElement = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resourceElement);
			for(Group groupElement: groupsFromResourceElement) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceElement, groupElement, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(resourceElement, groupElement, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------------------------MEMBER-GROUP-------------------------------

	/**
	 * Returns all relevant MemberGroup RichAttributes for given user.
	 * That means, returns all MemberGroup rich attributes for those members, who belong to the given user and are allowed,
	 * and for those groups that those members can access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for (Member memberElement: membersFromUser) {
			listOfRichAttributes.addAll(getMemberGroupAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberGroup RichAttributes for given member.
	 * That means, returns all MemberGroup rich attributes for the given member, and for groups that can this member access.
	 * If the given member is not allowed, an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Group> groupsFromMembers = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
			for(Group groupElement : groupsFromMembers) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, member, groupElement, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(member, groupElement, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberGroup RichAttributes for given group.
	 * That means, returns all MemberGroup rich attributes for the given group and those members who belong to this group
	 * and are allowed.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromGroups = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		for (Member memberElement: membersFromGroups) {
			if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, memberElement, group, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(memberElement, group, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberGroup RichAttributes for given resource.
	 * That means, returns all MemberGroup rich attributes for groups that can access the given resource and for members
	 * that can access those groups and are allowed.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Group> groupsFromResources = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
		for (Group groupElement : groupsFromResources) {
			listOfRichAttributes.addAll(getMemberGroupAttributes(sess, groupElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberGroup RichAttributes for given vo.
	 * That means, returns all MemberGroup rich attributes for groups from the given vo and for those members, who
	 * belong to these groups and are allowed.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Group> groupsFromVo = getPerunBl().getGroupsManagerBl().getAllGroups(sess, vo);
		for (Group groupElement : groupsFromVo) {
			listOfRichAttributes.addAll(getMemberGroupAttributes(sess, groupElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberGroup RichAttributes for given facility.
	 * That means, returns all MemberGroup rich attributes for groups that can access the given facility and for members
	 * that can access these groups and are allowed.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Group> groupsFromFacility = getPerunBl().getGroupsManagerBl().getAssignedGroupsToFacility(sess, facility);
		for (Group groupElement : groupsFromFacility) {
			listOfRichAttributes.addAll(getMemberGroupAttributes(sess, groupElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberGroup RichAttributes for given host.
	 * That means, returns all MemberGroup rich attributes for groups that can access the given host's facility and
	 * for members that can access these groups and are allowed.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getMemberGroupAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberGroup RichAttributes for given userExtSource.
	 * That means, returns all MemberGroup rich attributes for those members, who belong to the given userExtSource's user
	 * and are allowed, and for those groups that those members can access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource userExtSource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws UserNotExistsException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		listOfRichAttributes.addAll(getMemberGroupAttributes(sess, user, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberGroup RichAttributes for given member and group.
	 * That means, returns all MemberGroup rich attributes for the given member and group.
	 * If the member is not allowed, an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, Member member, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, member, group, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(member, group, attribute));
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberGroup RichAttributes for given member and resource.
	 * That means, returns all MemberGroup rich attributes for the given member and groups that can access the given resource.
	 * If the member is not allowed, an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, Member member, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		//If member is not allowed, skip whole process
		if(!getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) return listOfRichAttributes;
		List<Group> groupFromMembers = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			groupFromMembers = getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member);
		}
		List<Group> groupsFromResources = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
		groupsFromResources.retainAll(groupFromMembers);
		groupsFromResources = new ArrayList<>(new HashSet<>(groupsFromResources));
		for (Group groupElement : groupsFromResources) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, member, groupElement, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(member, groupElement, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberGroup RichAttributes for given user and facility.
	 * That means, returns all MemberGroup rich attributes for members that belong to the given user and are allowed
	 * and those groups that can access the given facility.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		// get all groups from facility
		List<Group> groupsFromFacility = getPerunBl().getGroupsManagerBl().getAssignedGroupsToFacility(sess, facility);
		// get all groups from user
		List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		Set<Group> groupsFromMembers = new HashSet<>();
		for (Member memberElement : membersFromUser) {
			groupsFromMembers.addAll(getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, memberElement));
		}
		// retain of groups
		groupsFromMembers.retainAll(groupsFromFacility);
		List<Group> retainedGroups = new ArrayList<>(groupsFromMembers);
		// all possible groups
		for (Group groupElement : retainedGroups) {
			// get all members for 'groupElement' variable
			List<Member> membersFromGroup = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, groupElement);
			membersFromGroup.retainAll(membersFromUser);
			// all possibilities
			for (Member memberElement : membersFromGroup) {
				if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
					Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, memberElement, groupElement, attrDef.getName());
					listOfRichAttributes.add(new RichAttribute<>(memberElement, groupElement, attribute));
				}
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all MemberGroup RichAttributes.
	 * That means, returns all MemberGroup rich attributes for all members who are allowed and all groups that can those
	 * members access.
	 * The key param is not used.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key not used
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getMemberGroupAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Vo> vos = getPerunBl().getVosManagerBl().getVos(sess);
		List<Group> groupsFromVo = new ArrayList<>();
		for(Vo voElement : vos) {
			groupsFromVo.addAll(getPerunBl().getGroupsManagerBl().getAllGroups(sess, voElement));
		}
		groupsFromVo = new ArrayList<>(new HashSet<>(groupsFromVo));
		for (Group groupElement : groupsFromVo) {
			// get all members for 'groupElement' variable
			List<Member> membersFromGroup = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, groupElement);
			for (Member memberElement : membersFromGroup) {
				if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
					Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, memberElement, groupElement, attrDef.getName());
					listOfRichAttributes.add(new RichAttribute<>(memberElement, groupElement, attribute));
				}
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------------------------MEMBER-RESOURCE-------------------------------

	/**
	 * Returns all relevant MemberResource RichAttributes for given user.
	 * That means, returns all MemberResource rich attributes for those members, who belong to the given user and are allowed,
	 * and for those resources that can those members access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for(Member memberElement: members) {
			listOfRichAttributes.addAll(getMemberResourceAttributes(sess, memberElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given member.
	 * That means, returns all MemberResource rich attributes for the given member and those resources that can those
	 * members access.
	 * If the given member is not allowed, an empty list is returned.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getAllowedResources(sess, member);
			for(Resource resourceElement: resources) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceElement, member, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(resourceElement, member, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given group.
	 * That means, returns all MemberResource rich attributes for allowed members from the given group and for the resources
	 * that can those members access via this group.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resourcesFromGroup = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		List<Member> membersFromGroup = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		for(Resource resourceElement: resourcesFromGroup) {
			List<Member> membersForResourceElement = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resourceElement);
			membersForResourceElement.retainAll(membersFromGroup);
			membersForResourceElement = new ArrayList<>(new HashSet<>(membersForResourceElement));
			for(Member memberElement: membersForResourceElement) {
				if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
					Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceElement, memberElement, attrDef.getName());
					listOfRichAttributes.add(new RichAttribute<>(resourceElement, memberElement, attribute));
				}
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given resource.
	 * That means, returns all MemberResource rich attributes for the given resource and for those members that can
	 * access the given resource and are allowed.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> members = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
		for(Member memberElement: members) {
			if (getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, memberElement, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(resource, memberElement, attribute));
			}
		}
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given vo.
	 * That means, returns all MemberResource rich attributes for resources that belongs to the given vo and for members
	 * that can access those resource and are allowed.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
		for(Resource resourceElement: resources) {
			listOfRichAttributes.addAll(getMemberResourceAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given facility.
	 * That means, returns all MemberResource rich attributes for resources that belongs to the given facility and for
	 * members who can access those resources and are allowed.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Resource> resources = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		for(Resource resourceElement: resources) {
			listOfRichAttributes.addAll(getMemberResourceAttributes(sess, resourceElement, attrDef));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given host.
	 * That means, returns all MemberResource rich attributes for resources that belongs to the given host's facility
	 * and for members who can access those resources and are allowed.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getMemberResourceAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given userExtSource.
	 * That means, returns all MemberResource rich attributes for members of given userExtSource's user that are allowed
	 * and for those resources that those members can access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource userExtSource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 * @throws UserNotExistsException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		listOfRichAttributes.addAll(getMemberResourceAttributes(sess, user, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given group and resource.
	 * That means, returns all MemberResource rich attributes for the given resource and groups that can access this
	 * resource via theirs allowed users.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, Group group, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromGroup = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		List<Member> membersFromResource = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
		membersFromGroup.retainAll(membersFromResource);
		membersFromGroup = new ArrayList<>(new HashSet<>(membersFromGroup));
		for(Member memberElement: membersFromGroup) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, memberElement, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(resource, memberElement, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given group and member.
	 * That means, returns all MemberResource rich attributes for the given member and resources that can this member
	 * access via the given group.
	 * If the given member is not allowed, returns an empty list.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, Member member, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		//we can take everything from group, because member should be assigned to this group already
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Resource> resourcesFromGroup = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
			for (Resource resourceElement : resourcesFromGroup) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceElement, member, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(resourceElement, member, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given resource and member.
	 * That means, returns all MemberResource rich attributes for the given member and resource.
	 * If the given member is not allowed, returns an empty list.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, Member member, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resource, member, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(resource, member, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant MemberResource RichAttributes for given user and facility.
	 * That means, returns all MemberResource rich attributes for the given user's members who are allowed and for
	 * resources that can those members access and are located on the given facility.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> membersFromUser = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		List<Member> membersFromFacility = getPerunBl().getFacilitiesManagerBl().getAllowedMembers(sess, facility);
		membersFromUser.retainAll(membersFromFacility);
		List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		List<Resource> resourcesFromUser =getPerunBl().getUsersManagerBl().getAllowedResources(sess, user);
		resourcesFromUser.retainAll(resourcesFromFacility);
		resourcesFromUser = new ArrayList<>(new HashSet<>(resourcesFromUser));
		for(Resource resourceElement: resourcesFromUser) {
			List<Member> membersForResourceElement = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resourceElement);
			membersForResourceElement.retainAll(membersFromUser);
			membersForResourceElement = new ArrayList<>(new HashSet<>(membersForResourceElement));
			for(Member memberElement: membersForResourceElement) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceElement, memberElement, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(resourceElement, memberElement, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all MemberResource RichAttributes.
	 * That means, returns all MemberResource RichAttributes for all members who are allowed and all resources they
	 * can access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key key
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberResourceMismatchException
	 */
	private List<RichAttribute> getMemberResourceAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, MemberResourceMismatchException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Vo> vos = getPerunBl().getVosManagerBl().getVos(sess);
		List<Resource> resources = new ArrayList<>();
		for(Vo voElement: vos) {
			resources.addAll(getPerunBl().getResourcesManagerBl().getResources(sess, voElement));
		}
		resources = new ArrayList<>(new HashSet<>(resources));
		for(Resource resourceElement: resources) {
			List<Member> membersFromResource = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resourceElement);
			for(Member memberElement: membersFromResource) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, resourceElement, memberElement, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(resourceElement, memberElement, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------------------------USER-FACILITY-------------------------------

	/**
	 * Returns all relevant UserFacility RichAttributes for given user.
	 * That means, returns all UserFacility rich attributes for the given user and for facilities it can access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, User user, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, user);
		for(Facility facilityElement: facilities) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facilityElement, user, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(facilityElement, user, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserFacility RichAttributes for given member.
	 * That means, returns all UserFacility rich attributes for the facilities the given member can access and for its user.
	 * If the given member is not allowed, returns an empty list
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, Member member, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			List<Resource> memberResources = getPerunBl().getResourcesManagerBl().getAllowedResources(sess, member);
			List<Facility> facilities = new ArrayList<>();
			for(Resource resourceElement: memberResources) {
				facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElement));
			}
			facilities = new ArrayList<>(new HashSet<>(facilities));
			List<Facility> userAllowedFacilities = getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, user);
			for(Facility facilityElement: facilities) {
				if(userAllowedFacilities.contains(facilityElement)) {
					Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facilityElement, user, attrDef.getName());
					listOfRichAttributes.add(new RichAttribute<>(facilityElement, user, attribute));
				}
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserFacility RichAttributes for given group.
	 * That means, returns all UserFacility rich attributes for the facilities that can access members from the given
	 * group and that are allowed and for users from these members.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Member> members = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		List<User> users = new ArrayList<>();
		for(Member memberElement: members) {
			if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
				users.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
			}
		}
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		List<Facility> facilities = new ArrayList<>();
		for(Resource resourceElement: resources) {
			facilities.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElement));
		}
		users = new ArrayList<>(new HashSet<>(users));
		for(User userElement: users) {
			List<Facility> facilitiesFromUser = getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, userElement);
			facilities.retainAll(facilitiesFromUser);
			facilities = new ArrayList<>(new HashSet<>(facilities));
			for(Facility facilityElement: facilities) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facilityElement, userElement, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(facilityElement, userElement, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserFacility RichAttributes for given resource.
	 * That means, returns all UserFacility rich attributes for the facility from the given resource and for members
	 * that can access this resource and are allowed.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		List<User> usersFromResource = getPerunBl().getResourcesManagerBl().getAllowedUsers(sess, resource);
		for(User userElement: usersFromResource) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, userElement, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(facility, userElement, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserFacility RichAttributes for given vo.
	 * That means, returns all UserFacility rich attributes for the users who has at least one allowed member in the
	 * given Vo and for the facilities that can those users access via a group in this Vo.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, Vo vo, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Group> groupsFromVo = getPerunBl().getGroupsManagerBl().getGroups(sess, vo);
		for (Group groupElement : groupsFromVo) {
			List<Facility> groupFacilities = getPerunBl().getFacilitiesManagerBl().getAssignedFacilities(sess, groupElement);
			List<Member> groupMembers = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, groupElement);
			List<Member> allowedMembers = new ArrayList<>();
			for(Member memberElement : groupMembers) {
				if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
					allowedMembers.add(memberElement);
				}
			}
			List<User> groupUsers = new ArrayList<>();
			for (Member memberElement : allowedMembers) {
				groupUsers.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
			}
			for (Facility facilityElement : groupFacilities) {
				for (User userElement : groupUsers) {
					Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facilityElement, userElement, attrDef.getName());
					listOfRichAttributes.add(new RichAttribute<>(facilityElement, userElement, attribute));
				}
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserFacility RichAttributes for given facility.
	 * That means, returns all UserFacility rich attributes for the given facility and users that can access it.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<User> users = getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
		for(User userElement: users) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, userElement, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(facility, userElement, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserFacility RichAttributes for given host.
	 * That means, returns all UserFacility rich attributes for the given host's facility and users that can access it.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param host host
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, Host host, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		listOfRichAttributes.addAll(getUserFacilityAttributes(sess, facility, attrDef));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserFacility RichAttributes for given userExtSource.
	 * That means, returns all UserFacility rich attributes for the given userExtSource's user and for facilities it can access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param userExtSource userExtSource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws UserNotExistsException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, UserExtSource userExtSource, AttributeDefinition attrDef) throws InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		User user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, user);
		for(Facility facilityElement: facilities) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facilityElement, user, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(facilityElement, user, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserFacility RichAttributes for given group and resource.
	 * That means, returns all UserFacility rich attributes for users who has at least one allowed member in the given group
	 * and for facilities that those users can access via the given group and via the given resource.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param group group
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, Group group, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		//get Users from Group
		List<Member> members = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		List<User> usersFromGroup = new ArrayList<>();
		for(Member memberElement: members) {
			if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
				usersFromGroup.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
			}
		}
		//get users from resource
		List<Member> membersFromResource = getPerunBl().getResourcesManagerBl().getAllowedMembers(sess, resource);
		List<User> usersFromResource = new ArrayList<>();
		for(Member memberElement: membersFromResource) {
			if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, memberElement)) {
				usersFromResource.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, memberElement));
			}
		}
		usersFromGroup.retainAll(usersFromResource);
		usersFromGroup = new ArrayList<>(new HashSet<>(usersFromGroup));
		for(User userElement: usersFromGroup) {
			if(getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, userElement).contains(facility)) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, userElement, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(facility, userElement, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserFacility RichAttributes for given group and member.
	 * That means, returns all UserFacility rich attributes for user of the given member and for facilities this member
	 * can access via the given group.
	 * If the given member is not allowed, returns an empty list.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, Member member, Group group, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			List<Resource> resourcesFromGroup = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
			List<Facility> facilitiesFromResources = new ArrayList<>();
			for (Resource resourceElement : resourcesFromGroup) {
				facilitiesFromResources.add(getPerunBl().getResourcesManagerBl().getFacility(sess, resourceElement));
			}
			facilitiesFromResources = new ArrayList<>(new HashSet<>(facilitiesFromResources));
			User userFromMember = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			for (Facility facilityElement : facilitiesFromResources) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facilityElement, userFromMember, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(facilityElement, userFromMember, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all relevant UserFacility RichAttributes for given resource and member.
	 * That means, returns all UserFacility rich attributes for user of the given member and for facilities this member
	 * can access via the given resource.
	 * If the given member is not allowed, returns an empty list.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param member member
	 * @param resource resource
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, Member member, Resource resource, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		if(getPerunBl().getMembersManagerBl().isMemberAllowed(sess, member)) {
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
			List<Resource> memberResources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, member);
			if(memberResources.contains(resource)) {
				User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(facility, user, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns UserFacility RichAttributes for the given user and facility.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param user user
	 * @param facility facility
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, User user, Facility facility, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facility, user, attrDef.getName());
		listOfRichAttributes.add(new RichAttribute<>(facility, user, attribute));
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all UserFacility RichAttributes.
	 * That means, returns all UserFacility rich attributes for all users and all facilities these users can access.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key not used
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getUserFacilityAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getFacilities(sess);
		for (Facility facilityElement : facilities) {
			List<User> users = getPerunBl().getFacilitiesManagerBl().getAllowedUsers(sess, facilityElement);
			for (User userElement : users) {
				Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, facilityElement, userElement, attrDef.getName());
				listOfRichAttributes.add(new RichAttribute<>(facilityElement, userElement, attribute));
			}
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------------------------ENTITYLESS---------------------------------

	/**
	 * Returns all entityless rich attributes.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getEntitylessAttributes(PerunSession sess, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		List<String> keys = this.getEntitylessKeys(sess, attrDef);
		for (String keyElement : keys) {
			Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, keyElement, attrDef.getName());
			listOfRichAttributes.add(new RichAttribute<>(keyElement, null, attribute));
		}
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	/**
	 * Returns all entityless rich attributes for the given key.
	 * Each rich attribute is returned only once.
	 *
	 * @param sess session
	 * @param key key
	 * @param attrDef type of attribute that will be returned
	 * @return List of RichAttribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private List<RichAttribute> getEntitylessAttributes(PerunSession sess, String key, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<RichAttribute> listOfRichAttributes = new ArrayList<>();
		Attribute attribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, key, attrDef.getName());
		listOfRichAttributes.add(new RichAttribute<>(key, null, attribute));
		listOfRichAttributes = new ArrayList<>(new HashSet<>(listOfRichAttributes));
		return listOfRichAttributes;
	}

	// --------------END OF METHODS FOR ATTRIBUTES DEPENDENCIES-------------------
}
