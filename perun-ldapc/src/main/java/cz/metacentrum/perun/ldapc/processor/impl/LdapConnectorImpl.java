package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.*;
import java.util.List;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.beans.LdapProperties;
import cz.metacentrum.perun.ldapc.processor.LdapConnector;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;

@org.springframework.stereotype.Service(value = "ldapConnector")
public class LdapConnectorImpl implements LdapConnector {

	private final static Logger log = LoggerFactory.getLogger(LdapConnectorImpl.class);

	@Autowired
	private LdapTemplate ldapTemplate;
	@Autowired
	private LdapProperties ldapProperties;
	private String ldapAttributeName = null;

	private Pattern userIdPattern = Pattern.compile("[0-9]+");

	public LdapConnectorImpl() {
	}

	//------------------RESOURCE MODIFICATION METHODS----------------------------

	public void createResource(Resource resource, String entityID) throws InternalErrorException {
		// Create a set of attributes
		Attributes attributes = new BasicAttributes();

		// Create the objectclass to add
		Attribute objClasses = new BasicAttribute("objectClass");
		objClasses.add("top");
		objClasses.add("perunResource");

		// Add attributes
		attributes.put(objClasses);
		attributes.put("cn", resource.getName());
		attributes.put("perunResourceId", String.valueOf(resource.getId()));
		attributes.put("perunFacilityId", String.valueOf(resource.getFacilityId()));
		attributes.put("perunVoId", String.valueOf(resource.getVoId()));
		if(resource.getDescription() != null && !resource.getDescription().isEmpty()) attributes.put("description", resource.getDescription());

		// get info about entityID attribute if exists
		if(entityID != null) attributes.put("entityID", entityID);

		// Create the entry
		try {
			ldapTemplate.bind(getResourceDN(String.valueOf(resource.getVoId()), String.valueOf(resource.getId())), null, attributes);
			log.debug("New entry created in LDAP: Resource {} in Vo with Id=" + resource.getVoId() + " and Facility with ID=" + resource.getFacilityId() + ".", resource);
		} catch (NameNotFoundException e) {
			throw new InternalErrorException(e);
		}
	}

	public void deleteResource(Resource resource) throws InternalErrorException {
		try {
			ldapTemplate.unbind(getResourceDN(String.valueOf(resource.getVoId()), String.valueOf(resource.getId())));
			log.debug("Entry deleted from LDAP: Resource {} from Vo with ID=" + resource.getVoId() + " and Facility with ID=" + resource.getFacilityId() + ".", resource);
		} catch (NameNotFoundException e) {
			throw new InternalErrorException(e);
		}
	}

	public void updateResource(Resource resource, ModificationItem[] modificationItems) {
		ldapTemplate.modifyAttributes(getResourceDN(String.valueOf(resource.getVoId()), String.valueOf(resource.getId())), modificationItems);
		log.debug("Entry modified in LDAP: Resource {}.", resource);
	}

	//------------------GROUP MODIFICATION METHODS-------------------------------

