package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.core.api.*;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.LdapName;
import javax.naming.directory.ModificationItem;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.ldapc.processor.LdapConnector;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ldap.InvalidAttributeValueException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapAttribute;
import org.springframework.ldap.core.LdapTemplate;

@org.springframework.stereotype.Service(value = "ldapConnector")
public class LdapConnectorImpl implements LdapConnector {

  private final static Logger log = LoggerFactory.getLogger(LdapConnectorImpl.class);

  @Autowired
  private LdapTemplate ldapTemplate;
  private static String ldapAttributeName;
  
  private Pattern userIdPattern = Pattern.compile("[0-9]+");
  
  public LdapConnectorImpl() {
  }
  
  //------------------GROUP MODIFICATION METHODS-------------------------------

  public void addGroup(Group group) throws InternalErrorException {
    // Create a set of attributes
    Attributes attributes = new BasicAttributes();

    // Create the objectclass to add
    Attribute objClasses = new BasicAttribute("objectClass");
    objClasses.add("top");
    objClasses.add("groupOfUniqueNames");
    objClasses.add("perunGroup");

    // Add attributes
    attributes.put(objClasses);
    attributes.put("cn", group.getName());
    attributes.put("perunGroupId", String.valueOf(group.getId()));
    attributes.put("perunUniqueGroupName", new String(this.getVoShortName(group.getVoId()) + ":" + group.getName()));
    attributes.put("perunVoId", String.valueOf(group.getVoId()));
    if(group.getDescription() != null) attributes.put("description", group.getDescription());
    if(group.getParentGroupId() != null) {
        attributes.put("perunParentGroup", "perunGroupId=" + group.getParentGroupId().toString() + ",perunVoId=" + group.getVoId() + ",dc=perun,dc=cesnet,dc=cz");
        attributes.put("perunParentGroupId", group.getParentGroupId().toString());
    }
    
   //Debug Info
   try {
    log.debug("There is perunUniqueName  fo group " + group + " with value: " + String.valueOf(attributes.get("perunUniqueGroupName").get()));
   } catch (NamingException ex) {
       log.debug("ERROR - naming exception was catched." + ex.getStackTrace());
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
    //TODO THIS METHOD IS NOT IMPLEMENTED CORRECTLY
    addGroup(group);
  }

  public void removeGroup(Group group) throws InternalErrorException {
      
    List<String> uniqueUsersIds = new ArrayList<String>();
    uniqueUsersIds = this.getAllUniqueMembersInGroup(group.getId(), group.getVoId());
    for(String s: uniqueUsersIds) {
        Attribute memberOf = new BasicAttribute("memberOf", "perunGroupId=" + group.getId() + ",perunVoId=" + group.getVoId() + ",dc=perun,dc=cesnet,dc=cz");
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
  
  public void updateGroup(Group group, ModificationItem[] modificationItems) throws InternalErrorException {
      ldapTemplate.modifyAttributes(getGroupDN(String.valueOf(group.getVoId()), String.valueOf(group.getId())), modificationItems);
      log.debug("Entry modified in LDAP: Group {}.", group);
  }
  
  public void addMemberToGroup(Member member, Group group) throws InternalErrorException {
     Attribute uniqueMember = new BasicAttribute("uniqueMember", "perunUserId=" + member.getUserId() + ",ou=People,dc=perun,dc=cesnet,dc=cz");
     ModificationItem uniqueMemberItem = new ModificationItem(DirContext.ADD_ATTRIBUTE, uniqueMember);   
     this.updateGroup(group, new ModificationItem[] {uniqueMemberItem});
     if(group.getName().equals(VosManager.MEMBERS_GROUP) && group.getParentGroupId() == null) this.updateVo(group.getVoId(), new ModificationItem[] {uniqueMemberItem});
     Attribute memberOf = new BasicAttribute("memberOf", "perunGroupId=" + group.getId() + ",perunVoId=" + group.getVoId() + ",dc=perun,dc=cesnet,dc=cz");
     ModificationItem memberOfItem = new ModificationItem(DirContext.ADD_ATTRIBUTE, memberOf);
     this.updateUserWithUserId(String.valueOf(member.getUserId()), new ModificationItem[] {memberOfItem});
  }

  public void removeMemberFromGroup(Member member, Group group) throws InternalErrorException {
    Attribute uniqueMember = new BasicAttribute("uniqueMember", "perunUserId=" + member.getUserId() + ",ou=People,dc=perun,dc=cesnet,dc=cz");
    ModificationItem uniqueMemberItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, uniqueMember);   
    this.updateGroup(group, new ModificationItem[] {uniqueMemberItem});
    if(group.getName().equals(VosManager.MEMBERS_GROUP) && group.getParentGroupId() == null) this.updateVo(group.getVoId(), new ModificationItem[] {uniqueMemberItem});
    Attribute memberOf = new BasicAttribute("memberOf", "perunGroupId=" + group.getId() + ",perunVoId=" + group.getVoId() + ",dc=perun,dc=cesnet,dc=cz");
    ModificationItem memberOfItem = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, memberOf);
    this.updateUserWithUserId(String.valueOf(member.getUserId()), new ModificationItem[] {memberOfItem});
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
  
  public void updateVo(Vo vo, ModificationItem[] modificationItems) throws InternalErrorException {
    ldapTemplate.modifyAttributes(getVoDNByVoId(String.valueOf(vo.getId())), modificationItems);  
    log.debug("Entry modified in LDAP: Vo {}.", vo);
  }
  
  public void updateVo(int voId, ModificationItem[] modificationItems) throws InternalErrorException {
    ldapTemplate.modifyAttributes(getVoDNByVoId(String.valueOf(voId)), modificationItems);  
    log.debug("Entry modified in LDAP: Vo {}.", voId);
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

    // Add attributes
    attributes.put(objClasses);
    attributes.put("entryStatus", "active");
    if(user.getLastName() != null) attributes.put("sn", user.getLastName());
    attributes.put("cn", user.getFirstName() + " " + user.getLastName());
    if(user.getFirstName() != null) attributes.put("givenName", user.getFirstName());
    attributes.put("perunUserId", String.valueOf(user.getId()));
    if(user.isServiceUser()) attributes.put("isServiceUser", "1"); 
    else attributes.put("isServiceUser", "0");

    // Create the entry
    try {
        ldapTemplate.bind(getUserDN(String.valueOf(user.getId())), null, attributes);
        log.debug("New entry created in LDAP: User {} in Group with Id=" + user.getId() + ".", user);
    } catch (NameNotFoundException e) {
        throw new InternalErrorException(e);
    } 
  }
  
  //IMPORTANT
  //We dont need delete member of this user from groups, it will depend on messages removeFrom Group
  //TRY TO TEST IF MESSAGES COME IN SERIALIZE FORM, FIRST MEMBER, LAST USER deleted
  public void deleteUser(User user) throws InternalErrorException {
    try {
        ldapTemplate.unbind(getUserDN(String.valueOf(user.getId()))); 
        log.debug("Entry deleted from LDAP: User {}.", user);
    } catch (NameNotFoundException e) {
        throw new InternalErrorException(e);
    }     
  }
  
  public void updateUser(User user, ModificationItem[] modificationItems) throws InternalErrorException {
     this.updateUserWithUserId(String.valueOf(user.getId()), modificationItems);
  }
  
  public void updateUsersCertSubjects(String userId, String[] certSubjects) throws InternalErrorException {
      DirContextOperations context = ldapTemplate.lookupContext(getUserDN(userId));
      context.setAttributeValues("userCertificateSubject", certSubjects);
      ldapTemplate.modifyAttributes(context);  
      log.debug("Entry modified in LDAP: UserId {}.", userId);
  }
  
  public void updateUserWithUserId(String userId, ModificationItem[] modificationItems) throws InternalErrorException {
     ldapTemplate.modifyAttributes(getUserDN(userId), modificationItems);  
     log.debug("Entry modified in LDAP: UserId {}.", userId);
  }
  
  //----------------------------UTILITY METHODS---------------------------------
  
  public boolean isAlreadyMember(Member member, Group group) throws InternalErrorException {
    Object o = ldapTemplate.lookup(getUserDN(String.valueOf(member.getUserId())), new UserMemberOfContextMapper());
    String[] memberOfInformation = (String []) o;
    if(memberOfInformation != null) {
        for(String s: memberOfInformation) {
            if(s.equals("perunGroupId="+group.getId()+",perunVoId="+group.getVoId()+",dc=perun,dc=cesnet,dc=cz")) return true;
        }
    }
    return false;
  }
  
  public Attributes getAllUsersAttributes(User user) throws InternalErrorException {
    Object o = ldapTemplate.lookup(getUserDN(String.valueOf(user.getId())), new UserAttributesContextMapper());
    Attributes attrs = null;
    if(o != null) attrs = (Attributes) o;
    return attrs;
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
  
  public boolean userExist(User user) throws InternalErrorException {
    Object o = null;
    try {
        o = ldapTemplate.lookup(getUserDN(String.valueOf(user.getId())), new UserPerunUserIdContextMapper());
    } catch (NameNotFoundException ex) {
        return false;
    }
    return true;
  }
  
  public boolean userAttributeExist(User user, String ldapAttributeName) throws InternalErrorException {
    Object o = null;
    try {
        this.ldapAttributeName = ldapAttributeName;
        o = ldapTemplate.lookup(getUserDN(String.valueOf(user.getId())), new UserPerunUserAttributeContextMapper());
    } catch (NameNotFoundException ex) {
        return false;    
    }
    if(o == null) return false;
    return true;
  }
  
  public boolean userPasswordExists(User user) throws InternalErrorException {
    Object o = ldapTemplate.lookup(getUserDN(String.valueOf(user.getId())), new UserAttributesContextMapper());
    Attributes attrs = null;
    if(o != null) attrs = (Attributes) o;
    
    if(attrs != null) {
        Attribute a = attrs.get("userPassword");
        if(a != null) return true;
    }
    return false;
  }
  
  public List<String> getAllUniqueMembersInGroup(int groupId, int voId) throws InternalErrorException {
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
  
  private String getGroupDN(String voId, String groupId) {
    return new StringBuffer()
    .append("perunGroupId=")
    .append(groupId)
    .append(",perunVoId=")
    .append(voId)
    .toString();
  }
  
  private String getVoDNByVoId(String voId) {
    return new StringBuffer()
    .append("perunVoId=")
    .append(voId)
    .toString();  
  }
  
  private String getVoDNByShortName(String voShortName) {
    return new StringBuffer()
    .append("o=")
    .append(voShortName)
    .toString();
  }
  
  private String getUserDN(String userId) {
    return new StringBuffer()
    .append("perunUserId=")
    .append(userId)
    .append(",ou=People")
    .toString();
  }

  public void setLdapTemplate(LdapTemplate ldapTemplate) {
    this.ldapTemplate = ldapTemplate;
  }
  
  private static class UserMemberOfContextMapper implements ContextMapper {
    public String[] mapFromContext(Object ctx) {
        DirContextAdapter context = (DirContextAdapter)ctx;
        String[] s=context.getStringAttributes("memberOf");
        return s;
    }
  }
  
  private static class UserAttributesContextMapper implements ContextMapper {
    public Attributes mapFromContext(Object ctx) {
        DirContextAdapter context = (DirContextAdapter)ctx;
        Attributes attrs=context.getAttributes();
        return attrs;
    }
  }
  
  private static class VoShortNameContextMapper implements ContextMapper {
    public String[] mapFromContext(Object ctx) {
        DirContextAdapter context = (DirContextAdapter) ctx;
        String[] s = context.getStringAttributes("o");
        return s;
    }    
  }
  
  private static class UserPerunUserAttributeContextMapper implements ContextMapper {
    public String mapFromContext(Object ctx) {
      DirContextAdapter context = (DirContextAdapter)ctx;
      String s=context.getStringAttribute(ldapAttributeName);
      return s;
    }
  }
  
  private static class UserPerunUserIdContextMapper implements ContextMapper {
    public String mapFromContext(Object ctx) {
      DirContextAdapter context = (DirContextAdapter)ctx;
      String s=context.getStringAttribute("perunUserId");
      return s;
    }    
  }
  
  private static class GroupUniqueMemberOfContextMapper implements ContextMapper {
    public String[] mapFromContext(Object ctx) {
      DirContextAdapter context = (DirContextAdapter)ctx;
      String[] s=context.getStringAttributes("uniqueMember");
      return s;
    }
  }
}