	public void addGroup(Group group) throws InternalErrorException {
		// Create a set of attributes
		Attributes attributes = new BasicAttributes();

		// Create the objectclass to add
		Attribute objClasses = new BasicAttribute("objectClass");
		objClasses.add("top");
		objClasses.add("perunGroup");

		// Add attributes
		attributes.put(objClasses);
		attributes.put("cn", group.getName());
		attributes.put("perunGroupId", String.valueOf(group.getId()));
		attributes.put("perunUniqueGroupName", new String(this.getVoShortName(group.getVoId()) + ":" + group.getName()));
		attributes.put("perunVoId", String.valueOf(group.getVoId()));
		if(group.getDescription() != null && !group.getDescription().isEmpty()) attributes.put("description", group.getDescription());
		if(group.getParentGroupId() != null) {
			attributes.put("perunParentGroup", "perunGroupId=" + group.getParentGroupId().toString() + ",perunVoId=" + group.getVoId() + "," + ldapProperties.getLdapBase());
			attributes.put("perunParentGroupId", group.getParentGroupId().toString());
		}

		// Create the entry
		try {
			ldapTemplate.bind(getGroupDN(String.valueOf(group.getVoId()), String.valueOf(group.getId())), null, attributes);
			log.debug("New entry created in LDAP: Group {} in Vo with Id=" + group.getVoId() + ".", group);
		} catch (NameNotFoundException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addGroupAsSubGroup(Group group, Group parentGroup) throws InternalErrorException {
		//This method has the same implemenation like 'addGroup'
		addGroup(group);
	}

	public void removeGroup(Group group) throws InternalErrorException {

		List<String> uniqueUsersIds = new ArrayList<String>();
		uniqueUsersIds = this.getAllUniqueMembersInGroup(group.getId(), group.getVoId());
		for(String s: uniqueUsersIds) {
			Attribute memberOf = new BasicAttribute("memberOf", "perunGroupId=" + group.getId() + ",perunVoId=" + group.getVoId() + "," + ldapProperties.getLdapBase());
			ModificationItem memberOfItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, memberOf);
			this.updateUserWithUserId(s, new ModificationItem[] {memberOfItem});
		}

		try {
			ldapTemplate.unbind(getGroupDN(String.valueOf(group.getVoId()), String.valueOf(group.getId())));
			log.debug("Entry deleted from LDAP: Group {} from Vo with ID=" + group.getVoId() + ".", group);
		} catch (NameNotFoundException e) {
			throw new InternalErrorException(e);
		}
	}

	public void updateGroup(Group group, ModificationItem[] modificationItems) {
		ldapTemplate.modifyAttributes(getGroupDN(String.valueOf(group.getVoId()), String.valueOf(group.getId())), modificationItems);
		log.debug("Entry modified in LDAP: Group {}.", group);
	}

	//-----------------------------MEMBER MODIFICATION METHODS--------------------

	public void addMemberToGroup(Member member, Group group) throws InternalErrorException {
		//Add member to group
		Attribute uniqueMember = new BasicAttribute("uniqueMember", "perunUserId=" + member.getUserId() + ",ou=People," + ldapProperties.getLdapBase());
		ModificationItem uniqueMemberItem = new ModificationItem(DirContext.ADD_ATTRIBUTE, uniqueMember);
		this.updateGroup(group, new ModificationItem[] {uniqueMemberItem});
		//Add member to vo if this group is memebrsGroup
		if(group.getName().equals(VosManager.MEMBERS_GROUP) && group.getParentGroupId() == null) {
			//Add info to vo
			this.updateVo(group.getVoId(), new ModificationItem[] {uniqueMemberItem});
			//Add info also to user
			Attribute memberOfPerunVo = new BasicAttribute("memberOfPerunVo", String.valueOf(group.getVoId()));
			ModificationItem memberOfPerunVoItem = new ModificationItem(DirContext.ADD_ATTRIBUTE, memberOfPerunVo);
			this.updateUserWithUserId(String.valueOf(member.getUserId()), new ModificationItem[] {memberOfPerunVoItem});
		}
		//Add group info to member
		Attribute memberOf = new BasicAttribute("memberOf", "perunGroupId=" + group.getId() + ",perunVoId=" + group.getVoId() + "," + ldapProperties.getLdapBase());
		ModificationItem memberOfItem = new ModificationItem(DirContext.ADD_ATTRIBUTE, memberOf);
		this.updateUserWithUserId(String.valueOf(member.getUserId()), new ModificationItem[] {memberOfItem});
	}

	public void removeMemberFromGroup(Member member, Group group) throws InternalErrorException {
		//Remove member from group
		Attribute uniqueMember = new BasicAttribute("uniqueMember", "perunUserId=" + member.getUserId() + ",ou=People," + ldapProperties.getLdapBase());
		ModificationItem uniqueMemberItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, uniqueMember);
		this.updateGroup(group, new ModificationItem[] {uniqueMemberItem});
		//Remove member from vo if this group is membersGroup
		if(group.getName().equals(VosManager.MEMBERS_GROUP) && group.getParentGroupId() == null) {
			//Remove info from vo
			this.updateVo(group.getVoId(), new ModificationItem[] {uniqueMemberItem});
			//Remove also information from user
			Attribute memberOfPerunVo = new BasicAttribute("memberOfPerunVo", String.valueOf(group.getVoId()));
			ModificationItem memberOfPerunVoItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, memberOfPerunVo);
			this.updateUserWithUserId(String.valueOf(member.getUserId()), new ModificationItem[] {memberOfPerunVoItem});
		}
		//Remove group info from member
		Attribute memberOf = new BasicAttribute("memberOf", "perunGroupId=" + group.getId() + ",perunVoId=" + group.getVoId() + "," + ldapProperties.getLdapBase());
		ModificationItem memberOfItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, memberOf);
		this.updateUserWithUserId(String.valueOf(member.getUserId()), new ModificationItem[] {memberOfItem});
	}

	public boolean isAlreadyMember(Member member, Group group) {
		Object o = ldapTemplate.lookup(getUserDN(String.valueOf(member.getUserId())), new UserMemberOfContextMapper());
		String[] memberOfInformation = (String []) o;
		if(memberOfInformation != null) {
			for(String s: memberOfInformation) {
				if(s.equals("perunGroupId="+group.getId()+",perunVoId="+group.getVoId()+"," + ldapProperties.getLdapBase())) return true;
			}
		}
		return false;
	}

	public List<String> getAllUniqueMembersInGroup(int groupId, int voId) {
		List<String> uniqueMembers = new ArrayList<String>();
		Object o = ldapTemplate.lookup(getGroupDN(String.valueOf(voId), String.valueOf(groupId)), new GroupUniqueMemberOfContextMapper());
		String[] uniqueGroupInformation = (String []) o;
		if(uniqueGroupInformation != null) {
			for(String s: uniqueGroupInformation) {
				Matcher userIdMatcher = userIdPattern.matcher(s);
				if(userIdMatcher.find()) uniqueMembers.add(s.substring(userIdMatcher.start(), userIdMatcher.end()));
			}
		}
		return uniqueMembers;
	}

	//--------------------------VO MODIFICATION METHODS---------------------------

	public void createVo(Vo vo) throws InternalErrorException {
		// Create a set of attributes for vo
		Attributes voAttributes = new BasicAttributes();

		// Create the objectclass to add
		Attribute voObjClasses = new BasicAttribute("objectClass");
		voObjClasses.add("top");
		voObjClasses.add("organization");
		voObjClasses.add("perunVO");

		// Add attributes
		voAttributes.put(voObjClasses);
		voAttributes.put("o", vo.getShortName());
		voAttributes.put("description", vo.getName());
		voAttributes.put("perunVoId", String.valueOf(vo.getId()));

		// Create the entires
		try {
			ldapTemplate.bind(getVoDNByVoId(String.valueOf(vo.getId())), null, voAttributes);
			log.debug("New entry created in LDAP: Vo {}.", vo);
		} catch (NameNotFoundException e) {
			throw new InternalErrorException(e);
		}
	}

	public void deleteVo(Vo vo) throws InternalErrorException {
		try {
			ldapTemplate.unbind(getVoDNByVoId(String.valueOf(vo.getId())));
			log.debug("Entry deleted from LDAP: Vo {}.", vo);
		} catch (NameNotFoundException e) {
			throw new InternalErrorException(e);
		}
	}

	public void updateVo(Vo vo, ModificationItem[] modificationItems) {
		ldapTemplate.modifyAttributes(getVoDNByVoId(String.valueOf(vo.getId())), modificationItems);
		log.debug("Entry modified in LDAP: Vo {}.", vo);
	}

	public void updateVo(int voId, ModificationItem[] modificationItems) {
		ldapTemplate.modifyAttributes(getVoDNByVoId(String.valueOf(voId)), modificationItems);
		log.debug("Entry modified in LDAP: Vo {}.", voId);
	}

	public String getVoShortName(int voId) throws InternalErrorException {
		Object o = ldapTemplate.lookup(getVoDNByVoId(String.valueOf(voId)), new VoShortNameContextMapper());
		String[] voShortNameInformation = (String []) o;
		String voShortName = null;
		if(voShortNameInformation == null || voShortNameInformation[0] == null) throw new InternalErrorException("There is no shortName in ldap for vo with id=" + voId);
		if(voShortNameInformation.length != 1) throw new InternalErrorException("There is not exactly one short name of vo with id=" +  voId + " in ldap. Count of shortnames is " + voShortNameInformation.length);
		voShortName = voShortNameInformation[0];
		return voShortName;
	}

	//-----------------------USER MODIFICATION METHODS----------------------------

	public void createUser(User user) throws InternalErrorException {
		// Create a set of attributes
		Attributes attributes = new BasicAttributes();

		// Create the objectclass to add
		Attribute objClasses = new BasicAttribute("objectClass");
		objClasses.add("top");
		objClasses.add("person");
		objClasses.add("organizationalPerson");
		objClasses.add("inetOrgPerson");
		objClasses.add("perunUser");
		objClasses.add("tenOperEntry");
		objClasses.add("inetUser");

		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		if(firstName == null) firstName = "";
		if(lastName == null || lastName.isEmpty()) lastName = "N/A";

		// Add attributes
		attributes.put(objClasses);
		attributes.put("entryStatus", "active");
		attributes.put("sn", lastName);
		attributes.put("cn", firstName + " " + lastName);
		if(!firstName.isEmpty()) attributes.put("givenName", firstName);
		attributes.put("perunUserId", String.valueOf(user.getId()));
		if(user.isServiceUser()) attributes.put("isServiceUser", "1");
		else attributes.put("isServiceUser", "0");
		if(user.isSponsoredUser()) attributes.put("isSponsoredUser", "1");
		else attributes.put("isSponsoredUser", "0");

		// Create the entry
		try {
			ldapTemplate.bind(getUserDN(String.valueOf(user.getId())), null, attributes);
			log.debug("New entry created in LDAP: User {} in Group with Id=" + user.getId() + ".", user);
		} catch (NameNotFoundException e) {
			throw new InternalErrorException(e);
		}
	}

	public void deleteUser(User user) throws InternalErrorException {
		try {
			ldapTemplate.unbind(getUserDN(String.valueOf(user.getId())));
			log.debug("Entry deleted from LDAP: User {}.", user);
		} catch (NameNotFoundException e) {
			throw new InternalErrorException(e);
		}
	}

	public void updateUser(User user, ModificationItem[] modificationItems) {
		this.updateUserWithUserId(String.valueOf(user.getId()), modificationItems);
	}

	public void updateUsersCertSubjects(String userId, String[] certSubjects) {
		DirContextOperations context = ldapTemplate.lookupContext(getUserDN(userId));
		context.setAttributeValues("userCertificateSubject", certSubjects);
		ldapTemplate.modifyAttributes(context);
		log.debug("Entry modified in LDAP: UserId {}.", userId);
	}

	public void updateUsersSchacHomeOrganizations(String userId, String[] organizations) {
		DirContextOperations context = ldapTemplate.lookupContext(getUserDN(userId));
		context.setAttributeValues("schacHomeOrganizations", organizations);
		ldapTemplate.modifyAttributes(context);
		log.debug("Entry modified in LDAP: UserId {}.", userId);
	}

	public void updateUsersEduPersonScopedAffiliations(String userId, String[] affiliations) {
		DirContextOperations context = ldapTemplate.lookupContext(getUserDN(userId));
		context.setAttributeValues("eduPersonScopedAffiliations", affiliations);
		ldapTemplate.modifyAttributes(context);
		log.debug("Entry modified in LDAP: UserId {}.", userId);
	}

	public void updateUsersLibraryIds(String userId, String[] libraryIDs) {
		DirContextOperations context = ldapTemplate.lookupContext(getUserDN(userId));
		context.setAttributeValues("libraryIDs", libraryIDs);
		ldapTemplate.modifyAttributes(context);
		log.debug("Entry modified in LDAP: UserId {}.", userId);
	}

	public void updateUserWithUserId(String userId, ModificationItem[] modificationItems) {
		ldapTemplate.modifyAttributes(getUserDN(userId), modificationItems);
		log.debug("Entry modified in LDAP: UserId {}.", userId);
	}

	public Attributes getAllUsersAttributes(User user) {
		Object o = ldapTemplate.lookup(getUserDN(String.valueOf(user.getId())), new UserAttributesContextMapper());
		Attributes attrs = null;
		if(o != null) attrs = (Attributes) o;
		return attrs;
	}

	public boolean userExist(User user) {
		Object o = null;
		try {
			o = ldapTemplate.lookup(getUserDN(String.valueOf(user.getId())), new UserPerunUserIdContextMapper());
		} catch (NameNotFoundException ex) {
			return false;
		}
		return true;
	}

	public boolean userAttributeExist(User user, String ldapAttributeName) throws InternalErrorException {
		if(ldapAttributeName == null) throw new InternalErrorException("ldapAttributeName can't be null.");
		Object o = null;
		try {
			setLdapAttributeName(ldapAttributeName);
			o = ldapTemplate.lookup(getUserDN(String.valueOf(user.getId())), new UserPerunUserAttributeContextMapper());
		} catch (NameNotFoundException ex) {
			return false;
		}
		if(o == null) return false;
		return true;
	}

	public boolean resourceAttributeExist(Resource resource, String ldapAttributeName) throws InternalErrorException {
		if(ldapAttributeName == null) throw new InternalErrorException("ldapAttributeName can't be null.");
		Object o = null;
		try {
			setLdapAttributeName(ldapAttributeName);
			o = ldapTemplate.lookup(getResourceDN(String.valueOf(resource.getVoId()), String.valueOf(resource.getId())), new ResourcePerunResourceAttributeContextMapper());
		} catch (NameNotFoundException ex) {
			return false;
		}
		if(o == null) return false;
		return true;
	}

	public boolean userPasswordExists(User user) {
		Object o = ldapTemplate.lookup(getUserDN(String.valueOf(user.getId())), new UserAttributesContextMapper());
		Attributes attrs = null;
		if(o != null) attrs = (Attributes) o;

		if(attrs != null) {
			Attribute a = attrs.get("userPassword");
			if(a != null) return true;
		}
		return false;
	}

	//------------------GETTERS AND SETTERS---------------

	/**
	 * Setter for ldapTemplate. (use autowire from spring)
	 *
	 * @param ldapTemplate
	 */
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	/**
	 * Setter for ldapAttributeName
	 *
	 * @return ldapAttributeName
	 */
	public String getLdapAttributeName() {
		return ldapAttributeName;
	}

	/**
	 * Getter for ldapAttributeName
	 *
	 * @param ldapAttributeName  ldapAttributeName
	 */
	public void setLdapAttributeName(String ldapAttributeName) {
		this.ldapAttributeName = ldapAttributeName;
	}

	//------------------PRIVATE METHODS-------------------

	/**
	 * Get Group DN using VoId and GroupId.
	 *
	 * @param voId vo id
	 * @param groupId group id
	 * @return DN in String
	 */
	private String getGroupDN(String voId, String groupId) {
		return new StringBuffer()
			.append("perunGroupId=")
			.append(groupId)
			.append(",perunVoId=")
			.append(voId)
			.toString();
	}

	/**
	 * Get Resource DN using VoId, FacilityId and ResourceId.
	 *
	 * @param voId vo id
	 * @param resourceId group id
	 * @return DN in String
	 */
	private String getResourceDN(String voId, String resourceId) {
		return new StringBuffer()
			.append("perunResourceId=")
			.append(resourceId)
			.append(",perunVoId=")
			.append(voId)
			.toString();
	}

	/**
	 * Get Vo DN using VoId.
	 *
	 * @param voId vo id
	 * @return DN in String
	 */
	private String getVoDNByVoId(String voId) {
		return new StringBuffer()
			.append("perunVoId=")
			.append(voId)
			.toString();
	}

	/**
	 * Get Vo DN using Vo shortName (o).
	 *
	 * @param voShortName the value of attribute 'o' in ldap
	 * @return DN in String
	 */
	private String getVoDNByShortName(String voShortName) {
		return new StringBuffer()
			.append("o=")
			.append(voShortName)
			.toString();
	}

	/**
	 * Get User DN using user id.
	 *
	 * @param userId user id
	 * @return DN in String
	 */
	private String getUserDN(String userId) {
		return new StringBuffer()
			.append("perunUserId=")
			.append(userId)
			.append(",ou=People")
			.toString();
	}

	/**
	 * User attribute 'memberOf' context Mapper
	 *
	 * Context mapper is used for choosing concrete attribute.
	 */
	private class UserMemberOfContextMapper implements ContextMapper {
		public String[] mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter)ctx;
			String[] s=context.getStringAttributes("memberOf");
			return s;
		}
	}

	/**
	 * All user's attributes context mapper.
	 *
	 * Context mapper is used for choosing concrete attributes.
	 */
	private class UserAttributesContextMapper implements ContextMapper {
		public Attributes mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter)ctx;
			Attributes attrs=context.getAttributes();
			return attrs;
		}
	}

	/**
	 * User attribute 'any' context Mapper (the name of attribute is in variable 'ldapAttributeName'
	 *
	 * Context mapper is used for choosing concrete attribute.
	 */
	private class UserPerunUserAttributeContextMapper implements ContextMapper {
		public String mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter)ctx;
			String s=context.getStringAttribute(getLdapAttributeName());
			return s;
		}
	}

	/**
	 * User attribute 'perunUserId' context Mapper
	 *
	 * Context mapper is used for choosing concrete attribute.
	 */
	private class UserPerunUserIdContextMapper implements ContextMapper {
		public String mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter)ctx;
			String s=context.getStringAttribute("perunUserId");
			return s;
		}
	}

	/**
	 * Vo attribute 'o' (shortName) context Mapper
	 *
	 * Context mapper is used for choosing concrete attribute.
	 */
	private class VoShortNameContextMapper implements ContextMapper {
		public String[] mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter) ctx;
			String[] s = context.getStringAttributes("o");
			return s;
		}
	}

	/**
	 * Group attribute 'uniqueMembeOf' context Mapper
	 *
	 * Context mapper is used for choosing concrete attribute.
	 */
	private class GroupUniqueMemberOfContextMapper implements ContextMapper {
		public String[] mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter)ctx;
			String[] s=context.getStringAttributes("uniqueMember");
			return s;
		}
	}

	/**
	 * Resource attribute 'any' context Mapper (the name of attribute is in variable 'ldapAttributeName'
	 *
	 * Context mapper is used for choosing concrete attribute.
	 */
	private class ResourcePerunResourceAttributeContextMapper implements ContextMapper {
		public String mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter)ctx;
			String s=context.getStringAttribute(getLdapAttributeName());
			return s;
		}
	}
}
